require "TimedActions/ISBaseTimedAction"
require "TimedActions/ISReloadWeaponAction"
require "MFSUnderbarrelRegistry"

-- Historical filename/class retained for compatibility. Despite the M203 name,
-- this action is generic and resolves every supported pseudo through
-- MFSUnderbarrelRegistry; do not clone it for GP25/M28.

-- Cosmetic underbarrel breech/case cycle. Vanilla RackAfterShoot is deliberately not
-- used: on a chamberless one-round weapon ISRackFirearm can return the fired
-- live grenade to inventory. Vanilla still owns and synchronizes ammunition;
-- this action only supplies the required post-shot handling time and motion.

MFSM203CycleAction = ISBaseTimedAction:derive("MFSM203CycleAction")

function MFSM203CycleAction:isValid()
    local valid = self.gun
        and MFSUnderbarrelRegistry.getForPseudo(self.gun) == self.definition
        and self.character:getInventory():contains(self.gun)
        and self.character:getPrimaryHandItem() == self.gun
    if not valid and self.gun then
        self.gun:getModData().MFSUnderbarrelCycleQueued = nil
    end
    return valid
end

function MFSM203CycleAction:start()
    self.gun:getModData().MFSUnderbarrelCycleQueued = true
    self:setAnimVariable("WeaponReloadType", "boltactionnomag")
    self:setAnimVariable("isRacking", true)
    self:setAnimVariable("RackAiming", false)
    self:setOverrideHandModels(self.gun, nil)
    self:setActionAnim(CharacterActionAnims.Reload)
    ISReloadWeaponAction.setReloadSpeed(self.character, true)
    self.character:reportEvent("EventReloading")
    self.character:getEmitter():playSound(self.definition.cycleSound)
end

function MFSM203CycleAction:update()
end

function MFSM203CycleAction:clearState()
    if self.gun then
        self.gun:getModData().MFSUnderbarrelCycleQueued = nil
    end
    self.character:clearVariable("isRacking")
    self.character:clearVariable("WeaponReloadType")
    self.character:clearVariable("RackAiming")
end

function MFSM203CycleAction:stop()
    self:clearState()
    ISBaseTimedAction.stop(self)
end

function MFSM203CycleAction:perform()
    self:clearState()
    ISBaseTimedAction.perform(self)
end

function MFSM203CycleAction:complete()
    return true
end

function MFSM203CycleAction:getDuration()
    return self.definition.cycleDuration
end

function MFSM203CycleAction:new(character, gun, definition)
    local action = ISBaseTimedAction.new(self, character)
    action.gun = gun
    action.definition = definition
    action.stopOnWalk = false
    action.stopOnRun = true
    action.stopOnAim = false
    action.useProgressBar = false
    action.maxTime = action:getDuration()
    return action
end

local function queueUnderbarrelCycle(playerObj, weapon)
    if not playerObj or playerObj ~= getPlayer() or not weapon then return end
    local definition = MFSUnderbarrelRegistry.getForPseudo(weapon)
    if not definition then return end
    local launchedAt = tonumber(weapon:getModData().MFSUnderbarrelLaunchedAt)
    if not launchedAt or getTimestampMs() - launchedAt > 2000 then return end
    weapon:getModData().MFSUnderbarrelLaunchedAt = nil
    if weapon:getModData().MFSUnderbarrelCycleQueued then return end

    weapon:getModData().MFSUnderbarrelCycleQueued = true
    ISTimedActionQueue.add(MFSM203CycleAction:new(playerObj, weapon, definition))
end

MFSPerformanceSafety = MFSPerformanceSafety or {}
if MFSPerformanceSafety.m203CycleAttackFinished then
    Events.OnPlayerAttackFinished.Remove(MFSPerformanceSafety.m203CycleAttackFinished)
end
if MFSPerformanceSafety.underbarrelCycleAttackFinished then
    Events.OnPlayerAttackFinished.Remove(MFSPerformanceSafety.underbarrelCycleAttackFinished)
end
MFSPerformanceSafety.m203CycleAttackFinished = nil
MFSPerformanceSafety.underbarrelCycleAttackFinished = queueUnderbarrelCycle
Events.OnPlayerAttackFinished.Add(queueUnderbarrelCycle)

print("[MFSUnderbarrelCycle] registered launcher post-shot cycle loaded")
