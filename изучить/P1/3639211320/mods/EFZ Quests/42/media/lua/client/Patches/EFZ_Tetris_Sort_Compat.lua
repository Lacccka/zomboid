local PATCH_TAG = "[EFZ_Tetris_Sort_Compat]"

if _G.__EFZ_TETRIS_SORT_COMPAT_FILE_LOADED then
    return
end
_G.__EFZ_TETRIS_SORT_COMPAT_FILE_LOADED = true
print(PATCH_TAG .. " Loaded EFZ_Tetris_Sort_Compat.lua")

local SORT_MODE_AUTO = "auto"
local SORT_MODE_CATEGORY = "category"
local SORT_MODE_SIZE = "size"
local PROX_INV_TYPE = "proxInv"
local ICON_PADDING_X = 12
local ICON_PADDING_Y = 8
local ICON_SIZE = 64
local PLAYER_SORT_MODE_KEY = "EFZTetrisSortMode"
local SORT_MODE_LABEL_KEYS = {
    [SORT_MODE_AUTO] = "ContextMenu_EFZ_Tetris_Organize_Auto",
    [SORT_MODE_CATEGORY] = "ContextMenu_EFZ_Tetris_Organize_Category",
    [SORT_MODE_SIZE] = "ContextMenu_EFZ_Tetris_Organize_Size",
}

local GridContainerInfo = nil
local ItemGridContainerUI = nil
local OPT = nil
local ItemGridUI = nil
local TetrisDevTool = nil
local TetrisItemData = nil
local TetrisItemCategory = nil
local TetrisContainerData = nil
local categoryOrder = nil

local function debugPrint(message)
    print(PATCH_TAG .. " " .. message)
end

local function isInventoryTetrisActive()
    local mods = getActivatedMods()
    return mods and mods:contains("INVENTORY_TETRIS")
end

local function tr(key)
    return getText(key)
end

local function isSortModeValid(sortMode)
    return sortMode == SORT_MODE_AUTO
        or sortMode == SORT_MODE_CATEGORY
        or sortMode == SORT_MODE_SIZE
end

local function getSortModeLabel(sortMode)
    local textKey = SORT_MODE_LABEL_KEYS[sortMode] or SORT_MODE_LABEL_KEYS[SORT_MODE_AUTO]
    return tr(textKey)
end

local function getSavedSortMode(playerObj)
    if not playerObj or not playerObj.getModData then
        return SORT_MODE_AUTO
    end

    local modData = playerObj:getModData()
    local sortMode = modData[PLAYER_SORT_MODE_KEY]
    if isSortModeValid(sortMode) then
        return sortMode
    end

    return SORT_MODE_AUTO
end

local function setSavedSortMode(playerObj, sortMode)
    if not playerObj or not playerObj.getModData or not isSortModeValid(sortMode) then
        return
    end

    local modData = playerObj:getModData()
    if modData[PLAYER_SORT_MODE_KEY] == sortMode then
        return
    end

    modData[PLAYER_SORT_MODE_KEY] = sortMode
    if playerObj.transmitModData then
        playerObj:transmitModData()
    end
end

local function ensureModules()
    if GridContainerInfo then
        return
    end

    GridContainerInfo = require("InventoryTetris/UI/Container/GridContainerInfo")
    ItemGridContainerUI = require("InventoryTetris/UI/Container/ItemGridContainerUI")
    OPT = require("InventoryTetris/Settings")
    ItemGridUI = require("InventoryTetris/UI/Grid/ItemGridUI")
    TetrisDevTool = require("InventoryTetris/Dev/TetrisDevTool")
    TetrisItemData = require("InventoryTetris/Data/TetrisItemData")
    TetrisItemCategory = require("InventoryTetris/Data/TetrisItemCategory")
    TetrisContainerData = require("InventoryTetris/Data/TetrisContainerData")

    categoryOrder = {
        [TetrisItemCategory.MELEE] = 1,
        [TetrisItemCategory.RANGED] = 2,
        [TetrisItemCategory.AMMO] = 3,
        [TetrisItemCategory.MAGAZINE] = 4,
        [TetrisItemCategory.ATTACHMENT] = 5,
        [TetrisItemCategory.FOOD] = 6,
        [TetrisItemCategory.CLOTHING] = 7,
        [TetrisItemCategory.CONTAINER] = 8,
        [TetrisItemCategory.HEALING] = 9,
        [TetrisItemCategory.BOOK] = 10,
        [TetrisItemCategory.ENTERTAINMENT] = 11,
        [TetrisItemCategory.KEY] = 12,
        [TetrisItemCategory.MISC] = 13,
        [TetrisItemCategory.SEED] = 14,
        [TetrisItemCategory.MOVEABLE] = 15,
        [TetrisItemCategory.CORPSEANIMAL] = 16,
    }
end

local function canOrganizeContainer(containerUi)
    local containerGrid = containerUi.containerGrid
    if containerGrid.isFloor then
        return false
    end

    return containerGrid.containerDefinition.trueType ~= PROX_INV_TYPE
end

local function getContainerDebugName(containerUi)
    if containerUi.isPlayerInventory then
        return "player-main-inventory"
    end

    if containerUi.item then
        return containerUi.item:getDisplayName()
    end

    return tostring(containerUi.inventory:getType())
end

local function collectSortableItems(containerGrid)
    local sortableItems = {}
    local seenCategories = {}
    local uniqueCategoryCount = 0
    local items = containerGrid.inventory:getItems()

    for i = 0, items:size() - 1 do
        local item = items:get(i)
        if containerGrid:_isItemValid(item) then
            local width, height = TetrisItemData.getItemSize(item, false)
            local category = TetrisItemCategory.getCategory(item)
            if not seenCategories[category] then
                seenCategories[category] = true
                uniqueCategoryCount = uniqueCategoryCount + 1
            end

            sortableItems[#sortableItems + 1] = {
                item = item,
                category = category,
                width = width,
                height = height,
                area = width * height,
                weight = item:getUnequippedWeight(),
                name = item:getDisplayName(),
                fullType = item:getFullType(),
                id = item:getID(),
            }
        end
    end

    return sortableItems, uniqueCategoryCount
end

local function resolveSortMode(requestedMode, containerUi, uniqueCategoryCount)
    if requestedMode ~= SORT_MODE_AUTO then
        return requestedMode
    end

    local containerGrid = containerUi.containerGrid
    local lockedCategory = TetrisContainerData.getSingleValidCategory(containerGrid.containerDefinition)
    if lockedCategory or uniqueCategoryCount <= 1 then
        return SORT_MODE_SIZE
    end

    if containerGrid.isPlayerInventory or containerGrid.isOnPlayer then
        return SORT_MODE_CATEGORY
    end

    return SORT_MODE_CATEGORY
end

local function compareNames(a, b)
    if a.name ~= b.name then
        return a.name < b.name
    end

    if a.fullType ~= b.fullType then
        return a.fullType < b.fullType
    end

    return a.id < b.id
end

local function compareByCategory(a, b)
    local aCategoryOrder = categoryOrder[a.category] or 999
    local bCategoryOrder = categoryOrder[b.category] or 999
    if aCategoryOrder ~= bCategoryOrder then
        return aCategoryOrder < bCategoryOrder
    end

    if a.area ~= b.area then
        return a.area > b.area
    end

    if a.weight ~= b.weight then
        return a.weight > b.weight
    end

    return compareNames(a, b)
end

local function compareBySize(a, b)
    if a.area ~= b.area then
        return a.area > b.area
    end

    if a.width ~= b.width then
        return a.width > b.width
    end

    if a.height ~= b.height then
        return a.height > b.height
    end

    local aCategoryOrder = categoryOrder[a.category] or 999
    local bCategoryOrder = categoryOrder[b.category] or 999
    if aCategoryOrder ~= bCategoryOrder then
        return aCategoryOrder < bCategoryOrder
    end

    return compareNames(a, b)
end

local function clearGridStacks(grid)
    local gridData = grid:_getSavedGridData()
    gridData.stacks = {}
    grid:refresh()
    grid:_sendModData()
end

local function clearContainerGrid(containerGrid)
    for i = 1, #containerGrid.grids do
        clearGridStacks(containerGrid.grids[i])
    end

    for _, grids in pairs(containerGrid.secondaryGrids) do
        for i = 1, #grids do
            clearGridStacks(grids[i])
        end
    end

    containerGrid.overflow = {}
end

local function organizeContainer(containerUi, requestedMode)
    if not canOrganizeContainer(containerUi) then
        return
    end

    local containerGrid = containerUi.containerGrid
    if containerGrid:areAnyUnsearched() then
        debugPrint("Skipped organize for " .. getContainerDebugName(containerUi) .. ": container is not fully searched.")
        return
    end

    local sortableItems, uniqueCategoryCount = collectSortableItems(containerGrid)
    if #sortableItems == 0 then
        debugPrint("Skipped organize for " .. getContainerDebugName(containerUi) .. ": no sortable items.")
        return
    end

    local resolvedMode = resolveSortMode(requestedMode, containerUi, uniqueCategoryCount)
    if resolvedMode == SORT_MODE_SIZE then
        table.sort(sortableItems, compareBySize)
    else
        table.sort(sortableItems, compareByCategory)
    end

    -- Keep search progress and other grid metadata intact by clearing only stacks.
    clearContainerGrid(containerGrid)

    for i = 1, #sortableItems do
        containerGrid:attemptToInsertItem(sortableItems[i].item, false, false)
    end

    containerGrid:_updateGridPositions()
    containerGrid.lastRefresh = getTimestampMs()
    containerGrid.needsImmediateRefresh = false
    containerUi.inventoryPane:refreshContainer()

    debugPrint(
        "Organized "
            .. getContainerDebugName(containerUi)
            .. " using "
            .. resolvedMode
            .. " (requested="
            .. requestedMode
            .. ", items="
            .. tostring(#sortableItems)
            .. ")."
    )
end

local function getOrganizeDisabledReason(containerUi)
    if containerUi.containerGrid:areAnyUnsearched() then
        return tr("ContextMenu_EFZ_Tetris_Organize_SearchFirst")
    end

    local sortableItems = collectSortableItems(containerUi.containerGrid)
    if #sortableItems == 0 then
        return tr("ContextMenu_EFZ_Tetris_Organize_NoItems")
    end

    return nil
end

local function onSelectOrganizeMode(containerUi, requestedMode)
    setSavedSortMode(containerUi.player, requestedMode)
    organizeContainer(containerUi, requestedMode)
end

local function addSortModeOptions(menu, containerUi)
    local savedSortMode = getSavedSortMode(containerUi.player)
    local sortModes = {
        SORT_MODE_AUTO,
        SORT_MODE_CATEGORY,
        SORT_MODE_SIZE,
    }

    for i = 1, #sortModes do
        local sortMode = sortModes[i]
        local label = getSortModeLabel(sortMode)
        if sortMode == savedSortMode then
            label = "* " .. label
        end

        menu:addOption(label, containerUi, onSelectOrganizeMode, sortMode)
    end
end

local function getOrganizeRootLabel(containerUi)
    return tr("ContextMenu_EFZ_Tetris_Organize") .. " [" .. getSortModeLabel(getSavedSortMode(containerUi.player)) .. "]"
end

local function addOrganizeMenu(menu, containerUi)
    if not canOrganizeContainer(containerUi) then
        return
    end

    local rootLabel = getOrganizeRootLabel(containerUi)
    local rootOption = menu:addOption(rootLabel, nil, nil)

    local disabledReason = getOrganizeDisabledReason(containerUi)

    if disabledReason then
        rootOption.name = rootLabel .. " (" .. disabledReason .. ")"
        rootOption.notAvailable = true
        return
    end

    local subMenu = menu:getNew(menu)
    menu:addSubMenu(rootOption, subMenu)
    addSortModeOptions(subMenu, containerUi)
end

local function populateOrganizeDropdown(menu, containerUi)
    if not canOrganizeContainer(containerUi) then
        return false
    end

    local disabledReason = getOrganizeDisabledReason(containerUi)

    if disabledReason then
        local unavailableOption = menu:addOption(disabledReason, nil, nil)
        unavailableOption.notAvailable = true
        return false
    end

    addSortModeOptions(menu, containerUi)
    return true
end

local function isPointOverContainerIcon(x, y)
    local scale = OPT.CONTAINER_INFO_SCALE
    return x > ICON_PADDING_X * scale
        and x < (ICON_SIZE + ICON_PADDING_X) * scale
        and y > ICON_PADDING_Y * scale
        and y < (ICON_SIZE + ICON_PADDING_Y) * scale
end

local function getOrganizeButtonBaseLabel()
    return getText("IGUI_EFZ_Tetris_Organize_Button")
end

local function getOrganizeButtonLabel(containerUi)
    if not containerUi then
        return getOrganizeButtonBaseLabel()
    end

    return getOrganizeButtonBaseLabel() .. ": " .. getSortModeLabel(getSavedSortMode(containerUi.player))
end

local function getOrganizeButtonTooltip(containerUi)
    if not containerUi then
        return getText("IGUI_EFZ_Tetris_Organize_ButtonTooltip")
    end

    return getText("IGUI_EFZ_Tetris_Organize_ButtonTooltip") .. " [" .. getSortModeLabel(getSavedSortMode(containerUi.player)) .. "]"
end

local function getOrganizeButtonWidth(containerUi)
    local label = getOrganizeButtonLabel(containerUi)
    return math.max(40, getTextManager():MeasureStringX(UIFont.Small, label) + 12)
end

local function getOrganizeButtonStaticTooltip()
    return getText("IGUI_EFZ_Tetris_Organize_ButtonTooltip")
end

local function patchGridContainerInfo()
    ensureModules()
    if GridContainerInfo._efzSortCompatPatched then
        return
    end

    function GridContainerInfo:onRightMouseUp(x, y)
        local menu
        if self.containerUi.item and isPointOverContainerIcon(x, y) then
            menu = ItemGridUI.openItemContextMenu(self, x, y, self.containerUi.item, self.containerUi.inventoryPane, self.containerUi.playerNum)
        else
            menu = ISContextMenu.get(0, getMouseX(), getMouseY())
        end

        TetrisDevTool.insertDebugOptions(menu, self.containerUi.item, self.containerUi.inventory, self.containerUi)
        addOrganizeMenu(menu, self.containerUi)
    end

    GridContainerInfo._efzSortCompatPatched = true
    debugPrint("Patched GridContainerInfo with organize menu.")
end

local function patchItemGridContainerUI()
    ensureModules()
    if ItemGridContainerUI._efzSortCompatPatched then
        return
    end

    local originalInitialise = ItemGridContainerUI.initialise
    function ItemGridContainerUI:initialise()
        originalInitialise(self)

        if self.organizeButton then
            return
        end

        local organizeButton = ISButton:new(0, 0, getOrganizeButtonWidth(self), 16, getOrganizeButtonLabel(self), self, ItemGridContainerUI.onOrganizeButtonClick)
        organizeButton:initialise()
        organizeButton.borderColor = { r = 1, g = 1, b = 1, a = 0.1 }
        organizeButton.backgroundColor = { r = 1, g = 1, b = 1, a = 0.1 }
        organizeButton.tooltip = getOrganizeButtonStaticTooltip()

        self.organizeButton = organizeButton
        self:addChild(organizeButton)
    end

    function ItemGridContainerUI:onOrganizeButtonClick(button)
        if not canOrganizeContainer(self) then
            return
        end

        local anchorX = button and button:getAbsoluteX() or getMouseX()
        local anchorY = button and (button:getAbsoluteY() + button:getHeight()) or getMouseY()
        local menu = ISContextMenu.get(self.playerNum, anchorX, anchorY)
        populateOrganizeDropdown(menu, self)
    end

    local originalPrerender = ItemGridContainerUI.prerender
    function ItemGridContainerUI:prerender()
        originalPrerender(self)

        if not self.organizeButton then
            return
        end

        local shouldShowButton = self.showTitle and canOrganizeContainer(self)
        self.organizeButton:setVisible(shouldShowButton)
        if not shouldShowButton then
            return
        end

        local buttonWidth = getOrganizeButtonWidth(self)
        self.organizeButton:setTitle(getOrganizeButtonLabel(self))
        self.organizeButton:setTooltip(getOrganizeButtonTooltip(self))
        self.organizeButton:setWidth(buttonWidth)
        self.organizeButton:setHeight(self.collapseButton:getHeight())
        self.organizeButton:setX(self.collapseButton:getX() + self.collapseButton:getWidth() + 4)
        self.organizeButton:setY(self.collapseButton:getY())

        local requiredWidth = self.organizeButton:getX() + self.organizeButton:getWidth() + 2
        if self:getWidth() < requiredWidth then
            self:setWidth(requiredWidth)
        end
    end

    ItemGridContainerUI._efzSortCompatPatched = true
    debugPrint("Patched ItemGridContainerUI with organize dropdown button.")
end

local function applyPatch()
    if not isInventoryTetrisActive() then
        return
    end

    patchGridContainerInfo()
    patchItemGridContainerUI()
end

Events.OnGameBoot.Add(applyPatch)
Events.OnCreatePlayer.Add(applyPatch)
Events.OnGameStart.Add(applyPatch)
