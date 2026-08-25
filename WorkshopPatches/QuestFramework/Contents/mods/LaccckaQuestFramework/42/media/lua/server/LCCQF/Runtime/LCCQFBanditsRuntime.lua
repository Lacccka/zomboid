require "Bandit"
require "BanditBrain"
require "BanditCustom"
require "BanditUtils"
require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/Content/LCCQFNPCDefinitions"

local C = LCCQF.Constants
local Adapter = {}
local bindings = {}
local NPC_ID_FIELD = "lccqNpcId"

local function log(message)
    print(C.LOG_PREFIX .. "[RUNTIME:BANDITS] " .. tostring(message))
end

local function getBrain(zombie)
    if not zombie or zombie:isDead() then return nil end

    local brain = BanditBrain.Get(zombie)
    if brain then return brain end

    local runtimeId = BanditUtils.GetZombieID(zombie)
    local gmd = runtimeId ~= nil and GetBanditClusterData(runtimeId) or nil
    if not gmd then return nil end
    return gmd[runtimeId] or gmd[tostring(runtimeId)]
end

local function anchorFor(zombie, brain)
    if zombie and not zombie:isDead() then
        return {
            x = zombie:getX(),
            y = zombie:getY(),
            z = zombie:getZ(),
        }
    end

    local coords = brain and brain.bornCoords or nil
    if type(coords) == "table" and coords.x ~= nil and coords.y ~= nil and coords.z ~= nil then
        return {
            x = coords.x,
            y = coords.y,
            z = coords.z,
        }
    end

    return nil
end

local function makeHandle(zombie, brain, definition)
    if not brain or not definition or brain.id == nil then return nil end

    local anchor = anchorFor(zombie, brain)
    local handle = {
        entity = zombie,
        npcId = definition.npcId,
        runtimeId = tostring(brain.id),
        displayNameKey = definition.displayNameKey,
    }
    if anchor then
        handle.x = anchor.x
        handle.y = anchor.y
        handle.z = anchor.z
    end

    bindings[definition.npcId] = handle
    LCCQF.NPCRuntime.BindRuntime(handle.runtimeId, definition.npcId, anchor)
    return handle
end

local function bind(zombie, brain, definition)
    if not zombie then return nil end
    return makeHandle(zombie, brain, definition)
end

local function getNPCId(brain)
    if not brain then return nil end
    if type(brain[NPC_ID_FIELD]) == "string" then
        return brain[NPC_ID_FIELD]
    end

    if type(brain.key) == "string" and LCCQF.NPCRegistry.IsRegistered(brain.key) then
        return brain.key
    end

    return nil
end

function Adapter.RefreshRuntimeBindings()
    local entries = {}

    if type(BanditClusters) == "table" then
        for _, cluster in pairs(BanditClusters) do
            if type(cluster) == "table" then
                for _, brain in pairs(cluster) do
                    if type(brain) == "table" and brain.id ~= nil then
                        local npcId = getNPCId(brain)
                        local definition = npcId and LCCQF.NPCRegistry.Get(npcId) or nil
                        if definition and definition.runtime.adapter == "Bandits" then
                            local anchor = anchorFor(nil, brain)
                            local entry = {
                                runtimeId = tostring(brain.id),
                                npcId = npcId,
                            }
                            if anchor then
                                entry.x = anchor.x
                                entry.y = anchor.y
                                entry.z = anchor.z
                            end
                            entries[#entries + 1] = entry
                        end
                    end
                end
            end
        end
    end

    -- Runtime tables survive some Lua resets. Rebuild from Bandits' current
    -- authoritative cluster snapshot instead of accumulating stale bindings from
    -- earlier test NPC instances.
    return LCCQF.NPCRuntime.ReplaceRuntimeBindings(entries)
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
    if brain[NPC_ID_FIELD] ~= definition.npcId then
        brain[NPC_ID_FIELD] = definition.npcId
        changed = true
    end
    if brain.key == definition.npcId then
        brain.key = nil
        changed = true
    end
    if brain.bid == nil and definition.runtime.profileId then
        brain.bid = definition.runtime.profileId
        changed = true
    end
    if definition.stationary and not brain.stationary then
        Bandit.ForceStationary(zombie, true)
        changed = true
    end

    LCCQF.NPCRuntime.BindRuntime(brain.id, definition.npcId, anchorFor(zombie, brain))

    if changed then
        BanditBrain.Update(zombie, brain)
        if TransmitBanditCluster and brain.id ~= nil then
            TransmitBanditCluster(brain.id)
        end
    end
end

local function matches(zombie, definition, runtimeId)
    local brain = getBrain(zombie)
    if not brain or getNPCId(brain) ~= definition.npcId or brain.id == nil then return nil end
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

local function findOwnedBrain(npcId)
    if type(BanditClusters) ~= "table" then return nil end

    for _, cluster in pairs(BanditClusters) do
        if type(cluster) == "table" then
            for _, brain in pairs(cluster) do
                if type(brain) == "table" and brain[NPC_ID_FIELD] == npcId then
                    return brain
                end
            end
        end
    end

    return nil
end

local function snapshotBrainIds()
    local ids = {}
    if type(BanditClusters) ~= "table" then return ids end

    for _, cluster in pairs(BanditClusters) do
        if type(cluster) == "table" then
            for _, brain in pairs(cluster) do
                if type(brain) == "table" and brain.id ~= nil then
                    ids[tostring(brain.id)] = true
                end
            end
        end
    end

    return ids
end

local function findCreatedBrain(previousIds, definition, spawn)
    if type(BanditClusters) ~= "table" then return nil end

    for _, cluster in pairs(BanditClusters) do
        if type(cluster) == "table" then
            for _, brain in pairs(cluster) do
                local coords = type(brain) == "table" and brain.bornCoords or nil
                if type(brain) == "table"
                    and brain.id ~= nil
                    and not previousIds[tostring(brain.id)]
                    and brain.bid == definition.runtime.profileId
                    and coords
                    and math.floor(coords.x) == spawn.x
                    and math.floor(coords.y) == spawn.y
                    and math.floor(coords.z) == spawn.z
                then
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

    local ownedBrain = findOwnedBrain(definition.npcId)
    if ownedBrain then
        return makeHandle(nil, ownedBrain, definition), "already registered"
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

    local previousIds = snapshotBrainIds()
    local displayName = getText and getText(definition.displayNameKey) or "Alexey"
    if displayName == definition.displayNameKey then displayName = "Alexey" end

    local previousCid = profile.cid
    local previousBid = profile.general.bid
    profile.cid = profile.general.cid
    profile.general.bid = definition.runtime.profileId
    local ok, err = pcall(BanditServer.Spawner.Individual, player, {
        bid = definition.runtime.profileId,
        x = spawn.x,
        y = spawn.y,
        z = spawn.z,
        program = definition.runtime.program or "Defend",
        permanent = true,
        hostile = false,
        hostileP = false,
        fullname = displayName,
    })
    profile.cid = previousCid
    profile.general.bid = previousBid

    if not ok then
        log("spawn error npcId=" .. tostring(definition.npcId) .. " error=" .. tostring(err))
        return nil, "Bandits2 spawn failed"
    end

    local brain = findCreatedBrain(previousIds, definition, spawn)
    if not brain then return nil, "Bandits2 did not register the spawned NPC" end

    brain[NPC_ID_FIELD] = definition.npcId
    brain.key = nil
    local gmd = GetBanditClusterData and GetBanditClusterData(brain.id) or nil
    if gmd then gmd[brain.id] = brain end
    if TransmitBanditCluster then TransmitBanditCluster(brain.id) end

    local handle = makeHandle(nil, brain, definition)

    log("spawned npcId=" .. tostring(handle.npcId)
        .. " runtimeId=" .. tostring(handle.runtimeId)
        .. " anchor=" .. tostring(handle.x) .. "," .. tostring(handle.y) .. "," .. tostring(handle.z))
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
