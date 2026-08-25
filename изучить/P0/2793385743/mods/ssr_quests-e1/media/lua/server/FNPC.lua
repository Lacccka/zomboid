-- Copyright (c) 2022-2025 Oneline/D.Borovsky
-- All rights reserved
require "BuildingObjects/ISBuildingObject"
require "BuildingObjects/ISDestroyCursor"
require "BuildingObjects/ISMoveableCursor"
require "BuildingObjects/ISPlace3DItemCursor"
require "Scripting/FFManager"

if QSystem.validate("ssr-quests-e1") and not isServer() then
    local ISDestroyCursor_canDestroy = ISDestroyCursor.canDestroy;
    function ISDestroyCursor:canDestroy(object)
        if ISDestroyCursor_canDestroy(self, object) then
            local name = object:getName() or "";
            if string.starts_with(name, "NPC_") then
                return false;
            end
            return true;
        else
            return false;
        end
    end

    local ISMoveableCursor_isValid = ISMoveableCursor.isValid;
    function ISMoveableCursor:isValid(square)
        if ISMoveableCursor.mode[self.player] == "place" and FFManager.instance and square then
            local x, y, z = square:getX(), square:getY(), square:getZ();
            for i=1, FFManager.instance.items_size do
                if FFManager.instance.items[i].instance and FFManager.instance.items[i].instance.javaObject and FFManager.instance.items[i].instance.x == x and FFManager.instance.items[i].instance.y == y and FFManager.instance.items[i].instance.z == z then
                    self.canCreate          = nil;
                    self.colorMod           = {r=1,g=0,b=0};
                    self.yOffset            = 0;
                    self:setInfoPanel(square, nil, nil);
                    self.cursorFacing = nil;
                    self.joypadFacing = nil;
                    return false;
                end
            end
        end
        return ISMoveableCursor_isValid(self, square);
    end

    local ISPlace3DItemCursor_isValid = ISPlace3DItemCursor.isValid;
    function ISPlace3DItemCursor:isValid(square)
        if FFManager.instance and square then
            local x, y, z = square:getX(), square:getY(), square:getZ();
            for i=1, FFManager.instance.items_size do
                if FFManager.instance.items[i].instance and FFManager.instance.items[i].instance.javaObject and FFManager.instance.items[i].instance.x == x and FFManager.instance.items[i].instance.y == y and FFManager.instance.items[i].instance.z == z then
                    return false;
                end
            end
        end
        return ISPlace3DItemCursor_isValid(self, square);
    end
end

FNPC = ISBuildingObject:derive("FNPC");

local colorInfo = ColorInfo.new(1, 1, 1, 1);
function FNPC:setAnimation(sprite, frame_count, speed, offset_x, offset_y, loop, deleteWhenFinished)
    local index = sprite:lastIndexOf('_');
    local sprite_name, sprite_id = string.sub(sprite, 1, index), string.sub(sprite, index+2);
    self.animation = { sprite, sprite_name, sprite_id, frame_count, speed, offset_x, offset_y, loop, deleteWhenFinished };
    if self.javaObject then
        self.javaObject:setSprite("");
        self.javaObject:AttachAnim(sprite_name, sprite_id, frame_count, speed, offset_x, offset_y, loop, 0.7, deleteWhenFinished, 0.7, colorInfo);
    end
end

function FNPC:removeAnimation()
    self.animation = {};
    if self.javaObject then
        self.javaObject:setSprite(self:getSprite());
        self.javaObject:RemoveAttachedAnims();
    end
end

function FNPC:spawn()
    if self.javaObject then
        self:despawn();
    end

    self.sq = getWorld():getCell():getGridSquare(self.x, self.y, self.z);

    if self.sq then
        if self.animation[1] then
            self.javaObject = IsoObject.getNew(self.sq, nil, "NPC_"..self.name, true);
            if self.javaObject then
                self.javaObject:AttachAnim(self.animation[2], self.animation[3], self.animation[4], self.animation[5], self.animation[6], self.animation[7], self.animation[8], 0.7, self.animation[9], 0.7, colorInfo);
            end
        else
            self.javaObject = IsoObject.getNew(self.sq, self:getSprite(), "NPC_"..self.name, true);
        end

        if self.javaObject then
            self.javaObject:setNoPicking(true);
            self.sq:AddSpecialObject(self.javaObject);
            self.javaObject:setRenderYOffset(10);
            print("[QSystem] FurnitureFolks: Added character '"..tostring(self.name).."' to square "..tostring(self.sq:getX())..", "..tostring(self.sq:getY())..", "..tostring(self.sq:getZ()));
            FFDuplicateRemoval.addSquare(self.sq);
            if CharacterManager.instance.items[self.character_id] then CharacterManager.instance.items[self.character_id].spawned = true; end
        else
            print("[QSystem] (Error) FurnitureFolks: Java Object is NULL. "..self.info);
        end
    else
        print("[QSystem] (Error) FurnitureFolks: Grid Square is NULL. "..self.info);
    end
end

function FNPC:despawn()
    if self.javaObject then
        local square = getWorld():getCell():getGridSquare(self.x, self.y, self.z);
        if self.sq then
            self.javaObject:removeFromSquare();
            self.javaObject = nil;
            print("[QSystem] FurnitureFolks: Removed character '"..tostring(self.name).."' from square "..tostring(self.sq:getX())..", "..tostring(self.sq:getY())..", "..tostring(self.sq:getZ()));
            if self.sq == square then FFDuplicateRemoval.clearSquare(self.sq); end
            if CharacterManager.instance.items[self.character_id] then CharacterManager.instance.items[self.character_id].spawned = false; end
        else
            self.sq = square;
        end
    end
end

function FNPC:new(name, sprite, x, y, z, character_id)
    local o = {};
    setmetatable(o, self);
    self.__index = self;
    o:init();
    sprite = tostring(sprite);
    if sprite:ends_with(".png") and not sprite:starts_with("media/textures/") then
        sprite = "media/textures/"..sprite;
    end
    o:setSprite(sprite);
    o:setNorthSprite(sprite);
    o.animation = {};
    o.name = name or "none";
    o.x = x;
    o.y = y;
    o.z = z;
    o.info = string.format("name='%s', sprite='%s', x=%i, y=%i, z=%i", o.name, sprite, x, y, z);
    o.character_id = character_id;
    return o;
end

function FNPC:getHealth()
    return 100;
end

function FNPC:isValid(square)
    return true;
end

function FNPC:render(x, y, z, square)
    ISBuildingObject.render(self, x, y, z, square)
end
