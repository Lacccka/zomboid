require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/Upgrades"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Upgrades = ExtractionMode.Upgrades
local Logistics = {}

local LOCKER_SPRITE = "location_military_generic_01_0"
local LOCKER_NAME = "Secure Delivery Crate"
local LOCKER_TYPE = "deliverylocker"

local COMMON_AMMO_BOXES = {
    "Base.Bullets9mmBox",
    "Base.Bullets45Box",
    "Base.Bullets38Box",
    "Base.Bullets357Box",
    "Base.Bullets44Box",
    "Base.ShotgunShellsBox",
    "Base.308Box",
    "Base.556Box",
    "Base.3030Box",
}

local PILL_TYPES = {
    "Base.Pills",
    "Base.PillsAntiDep",
    "Base.PillsBeta",
    "Base.PillsSleepingTablets",
    "Base.PillsVitamins",
}

local function currentDay()
    return math.max(0, math.floor(Util.worldHours() / 24))
end

local function tableHasEntries(values)
    if values == nil then return false end
    for _ in pairs(values) do return true end
    return false
end

local function shuffled(values)
    local result = {}
    for index, value in ipairs(values or {}) do result[index] = value end
    for index = #result, 2, -1 do
        local other = ZombRand(index) + 1
        result[index], result[other] = result[other], result[index]
    end
    return result
end

local function rotatingTownKeys()
    local result = {}
    for _, key in ipairs(Config.townKeys()) do
        local town = Config.town(key)
        if town and town.unlockQuestId == nil then result[#result + 1] = key end
    end
    return result
end

local function specialTownUnlocked(data, town)
    if town == nil or town.unlockQuestId == nil then return false end
    for _, completions in pairs((data and data.questProgress) or {}) do
        if completions[town.unlockQuestId] == true then return true end
    end
    return false
end

function Logistics.ensureState(data)
    if data == nil then return end
    data.availableTownKeys = data.availableTownKeys or {}
    data.pendingDeliveries = data.pendingDeliveries or {}
end

local function desiredTownCount(data)
    local key = Upgrades.isInstalled(data and data.upgrades, "comm_array")
        and "CommArrayDailyTownChoices" or "BaseDailyTownChoices"
    return math.max(1, math.min(#rotatingTownKeys(), math.floor(tonumber(Config.value(key)) or 3)))
end

function Logistics.refreshTownChoices(data)
    if data == nil then return false end
    Logistics.ensureState(data)
    local day = currentDay()
    local desired = desiredTownCount(data)
    local changed = false
    local rotationKeys = rotatingTownKeys()

    -- Quest-gated destinations are permanent unlocks and do not consume one of
    -- the rotating daily town slots. Strip any value left by an older/custom
    -- configuration before rebuilding or extending the rotation.
    local regularChoices = {}
    for _, key in ipairs(data.availableTownKeys) do
        local town = Config.town(key)
        if town and town.unlockQuestId == nil then regularChoices[#regularChoices + 1] = key end
    end
    if #regularChoices ~= #data.availableTownKeys then
        data.availableTownKeys = regularChoices
        changed = true
    end

    if tonumber(data.townRotationDay) ~= day or #data.availableTownKeys == 0 then
        local order = shuffled(rotationKeys)
        data.availableTownKeys = {}
        for index = 1, math.min(desired, #order) do
            data.availableTownKeys[#data.availableTownKeys + 1] = order[index]
        end
        data.townRotationDay = day
        changed = true
    elseif #data.availableTownKeys < desired then
        local present = {}
        for _, key in ipairs(data.availableTownKeys) do present[key] = true end
        local candidates = {}
        for _, key in ipairs(rotationKeys) do
            if not present[key] then candidates[#candidates + 1] = key end
        end
        for _, key in ipairs(shuffled(candidates)) do
            data.availableTownKeys[#data.availableTownKeys + 1] = key
            changed = true
            if #data.availableTownKeys >= desired then break end
        end
    end

    if changed and data.state == Config.STATE_HIDEOUT and data.selectedTownKey
        and not Logistics.isTownAvailable(data, data.selectedTownKey) then
        data.selectedTownKey = nil
        data.selectedTownBy = nil
        data.ready = {}
    end
    if changed then data.townChoicesChangedPending = true end
    return changed
end

local function sameTownChoices(left, right)
    if #(left or {}) ~= #(right or {}) then return false end
    local present = {}
    for _, key in ipairs(left or {}) do present[tostring(key)] = true end
    for _, key in ipairs(right or {}) do
        if present[tostring(key)] ~= true then return false end
    end
    return true
end

-- Debug/test helper: reroll today's regular destination rotation without
-- advancing world time or triggering daily item deliveries. When more towns
-- exist than available slots, make several attempts to avoid returning the
-- exact same set the tester just discarded.
function Logistics.forceRefreshTownChoices(data)
    if data == nil then return false end
    Logistics.ensureState(data)
    local previous = {}
    for index, key in ipairs(data.availableTownKeys or {}) do previous[index] = key end
    local canChange = #rotatingTownKeys() > desiredTownCount(data)
    local attempts = canChange and 12 or 1
    for _ = 1, attempts do
        data.availableTownKeys = {}
        data.townRotationDay = nil
        Logistics.refreshTownChoices(data)
        if not canChange or not sameTownChoices(previous, data.availableTownKeys) then
            return true
        end
    end
    return true
end

function Logistics.isTownAvailable(data, townKey)
    local wanted = tostring(townKey or "")
    local town = Config.town(wanted)
    if town and town.unlockQuestId ~= nil then return specialTownUnlocked(data, town) end
    for _, key in ipairs((data and data.availableTownKeys) or {}) do
        if key == wanted then return true end
    end
    return false
end

function Logistics.townChoices(data)
    Logistics.refreshTownChoices(data)
    local keys = {}
    local present = {}
    for _, key in ipairs((data and data.availableTownKeys) or {}) do
        keys[#keys + 1] = key
        present[key] = true
    end
    for _, key in ipairs(Config.townKeys()) do
        local town = Config.town(key)
        if not present[key] and specialTownUnlocked(data, town) then
            keys[#keys + 1] = key
        end
    end
    return Config.townSummaries(keys)
end

local function isDeliveryLocker(object)
    return object ~= nil and object:getModData().ExtractionModeDeliveryLocker == true
end

local function lockerOnSquare(square)
    local objects = square and square:getObjects()
    if objects == nil then return nil end
    for index = 0, objects:size() - 1 do
        local object = objects:get(index)
        if isDeliveryLocker(object) then return object end
    end
    return nil
end

local function ensureLockerContainer(object, square)
    if object == nil then return nil end
    local spriteName = nil
    pcall(function()
        local sprite = object:getSprite()
        spriteName = sprite and sprite:getName()
    end)
    if spriteName ~= LOCKER_SPRITE then
        object:setSprite(LOCKER_SPRITE)
        if isServer and isServer() then object:transmitUpdatedSpriteToClients() end
    end
    local container = object:getContainer()
    if container == nil then
        container = ItemContainer.new(LOCKER_TYPE, square, object, 8, 8)
        container:setCapacity(100)
        container:setExplored(true)
        object:setContainer(container)
    else
        container:setType(LOCKER_TYPE)
        container:setCapacity(100)
        container:setExplored(true)
    end
    object:SetName(LOCKER_NAME)
    return container
end

local function rememberedLocker(data)
    local point = data and data.deliveryLockerPoint
    local cell = getCell and getCell()
    if point == nil or cell == nil then return nil end
    local square = cell:getGridSquare(math.floor(point.x), math.floor(point.y), math.floor(point.z or 0))
    local object = lockerOnSquare(square)
    if object then ensureLockerContainer(object, square) end
    return object
end

local function placementSquare()
    local cell = getCell and getCell()
    if cell == nil then return nil end
    local hideout = Config.hideout()
    local anchor = cell:getGridSquare(math.floor(hideout.x), math.floor(hideout.y), math.floor(hideout.z or 0))
    if anchor == nil then return nil end
    local building = anchor:getBuilding()
    local maximumRadius = math.max(4, math.min(20, math.floor(tonumber(hideout.radius) or 14)))

    for radius = 2, maximumRadius do
        for dx = -radius, radius do
            for _, dy in ipairs({ -radius, radius }) do
                local square = cell:getGridSquare(anchor:getX() + dx, anchor:getY() + dy, anchor:getZ())
                if square and square:TreatAsSolidFloor() and square:isFree(false)
                    and square:getRoom() ~= nil and (building == nil or square:getBuilding() == building) then
                    return square
                end
            end
        end
        for dy = -radius + 1, radius - 1 do
            for _, dx in ipairs({ -radius, radius }) do
                local square = cell:getGridSquare(anchor:getX() + dx, anchor:getY() + dy, anchor:getZ())
                if square and square:TreatAsSolidFloor() and square:isFree(false)
                    and square:getRoom() ~= nil and (building == nil or square:getBuilding() == building) then
                    return square
                end
            end
        end
    end
    return nil
end

function Logistics.ensureLocker(data)
    if data == nil or not Upgrades.isInstalled(data.upgrades, "security") then return nil end
    Logistics.ensureState(data)
    local object = rememberedLocker(data)
    if object then return object end

    -- Recover a locker after an old save lost its remembered coordinates.
    local hideout = Config.hideout()
    local cell = getCell and getCell()
    if cell then
        local radius = math.max(4, math.min(24, math.floor(tonumber(hideout.radius) or 14)))
        for x = math.floor(hideout.x) - radius, math.floor(hideout.x) + radius do
            for y = math.floor(hideout.y) - radius, math.floor(hideout.y) + radius do
                local square = cell:getGridSquare(x, y, math.floor(hideout.z or 0))
                object = lockerOnSquare(square)
                if object then
                    data.deliveryLockerPoint = { x = x, y = y, z = math.floor(hideout.z or 0) }
                    ensureLockerContainer(object, square)
                    return object
                end
            end
        end
    end

    local square = placementSquare()
    if square == nil then return nil end
    object = IsoObject.new(square, LOCKER_SPRITE, LOCKER_NAME)
    object:getModData().ExtractionModeDeliveryLocker = true
    object:getModData().ExtractionModeIndestructible = true
    object:SetName(LOCKER_NAME)
    square:AddTileObject(object)
    ensureLockerContainer(object, square)
    data.deliveryLockerPoint = { x = square:getX(), y = square:getY(), z = square:getZ() }
    object:transmitModData()
    object:transmitCompleteItemToClients()
    Util.log("Created secure delivery crate at " .. square:getX() .. "," .. square:getY())
    return object
end

local function validScriptItem(fullType)
    if fullType == nil or fullType == "" then return false end
    local manager = getScriptManager and getScriptManager()
    if manager == nil then return true end
    local found = nil
    pcall(function() found = manager:FindItem(fullType) end)
    return found ~= nil
end

local function ammoBoxFromWeapon(item)
    if item == nil or not instanceof(item, "HandWeapon") then return nil end
    local ranged = false
    local ammoBox = nil
    pcall(function()
        ranged = item:isRanged()
        ammoBox = item:getAmmoBox()
    end)
    if ranged and validScriptItem(ammoBox) then return ammoBox end
    return nil
end

local function addAmmoPreference(result, seen, ammoBox)
    if ammoBox == nil or seen[ammoBox] == true then return end
    seen[ammoBox] = true
    result[#result + 1] = ammoBox
end

local function weaponAmmoBoxesInContainer(container, result, seen)
    if container == nil then return end
    local weapons = container:getAllEvalRecurse(function(item)
        return ammoBoxFromWeapon(item) ~= nil
    end)
    if weapons == nil then return end
    for index = 0, weapons:size() - 1 do
        local ammoBox = ammoBoxFromWeapon(weapons:get(index))
        addAmmoPreference(result, seen, ammoBox)
    end
end

local function characterAmmoPreferences(result, seen)
    result = result or {}
    seen = seen or {}
    for _, player in ipairs(Util.players()) do
        if player and not player:isDead() then
            weaponAmmoBoxesInContainer(player:getInventory(), result, seen)
        end
    end
    return result, seen
end

local function hideoutAmmoPreferences(result, seen)
    result = result or {}
    seen = seen or {}
    local cell = getCell and getCell()
    if cell == nil then return result end
    local hideout = Config.hideout()
    local anchorZ = math.floor(hideout.z or 0)
    local anchor = cell:getGridSquare(math.floor(hideout.x), math.floor(hideout.y), anchorZ)
    local building = anchor and anchor:getBuilding()
    local radius = math.max(4, math.min(50, math.floor(tonumber(hideout.radius) or 14)))
    local maximumZ = anchorZ
    pcall(function() maximumZ = math.min(31, math.max(anchorZ, tonumber(cell:getMaxZ()) or anchorZ)) end)
    for x = math.floor(hideout.x) - radius, math.floor(hideout.x) + radius do
        for y = math.floor(hideout.y) - radius, math.floor(hideout.y) + radius do
            for z = anchorZ, maximumZ do
                local square = cell:getGridSquare(x, y, z)
                if square and (building == nil or square:getBuilding() == building) then
                    local objects = square:getObjects()
                    if objects then
                        for objectIndex = 0, objects:size() - 1 do
                            local object = objects:get(objectIndex)
                            for containerIndex = 0, (object and object:getContainerCount() or 0) - 1 do
                                weaponAmmoBoxesInContainer(object:getContainerByIndex(containerIndex), result, seen)
                            end
                        end
                    end
                    local worldObjects = square:getWorldObjects()
                    if worldObjects then
                        for worldIndex = 0, worldObjects:size() - 1 do
                            local worldItem = worldObjects:get(worldIndex)
                            local item = worldItem and worldItem:getItem()
                            local ammoBox = ammoBoxFromWeapon(item)
                            addAmmoPreference(result, seen, ammoBox)
                        end
                    end
                end
            end
        end
    end
    return result
end

local function queueItem(data, fullType, amount)
    if not validScriptItem(fullType) then return 0 end
    Logistics.ensureState(data)
    local count = math.max(0, math.floor(tonumber(amount) or 0))
    data.pendingDeliveries[fullType] = (tonumber(data.pendingDeliveries[fullType]) or 0) + count
    return count
end

local function rollAmmoDelivery(data)
    local preferred = Upgrades.isInstalled(data.upgrades, "preferred_ammo_delivery")
    local amount = preferred and (3 + ZombRand(2)) or (1 + ZombRand(2))
    local pool = COMMON_AMMO_BOXES
    if preferred then
        local preferences, seen = characterAmmoPreferences()
        hideoutAmmoPreferences(preferences, seen)
        if #preferences > 0 then pool = preferences end
    end
    for _ = 1, amount do queueItem(data, pool[ZombRand(#pool) + 1], 1) end
    return amount
end

local function rollMedicalItem()
    local roll = ZombRand(10000)
    if roll < 5500 then return "Base.Bandage" end
    if roll < 8000 then return "Base.Disinfectant" end
    if roll < 9950 then return PILL_TYPES[ZombRand(#PILL_TYPES) + 1] end
    return Config.INFECTION_CURE_TYPE
end

local function rollMedicalDelivery(data)
    local amount = 2 + ZombRand(3)
    for _ = 1, amount do queueItem(data, rollMedicalItem(), 1) end
    return amount
end

function Logistics.materializePending(data)
    if data == nil or not tableHasEntries(data.pendingDeliveries) then return 0, 0 end
    local locker = Logistics.ensureLocker(data)
    local container = locker and locker:getContainer()
    if container == nil then return 0, 0 end
    local delivered = 0
    local failed = 0
    for fullType, amount in pairs(data.pendingDeliveries) do
        local remaining = math.max(0, math.floor(tonumber(amount) or 0))
        while remaining > 0 do
            local scriptItem = nil
            local manager = getScriptManager and getScriptManager()
            if manager then pcall(function() scriptItem = manager:getItem(fullType) end) end
            local itemWeight = scriptItem and tonumber(scriptItem:getActualWeight()) or 0
            local usedCapacity = tonumber(container:getCapacityWeight()) or 0
            local capacity = tonumber(container:getCapacity()) or 0
            if usedCapacity + math.max(0, itemWeight) > capacity + 0.0001 then
                -- Deliver everything that still fits, but never hold overflow for
                -- a later retry. Players must make room before the next shipment.
                failed = failed + remaining
                remaining = 0
                break
            end
            local item = container:AddItem(fullType)
            if item == nil then break end
            if sendAddItemToContainer then sendAddItemToContainer(container, item) end
            remaining = remaining - 1
            delivered = delivered + 1
        end
        data.pendingDeliveries[fullType] = remaining > 0 and remaining or nil
    end
    return delivered, failed
end

function Logistics.processDaily(data)
    if data == nil then return { townsChanged = false, ammo = 0, medical = 0, delivered = 0, failed = 0 } end
    Logistics.ensureState(data)
    local changedNow = Logistics.refreshTownChoices(data)
    local result = {
        townsChanged = changedNow or data.townChoicesChangedPending == true,
        ammo = 0,
        medical = 0,
        delivered = 0,
        failed = 0,
    }
    data.townChoicesChangedPending = nil
    local day = currentDay()
    if Upgrades.isInstalled(data.upgrades, "ammo_delivery")
        and tonumber(data.ammoDeliveryDay) ~= day then
        data.ammoDeliveryDay = day
        result.ammo = rollAmmoDelivery(data)
    end
    if Upgrades.isInstalled(data.upgrades, "medical_delivery")
        and tonumber(data.medicalDeliveryDay) ~= day then
        data.medicalDeliveryDay = day
        result.medical = rollMedicalDelivery(data)
    end
    if Upgrades.isInstalled(data.upgrades, "security") then Logistics.ensureLocker(data) end
    result.delivered, result.failed = Logistics.materializePending(data)
    return result
end

function Logistics.onUpgradeInstalled(data, upgradeId)
    if data == nil then return { townsChanged = false, ammo = 0, medical = 0, delivered = 0, failed = 0 } end
    if upgradeId == "comm_array" then Logistics.refreshTownChoices(data) end
    return Logistics.processDaily(data)
end

function Logistics.isDeliveryLocker(object)
    return isDeliveryLocker(object)
end

ExtractionMode.Logistics = Logistics
return Logistics
