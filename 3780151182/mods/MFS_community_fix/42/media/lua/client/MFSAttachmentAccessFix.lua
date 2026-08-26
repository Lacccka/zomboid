require "TimedActions/ISBaseTimedAction"
require "TimedActions/ISInventoryTransferAction"
require "ISUI/ISInventoryPaneContextMenu"

MFSAttachmentAccessFix = MFSAttachmentAccessFix or {}
local Fix = MFSAttachmentAccessFix

Fix.VERSION = "1.0.0"
Fix.REFRESH_INTERVAL_MS = 250
Fix.currentPane = Fix.currentPane or nil

local function log(message)
    print("[MFSAttachmentAccessFix] " .. tostring(message))
end

local function isNormalAttachmentButton(button)
    return button
        and button.attachmentType ~= nil
        and button.ClipType ~= "ClipType"
        and button.AttackModeType ~= "WeaponAttackType"
        and button.SkinType ~= "Skin"
end

local function getInventoryPage(playerNum)
    local ui = getPlayerInventory and getPlayerInventory(playerNum) or nil
    return ui and ui.inventoryPane and ui.inventoryPane.inventoryPage or nil
end

local function getLootPage(playerNum)
    local ui = getPlayerLoot and getPlayerLoot(playerNum) or nil
    return ui and ui.inventoryPane and ui.inventoryPane.inventoryPage or nil
end

function Fix.refreshContainerPages(player)
    if not player then return end
    local playerNum = player:getPlayerNum()
    local inventoryPage = getInventoryPage(playerNum)
    local lootPage = getLootPage(playerNum)

    if inventoryPage and inventoryPage.refreshBackpacks then
        pcall(function() inventoryPage:refreshBackpacks() end)
    end
    if lootPage and lootPage.refreshBackpacks then
        pcall(function() lootPage:refreshBackpacks() end)
    end
end

function Fix.getAccessibleContainers(player, refresh)
    if not player or not ISInventoryPaneContextMenu or not ISInventoryPaneContextMenu.getContainers then
        return nil
    end
    if refresh then
        Fix.refreshContainerPages(player)
    end
    local ok, containers = pcall(ISInventoryPaneContextMenu.getContainers, player)
    if not ok then
        log("failed to collect accessible containers: " .. tostring(containers))
        return nil
    end
    return containers
end

function Fix.isContainerAccessible(player, container)
    if not player or not container then return false end
    if container == player:getInventory() then return true end

    local containers = Fix.getAccessibleContainers(player, true)
    return containers ~= nil and containers:contains(container)
end

local function isCompatiblePart(part, weapon, category)
    if not part or not weapon or not instanceof(part, "WeaponPart") then
        return false
    end
    if category and part:getPartType() ~= category then
        return false
    end
    local mountOn = part:getMountOn()
    return mountOn ~= nil and mountOn:contains(weapon:getFullType())
end

function Fix.collectWeaponParts(player, weapon, category, refresh)
    local result = {}
    local seenTypes = {}
    local containers = Fix.getAccessibleContainers(player, refresh)
    if not containers then return result end

    for containerIndex = 0, containers:size() - 1 do
        local container = containers:get(containerIndex)
        if container then
            local parts = container:getItemsFromCategory("WeaponPart")
            if parts then
                for i = 0, parts:size() - 1 do
                    local part = parts:get(i)
                    if isCompatiblePart(part, weapon, category) then
                        local key = part:getFullType()
                        if not seenTypes[key] then
                            seenTypes[key] = true
                            table.insert(result, part)
                        end
                    end
                end
            end
        end
    end

    return result
end

function Fix.getPartsSignature(player, weapon, category)
    local parts = Fix.collectWeaponParts(player, weapon, category, false)
    local ids = {}
    for i = 1, #parts do
        local part = parts[i]
        ids[#ids + 1] = tostring(part:getID()) .. ":" .. tostring(part:getFullType())
    end
    table.sort(ids)
    return table.concat(ids, "|")
end

MFSAttachmentApplyAction = MFSAttachmentApplyAction or ISBaseTimedAction:derive("MFSAttachmentApplyAction")

function MFSAttachmentApplyAction:isValid()
    if not self.character or not self.weapon or not self.part then
        return false
    end
    if self.character:getPrimaryHandItem() ~= self.weapon then
        return false
    end
    if not self.character:getInventory():containsID(self.part:getID()) then
        return false
    end
    return isCompatiblePart(self.part, self.weapon, self.part:getPartType())
end

function MFSAttachmentApplyAction:perform()
    local partType = self.part:getPartType()
    local installed = self.weapon:getWeaponPart(partType)

    if installed and installed ~= self.part then
        ISTimedActionQueue.add(ISRemoveWeaponUpgrade:new(self.character, self.weapon, partType, 1))
    end

    ISTimedActionQueue.add(ISUpgradeWeapon:new(self.character, self.weapon, self.part, 1))
    ISTimedActionQueue.add(ISEquipWeaponAction:new(
        self.character,
        self.weapon,
        1,
        true,
        self.weapon:isTwoHandWeapon()
    ))

    ISBaseTimedAction.perform(self)
end

function MFSAttachmentApplyAction:new(character, weapon, part)
    local o = ISBaseTimedAction.new(self, character)
    o.character = character
    o.weapon = weapon
    o.part = part
    o.maxTime = 0
    o.stopOnWalk = false
    o.stopOnRun = false
    return o
end

function Fix.queueInstall(player, weapon, part)
    if not player or not weapon or not part then return false end
    if player:getPrimaryHandItem() ~= weapon then return false end
    if not isCompatiblePart(part, weapon, part:getPartType()) then return false end

    local rootInventory = player:getInventory()
    local sourceContainer = part:getContainer()
    if not sourceContainer then return false end

    if sourceContainer ~= rootInventory then
        if not Fix.isContainerAccessible(player, sourceContainer) then
            log("attachment source is no longer accessible: " .. tostring(part:getFullType()))
            return false
        end
        ISTimedActionQueue.add(ISInventoryTransferAction:new(
            player,
            part,
            sourceContainer,
            rootInventory,
            nil
        ))
    end

    ISTimedActionQueue.add(MFSAttachmentApplyAction:new(player, weapon, part))
    return true
end

local function clearPaneElements(pane)
    if not pane or not pane.elements then return end
    for i = #pane.elements, 1, -1 do
        local element = pane.elements[i]
        if element then
            pcall(function() element:close() end)
            pcall(function() pane:removeChild(element) end)
        end
    end
    table.wipe(pane.elements)
end

local function addPartButton(pane, part, weapon, enabled, itemNum, rowCount)
    if math.fmod(itemNum, 5) == 0 then
        rowCount = rowCount + 1
    end
    local x = 2 + 41 * math.fmod(itemNum, 5)
    local y = 2 + 41 * rowCount
    local button = addAttachmentButton:new(x, y, 40, 40, part, weapon, enabled, "WeaponPart")
    table.insert(pane.elements, button)
    pane:addChild(button)
    button:bringToTop()
    return itemNum + 1, rowCount
end

function Fix.renderWeaponParts(pane)
    local player = getPlayer and getPlayer() or nil
    local weapon = player and player:getPrimaryHandItem() or nil
    if not player or not weapon or not weapon:IsWeapon() then
        return
    end

    clearPaneElements(pane)
    pane:setWidth(40 * 5 + 20)

    if riskyShowPotentialAttachment and not pane._mfsPotentialBase then
        pane._mfsPotentialBase = {}
        for fullType, value in pairs(pane.potentialAttachment or {}) do
            pane._mfsPotentialBase[fullType] = value
        end
    end

    local potential = {}
    if riskyShowPotentialAttachment then
        for fullType, value in pairs(pane._mfsPotentialBase or {}) do
            potential[fullType] = value
        end
    end

    local parts = Fix.collectWeaponParts(player, weapon, pane.category, false)
    local actualTypes = {}
    local itemNum = 0
    local rowCount = -1

    for i = 1, #parts do
        local part = parts[i]
        local fullType = part:getFullType()
        if not actualTypes[fullType] then
            actualTypes[fullType] = true
            potential[fullType] = nil
            itemNum, rowCount = addPartButton(pane, part, weapon, true, itemNum, rowCount)
        end
    end

    if riskyShowPotentialAttachment then
        for fullType, _ in pairs(potential) do
            local potentialPart = instanceItem(fullType)
            if isCompatiblePart(potentialPart, weapon, pane.category) then
                itemNum, rowCount = addPartButton(pane, potentialPart, weapon, false, itemNum, rowCount)
            end
        end
    end

    pane:setScrollHeight(42 * (rowCount + 1))
    if pane:getHeight() >= pane:getScrollHeight() then
        pane:setWidth(40 * 5 + 8)
    end

    pane._mfsPartsSignature = Fix.getPartsSignature(player, weapon, pane.category)
end

function Fix.closeCurrentPane()
    local pane = Fix.currentPane
    if not pane then return end
    pcall(function() pane:close() end)
    if Fix.currentPane == pane then
        Fix.currentPane = nil
    end
end

function Fix.openPane(button)
    if not button or not riskyInspectWindow or not selectAttachmentPane then
        return false
    end

    Fix.closeCurrentPane()
    local player = getPlayer and getPlayer() or nil
    if not player then return false end
    Fix.refreshContainerPages(player)

    local pane = selectAttachmentPane:new(
        riskyInspectWindow:getX() + button:getX() + 43,
        riskyInspectWindow:getY() + button:getY() - 3,
        button.attachmentType
    )
    pane:addToUIManager()
    pane:bringToTop()
    Fix.currentPane = pane
    Fix.renderWeaponParts(pane)
    return true
end

function Fix.patchSelectAttachmentPane()
    if not selectAttachmentPane then return false end

    if not Fix._originalPaneRenderInventory then
        Fix._originalPaneRenderInventory = selectAttachmentPane.renderInventory
    end
    if not Fix._originalPaneUpdate then
        Fix._originalPaneUpdate = selectAttachmentPane.update
    end
    if not Fix._originalPaneClose then
        Fix._originalPaneClose = selectAttachmentPane.close
    end

    function selectAttachmentPane:renderInventory()
        if self.ClipType == "ClipType" or self.AttackModeType == "WeaponAttackType" or self.SkinType == "Skin" then
            return Fix._originalPaneRenderInventory(self)
        end
        return Fix.renderWeaponParts(self)
    end

    function selectAttachmentPane:update()
        if self.ClipType == "ClipType" or self.AttackModeType == "WeaponAttackType" or self.SkinType == "Skin" then
            return Fix._originalPaneUpdate(self)
        end

        if not self:getIsVisible() then return end
        local player = getPlayer and getPlayer() or nil
        if not player or self.currentPrimaryItem ~= player:getPrimaryHandItem() or riskyInspectWindow == nil then
            self:close()
            return
        end

        local now = getTimestampMs()
        self._mfsNextRefresh = self._mfsNextRefresh or 0
        if now >= self._mfsNextRefresh then
            self._mfsNextRefresh = now + Fix.REFRESH_INTERVAL_MS
            local signature = Fix.getPartsSignature(player, self.currentPrimaryItem, self.category)
            if signature ~= self._mfsPartsSignature then
                self:renderInventory()
            end
        end
    end

    function selectAttachmentPane:close()
        local result = Fix._originalPaneClose(self)
        if Fix.currentPane == self then
            Fix.currentPane = nil
        end
        return result
    end

    selectAttachmentPane.__MFSAttachmentAccessFixPatched = true
    return true
end

function Fix.patchAttachmentButton()
    if not attachmentButton then return false end

    if not Fix._originalAttachmentMouseUp then
        Fix._originalAttachmentMouseUp = attachmentButton.onMouseUp
    end

    function attachmentButton:onMouseUp(x, y)
        if isNormalAttachmentButton(self) then
            self.pressed = false
            return Fix.openPane(self)
        end
        return Fix._originalAttachmentMouseUp(self, x, y)
    end

    function attachmentButton:onMouseDoubleClick(x, y)
        -- Double LMB must never remove a weapon attachment. LMB is reserved
        -- for opening the compatible-attachment selector, including occupied slots.
        return true
    end

    function attachmentButton:onRightMouseDown(x, y)
        if not isNormalAttachmentButton(self) or not self.slotItem then
            return false
        end

        local player = getPlayer and getPlayer() or nil
        local weapon = self.attachingTo
        if not player or not weapon or player:getPrimaryHandItem() ~= weapon then
            return false
        end

        local partType = self.slotItem:getPartType()
        if not partType or not weapon:getWeaponPart(partType) then
            return false
        end

        Fix.closeCurrentPane()
        ISTimedActionQueue.add(ISRemoveWeaponUpgrade:new(player, weapon, partType, 1))
        getSoundManager():PlayWorldSound("WeaponPartInsertSound", player:getSquare(), 0, 0, 0, false)
        return true
    end

    attachmentButton.__MFSAttachmentAccessFixPatched = true
    return true
end

function Fix.patchAddAttachmentButton()
    if not addAttachmentButton then return false end

    if not Fix._originalAddAttachmentMouseDown then
        Fix._originalAddAttachmentMouseDown = addAttachmentButton.onMouseDown
    end

    function addAttachmentButton:onMouseDown(x, y)
        if self.type ~= "WeaponPart" then
            return Fix._originalAddAttachmentMouseDown(self, x, y)
        end
        if not self.slotItem or not self.enabled then
            return false
        end

        local player = getPlayer and getPlayer() or nil
        if not player then return false end

        if Fix.queueInstall(player, self.attachingTo, self.slotItem) then
            Fix.closeCurrentPane()
            getSoundManager():PlayWorldSound("WeaponPartInsertSound", player:getSquare(), 0, 0, 0, false)
            return true
        end
        return false
    end

    addAttachmentButton.__MFSAttachmentAccessFixPatched = true
    return true
end

function Fix.install()
    local panePatched = Fix.patchSelectAttachmentPane()
    local slotPatched = Fix.patchAttachmentButton()
    local addPatched = Fix.patchAddAttachmentButton()

    if panePatched and slotPatched and addPatched and not Fix._installLogged then
        Fix._installLogged = true
        log("version " .. Fix.VERSION .. " installed")
    end

    return panePatched and slotPatched and addPatched
end

Fix.install()

if not Fix._eventsRegistered then
    Events.OnGameStart.Add(Fix.install)
    Events.OnCreatePlayer.Add(function() Fix.install() end)
    Fix._eventsRegistered = true
end
