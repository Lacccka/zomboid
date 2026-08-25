require "BuildingObject/ISBuildCursorMouse"
require "BuildingObject/ISMoveableCursor"
require "BuildingObject/ISDestroyCursor"

QMapProtector = {}

local protected = {};
local protected_size = 0;

function QMapProtector.validate(x, y, z)
    for i=1, protected_size do
        if protected[i][1] == x and protected[i][2] == y and protected[i][3] == z then
            return false;
        end
    end
    return true;
end

function QMapProtector.protect(x, y, z)
    if QMapProtector.validate(x, y, z) then
        protected_size = protected_size + 1; protected[protected_size] = { x, y, z }
    end
end

Events.OnQSystemReset.Add(function ()
    protected = {};
    protected_size = 0;
end);

local ISBuildCursorMouse_isValid = ISBuildCursorMouse.isValid;
function ISBuildCursorMouse:isValid(square)
    return ISBuildCursorMouse_isValid(self, square) and QMapProtector.validate(square:getX(), square:getY(), square:getZ());
end

local ISMoveableCursor_isValid = ISMoveableCursor.isValid;
function ISMoveableCursor:isValid(square)
    if ISMoveableCursor_isValid(self, square) then
        if QMapProtector.validate(square:getX(), square:getY(), square:getZ()) then
            return true;
        else
            self.canCreate = nil;
            self:setInfoPanel( square, nil, nil );
            self.colorMod = {r=1,g=0,b=0};
        end
    end
end

local ISDestroyCursor_isValid = ISDestroyCursor.isValid;
function ISDestroyCursor:isValid(square)
    return ISDestroyCursor_isValid(self, square) and QMapProtector.validate(square:getX(), square:getY(), square:getZ());
end

local ISDestroyCursor_canDestroy = ISDestroyCursor.canDestroy;
function ISDestroyCursor:canDestroy(object)
    if ISDestroyCursor_canDestroy(self, object) then
        local square = object:getSquare();
        return QMapProtector.validate(square:getX(), square:getY(), square:getZ());
    end
end