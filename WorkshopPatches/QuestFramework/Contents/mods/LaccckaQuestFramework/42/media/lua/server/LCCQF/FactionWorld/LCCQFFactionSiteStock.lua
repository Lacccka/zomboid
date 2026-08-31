-- Read-only, server-authoritative settlement stock snapshots.
-- Persistent state contains only plain locator/count data; no IsoObject, ItemContainer,
-- IsoGridSquare, IsoRoom or IsoBuilding reference is retained.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFSupplyCategoryDefinitions"
require "LCCQF/World/LCCQFWorldContainerResolver"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Categories = LCCQF.SupplyCategoryRegistry
local Resolver = LCCQF.WorldContainerResolver
local Sites = LCCQF.FactionSiteRegistry
local Stock = LCCQF.FactionSiteStock or {}
local ROOM_PROBE_CAP = 1024

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:STOCK] " .. tostring(message))
end

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    return gameTime and gameTime.getWorldAgeHours and (tonumber(gameTime:getWorldAgeHours()) or 0) or 0
end

local function buildingFingerprint(building)
    if not building then return nil end
    local def
    pcall(function() def = building:getDef() end)
    if not def then return nil end
    local x, y, w, h
    pcall(function()
        x = math.floor(tonumber(def:getX()) or 0)
        y = math.floor(tonumber(def:getY()) or 0)
        w = math.floor(tonumber(def:getW()) or 0)
        h = math.floor(tonumber(def:getH()) or 0)
    end)
    if not x or not y or not w or not h or w <= 0 or h <= 0 then return nil end
    return "building:" .. tostring(x) .. ":" .. tostring(y)
        .. ":" .. tostring(w) .. ":" .. tostring(h)
end

local function findLoadedBuilding(site)
    if type(site) ~= "table" or type(site.buildingFingerprint) ~= "string" then
        return nil, "site building fingerprint unavailable"
    end
    local cell = getCell and getCell() or nil
    if not cell or not cell.getRoomList then return nil, "cell unavailable" end

    if type(site.sample) == "table" then
        local square
        pcall(function()
            square = cell:getGridSquare(
                math.floor(tonumber(site.sample.x) or 0),
                math.floor(tonumber(site.sample.y) or 0),
                math.floor(tonumber(site.sample.z) or 0)
            )
        end)
        if square then
            local building
            pcall(function() building = square:getBuilding() end)
            if building and buildingFingerprint(building) == site.buildingFingerprint then
                return building
            end
        end
    end

    local rooms
    pcall(function() rooms = cell:getRoomList() end)
    local size = rooms and rooms:size() or 0
    for index = 0, math.min(size, ROOM_PROBE_CAP) - 1 do
        local room = rooms:get(index)
        local building
        pcall(function() building = room and room:getBuilding() end)
        if building and buildingFingerprint(building) == site.buildingFingerprint then
            return building
        end
    end
    return nil, "settlement building is not currently loaded"
end

local function addQuantity(target, key, amount)
    if type(key) ~= "string" or key == "" then return end
    target[key] = math.max(0, math.floor(tonumber(target[key]) or 0))
        + math.max(0, math.floor(tonumber(amount) or 0))
end

local function itemFullType(item)
    if not item or not item.getFullType then return nil end
    local ok, value = pcall(function() return item:getFullType() end)
    return ok and type(value) == "string" and value ~= "" and value or nil
end

local function itemCategories(item)
    if Categories and Categories.Classify then
        return Categories.Classify(item)
    end
    return {}
end

local function scanContainer(container, itemBudget)
    local quantities = {}
    local categories = {}
    local counted = 0
    local exhausted = false
    local items
    pcall(function() items = container and container:getItems() end)
    local size = items and items:size() or 0
    for index = 0, size - 1 do
        if itemBudget.remaining <= 0 then
            exhausted = true
            break
        end
        itemBudget.remaining = itemBudget.remaining - 1
        local item = items:get(index)
        local fullType = itemFullType(item)
        if fullType then
            addQuantity(quantities, fullType, 1)
            counted = counted + 1
            for category, amount in pairs(itemCategories(item)) do
                addQuantity(categories, category, amount)
            end
        end
    end
    return quantities, categories, counted, exhausted
end

local function listObjects(square, methodName)
    if not square or type(square[methodName]) ~= "function" then return nil end
    local value
    pcall(function() value = square[methodName](square) end)
    return value
end

local function scanObjectList(objects, seenObjects, snapshot, budgets)
    if not objects then return false end
    for index = 0, objects:size() - 1 do
        local object = objects:get(index)
        if object and not seenObjects[object] then
            seenObjects[object] = true
            for _, entry in ipairs(Resolver.ListObjectContainers(object)) do
                if budgets.containers.remaining <= 0 then return true end
                budgets.containers.remaining = budgets.containers.remaining - 1

                local quantities, categories, itemCount, itemExhausted = scanContainer(entry.container, budgets.items)
                local row = {
                    locator = entry.locator,
                    locatorKey = Resolver.LocatorKey(entry.locator),
                    itemCount = itemCount,
                    quantitiesByFullType = quantities,
                    categories = categories,
                }
                snapshot.containers[#snapshot.containers + 1] = row
                snapshot.containerCount = snapshot.containerCount + 1
                snapshot.itemCount = snapshot.itemCount + itemCount
                for fullType, amount in pairs(quantities) do
                    addQuantity(snapshot.quantitiesByFullType, fullType, amount)
                end
                for category, amount in pairs(categories) do
                    addQuantity(snapshot.categories, category, amount)
                end
                if itemExhausted then return true end
            end
        end
    end
    return false
end

local function scanBuilding(site, building)
    local cell = getCell and getCell() or nil
    if not cell or not cell.getRoomList then return nil, "cell unavailable" end
    local rooms
    pcall(function() rooms = cell:getRoomList() end)
    if not rooms then return nil, "loaded room list unavailable" end

    local tileLimit = math.max(64, math.floor(tonumber(C.FACTION_SITE_STOCK_SCAN_MAX_TILES) or 4096))
    local containerLimit = math.max(1, math.floor(tonumber(C.FACTION_SITE_STOCK_MAX_CONTAINERS) or 128))
    local itemLimit = math.max(1, math.floor(tonumber(C.FACTION_SITE_STOCK_MAX_ITEMS) or 4096))
    local budgets = {
        tiles = { remaining = tileLimit },
        containers = { remaining = containerLimit },
        items = { remaining = itemLimit },
    }
    local snapshot = {
        schemaVersion = 1,
        scannedWorldHours = worldHours(),
        complete = true,
        tilesVisited = 0,
        tileBudget = tileLimit,
        containerBudget = containerLimit,
        itemBudget = itemLimit,
        containerCount = 0,
        itemCount = 0,
        quantitiesByFullType = {},
        categories = {},
        containers = {},
    }
    local seenSquares = {}
    local seenObjects = {}
    local exhaustedReason = nil

    for roomIndex = 0, rooms:size() - 1 do
        local room = rooms:get(roomIndex)
        local roomBuilding
        pcall(function() roomBuilding = room and room:getBuilding() end)
        if roomBuilding == building or buildingFingerprint(roomBuilding) == site.buildingFingerprint then
            local squares
            pcall(function() squares = room:getSquares() end)
            local count = squares and squares:size() or 0
            for squareIndex = 0, count - 1 do
                if budgets.tiles.remaining <= 0 then
                    exhaustedReason = "tile budget exhausted"
                    break
                end
                local square = squares:get(squareIndex)
                if square then
                    local x, y, z = square:getX(), square:getY(), square:getZ()
                    local key = tostring(x) .. ":" .. tostring(y) .. ":" .. tostring(z)
                    if not seenSquares[key] then
                        seenSquares[key] = true
                        budgets.tiles.remaining = budgets.tiles.remaining - 1
                        snapshot.tilesVisited = snapshot.tilesVisited + 1

                        local exhausted = scanObjectList(
                            listObjects(square, "getObjects"), seenObjects, snapshot, budgets
                        )
                        if not exhausted then
                            exhausted = scanObjectList(
                                listObjects(square, "getSpecialObjects"), seenObjects, snapshot, budgets
                            )
                        end
                        if exhausted then
                            exhaustedReason = budgets.containers.remaining <= 0
                                and "container budget exhausted" or "item budget exhausted"
                            break
                        end
                    end
                end
            end
        end
        if exhaustedReason then break end
    end

    if snapshot.tilesVisited == 0 then return nil, "settlement building has no loaded room squares" end
    snapshot.complete = exhaustedReason == nil
    snapshot.incompleteReason = exhaustedReason
    table.sort(snapshot.containers, function(a, b)
        return tostring(a.locatorKey) < tostring(b.locatorKey)
    end)
    return snapshot
end

local function sortedQuantitySignature(quantities)
    local keys = {}
    for key in pairs(type(quantities) == "table" and quantities or {}) do
        keys[#keys + 1] = tostring(key)
    end
    table.sort(keys)
    local parts = {}
    for _, key in ipairs(keys) do
        parts[#parts + 1] = key .. "=" .. tostring(math.floor(tonumber(quantities[key]) or 0))
    end
    return table.concat(parts, ",")
end

local function contentSignature(snapshot)
    if type(snapshot) ~= "table" then return "" end
    local parts = {
        tostring(snapshot.containerCount or 0),
        tostring(snapshot.itemCount or 0),
        sortedQuantitySignature(snapshot.quantitiesByFullType),
        sortedQuantitySignature(snapshot.categories),
    }
    for _, row in ipairs(snapshot.containers or {}) do
        parts[#parts + 1] = tostring(row.locatorKey or "")
            .. "#" .. tostring(row.itemCount or 0)
            .. "#" .. sortedQuantitySignature(row.quantitiesByFullType)
            .. "#" .. sortedQuantitySignature(row.categories)
    end
    return table.concat(parts, "|")
end

function Stock.Scan(site)
    local building, errorText = findLoadedBuilding(site)
    if not building then return nil, errorText end
    local snapshot, scanError = scanBuilding(site, building)
    if not snapshot then return nil, scanError end
    log("siteId=" .. tostring(site.siteId)
        .. " tiles=" .. tostring(snapshot.tilesVisited)
        .. " containers=" .. tostring(snapshot.containerCount)
        .. " items=" .. tostring(snapshot.itemCount)
        .. " food=" .. tostring(snapshot.categories.food or 0)
        .. " complete=" .. tostring(snapshot.complete)
        .. (snapshot.incompleteReason and " reason=" .. tostring(snapshot.incompleteReason) or ""))
    return snapshot
end

function Stock.ApplySnapshot(site, snapshot)
    if type(site) ~= "table" or type(snapshot) ~= "table" then
        return false, "invalid stock snapshot"
    end
    if snapshot.complete ~= true then
        return false, snapshot.incompleteReason or "incomplete stock snapshot"
    end

    local previous = type(site.stock) == "table" and site.stock or nil
    local changed = contentSignature(previous) ~= contentSignature(snapshot)
    local revision = previous and math.max(0, math.floor(tonumber(previous.revision) or 0)) or 0
    snapshot.revision = changed and (revision + 1) or revision
    snapshot.verifiedWorldHours = snapshot.scannedWorldHours
    site.stock = snapshot
    if changed then Sites.MarkDirty(site.siteId, "settlement stock snapshot changed") end
    return true, changed
end

function Stock.Refresh(site)
    local snapshot, errorText = Stock.Scan(site)
    if not snapshot then return false, errorText end
    return Stock.ApplySnapshot(site, snapshot)
end

function Stock.GetQuantity(siteOrId, fullType)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    local quantities = site and site.stock and site.stock.quantitiesByFullType or nil
    return math.max(0, math.floor(tonumber(quantities and quantities[fullType]) or 0))
end

function Stock.GetCategoryQuantity(siteOrId, category)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    local categories = site and site.stock and site.stock.categories or nil
    return math.max(0, math.floor(tonumber(categories and categories[category]) or 0))
end

function Stock.FindContainersForItem(siteOrId, fullType)
    local site = type(siteOrId) == "table" and siteOrId or Sites.GetSite(siteOrId)
    local out = {}
    for _, row in ipairs(site and site.stock and site.stock.containers or {}) do
        local amount = math.max(0, math.floor(tonumber(row.quantitiesByFullType and row.quantitiesByFullType[fullType]) or 0))
        if amount > 0 then
            out[#out + 1] = { locator = row.locator, locatorKey = row.locatorKey, amount = amount }
        end
    end
    return out
end

LCCQF.FactionSiteStock = Stock
return Stock
