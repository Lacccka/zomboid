require "BanditBrain"
require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/Content/LCCQFNPCDefinitions"

local Adapter = {}
local NPC_ID_FIELD = "lccqNpcId"

local function getNPCId(brain)
    if type(brain[NPC_ID_FIELD]) == "string" then
        return brain[NPC_ID_FIELD]
    end

    -- Migrate live v0.2.0 brains away from Bandits2's numeric door-key field.
    if type(brain.key) == "string" and LCCQF.NPCRegistry.IsRegistered(brain.key) then
        brain[NPC_ID_FIELD] = brain.key
        brain.key = nil
        return brain[NPC_ID_FIELD]
    end

    return nil
end

local function getCandidate(object, playerX, playerY, playerZ, rangeSq)
    if not object or not instanceof(object, "IsoZombie") or object:isDead() then return nil end

    local brain = BanditBrain.Get(object)
    if not brain or brain.id == nil then return nil end

    -- The server transmits the framework tag immediately after Bandits creates
    -- its brain. If the zombie was Banditized from the first cluster snapshot,
    -- refresh its attached brain from the newer tagged snapshot here.
    if type(brain[NPC_ID_FIELD]) ~= "string" and GetBanditClusterData then
        local gmd = GetBanditClusterData(brain.id)
        local current = gmd and (gmd[brain.id] or gmd[tostring(brain.id)]) or nil
        if current and current ~= brain and type(current[NPC_ID_FIELD]) == "string" then
            brain = current
            BanditBrain.Update(object, brain)
        end
    end

    local definition = LCCQF.NPCRegistry.Get(getNPCId(brain))
    if not definition or definition.runtime.adapter ~= "Bandits" then return nil end
    if math.abs(object:getZ() - playerZ) >= 0.5 then return nil end

    local dx = object:getX() - playerX
    local dy = object:getY() - playerY
    local distanceSq = dx * dx + dy * dy
    if distanceSq > rangeSq then return nil end

    return {
        npcId = definition.npcId,
        runtimeId = tostring(brain.id),
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
