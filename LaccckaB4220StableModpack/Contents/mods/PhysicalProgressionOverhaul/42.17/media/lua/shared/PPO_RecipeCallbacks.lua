-- Build 42.17 has no declarative way to put a fluid into a recipe output:
-- across every vanilla recipe file, `fluid` appears only as an input. The
-- crafting seam calls Lua through CraftRecipeData.luaCallOnCreate, so this is
-- the one place the shake can be created. Nothing else belongs in this file.

PPO = PPO or {}

PPO_RecipeCallbacks = PPO_RecipeCallbacks or {}
PPO.RecipeCallbacks = PPO_RecipeCallbacks

local Callbacks = PPO_RecipeCallbacks

Callbacks.SHAKE_FLUID = "ProteinShake"
Callbacks.SHAKE_AMOUNT = 0.5
Callbacks.WATER_FLUID = "Water"

-- The argument shape of OnCreate is not documented by any vanilla Lua example:
-- vanilla points every OnCreate at the Java class RecipeCodeOnCreate. The entry
-- point therefore accepts anything and looks for containers rather than
-- assuming a position, and the live probe records the real shape.
local ITEM_LIST_ACCESSORS = {
    "getAllCreatedItems",
    "getCreatedItems",
    "getOutputItems",
    "getItems",
}

function Callbacks.findFluid(typeString)
    if Fluid == nil or Fluid.getAllFluids == nil then
        return nil
    end
    local resolved, fluids = pcall(Fluid.getAllFluids)
    if not resolved or fluids == nil or fluids.size == nil then
        return nil
    end
    local counted, count = pcall(fluids.size, fluids)
    if not counted or count == nil then
        return nil
    end
    for index = 0, count - 1 do
        local read, fluid = pcall(fluids.get, fluids, index)
        if read and fluid ~= nil and fluid.getFluidTypeString ~= nil then
            local named, name = pcall(fluid.getFluidTypeString, fluid)
            if named and name == typeString then
                return fluid
            end
        end
    end
    return nil
end

function Callbacks.containerOf(item)
    if type(item) ~= "table" and type(item) ~= "userdata" then
        return nil
    end
    if item.hasComponent == nil or item.getFluidContainer == nil then
        return nil
    end
    if ComponentType == nil or ComponentType.FluidContainer == nil then
        return nil
    end
    local asked, present = pcall(
        item.hasComponent, item, ComponentType.FluidContainer)
    if not asked or not present then
        return nil
    end
    local opened, container = pcall(item.getFluidContainer, item)
    if not opened or container == nil then
        return nil
    end
    return container
end

-- The container may hold tainted water or soda beside the clean water the
-- recipe was paid with, so the container total is not the amount that may be
-- poured back as Water. Measuring the clean component instead is what keeps the
-- craft from laundering a poisoned bottle; anything else in the container is
-- destroyed with it, which is what the player already accepts by feeding a
-- container-destroying recipe.
function Callbacks.cleanWaterAmount(container)
    if type(container) ~= "table" and type(container) ~= "userdata" then
        return nil
    end
    if container.getSpecificFluidAmount == nil then
        return nil
    end
    local water = Callbacks.findFluid(Callbacks.WATER_FLUID)
    if water == nil then
        return nil
    end
    local measured, amount = pcall(
        container.getSpecificFluidAmount, container, water)
    if not measured or type(amount) ~= "number" then
        return nil
    end
    return amount
end

-- The recipe consumes `SHAKE_AMOUNT` of water, but the input bottle is
-- destroyed whole, so whatever is left in it would vanish with it.
-- `ISHandcraftAction` calls this file through `luaCallOnCreate` before
-- `processDestroyAndUsedItems`, which is the only window where the input is
-- still readable, and the shake's `BlendWhiteList` accepts water, so the
-- remainder is poured back beside the shake instead of being destroyed.
function Callbacks.inputFluidAmount(value)
    if type(value) ~= "table" and type(value) ~= "userdata" then
        return nil
    end
    if value.getAllInputItems == nil then
        return nil
    end
    local read, list = pcall(value.getAllInputItems, value)
    if not read or list == nil or list.size == nil or list.get == nil then
        return nil
    end
    local counted, count = pcall(list.size, list)
    if not counted or count == nil then
        return nil
    end
    local total = nil
    for index = 0, count - 1 do
        local got, item = pcall(list.get, list, index)
        if got then
            local container = Callbacks.containerOf(item)
            local amount = Callbacks.cleanWaterAmount(container)
            if amount ~= nil then
                total = (total or 0) + amount
            end
        end
    end
    return total
end

function Callbacks.carriedWater(inputAmount, container)
    if type(inputAmount) ~= "number" then
        return 0
    end
    -- What the input still holds here is already surplus: the crafting system
    -- takes the recipe's own `-fluid 0.5` before it reaches this callback. A
    -- bottle filled to `1.0` reports `0.5`, which is exactly the half that
    -- would otherwise be destroyed with the container.
    local surplus = inputAmount
    if surplus <= 0 then
        return 0
    end
    -- The output container is mapped from the input type, so it normally holds
    -- the whole input; the clamp only matters if a mapping ever narrows it.
    if container.getCapacity ~= nil then
        local read, capacity = pcall(container.getCapacity, container)
        if read and type(capacity) == "number" then
            local free = capacity - Callbacks.SHAKE_AMOUNT
            if free < surplus then
                surplus = free
            end
        end
    end
    if surplus <= 0 then
        return 0
    end
    return surplus
end

function Callbacks.applyToItem(item, inputAmount)
    local container = Callbacks.containerOf(item)
    if container == nil then
        return false
    end
    if container.Empty == nil or container.addFluid == nil then
        return false
    end
    local fluid = Callbacks.findFluid(Callbacks.SHAKE_FLUID)
    if fluid == nil then
        return false
    end
    local surplus = Callbacks.carriedWater(inputAmount, container)
    local water = nil
    if surplus > 0 then
        water = Callbacks.findFluid(Callbacks.WATER_FLUID)
        if water == nil then
            surplus = 0
        end
    end
    -- Emptying first makes the call idempotent: a repeated invocation replaces
    -- the contents instead of stacking a second shake into the same bottle.
    local filled = pcall(function()
        container:Empty()
        container:addFluid(fluid, Callbacks.SHAKE_AMOUNT)
        if surplus > 0 then
            container:addFluid(water, surplus)
        end
    end)
    return filled
end

local function applyToList(value, inputAmount)
    local applied = 0
    for _, accessor in ipairs(ITEM_LIST_ACCESSORS) do
        if value[accessor] ~= nil then
            local read, list = pcall(value[accessor], value)
            if read and list ~= nil and list.size ~= nil and list.get ~= nil then
                local counted, count = pcall(list.size, list)
                if counted and count ~= nil then
                    for index = 0, count - 1 do
                        local got, item = pcall(list.get, list, index)
                        if got and Callbacks.applyToItem(item, inputAmount) then
                            applied = applied + 1
                        end
                    end
                end
            end
        end
    end
    return applied
end

-- Every mixing recipe does the same thing with a different fluid, so the fluid
-- is the only parameter. `SHAKE_FLUID` stays the field the fill path reads, so
-- `applyToItem` and everything below it is unchanged.
function Callbacks.mixer(fluidName)
    return function(...)
        Callbacks.SHAKE_FLUID = fluidName
        local inputAmount = nil
        for index = 1, select("#", ...) do
            if inputAmount == nil then
                inputAmount = Callbacks.inputFluidAmount((select(index, ...)))
            end
        end
        local applied = 0
        for index = 1, select("#", ...) do
            local argument = select(index, ...)
            if type(argument) == "table" or type(argument) == "userdata" then
                if Callbacks.applyToItem(argument, inputAmount) then
                    applied = applied + 1
                else
                    applied = applied + applyToList(argument, inputAmount)
                end
            end
        end
        return applied
    end
end

Callbacks.mixProteinShake = Callbacks.mixer("ProteinShake")
Callbacks.mixCreatineDrink = Callbacks.mixer("CreatineDrink")
Callbacks.mixHomemadeProteinBlend = Callbacks.mixer("HomemadeProteinShake")
Callbacks.mixHomemadeCreatineDrink = Callbacks.mixer("HomemadeCreatineDrink")
Callbacks.mixHomemadePreWorkoutDrink = Callbacks.mixer("HomemadePreWorkoutDrink")
Callbacks.mixCalorieShake = Callbacks.mixer("CalorieShake")
Callbacks.mixElectrolyteDrink = Callbacks.mixer("ElectrolyteDrink")
Callbacks.mixHomemadeElectrolyteDrink = Callbacks.mixer("HomemadeElectrolyteDrink")
Callbacks.mixPreWorkoutDrink = Callbacks.mixer("PreWorkoutDrink")

return Callbacks
