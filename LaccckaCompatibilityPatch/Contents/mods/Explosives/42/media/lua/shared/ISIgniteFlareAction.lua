require "TimedActions/ISBaseTimedAction"

-- Bare-handed fumble animation played for the duration of the
-- flare_ignite sound, instead of the player just standing still.
local IGNITE_ANIM = "OpenBeerBottle"

ISIgniteFlareAction = ISBaseTimedAction:derive("ISIgniteFlareAction")

function ISIgniteFlareAction:isValid()
    return self.character:getInventory():containsID(self.item:getID())
        and self.item:getFullType() == "Explosives.Flare"
end

function ISIgniteFlareAction:start()
    -- Re-resolve by ID -- the reference captured when the action was
    -- queued can go stale by the time :complete() runs, especially in MP.
    if isClient() and self.item then
        self.item = self.character:getInventory():getItemById(self.item:getID())
    end
    self:setActionAnim(IGNITE_ANIM)
    if isClient() then
        self.sound = self.character:playSound("flare_ignite")
    end
end

function ISIgniteFlareAction:stop()
    if isClient() then
        self.character:stopOrTriggerSound(self.sound)
    end
    ISBaseTimedAction.stop(self)
end

function ISIgniteFlareAction:perform()
    if isClient() then
        self.character:stopOrTriggerSound(self.sound)
    end
    ISBaseTimedAction.perform(self)
end

-- Swaps the unlit item to Explosives.FlareBurning -- see
-- ExplosivesFlare.swapHeldItem in FlareHandler.lua for why a swap
-- instead of a modData flag (no in-place item-type change in PZ).
-- This class lives in shared/ (not client/) so the server can
-- reconstruct and run the replicated action too, but ExplosivesFlare
-- (light sources, sparkles, sounds) only exists client-side -- the
-- server's own copy of this action just needs to complete without
-- erroring; the authoritative item swap itself goes through the
-- SwapHeldFlare client->server command inside swapHeldItem.
function ISIgniteFlareAction:complete()
    if ExplosivesFlare then
        self.item:getModData().ignitedAtGameHours = getGameTime():getWorldAgeHours()
        ExplosivesFlare.swapHeldItem(self.character, self.item, "Explosives.FlareBurning")
    end
    return true
end

-- Not perk/trait-scaled on purpose (see adjustMaxTime override below) --
-- this needs to stay locked to the flare_ignite.ogg clip length, not
-- speed up/slow down with mood or action-speed modifiers like vanilla
-- timed actions normally do.
function ISIgniteFlareAction:getDuration()
    return 60 -- ticks; tune in-game against the ~3s flare_ignite.ogg length
end

function ISIgniteFlareAction:adjustMaxTime(maxTime)
    return maxTime
end

function ISIgniteFlareAction:new(character, item)
    local o = ISBaseTimedAction.new(self, character)
    o.item = item
    o.stopOnWalk = false
    o.stopOnRun = false
    o.maxTime = o:getDuration()
    return o
end
