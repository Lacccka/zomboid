-- Client-side observer for vanilla inventory transfers into settlement containers.
-- The client reports only pre-transaction item intents. The server decides whether the
-- destination belongs to a faction site and confirms the same item IDs after vanilla MP sync.
require "LCCQF/LCCQFConstants"
require "TimedActions/ISInventoryTransferAction"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Observer = LCCQF.SettlementTransferClientObserver or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:TRANSFER:CLIENT] " .. tostring(message))
end

local function excludedObject(object)
    if not object then return true end
    if instanceof then
        local ok, excluded = pcall(function()
            return instanceof(object, "IsoDeadBody")
                or instanceof(object, "IsoWorldInventoryObject")
                or instanceof(object, "IsoPlayer")
                or instanceof(object, "IsoZombie")
        end)
        if ok and excluded then return true end
    end
    return false
end

local function listSize(list)
    if not list or not list.size then return 0 end
    local ok, value = pcall(function() return list:size() end)
    return ok and math.max(0, math.floor(tonumber(value) or 0)) or 0
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

local function primaryContainer(object)
    if excludedObject(object) then return nil end
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
    if excludedObject(object) then return 0 end
    if object.getContainerCount then
        local ok, value = pcall(function() return object:getContainerCount() end)
        local count = ok and tonumber(value) or nil
        if count and count > 0 then return math.floor(count) end
    end
    return primaryContainer(object) and 1 or 0
end

local function containerByIndex(object, index)
    index = math.max(0, math.floor(tonumber(index) or 0))
    if object and object.getContainerByIndex then
        local ok, container = pcall(function() return object:getContainerByIndex(index) end)
        if ok and container then return container end
    end
    return index == 0 and primaryContainer(object) or nil
end

local function containerIndex(object, container)
    if not object or not container then return -1 end
    if object.getContainerIndex then
        local ok, value = pcall(function() return object:getContainerIndex(container) end)
        if ok and tonumber(value) and tonumber(value) >= 0 then
            return math.floor(tonumber(value))
        end
    end
    for index = 0, containerCount(object) - 1 do
        if containerByIndex(object, index) == container then return index end
    end
    return -1
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

local function buildLocator(container)
    if not container or not container.getParent then return nil end
    local object
    pcall(function() object = container:getParent() end)
    if excludedObject(object) or not object or not object.getSquare then return nil end

    local square
    pcall(function() square = object:getSquare() end)
    if not square then return nil end

    local cIndex = containerIndex(object, container)
    if cIndex < 0 then return nil end

    local objects, special
    if square.getObjects then pcall(function() objects = square:getObjects() end) end
    if square.getSpecialObjects then pcall(function() special = square:getSpecialObjects() end) end
    local normalIndex = indexInList(objects, object)
    local specialIndex = indexInList(special, object)
    local collection = normalIndex >= 0 and "objects" or (specialIndex >= 0 and "special" or "unknown")
    local collectionIndex = normalIndex >= 0 and normalIndex or specialIndex

    return {
        x = tonumber(square:getX()) or 0,
        y = tonumber(square:getY()) or 0,
        z = tonumber(square:getZ()) or 0,
        objectCollection = collection,
        collectionIndex = collectionIndex,
        objectIndex = objectIndex(object),
        containerIndex = cIndex,
        containerType = containerType(container),
        spriteName = spriteName(object),
    }
end

local function sourceOwnedByCharacter(container, character)
    if not container or not character then return false end
    if character.getInventory and container == character:getInventory() then return true end
    if container.isInCharacterInventory then
        local ok, value = pcall(function() return container:isInCharacterInventory(character) end)
        if ok and value == true then return true end
    end
    return false
end

local function itemId(item)
    if not item or not item.getID then return nil end
    local ok, value = pcall(function() return item:getID() end)
    value = ok and tonumber(value) or nil
    if not value then return nil end
    return math.floor(value)
end

local function reportItemIntent(action, item)
    if not action or not action.character or not item then return end
    if not sourceOwnedByCharacter(action.srcContainer, action.character) then return end

    local id = itemId(item)
    local locator = buildLocator(action.destContainer)
    if not id or not locator or type(sendClientCommand) ~= "function" then return end

    sendClientCommand(C.MODULE, C.COMMAND.REPORT_SETTLEMENT_TRANSFER_INTENT, {
        itemId = id,
        x = locator.x,
        y = locator.y,
        z = locator.z,
        objectCollection = locator.objectCollection,
        collectionIndex = locator.collectionIndex,
        objectIndex = locator.objectIndex,
        containerIndex = locator.containerIndex,
        containerType = locator.containerType,
        spriteName = locator.spriteName,
    })
end

local function reportQueuedIntents(action)
    if not action or type(action.queueList) ~= "table" then return end
    for _, group in ipairs(action.queueList) do
        for _, item in ipairs(type(group) == "table" and group.items or {}) do
            reportItemIntent(action, item)
        end
    end
end

function Observer.Install()
    if Observer.installed == true then return true end
    if not ISInventoryTransferAction
        or type(ISInventoryTransferAction.new) ~= "function"
        or type(ISInventoryTransferAction.checkQueueList) ~= "function"
    then
        return false
    end

    local originalNew = ISInventoryTransferAction.new
    local originalCheckQueueList = ISInventoryTransferAction.checkQueueList
    Observer.originalNew = originalNew
    Observer.originalCheckQueueList = originalCheckQueueList

    function ISInventoryTransferAction:new(character, item, srcContainer, destContainer, time)
        local action = originalNew(self, character, item, srcContainer, destContainer, time)
        reportItemIntent(action, item)
        return action
    end

    function ISInventoryTransferAction:checkQueueList()
        local result = originalCheckQueueList(self)
        reportQueuedIntents(self)
        return result
    end

    Observer.installed = true
    log("vanilla transfer intent hooks installed")
    return true
end

Observer.Install()
if Events and Events.OnGameStart then Events.OnGameStart.Add(Observer.Install) end

LCCQF.SettlementTransferClientObserver = Observer
return Observer
