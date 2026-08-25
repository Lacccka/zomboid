-- QP Survivor Contracts
-- QPSC_B41_DUAL_BUILD_V1
-- Build 41.78.19 compatibility copy for v1.3.0
-- Server-side storage and commands
-- v1.3.2 Production

-- QPSC_NUMERIC_TRANSLATION_FORMAT_HOTFIX_V1
-- Preserve numeric translation arguments as numbers.
local MODULE = "QPSurvivorContracts"
local DATA_KEY = "QPSC_Data"
local SCHEMA_VERSION = 12

-- QPSC_OPTIONAL_INTEGRATION_API_V1
QPSC_ServerAPI = QPSC_ServerAPI or {}
QPSC_ServerAPI._completionListeners = QPSC_ServerAPI._completionListeners or {}
QPSC_ServerAPI._notifiedContracts = QPSC_ServerAPI._notifiedContracts or {}

function QPSC_ServerAPI.registerCompletionListener(listener)
    if type(listener) ~= "function" then return false end
    table.insert(QPSC_ServerAPI._completionListeners, listener)
    return true
end

function QPSC_ServerAPI.isContractCompleted(contractId)
    local wanted = tostring(contractId or "")
    if wanted == "" or ModData == nil then return false end
    local data = ModData.getOrCreate(DATA_KEY)
    for _, contract in ipairs(data.contracts or {}) do
        if tostring(contract.id or "") == wanted then
            if contract.sharedCompleted == true or contract.globalCompleted == true then return true end
            for _, participant in ipairs(contract.participants or {}) do
                if tostring(participant.status or "") == "Completed" then return true end
            end
            return tostring(contract.status or "") == "Completed"
        end
    end
    return false
end

local function QPSC_notifyOptionalCompletion(contract)
    local contractId = tostring(contract and contract.id or "")
    if contractId == "" then return end
    local eventKey = contractId .. "|completed"
    if QPSC_ServerAPI._notifiedContracts[eventKey] == true then return end
    QPSC_ServerAPI._notifiedContracts[eventKey] = true
    for _, listener in ipairs(QPSC_ServerAPI._completionListeners or {}) do
        pcall(listener, contractId, eventKey)
    end
end

-- QPSC_V122_SERVER_INPUT_HARDENING_V1
local QPSC_TEXT_LIMITS = {
    title = 120,
    location = 160,
    description = 300,
    reward = 160,
    itemType = 160,
    itemName = 160,
    timeText = 64
}

local function QPSC_trimText(value)
    return tostring(value or "")
        :gsub("^%s+", "")
        :gsub("%s+$", "")
end

local function QPSC_limitText(value, maximum)
    local text = QPSC_trimText(value)
    local limit = math.max(0, tonumber(maximum) or 0)

    if limit > 0 and string.len(text) > limit then
        return string.sub(text, 1, limit)
    end

    return text
end

local function QPSC_isFiniteNumber(value)
    local number = tonumber(value)

    return number ~= nil
        and number == number
        and number ~= math.huge
        and number ~= -math.huge
end

local function QPSC_coordinateInputValid(value, minimum, maximum)
    if value == nil or tostring(value) == "" then
        return true
    end

    if not QPSC_isFiniteNumber(value) then
        return false
    end

    local number = tonumber(value)
    return number >= minimum and number <= maximum
end

local function QPSC_safeCoordinate(value, fallback, minimum, maximum)
    if QPSC_coordinateInputValid(value, minimum, maximum)
        and value ~= nil
        and tostring(value) ~= "" then
        return math.floor(tonumber(value))
    end

    local safeFallback = tonumber(fallback) or 0

    if safeFallback ~= safeFallback
        or safeFallback == math.huge
        or safeFallback == -math.huge then
        safeFallback = 0
    end

    return math.floor(
        math.max(minimum, math.min(maximum, safeFallback))
    )
end


-- QPSC_V122_R4_REQUEST_VALIDATION_V1
local QPSC_REQUEST_LIMITS = {
    entries = 160,
    keyBytes = 96,
    valueBytes = 2048,
    totalStringBytes = 8192
}

local function QPSC_enumInputValid(value, keys, allowBlank)
    local text = QPSC_trimText(value)

    if text == "" then
        return allowBlank == true
    end

    return keys[string.upper(text)] == true
end

local function QPSC_numberInputValid(
    value,
    allowBlank,
    minimum,
    maximum,
    integerOnly
)
    if value == nil or QPSC_trimText(value) == "" then
        return allowBlank == true
    end

    if not QPSC_isFiniteNumber(value) then
        return false
    end

    local number = tonumber(value)

    if minimum ~= nil and number < minimum then
        return false
    end

    if maximum ~= nil and number > maximum then
        return false
    end

    if integerOnly == true and number ~= math.floor(number) then
        return false
    end

    return true
end

local function QPSC_validateFlatRequest(args)
    if type(args) ~= "table" then
        return false, "payload is not a table"
    end

    local entries = 0
    local stringBytes = 0

    for key, value in pairs(args) do
        entries = entries + 1

        if entries > QPSC_REQUEST_LIMITS.entries then
            return false, "too many fields"
        end

        local keyType = type(key)

        if keyType ~= "string" and keyType ~= "number" then
            return false, "invalid field name"
        end

        local keyText = tostring(key)

        if string.len(keyText) > QPSC_REQUEST_LIMITS.keyBytes then
            return false, "field name is too long"
        end

        stringBytes = stringBytes + string.len(keyText)

        local valueType = type(value)

        if valueType == "table" then
            return false, "nested payloads are not allowed"
        end

        if valueType ~= "nil"
            and valueType ~= "string"
            and valueType ~= "number"
            and valueType ~= "boolean" then
            return false, "unsupported field type"
        end

        if valueType == "number" and not QPSC_isFiniteNumber(value) then
            return false, "non-finite numeric value"
        end

        if valueType == "string" then
            local valueLength = string.len(value)

            if valueLength > QPSC_REQUEST_LIMITS.valueBytes then
                return false, "individual text value is too large"
            end

            stringBytes = stringBytes + valueLength
        end

        if stringBytes > QPSC_REQUEST_LIMITS.totalStringBytes then
            return false, "total text payload is too large"
        end
    end

    return true, ""
end

-- QPSC_CONTRACT_CATEGORY_BADGES_V1
local QPSC_CATEGORY_KEYS = {
    NONE = true,
    FUEL = true,
    FOOD = true,
    MECHANIC = true,
    MEDICAL = true,
    CONSTRUCTION = true,
    DELIVERY = true,
    DANGER = true
}

local function QPSC_normalizeCategory(value)
    local category =
        string.upper(tostring(value or "NONE"))

    if QPSC_CATEGORY_KEYS[category] then
        return category
    end

    return "NONE"
end


-- QPSC_CONTRACT_DIFFICULTY_V070
local QPSC_DIFFICULTY_KEYS = {
    UNRATED = true,
    EASY = true,
    MEDIUM = true,
    HARD = true
}

local function QPSC_normalizeDifficulty(value)
    local difficulty = string.upper(tostring(value or "UNRATED"))

    if QPSC_DIFFICULTY_KEYS[difficulty] then
        return difficulty
    end

    return "UNRATED"
end


-- QPSC_SHARED_TEAM_COMPLETION_V090
local QPSC_COMPLETION_MODE_KEYS = {
    INDIVIDUAL = true,
    GLOBAL = true,
    SHARED_TEAM = true
}

local function QPSC_normalizeCompletionMode(value)
    local mode = string.upper(tostring(value or "INDIVIDUAL"))

    if QPSC_COMPLETION_MODE_KEYS[mode] then
        return mode
    end

    return "INDIVIDUAL"
end

local function QPSC_isGlobalCompletion(contract)
    return QPSC_normalizeCompletionMode(
        contract and contract.completionMode
    ) == "GLOBAL"
end

local function QPSC_isSharedTeamCompletion(contract)
    return QPSC_normalizeCompletionMode(
        contract and contract.completionMode
    ) == "SHARED_TEAM"
end


-- QPSC_TRACKED_OBJECTIVES_REWARDS_V1
local QPSC_OBJECTIVE_KEYS = {
    MANUAL = true,
    DELIVERY = true,
    KILL = true,
    LOCATION = true,
    MULTI = true
}

local function QPSC_normalizeObjectiveType(value)
    local objectiveType =
        string.upper(tostring(value or "MANUAL"))

    if QPSC_OBJECTIVE_KEYS[objectiveType] then
        return objectiveType
    end

    return "MANUAL"
end

local function QPSC_normalizePositiveInteger(value, maximum)
    local number = tonumber(value)

    if number == nil or number ~= number then
        return 0
    end

    number = math.floor(number)

    if number < 0 then
        return 0
    end

    if maximum ~= nil then
        number = math.min(number, maximum)
    end

    return number
end


-- QPSC_REPUTATION_REWARD_V110
local QPSC_REPUTATION_KEYS = {
    community = true,
    hunter = true,
    explorer = true,
    medic = true,
    mechanic = true,
    builder = true
}

local QPSC_REPUTATION_LABELS = {
    community = "Community",
    hunter = "Hunter",
    explorer = "Explorer",
    medic = "Medic",
    mechanic = "Mechanic",
    builder = "Builder"
}

local function QPSC_normalizeReputationPath(value)
    local path = string.lower(tostring(value or ""))
    path = path:gsub("^%s+", ""):gsub("%s+$", "")

    if QPSC_REPUTATION_KEYS[path] then
        return path
    end

    return ""
end

local function QPSC_reputationApiAvailable()
    return QPReputation ~= nil
        and QPReputation.Server ~= nil
        and type(QPReputation.Server.awardExternal) == "function"
end




local function QPSC_appendReputationRewardTexts(
    rewards,
    contract
)
    if type(rewards) ~= "table" then return end

    local primaryPath = QPSC_normalizeReputationPath(
        contract and contract.reputationPath
    )

    local function appendOne(pathValue, pointsValue)
        local path = QPSC_normalizeReputationPath(pathValue)
        local points = QPSC_normalizePositiveInteger(
            pointsValue,
            100000
        )

        if path ~= "" and points > 0 then
            table.insert(
                rewards,
                "+"
                    .. tostring(points)
                    .. " "
                    .. tostring(
                        QPSC_REPUTATION_LABELS[path]
                        or path
                    )
                    .. " Reputation"
            )
        end
    end

    appendOne(
        primaryPath,
        contract and contract.reputationPoints
    )

    local secondaryPath = QPSC_normalizeReputationPath(
        contract and contract.secondaryReputationPath
    )

    if secondaryPath ~= "" and secondaryPath ~= primaryPath then
        appendOne(
            secondaryPath,
            contract and contract.secondaryReputationPoints
        )
    end
end

-- QPSC_MULTI_OBJECTIVE_CONTRACTS_V100
local QPSC_MAX_OBJECTIVES = 5

local function QPSC_isMultiObjective(contract)
    return contract ~= nil
        and contract.multiObjective == true
        and type(contract.objectives) == "table"
        and #contract.objectives > 0
end

local function QPSC_multiObjectiveId(rawId, index)
    local value = tostring(rawId or "")

    if value == "" then
        value = "OBJ-" .. tostring(index or 1)
    end

    return string.sub(value, 1, 48)
end

local function QPSC_normalizeMultiObjective(raw, index, fallbackX, fallbackY, fallbackZ)
    raw = type(raw) == "table" and raw or {}
    local objectiveType = QPSC_normalizeObjectiveType(
        raw.type or raw.objectiveType
    )

    if objectiveType ~= "DELIVERY"
        and objectiveType ~= "KILL"
        and objectiveType ~= "LOCATION" then
        objectiveType = "KILL"
    end

    local target = QPSC_normalizePositiveInteger(
        raw.target or raw.objectiveTarget,
        10000
    )
    local radius = QPSC_normalizePositiveInteger(
        raw.radius or raw.objectiveRadius,
        1000
    )

    if objectiveType == "LOCATION" then
        target = 1
        radius = math.max(
            1,
            math.min(20, radius > 0 and radius or 3)
        )
    elseif objectiveType == "KILL" then
        target = math.max(1, target)
        radius = math.max(
            1,
            math.min(1000, radius > 0 and radius or 100)
        )
    else
        target = math.max(1, target)
        radius = 0
    end

    return {
        id = QPSC_multiObjectiveId(
            raw.id or raw.objectiveId,
            index
        ),
        type = objectiveType,
        target = target,
        radius = radius,
        itemFullType = QPSC_limitText(
            raw.itemFullType
                or raw.objectiveItemFullType
                or "",
            QPSC_TEXT_LIMITS.itemType
        ),
        itemDisplayName = QPSC_limitText(
            raw.itemDisplayName
                or raw.objectiveItemDisplayName
                or "",
            QPSC_TEXT_LIMITS.itemName
        ),
        targetX = QPSC_safeCoordinate(
            raw.targetX,
            fallbackX,
            -10000000,
            10000000
        ),
        targetY = QPSC_safeCoordinate(
            raw.targetY,
            fallbackY,
            -10000000,
            10000000
        ),
        targetZ = QPSC_safeCoordinate(
            raw.targetZ,
            fallbackZ,
            -64,
            64
        )
    }
end

local function QPSC_getMultiObjective(contract, objectiveId)
    if not QPSC_isMultiObjective(contract) then return nil end

    for _, objective in ipairs(contract.objectives or {}) do
        if tostring(objective.id or "") == tostring(objectiveId or "") then
            return objective
        end
    end

    return nil
end

local function QPSC_ensureMultiContractState(contract)
    if not QPSC_isMultiObjective(contract) then return false end
    local changed = false

    if type(contract.sharedObjectiveProgress) ~= "table" then
        contract.sharedObjectiveProgress = {}
        changed = true
    end

    for index, raw in ipairs(contract.objectives or {}) do
        local normalized = QPSC_normalizeMultiObjective(
            raw,
            index,
            contract.targetX,
            contract.targetY,
            contract.targetZ
        )
        contract.objectives[index] = normalized

        if QPSC_isSharedTeamCompletion(contract) then
            local current = QPSC_normalizePositiveInteger(
                contract.sharedObjectiveProgress[normalized.id],
                1000000
            )
            if contract.sharedObjectiveProgress[normalized.id] ~= current then
                contract.sharedObjectiveProgress[normalized.id] = current
                changed = true
            end
        end
    end

    contract.objectiveType = "MULTI"
    contract.objectiveTarget = #contract.objectives
    contract.objectiveRadius = 0
    contract.objectiveItemFullType = ""
    contract.objectiveItemDisplayName = ""
    return changed
end

local function QPSC_ensureParticipantMultiState(contract, participant)
    if not QPSC_isMultiObjective(contract) or participant == nil then
        return false
    end

    local changed = false

    if type(participant.objectiveProgress) ~= "table" then
        participant.objectiveProgress = {}
        changed = true
    end

    if type(participant.objectiveContributions) ~= "table" then
        participant.objectiveContributions = {}
        changed = true
    end

    for _, objective in ipairs(contract.objectives or {}) do
        local objectiveId = tostring(objective.id or "")
        local progress = QPSC_normalizePositiveInteger(
            participant.objectiveProgress[objectiveId],
            1000000
        )
        local contribution = QPSC_normalizePositiveInteger(
            participant.objectiveContributions[objectiveId],
            1000000
        )

        if participant.objectiveProgress[objectiveId] ~= progress then
            participant.objectiveProgress[objectiveId] = progress
            changed = true
        end

        if participant.objectiveContributions[objectiveId] ~= contribution then
            participant.objectiveContributions[objectiveId] = contribution
            changed = true
        end
    end

    return changed
end

local function QPSC_getMultiProgress(contract, participant, objective)
    if objective == nil then return 0 end
    local objectiveId = tostring(objective.id or "")

    if QPSC_isSharedTeamCompletion(contract) then
        contract.sharedObjectiveProgress = type(contract.sharedObjectiveProgress) == "table"
            and contract.sharedObjectiveProgress or {}
        return tonumber(contract.sharedObjectiveProgress[objectiveId]) or 0
    end

    QPSC_ensureParticipantMultiState(contract, participant)
    return participant and tonumber(participant.objectiveProgress[objectiveId]) or 0
end

local function QPSC_setMultiProgress(contract, participant, objective, value)
    if objective == nil then return 0 end
    local objectiveId = tostring(objective.id or "")
    local target = math.max(1, tonumber(objective.target) or 1)
    local normalized = math.min(
        target,
        QPSC_normalizePositiveInteger(value, 1000000)
    )

    if QPSC_isSharedTeamCompletion(contract) then
        contract.sharedObjectiveProgress = type(contract.sharedObjectiveProgress) == "table"
            and contract.sharedObjectiveProgress or {}
        contract.sharedObjectiveProgress[objectiveId] = normalized
    elseif participant ~= nil then
        QPSC_ensureParticipantMultiState(contract, participant)
        participant.objectiveProgress[objectiveId] = normalized
    end

    return normalized
end

local function QPSC_addMultiContribution(participant, objective, amount)
    if participant == nil or objective == nil then return 0 end
    participant.objectiveContributions = type(participant.objectiveContributions) == "table"
        and participant.objectiveContributions or {}
    local objectiveId = tostring(objective.id or "")
    local value = math.max(0, math.floor(tonumber(amount) or 0))
    participant.objectiveContributions[objectiveId] =
        (tonumber(participant.objectiveContributions[objectiveId]) or 0) + value
    return participant.objectiveContributions[objectiveId]
end

local function QPSC_getMultiContributionTotal(participant)
    local total = 0

    for _, value in pairs(
        type(participant and participant.objectiveContributions) == "table"
            and participant.objectiveContributions or {}
    ) do
        total = total + math.max(0, tonumber(value) or 0)
    end

    return total
end

local function QPSC_multiObjectiveCounts(contract, participant)
    local completed = 0
    local total = QPSC_isMultiObjective(contract) and #contract.objectives or 0

    for _, objective in ipairs(contract and contract.objectives or {}) do
        if QPSC_getMultiProgress(contract, participant, objective)
            >= math.max(1, tonumber(objective.target) or 1) then
            completed = completed + 1
        end
    end

    return completed, total
end

local function QPSC_allMultiObjectivesComplete(contract, participant)
    local completed, total = QPSC_multiObjectiveCounts(contract, participant)
    return total > 0 and completed >= total
end

local function QPSC_isPlayerNearMultiObjective(player, objective)
    if player == nil or objective == nil then return false end
    local dx = (tonumber(player:getX()) or 0) - (tonumber(objective.targetX) or 0)
    local dy = (tonumber(player:getY()) or 0) - (tonumber(objective.targetY) or 0)
    local dz = math.abs((tonumber(player:getZ()) or 0) - (tonumber(objective.targetZ) or 0))
    local radius = math.max(1, tonumber(objective.radius) or 3)
    return dz <= 1 and ((dx * dx) + (dy * dy)) <= (radius * radius)
end

-- QPSC_V122_ZOMBIE_KILL_ATTRIBUTION_HARDENING_V1
local function QPSC_getZombieObjectiveCoordinates(zombie)
    if zombie == nil then return nil, nil end

    -- A dead zombie's square is the most reliable position during
    -- OnZombieDead. Direct coordinates can be stale after death.
    if zombie.getSquare ~= nil then
        local squareOk, square = pcall(function()
            return zombie:getSquare()
        end)

        if squareOk
            and square ~= nil
            and square.getX ~= nil
            and square.getY ~= nil then
            local positionOk, squareX, squareY = pcall(function()
                return square:getX(), square:getY()
            end)

            squareX = tonumber(squareX)
            squareY = tonumber(squareY)

            if positionOk
                and squareX ~= nil
                and squareY ~= nil
                and squareX == squareX
                and squareY == squareY then
                return squareX, squareY
            end
        end
    end

    if zombie.getX ~= nil and zombie.getY ~= nil then
        local ok, zombieX, zombieY = pcall(function()
            return zombie:getX(), zombie:getY()
        end)

        zombieX = tonumber(zombieX)
        zombieY = tonumber(zombieY)

        if ok
            and zombieX ~= nil
            and zombieY ~= nil
            and zombieX == zombieX
            and zombieY == zombieY then
            return zombieX, zombieY
        end
    end

    return nil, nil
end

local function QPSC_isZombieInsideMultiObjective(zombie, objective)
    if zombie == nil or objective == nil then return false end

    local radius = math.max(
        1,
        tonumber(objective.radius) or 100
    )
    local zombieX, zombieY =
        QPSC_getZombieObjectiveCoordinates(zombie)

    if zombieX == nil or zombieY == nil then
        return false
    end

    local dx = zombieX
        - (tonumber(objective.targetX) or 0)
    local dy = zombieY
        - (tonumber(objective.targetY) or 0)

    return ((dx * dx) + (dy * dy))
        <= (radius * radius)
end

local function QPSC_getScriptItem(fullType)
    local itemType = tostring(fullType or "")

    if itemType == "" or getScriptManager == nil then
        return nil
    end

    local ok, result = pcall(function()
        local manager = getScriptManager()
        if manager == nil then return nil end

        if manager.FindItem ~= nil then
            return manager:FindItem(itemType)
        end

        if manager.getItem ~= nil then
            return manager:getItem(itemType)
        end

        return nil
    end)

    if not ok then return nil end
    return result
end

local function QPSC_getScriptDisplayName(fullType, fallback)
    local scriptItem = QPSC_getScriptItem(fullType)

    if scriptItem ~= nil and scriptItem.getDisplayName ~= nil then
        local ok, value = pcall(function()
            return scriptItem:getDisplayName()
        end)

        if ok and value ~= nil and tostring(value) ~= "" then
            return tostring(value)
        end
    end

    return tostring(fallback or fullType or "")
end

-- QPSC_V130_MULTIPLE_REWARDS_V1
local QPSC_MAX_REWARD_ITEMS = 5

local function QPSC_getContractRewardItems(contract)
    local rewards = {}
    local rawRewards =
        type(contract and contract.rewardItems) == "table"
        and contract.rewardItems
        or nil

    if rawRewards ~= nil then
        for index = 1, math.min(
            #rawRewards,
            QPSC_MAX_REWARD_ITEMS
        ) do
            local raw = rawRewards[index] or {}
            local fullType = QPSC_limitText(
                raw.fullType or raw.itemFullType,
                QPSC_TEXT_LIMITS.itemType
            )
            local displayName = QPSC_limitText(
                raw.displayName or raw.itemDisplayName,
                QPSC_TEXT_LIMITS.itemName
            )
            local quantity = QPSC_normalizePositiveInteger(
                raw.quantity,
                100
            )

            if fullType ~= "" and quantity > 0 then
                table.insert(rewards, {
                    fullType = fullType,
                    displayName = displayName,
                    quantity = quantity
                })
            end
        end
    end

    if #rewards == 0 and contract ~= nil then
        local legacyFullType = QPSC_limitText(
            contract.rewardItemFullType,
            QPSC_TEXT_LIMITS.itemType
        )
        local legacyDisplayName = QPSC_limitText(
            contract.rewardItemDisplayName,
            QPSC_TEXT_LIMITS.itemName
        )
        local legacyQuantity =
            QPSC_normalizePositiveInteger(
                contract.rewardQuantity,
                100
            )

        if legacyFullType ~= "" and legacyQuantity > 0 then
            table.insert(rewards, {
                fullType = legacyFullType,
                displayName = legacyDisplayName,
                quantity = legacyQuantity
            })
        end
    end

    return rewards
end

local function QPSC_rewardItemsEqual(left, right)
    if type(left) ~= "table" then left = {} end
    if type(right) ~= "table" then right = {} end
    if #left ~= #right then return false end

    for index = 1, #left do
        local a = left[index] or {}
        local b = right[index] or {}

        if tostring(a.fullType or a.itemFullType or "")
                ~= tostring(b.fullType or b.itemFullType or "")
            or tostring(a.displayName or a.itemDisplayName or "")
                ~= tostring(b.displayName or b.itemDisplayName or "")
            or QPSC_normalizePositiveInteger(a.quantity, 100)
                ~= QPSC_normalizePositiveInteger(b.quantity, 100) then
            return false
        end
    end

    return true
end

local function QPSC_applyContractRewardItems(
    contract,
    rewards
)
    if contract == nil then return end

    local normalized = {}
    for index = 1, math.min(
        #(rewards or {}),
        QPSC_MAX_REWARD_ITEMS
    ) do
        local reward = rewards[index] or {}
        local fullType = QPSC_limitText(
            reward.fullType or reward.itemFullType,
            QPSC_TEXT_LIMITS.itemType
        )
        local displayName = QPSC_limitText(
            reward.displayName or reward.itemDisplayName,
            QPSC_TEXT_LIMITS.itemName
        )
        local quantity = QPSC_normalizePositiveInteger(
            reward.quantity,
            100
        )

        if fullType ~= "" and quantity > 0 then
            table.insert(normalized, {
                fullType = fullType,
                displayName = displayName,
                quantity = quantity
            })
        end
    end

    contract.rewardItems = normalized

    local first = normalized[1]
    contract.rewardItemFullType =
        first and tostring(first.fullType or "") or ""
    contract.rewardItemDisplayName =
        first and tostring(first.displayName or "") or ""
    contract.rewardQuantity =
        first and QPSC_normalizePositiveInteger(
            first.quantity,
            100
        ) or 0
end

local function QPSC_cleanRewardItemsFromArgs(args)
    args = args or {}

    local rewards = {}
    local invalid = false
    local numericInvalidField = ""
    local seen = {}
    local hasExplicitCount =
        args.rewardCount ~= nil
        and QPSC_trimText(args.rewardCount) ~= ""

    local function markNumeric(field)
        if numericInvalidField == "" then
            numericInvalidField = tostring(field or "reward")
        end
        invalid = true
    end

    local function addReward(
        fullTypeValue,
        displayNameValue,
        quantityValue,
        quantityField
    )
        local fullType = QPSC_limitText(
            fullTypeValue,
            QPSC_TEXT_LIMITS.itemType
        )
        local displayName = QPSC_limitText(
            displayNameValue,
            QPSC_TEXT_LIMITS.itemName
        )

        if not QPSC_numberInputValid(
            quantityValue,
            false,
            1,
            100,
            true
        ) then
            markNumeric(quantityField)
            return
        end

        local quantity = math.floor(
            tonumber(quantityValue) or 0
        )

        if fullType == ""
            or quantity < 1
            or QPSC_getScriptItem(fullType) == nil
            or seen[fullType] == true then
            invalid = true
            return
        end

        seen[fullType] = true
        table.insert(rewards, {
            fullType = fullType,
            displayName = QPSC_getScriptDisplayName(
                fullType,
                displayName
            ),
            quantity = quantity
        })
    end

    if hasExplicitCount then
        if not QPSC_numberInputValid(
            args.rewardCount,
            false,
            0,
            QPSC_MAX_REWARD_ITEMS,
            true
        ) then
            markNumeric("rewardCount")
            return rewards, invalid, numericInvalidField
        end

        local count = math.floor(
            tonumber(args.rewardCount) or 0
        )

        for index = 1, count do
            local prefix = "reward" .. tostring(index)
            addReward(
                args[prefix .. "ItemFullType"],
                args[prefix .. "ItemDisplayName"],
                args[prefix .. "Quantity"],
                prefix .. "Quantity"
            )
        end

        if #rewards ~= count then
            invalid = true
        end
    else
        local legacyFullType = QPSC_limitText(
            args.rewardItemFullType,
            QPSC_TEXT_LIMITS.itemType
        )
        local legacyQuantity =
            QPSC_normalizePositiveInteger(
                args.rewardQuantity,
                100
            )

        if legacyFullType ~= "" or legacyQuantity > 0 then
            if not QPSC_numberInputValid(
                args.rewardQuantity,
                false,
                1,
                100,
                true
            ) then
                markNumeric("rewardQuantity")
            else
                addReward(
                    legacyFullType,
                    args.rewardItemDisplayName,
                    args.rewardQuantity,
                    "rewardQuantity"
                )
            end
        end
    end

    return rewards, invalid, numericInvalidField
end

local function QPSC_rewardItemTexts(contract)
    local texts = {}

    for _, reward in ipairs(
        QPSC_getContractRewardItems(contract)
    ) do
        table.insert(
            texts,
            tostring(reward.quantity)
                .. " x "
                .. tostring(
                    reward.displayName ~= ""
                    and reward.displayName
                    or reward.fullType
                )
        )
    end

    return texts
end

local function QPSC_rewardGrantedProgress(participant)
    local total = 0
    local counts =
        type(participant and participant.rewardGrantedCounts)
            == "table"
        and participant.rewardGrantedCounts
        or nil

    if counts ~= nil then
        for _, value in pairs(counts) do
            total = total
                + QPSC_normalizePositiveInteger(
                    value,
                    100
                )
        end
    else
        total = QPSC_normalizePositiveInteger(
            participant and participant.rewardGrantedCount,
            100
        )
    end

    return total
end

local function QPSC_normalizeParticipantRewardCounts(
    contract,
    participant
)
    local rewards = QPSC_getContractRewardItems(contract)
    local oldCounts =
        type(participant.rewardGrantedCounts) == "table"
        and participant.rewardGrantedCounts
        or {}
    local normalized = {}

    for index, reward in ipairs(rewards) do
        local count

        if participant.rewardGranted == true then
            count = reward.quantity
        elseif oldCounts[index] ~= nil then
            count = QPSC_normalizePositiveInteger(
                oldCounts[index],
                reward.quantity
            )
        elseif index == 1 then
            count = QPSC_normalizePositiveInteger(
                participant.rewardGrantedCount,
                reward.quantity
            )
        else
            count = 0
        end

        normalized[index] = math.min(
            reward.quantity,
            count
        )
    end

    participant.rewardGrantedCounts = normalized
    participant.rewardGrantedCount =
        tonumber(normalized[1]) or 0

    return normalized
end

-- Runtime-only timer marks. They are deliberately not persisted, so
-- offline time and server downtime never reduce a participant timer.
local QPSC_timerMarks = {}

local function QPSC_getData()
    local data = ModData.getOrCreate(DATA_KEY)

    if data.contracts == nil then data.contracts = {} end
    if data.nextId == nil then data.nextId = 1 end

    return data
end

local function QPSC_getUsername(player)
    if player == nil then return "Unknown" end

    local ok, result = pcall(function()
        return player:getUsername()
    end)

    if ok and result then
        return tostring(result)
    end

    return "Unknown"
end

local function QPSC_normalizeUsername(value)
    return string.lower(tostring(value or ""))
end

local function QPSC_sameUsername(left, right)
    return QPSC_normalizeUsername(left)
        == QPSC_normalizeUsername(right)
end


-- QPSC_V122_REPUTATION_DELIVERY_RECOVERY_V1
local function QPSC_awardContractReputation(
    contract,
    participant,
    actor
)
    local username = tostring(
        participant and participant.username or ""
    )

    if username == "" then
        return false, "disabled"
    end

    local primaryPath = QPSC_normalizeReputationPath(
        contract and contract.reputationPath
    )
    local primaryPoints = QPSC_normalizePositiveInteger(
        contract and contract.reputationPoints,
        100000
    )
    local secondaryPath = QPSC_normalizeReputationPath(
        contract and contract.secondaryReputationPath
    )
    local secondaryPoints = QPSC_normalizePositiveInteger(
        contract and contract.secondaryReputationPoints,
        100000
    )

    if secondaryPath == primaryPath then
        secondaryPath = ""
        secondaryPoints = 0
    end

    local primaryConfigured =
        primaryPath ~= "" and primaryPoints > 0
    local secondaryConfigured =
        secondaryPath ~= "" and secondaryPoints > 0
    local configuredCount =
        (primaryConfigured and 1 or 0)
        + (secondaryConfigured and 1 or 0)

    participant.reputationRewardAttempts =
        math.max(
            0,
            math.floor(
                tonumber(
                    participant.reputationRewardAttempts
                ) or 0
            )
        ) + 1

    if configuredCount < 1 then
        participant.reputationRewardPending = false
        participant.reputationRewardResolved = true
        participant.reputationRewardLastResult = "disabled"
        return false, "disabled"
    end

    if not QPSC_reputationApiAvailable() then
        participant.reputationRewardPending = true
        participant.reputationRewardResolved = false
        participant.reputationRewardLastResult =
            "api_unavailable"

        print(
            "[QPSC] Reputation reward pending for contract #"
                .. tostring(contract and contract.id or "")
                .. " and "
                .. username
                .. ": QP Survivor Reputation API unavailable."
        )

        return false, "api_unavailable"
    end

    local reason =
        "Contract completed: "
        .. tostring(
            contract and contract.title
            or ("#" .. tostring(contract and contract.id or ""))
        )
    local awardedAny = false
    local resolvedCount = 0
    local lastResult = "disabled"

    local function awardOne(
        path,
        points,
        sourceSuffix,
        rewardLabel
    )
        if path == "" or points < 1 then
            return false, true, "disabled"
        end

        local sourceId =
            "qpsc:contract:"
            .. tostring(contract and contract.id or "")
            .. ":participant:"
            .. QPSC_normalizeUsername(username)
            .. tostring(sourceSuffix)

        local ok, awarded, result = pcall(
            QPReputation.Server.awardExternal,
            username,
            path,
            points,
            sourceId,
            reason,
            tostring(actor or "QPSurvivorContracts")
        )

        if not ok then
            print(
                "[QPSC] Reputation integration error for contract #"
                    .. tostring(contract and contract.id or "")
                    .. " ("
                    .. tostring(rewardLabel)
                    .. "): "
                    .. tostring(awarded)
            )
            return false, false, "integration_error"
        end

        local resultText =
            string.lower(tostring(result or ""))
        local duplicate =
            resultText == "duplicate_award"
            or string.find(
                resultText,
                "duplicate",
                1,
                true
            ) ~= nil

        if awarded == true then
            print(
                "[QPSC] Awarded "
                    .. tostring(points)
                    .. " "
                    .. tostring(path)
                    .. " reputation to "
                    .. username
                    .. " for contract #"
                    .. tostring(contract and contract.id or "")
                    .. " ("
                    .. tostring(rewardLabel)
                    .. ")"
            )
            return true, true, tostring(result or "awarded")
        end

        if duplicate then
            print(
                "[QPSC] Reputation reward already resolved for contract #"
                    .. tostring(contract and contract.id or "")
                    .. " and "
                    .. username
                    .. " ("
                    .. tostring(rewardLabel)
                    .. ")."
            )
            return false, true, tostring(result or "duplicate_award")
        end

        print(
            "[QPSC] Reputation reward remains pending for contract #"
                .. tostring(contract and contract.id or "")
                .. " and "
                .. username
                .. " ("
                .. tostring(rewardLabel)
                .. "): "
                .. tostring(result)
        )

        return false, false, tostring(result or "not_awarded")
    end

    if primaryConfigured then
        local awarded, resolved, result =
            awardOne(
                primaryPath,
                primaryPoints,
                ":reputation:v1",
                "primary"
            )

        if awarded then awardedAny = true end
        if resolved then resolvedCount = resolvedCount + 1 end
        lastResult = result
    end

    if secondaryConfigured then
        local awarded, resolved, result =
            awardOne(
                secondaryPath,
                secondaryPoints,
                ":reputation:v1:secondary",
                "secondary"
            )

        if awarded then awardedAny = true end
        if resolved then resolvedCount = resolvedCount + 1 end
        lastResult = result
    end

    local allResolved = resolvedCount >= configuredCount

    participant.reputationRewardPending =
        not allResolved
    participant.reputationRewardResolved =
        allResolved
    participant.reputationRewardLastResult =
        tostring(lastResult or "")

    if allResolved then
        participant.reputationRewardResolvedBy =
            tostring(actor or "QPSurvivorContracts")
    end

    return awardedAny, lastResult
end



-- QPSC_V132_CLAIMABLE_OUTPOSTS_BRIDGE_CONFIGURABLE_TC3
-- Optional bridge only. No hard dependency:
-- activates only when ClaimableOutposts state/config and QPReputation API exist.
-- TC3:
--   - Keeps TC2 duplicate restart guard.
--   - Adds editable SandboxVars fallback settings:
--       QPSCClaimableOutpostsBridgeEnabled
--       QPSCClaimableOutpostsCommunityReward
--       QPSCClaimableOutpostsExplorerReward
local QPSC_CO_BRIDGE_DATA_KEY = "QPSC_ClaimableOutpostsBridge"
local QPSC_CO_BRIDGE_ACTOR = "QPSC Claimable Outposts Bridge"
local QPSC_CO_BRIDGE_DEFAULT_COMMUNITY_POINTS = 35
local QPSC_CO_BRIDGE_DEFAULT_EXPLORER_POINTS = 15
local QPSC_CO_bridgeLogState = QPSC_CO_bridgeLogState or {
    announcedLoaded = false,
    missingOutposts = false,
    missingReputation = false,
    initialized = false,
    disabled = false
}

local function QPSC_CO_bridgeDebug(message)
    print("[QPSC][ClaimableOutpostsBridge] " .. tostring(message))
end

local function QPSC_CO_bridgeGetSandboxNumber(key, defaultValue, minValue, maxValue)
    local value = nil

    if SandboxVars ~= nil then
        if SandboxVars[key] ~= nil then
            value = SandboxVars[key]
        elseif type(SandboxVars.QPSurvivorContracts) == "table"
            and SandboxVars.QPSurvivorContracts[key] ~= nil then
            value = SandboxVars.QPSurvivorContracts[key]
        elseif type(SandboxVars.QPSC) == "table"
            and SandboxVars.QPSC[key] ~= nil then
            value = SandboxVars.QPSC[key]
        end
    end

    local number = tonumber(value)
    if number == nil then
        number = tonumber(defaultValue) or 0
    end

    number = math.floor(number)

    if minValue ~= nil and number < minValue then
        number = minValue
    end

    if maxValue ~= nil and number > maxValue then
        number = maxValue
    end

    return number
end

local function QPSC_CO_bridgeGetSandboxBoolean(key, defaultValue)
    local value = nil

    if SandboxVars ~= nil then
        if SandboxVars[key] ~= nil then
            value = SandboxVars[key]
        elseif type(SandboxVars.QPSurvivorContracts) == "table"
            and SandboxVars.QPSurvivorContracts[key] ~= nil then
            value = SandboxVars.QPSurvivorContracts[key]
        elseif type(SandboxVars.QPSC) == "table"
            and SandboxVars.QPSC[key] ~= nil then
            value = SandboxVars.QPSC[key]
        end
    end

    if value == nil then
        return defaultValue == true
    end

    if type(value) == "boolean" then
        return value
    end

    local text = string.lower(tostring(value))
    if text == "false" or text == "0" or text == "no" or text == "off" then
        return false
    end

    return true
end

local function QPSC_CO_bridgeGetSettings()
    return {
        enabled = QPSC_CO_bridgeGetSandboxBoolean(
            "QPSCClaimableOutpostsBridgeEnabled",
            true
        ),
        communityPoints = QPSC_CO_bridgeGetSandboxNumber(
            "QPSCClaimableOutpostsCommunityReward",
            QPSC_CO_BRIDGE_DEFAULT_COMMUNITY_POINTS,
            0,
            10000
        ),
        explorerPoints = QPSC_CO_bridgeGetSandboxNumber(
            "QPSCClaimableOutpostsExplorerReward",
            QPSC_CO_BRIDGE_DEFAULT_EXPLORER_POINTS,
            0,
            10000
        )
    }
end

local function QPSC_CO_bridgeGetOutpostsConfig()
    if ClaimableOutposts == nil
        or ClaimableOutposts.Config == nil
        or type(ClaimableOutposts.Config.Outposts) ~= "table" then
        return nil
    end

    return ClaimableOutposts.Config.Outposts
end

local function QPSC_CO_bridgeGetStateData()
    if ModData == nil or ModData.getOrCreate == nil then
        return nil
    end

    local ok, data = pcall(ModData.getOrCreate, "ClaimableOutposts")
    if not ok or type(data) ~= "table" then
        return nil
    end

    if type(data.outposts) ~= "table" then
        return nil
    end

    return data
end

local function QPSC_CO_bridgeGetOwnData()
    if ModData == nil or ModData.getOrCreate == nil then
        return nil
    end

    local ok, data = pcall(ModData.getOrCreate, QPSC_CO_BRIDGE_DATA_KEY)
    if not ok or type(data) ~= "table" then
        return nil
    end

    data.schema = 3
    data.claimAwards = type(data.claimAwards) == "table" and data.claimAwards or {}
    data.observedClaims = type(data.observedClaims) == "table" and data.observedClaims or {}

    return data
end

local function QPSC_CO_bridgeStateForOutpost(stateData, outpost)
    if stateData == nil
        or type(stateData.outposts) ~= "table"
        or outpost == nil then
        return nil
    end

    local outpostId = tostring(outpost.id or "")
    if outpostId == "" then
        return nil
    end

    return stateData.outposts[outpostId]
end

local function QPSC_CO_bridgeStableKey(outpost, username)
    local outpostId = tostring(outpost and outpost.id or "")
    local normalizedUser = QPSC_normalizeUsername(username or "")

    if outpostId == "" or normalizedUser == "" then
        return ""
    end

    return outpostId .. "|" .. normalizedUser .. "|claim:v2"
end

local function QPSC_CO_bridgeAwardOne(username, path, points, sourceId, reason)
    if username == ""
        or path == ""
        or points < 1 then
        return false, true, "disabled"
    end

    if not QPSC_reputationApiAvailable() then
        return false, false, "api_unavailable"
    end

    local ok, awarded, result = pcall(
        QPReputation.Server.awardExternal,
        username,
        path,
        points,
        sourceId,
        reason,
        QPSC_CO_BRIDGE_ACTOR
    )

    if not ok then
        QPSC_CO_bridgeDebug(
            "Award error for "
                .. tostring(username)
                .. " "
                .. tostring(path)
                .. ": "
                .. tostring(awarded)
        )
        return false, false, "integration_error"
    end

    local resultText = string.lower(tostring(result or ""))
    local duplicate = resultText == "duplicate_award"
        or string.find(resultText, "duplicate", 1, true) ~= nil

    if awarded == true or duplicate then
        return awarded == true, true, tostring(result or (duplicate and "duplicate_award" or "awarded"))
    end

    return false, false, tostring(result or "not_awarded")
end

local function QPSC_CO_bridgeAwardClaim(outpost, state, bridgeData, settings)
    if outpost == nil or type(state) ~= "table" or bridgeData == nil then
        return false
    end

    if settings == nil or settings.enabled ~= true then
        return false
    end

    if state.claimed ~= true then
        return false
    end

    local username = tostring(state.claimedBy or "")
    if username == "" then
        return false
    end

    local outpostId = tostring(outpost.id or "")
    if outpostId == "" then
        return false
    end

    local awardKey = QPSC_CO_bridgeStableKey(outpost, username)
    if awardKey == "" then
        return false
    end

    if bridgeData.claimAwards[awardKey] == true
        or bridgeData.observedClaims[awardKey] == true then
        return false
    end

    local outpostName = tostring(outpost.name or outpostId)
    local reason = "Claimable Outpost secured: " .. outpostName
    local baseSource =
        "qpsc:claimableoutposts:"
        .. outpostId
        .. ":"
        .. QPSC_normalizeUsername(username)
        .. ":claim:v2"

    local communityAwarded, communityResolved, communityResult =
        QPSC_CO_bridgeAwardOne(
            username,
            "community",
            tonumber(settings.communityPoints) or 0,
            baseSource .. ":community",
            reason
        )

    local explorerAwarded, explorerResolved, explorerResult =
        QPSC_CO_bridgeAwardOne(
            username,
            "explorer",
            tonumber(settings.explorerPoints) or 0,
            baseSource .. ":explorer",
            reason
        )

    if communityResolved and explorerResolved then
        bridgeData.claimAwards[awardKey] = true
        bridgeData.observedClaims[awardKey] = true

        if ModData and ModData.transmit then
            ModData.transmit(QPSC_CO_BRIDGE_DATA_KEY)
        end

        QPSC_CO_bridgeDebug(
            "Claim reward resolved for "
                .. username
                .. " at "
                .. outpostName
                .. " | stableKey="
                .. tostring(awardKey)
                .. " settings={enabled="
                .. tostring(settings.enabled)
                .. ", community="
                .. tostring(settings.communityPoints)
                .. ", explorer="
                .. tostring(settings.explorerPoints)
                .. "} community="
                .. tostring(communityResult)
                .. " explorer="
                .. tostring(explorerResult)
        )

        return communityAwarded or explorerAwarded
    end

    QPSC_CO_bridgeDebug(
        "Claim reward pending for "
            .. username
            .. " at "
            .. outpostName
            .. " | stableKey="
            .. tostring(awardKey)
            .. " settings={enabled="
            .. tostring(settings.enabled)
            .. ", community="
            .. tostring(settings.communityPoints)
            .. ", explorer="
            .. tostring(settings.explorerPoints)
            .. "} community="
            .. tostring(communityResult)
            .. " explorer="
            .. tostring(explorerResult)
    )

    return false
end

local function QPSC_CO_bridgeMarkExistingClaims(outposts, stateData, bridgeData)
    if bridgeData == nil
        or bridgeData.initialized == true
        or type(outposts) ~= "table"
        or stateData == nil then
        return false
    end

    local marked = 0

    for _, outpost in ipairs(outposts) do
        local state = QPSC_CO_bridgeStateForOutpost(stateData, outpost)
        if type(state) == "table" and state.claimed == true then
            local username = tostring(state.claimedBy or "")
            local key = QPSC_CO_bridgeStableKey(outpost, username)
            if key ~= "" then
                bridgeData.observedClaims[key] = true
                marked = marked + 1
            end
        end
    end

    bridgeData.initialized = true
    bridgeData.initializedAt = os.time and os.time() or 0

    if ModData and ModData.transmit then
        ModData.transmit(QPSC_CO_BRIDGE_DATA_KEY)
    end

    QPSC_CO_bridgeDebug(
        "TC3 duplicate guard initialized; existing claimed outposts marked="
            .. tostring(marked)
    )

    return true
end

local function QPSC_CO_bridgeScan()
    local settings = QPSC_CO_bridgeGetSettings()

    if settings.enabled ~= true then
        if QPSC_CO_bridgeLogState.disabled ~= true then
            QPSC_CO_bridgeLogState.disabled = true
            QPSC_CO_bridgeDebug("Bridge disabled by SandboxVars; bridge idle.")
        end
        return
    end

    QPSC_CO_bridgeLogState.disabled = false

    local outposts = QPSC_CO_bridgeGetOutpostsConfig()
    local stateData = QPSC_CO_bridgeGetStateData()

    if outposts == nil or stateData == nil then
        if QPSC_CO_bridgeLogState.missingOutposts ~= true then
            QPSC_CO_bridgeLogState.missingOutposts = true
            QPSC_CO_bridgeDebug("Claimable Outposts not detected yet; bridge idle.")
        end
        return
    end

    QPSC_CO_bridgeLogState.missingOutposts = false

    if not QPSC_reputationApiAvailable() then
        if QPSC_CO_bridgeLogState.missingReputation ~= true then
            QPSC_CO_bridgeLogState.missingReputation = true
            QPSC_CO_bridgeDebug("QP Survivor Reputation API unavailable; claim rewards pending.")
        end
        return
    end

    QPSC_CO_bridgeLogState.missingReputation = false

    local bridgeData = QPSC_CO_bridgeGetOwnData()
    if bridgeData == nil then
        return
    end

    if bridgeData.initialized ~= true then
        QPSC_CO_bridgeMarkExistingClaims(outposts, stateData, bridgeData)
        return
    end

    local changed = false

    for _, outpost in ipairs(outposts) do
        local state = QPSC_CO_bridgeStateForOutpost(stateData, outpost)
        if state ~= nil then
            if QPSC_CO_bridgeAwardClaim(outpost, state, bridgeData, settings) then
                changed = true
            end
        end
    end

    if changed and ModData and ModData.transmit then
        ModData.transmit(QPSC_CO_BRIDGE_DATA_KEY)
    end
end

QPSC_ServerAPI.claimableOutpostsBridge = QPSC_ServerAPI.claimableOutpostsBridge or {}
QPSC_ServerAPI.claimableOutpostsBridge.scan = QPSC_CO_bridgeScan
QPSC_ServerAPI.claimableOutpostsBridge.version = "v1.3.2 Production"

local function QPSC_getWorldAgeHours()
    local ok, worldAgeHours = pcall(function()
        return getGameTime():getWorldAgeHours()
    end)

    if ok and worldAgeHours ~= nil then
        return tonumber(worldAgeHours) or 0
    end

    return 0
end

local function QPSC_normalizeTimeLimit(value)
    local number = tonumber(value)

    if number == nil or number ~= number or number <= 0 then
        return 0
    end

    return math.min(number, 8760)
end

local function QPSC_isTerminalParticipantStatus(status)
    status = tostring(status or "")

    return status == "Completed"
        or status == "NotCompleted"
        or status == "Expired"
        or status == "Cancelled"
        or status == "ClosedByOther"
end

local function QPSC_normalizeParticipantStatus(status)
    status = tostring(status or "Accepted")

    if status == "Not Completed" then
        return "NotCompleted"
    end

    if status == "Completed"
        or status == "NotCompleted"
        or status == "Expired"
        or status == "Cancelled"
        or status == "ClosedByOther" then
        return status
    end

    return "Accepted"
end

local function QPSC_findParticipant(contract, username)
    for _, participant in ipairs(contract.participants or {}) do
        if QPSC_sameUsername(participant.username, username) then
            return participant
        end
    end

    return nil
end

local function QPSC_firstParticipant(contract)
    local participants = contract.participants or {}
    return participants[1]
end

local function QPSC_timerKey(contractId, username)
    return tostring(contractId)
        .. "|"
        .. QPSC_normalizeUsername(username)
end

local function QPSC_recomputeContractStatus(contract)
    if QPSC_isSharedTeamCompletion(contract)
        and contract.sharedCompleted == true then
        contract.closed = true
        contract.status = "Completed"
        contract.acceptedBy = ""
        contract.completedBy = "Shared Team"
        return
    end

    if contract.globalCompleted == true then
        contract.closed = true
        contract.status = "Completed"
        contract.acceptedBy = ""
        contract.completedBy = tostring(
            contract.globalCompletedBy or ""
        )
        return
    end

    if contract.closed == true then
        contract.status =
            tostring(contract.legacyClosedStatus or "Closed")
        return
    end

    local hasActive = false
    local firstActive = nil

    for _, participant in ipairs(contract.participants or {}) do
        if tostring(participant.status or "") == "Accepted" then
            hasActive = true

            if firstActive == nil then
                firstActive = participant
            end
        end
    end

    contract.status = hasActive and "Accepted" or "Open"
    contract.acceptedBy =
        firstActive
        and tostring(firstActive.username or "")
        or ""

    contract.completedBy = ""

    for _, participant in ipairs(contract.participants or {}) do
        if tostring(participant.status or "") == "Completed"
            and tostring(participant.reviewedBy or "") ~= "" then
            contract.completedBy =
                tostring(participant.reviewedBy)
            break
        end
    end
end

-- QPSC_PARTICIPANT_MODEL_V1
local function QPSC_migrateContract(contract, now)
    local changed = false

    local normalizedCategory =
        QPSC_normalizeCategory(contract.category)

    if tostring(contract.category or "")
        ~= normalizedCategory then
        contract.category = normalizedCategory
        changed = true
    end

    local normalizedDifficulty =
        QPSC_normalizeDifficulty(contract.difficulty)

    if tostring(contract.difficulty or "")
        ~= normalizedDifficulty then
        contract.difficulty = normalizedDifficulty
        changed = true
    else
        contract.difficulty = normalizedDifficulty
    end

    local normalizedCompletionMode =
        QPSC_normalizeCompletionMode(contract.completionMode)

    if tostring(contract.completionMode or "")
        ~= normalizedCompletionMode then
        -- Existing contracts intentionally migrate to Individual.
        contract.completionMode = normalizedCompletionMode
        changed = true
    else
        contract.completionMode = normalizedCompletionMode
    end

    contract.globalCompleted = contract.globalCompleted == true
    contract.globalCompletedBy = tostring(
        contract.globalCompletedBy or ""
    )
    contract.globalCompletedAt = tonumber(
        contract.globalCompletedAt
    ) or 0
    contract.globalCompletionSource = tostring(
        contract.globalCompletionSource or ""
    )

    if contract.globalCompleted == true then
        contract.closed = true
        contract.legacyClosedStatus = "Completed"
    end


    -- Shared Team fields are initialized only for Shared Team contracts.
    -- Existing Individual and Global contracts remain byte-for-byte
    -- compatible in their persisted contract model.
    if QPSC_isSharedTeamCompletion(contract) then
        contract.sharedProgress =
            QPSC_normalizePositiveInteger(
                contract.sharedProgress,
                1000000
            )
        contract.sharedCompleted =
            contract.sharedCompleted == true
        contract.sharedCompletedBy = tostring(
            contract.sharedCompletedBy or ""
        )
        contract.sharedCompletedAt = tonumber(
            contract.sharedCompletedAt
        ) or 0
        contract.sharedCompletionSource = tostring(
            contract.sharedCompletionSource or ""
        )

        if contract.sharedCompleted == true then
            contract.closed = true
            contract.legacyClosedStatus = "Completed"
        end
    end

    if type(contract.objectives) == "table"
        and #contract.objectives > 0 then
        if contract.multiObjective ~= true then
            contract.multiObjective = true
            changed = true
        end

        if QPSC_ensureMultiContractState(contract) then
            changed = true
        end
    end

    local objectiveType = QPSC_isMultiObjective(contract)
        and "MULTI"
        or QPSC_normalizeObjectiveType(contract.objectiveType)

    if tostring(contract.objectiveType or "MANUAL")
        ~= objectiveType then
        contract.objectiveType = objectiveType
        changed = true
    else
        contract.objectiveType = objectiveType
    end

    local objectiveTarget =
        QPSC_normalizePositiveInteger(
            contract.objectiveTarget,
            10000
        )

    if objectiveType == "MULTI" then
        objectiveTarget = #(contract.objectives or {})
    elseif objectiveType ~= "MANUAL" and objectiveTarget < 1 then
        objectiveTarget = 1
    end

    if tonumber(contract.objectiveTarget) ~= objectiveTarget then
        contract.objectiveTarget = objectiveTarget
        changed = true
    end

    local objectiveRadius =
        QPSC_normalizePositiveInteger(
            contract.objectiveRadius,
            1000
        )

    if objectiveType == "LOCATION" then
        if objectiveRadius < 1 then
            objectiveRadius = 3
        elseif objectiveRadius > 20 then
            objectiveRadius = 20
        end
    end

    contract.objectiveRadius = objectiveRadius
    contract.objectiveItemFullType =
        tostring(contract.objectiveItemFullType or "")
    contract.objectiveItemDisplayName =
        tostring(contract.objectiveItemDisplayName or "")
    contract.targetX = tonumber(contract.targetX) or 0
    contract.targetY = tonumber(contract.targetY) or 0
    contract.targetZ = tonumber(contract.targetZ) or 0
    contract.rewardItemFullType =
        tostring(contract.rewardItemFullType or "")
    contract.rewardItemDisplayName =
        tostring(contract.rewardItemDisplayName or "")
    contract.rewardQuantity =
        QPSC_normalizePositiveInteger(
            contract.rewardQuantity,
            100
        )

    local normalizedRewardItems =
        QPSC_getContractRewardItems(contract)

    if not QPSC_rewardItemsEqual(
        contract.rewardItems,
        normalizedRewardItems
    ) then
        changed = true
    end

    QPSC_applyContractRewardItems(
        contract,
        normalizedRewardItems
    )

    contract.reputationPath =
        QPSC_normalizeReputationPath(
            contract.reputationPath
        )
    contract.reputationPoints =
        QPSC_normalizePositiveInteger(
            contract.reputationPoints,
            100000
        )

    if contract.reputationPath == ""
        or contract.reputationPoints < 1 then
        contract.reputationPath = ""
        contract.reputationPoints = 0
    end
    contract.secondaryReputationPath =
        QPSC_normalizeReputationPath(
            contract.secondaryReputationPath
        )
    contract.secondaryReputationPoints =
        QPSC_normalizePositiveInteger(
            contract.secondaryReputationPoints,
            100000
        )

    if contract.secondaryReputationPath == ""
        or contract.secondaryReputationPoints < 1
        or contract.secondaryReputationPath
            == contract.reputationPath then
        contract.secondaryReputationPath = ""
        contract.secondaryReputationPoints = 0
    end

    contract.firstFinisherBonusItemFullType =
        tostring(
            contract.firstFinisherBonusItemFullType or ""
        )
    contract.firstFinisherBonusItemDisplayName =
        tostring(
            contract.firstFinisherBonusItemDisplayName or ""
        )
    contract.firstFinisherBonusQuantity =
        QPSC_normalizePositiveInteger(
            contract.firstFinisherBonusQuantity,
            100
        )
    contract.firstFinisherWinner =
        tostring(contract.firstFinisherWinner or "")
    contract.firstFinisherWonAt =
        tonumber(contract.firstFinisherWonAt) or 0

    if type(contract.participants) ~= "table" then
        contract.participants = {}
        changed = true
    end

    local timeLimitHours =
        QPSC_normalizeTimeLimit(contract.timeLimitHours)

    if tonumber(contract.timeLimitHours) ~= timeLimitHours then
        contract.timeLimitHours = timeLimitHours
        changed = true
    end

    if #contract.participants == 0 then
        local legacyAcceptedBy =
            tostring(contract.acceptedBy or "")
        local legacyStatus =
            tostring(contract.status or "Open")

        if legacyAcceptedBy ~= "" then
            local participantStatus = "Accepted"

            if legacyStatus == "Completed" then
                participantStatus = "Completed"
            elseif legacyStatus == "Expired" then
                participantStatus = "Expired"
            end

            local remainingHours = 0

            if participantStatus == "Accepted"
                and timeLimitHours > 0 then
                local legacyExpiresAt =
                    tonumber(contract.expiresAt) or 0

                if legacyExpiresAt > 0 then
                    remainingHours =
                        math.max(0, legacyExpiresAt - now)
                else
                    remainingHours = timeLimitHours
                end

                if remainingHours <= 0 then
                    participantStatus = "Expired"
                end
            end

            table.insert(contract.participants, {
                username = legacyAcceptedBy,
                status = participantStatus,
                acceptedAt =
                    tonumber(contract.acceptedAt) or 0,
                remainingHours = remainingHours,
                completedAt = 0,
                expiredAt =
                    tonumber(contract.expiredAt) or 0,
                cancelledAt = 0,
                closedAt = 0,
                closedBy = "",
                reviewedAt = 0,
                reviewedBy =
                    participantStatus == "Completed"
                    and tostring(contract.completedBy or "")
                    or "",
                progress = 0,
                rewardGranted = false,
                rewardGrantedCount = 0,
                rewardGrantedCounts = {},
                rewardGrantedAt = 0,
                rewardPending = false,
                firstFinisherBonusGranted = false,
                firstFinisherBonusGrantedCount = 0,
                firstFinisherBonusGrantedAt = 0,
                firstFinisherBonusPending = false
            })

            if legacyStatus == "Completed"
                or legacyStatus == "Expired" then
                contract.closed = true
                contract.legacyClosedStatus = legacyStatus
            end

            changed = true
        elseif legacyStatus == "Completed"
            or legacyStatus == "Expired"
            or legacyStatus == "Closed" then
            contract.closed = true
            contract.legacyClosedStatus = legacyStatus
            changed = true
        end
    end

    for _, participant in ipairs(contract.participants) do
        local normalizedStatus =
            QPSC_normalizeParticipantStatus(
                participant.status
            )

        if participant.status ~= normalizedStatus then
            participant.status = normalizedStatus
            changed = true
        end

        participant.username =
            tostring(participant.username or "")
        participant.acceptedAt =
            tonumber(participant.acceptedAt) or 0
        participant.completedAt =
            tonumber(participant.completedAt) or 0
        participant.expiredAt =
            tonumber(participant.expiredAt) or 0
        participant.cancelledAt =
            tonumber(participant.cancelledAt) or 0
        participant.closedAt =
            tonumber(participant.closedAt) or 0
        participant.closedBy =
            tostring(participant.closedBy or "")
        participant.reviewedAt =
            tonumber(participant.reviewedAt) or 0
        participant.reviewedBy =
            tostring(participant.reviewedBy or "")
        participant.progress =
            QPSC_normalizePositiveInteger(
                participant.progress,
                1000000
            )
        participant.rewardGranted =
            participant.rewardGranted == true
        participant.rewardGrantedCount =
            QPSC_normalizePositiveInteger(
                participant.rewardGrantedCount,
                100
            )
        participant.rewardGrantedAt =
            tonumber(participant.rewardGrantedAt) or 0
        participant.rewardPending =
            participant.rewardPending == true

        local previousRewardCounts =
            participant.rewardGrantedCounts
        local previousRewardCount =
            participant.rewardGrantedCount

        QPSC_normalizeParticipantRewardCounts(
            contract,
            participant
        )

        if type(previousRewardCounts) ~= "table"
            or tonumber(previousRewardCount or 0)
                ~= tonumber(participant.rewardGrantedCount or 0) then
            changed = true
        end

        participant.firstFinisherBonusGranted =
            participant.firstFinisherBonusGranted == true
        participant.firstFinisherBonusGrantedCount =
            QPSC_normalizePositiveInteger(
                participant.firstFinisherBonusGrantedCount,
                100
            )
        participant.firstFinisherBonusGrantedAt =
            tonumber(
                participant.firstFinisherBonusGrantedAt
            ) or 0
        participant.firstFinisherBonusPending =
            participant.firstFinisherBonusPending == true

        if QPSC_ensureParticipantMultiState(contract, participant) then
            changed = true
        end

        local remainingHours =
            tonumber(participant.remainingHours)

        if remainingHours == nil then
            remainingHours = 0

            if participant.status == "Accepted"
                and timeLimitHours > 0 then
                local legacyExpiresAt =
                    tonumber(contract.expiresAt) or 0

                if legacyExpiresAt > 0 then
                    remainingHours =
                        math.max(0, legacyExpiresAt - now)
                else
                    remainingHours = timeLimitHours
                end
            end

            participant.remainingHours = remainingHours
            changed = true
        else
            local normalizedRemaining =
                math.max(0, remainingHours)

            if normalizedRemaining ~= remainingHours then
                participant.remainingHours =
                    normalizedRemaining
                changed = true
            end
        end

        if participant.status == "Accepted"
            and timeLimitHours > 0
            and (tonumber(participant.remainingHours) or 0) <= 0 then
            participant.status = "Expired"
            participant.expiredAt = now
            changed = true
        end
    end

    local oldStatus = tostring(contract.status or "")
    local oldAcceptedBy =
        tostring(contract.acceptedBy or "")
    local oldCompletedBy =
        tostring(contract.completedBy or "")

    QPSC_recomputeContractStatus(contract)

    if oldStatus ~= tostring(contract.status or "")
        or oldAcceptedBy ~= tostring(contract.acceptedBy or "")
        or oldCompletedBy ~= tostring(contract.completedBy or "") then
        changed = true
    end

    -- Legacy absolute deadlines are retained for compatibility but are
    -- no longer used after migration.
    contract.acceptedAt =
        tonumber(contract.acceptedAt) or 0
    contract.expiresAt =
        tonumber(contract.expiresAt) or 0
    contract.expiredAt =
        tonumber(contract.expiredAt) or 0

    return changed
end

local function QPSC_migrateData(data)
    data = data or QPSC_getData()

    local now = QPSC_getWorldAgeHours()
    local changed = false

    for _, contract in ipairs(data.contracts or {}) do
        if QPSC_migrateContract(contract, now) then
            changed = true
        end
    end

    if tonumber(data.schemaVersion) ~= SCHEMA_VERSION then
        data.schemaVersion = SCHEMA_VERSION
        changed = true
    end

    return changed
end


-- QPSC_V122_R4_SAVE_DIAGNOSTICS_V1
local QPSC_DIAGNOSTIC_CONTRACT_STATUS_KEYS = {
    Open = true,
    Accepted = true,
    Completed = true,
    Expired = true,
    Closed = true
}

local QPSC_DIAGNOSTIC_PARTICIPANT_STATUS_KEYS = {
    Accepted = true,
    Completed = true,
    NotCompleted = true,
    Expired = true,
    Cancelled = true,
    ClosedByOther = true
}

local function QPSC_runSaveDiagnostics(data, migrationApplied)
    data = data or {}

    local contractCount = 0
    local participantCount = 0
    local objectiveCount = 0
    local normalRewardsGranted = 0
    local firstBonusesGranted = 0
    local pendingRewards = 0
    local warnings = {}
    local seenContractIds = {}
    local highestNumericId = 0

    local function warn(code, detail)
        table.insert(
            warnings,
            tostring(code or "warning")
                .. ": "
                .. tostring(detail or "")
        )
    end

    if type(data.contracts) ~= "table" then
        warn("contracts_table", "QPSC_Data.contracts is not a table")
    else
        for index, contract in ipairs(data.contracts) do
            contractCount = contractCount + 1

            if type(contract) ~= "table" then
                warn(
                    "contract_entry",
                    "entry " .. tostring(index) .. " is not a table"
                )
            else
                local contractId = tostring(contract.id or "")

                if contractId == "" then
                    warn(
                        "contract_id",
                        "entry " .. tostring(index) .. " has no ID"
                    )
                elseif seenContractIds[contractId] then
                    warn(
                        "contract_id",
                        "duplicate contract ID " .. contractId
                    )
                else
                    seenContractIds[contractId] = true
                end

                local numericId = tonumber(contract.id)

                if numericId ~= nil and numericId > highestNumericId then
                    highestNumericId = numericId
                end

                local contractStatus = tostring(contract.status or "")

                if not QPSC_DIAGNOSTIC_CONTRACT_STATUS_KEYS[
                    contractStatus
                ] then
                    warn(
                        "contract_status",
                        "contract #"
                            .. contractId
                            .. " has status "
                            .. contractStatus
                    )
                end

                if QPSC_isMultiObjective(contract) then
                    local seenObjectiveIds = {}

                    for objectiveIndex, objective in ipairs(
                        contract.objectives or {}
                    ) do
                        objectiveCount = objectiveCount + 1
                        local objectiveId = tostring(
                            objective and objective.id or ""
                        )

                        if objectiveId == "" then
                            warn(
                                "objective_id",
                                "contract #"
                                    .. contractId
                                    .. " objective "
                                    .. tostring(objectiveIndex)
                                    .. " has no ID"
                            )
                        elseif seenObjectiveIds[objectiveId] then
                            warn(
                                "objective_id",
                                "contract #"
                                    .. contractId
                                    .. " has duplicate objective ID "
                                    .. objectiveId
                            )
                        else
                            seenObjectiveIds[objectiveId] = true
                        end
                    end
                end

                local seenParticipants = {}

                for participantIndex, participant in ipairs(
                    contract.participants or {}
                ) do
                    participantCount = participantCount + 1

                    if type(participant) ~= "table" then
                        warn(
                            "participant_entry",
                            "contract #"
                                .. contractId
                                .. " participant "
                                .. tostring(participantIndex)
                                .. " is not a table"
                        )
                    else
                        local username = tostring(
                            participant.username or ""
                        )
                        local usernameKey = string.lower(username)

                        if username == "" then
                            warn(
                                "participant_username",
                                "contract #"
                                    .. contractId
                                    .. " has a participant without username"
                            )
                        elseif seenParticipants[usernameKey] then
                            warn(
                                "participant_username",
                                "contract #"
                                    .. contractId
                                    .. " has duplicate participant "
                                    .. username
                            )
                        else
                            seenParticipants[usernameKey] = true
                        end

                        local participantStatus = tostring(
                            participant.status or ""
                        )

                        if not QPSC_DIAGNOSTIC_PARTICIPANT_STATUS_KEYS[
                            participantStatus
                        ] then
                            warn(
                                "participant_status",
                                "contract #"
                                    .. contractId
                                    .. " participant "
                                    .. username
                                    .. " has status "
                                    .. participantStatus
                            )
                        end

                        if participant.rewardGranted == true then
                            normalRewardsGranted =
                                normalRewardsGranted + 1
                        end

                        if participant.firstFinisherBonusGranted == true then
                            firstBonusesGranted =
                                firstBonusesGranted + 1
                        end

                        if participant.rewardPending == true then
                            pendingRewards = pendingRewards + 1
                        end

                        if participant.firstFinisherBonusPending == true then
                            pendingRewards = pendingRewards + 1
                        end
                    end
                end
            end
        end
    end

    local nextId = tonumber(data.nextId) or 0

    if nextId <= highestNumericId then
        warn(
            "next_id",
            "nextId="
                .. tostring(nextId)
                .. " is not above highest contract ID="
                .. tostring(highestNumericId)
        )
    end

    print(
        "[QPSC] Save diagnostics: schema="
            .. tostring(data.schemaVersion or "missing")
            .. " migration="
            .. (migrationApplied and "applied" or "none")
            .. " contracts="
            .. tostring(contractCount)
            .. " participants="
            .. tostring(participantCount)
            .. " objectives="
            .. tostring(objectiveCount)
            .. " normalRewardsGranted="
            .. tostring(normalRewardsGranted)
            .. " firstBonusesGranted="
            .. tostring(firstBonusesGranted)
            .. " pendingRewards="
            .. tostring(pendingRewards)
            .. " warnings="
            .. tostring(#warnings)
    )

    local visibleWarnings = math.min(#warnings, 12)

    for index = 1, visibleWarnings do
        print(
            "[QPSC] Save diagnostic warning "
                .. tostring(index)
                .. ": "
                .. tostring(warnings[index])
        )
    end

    if #warnings > visibleWarnings then
        print(
            "[QPSC] Save diagnostics: "
                .. tostring(#warnings - visibleWarnings)
                .. " additional warning(s) suppressed."
        )
    end
end

-- QPSC_ONE_ACTIVE_CONTRACT_V1
local function QPSC_findActiveContractForUsername(data, username)
    local normalizedUsername =
        QPSC_normalizeUsername(username)

    for _, contract in ipairs(data.contracts or {}) do
        for _, participant in ipairs(
            contract.participants or {}
        ) do
            if tostring(participant.status or "") == "Accepted"
                and QPSC_normalizeUsername(
                    participant.username
                ) == normalizedUsername then
                return contract, participant
            end
        end
    end

    return nil, nil
end

local function QPSC_contractLabel(contract)
    if not contract then
        return "Unknown contract"
    end

    return "#"
        .. tostring(contract.id or "?")
        .. " - "
        .. tostring(contract.title or "Untitled Contract")
end

-- QPSC_STRICT_ADMIN_PERMISSION_V2
local function QPSC_isPrivileged(player)
    if player == nil then return false end

    if player.isAccessLevel then
        local ok, result = pcall(function()
            return player:isAccessLevel("admin")
        end)

        if ok and result == true then
            return true
        end
    end

    if player.getAccessLevel then
        local ok, access = pcall(function()
            return player:getAccessLevel()
        end)

        if ok and access ~= nil then
            return string.lower(tostring(access)) == "admin"
        end
    end

    return false
end

local function QPSC_transmit()
    if ModData and ModData.transmit then
        ModData.transmit(DATA_KEY)
    end
end

-- QPSC_SERVER_MESSAGES_V2
local function QPSC_sendMessage(
    player,
    text,
    key,
    arg1,
    arg2,
    arg3
)
    if player ~= nil then
        sendServerCommand(player, MODULE, "Message", {
            text = tostring(text or ""),
            key = tostring(key or ""),
            arg1 = arg1,
            arg2 = arg2,
            arg3 = arg3
        })
    end
end

local function QPSC_buildParticipantPayload(participant)
    return {
        username = tostring(participant.username or ""),
        status = tostring(
            participant.status or "Accepted"
        ),
        acceptedAt =
            tonumber(participant.acceptedAt) or 0,
        remainingHours =
            tonumber(participant.remainingHours) or 0,
        completedAt =
            tonumber(participant.completedAt) or 0,
        expiredAt =
            tonumber(participant.expiredAt) or 0,
        cancelledAt =
            tonumber(participant.cancelledAt) or 0,
        closedAt =
            tonumber(participant.closedAt) or 0,
        closedBy =
            tostring(participant.closedBy or ""),
        reviewedAt =
            tonumber(participant.reviewedAt) or 0,
        reviewedBy =
            tostring(participant.reviewedBy or ""),
        progress =
            tonumber(participant.progress) or 0,
        rewardGranted =
            participant.rewardGranted == true,
        rewardGrantedCount =
            tonumber(participant.rewardGrantedCount) or 0,
        rewardGrantedCounts =
            type(participant.rewardGrantedCounts) == "table"
            and participant.rewardGrantedCounts
            or nil,
        rewardGrantedAt =
            tonumber(participant.rewardGrantedAt) or 0,
        rewardPending =
            participant.rewardPending == true,
        firstFinisherBonusGranted =
            participant.firstFinisherBonusGranted == true,
        firstFinisherBonusGrantedCount =
            tonumber(
                participant.firstFinisherBonusGrantedCount
            ) or 0,
        firstFinisherBonusGrantedAt =
            tonumber(
                participant.firstFinisherBonusGrantedAt
            ) or 0,
        firstFinisherBonusPending =
            participant.firstFinisherBonusPending == true,
        objectiveProgress = type(participant.objectiveProgress) == "table"
            and participant.objectiveProgress or nil,
        objectiveContributions = type(participant.objectiveContributions) == "table"
            and participant.objectiveContributions or nil
    }
end

-- QPSC_NETWORK_SAFE_SHARED_SYNC_V2
local function QPSC_buildContractsPayload()
    local data = QPSC_getData()
    QPSC_migrateData(data)

    local contracts = {}

    for _, contract in ipairs(data.contracts or {}) do
        local participants = {}

        for _, participant in ipairs(
            contract.participants or {}
        ) do
            table.insert(
                participants,
                QPSC_buildParticipantPayload(participant)
            )
        end

        table.insert(contracts, {
            id = contract.id,
            category =
                QPSC_normalizeCategory(contract.category),
            difficulty =
                QPSC_normalizeDifficulty(contract.difficulty),
            completionMode =
                QPSC_normalizeCompletionMode(contract.completionMode),
            multiObjective = QPSC_isMultiObjective(contract),
            objectives = QPSC_isMultiObjective(contract)
                and contract.objectives or nil,
            sharedObjectiveProgress = QPSC_isMultiObjective(contract)
                and QPSC_isSharedTeamCompletion(contract)
                and contract.sharedObjectiveProgress or nil,
            globalCompleted = contract.globalCompleted == true,
            globalCompletedBy = tostring(
                contract.globalCompletedBy or ""
            ),
            globalCompletedAt = tonumber(
                contract.globalCompletedAt
            ) or 0,
            globalCompletionSource = tostring(
                contract.globalCompletionSource or ""
            ),

            sharedProgress = QPSC_isSharedTeamCompletion(contract)
                and (tonumber(contract.sharedProgress) or 0)
                or nil,
            sharedCompleted = QPSC_isSharedTeamCompletion(contract)
                and contract.sharedCompleted == true
                or nil,
            sharedCompletedBy = QPSC_isSharedTeamCompletion(contract)
                and tostring(contract.sharedCompletedBy or "")
                or nil,
            sharedCompletedAt = QPSC_isSharedTeamCompletion(contract)
                and (tonumber(contract.sharedCompletedAt) or 0)
                or nil,
            sharedCompletionSource = QPSC_isSharedTeamCompletion(contract)
                and tostring(contract.sharedCompletionSource or "")
                or nil,
            title = tostring(contract.title or ""),
            description =
                tostring(contract.description or ""),
            location = tostring(contract.location or ""),
            reward = tostring(contract.reward or ""),
            objectiveType =
                QPSC_normalizeObjectiveType(
                    contract.objectiveType
                ),
            objectiveTarget =
                tonumber(contract.objectiveTarget) or 0,
            objectiveRadius =
                tonumber(contract.objectiveRadius) or 0,
            objectiveItemFullType =
                tostring(
                    contract.objectiveItemFullType or ""
                ),
            objectiveItemDisplayName =
                tostring(
                    contract.objectiveItemDisplayName or ""
                ),
            targetX = tonumber(contract.targetX) or 0,
            targetY = tonumber(contract.targetY) or 0,
            targetZ = tonumber(contract.targetZ) or 0,
            rewardItemFullType =
                tostring(contract.rewardItemFullType or ""),
            rewardItemDisplayName =
                tostring(contract.rewardItemDisplayName or ""),
            rewardQuantity =
                tonumber(contract.rewardQuantity) or 0,
            rewardItems =
                QPSC_getContractRewardItems(contract),
            reputationPath =
                QPSC_normalizeReputationPath(
                    contract.reputationPath
                ),
            reputationPoints =
                QPSC_normalizePositiveInteger(
                    contract.reputationPoints,
                    100000
                ),
            secondaryReputationPath =
                QPSC_normalizeReputationPath(
                    contract.secondaryReputationPath
                ),
            secondaryReputationPoints =
                QPSC_normalizePositiveInteger(
                    contract.secondaryReputationPoints,
                    100000
                ),
            firstFinisherBonusItemFullType =
                tostring(
                    contract.firstFinisherBonusItemFullType
                    or ""
                ),
            firstFinisherBonusItemDisplayName =
                tostring(
                    contract.firstFinisherBonusItemDisplayName
                    or ""
                ),
            firstFinisherBonusQuantity =
                tonumber(
                    contract.firstFinisherBonusQuantity
                ) or 0,
            firstFinisherWinner =
                tostring(contract.firstFinisherWinner or ""),
            firstFinisherWonAt =
                tonumber(contract.firstFinisherWonAt) or 0,
            status = tostring(contract.status or "Open"),
            postedBy = tostring(contract.postedBy or ""),
            acceptedBy =
                tostring(contract.acceptedBy or ""),
            completedBy =
                tostring(contract.completedBy or ""),
            timeLimitHours =
                tonumber(contract.timeLimitHours) or 0,
            acceptedAt =
                tonumber(contract.acceptedAt) or 0,
            expiresAt =
                tonumber(contract.expiresAt) or 0,
            expiredAt =
                tonumber(contract.expiredAt) or 0,
            closed = contract.closed == true,
            participants = participants
        })
    end

    return contracts
end

local function QPSC_sendContracts(player)
    if player == nil then return end

    sendServerCommand(player, MODULE, "ContractsData", {
        contracts = QPSC_buildContractsPayload(),
        adminAuthorized = QPSC_isPrivileged(player)
    })
end

local function QPSC_broadcastContracts()
    local players = getOnlinePlayers()
    if players == nil then return end

    for index = 0, players:size() - 1 do
        local onlinePlayer = players:get(index)

        if onlinePlayer ~= nil then
            QPSC_sendContracts(onlinePlayer)
        end
    end
end

-- QPSC_BROADCAST_CONTRACT_ANNOUNCEMENT_V1
local function QPSC_broadcastAnnouncement(
    text,
    key,
    arg1,
    arg2
)
    local players = getOnlinePlayers()
    if players == nil then return end

    for index = 0, players:size() - 1 do
        local onlinePlayer = players:get(index)

        if onlinePlayer ~= nil then
            sendServerCommand(
                onlinePlayer,
                MODULE,
                "Announcement",
                {
                    text = tostring(text or ""),
                    key = tostring(key or ""),
                    arg1 = arg1,
                    arg2 = arg2
                }
            )
        end
    end
end


-- QPSC_TRACKED_OBJECTIVE_RUNTIME_V1
local function QPSC_findOnlinePlayer(username)
    local players = getOnlinePlayers()
    if players == nil then return nil end

    for index = 0, players:size() - 1 do
        local candidate = players:get(index)

        if candidate ~= nil and QPSC_sameUsername(
            QPSC_getUsername(candidate),
            username
        ) then
            return candidate
        end
    end

    return nil
end

local function QPSC_getPlayerSquare(player)
    if player == nil then return nil end

    local ok, square = pcall(function()
        return player:getSquare()
    end)

    if ok then return square end
    return nil
end

local function QPSC_getSquareAt(x, y, z)
    local cell = getCell and getCell() or nil
    if cell == nil then return nil end

    local ok, square = pcall(function()
        return cell:getGridSquare(
            math.floor(tonumber(x) or 0),
            math.floor(tonumber(y) or 0),
            math.floor(tonumber(z) or 0)
        )
    end)

    if ok then return square end
    return nil
end

-- QPSC_V053_SOLO_REWARD_POSITION_SYNC_V1
-- The single-player command bridge can expose a stale server-side player
-- square. The client reports its current coordinates so Zombie Hunt rewards
-- are placed beside the survivor who actually completed the contract.
local QPSC_reportedRewardPositions = {}
local QPSC_rewardPositionRuntimeTick = 0

local function QPSC_advanceRewardPositionTick()
    QPSC_rewardPositionRuntimeTick =
        QPSC_rewardPositionRuntimeTick + 1
end

local function QPSC_rewardPositionKey(username)
    return QPSC_normalizeUsername(username or "")
end

local function QPSC_storeReportedRewardPosition(player, args)
    if player == nil then return false end
    args = args or {}

    local x = tonumber(args.x)
    local y = tonumber(args.y)
    local z = tonumber(args.z)

    if x == nil or y == nil or z == nil then
        return false
    end

    if x ~= x or y ~= y or z ~= z
        or math.abs(x) > 1000000
        or math.abs(y) > 1000000
        or z < -1 or z > 32 then
        return false
    end

    QPSC_reportedRewardPositions[
        QPSC_rewardPositionKey(QPSC_getUsername(player))
    ] = {
        x = x,
        y = y,
        z = z,
        contractId = tostring(args.contractId or ""),
        expectedProgress =
            tonumber(args.expectedProgress) or 0,
        receivedAt = QPSC_getWorldAgeHours(),
        receivedTick = QPSC_rewardPositionRuntimeTick
    }

    return true
end

local function QPSC_getReportedRewardSquare(
    username,
    contract,
    allowGeneric
)
    local key = QPSC_rewardPositionKey(username)
    local report = QPSC_reportedRewardPositions[key]

    if report == nil then return nil end

    local now = QPSC_getWorldAgeHours()
    local receivedAt = tonumber(report.receivedAt) or 0

    if receivedAt <= 0
        or now < receivedAt - 0.01
        or now - receivedAt > 0.25 then
        return nil
    end

    local reportContractId =
        tostring(report.contractId or "")
    local contractId =
        tostring(contract and contract.id or "")
    local receivedTick =
        tonumber(report.receivedTick) or 0
    local tickIsFresh =
        QPSC_rewardPositionRuntimeTick <= 0
        or receivedTick >=
            QPSC_rewardPositionRuntimeTick - 15

    local matchesCompletion =
        reportContractId ~= ""
        and reportContractId == contractId
        and tickIsFresh

    if not matchesCompletion
        and not (allowGeneric == true
            and reportContractId == "") then
        return nil
    end

    return QPSC_getSquareAt(
        report.x,
        report.y,
        report.z
    )
end

-- QPSC_V053_PLAYER_REWARD_DROP_AND_LOCK_V1
local QPSC_rewardLocks = {}

local function QPSC_rewardLockKey(contract, participant)
    return tostring(contract and contract.id or "")
        .. "|"
        .. QPSC_normalizeUsername(
            participant and participant.username or ""
        )
end

local function QPSC_spawnRewardItems(
    contract,
    participant,
    player,
    square
)
    if participant.rewardGranted == true then
        return true
    end

    local lockKey = QPSC_rewardLockKey(contract, participant)

    if QPSC_rewardLocks[lockKey] == true then
        return false
    end

    QPSC_rewardLocks[lockKey] = true

    local rewards =
        QPSC_getContractRewardItems(contract)

    if #rewards == 0 then
        participant.rewardGranted = true
        participant.rewardGrantedCount = 0
        participant.rewardGrantedCounts = {}
        participant.rewardGrantedAt =
            QPSC_getWorldAgeHours()
        participant.rewardPending = false
        QPSC_rewardLocks[lockKey] = nil
        return true
    end

    local counts =
        QPSC_normalizeParticipantRewardCounts(
            contract,
            participant
        )
    local rewardSquare =
        square or QPSC_getPlayerSquare(player)

    participant.rewardPending = true

    for rewardIndex, reward in ipairs(rewards) do
        local fullType = tostring(reward.fullType or "")
        local quantity =
            QPSC_normalizePositiveInteger(
                reward.quantity,
                100
            )

        if fullType == ""
            or quantity < 1
            or QPSC_getScriptItem(fullType) == nil then
            participant.rewardPending = true
            QPSC_rewardLocks[lockKey] = nil
            return false
        end

        local granted = math.min(
            quantity,
            QPSC_normalizePositiveInteger(
                counts[rewardIndex],
                quantity
            )
        )

        if rewardSquare ~= nil
            and rewardSquare.AddWorldInventoryItem ~= nil then
            for itemIndex = granted + 1, quantity do
                local offsetIndex =
                    (
                        ((rewardIndex - 1) * 7)
                        + (itemIndex - 1)
                    ) % 9
                local offset = offsetIndex * 0.045

                local ok = pcall(function()
                    rewardSquare:AddWorldInventoryItem(
                        fullType,
                        0.28 + offset,
                        0.28 + offset,
                        0
                    )
                end)

                if ok then
                    granted = granted + 1
                    counts[rewardIndex] = granted

                    if rewardIndex == 1 then
                        participant.rewardGrantedCount =
                            granted
                    end
                else
                    break
                end
            end
        end

        if granted < quantity then
            participant.rewardGrantedCounts = counts
            participant.rewardPending = true
            QPSC_rewardLocks[lockKey] = nil
            return false
        end
    end

    participant.rewardGranted = true
    participant.rewardGrantedCounts = counts
    participant.rewardGrantedCount =
        tonumber(counts[1]) or 0
    participant.rewardGrantedAt =
        QPSC_getWorldAgeHours()
    participant.rewardPending = false

    if #rewards == 1 then
        local reward = rewards[1]
        QPSC_sendMessage(
            player,
            "Reward spawned near you: "
                .. tostring(reward.quantity)
                .. " x "
                .. QPSC_getScriptDisplayName(
                    reward.fullType,
                    reward.displayName
                ),
            "UI_QPSC_MessageRewardSpawned",
            reward.quantity,
            QPSC_getScriptDisplayName(
                reward.fullType,
                reward.displayName
            )
        )
    else
        QPSC_sendMessage(
            player,
            "Contract rewards spawned near you.",
            "UI_QPSC_MessageRewardsSpawned",
            #rewards
        )
    end

    QPSC_rewardLocks[lockKey] = nil
    return true
end


local function QPSC_firstFinisherBonusLockKey(
    contract,
    participant
)
    return "bonus|"
        .. tostring(contract and contract.id or "")
        .. "|"
        .. QPSC_normalizeUsername(
            participant and participant.username or ""
        )
end

local QPSC_firstFinisherBonusLocks = QPSC_firstFinisherBonusLocks or {}

local function QPSC_spawnFirstFinisherBonus(
    contract,
    participant,
    player,
    square
)
    -- QPSC_V130_R2_FRESH_FIRST_FINISHER_DELIVERY_FIX_V1
    if contract == nil or participant == nil then
        print("[QPSC] First Finisher R2 skipped: missing contract or participant")
        return false
    end

    if participant.firstFinisherBonusGranted == true then
        return true
    end

    if not QPSC_sameUsername(
        contract.firstFinisherWinner,
        participant.username
    ) then
        participant.firstFinisherBonusPending = false
        return true
    end

    local lockKey = QPSC_firstFinisherBonusLockKey(
        contract,
        participant
    )

    if QPSC_firstFinisherBonusLocks[lockKey] == true then
        print(
            "[QPSC] First Finisher R2 locked for contract #"
                .. tostring(contract.id or "?")
                .. " | participant="
                .. tostring(participant.username or "")
        )
        return false
    end

    QPSC_firstFinisherBonusLocks[lockKey] = true

    local fullType = tostring(
        contract.firstFinisherBonusItemFullType or ""
    )
    local displayName = QPSC_getScriptDisplayName(
        fullType,
        contract.firstFinisherBonusItemDisplayName
    )
    local quantity = QPSC_normalizePositiveInteger(
        contract.firstFinisherBonusQuantity,
        100
    )

    print(
        "[QPSC] First Finisher R2 delivery start | contract #"
            .. tostring(contract.id or "?")
            .. " | winner="
            .. tostring(contract.firstFinisherWinner or "")
            .. " | participant="
            .. tostring(participant.username or "")
            .. " | item="
            .. tostring(fullType)
            .. " | qty="
            .. tostring(quantity)
    )

    if fullType == "" or quantity < 1 then
        participant.firstFinisherBonusGranted = true
        participant.firstFinisherBonusGrantedCount = 0
        participant.firstFinisherBonusGrantedAt =
            QPSC_getWorldAgeHours()
        participant.firstFinisherBonusPending = false
        QPSC_firstFinisherBonusLocks[lockKey] = nil
        print(
            "[QPSC] First Finisher R2 no item configured; marked resolved for contract #"
                .. tostring(contract.id or "?")
        )
        return true
    end

    if QPSC_getScriptItem(fullType) == nil then
        participant.firstFinisherBonusPending = true
        QPSC_firstFinisherBonusLocks[lockKey] = nil
        print(
            "[QPSC] First Finisher R2 failed: invalid item "
                .. tostring(fullType)
                .. " | contract #"
                .. tostring(contract.id or "?")
        )
        return false
    end

    local granted = math.min(
        quantity,
        QPSC_normalizePositiveInteger(
            participant.firstFinisherBonusGrantedCount,
            quantity
        )
    )

    participant.firstFinisherBonusPending = true

    local bonusSquare = square or QPSC_getPlayerSquare(player)
    local groundGranted = 0

    if bonusSquare ~= nil
        and bonusSquare.AddWorldInventoryItem ~= nil then
        for index = granted + 1, quantity do
            local offset = ((index - 1) % 9) * 0.045
            local ok = pcall(function()
                bonusSquare:AddWorldInventoryItem(
                    fullType,
                    0.56 + offset,
                    0.32 + offset,
                    0
                )
            end)

            if ok then
                granted = granted + 1
                groundGranted = groundGranted + 1
                participant.firstFinisherBonusGrantedCount =
                    granted
            else
                print(
                    "[QPSC] First Finisher R2 ground spawn failed after "
                        .. tostring(groundGranted)
                        .. " item(s) | contract #"
                        .. tostring(contract.id or "?")
                )
                break
            end
        end
    else
        print(
            "[QPSC] First Finisher R2 no valid square; trying inventory fallback | contract #"
                .. tostring(contract.id or "?")
        )
    end

    local inventoryGranted = 0

    if granted < quantity
        and player ~= nil
        and player.getInventory ~= nil then
        local inventory = nil
        local okInventory, resultInventory = pcall(function()
            return player:getInventory()
        end)

        if okInventory then
            inventory = resultInventory
        end

        if inventory ~= nil and inventory.AddItem ~= nil then
            for index = granted + 1, quantity do
                local okAdd = pcall(function()
                    inventory:AddItem(fullType)
                end)

                if okAdd then
                    granted = granted + 1
                    inventoryGranted = inventoryGranted + 1
                    participant.firstFinisherBonusGrantedCount =
                        granted
                else
                    print(
                        "[QPSC] First Finisher R2 inventory fallback failed after "
                            .. tostring(inventoryGranted)
                            .. " item(s) | contract #"
                            .. tostring(contract.id or "?")
                    )
                    break
                end
            end
        else
            print(
                "[QPSC] First Finisher R2 inventory fallback unavailable | contract #"
                    .. tostring(contract.id or "?")
            )
        end
    end

    if granted >= quantity then
        participant.firstFinisherBonusGranted = true
        participant.firstFinisherBonusGrantedCount = quantity
        participant.firstFinisherBonusGrantedAt =
            QPSC_getWorldAgeHours()
        participant.firstFinisherBonusPending = false

        local deliveryText = "spawned near you"
        local translationKey = "UI_QPSC_MessageFirstFinisherBonusSpawned"

        if groundGranted <= 0 and inventoryGranted > 0 then
            deliveryText = "delivered to your inventory"
            translationKey = "UI_QPSC_MessageFirstFinisherBonusDelivered"
        elseif groundGranted > 0 and inventoryGranted > 0 then
            deliveryText = "spawned near you and delivered to your inventory"
        end

        QPSC_sendMessage(
            player,
            "First finisher bonus "
                .. deliveryText
                .. ": "
                .. tostring(quantity)
                .. " x "
                .. displayName,
            translationKey,
            quantity,
            displayName
        )

        print(
            "[QPSC] First Finisher R2 delivered | contract #"
                .. tostring(contract.id or "?")
                .. " | item="
                .. tostring(fullType)
                .. " | qty="
                .. tostring(quantity)
                .. " | ground="
                .. tostring(groundGranted)
                .. " | inventory="
                .. tostring(inventoryGranted)
        )

        QPSC_firstFinisherBonusLocks[lockKey] = nil
        return true
    end

    participant.firstFinisherBonusPending = true
    QPSC_firstFinisherBonusLocks[lockKey] = nil

    print(
        "[QPSC] First Finisher R2 still pending | contract #"
            .. tostring(contract.id or "?")
            .. " | item="
            .. tostring(fullType)
            .. " | granted="
            .. tostring(granted)
            .. "/"
            .. tostring(quantity)
    )

    return false
end

local function QPSC_trySpawnFirstFinisherBonusForParticipant(
    contract,
    participant,
    player,
    square,
    source
)
    -- QPSC_V130_R2_FRESH_FIRST_FINISHER_DELIVERY_FIX_V1
    if contract == nil or participant == nil then return false end

    local fullType = tostring(
        contract.firstFinisherBonusItemFullType or ""
    )
    local quantity = QPSC_normalizePositiveInteger(
        contract.firstFinisherBonusQuantity,
        100
    )

    if fullType == "" or quantity < 1 then return false end

    if tostring(contract.firstFinisherWinner or "") == "" then
        contract.firstFinisherWinner = tostring(participant.username or "")
        contract.firstFinisherWonAt = QPSC_getWorldAgeHours()
    end

    if not QPSC_sameUsername(
        contract.firstFinisherWinner,
        participant.username
    ) then
        return false
    end

    if participant.firstFinisherBonusGranted == true then
        return true
    end

    local targetPlayer = player
    if targetPlayer == nil then
        targetPlayer = QPSC_findOnlinePlayer(participant.username)
    end

    if targetPlayer == nil then
        participant.firstFinisherBonusPending = true
        print(
            "[QPSC] First Finisher R2 pending: winner offline | contract #"
                .. tostring(contract.id or "?")
                .. " | source="
                .. tostring(source or "")
        )
        return false
    end

    local targetSquare = square or QPSC_getPlayerSquare(targetPlayer)

    return QPSC_spawnFirstFinisherBonus(
        contract,
        participant,
        targetPlayer,
        targetSquare
    )
end


-- QPSC_SHARED_TEAM_COMPLETION_V090
local QPSC_sharedTeamLocks = {}

local function QPSC_sharedTeamLockKey(contract)
    return "SHARED_TEAM|" .. tostring(contract and contract.id or "")
end

local function QPSC_closeSharedTeamContract(
    contract,
    finalParticipant,
    finalPlayer,
    finalRewardSquare,
    completionSource
)
    if contract == nil
        or finalParticipant == nil
        or not QPSC_isSharedTeamCompletion(contract)
        or QPSC_normalizeObjectiveType(contract.objectiveType) ~= "KILL" then
        return false
    end

    if contract.sharedCompleted == true then
        return false
    end

    local target = math.max(
        1,
        tonumber(contract.objectiveTarget) or 1
    )

    if (tonumber(contract.sharedProgress) or 0) < target then
        return false
    end

    local lockKey = QPSC_sharedTeamLockKey(contract)

    if QPSC_sharedTeamLocks[lockKey] == true then
        return false
    end

    QPSC_sharedTeamLocks[lockKey] = true

    local now = QPSC_getWorldAgeHours()
    local finalUsername = tostring(
        finalParticipant.username or ""
    )
    local source = tostring(
        completionSource or "Zombie Hunt"
    )
    local eligibleCount = 0
    local hasItemReward =
        tostring(contract.rewardItemFullType or "") ~= ""
        and QPSC_normalizePositiveInteger(
            contract.rewardQuantity,
            100
        ) > 0

    contract.sharedProgress = target
    contract.sharedCompleted = true
    contract.sharedCompletedBy = finalUsername
    contract.sharedCompletedAt = now
    contract.sharedCompletionSource = source
    contract.closed = true
    contract.legacyClosedStatus = "Completed"

    for _, participant in ipairs(contract.participants or {}) do
        if tostring(participant.status or "") == "Accepted" then
            QPSC_timerMarks[
                QPSC_timerKey(
                    contract.id,
                    participant.username
                )
            ] = nil

            participant.firstFinisherBonusPending = false

            if (tonumber(participant.progress) or 0) > 0 then
                eligibleCount = eligibleCount + 1
                participant.status = "Completed"
                participant.completedAt = now
                participant.reviewedAt = now
                participant.reviewedBy = source
                participant.closedAt = 0
                participant.closedBy = ""

                QPSC_awardContractReputation(
                    contract,
                    participant,
                    source
                )

                local participantPlayer = QPSC_findOnlinePlayer(
                    participant.username
                )

                if not hasItemReward then
                    QPSC_spawnRewardItems(
                        contract,
                        participant,
                        participantPlayer,
                        nil
                    )
                elseif participantPlayer ~= nil then
                    local rewardSquare = nil

                    if QPSC_sameUsername(
                        participant.username,
                        finalUsername
                    ) then
                        rewardSquare =
                            QPSC_getReportedRewardSquare(
                                participant.username,
                                contract,
                                false
                            )
                            or finalRewardSquare
                    end

                    if rewardSquare == nil then
                        rewardSquare = QPSC_getPlayerSquare(
                            participantPlayer
                        )
                    end

                    if rewardSquare ~= nil then
                        QPSC_spawnRewardItems(
                            contract,
                            participant,
                            participantPlayer,
                            rewardSquare
                        )
                    else
                        participant.rewardPending = true
                    end
                else
                    participant.rewardPending = true
                end

                if participantPlayer ~= nil then
                    QPSC_sendMessage(
                        participantPlayer,
                        "Shared Team contract #"
                            .. tostring(contract.id)
                            .. " completed. Your contribution: "
                            .. tostring(participant.progress)
                            .. ".",
                        "UI_QPSC_MessageSharedTeamCompleted",
                        contract.id,
                        participant.progress
                    )
                end
            else
                participant.status = "ClosedByOther"
                participant.closedAt = now
                participant.closedBy = "Shared Team"
                participant.rewardPending = false

                local participantPlayer = QPSC_findOnlinePlayer(
                    participant.username
                )

                if participantPlayer ~= nil then
                    QPSC_sendMessage(
                        participantPlayer,
                        "Shared Team contract #"
                            .. tostring(contract.id)
                            .. " completed. A contribution was required for the reward.",
                        "UI_QPSC_MessageSharedTeamNoContribution",
                        contract.id
                    )
                end
            end
        end
    end

    QPSC_recomputeContractStatus(contract)
    QPSC_notifyOptionalCompletion(contract)
    QPSC_transmit()
    QPSC_broadcastContracts()
    QPSC_broadcastAnnouncement(
        "Shared Team contract #"
            .. tostring(contract.id)
            .. " completed. "
            .. tostring(eligibleCount)
            .. " eligible contributors rewarded.",
        "UI_QPSC_AnnouncementSharedTeamCompleted",
        contract.id,
        eligibleCount
    )

    print(
        "[QPSC] Shared Team contract #"
            .. tostring(contract.id)
            .. " completed by team contribution; final kill by "
            .. finalUsername
            .. "; eligible contributors="
            .. tostring(eligibleCount)
    )

    QPSC_sharedTeamLocks[lockKey] = nil
    return true
end

-- QPSC_GLOBAL_CONTRACT_COMPLETION_V080
local function QPSC_closeGlobalContract(
    contract,
    winnerParticipant,
    completionSource
)
    if contract == nil
        or winnerParticipant == nil
        or not QPSC_isGlobalCompletion(contract) then
        return false
    end

    if contract.globalCompleted == true then
        return false
    end

    local now = QPSC_getWorldAgeHours()
    local winner = tostring(winnerParticipant.username or "")

    contract.globalCompleted = true
    contract.globalCompletedBy = winner
    contract.globalCompletedAt = now
    contract.globalCompletionSource = tostring(
        completionSource or "Automatic"
    )
    contract.closed = true
    contract.legacyClosedStatus = "Completed"

    for _, participant in ipairs(contract.participants or {}) do
        if not QPSC_sameUsername(participant.username, winner)
            and tostring(participant.status or "") == "Accepted" then
            participant.status = "ClosedByOther"
            participant.closedAt = now
            participant.closedBy = winner
            participant.rewardPending = false
            participant.firstFinisherBonusPending = false

            QPSC_timerMarks[
                QPSC_timerKey(contract.id, participant.username)
            ] = nil

            local participantPlayer = QPSC_findOnlinePlayer(
                participant.username
            )

            if participantPlayer ~= nil then
                QPSC_sendMessage(
                    participantPlayer,
                    "Contract #"
                        .. tostring(contract.id)
                        .. " was completed by "
                        .. winner
                        .. ". This objective is no longer available.",
                    "UI_QPSC_MessageGlobalContractClosed",
                    contract.id,
                    winner
                )
            end
        end
    end

    return true
end

-- QPSC_V053_COMPLETION_LOCK_V1
local QPSC_completionLocks = {}

local function QPSC_completionLockKey(contract, participant)
    return tostring(contract and contract.id or "")
        .. "|"
        .. QPSC_normalizeUsername(
            participant and participant.username or ""
        )
end

local function QPSC_completeTrackedParticipant(
    contract,
    participant,
    player,
    rewardSquare,
    completionSource
)
    if contract == nil or participant == nil then
        return false
    end

    if tostring(participant.status or "") ~= "Accepted" then
        return false
    end

    if QPSC_isGlobalCompletion(contract)
        and contract.globalCompleted == true then
        return false
    end

    local lockKey = QPSC_isGlobalCompletion(contract)
        and ("GLOBAL|" .. tostring(contract.id or ""))
        or QPSC_completionLockKey(contract, participant)

    if QPSC_completionLocks[lockKey] == true then
        return false
    end

    QPSC_completionLocks[lockKey] = true

    local now = QPSC_getWorldAgeHours()

    participant.status = "Completed"
    participant.completedAt = now
    participant.reviewedAt = now
    participant.reviewedBy =
        tostring(completionSource or "Automatic")
    participant.progress = math.max(
        tonumber(participant.progress) or 0,
        tonumber(contract.objectiveTarget) or 0
    )

    QPSC_awardContractReputation(
        contract,
        participant,
        completionSource
    )

    local hasFirstFinisherBonus =
        tostring(
            contract.firstFinisherBonusItemFullType or ""
        ) ~= ""
        and QPSC_normalizePositiveInteger(
            contract.firstFinisherBonusQuantity,
            100
        ) > 0
    local isFirstFinisher = false

    if hasFirstFinisherBonus then
        if tostring(contract.firstFinisherWinner or "") == "" then
            contract.firstFinisherWinner =
                tostring(participant.username or "")
            contract.firstFinisherWonAt = now
            isFirstFinisher = true
        elseif QPSC_sameUsername(
            contract.firstFinisherWinner,
            participant.username
        ) then
            isFirstFinisher = true
        end
    end

    QPSC_timerMarks[
        QPSC_timerKey(
            contract.id,
            participant.username
        )
    ] = nil

    QPSC_closeGlobalContract(
        contract,
        participant,
        completionSource
    )

    local hasItemReward =
        tostring(contract.rewardItemFullType or "") ~= ""
        and QPSC_normalizePositiveInteger(
            contract.rewardQuantity,
            100
        ) > 0

    if tostring(completionSource or "") == "Zombie Hunt"
        and hasItemReward then
        local reportedSquare =
            QPSC_getReportedRewardSquare(
                participant.username,
                contract,
                false
            )

        if reportedSquare ~= nil then
            QPSC_spawnRewardItems(
                contract,
                participant,
                player,
                reportedSquare
            )
        else
            participant.rewardPending = true
        end
    else
        QPSC_spawnRewardItems(
            contract,
            participant,
            player,
            rewardSquare or QPSC_getPlayerSquare(player)
        )
    end

    if isFirstFinisher then
        if tostring(completionSource or "") == "Zombie Hunt" then
            local reportedBonusSquare =
                QPSC_getReportedRewardSquare(
                    participant.username,
                    contract,
                    false
                )

            if reportedBonusSquare ~= nil then
                QPSC_trySpawnFirstFinisherBonusForParticipant(
                    contract,
                    participant,
                    player,
                    reportedBonusSquare,
                    completionSource
                )
            else
                participant.firstFinisherBonusPending = true
            end
        else
            QPSC_trySpawnFirstFinisherBonusForParticipant(
                contract,
                participant,
                player,
                rewardSquare or QPSC_getPlayerSquare(player),
                completionSource
            )
        end
    end

    if hasFirstFinisherBonus
        and participant.firstFinisherBonusGranted ~= true
        and QPSC_sameUsername(
            contract.firstFinisherWinner,
            participant.username
        ) then
        QPSC_trySpawnFirstFinisherBonusForParticipant(
            contract,
            participant,
            player,
            rewardSquare or QPSC_getPlayerSquare(player),
            completionSource
        )
    end

    QPSC_recomputeContractStatus(contract)
    QPSC_notifyOptionalCompletion(contract)
    QPSC_transmit()
    QPSC_broadcastContracts()

    QPSC_sendMessage(
        player,
        "Contract completed: #"
            .. tostring(contract.id),
        "UI_QPSC_MessageTrackedCompleted",
        contract.id
    )

    if QPSC_isGlobalCompletion(contract) then
        QPSC_broadcastAnnouncement(
            "Contract #"
                .. tostring(contract.id)
                .. " was completed by "
                .. tostring(participant.username)
                .. ".",
            "UI_QPSC_AnnouncementGlobalContractCompleted",
            contract.id,
            tostring(participant.username)
        )
    end

    print(
        "[QPSC] Tracked contract #"
            .. tostring(contract.id)
            .. " completed by "
            .. tostring(participant.username)
    )

    QPSC_completionLocks[lockKey] = nil
    return true
end



-- QPSC_MULTI_OBJECTIVE_RUNTIME_V100
local function QPSC_closeSharedMultiContract(
    contract,
    finalParticipant,
    finalPlayer,
    finalRewardSquare,
    completionSource
)
    if contract == nil
        or finalParticipant == nil
        or not QPSC_isMultiObjective(contract)
        or not QPSC_isSharedTeamCompletion(contract)
        or contract.sharedCompleted == true
        or not QPSC_allMultiObjectivesComplete(contract, finalParticipant) then
        return false
    end

    local lockKey = QPSC_sharedTeamLockKey(contract)
    if QPSC_sharedTeamLocks[lockKey] == true then return false end
    QPSC_sharedTeamLocks[lockKey] = true

    local now = QPSC_getWorldAgeHours()
    local finalUsername = tostring(finalParticipant.username or "")
    local source = tostring(completionSource or "Multi-Objective")
    local eligibleCount = 0
    local hasItemReward = tostring(contract.rewardItemFullType or "") ~= ""
        and QPSC_normalizePositiveInteger(contract.rewardQuantity, 100) > 0

    contract.sharedCompleted = true
    contract.sharedCompletedBy = finalUsername
    contract.sharedCompletedAt = now
    contract.sharedCompletionSource = source
    contract.closed = true
    contract.legacyClosedStatus = "Completed"
    contract.sharedProgress = #(contract.objectives or {})

    for _, participant in ipairs(contract.participants or {}) do
        if tostring(participant.status or "") == "Accepted" then
            QPSC_timerMarks[QPSC_timerKey(contract.id, participant.username)] = nil
            participant.firstFinisherBonusPending = false
            local contribution = QPSC_getMultiContributionTotal(participant)

            if contribution > 0 then
                eligibleCount = eligibleCount + 1
                participant.status = "Completed"
                participant.completedAt = now
                participant.reviewedAt = now
                participant.reviewedBy = source
                participant.closedAt = 0
                participant.closedBy = ""
                participant.progress = #(contract.objectives or {})

                QPSC_awardContractReputation(
                    contract,
                    participant,
                    source
                )

                local participantPlayer = QPSC_findOnlinePlayer(participant.username)

                if not hasItemReward then
                    QPSC_spawnRewardItems(contract, participant, participantPlayer, nil)
                elseif participantPlayer ~= nil then
                    local rewardSquare = nil
                    if QPSC_sameUsername(participant.username, finalUsername) then
                        rewardSquare = finalRewardSquare
                    end
                    rewardSquare = rewardSquare or QPSC_getPlayerSquare(participantPlayer)

                    if rewardSquare ~= nil then
                        QPSC_spawnRewardItems(contract, participant, participantPlayer, rewardSquare)
                    else
                        participant.rewardPending = true
                    end
                else
                    participant.rewardPending = true
                end

                if participantPlayer ~= nil then
                    QPSC_sendMessage(
                        participantPlayer,
                        "Shared Team multi-objective contract #" .. tostring(contract.id)
                            .. " completed. Your contribution: " .. tostring(contribution) .. ".",
                        "UI_QPSC_MessageSharedMultiCompleted",
                        contract.id,
                        contribution
                    )
                end
            else
                participant.status = "ClosedByOther"
                participant.closedAt = now
                participant.closedBy = "Shared Team"
                participant.rewardPending = false
                local participantPlayer = QPSC_findOnlinePlayer(participant.username)
                if participantPlayer ~= nil then
                    QPSC_sendMessage(
                        participantPlayer,
                        "Shared Team contract #" .. tostring(contract.id)
                            .. " completed. A contribution was required for the reward.",
                        "UI_QPSC_MessageSharedTeamNoContribution",
                        contract.id
                    )
                end
            end
        end
    end

    QPSC_recomputeContractStatus(contract)
    QPSC_notifyOptionalCompletion(contract)
    QPSC_transmit()
    QPSC_broadcastContracts()
    QPSC_broadcastAnnouncement(
        "Multi-objective Shared Team contract #" .. tostring(contract.id)
            .. " completed. " .. tostring(eligibleCount) .. " eligible contributors rewarded.",
        "UI_QPSC_AnnouncementSharedMultiCompleted",
        contract.id,
        eligibleCount
    )
    print("[QPSC] Shared Team multi-objective contract #" .. tostring(contract.id)
        .. " completed; eligible contributors=" .. tostring(eligibleCount))
    QPSC_sharedTeamLocks[lockKey] = nil
    return true
end

local function QPSC_advanceMultiObjective(
    contract,
    participant,
    objective,
    amount,
    player,
    rewardSquare,
    completionSource
)
    if not QPSC_isMultiObjective(contract)
        or participant == nil
        or objective == nil
        or tostring(participant.status or "") ~= "Accepted" then
        return false
    end

    if QPSC_isSharedTeamCompletion(contract) and contract.sharedCompleted == true then
        return false
    end
    if QPSC_isGlobalCompletion(contract) and contract.globalCompleted == true then
        return false
    end

    local target = math.max(1, tonumber(objective.target) or 1)
    local before = QPSC_getMultiProgress(contract, participant, objective)
    if before >= target then return false end
    local increment = math.max(0, math.floor(tonumber(amount) or 0))
    local after = QPSC_setMultiProgress(contract, participant, objective, before + increment)
    local credited = math.max(0, after - before)

    if credited > 0 then
        QPSC_addMultiContribution(participant, objective, credited)
    end

    local completed, total = QPSC_multiObjectiveCounts(contract, participant)
    participant.progress = completed
    if QPSC_isSharedTeamCompletion(contract) then contract.sharedProgress = completed end

    if completed >= total and total > 0 then
        if QPSC_isSharedTeamCompletion(contract) then
            return QPSC_closeSharedMultiContract(
                contract,
                participant,
                player,
                rewardSquare or QPSC_getPlayerSquare(player),
                completionSource
            )
        end

        return QPSC_completeTrackedParticipant(
            contract,
            participant,
            player,
            rewardSquare or QPSC_getPlayerSquare(player),
            completionSource or "Multi-Objective"
        )
    end

    QPSC_transmit()
    QPSC_broadcastContracts()
    QPSC_sendMessage(
        player,
        "Objective progress: " .. tostring(after) .. "/" .. tostring(target)
            .. " | Contract objectives: " .. tostring(completed) .. "/" .. tostring(total),
        "UI_QPSC_MessageMultiProgress",
        after,
        target,
        completed
    )
    return true
end

local function QPSC_tryGrantPendingRewards(player)
    if player == nil then return false end

    local data = QPSC_getData()
    local username = QPSC_getUsername(player)
    local changed = false

    for _, contract in ipairs(data.contracts or {}) do
        local participant =
            QPSC_findParticipant(contract, username)

        if participant ~= nil then
            local rewardSquare = nil

            if QPSC_normalizeObjectiveType(
                contract.objectiveType
            ) == "KILL"
                and tostring(
                    participant.reviewedBy or ""
                ) == "Zombie Hunt" then
                rewardSquare =
                    QPSC_getReportedRewardSquare(
                        participant.username,
                        contract,
                        true
                    )
            else
                rewardSquare = QPSC_getPlayerSquare(player)
            end

            if participant.rewardPending == true
                and participant.rewardGranted ~= true then
                local beforeProgress =
                    QPSC_rewardGrantedProgress(participant)
                local completed = false
                local rewards =
                    QPSC_getContractRewardItems(contract)

                if rewardSquare ~= nil
                    or #rewards == 0 then
                    completed = QPSC_spawnRewardItems(
                        contract,
                        participant,
                        player,
                        rewardSquare
                    )
                end

                local afterProgress =
                    QPSC_rewardGrantedProgress(participant)

                if completed
                    or afterProgress ~= beforeProgress then
                    changed = true
                end
            end

            if participant.firstFinisherBonusPending == true
                and participant.firstFinisherBonusGranted ~= true
                and QPSC_sameUsername(
                    contract.firstFinisherWinner,
                    participant.username
                ) then
                local beforeBonusCount = tonumber(
                    participant.firstFinisherBonusGrantedCount
                ) or 0
                local bonusCompleted = false

                if rewardSquare ~= nil
                    or tostring(
                        contract.firstFinisherBonusItemFullType
                        or ""
                    ) == "" then
                    bonusCompleted =
                        QPSC_spawnFirstFinisherBonus(
                            contract,
                            participant,
                            player,
                            rewardSquare
                        )
                end

                local afterBonusCount = tonumber(
                    participant.firstFinisherBonusGrantedCount
                ) or 0

                if bonusCompleted
                    or afterBonusCount ~= beforeBonusCount then
                    changed = true
                end
            end

            local primaryReputationPath =
                QPSC_normalizeReputationPath(
                    contract.reputationPath
                )
            local primaryReputationPoints =
                QPSC_normalizePositiveInteger(
                    contract.reputationPoints,
                    100000
                )
            local secondaryReputationPath =
                QPSC_normalizeReputationPath(
                    contract.secondaryReputationPath
                )
            local secondaryReputationPoints =
                QPSC_normalizePositiveInteger(
                    contract.secondaryReputationPoints,
                    100000
                )
            local hasConfiguredReputation =
                (
                    primaryReputationPath ~= ""
                    and primaryReputationPoints > 0
                )
                or (
                    secondaryReputationPath ~= ""
                    and secondaryReputationPoints > 0
                )

            -- Existing completed contracts created before R7 have no
            -- resolved marker. Replaying the same duplicate-safe source
            -- IDs either recovers a missing reward or returns duplicate.
            if tostring(participant.status or "") == "Completed"
                and hasConfiguredReputation
                and participant.reputationRewardResolved ~= true then
                local beforeResolved =
                    participant.reputationRewardResolved == true
                local beforePending =
                    participant.reputationRewardPending == true
                local beforeAttempts =
                    tonumber(
                        participant.reputationRewardAttempts
                    ) or 0

                QPSC_awardContractReputation(
                    contract,
                    participant,
                    "QPSurvivorContracts Recovery"
                )

                if beforeResolved
                        ~= (
                            participant.reputationRewardResolved
                            == true
                        )
                    or beforePending
                        ~= (
                            participant.reputationRewardPending
                            == true
                        )
                    or beforeAttempts
                        ~= (
                            tonumber(
                                participant.reputationRewardAttempts
                            ) or 0
                        ) then
                    changed = true
                end
            end
        end
    end

    if changed then
        QPSC_transmit()
        QPSC_broadcastContracts()
    end

    return changed
end

local function QPSC_isPlayerNearTarget(player, contract)
    if player == nil or contract == nil then return false end

    local dx = (tonumber(player:getX()) or 0)
        - (tonumber(contract.targetX) or 0)
    local dy = (tonumber(player:getY()) or 0)
        - (tonumber(contract.targetY) or 0)
    local dz = math.abs(
        (tonumber(player:getZ()) or 0)
        - (tonumber(contract.targetZ) or 0)
    )
    local radius = math.max(
        1,
        tonumber(contract.objectiveRadius) or 3
    )

    return dz < 0.5 and (dx * dx + dy * dy) <= radius * radius
end

-- QPSC_B41_INVENTORY_FULLTYPE_COMPAT_V1
local function QPSC_getInventoryItemsByFullType(
    inventory,
    fullType
)
    local result = {}
    local seen = {}
    local visited = {}
    local target = tostring(fullType or "")
    local normalizedTarget = string.lower(target)
    local shortTarget = normalizedTarget:match("([^%.]+)$")
        or normalizedTarget

    if inventory == nil or normalizedTarget == "" then
        return result
    end

    local function appendItem(item)
        if item == nil or seen[item] then return end
        seen[item] = true
        table.insert(result, item)
    end

    local function appendJavaList(list)
        if list == nil or list.size == nil or list.get == nil then
            return
        end

        local okSize, size = pcall(function()
            return list:size()
        end)

        if not okSize or size == nil then return end

        for index = 0, size - 1 do
            local okItem, item = pcall(function()
                return list:get(index)
            end)

            if okItem and item ~= nil then
                appendItem(item)
            end
        end
    end

    -- Prefer the engine's native full-type query on Build 41.
    -- The boolean overload includes nested inventories when available.
    if inventory.getItemsFromFullType ~= nil then
        local okNested, nestedItems = pcall(function()
            return inventory:getItemsFromFullType(target, true)
        end)

        if okNested then appendJavaList(nestedItems) end

        local okDirect, directItems = pcall(function()
            return inventory:getItemsFromFullType(target)
        end)

        if okDirect then appendJavaList(directItems) end
    end

    local function itemMatches(item)
        local candidates = {}
        local itemType = ""
        local itemModule = ""

        if item.getFullType ~= nil then
            local ok, value = pcall(function()
                return item:getFullType()
            end)
            if ok and value ~= nil then
                table.insert(candidates, tostring(value))
            end
        end

        if item.getType ~= nil then
            local ok, value = pcall(function()
                return item:getType()
            end)
            if ok and value ~= nil then
                itemType = tostring(value)
                table.insert(candidates, itemType)
            end
        end

        if item.getModule ~= nil then
            local ok, value = pcall(function()
                return item:getModule()
            end)
            if ok and value ~= nil then
                itemModule = tostring(value)
            end
        end

        if itemModule ~= "" and itemType ~= "" then
            table.insert(candidates, itemModule .. "." .. itemType)
        end

        if item.getScriptItem ~= nil then
            local okScript, scriptItem = pcall(function()
                return item:getScriptItem()
            end)

            if okScript and scriptItem ~= nil
                and scriptItem.getFullName ~= nil then
                local okName, value = pcall(function()
                    return scriptItem:getFullName()
                end)

                if okName and value ~= nil then
                    table.insert(candidates, tostring(value))
                end
            end
        end

        for _, candidate in ipairs(candidates) do
            local normalized = string.lower(
                tostring(candidate or "")
            )

            if normalized == normalizedTarget
                or normalized == shortTarget
                or normalized:match("([^%.]+)$") == shortTarget then
                return true
            end
        end

        return false
    end

    local function visit(container, depth)
        if container == nil
            or visited[container]
            or depth > 10
            or container.getItems == nil then
            return
        end

        visited[container] = true

        local ok, items = pcall(function()
            return container:getItems()
        end)

        if not ok or items == nil then return end

        for index = 0, items:size() - 1 do
            local item = items:get(index)

            if item ~= nil then
                if itemMatches(item) then
                    appendItem(item)
                end

                local nested = nil

                if item.getInventory ~= nil then
                    local okNested, value = pcall(function()
                        return item:getInventory()
                    end)
                    if okNested then nested = value end
                elseif item.getItemContainer ~= nil then
                    local okNested, value = pcall(function()
                        return item:getItemContainer()
                    end)
                    if okNested then nested = value end
                end

                if nested ~= nil then
                    visit(nested, depth + 1)
                end
            end
        end
    end

    visit(inventory, 0)
    return result
end

-- QPSC_DELIVERY_TWO_PHASE_SYNC_V2
local QPSC_pendingDeliveries = {}
local QPSC_deliveryTokenCounter = 0

local function QPSC_deliveryPendingKey(username, contractId)
    return string.lower(tostring(username or ""))
        .. "|"
        .. tostring(contractId or "")
end

local function QPSC_createDeliveryToken(username, contractId)
    QPSC_deliveryTokenCounter =
        QPSC_deliveryTokenCounter + 1

    return tostring(username or "")
        .. ":"
        .. tostring(contractId or "")
        .. ":"
        .. tostring(QPSC_deliveryTokenCounter)
end

local function QPSC_findContractById(data, contractId)
    for _, contract in ipairs(data.contracts or {}) do
        if tostring(contract.id) == tostring(contractId) then
            return contract
        end
    end

    return nil
end

local function QPSC_removeServerDeliveryItems(
    inventory,
    fullType,
    count
)
    local required = math.max(
        0,
        math.floor(tonumber(count) or 0)
    )

    if inventory == nil or required <= 0 then
        return 0
    end

    local items = QPSC_getInventoryItemsByFullType(
        inventory,
        fullType
    )

    if #items < required then
        return 0
    end

    local removed = 0

    for index = 1, required do
        local item = items[index]
        local container = inventory

        if item ~= nil and item.getContainer ~= nil then
            local okContainer, value = pcall(function()
                return item:getContainer()
            end)

            if okContainer and value ~= nil then
                container = value
            end
        end

        if container ~= nil and item ~= nil then
            local containedBefore = true

            if container.contains ~= nil then
                local okContains, value = pcall(function()
                    return container:contains(item)
                end)

                if okContains then
                    containedBefore = value == true
                end
            end

            if containedBefore then
                local okRemove = pcall(function()
                    container:Remove(item)
                end)

                if okRemove then
                    local containedAfter = false

                    if container.contains ~= nil then
                        local okContains, value = pcall(function()
                            return container:contains(item)
                        end)

                        if okContains then
                            containedAfter = value == true
                        end
                    end

                    if not containedAfter then
                        removed = removed + 1
                    end
                end
            end
        end
    end

    return removed
end

-- QPSC_MULTI_OBJECTIVE_DELIVERY_RUNTIME_V100
local function QPSC_findNextMultiDeliveryObjective(contract, participant, player)
    if not QPSC_isMultiObjective(contract) then return nil end
    for _, objective in ipairs(contract.objectives or {}) do
        if tostring(objective.type or "") == "DELIVERY"
            and QPSC_getMultiProgress(contract, participant, objective)
                < math.max(1, tonumber(objective.target) or 1)
            and QPSC_isPlayerNearMultiObjective(player, objective) then
            return objective
        end
    end
    return nil
end

local function QPSC_submitMultiDelivery(player, contract, participant)
    local objective = QPSC_findNextMultiDeliveryObjective(contract, participant, player)
    if objective == nil then
        QPSC_sendMessage(
            player,
            "There is no incomplete delivery objective at your current location.",
            "UI_QPSC_MessageNoMultiDeliveryHere"
        )
        return
    end

    local current = QPSC_getMultiProgress(contract, participant, objective)
    local remaining = math.max(0, (tonumber(objective.target) or 1) - current)
    local fullType = tostring(objective.itemFullType or "")
    local inventory = player:getInventory()
    local items = QPSC_getInventoryItemsByFullType(inventory, fullType)
    local available = #items

    -- QPSC_B41_MULTI_CLIENT_CONFIRMED_DELIVERY_V1
    -- Build 41 can temporarily expose an empty server-side inventory
    -- replica even while the submitting client visibly owns the items.
    -- Do not reject the delivery from the stale server count. Ask the
    -- client to validate and consume the exact remaining amount, then
    -- verify the acknowledgement before advancing this objective.
    local submitCount = remaining
    local username = QPSC_getUsername(player)
    local key = QPSC_deliveryPendingKey(username, contract.id)
    local token = QPSC_createDeliveryToken(username, contract.id)

    QPSC_pendingDeliveries[key] = {
        token = token,
        username = username,
        contractId = tostring(contract.id),
        objectiveId = tostring(objective.id or ""),
        fullType = fullType,
        target = submitCount,
        beforeCount = math.max(submitCount, available),
        serverCountAtRequest = available
    }

    sendServerCommand(player, MODULE, "ConsumeDeliveryItems", {
        token = token,
        contractId = tostring(contract.id),
        objectiveId = tostring(objective.id or ""),
        fullType = fullType,
        count = submitCount
    })
end

local function QPSC_confirmMultiDeliveryConsumption(player, args, pending, contract, participant)
    local objective = QPSC_getMultiObjective(contract, pending.objectiveId)
    if objective == nil or tostring(objective.type or "") ~= "DELIVERY" then return true end
    if not QPSC_isPlayerNearMultiObjective(player, objective) then
        QPSC_sendMessage(player, "You must be at the objective location.", "UI_QPSC_MessageGoToDeliveryLocation")
        return true
    end

    local target = math.max(1, tonumber(pending.target) or 1)
    local inventory = player:getInventory()
    local fullType = tostring(pending.fullType or "")
    local itemDisplayName = QPSC_getScriptDisplayName(fullType, objective.itemDisplayName)
    local currentItems = QPSC_getInventoryItemsByFullType(inventory, fullType)
    local currentCount = #currentItems
    local clientAvailable = math.max(0, math.floor(tonumber(args.available) or 0))
    local reportedAvailable = math.max(currentCount, clientAvailable)

    if args.success ~= true or tonumber(args.removed) ~= target then
        QPSC_sendMessage(
            player,
            "Delivery requires " .. tostring(target) .. " x " .. itemDisplayName
                .. "; you have " .. tostring(reportedAvailable) .. ".",
            "UI_QPSC_MessageDeliveryMissing",
            target,
            itemDisplayName,
            reportedAvailable
        )
        return true
    end

    local beforeCount = math.max(target, tonumber(pending.beforeCount) or currentCount, clientAvailable)
    local alreadyRemoved = math.max(0, beforeCount - currentCount)
    local remainingToRemove = math.max(0, target - alreadyRemoved)

    if remainingToRemove > currentCount then
        QPSC_sendMessage(player, "Delivery items could not be verified.", "UI_QPSC_MessageDeliveryMissing", target, itemDisplayName, currentCount)
        return true
    end

    local serverRemoved = QPSC_removeServerDeliveryItems(inventory, fullType, remainingToRemove)
    if serverRemoved ~= remainingToRemove then
        QPSC_sendMessage(player, "Delivery items could not be verified.", "UI_QPSC_MessageDeliveryMissing", target, itemDisplayName, currentCount)
        return true
    end

    QPSC_advanceMultiObjective(
        contract,
        participant,
        objective,
        target,
        player,
        QPSC_getSquareAt(objective.targetX, objective.targetY, objective.targetZ)
            or QPSC_getPlayerSquare(player),
        "Multi-Objective Delivery"
    )
    return true
end

local function QPSC_submitDelivery(player, contractId)
    local data = QPSC_getData()
    QPSC_migrateData(data)

    local username = QPSC_getUsername(player)
    local contract = QPSC_findContractById(
        data,
        contractId
    )

    if contract == nil then
        QPSC_sendMessage(
            player,
            "Contract not found.",
            "UI_QPSC_MessageContractNotFound"
        )
        return
    end

    local participant =
        QPSC_findParticipant(contract, username)

    if QPSC_isMultiObjective(contract) then
        if participant == nil
            or tostring(participant.status or "") ~= "Accepted" then
            QPSC_sendMessage(player, "You do not have this active delivery contract.", "UI_QPSC_MessageNoActiveDelivery")
            return
        end
        QPSC_submitMultiDelivery(player, contract, participant)
        return
    end

    if QPSC_normalizeObjectiveType(
        contract.objectiveType
    ) ~= "DELIVERY" then
        QPSC_sendMessage(
            player,
            "This is not a delivery contract.",
            "UI_QPSC_MessageNotDelivery"
        )
        return
    end

    if participant == nil
        or tostring(participant.status or "")
            ~= "Accepted" then
        QPSC_sendMessage(
            player,
            "You do not have this active delivery contract.",
            "UI_QPSC_MessageNoActiveDelivery"
        )
        return
    end

    if not QPSC_isPlayerNearTarget(player, contract) then
        QPSC_sendMessage(
            player,
            "You must be at the contract location to submit the delivery.",
            "UI_QPSC_MessageGoToDeliveryLocation"
        )
        return
    end

    local target = QPSC_normalizePositiveInteger(
        contract.objectiveTarget,
        10000
    )
    local fullType = tostring(
        contract.objectiveItemFullType or ""
    )
    local itemDisplayName = QPSC_getScriptDisplayName(
        fullType,
        contract.objectiveItemDisplayName
    )
    local inventory = player:getInventory()
    local items = QPSC_getInventoryItemsByFullType(
        inventory,
        fullType
    )
    local available = #items

    -- QPSC_B41_CLIENT_CONFIRMED_DELIVERY_V1
    -- Build 41 can temporarily expose an empty server-side inventory
    -- replica even while the submitting client visibly owns the items.
    -- Contract, participant, location, type and target remain validated
    -- by the server; the client must then remove the exact items and
    -- report the pre-removal count before completion can continue.
    participant.progress = math.min(
        target,
        available
    )

    local key = QPSC_deliveryPendingKey(
        username,
        contract.id
    )
    local token = QPSC_createDeliveryToken(
        username,
        contract.id
    )

    QPSC_pendingDeliveries[key] = {
        token = token,
        username = username,
        contractId = tostring(contract.id),
        fullType = fullType,
        target = target,
        beforeCount = math.max(target, available),
        serverCountAtRequest = available
    }

    sendServerCommand(
        player,
        MODULE,
        "ConsumeDeliveryItems",
        {
            token = token,
            contractId = tostring(contract.id),
            fullType = fullType,
            count = target
        }
    )
end

local function QPSC_confirmDeliveryConsumption(
    player,
    args
)
    args = args or {}

    local username = QPSC_getUsername(player)
    local contractId = tostring(args.contractId or "")
    local key = QPSC_deliveryPendingKey(
        username,
        contractId
    )
    local pending = QPSC_pendingDeliveries[key]

    if pending == nil
        or tostring(pending.token or "")
            ~= tostring(args.token or "") then
        return
    end

    QPSC_pendingDeliveries[key] = nil

    local data = QPSC_getData()
    QPSC_migrateData(data)

    local contract = QPSC_findContractById(
        data,
        contractId
    )

    if contract == nil then
        return
    end

    local participant =
        QPSC_findParticipant(contract, username)

    if participant == nil
        or tostring(participant.status or "")
            ~= "Accepted" then
        return
    end

    if QPSC_isMultiObjective(contract) then
        QPSC_confirmMultiDeliveryConsumption(
            player,
            args,
            pending,
            contract,
            participant
        )
        return
    end

    if QPSC_normalizeObjectiveType(
        contract.objectiveType
    ) ~= "DELIVERY" then
        return
    end

    local target = math.max(
        1,
        tonumber(pending.target) or 1
    )
    local inventory = player:getInventory()
    local fullType = tostring(
        pending.fullType or ""
    )
    local itemDisplayName = QPSC_getScriptDisplayName(
        fullType,
        contract.objectiveItemDisplayName
    )
    local currentItems = QPSC_getInventoryItemsByFullType(
        inventory,
        fullType
    )
    local currentCount = #currentItems

    local clientAvailable = math.max(
        0,
        math.floor(tonumber(args.available) or 0)
    )
    local reportedAvailable = math.max(
        currentCount,
        clientAvailable
    )

    if args.success ~= true
        or tonumber(args.removed) ~= target then
        participant.progress = math.min(
            target,
            reportedAvailable
        )
        QPSC_transmit()
        QPSC_broadcastContracts()
        QPSC_sendMessage(
            player,
            "Delivery requires "
                .. tostring(target)
                .. " x "
                .. itemDisplayName
                .. "; you have "
                .. tostring(reportedAvailable)
                .. ".",
            "UI_QPSC_MessageDeliveryMissing",
            target,
            itemDisplayName,
            reportedAvailable
        )
        return
    end

    local beforeCount = math.max(
        target,
        tonumber(pending.beforeCount) or currentCount,
        clientAvailable
    )
    local alreadyRemoved = math.max(
        0,
        beforeCount - currentCount
    )
    local remainingToRemove = math.max(
        0,
        target - alreadyRemoved
    )

    if remainingToRemove > currentCount then
        participant.progress = math.min(
            target,
            currentCount
        )
        QPSC_transmit()
        QPSC_broadcastContracts()
        QPSC_sendMessage(
            player,
            "Delivery requires "
                .. tostring(target)
                .. " x "
                .. itemDisplayName
                .. "; you have "
                .. tostring(currentCount)
                .. ".",
            "UI_QPSC_MessageDeliveryMissing",
            target,
            itemDisplayName,
            currentCount
        )
        return
    end

    local serverRemoved = QPSC_removeServerDeliveryItems(
        inventory,
        fullType,
        remainingToRemove
    )

    if serverRemoved ~= remainingToRemove then
        participant.progress = math.min(
            target,
            currentCount
        )
        QPSC_transmit()
        QPSC_broadcastContracts()
        QPSC_sendMessage(
            player,
            "Delivery requires "
                .. tostring(target)
                .. " x "
                .. itemDisplayName
                .. "; you have "
                .. tostring(currentCount)
                .. ".",
            "UI_QPSC_MessageDeliveryMissing",
            target,
            itemDisplayName,
            currentCount
        )
        return
    end

    participant.progress = target

    QPSC_completeTrackedParticipant(
        contract,
        participant,
        player,
        QPSC_getSquareAt(
            contract.targetX,
            contract.targetY,
            contract.targetZ
        ) or QPSC_getPlayerSquare(player),
        "Delivery"
    )
end

-- QPSC_V123_IMMEDIATE_REACH_LOCATION_V1
local function QPSC_reportImmediateLocationCheck(
    player,
    args
)
    if player == nil or type(args) ~= "table" then
        return false
    end

    local requestedContractId =
        tostring(args.contractId or "")
    local requestedObjectiveId =
        tostring(args.objectiveId or "")

    if requestedContractId == "" then
        return false
    end

    local data = QPSC_getData()
    QPSC_migrateData(data)

    local contract, participant =
        QPSC_findActiveContractForUsername(
            data,
            QPSC_getUsername(player)
        )

    if contract == nil
        or participant == nil
        or tostring(participant.status or "") ~= "Accepted"
        or tostring(contract.id or "") ~= requestedContractId then
        return false
    end

    if QPSC_isMultiObjective(contract) then
        for _, objective in ipairs(
            contract.objectives or {}
        ) do
            if tostring(objective.type or "") == "LOCATION"
                and (
                    requestedObjectiveId == ""
                    or tostring(objective.id or "")
                        == requestedObjectiveId
                )
                and QPSC_getMultiProgress(
                    contract,
                    participant,
                    objective
                ) < math.max(
                    1,
                    tonumber(objective.target) or 1
                )
                and QPSC_isPlayerNearMultiObjective(
                    player,
                    objective
                ) then
                QPSC_advanceMultiObjective(
                    contract,
                    participant,
                    objective,
                    1,
                    player,
                    QPSC_getPlayerSquare(player),
                    "Immediate Multi-Objective Location"
                )

                print(
                    "[QPSC] Immediate Reach Location accepted for "
                        .. QPSC_getUsername(player)
                        .. " | contract #"
                        .. tostring(contract.id or "?")
                        .. " | objective "
                        .. tostring(objective.id or "?")
                )

                return true
            end
        end

        return false
    end

    if QPSC_normalizeObjectiveType(
            contract.objectiveType
        ) ~= "LOCATION"
        or not QPSC_isPlayerNearTarget(
            player,
            contract
        ) then
        return false
    end

    participant.progress =
        tonumber(contract.objectiveTarget) or 1

    QPSC_completeTrackedParticipant(
        contract,
        participant,
        player,
        QPSC_getPlayerSquare(player),
        "Immediate Location"
    )

    print(
        "[QPSC] Immediate Reach Location accepted for "
            .. QPSC_getUsername(player)
            .. " | contract #"
            .. tostring(contract.id or "?")
    )

    return true
end

local function QPSC_updateLocationObjectives()
    local data = QPSC_getData()
    QPSC_migrateData(data)
    local players = getOnlinePlayers()

    if players == nil then return end

    for index = 0, players:size() - 1 do
        local player = players:get(index)

        if player ~= nil then
            local contract, participant =
                QPSC_findActiveContractForUsername(
                    data,
                    QPSC_getUsername(player)
                )

            if contract ~= nil
                and participant ~= nil
                and QPSC_isMultiObjective(contract) then
                for _, objective in ipairs(contract.objectives or {}) do
                    if tostring(objective.type or "") == "LOCATION"
                        and QPSC_getMultiProgress(contract, participant, objective)
                            < math.max(1, tonumber(objective.target) or 1)
                        and QPSC_isPlayerNearMultiObjective(player, objective) then
                        QPSC_advanceMultiObjective(
                            contract, participant, objective, 1, player,
                            QPSC_getPlayerSquare(player),
                            "Multi-Objective Location"
                        )
                        if tostring(participant.status or "") ~= "Accepted" then break end
                    end
                end
            elseif contract ~= nil
                and participant ~= nil
                and QPSC_normalizeObjectiveType(
                    contract.objectiveType
                ) == "LOCATION"
                and QPSC_isPlayerNearTarget(
                    player,
                    contract
                ) then
                participant.progress =
                    tonumber(contract.objectiveTarget) or 1

                QPSC_completeTrackedParticipant(
                    contract,
                    participant,
                    player,
                    QPSC_getPlayerSquare(player),
                    "Location"
                )
            end
        end
    end
end

-- QPSC_V053_ZOMBIE_EVENT_DEDUP_V1
local function QPSC_claimZombieContractCredit(zombie)
    if zombie == nil or zombie.getModData == nil then
        return true
    end

    local ok, claimed = pcall(function()
        local modData = zombie:getModData()

        if modData == nil then return false end
        if modData.QPSC_KillCredited_v053 == true then
            return true
        end

        modData.QPSC_KillCredited_v053 = true
        return false
    end)

    if not ok then return true end
    return claimed ~= true
end

local function QPSC_getZombieKiller(zombie)
    if zombie == nil then return nil end

    local methods = {
        "getAttackedBy",
        "getLastHitCharacter"
    }

    for _, methodName in ipairs(methods) do
        local method = zombie[methodName]

        if method ~= nil then
            local ok, candidate = pcall(function()
                return method(zombie)
            end)

            if ok and candidate ~= nil then
                local isPlayer = false

                if instanceof ~= nil then
                    local okType, value = pcall(function()
                        return instanceof(candidate, "IsoPlayer")
                    end)
                    isPlayer = okType and value == true
                elseif candidate.getUsername ~= nil then
                    isPlayer = true
                end

                if isPlayer then return candidate end
            end
        end
    end

    -- Build 41/42 may clear the direct attacker reference before
    -- OnZombieDead. Use a conservative server-observed fallback:
    -- exactly one nearby online survivor must have an eligible,
    -- accepted Kill objective at this zombie's position.
    local zombieX, zombieY =
        QPSC_getZombieObjectiveCoordinates(zombie)

    if zombieX == nil or zombieY == nil then
        return nil
    end

    local players = nil
    local playersOk = pcall(function()
        players = getOnlinePlayers()
    end)

    if not playersOk or players == nil then
        return nil
    end

    local data = QPSC_getData()
    QPSC_migrateData(data)

    local matchedPlayer = nil
    local matchedContract = nil
    local matchCount = 0
    local maxFallbackDistanceSquared = 144

    for index = 0, players:size() - 1 do
        local candidate = players:get(index)

        if candidate ~= nil then
            local contract, participant =
                QPSC_findActiveContractForUsername(
                    data,
                    QPSC_getUsername(candidate)
                )

            local eligible = false

            if contract ~= nil and participant ~= nil then
                if QPSC_isMultiObjective(contract) then
                    for _, objective in ipairs(
                        contract.objectives or {}
                    ) do
                        if tostring(objective.type or "") == "KILL"
                            and QPSC_getMultiProgress(
                                contract,
                                participant,
                                objective
                            ) < math.max(
                                1,
                                tonumber(objective.target) or 1
                            )
                            and QPSC_isZombieInsideMultiObjective(
                                zombie,
                                objective
                            ) then
                            eligible = true
                            break
                        end
                    end
                elseif QPSC_normalizeObjectiveType(
                    contract.objectiveType
                ) == "KILL" then
                    local radius =
                        tonumber(contract.objectiveRadius) or 0

                    if radius < 1 then
                        eligible = true
                    else
                        local dx = zombieX
                            - (tonumber(contract.targetX) or 0)
                        local dy = zombieY
                            - (tonumber(contract.targetY) or 0)

                        eligible =
                            ((dx * dx) + (dy * dy))
                            <= (radius * radius)
                    end
                end
            end

            if eligible then
                local positionOk, playerX, playerY =
                    pcall(function()
                        return candidate:getX(),
                            candidate:getY()
                    end)

                playerX = tonumber(playerX)
                playerY = tonumber(playerY)

                if positionOk
                    and playerX ~= nil
                    and playerY ~= nil then
                    local dx = playerX - zombieX
                    local dy = playerY - zombieY
                    local distanceSquared =
                        (dx * dx) + (dy * dy)

                    if distanceSquared
                        <= maxFallbackDistanceSquared then
                        matchCount = matchCount + 1
                        matchedPlayer = candidate
                        matchedContract = contract
                    end
                end
            end
        end
    end

    if matchCount == 1 and matchedPlayer ~= nil then
        print(
            "[QPSC] Zombie killer fallback attributed kill to "
                .. QPSC_getUsername(matchedPlayer)
                .. " for contract #"
                .. tostring(
                    matchedContract
                    and matchedContract.id
                    or "?"
                )
        )
        return matchedPlayer
    end

    return nil
end

-- QPSC_V054_LOCATION_RESTRICTED_ZOMBIE_HUNTS_V1
local function QPSC_isZombieInsideKillZone(zombie, contract)
    local radius = tonumber(
        contract and contract.objectiveRadius
    ) or 0

    -- Existing v0.5.3 Zombie Hunt contracts used radius 0.
    -- Keep those contracts unrestricted for save compatibility.
    if radius < 1 then return true end
    if zombie == nil or contract == nil then return false end

    local zombieX, zombieY =
        QPSC_getZombieObjectiveCoordinates(zombie)

    if zombieX == nil or zombieY == nil then
        return false
    end

    local dx = zombieX
        - (tonumber(contract.targetX) or 0)
    local dy = zombieY
        - (tonumber(contract.targetY) or 0)

    return (dx * dx) + (dy * dy)
        <= (radius * radius)
end

local function QPSC_onZombieDead(zombie)
    local player = QPSC_getZombieKiller(zombie)
    if player == nil then return end

    local data = QPSC_getData()
    QPSC_migrateData(data)

    local contract, participant =
        QPSC_findActiveContractForUsername(
            data,
            QPSC_getUsername(player)
        )

    if contract == nil or participant == nil then return end

    if QPSC_isMultiObjective(contract) then
        local objective = nil
        for _, candidate in ipairs(contract.objectives or {}) do
            if tostring(candidate.type or "") == "KILL"
                and QPSC_getMultiProgress(contract, participant, candidate)
                    < math.max(1, tonumber(candidate.target) or 1)
                and QPSC_isZombieInsideMultiObjective(zombie, candidate) then
                objective = candidate
                break
            end
        end

        if objective == nil then return end
        if not QPSC_claimZombieContractCredit(zombie) then return end
        local rewardSquare = nil
        if zombie.getSquare ~= nil then
            local ok, square = pcall(function() return zombie:getSquare() end)
            if ok then rewardSquare = square end
        end
        QPSC_advanceMultiObjective(
            contract, participant, objective, 1, player,
            rewardSquare or QPSC_getPlayerSquare(player),
            "Multi-Objective Zombie Hunt"
        )
        return
    end

    if QPSC_normalizeObjectiveType(
        contract.objectiveType
    ) ~= "KILL" then
        return
    end

    if not QPSC_isZombieInsideKillZone(
        zombie,
        contract
    ) then
        return
    end

    if not QPSC_claimZombieContractCredit(zombie) then
        return
    end

    local target = math.max(
        1,
        tonumber(contract.objectiveTarget) or 1
    )

    if QPSC_isSharedTeamCompletion(contract) then
        if contract.sharedCompleted == true then
            return
        end

        participant.progress =
            (tonumber(participant.progress) or 0) + 1
        contract.sharedProgress = math.min(
            target,
            (tonumber(contract.sharedProgress) or 0) + 1
        )

        if contract.sharedProgress >= target then
            local rewardSquare = nil

            if zombie.getSquare ~= nil then
                local ok, square = pcall(function()
                    return zombie:getSquare()
                end)
                if ok then rewardSquare = square end
            end

            QPSC_closeSharedTeamContract(
                contract,
                participant,
                player,
                rewardSquare or QPSC_getPlayerSquare(player),
                "Zombie Hunt"
            )
        else
            QPSC_transmit()
            QPSC_broadcastContracts()
            QPSC_sendMessage(
                player,
                "Shared Team progress: "
                    .. tostring(contract.sharedProgress)
                    .. "/"
                    .. tostring(target)
                    .. " | Your contribution: "
                    .. tostring(participant.progress),
                "UI_QPSC_MessageSharedTeamProgress",
                contract.sharedProgress,
                target,
                participant.progress
            )
        end

        return
    end

    participant.progress = math.min(
        target,
        (tonumber(participant.progress) or 0) + 1
    )

    if participant.progress >= target then
        local rewardSquare = nil

        if zombie.getSquare ~= nil then
            local ok, square = pcall(function()
                return zombie:getSquare()
            end)
            if ok then rewardSquare = square end
        end

        QPSC_completeTrackedParticipant(
            contract,
            participant,
            player,
            rewardSquare or QPSC_getPlayerSquare(player),
            "Zombie Hunt"
        )
    else
        QPSC_transmit()
        QPSC_broadcastContracts()

        QPSC_sendMessage(
            player,
            "Zombie hunt progress: "
                .. tostring(participant.progress)
                .. "/"
                .. tostring(target),
            "UI_QPSC_MessageKillProgress",
            participant.progress,
            target
        )
    end
end

local function QPSC_getOnlineUsernameSet()
    local result = {}
    local players = getOnlinePlayers()

    if players == nil then
        return result
    end

    for index = 0, players:size() - 1 do
        local player = players:get(index)

        if player ~= nil then
            result[
                QPSC_normalizeUsername(
                    QPSC_getUsername(player)
                )
            ] = true
        end
    end

    return result
end

-- QPSC_CONTRACT_DEADLINES_V1
-- QPSC_ONLINE_ONLY_PARTICIPANT_TIMERS_V1
local function QPSC_updateParticipantTimers()
    local data = QPSC_getData()
    local changed = QPSC_migrateData(data)
    local now = QPSC_getWorldAgeHours()
    local onlineUsernames =
        QPSC_getOnlineUsernameSet()

    for _, contract in ipairs(data.contracts or {}) do
        local timeLimitHours =
            QPSC_normalizeTimeLimit(
                contract.timeLimitHours
            )

        for _, participant in ipairs(
            contract.participants or {}
        ) do
            local key = QPSC_timerKey(
                contract.id,
                participant.username
            )

            if participant.status == "Accepted"
                and timeLimitHours > 0 then
                local usernameKey =
                    QPSC_normalizeUsername(
                        participant.username
                    )

                if onlineUsernames[usernameKey] then
                    local previousMark =
                        tonumber(QPSC_timerMarks[key])

                    if previousMark ~= nil
                        and now >= previousMark then
                        local elapsed =
                            math.max(0, now - previousMark)

                        if elapsed > 0 then
                            local previousRemaining =
                                tonumber(
                                    participant.remainingHours
                                ) or timeLimitHours
                            local remaining =
                                math.max(
                                    0,
                                    previousRemaining - elapsed
                                )

                            if math.abs(
                                remaining - previousRemaining
                            ) > 0.000001 then
                                participant.remainingHours =
                                    remaining
                                changed = true
                            end
                        end
                    end

                    QPSC_timerMarks[key] = now

                    if (
                        tonumber(
                            participant.remainingHours
                        ) or 0
                    ) <= 0 then
                        participant.remainingHours = 0
                        participant.status = "Expired"
                        participant.expiredAt = now
                        QPSC_timerMarks[key] = nil
                        changed = true

                        print(
                            "[QPSC] Contract #"
                                .. tostring(contract.id)
                                .. " expired for "
                                .. tostring(
                                    participant.username
                                )
                        )
                    end
                else
                    -- Removing the runtime mark pauses the timer.
                    -- Reconnecting starts a fresh mark at the
                    -- current world age, so offline time is ignored.
                    QPSC_timerMarks[key] = nil
                end
            else
                QPSC_timerMarks[key] = nil
            end
        end

        QPSC_recomputeContractStatus(contract)
    end

    if changed then
        QPSC_transmit()
        QPSC_broadcastContracts()
    end

    return changed
end

local function QPSC_cleanArgs(args)
    args = args or {}

    local rawCategory = QPSC_trimText(args.category)
    local rawDifficulty = QPSC_trimText(args.difficulty)
    local rawObjectiveType = QPSC_trimText(args.objectiveType)
    local rawCompletionMode = QPSC_trimText(args.completionMode)
    local categoryInvalid = rawCategory ~= ""
        and not QPSC_enumInputValid(
            rawCategory,
            QPSC_CATEGORY_KEYS,
            false
        )
    local difficultyInvalid = rawDifficulty ~= ""
        and not QPSC_enumInputValid(
            rawDifficulty,
            QPSC_DIFFICULTY_KEYS,
            false
        )
    local objectiveTypeInvalid = rawObjectiveType ~= ""
        and not QPSC_enumInputValid(
            rawObjectiveType,
            QPSC_OBJECTIVE_KEYS,
            false
        )
    local completionModeValueInvalid =
        rawCompletionMode ~= ""
        and not QPSC_enumInputValid(
            rawCompletionMode,
            QPSC_COMPLETION_MODE_KEYS,
            false
        )
    local numericInvalid = false
    local numericInvalidField = ""

    local function markNumericInvalid(field, valid)
        if not valid and not numericInvalid then
            numericInvalid = true
            numericInvalidField = tostring(field or "unknown")
        end
    end

    markNumericInvalid(
        "timeLimitHours",
        QPSC_numberInputValid(
            args.timeLimitHours,
            true,
            0,
            8760,
            false
        )
    )
    markNumericInvalid(
        "objectiveTarget",
        QPSC_numberInputValid(
            args.objectiveTarget,
            true,
            0,
            10000,
            true
        )
    )
    markNumericInvalid(
        "objectiveRadius",
        QPSC_numberInputValid(
            args.objectiveRadius,
            true,
            0,
            1000,
            true
        )
    )
    markNumericInvalid(
        "rewardQuantity",
        QPSC_numberInputValid(
            args.rewardQuantity,
            true,
            0,
            100,
            true
        )
    )
    markNumericInvalid(
        "reputationPoints",
        QPSC_numberInputValid(
            args.reputationPoints,
            true,
            0,
            100000,
            true
        )
    )
    markNumericInvalid(
        "secondaryReputationPoints",
        QPSC_numberInputValid(
            args.secondaryReputationPoints,
            true,
            0,
            100000,
            true
        )
    )
    markNumericInvalid(
        "firstFinisherBonusQuantity",
        QPSC_numberInputValid(
            args.firstFinisherBonusQuantity,
            true,
            0,
            100,
            true
        )
    )

    local title = QPSC_limitText(args.title, QPSC_TEXT_LIMITS.title)
    local location = QPSC_limitText(args.location, QPSC_TEXT_LIMITS.location)
    local reward = QPSC_limitText(args.reward, QPSC_TEXT_LIMITS.reward)
    local description = QPSC_limitText(
        args.description,
        QPSC_TEXT_LIMITS.description
    )
    local category =
        QPSC_normalizeCategory(args.category)
    local difficulty =
        QPSC_normalizeDifficulty(args.difficulty)
    local timeLimitText = QPSC_limitText(
        args.timeLimitText,
        QPSC_TEXT_LIMITS.timeText
    )
    local timeLimitHours =
        QPSC_normalizeTimeLimit(args.timeLimitHours)
    local timeLimitInvalid =
        timeLimitText ~= ""
        and timeLimitHours <= 0
    local objectiveType =
        QPSC_normalizeObjectiveType(args.objectiveType)
    local completionModeText = tostring(args.completionMode or "")
    local completionMode = completionModeText ~= ""
        and QPSC_normalizeCompletionMode(completionModeText)
        or (
            (objectiveType == "KILL" or objectiveType == "LOCATION")
            and "GLOBAL"
            or "INDIVIDUAL"
        )
    local objectiveTarget =
        QPSC_normalizePositiveInteger(
            args.objectiveTarget,
            10000
        )
    local objectiveRadiusNumber =
        tonumber(args.objectiveRadius) or 0
    local objectiveRadius =
        QPSC_normalizePositiveInteger(
            objectiveRadiusNumber,
            1000
        )
    local objectiveItemFullType = QPSC_limitText(
        args.objectiveItemFullType,
        QPSC_TEXT_LIMITS.itemType
    )
    local objectiveItemDisplayName = QPSC_limitText(
        args.objectiveItemDisplayName,
        QPSC_TEXT_LIMITS.itemName
    )
    local rewardItemFullType = QPSC_limitText(
        args.rewardItemFullType,
        QPSC_TEXT_LIMITS.itemType
    )
    local rewardItemDisplayName = QPSC_limitText(
        args.rewardItemDisplayName,
        QPSC_TEXT_LIMITS.itemName
    )
    local rewardQuantity =
        QPSC_normalizePositiveInteger(
            args.rewardQuantity,
            100
        )
    local reputationPath =
        QPSC_normalizeReputationPath(
            args.reputationPath
        )
    local rawReputationPoints =
        tonumber(args.reputationPoints) or 0
    local reputationPoints =
        QPSC_normalizePositiveInteger(
            rawReputationPoints,
            100000
        )
    local secondaryReputationPath =
        QPSC_normalizeReputationPath(
            args.secondaryReputationPath
        )
    local rawSecondaryReputationPoints =
        tonumber(args.secondaryReputationPoints) or 0
    local secondaryReputationPoints =
        QPSC_normalizePositiveInteger(
            rawSecondaryReputationPoints,
            100000
        )
    local firstFinisherBonusItemFullType =
        QPSC_limitText(
            args.firstFinisherBonusItemFullType,
            QPSC_TEXT_LIMITS.itemType
        )
    local firstFinisherBonusItemDisplayName =
        QPSC_limitText(
            args.firstFinisherBonusItemDisplayName,
            QPSC_TEXT_LIMITS.itemName
        )
    local firstFinisherBonusQuantity =
        QPSC_normalizePositiveInteger(
            args.firstFinisherBonusQuantity,
            100
        )

    local rewardItems,
        rewardBundleInvalid,
        rewardNumericInvalidField =
        QPSC_cleanRewardItemsFromArgs(args)

    if rewardNumericInvalidField ~= "" then
        markNumericInvalid(
            rewardNumericInvalidField,
            false
        )
    end

    local firstReward = rewardItems[1]
    rewardItemFullType =
        firstReward and firstReward.fullType or ""
    rewardItemDisplayName =
        firstReward and firstReward.displayName or ""
    rewardQuantity =
        firstReward and firstReward.quantity or 0

    local objectiveInvalid = false
    local completionModeInvalid = false
    local rewardInvalid = rewardBundleInvalid == true
    local reputationInvalid = false
    local reputationUnavailable = false
    local firstFinisherBonusInvalid = false

    if completionMode == "SHARED_TEAM"
        and objectiveType ~= "KILL" then
        completionModeInvalid = true
    end

    if objectiveType ~= "MANUAL" and objectiveTarget < 1 then
        objectiveInvalid = true
    end

    if objectiveType == "KILL" then
        if objectiveRadiusNumber < 1
            or objectiveRadiusNumber > 1000 then
            objectiveInvalid = true
        end
    elseif objectiveType == "LOCATION" then
        if objectiveRadiusNumber < 1
            or objectiveRadiusNumber > 20 then
            objectiveInvalid = true
        end
    else
        objectiveRadius = 0
    end

    if objectiveType == "DELIVERY" then
        if objectiveItemFullType == ""
            or QPSC_getScriptItem(objectiveItemFullType) == nil then
            objectiveInvalid = true
        else
            objectiveItemDisplayName =
                QPSC_getScriptDisplayName(
                    objectiveItemFullType,
                    objectiveItemDisplayName
                )
        end
    end

    if rewardItemFullType ~= "" or rewardQuantity > 0 then
        if rewardItemFullType == ""
            or rewardQuantity < 1
            or QPSC_getScriptItem(rewardItemFullType) == nil then
            rewardInvalid = true
        else
            rewardItemDisplayName =
                QPSC_getScriptDisplayName(
                    rewardItemFullType,
                    rewardItemDisplayName
                )
        end
    end

    if reputationPath == "" and rawReputationPoints <= 0 then
        reputationPath = ""
        reputationPoints = 0
    elseif reputationPath == ""
        or rawReputationPoints ~= rawReputationPoints
        or rawReputationPoints < 1
        or rawReputationPoints > 100000 then
        reputationInvalid = true
    else
        reputationPoints = math.floor(rawReputationPoints)
        reputationUnavailable =
            not QPSC_reputationApiAvailable()
    end


    if secondaryReputationPath == ""
        and rawSecondaryReputationPoints <= 0 then
        secondaryReputationPath = ""
        secondaryReputationPoints = 0
    elseif secondaryReputationPath == ""
        or rawSecondaryReputationPoints
            ~= rawSecondaryReputationPoints
        or rawSecondaryReputationPoints < 1
        or rawSecondaryReputationPoints > 100000 then
        reputationInvalid = true
    else
        secondaryReputationPoints =
            math.floor(rawSecondaryReputationPoints)
        reputationUnavailable =
            reputationUnavailable
            or not QPSC_reputationApiAvailable()
    end

    if reputationPath ~= ""
        and secondaryReputationPath ~= ""
        and reputationPath == secondaryReputationPath then
        reputationInvalid = true
    end

    if firstFinisherBonusItemFullType ~= ""
        or firstFinisherBonusQuantity > 0 then
        if completionMode == "SHARED_TEAM"
            or firstFinisherBonusItemFullType == ""
            or firstFinisherBonusQuantity < 1
            or QPSC_getScriptItem(
                firstFinisherBonusItemFullType
            ) == nil then
            firstFinisherBonusInvalid = true
        else
            firstFinisherBonusItemDisplayName =
                QPSC_getScriptDisplayName(
                    firstFinisherBonusItemFullType,
                    firstFinisherBonusItemDisplayName
                )
        end
    end

    if title == "" then title = "Untitled Contract" end
    if location == "" then
        location = "Unknown location"
    end
    if reward == "" then reward = "Manual reward" end
    if description == "" then
        description = "No description."
    end

    return {
        title = title,
        category = category,
        difficulty = difficulty,
        completionMode = completionMode,
        location = location,
        reward = reward,
        description = description,
        timeLimitHours = timeLimitHours,
        timeLimitInvalid = timeLimitInvalid,
        objectiveType = objectiveType,
        objectiveTarget = objectiveTarget,
        objectiveRadius = objectiveRadius,
        objectiveItemFullType = objectiveItemFullType,
        objectiveItemDisplayName = objectiveItemDisplayName,
        rewardItemFullType = rewardItemFullType,
        rewardItemDisplayName = rewardItemDisplayName,
        rewardQuantity = rewardQuantity,
        rewardItems = rewardItems,
        reputationPath = reputationPath,
        reputationPoints = reputationPoints,
        secondaryReputationPath =
            secondaryReputationPath,
        secondaryReputationPoints =
            secondaryReputationPoints,
        reputationInvalid = reputationInvalid,
        reputationUnavailable = reputationUnavailable,
        firstFinisherBonusItemFullType =
            firstFinisherBonusItemFullType,
        firstFinisherBonusItemDisplayName =
            firstFinisherBonusItemDisplayName,
        firstFinisherBonusQuantity =
            firstFinisherBonusQuantity,
        objectiveInvalid = objectiveInvalid,
        completionModeInvalid = completionModeInvalid,
        rewardInvalid = rewardInvalid,
        firstFinisherBonusInvalid =
            firstFinisherBonusInvalid,
        categoryInvalid = categoryInvalid,
        difficultyInvalid = difficultyInvalid,
        objectiveTypeInvalid = objectiveTypeInvalid,
        completionModeValueInvalid =
            completionModeValueInvalid,
        numericInvalid = numericInvalid,
        numericInvalidField = numericInvalidField
    }
end


local function QPSC_validateCleanRequest(player, clean)
    if clean.categoryInvalid then
        QPSC_sendMessage(
            player,
            "Contract rejected: invalid category.",
            ""
        )
        return false
    end

    if clean.difficultyInvalid then
        QPSC_sendMessage(
            player,
            "Contract rejected: invalid difficulty.",
            ""
        )
        return false
    end

    if clean.objectiveTypeInvalid then
        QPSC_sendMessage(
            player,
            "Contract rejected: invalid objective type.",
            ""
        )
        return false
    end

    if clean.completionModeValueInvalid then
        QPSC_sendMessage(
            player,
            "Contract rejected: invalid completion mode.",
            ""
        )
        return false
    end

    if clean.numericInvalid then
        QPSC_sendMessage(
            player,
            "Contract rejected: malformed or out-of-range numeric field: "
                .. tostring(clean.numericInvalidField or "unknown")
                .. ".",
            ""
        )
        return false
    end

    return true
end

-- QPSC_MULTI_OBJECTIVE_CREATION_V100
local function QPSC_cleanMultiArgs(player, args)
    args = args or {}

    local rawCategory = QPSC_trimText(args.category)
    local rawDifficulty = QPSC_trimText(args.difficulty)
    local rawCompletionMode = QPSC_trimText(args.completionMode)
    local result = {
        title = QPSC_limitText(args.title, QPSC_TEXT_LIMITS.title),
        category = QPSC_normalizeCategory(args.category),
        difficulty = QPSC_normalizeDifficulty(args.difficulty),
        completionMode = QPSC_normalizeCompletionMode(args.completionMode),
        location = QPSC_limitText(args.location, QPSC_TEXT_LIMITS.location),
        description = QPSC_limitText(args.description, QPSC_TEXT_LIMITS.description),
        timeLimitHours = QPSC_normalizeTimeLimit(args.timeLimitHours),
        timeLimitInvalid = tostring(args.timeLimitText or "") ~= ""
            and QPSC_normalizeTimeLimit(args.timeLimitHours) <= 0,
        rewardItemFullType = QPSC_limitText(args.rewardItemFullType, QPSC_TEXT_LIMITS.itemType),
        rewardItemDisplayName = QPSC_limitText(args.rewardItemDisplayName, QPSC_TEXT_LIMITS.itemName),
        rewardQuantity = QPSC_normalizePositiveInteger(args.rewardQuantity, 100),
        reputationPath = "",
        reputationPoints = 0,
        secondaryReputationPath = "",
        secondaryReputationPoints = 0,
        reputationInvalid = false,
        reputationUnavailable = false,
        firstFinisherBonusItemFullType = QPSC_limitText(args.firstFinisherBonusItemFullType, QPSC_TEXT_LIMITS.itemType),
        firstFinisherBonusItemDisplayName = QPSC_limitText(args.firstFinisherBonusItemDisplayName, QPSC_TEXT_LIMITS.itemName),
        firstFinisherBonusQuantity = QPSC_normalizePositiveInteger(args.firstFinisherBonusQuantity, 100),
        objectives = {},
        invalidObjectives = false,
        rewardInvalid = false,
        firstFinisherBonusInvalid = false,
        categoryInvalid = rawCategory ~= ""
            and not QPSC_enumInputValid(
                rawCategory,
                QPSC_CATEGORY_KEYS,
                false
            ),
        difficultyInvalid = rawDifficulty ~= ""
            and not QPSC_enumInputValid(
                rawDifficulty,
                QPSC_DIFFICULTY_KEYS,
                false
            ),
        objectiveTypeInvalid = false,
        completionModeValueInvalid =
            rawCompletionMode ~= ""
            and not QPSC_enumInputValid(
                rawCompletionMode,
                QPSC_COMPLETION_MODE_KEYS,
                false
            ),
        numericInvalid = false,
        numericInvalidField = ""
    }

    local rewardItems,
        rewardBundleInvalid,
        rewardNumericInvalidField =
        QPSC_cleanRewardItemsFromArgs(args)

    result.rewardItems = rewardItems
    result.rewardInvalid =
        rewardBundleInvalid == true

    local firstReward = rewardItems[1]
    result.rewardItemFullType =
        firstReward and firstReward.fullType or ""
    result.rewardItemDisplayName =
        firstReward and firstReward.displayName or ""
    result.rewardQuantity =
        firstReward and firstReward.quantity or 0

    local function markNumericInvalid(field, valid)
        if not valid and not result.numericInvalid then
            result.numericInvalid = true
            result.numericInvalidField = tostring(field or "unknown")
        end
    end

    if rewardNumericInvalidField ~= "" then
        markNumericInvalid(
            rewardNumericInvalidField,
            false
        )
    end

    markNumericInvalid(
        "timeLimitHours",
        QPSC_numberInputValid(
            args.timeLimitHours,
            true,
            0,
            8760,
            false
        )
    )
    markNumericInvalid(
        "rewardQuantity",
        QPSC_numberInputValid(
            args.rewardQuantity,
            true,
            0,
            100,
            true
        )
    )
    markNumericInvalid(
        "reputationPoints",
        QPSC_numberInputValid(
            args.reputationPoints,
            true,
            0,
            100000,
            true
        )
    )
    markNumericInvalid(
        "secondaryReputationPoints",
        QPSC_numberInputValid(
            args.secondaryReputationPoints,
            true,
            0,
            100000,
            true
        )
    )
    markNumericInvalid(
        "firstFinisherBonusQuantity",
        QPSC_numberInputValid(
            args.firstFinisherBonusQuantity,
            true,
            0,
            100,
            true
        )
    )

    if result.title == "" then result.title = "Untitled Contract" end
    if result.location == "" then result.location = "Unknown location" end
    if result.description == "" then result.description = "No description." end

    local rawReputationPath =
        string.lower(tostring(args.reputationPath or ""))
    rawReputationPath =
        rawReputationPath:gsub("^%s+", ""):gsub("%s+$", "")
    local rawReputationPoints = tonumber(args.reputationPoints) or 0

    if rawReputationPath == ""
        and rawReputationPoints <= 0 then
        result.reputationPath = ""
        result.reputationPoints = 0
    elseif not QPSC_REPUTATION_KEYS[rawReputationPath]
        or rawReputationPoints ~= rawReputationPoints
        or rawReputationPoints < 1
        or rawReputationPoints > 100000 then
        result.reputationInvalid = true
    else
        result.reputationPath = rawReputationPath
        result.reputationPoints = math.floor(rawReputationPoints)
        result.reputationUnavailable =
            not QPSC_reputationApiAvailable()
    end


    local rawSecondaryReputationPath =
        string.lower(
            tostring(args.secondaryReputationPath or "")
        )
    rawSecondaryReputationPath =
        rawSecondaryReputationPath:gsub(
            "^%s+",
            ""
        ):gsub("%s+$", "")
    local rawSecondaryReputationPoints =
        tonumber(args.secondaryReputationPoints) or 0

    if rawSecondaryReputationPath == ""
        and rawSecondaryReputationPoints <= 0 then
        result.secondaryReputationPath = ""
        result.secondaryReputationPoints = 0
    elseif not QPSC_REPUTATION_KEYS[
        rawSecondaryReputationPath
    ]
        or rawSecondaryReputationPoints
            ~= rawSecondaryReputationPoints
        or rawSecondaryReputationPoints < 1
        or rawSecondaryReputationPoints > 100000 then
        result.reputationInvalid = true
    else
        result.secondaryReputationPath =
            rawSecondaryReputationPath
        result.secondaryReputationPoints =
            math.floor(rawSecondaryReputationPoints)
        result.reputationUnavailable =
            result.reputationUnavailable
            or not QPSC_reputationApiAvailable()
    end

    if result.reputationPath ~= ""
        and result.secondaryReputationPath ~= ""
        and result.reputationPath
            == result.secondaryReputationPath then
        result.reputationInvalid = true
    end

    local rawObjectiveCount = tonumber(args.objectiveCount)
    local objectiveCount = QPSC_normalizePositiveInteger(
        rawObjectiveCount,
        QPSC_MAX_OBJECTIVES
    )

    if not QPSC_isFiniteNumber(rawObjectiveCount)
        or rawObjectiveCount ~= math.floor(rawObjectiveCount)
        or rawObjectiveCount < 2
        or rawObjectiveCount > QPSC_MAX_OBJECTIVES then
        result.invalidObjectives = true
    end

    local fallbackX = player and tonumber(player:getX()) or 0
    local fallbackY = player and tonumber(player:getY()) or 0
    local fallbackZ = player and tonumber(player:getZ()) or 0
    local ids = {}

    for index = 1, objectiveCount do
        local prefix = "objective" .. tostring(index)
        local rawTypeText = QPSC_trimText(
            args[prefix .. "Type"]
        )
        local rawType = QPSC_normalizeObjectiveType(rawTypeText)
        local raw = {
            id = args[prefix .. "Id"],
            type = rawType,
            target = args[prefix .. "Target"],
            radius = args[prefix .. "Radius"],
            itemFullType = args[prefix .. "ItemFullType"],
            itemDisplayName = args[prefix .. "ItemDisplayName"],
            targetX = args[prefix .. "TargetX"],
            targetY = args[prefix .. "TargetY"],
            targetZ = args[prefix .. "TargetZ"]
        }
        local objective = QPSC_normalizeMultiObjective(raw, index, fallbackX, fallbackY, fallbackZ)

        if rawTypeText == ""
            or (
                rawType ~= "DELIVERY"
                and rawType ~= "KILL"
                and rawType ~= "LOCATION"
            ) then
            result.invalidObjectives = true
            result.objectiveTypeInvalid = true
        end

        if string.len(tostring(raw.id or "")) > 48 then
            result.invalidObjectives = true
        end

        if not QPSC_numberInputValid(
            raw.target,
            false,
            1,
            10000,
            true
        ) then
            result.invalidObjectives = true
            markNumericInvalid(prefix .. "Target", false)
        end

        local radiusValid = true

        if rawType == "KILL" then
            radiusValid = QPSC_numberInputValid(
                raw.radius,
                false,
                1,
                1000,
                true
            )
        elseif rawType == "LOCATION" then
            radiusValid = QPSC_numberInputValid(
                raw.radius,
                false,
                1,
                20,
                true
            )
        else
            radiusValid = QPSC_numberInputValid(
                raw.radius,
                true,
                0,
                1000,
                true
            )
        end

        if not radiusValid then
            result.invalidObjectives = true
            markNumericInvalid(prefix .. "Radius", false)
        end

        if (rawType == "KILL" or rawType == "LOCATION")
            and (
                not QPSC_coordinateInputValid(
                    raw.targetX,
                    -10000000,
                    10000000
                )
                or not QPSC_coordinateInputValid(
                    raw.targetY,
                    -10000000,
                    10000000
                )
                or not QPSC_coordinateInputValid(
                    raw.targetZ,
                    -64,
                    64
                )
            ) then
            result.invalidObjectives = true
        end

        if ids[objective.id] then
            result.invalidObjectives = true
        end
        ids[objective.id] = true

        if objective.type == "DELIVERY" then
            if objective.itemFullType == ""
                or QPSC_getScriptItem(objective.itemFullType) == nil then
                result.invalidObjectives = true
            else
                objective.itemDisplayName = QPSC_getScriptDisplayName(
                    objective.itemFullType,
                    objective.itemDisplayName
                )
            end
        end

        table.insert(result.objectives, objective)
    end

    if result.rewardItemFullType ~= "" or result.rewardQuantity > 0 then
        if result.rewardItemFullType == ""
            or result.rewardQuantity < 1
            or QPSC_getScriptItem(result.rewardItemFullType) == nil then
            result.rewardInvalid = true
        else
            result.rewardItemDisplayName = QPSC_getScriptDisplayName(
                result.rewardItemFullType,
                result.rewardItemDisplayName
            )
        end
    end

    if result.firstFinisherBonusItemFullType ~= ""
        or result.firstFinisherBonusQuantity > 0 then
        if result.completionMode == "SHARED_TEAM"
            or result.firstFinisherBonusItemFullType == ""
            or result.firstFinisherBonusQuantity < 1
            or QPSC_getScriptItem(result.firstFinisherBonusItemFullType) == nil then
            result.firstFinisherBonusInvalid = true
        else
            result.firstFinisherBonusItemDisplayName = QPSC_getScriptDisplayName(
                result.firstFinisherBonusItemFullType,
                result.firstFinisherBonusItemDisplayName
            )
        end
    end

    return result
end

local function QPSC_validateCleanMulti(player, clean)
    if not QPSC_validateCleanRequest(player, clean) then
        return false
    end

    if clean.timeLimitInvalid then
        QPSC_sendMessage(player, "The time limit must be a positive number of in-game hours.", "UI_QPSC_MessageInvalidTimeLimit")
        return false
    end
    if clean.invalidObjectives then
        QPSC_sendMessage(player, "A multi-objective contract requires 2 to 5 valid objectives.", "UI_QPSC_MessageInvalidMultiObjectives")
        return false
    end
    if clean.rewardInvalid then
        QPSC_sendMessage(player, "The item reward settings are invalid.", "UI_QPSC_MessageInvalidReward")
        return false
    end
    if clean.reputationInvalid then
        QPSC_sendMessage(
            player,
            "The reputation reward settings are invalid.",
            ""
        )
        return false
    end
    if clean.reputationUnavailable then
        QPSC_sendMessage(
            player,
            "QP Survivor Reputation must be enabled to create a reputation reward.",
            ""
        )
        return false
    end
    if clean.firstFinisherBonusInvalid then
        QPSC_sendMessage(player, "The first finisher bonus settings are invalid.", "UI_QPSC_MessageInvalidFirstFinisherBonus")
        return false
    end
    return true
end

local function QPSC_applyMultiRewardText(contract)
    local rewards = QPSC_rewardItemTexts(contract)

    QPSC_appendReputationRewardTexts(rewards, contract)

    contract.reward =
        #rewards > 0
        and table.concat(rewards, " + ")
        or "None"
end

local function QPSC_addMultiContract(player, args)
    local data = QPSC_getData()
    QPSC_migrateData(data)
    local clean = QPSC_cleanMultiArgs(player, args)
    if not QPSC_validateCleanMulti(player, clean) then return end

    local id = data.nextId or 1
    local contract = {
        id = id,
        category = clean.category,
        difficulty = clean.difficulty,
        completionMode = clean.completionMode,
        multiObjective = true,
        objectives = clean.objectives,
        sharedObjectiveProgress = {},
        globalCompleted = false,
        globalCompletedBy = "",
        globalCompletedAt = 0,
        globalCompletionSource = "",
        sharedProgress = clean.completionMode == "SHARED_TEAM" and 0 or nil,
        sharedCompleted = clean.completionMode == "SHARED_TEAM" and false or nil,
        sharedCompletedBy = clean.completionMode == "SHARED_TEAM" and "" or nil,
        sharedCompletedAt = clean.completionMode == "SHARED_TEAM" and 0 or nil,
        sharedCompletionSource = clean.completionMode == "SHARED_TEAM" and "" or nil,
        title = clean.title,
        description = clean.description,
        location = clean.location,
        reward = "None",
        objectiveType = "MULTI",
        objectiveTarget = #clean.objectives,
        objectiveRadius = 0,
        objectiveItemFullType = "",
        objectiveItemDisplayName = "",
        targetX = math.floor(tonumber(player:getX()) or 0),
        targetY = math.floor(tonumber(player:getY()) or 0),
        targetZ = math.floor(tonumber(player:getZ()) or 0),
        rewardItemFullType = clean.rewardItemFullType,
        rewardItemDisplayName = clean.rewardItemDisplayName,
        rewardQuantity = clean.rewardQuantity,
        rewardItems = clean.rewardItems,
        reputationPath = clean.reputationPath,
        reputationPoints = clean.reputationPoints,
        secondaryReputationPath =
            clean.secondaryReputationPath,
        secondaryReputationPoints =
            clean.secondaryReputationPoints,
        firstFinisherBonusItemFullType = clean.firstFinisherBonusItemFullType,
        firstFinisherBonusItemDisplayName = clean.firstFinisherBonusItemDisplayName,
        firstFinisherBonusQuantity = clean.firstFinisherBonusQuantity,
        firstFinisherWinner = "",
        firstFinisherWonAt = 0,
        status = "Open",
        postedBy = QPSC_getUsername(player),
        acceptedBy = "",
        completedBy = "",
        timeLimitHours = clean.timeLimitHours,
        acceptedAt = 0,
        expiresAt = 0,
        expiredAt = 0,
        closed = false,
        participants = {}
    }

    if clean.completionMode == "SHARED_TEAM" then
        contract.firstFinisherBonusItemFullType = ""
        contract.firstFinisherBonusItemDisplayName = ""
        contract.firstFinisherBonusQuantity = 0
        for _, objective in ipairs(contract.objectives) do
            contract.sharedObjectiveProgress[objective.id] = 0
        end
    end

    QPSC_applyMultiRewardText(contract)
    table.insert(data.contracts, contract)
    data.nextId = id + 1
    QPSC_transmit()
    QPSC_broadcastContracts()
    QPSC_broadcastAnnouncement(
        "A new multi-objective contract has appeared: #" .. tostring(id) .. " - " .. tostring(contract.title or ""),
        "UI_QPSC_AnnouncementNewMultiContract",
        id,
        tostring(contract.title or "")
    )
    QPSC_sendMessage(player, "Multi-objective contract created: #" .. tostring(id), "UI_QPSC_MessageMultiContractCreated", id)
    print("[QPSC] Multi-objective contract created #" .. tostring(id) .. " by " .. QPSC_getUsername(player))
end

local function QPSC_updateMultiContract(player, args)
    args = args or {}
    local data = QPSC_getData()
    QPSC_migrateData(data)
    QPSC_updateParticipantTimers()
    local contract = QPSC_findContractById(data, args.contractId)

    if contract == nil or not QPSC_isMultiObjective(contract) then
        QPSC_sendMessage(player, "Multi-objective contract not found.", "UI_QPSC_MessageContractNotFound")
        return
    end

    local cleanInput = {}
    for key, value in pairs(args) do cleanInput[key] = value end
    local participantCount = #(contract.participants or {})

    if participantCount > 0 then
        cleanInput.completionMode = contract.completionMode
        cleanInput.objectiveCount = #contract.objectives
        for index, objective in ipairs(contract.objectives) do
            local prefix = "objective" .. tostring(index)
            cleanInput[prefix .. "Id"] = objective.id
            cleanInput[prefix .. "Type"] = objective.type
            cleanInput[prefix .. "Target"] = objective.target
            cleanInput[prefix .. "Radius"] = objective.radius
            cleanInput[prefix .. "ItemFullType"] = objective.itemFullType
            cleanInput[prefix .. "ItemDisplayName"] = objective.itemDisplayName
            cleanInput[prefix .. "TargetX"] = objective.targetX
            cleanInput[prefix .. "TargetY"] = objective.targetY
            cleanInput[prefix .. "TargetZ"] = objective.targetZ
        end
    end

    local clean = QPSC_cleanMultiArgs(player, cleanInput)

    if clean.reputationUnavailable then
        local unchangedReputation =
            QPSC_normalizeReputationPath(
                contract.reputationPath
            ) == clean.reputationPath
            and QPSC_normalizePositiveInteger(
                contract.reputationPoints,
                100000
            ) == clean.reputationPoints
            and QPSC_normalizeReputationPath(
                contract.secondaryReputationPath
            ) == clean.secondaryReputationPath
            and QPSC_normalizePositiveInteger(
                contract.secondaryReputationPoints,
                100000
            ) == clean.secondaryReputationPoints

        if unchangedReputation then
            clean.reputationUnavailable = false
        end
    end

    if not QPSC_validateCleanMulti(player, clean) then return end
    local oldTimeLimit = tonumber(contract.timeLimitHours) or 0

    contract.title = clean.title
    contract.location = clean.location
    contract.description = clean.description
    contract.category = clean.category
    contract.difficulty = clean.difficulty

    if participantCount == 0 then
        contract.completionMode = clean.completionMode
        contract.objectives = clean.objectives
        contract.objectiveTarget = #clean.objectives
        contract.sharedObjectiveProgress = {}
        contract.sharedProgress = clean.completionMode == "SHARED_TEAM" and 0 or nil
        contract.sharedCompleted = clean.completionMode == "SHARED_TEAM" and false or nil
        contract.sharedCompletedBy = clean.completionMode == "SHARED_TEAM" and "" or nil
        contract.sharedCompletedAt = clean.completionMode == "SHARED_TEAM" and 0 or nil
        contract.sharedCompletionSource = clean.completionMode == "SHARED_TEAM" and "" or nil
        if clean.completionMode == "SHARED_TEAM" then
            for _, objective in ipairs(contract.objectives) do
                contract.sharedObjectiveProgress[objective.id] = 0
            end
        end
    end

    QPSC_applyContractRewardItems(
        contract,
        clean.rewardItems
    )
    contract.reputationPath = clean.reputationPath
    contract.reputationPoints = clean.reputationPoints
    contract.secondaryReputationPath =
        clean.secondaryReputationPath
    contract.secondaryReputationPoints =
        clean.secondaryReputationPoints
    contract.firstFinisherBonusItemFullType = clean.firstFinisherBonusItemFullType
    contract.firstFinisherBonusItemDisplayName = clean.firstFinisherBonusItemDisplayName
    contract.firstFinisherBonusQuantity = clean.firstFinisherBonusQuantity

    if QPSC_isSharedTeamCompletion(contract) then
        contract.firstFinisherBonusItemFullType = ""
        contract.firstFinisherBonusItemDisplayName = ""
        contract.firstFinisherBonusQuantity = 0
        contract.firstFinisherWinner = ""
        contract.firstFinisherWonAt = 0
    end

    QPSC_applyMultiRewardText(contract)
    contract.timeLimitHours = clean.timeLimitHours

    if oldTimeLimit ~= clean.timeLimitHours then
        local now = QPSC_getWorldAgeHours()
        for _, participant in ipairs(contract.participants or {}) do
            if tostring(participant.status or "") == "Accepted" then
                participant.remainingHours = clean.timeLimitHours
                local key = QPSC_timerKey(contract.id, participant.username)
                QPSC_timerMarks[key] = clean.timeLimitHours > 0 and now or nil
            end
        end
    end

    QPSC_recomputeContractStatus(contract)
    QPSC_notifyOptionalCompletion(contract)
    QPSC_transmit()
    QPSC_broadcastContracts()
    QPSC_broadcastAnnouncement("Contract #" .. tostring(contract.id) .. " was updated by an administrator.", "UI_QPSC_AnnouncementContractUpdated", contract.id)
    QPSC_sendMessage(player, "Contract updated: #" .. tostring(contract.id), "UI_QPSC_MessageContractUpdated", contract.id)
end

local function QPSC_addContract(player, args)
    local data = QPSC_getData()
    QPSC_migrateData(data)

    local clean = QPSC_cleanArgs(args)

    if not QPSC_validateCleanRequest(player, clean) then
        return
    end

    if clean.timeLimitInvalid then
        QPSC_sendMessage(
            player,
            "The time limit must be a positive number of in-game hours.",
            "UI_QPSC_MessageInvalidTimeLimit"
        )
        return
    end

    if clean.objectiveInvalid then
        QPSC_sendMessage(
            player,
            "The tracked objective settings are invalid.",
            "UI_QPSC_MessageInvalidObjective"
        )
        return
    end

    if clean.completionModeInvalid then
        QPSC_sendMessage(
            player,
            "Shared Team is available only for Zombie Hunt contracts.",
            "UI_QPSC_MessageSharedTeamKillOnly"
        )
        return
    end

    if clean.rewardInvalid then
        QPSC_sendMessage(
            player,
            "The item reward settings are invalid.",
            "UI_QPSC_MessageInvalidReward"
        )
        return
    end

    if clean.reputationInvalid then
        QPSC_sendMessage(
            player,
            "The reputation reward settings are invalid.",
            ""
        )
        return
    end

    if clean.reputationUnavailable then
        QPSC_sendMessage(
            player,
            "QP Survivor Reputation must be enabled to create a reputation reward.",
            ""
        )
        return
    end

    if clean.firstFinisherBonusInvalid then
        QPSC_sendMessage(
            player,
            "The first finisher bonus settings are invalid.",
            "UI_QPSC_MessageInvalidFirstFinisherBonus"
        )
        return
    end

    local id = data.nextId or 1

    local contract = {
        id = id,
        category = clean.category,
        difficulty = clean.difficulty,
        completionMode = clean.completionMode,
        globalCompleted = false,
        globalCompletedBy = "",
        globalCompletedAt = 0,
        globalCompletionSource = "",
        sharedProgress = clean.completionMode == "SHARED_TEAM" and 0 or nil,
        sharedCompleted = clean.completionMode == "SHARED_TEAM" and false or nil,
        sharedCompletedBy = clean.completionMode == "SHARED_TEAM" and "" or nil,
        sharedCompletedAt = clean.completionMode == "SHARED_TEAM" and 0 or nil,
        sharedCompletionSource = clean.completionMode == "SHARED_TEAM" and "" or nil,
        title = clean.title,
        description = clean.description,
        location = clean.location,
        reward = clean.reward,
        objectiveType = clean.objectiveType,
        objectiveTarget = clean.objectiveTarget,
        objectiveRadius = clean.objectiveRadius,
        objectiveItemFullType =
            clean.objectiveItemFullType,
        objectiveItemDisplayName =
            clean.objectiveItemDisplayName,
        targetX = math.floor(tonumber(player:getX()) or 0),
        targetY = math.floor(tonumber(player:getY()) or 0),
        targetZ = math.floor(tonumber(player:getZ()) or 0),
        rewardItemFullType =
            clean.rewardItemFullType,
        rewardItemDisplayName =
            clean.rewardItemDisplayName,
        rewardQuantity = clean.rewardQuantity,
        rewardItems = clean.rewardItems,
        reputationPath = clean.reputationPath,
        reputationPoints = clean.reputationPoints,
        secondaryReputationPath =
            clean.secondaryReputationPath,
        secondaryReputationPoints =
            clean.secondaryReputationPoints,
        firstFinisherBonusItemFullType =
            clean.firstFinisherBonusItemFullType,
        firstFinisherBonusItemDisplayName =
            clean.firstFinisherBonusItemDisplayName,
        firstFinisherBonusQuantity =
            clean.firstFinisherBonusQuantity,
        firstFinisherWinner = "",
        firstFinisherWonAt = 0,
        status = "Open",
        postedBy = QPSC_getUsername(player),
        acceptedBy = "",
        completedBy = "",
        timeLimitHours = clean.timeLimitHours,
        acceptedAt = 0,
        expiresAt = 0,
        expiredAt = 0,
        closed = false,
        participants = {}
    }

    local trackedRewards =
        QPSC_rewardItemTexts(contract)

    if #trackedRewards == 0
        and QPSC_normalizeObjectiveType(
            contract.objectiveType
        ) == "MANUAL"
        and tostring(contract.reward or "") ~= ""
        and tostring(contract.reward or "") ~= "None" then
        table.insert(
            trackedRewards,
            tostring(contract.reward)
        )
    end

    QPSC_appendReputationRewardTexts(
        trackedRewards,
        contract
    )

    contract.reward =
        #trackedRewards > 0
        and table.concat(trackedRewards, " + ")
        or "None"

    table.insert(data.contracts, contract)
    data.nextId = id + 1

    QPSC_transmit()
    QPSC_broadcastContracts()
    QPSC_broadcastAnnouncement(
        "A new contract has appeared: #"
            .. tostring(id)
            .. " - "
            .. tostring(contract.title or ""),
        "UI_QPSC_AnnouncementNewContract",
        id,
        tostring(contract.title or "")
    )

    print(
        "[QPSC] Contract created #"
            .. tostring(id)
            .. " by "
            .. QPSC_getUsername(player)
    )
end


-- QPSC_ADMIN_EDIT_CONTRACTS_V070
local function QPSC_contractHasCompletedParticipant(contract)
    for _, participant in ipairs(contract.participants or {}) do
        if tostring(participant.status or "") == "Completed" then
            return true
        end
    end

    return false
end

local function QPSC_updateContract(player, args)
    args = args or {}
    local data = QPSC_getData()
    QPSC_migrateData(data)
    QPSC_updateParticipantTimers()

    local contract = QPSC_findContractById(data, args.contractId)

    if contract == nil then
        QPSC_sendMessage(
            player,
            "Contract not found.",
            "UI_QPSC_MessageContractNotFound"
        )
        return
    end

    if QPSC_isMultiObjective(contract) then
        QPSC_sendMessage(
            player,
            "Use the multi-objective editor for this contract.",
            "UI_QPSC_MessageUseMultiEditor"
        )
        return
    end

    local participantCount = #(contract.participants or {})
    local hasCompleted = QPSC_contractHasCompletedParticipant(contract)
    local cleanInput = {}

    for key, value in pairs(args) do
        cleanInput[key] = value
    end

    if tostring(cleanInput.completionMode or "") == "" then
        cleanInput.completionMode = contract.completionMode
    end

    -- Enforce immutable fields before validation, not only after it. This
    -- prevents a modified client from changing or invalidating locked data.
    if participantCount > 0 then
        cleanInput.objectiveType = contract.objectiveType
        cleanInput.objectiveItemFullType =
            contract.objectiveItemFullType
        cleanInput.objectiveItemDisplayName =
            contract.objectiveItemDisplayName
        cleanInput.completionMode = contract.completionMode
    end

    if hasCompleted then
        cleanInput.objectiveTarget = contract.objectiveTarget
        cleanInput.objectiveRadius = contract.objectiveRadius
    end

    local clean = QPSC_cleanArgs(cleanInput)

    if not QPSC_validateCleanRequest(player, clean) then
        return
    end

    local legacyUnrestrictedHunt =
        QPSC_normalizeObjectiveType(contract.objectiveType) == "KILL"
        and (tonumber(contract.objectiveRadius) or 0) <= 0
        and QPSC_normalizeObjectiveType(cleanInput.objectiveType) == "KILL"
        and (tonumber(cleanInput.objectiveRadius) or 0) <= 0
        and (tonumber(cleanInput.objectiveTarget) or 0) >= 1

    if legacyUnrestrictedHunt then
        clean.objectiveInvalid = false
        clean.objectiveRadius = 0
    end

    if clean.timeLimitInvalid then
        QPSC_sendMessage(
            player,
            "The time limit must be a positive number of in-game hours.",
            "UI_QPSC_MessageInvalidTimeLimit"
        )
        return
    end

    if clean.objectiveInvalid then
        QPSC_sendMessage(
            player,
            "The tracked objective settings are invalid.",
            "UI_QPSC_MessageInvalidObjective"
        )
        return
    end

    if clean.completionModeInvalid then
        QPSC_sendMessage(
            player,
            "Shared Team is available only for Zombie Hunt contracts.",
            "UI_QPSC_MessageSharedTeamKillOnly"
        )
        return
    end

    if clean.rewardInvalid then
        QPSC_sendMessage(
            player,
            "The item reward settings are invalid.",
            "UI_QPSC_MessageInvalidReward"
        )
        return
    end

    if clean.reputationInvalid then
        QPSC_sendMessage(
            player,
            "The reputation reward settings are invalid.",
            ""
        )
        return
    end

    if clean.reputationUnavailable then
        local unchangedReputation =
            QPSC_normalizeReputationPath(
                contract.reputationPath
            ) == clean.reputationPath
            and QPSC_normalizePositiveInteger(
                contract.reputationPoints,
                100000
            ) == clean.reputationPoints
            and QPSC_normalizeReputationPath(
                contract.secondaryReputationPath
            ) == clean.secondaryReputationPath
            and QPSC_normalizePositiveInteger(
                contract.secondaryReputationPoints,
                100000
            ) == clean.secondaryReputationPoints

        if unchangedReputation then
            clean.reputationUnavailable = false
        else
            QPSC_sendMessage(
                player,
                "QP Survivor Reputation must be enabled to configure a reputation reward.",
                ""
            )
            return
        end
    end

    if clean.firstFinisherBonusInvalid then
        QPSC_sendMessage(
            player,
            "The first finisher bonus settings are invalid.",
            "UI_QPSC_MessageInvalidFirstFinisherBonus"
        )
        return
    end

    local oldTimeLimit = tonumber(contract.timeLimitHours) or 0

    contract.title = clean.title
    contract.location = clean.location
    contract.description = clean.description
    contract.category = clean.category
    contract.difficulty = clean.difficulty

    if participantCount == 0 then
        contract.completionMode = clean.completionMode

        if QPSC_isSharedTeamCompletion(contract) then
            contract.sharedProgress = 0
            contract.sharedCompleted = false
            contract.sharedCompletedBy = ""
            contract.sharedCompletedAt = 0
            contract.sharedCompletionSource = ""
        else
            contract.sharedProgress = nil
            contract.sharedCompleted = nil
            contract.sharedCompletedBy = nil
            contract.sharedCompletedAt = nil
            contract.sharedCompletionSource = nil
        end
    end

    -- Once anyone has joined, objective type, delivery item identity, and completion mode are immutable.
    if participantCount == 0 then
        contract.objectiveType = clean.objectiveType
        contract.objectiveItemFullType = clean.objectiveItemFullType
        contract.objectiveItemDisplayName = clean.objectiveItemDisplayName
    end

    -- Once a participant has completed, all objective settings are immutable.
    if not hasCompleted then
        contract.objectiveTarget = clean.objectiveTarget
        contract.objectiveRadius = clean.objectiveRadius
    end

    local objectiveType = QPSC_normalizeObjectiveType(contract.objectiveType)

    if objectiveType == "MANUAL" then
        contract.objectiveTarget = 0
        contract.objectiveRadius = 0
        contract.objectiveItemFullType = ""
        contract.objectiveItemDisplayName = ""
    elseif objectiveType == "LOCATION" then
        contract.objectiveTarget = 1
        contract.objectiveRadius = math.max(
            1,
            math.min(20, tonumber(contract.objectiveRadius) or 3)
        )
    elseif objectiveType == "KILL" then
        contract.objectiveTarget = math.max(
            1,
            tonumber(contract.objectiveTarget) or 1
        )
        local huntRadius = tonumber(contract.objectiveRadius) or 0
        if huntRadius <= 0 then
            contract.objectiveRadius = 0
        else
            contract.objectiveRadius = math.max(
                1,
                math.min(1000, huntRadius)
            )
        end
    elseif objectiveType == "DELIVERY" then
        contract.objectiveTarget = math.max(
            1,
            tonumber(contract.objectiveTarget) or 1
        )
        contract.objectiveRadius = 0
    end

    QPSC_applyContractRewardItems(
        contract,
        clean.rewardItems
    )
    contract.reputationPath = clean.reputationPath
    contract.reputationPoints = clean.reputationPoints
    contract.secondaryReputationPath =
        clean.secondaryReputationPath
    contract.secondaryReputationPoints =
        clean.secondaryReputationPoints
    contract.firstFinisherBonusItemFullType =
        clean.firstFinisherBonusItemFullType
    contract.firstFinisherBonusItemDisplayName =
        clean.firstFinisherBonusItemDisplayName
    contract.firstFinisherBonusQuantity =
        clean.firstFinisherBonusQuantity

    local trackedRewards =
        QPSC_rewardItemTexts(contract)

    if #trackedRewards == 0
        and objectiveType == "MANUAL"
        and tostring(clean.reward or "") ~= ""
        and tostring(clean.reward or "") ~= "None" then
        table.insert(trackedRewards, tostring(clean.reward))
    end

    QPSC_appendReputationRewardTexts(
        trackedRewards,
        contract
    )

    contract.reward =
        #trackedRewards > 0
        and table.concat(trackedRewards, " + ")
        or "None"

    contract.timeLimitHours = clean.timeLimitHours

    if oldTimeLimit ~= clean.timeLimitHours then
        local now = QPSC_getWorldAgeHours()

        for _, participant in ipairs(contract.participants or {}) do
            if tostring(participant.status or "") == "Accepted" then
                participant.remainingHours = clean.timeLimitHours
                local key = QPSC_timerKey(
                    contract.id,
                    participant.username
                )
                QPSC_timerMarks[key] =
                    clean.timeLimitHours > 0 and now or nil
            end
        end
    end

    -- Reward state is intentionally preserved. Editing never grants an item
    -- and cannot reset duplicate-reward protection for existing participants.
    QPSC_recomputeContractStatus(contract)
    QPSC_notifyOptionalCompletion(contract)
    QPSC_transmit()
    QPSC_broadcastContracts()
    QPSC_broadcastAnnouncement(
        "Contract #"
            .. tostring(contract.id)
            .. " was updated by an administrator.",
        "UI_QPSC_AnnouncementContractUpdated",
        contract.id
    )
    QPSC_sendMessage(
        player,
        "Contract updated: #" .. tostring(contract.id),
        "UI_QPSC_MessageContractUpdated",
        contract.id
    )

    print(
        "[QPSC] Contract #"
            .. tostring(contract.id)
            .. " updated by "
            .. QPSC_getUsername(player)
    )
end

local function QPSC_acceptContract(player, contractId)
    local data = QPSC_getData()
    QPSC_migrateData(data)
    QPSC_updateParticipantTimers()

    local username = QPSC_getUsername(player)

    for _, contract in ipairs(data.contracts) do
        if tostring(contract.id) == tostring(contractId) then
            if contract.closed == true then
                QPSC_sendMessage(
                    player,
                    "This contract is not open anymore.",
                    "UI_QPSC_MessageNotOpen"
                )
                return
            end

            local participant =
                QPSC_findParticipant(contract, username)

            if participant ~= nil
                and tostring(participant.status or "")
                    ~= "Cancelled" then
                QPSC_sendMessage(
                    player,
                    "You have already joined this contract.",
                    "UI_QPSC_MessageAlreadyJoined"
                )
                return
            end

            local activeContract =
                QPSC_findActiveContractForUsername(
                    data,
                    username
                )

            if activeContract then
                QPSC_sendMessage(
                    player,
                    "You already have an active contract: "
                        .. QPSC_contractLabel(activeContract),
                    "UI_QPSC_MessageActiveContractExists",
                    QPSC_contractLabel(activeContract)
                )
                return
            end

            local acceptedAt =
                QPSC_getWorldAgeHours()
            local timeLimitHours =
                QPSC_normalizeTimeLimit(
                    contract.timeLimitHours
                )

            if participant == nil then
                participant = {
                    username = username
                }

                table.insert(
                    contract.participants,
                    participant
                )
            end

            participant.status = "Accepted"
            participant.acceptedAt = acceptedAt
            participant.remainingHours = timeLimitHours
            participant.completedAt = 0
            participant.expiredAt = 0
            participant.cancelledAt = 0
            participant.closedAt = 0
            participant.closedBy = ""
            participant.reviewedAt = 0
            participant.reviewedBy = ""
            participant.progress = 0
            if QPSC_isMultiObjective(contract) then
                participant.objectiveProgress = {}
                participant.objectiveContributions = {}
                QPSC_ensureParticipantMultiState(contract, participant)
            end
            participant.rewardGranted = false
            participant.rewardGrantedCount = 0
            participant.rewardGrantedCounts = {}
            participant.rewardGrantedAt = 0
            participant.rewardPending = false

            if timeLimitHours > 0 then
                QPSC_timerMarks[
                    QPSC_timerKey(
                        contract.id,
                        username
                    )
                ] = acceptedAt
            end

            QPSC_recomputeContractStatus(contract)
            QPSC_transmit()
            QPSC_broadcastContracts()
            QPSC_sendMessage(
                player,
                "Contract accepted: #"
                    .. tostring(contract.id),
                "UI_QPSC_MessageContractAccepted",
                contract.id
            )

            print(
                "[QPSC] Contract #"
                    .. tostring(contract.id)
                    .. " joined by "
                    .. username
            )
            return
        end
    end

    QPSC_sendMessage(
        player,
        "Contract not found.",
        "UI_QPSC_MessageContractNotFound"
    )
end

-- QPSC_PLAYER_CANCEL_CONTRACT_V1
local function QPSC_cancelContract(player, contractId)
    local data = QPSC_getData()
    QPSC_migrateData(data)
    QPSC_updateParticipantTimers()

    local username = QPSC_getUsername(player)

    for _, contract in ipairs(data.contracts) do
        if tostring(contract.id) == tostring(contractId) then
            local participant =
                QPSC_findParticipant(contract, username)

            if participant == nil
                or tostring(participant.status or "") ~= "Accepted" then
                QPSC_sendMessage(
                    player,
                    "You do not have an active contract.",
                    "UI_QPSC_MessageNoActiveContract"
                )
                return
            end

            participant.status = "Cancelled"
            participant.cancelledAt =
                QPSC_getWorldAgeHours()

            QPSC_timerMarks[
                QPSC_timerKey(
                    contract.id,
                    participant.username
                )
            ] = nil

            QPSC_recomputeContractStatus(contract)
            QPSC_transmit()
            QPSC_broadcastContracts()
            QPSC_sendMessage(
                player,
                "Contract cancelled: #"
                    .. tostring(contract.id),
                "UI_QPSC_MessageContractCancelled",
                contract.id
            )

            print(
                "[QPSC] Contract #"
                    .. tostring(contract.id)
                    .. " cancelled by "
                    .. username
            )
            return
        end
    end

    QPSC_sendMessage(
        player,
        "Contract not found.",
        "UI_QPSC_MessageContractNotFound"
    )
end

-- QPSC_ADMIN_PARTICIPANT_REVIEW_V1
local function QPSC_reviewParticipant(
    player,
    contractId,
    participantUsername,
    resultStatus
)
    local data = QPSC_getData()
    QPSC_migrateData(data)
    QPSC_updateParticipantTimers()

    resultStatus =
        tostring(resultStatus or "")

    if resultStatus ~= "Completed"
        and resultStatus ~= "NotCompleted" then
        return
    end

    for _, contract in ipairs(data.contracts) do
        if tostring(contract.id) == tostring(contractId) then
            if QPSC_isSharedTeamCompletion(contract) then
                QPSC_sendMessage(
                    player,
                    "Shared Team Zombie Hunts complete automatically and cannot be reviewed per survivor.",
                    "UI_QPSC_MessageSharedTeamAutomatic"
                )
                return
            end

            local participant =
                QPSC_findParticipant(
                    contract,
                    participantUsername
                )

            if participant == nil then
                QPSC_sendMessage(
                    player,
                    "Participant not found.",
                    "UI_QPSC_MessageParticipantNotFound"
                )
                return
            end

            if QPSC_isGlobalCompletion(contract)
                and contract.globalCompleted == true then
                QPSC_sendMessage(
                    player,
                    "This global contract was already completed by "
                        .. tostring(contract.globalCompletedBy or "")
                        .. ".",
                    "UI_QPSC_MessageGlobalAlreadyCompleted",
                    tostring(contract.globalCompletedBy or "")
                )
                return
            end

            local now = QPSC_getWorldAgeHours()
            local reviewer = QPSC_getUsername(player)
            local closedGlobal = false
            local hasFirstFinisherBonus =
                tostring(
                    contract.firstFinisherBonusItemFullType or ""
                ) ~= ""
                and QPSC_normalizePositiveInteger(
                    contract.firstFinisherBonusQuantity,
                    100
                ) > 0
            local isFirstFinisher = false

            participant.status = resultStatus
            participant.reviewedBy = reviewer
            participant.reviewedAt = now
            participant.completedAt =
                resultStatus == "Completed" and now or 0

            if resultStatus == "Completed" then
                -- QPSC_V122_MANUAL_FIRST_FINISHER_COMPLETION_FIX_V1
                if hasFirstFinisherBonus then
                    if tostring(
                        contract.firstFinisherWinner or ""
                    ) == "" then
                        contract.firstFinisherWinner =
                            tostring(participant.username or "")
                        contract.firstFinisherWonAt = now
                        isFirstFinisher = true
                    elseif QPSC_sameUsername(
                        contract.firstFinisherWinner,
                        participant.username
                    ) then
                        isFirstFinisher = true
                    end
                end

                QPSC_awardContractReputation(
                    contract,
                    participant,
                    reviewer
                )

                local participantPlayer =
                    QPSC_findOnlinePlayer(
                        participant.username
                    )
                local participantSquare = nil

                if participantPlayer ~= nil then
                    participantSquare =
                        QPSC_getPlayerSquare(participantPlayer)

                    QPSC_spawnRewardItems(
                        contract,
                        participant,
                        participantPlayer,
                        participantSquare
                    )
                elseif tostring(
                    contract.rewardItemFullType or ""
                ) ~= "" then
                    participant.rewardPending = true
                end

                if isFirstFinisher then
                    QPSC_trySpawnFirstFinisherBonusForParticipant(
                        contract,
                        participant,
                        participantPlayer,
                        participantSquare,
                        reviewer
                    )
                end

                if hasFirstFinisherBonus
                    and participant.firstFinisherBonusGranted ~= true
                    and QPSC_sameUsername(
                        contract.firstFinisherWinner,
                        participant.username
                    ) then
                    QPSC_trySpawnFirstFinisherBonusForParticipant(
                        contract,
                        participant,
                        participantPlayer,
                        participantSquare,
                        reviewer
                    )
                end

                closedGlobal = QPSC_closeGlobalContract(
                    contract,
                    participant,
                    reviewer
                )
            else
                participant.rewardPending = false
                participant.firstFinisherBonusPending = false
            end

            QPSC_timerMarks[
                QPSC_timerKey(
                    contract.id,
                    participant.username
                )
            ] = nil

            QPSC_recomputeContractStatus(contract)
            QPSC_transmit()
            QPSC_broadcastContracts()

            if closedGlobal then
                QPSC_broadcastAnnouncement(
                    "Contract #"
                        .. tostring(contract.id)
                        .. " was completed by "
                        .. tostring(participant.username)
                        .. ".",
                    "UI_QPSC_AnnouncementGlobalContractCompleted",
                    contract.id,
                    tostring(participant.username)
                )
            end

            local messageKey =
                resultStatus == "Completed"
                and "UI_QPSC_MessageParticipantCompleted"
                or "UI_QPSC_MessageParticipantNotCompleted"

            QPSC_sendMessage(
                player,
                tostring(participant.username)
                    .. " marked "
                    .. resultStatus
                    .. ".",
                messageKey,
                tostring(participant.username)
            )

            print(
                "[QPSC] Contract #"
                    .. tostring(contract.id)
                    .. " participant "
                    .. tostring(participant.username)
                    .. " marked "
                    .. resultStatus
                    .. " by "
                    .. reviewer
            )
            return
        end
    end

    QPSC_sendMessage(
        player,
        "Contract not found.",
        "UI_QPSC_MessageContractNotFound"
    )
end

-- Compatibility handler for clients that still send the old
-- whole-contract completion command.
local function QPSC_completeContract(player, contractId)
    local data = QPSC_getData()
    QPSC_migrateData(data)

    for _, contract in ipairs(data.contracts) do
        if tostring(contract.id) == tostring(contractId) then
            local participant = nil

            for _, candidate in ipairs(
                contract.participants or {}
            ) do
                if candidate.status == "Accepted" then
                    participant = candidate
                    break
                end
            end

            if participant == nil then
                QPSC_sendMessage(
                    player,
                    "No active participant was found.",
                    "UI_QPSC_MessageParticipantNotFound"
                )
                return
            end

            QPSC_reviewParticipant(
                player,
                contractId,
                participant.username,
                "Completed"
            )
            return
        end
    end

    QPSC_sendMessage(
        player,
        "Contract not found.",
        "UI_QPSC_MessageContractNotFound"
    )
end

local function QPSC_deleteContract(player, contractId)
    local data = QPSC_getData()
    QPSC_migrateData(data)

    for index, contract in ipairs(data.contracts) do
        if tostring(contract.id) == tostring(contractId) then
            local deletedId = contract.id

            for _, participant in ipairs(
                contract.participants or {}
            ) do
                QPSC_timerMarks[
                    QPSC_timerKey(
                        contract.id,
                        participant.username
                    )
                ] = nil
            end

            table.remove(data.contracts, index)

            QPSC_transmit()
            QPSC_broadcastContracts()
            QPSC_sendMessage(
                player,
                "Contract deleted: #"
                    .. tostring(deletedId),
                "UI_QPSC_MessageContractDeleted",
                deletedId
            )

            print(
                "[QPSC] Contract #"
                    .. tostring(deletedId)
                    .. " deleted by "
                    .. QPSC_getUsername(player)
            )
            return
        end
    end

    QPSC_sendMessage(
        player,
        "Contract not found.",
        "UI_QPSC_MessageContractNotFound"
    )
end

local function QPSC_clearContracts(player)
    local data = QPSC_getData()

    data.contracts = {}
    data.nextId = 1
    data.schemaVersion = SCHEMA_VERSION
    QPSC_timerMarks = {}

    QPSC_transmit()
    QPSC_broadcastContracts()
    QPSC_sendMessage(
        player,
        "All contracts cleared.",
        "UI_QPSC_MessageAllCleared"
    )

    print(
        "[QPSC] All contracts cleared by "
            .. QPSC_getUsername(player)
    )
end

-- QPSC_V122_CLIENT_VERIFIED_MULTI_KILL_FALLBACK_V1
local function QPSC_reportMultiObjectiveZombieKill(
    player,
    args
)
    if player == nil or type(args) ~= "table" then
        return false
    end

    local contractId = tostring(args.contractId or "")
    local objectiveId = tostring(args.objectiveId or "")
    local expectedProgress = tonumber(args.expectedProgress)
    local zombieX = tonumber(args.zombieX)
    local zombieY = tonumber(args.zombieY)
    local zombieZ = tonumber(args.zombieZ)

    if contractId == "" or objectiveId == "" then
        return false
    end

    if not QPSC_isFiniteNumber(expectedProgress)
        or expectedProgress < 0
        or expectedProgress ~= math.floor(expectedProgress)
        or expectedProgress > 1000000 then
        return false
    end

    if not QPSC_isFiniteNumber(zombieX)
        or not QPSC_isFiniteNumber(zombieY)
        or not QPSC_isFiniteNumber(zombieZ) then
        return false
    end

    local data = QPSC_getData()
    QPSC_migrateData(data)

    local contract, participant =
        QPSC_findActiveContractForUsername(
            data,
            QPSC_getUsername(player)
        )

    if contract == nil
        or participant == nil
        or tostring(contract.id or "") ~= contractId
        or not QPSC_isMultiObjective(contract) then
        return false
    end

    local objective = nil

    for _, candidate in ipairs(contract.objectives or {}) do
        if tostring(candidate.id or "") == objectiveId
            and tostring(candidate.type or "") == "KILL" then
            objective = candidate
            break
        end
    end

    if objective == nil then
        return false
    end

    local currentProgress =
        QPSC_getMultiProgress(
            contract,
            participant,
            objective
        )
    local target = math.max(
        1,
        tonumber(objective.target) or 1
    )

    -- The client report is delayed. If the normal server death event
    -- already advanced this objective, the expected value is stale and
    -- the fallback must not award a duplicate.
    if currentProgress ~= expectedProgress
        or currentProgress >= target then
        return false
    end

    local targetX = tonumber(objective.targetX) or 0
    local targetY = tonumber(objective.targetY) or 0
    local targetZ = tonumber(objective.targetZ) or 0
    local radius = math.max(
        1,
        tonumber(objective.radius) or 100
    )

    local objectiveDx = zombieX - targetX
    local objectiveDy = zombieY - targetY
    local objectiveDz = math.abs(zombieZ - targetZ)

    if objectiveDz > 1
        or ((objectiveDx * objectiveDx)
            + (objectiveDy * objectiveDy))
            > (radius * radius) then
        return false
    end

    local playerX = nil
    local playerY = nil
    local playerZ = nil
    local playerPositionOk = pcall(function()
        playerX = tonumber(player:getX())
        playerY = tonumber(player:getY())
        playerZ = tonumber(player:getZ())
    end)

    if not playerPositionOk
        or not QPSC_isFiniteNumber(playerX)
        or not QPSC_isFiniteNumber(playerY)
        or not QPSC_isFiniteNumber(playerZ) then
        return false
    end

    -- Supports ranged weapons while preventing arbitrary remote reports.
    local playerDx = zombieX - playerX
    local playerDy = zombieY - playerY
    local playerDz = math.abs(zombieZ - playerZ)
    local maxPlayerDistance = 50

    if playerDz > 1
        or ((playerDx * playerDx)
            + (playerDy * playerDy))
            > (maxPlayerDistance * maxPlayerDistance) then
        return false
    end

    QPSC_advanceMultiObjective(
        contract,
        participant,
        objective,
        1,
        player,
        QPSC_getPlayerSquare(player),
        "Client-Verified Multi-Objective Zombie Hunt"
    )

    print(
        "[QPSC] Client-verified zombie kill fallback accepted for "
            .. QPSC_getUsername(player)
            .. " | contract #"
            .. tostring(contract.id or "?")
            .. " | objective "
            .. objectiveId
            .. " | progress "
            .. tostring(currentProgress)
            .. " -> "
            .. tostring(
                QPSC_getMultiProgress(
                    contract,
                    participant,
                    objective
                )
            )
    )

    return true
end

local function QPSC_onClientCommand(
    module,
    command,
    player,
    args
)
    if module ~= MODULE then return end
    args = args or {}

    local requestValid, requestReason =
        QPSC_validateFlatRequest(args)

    if not requestValid then
        local username = QPSC_getUsername(player)

        QPSC_sendMessage(
            player,
            "Request rejected: "
                .. tostring(requestReason or "invalid payload")
                .. ".",
            ""
        )

        print(
            "[QPSC] Rejected command "
                .. tostring(command or "")
                .. " from "
                .. username
                .. ": "
                .. tostring(requestReason or "invalid payload")
        )
        return
    end

    if command == "ReportRewardPosition" then
        if QPSC_storeReportedRewardPosition(player, args) then
            QPSC_tryGrantPendingRewards(player)
        end
        return
    end

    if command == "ReportMultiZombieKill" then
        QPSC_reportMultiObjectiveZombieKill(
            player,
            args
        )
        return
    end

    if command == "ReportImmediateLocation" then
        QPSC_reportImmediateLocationCheck(
            player,
            args
        )
        return
    end

    if command == "RequestContracts" then
        QPSC_updateParticipantTimers()
        QPSC_tryGrantPendingRewards(player)
        QPSC_sendContracts(player)
        return
    end

    if command == "AddMultiContract" then
        if not QPSC_isPrivileged(player) then
            QPSC_sendMessage(player, "Only admins can create contracts.", "UI_QPSC_MessageOnlyAdminsCreate")
            return
        end
        QPSC_addMultiContract(player, args)
        return
    end

    if command == "UpdateMultiContract" then
        if not QPSC_isPrivileged(player) then
            QPSC_sendMessage(player, "Only admins can edit contracts.", "UI_QPSC_MessageOnlyAdminsEdit")
            return
        end
        QPSC_updateMultiContract(player, args)
        return
    end

    if command == "AddContract" then
        if not QPSC_isPrivileged(player) then
            QPSC_sendMessage(
                player,
                "Only admins can create contracts.",
                "UI_QPSC_MessageOnlyAdminsCreate"
            )
            return
        end

        QPSC_addContract(player, args)
        return
    end

    if command == "AcceptContract" then
        QPSC_acceptContract(player, args.contractId)
        return
    end

    if command == "UpdateContract" then
        if not QPSC_isPrivileged(player) then
            QPSC_sendMessage(
                player,
                "Only admins can edit contracts.",
                "UI_QPSC_MessageOnlyAdminsEdit"
            )
            return
        end

        QPSC_updateContract(player, args)
        return
    end

    if command == "CancelContract" then
        QPSC_cancelContract(player, args.contractId)
        return
    end

    if command == "SubmitDelivery" then
        QPSC_submitDelivery(player, args.contractId)
        return
    end

    if command == "DeliveryConsumptionAck" then
        QPSC_confirmDeliveryConsumption(player, args)
        return
    end

    if command == "ReviewParticipant" then
        if not QPSC_isPrivileged(player) then
            QPSC_sendMessage(
                player,
                "Only admins can review participants.",
                "UI_QPSC_MessageOnlyAdminsReview"
            )
            return
        end

        QPSC_reviewParticipant(
            player,
            args.contractId,
            args.participantUsername,
            args.resultStatus
        )
        return
    end

    if command == "CompleteContract" then
        if not QPSC_isPrivileged(player) then
            QPSC_sendMessage(
                player,
                "Only admins can complete contracts.",
                "UI_QPSC_MessageOnlyAdminsComplete"
            )
            return
        end

        QPSC_completeContract(player, args.contractId)
        return
    end

    if command == "DeleteContract" then
        if not QPSC_isPrivileged(player) then
            QPSC_sendMessage(
                player,
                "Only admins can delete contracts.",
                "UI_QPSC_MessageOnlyAdminsDelete"
            )
            return
        end

        QPSC_deleteContract(player, args.contractId)
        return
    end

    if command == "ClearContracts" then
        if not QPSC_isPrivileged(player) then
            QPSC_sendMessage(
                player,
                "Only admins can clear contracts.",
                "UI_QPSC_MessageOnlyAdminsClear"
            )
            return
        end

        QPSC_clearContracts(player)
        return
    end
end

local data = QPSC_getData()
local QPSC_startupMigrationApplied =
    QPSC_migrateData(data)

if QPSC_startupMigrationApplied then
    QPSC_transmit()
end

QPSC_runSaveDiagnostics(
    data,
    QPSC_startupMigrationApplied
)

-- QPSC_DEFER_INITIAL_TIMER_SCAN_V1
-- Build 42 local-host initialization can load this file before
-- GameServer.udpEngine exists. Calling getOnlinePlayers() here creates a
-- red startup error. Defer the first timer scan until the server-started
-- event; normal command and periodic scans remain unchanged.
local QPSC_initialTimerScanComplete = false

local function QPSC_runInitialTimerScan()
    if QPSC_initialTimerScanComplete then
        return
    end

    QPSC_initialTimerScanComplete = true
    QPSC_updateParticipantTimers()
end

QPSC_ServerRuntime = QPSC_ServerRuntime or {}

if QPSC_ServerRuntime.eventsRegisteredV070 ~= true then
    QPSC_ServerRuntime.eventsRegisteredV070 = true
    Events.OnClientCommand.Add(QPSC_onClientCommand)

    if Events.OnServerStarted then
        Events.OnServerStarted.Add(
            QPSC_runInitialTimerScan
        )
    end

    if Events.OnTick then
        Events.OnTick.Add(QPSC_advanceRewardPositionTick)
    end

    if Events.EveryOneMinute then
        Events.EveryOneMinute.Add(
            QPSC_updateParticipantTimers
        )
        Events.EveryOneMinute.Add(
            QPSC_updateLocationObjectives
        )
        Events.EveryOneMinute.Add(
            QPSC_CO_bridgeScan
        )
    elseif Events.EveryTenMinutes then
        Events.EveryTenMinutes.Add(
            QPSC_updateParticipantTimers
        )
        Events.EveryTenMinutes.Add(
            QPSC_updateLocationObjectives
        )
        Events.EveryTenMinutes.Add(
            QPSC_CO_bridgeScan
        )
    end

    if Events.OnZombieDead then
        Events.OnZombieDead.Add(QPSC_onZombieDead)
    end
end

print("[QPSC] Build 41 server loaded v1.3.2 Production.")
