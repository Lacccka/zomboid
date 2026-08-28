require "TimedActions/ISBaseTimedAction"

-- Local pre-transfer action used only by PackFlow mass consolidation.
--
-- B42 multiplayer inventory transfers are server-authoritative. The real
-- ISInventoryTransferAction sets waitForFinished=true and the engine performs it
-- immediately when the ItemTransaction ACK calls forceComplete(), regardless of
-- the action's maxTime. That makes a healthy low-latency server visually move
-- many items almost instantly.
--
-- This action restores the physical Loot/TransferItemOnSelf phase BEFORE the
-- network transaction is created. It never moves an InventoryItem itself. The
-- ordinary vanilla ISInventoryTransferAction is queued directly after this one
-- and remains the only action allowed to create/commit the MP transaction.

local LCCMassTransferVisualAction = ISBaseTimedAction:derive("LCCMassTransferVisualAction")

local function isInCharacterInventory(container, character)
    if not container or not character or not container.isInCharacterInventory then return false end
    local ok, result = pcall(function() return container:isInCharacterInventory(character) end)
    return ok and result or false
end

-- Mirrors the non-MP timing portion of ISInventoryTransferAction:new().
-- ISBaseTimedAction:create() will subsequently apply the normal unhappy/drunk/
-- hand-pain/temperature modifiers through adjustMaxTime().
local function calculatePhysicalTime(character, item, srcContainer, destContainer)
    if not character or not item or not srcContainer or not destContainer then return 0 end
    if (destContainer.getType and destContainer:getType() == "TradeUI")
        or (srcContainer.getType and srcContainer:getType() == "TradeUI") then
        return 0
    end

    local maxTime = 120
    local destCapacityDelta = 1.0
    local rootInventory = character.getInventory and character:getInventory() or nil
    local srcInCharacter = isInCharacterInventory(srcContainer, character)
    local destInCharacter = isInCharacterInventory(destContainer, character)

    if srcContainer == rootInventory then
        if destInCharacter then
            local maxWeight = destContainer.getMaxWeight and tonumber(destContainer:getMaxWeight()) or 0
            local capacityWeight = destContainer.getCapacityWeight and tonumber(destContainer:getCapacityWeight()) or 0
            if maxWeight and maxWeight > 0 then
                destCapacityDelta = capacityWeight / maxWeight
            end
        else
            maxTime = 50
        end
    elseif not srcInCharacter and destInCharacter then
        maxTime = 50
    end

    if destCapacityDelta < 0.4 then destCapacityDelta = 0.4 end

    local weight = item.getActualWeight and tonumber(item:getActualWeight()) or 1.0
    if not weight or weight < 0 then weight = 1.0 end
    if weight > 3 then weight = 3 end
    maxTime = maxTime * weight * destCapacityDelta

    if getCore and getCore():getGameMode() == "LastStand" then
        maxTime = maxTime * 0.3
    end

    if destContainer.getType and destContainer:getType() == "floor" then
        if srcContainer == rootInventory then
            maxTime = maxTime * 0.1
        elseif srcInCharacter then
            -- Vanilla leaves unpack -> floor at the calculated value.
        else
            maxTime = maxTime * 0.2
        end
    end

    if character.hasTrait and CharacterTrait then
        if character:hasTrait(CharacterTrait.DEXTROUS) then
            maxTime = maxTime * 0.5
        end
        if character:hasTrait(CharacterTrait.ALL_THUMBS)
            or (character.isWearingAwkwardGloves and character:isWearingAwkwardGloves()) then
            maxTime = maxTime * 2.0
        end
    end

    if character.isTimedActionInstant and character:isTimedActionInstant() then
        return 1
    end

    if item.isFavorite and item:isFavorite() and not destInCharacter then
        return 0
    end

    return math.max(1, math.floor(maxTime + 0.5))
end

local function doLootAnimation(self, container)
    if not container then return end

    if not self.stopOnWalk
        and (self.character:isPlayerMoving() or self.character:pressedMovement(false)) then
        self:setActionAnim("DropWhileMoving")
        return
    end

    self:setActionAnim("Loot")
    self:setAnimVariable("LootPosition", "")
    self:setOverrideHandModels(nil, nil)
    self.character:clearVariable("LootPosition")

    if container.getContainerPosition and container:getContainerPosition() then
        self:setAnimVariable("LootPosition", container:getContainerPosition())
    end
    if container.getType and container:getType() == "freezer"
        and container.getFreezerPosition and container:getFreezerPosition() then
        self:setAnimVariable("LootPosition", container:getFreezerPosition())
    end

    local parent = container.getParent and container:getParent() or nil
    if (parent and instanceof and instanceof(parent, "IsoDeadBody"))
        or (container.getType and container:getType() == "floor") then
        self:setAnimVariable("LootPosition", "Low")
    end

    local containingItem = container.getContainingItem and container:getContainingItem() or nil
    if containingItem and containingItem.getWorldItem and containingItem:getWorldItem() then
        self:setAnimVariable("LootPosition", "Low")
    end

    self.character:reportEvent("EventLootItem")
end

local function startPhysicalAnimation(self)
    local character = self.character
    local srcContainer = self.srcContainer
    local destContainer = self.destContainer
    local rootInventory = character:getInventory()
    local srcInCharacter = isInCharacterInventory(srcContainer, character)
    local destInCharacter = isInCharacterInventory(destContainer, character)

    self.item:setJobType(getText("IGUI_MovingToContainer"))

    if srcContainer == rootInventory then
        if destInCharacter then
            self.item:setJobType(getText("IGUI_Packing"))
            self:setActionAnim("TransferItemOnSelf")
            return
        end
        self.item:setJobType(getText("IGUI_PuttingInContainer"))
        self.animContainer = destContainer
        doLootAnimation(self, destContainer)
        return
    end

    if srcInCharacter then
        if destContainer == rootInventory then
            self.item:setJobType(getText("IGUI_Unpacking"))
            self:setActionAnim("TransferItemOnSelf")
            return
        end
        self.item:setJobType(getText("IGUI_TakingFromContainer"))
        self.animContainer = destContainer
        doLootAnimation(self, destContainer)
        return
    end

    if destInCharacter then
        self.item:setJobType(getText("IGUI_TakingFromContainer"))
        self.animContainer = srcContainer
        doLootAnimation(self, srcContainer)
    elseif srcContainer.getType and srcContainer:getType() == "floor" then
        self.animContainer = srcContainer
        doLootAnimation(self, srcContainer)
    else
        self.animContainer = destContainer
        doLootAnimation(self, destContainer)
    end
end

function LCCMassTransferVisualAction:isValid()
    if not self.character or not self.item or not self.srcContainer or not self.destContainer then return false end
    if self.srcContainer == self.destContainer then return false end
    if self.item.isFavorite and self.item:isFavorite() then return false end
    if self.item.getContainer and self.item:getContainer() ~= self.srcContainer then return false end
    if self.srcContainer.contains then
        local ok, contained = pcall(function() return self.srcContainer:contains(self.item) end)
        if not ok or not contained then return false end
    end
    return true
end

function LCCMassTransferVisualAction:update()
    if self.item then self.item:setJobDelta(self:getJobDelta()) end
    if self.character and self.character.setMetabolicTarget and Metabolics then
        self.character:setMetabolicTarget(Metabolics.LightWork)
    end

    local container = self.animContainer
    local parent = container and container.getParent and container:getParent() or nil
    if parent and self.character and self.character.faceThisObject
        and not self.character:isSittingOnFurniture() then
        self.character:faceThisObject(parent)
    end
end

function LCCMassTransferVisualAction:start()
    startPhysicalAnimation(self)
end

function LCCMassTransferVisualAction:stop()
    if self.item then self.item:setJobDelta(0.0) end
    ISBaseTimedAction.stop(self)
end

function LCCMassTransferVisualAction:perform()
    if self.item then self.item:setJobDelta(0.0) end
    ISBaseTimedAction.perform(self)
end

function LCCMassTransferVisualAction:new(character, item, srcContainer, destContainer)
    local o = ISBaseTimedAction.new(self, character)
    o.item = item
    o.srcContainer = srcContainer
    o.destContainer = destContainer
    o.maxTime = calculatePhysicalTime(character, item, srcContainer, destContainer)

    local srcInCharacter = isInCharacterInventory(srcContainer, character)
    local destInCharacter = isInCharacterInventory(destContainer, character)
    o.stopOnWalk = not destInCharacter or not srcInCharacter
    if srcContainer == character:getInventory()
        and destContainer.getType and destContainer:getType() == "floor" then
        o.stopOnWalk = false
    end
    o.stopOnRun = true
    o.stopOnAim = false
    return o
end

return LCCMassTransferVisualAction
