-- Observe the Bandits death-item pipeline without changing corpse contents.
--
-- B42.20 Bandits renders most NPC clothing through ItemVisual after clearing
-- getWornItems(), while its OnZombieDead handler removes inventory entries that
-- were not previously marked with modData.preserve. This diagnostic records the
-- state prepared by Bandit.UpdateItemsToSpawnAtDeath and compares it with the
-- live zombie after death cleanup and the resulting IsoDeadBody.
--
-- No items are added, removed, worn, cloned, or otherwise modified here.

local Guard = require "LCC/Guard"
local FEATURE = "bandits.death-loot-diagnostics"

Guard.safeRequire(FEATURE, "Bandit")
Guard.safeRequire(FEATURE, "BanditUtils")
if not Guard.isEnabled(FEATURE) then return end

local snapshots = {}

local function sideName()
    if isServer and isServer() then return "server" end
    if isClient and isClient() then return "client" end
    return "local"
end

local function tableCount(value)
    if type(value) ~= "table" then return 0 end
    local count = 0
    for _ in pairs(value) do count = count + 1 end
    return count
end

local function safeSize(value)
    if value == nil or type(value.size) ~= "function" then return -1 end
    local ok, size = pcall(function() return value:size() end)
    if ok and size ~= nil then return tonumber(size) or -1 end
    return -1
end

local function inventoryCount(character)
    if not character or type(character.getInventory) ~= "function" then return -1 end
    local ok, inventory = pcall(function() return character:getInventory() end)
    if not ok or not inventory then return -1 end
    local okItems, items = pcall(function() return inventory:getItems() end)
    if not okItems then return -1 end
    return safeSize(items)
end

local function wornCount(character)
    if not character or type(character.getWornItems) ~= "function" then return -1 end
    local ok, worn = pcall(function() return character:getWornItems() end)
    if not ok then return -1 end
    return safeSize(worn)
end

local function modDataBrainId(character)
    if not character or type(character.getModData) ~= "function" then return nil end
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

    if character and type(character.getPersistentOutfitID) == "function" then
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

local function rememberManifest(zombie, brain)
    if not zombie or not brain then return end

    local id = characterId(zombie, brain)
    if id == "nil" then return end

    snapshots[id] = {
        expectedClothing = tableCount(brain.clothing),
        brainLoot = tableCount(brain.loot),
        inventory = inventoryCount(zombie),
        worn = wornCount(zombie),
        bag = bagName(brain),
    }
end

local function snapshotSummary(id)
    local snapshot = snapshots[id]
    if not snapshot then
        return "expectedClothing=-1 brainLoot=-1 manifestInventory=-1 manifestWorn=-1 bag=<unknown>"
    end

    return string.format(
        "expectedClothing=%d brainLoot=%d manifestInventory=%d manifestWorn=%d bag=%s",
        snapshot.expectedClothing,
        snapshot.brainLoot,
        snapshot.inventory,
        snapshot.worn,
        snapshot.bag
    )
end

local function onZombieDead(zombie)
    if not isBandit(zombie) then return end

    local id = characterId(zombie, nil)
    print(string.format(
        "[LCC][BanditsDeathLoot][DEAD] side=%s id=%s postCleanupInventory=%d postCleanupWorn=%d %s",
        sideName(),
        id,
        inventoryCount(zombie),
        wornCount(zombie),
        snapshotSummary(id)
    ))
end

local function bodyId(body)
    local id = modDataBrainId(body)
    return id or "nil"
end

local function corpseContainerCount(body)
    if not body or type(body.getContainer) ~= "function" then return -1 end
    local ok, container = pcall(function() return body:getContainer() end)
    if not ok or not container then return -1 end
    local okItems, items = pcall(function() return container:getItems() end)
    if not okItems then return -1 end
    return safeSize(items)
end

local function onDeadBodySpawn(body)
    local id = bodyId(body)
    if id == "nil" or not snapshots[id] then return end

    print(string.format(
        "[LCC][BanditsDeathLoot][CORPSE] side=%s id=%s corpseItems=%d corpseWorn=%d %s",
        sideName(),
        id,
        corpseContainerCount(body),
        wornCount(body),
        snapshotSummary(id)
    ))

    snapshots[id] = nil
end

Guard.install {
    id = FEATURE,
    validate = function()
        if type(Bandit) ~= "table" or type(Bandit.UpdateItemsToSpawnAtDeath) ~= "function" then
            return false, "Bandit.UpdateItemsToSpawnAtDeath is unavailable"
        end
        if not Events or not Events.OnZombieDead or not Events.OnDeadBodySpawn then
            return false, "death/corpse events are unavailable"
        end
        return true
    end,
    install = function()
        if not Bandit.__LCCDeathLootDiagnosticsOriginal then
            Bandit.__LCCDeathLootDiagnosticsOriginal = Bandit.UpdateItemsToSpawnAtDeath
            Bandit.UpdateItemsToSpawnAtDeath = function(zombie, brain, ...)
                rememberManifest(zombie, brain)
                return Bandit.__LCCDeathLootDiagnosticsOriginal(zombie, brain, ...)
            end
        end

        Events.OnZombieDead.Add(function(zombie)
            Guard.protect(FEATURE, "observe Bandit death cleanup", onZombieDead, zombie)
        end)
        Events.OnDeadBodySpawn.Add(function(body)
            Guard.protect(FEATURE, "observe Bandit corpse contents", onDeadBodySpawn, body)
        end)

        print("[LCC][BanditsDeathLoot][INIT] diagnostic-only death manifest/corpse tracing active; no inventory mutation")
    end,
}
