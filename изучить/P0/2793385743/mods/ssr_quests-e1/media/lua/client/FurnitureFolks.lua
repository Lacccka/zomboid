-- Copyright (c) 2022-2025 Oneline/D.Borovsky
-- All rights reserved
require "TimedActions/ISInventoryTransferAction"
require "TimedActions/ISDropWorldItemAction"
require "Communications/QSystem"
require "Scripting/FFManager"

if not QSystem.validate("ssr-quests-e1") then return end

FurnitureFolks = {}
FurnitureFolks.instances = {}

FurnitureFolks.createOption = function(context, character_id) -- modders might want to override this function to add different options like "Inspect corpse" or something
    if CharacterManager.instance.items[character_id]:isAlive() then
        --context:addOptionOnTop((getTextOrNull("UI_QSystem_TalkTo") or "Talk to ")..CharacterManager.instance.items[character_id].displayName, CharacterManager.instance.items[character_id].file, DialoguePanel.create);
        context:addOptionOnTop((getTextOrNull("UI_QSystem_Talk") or "Talk"), CharacterManager.instance.items[character_id].file, DialoguePanel.create);
    end
end

FurnitureFolks.createMenu = function(player, context, worldobjects)
    local playerObj = getPlayer();
    local z = math.floor(playerObj:getZ());
    local x = math.floor(screenToIsoX(player, context.x, context.y, z));
	local y = math.floor(screenToIsoY(player, context.x, context.y, z));

    local occupied = false;
    local function validate(square)
        if not square then return end
        local objects = square:getObjects();
        if objects:size() > 0 then
            for i=objects:size()-1, 0, -1 do
                local object = objects:get(i);
                local name = object:getName() or "nil";
                if name:starts_with("NPC_") then
                    if instanceof(object, "IsoMannequin") or not instanceof(object, "IsoObject") then
                        occupied = true;
                        break;
                    end
                    local character = string.sub(name, 5);
                    local char_id = CharacterManager.instance:indexOf(character);
                    local distance = square:DistToProper(playerObj);
                    if char_id and distance <= 3 then
                        FurnitureFolks.createOption(context, char_id);
                        occupied = true;
                    end
                end
            end
        end
    end

    validate(getCell():getGridSquare(x, y, z));
    if not occupied then
        validate(getCell():getGridSquare(x+1, y+1, z));
    end

    if isClient() then
        local accessLevel = getAccessLevel();
        if accessLevel == "" or accessLevel == "None" then return end
    elseif not isDebugEnabled() then
        return;
    end

    local option = context:addOption("[DEBUG] Furniture Folks", worldobjects, nil);
    local subMenu = ISContextMenu:getNew(context);
    context:addSubMenu(option, subMenu);

    subMenu:addOption("Respawn", 4, FurnitureFolks.respawn);
    subMenu:addOption("Despawn all", nil, FurnitureFolks.despawnAll);
end


FurnitureFolks.despawnAll = function ()
    if FFManager.instance then
        FFManager.instance:removeAll(true);
    end
end

Events.OnFillWorldObjectContextMenu.Add(FurnitureFolks.createMenu);

FurnitureFolks.respawn = function(code)
    if code == 4 then
        FurnitureFolks.despawnAll();
        FFManager.updateSpawnPoints(nil, true);
    end
end

Events.OnQSystemUpdate.Add(FurnitureFolks.respawn);

if QSystem.validate("ssr-quests-e1") then
    local ISDropWorldItemAction_perform = ISDropWorldItemAction.perform;
    function ISDropWorldItemAction:perform()
        ISDropWorldItemAction_perform(self);
        if FFManager.instance and self.sq then
            local x, y, z = self.sq:getX(), self.sq:getY(), self.sq:getZ();
            for i=1, FFManager.instance.items_size do
                if FFManager.instance.items[i].instance and FFManager.instance.items[i].instance.javaObject and FFManager.instance.items[i].instance.x == x and FFManager.instance.items[i].instance.y == y and FFManager.instance.items[i].instance.z == z then
                    FFManager.instance.items[i].instance:spawn();
                    return;
                end
            end
        end
    end

    local ISInventoryTransferAction_canDropOnFloor = ISInventoryTransferAction.canDropOnFloor;
    function ISInventoryTransferAction:canDropOnFloor(square)
        if ISInventoryTransferAction_canDropOnFloor(self, square) then
            if FFManager.instance then
                local x, y, z = square:getX(), square:getY(), square:getZ();
                for i=1, FFManager.instance.items_size do
                    if FFManager.instance.items[i].instance and FFManager.instance.items[i].instance.javaObject and FFManager.instance.items[i].instance.x == x and FFManager.instance.items[i].instance.y == y and FFManager.instance.items[i].instance.z == z then
                        return false;
                    end
                end
            end
            return true;
        end
        return false;
    end
end


require "WMM"

if not WMM then return end

local ISWorldMap_render = ISWorldMap.render;
function ISWorldMap:render()
    ISWorldMap_render(self);
    if CharacterManager.instance and FFManager.instance then
        for i=1, FFManager.instance.items_size do
            if FFManager.instance.items[i].instance and CharacterManager.instance.items[FFManager.instance.items[i].character_id].showOnMap then
                WMM.renderDot(self, FFManager.instance.items[i].instance.x, FFManager.instance.items[i].instance.y, 0, 1, 0, 1)
            end
        end
    end
end

local ISMiniMapInner_render = ISMiniMapInner.render;
function ISMiniMapInner:render()
    ISMiniMapInner_render(self);
    if CharacterManager.instance and FFManager.instance then
        for i=1, FFManager.instance.items_size do
            if FFManager.instance.items[i].instance and CharacterManager.instance.items[FFManager.instance.items[i].character_id].showOnMap then
                WMM.renderDot(self, FFManager.instance.items[i].instance.x, FFManager.instance.items[i].instance.y, 0, 1, 0, 1)
            end
        end
    end
end