if not EFZ then
    EFZ = {}
end

require "TimedActions/ISTimedActionQueue"
require "TimedActions/EFZ_ReadSkillBookAction"
require "ISUI/ISInventoryPane"

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

local function getPerkLevelSafe(playerObj, perk)
    if not playerObj or not perk or not playerObj.getPerkLevel then
        return nil
    end
    local ok, result = pcall(function()
        return playerObj:getPerkLevel(perk)
    end)
    if not ok then
        return nil
    end
    return tonumber(result)
end

local function levelUpAimingOnce(playerObj)
    if not playerObj then
        return false
    end

    if not Perks or not Perks.Aiming then
        return false
    end

    local perk = Perks.Aiming
    local before = getPerkLevelSafe(playerObj, perk) or 0
    if before >= 10 then
        return false
    end

    if playerObj.LevelPerk then
        local ok = pcall(function()
            playerObj:LevelPerk(perk)
        end)
        if ok then
            local after = getPerkLevelSafe(playerObj, perk)
            if after and after > before then
                return true
            end
        end
    end

    local xp = playerObj.getXp and playerObj:getXp() or nil

    if xp and xp.setXPToLevel then
        local ok = pcall(function()
            xp:setXPToLevel(perk, before + 1)
        end)
        if ok then
            local after = getPerkLevelSafe(playerObj, perk)
            if after and after > before then
                return true
            end
        end
    end

    if xp and xp.AddXP then
        local maxSteps = 100000
        for _ = 1, maxSteps do
            xp:AddXP(perk, 1)
            local after = getPerkLevelSafe(playerObj, perk)
            if after and after > before then
                return true
            end
            if after and after >= 10 then
                return true
            end
        end
    end

    return false
end

function Use_AimingBook(item, player, percent)
    local targetPlayer = resolvePlayer(player)
    if not targetPlayer or not item then
        return
    end

    local modData = item.getModData and item:getModData() or nil
    if modData and modData.EFZ_AimingBookUsed == true then
        return
    end

    local before = getPerkLevelSafe(targetPlayer, Perks and Perks.Aiming) or 0
    if before >= 10 then
        return
    end

    -- B42+: perk 적용/아이템 소비는 TimedAction.complete()(서버)에서 처리
    ISTimedActionQueue.add(EFZ_ReadSkillBookAction:new(targetPlayer, item))
end

local function onFillInventoryObjectContextMenu(playerNum, context, items)
    if not context or not items then
        return
    end

    local playerObj = getSpecificPlayer(playerNum)
    if not playerObj or playerObj:isDead() then
        return
    end

    local actualItems = ISInventoryPane.getActualItems(items)
    for _, item in ipairs(actualItems) do
        if item and item.getFullType and item:getFullType() == "EFZ.AimingBook" then
            context:addOption(getText("ContextMenu_Use_AimingBook"), item, function(it)
                Use_AimingBook(it, playerObj, 1.0)
            end)
            break
        end
    end
end

Events.OnFillInventoryObjectContextMenu.Add(onFillInventoryObjectContextMenu)


