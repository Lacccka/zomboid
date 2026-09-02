--[[---------------------------------------------------------------------------
    FixWeapon.lua (client)

    Displays the repair-kit context option and runs the repair animation. In
    multiplayer the client requests the repair but never changes inventory or
    condition itself; the matching server file owns those changes.
-----------------------------------------------------------------------------]]

require "TimedActions/ISBaseTimedAction"
require "TimedActions/ISInventoryTransferAction"

local MODULE = "MFSRepairWeapon"
local COMMAND = "repair"
local REPAIR_KIT_TYPE = "Base.gongjvxiuli_cat"

MFSRepairWeaponAction = ISBaseTimedAction:derive("MFSRepairWeaponAction")

local function isRepairKit(item)
    return item and item:getFullType() == REPAIR_KIT_TYPE
end

local function isRepairableWeapon(item)
    return item and item:IsWeapon() and item:isRanged()
        and item:getConditionMax() > 0
        and item:getCondition() < item:getConditionMax()
end

local function itemsAreReady(character, repairKit, weapon)
    if not character or not repairKit or not weapon then return false end
    local inventory = character:getInventory()
    return character:getPrimaryHandItem() == repairKit
        and repairKit:getContainer() == inventory
        and weapon:getContainer() == inventory
        and isRepairKit(repairKit)
        and isRepairableWeapon(weapon)
end

local function applySoloRepair(character, repairKit, weapon)
    if not itemsAreReady(character, repairKit, weapon) then return false end

    weapon:setCondition(weapon:getConditionMax())
    character:removeFromHands(repairKit)
    character:getInventory():Remove(repairKit)
    character:setPrimaryHandItem(weapon)
    if weapon:isTwoHandWeapon() then
        character:setSecondaryHandItem(weapon)
    end
    return true
end

function MFSRepairWeaponAction:isValid()
    return itemsAreReady(self.character, self.repairKit, self.weapon)
end

function MFSRepairWeaponAction:update()
end

function MFSRepairWeaponAction:start()
    self.character:playSound("FlushingToilet")
    self:setActionAnim("RemoveGrass")
    self:setOverrideHandModels(nil, nil)
end

function MFSRepairWeaponAction:stop()
    ISBaseTimedAction.stop(self)
end

function MFSRepairWeaponAction:perform()
    if isClient() then
        sendClientCommand(self.character, MODULE, COMMAND, {
            repairKitID = self.repairKit:getID(),
            weaponID = self.weapon:getID(),
        })
    else
        -- Single-player has no remote server command path.
        applySoloRepair(self.character, self.repairKit, self.weapon)
    end

    ISBaseTimedAction.perform(self)
end

function MFSRepairWeaponAction:new(character, repairKit, weapon)
    local o = ISBaseTimedAction.new(self, character)
    o.repairKit = repairKit
    o.weapon = weapon
    o.stopOnWalk = false
    o.stopOnRun = true
    o.maxTime = 350
    return o
end

local function queueRepair(character, repairKit, weapon)
    local inventory = character:getInventory()
    if weapon:getContainer() ~= inventory then
        ISTimedActionQueue.add(ISInventoryTransferAction:new(character, weapon,
            weapon:getContainer(), inventory))
    end
    ISTimedActionQueue.add(MFSRepairWeaponAction:new(character, repairKit, weapon))
end

local function addRepairOption(playerIndex, context, items)
    local character = getSpecificPlayer(playerIndex)
    if not character then return end

    local repairKit = character:getPrimaryHandItem()
    if not isRepairKit(repairKit) then return end

    local seen = {}
    for _, entry in ipairs(items) do
        if instanceof(entry, "InventoryItem") then
            seen[entry] = true
        elseif entry.items then
            for _, item in ipairs(entry.items) do
                seen[item] = true
            end
        end
    end

    for weapon in pairs(seen) do
        if isRepairableWeapon(weapon) then
            context:addOption(getText("IGUI_FixItem"), character, queueRepair,
                repairKit, weapon)
            return
        end
    end
end

Events.OnFillInventoryObjectContextMenu.Add(addRepairOption)

