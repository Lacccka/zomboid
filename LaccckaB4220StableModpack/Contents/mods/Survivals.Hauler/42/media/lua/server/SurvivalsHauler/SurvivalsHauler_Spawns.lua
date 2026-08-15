require "VehicleZoneDefinition"

local vehicle = { index = -1, spawnChance = 12 }
local zones = {
    "farm",
    "business",
    "construction",
    "mechanic",
    "junkyard",
    "mccoy",
    "trailerpark",
}

local function addHaulerToZone(zoneName)
    local zone = VehicleZoneDistribution and VehicleZoneDistribution[zoneName]
    if zone and zone.vehicles then
        zone.vehicles["Base.TrailerSurvivalsHauler"] = vehicle
    end
end

for i = 1, #zones do
    addHaulerToZone(zones[i])
end

local MAX_LOAD_SLOTS = 6
local ENABLE_DISPLAY_VEHICLE_COPIES = false
local ENABLE_STATIC_CARGO_PROXIES = false
local ENABLE_HAULER_BODY_VARIANTS = true
local CARGO_PROXY_ITEM_TYPE = "Base.SurvivalsHaulerCargoProxy"
local HAULER_BODY_VISUAL_ITEM_TYPE = "Base.Wrench"
local DISPLAY_SNAP_INTERVAL_TICKS = 1
local DISPLAY_BODY_PARK_DISTANCE = 20000
local displaySnapTick = 0
local activeDisplayHaulers = {}
local activeLoadedOriginals = {}
local activeUnloadLocks = {}
local pendingVehicleStateRestores = {}
local unpackArgs = unpack or (table and table.unpack)
local invalidItemTypes = {}
local DISPLAY_SLOT_OFFSETS = {
    [1] = { x = 0.00, y = 3.60 },
    [2] = { x = 0.00, y = 0.40 },
    [3] = { x = 0.00, y = -2.80 },
    [4] = { x = -0.55, y = 2.00 },
    [5] = { x = -0.55, y = -1.20 },
    [6] = { x = -0.55, y = -4.40 },
}
local CARGO_PROXY_TYPES = {
    "Normal",
    "Lights",
    "LightsPolice",
    "LightsSheriff",
    "LightsRanger",
    "Modern",
    "Modern02",
    "Luxury",
    "Wagon",
    "Small",
    "Small02",
    "Sports",
    "PickupTruck",
    "PickupVan",
    "PickupVanLightsPolice",
    "Van",
    "VanMail",
    "VanSeats",
    "StepVan",
    "SUV",
    "OffRoad",
}
local snapDisplayVehicleToHauler
local createCargoDisplay
local removeCargoDisplay
local removeUnusedCargoDisplays
local removeLoadedOriginalVehicles
local removeAllCargoDisplayCopies
local syncHaulerBodyVisual

local function isHauler(vehicle)
    if not vehicle then
        return false
    end

    local scriptName = vehicle:getScriptName()
    return scriptName == "Base.TrailerSurvivalsHauler"
        or scriptName == "TrailerSurvivalsHauler"
        or scriptName == "Base.SurvivalsHauler"
        or scriptName == "SurvivalsHauler"
end

local function getVehicleByIdSafe(vehicleId)
    if not vehicleId then
        return nil
    end

    if getVehicleById then
        return getVehicleById(tonumber(vehicleId))
    end

    return nil
end

local function getLoadedOriginalKey(haulerId, slot)
    return tostring(haulerId or "nil") .. ":" .. tostring(slot or "nil")
end

local function getLoadedVehicles(hauler)
    local modData = hauler:getModData()
    modData.SurvivalsHaulerLoadedVehicles = modData.SurvivalsHaulerLoadedVehicles or {}
    return modData.SurvivalsHaulerLoadedVehicles
end

local function getRecoveryVehicles(hauler)
    local modData = hauler:getModData()
    modData.SurvivalsHaulerRecoveryVehicles = modData.SurvivalsHaulerRecoveryVehicles or {}
    return modData.SurvivalsHaulerRecoveryVehicles
end

local function findStoredVehicleEntry(hauler, slot)
    local targetSlot = tonumber(slot)
    if not hauler or not targetSlot then
        return nil, nil
    end

    for index, entry in pairs(getLoadedVehicles(hauler)) do
        if entry and tonumber(entry.slot) == targetSlot then
            return entry, index
        end
    end

    return getRecoveryVehicles(hauler)[tostring(targetSlot)] or getRecoveryVehicles(hauler)[targetSlot], nil
end

local function removeStoredVehicleSlot(hauler, slot)
    local targetSlot = tonumber(slot)
    if not hauler or not targetSlot then
        return
    end

    local modData = hauler:getModData()
    local oldLoaded = modData.SurvivalsHaulerLoadedVehicles or {}
    local newLoaded = {}

    for _, entry in pairs(oldLoaded) do
        if entry and tonumber(entry.slot) ~= targetSlot then
            table.insert(newLoaded, entry)
        end
    end

    local oldRecovery = modData.SurvivalsHaulerRecoveryVehicles or {}
    local newRecovery = {}
    for key, entry in pairs(oldRecovery) do
        if entry and tonumber(entry.slot) ~= targetSlot and tonumber(key) ~= targetSlot then
            newRecovery[key] = entry
        end
    end

    modData.SurvivalsHaulerLoadedVehicles = newLoaded
    modData.SurvivalsHaulerRecoveryVehicles = newRecovery
end

local function addStoredVehicleEntry(hauler, entry)
    if not hauler or not entry or not entry.slot then
        return
    end

    removeStoredVehicleSlot(hauler, entry.slot)
    table.insert(getLoadedVehicles(hauler), entry)
    getRecoveryVehicles(hauler)[tostring(entry.slot)] = entry
end

local function getLoadedVehicleCount(hauler)
    if not hauler then
        return 0
    end

    local count = 0
    for _, entry in pairs(getLoadedVehicles(hauler)) do
        if entry and entry.slot then
            count = count + 1
        end
    end

    return count
end

local function registerDisplayHauler(hauler)
    if hauler and hauler.getId then
        activeDisplayHaulers[tostring(hauler:getId())] = hauler:getId()
    end
end

local function isSlotFree(hauler, slot)
    local loaded = getLoadedVehicles(hauler)

    for _, entry in pairs(loaded) do
        if entry and tonumber(entry.slot) == tonumber(slot) then
            return false
        end
    end

    return getRecoveryVehicles(hauler)[tostring(slot)] == nil
end

local function getVehicleDisplayName(vehicle)
    local script = vehicle and vehicle:getScript()
    if script and script.getName then
        return script:getName()
    end

    return tostring(vehicle and vehicle:getScriptName() or "Vehicle")
end

local function getVehicleScriptFullName(vehicle)
    local script = vehicle and vehicle:getScript()
    if script and script.getFullName then
        return script:getFullName()
    end

    local scriptName = vehicle and vehicle:getScriptName()
    if scriptName and not string.find(scriptName, "%.") then
        return "Base." .. scriptName
    end

    return scriptName
end

local function getVehicleScriptShortName(vehicle)
    local scriptName = vehicle and vehicle:getScriptName()
    if not scriptName then
        return nil
    end

    return tostring(scriptName):gsub("^Base%.", "")
end

local function squareBlocksVehicle(square)
    if not square then
        return true
    end

    if square:isSolid() or square:isSolidTrans() then
        return true
    end

    local movingObjects = square:getMovingObjects()
    if movingObjects then
        for i = 0, movingObjects:size() - 1 do
            local object = movingObjects:get(i)
            if object and (instanceof(object, "BaseVehicle") or instanceof(object, "IsoGameCharacter")) then
                return true
            end
        end
    end

    return false
end

local function isClearVehicleFootprint(x, y, z)
    local cell = getCell()
    local baseX = math.floor(x)
    local baseY = math.floor(y)
    local baseZ = math.floor(z)

    for dx = -2, 2 do
        for dy = -3, 3 do
            if squareBlocksVehicle(cell:getGridSquare(baseX + dx, baseY + dy, baseZ)) then
                return false
            end
        end
    end

    return true
end

local function getVehicleYawRadians(vehicle)
    local angle = 0

    if vehicle and vehicle.getAngleY then
        angle = tonumber(vehicle:getAngleY()) or 0
    elseif vehicle and vehicle.getAngleZ then
        angle = tonumber(vehicle:getAngleZ()) or 0
    end

    if math.abs(angle) > 6.28319 then
        angle = math.rad(angle)
    end

    return angle
end

local function addHaulerLocalOffset(offsets, hauler, localX, localY)
    if not hauler then
        return
    end

    local angle = getVehicleYawRadians(hauler)
    local sin = math.sin(angle)
    local cos = math.cos(angle)
    table.insert(offsets, {
        localX * cos - localY * sin,
        localX * sin + localY * cos,
    })
end

local function getCandidateSpawnOffsets(player, hauler)
    local offsets = {}

    addHaulerLocalOffset(offsets, hauler, 0, -8)
    addHaulerLocalOffset(offsets, hauler, 0, -10)
    addHaulerLocalOffset(offsets, hauler, 0, -12)
    addHaulerLocalOffset(offsets, hauler, -2, -10)
    addHaulerLocalOffset(offsets, hauler, 2, -10)
    addHaulerLocalOffset(offsets, hauler, -3, -12)
    addHaulerLocalOffset(offsets, hauler, 3, -12)

    local rings = { 4, 6, 8, 10, 12, 14, 16, 18, 20 }
    for _, ring in ipairs(rings) do
        table.insert(offsets, { ring, 0 })
        table.insert(offsets, { -ring, 0 })
        table.insert(offsets, { 0, ring })
        table.insert(offsets, { 0, -ring })
        table.insert(offsets, { ring, ring })
        table.insert(offsets, { ring, -ring })
        table.insert(offsets, { -ring, ring })
        table.insert(offsets, { -ring, -ring })
    end

    return offsets
end

local function getVehicleSpawnPoint(player, hauler)
    local originX = hauler:getX()
    local originY = hauler:getY()
    local originZ = hauler:getZ()
    local offsets = getCandidateSpawnOffsets(player, hauler)

    for _, offset in ipairs(offsets) do
        local x = originX + offset[1]
        local y = originY + offset[2]
        if isClearVehicleFootprint(x, y, originZ) then
            return x, y, originZ
        end
    end

    return nil
end

local function getStoredVehicleSpawnPoint(entry, player, hauler)
    local x, y, z = getVehicleSpawnPoint(player, hauler)
    if x then
        return x, y, z
    end

    if entry and entry.x and entry.y then
        local entryX = tonumber(entry.x)
        local entryY = tonumber(entry.y)
        local entryZ = tonumber(entry.z) or (hauler and hauler:getZ()) or 0
        if entryX and entryY and isClearVehicleFootprint(entryX, entryY, entryZ) then
            return entryX, entryY, entryZ
        end
    end

    return nil
end

local function applyStoredAppearance(vehicle, entry)
    if not vehicle or not entry then
        return
    end

    if entry.skinIndex and vehicle.setSkinIndex then
        vehicle:setSkinIndex(tonumber(entry.skinIndex))
        vehicle:transmitSkinIndex()
    end

    if entry.h and entry.s and entry.v and vehicle.setColorHSV then
        vehicle:setColorHSV(tonumber(entry.h), tonumber(entry.s), tonumber(entry.v))
        vehicle:transmitColorHSV()
    end

    if entry.rust and vehicle.setRust then
        vehicle:setRust(tonumber(entry.rust))
        if vehicle.transmitRust then
            vehicle:transmitRust()
        elseif vehicle.transmit then
            vehicle:transmit()
        end
    end
end

local function clearContainerItems(container, syncRemovals)
    if not container then
        return
    end

    if syncRemovals and container.getItems then
        local okItems, items = pcall(function()
            return container:getItems()
        end)
        if okItems and items and items.size and items.get then
            for index = items:size() - 1, 0, -1 do
                local item = items:get(index)
                if item then
                    if container.DoRemoveItem then
                        pcall(function()
                            container:DoRemoveItem(item)
                        end)
                    elseif container.Remove then
                        pcall(function()
                            container:Remove(item)
                        end)
                    end
                    if sendRemoveItemFromContainer then
                        pcall(function()
                            sendRemoveItemFromContainer(container, item)
                        end)
                    end
                end
            end
            return
        end
    end

    if container.clear then
        local ok = pcall(function()
            container:clear()
        end)
        if ok then
            return
        end
    end

    if container.getItems then
        local ok, items = pcall(function()
            return container:getItems()
        end)
        if ok and items then
            if items.clear then
                pcall(function()
                    items:clear()
                end)
            elseif items.size and items.remove then
                for index = items:size() - 1, 0, -1 do
                    pcall(function()
                        items:remove(index)
                    end)
                end
            end
        end
    end
end

local function getObjectMethodValue(object, methodName)
    if not object or not methodName or not object[methodName] then
        return nil
    end

    local ok, result = pcall(function()
        return object[methodName](object)
    end)
    if ok then
        return result
    end

    return nil
end

local function setObjectMethodValue(object, methodName, value)
    if not object or value == nil or not methodName or not object[methodName] then
        return false
    end

    local ok = pcall(function()
        object[methodName](object, value)
    end)
    if ok then
        return true
    end

    return pcall(function()
        object[methodName](value)
    end)
end

local function getObjectBoolValue(object, methodName)
    local value = getObjectMethodValue(object, methodName)
    if value == nil then
        return nil
    end

    return value == true
end

local function restoreVehicleIdentity(vehicle, entry)
    if not vehicle or not entry then
        return
    end

    local keyId = tonumber(entry.keyId)
    if keyId then
        setObjectMethodValue(vehicle, "setKeyId", keyId)
    end

    if entry.haveKey ~= nil then
        setObjectMethodValue(vehicle, "setHaveKey", entry.haveKey == true)
    end

    if vehicle.transmitModData then
        pcall(function()
            vehicle:transmitModData()
        end)
    end
    if vehicle.transmit then
        pcall(function()
            vehicle:transmit()
        end)
    end
end

local function setObjectMethodValue2(object, methodName, value1, value2, value3)
    if not object or value1 == nil or not methodName or not object[methodName] then
        return false
    end

    local ok = pcall(function()
        object[methodName](object, value1, value2, value3)
    end)
    if ok then
        return true
    end

    return setObjectMethodValue(object, methodName, value1)
end

local function snapshotInventoryItem(item)
    if not item then
        return nil
    end

    return {
        fullType = tostring(getObjectMethodValue(item, "getFullType") or ""),
        condition = tonumber(getObjectMethodValue(item, "getCondition")),
        usedDelta = tonumber(getObjectMethodValue(item, "getUsedDelta")),
        drainableUses = tonumber(getObjectMethodValue(item, "getDrainableUsesInt")),
        uses = tonumber(getObjectMethodValue(item, "getUses")),
        currentUses = tonumber(getObjectMethodValue(item, "getCurrentUses")),
        currentUsesFloat = tonumber(getObjectMethodValue(item, "getCurrentUsesFloat")),
        count = tonumber(getObjectMethodValue(item, "getCount")),
        haveBeenRepaired = tonumber(getObjectMethodValue(item, "getHaveBeenRepaired")),
        keyId = tonumber(getObjectMethodValue(item, "getKeyId")),
    }
end

local function restoreInventoryItemState(item, snapshot)
    if not item or not snapshot then
        return
    end

    setObjectMethodValue(item, "setCondition", tonumber(snapshot.condition))
    setObjectMethodValue(item, "setUsedDelta", tonumber(snapshot.usedDelta))
    setObjectMethodValue(item, "setDrainableUsesInt", tonumber(snapshot.drainableUses))
    setObjectMethodValue(item, "setUses", tonumber(snapshot.uses))
    setObjectMethodValue(item, "setCurrentUses", tonumber(snapshot.currentUses))
    setObjectMethodValue(item, "setCurrentUsesFloat", tonumber(snapshot.currentUsesFloat))
    setObjectMethodValue(item, "setCount", tonumber(snapshot.count))
    setObjectMethodValue(item, "setHaveBeenRepaired", tonumber(snapshot.haveBeenRepaired))
    setObjectMethodValue(item, "setKeyId", tonumber(snapshot.keyId))
end

local function snapshotContainerItems(container)
    local snapshot = {}
    if not container or not container.getItems then
        return snapshot
    end

    local ok, items = pcall(function()
        return container:getItems()
    end)
    if not ok or not items or not items.size or not items.get then
        return snapshot
    end

    for index = 0, items:size() - 1 do
        local item = items:get(index)
        local itemSnapshot = snapshotInventoryItem(item)
        if itemSnapshot and itemSnapshot.fullType and itemSnapshot.fullType ~= "" then
            table.insert(snapshot, itemSnapshot)
        end
    end

    return snapshot
end

local function restoreContainerItems(container, itemsSnapshot, syncItems)
    if not container then
        return 0
    end

    clearContainerItems(container, syncItems == true)

    if not itemsSnapshot then
        return 0
    end

    local restoredCount = 0
    for _, itemSnapshot in ipairs(itemsSnapshot) do
        if itemSnapshot and itemSnapshot.fullType and itemSnapshot.fullType ~= "" and container.AddItem then
            local ok, item = pcall(function()
                return container:AddItem(itemSnapshot.fullType)
            end)
            if ok and item then
                restoreInventoryItemState(item, itemSnapshot)
                if syncItems and sendAddItemToContainer then
                    pcall(function()
                        sendAddItemToContainer(container, item)
                    end)
                end
                restoredCount = restoredCount + 1
            end
        end
    end

    if container.setExplored then
        pcall(function()
            container:setExplored(true)
        end)
    end
    if container.setDirty then
        pcall(function()
            container:setDirty(true)
        end)
    end
    if container.setDrawDirty then
        pcall(function()
            container:setDrawDirty(true)
        end)
    end
    if container.requestSync then
        pcall(function()
            container:requestSync()
        end)
    end

    return restoredCount
end

local function snapshotVehicleState(vehicle)
    local snapshot = { parts = {}, partCount = 0, containerCount = 0, itemCount = 0, damagedCount = 0 }
    if not vehicle or not vehicle.getPartCount or not vehicle.getPartByIndex then
        return snapshot
    end

    for index = 0, vehicle:getPartCount() - 1 do
        local part = vehicle:getPartByIndex(index)
        if part then
            local partId = tostring(getObjectMethodValue(part, "getId") or "")
            if partId ~= "" then
                local item = getObjectMethodValue(part, "getInventoryItem")
                local container = getObjectMethodValue(part, "getItemContainer")
                local door = getObjectMethodValue(part, "getDoor")
                local containerItems = snapshotContainerItems(container)
                local condition = tonumber(getObjectMethodValue(part, "getCondition"))
                snapshot.parts[partId] = {
                    condition = condition,
                    containerContentAmount = tonumber(getObjectMethodValue(part, "getContainerContentAmount")),
                    locked = getObjectBoolValue(part, "isLocked"),
                    doorLocked = getObjectBoolValue(door, "isLocked"),
                    containerLocked = getObjectBoolValue(container, "isLocked"),
                    item = snapshotInventoryItem(item),
                    containerItems = containerItems,
                }
                snapshot.partCount = snapshot.partCount + 1
                if condition and condition < 100 then
                    snapshot.damagedCount = snapshot.damagedCount + 1
                end
                if container then
                    snapshot.containerCount = snapshot.containerCount + 1
                    snapshot.itemCount = snapshot.itemCount + #(containerItems or {})
                end
            end
        end
    end

    return snapshot
end

local function encodeStateValue(value)
    if value == nil then
        return ""
    end

    local encoded = tostring(value)
        :gsub("%%", "%%25")
        :gsub("|", "%%7C")
        :gsub(";", "%%3B")
        :gsub("\n", "%%0A")
        :gsub("\r", "%%0D")

    return encoded
end

local function decodeStateValue(value)
    if not value or value == "" then
        return nil
    end

    local decoded = tostring(value)
        :gsub("%%0D", "\r")
        :gsub("%%0A", "\n")
        :gsub("%%3B", ";")
        :gsub("%%7C", "|")
        :gsub("%%25", "%%")

    return decoded
end

local function splitStateLine(line)
    local fields = {}
    local text = tostring(line or "")
    local startIndex = 1

    while true do
        local separatorIndex = string.find(text, "|", startIndex, true)
        if separatorIndex then
            table.insert(fields, string.sub(text, startIndex, separatorIndex - 1))
            startIndex = separatorIndex + 1
        else
            table.insert(fields, string.sub(text, startIndex))
            break
        end
    end

    return fields
end

local function serializeItemSnapshot(prefix, partId, item)
    if not item or not item.fullType or item.fullType == "" then
        return nil
    end

    return table.concat({
        prefix,
        encodeStateValue(partId),
        encodeStateValue(item.fullType),
        encodeStateValue(item.condition),
        encodeStateValue(item.usedDelta),
        encodeStateValue(item.drainableUses),
        encodeStateValue(item.uses),
        encodeStateValue(item.count),
        encodeStateValue(item.currentUses),
        encodeStateValue(item.currentUsesFloat),
        encodeStateValue(item.haveBeenRepaired),
        encodeStateValue(item.keyId),
    }, "|")
end

local function serializeVehicleState(snapshot)
    if not snapshot or not snapshot.parts then
        return ""
    end

    local lines = {}
    for partId, partSnapshot in pairs(snapshot.parts) do
        table.insert(lines, table.concat({
            "P",
            encodeStateValue(partId),
            encodeStateValue(partSnapshot.condition),
            encodeStateValue(partSnapshot.containerContentAmount),
            encodeStateValue(partSnapshot.locked),
            encodeStateValue(partSnapshot.doorLocked),
            encodeStateValue(partSnapshot.containerLocked),
        }, "|"))

        local itemLine = serializeItemSnapshot("I", partId, partSnapshot.item)
        if itemLine then
            table.insert(lines, itemLine)
        end

        for _, itemSnapshot in ipairs(partSnapshot.containerItems or {}) do
            local containerLine = serializeItemSnapshot("C", partId, itemSnapshot)
            if containerLine then
                table.insert(lines, containerLine)
            end
        end
    end

    return table.concat(lines, ";")
end

local function deserializeItemSnapshot(fields)
    return {
        fullType = decodeStateValue(fields[3]),
        condition = tonumber(decodeStateValue(fields[4])),
        usedDelta = tonumber(decodeStateValue(fields[5])),
        drainableUses = tonumber(decodeStateValue(fields[6])),
        uses = tonumber(decodeStateValue(fields[7])),
        count = tonumber(decodeStateValue(fields[8])),
        currentUses = tonumber(decodeStateValue(fields[9])),
        currentUsesFloat = tonumber(decodeStateValue(fields[10])),
        haveBeenRepaired = tonumber(decodeStateValue(fields[11])),
        keyId = tonumber(decodeStateValue(fields[12])),
    }
end

local function decodeStateBool(value)
    local decoded = decodeStateValue(value)
    if decoded == nil then
        return nil
    end

    return decoded == "true"
end

local function deserializeVehicleState(data)
    local snapshot = { parts = {} }
    if not data or data == "" then
        return snapshot
    end

    for line in tostring(data):gmatch("[^;\n]+") do
        local fields = splitStateLine(line)
        local recordType = fields[1]
        local partId = decodeStateValue(fields[2])
        if partId and partId ~= "" then
            snapshot.parts[partId] = snapshot.parts[partId] or { containerItems = {} }
            local partSnapshot = snapshot.parts[partId]
            if recordType == "P" then
                partSnapshot.condition = tonumber(decodeStateValue(fields[3]))
                partSnapshot.containerContentAmount = tonumber(decodeStateValue(fields[4]))
                partSnapshot.locked = decodeStateBool(fields[5])
                partSnapshot.doorLocked = decodeStateBool(fields[6])
                partSnapshot.containerLocked = decodeStateBool(fields[7])
            elseif recordType == "I" then
                partSnapshot.item = deserializeItemSnapshot(fields)
            elseif recordType == "C" then
                table.insert(partSnapshot.containerItems, deserializeItemSnapshot(fields))
            end
        end
    end

    return snapshot
end

local function sanitizeStoredEntry(entry)
    if not entry then
        return false
    end

    if entry.vehicleState and not entry.vehicleStateData then
        entry.vehicleStateData = serializeVehicleState(entry.vehicleState)
        entry.vehicleState = nil
        return true
    end

    if entry.vehicleState ~= nil then
        entry.vehicleState = nil
        return true
    end

    return false
end

local function sanitizeHaulerStoredVehicleState(hauler)
    if not hauler then
        return false
    end

    local changed = false
    for _, entry in pairs(getLoadedVehicles(hauler)) do
        changed = sanitizeStoredEntry(entry) or changed
    end

    for _, entry in pairs(getRecoveryVehicles(hauler)) do
        changed = sanitizeStoredEntry(entry) or changed
    end

    return changed
end

local function transmitRestoredPart(vehicle, part)
    if not vehicle or not part then
        return
    end

    if vehicle.transmitPartItem then
        pcall(function()
            vehicle:transmitPartItem(part)
        end)
    end
    if vehicle.transmitPartCondition then
        pcall(function()
            vehicle:transmitPartCondition(part)
        end)
    end
    if vehicle.transmitPartModData then
        pcall(function()
            vehicle:transmitPartModData(part)
        end)
    end
    if vehicle.transmitPartUsedDelta then
        pcall(function()
            vehicle:transmitPartUsedDelta(part)
        end)
    end
    if vehicle.transmitPartDoor then
        pcall(function()
            vehicle:transmitPartDoor(part)
        end)
    end
    if vehicle.transmitPartWindow then
        pcall(function()
            vehicle:transmitPartWindow(part)
        end)
    end
    if part.transmitCondition then
        pcall(function()
            part:transmitCondition()
        end)
    end
end

local function getVehiclePartByIdSafe(vehicle, partId)
    if not vehicle or not partId then
        return nil
    end

    if vehicle.getPartById then
        local ok, part = pcall(function()
            return vehicle:getPartById(partId)
        end)
        if ok and part then
            return part
        end
    end

    if vehicle.getPartCount and vehicle.getPartByIndex then
        for index = 0, vehicle:getPartCount() - 1 do
            local part = vehicle:getPartByIndex(index)
            if part and tostring(getObjectMethodValue(part, "getId") or "") == tostring(partId) then
                return part
            end
        end
    end

    return nil
end

local function restorePartState(vehicle, part, partSnapshot, restoreInventoryContainers)
    if not vehicle or not part or not partSnapshot then
        return
    end

    setObjectMethodValue(part, "setCondition", tonumber(partSnapshot.condition))
    setObjectMethodValue2(part, "setContainerContentAmount", tonumber(partSnapshot.containerContentAmount), false, true)
    setObjectMethodValue(part, "setLocked", partSnapshot.locked)

    local door = getObjectMethodValue(part, "getDoor")
    setObjectMethodValue(door, "setLocked", partSnapshot.doorLocked)

    if part.setInventoryItem then
        local currentItem = getObjectMethodValue(part, "getInventoryItem")
        if partSnapshot.item and partSnapshot.item.fullType and partSnapshot.item.fullType ~= "" then
            if not currentItem or tostring(getObjectMethodValue(currentItem, "getFullType") or "") ~= tostring(partSnapshot.item.fullType) then
                local ok, newItem = pcall(instanceItem, partSnapshot.item.fullType)
                if ok and newItem then
                    currentItem = newItem
                    pcall(function()
                        part:setInventoryItem(newItem)
                    end)
                end
            end
            restoreInventoryItemState(currentItem, partSnapshot.item)
        elseif currentItem then
            pcall(function()
                part:setInventoryItem(nil)
            end)
        end

        if currentItem and part.doInventoryItemStats then
            pcall(function()
                part:doInventoryItemStats(currentItem, getObjectMethodValue(part, "getMechanicSkillInstaller") or 0)
            end)
        end
    end

    local container = getObjectMethodValue(part, "getItemContainer")
    if container then
        setObjectMethodValue(container, "setLocked", partSnapshot.containerLocked)
        if restoreInventoryContainers then
            restoreContainerItems(container, partSnapshot.containerItems, true)
        end
        if restoreInventoryContainers and partSnapshot.containerItems and #(partSnapshot.containerItems) > 0 then
            local weight = tonumber(getObjectMethodValue(container, "getCapacityWeight"))
                or tonumber(getObjectMethodValue(container, "getContentsWeight"))
            setObjectMethodValue2(part, "setContainerContentAmount", weight, false, true)
        else
            setObjectMethodValue2(part, "setContainerContentAmount", tonumber(partSnapshot.containerContentAmount), false, true)
        end
    end

    transmitRestoredPart(vehicle, part)
end

local function restoreVehicleState(vehicle, snapshot, logRestore, restoreInventoryContainers)
    if not vehicle or not snapshot or not snapshot.parts or not vehicle.getPartCount or not vehicle.getPartByIndex then
        return
    end

    local restoredCount = 0
    local missingCount = 0
    local restoredDamagedCount = 0
    local restoredContainerCount = 0
    local restoredItemCount = 0
    for partId, partSnapshot in pairs(snapshot.parts) do
        local part = getVehiclePartByIdSafe(vehicle, partId)
        if part then
            restorePartState(vehicle, part, partSnapshot, restoreInventoryContainers == true)
            restoredCount = restoredCount + 1
            if tonumber(partSnapshot.condition) and tonumber(partSnapshot.condition) < 100 then
                restoredDamagedCount = restoredDamagedCount + 1
            end
            if partSnapshot.containerItems then
                restoredContainerCount = restoredContainerCount + 1
                restoredItemCount = restoredItemCount + #(partSnapshot.containerItems)
            end
        else
            missingCount = missingCount + 1
        end
    end

    if vehicle.updatePartStats then
        pcall(function()
            vehicle:updatePartStats()
        end)
    end
    if vehicle.updateBulletStats then
        pcall(function()
            vehicle:updateBulletStats()
        end)
    end
    if vehicle.setNeedPartsUpdate then
        pcall(function()
            vehicle:setNeedPartsUpdate(true)
        end)
    end

    local modData = vehicle:getModData()
    modData.SurvivalsHaulerLastRestoreParts = restoredCount
    modData.SurvivalsHaulerLastRestoreMissingParts = missingCount
    modData.SurvivalsHaulerLastRestoreDamagedParts = restoredDamagedCount
    modData.SurvivalsHaulerLastRestoreContainers = restoredContainerCount
    modData.SurvivalsHaulerLastRestoreItems = restoredItemCount

    if logRestore then
        print(string.format(
            "[SurvivalsHauler] restored state on %s: parts=%s damaged=%s containers=%s items=%s missing=%s",
            tostring(vehicle:getScriptName()),
            tostring(restoredCount),
            tostring(restoredDamagedCount),
            tostring(restoredContainerCount),
            tostring(restoredItemCount),
            tostring(missingCount)
        ))
    end

    if vehicle.transmitModData then
        pcall(function()
            vehicle:transmitModData()
        end)
    end
    if vehicle.transmit then
        pcall(function()
            vehicle:transmit()
        end)
    end
end

local function queueVehicleStateRestore(vehicle, stateData, entry)
    if not vehicle or not vehicle.getId or not stateData or stateData == "" then
        return
    end

    local vehicleId = tonumber(vehicle:getId())
    if not vehicleId then
        return
    end

    pendingVehicleStateRestores[tostring(vehicleId)] = {
        vehicleId = vehicleId,
        stateData = stateData,
        keyId = entry and entry.keyId or nil,
        haveKey = entry and entry.haveKey or nil,
        ticksUntilNext = 1,
        remaining = 8,
    }
end

local function processPendingVehicleStateRestores()
    for key, restore in pairs(pendingVehicleStateRestores) do
        restore.ticksUntilNext = tonumber(restore.ticksUntilNext or 0) - 1
        if restore.ticksUntilNext <= 0 then
            local vehicle = getVehicleByIdSafe(restore.vehicleId)
            if vehicle then
                restoreVehicleState(vehicle, deserializeVehicleState(restore.stateData), false, false)
                restoreVehicleIdentity(vehicle, restore)
                restore.remaining = tonumber(restore.remaining or 1) - 1
                restore.ticksUntilNext = 10
                if restore.remaining <= 0 then
                    pendingVehicleStateRestores[key] = nil
                end
            else
                pendingVehicleStateRestores[key] = nil
            end
        end
    end
end

local function stripFreshSpawnLoot(vehicle)
    if not vehicle or not vehicle.getPartCount or not vehicle.getPartByIndex then
        return
    end

    for index = 0, vehicle:getPartCount() - 1 do
        local part = vehicle:getPartByIndex(index)
        if part and part.getItemContainer then
            local ok, container = pcall(function()
                return part:getItemContainer()
            end)
            if ok and container then
                clearContainerItems(container, true)
            end
        end
    end
end

local function getCargoProxyType(scriptName)
    local script = string.lower(tostring(scriptName or ""))

    if string.find(script, "pickupvan") then
        if string.find(script, "police") then
            return "PickupVanLightsPolice"
        end
        return "PickupVan"
    end
    if string.find(script, "pickuptruck") or string.find(script, "pickup") then
        return "PickupTruck"
    end
    if string.find(script, "vanseats") then
        return "VanSeats"
    end
    if string.find(script, "stepvan") then
        return "StepVan"
    end
    if string.find(script, "vanmail") or string.find(script, "postal") or string.find(script, "mail") then
        return "VanMail"
    end
    if string.find(script, "ambulance") or string.find(script, "van") then
        return "Van"
    end
    if string.find(script, "stationwagon") then
        return "Wagon"
    end
    if string.find(script, "offroad") then
        return "OffRoad"
    end
    if string.find(script, "suv") then
        return "SUV"
    end
    if string.find(script, "sports") or string.find(script, "race") then
        return "Sports"
    end
    if string.find(script, "small02") then
        return "Small02"
    end
    if string.find(script, "small") then
        return "Small"
    end
    if string.find(script, "modern02") then
        return "Modern02"
    end
    if string.find(script, "modern") then
        return "Modern"
    end
    if string.find(script, "luxury") then
        return "Luxury"
    end
    if string.find(script, "bulletinsheriff") or string.find(script, "sheriff") then
        return "LightsSheriff"
    end
    if string.find(script, "police") then
        return "LightsPolice"
    end
    if string.find(script, "ranger") then
        return "LightsRanger"
    end
    if string.find(script, "lights") then
        return "Lights"
    end

    return "Normal"
end

local function scriptItemExists(fullType)
    if not fullType or invalidItemTypes[fullType] then
        return false
    end

    local scriptManager = getScriptManager and getScriptManager() or nil
    if scriptManager and scriptManager.FindItem then
        local ok, scriptItem = pcall(function()
            return scriptManager:FindItem(fullType)
        end)
        if ok and scriptItem then
            return true
        end

        invalidItemTypes[fullType] = true
        print(string.format("[SurvivalsHauler] disabled missing item token %s", tostring(fullType)))
        return false
    end

    return true
end

local function vehicleScriptExists(scriptName)
    if not scriptName then
        return false
    end

    local scriptManager = getScriptManager and getScriptManager() or nil
    if scriptManager and scriptManager.getVehicle then
        local ok, script = pcall(function()
            return scriptManager:getVehicle(scriptName)
        end)
        return ok and script ~= nil
    end

    return true
end

local function createItemSafe(fullType)
    if not scriptItemExists(fullType) then
        return nil
    end

    local ok, item = pcall(instanceItem, fullType)
    if ok and item then
        return item
    end

    invalidItemTypes[fullType] = true
    print(string.format("[SurvivalsHauler] disabled failing item token %s", tostring(fullType)))
    return nil
end

local function setCargoProxyVisible(hauler, slot, visible, proxyType)
    if not hauler or not slot then
        return
    end

    if not ENABLE_STATIC_CARGO_PROXIES then
        visible = false
    end

    local targetType = proxyType or "Normal"
    local showedPart = false

    for _, cargoProxyType in ipairs(CARGO_PROXY_TYPES) do
        local partId = "CargoProxy" .. tostring(slot) .. "_" .. cargoProxyType
        local part = hauler:getPartById(partId)
        if part then
            if visible and cargoProxyType == targetType then
                if not part:getInventoryItem() then
                    local item = createItemSafe(CARGO_PROXY_ITEM_TYPE)
                    if item then
                        part:setInventoryItem(item)
                    end
                end
                showedPart = true
            elseif part:getInventoryItem() then
                part:setInventoryItem(nil)
            end

            hauler:transmitPartItem(part)
        end
    end

    if visible and not showedPart then
        print(string.format("[SurvivalsHauler] missing CargoProxy%s_%s part", tostring(slot), tostring(targetType)))
    end
end

local function syncCargoProxyParts(hauler)
    if not isHauler(hauler) then
        return
    end

    local occupied = {}
    for _, entry in pairs(getLoadedVehicles(hauler)) do
        if entry and entry.slot then
            entry.proxyType = getCargoProxyType(entry.script)
            occupied[tonumber(entry.slot)] = entry.proxyType
        end
    end
    for slot, entry in pairs(getRecoveryVehicles(hauler)) do
        if entry then
            entry.proxyType = getCargoProxyType(entry.script)
            occupied[tonumber(slot)] = entry.proxyType
        end
    end

    for slot = 1, MAX_LOAD_SLOTS do
        setCargoProxyVisible(hauler, slot, occupied[slot] ~= nil, occupied[slot])
    end
end

syncHaulerBodyVisual = function(hauler)
    if not isHauler(hauler) then
        return
    end

    local visualCount = 0
    if ENABLE_HAULER_BODY_VARIANTS then
        visualCount = math.min(5, math.max(0, getLoadedVehicleCount(hauler)))
    end

    for count = 1, 5 do
        local part = hauler:getPartById("HaulerBodyVisual" .. tostring(count))
        if part then
            local changed = false
            if visualCount > 0 and count == visualCount then
                local currentItem = part:getInventoryItem()
                local currentType = currentItem and currentItem.getFullType and currentItem:getFullType() or nil
                if currentType ~= HAULER_BODY_VISUAL_ITEM_TYPE then
                    local item = createItemSafe(HAULER_BODY_VISUAL_ITEM_TYPE)
                    if item then
                        part:setInventoryItem(item)
                        changed = true
                    end
                end
            elseif part:getInventoryItem() then
                part:setInventoryItem(nil)
                changed = true
            end

            if changed then
                hauler:transmitPartItem(part)
            end
        elseif visualCount == count then
            print(string.format("[SurvivalsHauler] missing HaulerBodyVisual%s part", tostring(count)))
        end
    end
end

local function syncHaulerBodyVisualsInCell()
    if not getCell then
        return
    end

    local cell = getCell()
    if not cell or not cell.getVehicles then
        return
    end

    local okVehicles, vehicles = pcall(function()
        return cell:getVehicles()
    end)
    if not okVehicles or not vehicles or not vehicles.size or not vehicles.get then
        return
    end

    for index = 0, vehicles:size() - 1 do
        local okVehicle, vehicle = pcall(function()
            return vehicles:get(index)
        end)
        if okVehicle and isHauler(vehicle) then
            if sanitizeHaulerStoredVehicleState(vehicle) then
                vehicle:transmitModData()
            end
            syncHaulerBodyVisual(vehicle)
            if removeLoadedOriginalVehicles then
                removeLoadedOriginalVehicles(vehicle)
            end
        end
    end
end

local function getVehicleSpeedSafe(vehicle)
    if not vehicle or not vehicle.getCurrentSpeedKmHour then
        return 0
    end

    local ok, speed = pcall(function()
        return vehicle:getCurrentSpeedKmHour()
    end)

    if ok and tonumber(speed) then
        return math.abs(tonumber(speed))
    end

    return 0
end

local function hasNearbyTowVehicle(hauler)
    if not hauler or not hauler.getSquare or not getCell then
        return false
    end

    local square = hauler:getSquare() or hauler:getCurrentSquare()
    if not square then
        return false
    end

    local cell = getCell()
    local haulerX = hauler:getX()
    local haulerY = hauler:getY()
    local radius = 8

    for y = square:getY() - radius, square:getY() + radius do
        for x = square:getX() - radius, square:getX() + radius do
            local square2 = cell:getGridSquare(x, y, square:getZ())
            if square2 then
                local movingObjects = square2:getMovingObjects()
                if movingObjects then
                    for i = 0, movingObjects:size() - 1 do
                        local vehicle = movingObjects:get(i)
                        if vehicle
                                and vehicle ~= hauler
                                and instanceof(vehicle, "BaseVehicle")
                                and not isHauler(vehicle)
                                and vehicle.getModData then
                            local modData = vehicle:getModData()
                            if modData
                                    and not modData.SurvivalsHaulerDisplayOnly
                                    and not modData.SurvivalsHaulerLoadedOn then
                                local dx = vehicle:getX() - haulerX
                                local dy = vehicle:getY() - haulerY
                                if dx * dx + dy * dy <= radius * radius then
                                    return true
                                end
                            end
                        end
                    end
                end
            end
        end
    end

    return false
end

local function shouldShowCargoDisplays(hauler)
    if not ENABLE_DISPLAY_VEHICLE_COPIES or not hauler then
        return false
    end

    return true
end

local function syncCargoDisplays(hauler, player)
    if not isHauler(hauler) then
        return
    end

    if sanitizeHaulerStoredVehicleState(hauler) then
        hauler:transmitModData()
    end

    registerDisplayHauler(hauler)
    syncHaulerBodyVisual(hauler)
    if removeLoadedOriginalVehicles then
        removeLoadedOriginalVehicles(hauler)
    end
    if removeUnusedCargoDisplays then
        removeUnusedCargoDisplays(hauler)
    end

    if not shouldShowCargoDisplays(hauler) then
        for _, entry in pairs(getLoadedVehicles(hauler)) do
            if entry and entry.displayVehicleId and removeCargoDisplay then
                removeCargoDisplay(entry)
            end
        end
        return
    end

    for _, entry in pairs(getLoadedVehicles(hauler)) do
        if entry and entry.slot and entry.script then
            local displayVehicle = entry.displayVehicleId and getVehicleByIdSafe(entry.displayVehicleId) or nil
            if displayVehicle then
                snapDisplayVehicleToHauler(hauler, entry)
            else
                createCargoDisplay(hauler, entry, player)
            end
        end
    end
end

local function removeVehicleCompletely(vehicle)
    if not vehicle then
        return
    end

    if vehicle.breakConstraint then
        pcall(function()
            vehicle:breakConstraint(true, false)
        end)
    end
    if vehicle.setPhysicsActive then
        pcall(function()
            vehicle:setPhysicsActive(false)
        end)
    end
    if vehicle.removeFromWorld then
        pcall(function()
            vehicle:removeFromWorld()
        end)
    end
    if vehicle.removeFromSquare then
        pcall(function()
            vehicle:removeFromSquare()
        end)
    end
    if vehicle.permanentlyRemove then
        pcall(function()
            vehicle:permanentlyRemove()
        end)
    end
end

local function detachVehicleFromWorld(vehicle)
    if not vehicle then
        return
    end

    if vehicle.breakConstraint then
        pcall(function()
            vehicle:breakConstraint(true, false)
        end)
    end
    if vehicle.setPhysicsActive then
        pcall(function()
            vehicle:setPhysicsActive(false)
        end)
    end
    if vehicle.transmitModData then
        pcall(function()
            vehicle:transmitModData()
        end)
    end
    if vehicle.transmit then
        pcall(function()
            vehicle:transmit()
        end)
    end
    if vehicle.removeFromWorld then
        pcall(function()
            vehicle:removeFromWorld()
        end)
    end
    if vehicle.removeFromSquare then
        pcall(function()
            vehicle:removeFromSquare()
        end)
    end
end

local function getParkedVehiclePosition(hauler, slot)
    local baseX = hauler and hauler.getX and hauler:getX() or 0
    local baseY = hauler and hauler.getY and hauler:getY() or 0
    local baseZ = hauler and hauler.getZ and hauler:getZ() or 0
    local slotOffset = tonumber(slot) or 0

    return baseX + DISPLAY_BODY_PARK_DISTANCE + slotOffset * 6, baseY + DISPLAY_BODY_PARK_DISTANCE, baseZ
end

local function callVehicleMethod(vehicle, methodName, ...)
    if not vehicle or not methodName then
        return false
    end

    local method = vehicle[methodName]
    if not method then
        return false
    end

    local args = { ... }
    return pcall(function()
        method(vehicle, unpackArgs(args))
    end)
end

local function setVehicleWorldPosition(vehicle, x, y, z, activePhysics)
    if not vehicle or not x or not y or not z then
        return
    end

    callVehicleMethod(vehicle, "setPhysicsActive", false)
    callVehicleMethod(vehicle, "setX", x)
    callVehicleMethod(vehicle, "setY", y)
    callVehicleMethod(vehicle, "setZ", z)
    callVehicleMethod(vehicle, "setForceX", x)
    callVehicleMethod(vehicle, "setForceY", y)
    callVehicleMethod(vehicle, "setLastX", x)
    callVehicleMethod(vehicle, "setLastY", y)
    callVehicleMethod(vehicle, "setLastZ", z)
    callVehicleMethod(vehicle, "setNextX", x)
    callVehicleMethod(vehicle, "setNextY", y)
    callVehicleMethod(vehicle, "setScriptnx", x)
    callVehicleMethod(vehicle, "setScriptny", y)
    callVehicleMethod(vehicle, "setDebugZ", 0)

    local square = getCell():getGridSquare(math.floor(x), math.floor(y), math.floor(z))
    if square then
        callVehicleMethod(vehicle, "setSquare", square)
        callVehicleMethod(vehicle, "setCurrent", square)
        callVehicleMethod(vehicle, "setCurrentSquare", square)
        callVehicleMethod(vehicle, "setMovingSquare", square)
        callVehicleMethod(vehicle, "setMovingSquareNow")
    end

    callVehicleMethod(vehicle, "setPhysicsActive", activePhysics == true)
    callVehicleMethod(vehicle, "transmitModData")
    callVehicleMethod(vehicle, "transmit")
end

local function moveVehicleBodyAway(vehicle, hauler, slot)
    if not vehicle then
        return
    end

    local x, y, z = getParkedVehiclePosition(hauler, slot)
    setVehicleWorldPosition(vehicle, x, y, z, false)
end

local function moveVehicleToWorld(vehicle, x, y, z)
    setVehicleWorldPosition(vehicle, x, y, z, false)
    if vehicle and vehicle.setPhysicsActive then
        pcall(function()
            vehicle:setPhysicsActive(true)
        end)
    end
    callVehicleMethod(vehicle, "transmitModData")
    callVehicleMethod(vehicle, "transmit")
end

removeLoadedOriginalVehicles = function(hauler)
    if not hauler or not hauler.getId or not getCell then
        return
    end

    local cell = getCell()
    if not cell or not cell.getVehicles then
        return
    end

    local okVehicles, vehicles = pcall(function()
        return cell:getVehicles()
    end)
    if not okVehicles or not vehicles or not vehicles.size or not vehicles.get then
        return
    end

    local haulerId = tonumber(hauler:getId())
    for index = vehicles:size() - 1, 0, -1 do
        local okVehicle, vehicle = pcall(function()
            return vehicles:get(index)
        end)
        if okVehicle and vehicle and vehicle ~= hauler and vehicle.getModData then
            local modData = vehicle:getModData()
            if modData
                    and not modData.SurvivalsHaulerDisplayOnly
                    and tonumber(modData.SurvivalsHaulerLoadedOn) == haulerId then
                moveVehicleBodyAway(vehicle, hauler, modData.SurvivalsHaulerLoadedSlot)
                removeVehicleCompletely(vehicle)
            end
        end
    end
end

removeCargoDisplay = function(entry)
    if not entry or not entry.displayVehicleId then
        return
    end

    local displayVehicle = getVehicleByIdSafe(entry.displayVehicleId)
    if displayVehicle then
        moveVehicleBodyAway(displayVehicle, nil, entry.slot)
        removeVehicleCompletely(displayVehicle)
    end

    entry.displayVehicleId = nil
end

local function removeStrayCargoDisplays(hauler, slot)
    if not hauler or not hauler.getId or not getCell then
        return
    end

    local cell = getCell()
    if not cell or not cell.getVehicles then
        return
    end

    local okVehicles, vehicles = pcall(function()
        return cell:getVehicles()
    end)
    if not okVehicles or not vehicles or not vehicles.size or not vehicles.get then
        return
    end

    local haulerId = tonumber(hauler:getId())
    local targetSlot = tonumber(slot)

    for index = vehicles:size() - 1, 0, -1 do
        local okVehicle, vehicle = pcall(function()
            return vehicles:get(index)
        end)
        if okVehicle and vehicle and vehicle ~= hauler and vehicle.getModData then
            local modData = vehicle:getModData()
            if modData
                    and modData.SurvivalsHaulerDisplayOnly
                    and tonumber(modData.SurvivalsHaulerDisplayFor) == haulerId
                    and (not targetSlot or tonumber(modData.SurvivalsHaulerDisplaySlot) == targetSlot) then
                moveVehicleBodyAway(vehicle, hauler, modData.SurvivalsHaulerDisplaySlot)
                removeVehicleCompletely(vehicle)
            end
        end
    end
end

removeUnusedCargoDisplays = function(hauler)
    if not hauler or not hauler.getId or not getCell then
        return
    end

    local keepById = {}
    local keepBySlot = {}
    for _, entry in pairs(getLoadedVehicles(hauler)) do
        if entry and entry.slot then
            local slot = tonumber(entry.slot)
            keepBySlot[slot] = true
            if entry.displayVehicleId then
                keepById[tonumber(entry.displayVehicleId)] = true
            end
        end
    end

    local cell = getCell()
    if not cell or not cell.getVehicles then
        return
    end

    local okVehicles, vehicles = pcall(function()
        return cell:getVehicles()
    end)
    if not okVehicles or not vehicles or not vehicles.size or not vehicles.get then
        return
    end

    local haulerId = tonumber(hauler:getId())
    for index = vehicles:size() - 1, 0, -1 do
        local okVehicle, vehicle = pcall(function()
            return vehicles:get(index)
        end)
        if okVehicle and vehicle and vehicle ~= hauler and vehicle.getModData then
            local modData = vehicle:getModData()
            if modData
                    and modData.SurvivalsHaulerDisplayOnly
                    and tonumber(modData.SurvivalsHaulerDisplayFor) == haulerId then
                local vehicleId = vehicle.getId and tonumber(vehicle:getId()) or nil
                local slot = tonumber(modData.SurvivalsHaulerDisplaySlot)
                if not keepById[vehicleId] or not keepBySlot[slot] then
                    moveVehicleBodyAway(vehicle, hauler, slot)
                    removeVehicleCompletely(vehicle)
                end
            end
        end
    end
end

removeAllCargoDisplayCopies = function()
    if ENABLE_DISPLAY_VEHICLE_COPIES or not getCell then
        return
    end

    local cell = getCell()
    if not cell or not cell.getVehicles then
        return
    end

    local okVehicles, vehicles = pcall(function()
        return cell:getVehicles()
    end)
    if not okVehicles or not vehicles or not vehicles.size or not vehicles.get then
        return
    end

    for index = vehicles:size() - 1, 0, -1 do
        local okVehicle, vehicle = pcall(function()
            return vehicles:get(index)
        end)
        if okVehicle and vehicle and vehicle.getModData then
            local modData = vehicle:getModData()
            if modData and modData.SurvivalsHaulerDisplayOnly then
                moveVehicleBodyAway(vehicle, nil, modData.SurvivalsHaulerDisplaySlot)
                removeVehicleCompletely(vehicle)
            end
        end
    end
end

local function getVehicleYawAngle(vehicle)
    return getVehicleYawRadians(vehicle)
end

local function copyVehicleAngles(targetVehicle, sourceVehicle)
    if not targetVehicle or not sourceVehicle or not targetVehicle.setAngles then
        return
    end

    if sourceVehicle.getAngleX and sourceVehicle.getAngleY and sourceVehicle.getAngleZ then
        pcall(function()
            targetVehicle:setAngles(sourceVehicle:getAngleX(), sourceVehicle:getAngleY(), sourceVehicle:getAngleZ())
        end)
    else
        local yaw = getVehicleYawAngle(sourceVehicle)
        pcall(function()
            targetVehicle:setAngles(0, yaw, 0)
        end)
    end
end

local function getDisplayVehiclePosition(hauler, slot)
    local offset = DISPLAY_SLOT_OFFSETS[tonumber(slot)] or DISPLAY_SLOT_OFFSETS[1]
    local angle = getVehicleYawAngle(hauler)

    local sin = math.sin(angle)
    local cos = math.cos(angle)
    local x = hauler:getX() + offset.x * cos - offset.y * sin
    local y = hauler:getY() + offset.x * sin + offset.y * cos

    return x, y, hauler:getZ()
end

local function freezeDisplayVehicle(displayVehicle)
    if not displayVehicle then
        return
    end

    if displayVehicle.setPhysicsActive then
        pcall(function()
            displayVehicle:setPhysicsActive(false)
        end)
    end
    if displayVehicle.setActiveInBullet then
        pcall(function()
            displayVehicle:setActiveInBullet(false)
        end)
    end
    if displayVehicle.setCollidable then
        pcall(function()
            displayVehicle:setCollidable(false)
        end)
    end
    if displayVehicle.setSolid then
        pcall(function()
            displayVehicle:setSolid(false)
        end)
    end
    if displayVehicle.setShootable then
        pcall(function()
            displayVehicle:setShootable(false)
        end)
    end
    if displayVehicle.setNoPicking then
        pcall(function()
            displayVehicle:setNoPicking(true)
        end)
    end
    if displayVehicle.setMass then
        pcall(function()
            displayVehicle:setMass(0.001)
        end)
    end
    if displayVehicle.setInitialMass then
        pcall(function()
            displayVehicle:setInitialMass(0.001)
        end)
    end
end

local function pinDisplayVehiclePosition(displayVehicle, x, y, z)
    if not displayVehicle then
        return
    end

    callVehicleMethod(displayVehicle, "setX", x)
    callVehicleMethod(displayVehicle, "setY", y)
    callVehicleMethod(displayVehicle, "setZ", z)
    callVehicleMethod(displayVehicle, "setForceX", x)
    callVehicleMethod(displayVehicle, "setForceY", y)
    callVehicleMethod(displayVehicle, "setLastX", x)
    callVehicleMethod(displayVehicle, "setLastY", y)
    callVehicleMethod(displayVehicle, "setLastZ", z)
    callVehicleMethod(displayVehicle, "setNextX", x)
    callVehicleMethod(displayVehicle, "setNextY", y)
    callVehicleMethod(displayVehicle, "setScriptnx", x)
    callVehicleMethod(displayVehicle, "setScriptny", y)

    local square = getCell():getGridSquare(math.floor(x), math.floor(y), math.floor(z))
    if square then
        callVehicleMethod(displayVehicle, "setSquare", square)
        callVehicleMethod(displayVehicle, "setCurrent", square)
        callVehicleMethod(displayVehicle, "setCurrentSquare", square)
        callVehicleMethod(displayVehicle, "setMovingSquare", square)
        callVehicleMethod(displayVehicle, "setMovingSquareNow")
    end
end

function snapDisplayVehicleToHauler(hauler, entry)
    if not hauler or not entry or not entry.displayVehicleId then
        return
    end

    local displayVehicle = getVehicleByIdSafe(entry.displayVehicleId)
    if not displayVehicle then
        entry.displayVehicleId = nil
        return
    end

    local x, y, z = getDisplayVehiclePosition(hauler, entry.slot)
    pinDisplayVehiclePosition(displayVehicle, x, y, z)
    copyVehicleAngles(displayVehicle, hauler)
    freezeDisplayVehicle(displayVehicle)
    pcall(function() displayVehicle:transmitModData() end)
end

function createCargoDisplay(hauler, entry, player)
    if not shouldShowCargoDisplays(hauler) then
        return
    end

    if not hauler or not entry or not entry.script or not entry.slot then
        return
    end

    removeCargoDisplay(entry)
    removeStrayCargoDisplays(hauler, entry.slot)

    local x, y, z = getDisplayVehiclePosition(hauler, entry.slot)
    local ok, displayVehicle = pcall(addVehicle, entry.script, x, y, z)
    if not ok or not displayVehicle then
        print(string.format("[SurvivalsHauler] display failed for slot %s: %s", tostring(entry.slot), tostring(displayVehicle)))
        return
    end

    applyStoredAppearance(displayVehicle, entry)

    local displayModData = displayVehicle:getModData()
    displayModData.SurvivalsHaulerDisplayFor = hauler:getId()
    displayModData.SurvivalsHaulerDisplaySlot = entry.slot
    displayModData.SurvivalsHaulerDisplayOnly = true
    displayVehicle:transmitModData()

    entry.displayVehicleId = displayVehicle:getId()
    snapDisplayVehicleToHauler(hauler, entry)
    print(string.format("[SurvivalsHauler] display copy placed %s in slot %s", tostring(entry.script), tostring(entry.slot)))
end

local function spawnStoredVehicle(entry, player, hauler)
    if not entry or not entry.script then
        return nil, "missing stored script"
    end

    local x, y, z = getStoredVehicleSpawnPoint(entry, player, hauler)
    if not x then
        return nil, "no clear unload space nearby"
    end

    local square = getCell():getGridSquare(math.floor(x), math.floor(y), math.floor(z))
    if not square then
        return nil, "unload square is not loaded"
    end

    local scriptCandidates = {}
    local seenScripts = {}
    local function addScriptCandidate(scriptName)
        if scriptName and scriptName ~= "" and not seenScripts[scriptName] then
            table.insert(scriptCandidates, scriptName)
            seenScripts[scriptName] = true
        end
    end

    addScriptCandidate(entry.script)
    addScriptCandidate(entry.scriptShort)
    if entry.script then
        addScriptCandidate(tostring(entry.script):gsub("^Base%.", ""))
    end
    if entry.scriptShort then
        addScriptCandidate("Base." .. tostring(entry.scriptShort):gsub("^Base%.", ""))
    end

    local attemptedScripts = {}
    local lastError = nil
    for _, scriptName in ipairs(scriptCandidates) do
        table.insert(attemptedScripts, scriptName)
        if not vehicleScriptExists(scriptName) then
            print(string.format("[SurvivalsHauler] addVehicle skipped missing vehicle script %s", tostring(scriptName)))
        else
            local spawnedVehicle = nil
            if addVehicleDebug then
                local dir = IsoDirections and IsoDirections.S or nil
                local skinIndex = entry.skinIndex and tonumber(entry.skinIndex) or nil
                local ok, result = pcall(addVehicleDebug, scriptName, dir, skinIndex, square)
                if ok and result then
                    spawnedVehicle = result
                    print(string.format("[SurvivalsHauler] addVehicleDebug succeeded with %s", tostring(scriptName)))
                elseif not ok then
                    lastError = tostring(result)
                end
            end

            if not spawnedVehicle and addVehicle then
                local ok, result = pcall(addVehicle, scriptName)
                if ok and result then
                    spawnedVehicle = result
                    print(string.format("[SurvivalsHauler] addVehicle fallback succeeded with %s", tostring(scriptName)))
                elseif not ok then
                    lastError = tostring(result)
                end
            end

            if spawnedVehicle then
                moveVehicleToWorld(spawnedVehicle, x, y, z)
                applyStoredAppearance(spawnedVehicle, entry)
                restoreVehicleIdentity(spawnedVehicle, entry)
                stripFreshSpawnLoot(spawnedVehicle)
                local restoredStateData = entry.vehicleStateData
                local restoredState = entry.vehicleState or deserializeVehicleState(restoredStateData)
                restoreVehicleState(spawnedVehicle, restoredState, true, true)
                if not restoredStateData or restoredStateData == "" then
                    restoredStateData = serializeVehicleState(restoredState)
                end
                queueVehicleStateRestore(spawnedVehicle, restoredStateData, entry)
                copyVehicleAngles(spawnedVehicle, hauler)
                return spawnedVehicle, nil, x, y, z
            end
        end
    end

    return nil, "vehicle spawn returned nil for " .. table.concat(attemptedScripts, ", ") .. (lastError and ("; last error: " .. lastError) or "")
end

local function getVehicleLocalOffset(baseVehicle, targetVehicle)
    local dx = targetVehicle:getX() - baseVehicle:getX()
    local dy = targetVehicle:getY() - baseVehicle:getY()
    local angle = getVehicleYawAngle(baseVehicle)
    local sin = math.sin(angle)
    local cos = math.cos(angle)

    return dx * cos + dy * sin, -dx * sin + dy * cos
end

local function isLikelyTowVehicleAtHitch(hauler, vehicle)
    local localX, localY = getVehicleLocalOffset(hauler, vehicle)

    return math.abs(localX) <= 3.5 and localY >= 7.0 and localY <= 13.0
end

local function loadVehicle(player, args)
    local hauler = getVehicleByIdSafe(args.haulerId)
    local cargoVehicle = getVehicleByIdSafe(args.vehicleId)
    local slot = tonumber(args.slot)

    if not isHauler(hauler) or not cargoVehicle or cargoVehicle == hauler or not slot or slot < 1 or slot > MAX_LOAD_SLOTS then
        print("[SurvivalsHauler] load rejected: invalid hauler, vehicle, or slot")
        return
    end

    if not isSlotFree(hauler, slot) then
        print("[SurvivalsHauler] load rejected: slot occupied")
        return
    end

    if cargoVehicle:getVehicleTowedBy() or cargoVehicle:getVehicleTowing() then
        print("[SurvivalsHauler] load rejected: cargo vehicle is attached to something")
        return
    end

    if hauler:getVehicleTowedBy() == cargoVehicle or hauler:getVehicleTowing() == cargoVehicle then
        print("[SurvivalsHauler] load rejected: cargo vehicle is attached to this hauler")
        return
    end

    if isLikelyTowVehicleAtHitch(hauler, cargoVehicle) then
        print("[SurvivalsHauler] load rejected: cargo vehicle is positioned like the tow vehicle")
        return
    end

    local cargoModData = cargoVehicle:getModData()
    if cargoModData.SurvivalsHaulerLoadedOn then
        print("[SurvivalsHauler] load rejected: cargo vehicle already marked loaded")
        return
    end

    local dx = cargoVehicle:getX() - hauler:getX()
    local dy = cargoVehicle:getY() - hauler:getY()
    if dx * dx + dy * dy > 400 then
        print("[SurvivalsHauler] load rejected: cargo vehicle too far away")
        return
    end

    local cargoScriptName = tostring(cargoVehicle:getScriptName())
    local vehicleState = snapshotVehicleState(cargoVehicle)
    local entry = {
        slot = slot,
        vehicleId = cargoVehicle:getId(),
        script = getVehicleScriptFullName(cargoVehicle),
        scriptShort = getVehicleScriptShortName(cargoVehicle),
        name = getVehicleDisplayName(cargoVehicle),
        skinIndex = cargoVehicle:getSkinIndex(),
        h = cargoVehicle:getColorHue(),
        s = cargoVehicle:getColorSaturation(),
        v = cargoVehicle:getColorValue(),
        rust = cargoVehicle.getRust and cargoVehicle:getRust() or nil,
        keyId = getObjectMethodValue(cargoVehicle, "getKeyId"),
        haveKey = getObjectBoolValue(cargoVehicle, "getHaveKey"),
        proxyType = getCargoProxyType(cargoVehicle:getScriptName()),
        x = cargoVehicle:getX(),
        y = cargoVehicle:getY(),
        z = cargoVehicle:getZ(),
        worldRemoved = true,
        worldDetached = false,
        vehicleStateData = serializeVehicleState(vehicleState),
    }

    local loaded = getLoadedVehicles(hauler)
    table.insert(loaded, entry)
    getRecoveryVehicles(hauler)[tostring(slot)] = entry
    activeUnloadLocks[getLoadedOriginalKey(hauler:getId(), slot)] = nil

    hauler:transmitModData()
    moveVehicleBodyAway(cargoVehicle, hauler, slot)
    removeVehicleCompletely(cargoVehicle)
    registerDisplayHauler(hauler)
    setCargoProxyVisible(hauler, slot, true, entry.proxyType)
    createCargoDisplay(hauler, entry, player)
    syncCargoProxyParts(hauler)
    syncHaulerBodyVisual(hauler)
    syncCargoDisplays(hauler, player)
    hauler:transmitModData()
    print(string.format("[SurvivalsHauler] loaded and removed original %s in slot %s; saved parts=%s damaged=%s containers=%s items=%s stateBytes=%s",
        cargoScriptName,
        tostring(slot),
        tostring(vehicleState.partCount),
        tostring(vehicleState.damagedCount),
        tostring(vehicleState.containerCount),
        tostring(vehicleState.itemCount),
        tostring(string.len(entry.vehicleStateData or ""))))
end

local function unloadSlot(player, args)
    local hauler = getVehicleByIdSafe(args.haulerId)
    local slot = tonumber(args.slot)

    print(string.format("[SurvivalsHauler] server unload request slot %s from %s", tostring(slot), tostring(args.haulerId)))

    if not isHauler(hauler) or not slot then
        print("[SurvivalsHauler] unload rejected: invalid hauler or slot")
        return
    end

    sanitizeHaulerStoredVehicleState(hauler)

    local unloadKey = getLoadedOriginalKey(hauler:getId(), slot)
    if activeUnloadLocks[unloadKey] then
        print(string.format("[SurvivalsHauler] unload rejected: slot %s was already unloaded", tostring(slot)))
        hauler:transmitModData()
        return
    end

    local entry = findStoredVehicleEntry(hauler, slot)

    if not entry then
        print(string.format("[SurvivalsHauler] unload rejected: no stored vehicle in slot %s", tostring(slot)))
        return
    end

    activeUnloadLocks[unloadKey] = true

    if entry.worldRemoved ~= true and entry.worldDetached ~= true and entry.vehicleId then
        local originalKey = unloadKey
        local cargoVehicle = activeLoadedOriginals[originalKey] or getVehicleByIdSafe(entry.vehicleId)
        if cargoVehicle then
            local cargoModData = cargoVehicle:getModData()
            if tonumber(cargoModData.SurvivalsHaulerLoadedOn) == hauler:getId()
                    and tonumber(cargoModData.SurvivalsHaulerLoadedSlot) == slot then
                local x, y, z = getStoredVehicleSpawnPoint(entry, player, hauler)
                if not x then
                    print(string.format("[SurvivalsHauler] unload failed for slot %s: no clear unload space nearby", tostring(slot)))
                    activeUnloadLocks[unloadKey] = nil
                    hauler:transmitModData()
                    return
                end

                cargoModData.SurvivalsHaulerLoadedOn = nil
                cargoModData.SurvivalsHaulerLoadedSlot = nil
                moveVehicleToWorld(cargoVehicle, x, y, z)
                copyVehicleAngles(cargoVehicle, hauler)
                activeLoadedOriginals[originalKey] = nil
                cargoVehicle:transmitModData()
                removeStoredVehicleSlot(hauler, slot)
                setCargoProxyVisible(hauler, slot, false)
                removeCargoDisplay(entry)
                removeStrayCargoDisplays(hauler, slot)
                registerDisplayHauler(hauler)
                syncHaulerBodyVisual(hauler)
                hauler:transmitModData()
                print(string.format("[SurvivalsHauler] restored %s from slot %s at %.2f,%.2f,%.2f", tostring(entry.script), tostring(slot), x, y, z))
                return
            end
        end

        print(string.format("[SurvivalsHauler] unload failed for slot %s: preserved original vehicle is unavailable", tostring(slot)))
        activeUnloadLocks[unloadKey] = nil
        hauler:transmitModData()
        return
    end

    removeStoredVehicleSlot(hauler, slot)
    setCargoProxyVisible(hauler, slot, false)
    removeCargoDisplay(entry)
    removeStrayCargoDisplays(hauler, slot)
    syncHaulerBodyVisual(hauler)
    hauler:transmitModData()

    local spawnedVehicle, errorMessage, x, y, z = spawnStoredVehicle(entry, player, hauler)
    if not spawnedVehicle then
        print(string.format("[SurvivalsHauler] unload failed for slot %s: %s", tostring(slot), tostring(errorMessage)))
        addStoredVehicleEntry(hauler, entry)
        setCargoProxyVisible(hauler, slot, true, entry.proxyType)
        syncHaulerBodyVisual(hauler)
        activeUnloadLocks[unloadKey] = nil
        hauler:transmitModData()
        return
    end

    local originalKey = unloadKey
    local detachedOriginal = activeLoadedOriginals[originalKey]
    if detachedOriginal and detachedOriginal ~= spawnedVehicle then
        removeVehicleCompletely(detachedOriginal)
        activeLoadedOriginals[originalKey] = nil
    end

    registerDisplayHauler(hauler)
    syncHaulerBodyVisual(hauler)
    hauler:transmitModData()
    print(string.format("[SurvivalsHauler] spawned %s from slot %s at %.2f,%.2f,%.2f", tostring(entry.script), tostring(slot), x, y, z))
end

local function onClientCommand(module, command, player, args)
    if module ~= "SurvivalsHauler" then
        return
    end

    args = args or {}

    if command == "loadVehicle" then
        local ok, errorMessage = pcall(loadVehicle, player, args)
        if not ok then
            print(string.format("[SurvivalsHauler] load command error: %s", tostring(errorMessage)))
        end
    elseif command == "unloadSlot" then
        local ok, errorMessage = pcall(unloadSlot, player, args)
        if not ok then
            print(string.format("[SurvivalsHauler] unload command error: %s", tostring(errorMessage)))
        end
    elseif command == "syncCargoProxies" then
        local hauler = getVehicleByIdSafe(args.haulerId)
        local ok, errorMessage = pcall(function()
            syncCargoProxyParts(hauler)
            syncHaulerBodyVisual(hauler)
            syncCargoDisplays(hauler, player)
        end)
        if not ok then
            print(string.format("[SurvivalsHauler] sync cargo command error: %s", tostring(errorMessage)))
        end
    end
end

Events.OnClientCommand.Add(onClientCommand)

local function onTick()
    processPendingVehicleStateRestores()

    displaySnapTick = displaySnapTick + 1
    if displaySnapTick < DISPLAY_SNAP_INTERVAL_TICKS then
        return
    end
    displaySnapTick = 0

    for key, vehicleId in pairs(activeDisplayHaulers) do
        local vehicle = getVehicleByIdSafe(vehicleId)
        if vehicle and isHauler(vehicle) then
            syncCargoProxyParts(vehicle)
            syncHaulerBodyVisual(vehicle)
            syncCargoDisplays(vehicle, nil)
        else
            activeDisplayHaulers[key] = nil
        end
    end

    syncHaulerBodyVisualsInCell()

    if removeAllCargoDisplayCopies then
        removeAllCargoDisplayCopies()
    end
end

Events.OnTick.Add(onTick)
