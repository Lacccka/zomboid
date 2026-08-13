local function BayonetAttackZombie(playerObj, zombieObj, MainGun)

    if playerObj:getVariableBoolean("Bob_BayonetAttack") then
        local MaxDamage = 3
        local DamageKnife = ZombRandFloat(MaxDamage / 10, MaxDamage)
        zombieObj:setHealth(zombieObj:getHealth() - DamageKnife)
        playerObj:getEmitter():playSound("SpearCraftedHit")
        --zombieObj:playHurtSound()
        --zombieObj:addBlood(10)
        -- playerObj:Say(tostring(math.floor(DamageKnife * 10) / 10))
    end

end

Events.OnWeaponHitCharacter.Add(BayonetAttackZombie)

local function HasPart(weapon)
    for k, v in pairs(AWCWF_BayonetSet.Parts.Stool) do
        local part = weapon:getWeaponPart("Stool")
        if part ~= nil and k == part:getType() then
            return true
        end
    end
    return false
end

-- Made by SportXAI
local function DetectBayonet()
    if not isIngameState() then return end
    local playerObj = getSpecificPlayer(0)
    if playerObj == nil then return end
    
    local weapon = playerObj:getPrimaryHandItem()
    if weapon == nil then return end
    
    if weapon and weapon:IsWeapon() and weapon:isRanged() then
        if HasPart(weapon) then
            if not playerObj:getVariableBoolean("Bob_BayonetAttack") then
                playerObj:setVariable("Bob_BayonetAttack", true)
            end
            
            return
        end
    end
    playerObj:clearVariable("Bob_BayonetAttack")
end

Events.OnTick.Add(DetectBayonet)
