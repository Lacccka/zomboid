require "ExtractionMode/Config"
require "ExtractionMode/Localization"

ExtractionMode = ExtractionMode or {}

local Config = ExtractionMode.Config
local Localization = ExtractionMode.Localization
local Upgrades = {}

local LIGHT_BULB_TYPES = {
    "Base.LightBulb",
    "Base.LightBulbBlue",
    "Base.LightBulbCyan",
    "Base.LightBulbGreen",
    "Base.LightBulbMagenta",
    "Base.LightBulbOrange",
    "Base.LightBulbPink",
    "Base.LightBulbPurple",
    "Base.LightBulbRed",
    "Base.LightBulbYellow",
}

local ELECTRICAL_WIRE_TYPES = {
    "Base.ElectricWire",
    "Base.Wire",
}

local WRITING_TOOL_TYPES = {
    "Base.Pen",
    "Base.Pencil",
}

local CAR_BATTERY_TYPES = {
    "Base.CarBattery1",
    "Base.CarBattery2",
    "Base.CarBattery3",
}

local FIRST_AID_KIT_TYPES = {
    "Base.FirstAidKit",
    "Base.FirstAidKit_New",
    "Base.FirstAidKit_NewPro",
    "Base.FirstAidKit_Camping",
    "Base.FirstAidKit_Camping_New",
    "Base.FirstAidKit_Military",
}

-- Alternative radio devices are pooled into one requirement count. This lets
-- logistics upgrades accept any useful receiver/transceiver without forcing
-- survivors to find one specific radio model.
local COMMS_RADIO_TYPES = {
    "Base.WalkieTalkie1",
    "Base.WalkieTalkie2",
    "Base.WalkieTalkie3",
    "Base.WalkieTalkie4",
    "Base.WalkieTalkie5",
    "Base.WalkieTalkieMakeShift",
    "Base.ManPackRadio",
    "Base.HamRadio1",
    "Base.HamRadio2",
    "Base.HamRadioMakeShift",
    "Base.RadioBlack",
    "Base.RadioMakeShift",
    "Base.RadioRed",
}

local LONG_RANGE_RADIO_TYPES = {
    "Base.ManPackRadio",
    "Base.HamRadio1",
    "Base.HamRadio2",
    "Base.HamRadioMakeShift",
}

local definitions = {
    {
        id = "lighting",
        category = "utilities",
        name = "Hideout Lighting",
        description = "Install permanent electrical lighting throughout the hideout.",
        skillRequirements = {
            { label = "Electrical", perk = Perks.Electricity, level = 1 },
        },
        requirements = {
            {
                label = "Light Bulbs",
                amount = 10,
                types = LIGHT_BULB_TYPES,
            },
            { label = "Electrical Wire", amount = 4, types = ELECTRICAL_WIRE_TYPES },
        },
    },
    {
        id = "security",
        category = "utilities",
        name = "Security",
        description = "Secure the hideout and unlock protected logistics deliveries.",
        skillRequirements = {
            { label = "Welding", perk = Perks.MetalWelding, level = 1 },
        },
        requirements = {
            { label = "Sheet Metal", amount = 4, types = { "Base.SheetMetal" } },
            { label = "Steel Rods", amount = 2, types = { "Base.MetalBar" } },
            { label = "Padlocks or Combination Locks", amount = 2,
                types = { "Base.Padlock", "Base.CombinationPadlock" } },
        },
    },
    {
        id = "ventilation",
        category = "utilities",
        name = "Ventilation",
        description = "Improve sleep recovery and make generator efficiency upgrades safe to install.",
        skillRequirements = {
            { label = "Welding", perk = Perks.MetalWelding, level = 1 },
        },
        requirements = {
            { label = "Sheet Metal", amount = 4, types = { "Base.SheetMetal" } },
            { label = "Duct Tape", amount = 4, types = { "Base.DuctTape" } },
            { label = "Tarps", amount = 2, types = { "Base.Tarp" } },
            { label = "Electrical Wire", amount = 2, types = ELECTRICAL_WIRE_TYPES },
        },
    },
    {
        id = "heating",
        category = "utilities",
        name = "Heating",
        description = "Keep the powered hideout warm through winter weather.",
        prerequisites = { "ventilation" },
        skillRequirements = {
            { label = "Welding", perk = Perks.MetalWelding, level = 3 },
        },
        requirements = {
            { label = "Metal Pipes", amount = 6, types = { "Base.MetalPipe" } },
            { label = "Propane Tank", amount = 1, types = { "Base.PropaneTank" } },
            { label = "Rubber Hoses", amount = 2, types = { "Base.RubberHose" } },
            { label = "Tarps", amount = 2, types = { "Base.Tarp" } },
        },
    },
    {
        id = "generator_tuneup",
        category = "generator",
        name = "Generator Tune-Up",
        description = "Replace worn components and reduce generator fuel use by 1 L/day.",
        generatorFuelReduction = 1,
        prerequisites = { "ventilation" },
        skillRequirements = {
            { label = "Electrical", perk = Perks.Electricity, level = 2 },
            { label = "Mechanics", perk = Perks.Mechanics, level = 1 },
        },
        requirements = {
            { label = "Electrical Wire", amount = 2, types = ELECTRICAL_WIRE_TYPES },
            { label = "Duct Tape", amount = 2, types = { "Base.DuctTape" } },
            { label = "Electronic Scrap", amount = 4, types = { "Base.ElectronicsScrap" } },
        },
    },
    {
        id = "generator_governor",
        category = "generator",
        name = "Electronic Governor",
        description = "Regulate generator load and reduce generator fuel use by another 1 L/day.",
        prerequisites = { "generator_tuneup" },
        generatorFuelReduction = 1,
        skillRequirements = {
            { label = "Electricity", perk = Perks.Electricity, level = 3 },
            { label = "Mechanics", perk = Perks.Mechanics, level = 2 },
        },
        requirements = {
            { label = "Timer", amount = 1, types = { "Base.Timer", "Base.TimerCrafted" } },
            { label = "Amplifier", amount = 1, types = { "Base.Amplifier" } },
            { label = "Electrical Wire", amount = 4, types = ELECTRICAL_WIRE_TYPES },
            { label = "Electronic Scrap", amount = 6, types = { "Base.ElectronicsScrap" } },
        },
    },
    {
        id = "generator_retrofit",
        category = "generator",
        name = "High-Efficiency Retrofit",
        description = "Rebuild the control system and reduce generator fuel use by another 1 L/day.",
        prerequisites = { "generator_governor" },
        generatorFuelReduction = 1,
        skillRequirements = {
            { label = "Electrical", perk = Perks.Electricity, level = 3 },
            { label = "Mechanics", perk = Perks.Mechanics, level = 4 },
        },
        requirements = {
            { label = "Engine Parts", amount = 4, types = { "Base.EngineParts" } },
            { label = "Car Battery", amount = 1, types = CAR_BATTERY_TYPES },
            { label = "Aluminum Scrap", amount = 6, types = { "Base.AluminumScrap" } },
	        { label = "Electrical Wire", amount = 6, types = ELECTRICAL_WIRE_TYPES },
            { label = "Electronic Scrap", amount = 8, types = { "Base.ElectronicsScrap" } },
        },
    },
    {
        id = "ammo_delivery",
        category = "logistics",
        name = "Ammo Delivery",
        description = "Receive a small daily shipment of common pistol, shotgun, or rifle ammunition.",
        prerequisites = { "security" },
        skillRequirements = {
            { label = "Reloading", perk = Perks.Reloading, level = 1 },
        },
        requirements = {
            { label = "Radio or Walkie-Talkie", amount = 1, types = COMMS_RADIO_TYPES },
            { label = "Radio Transmitter", amount = 1, types = { "Base.RadioTransmitter" } },
            { label = "Batteries", amount = 4, types = { "Base.Battery" } },
            { label = "Sheet Metal", amount = 2, types = { "Base.SheetMetal" } },
        },
    },
    {
        id = "preferred_ammo_delivery",
        category = "logistics",
        name = "Preferred Ammo Delivery",
        description = "Medium daily ammo shipment prioritizing carried and hideout firearms.",
        prerequisites = { "ammo_delivery" },
        skillRequirements = {
            { label = "Reloading", perk = Perks.Reloading, level = 2 },
        },
        requirements = {
            { label = "Radio or Walkie-Talkie", amount = 1, types = COMMS_RADIO_TYPES },
            { label = "Amplifiers", amount = 2, types = { "Base.Amplifier" } },
            { label = "Radio Receivers", amount = 1, types = { "Base.RadioReceiver" } },
            { label = "Radio Transmitters", amount = 1, types = { "Base.RadioTransmitter" } },
            { label = "Batteries", amount = 6, types = { "Base.Battery" } },
        },
    },
    {
        id = "medical_delivery",
        category = "logistics",
        name = "Medical Delivery",
        description = "Receive daily medicine, heal 20% faster, and resist 20% of Knox infections.",
        prerequisites = { "security" },
        skillRequirements = {
            { label = "First Aid", perk = Perks.Doctor, level = 2 },
        },
        requirements = {
            { label = "Radio or Walkie-Talkie", amount = 1, types = COMMS_RADIO_TYPES },
            { label = "Bandages", amount = 6, types = { "Base.Bandage" } },
            { label = "Disinfectant", amount = 4, types = { "Base.Disinfectant" } },
            { label = "Antibiotics", amount = 2, types = { "Base.Antibiotics" } },
        },
    },
    {
        id = "intel_center",
        category = "operations",
        name = "Intel Center",
        description = "Clear a wider insertion perimeter and gain three additional hours before the horde window.",
        skillRequirements = {
            { label = "Carpentry", perk = Perks.Woodwork, level = 2 },
        },
        requirements = {
            { label = "Maps", amount = 2, acceptsAnyMap = true },
            { label = "Notepads or Notebooks", amount = 4,
                types = { "Base.Notepad", "Base.Notebook" } },
            { label = "Pens or Pencils", amount = 4, types = WRITING_TOOL_TYPES },
            { label = "Radio Receiver", amount = 1, types = { "Base.RadioReceiver" } },
            { label = "Batteries", amount = 4, types = { "Base.Battery" } },
        },
    },
    {
        id = "comm_array",
        category = "operations",
        name = "Comm Array",
        description = "Expand daily raid destinations from three towns to five and reduce helicopter response from 90 to 60 seconds.",
        skillRequirements = {
            { label = "Electrical", perk = Perks.Electricity, level = 4 },
            { label = "Mechanics", perk = Perks.Mechanics, level = 2 },
        },
        requirements = {
            { label = "Long-Range Radio", amount = 1, types = LONG_RANGE_RADIO_TYPES },
            { label = "Amplifiers", amount = 3, types = { "Base.Amplifier" } },
            { label = "Radio Receivers", amount = 4, types = { "Base.RadioReceiver" } },
            { label = "Radio Transmitters", amount = 4, types = { "Base.RadioTransmitter" } },
            { label = "Aluminum Scrap", amount = 4, types = { "Base.AluminumScrap" } },
        },
    },
}

local byId = {}
for _, definition in ipairs(definitions) do
    local prefix = "IGUI_ExtractionMode_Upgrade_" .. definition.id .. "_"
    definition.nameKey = prefix .. "Name"
    definition.descriptionKey = prefix .. "Description"
    for index, requirement in ipairs(definition.requirements or {}) do
        requirement.labelKey = prefix .. "Requirement_" .. tostring(index)
    end
    for index, requirement in ipairs(definition.skillRequirements or {}) do
        requirement.labelKey = prefix .. "Skill_" .. tostring(index)
    end
    byId[definition.id] = definition
end

function Upgrades.definitions()
    return definitions
end

function Upgrades.definition(id)
    return byId[tostring(id or "")]
end

function Upgrades.name(definition)
    return Localization.field(definition, "name")
end

function Upgrades.description(definition)
    return Localization.field(definition, "description")
end

function Upgrades.label(entry)
    return Localization.field(entry, "label")
end

function Upgrades.categories()
    return {
        { id = "utilities", label = Localization.get("IGUI_ExtractionMode_Category_Utilities", "UTILITIES") },
        { id = "generator", label = Localization.get("IGUI_ExtractionMode_Category_Generator", "GENERATOR") },
        { id = "logistics", label = Localization.get("IGUI_ExtractionMode_Category_Logistics", "LOGISTICS") },
        { id = "operations", label = Localization.get("IGUI_ExtractionMode_Category_Operations", "OPERATIONS") },
    }
end

function Upgrades.definitionsForCategory(category)
    local result = {}
    for _, definition in ipairs(definitions) do
        if definition.category == category then result[#result + 1] = definition end
    end
    return result
end

function Upgrades.isInstalled(completed, id)
    return completed ~= nil and completed[tostring(id or "")] == true
end

function Upgrades.prerequisitesMet(completed, definition)
    if definition == nil then return false end
    for _, id in ipairs(definition.prerequisites or {}) do
        if not Upgrades.isInstalled(completed, id) then return false end
    end
    return true
end

function Upgrades.missingPrerequisiteNames(completed, definition)
    local result = {}
    if definition == nil then return result end
    for _, id in ipairs(definition.prerequisites or {}) do
        if not Upgrades.isInstalled(completed, id) then
            local prerequisite = byId[id]
            result[#result + 1] = prerequisite and Upgrades.name(prerequisite) or tostring(id)
        end
    end
    return result
end

function Upgrades.skillLevel(player, requirement)
    if player == nil or requirement == nil or requirement.perk == nil then return 0 end
    return math.max(0, tonumber(player:getPerkLevel(requirement.perk)) or 0)
end

function Upgrades.skillRequirementsMet(player, definition)
    if player == nil or definition == nil then return false end
    for _, requirement in ipairs(definition.skillRequirements or {}) do
        if Upgrades.skillLevel(player, requirement) < (tonumber(requirement.level) or 0) then return false end
    end
    return true
end

function Upgrades.missingSkillNames(player, definition)
    local result = {}
    if player == nil or definition == nil then return result end
    for _, requirement in ipairs(definition.skillRequirements or {}) do
        local required = math.max(0, math.floor(tonumber(requirement.level) or 0))
        if Upgrades.skillLevel(player, requirement) < required then
            result[#result + 1] = Upgrades.label(requirement) .. " " .. tostring(required)
        end
    end
    return result
end

function Upgrades.upgradeSkillRequirementsMet(player, definition)
    if Config.value("RequireUpgradeSkills") == false then return true end
    return Upgrades.skillRequirementsMet(player, definition)
end

function Upgrades.missingUpgradeSkillNames(player, definition)
    if Config.value("RequireUpgradeSkills") == false then return {} end
    return Upgrades.missingSkillNames(player, definition)
end

function Upgrades.completionSnapshot(completed)
    local result = {}
    for _, definition in ipairs(definitions) do
        result[definition.id] = Upgrades.isInstalled(completed, definition.id)
    end
    return result
end

local function itemMatchesRequirement(item, requirement)
    if item == nil or requirement == nil then return false end
    local fullType = tostring(item:getFullType() or "")
    local selected = false

    if requirement.acceptsAnyMap == true then
        pcall(function() selected = item:IsMap() == true end)
    end

    if not selected then
        for _, candidate in ipairs(requirement.types or {}) do
            if fullType == candidate then selected = true; break end
        end
    end
    if not selected then
        for _, prefix in ipairs(requirement.typePrefixes or {}) do
            if fullType:sub(1, #prefix) == prefix then selected = true; break end
        end
    end
    if not selected then return false end

    for _, fragment in ipairs(requirement.excludedTypeFragments or {}) do
        if fullType:find(fragment, 1, true) then return false end
    end
    if requirement.requiresUsable == true then
        local usable = true
        pcall(function() usable = not item:isBroken() and item:getCondition() > 0 end)
        if not usable then return false end
    end
    if requirement.requiresWater == true then
        local containsWater = false
        pcall(function()
            local container = item:getFluidContainer()
            containsWater = container ~= nil and container:getAmount() > 0.0001
                and container:contains(Fluid.Water) and container:isPureFluid(Fluid.Water)
        end)
        if not containsWater then return false end
    end
    if requirement.requiresFullPetrol == true then
        local fullPetrol = false
        pcall(function()
            local container = item:getFluidContainer()
            fullPetrol = container ~= nil and container:getCapacity() > 0
                and container:getAmount() >= container:getCapacity() - 0.0001
                and container:contains(Fluid.Petrol) and container:isPureFluid(Fluid.Petrol)
        end)
        if not fullPetrol then return false end
    end
    if requirement.requiresNotEmpty == true then
        local containsSomething = false
        pcall(function()
            local container = item:getFluidContainer()
            containsSomething = container ~= nil and container:getAmount() > 0.0001
        end)
        if not containsSomething then
            pcall(function() containsSomething = item:getCurrentUsesFloat() > 0.0001 end)
        end
        if not containsSomething then return false end
    end
    return true
end

local function itemContentScore(item)
    if item == nil then return 0 end
    local score = 0
    pcall(function()
        if item:IsInventoryContainer() then
            local contents = item:getInventory()
            score = score + ((contents and contents:getItems():size() or 0) * 1000000)
        end
    end)
    pcall(function()
        local container = item:getFluidContainer()
        if container ~= nil then score = score + math.max(0, container:getAmount()) * 10000 end
    end)
    pcall(function()
        if instanceof(item, "HandWeapon") and item:getMaxAmmo() > 0 then
            score = score + math.max(0, item:getCurrentAmmoCount())
            if item:haveChamber() and item:isRoundChambered() then score = score + 1 end
        end
    end)
    return score
end

local function itemTurnInPriority(item)
    local priority = {
        contents = itemContentScore(item),
        damage = 0,
        condition = 0,
        hotbar = false,
        equipped = false,
    }
    if item == nil then return priority end
    pcall(function()
        local maximumCondition = math.max(1, tonumber(item:getConditionMax()) or 1)
        priority.condition = math.max(0, tonumber(item:getCondition()) or 0) / maximumCondition
    end)
    pcall(function()
        if instanceof(item, "HandWeapon") then
            priority.damage = math.max(0, tonumber(item:getMaxDamage()) or 0)
        end
    end)
    pcall(function() priority.hotbar = item:getAttachedSlot() >= 0 end)
    pcall(function() priority.equipped = item:isEquipped() == true end)
    return priority
end

function Upgrades.requirementItems(inventory, requirement)
    local result = {}
    if inventory == nil or requirement == nil then return result end
    local items = inventory:getAllEvalRecurse(function(item)
        return itemMatchesRequirement(item, requirement)
    end)
    if items then
        for index = 0, items:size() - 1 do result[#result + 1] = items:get(index) end
    end
    -- Turn-ins consume empty bags, vessels, and firearms first. Among equally
    -- empty candidates, preserve higher damage and condition before hotbar and
    -- equipped state, then retain inventory order for exact ties.
    local originalOrder = {}
    local priorities = {}
    for index, item in ipairs(result) do
        originalOrder[item] = index
        priorities[item] = itemTurnInPriority(item)
    end
    table.sort(result, function(left, right)
        local leftPriority = priorities[left]
        local rightPriority = priorities[right]
        if leftPriority.contents ~= rightPriority.contents then
            return leftPriority.contents < rightPriority.contents
        end
        if leftPriority.damage ~= rightPriority.damage then
            return leftPriority.damage < rightPriority.damage
        end
        if leftPriority.condition ~= rightPriority.condition then
            return leftPriority.condition < rightPriority.condition
        end
        if leftPriority.hotbar ~= rightPriority.hotbar then return not leftPriority.hotbar end
        if leftPriority.equipped ~= rightPriority.equipped then return not leftPriority.equipped end
        return (originalOrder[left] or 0) < (originalOrder[right] or 0)
    end)
    return result
end

function Upgrades.requirementCount(inventory, requirement)
    local count = 0
    for _, item in ipairs(Upgrades.requirementItems(inventory, requirement)) do
        local stackCount = 1
        pcall(function() stackCount = math.max(1, math.floor(tonumber(item:getCount()) or 1)) end)
        count = count + stackCount
    end
    return count
end

function Upgrades.requirementsMet(inventory, definition)
    if inventory == nil or definition == nil then return false end
    for _, requirement in ipairs(definition.requirements or {}) do
        if Upgrades.requirementCount(inventory, requirement) < (tonumber(requirement.amount) or 0) then
            return false
        end
    end
    return true
end

-- The server calls this only after requirementsMet succeeds. All item references
-- are collected before mutation so a missing requirement cannot cause a partial install.
function Upgrades.consumeRequirements(inventory, definition)
    if not Upgrades.requirementsMet(inventory, definition) then return false end
    local reserved = {}
    for _, requirement in ipairs(definition.requirements or {}) do
        local items = Upgrades.requirementItems(inventory, requirement)
        local remaining = math.max(0, math.floor(tonumber(requirement.amount) or 0))
        for _, item in ipairs(items) do
            if remaining <= 0 then break end
            local stackCount = 1
            pcall(function() stackCount = math.max(1, math.floor(tonumber(item:getCount()) or 1)) end)
            local available = math.max(0, stackCount - (reserved[item] or 0))
            local take = math.min(remaining, available)
            if take > 0 then
                reserved[item] = (reserved[item] or 0) + take
                remaining = remaining - take
            end
        end
        if remaining > 0 then return false end
    end

    for item, amount in pairs(reserved) do
        local container = item and item:getContainer()
        if container == nil then return false end
        local stackCount = 1
        pcall(function() stackCount = math.max(1, math.floor(tonumber(item:getCount()) or 1)) end)
        if amount >= stackCount then
            container:Remove(item)
            if sendRemoveItemFromContainer then sendRemoveItemFromContainer(container, item) end
        else
            item:setCount(stackCount - amount)
            if sendReplaceItemInContainer then
                sendReplaceItemInContainer(container, item, item)
            end
        end
    end
    return true
end

ExtractionMode.Upgrades = Upgrades
return Upgrades
