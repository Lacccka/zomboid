require "ExtractionMode/Config"
require "ExtractionMode/Upgrades"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Upgrades = ExtractionMode.Upgrades
local Generator = {}

function Generator.capacity()
    return math.max(1, tonumber(Config.value("GeneratorTankCapacityLiters")) or 40)
end

function Generator.transferLimit()
    return math.max(0.1, tonumber(Config.value("GeneratorTransferLiters")) or 5)
end

function Generator.fuelReduction(completed)
    local reduction = 0
    for _, definition in ipairs(Upgrades.definitions()) do
        if Upgrades.isInstalled(completed, definition.id) and definition.generatorFuelReduction then
            reduction = reduction + math.max(0, tonumber(definition.generatorFuelReduction) or 0)
        end
    end
    return reduction
end

function Generator.fuelPerDay(completed)
    local base = math.max(0.1, tonumber(Config.value("GeneratorFuelPerDay")) or 4)
    return math.max(1, base - Generator.fuelReduction(completed))
end

function Generator.fuelPerHour(completed, usageMultiplier)
    local multiplier = math.max(0, tonumber(usageMultiplier) or 1)
    return Generator.fuelPerDay(completed) * multiplier / 24
end

function Generator.hoursRemaining(fuel, completed, usageMultiplier)
    local rate = Generator.fuelPerHour(completed, usageMultiplier)
    if rate <= 0 then return 0 end
    return math.max(0, tonumber(fuel) or 0) / rate
end

local function gasolineAmount(item)
    local container = item and item:getFluidContainer()
    if container == nil or not container:contains(Fluid.Petrol) then return 0 end
    -- Do not silently discard another liquid from a mixed container. Vanilla
    -- gasoline cans and bottles are pure Petrol and modded containers using the
    -- same fluid work without an item-type allowlist.
    if not container:isPureFluid(Fluid.Petrol) then return 0 end
    return math.max(0, tonumber(container:getAmount()) or 0)
end

function Generator.gasolineItems(inventory)
    if inventory == nil then return nil end
    return inventory:getAllEvalRecurse(function(item) return gasolineAmount(item) > 0.0001 end)
end

function Generator.availableGasoline(inventory)
    local items = Generator.gasolineItems(inventory)
    if items == nil then return 0 end
    local amount = 0
    for index = 0, items:size() - 1 do amount = amount + gasolineAmount(items:get(index)) end
    return amount
end

-- Called only by the authoritative server after validating the player and tank.
function Generator.consumeGasoline(inventory, requested)
    local remaining = math.max(0, tonumber(requested) or 0)
    local consumed = 0
    local items = Generator.gasolineItems(inventory)
    if items == nil then return 0 end

    for index = 0, items:size() - 1 do
        if remaining <= 0.0001 then break end
        local item = items:get(index)
        local container = item and item:getFluidContainer()
        local available = gasolineAmount(item)
        local take = math.min(available, remaining)
        if container and take > 0 then
            container:adjustAmount(math.max(0, available - take))
            pcall(function() container:unsealIfNotFull() end)
            if sendItemStats then sendItemStats(item) end
            consumed = consumed + take
            remaining = remaining - take
        end
    end
    return consumed
end

ExtractionMode.Generator = Generator
return Generator
