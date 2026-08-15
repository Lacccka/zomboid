PPO = PPO or {}
PPO.SupplementDefinitions = PPO.SupplementDefinitions or {}

local Definitions = PPO.SupplementDefinitions
local MODULE = "PhysicalProgressionOverhaul."

Definitions.KINDS = { "protein", "creatine", "anabolic", "cardio" }

-- One nominal serving is the item's UseDelta share of a full container. Credit
-- follows the confirmed fraction, so a portion larger than one serving grants
-- proportionally more and the reservoir clamp is the only ceiling.
-- Protein and creatine are deliberately serving-for-serving identical: the two
-- reservoirs weigh the same in the multiplier, so charging five times as many
-- tubs for the creatine share only made the two feel arbitrary. The homemade
-- blend stays half strength because that gap is about crafting, not about kind.
local FOOD = {
    [MODULE .. "ProteinPowder"] = { kind = "protein", unit = 0.20, credit = 0.75 },
    [MODULE .. "CreatineComplex"] = { kind = "creatine", unit = 0.20, credit = 0.75 },
}

local FLUID = {
    ProteinShake = { kind = "protein", unit = 0.50, credit = 1.00 },
    HomemadeProteinShake = { kind = "protein", unit = 0.50, credit = 0.50 },
    CreatineDrink = { kind = "creatine", unit = 0.50, credit = 1.00 },
    HomemadeCreatineDrink = { kind = "creatine", unit = 0.50, credit = 0.50 },
}

local DRAINABLE = {
    [MODULE .. "AnabolicPreparation"] = { kind = "anabolic", unit = 0.50, credit = 0.22 },
    [MODULE .. "CardioPreparation"] = { kind = "cardio", unit = 0.50, credit = 0.22 },
}

local function positiveFinite(value)
    return type(value) == "number" and value == value
        and value ~= math.huge and value ~= -math.huge and value > 0
end

local function kindOf(rows, key)
    local row = type(key) == "string" and rows[key] or nil
    if row == nil then return nil end
    return row.kind
end

local function creditFrom(row, amount)
    if row == nil or not positiveFinite(amount) then return 0 end
    return amount * row.credit / row.unit
end

function Definitions.foodKind(fullType) return kindOf(FOOD, fullType) end
function Definitions.fluidKind(fluidName) return kindOf(FLUID, fluidName) end
function Definitions.drainableKind(fullType) return kindOf(DRAINABLE, fullType) end

-- Fluids of the same kind differ in strength, so fluid credit is keyed by the
-- exact fluid name rather than by kind.
function Definitions.fluidCredit(fluidName, amount)
    return creditFrom(type(fluidName) == "string" and FLUID[fluidName] or nil,
        amount)
end

local SOURCES = { food = FOOD, drainable = DRAINABLE }

function Definitions.credit(kind, source, amount)
    local rows = SOURCES[source]
    if rows == nil then return 0 end
    for _, row in pairs(rows) do
        if row.kind == kind then return creditFrom(row, amount) end
    end
    return 0
end

return Definitions
