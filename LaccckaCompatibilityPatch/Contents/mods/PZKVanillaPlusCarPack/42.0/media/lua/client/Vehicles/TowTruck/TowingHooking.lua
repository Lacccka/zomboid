if not getActivatedMods():contains("STowTruck")  and not getActivatedMods():contains("TowingCar")  then
if not TowTruckMod then TowTruckMod = {} end
if not TowTruckMod.Hook then TowTruckMod.Hook = {} end

---------------------------------------------------------------------------
------ TowTruck functions -------------------------------------------------
--- Attach vehicle to TowTruck --------------------------------------------
---------------------------------------------------------------------------

-- ISBaseTimedAction

function TowTruckMod.Hook.attachTowTruckAction(playerObj, towingVehicle, towedVehicle, towingPoint, towedPoint)
    if playerObj == nil or towingVehicle == nil or towedVehicle == nil then return end
	
    -- check vehicle available
    if #(TowTruckMod.Utils.getHookTypeVariants(towingVehicle, towedVehicle)) == 0 then return end	
	
    --- Unequip item
    local storePrim = playerObj:getPrimaryHandItem()
    if storePrim ~= nil then
       ISTimedActionQueue.add(ISUnequipAction:new(playerObj, storePrim, 12));
    end

	-- Are we driving? If so skip the walking around and just hook the vehicle
	local playerDriving = towingVehicle:isDriver(playerObj)
	local hookTime = 250
	
	if not playerDriving then
		local hookPos = towingVehicle:getAttachmentWorldPos("hook", Vector3f.new())
		ISTimedActionQueue.add(ISPathFindAction:pathToLocationF(playerObj, hookPos:x(), hookPos:y(), hookPos:z()))		
		hookTime = 150
	end	
	
    -- Schedule attach vehicle
	playerObj:setVariable("bSettingHook", "true")
    ISTimedActionQueue.add(TAHookAttachVehicle:new(playerObj, hookTime, TowTruckMod.Config.lowLevelAnimation, TowTruckMod.Hook.performAttachTowTruck, towingVehicle, towedVehicle, "hook", towedPoint))
	ISTimedActionQueue.add(TAHookSchedule:new(playerObj, 25, TowTruckMod.Hook.restoreScript, towedVehicle))
end

function TowTruckMod.Hook.performAttachTowTruck(playerObj, towingVehicle, towedVehicle, attachmentA, attachmentB)
    if #(TowTruckMod.Utils.getHookTypeVariants(towingVehicle, towedVehicle)) == 0 then return end

	local modData = towingVehicle:getModData()
	modData.towHeight = 0
	towingVehicle:transmitModData()
	
	TowTruckMod.Hook.setDefaultHookOffset(towingVehicle)
	
	-- Check alarm
	local alarmSet = towedVehicle:isAlarmed()	
	if alarmSet == true then
		towedVehicle:triggerAlarm()
	end
	
	-- Make it so we can tow it with the hook
	TowTruckMod.Hook.setTowingScript(towedVehicle)
	
	-- Hoist the vehicle up
    local args = { vehicleA = towingVehicle:getId(), vehicleB = towedVehicle:getId(), attachmentA = attachmentA, attachmentB = attachmentB }
	sendClientCommand(playerObj, 'towing', 'attachTowTruck', args)
	
	playerObj:clearVariable("bSettingHook")
	local mass = towingVehicle:getMass()
	if mass > 1000 then
		towingVehicle:setMass(mass / 1.3)
	end
end

---------------------------------------
-- Detach vehicle from TowTruck
---------------------------------------

function TowTruckMod.Hook.detachTowTruckAction(playerObj, vehicle)
    local towedVehicle = vehicle:getVehicleTowing()
	if towedVehicle == nil then
		return
	end
	
	local playerDriving = vehicle:isDriver(playerObj)
	-- Are we driving? If so just skip the walking around and just hook the vehicle
	if not playerDriving then
		--- Go to attachment point of towed vehicle
		local hookPos = vehicle:getAttachmentWorldPos("hook", Vector3f.new())
		ISTimedActionQueue.add(ISPathFindAction:pathToLocationF(playerObj, hookPos:x(), hookPos:y(), hookPos:z()))
		
		-- Unequip item
		local storePrim = playerObj:getPrimaryHandItem()
		if storePrim ~= nil then
			ISTimedActionQueue.add(ISUnequipAction:new(playerObj, storePrim, 12));
		end			
	end
	
	local attachmentA = vehicle:getTowAttachmentSelf()
	local attachmentB = vehicle:getTowAttachmentOther()	
	local args = { vehicle = vehicle:getId() }
	args = { vehicleA = vehicle:getId(), vehicleB = towedVehicle:getId(), attachmentA = attachmentA, attachmentB = attachmentB }
	sendClientCommand(playerObj, 'vehicle', 'attachTrailer', args)
	
	-- Schedule the detach
    ISTimedActionQueue.add(TAHookDetachVehicle:new(playerObj, 50, TowTruckMod.Config.highLevelAnimation, TowTruckMod.Hook.performDetachTowTruck, vehicle, towedVehicle))
end

function TowTruckMod.Hook.performDetachTowTruck(playerObj, towingVehicle, towedVehicle)
	local args = { vehicle = towedVehicle:getId() }
	sendClientCommand(playerObj, 'towing', 'detachTowTruck', args)
	
	local script = towingVehicle:getScript()
	local mass = script:getMass()
	towingVehicle:setMass(mass)
	
	TowTruckMod.Hook.resetHook(towingVehicle)
	TowTruckMod.Hook.restoreScript(playerObj, towedVehicle)
end

-------------------------------------------------------------------------------------
----- Utility Functions -----
-------------------------------------------------------------------------------------

function TowTruckMod.Hook.increaseTowHeight(playerObj, towingVehicle, towedVehicle, offsetAmount)
	local script = towingVehicle:getScript() 
	local attachmentA = towingVehicle:getTowAttachmentSelf()
	local attachmentB = towingVehicle:getTowAttachmentOther()
	local hook = script:getAttachmentById(attachmentA)
	local hookPos = hook:getOffset()
	hook:getOffset():setComponent(1, hookPos:y() + offsetAmount)
	
	local modData = towingVehicle:getModData()		
	modData.towHeight = modData.towHeight + 1
	towingVehicle:transmitModData()
	
	TowTruckMod.Hook.setTowingScript(towedVehicle)
	
	local args = { vehicle = towingVehicle:getId() }
	args = { vehicleA = towingVehicle:getId(), vehicleB = towedVehicle:getId(), attachmentA = attachmentA, attachmentB = attachmentB }
	sendClientCommand(playerObj, 'vehicle', 'attachTrailer', args)
	
	ISTimedActionQueue.add(TAHookSchedule:new(playerObj, 10, TowTruckMod.Hook.restoreScript, towedVehicle))
end

function TowTruckMod.Hook.decreaseTowHeight(playerObj, towingVehicle, towedVehicle)
	local script = towingVehicle:getScript()
	local attachmentA = towingVehicle:getTowAttachmentSelf()
	local attachmentB = towingVehicle:getTowAttachmentOther()
	local hook = script:getAttachmentById(attachmentA)
	local hookPos = hook:getOffset()
	local inc = 0.25
	hook:getOffset():setComponent(1, hookPos:y() - inc)
	
	local modData = towingVehicle:getModData()
	modData.towHeight = modData.towHeight - 1
	towingVehicle:transmitModData()
	
	TowTruckMod.Hook.setTowingScript(towedVehicle)
	
	local args = { vehicle = towingVehicle:getId() }
	args = { vehicleA = towingVehicle:getId(), vehicleB = towedVehicle:getId(), attachmentA = attachmentA, attachmentB = attachmentB }
	sendClientCommand(playerObj, 'vehicle', 'attachTrailer', args)
	
	ISTimedActionQueue.add(TAHookSchedule:new(playerObj, 10, TowTruckMod.Hook.restoreScript, towedVehicle)) 
end

function TowTruckMod.Hook.setDefaultHookOffset(towTruck)
	if not towTruck then return end
	
	local modData = towTruck:getModData()
	-- Set this only once to establish the modData
	if modData.hookOffset == nil then
		local script = towTruck:getScript()
		local hookAttachment = script:getAttachmentById("hook")
		local offSet = hookAttachment:getOffset()
		
		modData.hookOffsetY = offSet:get(1)
		towTruck:transmitModData()
	end
end

function TowTruckMod.Hook.resetHook(towTruck)
	if not towTruck then return end
	
	local modData = towTruck:getModData()	
	local script = towTruck:getScript()
	local hookAttachment = script:getAttachmentById("hook")	

	local hookOffsetY = modData.hookOffsetY or 0
	
	hookAttachment:getOffset():setComponent(1, hookOffsetY)
end

function TowTruckMod.Hook.setTowingScript(towedVehicle)
	if not towedVehicle then
		print('no such vehicle (B) id='..tostring(towedVehicle))
		return
	end
	
	local modData = towedVehicle:getModData()
	modData.originalScript = towedVehicle:getScriptName()
	modData.towScript = modData.originalScript .. "_Trailer"
	modData.originalMass = towedVehicle:getMass()
	towedVehicle:setScriptName(modData.towScript)
    towedVehicle:transmitModData()
end

function TowTruckMod.Hook.restoreScript(playerObj, towedVehicle)
	if not towedVehicle then
		print('no such vehicle (B) id='..tostring(towedVehicle))
		return
	end
	
	local modData = towedVehicle:getModData()
	if modData.originalScript ~= nil then
		towedVehicle:setScriptName(modData.originalScript)
	end
	modData.towScript = ""
	towedVehicle:transmitModData()
end

function TowTruckMod.Hook.flipVehicleAction(playerObj, towingVehicle, towedVehicle, towingPoint, towedPoint)
    if playerObj == nil or towingVehicle == nil or towedVehicle == nil then return end

    -- Attach vehicle
    ISTimedActionQueue.add(TAHookAttachVehicle:new(playerObj, 300, TowTruckMod.Config.lowLevelAnimation, TowTruckMod.Hook.performFlipVehicle, towingVehicle, towedVehicle, "hook", "flipNode"))
end

function TowTruckMod.Hook.performFlipVehicle(playerObj, towingVehicle, towedVehicle, attachmentA, attachmentB)
    if #(TowTruckMod.Utils.getCenterHook(towingVehicle, towedVehicle)) == 0 then return end
		
	local modData = towingVehicle:getModData()
	modData.towHeight = 0
	towingVehicle:transmitModData()
	
	-- Check alarm
	local alarmSet = towedVehicle:isAlarmed()	
	if alarmSet == true then
		towedVehicle:triggerAlarm()
	end
	
	TowTruckMod.Hook.setDefaultHookOffset(towingVehicle)

	-- Use rope to pull vehicle over *fingers crossed*
    local args = { vehicleA = towingVehicle:getId(), vehicleB = towedVehicle:getId(), attachmentA = attachmentA, attachmentB = attachmentB }
	sendClientCommand(playerObj, 'vehicle', 'attachTrailer', args)
	
	local script = towingVehicle:getScript()
	local mass = script:getMass()
	towingVehicle:setMass(mass)	
end

function TowTruckMod.Hook.fakeFunc()
	return
end
end