-- LCC controlled post-corpse repair for the B42.20 Bandits clothing lifecycle.
--
-- Runtime tests proved two separate facts:
--   1. real WornItems can be restored on persistent Bandits after reconnect;
--   2. B42 corpse creation may still discard those items, even when the same
--      objects were also queued through addItemToSpawnAtDeath().
--
-- This PoC therefore waits for OnDeadBodySpawn and repairs only missing wearable
-- clothing from the last brain.clothing snapshot. Existing corpse clothing is
-- preserved and existing matching container items are reused before creating
-- anything new.
if isServer() then return end

local MARKER = "post-corpse-clothing-repair-v1"
local SNAPSHOT_MARKER = "real-worn-reconnect-v2"
local MATCH_MS = 10000
local MATCH_DIST2 = 2.25 * 2.25

LCC_BANDITS_CORPSE_CLOTHING_REPAIR = MARKER

local snapshots = rawget(_G, "LCC_BanditsClothingSnapshots")
if type(snapshots) ~= "table" then
    snapshots = {}
    _G.LCC_BanditsClothingSnapshots = snapshots
end

local recentDeaths = {}
local warned = {}

local function warnOnce(key, message)
    if warned[key] then return end
    warned[key] = true
    print(message)
end

local function nowMs()
    if type(getTimestampMs) == "function" then return getTimestampMs() end
    local gt = getGameTime and getGameTime()
    if gt then return math.floor(gt:getWorldAgeHours() * 3600000) end
    return 0
end

local function safeSize(value)
    if not value then return -1 end
    local ok, size = pcall(function() return value:size() end)
    return ok and tonumber(size) or -1
end

local function fullType(item)
    if not item then return nil end
    local ok, value = pcall(function() return item:getFullType() end)
    if ok and value then return tostring(value) end
    ok, value = pcall(function() return item:getType() end)
    return ok and value and tostring(value) or nil
end

local function modDataId(object)
    if not object then return nil end
    local ok, md = pcall(function() return object:getModData() end)
    if not ok or not md then return nil end
    if md.LCC_BanditsBrainId ~= nil then return tostring(md.LCC_BanditsBrainId) end
    if md.brainId ~= nil then return tostring(md.brainId) end
    if md.banditId ~= nil then return tostring(md.banditId) end
    return nil
end

local function characterId(character)
    local direct = modDataId(character)
    if direct then return direct end
    if character and BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end
    return nil
end

local function coords(object)
    if not object then return nil, nil, nil end
    local ok, x, y, z = pcall(function()
        return object:getX(), object:getY(), object:getZ()
    end)
    if not ok then return nil, nil, nil end
    return tonumber(x), tonumber(y), tonumber(z)
end

local function rememberDeath(zombie)
    local id = characterId(zombie)
    if not id or not snapshots[id] then return end
    local x, y, z = coords(zombie)
    if not x or not y or not z then return end
    recentDeaths[id] = {id=id, x=x, y=y, z=z, at=nowMs()}
end

local function resolveCorpse(body)
    local direct = modDataId(body)
    if direct and snapshots[direct] then
        recentDeaths[direct] = nil
        return direct, "modData", 0
    end

    local bx, by, bz = coords(body)
    if not bx or not by or not bz then return nil, "none", -1 end

    local bestId, bestDist2
    local now = nowMs()
    for id, death in pairs(recentDeaths) do
        local age = now - tonumber(death.at or now)
        if age > MATCH_MS then
            recentDeaths[id] = nil
        elseif snapshots[id] and math.abs((tonumber(death.z) or 0) - bz) < 0.5 then
            local dx = (tonumber(death.x) or 0) - bx
            local dy = (tonumber(death.y) or 0) - by
            local dist2 = dx * dx + dy * dy
            if dist2 <= MATCH_DIST2 and (bestDist2 == nil or dist2 < bestDist2) then
                bestId = id
                bestDist2 = dist2
            end
        end
    end

    if not bestId then return nil, "none", -1 end
    recentDeaths[bestId] = nil
    return bestId, "position", math.sqrt(bestDist2)
end

local function containerFor(body)
    local ok, container = pcall(function() return body:getContainer() end)
    if not ok then return nil end
    return container
end

local function wornFor(body)
    local ok, worn = pcall(function() return body:getWornItems() end)
    if not ok then return nil end
    return worn
end

local function buildContainerIndex(container)
    local byType = {}
    if not container then return byType, -1 end
    local ok, items = pcall(function() return container:getItems() end)
    if not ok or not items then return byType, -1 end
    local size = safeSize(items)
    if size < 0 then return byType, size end

    for i = 0, size - 1 do
        local okItem, item = pcall(function() return items:get(i) end)
        if okItem and item then
            local itemType = fullType(item)
            if itemType then
                local list = byType[itemType]
                if not list then
                    list = {}
                    byType[itemType] = list
                end
                list[#list + 1] = item
            end
        end
    end
    return byType, size
end

local function markAlreadyWorn(worn)
    local used = {}
    if not worn then return used, -1 end
    local size = safeSize(worn)
    if size < 0 then return used, size end
    for i = 0, size - 1 do
        local okEntry, entry = pcall(function() return worn:get(i) end)
        if okEntry and entry then
            local okItem, item = pcall(function() return entry:getItem() end)
            if okItem and item then used[item] = true end
        end
    end
    return used, size
end

local function firstUnused(index, itemType, used)
    local list = index[itemType]
    if not list then return nil end
    for _, item in ipairs(list) do
        if item and not used[item] then return item end
    end
    return nil
end

local function typedBodyLocation(item)
    if not item then return nil end
    local ok, location = pcall(function() return item:getBodyLocation() end)
    if not ok or location == nil then return nil end
    return location
end

local function applySnapshotTint(item, snapshot, brainLocation)
    if not item or not snapshot or type(snapshot.tint) ~= "table" then return end
    local packed = snapshot.tint[brainLocation]
    if packed == nil or not BanditUtils or type(BanditUtils.dec2rgb) ~= "function" then return end
    pcall(function()
        local visual = item:getVisual()
        if not visual then return end
        local color = BanditUtils.dec2rgb(packed)
        visual:setTint(ImmutableColor.new(color.r, color.g, color.b, 1))
    end)
end

local function markRepairItem(item, id, brainLocation)
    if not item then return end
    pcall(function()
        local md = item:getModData()
        if not md then return end
        md.LCC_BanditsCorpseRepair = MARKER
        md.LCC_BanditsCorpseBrainId = id
        md.LCC_BanditsCorpseBrainLocation = tostring(brainLocation)
        md.preserve = true
    end)
end

local function addContainerItem(container, itemType, fallbackItem)
    local ok, item = pcall(function() return container:AddItem(itemType) end)
    if ok and item then return item end

    if fallbackItem then
        local okFallback = pcall(function() container:AddItem(fallbackItem) end)
        if okFallback then return fallbackItem end
    end
    return nil
end

local function bodyAlreadyRepaired(body)
    local ok, md = pcall(function() return body:getModData() end)
    return ok and md and md.LCC_BanditsCorpseRepair == MARKER
end

local function markBodyRepaired(body, id)
    pcall(function()
        local md = body:getModData()
        if md then
            md.LCC_BanditsCorpseRepair = MARKER
            md.LCC_BanditsBrainId = id
        end
    end)
end

local function repairCorpse(body, id, source, distance)
    if not body or not id or bodyAlreadyRepaired(body) then return end
    local snapshot = snapshots[id]
    if not snapshot or type(snapshot.clothing) ~= "table" then return end

    local container = containerFor(body)
    local worn = wornFor(body)
    if not container or not worn then
        warnOnce("container-or-worn", "[LCC][BanditsCorpseRepair][ERROR] corpse container/worn API unavailable")
        return
    end

    local index, beforeItems = buildContainerIndex(container)
    local used, beforeWorn = markAlreadyWorn(worn)
    local processed = {}
    local stats = {
        expected = 0,
        wearableExpected = 0,
        already = 0,
        reused = 0,
        created = 0,
        repaired = 0,
        conflicts = 0,
        noLocation = 0,
        instanceFailures = 0,
        addFailures = 0,
        wearFailures = 0,
    }

    for _ in pairs(snapshot.clothing) do stats.expected = stats.expected + 1 end

    local function processLocation(brainLocation)
        if processed[brainLocation] then return end
        processed[brainLocation] = true
        local itemType = snapshot.clothing[brainLocation]
        if not itemType then return end
        itemType = tostring(itemType)

        local existing = firstUnused(index, itemType, used)
        local probe = existing or BanditCompatibility.InstanceItem(itemType)
        if not probe then
            stats.instanceFailures = stats.instanceFailures + 1
            return
        end

        local location = typedBodyLocation(probe)
        if not location then
            stats.noLocation = stats.noLocation + 1
            return
        end
        stats.wearableExpected = stats.wearableExpected + 1

        local okCurrent, current = pcall(function() return body:getWornItem(location) end)
        if not okCurrent then
            stats.wearFailures = stats.wearFailures + 1
            return
        end

        if current then
            if fullType(current) == itemType then
                stats.already = stats.already + 1
                used[current] = true
                return
            end
            stats.conflicts = stats.conflicts + 1
            print(string.format(
                "[LCC][BanditsCorpseRepair][SLOT_CONFLICT] marker=%s id=%s brainLocation=%s expected=%s actual=%s intervention=false",
                MARKER, id, tostring(brainLocation), itemType, tostring(fullType(current) or "<unknown>")
            ))
            return
        end

        local item = existing
        if item then
            stats.reused = stats.reused + 1
        else
            item = addContainerItem(container, itemType, probe)
            if not item then
                stats.addFailures = stats.addFailures + 1
                return
            end
            stats.created = stats.created + 1
            local list = index[itemType]
            if not list then
                list = {}
                index[itemType] = list
            end
            list[#list + 1] = item
            markRepairItem(item, id, brainLocation)
        end

        applySnapshotTint(item, snapshot, brainLocation)
        local okSet = pcall(function() body:setWornItem(location, item) end)
        if not okSet then
            -- Keep the item in the corpse container even if IsoDeadBody refuses
            -- the worn mutation; this still restores loot without duplicating it.
            stats.wearFailures = stats.wearFailures + 1
            used[item] = true
            return
        end

        used[item] = true
        stats.repaired = stats.repaired + 1
    end

    if BanditCompatibility and type(BanditCompatibility.GetBodyLocationsOrdered) == "function" then
        for _, brainLocation in pairs(BanditCompatibility.GetBodyLocationsOrdered()) do
            processLocation(brainLocation)
        end
    end
    for brainLocation in pairs(snapshot.clothing) do
        processLocation(brainLocation)
    end

    markBodyRepaired(body, id)

    local _, afterItems = buildContainerIndex(container)
    local _, afterWorn = markAlreadyWorn(worn)
    print(string.format(
        "[LCC][BanditsCorpseRepair][REPAIR] marker=%s id=%s fullname=%s match=%s distance=%.3f expected=%d wearableExpected=%d beforeItems=%d beforeWorn=%d already=%d reused=%d created=%d repaired=%d conflicts=%d noLocation=%d instanceFailures=%d addFailures=%d wearFailures=%d afterItems=%d afterWorn=%d",
        MARKER,
        id,
        tostring(snapshot.fullname or "<unknown>"):gsub("%s+", "_"),
        tostring(source),
        tonumber(distance) or -1,
        stats.expected,
        stats.wearableExpected,
        beforeItems,
        beforeWorn,
        stats.already,
        stats.reused,
        stats.created,
        stats.repaired,
        stats.conflicts,
        stats.noLocation,
        stats.instanceFailures,
        stats.addFailures,
        stats.wearFailures,
        afterItems,
        afterWorn
    ))
end

Events.OnZombieDead.Add(function(zombie)
    local ok, err = pcall(rememberDeath, zombie)
    if not ok then warnOnce("dead", "[LCC][BanditsCorpseRepair][DEAD_ERROR] " .. tostring(err)) end
end)

Events.OnDeadBodySpawn.Add(function(body)
    local ok, err = pcall(function()
        local id, source, distance = resolveCorpse(body)
        if id then repairCorpse(body, id, source, distance) end
    end)
    if not ok then warnOnce("corpse", "[LCC][BanditsCorpseRepair][CORPSE_ERROR] " .. tostring(err)) end
end)

print(string.format(
    "[LCC][BanditsCorpseRepair][BOOT] marker=%s snapshotMarker=%s mode=post-OnDeadBodySpawn dedupe=slot+container",
    MARKER,
    SNAPSHOT_MARKER
))
