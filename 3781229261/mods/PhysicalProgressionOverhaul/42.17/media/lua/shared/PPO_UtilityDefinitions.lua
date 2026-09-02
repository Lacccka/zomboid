require "PPO_Num"

PPO = PPO or {}
PPO.UtilityDefinitions = PPO.UtilityDefinitions or {}

local Definitions = PPO.UtilityDefinitions
local Num = PPO.Num
local MODULE = "PhysicalProgressionOverhaul."

-- Class B utility content. Deliberately a separate table from
-- PPO_SupplementDefinitions: that module is the multiplier's whitelist, and a
-- contract pins Class B out of it. A kind here names no reservoir and no
-- course, only a persisted window.
local FOOD = {
    [MODULE .. "PreWorkoutStimulant"] = { kind = "stimulant", unit = 0.20 },
}

local FLUID = {
    PreWorkoutDrink = { kind = "stimulant", unit = 0.50 },
    HomemadePreWorkoutDrink = { kind = "stimulant", unit = 1.00 },
}

-- The thermogenic is a five-dose drainable, so it travels the pill seam rather
-- than the food one. UseDelta is 0.2, which makes one dose exactly one nominal
-- serving.
local DRAINABLE = {
    [MODULE .. "ThermogenicComplex"] = { kind = "thermogenic", unit = 0.20 },
}

local function kindOf(rows, key)
    local row = type(key) == "string" and rows[key] or nil
    if row == nil then return nil end
    return row.kind
end

-- Servings, not credit: the stimulant has no reservoir to fill. One nominal
-- unit is one serving, and a larger portion is proportionally more.
local function servingsFrom(row, amount)
    if row == nil or not Num.isPositive(amount) then return 0 end
    return amount / row.unit
end

function Definitions.foodKind(fullType) return kindOf(FOOD, fullType) end
function Definitions.fluidKind(fluidName) return kindOf(FLUID, fluidName) end

function Definitions.drainableKind(fullType)
    return kindOf(DRAINABLE, fullType)
end

function Definitions.fluidServings(fluidName, amount)
    return servingsFrom(
        type(fluidName) == "string" and FLUID[fluidName] or nil, amount)
end

local SOURCES = { food = FOOD, drainable = DRAINABLE }

function Definitions.servings(kind, source, amount)
    local rows = SOURCES[source]
    if rows == nil then return 0 end
    for _, row in pairs(rows) do
        if row.kind == kind then return servingsFrom(row, amount) end
    end
    return 0
end

return Definitions
