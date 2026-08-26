require "Bandit"
require "BanditBrain"
require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFNPCDefinitions"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local PROGRAM_ID = "LCCQFQuestGiver"
local SCAN_RANGE = 8
local UPDATE_INTERVAL_MS = 250
local nextUpdateMs = 0

local function log(message)
    print(C.LOG_PREFIX .. "[QUEST-GIVER-PROTECTION:SERVER] " .. tostring(message))
end

local function isProtectedBrain(brain)
    if type(brain) ~= "table" then return false end
    local npcId = brain.lccqNpcId
    if type(npcId) ~= "string" then return false end

    local definition = LCCQF.NPCRegistry and LCCQF.NPCRegistry.Get(npcId) or nil
    return definition ~= nil
        and definition.stationary == true
        and type(definition.runtime) == "table"
        and definition.runtime.adapter == "Bandits"
        and definition.runtime.program == PROGRAM_ID
end

local function clearZombieTargets(protected)
    local cell = protected and protected:getCell() or nil
    if not cell then return 0 end

    local px = math.floor(protected:getX())
    local py = math.floor(protected:getY())
    local pz = math.floor(protected:getZ())
    local cleared = 0

    for x = px - SCAN_RANGE, px + SCAN_RANGE do
        for y = py - SCAN_RANGE, py + SCAN_RANGE do
            local square = cell:getGridSquare(x, y, pz)
            if square then
                local movingObjects = square:getMovingObjects()
                for i = 0, movingObjects:size() - 1 do
                    local candidate = movingObjects:get(i)
                    if candidate and candidate ~= protected and instanceof(candidate, "IsoZombie")
                        and candidate.getTarget and candidate:getTarget() == protected
                    then
                        candidate:setTarget(nil)
                        candidate.spottedLast = nil
                        cleared = cleared + 1
                    end
                end
            end
        end
    end

    return cleared
end

local function enforceProtectedNPC(zombie, brain)
    if not zombie or zombie:isDead() or not isProtectedBrain(brain) then return false end

    local changed = false

    if brain.program ~= PROGRAM_ID then
        brain.program = PROGRAM_ID
        changed = true
    end

    if not brain.stationary then
        Bandit.ForceStationary(zombie, true)
        changed = true
    end

    Bandit.ClearMoveTasks(zombie)

    if zombie.setInvulnerable then
        zombie:setInvulnerable(true)
    end

    local cleared = clearZombieTargets(zombie)

    if changed then
        BanditBrain.Update(zombie, brain)
        if TransmitBanditCluster and brain.id ~= nil then
            TransmitBanditCluster(brain.id)
        end
    end

    if cleared > 0 then
        log("cleared zombie targets npcId=" .. tostring(brain.lccqNpcId)
            .. " runtimeId=" .. tostring(brain.id)
            .. " count=" .. tostring(cleared))
    end

    return true
end

local function scanNearPlayer(player, seen)
    if not player or player:isDead() then return end
    local cell = player:getCell()
    if not cell then return end

    local px = math.floor(player:getX())
    local py = math.floor(player:getY())
    local pz = math.floor(player:getZ())
    local scanRange = SCAN_RANGE + 4

    for x = px - scanRange, px + scanRange do
        for y = py - scanRange, py + scanRange do
            local square = cell:getGridSquare(x, y, pz)
            if square then
                local movingObjects = square:getMovingObjects()
                for i = 0, movingObjects:size() - 1 do
                    local zombie = movingObjects:get(i)
                    if zombie and instanceof(zombie, "IsoZombie") and not zombie:isDead() then
                        local brain = BanditBrain.Get(zombie)
                        if isProtectedBrain(brain) then
                            local runtimeId = tostring(brain.id or zombie)
                            if not seen[runtimeId] then
                                seen[runtimeId] = true
                                enforceProtectedNPC(zombie, brain)
                            end
                        end
                    end
                end
            end
        end
    end
end

local function onTick()
    local now = getTimestampMs()
    if now < nextUpdateMs then return end
    nextUpdateMs = now + UPDATE_INTERVAL_MS

    if not getOnlinePlayers then return end
    local players = getOnlinePlayers()
    if not players then return end

    local seen = {}
    for i = 0, players:size() - 1 do
        scanNearPlayer(players:get(i), seen)
    end
end

if isServer and isServer() then
    Events.OnTick.Add(onTick)
    log("loaded program=" .. PROGRAM_ID
        .. " intervalMs=" .. tostring(UPDATE_INTERVAL_MS)
        .. " zombieAggro=clear-target serverInvulnerable=true")
end

return true
