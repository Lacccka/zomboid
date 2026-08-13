if not getActivatedMods():contains("STowTruck")  then
require('TimedActions/ISBaseTimedAction')

TAHookAttachVehicle = ISBaseTimedAction:derive("TAHookAttachVehicle")

-- The condition which tells the timed action if it is still valid
function TAHookAttachVehicle:isValid()
	local settingHook = self.character:getVariable("bSettingHook")
	return self.vehicleA:getVehicleTowing() == nil and self.vehicleA:isStopped() and settingHook ~= "true"
end

-- Starts the Timed Action
function TAHookAttachVehicle:start()	
	self:setActionAnim(self.animation)
	self.sound = getSoundManager():PlayWorldSound("towingCar_hookingSound", false, self.character:getSquare(), 0, 5, 1, true)
end

-- Is called when the time has passed
function TAHookAttachVehicle:perform()
	self.sound:stop()

    if self.performFunc ~= nil then
        self.performFunc(self.character, self.vehicleA, self.vehicleB, self.attachmentA, self.attachmentB)
    end
	
    ISBaseTimedAction.perform(self)
end


function TAHookAttachVehicle:stop()
    if self.sound then
        self.sound:stop()
    end 
	
    ISBaseTimedAction.stop(self)
end

function TAHookAttachVehicle:new(character, time, animation, performFunc, arg1, arg2, arg3, arg4)
    local o = {}
    setmetatable(o, self)
    self.__index = self
	
	o.useProgressBar = true	
	o.stopOnWalk = true
	o.stopOnRun = true

    o.maxTime = time
	o.character = character
	o.animation = animation
	
	o.performFunc = performFunc
	o.vehicleA = arg1
	o.vehicleB = arg2
	o.attachmentA = arg3
	o.attachmentB = arg4
   
    return o
end
end