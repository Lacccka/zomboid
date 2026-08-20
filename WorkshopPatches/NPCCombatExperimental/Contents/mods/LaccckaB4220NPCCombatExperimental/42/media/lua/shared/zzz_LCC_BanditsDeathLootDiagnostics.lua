-- Observe the Bandits death-item pipeline without changing corpse contents.
--
-- The first OnZombieDead observer is registered from shared Lua before the
-- client BanditUpdate cleanup. A second observer is registered at OnGameStart so
-- it runs after upstream cleanup. Corpse matching prefers preserved modData but
-- can fall back to recent death coordinates because IsoDeadBody does not always
-- retain the Bandit brain id in multiplayer.
--
-- Important B42.20.3 finding: probing getItemsToSpawnAtDeath() from Lua emits a
-- Java/Kahlua RuntimeException even inside pcall, so this diagnostic deliberately
-- does not inspect the native death queue anymore.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.death-loot-diagnostics"
local unpackFn = unpack or table.unpack
local CORPSE_MATCH_MS = 10000
local CORPSE_MATCH_DIST2 = 2.25 * 2.25

Guard.safeRequire(FEATURE, "Bandit")
Guard.safeRequire(FEATURE, "BanditUtils")
if not Guard.isEnabled(FEATURE) then return end

local snapshots = {}
local recentDeaths = {}
local lateObserversInstalled = false

local function pack(...)
    return { n = select("#", ...), ... }
end

local function nowMs()
    if type(getTimestampMs) == "function" then return getTimestampMs() end
    local gt = getGameTime and getGameTime()
    if gt then return math.floor(gt:getWorldAgeHours() * 3600000) end
    return 0
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
    if not character or not instanceof(character, "IsoZombie") then return -1 end
    local ok, visuals = pcall(function() return character:getItemVisuals() end)
    if not ok or not visuals then return -1 end
    return safeSize(visuals)
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
    local id = modDataBrainId(character)
    if id then return id end

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

local function valueString(value, fallback)
    if value == nil then return fallback or "<none>" end
    return tostring(value):gsub("%s+", "_")
end

local function bagName(brain)
    if not brain or brain.bag == nil then return "<none>" end
    if type(brain.bag) == "table" then return valueString(brain.bag.name, "<unnamed>") end
    return valueString(brain.bag, "<none>")
end

local function weaponName(weapons, slot)
    if type(weapons) ~= "table" then return "<none>" end
    if slot == "melee" then return valueString(weapons.melee, "<none>") end
    local entry = weapons[slot]
    if type(entry) ~= "table" then return "<none>" end
    return valueString(entry.name, "<none>")
end

local function weaponSummary(brain)
    local weapons = brain and brain.weapons
    return string.format(
        "melee=%s,primary=%s,secondary=%s",
        weaponName(weapons, "melee"),
        weaponName(weapons, "primary"),
        weaponName(weapons, "secondary")
    )
end

local function isBandit(zombie)
    if not zombie or not instanceof(zombie, "IsoZombie") then return false end
    local ok, value = pcall(function() return zombie:getVariableBoolean("Bandit") end)
    return ok and value == true
end

local function captureRuntime(character)
    return {
        inventory = inventoryCount(character),
        worn = wornCount(character),
        visuals = itemVisualCount(character),
    }
end

local function sortedBrainClothing(brain)
    if not brain or type(brain.clothing) ~= "table" then return "<none>" end
    local values = {}
    for bodyLocation, itemName in pairs(brain.clothing) do
        values[#values + 1] = tostring(bodyLocation) .. "=" .. tostring(itemName)
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
    local okInv, inventory = pcall(function() return character:getInventory() end)
    if not okInv or not inventory then return "<unavailable>" end
    local okItems, items = pcall(function() return inventory:getItems() end)
    if not okItems or not items then return "<unavailable>" end

    local size = safeSize(items)
    if size < 0 then return "<unavailable>" end
    local values = {}
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

    local size = safeSize(worn)
    if size < 0 then return "<unavailable>" end
    local values = {}
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

    snapshot.cid = valueString(brain.cid or brain.clan, "<none>")
    snapshot.bid = valueString(brain.bid, "<none>")
    snapshot.fullname = valueString(brain.fullname, "<none>")
    snapshot.expectedClothing = tableCount(brain.clothing)
    snapshot.brainLoot = tableCount(brain.loot)
    snapshot.bag = bagName(brain)
    snapshot.weapons = weaponSummary(brain)
    snapshot.clothing = sortedBrainClothing(brain)
    return snapshot, id
end

local function formatRuntime(prefix, runtime)
    runtime = runtime or { inventory = -1, worn = -1, visuals = -1 }
    return string.format(
        "%sInventory=%d %sWorn=%d %sVisuals=%d",
        prefix, runtime.inventory,
        prefix, runtime.worn,
        prefix, runtime.visuals
    )
end

local function formatBrain(snapshot)
    if not snapshot then
        return "cid=<unknown> bid=<unknown> fullname=<unknown> expectedClothing=-1 brainLoot=-1 bag=<unknown> weapons=<unknown> clothing=<unknown>"
    end
    return string.format(
        "cid=%s bid=%s fullname=%s expectedClothing=%d brainLoot=%d bag=%s weapons=%s clothing=%s",
        tostring(snapshot.cid or "<unknown>"),
        tostring(snapshot.bid or "<unknown>"),
        tostring(snapshot.fullname or "<unknown>"),
        snapshot.expectedClothing or -1,
        snapshot.brainLoot or -1,
        tostring(snapshot.bag or "<unknown>"),
        tostring(snapshot.weapons or "<unknown>"),
        tostring(snapshot.clothing or "<unknown>")
    )
end

local function printLastUpdateStages(id, snapshot)
    if not snapshot then return end
    print(string.format(
        "[LCC][BanditsDeathLoot][BEFORE_UPDATE] id=%s seq=%d %s %s",
        id, snapshot.seq or 0, formatRuntime("before", snapshot.before), formatBrain(snapshot)
    ))
    print(string.format(
        "[LCC][BanditsDeathLoot][AFTER_UPDATE] id=%s seq=%d %s %s",
        id, snapshot.seq or 0, formatRuntime("after", snapshot.after), formatBrain(snapshot)
    ))
end

local function coords(object)
    if not object then return nil, nil, nil end
    local ok, x, y, z = pcall(function()
        return object:getX(), object:getY(), object:getZ()
    end)
    if not ok then return nil, nil, nil end
    return tonumber(x), tonumber(y), tonumber(z)
end

local function rememberRecentDeath(id, zombie)
    local x, y, z = coords(zombie)
    if not x or not y or not z then return end
    recentDeaths[#recentDeaths + 1] = {
        id = id,
        x = x,
        y = y,
        z = z,
        at = nowMs(),
    }
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
        formatRuntime("pre", captureRuntime(zombie)),
        inventoryTypes(zombie),
        wornTypes(zombie),
        formatBrain(snapshot)
    ))
end

local function onZombieDeadPostCleanup(zombie)
    local id = modDataBrainId(zombie) or characterId(zombie, nil)
    local snapshot = snapshots[id]
    if not snapshot then return end

    rememberRecentDeath(id, zombie)
    print(string.format(
        "[LCC][BanditsDeathLoot][DEAD] phase=POST_CLEANUP id=%s banditFlag=%s %s inventoryTypes=%s wornTypes=%s %s",
        id,
        tostring(isBandit(zombie)),
        formatRuntime("post", captureRuntime(zombie)),
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

    local size = safeSize(items)
    if size < 0 then return "<unavailable>" end
    local values = {}
    for i = 0, math.min(size - 1, 23) do
        local okItem, item = pcall(function() return items:get(i) end)
        if okItem and item then values[#values + 1] = itemType(item) end
    end
    if size > 24 then values[#values + 1] = "...+" .. tostring(size - 24) end
    if #values == 0 then return "<empty>" end
    return table.concat(values, ",")
end

local function resolveCorpseId(body)
    local direct = modDataBrainId(body)
    if direct and snapshots[direct] then return direct, "modData", 0 end

    local bx, by, bz = coords(body)
    if not bx or not by or not bz then return nil, "none", -1 end

    local now = nowMs()
    local bestIndex, bestDist2
    for i = #recentDeaths, 1, -1 do
        local death = recentDeaths[i]
        local age = now - (death.at or now)
        if age > CORPSE_MATCH_MS then
            table.remove(recentDeaths, i)
        elseif snapshots[death.id] and math.abs((death.z or 0) - bz) < 0.5 then
            local dx = (death.x or 0) - bx
            local dy = (death.y or 0) - by
            local dist2 = dx * dx + dy * dy
            if dist2 <= CORPSE_MATCH_DIST2 and (bestDist2 == nil or dist2 < bestDist2) then
                bestIndex = i
                bestDist2 = dist2
            end
        end
    end

    if not bestIndex then return nil, "none", -1 end
    local death = table.remove(recentDeaths, bestIndex)
    return death.id, "position", math.sqrt(bestDist2 or 0)
end

local function onDeadBodySpawn(body)
    local id, source, distance = resolveCorpseId(body)
    if not id then
        print("[LCC][BanditsDeathLoot][CORPSE_UNMATCHED] no recent Bandit death matched body")
        return
    end

    local snapshot = snapshots[id]
    if not snapshot then return end

    print(string.format(
        "[LCC][BanditsDeathLoot][CORPSE] id=%s match=%s matchDist=%.3f corpseItems=%d corpseWorn=%d corpseVisuals=-1 inventoryTypes=%s wornTypes=%s %s",
        id,
        source,
        distance or -1,
        corpseContainerCount(body),
        wornCount(body),
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

    print("[LCC][BanditsDeathLoot][LATE_INIT] post-cleanup and positional corpse observers registered")
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
                    snapshot.before = captureRuntime(zombie)
                end

                local result = pack(Bandit.__LCCDeathLootDiagnosticsOriginal(zombie, brain, ...))

                if snapshot then
                    snapshot.after = captureRuntime(zombie)
                    snapshots[id] = snapshot
                end

                return unpackFn(result, 1, result.n)
            end
        end

        Events.OnZombieDead.Add(function(zombie)
            Guard.protect(FEATURE, "observe Bandit death before upstream cleanup", onZombieDeadPreCleanup, zombie)
        end)

        Events.OnGameStart.Add(function()
            Guard.protect(FEATURE, "register late death observers", installLateObservers)
        end)

        print("[LCC][BanditsDeathLoot][INIT] four-stage death tracing active; native death queue probing disabled; no inventory mutation")
    end,
}
