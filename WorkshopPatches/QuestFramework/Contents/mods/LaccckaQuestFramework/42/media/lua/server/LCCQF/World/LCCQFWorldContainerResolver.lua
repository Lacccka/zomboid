-- Server-authoritative exact locator for loaded world ItemContainers.
-- Persistent callers must keep only the plain locator returned by MakeLocator;
-- never keep IsoObject/ItemContainer references in ModData.
if isClient and isClient() and not (isServer and isServer()) then return {} end

LCCQF = LCCQF or {}
local Resolver = LCCQF.WorldContainerResolver or {}

local function excluded(object)
    if not object then return true end
    if instanceof then
        local ok, value = pcall(function()
            return instanceof(object, "IsoDeadBody")
                or instanceof(object, "IsoWorldInventoryObject")
                or instanceof(object, "IsoPlayer")
                or instanceof(object, "IsoZombie")
        end)
        if ok and value then return true end
    end
    return false
end

local function primaryContainer(object)
    if excluded(object) then return nil end
    if object.getContainer then
        local ok, container = pcall(function() return object:getContainer() end)
        if ok and container then return container end
    end
    if object.getItemContainer then
        local ok, container = pcall(function() return object:getItemContainer() end)
        if ok and container then return container end
    end
    return nil
end

local function containerCount(object)
    if excluded(object) then return 0 end
    if object.getContainerCount then
        local ok, count = pcall(function() return object:getContainerCount() end)
        count = ok and tonumber(count) or nil
        if count and count > 0 then return math.floor(count) end
    end
    -- Build 42 compatibility: ordinary furniture may report zero compartments while
    -- getContainer()/getItemContainer() still returns a valid primary ItemContainer.
    return primaryContainer(object) and 1 or 0
end

local function containerByIndex(object, index)
    if excluded(object) then return nil end
    index = math.max(0, math.floor(tonumber(index) or 0))
    if object.getContainerByIndex then
        local ok, container = pcall(function() return object:getContainerByIndex(index) end)
        if ok and container then return container end
    end
    return index == 0 and primaryContainer(object) or nil
end

local function containerType(container)
    if not container or not container.getType then return "" end
    local ok, value = pcall(function() return container:getType() end)
    return ok and value and tostring(value) or ""
end

local function spriteName(object)
    if not object or not object.getSprite then return "" end
    local ok, value = pcall(function()
        local sprite = object:getSprite()
        return sprite and sprite.getName and sprite:getName() or nil
    end)
    return ok and value and tostring(value) or ""
end

local function objectIndex(object)
    if not object or not object.getObjectIndex then return -1 end
    local ok, value = pcall(function() return object:getObjectIndex() end)
    return ok and tonumber(value) and math.floor(tonumber(value)) or -1
end

local function listSize(list)
    if not list or not list.size then return 0 end
    local ok, size = pcall(function() return list:size() end)
    return ok and math.max(0, math.floor(tonumber(size) or 0)) or 0
end

local function listGet(list, index)
    if not list or not list.get then return nil end
    local ok, value = pcall(function() return list:get(index) end)
    return ok and value or nil
end

local function indexInList(list, object)
    for index = 0, listSize(list) - 1 do
        if listGet(list, index) == object then return index end
    end
    return -1
end

local function squareLists(square)
    local objects, special
    if square and square.getObjects then
        pcall(function() objects = square:getObjects() end)
    end
    if square and square.getSpecialObjects then
        pcall(function() special = square:getSpecialObjects() end)
    end
    return objects, special
end

local function containerIndex(object, container)
    if not object or not container then return -1 end
    if object.getContainerIndex then
        local ok, value = pcall(function() return object:getContainerIndex(container) end)
        if ok and tonumber(value) then return math.floor(tonumber(value)) end
    end
    for index = 0, containerCount(object) - 1 do
        if containerByIndex(object, index) == container then return index end
    end
    return -1
end

function Resolver.MakeLocator(object, container)
    if excluded(object) or not container or not object.getSquare then return nil end
    local square
    pcall(function() square = object:getSquare() end)
    if not square then return nil end

    local cIndex = containerIndex(object, container)
    if cIndex < 0 then return nil end

    local objects, special = squareLists(square)
    local normalIndex = indexInList(objects, object)
    local specialIndex = indexInList(special, object)
    local collection = normalIndex >= 0 and "objects" or (specialIndex >= 0 and "special" or "unknown")
    local collectionIndex = normalIndex >= 0 and normalIndex or specialIndex

    return {
        schemaVersion = 1,
        x = square:getX(),
        y = square:getY(),
        z = square:getZ(),
        objectCollection = collection,
        collectionIndex = collectionIndex,
        objectIndex = objectIndex(object),
        containerIndex = cIndex,
        containerType = containerType(container),
        spriteName = spriteName(object),
    }
end

local function objectMatches(object, locator)
    if excluded(object) or containerCount(object) <= 0 then return false end
    local expectedSprite = tostring(locator.spriteName or "")
    return expectedSprite == "" or spriteName(object) == expectedSprite
end

local function resolveContainerFromObject(object, locator)
    if not objectMatches(object, locator) then return nil end
    local wantedIndex = tonumber(locator.containerIndex)
    local wantedType = tostring(locator.containerType or "")
    if wantedIndex and wantedIndex >= 0 then
        local container = containerByIndex(object, wantedIndex)
        if container and (wantedType == "" or containerType(container) == wantedType) then
            return container
        end
    end
    if wantedType ~= "" and object.getContainerByType then
        local ok, container = pcall(function() return object:getContainerByType(wantedType) end)
        if ok and container then return container end
    end
    return nil
end

local function resolveFromList(list, locator, preferredIndex)
    local size = listSize(list)
    if preferredIndex and preferredIndex >= 0 and preferredIndex < size then
        local object = listGet(list, preferredIndex)
        local container = resolveContainerFromObject(object, locator)
        if container then return object, container end
    end
    for index = 0, size - 1 do
        if index ~= preferredIndex then
            local object = listGet(list, index)
            local container = resolveContainerFromObject(object, locator)
            if container then return object, container end
        end
    end
    return nil, nil
end

function Resolver.Resolve(locator)
    if type(locator) ~= "table" or not getCell then return nil, nil end
    local x, y, z = tonumber(locator.x), tonumber(locator.y), tonumber(locator.z)
    if not x or not y or not z then return nil, nil end

    local cell = getCell()
    local square = cell and cell:getGridSquare(math.floor(x), math.floor(y), math.floor(z)) or nil
    if not square then return nil, nil end

    local objects, special = squareLists(square)
    local preferred = tonumber(locator.collectionIndex)
    preferred = preferred and math.floor(preferred) or nil
    local collection = tostring(locator.objectCollection or "")

    if collection == "special" then
        local object, container = resolveFromList(special, locator, preferred)
        if container then return object, container end
        return resolveFromList(objects, locator, tonumber(locator.objectIndex))
    end

    local normalPreferred = collection == "objects" and preferred or tonumber(locator.objectIndex)
    local object, container = resolveFromList(objects, locator, normalPreferred)
    if container then return object, container end
    return resolveFromList(special, locator, collection == "special" and preferred or nil)
end

function Resolver.ListObjectContainers(object)
    local out = {}
    for index = 0, containerCount(object) - 1 do
        local container = containerByIndex(object, index)
        if container then
            local locator = Resolver.MakeLocator(object, container)
            if locator then out[#out + 1] = { container = container, locator = locator } end
        end
    end
    return out
end

function Resolver.LocatorKey(locator)
    if type(locator) ~= "table" then return "" end
    return table.concat({
        tostring(math.floor(tonumber(locator.x) or 0)),
        tostring(math.floor(tonumber(locator.y) or 0)),
        tostring(math.floor(tonumber(locator.z) or 0)),
        tostring(locator.objectCollection or "unknown"),
        tostring(math.floor(tonumber(locator.collectionIndex) or -1)),
        tostring(math.floor(tonumber(locator.objectIndex) or -1)),
        tostring(math.floor(tonumber(locator.containerIndex) or -1)),
        tostring(locator.containerType or ""),
        tostring(locator.spriteName or ""),
    }, ":")
end

LCCQF.WorldContainerResolver = Resolver
return Resolver
