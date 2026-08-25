require "TimedActions/ISBaseTimedAction"
require "MissionsEvents_UI"

MissionsEvents_PagerAction = ISBaseTimedAction:derive("MissionsEvents_PagerAction")

function MissionsEvents_PagerAction:isValid()
    return self.item and self.character and self.character:getInventory():contains(self.item)
end

function MissionsEvents_PagerAction:start()
    if self.character and self.character:getEmitter() then
        self.character:getEmitter():playSound("RadioButton")
    end
end

function MissionsEvents_PagerAction:update()
end

function MissionsEvents_PagerAction:stop()
    ISBaseTimedAction.stop(self)
end

function MissionsEvents_PagerAction:perform()
    ISBaseTimedAction.perform(self)

    if MissionsEvents_UI and MissionsEvents_UI.open then
        MissionsEvents_UI.open(self.character)
    end
end

function MissionsEvents_PagerAction:new(character, item)
    local o = {}
    setmetatable(o, self)
    self.__index = self

    o.character = character
    o.item = item

    o.stopOnWalk = true
    o.stopOnRun = true
    o.maxTime = 50

    if character and character:isTimedActionInstant() then
        o.maxTime = 1
    end

    return o
end