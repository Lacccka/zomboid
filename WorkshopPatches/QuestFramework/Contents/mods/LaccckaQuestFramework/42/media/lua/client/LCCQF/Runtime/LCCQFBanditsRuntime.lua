require "BanditBrain"
require "BanditUtils"
require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/Content/LCCQFNPCDefinitions"

local Adapter = {}
local NPC_ID_FIELD = "lccqNpcId"

local function getNPCId(brain)
    if not brain then return nil end
    if type(brain[NPC_ID_FIELD]) == "string" then
        return brain[NPC_ID_FIELD]
    end

    -- Compatibility fallback for live v0.2.0 brains. Quest identity is no
    -- longer expected to travel through Bandits2 brain snapshots.
    if type(brain.key) == "string" and LCCQF.NPCRegistry.IsRegistered(brain.key) then
        return brain.key
    end

    return nil
end

local function getRuntimeId(object, brain)
    local runtimeId = BanditUtils.GetZombieID and BanditUtils.GetZombieID(object) or nil
    if runtimeId == nil and brain then runtimeId = brain.id end
    if runtimeId == nil then return nil end
    return tostring(runtimeId)
end

local function getCandidate(object, playerX, playerY, playerZ, rangeSq)
    if not object or not instanceof(object, "IsoZombie") or object:isDead() then return nil end

    -- Bandits2 may keep an older attached brain after a later GlobalModData
    -- update. Only use the brain to confirm provider ownership and as a legacy
    -- fallback; framework identity comes from LCCQF's own runtime binding map.
    local brain = BanditBrain.Get(object)
    if not brain then return nil end

    local runtimeId = getRuntimeId(object, brain)
    if not runtimeId then return nil end

    local npcId = LCCQF.NPCRuntime.GetBoundNPCId(runtimeId)
    if not npcId then
        npcId = getNPCId(brain)
        if npcId then
            LCCQF.NPCRuntime.BindRuntime(runtimeId, npcId)
        end
    end

    local definition = npcId and LCCQF.NPCRegistry.Get(npcId) or nil
    if not definition or definition.runtime.adapter ~= "Bandits" then return nil end
    if math.abs(object:getZ() - playerZ) >= 0.5 then return nil end

    local dx = object:getX() - playerX
    local dy = object:getY() - playerY
    local distanceSq = dx * dx + dy * dy
    if distanceSq > rangeSq then return nil end

    return {
        npcId = definition.npcId,
        runtimeId = runtimeId,
        displayNameKey = definition.displayNameKey,
        distanceSq = distanceSq,
    }
end

function Adapter.FindNearestInteractive(player, range)
    if not player or player:isDead() or player:getVehicle() then return nil end

    local cell = player:getCell()
    if not cell then return nil end

    local px = player:getX()
    local py = player:getY()
    local pz = player:getZ()
    local rangeSq = range * range
    local tileRange = math.ceil(range) + 1
    local best = nil

    for x = math.floor(px) - tileRange, math.floor(px) + tileRange do
        for y = math.floor(py) - tileRange, math.floor(py) + tileRange do
            local square = cell:getGridSquare(x, y, math.floor(pz))
            if square then
                local movingObjects = square:getMovingObjects()
                for i = 0, movingObjects:size() - 1 do
                    local candidate = getCandidate(movingObjects:get(i), px, py, pz, rangeSq)
                    if candidate and (not best or candidate.distanceSq < best.distanceSq) then
                        best = candidate
                    end
                end
            end
        end
    end

    return best
end

LCCQF.NPCRuntime.RegisterAdapter("Bandits", Adapter)

return Adapter
