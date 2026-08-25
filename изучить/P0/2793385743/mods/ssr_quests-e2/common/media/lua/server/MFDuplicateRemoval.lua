-- Copyright (c) 2022-2025 Oneline/D.Borovsky
-- All rights reserved
require "Communications/QSystem"
if not QSystem.validate("ssr-quests-e2") then return end
MFDuplicateRemoval = {}
MFDuplicateRemoval.enabled = not isClient() and not isServer();

-- removes duplicates of IsoObjects left from MNPCs in singleplayer
local function removeDupesFromSquare(square)
    if square then
        local objects = square:getObjects();
        if objects:size() > 0 then
            for i=objects:size()-1, 0, -1 do
                local object = objects:get(i);
                local name = object:getName() or "nil";
                if name:starts_with("NPC_") then
                    local character = string.sub(name, 5);
                    QuestLogger.print("[QSystem] MannequinFolks: Removed duped NPC - "..tostring(character));
                    object:removeFromSquare();
                end
            end
            return true;
        end
    else
        print("[QSystem] MannequinFolks: Failed to check square for duped NPCs because it's NULL");
    end
end

function MFDuplicateRemoval.addSquare(square)
    if MFDuplicateRemoval.enabled then
        local m = getGameTime():getModData();
        if type(m.MNPC) ~= "table" then
            m.MNPC = {};
        end
        local x, y, z = square:getX(), square:getY(), square:getZ();
        table.insert(m.MNPC, {x=x, y=y, z=z});
    end
end

function MFDuplicateRemoval.clearSquare(square)
    if MFDuplicateRemoval.enabled then
        local m = getGameTime():getModData();
        if type(m.MNPC) == 'table' then
            local x, y, z = square:getX(), square:getY(), square:getZ();
            for i=#m.MNPC, 1, -1 do
                if x == m.MNPC[i].x and y == m.MNPC[i].y and z == m.MNPC[i].z and removeDupesFromSquare(square) then
                    table.remove(m.MNPC, i);
                end
            end
        end
    end
end

function MFDuplicateRemoval.onGameStart()
    if MFDuplicateRemoval.enabled then
        local m = getGameTime():getModData();
        if type(m.MNPC) == 'table' then
            for i=#m.MNPC, 1, -1 do
                local square = getCell():getGridSquare(m.MNPC[i].x, m.MNPC[i].y, m.MNPC[i].z);
                if square and removeDupesFromSquare(square) then
                    table.remove(m.MNPC, i);
                end
            end
        end
    end
end

Events.OnGameStart.Add(MFDuplicateRemoval.onGameStart);