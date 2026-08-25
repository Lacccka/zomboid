-- QP Supply Requests
-- Shared constants and compact request serialization
-- v0.6.6: optional QP Community Reputation completion integration

QPSR_Shared = QPSR_Shared or {}

QPSR_Shared.MOD_ID = "QPSupplyRequests"
QPSR_Shared.MODULE = "QPSupplyRequests"
QPSR_Shared.DATA_KEY = "QPSR_Data"
QPSR_Shared.VERSION = "0.7.0 TC"

QPSR_Shared.COMMAND_CREATE = "CreateRequest"
QPSR_Shared.COMMAND_REMOVE = "RemoveRequest"
QPSR_Shared.COMMAND_REQUEST_UPDATED = "RequestUpdated"
QPSR_Shared.COMMAND_ACTION_RESULT = "ActionResult"
QPSR_Shared.COMMAND_PROGRESS_CHANGED = "ProgressChanged"
QPSR_Shared.COMMAND_TRANSFER_INTENT = "TransferIntent"
QPSR_Shared.COMMAND_UNLOCK_PROTECTION = "UnlockProtection" -- legacy compatibility
QPSR_Shared.COMMAND_RELEASE_SUPPLIES = "ReleaseSupplies"

QPSR_Shared.OBJECT_HAS_REQUEST = "QPSR_hasRequest"
QPSR_Shared.OBJECT_TITLE = "QPSR_title"
QPSR_Shared.OBJECT_ITEMS = "QPSR_items"
QPSR_Shared.OBJECT_ITEM_PROGRESS = "QPSR_itemProgress"
QPSR_Shared.OBJECT_ITEM_CONTRIBUTIONS = "QPSR_itemContributions"
QPSR_Shared.OBJECT_ITEM_DONORS = "QPSR_itemDonors" -- active request item ID -> donor
QPSR_Shared.OBJECT_ITEM_NAME = "QPSR_itemName" -- legacy first item
QPSR_Shared.OBJECT_ITEM_FULL_TYPE = "QPSR_itemFullType" -- legacy first item
QPSR_Shared.OBJECT_ITEM_DISPLAY_NAME = "QPSR_itemDisplayName" -- legacy first item
QPSR_Shared.OBJECT_TARGET_AMOUNT = "QPSR_targetAmount" -- legacy first item
QPSR_Shared.OBJECT_PRIORITY = "QPSR_priority"
QPSR_Shared.OBJECT_NOTE = "QPSR_note"
QPSR_Shared.OBJECT_CREATED_BY = "QPSR_createdBy"
QPSR_Shared.OBJECT_CREATED_AT = "QPSR_createdAt"
QPSR_Shared.OBJECT_REQUEST_ID = "QPSR_requestId"
QPSR_Shared.OBJECT_CONTAINER_INDEX = "QPSR_containerIndex"
QPSR_Shared.OBJECT_CONTAINER_TYPE = "QPSR_containerType"
QPSR_Shared.OBJECT_FULFILLED_BY = "QPSR_fulfilledBy"
QPSR_Shared.OBJECT_FULFILLED_AT = "QPSR_fulfilledAt"
QPSR_Shared.OBJECT_PROTECTION_ENABLED = "QPSR_protectionEnabled"
QPSR_Shared.OBJECT_PROTECTION_LOCKED = "QPSR_protectionLocked"
QPSR_Shared.OBJECT_CONTRIBUTIONS = "QPSR_contributions" -- legacy aggregate
QPSR_Shared.OBJECT_LAST_PROGRESS = "QPSR_lastProgress" -- legacy first item
QPSR_Shared.OBJECT_COMPLETED = "QPSR_completed"
QPSR_Shared.OBJECT_SUPPLIES_RELEASED = "QPSR_suppliesReleased"
QPSR_Shared.OBJECT_ACTIVATION_MODE = "QPSR_activationMode"
QPSR_Shared.OBJECT_LINKED_CONTRACT_ID = "QPSR_linkedContractId"
QPSR_Shared.OBJECT_ACTIVATION_STATE = "QPSR_activationState"
QPSR_Shared.OBJECT_ACTIVATED_AT = "QPSR_activatedAt"
QPSR_Shared.OBJECT_ACTIVATION_EVENT_KEY = "QPSR_activationEventKey"

QPSR_Shared.ACTIVATION_IMMEDIATE = "Immediate"
QPSR_Shared.ACTIVATION_CONTRACT = "Contract"
QPSR_Shared.STATE_LOCKED = "LOCKED"
QPSR_Shared.STATE_ACTIVE = "ACTIVE"

QPSR_Shared.MAX_REQUEST_TITLE_LENGTH = 80
QPSR_Shared.MAX_ITEM_NAME_LENGTH = 80
QPSR_Shared.MAX_ITEM_FULL_TYPE_LENGTH = 160
QPSR_Shared.MAX_NOTE_LENGTH = 160
QPSR_Shared.MAX_TARGET_AMOUNT = 100000
QPSR_Shared.MAX_REQUEST_ITEMS = 20
QPSR_Shared.MAX_ITEMS_SERIALIZED_LENGTH = 32768
QPSR_Shared.MAX_PROGRESS_SERIALIZED_LENGTH = 32768
QPSR_Shared.MAX_ITEM_CONTRIBUTIONS_SERIALIZED_LENGTH = 65535
QPSR_Shared.MAX_ITEM_DONORS_SERIALIZED_LENGTH = 262144
QPSR_Shared.MAX_CONTRIBUTION_NAME_LENGTH = 64
QPSR_Shared.MAX_CONTRIBUTION_SERIALIZED_LENGTH = 16384
QPSR_Shared.MAX_PENDING_PROGRESS_SIGNALS = 2000
QPSR_Shared.MAX_PENDING_TRANSFER_INTENTS = 4000

QPSR_Shared.MIN_SEARCH_LENGTH = 2
QPSR_Shared.MAX_SEARCH_RESULTS = 100
QPSR_Shared.CATALOG_BATCH_SIZE = 200
QPSR_Shared.SEARCH_DELAY_FRAMES = 6
QPSR_Shared.PROGRESS_CHECK_DELAY_TICKS = 10
QPSR_Shared.TRANSFER_INTENT_POLL_TICKS = 5
QPSR_Shared.TRANSFER_INTENT_SETTLE_TICKS = 120
QPSR_Shared.TRANSFER_INTENT_TTL_TICKS = 3600
QPSR_Shared.MAX_PROGRESS_CHECK_DISTANCE = 5

local function QPSR_sharedTrim(text)
    return tostring(text or ""):match("^%s*(.-)%s*$")
end

local function QPSR_encodeField(value)
    local text = tostring(value or "")
    text = string.gsub(text, "%%", "%%25")
    text = string.gsub(text, ";", "%%3B")
    text = string.gsub(text, ",", "%%2C")
    text = string.gsub(text, "\r", "%%0D")
    text = string.gsub(text, "\n", "%%0A")
    return text
end

local function QPSR_decodeField(value)
    local text = tostring(value or "")
    text = string.gsub(text, "%%0A", "\n")
    text = string.gsub(text, "%%0D", "\r")
    text = string.gsub(text, "%%2C", ",")
    text = string.gsub(text, "%%3B", ";")
    text = string.gsub(text, "%%25", "%%")
    return text
end

local function QPSR_splitRecord(record)
    local first = string.find(record, ",", 1, true)
    if first == nil then
        return nil, nil, nil
    end

    local second = string.find(record, ",", first + 1, true)
    if second == nil then
        return string.sub(record, 1, first - 1), string.sub(record, first + 1), nil
    end

    return string.sub(record, 1, first - 1),
        string.sub(record, first + 1, second - 1),
        string.sub(record, second + 1)
end

function QPSR_Shared.normalizeRequestItems(items)
    local result = {}
    local seen = {}

    if type(items) ~= "table" then
        return result
    end

    for _, item in ipairs(items) do
        if #result >= QPSR_Shared.MAX_REQUEST_ITEMS then
            break
        end

        local fullType = QPSR_sharedTrim(item.fullType or item.itemFullType)
        fullType = string.sub(fullType, 1, QPSR_Shared.MAX_ITEM_FULL_TYPE_LENGTH)
        local displayName = QPSR_sharedTrim(item.displayName or item.itemDisplayName or fullType)
        displayName = string.sub(displayName, 1, QPSR_Shared.MAX_ITEM_NAME_LENGTH)
        local targetAmount = math.floor(tonumber(item.targetAmount or item.amount) or 0)

        if fullType ~= "" and not seen[fullType] and
           targetAmount >= 1 and targetAmount <= QPSR_Shared.MAX_TARGET_AMOUNT then
            seen[fullType] = true
            table.insert(result, {
                fullType = fullType,
                displayName = displayName ~= "" and displayName or fullType,
                targetAmount = targetAmount
            })
        end
    end

    return result
end

function QPSR_Shared.encodeRequestItems(items)
    local normalized = QPSR_Shared.normalizeRequestItems(items)
    local rows = {}

    for _, item in ipairs(normalized) do
        table.insert(rows,
            QPSR_encodeField(item.fullType) .. "," ..
            QPSR_encodeField(item.displayName) .. "," ..
            tostring(item.targetAmount)
        )
    end

    local serialized = table.concat(rows, ";")
    if string.len(serialized) > QPSR_Shared.MAX_ITEMS_SERIALIZED_LENGTH then
        return ""
    end

    return serialized
end

function QPSR_Shared.decodeRequestItems(serialized)
    local items = {}
    local raw = tostring(serialized or "")
    if raw == "" then
        return items
    end

    for record in string.gmatch(raw, "[^;]+") do
        local fullTypeRaw, displayNameRaw, targetRaw = QPSR_splitRecord(record)
        if fullTypeRaw ~= nil and displayNameRaw ~= nil and targetRaw ~= nil then
            table.insert(items, {
                fullType = QPSR_decodeField(fullTypeRaw),
                displayName = QPSR_decodeField(displayNameRaw),
                targetAmount = math.floor(tonumber(targetRaw) or 0)
            })
        end
    end

    return QPSR_Shared.normalizeRequestItems(items)
end

function QPSR_Shared.getRequestItem(items, fullType)
    local requested = tostring(fullType or "")
    if type(items) ~= "table" or requested == "" then
        return nil
    end

    for _, item in ipairs(items) do
        if tostring(item.fullType or "") == requested then
            return item
        end
    end

    return nil
end

function QPSR_Shared.encodeProgress(progressByType)
    if type(progressByType) ~= "table" then
        return ""
    end

    local names = {}
    for fullType, amount in pairs(progressByType) do
        if tostring(fullType or "") ~= "" and tonumber(amount) ~= nil then
            table.insert(names, tostring(fullType))
        end
    end
    table.sort(names)

    local rows = {}
    for _, fullType in ipairs(names) do
        local amount = math.max(0, math.floor(tonumber(progressByType[fullType]) or 0))
        table.insert(rows, QPSR_encodeField(fullType) .. "," .. tostring(amount))
    end

    local serialized = table.concat(rows, ";")
    if string.len(serialized) > QPSR_Shared.MAX_PROGRESS_SERIALIZED_LENGTH then
        return ""
    end

    return serialized
end

function QPSR_Shared.decodeProgress(serialized)
    local result = {}
    local raw = tostring(serialized or "")
    if raw == "" then
        return result
    end

    for record in string.gmatch(raw, "[^;]+") do
        local fullTypeRaw, amountRaw = QPSR_splitRecord(record)
        if fullTypeRaw ~= nil and amountRaw ~= nil then
            local fullType = QPSR_decodeField(fullTypeRaw)
            local amount = math.max(0, math.floor(tonumber(amountRaw) or 0))
            if fullType ~= "" then
                result[fullType] = amount
            end
        end
    end

    return result
end

local function QPSR_encodeContributionName(name)
    local value = QPSR_sharedTrim(name)
    value = string.sub(value, 1, QPSR_Shared.MAX_CONTRIBUTION_NAME_LENGTH)
    value = string.gsub(value, "%%", "%%25")
    value = string.gsub(value, "|", "%%7C")
    value = string.gsub(value, "=", "%%3D")
    value = string.gsub(value, "\r", "%%0D")
    value = string.gsub(value, "\n", "%%0A")
    return value
end

local function QPSR_decodeContributionName(name)
    local value = tostring(name or "")
    value = string.gsub(value, "%%0A", "\n")
    value = string.gsub(value, "%%0D", "\r")
    value = string.gsub(value, "%%3D", "=")
    value = string.gsub(value, "%%7C", "|")
    value = string.gsub(value, "%%25", "%%")
    return string.sub(value, 1, QPSR_Shared.MAX_CONTRIBUTION_NAME_LENGTH)
end

function QPSR_Shared.decodeContributions(serialized)
    local result = {}
    local raw = tostring(serialized or "")

    if raw == "" then
        return result
    end

    for entry in string.gmatch(raw, "[^|]+") do
        local separator = string.find(entry, "=", 1, true)
        if separator ~= nil then
            local encodedName = string.sub(entry, 1, separator - 1)
            local amountText = string.sub(entry, separator + 1)
            local name = QPSR_decodeContributionName(encodedName)
            local amount = math.floor(tonumber(amountText) or 0)

            if name ~= "" and amount > 0 then
                result[name] = (tonumber(result[name]) or 0) + amount
            end
        end
    end

    return result
end

function QPSR_Shared.encodeContributions(contributions)
    if type(contributions) ~= "table" then
        return ""
    end

    local names = {}
    for name, amount in pairs(contributions) do
        local cleanName = QPSR_sharedTrim(name)
        local cleanAmount = math.floor(tonumber(amount) or 0)
        if cleanName ~= "" and cleanAmount > 0 then
            table.insert(names, cleanName)
        end
    end

    table.sort(names, function(left, right)
        return string.lower(left) < string.lower(right)
    end)

    local parts = {}
    for _, name in ipairs(names) do
        local amount = math.floor(tonumber(contributions[name]) or 0)
        if amount > 0 then
            table.insert(parts, QPSR_encodeContributionName(name) .. "=" .. tostring(amount))
        end
    end

    local serialized = table.concat(parts, "|")
    if string.len(serialized) > QPSR_Shared.MAX_CONTRIBUTION_SERIALIZED_LENGTH then
        return string.sub(serialized, 1, QPSR_Shared.MAX_CONTRIBUTION_SERIALIZED_LENGTH)
    end

    return serialized
end

function QPSR_Shared.encodeItemContributions(itemContributions)
    if type(itemContributions) ~= "table" then
        return ""
    end

    local fullTypes = {}
    for fullType, contributions in pairs(itemContributions) do
        if tostring(fullType or "") ~= "" and type(contributions) == "table" then
            table.insert(fullTypes, tostring(fullType))
        end
    end
    table.sort(fullTypes)

    local rows = {}
    for _, fullType in ipairs(fullTypes) do
        local encoded = QPSR_Shared.encodeContributions(itemContributions[fullType])
        if encoded ~= "" then
            table.insert(rows, QPSR_encodeField(fullType) .. "," .. QPSR_encodeField(encoded))
        end
    end

    local serialized = table.concat(rows, ";")
    if string.len(serialized) > QPSR_Shared.MAX_ITEM_CONTRIBUTIONS_SERIALIZED_LENGTH then
        return ""
    end

    return serialized
end

function QPSR_Shared.decodeItemContributions(serialized)
    local result = {}
    local raw = tostring(serialized or "")
    if raw == "" then
        return result
    end

    for record in string.gmatch(raw, "[^;]+") do
        local fullTypeRaw, contributionsRaw = QPSR_splitRecord(record)
        if fullTypeRaw ~= nil and contributionsRaw ~= nil then
            local fullType = QPSR_decodeField(fullTypeRaw)
            local contributions = QPSR_Shared.decodeContributions(QPSR_decodeField(contributionsRaw))
            if fullType ~= "" then
                result[fullType] = contributions
            end
        end
    end

    return result
end

function QPSR_Shared.encodeItemDonors(itemDonors)
    if type(itemDonors) ~= "table" then
        return ""
    end

    local itemIds = {}
    for itemId, donor in pairs(itemDonors) do
        local resolvedId = tonumber(itemId)
        if resolvedId ~= nil and
           resolvedId >= 0 and
           type(donor) == "table" and
           tostring(donor.fullType or "") ~= "" and
           tostring(donor.username or "") ~= "" then
            table.insert(itemIds, math.floor(resolvedId))
        end
    end

    table.sort(itemIds)

    local rows = {}
    for _, itemId in ipairs(itemIds) do
        local donor = itemDonors[tostring(itemId)] or itemDonors[itemId]
        if type(donor) == "table" then
            table.insert(
                rows,
                tostring(itemId) .. "," ..
                QPSR_encodeField(donor.fullType) .. "," ..
                QPSR_encodeField(donor.username)
            )
        end
    end

    local serialized = table.concat(rows, ";")
    if string.len(serialized) >
       QPSR_Shared.MAX_ITEM_DONORS_SERIALIZED_LENGTH then
        return ""
    end

    return serialized
end

function QPSR_Shared.decodeItemDonors(serialized)
    local result = {}
    local raw = tostring(serialized or "")
    if raw == "" then
        return result
    end

    for record in string.gmatch(raw, "[^;]+") do
        local itemIdRaw, fullTypeRaw, usernameRaw =
            QPSR_splitRecord(record)
        local itemId = tonumber(itemIdRaw)

        if itemId ~= nil and
           itemId >= 0 and
           fullTypeRaw ~= nil and
           usernameRaw ~= nil then
            local fullType = QPSR_decodeField(fullTypeRaw)
            local username = QPSR_decodeField(usernameRaw)

            if fullType ~= "" and username ~= "" then
                result[tostring(math.floor(itemId))] = {
                    fullType = string.sub(
                        fullType,
                        1,
                        QPSR_Shared.MAX_ITEM_FULL_TYPE_LENGTH
                    ),
                    username = string.sub(
                        username,
                        1,
                        QPSR_Shared.MAX_CONTRIBUTION_NAME_LENGTH
                    )
                }
            end
        end
    end

    return result
end

function QPSR_Shared.addContribution(contributions, username, amount)
    local result = contributions
    if type(result) ~= "table" then
        result = {}
    end

    local name = QPSR_sharedTrim(username)
    name = string.sub(name, 1, QPSR_Shared.MAX_CONTRIBUTION_NAME_LENGTH)
    local value = math.floor(tonumber(amount) or 0)

    if name ~= "" and value > 0 then
        result[name] = math.max(0, math.floor(tonumber(result[name]) or 0)) + value
    end

    return result
end

function QPSR_Shared.removeContribution(
    contributions,
    preferredUsername,
    amount
)
    local result = contributions
    if type(result) ~= "table" then
        result = {}
    end

    local remaining = math.max(
        0,
        math.floor(tonumber(amount) or 0)
    )
    local removed = 0
    local preferred = QPSR_sharedTrim(preferredUsername)

    local function removeFromName(name)
        if remaining <= 0 then
            return
        end

        local available = math.max(
            0,
            math.floor(tonumber(result[name]) or 0)
        )
        if available <= 0 then
            return
        end

        local take = math.min(available, remaining)
        local nextAmount = available - take

        if nextAmount > 0 then
            result[name] = nextAmount
        else
            result[name] = nil
        end

        remaining = remaining - take
        removed = removed + take
    end

    if preferred ~= "" then
        removeFromName(preferred)
    end

    if remaining > 0 then
        local names = {}
        for name, existingAmount in pairs(result) do
            if tostring(name) ~= preferred and
               tonumber(existingAmount) ~= nil and
               tonumber(existingAmount) > 0 then
                table.insert(names, tostring(name))
            end
        end

        table.sort(names, function(left, right)
            local leftAmount = math.floor(
                tonumber(result[left]) or 0
            )
            local rightAmount = math.floor(
                tonumber(result[right]) or 0
            )

            if leftAmount == rightAmount then
                return string.lower(left) < string.lower(right)
            end

            return leftAmount > rightAmount
        end)

        for _, name in ipairs(names) do
            removeFromName(name)
            if remaining <= 0 then
                break
            end
        end
    end

    return result, removed
end

function QPSR_Shared.aggregateItemContributions(itemContributions)
    local aggregate = {}
    if type(itemContributions) ~= "table" then
        return aggregate
    end

    for _, contributions in pairs(itemContributions) do
        if type(contributions) == "table" then
            for name, amount in pairs(contributions) do
                aggregate = QPSR_Shared.addContribution(aggregate, name, amount)
            end
        end
    end

    return aggregate
end

function QPSR_Shared.getContributionTotal(contributions)
    local total = 0
    if type(contributions) ~= "table" then
        return total
    end

    for _, amount in pairs(contributions) do
        total = total + math.max(0, math.floor(tonumber(amount) or 0))
    end

    return total
end

function QPSR_Shared.getSortedContributions(contributions)
    local result = {}
    if type(contributions) ~= "table" then
        return result
    end

    for name, amount in pairs(contributions) do
        local value = math.floor(tonumber(amount) or 0)
        if tostring(name or "") ~= "" and value > 0 then
            table.insert(result, {name = tostring(name), amount = value})
        end
    end

    table.sort(result, function(left, right)
        if left.amount == right.amount then
            return string.lower(left.name) < string.lower(right.name)
        end
        return left.amount > right.amount
    end)

    return result
end

function QPSR_Shared.getOverallProgress(items, progressByType)
    local current = 0
    local target = 0

    for _, item in ipairs(type(items) == "table" and items or {}) do
        local itemTarget = math.max(1, math.floor(tonumber(item.targetAmount) or 1))
        local itemProgress = math.max(0, math.floor(tonumber(
            type(progressByType) == "table" and progressByType[item.fullType] or 0
        ) or 0))
        current = current + math.min(itemProgress, itemTarget)
        target = target + itemTarget
    end

    return current, target
end

function QPSR_Shared.areAllItemsComplete(items, progressByType)
    local normalized = QPSR_Shared.normalizeRequestItems(items)
    if #normalized == 0 then
        return false
    end

    for _, item in ipairs(normalized) do
        local progress = math.floor(tonumber(
            type(progressByType) == "table" and progressByType[item.fullType] or 0
        ) or 0)
        if progress < item.targetAmount then
            return false
        end
    end

    return true
end

return QPSR_Shared
