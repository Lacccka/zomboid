-- Observe the Bandits death-item pipeline without changing corpse contents.
--
-- The file lives in shared Lua so its first OnZombieDead observer is registered
-- before client/BanditUpdate.lua. A second observer is deliberately registered
-- from OnGameStart, after BanditUpdate has installed its own OnZombieDead
-- cleanup. This gives us an actual PRE -> POST cleanup delta instead of the
-- ambiguous single death sample used by the previous diagnostic.
--
-- Bandit.UpdateItemsToSpawnAtDeath is also wrapped to capture the last state
-- immediately before and after the upstream function. No items are added,
-- removed, worn, cloned, or otherwise modified here.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.death-loot-diagnostics"
local unpackFn = unpack or table.unpack

Guard.safeRequire(FEATURE, "Bandit")
Guard.safeRequire(FEATURE, "BanditUtils")
if not Guard.isEnabled(FEATURE) then return end

local snapshots = {}
local lateObserversInstalled = false

local function pack(...)
    return { n = select("#", ...), ... }
end

local function tableCount(value)
    if type(value) ~= "table" then return 0 end
    local count = 0
    for _ in pairs(value) do count = count + 1 end
    return count
end

local function safeSize(value)
    if value == nil then return -1 end
    local ok, size = pcall(function() return value:size() end)
    if ok and size ~= nil then return tonumber(size) or -1 end
    return -1
end

local function inventoryCount(character)
    if not character then return -1 end
    local ok, inventory = pcall(function() return character:getInventory() end)
    if not ok or not inventory then return -1 end
    local okItems, items = pcall(function() return inventory:getItems() end)
    if not okItems or not items then return -1 end
    return safeSize(items)
end

local function wornCount(character)
    if not character then return -1 end
    local ok, worn = pcall(function() return character:getWornItems() end)
    if not ok or not worn then return -1 end
    return safeSize(worn)
end

local function itemVisualCount(character)
    if not character then return -1 end
    local ok, visuals = pcall(function() return character:getItemVisuals() end)
    if not ok or not visuals then return -1 end
    return safeSize(visuals)
end

local function deathQueueCount(zombie)
    if not zombie then return -1 end

    -- There is no documented B42 getter paired with the death-item enqueue API.
    -- Probe both likely access paths behind pcall; -1 means unavailable rather
    -- than an empty queue.
    local okGetter, queue = pcall(function()
        return zombie:getItemsToSpawnAtDeath()
    end)
    if okGetter and queue then
        return safeSize(queue)
    end

    local okField, field = pcall(function()
        return zombie.itemsToSpawnAtDeath
    end)
    if okField and field then
        return safeSize(field)
    end

    return -1
end

local function modDataBrainId(character)
    if not character then return nil end
    local ok, md = pcall(function() return character:getModData() end)
    if not ok or type(md) ~= "table" then return nil end
    if md.brainId ~= nil then return tostring(md.brainId) end
    if md.banditId ~= nil then return tostring(md.banditId) end
    return nil
end

local function characterId(character, brain)
    if brain and brain.id ~= nil then return tostring(brain.id) end

    local brainId = modDataBrainId(character)
    if brainId then return brainId end

    if character and BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end

    if character then
        local ok, value = pcall(function() return character:getPersistentOutfitID() end)
        if ok and value ~= nil then return tostring(value) end
    end

    return "nil"
end

local function bagName(brain)
    if not brain or brain.bag == nil then return "<none>" end
    if type(brain.bag) == "table" then
        return tostring(brain.bag.name or "<unnamed>")
    end
    return tostring(brain.bag)
end

local function isBandit(zombie)
    if not zombie or not instanceof(zombie, "IsoZombie") then return false end
    local ok, value = pcall(function() return zombie:getVariableBoolean("Bandit") end)
    return ok and value == true
end

local function captureRuntime(character, includeDeathQueue)
    return {
        inventory = inventoryCount(character),
        worn = wornCount(character),
        visuals = itemVisualCount(character),
        deathQueue = includeDeathQueue and deathQueueCount(character) or -1,
    }
end

local function sortedBrainClothing(brain)
    if not brain or type(brain.clothing) ~= "table" then return "<none>" end
    local values = {}
    for bodyLocation, itemType in pairs(brain.clothing) do
        values[#values + 1] = tostring(bodyLocation) .. "=" .. tostring(itemType)
    end
    table.sort(values)
    if #values == 0 then return "<none>" end
    return table.concat(values, ",")
end

local function itemType(item)
    if not item then return "<nil>" end
    local ok, value = pcall(function() return item:getFullType() end)
    if ok and value then return tostring(value) end
    ok, value = pcall(function() return item:getType() end)
    if ok and value then return tostring(value) end
    return tostring(item)
end

local function inventoryTypes(character)
    if not character then return "<unavailable>" end
    local okInventory, inventory = pcall(function() return character:getInventory() end)
    if not okInventory or not inventory then return "<unavailable>" end
    local okItems, items = pcall(function() return inventory:getItems() end)
    if not okItems or not items then return "<unavailable>" end

    local values = {}
    local size = safeSize(items)
    if size < 0 then return "<unavailable>" end
    for i = 0, math.min(size - 1, 23) do
        local okItem, item = pcall(function() return items:get(i) end)
        if okItem and item then values[#values + 1] = itemType(item) end
    end
    if size > 24 then values[#values + 1] = "...+" .. tostring(size - 24) end
    if #values == 0 then return "<empty>" end
    return table.concat(values, ",")
end

local function wornTypes(character)
    if not character then return "<unavailable>" end
    local okWorn, worn = pcall(function() return character:getWornItems() end)
    if not okWorn or not worn then return "<unavailable>" end

    local values = {}
    local size = safeSize(worn)
    if size < 0 then return "<unavailable>" end
    for i = 0, math.min(size - 1, 23) do
        local okEntry, entry = pcall(function() return worn:get(i) end)
        if okEntry and entry then
            local okItem, item = pcall(function() return entry:getItem() end)
            if okItem and item then values[#values + 1] = itemType(item) end
        end
    end
    if size > 24 then values[#values + 1] = "...+" .. tostring(size - 24) end
    if #values == 0 then return "<empty>" end
    return table.concat(values, ",")
end

local function snapshotFor(zombie, brain)
    if not zombie or not brain then return nil, "nil" end
    local id = characterId(zombie, brain)
    if id == "nil" then return nil, id end

    local snapshot = snapshots[id]
    if not snapshot then
        snapshot = { seq = 0 }
        snapshots[id] = snapshot
    end

    snapshot.expectedClothing = tableCount(brain.clothing)
    snapshot.brainLoot = tableCount(brain.loot)
    snapshot.bag = bagName(brain)
    snapshot.clothing = sortedBrainClothing(brain)
    return snapshot, id
end

local function formatRuntime(prefix, runtime)
    runtime = runtime or { inventory = -1, worn = -1, visuals = -1, deathQueue = -1 }
    return string.format(
        "%sInventory=%d %sWorn=%d %sVisuals=%d %sDeathQueue=%d",
        prefix, runtime.inventory,
        prefix, runtime.worn,
        prefix, runtime.visuals,
        prefix, runtime.deathQueue
    )
end

local function formatBrain(snapshot)
    if not snapshot then
        return "expectedClothing=-1 brainLoot=-1 bag=<unknown> clothing=<unknown>"
    end
    return string.format(
        "expectedClothing=%d brainLoot=%d bag=%s clothing=%s",
        snapshot.expectedClothing or -1,
        snapshot.brainLoot or -1,
        tostring(snapshot.bag or "<unknown>"),
        tostring(snapshot.clothing or "<unknown>")
    )
end

local function printLastUpdateStages(id, snapshot)
    if not snapshot then return end
    print(string.format(
        "[LCC][BanditsDeathLoot][BEFORE_UPDATE] id=%s seq=%d %s %s",
        id,
        snapshot.seq or 0,
        formatRuntime("before", snapshot.before),
        formatBrain(snapshot)
    ))
    print(string.format(
        "[LCC][BanditsDeathLoot][AFTER_UPDATE] id=%s seq=%d %s %s",
        id,
        snapshot.seq or 0,
        formatRuntime("after", snapshot.after),
        formatBrain(snapshot)
    ))
end

local function onZombieDeadPreCleanup(zombie)
    if not isBandit(zombie) then return end
    local id = characterId(zombie, nil)
    local snapshot = snapshots[id]
    if not snapshot then return end

    printLastUpdateStages(id, snapshot)
    print(string.format(
        "[LCC][BanditsDeathLoot][DEAD] phase=PRE_CLEANUP id=%s %s inventoryTypes=%s wornTypes=%s %s",
        id,
        formatRuntime("pre", captureRuntime(zombie, true)),
        inventoryTypes(zombie),
        wornTypes(zombie),
        formatBrain(snapshot)
    ))
end

local function onZombieDeadPostCleanup(zombie)
    local id = modDataBrainId(zombie) or characterId(zombie, nil)
    local snapshot = snapshots[id]
    if not snapshot then return end

    print(string.format(
        "[LCC][BanditsDeathLoot][DEAD] phase=POST_CLEANUP id=%s banditFlag=%s %s inventoryTypes=%s wornTypes=%s %s",
        id,
        tostring(isBandit(zombie)),
        formatRuntime("post", captureRuntime(zombie, true)),
        inventoryTypes(zombie),
        wornTypes(zombie),
        formatBrain(snapshot)
    ))
end

local function corpseContainerCount(body)
    if not body then return -1 end
    local ok, container = pcall(function() return body:getContainer() end)
    if not ok or not container then return -1 end
    local okItems, items = pcall(function() return container:getItems() end)
    if not okItems or not items then return -1 end
    return safeSize(items)
end

local function corpseInventoryTypes(body)
    if not body then return "<unavailable>" end
    local ok, container = pcall(function() return body:getContainer() end)
    if not ok or not container then return "<unavailable>" end
    local okItems, items = pcall(function() return container:getItems() end)
    if not okItems or not items then return "<unavailable>" end

    local values = {}
    local size = safeSize(items)
    if size < 0 then return "<unavailable>" end
    for i = 0, math.min(size - 1, 23) do
        local okItem, item = pcall(function() return items:get(i) end)
        if okItem and item then values[#values + 1] = itemType(item) end
    end
    if size > 24 then values[#values + 1] = "...+" .. tostring(size - 24) end
    if #values == 0 then return "<empty>" end
    return table.concat(values, ",")
end

local function onDeadBodySpawn(body)
    local id = modDataBrainId(body)
    if not id then return end
    local snapshot = snapshots[id]
    if not snapshot then return end

    print(string.format(
        "[LCC][BanditsDeathLoot][CORPSE] id=%s corpseItems=%d corpseWorn=%d corpseVisuals=%d inventoryTypes=%s wornTypes=%s %s",
        id,
        corpseContainerCount(body),
        wornCount(body),
        itemVisualCount(body),
        corpseInventoryTypes(body),
        wornTypes(body),
        formatBrain(snapshot)
    ))

    snapshots[id] = nil
end

local function installLateObservers()
    if lateObserversInstalled then return end
    lateObserversInstalled = true

    Events.OnZombieDead.Add(function(zombie)
        Guard.protect(FEATURE, "observe Bandit death after upstream cleanup", onZombieDeadPostCleanup, zombie)
    end)
    Events.OnDeadBodySpawn.Add(function(body)
        Guard.protect(FEATURE, "observe Bandit corpse contents", onDeadBodySpawn, body)
    end)

    print("[LCC][BanditsDeathLoot][LATE_INIT] post-cleanup and corpse observers registered")
end

Guard.install {
    id = FEATURE,
    validate = function()
        if type(Bandit) ~= "table" or type(Bandit.UpdateItemsToSpawnAtDeath) ~= "function" then
            return false, "Bandit.UpdateItemsToSpawnAtDeath is unavailable"
        end
        if not Events or not Events.OnZombieDead or not Events.OnDeadBodySpawn or not Events.OnGameStart then
            return false, "death/corpse/game-start events are unavailable"
        end
        return true
    end,
    install = function()
        if not Bandit.__LCCDeathLootDiagnosticsOriginal then
            Bandit.__LCCDeathLootDiagnosticsOriginal = Bandit.UpdateItemsToSpawnAtDeath
            Bandit.UpdateItemsToSpawnAtDeath = function(zombie, brain, ...)
                local snapshot, id = snapshotFor(zombie, brain)
                if snapshot then
                    snapshot.seq = (snapshot.seq or 0) + 1
                    snapshot.before = captureRuntime(zombie, true)
                end

                local result = pack(Bandit.__LCCDeathLootDiagnosticsOriginal(zombie, brain, ...))

                if snapshot then
                    snapshot.after = captureRuntime(zombie, true)
                    snapshots[id] = snapshot
                end

                return unpackFn(result, 1, result.n)
            end
        end

        -- Registered now (shared-Lua phase): expected to run before BanditUpdate's
        -- client-side cleanup handler.
        Events.OnZombieDead.Add(function(zombie)
            Guard.protect(FEATURE, "observe Bandit death before upstream cleanup", onZombieDeadPreCleanup, zombie)
        end)

        -- Registered at game start: expected to run after BanditUpdate.lua has
        -- installed its own OnZombieDead and OnDeadBodySpawn handlers.
        Events.OnGameStart.Add(function()
            Guard.protect(FEATURE, "register late death observers", installLateObservers)
        end)

        print("[LCC][BanditsDeathLoot][INIT] four-stage death tracing active; no inventory mutation")
    end,
}
