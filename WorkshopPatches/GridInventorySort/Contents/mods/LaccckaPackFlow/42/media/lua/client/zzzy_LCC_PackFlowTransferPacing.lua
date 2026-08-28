-- PackFlow mass-transfer pacing for Build 42 MP.
--
-- B42 clients create ISInventoryTransferAction with maxTime = -1 and later use
-- ItemTransaction duration supplied by the server. For container-to-container
-- consolidation that duration can be much shorter than the normal inventory
-- handling animation, making a physical move look like teleportation.
--
-- The important ordering rule is: pacing must be attached BEFORE the first
-- transaction update can complete. ISTimedActionQueue may start an action while
-- add() is still on the stack, so waiting until GridMassSort stores waitingMove or
-- until the first update() is racy. Instead we identify PackFlow by its unique
-- completion-callback arguments inside ISInventoryTransferAction:start(), set a
-- positive duration there, and only then let vanilla create the MP transaction.
--
-- The server transaction remains authoritative. If it finishes before the visual
-- action, forceComplete is deferred until timed-action progress reaches the end.
-- Reject/cancel still goes through vanilla forceStop immediately.

require "TimedActions/ISInventoryTransferAction"

local GridMassSort = require("LCC/GridMassSort")

local PATCH_VERSION = 2
local MIN_VISUAL_TIME = 60

local function safeContainerIsInCharacterInventory(container, character)
    if not container or not character or not container.isInCharacterInventory then return false end
    local ok, result = pcall(function()
        return container:isInCharacterInventory(character)
    end)
    return ok and result or false
end

-- Mirrors the non-MP duration calculation in vanilla
-- ISInventoryTransferAction:new(), before B42 replaces client maxTime with -1.
local function calculateVisualTime(action)
    local character = action and action.character or nil
    local item = action and action.item or nil
    local source = action and action.srcContainer or nil
    local destination = action and action.destContainer or nil
    if not character or not item or not source or not destination then
        return MIN_VISUAL_TIME
    end

    if destination.getType and source.getType
        and (destination:getType() == "TradeUI" or source:getType() == "TradeUI") then
        return 1
    end

    local maxTime = 120
    local capacityDelta = 1.0
    local playerInventory = character.getInventory and character:getInventory() or nil
    local sourceOnCharacter = safeContainerIsInCharacterInventory(source, character)
    local destinationOnCharacter = safeContainerIsInCharacterInventory(destination, character)

    if playerInventory and source == playerInventory then
        if destinationOnCharacter then
            if destination.getCapacityWeight and destination.getMaxWeight then
                local maxWeight = tonumber(destination:getMaxWeight()) or 0
                if maxWeight > 0 then
                    capacityDelta = (tonumber(destination:getCapacityWeight()) or 0) / maxWeight
                end
            end
        else
            maxTime = 50
        end
    elseif not sourceOnCharacter and destinationOnCharacter then
        maxTime = 50
    end

    if capacityDelta < 0.4 then capacityDelta = 0.4 end

    if item.getActualWeight then
        local weight = tonumber(item:getActualWeight()) or 1
        if weight > 3 then weight = 3 end
        if weight < 0 then weight = 0 end
        maxTime = maxTime * weight * capacityDelta
    end

    if getCore and getCore():getGameMode() == "LastStand" then
        maxTime = maxTime * 0.3
    end

    if destination.getType and destination:getType() == "floor" then
        if playerInventory and source == playerInventory then
            maxTime = maxTime * 0.1
        elseif sourceOnCharacter then
            -- Vanilla has no extra multiplier for unpack -> floor here.
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

    if item.isFavorite and item:isFavorite() and not destinationOnCharacter then
        return 1
    end

    return math.max(MIN_VISUAL_TIME, math.max(1, math.floor(maxTime + 0.5)))
end

-- queueMove installs onTransferComplete(playerNum, operationId, itemId) before
-- ISTimedActionQueue.add(). Those arguments let us identify only PackFlow-owned
-- transfer actions without modifying every inventory transfer in the game.
local function packFlowContext(action)
    if not action or not action.onCompleteFunc or not action.onCompleteArgs then return nil, nil end

    local args = action.onCompleteArgs
    local playerNum = args[1]
    local operationId = args[2]
    local itemId = args[3]
    if playerNum == nil or operationId == nil or itemId == nil then return nil, nil end

    local state = GridMassSort.active[playerNum]
    if not state or state.operationId ~= operationId then return nil, nil end

    local actionItemId = action.item and action.item.getID and action.item:getID() or nil
    if actionItemId ~= itemId then return nil, nil end

    for _, move in ipairs(state.moves or {}) do
        if move.itemId == itemId then return state, itemId end
    end
    return nil, nil
end

if ISInventoryTransferAction._lccPackFlowPacingPatchVersion ~= PATCH_VERSION then
    local previousStart = ISInventoryTransferAction.start
    local previousForceComplete = ISInventoryTransferAction.forceComplete

    function ISInventoryTransferAction:start()
        local state, itemId = packFlowContext(self)
        if state and not self._lccPackFlowPacingV2 then
            local baseTime = calculateVisualTime(self)
            local adjustedTime = baseTime
            if self.adjustMaxTime then
                local ok, value = pcall(function() return self:adjustMaxTime(baseTime) end)
                if ok and tonumber(value) then adjustedTime = tonumber(value) end
            end

            adjustedTime = math.max(1, math.floor(adjustedTime + 0.5))
            self._lccPackFlowPacing = true
            self._lccPackFlowPacingV2 = true
            self._lccPackFlowVisualTime = adjustedTime
            self.maxTime = adjustedTime
            if self.action and self.action.setTime then
                self.action:setTime(adjustedTime)
            end

            print("[LCC GridSort] mass transfer paced item " .. tostring(itemId)
                .. ": base=" .. tostring(baseTime)
                .. " adjusted=" .. tostring(adjustedTime))
        end

        return previousStart(self)
    end

    function ISInventoryTransferAction:forceComplete()
        if self._lccPackFlowPacingV2 and self.action then
            local delta = self.action.getJobDelta and self.action:getJobDelta() or 1
            if delta < 0.999 then
                -- Transaction already succeeded, but the physical timed action has
                -- not finished. update() will ask again while transactionDone stays
                -- true, so simply defer completion without touching server state.
                self._lccPackFlowTransactionDone = true
                return
            end
        end

        return previousForceComplete(self)
    end

    ISInventoryTransferAction._lccPackFlowPacingPatchVersion = PATCH_VERSION
    print("[LCC GridSort] mass transfer physical pacing v2 installed")
end
