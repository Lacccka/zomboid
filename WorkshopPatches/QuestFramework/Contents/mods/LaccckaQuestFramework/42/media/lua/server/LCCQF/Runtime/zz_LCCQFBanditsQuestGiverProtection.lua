require "Bandit"
require "BanditBrain"
require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFNPCDefinitions"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local PROGRAM_ID = "LCCQFQuestGiver"
local SCAN_RANGE = 12
local UPDATE_INTERVAL_MS = 250
local NON_COMBAT_MODDATA_KEY = "lccqIgnoreZombieAggro"
local NON_COMBAT_VARIABLE = "LCCQFNonCombat"
local nextUpdateMs = 0
local reported = {}

-- B42.20.3 PlayerCheats is Java userdata and its internal set(CheatType,...)
-- method is not a Lua-indexable API. Do not attempt getCheats():set(...).
-- Zombie-vs-bandit aggro suppression is handled on the controlling client by
-- removing essential quest givers from BanditZombie.CacheLightB.

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

local function enforceProgram(brain)
    local changed = false
    if type(brain.program) ~= "table" then
        brain.program = {}
        changed = true
    end
    if brain.program.name ~= PROGRAM_ID then
        brain.program.name = PROGRAM_ID
        brain.program.stage = "Prepare"
        changed = true
    elseif type(brain.program.stage) ~= "string" or brain.program.stage == "" then
        brain.program.stage = "Prepare"
        changed = true
    end
    if brain.lccqIgnoreZombieAggro ~= true then
        brain.lccqIgnoreZombieAggro = true
        changed = true
    end
    return changed
end

local function markNonCombatPhysicalState(zombie)
    local modData = zombie.getModData and zombie:getModData() or nil
    if modData then
        modData[NON_COMBAT_MODDATA_KEY] = true
    end
    if zombie.setVariable then
        zombie:setVariable(NON_COMBAT_VARIABLE, true)
    end
end

local function recoverStandingState(zombie)
    if zombie.isKnockedDown and zombie:isKnockedDown() and zombie.setKnockedDown then
        zombie:setKnockedDown(false)
    end
    if zombie.isOnFloor and zombie:isOnFloor() and zombie.setOnFloor then
        zombie:setOnFloor(false)
    end
    if zombie.setFallOnFront then
        zombie:setFallOnFront(false)
    end
end

local function enforceProtectedNPC(zombie, brain)
    if not zombie or zombie:isDead() or not isProtectedBrain(brain) then return false end

    local changed = enforceProgram(brain)

    if not brain.stationary then
        Bandit.ForceStationary(zombie, true)
        changed = true
    end

    Bandit.ClearMoveTasks(zombie)
    markNonCombatPhysicalState(zombie)

    if zombie.setUseless then zombie:setUseless(true) end
    if zombie.setInvulnerable then zombie:setInvulnerable(true) end
    if zombie.setShootable then zombie:setShootable(false) end
    if zombie.setIgnoreStaggerBack then zombie:setIgnoreStaggerBack(true) end
    recoverStandingState(zombie)

    if changed then
        BanditBrain.Update(zombie, brain)
        if TransmitBanditCluster and brain.id ~= nil then
            TransmitBanditCluster(brain.id)
        end
    end

    local runtimeId = tostring(brain.id or zombie)
    if not reported[runtimeId] then
        reported[runtimeId] = true
        log("protected npcId=" .. tostring(brain.lccqNpcId)
            .. " runtimeId=" .. runtimeId
            .. " zombieAggro=bandit-cache-optout"
            .. " shootable=" .. tostring(zombie.isShootable and zombie:isShootable() or "unknown")
            .. " invulnerable=" .. tostring(zombie.isInvulnerable and zombie:isInvulnerable() or "unknown"))
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

    for x = px - SCAN_RANGE, px + SCAN_RANGE do
        for y = py - SCAN_RANGE, py + SCAN_RANGE do
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
        .. " zombieAggro=bandit-cache-optout nonCombat=true")
end

return true
