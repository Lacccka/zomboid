-- QP Supply Requests
-- QPSR_CONTRACT_ACTIVATION_LOCK_HOTFIX_V071
-- QPSR_LOCKED_NOTICE_DEBOUNCE_V072
-- v0.6.5: net contribution credit with exact active-item donor tracking

require "ISUI/ISContextMenu"
require "ISUI/ISPanel"
require "ISUI/ISButton"
require "ISUI/ISTextEntryBox"
require "ISUI/ISComboBox"
require "ISUI/ISScrollingListBox"
require "TimedActions/ISInventoryTransferAction"
require "QPSupplyRequests/QPSR_Shared"
require "QPSupplyRequests/QPSR_I18N"

QPSR_Client = QPSR_Client or {}
QPSR_Client.createWindow = QPSR_Client.createWindow or nil
QPSR_Client.viewWindow = QPSR_Client.viewWindow or nil
QPSR_Client.itemCatalog = QPSR_Client.itemCatalog or nil
QPSR_Client.itemCatalogBuild = QPSR_Client.itemCatalogBuild or nil
QPSR_Client.pendingAction = QPSR_Client.pendingAction or nil
QPSR_Client.TransferHookInstalled = QPSR_Client.TransferHookInstalled or false
QPSR_Client.OriginalTransferNew = QPSR_Client.OriginalTransferNew or nil
QPSR_Client.OriginalTransferItem = QPSR_Client.OriginalTransferItem or nil
QPSR_Client.lastProtectionNoticeAt = QPSR_Client.lastProtectionNoticeAt or 0
QPSR_Client.lastActivationLockedNoticeAt = QPSR_Client.lastActivationLockedNoticeAt or 0

local function QPSR_dummy()
end

local function QPSR_trim(text)
    return tostring(text or ""):match("^%s*(.-)%s*$")
end

local function QPSR_limitText(text, maximumLength)
    local value = QPSR_trim(text)

    if string.len(value) > maximumLength then
        return string.sub(value, 1, maximumLength)
    end

    return value
end

local function QPSR_say(player, text)
    if player == nil then
        player = getPlayer()
    end

    if player ~= nil then
        player:Say(tostring(text or ""))
    end
end

local function QPSR_isExcludedWorldObject(object)
    if object == nil then
        return true
    end

    if instanceof then
        if instanceof(object, "IsoDeadBody") then
            return true
        end

        if instanceof(object, "IsoWorldInventoryObject") then
            return true
        end

        if instanceof(object, "IsoPlayer") or
           instanceof(object, "IsoZombie") then
            return true
        end
    end

    return false
end

-- QPSR_CONTAINER_API_COMPAT_V2
-- Build 42 world furniture is not consistent about which primary-container
-- accessor it exposes. Refrigerators commonly work through the indexed
-- container API, while many normal furniture containers expose their storage
-- through getItemContainer() even when getContainer() returns nil.
local function QPSR_getContainer(object)
    if QPSR_isExcludedWorldObject(object) then
        return nil
    end

    if object.getContainer ~= nil then
        local ok, container = pcall(function()
            return object:getContainer()
        end)

        if ok and container ~= nil then
            return container
        end
    end

    if object.getItemContainer ~= nil then
        local ok, container = pcall(function()
            return object:getItemContainer()
        end)

        if ok and container ~= nil then
            return container
        end
    end

    return nil
end

-- QPSR_EXACT_CONTAINER_TARGET_V1
local function QPSR_getContainerCount(object)
    if QPSR_isExcludedWorldObject(object) then
        return 0
    end

    if object.getContainerCount ~= nil then
        local ok, count = pcall(function()
            return object:getContainerCount()
        end)

        if ok and tonumber(count) ~= nil then
            local resolvedCount = math.max(0, math.floor(tonumber(count)))
            if resolvedCount > 0 then
                return resolvedCount
            end
        end
    end

    -- QPSR_SINGLE_CONTAINER_COMPAT_V1
    -- Many normal Build 42 world containers expose getContainerCount() but
    -- report zero even though getContainer() returns a valid ItemContainer.
    -- Treat those as a single-compartment container; true multi-compartment
    -- objects such as refrigerators still return their real count above.
    return QPSR_getContainer(object) ~= nil and 1 or 0
end

local function QPSR_getContainerByIndex(object, containerIndex)
    if QPSR_isExcludedWorldObject(object) then
        return nil
    end

    local index = math.floor(tonumber(containerIndex) or 0)

    if object.getContainerByIndex ~= nil then
        local ok, container = pcall(function()
            return object:getContainerByIndex(index)
        end)

        if ok and container ~= nil then
            return container
        end
    end

    if index == 0 then
        return QPSR_getContainer(object)
    end

    return nil
end

local function QPSR_getContainerTypeValue(container)
    if container == nil or container.getType == nil then
        return ""
    end

    local ok, value = pcall(function()
        return container:getType()
    end)

    if not ok or value == nil then
        return ""
    end

    return tostring(value)
end

local function QPSR_getContainerIndexValue(object, container)
    if object == nil or container == nil then
        return -1
    end

    if object.getContainerIndex ~= nil then
        local ok, index = pcall(function()
            return object:getContainerIndex(container)
        end)

        if ok and tonumber(index) ~= nil then
            return math.floor(tonumber(index))
        end
    end

    local count = QPSR_getContainerCount(object)
    for index = 0, count - 1 do
        if QPSR_getContainerByIndex(object, index) == container then
            return index
        end
    end

    return -1
end

local function QPSR_getContainerDescriptor(object, containerIndex)
    local index = math.floor(tonumber(containerIndex) or 0)
    local container = QPSR_getContainerByIndex(object, index)

    if container == nil then
        return nil
    end

    local containerType = QPSR_getContainerTypeValue(container)

    return {
        index = index,
        type = containerType,
        label = QPSR_I18N.getContainerLabel(containerType, index)
    }
end

local function QPSR_getContainerDescriptors(object)
    local descriptors = {}
    local count = QPSR_getContainerCount(object)

    for index = 0, count - 1 do
        local descriptor = QPSR_getContainerDescriptor(object, index)
        if descriptor ~= nil then
            table.insert(descriptors, descriptor)
        end
    end

    -- QPSR_PRIMARY_CONTAINER_DESCRIPTOR_FALLBACK_V1
    -- Some single-compartment furniture reports inconsistent indexed-container
    -- data. If the primary ItemContainer exists, never hide the QPSR menu only
    -- because getContainerCount()/getContainerByIndex() disagreed.
    if #descriptors == 0 then
        local container = QPSR_getContainer(object)

        if container ~= nil then
            local index = QPSR_getContainerIndexValue(object, container)
            if index < 0 then
                index = 0
            end

            local containerType = QPSR_getContainerTypeValue(container)
            table.insert(descriptors, {
                index = index,
                type = containerType,
                label = QPSR_I18N.getContainerLabel(containerType, index)
            })
        end
    end

    return descriptors
end

local function QPSR_getRequestContainer(object, request)
    if object == nil then
        return nil
    end

    local requestedIndex = request ~= nil and tonumber(request.containerIndex) or nil
    local requestedType = request ~= nil and tostring(request.containerType or "") or ""

    if requestedIndex ~= nil and requestedIndex >= 0 then
        local indexedContainer = QPSR_getContainerByIndex(object, requestedIndex)
        if indexedContainer ~= nil then
            local actualType = QPSR_getContainerTypeValue(indexedContainer)
            if requestedType == "" or actualType == requestedType then
                return indexedContainer
            end
        end
    end

    if requestedType ~= "" and object.getContainerByType ~= nil then
        local ok, typedContainer = pcall(function()
            return object:getContainerByType(requestedType)
        end)

        if ok and typedContainer ~= nil then
            return typedContainer
        end
    end

    return QPSR_getContainer(object)
end

-- QPSR_ROBUST_CONTEXT_CONTAINER_DISCOVERY_V2
local function QPSR_objectHasContainers(object)
    return QPSR_getContainerCount(object) > 0
end

local function QPSR_findContainerInObjectList(objects)
    if objects == nil then
        return nil
    end

    local okSize, size = pcall(function()
        return objects:size()
    end)

    if not okSize or tonumber(size) == nil then
        return nil
    end

    for index = 0, math.floor(tonumber(size)) - 1 do
        local okObject, object = pcall(function()
            return objects:get(index)
        end)

        if okObject and QPSR_objectHasContainers(object) then
            return object
        end
    end

    return nil
end

local function QPSR_findContainerInSquare(square)
    if square == nil then
        return nil
    end

    if square.getObjects ~= nil then
        local ok, objects = pcall(function()
            return square:getObjects()
        end)

        if ok then
            local object = QPSR_findContainerInObjectList(objects)
            if object ~= nil then
                return object
            end
        end
    end

    -- Player-built storage and some Build 42 furniture are exposed through
    -- the square's special-object list instead of the normal object list.
    if square.getSpecialObjects ~= nil then
        local ok, specialObjects = pcall(function()
            return square:getSpecialObjects()
        end)

        if ok then
            local object = QPSR_findContainerInObjectList(specialObjects)
            if object ~= nil then
                return object
            end
        end
    end

    return nil
end

local function QPSR_getSquareKey(square)
    if square == nil then
        return nil
    end

    local ok, key = pcall(function()
        return tostring(square:getX())
            .. ":"
            .. tostring(square:getY())
            .. ":"
            .. tostring(square:getZ())
    end)

    if not ok then
        return nil
    end

    return key
end

local function QPSR_findContainerObject(worldObjects)
    if worldObjects == nil then
        return nil
    end

    local squares = {}
    local seenSquares = {}

    -- First prefer an exact object supplied by the context-menu event.
    for _, object in ipairs(worldObjects) do
        if QPSR_objectHasContainers(object) then
            return object
        end

        if object ~= nil and object.getSquare ~= nil then
            local ok, square = pcall(function()
                return object:getSquare()
            end)

            if ok and square ~= nil then
                local key = QPSR_getSquareKey(square)
                if key == nil or not seenSquares[key] then
                    if key ~= nil then
                        seenSquares[key] = true
                    end
                    table.insert(squares, square)
                end
            end
        end
    end

    -- Build 42 does not always put the storage object itself in worldObjects.
    -- Scan every represented square, including both normal and special lists.
    for _, square in ipairs(squares) do
        local object = QPSR_findContainerInSquare(square)
        if object ~= nil then
            return object
        end
    end

    return nil
end

local function QPSR_getObjectModData(object)
    if object == nil or object.getModData == nil then
        return nil
    end

    local ok, modData = pcall(function()
        return object:getModData()
    end)

    if not ok then
        return nil
    end

    return modData
end

local function QPSR_transmitObjectModData(object)
    if object == nil or object.transmitModData == nil then
        return
    end

    pcall(function()
        object:transmitModData()
    end)
end


-- QPSR_MULTIPLAYER_OBJECT_LOCATOR_V1
local function QPSR_isMultiplayerClient()
    if isClient == nil then
        return false
    end

    local ok, result = pcall(function()
        return isClient()
    end)

    return ok and result == true
end

local function QPSR_sendActionCommand(player, action, command, args)
    if QPSR_Client.pendingAction ~= nil then
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_ErrorActionPending"))
        return false
    end

    QPSR_Client.pendingAction = tostring(action or "")

    local ok = pcall(function()
        sendClientCommand(
            QPSR_Shared.MODULE,
            command,
            args or {}
        )
    end)

    if not ok then
        QPSR_Client.pendingAction = nil
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_ErrorServerRejected"))
        return false
    end

    return true
end

local function QPSR_normalizeAccessLevel(access)
    return string.lower(tostring(access or ""))
end

local function QPSR_isLocalAdmin(player)
    if not QPSR_isMultiplayerClient() then
        return true
    end

    if isAdmin ~= nil then
        local ok, result = pcall(function()
            return isAdmin()
        end)

        if ok and result == true then
            return true
        end
    end

    if player ~= nil and player.getAccessLevel ~= nil then
        local ok, access = pcall(function()
            return player:getAccessLevel()
        end)

        if ok and QPSR_normalizeAccessLevel(access) == "admin" then
            return true
        end
    end

    if getAccessLevel ~= nil then
        local ok, access = pcall(function()
            return getAccessLevel()
        end)

        if ok and QPSR_normalizeAccessLevel(access) == "admin" then
            return true
        end
    end

    return false
end

local function QPSR_getObjectIndex(object)
    if object == nil then
        return -1
    end

    if object.getObjectIndex ~= nil then
        local ok, index = pcall(function()
            return object:getObjectIndex()
        end)

        if ok and tonumber(index) ~= nil then
            return tonumber(index)
        end
    end

    if object.getSquare == nil then
        return -1
    end

    local ok, square = pcall(function()
        return object:getSquare()
    end)

    if not ok or square == nil or square.getObjects == nil then
        return -1
    end

    local objects = square:getObjects()
    if objects == nil then
        return -1
    end

    for index = 0, objects:size() - 1 do
        if objects:get(index) == object then
            return index
        end
    end

    return -1
end

local function QPSR_getContainerType(object)
    return QPSR_getContainerTypeValue(
        QPSR_getContainerByIndex(object, 0)
    )
end

local function QPSR_getSpriteName(object)
    if object == nil or object.getSprite == nil then
        return ""
    end

    local ok, value = pcall(function()
        local sprite = object:getSprite()
        if sprite ~= nil and sprite.getName ~= nil then
            return sprite:getName()
        end
        return nil
    end)

    if not ok or value == nil then
        return ""
    end

    return tostring(value)
end

local function QPSR_buildObjectLocator(object, requestContainerIndex)
    if object == nil or object.getSquare == nil then
        return nil
    end

    local ok, square = pcall(function()
        return object:getSquare()
    end)

    if not ok or square == nil then
        return nil
    end

    local payload = {
        x = tonumber(square:getX()) or 0,
        y = tonumber(square:getY()) or 0,
        z = tonumber(square:getZ()) or 0,
        objectIndex = QPSR_getObjectIndex(object),
        containerType = QPSR_getContainerType(object),
        spriteName = QPSR_getSpriteName(object)
    }

    if requestContainerIndex ~= nil then
        local descriptor = QPSR_getContainerDescriptor(object, requestContainerIndex)
        if descriptor == nil then
            return nil
        end

        payload.requestContainerIndex = descriptor.index
        payload.requestContainerType = descriptor.type
    end

    return payload
end

local function QPSR_objectMatchesLocator(object, args)
    if not QPSR_objectHasContainers(object) then
        return false
    end

    local expectedContainer = tostring(args.containerType or "")
    if expectedContainer ~= "" and QPSR_getContainerType(object) ~= expectedContainer then
        return false
    end

    local expectedSprite = tostring(args.spriteName or "")
    if expectedSprite ~= "" and QPSR_getSpriteName(object) ~= expectedSprite then
        return false
    end

    return true
end

local function QPSR_resolveObjectLocator(args)
    args = args or {}

    local x = tonumber(args.x)
    local y = tonumber(args.y)
    local z = tonumber(args.z)
    local objectIndex = tonumber(args.objectIndex)

    if x == nil or y == nil or z == nil or getCell == nil then
        return nil
    end

    local ok, square = pcall(function()
        return getCell():getGridSquare(math.floor(x), math.floor(y), math.floor(z))
    end)

    if not ok or square == nil or square.getObjects == nil then
        return nil
    end

    local objects = square:getObjects()
    if objects == nil then
        return nil
    end

    if objectIndex ~= nil and objectIndex >= 0 and objectIndex < objects:size() then
        local candidate = objects:get(math.floor(objectIndex))
        if QPSR_objectMatchesLocator(candidate, args) then
            return candidate
        end
    end

    for index = 0, objects:size() - 1 do
        local candidate = objects:get(index)
        if QPSR_objectMatchesLocator(candidate, args) then
            return candidate
        end
    end

    return nil
end

-- QPSR_STABLE_ITEM_CATALOG_V1
local function QPSR_getScriptFullType(scriptItem)
    if scriptItem == nil or scriptItem.getFullName == nil then
        return ""
    end

    local ok, value = pcall(function()
        return scriptItem:getFullName()
    end)

    if not ok or value == nil then
        return ""
    end

    return tostring(value)
end

local function QPSR_getScriptDisplayName(scriptItem)
    if scriptItem == nil or scriptItem.getDisplayName == nil then
        return ""
    end

    local ok, value = pcall(function()
        return scriptItem:getDisplayName()
    end)

    if not ok or value == nil then
        return ""
    end

    return tostring(value)
end

local function QPSR_getScriptTypeName(scriptItem)
    if scriptItem == nil or scriptItem.getName == nil then
        return ""
    end

    local ok, value = pcall(function()
        return scriptItem:getName()
    end)

    if not ok or value == nil then
        return ""
    end

    return tostring(value)
end

local function QPSR_isScriptItemObsolete(scriptItem)
    if scriptItem == nil or scriptItem.getObsolete == nil then
        return false
    end

    local ok, value = pcall(function()
        return scriptItem:getObsolete()
    end)

    return ok and value == true
end

local function QPSR_isScriptItemHidden(scriptItem)
    if scriptItem == nil or scriptItem.isHidden == nil then
        return false
    end

    local ok, value = pcall(function()
        return scriptItem:isHidden()
    end)

    return ok and value == true
end

local function QPSR_getAllScriptItems()
    local ok, result = pcall(function()
        if getScriptManager ~= nil then
            return getScriptManager():getAllItems()
        end

        if getAllItems ~= nil then
            return getAllItems()
        end

        return nil
    end)

    if not ok then
        return nil
    end

    return result
end

local function QPSR_makeCatalogEntry(scriptItem)
    if scriptItem == nil then
        return nil
    end

    local fullType = QPSR_getScriptFullType(scriptItem)
    local displayName = QPSR_getScriptDisplayName(scriptItem)
    local typeName = QPSR_getScriptTypeName(scriptItem)
    local obsolete = QPSR_isScriptItemObsolete(scriptItem)
    local hidden = QPSR_isScriptItemHidden(scriptItem)

    if fullType == "" or displayName == "" or obsolete or hidden then
        return nil
    end

    return {
        fullType = fullType,
        typeName = typeName,
        displayName = displayName,
        searchText = string.lower(displayName .. " " .. fullType .. " " .. typeName)
    }
end

local function QPSR_startItemCatalogBuild()
    if QPSR_Client.itemCatalog ~= nil then
        return true
    end

    if QPSR_Client.itemCatalogBuild ~= nil then
        return false
    end

    local scriptItems = QPSR_getAllScriptItems()
    if scriptItems == nil then
        QPSR_Client.itemCatalog = {}
        return true
    end

    QPSR_Client.itemCatalogBuild = {
        source = scriptItems,
        index = 0,
        total = scriptItems:size(),
        catalog = {},
        seen = {}
    }

    return false
end

local function QPSR_stepItemCatalogBuild(batchSize)
    if QPSR_Client.itemCatalog ~= nil then
        return true
    end

    QPSR_startItemCatalogBuild()

    local build = QPSR_Client.itemCatalogBuild
    if build == nil then
        return QPSR_Client.itemCatalog ~= nil
    end

    local maximum = tonumber(batchSize) or 200
    local processed = 0

    while build.index < build.total and processed < maximum do
        local scriptItem = build.source:get(build.index)
        build.index = build.index + 1
        processed = processed + 1

        local entry = QPSR_makeCatalogEntry(scriptItem)
        if entry ~= nil and not build.seen[entry.fullType] then
            build.seen[entry.fullType] = true
            table.insert(build.catalog, entry)
        end
    end

    if build.index >= build.total then
        QPSR_Client.itemCatalog = build.catalog
        QPSR_Client.itemCatalogBuild = nil
        print("[QPSR] Item catalog ready: " .. tostring(#QPSR_Client.itemCatalog) .. " items")
        return true
    end

    return false
end

local function QPSR_isItemCatalogReady()
    return QPSR_Client.itemCatalog ~= nil
end

local function QPSR_findScriptItemDirect(fullType)
    local target = QPSR_trim(fullType)
    if target == "" or getScriptManager == nil then
        return nil
    end

    local ok, result = pcall(function()
        local manager = getScriptManager()
        if manager == nil then
            return nil
        end

        if manager.FindItem ~= nil then
            return manager:FindItem(target)
        end

        if manager.getItem ~= nil then
            return manager:getItem(target)
        end

        return nil
    end)

    if not ok then
        return nil
    end

    return result
end

local function QPSR_findCatalogItem(fullType)
    local target = string.lower(QPSR_trim(fullType))
    if target == "" then
        return nil
    end

    local directEntry = QPSR_makeCatalogEntry(QPSR_findScriptItemDirect(fullType))
    if directEntry ~= nil then
        return directEntry
    end

    if QPSR_Client.itemCatalog ~= nil then
        for _, entry in ipairs(QPSR_Client.itemCatalog) do
            if string.lower(tostring(entry.fullType or "")) == target then
                return entry
            end
        end
    end

    return nil
end

local function QPSR_resolveLegacyItemName(legacyName)
    local legacy = QPSR_trim(legacyName)
    if legacy == "" then
        return nil
    end

    local legacyLower = string.lower(legacy)
    local baseCandidate = QPSR_findCatalogItem("Base." .. legacy)
    if baseCandidate ~= nil then
        return baseCandidate
    end

    local typeMatches = {}
    local displayMatches = {}

    for _, entry in ipairs(QPSR_Client.itemCatalog or {}) do
        local fullTypeLower = string.lower(tostring(entry.fullType or ""))
        local typeLower = string.lower(tostring(entry.typeName or ""))
        local displayLower = string.lower(tostring(entry.displayName or ""))

        if fullTypeLower == legacyLower then
            return entry
        end

        if typeLower == legacyLower then
            table.insert(typeMatches, entry)
        end

        if displayLower == legacyLower then
            table.insert(displayMatches, entry)
        end
    end

    if #typeMatches == 1 then
        return typeMatches[1]
    end

    for _, entry in ipairs(typeMatches) do
        if string.sub(tostring(entry.fullType or ""), 1, 5) == "Base." then
            return entry
        end
    end

    if #displayMatches == 1 then
        return displayMatches[1]
    end

    for _, entry in ipairs(displayMatches) do
        if string.sub(tostring(entry.fullType or ""), 1, 5) == "Base." then
            return entry
        end
    end

    return nil
end

local function QPSR_getLocalizedItemName(fullType, fallback)
    local catalogItem = QPSR_findCatalogItem(fullType)
    if catalogItem ~= nil and tostring(catalogItem.displayName or "") ~= "" then
        return tostring(catalogItem.displayName)
    end

    local value = QPSR_trim(fallback)
    if value ~= "" then
        return value
    end

    return tostring(fullType or "")
end

local function QPSR_hasRequest(object)
    local modData = QPSR_getObjectModData(object)
    return modData ~= nil and modData[QPSR_Shared.OBJECT_HAS_REQUEST] == true
end

local function QPSR_readRequest(object)
    local modData = QPSR_getObjectModData(object)

    if modData == nil or modData[QPSR_Shared.OBJECT_HAS_REQUEST] ~= true then
        return nil
    end

    local legacyItemName = tostring(modData[QPSR_Shared.OBJECT_ITEM_NAME] or "")
    local legacyFullType = tostring(modData[QPSR_Shared.OBJECT_ITEM_FULL_TYPE] or "")
    local legacyDisplayName = tostring(modData[QPSR_Shared.OBJECT_ITEM_DISPLAY_NAME] or legacyItemName)
    local legacyTarget = tonumber(modData[QPSR_Shared.OBJECT_TARGET_AMOUNT]) or 1

    -- Resolve very old display-name-only requests in solo mode.
    if legacyFullType == "" and legacyItemName ~= "" and not QPSR_isMultiplayerClient() then
        local migratedItem = QPSR_resolveLegacyItemName(legacyItemName)
        if migratedItem ~= nil then
            legacyFullType = tostring(migratedItem.fullType or "")
            legacyDisplayName = tostring(migratedItem.displayName or legacyItemName)
        end
    end

    local itemsRaw = tostring(modData[QPSR_Shared.OBJECT_ITEMS] or "")
    local items = QPSR_Shared.decodeRequestItems(itemsRaw)

    -- v0.5.x migration: every single-item request becomes a one-line list.
    if #items == 0 and legacyFullType ~= "" then
        items = QPSR_Shared.normalizeRequestItems({{
            fullType = legacyFullType,
            displayName = legacyDisplayName,
            targetAmount = legacyTarget
        }})
        itemsRaw = QPSR_Shared.encodeRequestItems(items)
    end

    local progressRaw = tostring(modData[QPSR_Shared.OBJECT_ITEM_PROGRESS] or "")
    local progressByType = QPSR_Shared.decodeProgress(progressRaw)
    local legacyProgress = tonumber(modData[QPSR_Shared.OBJECT_LAST_PROGRESS])
    if #items == 1 and progressByType[items[1].fullType] == nil and legacyProgress ~= nil then
        progressByType[items[1].fullType] = legacyProgress
        progressRaw = QPSR_Shared.encodeProgress(progressByType)
    end

    local itemContributionsRaw = tostring(modData[QPSR_Shared.OBJECT_ITEM_CONTRIBUTIONS] or "")
    local itemContributions = QPSR_Shared.decodeItemContributions(itemContributionsRaw)
    local legacyContributionsRaw = tostring(modData[QPSR_Shared.OBJECT_CONTRIBUTIONS] or "")
    if #items == 1 and itemContributions[items[1].fullType] == nil and legacyContributionsRaw ~= "" then
        itemContributions[items[1].fullType] = QPSR_Shared.decodeContributions(legacyContributionsRaw)
        itemContributionsRaw = QPSR_Shared.encodeItemContributions(itemContributions)
    end

    local completed = modData[QPSR_Shared.OBJECT_COMPLETED] == true or
        QPSR_Shared.areAllItemsComplete(items, progressByType)
    local protectionEnabled = modData[QPSR_Shared.OBJECT_PROTECTION_ENABLED] == true
    local protectionLocked = modData[QPSR_Shared.OBJECT_PROTECTION_LOCKED] == true
    local storedReleaseState = modData[QPSR_Shared.OBJECT_SUPPLIES_RELEASED]
    local suppliesReleased = storedReleaseState == true

    -- Preserve already-unlocked v0.5.x completed requests as released.
    if storedReleaseState == nil then
        if protectionEnabled then
            suppliesReleased = protectionLocked ~= true
        else
            suppliesReleased = true
        end
    end

    if completed and protectionEnabled then
        protectionLocked = suppliesReleased ~= true
    end

    local firstItem = items[1] or {
        fullType = legacyFullType,
        displayName = legacyDisplayName,
        targetAmount = legacyTarget
    }

    return {
        title = tostring(modData[QPSR_Shared.OBJECT_TITLE] or ""),
        items = items,
        itemsRaw = itemsRaw,
        progressByType = progressByType,
        progressRaw = progressRaw,
        itemContributions = itemContributions,
        itemContributionsRaw = itemContributionsRaw,
        itemName = legacyItemName,
        itemFullType = tostring(firstItem.fullType or ""),
        itemDisplayName = tostring(firstItem.displayName or legacyDisplayName),
        targetAmount = tonumber(firstItem.targetAmount) or legacyTarget,
        priority = tostring(modData[QPSR_Shared.OBJECT_PRIORITY] or "Normal"),
        note = tostring(modData[QPSR_Shared.OBJECT_NOTE] or ""),
        createdBy = tostring(modData[QPSR_Shared.OBJECT_CREATED_BY] or ""),
        createdAt = tonumber(modData[QPSR_Shared.OBJECT_CREATED_AT]) or 0,
        containerIndex = tonumber(modData[QPSR_Shared.OBJECT_CONTAINER_INDEX]),
        containerType = tostring(modData[QPSR_Shared.OBJECT_CONTAINER_TYPE] or ""),
        fulfilledBy = tostring(modData[QPSR_Shared.OBJECT_FULFILLED_BY] or ""),
        fulfilledAt = tonumber(modData[QPSR_Shared.OBJECT_FULFILLED_AT]) or 0,
        protectionEnabled = protectionEnabled,
        protectionLocked = protectionLocked,
        contributionsRaw = legacyContributionsRaw,
        lastProgress = legacyProgress,
        completed = completed,
        suppliesReleased = suppliesReleased,
        activationMode = tostring(modData[QPSR_Shared.OBJECT_ACTIVATION_MODE] or QPSR_Shared.ACTIVATION_IMMEDIATE),
        linkedContractId = tostring(modData[QPSR_Shared.OBJECT_LINKED_CONTRACT_ID] or ""),
        activationState = tostring(modData[QPSR_Shared.OBJECT_ACTIVATION_STATE] or QPSR_Shared.STATE_ACTIVE),
        activatedAt = tonumber(modData[QPSR_Shared.OBJECT_ACTIVATED_AT]) or 0,
        activationEventKey = tostring(modData[QPSR_Shared.OBJECT_ACTIVATION_EVENT_KEY] or "")
    }
end

local function QPSR_getPlayerName(player)
    if player == nil then
        return ""
    end

    if player.getUsername ~= nil then
        local ok, username = pcall(function()
            return tostring(player:getUsername() or "")
        end)

        if ok and username ~= "" and username ~= "None" and username ~= "none" then
            return username
        end
    end

    if player.getDisplayName ~= nil then
        local ok, displayName = pcall(function()
            return tostring(player:getDisplayName() or "")
        end)

        if ok and displayName ~= "" then
            return displayName
        end
    end

    return ""
end

-- QPSR_SAFEHOUSE_PERMISSION_V1
local function QPSR_getSafehouseForObject(object)
    if object == nil or object.getSquare == nil or SafeHouse == nil then
        return nil
    end

    local okSquare, square = pcall(function()
        return object:getSquare()
    end)

    if not okSquare or square == nil then
        return nil
    end

    if SafeHouse.getSafeHouse ~= nil then
        local ok, safehouse = pcall(function()
            return SafeHouse.getSafeHouse(square)
        end)

        if ok then
            return safehouse
        end
    end

    if SafeHouse.isSafeHouse ~= nil then
        local ok, safehouse = pcall(function()
            return SafeHouse.isSafeHouse(square, nil, false)
        end)

        if ok then
            return safehouse
        end
    end

    return nil
end

local function QPSR_getFactionForUsername(username)
    local name = QPSR_trim(username)
    if name == "" or Faction == nil or Faction.getPlayerFaction == nil then
        return nil
    end

    local ok, faction = pcall(function()
        return Faction.getPlayerFaction(name)
    end)

    if not ok then
        return nil
    end

    return faction
end

local function QPSR_getFactionName(faction)
    if faction == nil or faction.getName == nil then
        return ""
    end

    local ok, value = pcall(function()
        return faction:getName()
    end)

    if not ok or value == nil then
        return ""
    end

    return tostring(value)
end

local function QPSR_isSameFaction(usernameA, usernameB)
    local factionA = QPSR_getFactionForUsername(usernameA)
    local factionB = QPSR_getFactionForUsername(usernameB)

    if factionA == nil or factionB == nil then
        return false
    end

    if factionA == factionB then
        return true
    end

    local nameA = QPSR_getFactionName(factionA)
    local nameB = QPSR_getFactionName(factionB)

    return nameA ~= "" and nameA == nameB
end

local function QPSR_canCreateAtObject(player, object)
    if not QPSR_isMultiplayerClient() then
        return true, nil
    end

    if QPSR_isLocalAdmin(player) then
        return true, nil
    end

    return false, "UI_QPSR_ErrorCreateAdminOnly"
end

local function QPSR_canRemoveRequest(player, request)
    if not QPSR_isMultiplayerClient() then
        return true
    end

    return QPSR_isLocalAdmin(player)
end

local function QPSR_canReleaseSupplies(player, request)
    if not QPSR_isMultiplayerClient() then
        return true
    end

    return QPSR_isLocalAdmin(player)
end

local function QPSR_getWorldAgeHours()
    local ok, value = pcall(function()
        return getGameTime():getWorldAgeHours()
    end)

    if ok and value ~= nil then
        return tonumber(value) or 0
    end

    return 0
end

local function QPSR_writeRequest(object, request, shouldTransmit)
    local modData = QPSR_getObjectModData(object)
    if modData == nil then
        return false
    end

    local items = QPSR_Shared.normalizeRequestItems(request.items)
    if #items == 0 and tostring(request.itemFullType or "") ~= "" then
        items = QPSR_Shared.normalizeRequestItems({{
            fullType = request.itemFullType,
            displayName = request.itemDisplayName,
            targetAmount = request.targetAmount
        }})
    end
    if #items == 0 then
        return false
    end

    local progressByType = type(request.progressByType) == "table" and request.progressByType or
        QPSR_Shared.decodeProgress(request.progressRaw)
    local itemContributions = type(request.itemContributions) == "table" and request.itemContributions or
        QPSR_Shared.decodeItemContributions(request.itemContributionsRaw)
    local firstItem = items[1]
    local aggregate = QPSR_Shared.aggregateItemContributions(itemContributions)

    modData[QPSR_Shared.OBJECT_HAS_REQUEST] = true
    modData[QPSR_Shared.OBJECT_TITLE] = QPSR_limitText(request.title, QPSR_Shared.MAX_REQUEST_TITLE_LENGTH)
    modData[QPSR_Shared.OBJECT_ITEMS] = QPSR_Shared.encodeRequestItems(items)
    modData[QPSR_Shared.OBJECT_ITEM_PROGRESS] = QPSR_Shared.encodeProgress(progressByType)
    modData[QPSR_Shared.OBJECT_ITEM_CONTRIBUTIONS] = QPSR_Shared.encodeItemContributions(itemContributions)
    modData[QPSR_Shared.OBJECT_ITEM_NAME] = tostring(firstItem.displayName or "")
    modData[QPSR_Shared.OBJECT_ITEM_FULL_TYPE] = tostring(firstItem.fullType or "")
    modData[QPSR_Shared.OBJECT_ITEM_DISPLAY_NAME] = tostring(firstItem.displayName or "")
    modData[QPSR_Shared.OBJECT_TARGET_AMOUNT] = tonumber(firstItem.targetAmount) or 1
    modData[QPSR_Shared.OBJECT_PRIORITY] = tostring(request.priority or "Normal")
    modData[QPSR_Shared.OBJECT_NOTE] = tostring(request.note or "")
    modData[QPSR_Shared.OBJECT_CREATED_BY] = tostring(request.createdBy or "")
    modData[QPSR_Shared.OBJECT_CREATED_AT] = tonumber(request.createdAt) or 0
    modData[QPSR_Shared.OBJECT_CONTAINER_INDEX] = tonumber(request.containerIndex) or 0
    modData[QPSR_Shared.OBJECT_CONTAINER_TYPE] = tostring(request.containerType or "")
    modData[QPSR_Shared.OBJECT_FULFILLED_BY] = tostring(request.fulfilledBy or "")
    modData[QPSR_Shared.OBJECT_FULFILLED_AT] = tonumber(request.fulfilledAt) or 0
    modData[QPSR_Shared.OBJECT_PROTECTION_ENABLED] = request.protectionEnabled == true
    modData[QPSR_Shared.OBJECT_PROTECTION_LOCKED] = request.protectionLocked == true
    modData[QPSR_Shared.OBJECT_CONTRIBUTIONS] = QPSR_Shared.encodeContributions(aggregate)
    modData[QPSR_Shared.OBJECT_LAST_PROGRESS] = tonumber(progressByType[firstItem.fullType]) or 0
    modData[QPSR_Shared.OBJECT_COMPLETED] = request.completed == true
    modData[QPSR_Shared.OBJECT_SUPPLIES_RELEASED] = request.suppliesReleased == true
    modData[QPSR_Shared.OBJECT_ACTIVATION_MODE] = tostring(request.activationMode or QPSR_Shared.ACTIVATION_IMMEDIATE)
    modData[QPSR_Shared.OBJECT_LINKED_CONTRACT_ID] = tostring(request.linkedContractId or "")
    modData[QPSR_Shared.OBJECT_ACTIVATION_STATE] = tostring(request.activationState or QPSR_Shared.STATE_ACTIVE)
    modData[QPSR_Shared.OBJECT_ACTIVATED_AT] = tonumber(request.activatedAt) or 0
    modData[QPSR_Shared.OBJECT_ACTIVATION_EVENT_KEY] = tostring(request.activationEventKey or "")

    if shouldTransmit ~= false then
        QPSR_transmitObjectModData(object)
    end
    return true
end

local function QPSR_clearRequest(object, shouldTransmit)
    local modData = QPSR_getObjectModData(object)
    if modData == nil then
        return false
    end

    for _, key in ipairs({
        QPSR_Shared.OBJECT_HAS_REQUEST,
        QPSR_Shared.OBJECT_TITLE,
        QPSR_Shared.OBJECT_ITEMS,
        QPSR_Shared.OBJECT_ITEM_PROGRESS,
        QPSR_Shared.OBJECT_ITEM_CONTRIBUTIONS,
        QPSR_Shared.OBJECT_ITEM_NAME,
        QPSR_Shared.OBJECT_ITEM_FULL_TYPE,
        QPSR_Shared.OBJECT_ITEM_DISPLAY_NAME,
        QPSR_Shared.OBJECT_TARGET_AMOUNT,
        QPSR_Shared.OBJECT_PRIORITY,
        QPSR_Shared.OBJECT_NOTE,
        QPSR_Shared.OBJECT_CREATED_BY,
        QPSR_Shared.OBJECT_CREATED_AT,
        QPSR_Shared.OBJECT_CONTAINER_INDEX,
        QPSR_Shared.OBJECT_CONTAINER_TYPE,
        QPSR_Shared.OBJECT_FULFILLED_BY,
        QPSR_Shared.OBJECT_FULFILLED_AT,
        QPSR_Shared.OBJECT_PROTECTION_ENABLED,
        QPSR_Shared.OBJECT_PROTECTION_LOCKED,
        QPSR_Shared.OBJECT_CONTRIBUTIONS,
        QPSR_Shared.OBJECT_LAST_PROGRESS,
        QPSR_Shared.OBJECT_COMPLETED,
        QPSR_Shared.OBJECT_SUPPLIES_RELEASED,
        QPSR_Shared.OBJECT_ACTIVATION_MODE, QPSR_Shared.OBJECT_LINKED_CONTRACT_ID,
        QPSR_Shared.OBJECT_ACTIVATION_STATE, QPSR_Shared.OBJECT_ACTIVATED_AT,
        QPSR_Shared.OBJECT_ACTIVATION_EVENT_KEY
    }) do
        modData[key] = nil
    end

    if shouldTransmit ~= false then
        QPSR_transmitObjectModData(object)
    end
    return true
end

local function QPSR_matchesRequestedItem(item, requestLine)
    if item == nil or requestLine == nil then
        return false
    end

    local requestedFullType = QPSR_trim(requestLine.fullType or requestLine.itemFullType)
    if requestedFullType ~= "" and item.getFullType ~= nil then
        local ok, fullType = pcall(function()
            return tostring(item:getFullType() or "")
        end)
        if ok then
            return fullType == requestedFullType
        end
    end

    local requestedName = QPSR_trim(requestLine.displayName or requestLine.itemDisplayName or requestLine.itemName)
    if requestedName == "" then
        return false
    end

    local requestedLower = string.lower(requestedName)
    local candidates = {}

    if item.getFullType ~= nil then
        local ok, value = pcall(function() return item:getFullType() end)
        if ok and value ~= nil then table.insert(candidates, tostring(value)) end
    end
    if item.getType ~= nil then
        local ok, value = pcall(function() return item:getType() end)
        if ok and value ~= nil then table.insert(candidates, tostring(value)) end
    end
    if item.getDisplayName ~= nil then
        local ok, value = pcall(function() return item:getDisplayName() end)
        if ok and value ~= nil then table.insert(candidates, tostring(value)) end
    end
    if item.getName ~= nil then
        local ok, value = pcall(function() return item:getName() end)
        if ok and value ~= nil then table.insert(candidates, tostring(value)) end
    end

    for _, candidate in ipairs(candidates) do
        if candidate == requestedName or string.lower(candidate) == requestedLower then
            return true
        end
    end
    return false
end

local function QPSR_findRequestedLineForItem(item, request)
    if request == nil or type(request.items) ~= "table" then
        return nil
    end
    for _, line in ipairs(request.items) do
        if QPSR_matchesRequestedItem(item, line) then
            return line
        end
    end
    return nil
end

local function QPSR_countRequestLine(object, request, line)
    local container = QPSR_getRequestContainer(object, request)
    if container == nil or line == nil then
        return 0
    end

    local ok, items = pcall(function() return container:getItems() end)
    if not ok or items == nil then
        return 0
    end

    local count = 0
    for index = 0, items:size() - 1 do
        if QPSR_matchesRequestedItem(items:get(index), line) then
            count = count + 1
        end
    end
    return count
end

local function QPSR_countAllRequestItems(object, request)
    local progressByType = {}
    if request == nil then
        return progressByType, 0, 0
    end

    if tostring(request.activationState or QPSR_Shared.STATE_ACTIVE) == QPSR_Shared.STATE_LOCKED then
        for _, line in ipairs(request.items or {}) do
            progressByType[line.fullType] = 0
        end
        local current, target = QPSR_Shared.getOverallProgress(request.items, progressByType)
        return progressByType, current, target
    end

    for _, line in ipairs(request.items or {}) do
        progressByType[line.fullType] = QPSR_countRequestLine(object, request, line)
    end

    local current, target = QPSR_Shared.getOverallProgress(request.items, progressByType)
    return progressByType, current, target
end

-- QPSR_PROTECTED_DONATIONS_AND_CONTRIBUTIONS_V1
local function QPSR_getWorldObjectFromContainer(container)
    if container == nil or container.getParent == nil then
        return nil
    end

    local ok, object = pcall(function()
        return container:getParent()
    end)

    if not ok or object == nil or QPSR_getObjectModData(object) == nil then
        return nil
    end

    return object
end

local function QPSR_containerMatchesRequest(object, container, request)
    if object == nil or container == nil or request == nil then
        return false
    end

    local requestContainer = QPSR_getRequestContainer(object, request)
    if requestContainer == container then
        return true
    end

    local actualIndex = QPSR_getContainerIndexValue(object, container)
    local expectedIndex = tonumber(request.containerIndex)

    if expectedIndex ~= nil and actualIndex == expectedIndex then
        local expectedType = tostring(request.containerType or "")
        local actualType = QPSR_getContainerTypeValue(container)
        return expectedType == "" or expectedType == actualType
    end

    return false
end

local function QPSR_getItemFullType(item)
    if item == nil or item.getFullType == nil then
        return ""
    end

    local ok, value = pcall(function()
        return item:getFullType()
    end)

    if not ok or value == nil then
        return ""
    end

    return tostring(value)
end

local function QPSR_getTransferRequestState(container, item)
    local object = QPSR_getWorldObjectFromContainer(container)
    if object == nil then
        return nil
    end

    local request = QPSR_readRequest(object)
    if request == nil or not QPSR_containerMatchesRequest(object, container, request) then
        return nil
    end

    local line = QPSR_findRequestedLineForItem(item, request)
    return {
        object = object,
        request = request,
        line = line,
        progress = line ~= nil and QPSR_countRequestLine(object, request, line) or 0,
        itemMatches = line ~= nil
    }
end

local function QPSR_showProtectionNotice(player)
    local now = 0

    if getTimestampMs ~= nil then
        local ok, value = pcall(function() return getTimestampMs() end)
        if ok then now = tonumber(value) or 0 end
    end

    if now <= 0 then
        local ok, value = pcall(function()
            return getGameTime():getWorldAgeHours() * 3600000
        end)
        if ok then now = tonumber(value) or 0 end
    end

    if now <= 0 or now - (tonumber(QPSR_Client.lastProtectionNoticeAt) or 0) >= 1500 then
        QPSR_Client.lastProtectionNoticeAt = now
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_ErrorProtectedWithdrawal"))
    end
end

local function QPSR_shouldBlockLockedDeposit(state)
    if state == nil or state.request == nil or state.itemMatches ~= true then
        return false
    end

    return tostring(state.request.activationState or QPSR_Shared.STATE_ACTIVE) ==
        QPSR_Shared.STATE_LOCKED
end

local function QPSR_showActivationLockedNotice(player)
    local now = 0

    if getTimestampMs ~= nil then
        local ok, value = pcall(function()
            return getTimestampMs()
        end)
        if ok then
            now = tonumber(value) or 0
        end
    end

    if now <= 0 or
       now - (tonumber(QPSR_Client.lastActivationLockedNoticeAt) or 0) >= 1500 then
        QPSR_Client.lastActivationLockedNoticeAt = now
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_StatusLocked"))
    end
end

local function QPSR_shouldBlockWithdrawal(player, state)
    if state == nil or state.request == nil then
        return false
    end

    if state.request.protectionLocked ~= true then
        return false
    end

    return not QPSR_isLocalAdmin(player)
end

local function QPSR_reconcileProgressLocal(
    player,
    object,
    itemFullType,
    direction,
    delta
)
    local request = QPSR_readRequest(object)
    if request == nil or request.completed == true then
        return
    end

    local line = QPSR_Shared.getRequestItem(
        request.items,
        itemFullType
    )
    if line == nil then
        return
    end

    local progress = QPSR_countRequestLine(
        object,
        request,
        line
    )
    local target = tonumber(line.targetAmount) or 1
    local changeAmount = math.max(
        0,
        math.floor(tonumber(delta) or 0)
    )
    local previousProgress = tonumber(
        request.progressByType[line.fullType]
    )

    if previousProgress == nil then
        if direction == "add" then
            previousProgress = math.max(
                0,
                progress - changeAmount
            )
        elseif direction == "remove" then
            previousProgress = progress + changeAmount
        else
            previousProgress = progress
        end
    end

    local actor = QPSR_getPlayerName(player)
    if actor == "" then
        actor = QPSR_I18N.getText(
            "UI_QPSR_SoloPlayer"
        )
    end

    local changed = false

    if direction == "add" and changeAmount > 0 then
        local contributions =
            request.itemContributions[line.fullType] or {}
        local creditedTotal =
            QPSR_Shared.getContributionTotal(contributions)
        local remainingCredit = math.max(
            0,
            target - creditedTotal
        )
        local actualIncrease = math.max(
            0,
            progress - previousProgress
        )
        local creditedAmount = math.min(
            changeAmount,
            actualIncrease,
            remainingCredit
        )

        if creditedAmount > 0 then
            request.itemContributions[line.fullType] =
                QPSR_Shared.addContribution(
                    contributions,
                    actor,
                    creditedAmount
                )
            changed = true
        end
    elseif direction == "remove" and
           changeAmount > 0 then
        local actualDecrease = math.max(
            0,
            previousProgress - progress
        )
        local debitAmount = math.min(
            changeAmount,
            actualDecrease
        )

        if debitAmount > 0 then
            local contributions =
                request.itemContributions[line.fullType] or {}
            local removedAmount = 0

            request.itemContributions[line.fullType],
            removedAmount =
                QPSR_Shared.removeContribution(
                    contributions,
                    actor,
                    debitAmount
                )

            if removedAmount > 0 then
                changed = true
            end
        end
    end

    local liveProgress = QPSR_countAllRequestItems(
        object,
        request
    )
    local oldProgressRaw = QPSR_Shared.encodeProgress(
        request.progressByType
    )
    local newProgressRaw = QPSR_Shared.encodeProgress(
        liveProgress
    )

    request.progressByType = liveProgress

    if oldProgressRaw ~= newProgressRaw then
        changed = true
    end

    if QPSR_Shared.areAllItemsComplete(
        request.items,
        liveProgress
    ) then
        request.completed = true

        for _, requestLine in ipairs(request.items) do
            request.progressByType[requestLine.fullType] =
                requestLine.targetAmount
        end

        changed = true

        if direction == "add" and
           tostring(request.fulfilledBy or "") == "" then
            request.fulfilledBy = actor
            request.fulfilledAt =
                QPSR_getWorldAgeHours()
        end

        if request.protectionEnabled == true and
           request.suppliesReleased ~= true then
            request.protectionLocked = true
        else
            request.protectionLocked = false
        end
    end

    if changed then
        QPSR_writeRequest(object, request, true)
    end
end

local function QPSR_notifyProgressChanged(player, state, direction, delta)
    if state == nil or state.object == nil or state.request == nil or state.line == nil then
        return
    end

    if state.request.completed == true or not state.itemMatches then
        return
    end

    local changeAmount = math.max(0, math.floor(tonumber(delta) or 0))
    if changeAmount < 1 then
        return
    end

    if QPSR_isMultiplayerClient() then
        local container = QPSR_getRequestContainer(state.object, state.request)
        local containerIndex = QPSR_getContainerIndexValue(state.object, container)
        local locator = QPSR_buildObjectLocator(state.object, containerIndex)
        if locator == nil then
            return
        end

        locator.itemFullType = tostring(state.line.fullType or "")
        locator.direction = tostring(direction or "")
        locator.delta = changeAmount
        locator.beforeProgress = tonumber(state.progress)

        pcall(function()
            sendClientCommand(
                QPSR_Shared.MODULE,
                QPSR_Shared.COMMAND_PROGRESS_CHANGED,
                locator
            )
        end)
        return
    end

    QPSR_reconcileProgressLocal(
        player,
        state.object,
        tostring(state.line.fullType or ""),
        direction,
        changeAmount
    )
end


-- QPSR_NET_ITEM_ID_CONTRIBUTIONS_V5
-- Multiplayer inventory transactions may finish before transferItem() runs.
-- Register the exact InventoryItem ID while the timed action is being created,
-- then let the server credit it only after that same ID appears in the request
-- container.
local function QPSR_getInventoryItemId(item)
    if item == nil or item.getID == nil then
        return nil
    end

    local ok, value = pcall(function()
        return item:getID()
    end)

    if not ok or tonumber(value) == nil then
        return nil
    end

    local itemId = math.floor(tonumber(value))
    if itemId < 0 then
        return nil
    end

    return itemId
end

local function QPSR_sendTransferIntent(player, state, item, direction)
    if not QPSR_isMultiplayerClient() or
       state == nil or
       state.object == nil or
       state.request == nil or
       state.line == nil or
       state.itemMatches ~= true or
       state.request.completed == true then
        return false
    end

    local itemId = QPSR_getInventoryItemId(item)
    if itemId == nil then
        return false
    end

    local container = QPSR_getRequestContainer(state.object, state.request)
    local containerIndex = QPSR_getContainerIndexValue(state.object, container)
    local locator = QPSR_buildObjectLocator(state.object, containerIndex)
    if locator == nil then
        return false
    end

    locator.itemId = itemId
    locator.itemFullType = tostring(state.line.fullType or "")
    locator.direction = tostring(direction or "add")
    locator.beforeProgress = tonumber(state.progress) or 0

    local ok = pcall(function()
        sendClientCommand(
            QPSR_Shared.MODULE,
            QPSR_Shared.COMMAND_TRANSFER_INTENT,
            locator
        )
    end)

    return ok
end

local function QPSR_installTransferHook()
    if QPSR_Client.TransferHookInstalled then
        return
    end

    if ISInventoryTransferAction == nil or
       ISInventoryTransferAction.new == nil or
       ISInventoryTransferAction.isValid == nil or
       ISInventoryTransferAction.transferItem == nil then
        print("[QPSR] Transfer hook unavailable.")
        return
    end

    local originalTransferNew = ISInventoryTransferAction.new
    local originalTransferIsValid =
        ISInventoryTransferAction.isValid
    local originalTransferItem =
        ISInventoryTransferAction.transferItem

    QPSR_Client.OriginalTransferNew = originalTransferNew
    QPSR_Client.OriginalTransferIsValid =
        originalTransferIsValid
    QPSR_Client.OriginalTransferItem = originalTransferItem

    -- QPSR_PRE_TRANSACTION_PROTECTION_GUARD_V5
    -- In multiplayer, the vanilla item transaction starts before
    -- transferItem() runs. Reject protected withdrawals in isValid(),
    -- before createItemTransaction() can move anything.
    function ISInventoryTransferAction:isValid()
        local sourceState = QPSR_getTransferRequestState(
            self.srcContainer,
            self.item
        )

        local destinationState = QPSR_getTransferRequestState(
            self.destContainer,
            self.item
        )

        if QPSR_shouldBlockWithdrawal(
            self.character,
            sourceState
        ) then
            QPSR_showProtectionNotice(self.character)
            return false
        end

        if QPSR_shouldBlockLockedDeposit(destinationState) then
            QPSR_showActivationLockedNotice(self.character)
            return false
        end

        return originalTransferIsValid(self)
    end

    -- The constructor runs before the action is added to the timed-action
    -- queue and before createItemTransaction() starts. This is the reliable
    -- point to register every selected item, including items later merged
    -- into one vanilla transfer action.
    function ISInventoryTransferAction:new(character, item, srcContainer, destContainer, time)
        local sourceState =
            QPSR_getTransferRequestState(srcContainer, item)
        local destinationState =
            QPSR_getTransferRequestState(destContainer, item)

        local action = originalTransferNew(
            self,
            character,
            item,
            srcContainer,
            destContainer,
            time
        )

        if action ~= nil and
           sourceState ~= nil and
           not QPSR_shouldBlockWithdrawal(
               character,
               sourceState
           ) then
            QPSR_sendTransferIntent(
                character,
                sourceState,
                item,
                "remove"
            )
        end

        if action ~= nil and
           destinationState ~= nil and
           not QPSR_shouldBlockLockedDeposit(destinationState) then
            QPSR_sendTransferIntent(
                character,
                destinationState,
                item,
                "add"
            )
        end

        return action
    end

    function ISInventoryTransferAction:transferItem(item)
        local sourceContainer = self.srcContainer
        local destinationContainer = self.destContainer
        local character = self.character
        local movedItem = item
        local sourceState =
            QPSR_getTransferRequestState(sourceContainer, movedItem)
        local destinationState =
            QPSR_getTransferRequestState(destinationContainer, movedItem)

        if QPSR_shouldBlockWithdrawal(character, sourceState) then
            QPSR_showProtectionNotice(character)
            return false
        end

        if QPSR_shouldBlockLockedDeposit(destinationState) then
            QPSR_showActivationLockedNotice(character)
            return false
        end

        -- Fallback registration for unusual actions created before this hook
        -- was installed. The server deduplicates by request + direction +
        -- InventoryItem ID.
        if sourceState ~= nil and
           QPSR_isMultiplayerClient() and
           not QPSR_shouldBlockWithdrawal(
               character,
               sourceState
           ) then
            QPSR_sendTransferIntent(
                character,
                sourceState,
                movedItem,
                "remove"
            )
        end

        if destinationState ~= nil and
           QPSR_isMultiplayerClient() and
           not QPSR_shouldBlockLockedDeposit(destinationState) then
            QPSR_sendTransferIntent(
                character,
                destinationState,
                movedItem,
                "add"
            )
        end

        local result = originalTransferItem(self, item)

        if sourceState ~= nil then
            local removed = 1

            if not QPSR_isMultiplayerClient() then
                local afterSource = QPSR_countRequestLine(
                    sourceState.object,
                    sourceState.request,
                    sourceState.line
                )
                removed = math.max(
                    0,
                    sourceState.progress - afterSource
                )
            end

            QPSR_notifyProgressChanged(
                character,
                sourceState,
                "remove",
                removed
            )
        end

        -- Multiplayer additions are attributed by the pre-transaction
        -- item-ID intent. Solo play continues to reconcile immediately.
        if destinationState ~= nil and not QPSR_isMultiplayerClient() then
            destinationState.line =
                QPSR_findRequestedLineForItem(
                    movedItem,
                    destinationState.request
                )
            destinationState.itemMatches =
                destinationState.line ~= nil

            local afterDestination = QPSR_countRequestLine(
                destinationState.object,
                destinationState.request,
                destinationState.line
            )
            local added = math.max(
                0,
                afterDestination - destinationState.progress
            )

            QPSR_notifyProgressChanged(
                character,
                destinationState,
                "add",
                added
            )
        end

        return result
    end

    QPSR_Client.TransferHookInstalled = true
    print(
        "[QPSR] Net item-ID tracking and pre-transaction protection guard installed."
    )
end

local function QPSR_priorityText(priority)
    local value = tostring(priority or "Normal")

    if value == "Low" then
        return QPSR_I18N.getText("UI_QPSR_PriorityLow")
    end

    if value == "High" then
        return QPSR_I18N.getText("UI_QPSR_PriorityHigh")
    end

    if value == "Critical" then
        return QPSR_I18N.getText("UI_QPSR_PriorityCritical")
    end

    return QPSR_I18N.getText("UI_QPSR_PriorityNormal")
end

local function QPSR_statusCode(progress, targetAmount, completed, protectionEnabled, suppliesReleased, activationState)
    if tostring(activationState or QPSR_Shared.STATE_ACTIVE) == QPSR_Shared.STATE_LOCKED then return "Locked" end
    local target = tonumber(targetAmount) or 1
    local current = tonumber(progress) or 0

    if completed == true or current >= target then
        if protectionEnabled == true and suppliesReleased ~= true then
            return "AwaitingCollection"
        end

        if protectionEnabled == true and suppliesReleased == true then
            return "Released"
        end

        return "Completed"
    end

    if current > 0 then
        return "Partial"
    end

    return "Open"
end

local function QPSR_statusText(statusCode)
    if statusCode == "Locked" then return QPSR_I18N.getText("UI_QPSR_StatusLocked") end
    if statusCode == "AwaitingCollection" then
        return QPSR_I18N.getText("UI_QPSR_StatusAwaitingCollection")
    end

    if statusCode == "Released" then
        return QPSR_I18N.getText("UI_QPSR_StatusReleased")
    end

    if statusCode == "Completed" then
        return QPSR_I18N.getText("UI_QPSR_StatusCompleted")
    end

    if statusCode == "Partial" then
        return QPSR_I18N.getText("UI_QPSR_StatusPartial")
    end

    return QPSR_I18N.getText("UI_QPSR_StatusOpen")
end

-- QPSR_ITEM_PICKER_PERFORMANCE_V4
local function QPSR_itemMatchRank(catalogItem, searchLower)
    if searchLower == "" then
        return 100
    end

    local displayLower = string.lower(tostring(catalogItem.displayName or ""))
    local typeLower = string.lower(tostring(catalogItem.typeName or ""))
    local fullTypeLower = string.lower(tostring(catalogItem.fullType or ""))

    if displayLower == searchLower then
        return 0
    end

    if typeLower == searchLower then
        return 1
    end

    if fullTypeLower == searchLower then
        return 2
    end

    if string.sub(displayLower, 1, string.len(searchLower)) == searchLower then
        return 3
    end

    if string.sub(typeLower, 1, string.len(searchLower)) == searchLower then
        return 4
    end

    if string.sub(fullTypeLower, 1, string.len(searchLower)) == searchLower then
        return 5
    end

    if string.find(displayLower, searchLower, 1, true) ~= nil then
        return 6
    end

    if string.find(typeLower, searchLower, 1, true) ~= nil then
        return 7
    end

    if string.find(fullTypeLower, searchLower, 1, true) ~= nil then
        return 8
    end

    return nil
end

local function QPSR_getItemModule(fullType)
    local value = tostring(fullType or "")
    local moduleName = string.match(value, "^([^%.]+)%.")

    if moduleName == nil or moduleName == "" then
        return value
    end

    return moduleName
end

local function QPSR_drawCatalogItem(list, y, row, alt)
    local rowHeight = tonumber(list.itemheight) or 26

    if alt then
        list:drawRect(0, y, list.width, rowHeight - 1, 0.04, 1, 1, 1)
    end

    if list.selected == row.index then
        list:drawRect(0, y, list.width, rowHeight - 1, 0.35, 0.45, 0.27, 0.12)
        list:drawRectBorder(0, y, list.width, rowHeight - 1, 0.85, 0.75, 0.65, 0.35)
    end

    list:drawText(
        tostring(row.text or ""),
        8,
        y + 3,
        0.9,
        0.9,
        0.9,
        1,
        UIFont.Small
    )

    return y + rowHeight
end

-- =========================================================
-- CREATE REQUEST WINDOW
-- =========================================================

QPSR_CreateRequestWindow = ISPanel:derive("QPSR_CreateRequestWindow")

local function QPSR_drawRequestedItem(list, y, row, alt)
    local rowHeight = tonumber(list.itemheight) or 24
    if alt then list:drawRect(0, y, list.width, rowHeight - 1, 0.04, 1, 1, 1) end
    if list.selected == row.index then
        list:drawRect(0, y, list.width, rowHeight - 1, 0.35, 0.45, 0.27, 0.12)
        list:drawRectBorder(0, y, list.width, rowHeight - 1, 0.85, 0.75, 0.65, 0.35)
    end
    list:drawText(tostring(row.text or ""), 8, y + 3, 0.9, 0.9, 0.9, 1, UIFont.Small)
    return y + rowHeight
end

function QPSR_CreateRequestWindow:new(x, y, width, height, player, containerObject, containerIndex)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self

    o.player = player
    o.containerObject = containerObject
    o.containerIndex = math.floor(tonumber(containerIndex) or 0)
    local descriptor = QPSR_getContainerDescriptor(containerObject, o.containerIndex)
    o.containerType = descriptor ~= nil and descriptor.type or ""
    o.containerLabel = descriptor ~= nil and descriptor.label or QPSR_I18N.getContainerLabel("", o.containerIndex)
    o.moveWithMouse = true
    o.backgroundColor = {r=0.05, g=0.05, b=0.05, a=0.96}
    o.borderColor = {r=0.75, g=0.65, b=0.35, a=0.95}
    o.priorityValues = {"Low", "Normal", "High", "Critical"}
    o.lastSearchText = nil
    o.observedSearchText = ""
    o.searchDelayFrames = 0
    o.matchCount = 0
    o.catalogLoading = true
    o.canEnableProtection = QPSR_isLocalAdmin(player)
    o.protectionSelected = false
    o.requestedItems = {}
    return o
end

function QPSR_CreateRequestWindow:initialise()
    ISPanel.initialise(self)

    local detailsLabelY = self.height - 178
    local detailsFieldY = detailsLabelY + 20
    local protectionY = self.height - 108
    self.layout = {
        margin = 20,
        containerY = 48,
        searchLabelY = 70,
        searchY = 90,
        searchHintY = 120,
        matchingY = 140,
        listY = 160,
        listHeight = 145,
        selectedY = 312,
        selectedTypeY = 332,
        quantityLabelY = 354,
        quantityY = 374,
        requestedLabelY = 410,
        requestedListY = 432,
        detailsLabelY = detailsLabelY,
        detailsFieldY = detailsFieldY,
        protectionY = protectionY
    }
    self.layout.requestedListHeight = math.max(82, detailsLabelY - self.layout.requestedListY - 38)

    self.searchEntry = ISTextEntryBox:new("", 20, self.layout.searchY, self.width - 40, 26)
    self.searchEntry:initialise(); self.searchEntry:instantiate(); self:addChild(self.searchEntry)

    self.itemList = ISScrollingListBox:new(20, self.layout.listY, self.width - 40, self.layout.listHeight)
    self.itemList:initialise(); self.itemList:instantiate()
    self.itemList.itemheight = 26; self.itemList.font = UIFont.Small
    self.itemList.doDrawItem = QPSR_drawCatalogItem; self.itemList.selected = 0
    self:addChild(self.itemList)

    self.quantityEntry = ISTextEntryBox:new("1", 20, self.layout.quantityY, 130, 26)
    self.quantityEntry:initialise(); self.quantityEntry:instantiate(); self.quantityEntry:setOnlyNumbers(true)
    self:addChild(self.quantityEntry)

    self.addItemButton = ISButton:new(170, self.layout.quantityY, 150, 26, QPSR_I18N.getText("UI_QPSR_AddItem"), self, QPSR_CreateRequestWindow.onAddItem)
    self.addItemButton:initialise(); self.addItemButton:instantiate(); self:addChild(self.addItemButton)

    self.requestedItemsList = ISScrollingListBox:new(20, self.layout.requestedListY, self.width - 40, self.layout.requestedListHeight)
    self.requestedItemsList:initialise(); self.requestedItemsList:instantiate()
    self.requestedItemsList.itemheight = 24; self.requestedItemsList.font = UIFont.Small
    self.requestedItemsList.doDrawItem = QPSR_drawRequestedItem; self.requestedItemsList.selected = 0
    self:addChild(self.requestedItemsList)

    self.removeItemButton = ISButton:new(20, self.layout.requestedListY + self.layout.requestedListHeight + 5, 180, 25, QPSR_I18N.getText("UI_QPSR_RemoveSelectedItem"), self, QPSR_CreateRequestWindow.onRemoveItem)
    self.removeItemButton:initialise(); self.removeItemButton:instantiate(); self:addChild(self.removeItemButton)

    local available = self.width - 40
    local titleWidth = math.floor(available * 0.34)
    local priorityWidth = 170
    local gap = 16
    local titleX = 20
    local priorityX = titleX + titleWidth + gap
    local noteX = priorityX + priorityWidth + gap
    local noteWidth = self.width - noteX - 20

    self.titleEntry = ISTextEntryBox:new("", titleX, self.layout.detailsFieldY, titleWidth, 26)
    self.titleEntry:initialise(); self.titleEntry:instantiate(); self:addChild(self.titleEntry)

    self.priorityCombo = ISComboBox:new(priorityX, self.layout.detailsFieldY, priorityWidth, 26, self, QPSR_CreateRequestWindow.onPriorityChanged)
    self.priorityCombo:initialise(); self.priorityCombo:instantiate()
    self.priorityCombo:addOption(QPSR_I18N.getText("UI_QPSR_PriorityLow"))
    self.priorityCombo:addOption(QPSR_I18N.getText("UI_QPSR_PriorityNormal"))
    self.priorityCombo:addOption(QPSR_I18N.getText("UI_QPSR_PriorityHigh"))
    self.priorityCombo:addOption(QPSR_I18N.getText("UI_QPSR_PriorityCritical"))
    self.priorityCombo.selected = 2; self:addChild(self.priorityCombo)

    self.noteEntry = ISTextEntryBox:new("", noteX, self.layout.detailsFieldY, noteWidth, 26)
    self.noteEntry:initialise(); self.noteEntry:instantiate(); self:addChild(self.noteEntry)

    self.detailPositions = {titleX=titleX, priorityX=priorityX, noteX=noteX}

    self.protectionButton = ISButton:new(20, self.layout.protectionY, self.width - 40, 28, QPSR_I18N.getText("UI_QPSR_ProtectionToggleOff"), self, QPSR_CreateRequestWindow.onToggleProtection)
    self.protectionButton:initialise(); self.protectionButton:instantiate()
    self.protectionButton.enable = self.canEnableProtection == true; self:addChild(self.protectionButton)
    self.activationCombo = ISComboBox:new(20, self.layout.protectionY + 34, 260, 26, self, QPSR_CreateRequestWindow.onActivationChanged)
    self.activationCombo:initialise(); self.activationCombo:instantiate()
    self.activationCombo:addOption(QPSR_I18N.getText("UI_QPSR_ActivationImmediate"))
    self.activationCombo:addOption(QPSR_I18N.getText("UI_QPSR_ActivationContract"))
    self:addChild(self.activationCombo)
    self.contractIdEntry = ISTextEntryBox:new("", 290, self.layout.protectionY + 34, self.width - 310, 26)
    self.contractIdEntry:initialise(); self.contractIdEntry:instantiate(); self.contractIdEntry:setVisible(false); self:addChild(self.contractIdEntry)

    self.createButton = ISButton:new(20, self.height - 46, 160, 28, QPSR_I18N.getText("UI_QPSR_Create"), self, QPSR_CreateRequestWindow.onCreate)
    self.createButton:initialise(); self.createButton:instantiate(); self:addChild(self.createButton)
    self.cancelButton = ISButton:new(self.width - 180, self.height - 46, 160, 28, QPSR_I18N.getText("UI_QPSR_Cancel"), self, QPSR_CreateRequestWindow.onCancel)
    self.cancelButton:initialise(); self.cancelButton:instantiate(); self:addChild(self.cancelButton)

    QPSR_startItemCatalogBuild(); self:refreshItemList(true); self:refreshRequestedItemsList()
end

function QPSR_CreateRequestWindow:onPriorityChanged() end

function QPSR_CreateRequestWindow:onActivationChanged()
    local linked = self.activationCombo.selected == 2
    self.contractIdEntry:setVisible(linked)
end

function QPSR_CreateRequestWindow:onToggleProtection()
    if self.canEnableProtection ~= true then
        QPSR_say(self.player, QPSR_I18N.getText("UI_QPSR_ProtectionAdminOnly")); return
    end
    self.protectionSelected = not self.protectionSelected
    local key = self.protectionSelected and "UI_QPSR_ProtectionToggleOn" or "UI_QPSR_ProtectionToggleOff"
    if self.protectionButton.setTitle ~= nil then self.protectionButton:setTitle(QPSR_I18N.getText(key)) else self.protectionButton.title = QPSR_I18N.getText(key) end
end

function QPSR_CreateRequestWindow:getSelectedCatalogItem()
    if self.itemList == nil or self.itemList.items == nil then return nil end
    local selected = tonumber(self.itemList.selected) or 0
    if selected < 1 or selected > #self.itemList.items then return nil end
    local row = self.itemList.items[selected]
    return row ~= nil and row.item or nil
end

function QPSR_CreateRequestWindow:clearItemResults()
    if self.itemList == nil then return end
    if self.itemList.clear ~= nil then self.itemList:clear() else self.itemList.items = {} end
    self.itemList.selected = 0; self.matchCount = 0
end

function QPSR_CreateRequestWindow:refreshItemList(force)
    if self.itemList == nil or self.searchEntry == nil then return end
    local searchText = QPSR_trim(self.searchEntry:getText())
    if not force and searchText ~= self.observedSearchText then
        self.observedSearchText = searchText; self.searchDelayFrames = QPSR_Shared.SEARCH_DELAY_FRAMES
        self.lastSearchText = nil; self:clearItemResults(); return
    end
    if not force and self.searchDelayFrames > 0 then self.searchDelayFrames = self.searchDelayFrames - 1; return end
    if not force and searchText == self.lastSearchText then return end
    self.lastSearchText = searchText; self:clearItemResults()
    if string.len(searchText) < QPSR_Shared.MIN_SEARCH_LENGTH then return end
    if not QPSR_isItemCatalogReady() then self.lastSearchText = nil; return end

    local searchLower = string.lower(searchText); local buckets = {}
    for rank = 0, 8 do buckets[rank] = {} end
    for _, catalogItem in ipairs(QPSR_Client.itemCatalog) do
        local rank = QPSR_itemMatchRank(catalogItem, searchLower)
        if rank ~= nil and #buckets[rank] < QPSR_Shared.MAX_SEARCH_RESULTS then table.insert(buckets[rank], catalogItem) end
    end
    local shown = 0
    for rank = 0, 8 do
        local bucket = buckets[rank]
        table.sort(bucket, function(left, right)
            local leftName = string.lower(tostring(left.displayName or "")); local rightName = string.lower(tostring(right.displayName or ""))
            if leftName == rightName then return tostring(left.fullType or "") < tostring(right.fullType or "") end
            return leftName < rightName
        end)
        for _, catalogItem in ipairs(bucket) do
            if shown >= QPSR_Shared.MAX_SEARCH_RESULTS then break end
            self.itemList:addItem(QPSR_I18N.getText("UI_QPSR_ItemListEntry", catalogItem.displayName, QPSR_getItemModule(catalogItem.fullType)), catalogItem)
            shown = shown + 1
        end
        if shown >= QPSR_Shared.MAX_SEARCH_RESULTS then break end
    end
    self.matchCount = shown
end

function QPSR_CreateRequestWindow:refreshRequestedItemsList()
    if self.requestedItemsList == nil then return end
    if self.requestedItemsList.clear ~= nil then self.requestedItemsList:clear() else self.requestedItemsList.items = {} end
    for _, item in ipairs(self.requestedItems) do
        self.requestedItemsList:addItem(QPSR_I18N.getText("UI_QPSR_RequestedItemRow", item.displayName, item.targetAmount, QPSR_getItemModule(item.fullType)), item)
    end
    if #self.requestedItems == 0 then self.requestedItemsList.selected = 0 elseif self.requestedItemsList.selected < 1 then self.requestedItemsList.selected = 1 end
end

function QPSR_CreateRequestWindow:onAddItem()
    local selectedItem = self:getSelectedCatalogItem()
    if selectedItem == nil then QPSR_say(self.player, QPSR_I18N.getText("UI_QPSR_ErrorSelectItem")); return end
    local quantityText = QPSR_trim(self.quantityEntry:getText())
    if string.match(quantityText, "^%d+$") == nil then QPSR_say(self.player, QPSR_I18N.getText("UI_QPSR_ErrorQuantity")); return end
    local quantity = tonumber(quantityText) or 0
    if quantity < 1 or quantity > QPSR_Shared.MAX_TARGET_AMOUNT then
        local key = quantity > QPSR_Shared.MAX_TARGET_AMOUNT and "UI_QPSR_ErrorQuantityTooLarge" or "UI_QPSR_ErrorQuantity"
        QPSR_say(self.player, QPSR_I18N.getText(key, QPSR_Shared.MAX_TARGET_AMOUNT)); return
    end
    if #self.requestedItems >= QPSR_Shared.MAX_REQUEST_ITEMS then
        QPSR_say(self.player, QPSR_I18N.getText("UI_QPSR_ErrorTooManyItems", QPSR_Shared.MAX_REQUEST_ITEMS)); return
    end
    local fullType = tostring(selectedItem.fullType or "")
    for _, item in ipairs(self.requestedItems) do
        if item.fullType == fullType then QPSR_say(self.player, QPSR_I18N.getText("UI_QPSR_ErrorDuplicateItem")); return end
    end
    table.insert(self.requestedItems, {
        fullType = fullType,
        displayName = QPSR_limitText(selectedItem.displayName, QPSR_Shared.MAX_ITEM_NAME_LENGTH),
        targetAmount = quantity
    })
    self:refreshRequestedItemsList()
end

function QPSR_CreateRequestWindow:onRemoveItem()
    local selected = tonumber(self.requestedItemsList.selected) or 0
    if selected < 1 or selected > #self.requestedItems then return end
    table.remove(self.requestedItems, selected)
    self.requestedItemsList.selected = math.min(selected, #self.requestedItems)
    self:refreshRequestedItemsList()
end

function QPSR_CreateRequestWindow:onCreate()
    local selectedContainer = QPSR_getContainerByIndex(self.containerObject, self.containerIndex)
    if selectedContainer == nil then QPSR_say(self.player, QPSR_I18N.getText("UI_QPSR_ErrorContainerUnavailable")); self:onCancel(); return end
    local canCreate, permissionError = QPSR_canCreateAtObject(self.player, self.containerObject)
    if not canCreate then QPSR_say(self.player, QPSR_I18N.getText(permissionError or "UI_QPSR_ErrorCreateAdminOnly")); self:onCancel(); return end
    if QPSR_hasRequest(self.containerObject) then QPSR_say(self.player, QPSR_I18N.getText("UI_QPSR_ErrorExistingRequest")); self:onCancel(); return end
    local items = QPSR_Shared.normalizeRequestItems(self.requestedItems)
    if #items == 0 then QPSR_say(self.player, QPSR_I18N.getText("UI_QPSR_ErrorNoRequestedItems")); return end

    local selected = tonumber(self.priorityCombo.selected) or 2
    local requestArgs = {
        title = QPSR_limitText(self.titleEntry:getText(), QPSR_Shared.MAX_REQUEST_TITLE_LENGTH),
        items = items,
        priority = self.priorityValues[selected] or "Normal",
        note = QPSR_limitText(self.noteEntry:getText(), QPSR_Shared.MAX_NOTE_LENGTH),
        createdBy = QPSR_getPlayerName(self.player),
        createdAt = QPSR_getWorldAgeHours(),
        containerIndex = self.containerIndex,
        containerType = QPSR_getContainerTypeValue(selectedContainer),
        protectionEnabled = self.canEnableProtection == true and self.protectionSelected == true,
        protectionLocked = false,
        progressByType = {},
        itemContributions = {},
        fulfilledBy = "",
        fulfilledAt = 0,
        completed = false,
        suppliesReleased = false,
        activationMode = self.activationCombo.selected == 2 and QPSR_Shared.ACTIVATION_CONTRACT or QPSR_Shared.ACTIVATION_IMMEDIATE,
        linkedContractId = QPSR_limitText(self.contractIdEntry:getText(), 80),
        activationState = self.activationCombo.selected == 2 and QPSR_Shared.STATE_LOCKED or QPSR_Shared.STATE_ACTIVE,
        activatedAt = 0,
        activationEventKey = ""
    }
    if requestArgs.activationMode == QPSR_Shared.ACTIVATION_CONTRACT and requestArgs.linkedContractId == "" then QPSR_say(self.player, QPSR_I18N.getText("UI_QPSR_ErrorContractIdRequired")); return end

    requestArgs.progressByType = QPSR_countAllRequestItems(self.containerObject, requestArgs)
    requestArgs.completed = QPSR_Shared.areAllItemsComplete(items, requestArgs.progressByType)
    if requestArgs.completed then for _, line in ipairs(items) do requestArgs.progressByType[line.fullType] = line.targetAmount end end
    requestArgs.suppliesReleased = requestArgs.protectionEnabled ~= true
    requestArgs.protectionLocked = requestArgs.protectionEnabled == true and requestArgs.suppliesReleased ~= true

    if QPSR_isMultiplayerClient() then
        local locator = QPSR_buildObjectLocator(self.containerObject, self.containerIndex)
        if locator == nil then QPSR_say(self.player, QPSR_I18N.getText("UI_QPSR_ErrorContainerUnavailable")); return end
        locator.title = requestArgs.title
        locator.itemsRaw = QPSR_Shared.encodeRequestItems(items)
        locator.activationMode = self.activationCombo.selected == 2 and QPSR_Shared.ACTIVATION_CONTRACT or QPSR_Shared.ACTIVATION_IMMEDIATE
        locator.linkedContractId = QPSR_limitText(self.contractIdEntry:getText(), 80)
        if locator.activationMode == QPSR_Shared.ACTIVATION_CONTRACT and locator.linkedContractId == "" then QPSR_say(self.player, QPSR_I18N.getText("UI_QPSR_ErrorContractIdRequired")); return end
        locator.priority = requestArgs.priority
        locator.note = requestArgs.note
        locator.protectionEnabled = requestArgs.protectionEnabled == true
        if QPSR_sendActionCommand(self.player, "create", QPSR_Shared.COMMAND_CREATE, locator) then
            QPSR_say(self.player, QPSR_I18N.getText("UI_QPSR_MessageCreating")); self:onCancel()
        end
        return
    end

    if not QPSR_writeRequest(self.containerObject, requestArgs, true) then
        QPSR_say(self.player, QPSR_I18N.getText("UI_QPSR_ErrorContainerUnavailable")); return
    end
    QPSR_say(self.player, QPSR_I18N.getText("UI_QPSR_MessageCreated")); self:onCancel()
end

function QPSR_CreateRequestWindow:onCancel()
    self:removeFromUIManager(); QPSR_Client.createWindow = nil
end

function QPSR_CreateRequestWindow:prerender()
    ISPanel.prerender(self)
    local wasLoading = self.catalogLoading
    self.catalogLoading = not QPSR_stepItemCatalogBuild(QPSR_Shared.CATALOG_BATCH_SIZE)
    if wasLoading and not self.catalogLoading then self.lastSearchText = nil; self.searchDelayFrames = 0 end
    self:refreshItemList(false)

    self:drawRect(0, 0, self.width, self.height, 0.96, 0.05, 0.05, 0.05)
    self:drawRectBorder(0, 0, self.width, self.height, 0.95, 0.75, 0.65, 0.35)
    self:drawText(QPSR_I18N.getText("UI_QPSR_WindowCreateTitle"), 20, 14, 1, 1, 1, 1, UIFont.Medium)
    self:drawText(QPSR_I18N.getText("UI_QPSR_AttachedContainerValue", self.containerLabel), 20, self.layout.containerY, 0.8, 0.85, 1, 1, UIFont.Small)
    self:drawText(QPSR_I18N.getText("UI_QPSR_SearchLabel"), 20, self.layout.searchLabelY, 0.9, 0.9, 0.9, 1, UIFont.Small)
    self:drawText(QPSR_I18N.getText("UI_QPSR_SearchHint"), 20, self.layout.searchHintY, 0.65, 0.65, 0.65, 1, UIFont.Small)
    local matchingText = self.catalogLoading and QPSR_I18N.getText("UI_QPSR_CatalogLoading") or QPSR_I18N.getText("UI_QPSR_MatchingItems", self.matchCount)
    self:drawText(matchingText, 20, self.layout.matchingY, 0.85, 0.85, 0.65, 1, UIFont.Small)

    local selectedItem = self:getSelectedCatalogItem()
    local selectedLabel = QPSR_I18N.getText("UI_QPSR_NoItemSelected")
    local selectedType = ""
    if selectedItem ~= nil then selectedLabel = QPSR_I18N.getText("UI_QPSR_SelectedItem", selectedItem.displayName); selectedType = tostring(selectedItem.fullType or "") end
    self:drawText(selectedLabel, 20, self.layout.selectedY, 0.8, 0.85, 1, 1, UIFont.Small)
    if selectedType ~= "" then self:drawText(selectedType, 20, self.layout.selectedTypeY, 0.62, 0.68, 0.78, 1, UIFont.Small) end
    self:drawText(QPSR_I18N.getText("UI_QPSR_QuantityLabel"), 20, self.layout.quantityLabelY, 0.9, 0.9, 0.9, 1, UIFont.Small)
    self:drawText(QPSR_I18N.getText("UI_QPSR_RequestedItemsTitle", #self.requestedItems, QPSR_Shared.MAX_REQUEST_ITEMS), 20, self.layout.requestedLabelY, 0.8, 0.85, 1, 1, UIFont.Small)
    self:drawText(QPSR_I18N.getText("UI_QPSR_RequestTitleLabel"), self.detailPositions.titleX, self.layout.detailsLabelY, 0.9, 0.9, 0.9, 1, UIFont.Small)
    self:drawText(QPSR_I18N.getText("UI_QPSR_PriorityLabel"), self.detailPositions.priorityX, self.layout.detailsLabelY, 0.9, 0.9, 0.9, 1, UIFont.Small)
    self:drawText(QPSR_I18N.getText("UI_QPSR_NoteLabel"), self.detailPositions.noteX, self.layout.detailsLabelY, 0.9, 0.9, 0.9, 1, UIFont.Small)
end

-- =========================================================
-- VIEW REQUEST WINDOW
-- =========================================================

QPSR_RequestViewWindow = ISPanel:derive("QPSR_RequestViewWindow")

function QPSR_RequestViewWindow:new(x, y, width, height, player, containerObject)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self); self.__index = self
    o.player = player; o.containerObject = containerObject; o.moveWithMouse = true
    o.backgroundColor = {r=0.05, g=0.05, b=0.05, a=0.96}
    o.borderColor = {r=0.75, g=0.65, b=0.35, a=0.95}
    return o
end

function QPSR_RequestViewWindow:initialise()
    ISPanel.initialise(self)
    self.closeButton = ISButton:new(self.width - 150, self.height - 46, 130, 28, QPSR_I18N.getText("UI_QPSR_Close"), self, QPSR_RequestViewWindow.onClose)
    self.closeButton:initialise(); self.closeButton:instantiate(); self:addChild(self.closeButton)

    self.itemsList = ISScrollingListBox:new(20, 104, self.width - 40, 170)
    self.itemsList:initialise(); self.itemsList:instantiate(); self.itemsList.itemheight = 24; self.itemsList.font = UIFont.Small
    self:addChild(self.itemsList)

    self.contributionsList = ISScrollingListBox:new(20, self.height - 204, self.width - 40, 142)
    self.contributionsList:initialise(); self.contributionsList:instantiate(); self.contributionsList.itemheight = 22; self.contributionsList.font = UIFont.Small
    self:addChild(self.contributionsList)
    self.lastItemsSignature = nil; self.lastContributionsSignature = nil
end

function QPSR_RequestViewWindow:onClose()
    self:removeFromUIManager(); QPSR_Client.viewWindow = nil
end

function QPSR_RequestViewWindow:refreshItems(request, progressByType)
    local signature = tostring(request.itemsRaw or "") .. "|" .. QPSR_Shared.encodeProgress(progressByType) .. "|" .. tostring(request.completed == true)
    if signature == self.lastItemsSignature then return end
    self.lastItemsSignature = signature
    if self.itemsList.clear ~= nil then self.itemsList:clear() else self.itemsList.items = {} end
    for _, line in ipairs(request.items or {}) do
        local progress = tonumber(progressByType[line.fullType]) or 0
        if request.completed == true then progress = line.targetAmount end
        local name = QPSR_getLocalizedItemName(line.fullType, line.displayName)
        local key = progress >= line.targetAmount and "UI_QPSR_RequestItemCompletedEntry" or "UI_QPSR_RequestItemEntry"
        self.itemsList:addItem(QPSR_I18N.getText(key, name, math.min(progress, line.targetAmount), line.targetAmount), line)
    end
end

function QPSR_RequestViewWindow:refreshContributions(request)
    local signature = tostring(request.itemContributionsRaw or "") .. "|" .. tostring(request.itemsRaw or "")
    if signature == self.lastContributionsSignature then return end
    self.lastContributionsSignature = signature
    if self.contributionsList.clear ~= nil then self.contributionsList:clear() else self.contributionsList.items = {} end
    local added = 0
    for _, line in ipairs(request.items or {}) do
        local displayName = QPSR_getLocalizedItemName(line.fullType, line.displayName)
        local rows = QPSR_Shared.getSortedContributions(request.itemContributions[line.fullType] or {})
        for _, row in ipairs(rows) do
            self.contributionsList:addItem(QPSR_I18N.getText("UI_QPSR_ContributionItemEntry", row.name, row.amount, displayName), row)
            added = added + 1
        end
    end
    if added == 0 then self.contributionsList:addItem(QPSR_I18N.getText("UI_QPSR_NoContributions"), {name="", amount=0}) end
end

function QPSR_RequestViewWindow:prerender()
    ISPanel.prerender(self)
    self:drawRect(0, 0, self.width, self.height, 0.96, 0.05, 0.05, 0.05)
    self:drawRectBorder(0, 0, self.width, self.height, 0.95, 0.75, 0.65, 0.35)
    self:drawText(QPSR_I18N.getText("UI_QPSR_WindowViewTitle"), 20, 14, 1, 1, 1, 1, UIFont.Medium)

    local request = QPSR_readRequest(self.containerObject)
    if request == nil then self:drawText(QPSR_I18N.getText("UI_QPSR_ErrorNoRequest"), 20, 62, 1, 0.65, 0.65, 1, UIFont.Small); return end

    local liveProgress = request.progressByType
    if request.completed ~= true then liveProgress = QPSR_countAllRequestItems(self.containerObject, request) end
    local overallProgress, overallTarget = QPSR_Shared.getOverallProgress(request.items, liveProgress)
    if request.completed == true then overallProgress = overallTarget end
    local status = QPSR_statusCode(overallProgress, overallTarget, request.completed, request.protectionEnabled, request.suppliesReleased, request.activationState)

    local title = QPSR_trim(request.title)
    if title == "" then title = QPSR_I18N.getText("UI_QPSR_DefaultRequestTitle") end
    self:drawText(QPSR_I18N.getText("UI_QPSR_RequestTitleValue", title), 20, 52, 0.9, 0.9, 1, 1, UIFont.Small)
    self:drawText(QPSR_I18N.getText("UI_QPSR_RequestItemsTitle"), 20, 82, 0.8, 0.85, 1, 1, UIFont.Small)
    self:refreshItems(request, liveProgress)

    local statusR, statusG, statusB = 0.9, 0.9, 0.9
    if status == "Completed" or status == "Released" then statusR, statusG, statusB = 0.65, 1, 0.65
    elseif status == "AwaitingCollection" or status == "Partial" then statusR, statusG, statusB = 1, 0.82, 0.45 end

    local requestContainer = QPSR_getRequestContainer(self.containerObject, request)
    local requestContainerIndex = tonumber(request.containerIndex) or QPSR_getContainerIndexValue(self.containerObject, requestContainer)
    local requestContainerType = tostring(request.containerType or "")
    if requestContainerType == "" then requestContainerType = QPSR_getContainerTypeValue(requestContainer) end
    local containerLabel = QPSR_I18N.getContainerLabel(requestContainerType, requestContainerIndex)

    local y = 280
    self:drawText(QPSR_I18N.getText("UI_QPSR_StatusValue", QPSR_statusText(status)), 20, y, statusR, statusG, statusB, 1, UIFont.Small); y = y + 22
    if tostring(request.activationState or QPSR_Shared.STATE_ACTIVE) == QPSR_Shared.STATE_LOCKED then
        self:drawText(QPSR_I18N.getText("UI_QPSR_LinkedContractValue", request.linkedContractId), 20, y, 1, 0.65, 0.35, 1, UIFont.Small); y = y + 22
    end
    self:drawText(QPSR_I18N.getText("UI_QPSR_OverallProgressValue", overallProgress, overallTarget), 20, y, 0.75, 1, 0.75, 1, UIFont.Small); y = y + 22
    self:drawText(QPSR_I18N.getText("UI_QPSR_AttachedContainerValue", containerLabel), 20, y, 0.8, 0.85, 1, 1, UIFont.Small); y = y + 22

    local protectionText = QPSR_I18N.getText("UI_QPSR_ProtectionDisabled")
    if request.protectionEnabled == true then
        if request.completed == true and request.suppliesReleased ~= true then protectionText = QPSR_I18N.getText("UI_QPSR_ProtectionAwaitingRelease")
        elseif request.suppliesReleased == true then protectionText = QPSR_I18N.getText("UI_QPSR_ProtectionReleased")
        elseif request.protectionLocked == true then protectionText = QPSR_I18N.getText("UI_QPSR_ProtectionLocked")
        else protectionText = QPSR_I18N.getText("UI_QPSR_ProtectionUnlocked") end
    end
    self:drawText(QPSR_I18N.getText("UI_QPSR_ProtectionValue", protectionText), 20, y, 0.9, 0.8, 0.45, 1, UIFont.Small); y = y + 22
    self:drawText(QPSR_I18N.getText("UI_QPSR_PriorityValue", QPSR_priorityText(request.priority)), 20, y, 1, 0.82, 0.45, 1, UIFont.Small); y = y + 22

    local createdBy = request.createdBy ~= "" and request.createdBy or QPSR_I18N.getText("UI_QPSR_SoloPlayer")
    self:drawText(QPSR_I18N.getText("UI_QPSR_CreatedByValue", createdBy), 20, y, 0.8, 0.85, 1, 1, UIFont.Small); y = y + 22
    if request.completed == true then
        local fulfilledBy = tostring(request.fulfilledBy or "")
        if fulfilledBy == "" then fulfilledBy = QPSR_I18N.getText("UI_QPSR_FulfillerUnknown") end
        self:drawText(QPSR_I18N.getText("UI_QPSR_FulfilledByValue", fulfilledBy), 20, y, 0.65, 1, 0.65, 1, UIFont.Small); y = y + 22
    end
    local note = request.note ~= "" and request.note or QPSR_I18N.getText("UI_QPSR_NoNote")
    self:drawText(QPSR_I18N.getText("UI_QPSR_NoteValue", note), 20, y, 0.9, 0.9, 0.9, 1, UIFont.Small)

    self:drawText(QPSR_I18N.getText("UI_QPSR_ContributionsTitle"), 20, self.height - 228, 0.8, 0.85, 1, 1, UIFont.Small)
    self:refreshContributions(request)
end

-- =========================================================
-- CLIENT ACTIONS
-- =========================================================

function QPSR_Client.openCreateRequestWindow(player, containerObject, containerIndex)
    if QPSR_getContainerByIndex(containerObject, containerIndex) == nil then
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_ErrorContainerUnavailable"))
        return
    end

    local canCreate, permissionError = QPSR_canCreateAtObject(player, containerObject)
    if not canCreate then
        QPSR_say(player, QPSR_I18N.getText(permissionError or "UI_QPSR_ErrorCreateAdminOnly"))
        return
    end

    if QPSR_hasRequest(containerObject) then
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_ErrorExistingRequest"))
        return
    end

    if QPSR_Client.createWindow ~= nil then
        QPSR_Client.createWindow:removeFromUIManager()
        QPSR_Client.createWindow = nil
    end

    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()
    local width = math.min(860, math.max(760, screenWidth - 80))
    local height = math.min(790, math.max(740, screenHeight - 60))
    local x = math.max(0, (screenWidth / 2) - (width / 2))
    local y = math.max(0, (screenHeight / 2) - (height / 2))

    local window = QPSR_CreateRequestWindow:new(
        x,
        y,
        width,
        height,
        player,
        containerObject,
        containerIndex
    )

    window:initialise()
    window:addToUIManager()
    QPSR_Client.createWindow = window
end

function QPSR_Client.openRequestView(player, containerObject)
    local request = QPSR_readRequest(containerObject)

    if request == nil then
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_ErrorNoRequest"))
        return
    end

    if QPSR_Client.viewWindow ~= nil then
        QPSR_Client.viewWindow:removeFromUIManager()
        QPSR_Client.viewWindow = nil
    end

    local width = 700
    local height = 680
    local x = (getCore():getScreenWidth() / 2) - (width / 2)
    local y = (getCore():getScreenHeight() / 2) - (height / 2)

    local window = QPSR_RequestViewWindow:new(
        x,
        y,
        width,
        height,
        player,
        containerObject
    )

    window:initialise()
    window:addToUIManager()
    QPSR_Client.viewWindow = window
end

function QPSR_Client.removeRequest(player, containerObject)
    local request = QPSR_readRequest(containerObject)

    if request == nil then
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_ErrorNoRequest"))
        return
    end

    if not QPSR_canRemoveRequest(player, request) then
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_ErrorNotAllowed"))
        return
    end

    if QPSR_isMultiplayerClient() then
        local locator = QPSR_buildObjectLocator(containerObject)
        if locator == nil then
            QPSR_say(player, QPSR_I18N.getText("UI_QPSR_ErrorContainerUnavailable"))
            return
        end

        if QPSR_sendActionCommand(
            player,
            "remove",
            QPSR_Shared.COMMAND_REMOVE,
            locator
        ) then
            QPSR_say(player, QPSR_I18N.getText("UI_QPSR_MessageRemoving"))
        end
        return
    end

    if QPSR_clearRequest(containerObject, true) then
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_MessageRemoved"))
    else
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_ErrorContainerUnavailable"))
    end
end


function QPSR_Client.releaseSupplies(player, containerObject)
    local request = QPSR_readRequest(containerObject)
    if request == nil then
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_ErrorNoRequest"))
        return
    end

    if not QPSR_canReleaseSupplies(player, request) then
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_ErrorReleaseNotAllowed"))
        return
    end

    if request.completed ~= true then
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_ErrorRequestNotCompleted"))
        return
    end

    if request.suppliesReleased == true and request.protectionLocked ~= true then
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_MessageSuppliesReleased"))
        return
    end

    if QPSR_isMultiplayerClient() then
        local locator = QPSR_buildObjectLocator(containerObject, request.containerIndex)
        if locator == nil then
            QPSR_say(player, QPSR_I18N.getText("UI_QPSR_ErrorContainerUnavailable"))
            return
        end

        if QPSR_sendActionCommand(
            player,
            "release",
            QPSR_Shared.COMMAND_RELEASE_SUPPLIES,
            locator
        ) then
            QPSR_say(player, QPSR_I18N.getText("UI_QPSR_MessageReleasingSupplies"))
        end
        return
    end

    request.suppliesReleased = true
    request.protectionLocked = false
    if QPSR_writeRequest(containerObject, request, true) then
        QPSR_say(player, QPSR_I18N.getText("UI_QPSR_MessageSuppliesReleased"))
    end
end

local function QPSR_addExistingRequestActions(submenu, player, containerObject, request)
    local requestContainer = QPSR_getRequestContainer(containerObject, request)
    local requestContainerIndex = tonumber(request.containerIndex) or QPSR_getContainerIndexValue(containerObject, requestContainer)
    local requestContainerType = tostring(request.containerType or "")
    if requestContainerType == "" then requestContainerType = QPSR_getContainerTypeValue(requestContainer) end
    local containerLabel = QPSR_I18N.getContainerLabel(requestContainerType, requestContainerIndex)

    local attachedOption = submenu:addOption(QPSR_I18N.getText("UI_QPSR_AttachedContainerValue", containerLabel), player, QPSR_dummy)
    attachedOption.notAvailable = true
    local createOption = submenu:addOption(QPSR_I18N.getText("UI_QPSR_CreateRequestUnavailable"), player, QPSR_dummy)
    createOption.notAvailable = true

    local progressByType = request.progressByType
    if request.completed ~= true then progressByType = QPSR_countAllRequestItems(containerObject, request) end
    local progress, target = QPSR_Shared.getOverallProgress(request.items, progressByType)
    if request.completed == true then progress = target end

    local viewTitle
    if request.completed == true or (target > 0 and progress >= target) then
        if request.protectionEnabled == true and request.suppliesReleased ~= true then viewTitle = QPSR_I18N.getText("UI_QPSR_ViewRequestAwaitingCollection")
        elseif request.protectionEnabled == true and request.suppliesReleased == true then viewTitle = QPSR_I18N.getText("UI_QPSR_ViewRequestReleased")
        else viewTitle = QPSR_I18N.getText("UI_QPSR_ViewRequestCompleted") end
    else
        viewTitle = QPSR_I18N.getText("UI_QPSR_ViewRequestProgress", progress, target)
    end
    submenu:addOption(viewTitle, player, QPSR_Client.openRequestView, containerObject)

    if request.protectionEnabled == true then
        local stateKey = "UI_QPSR_ProtectionLocked"
        if request.completed == true and request.suppliesReleased ~= true then stateKey = "UI_QPSR_ProtectionAwaitingRelease"
        elseif request.suppliesReleased == true then stateKey = "UI_QPSR_ProtectionReleased"
        elseif request.protectionLocked ~= true then stateKey = "UI_QPSR_ProtectionUnlocked" end
        local stateOption = submenu:addOption(QPSR_I18N.getText("UI_QPSR_ProtectionValue", QPSR_I18N.getText(stateKey)), player, QPSR_dummy)
        stateOption.notAvailable = true
        if request.completed == true and request.suppliesReleased ~= true then
            if QPSR_canReleaseSupplies(player, request) then submenu:addOption(QPSR_I18N.getText("UI_QPSR_ReleaseSupplies"), player, QPSR_Client.releaseSupplies, containerObject)
            else local option = submenu:addOption(QPSR_I18N.getText("UI_QPSR_ReleaseSuppliesUnavailable"), player, QPSR_dummy); option.notAvailable = true end
        end
    end

    if QPSR_canRemoveRequest(player, request) then submenu:addOption(QPSR_I18N.getText("UI_QPSR_RemoveRequest"), player, QPSR_Client.removeRequest, containerObject)
    else local option = submenu:addOption(QPSR_I18N.getText("UI_QPSR_RemoveRequestUnavailable"), player, QPSR_dummy); option.notAvailable = true end
end

local function QPSR_addCreateActions(submenu, player, containerObject, descriptor)
    local canCreate = QPSR_canCreateAtObject(player, containerObject)

    if canCreate then
        submenu:addOption(
            QPSR_I18N.getText("UI_QPSR_CreateRequest"),
            player,
            QPSR_Client.openCreateRequestWindow,
            containerObject,
            descriptor.index
        )
    else
        local createOption = submenu:addOption(
            QPSR_I18N.getText("UI_QPSR_CreateRequestAdminUnavailable"),
            player,
            QPSR_dummy
        )
        createOption.notAvailable = true
    end

    local viewOption = submenu:addOption(
        QPSR_I18N.getText("UI_QPSR_ViewRequest")
            .. " - "
            .. QPSR_I18N.getText("UI_QPSR_NoRequest"),
        player,
        QPSR_dummy
    )

    viewOption.notAvailable = true
end

function QPSR_Client.onWorldContextMenu(playerNum, context, worldObjects, test)
    if test then
        return
    end

    if context == nil then
        return
    end

    local player = getSpecificPlayer(playerNum)
    if player == nil then
        return
    end

    local containerObject = QPSR_findContainerObject(worldObjects)
    if containerObject == nil then
        return
    end

    local descriptors = QPSR_getContainerDescriptors(containerObject)
    if #descriptors == 0 then
        return
    end

    local rootOption = context:addOption(
        QPSR_I18N.getText("UI_QPSR_ModName"),
        player,
        QPSR_dummy
    )

    local submenu = ISContextMenu:getNew(context)
    context:addSubMenu(rootOption, submenu)

    local request = QPSR_readRequest(containerObject)
    if request ~= nil then
        QPSR_addExistingRequestActions(submenu, player, containerObject, request)
        return
    end

    if #descriptors == 1 then
        QPSR_addCreateActions(submenu, player, containerObject, descriptors[1])
        return
    end

    for _, descriptor in ipairs(descriptors) do
        local compartmentOption = submenu:addOption(
            descriptor.label,
            player,
            QPSR_dummy
        )
        local compartmentMenu = ISContextMenu:getNew(submenu)
        submenu:addSubMenu(compartmentOption, compartmentMenu)
        QPSR_addCreateActions(compartmentMenu, player, containerObject, descriptor)
    end
end

-- QPSR_SERVER_COMMAND_SYNC_V1
function QPSR_Client.onServerCommand(module, command, args)
    if module ~= QPSR_Shared.MODULE then
        return
    end

    if type(args) ~= "table" then
        args = {}
    end

    if command == QPSR_Shared.COMMAND_REQUEST_UPDATED then
        local object = QPSR_resolveObjectLocator(args)
        if object == nil then
            return
        end

        if args.hasRequest == true then
            QPSR_writeRequest(object, {
                title = tostring(args.title or ""),
                items = QPSR_Shared.decodeRequestItems(args.itemsRaw),
                progressByType = QPSR_Shared.decodeProgress(args.progressRaw),
                itemContributions = QPSR_Shared.decodeItemContributions(args.itemContributionsRaw),
                itemFullType = tostring(args.itemFullType or ""),
                itemDisplayName = tostring(args.itemDisplayName or ""),
                targetAmount = tonumber(args.targetAmount) or 1,
                priority = tostring(args.priority or "Normal"),
                note = tostring(args.note or ""),
                createdBy = tostring(args.createdBy or ""),
                createdAt = tonumber(args.createdAt) or 0,
                containerIndex = tonumber(args.requestContainerIndex),
                containerType = tostring(args.requestContainerType or ""),
                fulfilledBy = tostring(args.fulfilledBy or ""),
                fulfilledAt = tonumber(args.fulfilledAt) or 0,
                protectionEnabled = args.protectionEnabled == true,
                protectionLocked = args.protectionLocked == true,
                contributionsRaw = tostring(args.contributionsRaw or ""),
                progressRaw = tostring(args.progressRaw or ""),
                itemContributionsRaw = tostring(args.itemContributionsRaw or ""),
                lastProgress = tonumber(args.lastProgress),
                completed = args.completed == true,
                suppliesReleased = args.suppliesReleased == true,
                activationMode = tostring(args.activationMode or QPSR_Shared.ACTIVATION_IMMEDIATE),
                linkedContractId = tostring(args.linkedContractId or ""),
                activationState = tostring(args.activationState or QPSR_Shared.STATE_ACTIVE),
                activatedAt = tonumber(args.activatedAt) or 0,
                activationEventKey = tostring(args.activationEventKey or "")
            }, false)
        else
            QPSR_clearRequest(object, false)
        end

        return
    end

    if command == QPSR_Shared.COMMAND_ACTION_RESULT then
        QPSR_Client.pendingAction = nil

        local key = tostring(args.messageKey or "UI_QPSR_ErrorServerRejected")
        QPSR_say(
            getPlayer(),
            QPSR_I18N.getText(key, args.value1, args.value2)
        )
    end
end

function QPSR_Client.onGameStart()
    QPSR_Client.pendingAction = nil
    QPSR_installTransferHook()
end

Events.OnFillWorldObjectContextMenu.Add(QPSR_Client.onWorldContextMenu)
Events.OnServerCommand.Add(QPSR_Client.onServerCommand)
Events.OnGameStart.Add(QPSR_Client.onGameStart)

print("[QPSR] Client loaded v" .. tostring(QPSR_Shared.VERSION) .. " multi-item protected supply flow.")
