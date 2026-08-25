---@diagnostic disable: duplicate-set-field

local PATCH_TAG = "[EFZ_Tetris_CarryCapacity_Compat]"

if _G.__EFZ_TETRIS_CARRY_CAPACITY_COMPAT_FILE_LOADED then
    return
end
_G.__EFZ_TETRIS_CARRY_CAPACITY_COMPAT_FILE_LOADED = true
print(PATCH_TAG .. " Loaded EFZ_Tetris_CarryCapacity_Compat.lua")

local TetrisContainerData = nil

local function debugPrint(message)
    print(PATCH_TAG .. " " .. message)
end

local function isInventoryTetrisActive()
    local mods = getActivatedMods()
    return mods and mods:contains("INVENTORY_TETRIS")
end

local function disableCarryWeightOnContainer(container, callback, ...)
    if container and instanceof(container, "InventoryContainer") then
        container = container:getInventory()
    end

    if not container then
        return callback(...)
    end

    local originalType = container:getType()
    local sandboxVars = SandboxVars and SandboxVars.InventoryTetris
    if originalType == "floor" or (sandboxVars and sandboxVars.EnforceCarryWeight) then
        return callback(...)
    end

    local containerDef = TetrisContainerData.getContainerDefinition(container)
    if containerDef.isFragile then
        return callback(...)
    end

    local originalCapacity = container:getCapacity()
    if originalCapacity == 50 then
        container:setCapacity(49)
    end

    container:setType("floor")
    TetrisContainerData.setContainerDefinition(container, containerDef)

    local results = { callback(...) }

    TetrisContainerData.setContainerDefinition(container, nil)
    container:setType(originalType)
    if originalCapacity == 50 then
        container:setCapacity(originalCapacity)
    end

    return unpack(results)
end

local function applyCarryCapacityPatch()
    if _G.__EFZ_TETRIS_CARRY_CAPACITY_PATCH_APPLIED then
        return
    end

    if not isInventoryTetrisActive() then
        return
    end

    require("InventoryTetris/Data/TetrisContainerData")
    require("ISUI/ISInventoryPane")
    require("ISUI/ISInventoryPage")
    require("TimedActions/ISInventoryTransferAction")
    require("ISUI/ISInventoryPaneContextMenu")
    require("Foraging/ISBaseIcon")
    require("ISUI/ISVehicleMenu")

    TetrisContainerData = TetrisContainerData or require("InventoryTetris/Data/TetrisContainerData")

    local originalPaneCanPutIn = ISInventoryPane.canPutIn
    function ISInventoryPane:canPutIn()
        return disableCarryWeightOnContainer(self.inventory, originalPaneCanPutIn, self)
    end

    local originalDraggedItemsUpdate = ISInventoryPaneDraggedItems.update
    function ISInventoryPaneDraggedItems:update()
        -- The carry-capacity wrapper needs a valid player index before getDropContainer()
        -- resolves icon targets, otherwise bag-icon drops can fall back to the wrong target.
        self.playerNum = self.inventoryPane.player
        local container = self:getDropContainer()
        return disableCarryWeightOnContainer(container, originalDraggedItemsUpdate, self)
    end

    local originalPageCanPutIn = ISInventoryPage.canPutIn
    function ISInventoryPage:canPutIn()
        local container = self.mouseOverButton and self.mouseOverButton.inventory or nil
        return disableCarryWeightOnContainer(container, originalPageCanPutIn, self)
    end

    local originalDropItemsInContainer = ISInventoryPage.dropItemsInContainer
    function ISInventoryPage:dropItemsInContainer(button)
        local previousMouseOverButton = self.mouseOverButton
        self.mouseOverButton = button or previousMouseOverButton

        local results = { originalDropItemsInContainer(self, button) }

        self.mouseOverButton = previousMouseOverButton
        return unpack(results)
    end

    local originalTransferIsValid = ISInventoryTransferAction.isValid
    function ISInventoryTransferAction:isValid()
        return disableCarryWeightOnContainer(self.destContainer, originalTransferIsValid, self)
    end

    local originalHasRoomForAny = ISInventoryPaneContextMenu.hasRoomForAny
    function ISInventoryPaneContextMenu.hasRoomForAny(playerObj, container, items)
        return disableCarryWeightOnContainer(container, originalHasRoomForAny, playerObj, container, items)
    end

    local originalBaseIconDoContextMenu = ISBaseIcon.doContextMenu
    function ISBaseIcon:doContextMenu(context)
        local playerInventory = self.character:getInventory()
        return disableCarryWeightOnContainer(playerInventory, originalBaseIconDoContextMenu, self, context)
    end

    local originalMoveItemsOnSeat = ISVehicleMenu.moveItemsOnSeat
    function ISVehicleMenu.moveItemsOnSeat(seat, newSeat, playerObj, moveThem, itemListIndex)
        local container = newSeat:getItemContainer()
        return disableCarryWeightOnContainer(container, originalMoveItemsOnSeat, seat, newSeat, playerObj, moveThem, itemListIndex)
    end

    _G.__EFZ_TETRIS_CARRY_CAPACITY_PATCH_APPLIED = true
    debugPrint("Patched Inventory Tetris carry-capacity checks and bag icon drag targets.")
end

local function applyCarryCapacityPatchDeferred()
    Events.OnTick.Remove(applyCarryCapacityPatchDeferred)
    _G.__EFZ_TETRIS_CARRY_CAPACITY_PATCH_QUEUED = nil
    applyCarryCapacityPatch()
end

local function scheduleCarryCapacityPatch()
    if _G.__EFZ_TETRIS_CARRY_CAPACITY_PATCH_APPLIED or _G.__EFZ_TETRIS_CARRY_CAPACITY_PATCH_QUEUED then
        return
    end

    _G.__EFZ_TETRIS_CARRY_CAPACITY_PATCH_QUEUED = true
    Events.OnTick.Add(applyCarryCapacityPatchDeferred)
end

Events.OnGameStart.Add(scheduleCarryCapacityPatch)
Events.OnCreatePlayer.Add(scheduleCarryCapacityPatch)
