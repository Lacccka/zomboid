require "TimedActions/ISBaseTimedAction"

-- Bare-handed fumble anim played for the flare_ignite sound duration.
local IGNITE_ANIM = "OpenBeerBottle"

ISIgniteFlareAction = ISBaseTimedAction:derive("ISIgniteFlareAction")

function ISIgniteFlareAction:isValid()
    return self.character:getInventory():containsID(self.item:getID())
        and self.item:getFullType() == "Explosives.Flare"
end

function ISIgniteFlareAction:start()
    -- Re-resolve by ID: the queued reference can go stale by complete(), especially in MP.
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

-- Swaps to Explosives.FlareBurning (see ExplosivesFlare.swapHeldItem for why a swap, not a modData flag).
-- Lives in shared/ so the server can run the replicated action too; ExplosivesFlare itself is
-- client-only, the server copy just needs to complete without erroring.
function ISIgniteFlareAction:complete()
    if ExplosivesFlare then
        self.item:getModData().ignitedAtGameHours = getGameTime():getWorldAgeHours()
        ExplosivesFlare.swapHeldItem(self.character, self.item, "Explosives.FlareBurning")
    end
    return true
end

-- Not perk/trait-scaled on purpose: stays locked to the flare_ignite.ogg clip length,
-- unlike normal timed actions (see adjustMaxTime override below).
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
