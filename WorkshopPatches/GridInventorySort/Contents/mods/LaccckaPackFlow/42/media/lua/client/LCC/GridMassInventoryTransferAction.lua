require "TimedActions/ISInventoryTransferAction"

-- PackFlow-only subclass of the vanilla B42 inventory transfer action.
--
-- B42 MP sets waitForFinished=true and completes the Lua timed action from the
-- authoritative ItemTransaction ACK. ISInventoryTransferAction:forceComplete()
-- immediately stops the Loot animation, so very light / low-latency transfers
-- can become visually indistinguishable from teleportation.
--
-- We do NOT delay transaction creation and do NOT replace inventory mutation.
-- The normal vanilla/server transaction starts immediately. We only keep the
-- existing vanilla Loot/TransferItemOnSelf animation alive until a small visual
-- floor has elapsed. Server durations longer than this floor are untouched.

local LCCMassInventoryTransferAction = ISInventoryTransferAction:derive("LCCMassInventoryTransferAction")

local MIN_VISUAL_MS = 1000
local MIN_VISUAL_TICKS = math.ceil(MIN_VISUAL_MS / 20)

local function nowMs()
    return getTimestampMs and getTimestampMs() or (getTimeInMillis and getTimeInMillis() or 0)
end

local function itemId(self)
    return self.item and self.item.getID and self.item:getID() or "?"
end

local function shouldUseVisualFloor(self)
    if not isClient() then return false end
    if not self.character or not self.item then return false end
    if self.character.isTimedActionInstant and self.character:isTimedActionInstant() then return false end
    if self.dontAdd then return false end
    if self.isAlreadyTransferred and self:isAlreadyTransferred(self.item) then return false end
    return true
end

function LCCMassInventoryTransferAction:start()
    if shouldUseVisualFloor(self) then
        self._lccVisualStartedAt = nowMs()
        self._lccVisualUntil = self._lccVisualStartedAt + MIN_VISUAL_MS
        self._lccVisualFloorTicks = MIN_VISUAL_TICKS
    else
        self._lccVisualStartedAt = nil
        self._lccVisualUntil = nil
        self._lccVisualFloorTicks = nil
    end

    ISInventoryTransferAction.start(self)
end

function LCCMassInventoryTransferAction:update()
    -- The server sends the authoritative expected transaction duration. Keep
    -- vanilla completion semantics, but make jobDelta/animation progress use at
    -- least our visual floor instead of reaching 100% after a 100-300ms move.
    if isClient() and self.transactionId and self.action and self._lccVisualFloorTicks then
        local duration = getItemTransactionDuration(self.transactionId)
        if duration and duration > 0 then
            local displayTime = math.max(duration, self._lccVisualFloorTicks)
            if self.maxTime ~= displayTime then
                self.maxTime = displayTime
                self.action:setTime(displayTime)
            end
            if not self._lccDurationLogged then
                self._lccDurationLogged = true
                print("[LCC GridSort] mass transfer timing item=" .. tostring(itemId(self))
                    .. " serverTicks=" .. tostring(duration)
                    .. " visualTicks=" .. tostring(displayTime))
            end
        elseif self.maxTime == -1 then
            self.maxTime = self._lccVisualFloorTicks
            self.action:setTime(self.maxTime)
        end
    end

    ISInventoryTransferAction.update(self)
end

function LCCMassInventoryTransferAction:forceComplete()
    -- This method is invoked by the normal vanilla update() when the MP
    -- ItemTransaction becomes Done. Java BaseAction.finished() cannot help here
    -- because waitForFinished=true disables maxTime completion entirely. Hold
    -- only the *visual completion* until the floor; the server transaction has
    -- already remained authoritative throughout.
    local now = nowMs()
    if self._lccVisualUntil and now < self._lccVisualUntil then
        self._lccServerDoneBeforeVisualFloor = true
        if not self._lccFloorLogged then
            self._lccFloorLogged = true
            local elapsed = self._lccVisualStartedAt and (now - self._lccVisualStartedAt) or -1
            print("[LCC GridSort] mass transfer visual floor holding item="
                .. tostring(itemId(self))
                .. " elapsedMs=" .. tostring(elapsed)
                .. " remainingMs=" .. tostring(self._lccVisualUntil - now))
        end
        return
    end

    ISInventoryTransferAction.forceComplete(self)
end

function LCCMassInventoryTransferAction:new(character, item, srcContainer, destContainer, time)
    local o = ISInventoryTransferAction.new(self, character, item, srcContainer, destContainer, time)
    return o
end

LCCMassInventoryTransferAction.MIN_VISUAL_MS = MIN_VISUAL_MS
LCCMassInventoryTransferAction.MIN_VISUAL_TICKS = MIN_VISUAL_TICKS

return LCCMassInventoryTransferAction
