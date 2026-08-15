-- From "More Damaged Objects [B41]" mod -- Author = carlesturo

MDO_ContextMenu = {}

-- ------------------------------------------------------------------------------------------------

-- **************** SET GNOME UPRIGHT ****************

MDO_ContextMenu.onSetGnomeUpright = function(object, playerObj)
    if object == nil then return end
    if luautils.walkAdj(playerObj, object:getSquare()) then
        ISTimedActionQueue.add(SetGnomeUpright:new(playerObj, object, 50))
    end
end

-- **************** MORE DAMAGED OBJECTS - CONTEXT MENU - FALLEN GNOME ****************

MDO_ContextMenu.initContextMenu = function(player, context, worldobjects)
    local playerObj = getSpecificPlayer(player)
    local FallenGnome = nil

    for _, object in ipairs(worldobjects) do
        if instanceof(object, "IsoObject") and object:getSprite() then
            local spriteName = object:getSprite():getName()
            if spriteName == "ct_more_damaged_objects_01_24" or spriteName == "ct_more_damaged_objects_01_25" then
                FallenGnome = object
                break
            end
        end
    end

    if not FallenGnome then return end

    context:addOption(getText("ContextMenu_SetUpright"), FallenGnome, MDO_ContextMenu.onSetGnomeUpright, playerObj)
end

Events.OnFillWorldObjectContextMenu.Add(MDO_ContextMenu.initContextMenu)

-- ------------------------------------------------------------------------------------------------