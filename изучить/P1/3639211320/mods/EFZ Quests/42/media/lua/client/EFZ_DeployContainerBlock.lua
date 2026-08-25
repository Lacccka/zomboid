require "ISUI/ISInventoryPage"

local config = require "EFZ_DeployConfig"

local function isCoordInDeployZone(x, y)
    local zone = config.deployZone
    return x >= zone.minX and x <= zone.maxX and y >= zone.minY and y <= zone.maxY
end

local function isPlayerNoClip(playerObj)
    return playerObj and playerObj.isNoClip and playerObj:isNoClip()
end

local function isAllowedContainerParent(parent)
    if not parent then
        return false
    end
    if instanceof(parent, "IsoDeadBody") then
        return true
    end
    if instanceof(parent, "IsoWorldInventoryObject") then
        return true
    end
    if instanceof(parent, "IsoPlayer") then
        return true
    end
    if instanceof(parent, "BaseVehicle") then
        return true
    end
    return false
end

local function isDeployZoneFurnitureContainer(container)
    if not container then
        return false
    end

    if container.getType and container:getType() == "floor" then
        return false
    end

    local containingItem = container.getContainingItem and container:getContainingItem() or nil
    if containingItem then
        return false
    end

    local parent = container.getParent and container:getParent() or nil
    if isAllowedContainerParent(parent) then
        return false
    end

    local square = nil
    if parent and parent.getSquare then
        square = parent:getSquare()
    end
    if (not square) and container.getSourceGrid then
        square = container:getSourceGrid()
    end
    if not square then
        return false
    end

    return isCoordInDeployZone(square:getX(), square:getY())
end

local function shouldBlockContainerForPage(page, container)
    if not page or page.onCharacter then
        return false
    end
    local playerObj = getSpecificPlayer(page.player)
    if not playerObj or isPlayerNoClip(playerObj) then
        return false
    end
    return isDeployZoneFurnitureContainer(container)
end

local function filterBlockedContainerButtons(page)
    local backpacks = page.backpacks
    if not backpacks or #backpacks == 0 then
        return
    end

    local kept = {}
    local removedAny = false
    for i = 1, #backpacks do
        local button = backpacks[i]
        if button and shouldBlockContainerForPage(page, button.inventory) then
            removedAny = true
            if page.containerButtonPanel and page.containerButtonPanel.removeChild then
                page.containerButtonPanel:removeChild(button)
            end
            if page.buttonPool then
                page.buttonPool[#page.buttonPool + 1] = button
            end
        else
            kept[#kept + 1] = button
        end
    end

    if not removedAny then
        return
    end

    table.wipe(backpacks)
    local y = -1
    for i = 1, #kept do
        local button = kept[i]
        backpacks[i] = button
        button:setY(y)
        y = y + page.buttonSize
    end

    local invPane = page.inventoryPane
    if invPane and shouldBlockContainerForPage(page, invPane.inventory) then
        for i = 1, #backpacks do
            local button = backpacks[i]
            if button and button.inventory and (not shouldBlockContainerForPage(page, button.inventory)) then
                invPane.inventory = button.inventory
                page.inventory = button.inventory
                page.capacity = button.capacity
                break
            end
        end
    end
end

local function onRefreshInventoryWindowContainers(page, stage)
    if stage ~= "buttonsAdded" then
        return
    end
    filterBlockedContainerButtons(page)
end

Events.OnRefreshInventoryWindowContainers.Add(onRefreshInventoryWindowContainers)

if ISInventoryPage and not ISInventoryPage._efzDeployContainerBlockPatched then
    local originalSelectContainer = ISInventoryPage.selectContainer
    ISInventoryPage.selectContainer = function(self, button)
        if button and shouldBlockContainerForPage(self, button.inventory) then
            if isDebugEnabled() then
                local ctype = button.inventory and button.inventory.getType and button.inventory:getType() or "unknown"
                print("[EFZ_DeployContainerBlock] Blocked selectContainer for deploy furniture container type=" .. tostring(ctype))
            end
            self:refreshBackpacks()
            return
        end
        return originalSelectContainer(self, button)
    end

    local originalSetNewContainer = ISInventoryPage.setNewContainer
    ISInventoryPage.setNewContainer = function(self, inventory)
        if shouldBlockContainerForPage(self, inventory) then
            if isDebugEnabled() then
                local ctype = inventory and inventory.getType and inventory:getType() or "unknown"
                print("[EFZ_DeployContainerBlock] Blocked setNewContainer for deploy furniture container type=" .. tostring(ctype))
            end
            self:refreshBackpacks()
            return
        end
        return originalSetNewContainer(self, inventory)
    end

    ISInventoryPage._efzDeployContainerBlockPatched = true
end

-- Natural water source action blocking in deploy zone.
local function blockDeployZoneNaturalWater(playerNum, context, worldobjects, test)
    if test then
        return
    end

    local playerObj = getSpecificPlayer(playerNum)
    if not playerObj then
        return
    end
    if isPlayerNoClip(playerObj) then
        return
    end
    if not isCoordInDeployZone(playerObj:getX(), playerObj:getY()) then
        return
    end

    local naturalWaterKeys = {
        "ContextMenu_NaturalWaterSource",
        "ContextMenu_Fishing",
        "ContextMenu_Place_Fishing_Net",
    }
    for i = 1, #naturalWaterKeys do
        local text = getText(naturalWaterKeys[i])
        if text then
            context:removeOptionByName(text)
        end
    end
end

Events.OnFillWorldObjectContextMenu.Add(blockDeployZoneNaturalWater)
