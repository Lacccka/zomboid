require "TimedActions/ISBaseTimedAction"
require "TimedActions/ISInventoryTransferAction"
require "ISUI/ISInventoryPaneContextMenu"

MFSAttachmentAccessFix = MFSAttachmentAccessFix or {}
local Fix = MFSAttachmentAccessFix

Fix.VERSION = "1.1.0"
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

local function isWeaponPart(item)
    return item and instanceof(item, "WeaponPart")
end

local function isStructurallyCompatible(part, player, weapon, expectedPartType)
    if not isWeaponPart(part) or not player or not weapon then return false end
    if part:isBroken() then return false end

    local partType = part:getPartType()
    if not partType or (expectedPartType and partType ~= expectedPartType) then
        return false
    end

    -- Prefer the current upstream compatibility predicate. Only fall back to
    -- mountOn when that API itself is unavailable/throws on an older mix.
    local ok, allowed = pcall(function() return part:canAttach(player, weapon) end)
    if ok then return allowed == true end

    local mountOn = part:getMountOn()
    return mountOn ~= nil and mountOn:contains(weapon:getFullType())
end

local function containerTreeContains(container, target, visited)
    if not container or not target then return false end
    if container == target then return true end

    visited = visited or {}
    local key = tostring(container)
    if visited[key] then return false end
    visited[key] = true

    local items = container:getItems()
    if not items then return false end

    for i = 0, items:size() - 1 do
        local item = items:get(i)
        if item and instanceof(item, "InventoryContainer") then
            local nested = item:getInventory()
            if nested and containerTreeContains(nested, target, visited) then
                return true
            end
        end
    end

    return false
end

local function anyRootContains(roots, target)
    if not roots or not target then return false end

    if type(roots) == "table" then
        for _, root in ipairs(roots) do
            if containerTreeContains(root, target, {}) then
                return true
            end
        end
        return false
    end

    local okSize, size = pcall(function() return roots:size() end)
    if not okSize or not size then return false end

    for i = 0, size - 1 do
        local root = roots:get(i)
        if containerTreeContains(root, target, {}) then
            return true
        end
    end

    return false
end

function Fix.isSourceReachable(player, sourceContainer)
    if not player or not sourceContainer then return false end
    if sourceContainer == player:getInventory() then return true end

    -- MFS 2026-09-02 owns nearby-container discovery. Reuse its roots so this
    -- guard cannot drift from the selector's definition of "reachable".
    if type(getReachableContainers) == "function" then
        local ok, roots = pcall(getReachableContainers, player)
        if ok and anyRootContains(roots, sourceContainer) then
            return true
        end
    end

    -- Compatibility fallback for older MFS/community-fix combinations.
    if ISInventoryPaneContextMenu and ISInventoryPaneContextMenu.getContainers then
        local ok, roots = pcall(ISInventoryPaneContextMenu.getContainers, player)
        if ok and anyRootContains(roots, sourceContainer) then
            return true
        end
    end

    return false
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
    if not isStructurallyCompatible(self.part, self.character, self.weapon, self.partType) then
        return false
    end

    -- CAS-style slot validation: never remove a part that changed after the
    -- selector was opened/clicked.
    local current = self.weapon:getWeaponPart(self.partType)
    if self.expectedInstalledId ~= nil then
        return current ~= nil and current:getID() == self.expectedInstalledId
    end
    return current == nil
end

function MFSAttachmentApplyAction:perform()
    local installed = self.weapon:getWeaponPart(self.partType)

    if installed and installed ~= self.part then
        ISTimedActionQueue.add(ISRemoveWeaponUpgrade:new(
            self.character,
            self.weapon,
            self.partType,
            1
        ))
    end

    -- Keep upstream actions authoritative. The updated MFS ISUpgradeWeapon
    -- refreshes attachment state, hand models, MP sync and the inspect preview.
    ISTimedActionQueue.add(ISUpgradeWeapon:new(
        self.character,
        self.weapon,
        self.part,
        1
    ))
    ISTimedActionQueue.add(ISEquipWeaponAction:new(
        self.character,
        self.weapon,
        1,
        true,
        self.weapon:isTwoHandWeapon()
    ))

    ISBaseTimedAction.perform(self)
end

function MFSAttachmentApplyAction:new(character, weapon, part, expectedInstalledId)
    local o = ISBaseTimedAction.new(self, character)
    o.character = character
    o.weapon = weapon
    o.part = part
    o.partType = part and part:getPartType() or nil
    o.expectedInstalledId = expectedInstalledId
    o.maxTime = 0
    o.stopOnWalk = false
    o.stopOnRun = false
    return o
end

function Fix.queueInstallOrReplace(player, weapon, part)
    if not player or not weapon or not part then return false end
    if player:getPrimaryHandItem() ~= weapon then return false end
    if not isStructurallyCompatible(part, player, weapon, part:getPartType()) then return false end

    local rootInventory = player:getInventory()
    local sourceContainer = part:getContainer()
    if not sourceContainer then return false end

    local partType = part:getPartType()
    local installed = weapon:getWeaponPart(partType)
    local expectedInstalledId = installed and installed:getID() or nil

    if sourceContainer ~= rootInventory then
        if not Fix.isSourceReachable(player, sourceContainer) then
            log("attachment source is no longer reachable: " .. tostring(part:getFullType()))
            return false
        end

        ISTimedActionQueue.add(ISInventoryTransferAction:new(
            player,
            part,
            sourceContainer,
            rootInventory
        ))
    end

    ISTimedActionQueue.add(MFSAttachmentApplyAction:new(
        player,
        weapon,
        part,
        expectedInstalledId
    ))
    return true
end

function Fix.closeCurrentPane()
    local pane = Fix.currentPane
    Fix.currentPane = nil
    if not pane then return end
    pcall(function() pane:close() end)
end

function Fix.openPane(button)
    if not isNormalAttachmentButton(button) or not riskyInspectWindow or not selectAttachmentPane then
        return false
    end

    local player = getPlayer and getPlayer() or nil
    if not player or player:getPrimaryHandItem() ~= button.attachingTo then
        return false
    end

    Fix.closeCurrentPane()

    -- Deliberately use the upstream pane unchanged. Since the 2026-09-02 MFS
    -- update it owns recursive bag/nearby-container discovery and magazine UI.
    local pane = selectAttachmentPane:new(
        riskyInspectWindow:getX() + button:getX() + 43,
        riskyInspectWindow:getY() + button:getY() - 3,
        button.attachmentType
    )
    pane:addToUIManager()
    pane:bringToTop()
    Fix.currentPane = pane
    return true
end

function Fix.patchAttachmentButton()
    if not attachmentButton then return false end

    if attachmentButton.onMouseUp ~= Fix._patchedAttachmentMouseUp then
        Fix._originalAttachmentMouseUp = attachmentButton.onMouseUp
        Fix._patchedAttachmentMouseUp = function(self, x, y)
            if isNormalAttachmentButton(self) then
                self.pressed = false
                return Fix.openPane(self)
            end
            if Fix._originalAttachmentMouseUp then
                return Fix._originalAttachmentMouseUp(self, x, y)
            end
            return false
        end
        attachmentButton.onMouseUp = Fix._patchedAttachmentMouseUp
    end

    if attachmentButton.onMouseDoubleClick ~= Fix._patchedAttachmentDoubleClick then
        Fix._originalAttachmentDoubleClick = attachmentButton.onMouseDoubleClick
        Fix._patchedAttachmentDoubleClick = function(self, x, y)
            if isNormalAttachmentButton(self) then
                -- LMB is selector-only for normal parts. Removal is explicit RMB.
                return true
            end
            if Fix._originalAttachmentDoubleClick then
                return Fix._originalAttachmentDoubleClick(self, x, y)
            end
            return false
        end
        attachmentButton.onMouseDoubleClick = Fix._patchedAttachmentDoubleClick
    end

    if attachmentButton.onRightMouseDown ~= Fix._patchedAttachmentRightMouseDown then
        Fix._originalAttachmentRightMouseDown = attachmentButton.onRightMouseDown
        Fix._patchedAttachmentRightMouseDown = function(self, x, y)
            if not isNormalAttachmentButton(self) then
                if Fix._originalAttachmentRightMouseDown then
                    return Fix._originalAttachmentRightMouseDown(self, x, y)
                end
                return false
            end

            local player = getPlayer and getPlayer() or nil
            local weapon = self.attachingTo
            if not player or not weapon or player:getPrimaryHandItem() ~= weapon then
                return false
            end

            local partType = self.slotItem and self.slotItem:getPartType() or self.attachmentType
            local installed = partType and weapon:getWeaponPart(partType) or nil
            if not installed then return false end

            Fix.closeCurrentPane()
            ISTimedActionQueue.add(ISRemoveWeaponUpgrade:new(player, weapon, partType, 1))
            getSoundManager():PlayWorldSound("WeaponPartInsertSound", player:getSquare(), 0, 0, 0, false)
            return true
        end
        attachmentButton.onRightMouseDown = Fix._patchedAttachmentRightMouseDown
    end

    return true
end

function Fix.patchAddAttachmentButton()
    if not addAttachmentButton then return false end

    if addAttachmentButton.onMouseDown ~= Fix._patchedAddAttachmentMouseDown then
        Fix._originalAddAttachmentMouseDown = addAttachmentButton.onMouseDown
        Fix._patchedAddAttachmentMouseDown = function(self, x, y)
            if self.type ~= "WeaponPart" then
                if Fix._originalAddAttachmentMouseDown then
                    return Fix._originalAddAttachmentMouseDown(self, x, y)
                end
                return false
            end

            if not self.slotItem or not self.enabled then return false end

            local player = getPlayer and getPlayer() or nil
            if not player then return false end

            if Fix.queueInstallOrReplace(player, self.attachingTo, self.slotItem) then
                Fix.closeCurrentPane()
                getSoundManager():PlayWorldSound("WeaponPartInsertSound", player:getSquare(), 0, 0, 0, false)
                return true
            end
            return false
        end
        addAttachmentButton.onMouseDown = Fix._patchedAddAttachmentMouseDown
    end

    return true
end

function Fix.install()
    local slotPatched = Fix.patchAttachmentButton()
    local addPatched = Fix.patchAddAttachmentButton()

    if slotPatched and addPatched and not Fix._installLogged then
        Fix._installLogged = true
        log("version " .. Fix.VERSION .. " installed; upstream selector retained")
    end

    return slotPatched and addPatched
end

Fix.install()

if not Fix._eventsRegistered then
    Events.OnGameStart.Add(Fix.install)
    Events.OnCreatePlayer.Add(function() Fix.install() end)
    Fix._eventsRegistered = true
end
