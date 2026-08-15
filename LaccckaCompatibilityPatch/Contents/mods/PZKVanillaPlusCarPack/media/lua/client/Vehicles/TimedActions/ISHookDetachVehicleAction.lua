if not getActivatedMods():contains("STowTruck")  then
require('TimedActions/ISBaseTimedAction')

TAHookDetachVehicle = ISBaseTimedAction:derive("TAHookDetachVehicle")

-- The condition which tells the timed action if it is still valid
function TAHookDetachVehicle:isValid()
	return true
end

-- Starts the Timed Action
function TAHookDetachVehicle:start()	
	self:setActionAnim(self.animation)
	self.sound = getSoundManager():PlayWorldSound("towingCar_hookingSound", false, self.character:getSquare(), 0, 5, 1, true)
	self.character:SetVariable("bDetachTowHook", "true")
end

-- Is called when the time has passed
function TAHookDetachVehicle:perform()
	self.sound:stop()

    if self.performFunc ~= nil then
        self.performFunc(self.character, self.vehicleA, self.vehicleB, self.attachmentA, self.attachmentB)
    end
	
	self.character:ClearVariable("bDetachTowHook") 
	ISBaseTimedAction.perform(self)
end


function TAHookDetachVehicle:stop()
    if self.sound then
        self.sound:stop()
    end 
	
	self.character:ClearVariable("bDetachTowHook")	
    ISBaseTimedAction.stop(self)
end

function TAHookDetachVehicle:new(character, time, animation, performFunc, arg1, arg2, arg3, arg4)
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