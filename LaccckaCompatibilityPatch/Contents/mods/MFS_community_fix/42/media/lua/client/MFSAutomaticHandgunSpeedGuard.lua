-- Build 42.20 can leave singleShootSpeed as NaN after a melee swing. The
-- invalid character animation variable prevents MFS automatic-handgun attacks
-- from reaching AttackAnim/OnWeaponSwingHitPoint. Keep this repair narrow so
-- rifles, shotguns, melee weapons, Single mode, and an unset engine-defaulted
-- value remain untouched.

local VARIABLE = "singleShootSpeed"
local SAFE_SPEED = 1.0

local function isAutomaticRangedHandgun(item)
    return item
        and item.IsWeapon and item:IsWeapon()
        and item.isRanged and item:isRanged()
        and item.getSwingAnim and item:getSwingAnim() == "Handgun"
        and item.getFireMode and item:getFireMode() == "Auto"
end

local function repairInvalidShootSpeed(playerObj)
    if not playerObj or not playerObj.isLocalPlayer or not playerObj:isLocalPlayer() then return end

    local weapon = playerObj:getPrimaryHandItem()
    if not isAutomaticRangedHandgun(weapon) then return end

    -- Do not create the variable when it is genuinely absent: Auto handguns
    -- work normally with the engine default in that state.
    local raw = playerObj:getVariableString(VARIABLE)
    if raw == nil or raw == "" then return end

    local value = tonumber(raw)
    if value and value == value and value > 0 then return end

    playerObj:setVariable(VARIABLE, SAFE_SPEED)
    print("[MFS AUTO HANDGUN GUARD] repaired " .. VARIABLE
        .. " previous=" .. tostring(raw)
        .. " weapon=" .. tostring(weapon:getFullType()))
end

Events.OnPlayerUpdate.Add(repairInvalidShootSpeed)

