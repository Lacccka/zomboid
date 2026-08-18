-- Lacccka B42.20 Compatibility Patch
-- Lifestyle: expose the hidden Yoga skill in the normal Skills panel.
--
-- Yoga remains authoritative in Lifestyle's HiddenSkills/LSHiddenSkills storage.
-- The perk declared by this patch is only a UI proxy, so existing saves and
-- Lifestyle's own progression logic are not migrated or duplicated.

require "XpSystem/ISUI/ISSkillProgressBar"
require "Helper/HSMng"

if LCC_LifestyleYogaProgressInstalled then return end
if not ISSkillProgressBar or not HiddenSkills then return end
LCC_LifestyleYogaProgressInstalled = true

local FONT_HGT_SMALL = getTextManager():getFontHeight(UIFont.Small)
local SKILL_POINT_HGT = math.floor((FONT_HGT_SMALL + 6) / 2)
local SKILL_POINT_SPACING = getCore():getOptionFontSizeReal()

local FILLED_R = 1.00
local FILLED_G = 0.89
local FILLED_B = 0.38

local originalNew = ISSkillProgressBar.new

LCCYogaSkillProgressBar = ISSkillProgressBar:derive("LCCYogaSkillProgressBar")

local function isYogaPerk(perk)
    if not perk then return false end
    if Perks and Perks.Yoga and perk.getType and perk:getType() == Perks.Yoga then return true end
    if perk.getName then
        local name = perk:getName()
        return name == "Yoga" or name == "Йога"
    end
    return false
end

local function clamp(value, minValue, maxValue)
    if value < minValue then return minValue end
    if value > maxValue then return maxValue end
    return value
end

local function lccGetTextOrNull(key)
    if getTextOrNull then
        return getTextOrNull(key)
    end
    local value = getText(key)
    if value == key then return nil end
    return value
end

local function getYogaName()
    return lccGetTextOrNull("IGUI_perks_Yoga")
        or lccGetTextOrNull("UI_LSHS_Yoga")
        or "Yoga"
end

local function getYogaDescription()
    return lccGetTextOrNull("IGUI_perks_Yoga_Description")
        or lccGetTextOrNull("Tooltip_Yoga_Option")
end

function LCCYogaSkillProgressBar:syncHiddenYoga()
    local skill = HiddenSkills.getSkill(self.char, "Yoga")
    if not skill then
        self.level = 0
        self.xp = 0
        self.xpForLvl = 100
        return
    end

    local oldLevel = self.level or 0
    self.level = clamp(math.floor(tonumber(skill[1]) or 0), 0, 10)
    self.xp = math.max(0, tonumber(skill[2]) or 0)
    self.xpForLvl = math.max(1, tonumber(skill[3]) or 100)

    if self.level >= 10 then
        self.xp = 0
        self.xpForLvl = 1
    elseif self.xp > self.xpForLvl then
        self.xp = self.xpForLvl
    end

    if oldLevel ~= self.level and self.parent then
        self.parent.lastLeveledUpPerk = self.perk
        self.parent.lastLevelUpTime = 1
    end
end

function LCCYogaSkillProgressBar:renderPerkRect()
    self:syncHiddenYoga()

    local x = 0
    local y = 0

    -- Completed levels.
    for _ = 0, self.level - 1 do
        self:drawTextureScaled(self.SkillUnitFilled, x, y, SKILL_POINT_HGT, SKILL_POINT_HGT, 1, FILLED_R, FILLED_G, FILLED_B)
        self:drawTextureScaled(self.SkillUnitBorder, x, y, SKILL_POINT_HGT, SKILL_POINT_HGT, 1, FILLED_R, FILLED_G, FILLED_B)
        x = x + SKILL_POINT_HGT + SKILL_POINT_SPACING
    end

    -- Current level progress.
    if self.level < 10 then
        local percentProgress = clamp((self.xp / self.xpForLvl) * 100, 0, 100)
        local sliceWidth = SKILL_POINT_HGT / 100

        self:drawTextureScaled(self.SkillUnitBorder, x, y, SKILL_POINT_HGT, SKILL_POINT_HGT, 1, 0.4, 0.4, 0.4)
        if percentProgress > 0 then
            self:drawTextureScaled(self.SkillUnitFilled, x, y, sliceWidth * percentProgress, SKILL_POINT_HGT, 1, 0.4, 0.4, 0.4)
        end
        x = x + SKILL_POINT_HGT + SKILL_POINT_SPACING
    end

    -- Locked levels.
    for _ = self.level + 1, 9 do
        self:drawTextureScaled(self.SkillUnitBorder, x, y, SKILL_POINT_HGT, SKILL_POINT_HGT, 1, 0.2, 0.2, 0.2)
        x = x + SKILL_POINT_HGT + SKILL_POINT_SPACING
    end
end

function LCCYogaSkillProgressBar:updateTooltip(lvlSelected)
    self:syncHiddenYoga()

    lvlSelected = clamp(math.floor(tonumber(lvlSelected) or self.level), 0, 9)
    self.message = getYogaName() .. " " .. xpSystemText.lvl .. " " .. tostring(lvlSelected + 1)

    if lvlSelected < self.level then
        self.message = self.message .. " <LINE> " .. xpSystemText.unlocked
    elseif lvlSelected == self.level then
        if self.level >= 10 then
            self.message = self.message .. " <LINE> " .. xpSystemText.unlocked
        else
            self.message = self.message .. " <LINE> " .. getText("IGUI_XP_tooltipxp", round(self.xp, 2), self.xpForLvl)
        end
    else
        self.message = self.message .. " <LINE> " .. xpSystemText.locked
    end

    local description = getYogaDescription()
    if description and description ~= "" then
        self.message = self.message .. " <LINE><LINE> " .. description
    end

    local levelDescription = lccGetTextOrNull("IGUI_perks_Yoga_Description" .. tostring(lvlSelected + 1))
    if levelDescription and levelDescription ~= "" then
        self.message = self.message .. " <LINE><LINE> " .. levelDescription
    end
end

-- Hidden Yoga levels automatically; clicking the proxy bar must never call
-- LevelPerk() on the UI-only Perks.Yoga entry.
function LCCYogaSkillProgressBar:onMouseUp(x, y)
end

local function newYogaProgressBar(x, y, width, height, playerNum, perk, parent)
    local o = originalNew(ISSkillProgressBar, x, y, width, height, playerNum, perk, parent)
    setmetatable(o, LCCYogaSkillProgressBar)
    LCCYogaSkillProgressBar.__index = LCCYogaSkillProgressBar
    o:syncHiddenYoga()
    return o
end

ISSkillProgressBar.new = function(self, x, y, width, height, playerNum, perk, parent)
    if isYogaPerk(perk) then
        return newYogaProgressBar(x, y, width, height, playerNum, perk, parent)
    end
    return originalNew(self, x, y, width, height, playerNum, perk, parent)
end

print("[LaccckaCompatibilityPatch] Lifestyle Yoga progress UI installed")
