--[[---------------------------------------------------------------------------
    FixWeapon.lua (server)

    Authoritative multiplayer handler for the universal weapon repair kit.
    Item IDs are resolved only inside the requesting player's direct inventory;
    the server validates the held kit and damaged ranged weapon before changing
    condition or consuming anything.
-----------------------------------------------------------------------------]]

if isClient() then return end

local MODULE = "MFSRepairWeapon"
local COMMAND = "repair"
local REPAIR_KIT_TYPE = "Base.gongjvxiuli_cat"
local COMMAND_COOLDOWN_MS = 250
local nextRepairAt = {}

local function playerKey(player)
    local onlineID = -1
    pcall(function() onlineID = player:getOnlineID() end)
    return tostring(onlineID)
end

local function acceptCommand(player)
    local now = getTimestampMs()
    local key = playerKey(player)
    if now < (nextRepairAt[key] or 0) then return false end
    nextRepairAt[key] = now + COMMAND_COOLDOWN_MS
    return true
end

local function findDirectItem(inventory, itemID)
    itemID = tonumber(itemID)
    if not inventory or not itemID then return nil end
    return inventory:getItemById(itemID)
end

local function validateRepair(player, args)
    if not player or type(args) ~= "table" then
        return nil, nil, "invalid request"
    end

    local inventory = player:getInventory()
    local repairKit = findDirectItem(inventory, args.repairKitID)
    local weapon = findDirectItem(inventory, args.weaponID)

    if not repairKit or repairKit:getContainer() ~= inventory
        or repairKit:getFullType() ~= REPAIR_KIT_TYPE then
        return nil, nil, "repair kit is unavailable"
    end
    if player:getPrimaryHandItem() ~= repairKit then
        return nil, nil, "repair kit is not held in the primary hand"
    end
    if not weapon or weapon:getContainer() ~= inventory
        or not weapon:IsWeapon() or not weapon:isRanged() then
        return nil, nil, "weapon is unavailable or invalid"
    end
    if weapon:getConditionMax() <= 0
        or weapon:getCondition() >= weapon:getConditionMax() then
        return nil, nil, "weapon does not need repair"
    end

    return repairKit, weapon
end

local function repairWeapon(player, args)
    local repairKit, weapon, reason = validateRepair(player, args)
    if not repairKit then return false, reason end

    local inventory = player:getInventory()
    weapon:setCondition(weapon:getConditionMax())
    sendItemStats(weapon)

    player:removeFromHands(repairKit)
    inventory:Remove(repairKit)
    sendRemoveItemFromContainer(inventory, repairKit)

    -- Preserve the original feature's behavior of equipping the repaired gun.
    player:setPrimaryHandItem(weapon)
    if weapon:isTwoHandWeapon() then
        player:setSecondaryHandItem(weapon)
    end
    sendEquip(player)
    return true
end

local function onClientCommand(module, command, player, args)
    if module ~= MODULE or command ~= COMMAND then return end
    if not acceptCommand(player) then return end

    local ok, accepted, reason = pcall(repairWeapon, player, args)
    if not ok then
        print("[MFSRepairWeapon] repair failed: " .. tostring(accepted))
    elseif not accepted then
        print("[MFSRepairWeapon] repair rejected for " .. playerKey(player)
            .. ": " .. tostring(reason))
    end
end

Events.OnClientCommand.Add(onClientCommand)
print("[MFSRepairWeapon] server-authoritative repair handler loaded")

