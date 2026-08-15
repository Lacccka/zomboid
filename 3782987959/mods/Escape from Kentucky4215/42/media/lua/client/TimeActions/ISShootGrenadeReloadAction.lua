require "TimedActions/ISBaseTimedAction"
require "Grenade_Tajectory_core"
ISShootGrenadeReload = ISBaseTimedAction:derive("ISShootGrenadeReload");

function ISShootGrenadeReload:isValid()
    if not string.match(self.weaopon:getWeaponPart("Stool"):getType(), "_empty") then
       self:forceStop()
       return false
    end

    return true
end

function ISShootGrenadeReload:update()
    if not string.match(self.weaopon:getWeaponPart("Stool"):getType(), "_empty") then
       self:forceStop()
       return
    end
end

function ISShootGrenadeReload:start()
    if not string.match(self.weaopon:getWeaponPart("Stool"):getType(), "_empty") then
       self:forceStop()
       return
    end

    self.character:playSound(self.GrenadelauncherType.LoadSound)

end

function ISShootGrenadeReload:stop()
    ISBaseTimedAction.stop(self);
end

function ISShootGrenadeReload:perform()
    ISBaseTimedAction.perform(self);
    local MagPart = instanceItem(self.GrenadelauncherType.LoadType)
    self.weaopon:setWeaponPart("Stool", MagPart)
    local inv = self.character:getInventory()

    inv:RemoveOneOf(self.GrenadelauncherType.AmmoType)

end

function ISShootGrenadeReload:new(character, time, weapon, GrenadelauncherType)
    local o = {}
    setmetatable(o, self)
    self.__index = self
    o.weaopon = weapon
    o.GrenadelauncherType = GrenadelauncherType
    o.character = character;
    o.stopOnWalk = false;
    o.stopOnRun = true;
    o.maxTime = time;

    return o;
end
