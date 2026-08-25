require "BanditBrain"
require "BanditUtils"
require "BanditZombie"
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

local function collectRuntimeIds(object, brain, cacheId)
    local ids = {}
    local seen = {}

    -- brain.id is the raw persistent outfit id produced by BanditServerSpawner
    -- and is the id LCCQF broadcasts to clients.
    if brain then addRuntimeId(ids, seen, brain.id) end

    -- Bandits2 cache ids may be normalized (hat bit cleared), while brain.id is
    -- raw. Keep all provider id forms and resolve them against LCCQF's binding
    -- table rather than relying on a transient animation variable.
    if object and object.getPersistentOutfitID then
        addRuntimeId(ids, seen, object:getPersistentOutfitID())
    end
    addRuntimeId(ids, seen, cacheId)
    if object and BanditUtils.GetZombieID then
        addRuntimeId(ids, seen, BanditUtils.GetZombieID(object))
    end

    return ids
end

local function resolveQuestIdentity(object, brain, cacheId)
    local runtimeIds = collectRuntimeIds(object, brain, cacheId)

    -- A server-synchronized runtime binding is the authoritative ownership and
    -- quest-identity check. This is stronger than getVariableBoolean("Bandit"):
    -- the latter is an animation/runtime classification that can lag behind a
    -- valid synchronized Bandits brain/object on MP clients.
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

local function makeCandidate(object, brain, cacheId, x, y, z, playerX, playerY, playerZ, rangeSq)
    if z == nil or math.abs(z - playerZ) >= 0.5 then return nil end

    local dx = x - playerX
    local dy = y - playerY
    local distanceSq = dx * dx + dy * dy
    if distanceSq > rangeSq then return nil end

    local npcId, runtimeId = resolveQuestIdentity(object, brain, cacheId)
    local definition = npcId and LCCQF.NPCRegistry.Get(npcId) or nil
    if not definition or definition.runtime.adapter ~= "Bandits" then return nil end

    return {
        npcId = definition.npcId,
        runtimeId = runtimeId,
        displayNameKey = definition.displayNameKey,
        distanceSq = distanceSq,
    }
end

local function getObjectCandidate(object, playerX, playerY, playerZ, rangeSq, cacheId)
    if not object or not instanceof(object, "IsoZombie") or object:isDead() then return nil end

    -- Do not gate on object:getVariableBoolean("Bandit"). Fresh MP acceptance
    -- logs proved LCCQF can receive the exact server binding and BanditBrain can
    -- already expose the same id while that transient variable/cache classifier
    -- still prevents discovery. Exact bound runtime identity is the safe gate.
    local brain = BanditBrain.Get(object)
    return makeCandidate(
        object,
        brain,
        cacheId,
        object:getX(),
        object:getY(),
        object:getZ(),
        playerX,
        playerY,
        playerZ,
        rangeSq
    )
end

local function findFromBanditsCache(playerX, playerY, playerZ, rangeSq)
    local objectCache = BanditZombie and BanditZombie.Cache or nil
    if type(objectCache) ~= "table" then return nil end

    local best = nil
    for cacheId, object in pairs(objectCache) do
        local candidate = getObjectCandidate(object, playerX, playerY, playerZ, rangeSq, cacheId)
        if candidate and (not best or candidate.distanceSq < best.distanceSq) then
            best = candidate
        end
    end

    return best
end

local function findFromNearbySquares(cell, playerX, playerY, playerZ, rangeSq, range)
    local best = nil
    local z = math.floor(playerZ)
    local tileRange = math.ceil(range) + 1

    -- Correctness fallback: inspect only nearby moving objects, as proven by
    -- existing NPC interaction mods. No whole-cell zombie-list scan is used.
    -- Quest ownership is accepted only when one of the object's Bandits runtime
    -- ids matches a server-synchronized LCCQF binding.
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
                            rangeSq,
                            nil
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

    -- BanditZombie.Cache contains the provider's synchronized physical objects
    -- regardless of whether CacheLightB has already classified them as bandits.
    local best = findFromBanditsCache(px, py, pz, rangeSq)
    if best then return best end

    return findFromNearbySquares(cell, px, py, pz, rangeSq, range)
end

LCCQF.NPCRuntime.RegisterAdapter("Bandits", Adapter)

return Adapter
