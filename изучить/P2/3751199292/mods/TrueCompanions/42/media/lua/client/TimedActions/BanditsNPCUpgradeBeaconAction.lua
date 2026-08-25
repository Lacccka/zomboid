--
-- True Companions - "work on the beacon" timed action
--
-- Upgrading used to happen the instant you clicked the button: the sprites changed
-- under your cursor with no walk, no animation and no time passing, which reads as a
-- menu option rather than as building something.
--
-- This is a plain ISBaseTimedAction so it behaves like every other construction job in
-- the game -- it can be cancelled, it is interrupted by combat, it plays the hammering
-- animation and it costs real time. The upgrade itself only happens in perform(), so
-- walking away half way through changes nothing and consumes nothing.
--
-- The MATERIALS ARE CHECKED TWICE ON PURPOSE: once before starting, so the action is
-- never begun for an upgrade that cannot be paid for, and again in perform(), because
-- the player can drop or use the parts during the several seconds this takes.
--

require "TimedActions/ISBaseTimedAction"

BanditsNPCUpgradeBeaconAction = ISBaseTimedAction:derive("BanditsNPCUpgradeBeaconAction")

function BanditsNPCUpgradeBeaconAction:isValid()
    -- The beacon can be destroyed mid-swing.
    return self.beacon ~= nil and BanditsNPC.Beacon.IsBeacon(self.beacon)
end

function BanditsNPCUpgradeBeaconAction:waitToStart()
    self.character:faceThisObject(self.beacon)
    return self.character:shouldBeTurning()
end

function BanditsNPCUpgradeBeaconAction:update()
    self.character:faceThisObject(self.beacon)
    self.character:setMetabolicTarget(Metabolics.HeavyWork)
end

function BanditsNPCUpgradeBeaconAction:start()
    self:setActionAnim("Loot")
    self.character:SetVariable("LootPosition", "Mid")
    self:setOverrideHandModels(nil, nil)
end

function BanditsNPCUpgradeBeaconAction:stop()
    ISBaseTimedAction.stop(self)
end

function BanditsNPCUpgradeBeaconAction:perform()
    -- Re-checked here, not just at the start: several seconds have passed and the
    -- player may no longer have the parts.
    BanditsNPC.Beacon.ApplyUpgrade(self.beacon, self.character)
    ISBaseTimedAction.perform(self)
end

function BanditsNPCUpgradeBeaconAction:new(character, beacon, time)
    local o = ISBaseTimedAction.new(self, character)
    o.beacon = beacon
    o.stopOnWalk = true
    o.stopOnRun = true
    o.maxTime = time or 300
    if character:isTimedActionInstant() then o.maxTime = 1 end
    return o
end
