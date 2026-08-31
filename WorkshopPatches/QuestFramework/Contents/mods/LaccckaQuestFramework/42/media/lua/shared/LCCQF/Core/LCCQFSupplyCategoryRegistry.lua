require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Registry = LCCQF.SupplyCategoryRegistry or {}
local definitions = Registry.definitions or {}

local function validId(value)
    return type(value) == "string" and value ~= "" and #value <= C.MAX_IDENTIFIER_LENGTH
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

function Registry.Register(definition)
    if type(definition) ~= "table" then return false, "category definition must be a table" end
    if not validId(definition.categoryId) then return false, "invalid categoryId" end
    if definitions[definition.categoryId] then return false, "duplicate categoryId" end
    local ok, err = validateMatch(definition.match)
    if not ok then return false, err end

    definitions[definition.categoryId] = {
        categoryId = definition.categoryId,
        match = definition.match,
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

-- Shared category predicate used by physical stock observation, transactional
-- consumption and future trader/economy systems. Callers never reimplement the
-- semantic meaning of a category such as food/medicine/ammunition.
function Registry.Matches(categoryId, item)
    local definition = Registry.Get(categoryId)
    return definition ~= nil and matches(definition, item) or false
end

function Registry.Classify(item)
    local out = {}
    for categoryId, definition in pairs(definitions) do
        if matches(definition, item) then out[categoryId] = 1 end
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
