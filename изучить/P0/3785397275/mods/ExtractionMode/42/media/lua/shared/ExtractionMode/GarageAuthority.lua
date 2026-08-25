require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/Garage"
require "ExtractionMode/ModCompatibility"

ExtractionMode = ExtractionMode or {}

local Authority = {}
local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Garage = ExtractionMode.Garage
local Compatibility = ExtractionMode.ModCompatibility

local STORE_SETTLE_MS = 1500
local OWNER_DISCONNECT_GRACE_MS = 2000
local STARTUP_OWNER_GRACE_MS = 15000
local LIVE_CAPTURE_INTERVAL_MS = 5000
local MECHANICS_SAFETY_RADIUS = 8
local SWAP_RETRY_DELAY_MS = 250
local SWAP_RETRY_TIMEOUT_MS = 15000
local runtime = {
    vehicle = nil,
    activeToken = nil,
    sessionStartedMs = 0,
    ownerSeen = false,
    ownerAbsentSinceMs = nil,
    lastCaptureMs = 0,
    lastInteractionMs = 0,
    interactionFingerprint = nil,
    inactiveState = false,
    driverUsername = nil,
    pendingRemovalMissingSince = {},
    pendingSwap = nil,
}

local function copyInto(destination, source, depth)
    if destination == nil or type(source) ~= "table" or (tonumber(depth) or 0) >= 16 then return end
    for key in pairs(destination) do destination[key] = nil end
    for key, value in pairs(source) do
        if type(value) == "table" then
            destination[key] = {}
            copyInto(destination[key], value, (tonumber(depth) or 0) + 1)
        elseif type(value) == "string" or type(value) == "number" or type(value) == "boolean" then
            destination[key] = value
        end
    end
end

local function isItemType(item, className)
    if item == nil or instanceof == nil then return false end
    local matches = false
    pcall(function() matches = instanceof(item, className) == true end)
    return matches
end

local function moveableSpriteName(snapshot)
    if type(snapshot) ~= "table" then return nil end
    local explicit = tostring(snapshot.worldSprite or "")
    if explicit ~= "" then return explicit end

    -- Older garage records predate the explicit worldSprite field. Build 42
    -- reports a generic picked-up Moveable as Base.<world-sprite>, although
    -- that value is not registered with ScriptManager as a normal Base item.
    return tostring(snapshot.fullType or ""):match("^Base%.(.+_%d+_%d+)$")
end

local function restoreMoveable(snapshot)
    local spriteName = moveableSpriteName(snapshot)
    if spriteName == nil or spriteName == "" then return nil end

    -- This is the same construction route used by vanilla's
    -- ISMoveableSpriteProps: single-tile furniture gets its sprite-specific
    -- Moveables type, while the generic item remains a safe fallback for
    -- multi-sprite/special objects.
    local candidates = { "Moveables." .. spriteName, "Moveables.Moveable" }
    for _, itemType in ipairs(candidates) do
        local item = nil
        local restored = pcall(function()
            item = instanceItem(itemType)
            local spriteRestored = item ~= nil and isItemType(item, "Moveable")
                and item:ReadFromWorldSprite(spriteName)
            if not spriteRestored then item = nil end
        end)
        if restored and item ~= nil then return item end
    end
    return nil
end

local function restoreItem(snapshot)
    if type(snapshot) ~= "table" or tostring(snapshot.fullType or "") == "" then return nil end
    local item = nil
    local scriptItem = nil
    local scriptLookupCompleted = pcall(function()
        if ScriptManager ~= nil and ScriptManager.instance ~= nil then
            scriptItem = ScriptManager.instance:FindItem(tostring(snapshot.fullType))
        else
            error("ScriptManager is unavailable")
        end
    end)
    -- Avoid asking instanceItem for a dynamic Base.<sprite> type: doing so
    -- emits the misleading "missing item type" error before returning nil.
    if scriptItem ~= nil or not scriptLookupCompleted then
        pcall(function() item = instanceItem(snapshot.fullType) end)
    end
    if item == nil then item = restoreMoveable(snapshot) end
    if item == nil then return nil, "Missing item type " .. tostring(snapshot.fullType) end
    local isFood = isItemType(item, "Food")
    local isDrainable = isItemType(item, "DrainableComboItem")
    local isLiterature = isItemType(item, "Literature")
    local isWeapon = isItemType(item, "HandWeapon")
    local isClothing = isItemType(item, "Clothing")
    local isInventoryContainer = isItemType(item, "InventoryContainer")
    if snapshot.conditionMax ~= nil then
        pcall(function() item:setConditionMax(math.max(0, math.floor(tonumber(snapshot.conditionMax) or 0))) end)
    end
    if snapshot.condition ~= nil then
        pcall(function() item:setCondition(math.max(0, math.floor(tonumber(snapshot.condition) or 0))) end)
    end
    if snapshot.currentUsesFloat ~= nil then
        pcall(function() item:setCurrentUsesFloat(tonumber(snapshot.currentUsesFloat) or 0) end)
    elseif snapshot.currentUses ~= nil then
        pcall(function() item:setCurrentUses(math.max(0, math.floor(tonumber(snapshot.currentUses) or 0))) end)
    end
    if isClothing and snapshot.usedDelta ~= nil then
        pcall(function() item:setUsedDelta(tonumber(snapshot.usedDelta) or 0) end)
    end
    if snapshot.age ~= nil then pcall(function() item:setAge(tonumber(snapshot.age) or 0) end) end
    pcall(function() item:setFavorite(snapshot.favorite == true) end)
    pcall(function() item:setActivated(snapshot.activated == true) end)
    if snapshot.customName == true and snapshot.name ~= nil then
        pcall(function() item:setName(tostring(snapshot.name)); item:setCustomName(true) end)
    end
    if snapshot.keyId ~= nil then
        pcall(function() item:setKeyId(math.floor(tonumber(snapshot.keyId) or 0)) end)
    end
    if type(snapshot.modData) == "table" then
        pcall(function() copyInto(item:getModData(), snapshot.modData, 0) end)
    end
    local extra = type(snapshot.extra) == "table" and snapshot.extra or {}
    local extraWriters = {
        cooked = function(value) item:setCooked(value == true) end,
        burnt = function(value) item:setBurnt(value == true) end,
        wet = function(value) item:setWet(value == true) end,
        itemCapacity = function(value) item:setItemCapacity(tonumber(value) or 0) end,
        currentAmmoCount = function(value) item:setCurrentAmmoCount(math.floor(tonumber(value) or 0)) end,
        recordedMediaIndex = function(value) item:setRecordedMediaIndexInteger(math.floor(tonumber(value) or -1)) end,
        bloodLevel = function(value) item:setBloodLevel(tonumber(value) or 0) end,
    }
    if isFood then
        extraWriters.tainted = function(value) item:setTainted(value == true) end
        extraWriters.frozen = function(value) item:setFrozen(value == true) end
        extraWriters.freezingTime = function(value) item:setFreezingTime(tonumber(value) or 0) end
        extraWriters.poisonPower = function(value)
            item:setPoisonPower(math.floor(tonumber(value) or 0))
        end
    end
    if isFood or isDrainable then
        extraWriters.heat = function(value) item:setHeat(tonumber(value) or 0) end
    end
    if isLiterature then
        extraWriters.alreadyReadPages = function(value)
            item:setAlreadyReadPages(math.floor(tonumber(value) or 0))
        end
        extraWriters.bookName = function(value) item:setBookName(tostring(value)) end
    end
    if isWeapon then
        extraWriters.jammed = function(value) item:setJammed(value == true) end
        extraWriters.containsClip = function(value) item:setContainsClip(value == true) end
        extraWriters.roundChambered = function(value) item:setRoundChambered(value == true) end
        extraWriters.spentRoundChambered = function(value)
            item:setSpentRoundChambered(value == true)
        end
        extraWriters.spentRoundCount = function(value)
            item:setSpentRoundCount(math.floor(tonumber(value) or 0))
        end
        extraWriters.fireMode = function(value) item:setFireMode(tostring(value)) end
        extraWriters.magazineType = function(value) item:setMagazineType(tostring(value)) end
    end
    if isClothing then
        extraWriters.wetness = function(value) item:setWetness(tonumber(value) or 0) end
        extraWriters.dirtiness = function(value) item:setDirtiness(tonumber(value) or 0) end
    end
    for key, writer in pairs(extraWriters) do
        if extra[key] ~= nil then pcall(function() writer(extra[key]) end) end
    end
    if (isClothing or isInventoryContainer)
        and type(snapshot.bodyState) == "table" and BloodBodyPartType ~= nil then
        if isClothing then pcall(function() item:removeAllPatches() end) end
        for _, state in ipairs(snapshot.bodyState) do
            local bodyPart = BloodBodyPartType.FromIndex(math.floor(tonumber(state.index) or 0))
            if state.blood ~= nil then
                pcall(function() item:setBlood(bodyPart, tonumber(state.blood) or 0) end)
            end
            if state.dirt ~= nil then
                pcall(function() item:setDirt(bodyPart, tonumber(state.dirt) or 0) end)
            end
            if isClothing and type(state.patch) == "table" then
                pcall(function()
                    item:addPatchForSync(math.floor(tonumber(state.index) or 0),
                        math.floor(tonumber(state.patch.tailorLevel) or 0),
                        math.floor(tonumber(state.patch.fabricType) or 0),
                        state.patch.hasHole == true)
                end)
            end
        end
    end
    if isWeapon and type(snapshot.weaponParts) == "table" then
        pcall(function() item:detachAllWeaponParts() end)
        for _, partSnapshot in ipairs(snapshot.weaponParts) do
            local weaponPart, weaponPartError = restoreItem(partSnapshot)
            if weaponPart == nil then return nil, weaponPartError end
            local attached = pcall(function() item:attachWeaponPart(weaponPart, true) end)
            if not attached then
                return nil, "Could not restore attachment " .. tostring(partSnapshot.fullType)
            end
        end
    end
    if type(snapshot.items) == "table" then
        local container = nil
        if isInventoryContainer then
            pcall(function() container = item:getItemContainer() end)
            if container == nil then pcall(function() container = item:getInventory() end) end
        end
        if container ~= nil then
            pcall(function() container:clear() end)
            for _, nestedSnapshot in ipairs(snapshot.items) do
                local nested, nestedError = restoreItem(nestedSnapshot)
                if nested == nil then return nil, nestedError end
                local added = pcall(function() container:AddItem(nested) end)
                if not added then return nil, "Could not restore nested item " .. tostring(nestedSnapshot.fullType) end
            end
        elseif #snapshot.items > 0 then
            return nil, "Container unavailable for " .. tostring(snapshot.fullType)
        end
    end
    return item
end

local function transmitRestoredPart(vehicle, part)
    pcall(function() vehicle:transmitPartItem(part) end)
    pcall(function() vehicle:transmitPartCondition(part) end)
    pcall(function() vehicle:transmitPartUsedDelta(part) end)
    pcall(function() vehicle:transmitPartModData(part) end)
    pcall(function() vehicle:transmitPartDoor(part) end)
    pcall(function() vehicle:transmitPartWindow(part) end)
end

local function restorePart(vehicle, snapshot)
    if type(snapshot) ~= "table" then return false, "Invalid vehicle part snapshot" end
    local part = nil
    pcall(function() part = vehicle:getPartById(tostring(snapshot.id or "")) end)
    if part == nil then return false, "Missing vehicle part " .. tostring(snapshot.id) end

    local installed, installedError = restoreItem(snapshot.installedItem)
    if snapshot.installedItem ~= nil and installed == nil then return false, installedError end
    pcall(function() part:setInventoryItem(installed) end)
    if snapshot.condition ~= nil then
        pcall(function() part:setCondition(math.max(0, math.floor(tonumber(snapshot.condition) or 0))) end)
    end
    if snapshot.contentAmount ~= nil then
        pcall(function() part:setContainerContentAmount(math.max(0,
            tonumber(snapshot.contentAmount) or 0)) end)
    end
    if type(snapshot.modData) == "table" then
        pcall(function() copyInto(part:getModData(), snapshot.modData, 0) end)
    end

    if type(snapshot.items) == "table" then
        local container = nil
        pcall(function() container = part:getItemContainer() end)
        if container ~= nil then
            pcall(function() container:clear() end)
            for _, itemSnapshot in ipairs(snapshot.items) do
                local item, itemError = restoreItem(itemSnapshot)
                if item == nil then return false, itemError end
                local added = pcall(function() container:AddItem(item) end)
                if not added then return false, "Could not restore vehicle cargo " .. tostring(itemSnapshot.fullType) end
            end
        elseif #snapshot.items > 0 then
            return false, "Cargo container unavailable for vehicle part " .. tostring(snapshot.id)
        end
    end

    if type(snapshot.door) == "table" then
        local door = nil
        pcall(function() door = part:getDoor() end)
        if door ~= nil then
            pcall(function() door:setOpen(snapshot.door.open == true) end)
            pcall(function() door:setLocked(snapshot.door.locked == true) end)
            pcall(function() door:setLockBroken(snapshot.door.lockBroken == true) end)
        end
    end
    if type(snapshot.window) == "table" then
        local window = nil
        pcall(function() window = part:getWindow() end)
        if window ~= nil then
            pcall(function() window:setOpen(snapshot.window.open == true) end)
            pcall(function() window:setOpenDelta(tonumber(snapshot.window.openDelta) or 0) end)
            if snapshot.window.destroyed == true then
                pcall(function() window:damage(math.max(1, window:getHealth() + 1)) end)
            end
        end
    end
    transmitRestoredPart(vehicle, part)
    return true
end

local function tagVehicle(vehicle, active)
    if vehicle == nil or type(active) ~= "table" then return end
    pcall(function()
        local modData = vehicle:getModData()
        modData.ExtractionModeHideoutVehicle = true
        modData.ExtractionModeGarageOwner = active.owner
        modData.ExtractionModeGarageVehicleId = active.record and active.record.id or nil
        if vehicle.transmitModData then vehicle:transmitModData() end
    end)
end

local function clearVehicleTag(vehicle)
    if vehicle == nil then return end
    pcall(function()
        local modData = vehicle:getModData()
        modData.ExtractionModeHideoutVehicle = nil
        modData.ExtractionModeGarageOwner = nil
        modData.ExtractionModeGarageVehicleId = nil
        if vehicle.transmitModData then vehicle:transmitModData() end
    end)
end

-- PZ's vehicle creation validation rejects indoor squares unless the square is
-- covered by a Vehicle/ParkingStall meta-zone.  The hideout garage is authored
-- as an interior room, so establish one deliberately small vehicle zone for the
-- single deployment tile.  Its custom name has no VehicleZoneDistribution
-- entry, preventing normal map vehicle generation from using the bay.
local function ensureHideoutVehicleZone(point, square)
    if square == nil then return false, "The hideout vehicle bay is not loaded." end
    local outside = false
    pcall(function() outside = square:isOutside() == true end)
    if outside then return true end

    local x, y, z = math.floor(point.x), math.floor(point.y), math.floor(point.z)
    local world = getWorld and getWorld() or nil
    if world == nil then return false, "The hideout vehicle bay could not be registered." end
    local metaGrid = nil
    pcall(function() metaGrid = world:getMetaGrid() end)
    local existing = nil
    if metaGrid ~= nil then
        pcall(function() existing = metaGrid:getVehicleZoneAt(x, y, z) end)
    end
    if existing ~= nil then return true end

    local zone = nil
    local registered = pcall(function()
        zone = world:registerVehiclesZone("ExtractionModeGarageBay", "ParkingStall",
            x, y, z, 1, 1, {})
    end)
    if not registered or zone == nil then
        return false, "Project Zomboid could not register the indoor hideout vehicle bay."
    end
    print(string.format("[ExtractionMode] Registered indoor garage vehicle bay at %d,%d,%d.", x, y, z))
    return true
end

local function createdVehicleIsUsable(vehicle)
    if vehicle == nil then return false end
    local script, square = nil, nil
    local scriptOk = pcall(function() script = vehicle:getScript() end)
    local squareOk = pcall(function() square = vehicle:getSquare() end)
    return scriptOk and squareOk and script ~= nil and square ~= nil
end

-- Zero engine power is the hideout's temporary movement lock, not a valid
-- persistent drivetrain value. Older records and interrupted transitions may
-- nevertheless contain zero. Repair those records from the vehicle script so a
-- reconstructed raid vehicle cannot keep the garage lock forever.
local function persistentEnginePower(vehicle, record)
    local power = math.floor(tonumber(record and record.enginePower) or 0)
    if power <= 0 and vehicle ~= nil then
        pcall(function()
            local script = vehicle:getScript()
            if script ~= nil then power = math.floor(tonumber(script:getEngineForce()) or 0) end
        end)
    end
    power = math.max(1, power)
    if type(record) == "table" then record.enginePower = power end
    return power
end

local function restoreVehicle(vehicle, record, point, deferEngineNetwork)
    if vehicle == nil or type(record) ~= "table" then return false, "Vehicle restoration failed." end
    local heading = tonumber(point.angleY) or tonumber(point.angleZ) or 0
    pcall(function() vehicle:setAngles(0, heading, 0) end)
    pcall(function() vehicle:setSkinIndex(math.max(0, math.floor(tonumber(record.skinIndex) or 0))) end)
    pcall(function() vehicle:setColorHSV(tonumber(record.colorHue) or 0,
        tonumber(record.colorSaturation) or 0, tonumber(record.colorValue) or 0.5) end)
    pcall(function() vehicle:setRust(tonumber(record.rust) or 0) end)
    pcall(function() vehicle:setEngineFeature(math.floor(tonumber(record.engineQuality) or 0),
        math.floor(tonumber(record.engineLoudness) or 0), persistentEnginePower(vehicle, record)) end)
    if record.keyId ~= nil then
        pcall(function() vehicle:setKeyId(math.floor(tonumber(record.keyId) or -1)) end)
    end
    pcall(function() vehicle:setMechanicalID(math.floor(tonumber(record.mechanicalId) or 0)) end)
    local forceHotwired = Config.value("HotwireExtractedVehicles") == true
    pcall(function() vehicle:setHotwired(forceHotwired or record.hotwired == true) end)
    pcall(function() vehicle:setHotwiredBroken(not forceHotwired and record.hotwiredBroken == true) end)
    pcall(function() vehicle:setAlarmed(record.alarmed == true) end)
    Compatibility.restoreVehicleMetadata(vehicle, record.compatibility)
    for area, intensity in pairs(record.blood or {}) do
        pcall(function() vehicle:setBloodIntensity(area, tonumber(intensity) or 0) end)
    end
    for _, partSnapshot in ipairs(record.parts or {}) do
        local restored, partError = restorePart(vehicle, partSnapshot)
        if not restored then return false, tostring(partError or "Vehicle part restoration failed") end
    end
    pcall(function() vehicle:updateParts() end)
    pcall(function() vehicle:updatePartStats() end)
    pcall(function() vehicle:transmitSkinIndex() end)
    pcall(function() vehicle:transmitColorHSV() end)
    pcall(function() vehicle:transmitRust() end)
    pcall(function() vehicle:transmitBlood() end)
    pcall(function() vehicle:transmitAlarmed() end)
    -- Cross-cell raid reconstruction creates the server vehicle before clients
    -- have initialized VehicleSounds. An immediate incremental engine packet can
    -- crash VehicleEngine.parse and discard the rest of that vehicle update.
    -- The driver client synchronizes the restored engine after seating instead.
    if deferEngineNetwork ~= true then pcall(function() vehicle:transmitEngine() end) end
    local verification, verificationError = Garage.captureVehicle(vehicle)
    if verification == nil then
        return false, "Vehicle restoration verification failed: " .. tostring(verificationError)
    end
    local actualSignature = Garage.cargoSignature(verification)
    local expectedSignature = Garage.cargoSignature(record)
    if actualSignature ~= expectedSignature then
        local mismatchAt = 1
        local maximum = math.min(#expectedSignature, #actualSignature)
        while mismatchAt <= maximum
            and string.sub(expectedSignature, mismatchAt, mismatchAt)
                == string.sub(actualSignature, mismatchAt, mismatchAt) do
            mismatchAt = mismatchAt + 1
        end
        local fragmentStart = math.max(1, mismatchAt - 80)
        local fragmentEnd = mismatchAt + 160
        Util.log("Vehicle cargo verification mismatch at character " .. tostring(mismatchAt)
            .. " expected=" .. string.sub(expectedSignature, fragmentStart, fragmentEnd)
            .. " actual=" .. string.sub(actualSignature, fragmentStart, fragmentEnd))
        return false, "Vehicle restoration verification found missing or changed cargo."
    end
    return true
end

local function ensureOwnerHasVehicleKey(vehicle, ownerPlayer)
    if vehicle == nil or ownerPlayer == nil or ownerPlayer:isDead() then
        return false, "The vehicle owner is unavailable for key delivery."
    end
    local inventory = ownerPlayer:getInventory()
    if inventory == nil then return false, "The vehicle owner has no available inventory." end
    local keyId = nil
    pcall(function() keyId = vehicle:getKeyId() end)
    keyId = tonumber(keyId)
    -- Vehicles without a registered key ID cannot have a functional matching
    -- key. Preserve their existing hotwire/lock state without manufacturing an
    -- unusable generic key.
    if keyId == nil or keyId < 0 then return true, false end

    -- ItemContainer:haveThisKeyId returns the matching InventoryItem (or nil),
    -- not a boolean. Comparing it with `true` caused every existing key to be
    -- treated as missing and manufactured another key on every deployment.
    local existingKey = nil
    pcall(function() existingKey = inventory:haveThisKeyId(math.floor(keyId)) end)
    if existingKey ~= nil and existingKey ~= false then return true, false end

    local key = nil
    local created = pcall(function() key = vehicle:createVehicleKey() end)
    if not created or key == nil then
        return false, "Project Zomboid could not create the replacement vehicle key."
    end
    pcall(function() key:setKeyId(math.floor(keyId)) end)
    pcall(function() vehicle:keyNamerVehicle(key) end)
    local added = pcall(function() inventory:AddItem(key) end)
    local finalContainer = nil
    pcall(function() finalContainer = key:getContainer() end)
    if not added or finalContainer ~= inventory then
        return false, "The replacement vehicle key could not be added to the owner's inventory."
    end
    pcall(function() key:getModData().keyRing = nil end)
    if sendAddItemToContainer then
        pcall(function() sendAddItemToContainer(inventory, key) end)
    end
    return true, true
end

-- Reconstruct a previously captured vehicle at a loaded world square. Raid
-- insertion uses this instead of attempting to move a live Bullet body between
-- distant, mutually unloaded cells. The returned vehicle has a new network ID
-- but preserves its key registration, parts, fuel, appearance, and cargo.
function Authority.spawnSnapshotAt(record, point, options)
    if type(record) ~= "table" or type(point) ~= "table" then
        return nil, "Vehicle reconstruction data is unavailable."
    end
    local x = math.floor(tonumber(point.x) or 0)
    local y = math.floor(tonumber(point.y) or 0)
    local z = math.floor(tonumber(point.z) or 0)
    local square = nil
    pcall(function() square = getCell():getGridSquare(x, y, z) end)
    if square == nil then return nil, "The vehicle destination is not loaded." end

    local vehicle = nil
    local created = pcall(function()
        vehicle = addVehicleDebug(record.scriptName, IsoDirections.S,
            math.max(0, math.floor(tonumber(record.skinIndex) or 0)), square)
    end)
    if not created or not createdVehicleIsUsable(vehicle) then
        if vehicle ~= nil then pcall(function() vehicle:permanentlyRemove() end) end
        return nil, "Project Zomboid could not reconstruct the raid vehicle."
    end
    local restored, restoreError = restoreVehicle(vehicle, record, point,
        type(options) == "table" and options.deferEngineNetwork == true)
    if not restored then
        pcall(function() vehicle:permanentlyRemove() end)
        return nil, restoreError
    end
    return vehicle
end

local function loadedVehiclesSnapshot()
    -- Build 42's vehicle collection exposes size() and toArray() in this
    -- runtime, but not get(). The returned Java array also has no dependable
    -- Lua # length after reload. Use the collection's authoritative count and
    -- the Lua-facing array's one-based indices, then copy references before
    -- anyone removes a vehicle.
    local snapshot = {}
    local available = false
    local listed = pcall(function()
        local vehicles = getCell():getVehicles()
        if vehicles == nil then return end
        local count = math.max(0, vehicles:size())
        local array = vehicles:toArray()
        if array == nil then return end
        available = true
        for index = 1, count do
            local vehicle = array[index]
            if vehicle ~= nil then snapshot[#snapshot + 1] = vehicle end
        end
    end)
    if not listed or not available then return nil end
    return snapshot
end

local function vehicleBayOccupied(point)
    local vehicles = loadedVehiclesSnapshot()
    -- A collection that cannot be inspected is not evidence that the bay is
    -- clear. Treat it as temporarily occupied so deferred swaps retry safely.
    if vehicles == nil then return true end
    for _, candidate in ipairs(vehicles) do
        local occupied = false
        pcall(function()
            -- Any registered vehicle body at the bay blocks reconstruction.
            -- A body can retain/render its coordinates while getSquare() is in
            -- transition, so requiring a square allowed vehicles to overlap.
            occupied = candidate ~= nil and math.abs(candidate:getX() - point.x) <= 4
                and math.abs(candidate:getY() - point.y) <= 4
                and math.floor(candidate:getZ()) == math.floor(point.z)
        end)
        if occupied then return true end
    end
    return false
end

local function clearPlayersFromVehicleSpawn(point, players, deliver)
    local bounds = Config.hideoutVehicleUnsafeBounds()
    local clearPoint = Config.hideoutVehiclePlayerClearPoint()
    local clearSquare = nil
    pcall(function()
        clearSquare = getCell():getGridSquare(clearPoint.x, clearPoint.y, clearPoint.z)
    end)
    local clearSquareSafe = false
    if clearSquare ~= nil then
        pcall(function()
            clearSquareSafe = clearSquare:getFloor() ~= nil and clearSquare:TreatAsSolidFloor()
                and not clearSquare:isSolid() and not clearSquare:isSolidTrans()
                and not clearSquare:has(IsoFlagType.water)
        end)
    end
    if not clearSquareSafe then
        return false, "The garage's player clearance tile is unavailable."
    end

    -- Keep split-screen/local players on the requested tile without stacking
    -- their exact coordinates on top of one another.
    local offsets = {
        { 0.30, 0.30 }, { 0.70, 0.30 }, { 0.30, 0.70 }, { 0.70, 0.70 },
        { 0.50, 0.50 }, { 0.50, 0.20 }, { 0.80, 0.50 }, { 0.50, 0.80 }, { 0.20, 0.50 },
    }
    local used = {}
    for _, player in ipairs(players or Util.players()) do
        local blocks = false
        if player ~= nil and not player:isDead() and player:getVehicle() == nil then
            pcall(function()
                local x, y = math.floor(player:getX()), math.floor(player:getY())
                blocks = x >= bounds.minX and x <= bounds.maxX
                    and y >= bounds.minY and y <= bounds.maxY
                    and math.floor(player:getZ()) == bounds.z
            end)
        end
        if blocks then
            local destination = nil
            for index, offset in ipairs(offsets) do
                if used[index] ~= true then
                    local x = clearPoint.x + offset[1]
                    local y = clearPoint.y + offset[2]
                    local available = true
                    for _, other in ipairs(players or Util.players()) do
                        if other ~= nil and other ~= player and not other:isDead()
                            and math.abs(other:getX() - x) < 0.20
                            and math.abs(other:getY() - y) < 0.20
                            and math.floor(other:getZ()) == clearPoint.z then
                            available = false
                            break
                        end
                    end
                    if available then
                        used[index] = true
                        destination = {
                            x = x,
                            y = y,
                            z = clearPoint.z,
                        }
                        break
                    end
                end
            end
            if destination == nil then
                return false, "The garage player clearance tile is occupied."
            end
            pcall(function()
                player:teleportTo(destination.x, destination.y, destination.z)
            end)
            if deliver ~= nil then deliver(player, "Teleport", destination) end
        end
    end
    return true
end

function Authority.ensureState(root)
    if root == nil then return end
    Garage.ensureState(root)
    if type(root.pendingGarageVehicleRemovals) ~= "table" then
        root.pendingGarageVehicleRemovals = {}
    end
    if type(root.activeHideoutVehicle) ~= "table" then root.activeHideoutVehicle = nil end
end

local function pendingHideoutRemoval(root)
    for removalKey, pending in pairs(root.pendingGarageVehicleRemovals or {}) do
        if type(pending) == "table" and (pending.kind == "HIDEOUT"
            or string.sub(tostring(removalKey), 1, 8) == "hideout:") then
            return tostring(removalKey), pending
        end
    end
    return nil, nil
end

local function activeToken(active)
    if type(active) ~= "table" then return nil end
    return tostring(active.owner or "") .. ":" .. tostring(active.record and active.record.id or "")
end

local function resetRuntimeFor(active, now)
    runtime.vehicle = nil
    runtime.activeToken = activeToken(active)
    runtime.ownerSeen = false
    runtime.ownerAbsentSinceMs = nil
    runtime.lastCaptureMs = 0
    runtime.lastInteractionMs = tonumber(now) or Util.timerNowMs()
    runtime.interactionFingerprint = nil
    runtime.inactiveState = false
    runtime.driverUsername = nil
    runtime.sessionStartedMs = tonumber(now) or Util.timerNowMs()
end

local function vehicleById(vehicleId)
    if vehicleId == nil or getVehicleById == nil then return nil end
    local vehicle = nil
    pcall(function() vehicle = getVehicleById(tonumber(vehicleId)) end)
    return vehicle
end

local function loadedVehicleSquare(vehicle)
    if vehicle == nil then return nil end
    local square = nil
    pcall(function() square = vehicle:getSquare() end)
    return square
end

local function vehicleMatchesActive(vehicle, active)
    if vehicle == nil or type(active) ~= "table" or loadedVehicleSquare(vehicle) == nil then
        return false
    end
    local tagged = false
    pcall(function()
        local data = vehicle:getModData()
        tagged = data.ExtractionModeHideoutVehicle == true
            and tostring(data.ExtractionModeGarageOwner or "") == tostring(active.owner or "")
            and tostring(data.ExtractionModeGarageVehicleId or "")
                == tostring(active.record and active.record.id or "")
    end)
    if tagged then return true end

    -- Fast compatibility fallback for an active vehicle saved before tags
    -- existed. Runtime IDs alone are recyclable, so position is also required.
    local fallback = false
    pcall(function()
        fallback = tostring(vehicle:getId()) == tostring(active.vehicleId)
            and math.abs(vehicle:getX() - (tonumber(active.x) or vehicle:getX())) <= 4
            and math.abs(vehicle:getY() - (tonumber(active.y) or vehicle:getY())) <= 4
            and math.floor(vehicle:getZ()) == math.floor(tonumber(active.z) or vehicle:getZ())
    end)
    if fallback then
        tagVehicle(vehicle, active)
        return true
    end

    -- Chunk streaming can assign a new runtime ID and may discard vehicle
    -- modData. Rebind the physical bay vehicle through identifiers that are part
    -- of the captured vehicle itself. Key and mechanical IDs are independent
    -- evidence: a restored/generated key must not prevent the mechanical-ID or
    -- appearance fallbacks from being considered.
    local reloadedMatch = false
    local rebindMethod = nil
    pcall(function()
        local record = active.record or {}
        local vehicleX, vehicleY = vehicle:getX(), vehicle:getY()
        local nearSavedPosition = math.abs(vehicleX - (tonumber(active.x) or vehicleX)) <= 4
            and math.abs(vehicleY - (tonumber(active.y) or vehicleY)) <= 4
            and math.floor(vehicle:getZ()) == math.floor(tonumber(active.z) or vehicle:getZ())
        local bay = Config.hideoutVehicleSpawn()
        local insideProtectedBay = math.abs(vehicleX - (tonumber(bay.x) or vehicleX)) <= 3
            and math.abs(vehicleY - (tonumber(bay.y) or vehicleY)) <= 3
            and math.floor(vehicle:getZ()) == math.floor(tonumber(bay.z) or vehicle:getZ())
        local sameScript = tostring(vehicle:getScriptName()) == tostring(record.scriptName or "")
        local expectedKey = tonumber(record.keyId)
        local expectedMechanical = tonumber(record.mechanicalId)
        local keyMatches = expectedKey ~= nil and expectedKey >= 0
            and tonumber(vehicle:getKeyId()) == math.floor(expectedKey)
        local mechanicalMatches = expectedMechanical ~= nil and expectedMechanical > 0
            and tonumber(vehicle:getMechanicalID()) == math.floor(expectedMechanical)
        local appearanceMatches = math.floor(tonumber(vehicle:getSkinIndex()) or -1)
                == math.floor(tonumber(record.skinIndex) or -2)
            and math.abs((tonumber(vehicle:getColorHue()) or -1)
                - (tonumber(record.colorHue) or -2)) <= 0.02
            and math.abs((tonumber(vehicle:getColorSaturation()) or -1)
                - (tonumber(record.colorSaturation) or -2)) <= 0.02
            and math.abs((tonumber(vehicle:getColorValue()) or -1)
                - (tonumber(record.colorValue) or -2)) <= 0.02
        local stableIdentity = keyMatches or mechanicalMatches or appearanceMatches

        -- The garage is a protected, single-vehicle bay and normal world cars
        -- cannot spawn there. If Build 42 regenerated every mutable identifier
        -- during load, script + the exact bay remains a safe durable identity.
        local protectedBayFallback = insideProtectedBay and nearSavedPosition
        reloadedMatch = sameScript and nearSavedPosition
            and (stableIdentity or protectedBayFallback)
        if keyMatches then
            rebindMethod = "key ID"
        elseif mechanicalMatches then
            rebindMethod = "mechanical ID"
        elseif appearanceMatches then
            rebindMethod = "appearance"
        elseif protectedBayFallback then
            rebindMethod = "protected garage bay"
        end
    end)
    if reloadedMatch then
        local previousId = tostring(active.vehicleId or "")
        local reboundId = previousId
        pcall(function() reboundId = tostring(vehicle:getId()) end)
        active.vehicleId = reboundId
        tagVehicle(vehicle, active)
        Util.log("Rebound reloaded active hideout vehicle "
            .. tostring(active.record and active.record.id or "unknown")
            .. " from runtime ID " .. previousId .. " to " .. reboundId
            .. " using " .. tostring(rebindMethod or "saved identity"))
        return true
    end
    return false
end

local function liveVehicle(active)
    if type(active) ~= "table" then return nil end
    if vehicleMatchesActive(runtime.vehicle, active) then return runtime.vehicle end
    runtime.vehicle = nil

    -- A BaseVehicle instance cached before a chunk unload can become a stale
    -- Java object. Find the currently loaded, tagged instance instead of trusting
    -- its short ID, which may have been reused by a raid vehicle.
    local vehicles = loadedVehiclesSnapshot()
    if vehicles ~= nil then
        for _, candidate in ipairs(vehicles) do
            if vehicleMatchesActive(candidate, active) then
                runtime.vehicle = candidate
                return runtime.vehicle
            end
        end
    end
    local byId = vehicleById(active.vehicleId)
    if vehicleMatchesActive(byId, active) then runtime.vehicle = byId end
    return runtime.vehicle
end

local function vehiclePhysicsReady(vehicle)
    if loadedVehicleSquare(vehicle) == nil then return false end
    local controller = nil
    local physicsActive = false
    pcall(function() controller = vehicle:getController() end)
    if controller == nil then return false end
    pcall(function() physicsActive = vehicle:isPhysicsActive() == true end)
    return physicsActive
end

local function stopLoadedVehicle(vehicle)
    if loadedVehicleSquare(vehicle) == nil then return false end
    if vehiclePhysicsReady(vehicle) then
        pcall(function() vehicle:setBraking(true) end)
        pcall(function() vehicle:setForceBrake() end)
    end
    pcall(function() vehicle:shutOff() end)
    return true
end

local function vehicleOccupied(vehicle)
    if vehicle == nil then return false end
    local seats = 0
    pcall(function() seats = math.max(0, vehicle:getMaxPassengers()) end)
    for seat = 0, seats - 1 do
        local character = nil
        pcall(function() character = vehicle:getCharacter(seat) end)
        if character ~= nil then return true end
    end
    local remoteOccupied = Compatibility.hasRemoteRVOccupants(vehicle, Util.players())
    if remoteOccupied then return true end
    return false
end

local function currentDriver(vehicle)
    if vehicle == nil then return nil end
    local driver = nil
    pcall(function() driver = vehicle:getDriver() end)
    return driver
end

local function interactionFingerprint(vehicle)
    if vehicle == nil then return "missing" end
    local running = false
    local fuel = 0
    local battery = -1
    local engineCondition = -1
    pcall(function() running = vehicle:isEngineRunning() end)
    pcall(function()
        local tank = vehicle:getPartById("GasTank")
        if tank ~= nil then fuel = tonumber(tank:getContainerContentAmount()) or 0 end
    end)
    pcall(function()
        local part = vehicle:getPartById("Battery")
        local item = part and part:getInventoryItem() or nil
        if item ~= nil then battery = tonumber(item:getCurrentUsesFloat()) or -1 end
    end)
    pcall(function()
        local part = vehicle:getPartById("Engine")
        if part ~= nil then engineCondition = tonumber(part:getCondition()) or -1 end
    end)
    local values = {
        tostring(running), string.format("%.3f", fuel),
        string.format("%.3f", battery), tostring(engineCondition),
    }
    local partCount = 0
    pcall(function() partCount = math.max(0, vehicle:getPartCount()) end)
    for index = 0, partCount - 1 do
        pcall(function()
            local part = vehicle:getPartByIndex(index)
            local item = part and part:getInventoryItem() or nil
            values[#values + 1] = tostring(part and part:getId() or index)
                .. "=" .. tostring(part and part:getCondition() or -1)
                .. ":" .. tostring(item and item:getFullType() or "none")
                .. ":" .. string.format("%.3f",
                    tonumber(part and part:getContainerContentAmount()) or 0)
            local container = part and part:getItemContainer() or nil
            local items = container and container:getItems() or nil
            if items ~= nil then
                values[#values + 1] = "cargo:" .. tostring(items:size())
                for itemIndex = 0, items:size() - 1 do
                    local cargoItem = items:get(itemIndex)
                    values[#values + 1] = tostring(cargoItem and cargoItem:getFullType() or "none")
                        .. ":" .. tostring(cargoItem and cargoItem:getID() or itemIndex)
                end
            end
        end)
    end
    return table.concat(values, "|")
end

local function updateInteraction(active, vehicle, players, now)
    now = tonumber(now) or Util.timerNowMs()
    local occupied = vehicleOccupied(vehicle)
    local interacted = occupied
    if vehicle ~= nil and not interacted then
        local running = false
        pcall(function() running = vehicle:isEngineRunning() end)
        interacted = running
    end
    local fingerprint = interactionFingerprint(vehicle)
    if runtime.interactionFingerprint == nil or runtime.interactionFingerprint ~= fingerprint then
        runtime.interactionFingerprint = fingerprint
        interacted = true
    end
    if interacted or type(active and active.raidReservation) == "table" then
        runtime.lastInteractionMs = now
    end
    return occupied, interacted
end

function Authority.isInactive(root, players)
    Authority.ensureState(root)
    local active = root.activeHideoutVehicle
    if type(active) ~= "table" or type(active.storePending) == "table"
        or type(active.raidReservation) == "table" then return false end
    local vehicle = liveVehicle(active)
    local occupied = updateInteraction(active, vehicle, players or Util.players(), Util.timerNowMs())
    if occupied then return false end
    local minutes = math.max(1, tonumber(Config.value("HideoutVehicleInactivityMinutes")) or 5)
    return Util.timerNowMs() - runtime.lastInteractionMs >= minutes * 60 * 1000
end

function Authority.reserveForRaid(root, vehicleId, raidKey)
    Authority.ensureState(root)
    local active = root.activeHideoutVehicle
    if type(active) ~= "table" or tostring(active.vehicleId) ~= tostring(vehicleId) then return true end
    if type(active.storePending) == "table" then
        return false, "The vehicle is already being returned to its owner's garage."
    end
    active.raidReservation = {
        raidKey = tostring(raidKey or ""),
        vehicleId = tostring(vehicleId),
        reservedAtMs = Util.timerNowMs(),
    }
    runtime.lastInteractionMs = Util.timerNowMs()
    return true
end

function Authority.releaseRaidReservation(root, vehicleId, raidKey)
    Authority.ensureState(root)
    local active = root.activeHideoutVehicle
    if type(active) ~= "table" or type(active.raidReservation) ~= "table" then return false end
    if vehicleId ~= nil and tostring(active.vehicleId) ~= tostring(vehicleId) then return false end
    if raidKey ~= nil and tostring(active.raidReservation.raidKey) ~= tostring(raidKey) then return false end
    active.raidReservation = nil
    runtime.lastInteractionMs = Util.timerNowMs()
    return true
end

function Authority.markInteraction(root, vehicleId, username)
    Authority.ensureState(root)
    local active = root.activeHideoutVehicle
    if type(active) ~= "table" or tostring(active.vehicleId) ~= tostring(vehicleId) then return false end
    runtime.lastInteractionMs = Util.timerNowMs()
    active.lastInteractionBy = tostring(username or "")
    return true
end

local function queueRemoval(root, active)
    if type(active) ~= "table" or active.vehicleId == nil then return nil end
    -- Do not key hideout removals by PZ's recyclable vehicle ID. An extracted
    -- raid vehicle and the unloaded hideout vehicle can temporarily share it.
    local removalKey = "hideout:" .. tostring(active.owner or "") .. ":"
        .. tostring(active.record and active.record.id or "") .. ":"
        .. tostring(active.vehicleId)
    root.pendingGarageVehicleRemovals[removalKey] = {
        kind = "HIDEOUT",
        vehicleId = tostring(active.vehicleId),
        owner = active.owner,
        garageId = active.record and active.record.id or nil,
        scriptName = active.record and active.record.scriptName or nil,
        x = active.x,
        y = active.y,
        z = active.z,
    }
    return removalKey
end

local function vehicleMatchesRemoval(vehicle, pending)
    if vehicle == nil or type(pending) ~= "table" then return false end
    local tagged, extractionTagged, raidSourceTagged = false, false, false
    pcall(function()
        local data = vehicle:getModData()
        tagged = data.ExtractionModeHideoutVehicle == true
            and tostring(data.ExtractionModeGarageOwner or "") == tostring(pending.owner or "")
            and tostring(data.ExtractionModeGarageVehicleId or "") == tostring(pending.garageId or "")
        extractionTagged = pending.transactionId ~= nil
            and tostring(data.ExtractionModeGarageRemovalTransactionId or "")
                == tostring(pending.transactionId)
        raidSourceTagged = pending.raidSourceToken ~= nil
            and tostring(data.ExtractionModeRaidRebuildSourceToken or "")
                == tostring(pending.raidSourceToken)
    end)
    -- A raid-source record must never match a later deployment merely because
    -- it has the same owner/garage ID or identical appearance. The unique token
    -- is written only after the raid snapshot is captured, so it exists on the
    -- disposable hideout body and can never be inherited by the raid copy.
    if pending.kind == "RAID_SOURCE" then return raidSourceTagged end
    if tagged or extractionTagged then return true end
    local sameScript, nearPoint = false, false
    pcall(function()
        sameScript = tostring(vehicle:getScriptName()) == tostring(pending.scriptName)
        nearPoint = math.abs(vehicle:getX() - (tonumber(pending.x) or vehicle:getX())) <= 3
            and math.abs(vehicle:getY() - (tonumber(pending.y) or vehicle:getY())) <= 3
            and math.floor(vehicle:getZ()) == math.floor(tonumber(pending.z) or vehicle:getZ())
    end)
    return sameScript and nearPoint
end

local function vehicleAbsentFromLoadedWorld(vehicle)
    if vehicle == nil then return false end
    local vehicles = loadedVehiclesSnapshot()
    if vehicles ~= nil then
        for _, candidate in ipairs(vehicles) do
            if candidate == vehicle then return false end
        end
        -- permanentlyRemove() unregisters the vehicle before its old Java object
        -- necessarily drops getSquare(). The registered collection is therefore
        -- the authoritative same-tick confirmation, not the stale square field.
        return true
    end
    -- If collection enumeration itself is unavailable, retain the conservative
    -- legacy fallback and let the persisted retry record handle it later.
    return loadedVehicleSquare(vehicle) == nil
end

function Authority.removeVehicleNow(vehicle)
    if vehicle == nil then return false, "vehicle is unavailable" end
    local invoked, removeError = pcall(function() vehicle:permanentlyRemove() end)
    if not invoked then return false, removeError end
    if not vehicleAbsentFromLoadedWorld(vehicle) then
        return false, "vehicle removal has not been confirmed"
    end
    return true
end

local function ejectOccupants(vehicle)
    if vehicle == nil then return end
    stopLoadedVehicle(vehicle)
    local seats = 0
    pcall(function() seats = math.max(0, vehicle:getMaxPassengers()) end)
    for seat = 0, seats - 1 do
        local character = nil
        pcall(function() character = vehicle:getCharacter(seat) end)
        if character ~= nil then
            pcall(function() vehicle:exit(character) end)
            pcall(function() vehicle:setCharacterPosition(character, seat, "outside") end)
            pcall(function() vehicle:updateHasExtendOffsetForExitEnd(character) end)
        end
    end
end

local function notifyPreparation(players, deliver, active)
    if deliver == nil then return end
    for _, player in ipairs(players or {}) do
        deliver(player, "GaragePrepareVehicleRemoval", {
            vehicleId = active.vehicleId,
            x = active.x,
            y = active.y,
            z = active.z,
            radius = MECHANICS_SAFETY_RADIUS,
        })
    end
end

function Authority.spawn(root, ownerPlayer, garageId, players, deliver)
    Authority.ensureState(root)
    if ownerPlayer == nil or ownerPlayer:isDead() then return false, "A living owner is required." end
    local owner = Util.garageUsername(ownerPlayer)
    if owner == "" then return false, "The vehicle owner is unavailable." end
    local hideout = Config.hideout()
    if not Util.playerNear(ownerPlayer, hideout, hideout.radius) then
        return false, "Vehicles can only be deployed while their owner is inside the hideout."
    end
    local active = root.activeHideoutVehicle
    if type(active) == "table" then
        local ownsActive = tostring(active.owner) == tostring(owner)
        if not ownsActive and not Authority.isInactive(root, players or Util.players()) then
            if type(active.raidReservation) == "table" then
                return false, "The active hideout vehicle is reserved for a raid deployment."
            end
            if vehicleOccupied(liveVehicle(active)) then
                return false, "The active hideout vehicle is occupied."
            end
            return false, "The active hideout vehicle has not been inactive long enough to swap."
        end
        local requested = Garage.record(root, owner, garageId)
        if requested == nil then return false, "That garage vehicle no longer exists." end
        local ok, pending = Authority.beginStore(root,
            ownsActive and "swapped by owner" or "swapped after inactivity",
            active.owner, players or Util.players(), deliver)
        if not ok then return false, pending end
        pending.swapRequest = { owner = owner, garageId = tostring(garageId) }
        Garage.refreshBackup(root, "vehicle swap queued")
        return true, {
            queued = true,
            returningOwner = active.owner,
            requestedGarageId = tostring(garageId),
        }
    end
    local pendingRemovalKey = pendingHideoutRemoval(root)
    if pendingRemovalKey ~= nil then
        return false, "The previous active vehicle is still despawning. Wait for the current garage transition to finish."
    end
    -- Keep the record in durable garage state throughout preflight. Previously
    -- a bay-list exception during a swap happened after Garage.take(), which
    -- could strand the requested vehicle outside both the garage and the world.
    local requested = Garage.record(root, owner, garageId)
    if requested == nil then return false, "That garage vehicle no longer exists." end

    local point = Config.hideoutVehicleSpawn()
    local square = nil
    pcall(function() square = getCell():getGridSquare(math.floor(point.x),
        math.floor(point.y), math.floor(point.z)) end)
    if square == nil then
        return false, "The hideout vehicle bay is not loaded."
    end
    local zoneReady, zoneError = ensureHideoutVehicleZone(point, square)
    if not zoneReady then
        return false, zoneError
    end
    if vehicleBayOccupied(point) then
        return false, "The hideout vehicle bay is blocked by another vehicle."
    end
    local cleared, clearError = clearPlayersFromVehicleSpawn(point,
        players or Util.players(), deliver)
    if not cleared then
        return false, clearError
    end

    local record, originalIndex, takeError = Garage.take(root, owner, garageId)
    if record == nil then return false, takeError end

    local vehicle = nil
    local created = pcall(function()
        -- Unlike addVehicle(), addVehicleDebug() initializes the script and
        -- registers the vehicle in single player, listen servers, and dedicated
        -- servers.  Direction is restored exactly from our record immediately
        -- afterward, so this initial direction is only a safe constructor input.
        vehicle = addVehicleDebug(record.scriptName, IsoDirections.S,
            math.max(0, math.floor(tonumber(record.skinIndex) or 0)), square)
    end)
    if not created or not createdVehicleIsUsable(vehicle) then
        if vehicle ~= nil then pcall(function() vehicle:permanentlyRemove() end) end
        Garage.put(root, owner, record, originalIndex)
        return false, "Project Zomboid could not create that vehicle in the indoor hideout bay."
    end
    local ok, restoreError = restoreVehicle(vehicle, record, point)
    if not ok then
        pcall(function() vehicle:permanentlyRemove() end)
        Garage.put(root, owner, record, originalIndex)
        return false, restoreError
    end
    local keyReady, keyResult = ensureOwnerHasVehicleKey(vehicle, ownerPlayer)
    if not keyReady then
        pcall(function() vehicle:permanentlyRemove() end)
        Garage.put(root, owner, record, originalIndex)
        return false, keyResult
    end

    local active = {
        owner = owner,
        record = record,
        vehicleId = tostring(vehicle:getId()),
        x = point.x,
        y = point.y,
        z = point.z,
        angleY = tonumber(point.angleY) or tonumber(point.angleZ) or 0,
        deployedWorldHours = getGameTime():getWorldAgeHours(),
    }
    root.activeHideoutVehicle = active
    tagVehicle(vehicle, active)
    -- The initial full vehicle replication already includes the locked engine
    -- feature. Avoid a second incremental engine packet while a multiplayer
    -- client's VehicleSounds object may still be uninitialized.
    Authority.applyMovementLock(vehicle, active.record, false)
    resetRuntimeFor(active, Util.timerNowMs())
    runtime.vehicle = vehicle
    runtime.ownerSeen = true
    Garage.refreshBackup(root, "vehicle deployed")
    runtime.pendingSwap = nil
    Util.log("Deployed garage vehicle " .. tostring(record.id) .. " for " .. owner
        .. " at " .. tostring(point.x) .. "," .. tostring(point.y)
        .. "," .. tostring(point.z) .. " heading="
        .. tostring(tonumber(point.angleY) or tonumber(point.angleZ) or 0))
    if keyResult == true and deliver ~= nil then
        deliver(ownerPlayer, "Announcement", {
            message = "Your missing vehicle key was replaced and added to your inventory.",
        })
        Util.log("Created replacement key " .. tostring(record.keyId or "unknown")
            .. " for deployed garage vehicle " .. tostring(record.id)
            .. " owner=" .. tostring(owner))
    end
    return true, active
end

function Authority.beginStore(root, reason, destinationOwner, players, deliver)
    Authority.ensureState(root)
    local active = root.activeHideoutVehicle
    if type(active) ~= "table" then return false, "There is no active hideout vehicle." end
    if type(active.raidReservation) == "table" then
        return false, "The active hideout vehicle is reserved for a raid deployment."
    end
    if type(active.storePending) == "table" then return true, active.storePending end
    local vehicle = liveVehicle(active)
    local reasonText = tostring(reason or "stored")
    local allowMissingVehicle = reasonText == "owner disconnected"
        or reasonText == "owner absent after restart"
    if vehicle == nil and not allowMissingVehicle then
        Util.log("Active hideout vehicle could not be resolved for storage owner="
            .. tostring(active.owner or "") .. " garageId="
            .. tostring(active.record and active.record.id or "") .. " priorRuntimeId="
            .. tostring(active.vehicleId or ""))
        return false, "The active vehicle is still loading and could not be identified safely. Wait a moment and try again."
    end
    local remoteOccupied = Compatibility.hasRemoteRVOccupants(
        vehicle, players or Util.players())
    if remoteOccupied then
        return false, "That RV cannot be returned while a player is inside its interior."
    end
    active.storePending = {
        reason = reasonText,
        destinationOwner = tostring(destinationOwner or active.owner),
        deadlineMs = Util.timerNowMs() + STORE_SETTLE_MS,
        allowMissingVehicle = allowMissingVehicle,
    }
    if vehicle ~= nil then
        stopLoadedVehicle(vehicle)
    end
    notifyPreparation(players, deliver, active)
    Garage.refreshBackup(root, "active vehicle storage started")
    return true, active.storePending
end

local function storeEscrow(root, active, destinationOwner, currentSnapshot)
    local record = Garage.mergeRecord(currentSnapshot, active.record)
    if type(record) ~= "table" then return false, "The active vehicle has no recoverable snapshot." end
    local ok, message = Garage.put(root, destinationOwner, record)
    if not ok then return false, message end
    active.record = record
    return true, record
end

local function activeOwnerPlayer(active, vehicle, players)
    local owner = tostring(active and active.owner or "")
    local driver = currentDriver(vehicle)
    if driver ~= nil and not driver:isDead()
        and tostring(Util.garageUsername(driver)) == owner then return driver end
    for _, player in ipairs(players or {}) do
        if player ~= nil and not player:isDead()
            and tostring(Util.garageUsername(player)) == owner then return player end
    end
    return nil
end

local function queueIgnitionKeyForOwner(root, owner, vehicle)
    local inIgnition = false
    local stateRead = pcall(function() inIgnition = vehicle:isKeysInIgnition() == true end)
    if not stateRead then return false, "the vehicle ignition state is unavailable" end
    if not inIgnition then return true, nil end
    local key = nil
    pcall(function() key = vehicle:getCurrentKey() end)
    if key == nil then return false, "the ignition key item is unavailable" end
    local snapshot, snapshotError = Garage.snapshotItem(key, 0, {})
    if snapshot == nil then return false, snapshotError end
    snapshot.modData = type(snapshot.modData) == "table" and snapshot.modData or {}
    snapshot.modData.keyRing = nil
    owner = tostring(owner or "")
    if owner == "" then return false, "the vehicle owner is unavailable" end
    root.pendingGarageOwnerKeys[owner] = type(root.pendingGarageOwnerKeys[owner]) == "table"
        and root.pendingGarageOwnerKeys[owner] or {}
    table.insert(root.pendingGarageOwnerKeys[owner], snapshot)
    return true, {
        keyId = snapshot.keyId,
        method = "pending-owner",
    }
end

local function finalizeStore(root, players, deliver)
    local active = root.activeHideoutVehicle
    if type(active) ~= "table" or type(active.storePending) ~= "table" then return false end
    local vehicle = liveVehicle(active)
    if vehicle == nil and active.storePending.allowMissingVehicle ~= true then
        active.storePending = nil
        Garage.refreshBackup(root, "active vehicle storage unresolved")
        local message = "Vehicle storage was cancelled because the active vehicle could not be identified safely. Wait for it to finish loading and try again."
        if deliver ~= nil then
            for _, player in ipairs(players or {}) do
                if Util.garageUsername(player) == tostring(active.owner) then
                    deliver(player, "Error", { message = message })
                    break
                end
            end
        end
        Util.log("Active hideout vehicle storage cancelled because its live vehicle could not be resolved")
        return false, message
    end
    local remoteOccupied = Compatibility.hasRemoteRVOccupants(
        vehicle, players or Util.players())
    if remoteOccupied then
        active.storePending = nil
        Garage.refreshBackup(root, "RV storage cancelled while interior occupied")
        local message = "That RV cannot be returned while a player is inside its interior."
        if deliver ~= nil then
            for _, player in ipairs(players or {}) do
                if Util.garageUsername(player) == tostring(active.owner) then
                    deliver(player, "Error", { message = message })
                    break
                end
            end
        end
        return false, message
    end
    local latest = nil
    if vehicle ~= nil then
        local captureError = nil
        latest, captureError = Garage.captureVehicle(vehicle)
        if latest == nil then
            active.storePending = nil
            Garage.refreshBackup(root, "active vehicle storage capture failed")
            Util.log("Active hideout vehicle storage cancelled: " .. tostring(captureError))
            if deliver ~= nil then
                for _, player in ipairs(players or {}) do
                    if Util.garageUsername(player) == tostring(active.owner) then
                        deliver(player, "Error", {
                            message = "Vehicle storage was cancelled because its vehicle or cargo data could not be saved safely: "
                                .. tostring(captureError),
                        })
                        break
                    end
                end
            end
            return false, captureError
        end
    end
    local destination = tostring(active.storePending.destinationOwner or active.owner or "")
    local reason = tostring(active.storePending.reason or "stored")
    local ok, recordOrError = storeEscrow(root, active, destination, latest)
    if not ok then
        active.storePending = nil
        Util.log("Active hideout vehicle storage failed: " .. tostring(recordOrError))
        return false
    end


    if vehicle ~= nil then
        local ownerPlayer = activeOwnerPlayer(active, vehicle, players)
        local keyReturned, keyResult
        if ownerPlayer ~= nil then
            keyReturned, keyResult = Garage.returnIgnitionKeyToDriver(vehicle, ownerPlayer)
        else
            -- Owner disconnect storage must still complete. Preserve a complete
            -- key snapshot and grant it the next time that garage owner is online.
            keyReturned, keyResult = queueIgnitionKeyForOwner(root, active.owner, vehicle)
        end
        if not keyReturned then
            local rolledBack = Garage.take(root, destination, recordOrError.id)
            active.storePending = nil
            Garage.refreshBackup(root, "active vehicle storage ignition key blocked")
            Util.log("Active hideout vehicle storage cancelled before key loss: "
                .. tostring(keyResult) .. " escrowRollback=" .. tostring(rolledBack ~= nil))
            if deliver ~= nil and ownerPlayer ~= nil then
                deliver(ownerPlayer, "Error", {
                    message = "Vehicle storage was cancelled to protect its ignition key: "
                        .. tostring(keyResult) .. ".",
                })
            end
            return false, keyResult
        elseif type(keyResult) == "table" then
            Util.log("Returned ignition key " .. tostring(keyResult.keyId or "unknown")
                .. " via " .. tostring(keyResult.method or "native")
                .. " to owner " .. tostring(active.owner)
                .. " before storing the active hideout vehicle")
        end
        ejectOccupants(vehicle)
    end

    local removalKey = queueRemoval(root, active)
    local removed = false
    if vehicle ~= nil then
        removed = Authority.removeVehicleNow(vehicle)
    end
    if removed and removalKey ~= nil then root.pendingGarageVehicleRemovals[removalKey] = nil end
    root.activeHideoutVehicle = nil
    resetRuntimeFor(nil, Util.timerNowMs())
    Garage.refreshBackup(root, "active vehicle stored")
    Util.log("Stored active hideout vehicle " .. tostring(recordOrError.id) .. " for "
        .. destination .. " reason=" .. reason .. " removed=" .. tostring(removed))
    return true, {
        owner = destination,
        record = recordOrError,
        reason = reason,
        removalKey = removalKey,
        removalConfirmed = removed,
    }
end

function Authority.transferStored(root, fromUsername, toUsername, garageId)
    Authority.ensureState(root)
    return Garage.transfer(root, fromUsername, toUsername, garageId)
end

function Authority.giveActive(root, ownerUsername, toUsername, players, deliver)
    Authority.ensureState(root)
    local active = root.activeHideoutVehicle
    if type(active) ~= "table" then return false, "There is no active hideout vehicle." end
    if tostring(active.owner) ~= tostring(ownerUsername) then
        return false, "Only the active vehicle's owner can give it away."
    end
    if tostring(ownerUsername) == tostring(toUsername) or tostring(toUsername or "") == "" then
        return false, "Choose a different player."
    end
    return Authority.beginStore(root, "given by " .. tostring(ownerUsername), toUsername, players, deliver)
end

function Authority.transferActiveToDriver(root, ownerUsername, expectedDriverUsername)
    Authority.ensureState(root)
    local active = root.activeHideoutVehicle
    if type(active) ~= "table" or type(active.record) ~= "table" then
        return false, "There is no active hideout vehicle."
    end
    if tostring(active.owner) ~= tostring(ownerUsername) then
        return false, "Only the active vehicle's owner can transfer it."
    end
    if type(active.storePending) == "table" then
        return false, "Wait for the active vehicle to finish returning before transferring it."
    end
    if type(active.raidReservation) == "table" then
        return false, "The vehicle cannot be transferred while it is reserved for raid deployment."
    end
    local vehicle = liveVehicle(active)
    if vehicle == nil then return false, "The active vehicle is not currently loaded." end
    local driver = currentDriver(vehicle)
    if driver == nil or driver:isDead() then
        return false, "A living player must be sitting in the driver's seat."
    end
    local newOwner = Util.garageUsername(driver)
    local driverUsername = Util.username(driver)
    if newOwner == "" then return false, "The current driver's garage identity is unavailable." end
    if tostring(expectedDriverUsername or "") ~= ""
        and tostring(expectedDriverUsername) ~= tostring(driverUsername) then
        return false, "The player in the driver's seat changed. Review the current driver and try again."
    end
    if tostring(newOwner) == tostring(ownerUsername) then
        return false, "The current driver already shares ownership of this garage."
    end

    local previousOwner = tostring(active.owner)
    active.owner = newOwner
    active.lastInteractionBy = driverUsername
    tagVehicle(vehicle, active)
    resetRuntimeFor(active, Util.timerNowMs())
    runtime.vehicle = vehicle
    runtime.ownerSeen = true
    runtime.driverUsername = driverUsername
    Garage.refreshBackup(root, "active vehicle ownership transferred")
    Util.log("Transferred active hideout vehicle " .. tostring(active.record.id)
        .. " from " .. previousOwner .. " to " .. tostring(newOwner)
        .. " via driver " .. tostring(driverUsername))
    return true, {
        owner = newOwner,
        driverUsername = driverUsername,
        previousOwner = previousOwner,
        vehicleName = active.record.name or active.record.modelKey or active.record.scriptName,
    }
end

function Authority.releaseToWorld(root, vehicle, deferEngineNetwork)
    Authority.ensureState(root)
    local active = root.activeHideoutVehicle
    if type(active) ~= "table" then return false, "There is no active hideout vehicle." end
    local current = vehicle or liveVehicle(active)
    if current ~= nil and tostring(current:getId()) ~= tostring(active.vehicleId) then
        return false, "That is not the active hideout vehicle."
    end
    Authority.releaseMovementLock(current, active.record, deferEngineNetwork ~= true)
    clearVehicleTag(current)
    root.activeHideoutVehicle = nil
    resetRuntimeFor(nil, Util.timerNowMs())
    Garage.refreshBackup(root, "vehicle released into raid")
    Util.log("Released hideout garage vehicle " .. tostring(active.record and active.record.id)
        .. " into the persistent world")
    return true, current
end

-- Complete the source side of a reconstructed raid insertion only after the
-- destination copy has received its own network ID. Unlike releaseToWorld(),
-- this deliberately keeps the hideout identity tag on an unloaded source and
-- persists a removal record. When the garage chunk streams back in, the retry
-- can therefore identify and remove the old body instead of leaving a duplicate
-- behind as an ordinary world vehicle.
function Authority.releaseRaidRebuildSource(root, expectedVehicleId, vehicle, sourceToken)
    Authority.ensureState(root)
    local active = root.activeHideoutVehicle
    if type(active) ~= "table"
        or tostring(active.vehicleId) ~= tostring(expectedVehicleId) then
        return false, "The raid source is no longer the active hideout vehicle."
    end

    local initialRemovalKey = queueRemoval(root, active)
    if initialRemovalKey == nil then return false, "The raid source removal could not be queued." end
    local pending = root.pendingGarageVehicleRemovals[initialRemovalKey]
    root.pendingGarageVehicleRemovals[initialRemovalKey] = nil
    sourceToken = tostring(sourceToken or (tostring(active.owner or "") .. ":"
        .. tostring(active.record and active.record.id or "") .. ":"
        .. tostring(expectedVehicleId) .. ":" .. tostring(Util.timerNowMs())))
    local removalKey = "raid-source:" .. sourceToken
    pending.kind = "RAID_SOURCE"
    pending.raidSourceToken = sourceToken
    root.pendingGarageVehicleRemovals[removalKey] = pending
    local source = vehicle
    if source ~= nil and not vehicleMatchesRemoval(source, pending) then source = nil end
    if source == nil then
        -- Do not fall back to runtime.vehicle here. After its chunk unloads that
        -- reference can retain a stale square even though the body is no longer
        -- registered; permanentlyRemove() on it appears successful but does not
        -- delete the serialized vehicle that will return with the chunk.
        local vehicles = loadedVehiclesSnapshot()
        if vehicles ~= nil then
            for _, candidate in ipairs(vehicles) do
                if vehicleMatchesRemoval(candidate, pending) then
                    source = candidate
                    break
                end
            end
        end
    end

    local removed = false
    local removeError = nil
    if source ~= nil and vehicleMatchesRemoval(source, pending) then
        removed, removeError = Authority.removeVehicleNow(source)
    end
    if removed then root.pendingGarageVehicleRemovals[removalKey] = nil end

    root.activeHideoutVehicle = nil
    resetRuntimeFor(nil, Util.timerNowMs())
    Garage.refreshBackup(root, "hideout vehicle committed to raid reconstruction")
    Util.log("Committed hideout raid source vehicle " .. tostring(expectedVehicleId)
        .. " removalConfirmed=" .. tostring(removed)
        .. (removeError ~= nil and (" error=" .. tostring(removeError)) or ""))
    return true, {
        removalKey = removalKey,
        removalConfirmed = removed,
    }
end

-- Keep the physics body active so suspension and vertical placement continue to
-- behave normally. The hideout lock removes drivetrain power while preserving
-- the original engine features in the garage record, so the engine can start and
-- be serviced but cannot translate the vehicle. Braking covers passive rolling.
function Authority.applyMovementLock(vehicle, record, transmit)
    if vehicle == nil then return false end
    if loadedVehicleSquare(vehicle) == nil then return false end

    -- The zero-power engine feature is the authoritative movement lock and is
    -- safe to maintain before Bullet has recreated the vehicle controller.
    local engineChanged = false
    local engineOk = pcall(function()
        if type(record) == "table" then persistentEnginePower(vehicle, record) end
        if type(record) == "table" and tonumber(vehicle:getEnginePower()) ~= 0 then
            vehicle:setEngineFeature(math.floor(tonumber(record.engineQuality) or 0),
                math.floor(tonumber(record.engineLoudness) or 0), 0)
            engineChanged = true
        end
        if transmit == true and engineChanged then vehicle:transmitEngine() end
    end)

    -- Hideout chunks unload during an on-foot raid. On return, BaseVehicle can
    -- exist for several ticks before its physics/controller does. Calling force
    -- or brake methods in that window throws Java NPEs even from inside pcall.
    if not vehiclePhysicsReady(vehicle) then return engineOk end
    local physicsOk = pcall(function()
        vehicle:setCurrentSteering(0)
        vehicle:setSpeedKmHour(0)
        vehicle:setClientForce(0)
        vehicle:setBraking(true)
        vehicle:setForceBrake()
    end)
    return engineOk and physicsOk
end

function Authority.releaseMovementLock(vehicle, record, transmit)
    if vehicle == nil then return false end
    local engineOk = pcall(function()
        local engineChanged = false
        if type(record) == "table" then
            local originalPower = persistentEnginePower(vehicle, record)
            if math.floor(tonumber(vehicle:getEnginePower()) or 0) ~= originalPower then
                vehicle:setEngineFeature(math.floor(tonumber(record.engineQuality) or 0),
                    math.floor(tonumber(record.engineLoudness) or 0), originalPower)
                engineChanged = true
            end
        end
        if transmit == true and engineChanged then vehicle:transmitEngine() end
    end)
    if loadedVehicleSquare(vehicle) == nil then return engineOk end
    if not vehiclePhysicsReady(vehicle) then
        pcall(function() vehicle:setPhysicsActive(true, true) end)
        return engineOk
    end
    local physicsOk = pcall(function()
        vehicle:setClientForce(0)
        vehicle:setBraking(false)
        vehicle:setSpeedKmHour(0)
        vehicle:setPhysicsActive(true, true)
    end)
    return engineOk and physicsOk
end

function Authority.captureForSave(root)
    Authority.ensureState(root)
    local active = root.activeHideoutVehicle
    if type(active) ~= "table" then return false end
    local vehicle = liveVehicle(active)
    if vehicle == nil then return false end
    local snapshot = Garage.captureVehicle(vehicle)
    if snapshot == nil then return false end
    -- The deployed vehicle deliberately has zero drivetrain power. Never allow
    -- that temporary lock value to overwrite its real stored engine features.
    if type(active.record) == "table" then
        snapshot.engineQuality = active.record.engineQuality
        snapshot.engineLoudness = active.record.engineLoudness
        snapshot.enginePower = active.record.enginePower
    end
    active.record = Garage.mergeRecord(snapshot, active.record)
    active.x, active.y, active.z = vehicle:getX(), vehicle:getY(), vehicle:getZ()
    runtime.lastCaptureMs = Util.timerNowMs()
    return true
end

local function onlinePlayersByName(players)
    local result = {}
    for _, player in ipairs(players or {}) do
        local username = Util.garageUsername(player)
        if username ~= "" then result[username] = player end
    end
    return result
end

local function deliverPendingOwnerKeys(root, players, deliver)
    local online = onlinePlayersByName(players)
    local changed = false
    for owner, snapshots in pairs(root.pendingGarageOwnerKeys or {}) do
        local player = online[tostring(owner)]
        if player ~= nil and type(snapshots) == "table" then
            local remaining = {}
            local deliveredCount = 0
            for _, snapshot in ipairs(snapshots) do
                local key, restoreError = restoreItem(snapshot)
                local inventory = player:getInventory()
                local added = key ~= nil and pcall(function() inventory:AddItem(key) end)
                if added then
                    pcall(function() key:getModData().keyRing = nil end)
                    if sendAddItemToContainer then
                        pcall(function() sendAddItemToContainer(inventory, key) end)
                    end
                    deliveredCount = deliveredCount + 1
                    changed = true
                else
                    remaining[#remaining + 1] = snapshot
                    Util.log("Could not deliver pending garage ignition key to "
                        .. tostring(owner) .. ": " .. tostring(restoreError or "inventory add failed"))
                end
            end
            if #remaining > 0 then
                root.pendingGarageOwnerKeys[owner] = remaining
            else
                root.pendingGarageOwnerKeys[owner] = nil
            end
            if deliveredCount > 0 then
                Util.log("Delivered " .. tostring(deliveredCount)
                    .. " pending garage ignition key(s) to " .. tostring(owner))
                if deliver ~= nil then
                    deliver(player, "Announcement", {
                        message = deliveredCount == 1
                            and "The ignition key from your stored vehicle was returned to your inventory."
                            or tostring(deliveredCount)
                                .. " stored-vehicle ignition keys were returned to your inventory.",
                    })
                end
            end
        end
    end
    if changed then Garage.refreshBackup(root, "pending garage ignition keys delivered") end
    return changed
end

local function processPendingRemovals(root)
    local changed = false
    for removalKey, pending in pairs(root.pendingGarageVehicleRemovals) do
        local vehicleId = tostring(pending and pending.vehicleId or removalKey)
        local vehicle = vehicleById(vehicleId)
        -- Reused short IDs can resolve to an unrelated raid/hideout vehicle.
        -- Reject it and scan the loaded collection for the tagged/positioned one.
        if vehicle ~= nil and not vehicleMatchesRemoval(vehicle, pending) then vehicle = nil end
        if vehicle == nil then
            local vehicles = loadedVehiclesSnapshot()
            if vehicles ~= nil then
                for _, candidate in ipairs(vehicles) do
                    if vehicleMatchesRemoval(candidate, pending) then
                        vehicle = candidate
                        break
                    end
                end
            end
        end
        local remoteOccupied = Compatibility.hasRemoteRVOccupants(vehicle, Util.players())
        if vehicle ~= nil and vehicleMatchesRemoval(vehicle, pending) and not remoteOccupied then
            ejectOccupants(vehicle)
            local removed, removeError = Authority.removeVehicleNow(vehicle)
            if removed then
                root.pendingGarageVehicleRemovals[removalKey] = nil
                runtime.pendingRemovalMissingSince[removalKey] = nil
                if pending.transactionId ~= nil then
                    Garage.finishTransaction(root, pending.transactionId,
                        "late-loading extracted vehicle removed")
                else
                    Garage.refreshBackup(root, "pending garage vehicle removed")
                end
                Util.log("Removed late-loading stored garage vehicle " .. tostring(vehicleId))
                changed = true
            else
                pending.retryAttempts = math.max(0,
                    math.floor(tonumber(pending.retryAttempts) or 0)) + 1
                if pending.retryAttempts == 1 or pending.retryAttempts % 10 == 0 then
                    Util.log("Pending garage vehicle removal retry "
                        .. tostring(pending.retryAttempts) .. " still waiting for vehicle "
                        .. tostring(vehicleId) .. ": " .. tostring(removeError))
                end
            end
        elseif vehicle == nil and pending.kind == "RAID_SOURCE" then
            -- A raid source can remain serialized in an unloaded vehicle chunk
            -- while its old grid square is still cached. Never interpret that
            -- temporary absence as confirmed deletion; retain the non-blocking
            -- tokenized cleanup until the old body streams in and is removed.
            runtime.pendingRemovalMissingSince[removalKey] = nil
        elseif vehicle == nil and tonumber(pending.x) ~= nil and tonumber(pending.y) ~= nil then
            local square = nil
            pcall(function()
                square = getCell():getGridSquare(math.floor(tonumber(pending.x)),
                    math.floor(tonumber(pending.y)), math.floor(tonumber(pending.z) or 0))
            end)
            if square ~= nil then
                local missingSince = runtime.pendingRemovalMissingSince[removalKey]
                if missingSince == nil then
                    runtime.pendingRemovalMissingSince[removalKey] = Util.timerNowMs()
                elseif Util.timerNowMs() - missingSince >= 10000 then
                    root.pendingGarageVehicleRemovals[removalKey] = nil
                    runtime.pendingRemovalMissingSince[removalKey] = nil
                    if pending.transactionId ~= nil then
                        Garage.finishTransaction(root, pending.transactionId,
                            "confirmed extracted vehicle already removed")
                    else
                        Garage.refreshBackup(root, "confirmed stored vehicle already removed")
                    end
                    changed = true
                    Util.log("Confirmed pending garage vehicle was already absent " .. tostring(vehicleId))
                end
            else
                runtime.pendingRemovalMissingSince[removalKey] = nil
            end
        else
            runtime.pendingRemovalMissingSince[removalKey] = nil
        end
    end
    return changed
end

local function cleanupOrphanedHideoutVehicles(root)
    local vehicles = loadedVehiclesSnapshot()
    if vehicles == nil then return false end
    local active = root.activeHideoutVehicle
    local current = type(active) == "table" and liveVehicle(active) or nil
    local point = Config.hideoutVehicleSpawn()
    local changed = false
    for _, candidate in ipairs(vehicles) do
        -- The cell vehicle snapshot can contain a transient null slot while
        -- chunks stream during a raid transition. Kahlua logs a Lua error even
        -- when the invalid method access is inside pcall, so reject it first.
        if candidate ~= nil then
            local tagged, nearBay = false, false
            pcall(function()
                tagged = candidate:getModData().ExtractionModeHideoutVehicle == true
                nearBay = candidate:getSquare() ~= nil
                    and math.abs(candidate:getX() - point.x) <= 8
                    and math.abs(candidate:getY() - point.y) <= 8
                    and math.floor(candidate:getZ()) == math.floor(point.z)
            end)
            if tagged and nearBay and candidate ~= current then
                local garageId = nil
                pcall(function()
                    garageId = candidate:getModData().ExtractionModeGarageVehicleId
                end)
                ejectOccupants(candidate)
                local removed = Authority.removeVehicleNow(candidate)
                if removed then
                    changed = true
                    Util.log("Removed orphaned duplicate hideout vehicle " .. tostring(garageId or "unknown"))
                end
            end
        end
    end
    return changed
end

function Authority.initialize(root)
    local recovered = Garage.recoverBackup(root)
    Authority.ensureState(root)
    local recoveredTransactions = Garage.recoverTransactions(root)
    local now = Util.timerNowMs()
    local active = root.activeHideoutVehicle
    if type(active) == "table" then
        if active.angleY == nil then active.angleY = tonumber(active.angleZ) or 0 end
        active.angleZ = nil
        active.storePending = nil
        active.raidReservation = nil
        local duplicate = active.record and Garage.record(root, active.owner, active.record.id) or nil
        if duplicate ~= nil then
            queueRemoval(root, active)
            root.activeHideoutVehicle = nil
            active = nil
            Util.log("Recovered completed garage storage transaction after restart")
        end
    end
    resetRuntimeFor(active, now)
    if recovered then Util.log("Recovered personal garages from the garage safety backup") end
    if recoveredTransactions > 0 then
        Util.log("Recovered " .. tostring(recoveredTransactions) .. " interrupted garage transaction(s)")
    end
end

local function playerForGarageOwner(players, owner)
    for _, candidate in ipairs(players or {}) do
        if candidate ~= nil and not candidate:isDead()
            and Util.garageUsername(candidate) == tostring(owner) then
            return candidate
        end
    end
    return nil
end

local function transientSwapFailure(message)
    local text = string.lower(tostring(message or ""))
    return string.find(text, "not loaded", 1, true) ~= nil
        or string.find(text, "blocked by another vehicle", 1, true) ~= nil
        or string.find(text, "could not be registered", 1, true) ~= nil
end

local function processPendingSwap(root, players, deliver, now)
    local pending = runtime.pendingSwap
    if type(pending) ~= "table" then return false end
    if now < (tonumber(pending.nextAttemptMs) or now) then return false end

    if pending.removalKey ~= nil
        and root.pendingGarageVehicleRemovals[tostring(pending.removalKey)] ~= nil then
        if now < (tonumber(pending.deadlineMs) or now) then
            pending.nextAttemptMs = now + SWAP_RETRY_DELAY_MS
            return false
        end
        runtime.pendingSwap = nil
        local blockedPlayer = playerForGarageOwner(players, pending.owner)
        if blockedPlayer ~= nil and deliver ~= nil then
            deliver(blockedPlayer, "Error", {
                message = "Your vehicle was kept safely in the garage because the previous vehicle has not finished despawning.",
            })
        end
        Util.log("Deferred garage swap cancelled because prior vehicle removal was not confirmed")
        return true
    end

    local requestingPlayer = playerForGarageOwner(players, pending.owner)
    if requestingPlayer == nil then
        if now < (tonumber(pending.deadlineMs) or now) then
            pending.nextAttemptMs = now + SWAP_RETRY_DELAY_MS
            return false
        end
        runtime.pendingSwap = nil
        Util.log("Deferred garage swap cancelled because its requesting owner was unavailable")
        return true
    end

    local callOk, spawned, spawnResult = pcall(function()
        return Authority.spawn(root, requestingPlayer, pending.garageId, players, deliver)
    end)
    if not callOk then
        spawnResult = spawned
        spawned = false
    end
    if spawned then
        runtime.pendingSwap = nil
        if deliver ~= nil then
            deliver(requestingPlayer, "Announcement", {
                message = "The inactive vehicle was returned to its owner's garage and your vehicle was deployed.",
            })
        end
        return true
    end

    if now < (tonumber(pending.deadlineMs) or now)
        and (not callOk or transientSwapFailure(spawnResult)) then
        pending.nextAttemptMs = now + SWAP_RETRY_DELAY_MS
        return false
    end

    runtime.pendingSwap = nil
    if deliver ~= nil then
        deliver(requestingPlayer, "Error", {
            message = "The inactive vehicle was returned, but your vehicle could not be deployed: "
                .. tostring(spawnResult or "unknown deployment error"),
        })
    end
    Util.log("Deferred garage swap failed safely: " .. tostring(spawnResult))
    return true
end

function Authority.tick(root, players, deliver)
    Authority.ensureState(root)
    local keysChanged = deliverPendingOwnerKeys(root, players, deliver)
    local removalsChanged = processPendingRemovals(root)
    if cleanupOrphanedHideoutVehicles(root) then removalsChanged = true end
    if keysChanged then removalsChanged = true end
    local now = Util.timerNowMs()
    local active = root.activeHideoutVehicle
    if type(active) ~= "table" then
        local swapChanged = processPendingSwap(root, players, deliver, now)
        return swapChanged or removalsChanged
    end
    if runtime.activeToken ~= activeToken(active) then resetRuntimeFor(active, now) end

    if type(active.storePending) == "table" then
        if now >= (tonumber(active.storePending.deadlineMs) or now) then
            local swapRequest = active.storePending.swapRequest
            local stored, result = finalizeStore(root, players, deliver)
            if stored and type(swapRequest) == "table" then
                -- Do not create the replacement in the same vehicle-update tick
                -- as permanentlyRemove(). Let PZ retire the old physics body and
                -- retry briefly while the bay collection settles.
                runtime.pendingSwap = {
                    owner = tostring(swapRequest.owner or ""),
                    garageId = tostring(swapRequest.garageId or ""),
                    removalKey = type(result) == "table" and result.removalKey or nil,
                    nextAttemptMs = now + SWAP_RETRY_DELAY_MS,
                    deadlineMs = now + SWAP_RETRY_TIMEOUT_MS,
                }
            elseif not stored and type(swapRequest) == "table" and deliver ~= nil then
                for _, candidate in ipairs(players or {}) do
                    if Util.garageUsername(candidate) == tostring(swapRequest.owner) then
                        deliver(candidate, "Error", {
                            message = "Vehicle swap was cancelled because the active vehicle could not be saved safely: "
                                .. tostring(result or "unknown storage error"),
                        })
                        break
                    end
                end
            end
            -- finalizeStore always clears or replaces the pending state. Broadcast
            -- even on a safe capture failure so clients stop removal feedback and
            -- immediately regain their controls.
            return true, result
        end
        return removalsChanged
    end

    local vehicle = liveVehicle(active)
    if vehicle ~= nil then
        updateInteraction(active, vehicle, players, now)
        local driver = currentDriver(vehicle)
        local driverUsername = driver ~= nil and Util.username(driver) or nil
        if tostring(runtime.driverUsername or "") ~= tostring(driverUsername or "") then
            runtime.driverUsername = driverUsername
            removalsChanged = true
        end
        -- Reassert the lightweight brake/force lock. Engine features are only
        -- rewritten if another system has actually restored drivetrain power.
        Authority.applyMovementLock(vehicle, active.record, false)
        if now - runtime.lastCaptureMs >= LIVE_CAPTURE_INTERVAL_MS then
            Authority.captureForSave(root)
        end
    end
    local inactiveNow = Authority.isInactive(root, players)
    if runtime.inactiveState ~= inactiveNow then
        runtime.inactiveState = inactiveNow
        removalsChanged = true
    end

    local online = onlinePlayersByName(players)
    if online[tostring(active.owner)] ~= nil then
        runtime.ownerSeen = true
        runtime.ownerAbsentSinceMs = nil
        return removalsChanged
    end

    if runtime.ownerAbsentSinceMs == nil then runtime.ownerAbsentSinceMs = now end
    local grace = runtime.ownerSeen and OWNER_DISCONNECT_GRACE_MS or STARTUP_OWNER_GRACE_MS
    if now - runtime.ownerAbsentSinceMs >= grace then
        local reason = runtime.ownerSeen and "owner disconnected" or "owner absent after restart"
        local started = Authority.beginStore(root, reason, active.owner, players, deliver)
        return started == true or removalsChanged
    end
    return removalsChanged
end

function Authority.activeSummary(root)
    Authority.ensureState(root)
    local active = root.activeHideoutVehicle
    if type(active) ~= "table" then return nil end
    local inactivityMinutes = math.max(1,
        tonumber(Config.value("HideoutVehicleInactivityMinutes")) or 5)
    local inactive = Authority.isInactive(root, Util.players())
    local vehicle = liveVehicle(active)
    local occupied = vehicleOccupied(vehicle)
    local driver = currentDriver(vehicle)
    local driverUsername = driver ~= nil and Util.username(driver) or nil
    local driverGarageOwner = driver ~= nil and Util.garageUsername(driver) or nil
    local inactivityRemaining = math.max(0,
        math.ceil((inactivityMinutes * 60 * 1000
            - math.max(0, Util.timerNowMs() - runtime.lastInteractionMs)) / 1000))
    return {
        owner = active.owner,
        garageId = active.record and active.record.id or nil,
        name = active.record and active.record.name or nil,
        customName = active.record and active.record.customName == true,
        nameOrdinal = active.record and active.record.nameOrdinal or nil,
        scriptName = active.record and active.record.scriptName or nil,
        modelKey = active.record and active.record.modelKey or nil,
        vehicleId = active.vehicleId,
        x = active.x,
        y = active.y,
        z = active.z,
        angleY = tonumber(active.angleY) or tonumber(active.angleZ) or 0,
        engineQuality = active.record and active.record.engineQuality or 0,
        engineLoudness = active.record and active.record.engineLoudness or 0,
        enginePower = active.record and active.record.enginePower or 0,
        storing = type(active.storePending) == "table",
        raidReserved = type(active.raidReservation) == "table",
        occupied = occupied,
        driverUsername = driverUsername,
        driverGarageOwner = driverGarageOwner,
        inactive = inactive,
        inactivitySecondsRemaining = inactivityRemaining,
    }
end

function Authority.transitionSummary(root)
    Authority.ensureState(root)
    local removalKey = pendingHideoutRemoval(root)
    local swap = runtime.pendingSwap
    if removalKey == nil and type(swap) ~= "table" then return nil end
    return {
        busy = true,
        removalPending = removalKey ~= nil,
        swapPending = type(swap) == "table",
        requestedGarageId = type(swap) == "table" and swap.garageId or nil,
    }
end

ExtractionMode.GarageAuthority = Authority
return Authority
