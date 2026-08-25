require "ExtractionMode/ModCompatibility"

ExtractionMode = ExtractionMode or {}
local Compatibility = ExtractionMode.ModCompatibility
local installed = false

local function liveWorldObject(object)
    if object == nil or Compatibility.isProtectedHideoutObject(object) then return false end
    local square, index = nil, -1
    local ok = pcall(function()
        square = object:getSquare()
        index = object:getObjectIndex()
    end)
    if not ok or square == nil or tonumber(index) == nil or index < 0 then return false end
    local liveSquare = getCell and getCell():getGridSquare(
        square:getX(), square:getY(), square:getZ()) or nil
    return liveSquare == square
end

local function liveVehicleAction(action)
    if action == nil or action.vehicleID == nil or getVehicleById == nil then return false end
    local vehicle = nil
    pcall(function() vehicle = getVehicleById(tonumber(action.vehicleID)) end)
    if vehicle == nil or action.vehicle ~= vehicle then return false end
    local part = nil
    pcall(function() part = vehicle:getPartById(action.priableObjectID) end)
    return part ~= nil and part == action.priableObject
end

local function pryTargetValid(action)
    if action == nil then return false end
    if action.typeTimeAction == "pryDoorOrWindow" then
        return liveWorldObject(action.priableObject)
    elseif action.typeTimeAction == "pryVehicleDoor" then
        return liveVehicleAction(action)
    end
    return true
end

local function collectTargetValid(action)
    local square = action and action.clickedSquare or nil
    if square == nil then return false end
    if ExtractionMode.Config.isHideoutGarageProtectedSquare(square) then return false end
    local live = getCell and getCell():getGridSquare(
        square:getX(), square:getY(), square:getZ()) or nil
    if live ~= square then return false end
    local objects = square:getObjects()
    for index = 0, objects:size() - 1 do
        local object = objects:get(index)
        local spriteName = ""
        pcall(function() spriteName = object:getSpriteName() or "" end)
        if spriteName == tostring(action.spriteName or "") then
            return not Compatibility.isProtectedHideoutObject(object)
        end
    end
    return false
end

local function finishWithoutEffect(action)
    if ISBaseTimedAction and ISBaseTimedAction.perform then
        ISBaseTimedAction.perform(action)
    end
end

local function install()
    if installed or not Compatibility.isCommonSenseActive() then return end
    if BB_CS_PryTimedAction == nil or BB_CS_CollectTimedAction == nil then return end

    if not BB_CS_PryTimedAction.ExtractionModeOriginalIsValid then
        BB_CS_PryTimedAction.ExtractionModeOriginalIsValid = BB_CS_PryTimedAction.isValid
        function BB_CS_PryTimedAction:isValid()
            return pryTargetValid(self) and self:ExtractionModeOriginalIsValid()
        end
        BB_CS_PryTimedAction.ExtractionModeOriginalWaitToStart = BB_CS_PryTimedAction.waitToStart
        function BB_CS_PryTimedAction:waitToStart()
            if not pryTargetValid(self) then return false end
            return self:ExtractionModeOriginalWaitToStart()
        end
        BB_CS_PryTimedAction.ExtractionModeOriginalStart = BB_CS_PryTimedAction.start
        function BB_CS_PryTimedAction:start()
            if not pryTargetValid(self) then return end
            return self:ExtractionModeOriginalStart()
        end
        BB_CS_PryTimedAction.ExtractionModeOriginalStop = BB_CS_PryTimedAction.stop
        function BB_CS_PryTimedAction:stop()
            if not pryTargetValid(self) then
                if ISBaseTimedAction and ISBaseTimedAction.stop then ISBaseTimedAction.stop(self) end
                return
            end
            return self:ExtractionModeOriginalStop()
        end
        BB_CS_PryTimedAction.ExtractionModeOriginalPerform = BB_CS_PryTimedAction.perform
        function BB_CS_PryTimedAction:perform()
            if not pryTargetValid(self) then finishWithoutEffect(self); return end
            return self:ExtractionModeOriginalPerform()
        end
    end

    if not BB_CS_CollectTimedAction.ExtractionModeOriginalIsValid then
        BB_CS_CollectTimedAction.ExtractionModeOriginalIsValid = BB_CS_CollectTimedAction.isValid
        function BB_CS_CollectTimedAction:isValid()
            return collectTargetValid(self) and self:ExtractionModeOriginalIsValid()
        end
        BB_CS_CollectTimedAction.ExtractionModeOriginalPerform = BB_CS_CollectTimedAction.perform
        function BB_CS_CollectTimedAction:perform()
            if not collectTargetValid(self) then finishWithoutEffect(self); return end
            return self:ExtractionModeOriginalPerform()
        end
    end

    if BB_CS_PryUtils and BB_CS_PryUtils.PryDoorOrWindowOpen
        and not BB_CS_PryUtils.ExtractionModeOriginalPryDoorOrWindowOpen then
        BB_CS_PryUtils.ExtractionModeOriginalPryDoorOrWindowOpen =
            BB_CS_PryUtils.PryDoorOrWindowOpen
        BB_CS_PryUtils.PryDoorOrWindowOpen = function(worldObjects, object, player, tool)
            if Compatibility.isProtectedHideoutObject(object) then return end
            return BB_CS_PryUtils.ExtractionModeOriginalPryDoorOrWindowOpen(
                worldObjects, object, player, tool)
        end
    end

    installed = true
end

install()
Events.OnGameStart.Add(install)
Events.OnFillWorldObjectContextMenu.Add(install)

ExtractionMode.CommonSenseCompatibility = { install = install }
return ExtractionMode.CommonSenseCompatibility
