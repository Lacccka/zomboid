-- QP Supply Requests
-- QPSR_CONTRACT_ACTIVATION_LOCK_HOTFIX_V071
-- Server-authoritative multiplayer request creation/removal
-- v0.6.6: optional QP Community Reputation completion integration

require "QPSupplyRequests/QPSR_Shared"

QPSR_Server = QPSR_Server or {}
QPSR_Server.pendingProgressChecks = QPSR_Server.pendingProgressChecks or {}
QPSR_Server.pendingTransferIntents = QPSR_Server.pendingTransferIntents or {}

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

local function QPSR_isExcludedWorldObject(object)
    if object == nil then
        return true
    end

    if instanceof ~= nil then
        if instanceof(object, "IsoDeadBody") or
           instanceof(object, "IsoWorldInventoryObject") or
           instanceof(object, "IsoPlayer") or
           instanceof(object, "IsoZombie") then
            return true
        end
    end

    return false
end

-- QPSR_CONTAINER_API_COMPAT_V2
-- Match the client resolver: normal Build 42 furniture may expose its primary
-- ItemContainer through getItemContainer() instead of getContainer().
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

local function QPSR_resolveRequestContainer(object, args)
    local requestedIndex = tonumber(args.requestContainerIndex)
    local requestedType = tostring(args.requestContainerType or "")

    if requestedIndex ~= nil and requestedIndex >= 0 then
        local indexedContainer = QPSR_getContainerByIndex(object, requestedIndex)
        if indexedContainer ~= nil then
            local actualType = QPSR_getContainerTypeValue(indexedContainer)
            if requestedType == "" or actualType == requestedType then
                return indexedContainer, requestedIndex, actualType
            end
        end
    end

    if requestedType ~= "" and object.getContainerByType ~= nil then
        local ok, typedContainer = pcall(function()
            return object:getContainerByType(requestedType)
        end)

        if ok and typedContainer ~= nil then
            return typedContainer, QPSR_getContainerIndexValue(object, typedContainer), requestedType
        end
    end

    return nil, -1, ""
end

local function QPSR_getRequestContainer(object, request)
    if object == nil or request == nil then
        return nil
    end

    local container, _, _ = QPSR_resolveRequestContainer(object, {
        requestContainerIndex = request.containerIndex,
        requestContainerType = request.containerType
    })

    if container ~= nil then
        return container
    end

    return QPSR_getContainer(object)
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

local function QPSR_getObjectIndex(object)
    if object == nil or object.getObjectIndex == nil then
        return -1
    end

    local ok, index = pcall(function()
        return object:getObjectIndex()
    end)

    if not ok or tonumber(index) == nil then
        return -1
    end

    return tonumber(index)
end


-- QPSR_COMMUNITY_REPUTATION_INTEGRATION_V066
local function QPSR_buildRequestId(object, request)
    local square = object
        and object.getSquare
        and object:getSquare()
        or nil

    local x = square and square:getX() or 0
    local y = square and square:getY() or 0
    local z = square and square:getZ() or 0
    local objectIndex = QPSR_getObjectIndex(object)
    local containerIndex = math.floor(
        tonumber(request and request.containerIndex) or 0
    )
    local createdStamp = math.floor(
        (tonumber(request and request.createdAt) or 0)
            * 1000
    )
    local title = string.lower(
        tostring(request and request.title or "")
    )

    title = string.gsub(title, "[^%w]+", "_")
    title = string.sub(title, 1, 32)

    return table.concat({
        tostring(x),
        tostring(y),
        tostring(z),
        tostring(objectIndex),
        tostring(containerIndex),
        tostring(createdStamp),
        title,
    }, ":")
end

local function QPSR_awardCompletedRequestCommunity(
    object,
    request
)
    if not QPReputation
        or not QPReputation.Server
        or type(
            QPReputation.Server.awardCommunityEvent
        ) ~= "function"
    then
        return
    end

    local requestId = tostring(
        request and request.requestId or ""
    )

    if requestId == "" then
        requestId = QPSR_buildRequestId(
            object,
            request
        )
        request.requestId = requestId
    end

    local contributions =
        QPSR_Shared.aggregateItemContributions(
            request.itemContributions
        )
    local rows =
        QPSR_Shared.getSortedContributions(
            contributions
        )
    local overallTarget = 0

    for _, line in ipairs(request.items or {}) do
        overallTarget = overallTarget
            + math.max(
                0,
                math.floor(
                    tonumber(line.targetAmount) or 0
                )
            )
    end

    for _, row in ipairs(rows) do
        local username = tostring(row.name or "")
        local amount = math.max(
            0,
            math.floor(tonumber(row.amount) or 0)
        )

        if username ~= "" and amount > 0 then
            local ok, awarded, result = pcall(
                QPReputation.Server
                    .awardCommunityEvent,
                username,
                "supply_request",
                requestId,
                "Automatic Community: Supply Request completed - "
                    .. tostring(
                        tostring(request.title or "") ~= ""
                            and request.title
                            or requestId
                    ),
                "QP Supply Requests",
                {
                    creatorUsername =
                        tostring(
                            request.createdBy or ""
                        ),
                    progress = amount,
                    target = overallTarget,
                }
            )

            if not ok then
                print(
                    "[QPSR] Community integration error for "
                        .. username
                        .. ": "
                        .. tostring(awarded)
                )
            elseif awarded == true then
                print(
                    "[QPSR] Community Reputation awarded to "
                        .. username
                        .. " for completed Supply Request "
                        .. requestId
                )
            elseif tostring(result or "") ~=
                "community_automation_disabled"
                and tostring(result or "") ~=
                    "community_source_disabled"
                and tostring(result or "") ~=
                    "self_created_activity"
                and tostring(result or "") ~=
                    "duplicate_award"
            then
                print(
                    "[QPSR] Community Reputation not awarded to "
                        .. username
                        .. " for request "
                        .. requestId
                        .. ": "
                        .. tostring(result)
                )
            end
        end
    end
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

local function QPSR_objectMatchesLocator(object, args)
    if QPSR_getContainerCount(object) <= 0 then
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

-- QPSR_SERVER_OBJECT_RESOLUTION_V3
local function QPSR_findMatchingObjectInList(objects, args, preferredIndex)
    if objects == nil then
        return nil
    end

    local okSize, size = pcall(function()
        return objects:size()
    end)

    if not okSize or tonumber(size) == nil then
        return nil
    end

    size = math.floor(tonumber(size))

    if preferredIndex ~= nil
        and preferredIndex >= 0
        and preferredIndex < size then
        local okCandidate, candidate = pcall(function()
            return objects:get(math.floor(preferredIndex))
        end)

        if okCandidate and QPSR_objectMatchesLocator(candidate, args) then
            return candidate
        end
    end

    for index = 0, size - 1 do
        local okCandidate, candidate = pcall(function()
            return objects:get(index)
        end)

        if okCandidate and QPSR_objectMatchesLocator(candidate, args) then
            return candidate
        end
    end

    return nil
end

local function QPSR_resolveObject(args)
    args = args or {}

    local x = tonumber(args.x)
    local y = tonumber(args.y)
    local z = tonumber(args.z)
    local objectIndex = tonumber(args.objectIndex)

    if x == nil or y == nil or z == nil or getCell == nil then
        return nil
    end

    x = math.floor(x)
    y = math.floor(y)
    z = math.floor(z)

    local ok, square = pcall(function()
        return getCell():getGridSquare(x, y, z)
    end)

    if not ok or square == nil then
        return nil
    end

    if square.getObjects ~= nil then
        local okObjects, objects = pcall(function()
            return square:getObjects()
        end)

        if okObjects then
            local candidate = QPSR_findMatchingObjectInList(
                objects,
                args,
                objectIndex
            )

            if candidate ~= nil then
                return candidate
            end
        end
    end

    -- Player-built and some movable storage objects can live only in the
    -- special-object collection. Scan it as a second authoritative source.
    if square.getSpecialObjects ~= nil then
        local okSpecial, specialObjects = pcall(function()
            return square:getSpecialObjects()
        end)

        if okSpecial then
            local candidate = QPSR_findMatchingObjectInList(
                specialObjects,
                args,
                nil
            )

            if candidate ~= nil then
                return candidate
            end
        end
    end

    return nil
end

local function QPSR_getPlayerName(player)
    if player == nil then
        return ""
    end

    if player.getUsername ~= nil then
        local ok, username = pcall(function()
            return player:getUsername()
        end)

        if ok and username ~= nil then
            local value = tostring(username)
            if value ~= "" and value ~= "None" and value ~= "none" then
                return value
            end
        end
    end

    return ""
end

local function QPSR_isAdmin(player)
    if player == nil or player.getAccessLevel == nil then
        return false
    end

    local ok, access = pcall(function()
        return player:getAccessLevel()
    end)

    return ok and string.lower(tostring(access or "")) == "admin"
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
    if QPSR_isAdmin(player) then
        return true, nil
    end

    return false, "UI_QPSR_ErrorCreateAdminOnly"
end

local function QPSR_isPlayerNearObject(player, object)
    if player == nil or object == nil or object.getSquare == nil then
        return false
    end

    local ok, result = pcall(function()
        local square = object:getSquare()
        if square == nil then
            return false
        end

        local dx = (tonumber(player:getX()) or 0) - (tonumber(square:getX()) or 0)
        local dy = (tonumber(player:getY()) or 0) - (tonumber(square:getY()) or 0)
        local dz = math.abs((tonumber(player:getZ()) or 0) - (tonumber(square:getZ()) or 0))
        local maximum = tonumber(QPSR_Shared.MAX_PROGRESS_CHECK_DISTANCE) or 5

        return dz <= 1 and ((dx * dx) + (dy * dy)) <= (maximum * maximum)
    end)

    return ok and result == true
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

local function QPSR_findScriptItem(fullType)
    local target = QPSR_limitText(fullType, QPSR_Shared.MAX_ITEM_FULL_TYPE_LENGTH)
    if target == "" or getScriptManager == nil then
        return nil
    end

    local ok, item = pcall(function()
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

    return item
end

local function QPSR_isScriptItemAllowed(scriptItem)
    if scriptItem == nil then
        return false
    end

    if scriptItem.getObsolete ~= nil then
        local ok, obsolete = pcall(function()
            return scriptItem:getObsolete()
        end)

        if ok and obsolete == true then
            return false
        end
    end

    if scriptItem.isHidden ~= nil then
        local ok, hidden = pcall(function()
            return scriptItem:isHidden()
        end)

        if ok and hidden == true then
            return false
        end
    end

    return true
end

local function QPSR_getScriptDisplayName(scriptItem, fallback)
    if scriptItem ~= nil and scriptItem.getDisplayName ~= nil then
        local ok, value = pcall(function()
            return scriptItem:getDisplayName()
        end)

        if ok and value ~= nil and tostring(value) ~= "" then
            return QPSR_limitText(value, QPSR_Shared.MAX_ITEM_NAME_LENGTH)
        end
    end

    return QPSR_limitText(fallback, QPSR_Shared.MAX_ITEM_NAME_LENGTH)
end

local function QPSR_readRequest(object)
    local modData = QPSR_getObjectModData(object)
    if modData == nil or modData[QPSR_Shared.OBJECT_HAS_REQUEST] ~= true then return nil end

    local legacyFullType = tostring(modData[QPSR_Shared.OBJECT_ITEM_FULL_TYPE] or "")
    local legacyDisplayName = tostring(modData[QPSR_Shared.OBJECT_ITEM_DISPLAY_NAME] or modData[QPSR_Shared.OBJECT_ITEM_NAME] or "")
    local legacyTarget = tonumber(modData[QPSR_Shared.OBJECT_TARGET_AMOUNT]) or 1
    local itemsRaw = tostring(modData[QPSR_Shared.OBJECT_ITEMS] or "")
    local items = QPSR_Shared.decodeRequestItems(itemsRaw)
    if #items == 0 and legacyFullType ~= "" then
        items = QPSR_Shared.normalizeRequestItems({{fullType=legacyFullType, displayName=legacyDisplayName, targetAmount=legacyTarget}})
        itemsRaw = QPSR_Shared.encodeRequestItems(items)
    end

    local progressRaw = tostring(modData[QPSR_Shared.OBJECT_ITEM_PROGRESS] or "")
    local progressByType = QPSR_Shared.decodeProgress(progressRaw)
    local legacyProgress = tonumber(modData[QPSR_Shared.OBJECT_LAST_PROGRESS])
    if #items == 1 and progressByType[items[1].fullType] == nil and legacyProgress ~= nil then
        progressByType[items[1].fullType] = legacyProgress
    end

    local itemContributionsRaw = tostring(modData[QPSR_Shared.OBJECT_ITEM_CONTRIBUTIONS] or "")
    local itemContributions = QPSR_Shared.decodeItemContributions(itemContributionsRaw)
    local legacyContributionsRaw = tostring(modData[QPSR_Shared.OBJECT_CONTRIBUTIONS] or "")
    if #items == 1 and itemContributions[items[1].fullType] == nil and legacyContributionsRaw ~= "" then
        itemContributions[items[1].fullType] = QPSR_Shared.decodeContributions(legacyContributionsRaw)
    end

    local itemDonorsRaw = tostring(
        modData[QPSR_Shared.OBJECT_ITEM_DONORS] or ""
    )
    local itemDonors = QPSR_Shared.decodeItemDonors(
        itemDonorsRaw
    )

    local completed = modData[QPSR_Shared.OBJECT_COMPLETED] == true or QPSR_Shared.areAllItemsComplete(items, progressByType)
    local protectionEnabled = modData[QPSR_Shared.OBJECT_PROTECTION_ENABLED] == true
    local protectionLocked = modData[QPSR_Shared.OBJECT_PROTECTION_LOCKED] == true
    local storedReleaseState = modData[QPSR_Shared.OBJECT_SUPPLIES_RELEASED]
    local suppliesReleased = storedReleaseState == true
    if storedReleaseState == nil then suppliesReleased = protectionEnabled and protectionLocked ~= true or not protectionEnabled end
    if completed and protectionEnabled then protectionLocked = suppliesReleased ~= true end

    local firstItem = items[1] or {fullType=legacyFullType, displayName=legacyDisplayName, targetAmount=legacyTarget}
    return {
        title = tostring(modData[QPSR_Shared.OBJECT_TITLE] or ""),
        items = items,
        itemsRaw = QPSR_Shared.encodeRequestItems(items),
        progressByType = progressByType,
        progressRaw = QPSR_Shared.encodeProgress(progressByType),
        itemContributions = itemContributions,
        itemContributionsRaw = QPSR_Shared.encodeItemContributions(itemContributions),
        itemDonors = itemDonors,
        itemDonorsRaw = QPSR_Shared.encodeItemDonors(itemDonors),
        itemFullType = tostring(firstItem.fullType or ""),
        itemDisplayName = tostring(firstItem.displayName or ""),
        targetAmount = tonumber(firstItem.targetAmount) or 1,
        priority = tostring(modData[QPSR_Shared.OBJECT_PRIORITY] or "Normal"),
        note = tostring(modData[QPSR_Shared.OBJECT_NOTE] or ""),
        createdBy = tostring(modData[QPSR_Shared.OBJECT_CREATED_BY] or ""),
        createdAt = tonumber(modData[QPSR_Shared.OBJECT_CREATED_AT]) or 0,
        requestId = tostring(modData[QPSR_Shared.OBJECT_REQUEST_ID] or ""),
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

local function QPSR_writeRequest(object, request)
    local modData = QPSR_getObjectModData(object)
    if modData == nil then return false end

    local items = QPSR_Shared.normalizeRequestItems(request.items)
    if #items == 0 then return false end
    local progressByType = type(request.progressByType) == "table" and request.progressByType or {}
    local itemContributions = type(request.itemContributions) == "table" and request.itemContributions or {}
    local itemDonors = type(request.itemDonors) == "table" and request.itemDonors or {}
    local firstItem = items[1]
    local aggregate = QPSR_Shared.aggregateItemContributions(itemContributions)

    if tostring(request.requestId or "") == "" then
        request.requestId = QPSR_buildRequestId(
            object,
            request
        )
    end

    modData[QPSR_Shared.OBJECT_HAS_REQUEST] = true
    modData[QPSR_Shared.OBJECT_TITLE] = QPSR_limitText(request.title, QPSR_Shared.MAX_REQUEST_TITLE_LENGTH)
    modData[QPSR_Shared.OBJECT_ITEMS] = QPSR_Shared.encodeRequestItems(items)
    modData[QPSR_Shared.OBJECT_ITEM_PROGRESS] = QPSR_Shared.encodeProgress(progressByType)
    modData[QPSR_Shared.OBJECT_ITEM_CONTRIBUTIONS] = QPSR_Shared.encodeItemContributions(itemContributions)
    modData[QPSR_Shared.OBJECT_ITEM_DONORS] = QPSR_Shared.encodeItemDonors(itemDonors)
    modData[QPSR_Shared.OBJECT_ITEM_NAME] = tostring(firstItem.displayName or "")
    modData[QPSR_Shared.OBJECT_ITEM_FULL_TYPE] = tostring(firstItem.fullType or "")
    modData[QPSR_Shared.OBJECT_ITEM_DISPLAY_NAME] = tostring(firstItem.displayName or "")
    modData[QPSR_Shared.OBJECT_TARGET_AMOUNT] = tonumber(firstItem.targetAmount) or 1
    modData[QPSR_Shared.OBJECT_PRIORITY] = tostring(request.priority or "Normal")
    modData[QPSR_Shared.OBJECT_NOTE] = tostring(request.note or "")
    modData[QPSR_Shared.OBJECT_CREATED_BY] = tostring(request.createdBy or "")
    modData[QPSR_Shared.OBJECT_CREATED_AT] = tonumber(request.createdAt) or 0
    modData[QPSR_Shared.OBJECT_REQUEST_ID] = tostring(request.requestId or "")
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
    if object.transmitModData ~= nil then pcall(function() object:transmitModData() end) end
    return true
end

local function QPSR_clearRequest(object)
    local modData = QPSR_getObjectModData(object)
    if modData == nil then return false end
    for _, key in ipairs({
        QPSR_Shared.OBJECT_HAS_REQUEST, QPSR_Shared.OBJECT_TITLE, QPSR_Shared.OBJECT_ITEMS,
        QPSR_Shared.OBJECT_ITEM_PROGRESS, QPSR_Shared.OBJECT_ITEM_CONTRIBUTIONS,
        QPSR_Shared.OBJECT_ITEM_DONORS,
        QPSR_Shared.OBJECT_ITEM_NAME, QPSR_Shared.OBJECT_ITEM_FULL_TYPE,
        QPSR_Shared.OBJECT_ITEM_DISPLAY_NAME, QPSR_Shared.OBJECT_TARGET_AMOUNT,
        QPSR_Shared.OBJECT_PRIORITY, QPSR_Shared.OBJECT_NOTE, QPSR_Shared.OBJECT_CREATED_BY,
        QPSR_Shared.OBJECT_CREATED_AT, QPSR_Shared.OBJECT_REQUEST_ID,
        QPSR_Shared.OBJECT_CONTAINER_INDEX,
        QPSR_Shared.OBJECT_CONTAINER_TYPE, QPSR_Shared.OBJECT_FULFILLED_BY,
        QPSR_Shared.OBJECT_FULFILLED_AT, QPSR_Shared.OBJECT_PROTECTION_ENABLED,
        QPSR_Shared.OBJECT_PROTECTION_LOCKED, QPSR_Shared.OBJECT_CONTRIBUTIONS,
        QPSR_Shared.OBJECT_LAST_PROGRESS, QPSR_Shared.OBJECT_COMPLETED,
        QPSR_Shared.OBJECT_SUPPLIES_RELEASED,
        QPSR_Shared.OBJECT_ACTIVATION_MODE, QPSR_Shared.OBJECT_LINKED_CONTRACT_ID,
        QPSR_Shared.OBJECT_ACTIVATION_STATE, QPSR_Shared.OBJECT_ACTIVATED_AT,
        QPSR_Shared.OBJECT_ACTIVATION_EVENT_KEY
    }) do modData[key] = nil end
    if object.transmitModData ~= nil then pcall(function() object:transmitModData() end) end
    return true
end

local function QPSR_matchesRequestedItem(item, line)
    if item == nil or line == nil then return false end
    local requestedFullType = QPSR_trim(line.fullType or line.itemFullType)
    if requestedFullType ~= "" and item.getFullType ~= nil then
        local ok, fullType = pcall(function() return tostring(item:getFullType() or "") end)
        if ok then return fullType == requestedFullType end
    end
    local requestedName = QPSR_trim(line.displayName or line.itemDisplayName)
    if requestedName ~= "" and item.getDisplayName ~= nil then
        local ok, displayName = pcall(function() return tostring(item:getDisplayName() or "") end)
        if ok then return string.lower(displayName) == string.lower(requestedName) end
    end
    return false
end

local function QPSR_countRequestLine(object, request, line)
    local container = QPSR_getRequestContainer(object, request)
    if container == nil or line == nil or container.getItems == nil then return 0 end
    local ok, items = pcall(function() return container:getItems() end)
    if not ok or items == nil then return 0 end
    local count = 0
    for index = 0, items:size() - 1 do if QPSR_matchesRequestedItem(items:get(index), line) then count = count + 1 end end
    return count
end

local function QPSR_countAllRequestItems(object, request)
    local progressByType = {}

    if request ~= nil and
       tostring(request.activationState or QPSR_Shared.STATE_ACTIVE) == QPSR_Shared.STATE_LOCKED then
        for _, line in ipairs(request.items or {}) do
            progressByType[line.fullType] = 0
        end
        return progressByType
    end

    for _, line in ipairs(request ~= nil and request.items or {}) do
        progressByType[line.fullType] = QPSR_countRequestLine(object, request, line)
    end
    return progressByType
end

local function QPSR_buildLocatorPayload(object)
    local square = object ~= nil and object:getSquare() or nil
    if square == nil then
        return nil
    end

    return {
        x = tonumber(square:getX()) or 0,
        y = tonumber(square:getY()) or 0,
        z = tonumber(square:getZ()) or 0,
        objectIndex = QPSR_getObjectIndex(object),
        containerType = QPSR_getContainerType(object),
        spriteName = QPSR_getSpriteName(object)
    }
end

local function QPSR_sendServerCommandSafe(player, command, payload)
    if player == nil then
        return false
    end

    local ok = pcall(function()
        sendServerCommand(
            player,
            QPSR_Shared.MODULE,
            command,
            payload or {}
        )
    end)

    return ok
end

local function QPSR_sendResult(player, action, success, messageKey, value1, value2)
    QPSR_sendServerCommandSafe(
        player,
        QPSR_Shared.COMMAND_ACTION_RESULT,
        {
            action = tostring(action or ""),
            success = success == true,
            messageKey = tostring(messageKey or "UI_QPSR_ErrorServerRejected"),
            value1 = value1,
            value2 = value2
        }
    )
end

local function QPSR_buildStatePayload(object, request)
    local payload = QPSR_buildLocatorPayload(object)
    if payload == nil then return nil end
    payload.hasRequest = request ~= nil
    if request ~= nil then
        local firstItem = request.items[1] or {}
        payload.title = tostring(request.title or "")
        payload.itemsRaw = QPSR_Shared.encodeRequestItems(request.items)
        payload.progressRaw = QPSR_Shared.encodeProgress(request.progressByType)
        payload.itemContributionsRaw = QPSR_Shared.encodeItemContributions(request.itemContributions)
        payload.itemFullType = tostring(firstItem.fullType or "")
        payload.itemDisplayName = tostring(firstItem.displayName or "")
        payload.targetAmount = tonumber(firstItem.targetAmount) or 1
        payload.priority = tostring(request.priority or "Normal")
        payload.note = tostring(request.note or "")
        payload.createdBy = tostring(request.createdBy or "")
        payload.createdAt = tonumber(request.createdAt) or 0
        payload.requestContainerIndex = tonumber(request.containerIndex) or 0
        payload.requestContainerType = tostring(request.containerType or "")
        payload.fulfilledBy = tostring(request.fulfilledBy or "")
        payload.fulfilledAt = tonumber(request.fulfilledAt) or 0
        payload.protectionEnabled = request.protectionEnabled == true
        payload.protectionLocked = request.protectionLocked == true
        payload.contributionsRaw = QPSR_Shared.encodeContributions(QPSR_Shared.aggregateItemContributions(request.itemContributions))
        payload.lastProgress = tonumber(request.progressByType[firstItem.fullType]) or 0
        payload.completed = request.completed == true
        payload.suppliesReleased = request.suppliesReleased == true
        payload.activationMode = tostring(request.activationMode or QPSR_Shared.ACTIVATION_IMMEDIATE)
        payload.linkedContractId = tostring(request.linkedContractId or "")
        payload.activationState = tostring(request.activationState or QPSR_Shared.STATE_ACTIVE)
        payload.activatedAt = tonumber(request.activatedAt) or 0
        payload.activationEventKey = tostring(request.activationEventKey or "")
    end
    return payload
end

local function QPSR_broadcastState(object, request)
    local payload = QPSR_buildStatePayload(object, request)
    if payload == nil or getOnlinePlayers == nil then
        return
    end

    local players = getOnlinePlayers()
    if players == nil then
        return
    end

    for index = 0, players:size() - 1 do
        local onlinePlayer = players:get(index)
        if onlinePlayer ~= nil then
            QPSR_sendServerCommandSafe(
                onlinePlayer,
                QPSR_Shared.COMMAND_REQUEST_UPDATED,
                payload
            )
        end
    end
end

-- QPSR_MULTI_ITEM_CONTRIBUTION_TRACKING_V2
-- All item-transfer signals for the same request share one debounce queue.
-- This prevents the first processed item from completing the request before
-- the remaining item contributors have been credited.
local function QPSR_progressCheckKey(args)
    return table.concat({
        tostring(args.x or ""),
        tostring(args.y or ""),
        tostring(args.z or ""),
        tostring(args.objectIndex or ""),
        tostring(args.requestContainerIndex or "")
    }, ":")
end


-- QPSR_PRE_TRANSACTION_ITEM_ID_INTENTS_V4
-- A client registers each exact InventoryItem ID before the vanilla
-- multiplayer transaction starts. The server credits only IDs that later
-- appear in the attached request container, so canceled transfers receive
-- no credit and mixed-item batches retain every contributor.
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

local function QPSR_findContainerItemById(container, itemId)
    if container == nil or tonumber(itemId) == nil then
        return nil
    end

    local resolvedId = math.floor(tonumber(itemId))

    if container.getItemWithID ~= nil then
        local ok, item = pcall(function()
            return container:getItemWithID(resolvedId)
        end)

        if ok and item ~= nil then
            return item
        end
    end

    if container.getItems == nil then
        return nil
    end

    local ok, items = pcall(function()
        return container:getItems()
    end)

    if not ok or items == nil then
        return nil
    end

    for index = 0, items:size() - 1 do
        local item = items:get(index)
        if QPSR_getInventoryItemId(item) == resolvedId then
            return item
        end
    end

    return nil
end

local function QPSR_queueTransferIntent(player, args)
    local object = QPSR_resolveObject(args)
    if object == nil or not QPSR_isPlayerNearObject(player, object) then
        return
    end

    local request = QPSR_readRequest(object)
    if request == nil or request.completed == true then
        return
    end

    local requestedIndex = tonumber(args.requestContainerIndex)
    local requestedType = tostring(args.requestContainerType or "")

    if requestedIndex ~= tonumber(request.containerIndex) or
       (requestedType ~= "" and
        requestedType ~= tostring(request.containerType or "")) then
        return
    end

    local itemFullType = QPSR_limitText(
        args.itemFullType,
        QPSR_Shared.MAX_ITEM_FULL_TYPE_LENGTH
    )
    local line = QPSR_Shared.getRequestItem(
        request.items,
        itemFullType
    )
    local itemId = tonumber(args.itemId)
    local username = QPSR_getPlayerName(player)
    local direction = tostring(args.direction or "add")

    if line == nil or
       itemId == nil or
       itemId < 0 or
       username == "" or
       (direction ~= "add" and direction ~= "remove") then
        return
    end

    if direction == "remove" and
       request.protectionLocked == true and
       not QPSR_isAdmin(player) then
        QPSR_sendResult(
            player,
            "protection",
            false,
            "UI_QPSR_ErrorProtectedWithdrawal"
        )
        print(
            "[QPSR] Rejected protected withdrawal intent from " ..
            username
        )
        return
    end

    itemId = math.floor(itemId)

    local key = QPSR_progressCheckKey(args)
    local entry = QPSR_Server.pendingTransferIntents[key]

    if entry == nil then
        entry = {
            args = {
                x = args.x,
                y = args.y,
                z = args.z,
                objectIndex = args.objectIndex,
                containerType = args.containerType,
                spriteName = args.spriteName,
                requestContainerIndex = args.requestContainerIndex,
                requestContainerType = args.requestContainerType
            },
            intents = {},
            seenIntentKeys = {},
            removeBaselineByType = {},
            nextOrder = 0,
            pollTicks = 1,
            settleTicks =
                tonumber(QPSR_Shared.TRANSFER_INTENT_SETTLE_TICKS) or
                120,
            ttlTicks =
                tonumber(QPSR_Shared.TRANSFER_INTENT_TTL_TICKS) or
                3600,
            lastContributor = ""
        }

        QPSR_Server.pendingTransferIntents[key] = entry
    end

    entry.seenIntentKeys = entry.seenIntentKeys or {}
    entry.removeBaselineByType =
        entry.removeBaselineByType or {}

    local intentKey = direction .. ":" .. tostring(itemId)

    if entry.seenIntentKeys[intentKey] == true then
        return
    end

    if #entry.intents >=
       (tonumber(QPSR_Shared.MAX_PENDING_TRANSFER_INTENTS) or 4000) then
        return
    end

    entry.nextOrder = (tonumber(entry.nextOrder) or 0) + 1
    entry.seenIntentKeys[intentKey] = true

    local beforeProgress = math.max(
        0,
        math.floor(tonumber(args.beforeProgress) or 0)
    )

    if direction == "remove" then
        local existingBaseline = tonumber(
            entry.removeBaselineByType[itemFullType]
        )

        if existingBaseline == nil or
           beforeProgress > existingBaseline then
            entry.removeBaselineByType[itemFullType] =
                beforeProgress
        end
    end

    table.insert(entry.intents, {
        itemId = itemId,
        itemFullType = itemFullType,
        username = username,
        direction = direction,
        beforeProgress = beforeProgress,
        order = entry.nextOrder
    })

    entry.pollTicks = 1
    entry.settleTicks =
        tonumber(QPSR_Shared.TRANSFER_INTENT_SETTLE_TICKS) or
        120
    entry.ttlTicks =
        tonumber(QPSR_Shared.TRANSFER_INTENT_TTL_TICKS) or
        3600
end


local function QPSR_processTransferIntentEntry(entry)
    if entry == nil or type(entry.args) ~= "table" then
        return true
    end

    local object = QPSR_resolveObject(entry.args)
    if object == nil then
        return entry.ttlTicks <= 0
    end

    local request = QPSR_readRequest(object)
    if request == nil or request.completed == true then
        return true
    end
    if tostring(request.activationState or QPSR_Shared.STATE_ACTIVE) == QPSR_Shared.STATE_LOCKED then
        return true
    end

    local container = QPSR_getRequestContainer(object, request)
    if container == nil then
        return entry.ttlTicks <= 0
    end

    request.itemDonors =
        type(request.itemDonors) == "table" and
        request.itemDonors or {}

    local oldProgressRaw = QPSR_Shared.encodeProgress(
        request.progressByType
    )
    local oldContributionsRaw =
        QPSR_Shared.encodeItemContributions(
            request.itemContributions
        )
    local oldDonorsRaw = QPSR_Shared.encodeItemDonors(
        request.itemDonors
    )

    local liveProgress = QPSR_countAllRequestItems(
        object,
        request
    )
    local desiredCreditByType = {}

    for _, line in ipairs(request.items or {}) do
        local fullType = tostring(line.fullType or "")
        local target = math.max(
            1,
            math.floor(tonumber(line.targetAmount) or 1)
        )
        local currentProgress = math.max(
            0,
            math.floor(tonumber(liveProgress[fullType]) or 0)
        )

        desiredCreditByType[fullType] = math.min(
            target,
            currentProgress
        )
    end

    local remainingIntents = {}
    local addIntents = {}
    local legacyRemovalNames = {}
    local creditedThisPass = 0
    local removedCreditThisPass = 0

    -- Process exact removals first. This lets a same-tick replacement item
    -- inherit the newly available credit slot even when net progress stays
    -- unchanged.
    for _, intent in ipairs(entry.intents or {}) do
        local fullType = tostring(intent.itemFullType or "")
        local direction = tostring(intent.direction or "add")
        local line = QPSR_Shared.getRequestItem(
            request.items,
            fullType
        )
        local item = QPSR_findContainerItemById(
            container,
            intent.itemId
        )
        local itemKey = tostring(
            math.floor(tonumber(intent.itemId) or -1)
        )

        if direction == "remove" then
            if line ~= nil and item == nil then
                local donor = request.itemDonors[itemKey]

                if type(donor) == "table" and
                   tostring(donor.fullType or "") == fullType and
                   tostring(donor.username or "") ~= "" then
                    local contributions =
                        request.itemContributions[fullType] or {}
                    local removedAmount = 0

                    request.itemContributions[fullType],
                    removedAmount =
                        QPSR_Shared.removeContribution(
                            contributions,
                            tostring(donor.username or ""),
                            1
                        )

                    removedCreditThisPass =
                        removedCreditThisPass +
                        removedAmount
                else
                    legacyRemovalNames[fullType] =
                        legacyRemovalNames[fullType] or {}
                    table.insert(
                        legacyRemovalNames[fullType],
                        tostring(intent.username or "")
                    )
                end

                request.itemDonors[itemKey] = nil
            else
                table.insert(remainingIntents, intent)
            end
        else
            table.insert(addIntents, intent)
        end
    end

    -- Clamp any legacy or previously inflated totals to the amount of
    -- requested stock that is actually present. Prefer the withdrawing
    -- player when exact item ownership data does not exist yet.
    for _, line in ipairs(request.items or {}) do
        local fullType = tostring(line.fullType or "")
        local desired = math.max(
            0,
            math.floor(
                tonumber(desiredCreditByType[fullType]) or 0
            )
        )
        local contributions =
            request.itemContributions[fullType] or {}
        local creditedTotal =
            QPSR_Shared.getContributionTotal(contributions)
        local excess = math.max(0, creditedTotal - desired)

        for _, preferredName in ipairs(
            legacyRemovalNames[fullType] or {}
        ) do
            if excess <= 0 then
                break
            end

            local removedAmount = 0
            contributions, removedAmount =
                QPSR_Shared.removeContribution(
                    contributions,
                    preferredName,
                    1
                )

            excess = excess - removedAmount
            removedCreditThisPass =
                removedCreditThisPass +
                removedAmount
        end

        if excess > 0 then
            local removedAmount = 0
            contributions, removedAmount =
                QPSR_Shared.removeContribution(
                    contributions,
                    "",
                    excess
                )

            removedCreditThisPass =
                removedCreditThisPass +
                removedAmount
        end

        request.itemContributions[fullType] = contributions
    end

    -- Then process exact additions. A confirmed item receives credit only
    -- when the current request stock still has an uncredited target slot.
    for _, intent in ipairs(addIntents) do
        local fullType = tostring(intent.itemFullType or "")
        local line = QPSR_Shared.getRequestItem(
            request.items,
            fullType
        )
        local item = QPSR_findContainerItemById(
            container,
            intent.itemId
        )
        local itemKey = tostring(
            math.floor(tonumber(intent.itemId) or -1)
        )

        if line ~= nil and
           item ~= nil and
           QPSR_matchesRequestedItem(item, line) then
            local existingDonor = request.itemDonors[itemKey]

            if existingDonor == nil then
                local contributions =
                    request.itemContributions[fullType] or {}
                local creditedTotal =
                    QPSR_Shared.getContributionTotal(contributions)
                local desired = math.max(
                    0,
                    math.floor(
                        tonumber(
                            desiredCreditByType[fullType]
                        ) or 0
                    )
                )

                if creditedTotal < desired then
                    local username =
                        tostring(intent.username or "")

                    request.itemContributions[fullType] =
                        QPSR_Shared.addContribution(
                            contributions,
                            username,
                            1
                        )

                    request.itemDonors[itemKey] = {
                        fullType = fullType,
                        username = username
                    }

                    creditedThisPass =
                        creditedThisPass + 1
                    entry.lastContributor = username
                end
            end

            -- The exact item reached the destination. Consume the intent even
            -- when it is excess stock or was already credited earlier.
        else
            table.insert(remainingIntents, intent)
        end
    end

    entry.intents = remainingIntents

    local allComplete = QPSR_Shared.areAllItemsComplete(
        request.items,
        liveProgress
    )

    -- Preserve previous stored progress during the final settle window.
    -- This prevents request loading from inferring completion before all
    -- late add/remove item-ID intents have been reconciled.
    if not allComplete then
        request.progressByType = liveProgress
    end

    local newProgressRaw = QPSR_Shared.encodeProgress(
        request.progressByType
    )
    local newContributionsRaw =
        QPSR_Shared.encodeItemContributions(
            request.itemContributions
        )
    local newDonorsRaw = QPSR_Shared.encodeItemDonors(
        request.itemDonors
    )

    local changed =
        oldProgressRaw ~= newProgressRaw or
        oldContributionsRaw ~= newContributionsRaw or
        oldDonorsRaw ~= newDonorsRaw

    if creditedThisPass > 0 or
       removedCreditThisPass > 0 then
        entry.settleTicks =
            tonumber(QPSR_Shared.TRANSFER_INTENT_SETTLE_TICKS) or
            120
    end

    local function finalizeRequest()
        request.progressByType = liveProgress
        request.completed = true

        for _, requestLine in ipairs(request.items or {}) do
            request.progressByType[requestLine.fullType] =
                requestLine.targetAmount
        end

        if tostring(request.fulfilledBy or "") == "" and
           tostring(entry.lastContributor or "") ~= "" then
            request.fulfilledBy =
                tostring(entry.lastContributor or "")
            request.fulfilledAt = QPSR_getWorldAgeHours()
        end

        request.protectionLocked =
            request.protectionEnabled == true and
            request.suppliesReleased ~= true

        -- Contributor totals become frozen completion history after this
        -- point, so exact active item ownership is no longer required.
        request.itemDonors = {}
        changed = true
    end

    if allComplete and entry.settleTicks <= 0 then
        finalizeRequest()
    end

    if changed and QPSR_writeRequest(object, request) then
        QPSR_broadcastState(object, request)

        if request.completed == true then
            QPSR_awardCompletedRequestCommunity(
                object,
                request
            )
        end

        local overall, total = QPSR_Shared.getOverallProgress(
            request.items,
            request.progressByType
        )

        print(
            "[QPSR] Net item-ID intents reconciled at " ..
            tostring(entry.args.x) .. "," ..
            tostring(entry.args.y) .. "," ..
            tostring(entry.args.z) ..
            " progress=" .. tostring(overall) ..
            "/" .. tostring(total) ..
            " completed=" ..
            tostring(request.completed == true) ..
            " credited=" .. tostring(creditedThisPass) ..
            " withdrawnCredit=" ..
            tostring(removedCreditThisPass) ..
            " pending=" .. tostring(#entry.intents)
        )
    end

    if request.completed == true then
        return true
    end

    if not allComplete and #entry.intents == 0 then
        return true
    end

    if entry.ttlTicks <= 0 then
        if allComplete then
            finalizeRequest()

            if QPSR_writeRequest(object, request) then
                QPSR_broadcastState(object, request)
                QPSR_awardCompletedRequestCommunity(
                    object,
                    request
                )
            end
        end

        return true
    end

    return false
end

local function QPSR_queueProgressCheck(player, args)
    local object = QPSR_resolveObject(args)
    if object == nil or not QPSR_isPlayerNearObject(player, object) then
        return
    end

    local request = QPSR_readRequest(object)
    if request == nil or request.completed == true then
        return
    end

    local itemFullType = QPSR_limitText(
        args.itemFullType,
        QPSR_Shared.MAX_ITEM_FULL_TYPE_LENGTH
    )
    local line = QPSR_Shared.getRequestItem(request.items, itemFullType)
    if line == nil then
        return
    end

    local requestedIndex = tonumber(args.requestContainerIndex)
    local requestedType = tostring(args.requestContainerType or "")
    if requestedIndex ~= tonumber(request.containerIndex) or
       (requestedType ~= "" and
        requestedType ~= tostring(request.containerType or "")) then
        return
    end

    local username = QPSR_getPlayerName(player)
    local direction = tostring(args.direction or "")
    local delta = math.floor(tonumber(args.delta) or 1)

    -- v0.6.5 uses exact add/remove item-ID intents.
    -- Keep this legacy command only for withdrawal/progress reconciliation.
    if direction == "add" then
        return
    end

    if username == "" or
       direction ~= "remove" or
       delta < 1 then
        return
    end

    delta = math.min(delta, 100)

    local key = QPSR_progressCheckKey(args)
    local entry = QPSR_Server.pendingProgressChecks[key]

    if entry == nil then
        entry = {
            args = {
                x = args.x,
                y = args.y,
                z = args.z,
                objectIndex = args.objectIndex,
                containerType = args.containerType,
                spriteName = args.spriteName,
                requestContainerIndex = args.requestContainerIndex
            },
            signals = {},
            ticks = tonumber(QPSR_Shared.PROGRESS_CHECK_DELAY_TICKS) or 10
        }
        QPSR_Server.pendingProgressChecks[key] = entry
    end

    if #entry.signals < QPSR_Shared.MAX_PENDING_PROGRESS_SIGNALS then
        table.insert(entry.signals, {
            itemFullType = itemFullType,
            username = username,
            direction = direction,
            delta = delta,
            beforeProgress = tonumber(args.beforeProgress)
        })
    end

    -- Any new transfer for this request restarts the shared debounce timer.
    entry.ticks = tonumber(QPSR_Shared.PROGRESS_CHECK_DELAY_TICKS) or 10
end

local function QPSR_processProgressCheck(entry)
    if entry == nil or type(entry.args) ~= "table" then
        return
    end

    local object = QPSR_resolveObject(entry.args)
    if object == nil then
        return
    end

    local request = QPSR_readRequest(object)
    if request == nil or request.completed == true then
        return
    end
    if tostring(request.activationState or QPSR_Shared.STATE_ACTIVE) == QPSR_Shared.STATE_LOCKED then
        return
    end

    -- v0.6.5 routes additions and contribution debits through exact item-ID intents. This legacy
    -- queue now reconciles withdrawals only, and a withdrawal can never be
    -- the event that completes a request.
    local liveProgress = QPSR_countAllRequestItems(object, request)

    if QPSR_Shared.areAllItemsComplete(
        request.items,
        liveProgress
    ) then
        return
    end

    local oldProgressRaw = QPSR_Shared.encodeProgress(
        request.progressByType
    )
    local newProgressRaw = QPSR_Shared.encodeProgress(
        liveProgress
    )

    if oldProgressRaw == newProgressRaw then
        return
    end

    request.progressByType = liveProgress

    if QPSR_writeRequest(object, request) then
        QPSR_broadcastState(object, request)

        local overall, total = QPSR_Shared.getOverallProgress(
            request.items,
            request.progressByType
        )

        print(
            "[QPSR] Withdrawal progress reconciled at " ..
            tostring(entry.args.x) .. "," ..
            tostring(entry.args.y) .. "," ..
            tostring(entry.args.z) ..
            " progress=" .. tostring(overall) ..
            "/" .. tostring(total)
        )
    end
end

-- QPSR_OPTIONAL_QPSC_ACTIVATION_V1
QPSR_Server.linkedRequests = QPSR_Server.linkedRequests or {}
QPSR_Server.activationPollTicks = QPSR_Server.activationPollTicks or 0

local function QPSR_linkedKey(args)
    return table.concat({tostring(args.x or ""), tostring(args.y or ""), tostring(args.z or ""), tostring(args.objectIndex or ""), tostring(args.requestContainerIndex or "")}, ":")
end

local function QPSR_registerLinkedRequest(object, request)
    if request == nil or tostring(request.linkedContractId or "") == "" then return end
    local locator = QPSR_buildLocatorPayload(object)
    if locator == nil then return end
    locator.requestContainerIndex = tonumber(request.containerIndex) or 0
    locator.requestContainerType = tostring(request.containerType or "")
    locator.contractId = tostring(request.linkedContractId or "")
    QPSR_Server.linkedRequests[QPSR_linkedKey(locator)] = locator
end

local function QPSR_tryActivateLinked(locator, eventKey)
    local object = QPSR_resolveObject(locator)
    if object == nil then return false end
    local request = QPSR_readRequest(object)
    if request == nil or tostring(request.activationState or "") ~= QPSR_Shared.STATE_LOCKED then return true end
    if tostring(request.linkedContractId or "") ~= tostring(locator.contractId or "") then return true end
    request.activationState = QPSR_Shared.STATE_ACTIVE
    request.activatedAt = QPSR_getWorldAgeHours()
    request.activationEventKey = tostring(eventKey or (tostring(locator.contractId) .. "|completed"))
    request.progressByType = QPSR_countAllRequestItems(object, request)
    request.completed = false
    if QPSR_writeRequest(object, request) then
        QPSR_broadcastState(object, request)
        print("[QPSR] Linked request activated by contract #" .. tostring(locator.contractId))
        return true
    end
    return false
end

local function QPSR_onContractCompleted(contractId, eventKey)
    for _, locator in pairs(QPSR_Server.linkedRequests) do
        if tostring(locator.contractId or "") == tostring(contractId or "") then
            QPSR_tryActivateLinked(locator, eventKey)
        end
    end
end

local function QPSR_installQPSCIntegration()
    if QPSR_Server.qpscListenerInstalled == true then return end
    if QPSC_ServerAPI ~= nil and type(QPSC_ServerAPI.registerCompletionListener) == "function" then
        if QPSC_ServerAPI.registerCompletionListener(QPSR_onContractCompleted) then
            QPSR_Server.qpscListenerInstalled = true
            print("[QPSR] Optional QPSC completion integration enabled.")
        end
    end
end

function QPSR_Server.onTick()
    QPSR_installQPSCIntegration()
    QPSR_Server.activationPollTicks = (tonumber(QPSR_Server.activationPollTicks) or 0) - 1
    if QPSR_Server.activationPollTicks <= 0 then
        QPSR_Server.activationPollTicks = 600
        if QPSC_ServerAPI ~= nil and type(QPSC_ServerAPI.isContractCompleted) == "function" then
            for _, locator in pairs(QPSR_Server.linkedRequests) do
                if QPSC_ServerAPI.isContractCompleted(locator.contractId) then
                    QPSR_tryActivateLinked(locator, tostring(locator.contractId) .. "|completed")
                end
            end
        end
    end
    for key, entry in pairs(QPSR_Server.pendingProgressChecks) do
        entry.ticks = (tonumber(entry.ticks) or 1) - 1

        if entry.ticks <= 0 then
            QPSR_Server.pendingProgressChecks[key] = nil
            QPSR_processProgressCheck(entry)
        end
    end

    for key, entry in pairs(QPSR_Server.pendingTransferIntents) do
        entry.pollTicks = (tonumber(entry.pollTicks) or 1) - 1
        entry.settleTicks =
            (tonumber(entry.settleTicks) or 0) - 1
        entry.ttlTicks =
            (tonumber(entry.ttlTicks) or 1) - 1

        if entry.pollTicks <= 0 then
            entry.pollTicks =
                tonumber(QPSR_Shared.TRANSFER_INTENT_POLL_TICKS) or
                5

            if QPSR_processTransferIntentEntry(entry) then
                QPSR_Server.pendingTransferIntents[key] = nil
            end
        end
    end
end

local function QPSR_validatePriority(priority)
    local value = tostring(priority or "Normal")
    if value == "Low" or value == "Normal" or value == "High" or value == "Critical" then
        return value
    end
    return "Normal"
end

local function QPSR_handleCreate(player, args)
    local object = QPSR_resolveObject(args)
    if object == nil then QPSR_sendResult(player, "create", false, "UI_QPSR_ErrorContainerUnavailable"); return end
    local canCreate, permissionError = QPSR_canCreateAtObject(player, object)
    if not canCreate then QPSR_sendResult(player, "create", false, permissionError or "UI_QPSR_ErrorCreateAdminOnly"); return end
    if QPSR_readRequest(object) ~= nil then QPSR_sendResult(player, "create", false, "UI_QPSR_ErrorExistingRequest"); return end

    local requestContainer, requestContainerIndex, requestContainerType = QPSR_resolveRequestContainer(object, args)
    if requestContainer == nil or requestContainerIndex < 0 then QPSR_sendResult(player, "create", false, "UI_QPSR_ErrorContainerUnavailable"); return end

    local decodedItems = QPSR_Shared.decodeRequestItems(args.itemsRaw)
    if #decodedItems < 1 or #decodedItems > QPSR_Shared.MAX_REQUEST_ITEMS then
        QPSR_sendResult(player, "create", false, "UI_QPSR_ErrorInvalidItems"); return
    end

    local items = {}
    local seen = {}
    for _, requested in ipairs(decodedItems) do
        local fullType = QPSR_limitText(requested.fullType, QPSR_Shared.MAX_ITEM_FULL_TYPE_LENGTH)
        local quantity = tonumber(requested.targetAmount)
        local scriptItem = QPSR_findScriptItem(fullType)
        if seen[fullType] or not QPSR_isScriptItemAllowed(scriptItem) or quantity == nil or quantity < 1 or quantity > QPSR_Shared.MAX_TARGET_AMOUNT or math.floor(quantity) ~= quantity then
            QPSR_sendResult(player, "create", false, "UI_QPSR_ErrorInvalidItems"); return
        end
        seen[fullType] = true
        table.insert(items, {fullType=fullType, displayName=QPSR_getScriptDisplayName(scriptItem, fullType), targetAmount=quantity})
    end

    local request = {
        title = QPSR_limitText(args.title, QPSR_Shared.MAX_REQUEST_TITLE_LENGTH),
        items = items,
        priority = QPSR_validatePriority(args.priority),
        note = QPSR_limitText(args.note, QPSR_Shared.MAX_NOTE_LENGTH),
        createdBy = QPSR_getPlayerName(player),
        createdAt = QPSR_getWorldAgeHours(),
        requestId = "",
        containerIndex = requestContainerIndex,
        containerType = requestContainerType,
        protectionEnabled = QPSR_isAdmin(player) and args.protectionEnabled == true,
        protectionLocked = false,
        progressByType = {},
        itemContributions = {},
        itemDonors = {},
        fulfilledBy = "",
        fulfilledAt = 0,
        completed = false,
        suppliesReleased = false,
        activationMode = tostring(args.activationMode or QPSR_Shared.ACTIVATION_IMMEDIATE),
        linkedContractId = QPSR_limitText(args.linkedContractId, 80),
        activationState = QPSR_Shared.STATE_ACTIVE,
        activatedAt = 0,
        activationEventKey = ""
    }
    if request.activationMode == QPSR_Shared.ACTIVATION_CONTRACT then
        if request.linkedContractId == "" then QPSR_sendResult(player, "create", false, "UI_QPSR_ErrorContractIdRequired"); return end
        request.activationState = QPSR_Shared.STATE_LOCKED
    else
        request.activationMode = QPSR_Shared.ACTIVATION_IMMEDIATE
        request.linkedContractId = ""
    end
    request.progressByType = QPSR_countAllRequestItems(object, request)
    request.completed = request.activationState ~= QPSR_Shared.STATE_LOCKED and QPSR_Shared.areAllItemsComplete(items, request.progressByType)
    if request.completed then for _, line in ipairs(items) do request.progressByType[line.fullType] = line.targetAmount end end
    request.suppliesReleased = request.protectionEnabled ~= true
    request.protectionLocked = request.protectionEnabled == true and request.suppliesReleased ~= true

    if request.createdBy == "" or not QPSR_writeRequest(object, request) then QPSR_sendResult(player, "create", false, "UI_QPSR_ErrorInvalidRequest"); return end
    QPSR_registerLinkedRequest(object, request)
    QPSR_broadcastState(object, request)
    QPSR_sendResult(player, "create", true, request.activationState == QPSR_Shared.STATE_LOCKED and "UI_QPSR_MessageCreatedLocked" or "UI_QPSR_MessageCreated")
    print("[QPSR] Multi-item request created by " .. request.createdBy .. " with " .. tostring(#items) .. " item lines at " .. tostring(args.x) .. "," .. tostring(args.y) .. "," .. tostring(args.z))
end

local function QPSR_handleRemove(player, args)
    local object = QPSR_resolveObject(args)
    if object == nil then
        QPSR_sendResult(player, "remove", false, "UI_QPSR_ErrorContainerUnavailable")
        return
    end

    local request = QPSR_readRequest(object)
    if request == nil then
        QPSR_sendResult(player, "remove", false, "UI_QPSR_ErrorNoRequest")
        return
    end

    local username = QPSR_getPlayerName(player)
    if not QPSR_isAdmin(player) then
        QPSR_sendResult(player, "remove", false, "UI_QPSR_ErrorNotAllowed")
        return
    end

    if not QPSR_clearRequest(object) then
        QPSR_sendResult(player, "remove", false, "UI_QPSR_ErrorContainerUnavailable")
        return
    end

    QPSR_broadcastState(object, nil)
    QPSR_sendResult(player, "remove", true, "UI_QPSR_MessageRemoved")
    print("[QPSR] Request removed by " .. username .. " at " .. tostring(args.x) .. "," .. tostring(args.y) .. "," .. tostring(args.z))
end


local function QPSR_handleReleaseSupplies(player, args)
    local object = QPSR_resolveObject(args)
    if object == nil or not QPSR_isPlayerNearObject(player, object) then
        QPSR_sendResult(player, "release", false, "UI_QPSR_ErrorContainerUnavailable")
        return
    end

    local request = QPSR_readRequest(object)
    if request == nil then
        QPSR_sendResult(player, "release", false, "UI_QPSR_ErrorNoRequest")
        return
    end

    local username = QPSR_getPlayerName(player)

    if not QPSR_isAdmin(player) then
        QPSR_sendResult(player, "release", false, "UI_QPSR_ErrorReleaseNotAllowed")
        return
    end

    if request.completed ~= true then
        QPSR_sendResult(player, "release", false, "UI_QPSR_ErrorRequestNotCompleted")
        return
    end

    if request.protectionEnabled ~= true then
        QPSR_sendResult(player, "release", true, "UI_QPSR_MessageSuppliesReleased")
        return
    end

    if request.suppliesReleased == true and request.protectionLocked ~= true then
        QPSR_sendResult(player, "release", true, "UI_QPSR_MessageSuppliesReleased")
        return
    end

    request.suppliesReleased = true
    request.protectionLocked = false

    if not QPSR_writeRequest(object, request) then
        QPSR_sendResult(player, "release", false, "UI_QPSR_ErrorContainerUnavailable")
        return
    end

    QPSR_broadcastState(object, request)
    QPSR_sendResult(player, "release", true, "UI_QPSR_MessageSuppliesReleased")
    print("[QPSR] Supplies released by " .. username)
end

function QPSR_Server.onClientCommand(module, command, player, args)
    if module ~= QPSR_Shared.MODULE then
        return
    end

    if type(args) ~= "table" then
        args = {}
    end

    if command == QPSR_Shared.COMMAND_CREATE then
        QPSR_handleCreate(player, args)
        return
    end

    if command == QPSR_Shared.COMMAND_REMOVE then
        QPSR_handleRemove(player, args)
        return
    end

    if command == QPSR_Shared.COMMAND_RELEASE_SUPPLIES or
       command == QPSR_Shared.COMMAND_UNLOCK_PROTECTION then
        QPSR_handleReleaseSupplies(player, args)
        return
    end

    if command == QPSR_Shared.COMMAND_TRANSFER_INTENT then
        QPSR_queueTransferIntent(player, args)
        return
    end

    if command == QPSR_Shared.COMMAND_PROGRESS_CHANGED then
        QPSR_queueProgressCheck(player, args)
    end
end

Events.OnClientCommand.Add(QPSR_Server.onClientCommand)
Events.OnTick.Add(QPSR_Server.onTick)

print("[QPSR] Server loaded v" .. tostring(QPSR_Shared.VERSION) .. " multi-item protected supply flow.")
