require "BanditBrain"
require "BanditUtils"
require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/Content/LCCQFNPCDefinitions"

local Adapter = {}
local NPC_ID_FIELD = "lccqNpcId"

local function normalizeRuntimeId(value)
    if value == nil then return nil end
    local id = tostring(value)
    if id == "" then return nil end
    return id
end

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

local function addRuntimeId(ids, seen, value)
    local id = normalizeRuntimeId(value)
    if not id or seen[id] then return end
    seen[id] = true
    ids[#ids + 1] = id
end

local function collectRuntimeIds(object, brain)
    local ids = {}
    local seen = {}

    -- brain.id is the raw persistent outfit id produced by BanditServerSpawner
    -- and is the id LCCQF broadcasts to clients.
    if brain then addRuntimeId(ids, seen, brain.id) end

    -- Bandits2 cache ids may normalize the persistent outfit id by clearing its
    -- hat bit. Interaction does not depend on cache membership, but we still
    -- accept both provider id forms when resolving the server binding.
    if object and object.getPersistentOutfitID then
        addRuntimeId(ids, seen, object:getPersistentOutfitID())
    end
    if object and BanditUtils.GetZombieID then
        addRuntimeId(ids, seen, BanditUtils.GetZombieID(object))
    end

    return ids
end

local function resolveQuestIdentity(object, brain)
    local runtimeIds = collectRuntimeIds(object, brain)

    -- A server-synchronized runtime binding is the authoritative client-side
    -- ownership and quest-identity check. Do not depend on the transient
    -- getVariableBoolean("Bandit") classifier or CacheLightB membership.
    for _, runtimeId in ipairs(runtimeIds) do
        local npcId = LCCQF.NPCRuntime.GetBoundNPCId(runtimeId)
        if npcId then return npcId, runtimeId end
    end

    -- Legacy migration only. New NPCs resolve through the binding map above.
    local npcId = getNPCId(brain)
    if npcId and runtimeIds[1] then
        LCCQF.NPCRuntime.BindRuntime(runtimeIds[1], npcId)
        return npcId, runtimeIds[1]
    end

    return nil, nil
end

local function getObjectCandidate(object, playerX, playerY, playerZ, rangeSq)
    if not object or not instanceof(object, "IsoZombie") or object:isDead() then return nil end

    local z = object:getZ()
    if z == nil or math.abs(z - playerZ) >= 0.5 then return nil end

    local dx = object:getX() - playerX
    local dy = object:getY() - playerY
    local distanceSq = dx * dx + dy * dy
    if distanceSq > rangeSq then return nil end

    -- The physical object is discovered exactly like the working proximity
    -- interaction mods in our research set: from nearby squares' moving-object
    -- lists. Quest ownership is then proven by the exact server binding.
    local brain = BanditBrain.Get(object)
    local npcId, runtimeId = resolveQuestIdentity(object, brain)
    local definition = npcId and LCCQF.NPCRegistry.Get(npcId) or nil
    if not definition or definition.runtime.adapter ~= "Bandits" then return nil end

    return {
        npcId = definition.npcId,
        runtimeId = runtimeId,
        displayNameKey = definition.displayNameKey,
        distanceSq = distanceSq,
    }
end

local function findFromNearbySquares(cell, playerX, playerY, playerZ, rangeSq, range)
    local best = nil
    local z = math.floor(playerZ)
    local tileRange = math.ceil(range) + 1

    -- Deliberately bounded. We neither walk the whole cell zombie list nor scan
    -- Bandits2's all-zombie cache every interaction tick.
    for x = math.floor(playerX) - tileRange, math.floor(playerX) + tileRange do
        for y = math.floor(playerY) - tileRange, math.floor(playerY) + tileRange do
            local square = cell:getGridSquare(x, y, z)
            if square then
                local movingObjects = square:getMovingObjects()
                if movingObjects then
                    for i = 0, movingObjects:size() - 1 do
                        local candidate = getObjectCandidate(
                            movingObjects:get(i),
                            playerX,
                            playerY,
                            playerZ,
                            rangeSq
                        )
                        if candidate and (not best or candidate.distanceSq < best.distanceSq) then
                            best = candidate
                        end
                    end
                end
            end
        end
    end

    return best
end

function Adapter.FindNearestInteractive(player, range)
    if not player or player:isDead() or player:getVehicle() then return nil end

    local cell = player:getCell()
    if not cell then return nil end

    local px = player:getX()
    local py = player:getY()
    local pz = player:getZ()
    local rangeSq = range * range

    return findFromNearbySquares(cell, px, py, pz, rangeSq, range)
end

LCCQF.NPCRuntime.RegisterAdapter("Bandits", Adapter)

return Adapter
