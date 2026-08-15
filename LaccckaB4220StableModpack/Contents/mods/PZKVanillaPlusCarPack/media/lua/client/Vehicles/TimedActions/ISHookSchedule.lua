if not getActivatedMods():contains("STowTruck")  then
require('TimedActions/ISBaseTimedAction')

TAHookSchedule = ISBaseTimedAction:derive("TAHookSchedule")

-- The condition which tells the timed action if it is still valid
function TAHookSchedule:isValid()
	return true
end

-- Starts the Timed Action
function TAHookSchedule:start()	

end

-- Is called when the time has passed
function TAHookSchedule:perform()
    if self.performFunc ~= nil then
        self.performFunc(self.character, self.arg1, self.arg2, self.arg3, self.arg4)
    end
   
	ISBaseTimedAction.perform(self)
end


function TAHookSchedule:stop()	
    ISBaseTimedAction.stop(self)
end

function TAHookSchedule:new(character, time, performFunc, arg1, arg2, arg3, arg4)
    local o = {}
    setmetatable(o, self)
    self.__index = self
	
	o.useProgressBar = false	
	o.stopOnWalk = false
	o.stopOnRun = false

    o.maxTime = time
	o.character = character
	
	o.performFunc = performFunc
	o.arg1 = arg1
	o.arg2 = arg2
	o.arg3 = arg3
	o.arg4 = arg4
   
    return o
end
end