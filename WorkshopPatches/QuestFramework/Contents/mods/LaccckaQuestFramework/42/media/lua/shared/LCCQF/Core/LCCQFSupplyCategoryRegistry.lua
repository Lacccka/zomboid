require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Registry = LCCQF.SupplyCategoryRegistry or {}
local definitions = Registry.definitions or {}

local UNIT_KINDS = {
    ITEM = true,
    USE = true,
    ROUND = true,
    LITER = true,
    PORTION = true,
    CUSTOM = true,
}

local MEASURE_KINDS = {
    ITEM = true,
    CUSTOM = true,
}

local function validId(value)
    return type(value) == "string" and value ~= "" and #value <= C.MAX_IDENTIFIER_LENGTH
end

local function finiteNumber(value)
    local number = tonumber(value)
    if number == nil or number ~= number or number == math.huge or number == -math.huge then return nil end
    return number
end

local function itemFullType(item)
    if not item or not item.getFullType then return nil end
    local ok, value = pcall(function() return item:getFullType() end)
    return ok and type(value) == "string" and value ~= "" and value or nil
end

local function validateMatch(match)
    if type(match) ~= "table" or not validId(match.kind) then return false, "invalid category match" end
    if match.kind == "food" then return true end
    if match.kind == "fullTypeSet" then
        if type(match.fullTypes) ~= "table" then return false, "fullTypeSet requires fullTypes" end
        local count = 0
        for fullType, enabled in pairs(match.fullTypes) do
            if type(fullType) ~= "string" or fullType == "" or enabled ~= true then
                return false, "invalid fullTypeSet entry"
            end
            count = count + 1
        end
        return count > 0, count > 0 and nil or "fullTypeSet cannot be empty"
    end
    if match.kind == "fullTypePrefix" then
        if type(match.prefixes) ~= "table" or #match.prefixes < 1 then
            return false, "fullTypePrefix requires prefixes"
        end
        for _, prefix in ipairs(match.prefixes) do
            if type(prefix) ~= "string" or prefix == "" or #prefix > C.MAX_IDENTIFIER_LENGTH * 2 then
                return false, "invalid fullTypePrefix entry"
            end
        end
        return true
    end
    return false, "unsupported category match kind"
end

local function normalizeQuantityDefinition(quantity)
    quantity = type(quantity) == "table" and quantity or {}
    local unitKind = tostring(quantity.unitKind or "ITEM")
    local measureKind = tostring(quantity.measureKind or "ITEM")
    local precision = math.floor(tonumber(quantity.precision) or 0)
    local splittable = quantity.splittable == true

    if UNIT_KINDS[unitKind] ~= true then return nil, "unsupported quantity unitKind" end
    if MEASURE_KINDS[measureKind] ~= true then return nil, "unsupported quantity measureKind" end
    if precision < 0 or precision > 6 then return nil, "quantity precision must be between 0 and 6" end
    if measureKind == "CUSTOM" and type(quantity.measureFn) ~= "function" then
        return nil, "CUSTOM quantity measureKind requires measureFn"
    end
    if measureKind == "ITEM" then
        precision = 0
        splittable = false
    end

    return {
        unitKind = unitKind,
        measureKind = measureKind,
        precision = precision,
        splittable = splittable,
        measureFn = measureKind == "CUSTOM" and quantity.measureFn or nil,
    }
end

local function normalizeMeasuredValue(quantity, value)
    local number = finiteNumber(value)
    if number == nil or number <= 0 then return 0 end
    local precision = math.max(0, math.min(6, math.floor(tonumber(quantity and quantity.precision) or 0)))
    if precision == 0 then return math.max(0, math.floor(number + 0.0000001)) end
    local scale = 10 ^ precision
    return math.max(0, math.floor((number * scale) + 0.5) / scale)
end

function Registry.Register(definition)
    if type(definition) ~= "table" then return false, "category definition must be a table" end
    if not validId(definition.categoryId) then return false, "invalid categoryId" end
    if definitions[definition.categoryId] then return false, "duplicate categoryId" end
    local ok, err = validateMatch(definition.match)
    if not ok then return false, err end
    local quantity, quantityError = normalizeQuantityDefinition(definition.quantity)
    if not quantity then return false, quantityError end

    definitions[definition.categoryId] = {
        categoryId = definition.categoryId,
        match = definition.match,
        quantity = quantity,
    }
    return true
end

function Registry.Get(categoryId)
    return validId(categoryId) and definitions[categoryId] or nil
end

function Registry.IsRegistered(categoryId)
    return Registry.Get(categoryId) ~= nil
end

local function matches(definition, item)
    local match = definition and definition.match or nil
    if not match then return false end

    if match.kind == "food" then
        if not item or not item.IsFood then return false end
        local ok, value = pcall(function() return item:IsFood() end)
        return ok and value == true
    end

    local fullType = itemFullType(item)
    if not fullType then return false end
    if match.kind == "fullTypeSet" then
        return match.fullTypes[fullType] == true
    end
    if match.kind == "fullTypePrefix" then
        for _, prefix in ipairs(match.prefixes) do
            if string.sub(fullType, 1, #prefix) == prefix then return true end
        end
    end
    return false
end

local function measure(definition, item)
    if not definition or not matches(definition, item) then return 0 end
    local quantity = definition.quantity or { measureKind = "ITEM", precision = 0 }
    if quantity.measureKind == "ITEM" then return 1 end
    if quantity.measureKind == "CUSTOM" and type(quantity.measureFn) == "function" then
        local ok, value = pcall(quantity.measureFn, item)
        if not ok then return 0 end
        return normalizeMeasuredValue(quantity, value)
    end
    return 0
end

-- Shared category predicate used by physical stock observation, transactional
-- consumption and future trader/economy systems. Callers never reimplement the
-- semantic meaning of a category such as food/medicine/ammunition.
function Registry.Matches(categoryId, item)
    local definition = Registry.Get(categoryId)
    return definition ~= nil and matches(definition, item) or false
end

-- Returns economic quantity represented by one physical InventoryItem. A matching
-- category does not imply one unit: future categories may measure rounds, liters,
-- portions, doses or other custom quantities.
function Registry.Measure(categoryId, item)
    local definition = Registry.Get(categoryId)
    return definition and measure(definition, item) or 0
end

function Registry.NormalizeQuantity(categoryId, value)
    local definition = Registry.Get(categoryId)
    return definition and normalizeMeasuredValue(definition.quantity, value) or 0
end

function Registry.GetQuantitySemantics(categoryId)
    local definition = Registry.Get(categoryId)
    local quantity = definition and definition.quantity or nil
    if not quantity then return nil end
    return {
        unitKind = quantity.unitKind,
        measureKind = quantity.measureKind,
        precision = quantity.precision,
        splittable = quantity.splittable == true,
    }
end

-- Current destructive executor removes whole InventoryItem objects. Until a category
-- gets a category-specific split/mutation executor, only exact ITEM semantics are safe.
function Registry.SupportsWholeItemConsumption(categoryId)
    local semantics = Registry.GetQuantitySemantics(categoryId)
    return semantics ~= nil
        and semantics.unitKind == "ITEM"
        and semantics.measureKind == "ITEM"
        and semantics.precision == 0
        and semantics.splittable ~= true
end

function Registry.Classify(item)
    local out = {}
    for categoryId, definition in pairs(definitions) do
        local amount = measure(definition, item)
        if amount > 0 then out[categoryId] = amount end
    end
    return out
end

function Registry.List()
    local out = {}
    for _, definition in pairs(definitions) do out[#out + 1] = definition end
    table.sort(out, function(a, b) return a.categoryId < b.categoryId end)
    return out
end

Registry.definitions = definitions
LCCQF.SupplyCategoryRegistry = Registry
return Registry
