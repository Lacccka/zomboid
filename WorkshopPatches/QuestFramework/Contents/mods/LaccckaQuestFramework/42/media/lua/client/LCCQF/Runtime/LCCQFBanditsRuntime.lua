require "Bandit"
require "BanditBrain"
require "BanditUtils"
require "BanditZombie"
require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/Content/LCCQFNPCDefinitions"

local Adapter = {}
local NPC_ID_FIELD = "lccqNpcId"

-- Client Bandits objects are not reliably discoverable through a fresh
-- square:getMovingObjects() walk on B42.20 MP. Keep a provider-native view keyed
-- by Bandits runtime id instead. Weak values avoid keeping unloaded zombies alive.
local physicalByRuntimeId = setmetatable({}, { __mode = "v" })
local physicalLogState = {}
local unresolvedLogState = {}
local rejectedLogState = {}
local visualObserver = nil

local function log(message)
    print("[LCCQF][RUNTIME:BANDITS] " .. tostring(message))
end

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

    -- brain.id is the exact id created by BanditServerSpawner and the id LCCQF
    -- broadcasts. The fresh 0.2.5 MP log also proves Bandit.ApplyVisuals receives
    -- this same id on the client even when LCCQF's square scan sees no target.
    if brain then addRuntimeId(ids, seen, brain.id) end

    if object and object.getPersistentOutfitID then
        addRuntimeId(ids, seen, object:getPersistentOutfitID())
    end
    if object and BanditUtils.GetZombieID then
        addRuntimeId(ids, seen, BanditUtils.GetZombieID(object))
    end

    return ids
end

local function rememberPhysical(object, brain, source)
    if not object or not instanceof(object, "IsoZombie") then return end

    local ids = collectRuntimeIds(object, brain)
    for _, runtimeId in ipairs(ids) do
        physicalByRuntimeId[runtimeId] = object
        unresolvedLogState[runtimeId] = nil

        local npcId = LCCQF.NPCRuntime.GetBoundNPCId(runtimeId)
        if npcId and not physicalLogState[runtimeId] then
            physicalLogState[runtimeId] = true
            log("physical object observed source=" .. tostring(source)
                .. " npcId=" .. tostring(npcId)
                .. " runtimeId=" .. tostring(runtimeId))
        end
    end
end

local function ensureVisualObserver()
    if type(Bandit) ~= "table" or type(Bandit.ApplyVisuals) ~= "function" then
        return false
    end
    if Bandit.ApplyVisuals == visualObserver then return true end

    local original = Bandit.ApplyVisuals
    local observer
    observer = function(bandit, brain, ...)
        original(bandit, brain, ...)
        local ok, err = pcall(rememberPhysical, bandit, brain, "Bandit.ApplyVisuals")
        if not ok then
            log("physical observer error=" .. tostring(err))
        end
    end

    visualObserver = observer
    Bandit.ApplyVisuals = observer
    log("physical observer installed source=Bandit.ApplyVisuals")
    return true
end

local function resolveQuestIdentity(object, brain)
    local runtimeIds = collectRuntimeIds(object, brain)

    for _, runtimeId in ipairs(runtimeIds) do
        local npcId = LCCQF.NPCRuntime.GetBoundNPCId(runtimeId)
        if npcId then return npcId, runtimeId end
    end

    -- Legacy migration only. New NPCs resolve through the server binding map.
    local npcId = getNPCId(brain)
    if npcId and runtimeIds[1] then
        LCCQF.NPCRuntime.BindRuntime(runtimeIds[1], npcId)
        return npcId, runtimeIds[1]
    end

    return nil, nil
end

local function makeObjectCandidate(object, playerX, playerY, playerZ, rangeSq)
    if not object or not instanceof(object, "IsoZombie") then return nil, "not-IsoZombie" end
    if object:isDead() then return nil, "dead" end

    local z = object:getZ()
    if z == nil then return nil, "missing-z" end
    if math.abs(z - playerZ) >= 0.5 then
        return nil, "z-mismatch objectZ=" .. tostring(z) .. " playerZ=" .. tostring(playerZ)
    end

    local dx = object:getX() - playerX
    local dy = object:getY() - playerY
    local distanceSq = dx * dx + dy * dy
    if distanceSq > rangeSq then
        return nil, "out-of-range distance=" .. tostring(math.sqrt(distanceSq))
    end

    local brain = BanditBrain.Get(object)
    local npcId, runtimeId = resolveQuestIdentity(object, brain)
    if not npcId then
        local ids = collectRuntimeIds(object, brain)
        return nil, "identity-unresolved ids=" .. table.concat(ids, ",")
    end

    local definition = LCCQF.NPCRegistry.Get(npcId)
    if not definition then return nil, "definition-missing npcId=" .. tostring(npcId) end
    if definition.runtime.adapter ~= "Bandits" then
        return nil, "wrong-adapter npcId=" .. tostring(npcId)
    end

    rememberPhysical(object, brain, "candidate")
    return {
        npcId = definition.npcId,
        runtimeId = runtimeId,
        displayNameKey = definition.displayNameKey,
        distanceSq = distanceSq,
    }, nil
end

local function cacheObjectForRuntimeId(runtimeId)
    local id = normalizeRuntimeId(runtimeId)
    if not id then return nil, nil end

    local object = physicalByRuntimeId[id]
    if object then return object, "observed" end

    local cache = BanditZombie and BanditZombie.Cache or nil
    if type(cache) == "table" then
        object = cache[id]
        if not object then
            local numericId = tonumber(id)
            if numericId ~= nil then object = cache[numericId] end
        end
        if object then
            local brain = BanditBrain.Get(object)
            rememberPhysical(object, brain, "BanditZombie.Cache")
            return object, "BanditZombie.Cache"
        end
    end

    return nil, nil
end

local function reportUnresolvedLater(binding)
    local runtimeId = tostring(binding.runtimeId)
    local state = unresolvedLogState[runtimeId]
    local now = getTimestampMs()

    if state == nil then
        unresolvedLogState[runtimeId] = now
        return
    end
    if state == true or now - state < 1000 then return end

    unresolvedLogState[runtimeId] = true
    log("physical object unresolved npcId=" .. tostring(binding.npcId)
        .. " runtimeId=" .. runtimeId
        .. " afterMs=" .. tostring(now - state))
end

local function findFromRuntimeBindings(playerX, playerY, playerZ, rangeSq)
    local best = nil

    for _, binding in ipairs(LCCQF.NPCRuntime.ExportRuntimeBindings()) do
        if type(binding) == "table" then
            local definition = LCCQF.NPCRegistry.Get(binding.npcId)
            if definition and definition.runtime.adapter == "Bandits" then
                local object, source = cacheObjectForRuntimeId(binding.runtimeId)
                if object then
                    unresolvedLogState[tostring(binding.runtimeId)] = nil
                    local candidate, reason = makeObjectCandidate(object, playerX, playerY, playerZ, rangeSq)
                    if candidate and (not best or candidate.distanceSq < best.distanceSq) then
                        best = candidate
                    elseif not candidate and not rejectedLogState[tostring(binding.runtimeId)] then
                        rejectedLogState[tostring(binding.runtimeId)] = true
                        log("physical object rejected source=" .. tostring(source)
                            .. " npcId=" .. tostring(binding.npcId)
                            .. " runtimeId=" .. tostring(binding.runtimeId)
                            .. " reason=" .. tostring(reason))
                    end
                else
                    reportUnresolvedLater(binding)
                end
            end
        end
    end

    return best
end

local function findFromNearbySquares(cell, playerX, playerY, playerZ, rangeSq, range)
    local best = nil
    local z = math.floor(playerZ)
    local tileRange = math.ceil(range) + 1

    -- Last-resort bounded fallback. The primary path above never depends on the
    -- NPC appearing in this list; it follows Bandits2's own object exposure.
    for x = math.floor(playerX) - tileRange, math.floor(playerX) + tileRange do
        for y = math.floor(playerY) - tileRange, math.floor(playerY) + tileRange do
            local square = cell:getGridSquare(x, y, z)
            if square then
                local movingObjects = square:getMovingObjects()
                if movingObjects then
                    for i = 0, movingObjects:size() - 1 do
                        local object = movingObjects:get(i)
                        if object and instanceof(object, "IsoZombie") then
                            local brain = BanditBrain.Get(object)
                            rememberPhysical(object, brain, "nearby-square")
                        end
                        local candidate = makeObjectCandidate(
                            object,
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

    ensureVisualObserver()

    local cell = player:getCell()
    if not cell then return nil end

    local px = player:getX()
    local py = player:getY()
    local pz = player:getZ()
    local rangeSq = range * range

    local best = findFromRuntimeBindings(px, py, pz, rangeSq)
    if best then return best end

    return findFromNearbySquares(cell, px, py, pz, rangeSq, range)
end

ensureVisualObserver()
LCCQF.NPCRuntime.RegisterAdapter("Bandits", Adapter)

return Adapter
