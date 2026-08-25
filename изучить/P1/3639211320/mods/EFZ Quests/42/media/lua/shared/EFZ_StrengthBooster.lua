if not EFZ then
    EFZ = {}
end

local PerkXP = require "EFZ_PerkXP"

local function isIsoPlayer(obj)
    return obj ~= nil and instanceof(obj, "IsoPlayer")
end

local function resolvePlayer(obj)
    if isIsoPlayer(obj) then
        return obj
    end
    if type(obj) == "table" and isIsoPlayer(obj.player) then
        return obj.player
    end
    if type(getPlayer) == "function" then
        local p = getPlayer()
        if isIsoPlayer(p) then
            return p
        end
    end
    return nil
end

local function levelUpStrengthOnce(playerObj)
    if not playerObj or not Perks or not Perks.Strength then
        return false
    end

    return PerkXP.levelUpPerkKeepingProgress(playerObj, Perks.Strength)
end

-- Called by item script: `OnEat = Use_StrengthBooster`
function Use_StrengthBooster(item, player, percent)
    -- MP에서는 아이템 처리/스탯 반영이 서버 권한이므로, 클라 실행은 무시한다.
    if isClient and isClient() then
        return
    end

    local targetPlayer = resolvePlayer(player)
    if not targetPlayer then
        return
    end

    levelUpStrengthOnce(targetPlayer)
end


