require "ExtractionMode/Config"
require "ExtractionMode/ModCompatibility"

ExtractionMode = ExtractionMode or {}

local Garage = {}
local SNAPSHOT_SCHEMA = 1
local MAX_ITEM_DEPTH = 12
local MAX_NAME_LENGTH = 48
local BACKUP_SCHEMA = 1

local function clamp(value, minimum, maximum)
    return math.max(minimum, math.min(maximum, tonumber(value) or minimum))
end

local function copySerializable(value, seen, depth)
    local valueType = type(value)
    if valueType == "nil" or valueType == "boolean" or valueType == "number"
        or valueType == "string" then return value end
    if valueType ~= "table" or (tonumber(depth) or 0) >= MAX_ITEM_DEPTH then return nil end
    seen = seen or {}
    if seen[value] then return nil end
    seen[value] = true
    local copied = {}
    for key, nested in pairs(value) do
        local keyType = type(key)
        if keyType == "string" or keyType == "number" then
            local nestedCopy = copySerializable(nested, seen, (tonumber(depth) or 0) + 1)
            if nestedCopy ~= nil then copied[key] = nestedCopy end
        end
    end
    seen[value] = nil
    return copied
end

local function readNumber(callback, fallback)
    local value = fallback
    pcall(function() value = tonumber(callback()) or fallback end)
    return value
end

local function readBoolean(callback, fallback)
    local value = fallback == true
    pcall(function() value = callback() == true end)
    return value
end

local function readString(callback, fallback)
    local value = fallback
    pcall(function()
        local result = callback()
        if result ~= nil then value = tostring(result) end
    end)
    return value
end

local function readOptional(callback)
    local ok, value = pcall(callback)
    if ok and value ~= nil then return value end
    return nil
end

local function isItemType(item, className)
    if item == nil or instanceof == nil then return false end
    local matches = false
    pcall(function() matches = instanceof(item, className) == true end)
    return matches
end

local function snapshotContainer(container, depth, errors)
    if container == nil then return {} end
    if depth >= MAX_ITEM_DEPTH then
        errors[#errors + 1] = "nested container depth exceeds " .. tostring(MAX_ITEM_DEPTH)
        return {}
    end
    local result = {}
    local items = nil
    pcall(function() items = container:getItems() end)
    if items == nil then return result end
    for index = 0, items:size() - 1 do
        local item = items:get(index)
        if item ~= nil then
            local itemSnapshot, itemError = Garage.snapshotItem(item, depth + 1, errors)
            if itemSnapshot ~= nil then
                result[#result + 1] = itemSnapshot
            else
                errors[#errors + 1] = tostring(itemError or "unknown item capture error")
            end
        end
    end
    return result
end

function Garage.snapshotItem(item, depth, errors)
    depth = tonumber(depth) or 0
    errors = errors or {}
    if item == nil then return nil, "item is unavailable" end
    if depth >= MAX_ITEM_DEPTH then return nil, "item nesting exceeds safe garage depth" end
    local isFood = isItemType(item, "Food")
    local isDrainable = isItemType(item, "DrainableComboItem")
    local isLiterature = isItemType(item, "Literature")
    local isWeapon = isItemType(item, "HandWeapon")
    local isClothing = isItemType(item, "Clothing")
    local isInventoryContainer = isItemType(item, "InventoryContainer")
    local isMoveable = isItemType(item, "Moveable")
    local snapshot = {
        fullType = readString(function() return item:getFullType() end, ""),
        -- Picked-up furniture is represented by a generic Moveable whose
        -- runtime full type may be the world-sprite name (for example
        -- Base.location_business_office_generic_01_59). That is not a script
        -- item which instanceItem can recreate, so preserve the sprite
        -- explicitly for the garage restorer.
        worldSprite = isMoveable and readString(function() return item:getWorldSprite() end, nil) or nil,
        condition = readNumber(function() return item:getCondition() end, nil),
        conditionMax = readNumber(function() return item:getConditionMax() end, nil),
        currentUses = readNumber(function() return item:getCurrentUses() end, nil),
        currentUsesFloat = readNumber(function() return item:getCurrentUsesFloat() end, nil),
        usedDelta = nil,
        age = readNumber(function() return item:getAge() end, nil),
        favorite = readBoolean(function() return item:isFavorite() end, false),
        activated = readBoolean(function() return item:isActivated() end, false),
        customName = readBoolean(function() return item:isCustomName() end, false),
        name = readString(function() return item:getName() end, nil),
        keyId = readNumber(function() return item:getKeyId() end, nil),
        modData = nil,
        extra = {},
    }
    if snapshot.fullType == "" then return nil, "item type is unavailable" end
    if isClothing then
        snapshot.usedDelta = readNumber(function() return item:getUsedDelta() end, nil)
    end
    pcall(function() snapshot.modData = copySerializable(item:getModData(), {}, 0) end)
    local extraReaders = {
        cooked = function() return item:isCooked() end,
        burnt = function() return item:isBurnt() end,
        wet = function() return item:isWet() end,
        itemCapacity = function() return item:getItemCapacity() end,
        currentAmmoCount = function() return item:getCurrentAmmoCount() end,
        recordedMediaIndex = function() return item:getRecordedMediaIndex() end,
        bloodLevel = function() return item:getBloodLevel() end,
    }
    if isFood then
        extraReaders.tainted = function() return item:isTainted() end
        extraReaders.frozen = function() return item:isFrozen() end
        extraReaders.freezingTime = function() return item:getFreezingTime() end
        extraReaders.poisonPower = function() return item:getPoisonPower() end
    end
    if isFood or isDrainable then
        extraReaders.heat = function() return item:getHeat() end
    end
    if isLiterature then
        extraReaders.alreadyReadPages = function() return item:getAlreadyReadPages() end
        extraReaders.bookName = function() return item:getBookName() end
    end
    if isWeapon then
        extraReaders.jammed = function() return item:isJammed() end
        extraReaders.containsClip = function() return item:isContainsClip() end
        extraReaders.roundChambered = function() return item:isRoundChambered() end
        extraReaders.spentRoundChambered = function() return item:isSpentRoundChambered() end
        extraReaders.spentRoundCount = function() return item:getSpentRoundCount() end
        extraReaders.fireMode = function() return item:getFireMode() end
        extraReaders.magazineType = function() return item:getMagazineType() end
    end
    if isClothing then
        extraReaders.wetness = function() return item:getWetness() end
        extraReaders.dirtiness = function() return item:getDirtiness() end
    end
    for key, reader in pairs(extraReaders) do snapshot.extra[key] = readOptional(reader) end
    if (isClothing or isInventoryContainer)
        and BloodBodyPartType ~= nil and BloodBodyPartType.MAX ~= nil then
        local maximum = 0
        pcall(function() maximum = BloodBodyPartType.MAX:index() end)
        for index = 0, maximum - 1 do
            local bodyPart = BloodBodyPartType.FromIndex(index)
            local blood = readOptional(function() return item:getBlood(bodyPart) end)
            local dirt = readOptional(function() return item:getDirt(bodyPart) end)
            local patch = nil
            if isClothing then
                patch = readOptional(function() return item:getPatchType(bodyPart) end)
            end
            if (tonumber(blood) or 0) > 0 or (tonumber(dirt) or 0) > 0 or patch ~= nil then
                snapshot.bodyState = snapshot.bodyState or {}
                local state = { index = index, blood = blood, dirt = dirt }
                if patch ~= nil then
                    state.patch = {
                        tailorLevel = readOptional(function() return patch.tailorLvl end),
                        fabricType = readOptional(function() return patch.fabricType end),
                        hasHole = readOptional(function() return patch.hasHole end),
                    }
                end
                snapshot.bodyState[#snapshot.bodyState + 1] = state
            end
        end
    end
    local weaponParts = nil
    if isWeapon then weaponParts = readOptional(function() return item:getAllWeaponParts() end) end
    local weaponPartCount = 0
    if weaponParts ~= nil then pcall(function() weaponPartCount = weaponParts:size() end) end
    if weaponPartCount > 0 then
        snapshot.weaponParts = {}
        for index = 0, weaponPartCount - 1 do
            local partSnapshot, partError = Garage.snapshotItem(weaponParts:get(index), depth + 1, errors)
            if partSnapshot ~= nil then
                snapshot.weaponParts[#snapshot.weaponParts + 1] = partSnapshot
            else
                errors[#errors + 1] = tostring(partError or "weapon attachment capture failed")
            end
        end
    end
    local nested = nil
    if isInventoryContainer then
        pcall(function() nested = item:getItemContainer() end)
        if nested == nil then pcall(function() nested = item:getInventory() end) end
    end
    if nested ~= nil then snapshot.items = snapshotContainer(nested, depth + 1, errors) end
    return snapshot
end

local function snapshotPart(part, errors)
    local result = {
        id = readString(function() return part:getId() end, ""),
        index = readNumber(function() return part:getIndex() end, 0),
        condition = readNumber(function() return part:getCondition() end, 0),
        contentAmount = readNumber(function() return part:getContainerContentAmount() end, nil),
        modData = nil,
    }
    pcall(function() result.modData = copySerializable(part:getModData(), {}, 0) end)

    local installed = nil
    pcall(function() installed = part:getInventoryItem() end)
    if installed ~= nil then
        local itemSnapshot, itemError = Garage.snapshotItem(installed, 0, errors)
        if itemSnapshot == nil then errors[#errors + 1] = tostring(itemError) end
        result.installedItem = itemSnapshot
    end

    local container = nil
    pcall(function() container = part:getItemContainer() end)
    if container ~= nil then result.items = snapshotContainer(container, 0, errors) end

    local door = nil
    pcall(function() door = part:getDoor() end)
    if door ~= nil then
        result.door = {
            open = readBoolean(function() return door:isOpen() end, false),
            locked = readBoolean(function() return door:isLocked() end, false),
            lockBroken = readBoolean(function() return door:isLockBroken() end, false),
        }
    end

    local window = nil
    pcall(function() window = part:getWindow() end)
    if window ~= nil then
        result.window = {
            health = readNumber(function() return window:getHealth() end, 0),
            destroyed = readBoolean(function() return window:isDestroyed() end, false),
            open = readBoolean(function() return window:isOpen() end, false),
            openDelta = readNumber(function() return window:getOpenDelta() end, 0),
        }
    end
    return result
end

local function moveKeyToPlayerInventory(key, player)
    if key == nil or player == nil or player:isDead() then
        return false, "the ignition-key recipient is unavailable"
    end
    local target = nil
    pcall(function() target = player:getInventory() end)
    if target == nil then return false, "the ignition-key recipient has no inventory" end
    local source = nil
    pcall(function() source = key:getContainer() end)
    if source == target then return true end

    local removed = true
    if source ~= nil then
        removed = pcall(function() source:DoRemoveItem(key) end)
    end
    if not removed then return false, "the key could not be removed from its current container" end
    local added = pcall(function() target:AddItem(key) end)
    local finalContainer = nil
    pcall(function() finalContainer = key:getContainer() end)
    if not added or finalContainer ~= target then
        pcall(function()
            if finalContainer ~= nil then finalContainer:DoRemoveItem(key) end
            if source ~= nil then source:AddItem(key) end
        end)
        return false, "the key could not be added to the owner's inventory"
    end
    pcall(function() key:getModData().keyRing = nil end)
    if sendAddItemToContainer then pcall(function() sendAddItemToContainer(target, key) end) end
    return true
end

-- A running vehicle's key is a real inventory item held by the vehicle's
-- private ignition container. Removing the vehicle while it is there destroys
-- the item. PZ's native operation returns that same object to the driver; an
-- optional recipient lets hideout storage force it into the vehicle owner's
-- inventory even when nobody remains in the driver's seat.
function Garage.returnIgnitionKeyToDriver(vehicle, targetPlayer)
    if vehicle == nil then return false, "vehicle is unavailable" end
    local inIgnition = false
    local stateRead = pcall(function() inIgnition = vehicle:isKeysInIgnition() == true end)
    if not stateRead then return false, "the vehicle ignition state is unavailable" end
    if not inIgnition then return true, nil end

    local driver, key = nil, nil
    pcall(function() driver = vehicle:getDriver() end)
    pcall(function() key = vehicle:getCurrentKey() end)
    if key == nil then
        return false, "the vehicle reports an ignition key but its key item is unavailable"
    end
    local recipient = targetPlayer or driver
    if recipient == nil or recipient:isDead() then
        return false, "the ignition key cannot be returned because its owner is unavailable"
    end

    -- With no living driver, both native methods intentionally return without
    -- doing anything. Move the actual key object out of the private ignition
    -- container and into the requested owner's inventory instead. The vehicle
    -- is immediately being stored, so a stale keysInIgnition boolean is harmless;
    -- currentKey is cleared to ensure the live object no longer owns the item.
    if driver == nil or driver:isDead() then
        local moved, moveError = moveKeyToPlayerInventory(key, recipient)
        if not moved then return false, moveError end
        pcall(function() vehicle:setCurrentKey(nil) end)
        pcall(function() vehicle:setKeysInIgnition(false) end)
        return true, {
            driver = recipient,
            keyId = readNumber(function() return key:getKeyId() end, nil),
            method = "forced-owner",
            ignitionStateMayBeStale = true,
        }
    end

    local removed, removeError = pcall(function() vehicle:removeKeyFromIgnition() end)
    if not removed then
        return false, "Project Zomboid could not return the ignition key: " .. tostring(removeError)
    end
    local stillInIgnition = true
    pcall(function() stillInIgnition = vehicle:isKeysInIgnition() == true end)
    local currentKey = key
    pcall(function() currentKey = vehicle:getCurrentKey() end)

    -- Build 42 splits this vanilla operation by runtime. The method above is
    -- server-only and silently returns in single-player; setKeysInIgnition(false)
    -- is the local path that restores the actual key to the inventory/key ring.
    -- It is safe as a fallback because it does nothing on the dedicated server.
    local returnMethod = "server"
    if stillInIgnition or currentKey ~= nil then
        local toggled, toggleError = pcall(function() vehicle:setKeysInIgnition(false) end)
        if not toggled then
            return false, "Project Zomboid could not return the single-player ignition key: "
                .. tostring(toggleError)
        end
        returnMethod = "singleplayer"
        stillInIgnition = true
        pcall(function() stillInIgnition = vehicle:isKeysInIgnition() == true end)
        currentKey = key
        pcall(function() currentKey = vehicle:getCurrentKey() end)
    end

    -- Do not use containsRecursive(key) as the success condition here.  The
    -- native operation may return the key to its original nested key ring, and
    -- that inventory lookup can remain false during this authoritative tick.
    -- PZ adds the key to the driver's chosen inventory container before it
    -- clears both ignition fields, so those fields are the reliable completion
    -- signal (and avoid falsely cancelling every vehicle extraction).
    if stillInIgnition or currentKey ~= nil then
        return false, "Project Zomboid did not clear the vehicle's ignition key state"
    end
    if recipient ~= driver then
        local moved, moveError = moveKeyToPlayerInventory(key, recipient)
        if not moved then return false, moveError end
        returnMethod = returnMethod .. "-owner"
    end
    return true, {
        driver = recipient,
        keyId = readNumber(function() return key:getKeyId() end, nil),
        method = returnMethod,
    }
end

function Garage.captureVehicle(vehicle)
    if vehicle == nil then return nil, "vehicle is unavailable" end
    local script = nil
    pcall(function() script = vehicle:getScript() end)
    local scriptName = readString(function() return vehicle:getScriptName() end, "")
    if scriptName == "" then return nil, "vehicle script is unavailable" end

    local modelKey = scriptName
    if script ~= nil then
        modelKey = readString(function()
            return script:getCarModelName() or script:getName()
        end, scriptName)
    end

    local gasTank = nil
    local batteryPart = nil
    pcall(function() gasTank = vehicle:getPartById("GasTank") end)
    pcall(function() batteryPart = vehicle:getPartById("Battery") end)
    local batteryItem = nil
    if batteryPart ~= nil then pcall(function() batteryItem = batteryPart:getInventoryItem() end) end

    local errors = {}
    local snapshot = {
        schema = SNAPSHOT_SCHEMA,
        scriptName = scriptName,
        modelKey = modelKey,
        skinIndex = readNumber(function() return vehicle:getSkinIndex() end, 0),
        colorHue = clamp(readNumber(function() return vehicle:getColorHue() end, 0), 0, 1),
        colorSaturation = clamp(readNumber(function() return vehicle:getColorSaturation() end, 0), 0, 1),
        colorValue = clamp(readNumber(function() return vehicle:getColorValue() end, 0.5), 0, 1),
        rust = clamp(readNumber(function() return vehicle:getRust() end, 0), 0, 1),
        engineCondition = clamp(readNumber(function() return vehicle:getEngineCondition() end, 0), 0, 100),
        engineQuality = readNumber(function() return vehicle:getEngineQuality() end, 0),
        engineLoudness = readNumber(function() return vehicle:getEngineLoudness() end, 0),
        enginePower = readNumber(function() return vehicle:getEnginePower() end, 0),
        keyId = readNumber(function() return vehicle:getKeyId() end, nil),
        mechanicalId = readNumber(function() return vehicle:getMechanicalID() end, 0),
        hotwired = readBoolean(function() return vehicle:isHotwired() end, false),
        hotwiredBroken = readBoolean(function() return vehicle:isHotwiredBroken() end, false),
        alarmed = readBoolean(function() return vehicle:isAlarmed() end, false),
        fuel = gasTank and math.max(0,
            readNumber(function() return gasTank:getContainerContentAmount() end, 0)) or 0,
        fuelCapacity = gasTank and math.max(0,
            readNumber(function() return gasTank:getContainerCapacity() end, 0)) or 0,
        batteryPresent = batteryItem ~= nil,
        batteryCharge = batteryItem and clamp(
            readNumber(function() return batteryItem:getCurrentUsesFloat() end, 0) * 100, 0, 100) or nil,
        blood = {},
        parts = {},
        capturedWorldHours = readNumber(function()
            return getGameTime():getWorldAgeHours()
        end, 0),
        compatibility = ExtractionMode.ModCompatibility.captureVehicleMetadata(vehicle),
    }
    for _, area in ipairs({ "Front", "Rear", "Left", "Right" }) do
        snapshot.blood[area] = clamp(readNumber(function()
            return vehicle:getBloodIntensity(area)
        end, 0), 0, 1)
    end
    local partCount = math.max(0, math.floor(readNumber(function() return vehicle:getPartCount() end, 0)))
    for index = 0, partCount - 1 do
        local part = nil
        pcall(function() part = vehicle:getPartByIndex(index) end)
        if part ~= nil then snapshot.parts[#snapshot.parts + 1] = snapshotPart(part, errors) end
    end
    if #errors > 0 then
        return nil, "vehicle cargo could not be captured safely: " .. table.concat(errors, "; ")
    end
    return snapshot
end

local function appendItemSignature(values, item)
    if type(item) ~= "table" then values[#values + 1] = "none"; return end
    local signatureType = tostring(item.fullType or "missing")
    -- Only a dynamic Base.<sprite-name> needs normalization. Scripted vanilla
    -- furniture such as Base.Mov_WaterDispenser also exposes a worldSprite, but
    -- its full type is stable and must remain the verification identity. Using
    -- worldSprite for every Moveable made pre-update records disagree with every
    -- newly captured copy containing sinks, toilets, dispensers, and similar
    -- cargo.
    local dynamicModule, dynamicSprite = signatureType:match("^([^%.]+)%.(.+_%d+_%d+)$")
    if dynamicModule ~= "Base" and dynamicModule ~= "Moveables" then dynamicSprite = nil end
    if (signatureType == "Base.Moveable" or signatureType == "Moveables.Moveable")
        and tostring(item.worldSprite or "") ~= "" then
        dynamicSprite = tostring(item.worldSprite)
    end
    dynamicSprite = dynamicSprite or ""
    values[#values + 1] = dynamicSprite ~= ""
        and ("moveable:" .. dynamicSprite) or signatureType
    values[#values + 1] = "attachments["
    for _, attachment in ipairs(item.weaponParts or {}) do appendItemSignature(values, attachment) end
    values[#values + 1] = "]items["
    for _, nested in ipairs(item.items or {}) do appendItemSignature(values, nested) end
    values[#values + 1] = "]"
end

function Garage.cargoSignature(record)
    local values = {}
    for _, part in ipairs(type(record) == "table" and record.parts or {}) do
        values[#values + 1] = "part:" .. tostring(part.id or part.index or "unknown")
        appendItemSignature(values, part.installedItem)
        values[#values + 1] = "cargo["
        for _, item in ipairs(part.items or {}) do appendItemSignature(values, item) end
        values[#values + 1] = "]"
    end
    return table.concat(values, "|")
end

function Garage.ensureState(root)
    root.personalGarages = root.personalGarages or {}
    root.pendingGarageOwnerKeys = type(root.pendingGarageOwnerKeys) == "table"
        and root.pendingGarageOwnerKeys or {}
    root.nextGarageVehicleId = math.max(0, math.floor(tonumber(root.nextGarageVehicleId) or 0))
    root.garageTransactions = type(root.garageTransactions) == "table" and root.garageTransactions or {}
    root.garageBackupGeneration = math.max(0,
        math.floor(tonumber(root.garageBackupGeneration) or 0))
    if root.garageDoorUnlocked ~= true then
        local hasGarageHistory = root.nextGarageVehicleId > 0
        if not hasGarageHistory then
            for _, records in pairs(root.personalGarages) do
                if type(records) == "table" then
                    for _ in pairs(records) do hasGarageHistory = true break end
                end
                if hasGarageHistory then break end
            end
        end
        root.garageDoorUnlocked = hasGarageHistory
    end
    return root.personalGarages
end

local function copyValue(value, seen)
    if type(value) ~= "table" then return value end
    seen = seen or {}
    if seen[value] then return seen[value] end
    local result = {}
    seen[value] = result
    for key, nested in pairs(value) do result[copyValue(key, seen)] = copyValue(nested, seen) end
    return result
end

function Garage.refreshBackup(root, reason)
    if root == nil or ModData == nil or ModData.getOrCreate == nil then return false end
    Garage.ensureState(root)
    root.garageBackupGeneration = root.garageBackupGeneration + 1
    local backup = ModData.getOrCreate(ExtractionMode.Config.GARAGE_BACKUP_DATA_KEY)
    for key in pairs(backup) do backup[key] = nil end
    backup.schema = BACKUP_SCHEMA
    backup.generation = root.garageBackupGeneration
    backup.reason = tostring(reason or "garage update")
    backup.personalGarages = copyValue(root.personalGarages)
    backup.nextGarageVehicleId = root.nextGarageVehicleId
    backup.activeHideoutVehicle = copyValue(root.activeHideoutVehicle)
    backup.pendingGarageVehicleRemovals = copyValue(root.pendingGarageVehicleRemovals)
    backup.pendingGarageOwnerKeys = copyValue(root.pendingGarageOwnerKeys)
    backup.garageTransactions = copyValue(root.garageTransactions)
    backup.garageDoorUnlocked = root.garageDoorUnlocked == true
    return true
end

function Garage.recoverBackup(root)
    if root == nil or ModData == nil or ModData.get == nil then return false end
    local backup = ModData.get(ExtractionMode.Config.GARAGE_BACKUP_DATA_KEY)
    if type(backup) ~= "table" or tonumber(backup.schema) ~= BACKUP_SCHEMA
        or type(backup.personalGarages) ~= "table" then return false end
    local rootGeneration = tonumber(root.garageBackupGeneration) or 0
    if type(root.personalGarages) == "table"
        and rootGeneration >= (tonumber(backup.generation) or 0) then return false end
    root.personalGarages = copyValue(backup.personalGarages)
    root.nextGarageVehicleId = tonumber(backup.nextGarageVehicleId) or 0
    root.activeHideoutVehicle = copyValue(backup.activeHideoutVehicle)
    root.pendingGarageVehicleRemovals = copyValue(backup.pendingGarageVehicleRemovals) or {}
    root.pendingGarageOwnerKeys = copyValue(backup.pendingGarageOwnerKeys) or {}
    root.garageTransactions = copyValue(backup.garageTransactions) or {}
    root.garageDoorUnlocked = backup.garageDoorUnlocked == true
        or (tonumber(root.nextGarageVehicleId) or 0) > 0
    root.garageBackupGeneration = tonumber(backup.generation) or 0
    return true
end

function Garage.beginExtractionTransaction(root, transactionId, owner, snapshot, source)
    Garage.ensureState(root)
    transactionId = tostring(transactionId or "")
    if transactionId == "" or tostring(owner or "") == "" or type(snapshot) ~= "table" then
        return nil, "invalid extraction transaction"
    end
    local existing = root.garageTransactions[transactionId]
    if type(existing) == "table" then return existing end
    local transaction = {
        id = transactionId,
        kind = "EXTRACT",
        stage = "CAPTURED",
        owner = tostring(owner),
        snapshot = snapshot,
        vehicleId = source and tostring(source.vehicleId or "") or "",
        scriptName = source and source.scriptName or snapshot.scriptName,
        x = source and source.x or nil,
        y = source and source.y or nil,
        z = source and source.z or nil,
    }
    root.garageTransactions[transactionId] = transaction
    Garage.refreshBackup(root, "raid vehicle captured")
    return transaction
end

function Garage.commitExtractionTransaction(root, transactionId)
    Garage.ensureState(root)
    local transaction = root.garageTransactions[tostring(transactionId or "")]
    if type(transaction) ~= "table" or type(transaction.snapshot) ~= "table" then
        return nil, "extraction transaction is unavailable"
    end
    local record = transaction.snapshot
    local existing = record.id and Garage.record(root, transaction.owner, record.id) or nil
    if existing == nil then
        local garageId, storeError = nil, nil
        if record.id ~= nil then
            local putOk, putError = Garage.put(root, transaction.owner, record)
            if putOk then garageId = record.id else storeError = putError end
        else
            garageId, storeError = Garage.store(root, transaction.owner, record)
        end
        if garageId == nil then return nil, storeError end
    end
    transaction.garageId = record.id
    transaction.stage = "COMMITTED"
    root.pendingGarageVehicleRemovals = root.pendingGarageVehicleRemovals or {}
    if transaction.vehicleId ~= "" then
        -- PZ recycles its short vehicle IDs. Keep extraction removals keyed by
        -- their durable transaction ID so two raids cannot replace each other's
        -- retry record after a chunk unload or server restart.
        local removalKey = tostring(transaction.id)
        root.pendingGarageVehicleRemovals[removalKey] = {
            vehicleId = transaction.vehicleId,
            owner = transaction.owner,
            garageId = transaction.garageId,
            scriptName = transaction.scriptName,
            x = transaction.x,
            y = transaction.y,
            z = transaction.z,
            transactionId = transaction.id,
        }
        -- Migrate the old short-ID key when recovering an in-flight transaction.
        local legacy = root.pendingGarageVehicleRemovals[transaction.vehicleId]
        if transaction.vehicleId ~= removalKey and type(legacy) == "table"
            and tostring(legacy.transactionId or "") == tostring(transaction.id) then
            root.pendingGarageVehicleRemovals[transaction.vehicleId] = nil
        end
    end
    Garage.refreshBackup(root, "raid vehicle committed")
    return record.id, record
end

function Garage.finishTransaction(root, transactionId, reason)
    Garage.ensureState(root)
    if transactionId == nil then return false end
    root.garageTransactions[tostring(transactionId)] = nil
    Garage.refreshBackup(root, reason or "garage transaction completed")
    return true
end

function Garage.recoverTransactions(root)
    Garage.ensureState(root)
    local recovered = 0
    for transactionId, transaction in pairs(root.garageTransactions) do
        if type(transaction) == "table" and transaction.kind == "EXTRACT" then
            local garageId = Garage.commitExtractionTransaction(root, transactionId)
            if garageId ~= nil then recovered = recovered + 1 end
        end
    end
    return recovered
end

function Garage.sanitizeName(value)
    local name = tostring(value or ""):gsub("[%c]", " ")
    name = name:gsub("^%s+", ""):gsub("%s+$", ""):gsub("%s%s+", " ")
    if #name > MAX_NAME_LENGTH then name = name:sub(1, MAX_NAME_LENGTH) end
    return name
end

local function defaultNameKey(record)
    return tostring(record and (record.modelKey or record.scriptName) or "Vehicle")
end

-- Keep saved names distinct without storing localized display text in server
-- data. Clients translate default model keys, then append this ordinal.
local function assignUniqueStoredName(garage, record)
    if type(record) ~= "table" then return end
    if record.customName == true and Garage.sanitizeName(record.name) == "" then
        record.customName = false
    end

    local customName = record.customName == true
    local nameKey = customName and Garage.sanitizeName(record.name) or defaultNameKey(record)
    local comparisonKey = customName and string.lower(nameKey) or nameKey
    local used = {}
    for _, existing in ipairs(garage or {}) do
        if existing ~= record and type(existing) == "table" then
            local existingCustom = existing.customName == true
            local existingKey = existingCustom and string.lower(Garage.sanitizeName(existing.name))
                or defaultNameKey(existing)
            if existingCustom == customName and existingKey == comparisonKey then
                local ordinal = math.max(1, math.floor(tonumber(existing.nameOrdinal) or 1))
                if used[ordinal] == true then
                    ordinal = 1
                    while used[ordinal] == true do ordinal = ordinal + 1 end
                end
                used[ordinal] = true
                existing.nameOrdinal = ordinal > 1 and ordinal or nil
            end
        end
    end

    local ordinal = math.max(1, math.floor(tonumber(record.nameOrdinal) or 1))
    if used[ordinal] == true then
        ordinal = 1
        while used[ordinal] == true do ordinal = ordinal + 1 end
    end
    record.name = nameKey
    record.customName = customName
    record.nameOrdinal = ordinal > 1 and ordinal or nil
end

function Garage.store(root, username, snapshot)
    if root == nil or username == nil or username == "" or type(snapshot) ~= "table" then
        return nil, "invalid garage record"
    end
    Garage.ensureState(root)
    root.nextGarageVehicleId = root.nextGarageVehicleId + 1
    snapshot.id = "garage-" .. tostring(root.nextGarageVehicleId)
    snapshot.customName = false
    local garage = root.personalGarages[username]
    if type(garage) ~= "table" then
        garage = {}
        root.personalGarages[username] = garage
    end
    assignUniqueStoredName(garage, snapshot)
    garage[#garage + 1] = snapshot
    -- The authored garage is a shared hideout facility. The first successfully
    -- committed vehicle permanently opens access for the cooperative group;
    -- transfers, deletion, and temporary deployment never relock it.
    root.garageDoorUnlocked = true
    Garage.refreshBackup(root, "vehicle stored")
    return snapshot.id
end

local function recordFor(root, username, garageId)
    Garage.ensureState(root)
    local garage = root.personalGarages[username]
    if type(garage) ~= "table" then return nil, nil, garage end
    for index, record in ipairs(garage) do
        if tostring(record.id) == tostring(garageId) then return record, index, garage end
    end
    return nil, nil, garage
end

function Garage.record(root, username, garageId)
    return recordFor(root, username, garageId)
end

function Garage.take(root, username, garageId)
    Garage.ensureState(root)
    for _, transaction in pairs(root.garageTransactions) do
        if type(transaction) == "table" and tostring(transaction.owner) == tostring(username)
            and tostring(transaction.garageId) == tostring(garageId) then
            return nil, nil, "That vehicle is still completing its extraction transaction."
        end
    end
    local record, index, garage = recordFor(root, username, garageId)
    if record == nil then return nil, nil, "That garage vehicle no longer exists." end
    table.remove(garage, index)
    return record, index
end

function Garage.put(root, username, record, requestedIndex)
    if root == nil or username == nil or username == "" or type(record) ~= "table" then
        return false, "invalid garage record"
    end
    Garage.ensureState(root)
    local garage = root.personalGarages[username]
    if type(garage) ~= "table" then
        garage = {}
        root.personalGarages[username] = garage
    end
    for _, existing in ipairs(garage) do
        if tostring(existing.id) == tostring(record.id) then
            return false, "That vehicle is already in the destination garage."
        end
    end
    assignUniqueStoredName(garage, record)
    local index = math.max(1, math.min(#garage + 1,
        math.floor(tonumber(requestedIndex) or (#garage + 1))))
    table.insert(garage, index, record)
    return true
end

function Garage.mergeRecord(snapshot, previous)
    if type(snapshot) ~= "table" then return previous end
    if type(previous) == "table" then
        snapshot.id = previous.id
        snapshot.name = previous.name
        snapshot.customName = previous.customName == true
        snapshot.nameOrdinal = previous.nameOrdinal
        if snapshot.compatibility == nil then snapshot.compatibility = previous.compatibility end
    end
    return snapshot
end

function Garage.transfer(root, fromUsername, toUsername, garageId)
    if tostring(fromUsername or "") == tostring(toUsername or "") then
        return false, "Choose a different player."
    end
    local record, index, message = Garage.take(root, fromUsername, garageId)
    if record == nil then return false, message end
    local ok, putMessage = Garage.put(root, toUsername, record)
    if not ok then
        Garage.put(root, fromUsername, record, index)
        return false, putMessage
    end
    Garage.refreshBackup(root, "vehicle transferred")
    return true, record
end

function Garage.rename(root, username, garageId, requestedName)
    local record, _, garage = recordFor(root, username, garageId)
    if record == nil then return false, "That garage vehicle no longer exists." end
    local name = Garage.sanitizeName(requestedName)
    if name == "" then return false, "Vehicle names cannot be blank." end
    record.name = name
    record.customName = true
    record.nameOrdinal = nil
    assignUniqueStoredName(garage, record)
    Garage.refreshBackup(root, "vehicle renamed")
    return true
end

function Garage.delete(root, username, garageId)
    Garage.ensureState(root)
    for _, transaction in pairs(root.garageTransactions) do
        if type(transaction) == "table" and tostring(transaction.owner) == tostring(username)
            and tostring(transaction.garageId) == tostring(garageId) then
            return false, "That vehicle is still completing its extraction transaction."
        end
    end
    local record, index, garage = recordFor(root, username, garageId)
    if record == nil then return false, "That garage vehicle no longer exists." end
    table.remove(garage, index)
    Garage.refreshBackup(root, "vehicle deleted")
    return true, record
end

function Garage.summaries(root, username)
    Garage.ensureState(root)
    local result = {}
    for _, record in ipairs(root.personalGarages[username] or {}) do
        local transactionPending = false
        for _, transaction in pairs(root.garageTransactions) do
            if type(transaction) == "table" and tostring(transaction.owner) == tostring(username)
                and tostring(transaction.garageId) == tostring(record.id) then
                transactionPending = true
                break
            end
        end
        result[#result + 1] = {
            id = record.id,
            name = record.name,
            customName = record.customName == true,
            nameOrdinal = record.nameOrdinal,
            scriptName = record.scriptName,
            modelKey = record.modelKey,
            skinIndex = record.skinIndex,
            colorHue = record.colorHue,
            colorSaturation = record.colorSaturation,
            colorValue = record.colorValue,
            fuel = record.fuel,
            fuelCapacity = record.fuelCapacity,
            batteryPresent = record.batteryPresent == true,
            batteryCharge = record.batteryCharge,
            engineCondition = record.engineCondition,
            transactionPending = transactionPending,
        }
    end
    return result
end

ExtractionMode.Garage = Garage
return Garage
