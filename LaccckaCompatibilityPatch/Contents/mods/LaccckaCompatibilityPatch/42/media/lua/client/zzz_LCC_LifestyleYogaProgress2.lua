-- Lacccka B42.20 Compatibility Patch
-- Lifestyle: expose the hidden Yoga skill in the Wellness context tooltip.

local function lccYogaProgressText(player)
    if not player or not HiddenSkills or not HiddenSkills.getSkill then return nil end
    local skill = HiddenSkills.getSkill(player, "Yoga")
    if not skill then return nil end
    local level = tonumber(skill[1]) or 0
    local xp = tonumber(skill[2]) or 0
    local nextXP = tonumber(skill[3]) or 0
    if level >= 10 then return getText("LCC_YogaProgress_Max", level) end
    return getText("LCC_YogaProgress", level, xp, nextXP)
end

local function patchYogaTooltip()
    if not ZenWellnessContextMenu or not ZenWellnessContextMenu.Options or not ZenWellnessContextMenu.Options.Yoga or not ZenWellnessContextMenu.Options.Yoga.canPerform then return end
    if ZenWellnessContextMenu.Options.Yoga.__LCCProgressPatched then return end
    local originalCanPerform = ZenWellnessContextMenu.Options.Yoga.canPerform
    ZenWellnessContextMenu.Options.Yoga.canPerform = function(thisPlayer, tooltipText, tex)
        local notAvailable, text, texture = originalCanPerform(thisPlayer, tooltipText, tex)
        local progress = lccYogaProgressText(thisPlayer)
        if progress then text = progress .. " <LINE> " .. (text or "") end
        return notAvailable, text, texture
    end
    ZenWellnessContextMenu.Options.Yoga.__LCCProgressPatched = true
end

Events.OnGameStart.Add(patchYogaTooltip)
Events.OnCreatePlayer.Add(patchYogaTooltip)
