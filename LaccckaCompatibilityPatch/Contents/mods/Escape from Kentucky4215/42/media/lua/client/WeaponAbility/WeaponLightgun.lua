local function WeaponLightBeam()

    local attacker = getSpecificPlayer(0)
    local weapon = nil

    if attacker ~= nil then
        weapon = attacker:getPrimaryHandItem()
        if not weapon then
            return
        end
        if not weapon:IsWeapon() then
            return
        end
        if not weapon:isRanged() then
            return
        end
    end
    if attacker:isAiming() and attacker:getPrimaryHandItem() and weapon:getWeaponPart("Light") ~= nil then
        weapon:setTorchCone(true)
        weapon:setLightDistance(30)
        weapon:setLightStrength(9)
    end
    if not attacker:isAiming() and attacker:getPrimaryHandItem() and weapon:getWeaponPart("Light") ~= nil then
        weapon:setTorchCone(false)
        weapon:setLightDistance(0.0)
        weapon:setLightStrength(0.0)
    end
end

Events.OnPlayerUpdate.Add(WeaponLightBeam)
