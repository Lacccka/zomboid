if not EFZ then
    EFZ = {}
end

local function getLocalPlayer(playerNum)
    if playerNum and playerNum.getPlayerNum then
        return playerNum
    end

    if getSpecificPlayer then
        local playerObj = getSpecificPlayer(playerNum)
        if playerObj then
            return playerObj
        end
    end

    if getPlayer then
        return getPlayer()
    end

    return nil
end

local function onEnableGodmode(_, playerObj)
    EFZ.GodmodePlayer(playerObj)
end

local function onDisableGodmode(_, playerObj)
    EFZ.UnGodmodePlayer(playerObj)
end

local function canShowEfzDebugContextMenu(playerObj)
    if not playerObj then
        return false
    end

    if getDebug and getDebug() then
        return true
    end
    if playerObj.isAccessLevel and playerObj:isAccessLevel("admin") then
        return true
    end
    if isAdmin and isAdmin() then
        return true
    end

    return false
end

local function onForceDeployAmbush(_, playerObj)
    if not playerObj or not sendClientCommand then
        return
    end

    sendClientCommand(playerObj, "EFZ", "ForceDeployAmbush", {})
end

local function onFillWorldObjectContextMenu(playerNum, context, worldObjects, test)
    if test or not context then
        return
    end

    local playerObj = getLocalPlayer(playerNum)
    if not playerObj then
        return
    end

    if not canShowEfzDebugContextMenu(playerObj) then
        return
    end

    local rootOption = context:addOption("EFZ Debug", worldObjects, nil)
    local subMenu = context.getNew and context:getNew(context) or nil
    if not rootOption or not subMenu or not context.addSubMenu then
        context:addOption("EFZ Debug: Godmode On", playerObj, onEnableGodmode)
        context:addOption("EFZ Debug: Godmode Off", playerObj, onDisableGodmode)
        context:addOption("EFZ Debug: Force Deploy Ambush", playerObj, onForceDeployAmbush)
        return
    end

    context:addSubMenu(rootOption, subMenu)
    subMenu:addOption("Godmode On", playerObj, onEnableGodmode)
    subMenu:addOption("Godmode Off", playerObj, onDisableGodmode)
    subMenu:addOption("Force Deploy Ambush", playerObj, onForceDeployAmbush)
end

if Events and Events.OnFillWorldObjectContextMenu then
    Events.OnFillWorldObjectContextMenu.Add(onFillWorldObjectContextMenu)
end
