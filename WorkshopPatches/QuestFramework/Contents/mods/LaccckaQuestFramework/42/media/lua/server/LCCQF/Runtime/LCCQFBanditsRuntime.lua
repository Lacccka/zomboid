require "Bandit"
require "BanditBrain"
require "BanditCustom"
require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/Content/LCCQFNPCDefinitions"

local C = LCCQF.Constants
local Adapter = {}
local bindings = {}

local function log(message)
    print(C.LOG_PREFIX .. "[RUNTIME:BANDITS] " .. tostring(message))
end

local function getBrain(zombie)
    if not zombie or zombie:isDead() then return nil end
    return BanditBrain.Get(zombie)
end

local function bind(zombie, brain, definition)
    if not zombie or not brain or not definition or brain.id == nil then return nil end

    local handle = {
        entity = zombie,
        npcId = definition.npcId,
        runtimeId = tostring(brain.id),
        displayName = brain.fullname or definition.displayName,
    }
    bindings[definition.npcId] = handle
    return handle
end

local function ensureState(zombie, brain, definition)
    if not zombie or not brain or not definition then return end

    local changed = false
    if brain.hostile then
        brain.hostile = false
        changed = true
    end
    if brain.hostileP then
        brain.hostileP = false
        changed = true
    end
    if not brain.permanent then
        brain.permanent = true
        changed = true
    end
    if definition.stationary and not brain.stationary then
        Bandit.ForceStationary(zombie, true)
        changed = true
    end

    if changed then
        BanditBrain.Update(zombie, brain)
        if TransmitBanditCluster and brain.id ~= nil then
            TransmitBanditCluster(brain.id)
        end
    end
end

local function matches(zombie, definition, runtimeId)
    local brain = getBrain(zombie)
    if not brain or brain.key ~= definition.npcId or brain.id == nil then return nil end
    if runtimeId ~= nil and tostring(brain.id) ~= tostring(runtimeId) then return nil end
    return brain
end

local function isInRange(player, zombie, range)
    if not player or not zombie or player:isDead() or zombie:isDead() then return false end
    if math.abs(player:getZ() - zombie:getZ()) >= 0.5 then return false end

    local dx = player:getX() - zombie:getX()
    local dy = player:getY() - zombie:getY()
    return (dx * dx + dy * dy) <= (range * range)
end

local function scanNearPlayer(player, definition, runtimeId, range)
    if not player then return nil end
    local cell = player:getCell()
    if not cell then return nil end

    local px = player:getX()
    local py = player:getY()
    local pz = math.floor(player:getZ())
    local tileRange = math.ceil(range + C.RUNTIME_RESOLVE_PADDING)

    for x = math.floor(px) - tileRange, math.floor(px) + tileRange do
        for y = math.floor(py) - tileRange, math.floor(py) + tileRange do
            local square = cell:getGridSquare(x, y, pz)
            if square then
                local movingObjects = square:getMovingObjects()
                for i = 0, movingObjects:size() - 1 do
                    local zombie = movingObjects:get(i)
                    if zombie and instanceof(zombie, "IsoZombie") then
                        local brain = matches(zombie, definition, runtimeId)
                        if brain and isInRange(player, zombie, range) then
                            ensureState(zombie, brain, definition)
                            return bind(zombie, brain, definition)
                        end
                    end
                end
            end
        end
    end

    return nil
end

local function findPersistentBrain(npcId)
    if type(BanditClusters) ~= "table" then return nil end

    for _, cluster in pairs(BanditClusters) do
        if type(cluster) == "table" then
            for _, brain in pairs(cluster) do
                if type(brain) == "table" and brain.key == npcId then
                    return brain
                end
            end
        end
    end

    return nil
end

local function findSpawnSquare(player)
    if not player then return nil end
    local cell = player:getCell()
    if not cell then return nil end

    local px = math.floor(player:getX())
    local py = math.floor(player:getY())
    local pz = math.floor(player:getZ())
    local offsets = {
        { 2, 0 }, { -2, 0 }, { 0, 2 }, { 0, -2 },
        { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 },
        { 1, 2 }, { -1, 2 }, { 1, -2 }, { -1, -2 },
    }

    for _, offset in ipairs(offsets) do
        local square = cell:getGridSquare(px + offset[1], py + offset[2], pz)
        if square and square:isFree(false) then
            return { x = px + offset[1], y = py + offset[2], z = pz }
        end
    end

    return nil
end

function Adapter.ResolveForPlayer(player, definition, runtimeId, range)
    if not definition or definition.runtime.adapter ~= "Bandits" then return nil end

    local cached = bindings[definition.npcId]
    if cached and cached.entity then
        local brain = matches(cached.entity, definition, runtimeId)
        if brain and isInRange(player, cached.entity, range) then
            ensureState(cached.entity, brain, definition)
            return bind(cached.entity, brain, definition)
        end
        bindings[definition.npcId] = nil
    end

    return scanNearPlayer(player, definition, runtimeId, range)
end

function Adapter.Spawn(player, definition)
    if not definition or definition.runtime.adapter ~= "Bandits" then
        return nil, "invalid Bandits NPC definition"
    end

    local loaded = Adapter.ResolveForPlayer(player, definition, nil, 12)
    if loaded then return loaded, "already loaded" end

    local persistentBrain = findPersistentBrain(definition.npcId)
    if persistentBrain then
        return nil, "NPC already exists in Bandits persistence (runtimeId=" .. tostring(persistentBrain.id) .. ")"
    end

    if not BanditServer or not BanditServer.Spawner or not BanditServer.Spawner.Individual then
        return nil, "Bandits2 Spawner.Individual unavailable"
    end

    local profile = BanditCustom.GetById(definition.runtime.profileId)
    if not profile or not profile.general then
        return nil, "Bandits profile missing: " .. tostring(definition.runtime.profileId)
    end

    local spawn = findSpawnSquare(player)
    if not spawn then return nil, "no free spawn square nearby" end

    -- Bandits2 42.20 reads bandit.cid, while its custom loader stores general.cid.
    -- Keep the compatibility alias scoped to this adapter and restore the shared profile afterwards.
    local previousCid = profile.cid
    profile.cid = profile.general.cid
    local ok, err = pcall(BanditServer.Spawner.Individual, player, {
        bid = definition.runtime.profileId,
        x = spawn.x,
        y = spawn.y,
        z = spawn.z,
        program = definition.runtime.program or "Defend",
        permanent = true,
        key = definition.npcId,
        hostile = false,
        hostileP = false,
        fullname = definition.displayName,
    })
    profile.cid = previousCid

    if not ok then
        log("spawn error npcId=" .. tostring(definition.npcId) .. " error=" .. tostring(err))
        return nil, "Bandits2 spawn failed"
    end

    local handle = Adapter.ResolveForPlayer(player, definition, nil, 12)
    if not handle then return nil, "Bandits2 did not expose the spawned NPC" end

    log("spawned npcId=" .. tostring(handle.npcId) .. " runtimeId=" .. tostring(handle.runtimeId))
    return handle
end

local function pruneBindings()
    for npcId, handle in pairs(bindings) do
        local definition = LCCQF.NPCRegistry.Get(npcId)
        local brain = definition and handle.entity and matches(handle.entity, definition, handle.runtimeId) or nil
        if not brain then bindings[npcId] = nil end
    end
end

Events.EveryOneMinute.Add(pruneBindings)
LCCQF.NPCRuntime.RegisterAdapter("Bandits", Adapter)

return Adapter
