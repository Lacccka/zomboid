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

    -- brain.id is the id Bandits2's server spawner persists and the id LCCQF
    -- broadcasts. Prefer it whenever the client already has a brain snapshot.
    if brain then addRuntimeId(ids, seen, brain.id) end

    -- Bandits2 GetZombieID() deliberately clears its hat bit for cache keys,
    -- while BanditServerSpawner stores brain.id from getPersistentOutfitID().
    -- Keep both forms so a quest NPC remains discoverable even before its brain
    -- snapshot has arrived on this client.
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

    -- Framework runtime bindings are the authoritative client-side quest
    -- identity. Do not require zombie ModData.brain to be materialized first.
    for _, runtimeId in ipairs(runtimeIds) do
        local npcId = LCCQF.NPCRuntime.GetBoundNPCId(runtimeId)
        if npcId then return npcId, runtimeId end
    end

    -- Legacy migration only. New NPCs are expected to resolve through the
    -- server-synchronized binding map above.
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
    if not object:getVariableBoolean("Bandit") then return nil end

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
    local lightCache = BanditZombie and BanditZombie.CacheLightB or nil
    if type(lightCache) ~= "table" then return nil end

    local objectCache = BanditZombie.Cache or {}
    local best = nil

    for cacheId, light in pairs(lightCache) do
        if type(light) == "table" and light.x ~= nil and light.y ~= nil and light.z ~= nil then
            local object = objectCache[cacheId]
            local candidate

            if object then
                candidate = getObjectCandidate(object, playerX, playerY, playerZ, rangeSq, cacheId)
            else
                candidate = makeCandidate(
                    nil,
                    light.brain,
                    cacheId,
                    light.x,
                    light.y,
                    light.z,
                    playerX,
                    playerY,
                    playerZ,
                    rangeSq
                )
            end

            if candidate and (not best or candidate.distanceSq < best.distanceSq) then
                best = candidate
            end
        end
    end

    return best
end

local function findFromNearbySquares(cell, playerX, playerY, playerZ, rangeSq, range)
    local best = nil
    local z = math.floor(playerZ)
    local tileRange = math.ceil(range) + 1

    -- Local fallback for the short window before BanditZombie.CacheLightB is
    -- warm. This mirrors the proximity-scanner pattern used by dialogue mods,
    -- but provider ownership is checked through Bandits2's Bandit variable and
    -- quest identity still comes from the server-synchronized binding map.
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

    -- Bandits2's own client runtime view is the primary source. Crucially, a
    -- synchronized zombie ModData.brain is optional rather than a prerequisite.
    local best = findFromBanditsCache(px, py, pz, rangeSq)
    if best then return best end

    return findFromNearbySquares(cell, px, py, pz, rangeSq, range)
end

LCCQF.NPCRuntime.RegisterAdapter("Bandits", Adapter)

return Adapter
