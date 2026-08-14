-- From "More Damaged Objects [B41]" mod -- Author = carlesturo

-- **************** SET GNOME UPRIGHT ****************

require "TimedActions/ISBaseTimedAction"

SetGnomeUpright = ISBaseTimedAction:derive("SetGnomeUpright")

function SetGnomeUpright:isValid()
    return self.obj ~= nil
end

function SetGnomeUpright:update()
end

function SetGnomeUpright:start()
    self:setActionAnim("Loot")
    self:setAnimVariable("LootPosition", "Low")
    self:setOverrideHandModels(nil, nil)
end

function SetGnomeUpright:stop()
    ISBaseTimedAction.stop(self)
end

function SetGnomeUpright:perform()
    local square = self.obj:getSquare()
    local spriteName = self.obj:getSprite():getName()
    local newSpriteName = nil

    if spriteName == "ct_more_damaged_objects_01_24" then
        newSpriteName = "vegetation_ornamental_01_48"
    elseif spriteName == "ct_more_damaged_objects_01_25" then
        newSpriteName = "vegetation_ornamental_01_49"
    end

    if newSpriteName then
        square:transmitRemoveItemFromSquare(self.obj)

		local newObj = IsoObject.new(square, newSpriteName, nil, false)
		square:AddTileObject(newObj)
		newObj:transmitCompleteItemToServer()
    end

    ISBaseTimedAction.perform(self)
end

function SetGnomeUpright:new(character, obj, time)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.character = character
    o.obj = obj
    o.stopOnWalk = true
    o.stopOnRun = true
    o.maxTime = time
    return o
end