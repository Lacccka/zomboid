PPO = PPO or {}
PPO.NutritionState = PPO.NutritionState or {}

local NutritionState = PPO.NutritionState
local DEFAULT_DURATION_HOURS = 24

local function finiteOr(value, fallback)
    if type(value) ~= "number" or value ~= value
            or value == math.huge or value == -math.huge then
        return fallback
    end
    return value
end

local function clamp(value, minimum, maximum)
    if value < minimum then return minimum end
    if value > maximum then return maximum end
    return value
end

function NutritionState.new()
    return { foodContribution = 0 }
end

function NutritionState.normalize(source)
    local nutrition = source
    if type(nutrition) ~= "table" then
        nutrition = NutritionState.new()
    end
    nutrition.foodContribution = clamp(
        finiteOr(nutrition.foodContribution, 0), 0, 1)
    return nutrition
end

function NutritionState.addContribution(source, credit)
    local nutrition = NutritionState.normalize(source)
    local amount = finiteOr(credit, 0)
    if amount <= 0 then return nutrition end
    nutrition.foodContribution = clamp(
        nutrition.foodContribution + amount, 0, 1)
    return nutrition
end

function NutritionState.advance(source, elapsedActiveMinutes, durationHours)
    local nutrition = NutritionState.normalize(source)
    local elapsed = math.max(0, finiteOr(elapsedActiveMinutes, 0))
    local hours = clamp(finiteOr(
        durationHours, DEFAULT_DURATION_HOURS), 6, 336)
    nutrition.foodContribution = math.max(0,
        nutrition.foodContribution - elapsed / (hours * 60))
    return nutrition
end
