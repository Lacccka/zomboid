-- PackFlow mass-transfer pacing for Build 42 MP.
--
-- B42's ISInventoryTransferAction switches to server ItemTransaction duration on
-- clients (maxTime = -1). For container-to-container consolidation that network
-- duration can be far shorter than the normal inventory handling animation,
-- making a whole mass transfer look like teleportation.
--
-- Keep the authoritative vanilla transaction and completion callback intact, but
-- give PackFlow-owned transfer actions a normal positive timed-action duration.
-- The server transaction may finish first; forceComplete is then deferred until
-- the timed-action progress reaches the end. Reject/cancel still uses vanilla
-- forceStop immediately, so this never turns a failed transaction into success.

require "TimedActions/ISInventoryTransferAction"

local GridMassSort = require("LCC/GridMassSort")

if not ISInventoryTransferAction._lccPackFlowPacingPatched then
    ISInventoryTransferAction._lccPackFlowPacingPatched = true

    local MIN_VISUAL_TIME = 60 -- one visible action-second at normal 60-tick pacing

    local originalUpdate = ISInventoryTransferAction.update
    local originalForceComplete = ISInventoryTransferAction.forceComplete

    local function playerNumFor(action)
        local character = action and action.character or nil
        if character and character.getPlayerNum then
            return character:getPlayerNum()
        end
        return 0
    end

    local function isPackFlowTransfer(action)
        if not action then return false end
        local state = GridMassSort.active[playerNumFor(action)]
        local waiting = state and state.waitingMove or nil
        return waiting and waiting.action == action or false
    end

    local function safeContainerIsInCharacterInventory(container, character)
        if not container or not character or not container.isInCharacterInventory then return false end
        local ok, result = pcall(function()
            return container:isInCharacterInventory(character)
        end)
        return ok and result or false
    end

    -- Mirrors the non-MP duration calculation in vanilla
    -- ISInventoryTransferAction:new(), before B42 replaces client maxTime with -1.
    -- A small floor keeps very light duplicate items visibly handled instead of
    -- collapsing into a sub-second network transaction.
    local function calculateVisualTime(action)
        local character = action.character
        local item = action.item
        local source = action.srcContainer
        local destination = action.destContainer
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
                -- Vanilla has no additional multiplier for unpack -> floor here.
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

        -- PackFlow never relocates favorites outside character inventory, but retain
        -- vanilla semantics if that invariant changes later.
        if item.isFavorite and item:isFavorite() and not destinationOnCharacter then
            return 1
        end

        return math.max(MIN_VISUAL_TIME, math.max(1, math.floor(maxTime + 0.5)))
    end

    function ISInventoryTransferAction:update()
        if not self._lccPackFlowPacing and isPackFlowTransfer(self) then
            self._lccPackFlowPacing = true
            self._lccPackFlowVisualTime = calculateVisualTime(self)

            -- Do this before vanilla update(): maxTime no longer equals -1, so the
            -- short ItemTransaction duration does not replace our visual action time.
            self.maxTime = self._lccPackFlowVisualTime
            if self.action and self.action.setTime then
                self.action:setTime(self.maxTime)
            end
        end

        return originalUpdate(self)
    end

    function ISInventoryTransferAction:forceComplete()
        if self._lccPackFlowPacing and isPackFlowTransfer(self) and self.action then
            local delta = self.action.getJobDelta and self.action:getJobDelta() or 1
            if delta < 0.999 then
                -- The authoritative MP transaction is already complete, but keep the
                -- normal timed action/animation running. Vanilla update() will call
                -- forceComplete again while transactionDone remains true.
                self._lccPackFlowTransactionDone = true
                return
            end
        end

        return originalForceComplete(self)
    end

    print("[LCC GridSort] mass transfer physical pacing installed")
end
