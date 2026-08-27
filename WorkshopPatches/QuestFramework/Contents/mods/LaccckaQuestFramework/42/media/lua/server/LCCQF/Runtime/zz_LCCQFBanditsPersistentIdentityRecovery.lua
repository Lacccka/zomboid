require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/Content/LCCQFNPCDefinitions"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Runtime = LCCQF.NPCRuntime
local Adapter = require "LCCQF/Runtime/LCCQFBanditsServerRuntime"
local WORLD_NPC_SCHEMA_VERSION = 1
local RETRY_INTERVAL_MS = 1000
local RETRY_WINDOW_MS = 30000
local retryUntilMs = 0
local nextRetryMs = 0
local originalSpawn = Adapter.Spawn
local originalRefresh = Adapter.RefreshRuntimeBindings
local originalReconcile = Adapter.ReconcileRuntimeBindings

local function log(message)
    print(C.LOG_PREFIX .. "[RUNTIME:PERSISTENCE] " .. tostring(message))
end

local function finite(value)
    local number = tonumber(value)
    if number == nil or number ~= number or number == math.huge or number == -math.huge then
        return nil
    end
    return number
end

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    if not gameTime or not gameTime.getWorldAgeHours then return 0 end
    return tonumber(gameTime:getWorldAgeHours()) or 0
end

local function getRoot()
    if not ModData or not ModData.getOrCreate then return nil end
    local root = ModData.getOrCreate(C.PERSISTENCE_TAG)
    if not root then return nil end
    if type(root.worldNPCs) ~= "table" then root.worldNPCs = {} end
    return root
end

local function getRecord(npcId, create)
    local root = getRoot()
    if not root or type(npcId) ~= "string" then return nil end
    local record = root.worldNPCs[npcId]
    if type(record) ~= "table" and create then
        record = {
            schemaVersion = WORLD_NPC_SCHEMA_VERSION,
            npcId = npcId,
            status = "alive",
            createdWorldHours = worldHours(),
        }
        root.worldNPCs[npcId] = record
    end
    return type(record) == "table" and record or nil
end

local function recordAnchor(record)
    if type(record) ~= "table" then return nil end
    local x, y, z = finite(record.x), finite(record.y), finite(record.z)
    if x == nil or y == nil or z == nil then return nil end
    return { x = x, y = y, z = z }
end

local function brainAnchor(brain)
    local coords = type(brain) == "table" and brain.bornCoords or nil
    if type(coords) ~= "table" then return nil end
    local x, y, z = finite(coords.x), finite(coords.y), finite(coords.z)
    if x == nil or y == nil or z == nil then return nil end
    return { x = x, y = y, z = z }
end

local function updateRecord(record, definition, runtimeId, anchor, status)
    if not record or not definition then return false end
    record.schemaVersion = WORLD_NPC_SCHEMA_VERSION
    record.npcId = definition.npcId
    record.adapter = definition.runtime and definition.runtime.adapter or nil
    record.profileId = definition.runtime and definition.runtime.profileId or nil
    record.status = status or record.status or "alive"
    record.updatedWorldHours = worldHours()

    if runtimeId ~= nil then record.runtimeId = tostring(runtimeId) end
    if anchor then
        record.x = finite(anchor.x) or record.x
        record.y = finite(anchor.y) or record.y
        record.z = finite(anchor.z) or record.z
    end
    return true
end

local function persistHandle(handle, definition, source)
    if type(handle) ~= "table" or type(handle.npcId) ~= "string" then return false end
    definition = definition or LCCQF.NPCRegistry.Get(handle.npcId)
    if not definition or not definition.runtime or definition.runtime.adapter ~= "Bandits" then return false end

    local record = getRecord(handle.npcId, true)
    if not record then return false end
    updateRecord(record, definition, handle.runtimeId, handle, "alive")
    record.lastSource = tostring(source or "runtime")
    return true
end

local function eachBrain(visitor)
    if type(BanditClusters) ~= "table" then return 0 end
    local seen = {}
    local count = 0
    for _, cluster in pairs(BanditClusters) do
        if type(cluster) == "table" then
            for _, brain in pairs(cluster) do
                if type(brain) == "table" and brain.id ~= nil then
                    local key = tostring(brain.id)
                    if not seen[key] then
                        seen[key] = true
                        count = count + 1
                        visitor(brain)
                    end
                end
            end
        end
    end
    return count
end

local function collect(predicate)
    local result = {}
    eachBrain(function(brain)
        if predicate(brain) then result[#result + 1] = brain end
    end)
    return result
end

local function profileMatches(brain, definition)
    if type(brain) ~= "table" or not definition or not definition.runtime then return false end
    local expected = definition.runtime.profileId
    return expected ~= nil and tostring(brain.bid or "") == tostring(expected)
end

local function chooseBrain(definition, record)
    local exact = collect(function(brain)
        return brain.lccqNpcId == definition.npcId
    end)
    if #exact == 1 then return exact[1], "exact" end
    if #exact > 1 then return nil, "ambiguous-exact:" .. tostring(#exact) end

    if record and record.runtimeId ~= nil then
        local runtimeId = tostring(record.runtimeId)
        local byRuntime = collect(function(brain)
            if tostring(brain.id or "") ~= runtimeId then return false end
            return brain.lccqNpcId == definition.npcId or profileMatches(brain, definition)
        end)
        if #byRuntime == 1 then return byRuntime[1], "record-runtime" end
        if #byRuntime > 1 then return nil, "ambiguous-runtime:" .. tostring(#byRuntime) end
    end

    -- One-time migration for the existing test world only. Future authored NPCs
    -- may share visual/profile templates, so profile-only ownership must never
    -- become the general identity rule.
    if not record and definition.npcId == C.TEST_NPC_ID then
        local legacy = collect(function(brain)
            return profileMatches(brain, definition)
        end)
        if #legacy == 1 then return legacy[1], "legacy-profile" end
        if #legacy > 1 then return nil, "ambiguous-legacy-profile:" .. tostring(#legacy) end
    end

    return nil, "not-found"
end

local function applyFrameworkIdentity(brain, definition)
    if type(brain) ~= "table" or not definition then return false end
    local changed = false

    if brain.lccqNpcId ~= definition.npcId then
        brain.lccqNpcId = definition.npcId
        changed = true
    end
    if brain.permanent ~= true then
        brain.permanent = true
        changed = true
    end
    if brain.hostile == true then
        brain.hostile = false
        changed = true
    end
    if brain.hostileP == true then
        brain.hostileP = false
        changed = true
    end

    if definition.runtime and definition.runtime.program then
        if type(brain.program) ~= "table" then
            brain.program = {}
            changed = true
        end
        if brain.program.name ~= definition.runtime.program then
            brain.program.name = definition.runtime.program
            brain.program.stage = "Prepare"
            changed = true
        elseif type(brain.program.stage) ~= "string" or brain.program.stage == "" then
            brain.program.stage = "Prepare"
            changed = true
        end
        if brain.lccqNonCombat ~= true then
            brain.lccqNonCombat = true
            changed = true
        end
    end

    if definition.stationary == true and brain.stationary ~= true then
        brain.stationary = true
        changed = true
    end

    return changed
end

local function recoverDefinition(definition)
    if not definition or not definition.runtime or definition.runtime.adapter ~= "Bandits" then
        return false, "unsupported"
    end

    local record = getRecord(definition.npcId, false)
    if record and record.status == "dead" then return false, "dead" end

    local brain, source = chooseBrain(definition, record)
    if not brain then
        if source ~= "not-found" then
            log("recovery skipped npcId=" .. tostring(definition.npcId) .. " reason=" .. tostring(source))
        end
        return false, source
    end

    local changed = applyFrameworkIdentity(brain, definition)
    local anchor = recordAnchor(record) or brainAnchor(brain)
    Runtime.BindRuntime(brain.id, definition.npcId, anchor)

    local stored = record or getRecord(definition.npcId, true)
    if stored then
        updateRecord(stored, definition, brain.id, anchor, "alive")
        stored.lastSource = source
    end

    if changed and type(TransmitBanditCluster) == "function" then
        pcall(TransmitBanditCluster, brain.id)
    end

    log("recovered npcId=" .. tostring(definition.npcId)
        .. " runtimeId=" .. tostring(brain.id)
        .. " source=" .. tostring(source)
        .. " anchor=" .. tostring(anchor and anchor.x) .. ","
        .. tostring(anchor and anchor.y) .. "," .. tostring(anchor and anchor.z)
        .. " brainChanged=" .. tostring(changed))
    return true, source
end

local function recoverPersistentIdentities(source)
    local recovered = 0
    local unresolved = 0
    for npcId, definition in pairs(LCCQF.NPCRegistry.GetDefinitions()) do
        if definition.runtime and definition.runtime.adapter == "Bandits" then
            local record = getRecord(npcId, false)
            local shouldRecover = record ~= nil or npcId == C.TEST_NPC_ID
            if shouldRecover and (not record or record.status ~= "dead") then
                local ok, reason = recoverDefinition(definition)
                if ok then
                    recovered = recovered + 1
                elseif reason == "not-found" then
                    unresolved = unresolved + 1
                end
            end
        end
    end

    if recovered > 0 or source == "server-start" then
        log("recovery pass source=" .. tostring(source)
            .. " recovered=" .. tostring(recovered)
            .. " unresolved=" .. tostring(unresolved))
    end
    return recovered, unresolved
end

local function persistCurrentBindings(source)
    for _, handle in ipairs(Runtime.ExportRuntimeBindings()) do
        persistHandle(handle, nil, source)
    end
end

Adapter.Spawn = function(player, definition)
    local handle, result = originalSpawn(player, definition)
    if handle then persistHandle(handle, definition, "spawn") end
    return handle, result
end

Adapter.RefreshRuntimeBindings = function(...)
    recoverPersistentIdentities("refresh")
    local count = originalRefresh(...)
    persistCurrentBindings("refresh")
    return count
end

Adapter.ReconcileRuntimeBindings = function(...)
    local removed, rebound = originalReconcile(...)
    persistCurrentBindings("reconcile")
    return removed, rebound
end

local function onZombieDead(zombie)
    if not zombie or not zombie.getModData then return end
    local modData = zombie:getModData()
    local brain = modData and modData.brain or nil
    local npcId = type(brain) == "table" and brain.lccqNpcId or nil
    if type(npcId) ~= "string" then return end

    local definition = LCCQF.NPCRegistry.Get(npcId)
    if not definition or not definition.runtime or definition.runtime.adapter ~= "Bandits" then return end

    local record = getRecord(npcId, true)
    if not record then return end
    local anchor = {
        x = zombie.getX and zombie:getX() or record.x,
        y = zombie.getY and zombie:getY() or record.y,
        z = zombie.getZ and zombie:getZ() or record.z,
    }
    updateRecord(record, definition, brain.id, anchor, "dead")
    record.diedWorldHours = worldHours()
    record.lastSource = "death"
    log("marked dead npcId=" .. tostring(npcId) .. " runtimeId=" .. tostring(brain.id))
end

local function onServerStarted()
    local now = getTimestampMs()
    retryUntilMs = now + RETRY_WINDOW_MS
    nextRetryMs = now + RETRY_INTERVAL_MS
    local _, unresolved = recoverPersistentIdentities("server-start")
    if unresolved == 0 then retryUntilMs = 0 end
end

local function onTick()
    if retryUntilMs <= 0 then return end
    local now = getTimestampMs()
    if now > retryUntilMs then
        retryUntilMs = 0
        log("startup recovery retry window ended")
        return
    end
    if now < nextRetryMs then return end
    nextRetryMs = now + RETRY_INTERVAL_MS

    local _, unresolved = recoverPersistentIdentities("startup-retry")
    if unresolved == 0 then retryUntilMs = 0 end
end

if isServer and isServer() then
    Events.OnServerStarted.Add(onServerStarted)
    Events.OnTick.Add(onTick)
    if Events.OnZombieDead then Events.OnZombieDead.Add(onZombieDead) end
    log("loaded schema=" .. tostring(WORLD_NPC_SCHEMA_VERSION)
        .. " retryWindowMs=" .. tostring(RETRY_WINDOW_MS)
        .. " legacyMigrationNpcId=" .. tostring(C.TEST_NPC_ID))
end

return true
