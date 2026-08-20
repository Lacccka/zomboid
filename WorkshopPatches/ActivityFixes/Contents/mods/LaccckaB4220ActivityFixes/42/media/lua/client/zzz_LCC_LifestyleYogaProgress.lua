-- Lacccka B42.20 Compatibility Patch
-- Lifestyle: expose the hidden Yoga skill in the normal Skills panel.
--
-- Yoga remains authoritative in Lifestyle's HiddenSkills/LSHiddenSkills storage.
-- The perk declared by this patch is only a UI proxy, so existing saves and
-- Lifestyle's own progression logic are not migrated or duplicated.
--
-- IMPORTANT: keep this Lua source ASCII-only. Localized text is resolved via
-- Translator keys to avoid Build 42.20 Windows mojibake from Cyrillic literals.

local Guard = require "LCC/Guard"
local FEATURE = "lifestyle.yoga-progress-ui"
local PATCH_VERSION = "1.1.7"

Guard.safeRequire(FEATURE, "XpSystem/ISUI/ISSkillProgressBar")
Guard.safeRequire(FEATURE, "Helper/HSMng")
if not Guard.isEnabled(FEATURE) then return end
if LCC_LifestyleYogaProgressInstalled then return end

Guard.install {
    id = FEATURE,
    validate = function()
        if type(ISSkillProgressBar) ~= "table" or type(ISSkillProgressBar.new) ~= "function" then
            return false, "ISSkillProgressBar.new is unavailable"
        end
        if type(ISSkillProgressBar.derive) ~= "function" then
            return false, "ISSkillProgressBar.derive is unavailable"
        end
        if type(HiddenSkills) ~= "table" or type(HiddenSkills.getSkill) ~= "function" then
            return false, "Lifestyle HiddenSkills.getSkill is unavailable"
        end
        return true
    end,
    install = function()
        local FONT_HGT_SMALL = getTextManager():getFontHeight(UIFont.Small)
        local SKILL_POINT_HGT = math.floor((FONT_HGT_SMALL + 6) / 2)
        local SKILL_POINT_SPACING = getCore():getOptionFontSizeReal()

        local FILLED_R = 1.00
        local FILLED_G = 0.89
        local FILLED_B = 0.38

        local originalNew = ISSkillProgressBar.new
        local originalRenderPerkRect = ISSkillProgressBar.renderPerkRect
        local originalUpdateTooltip = ISSkillProgressBar.updateTooltip

        local function clamp(value, minValue, maxValue)
            if value < minValue then return minValue end
            if value > maxValue then return maxValue end
            return value
        end

        local function lccGetTextOrNull(key)
            if not key then return nil end
            if getTextOrNull then
                return getTextOrNull(key)
            end
            local value = getText(key)
            if value == key then return nil end
            return value
        end

        local function getLanguageCode()
            if Translator and Translator.getLanguage then
                local okLanguage, language = pcall(function()
                    return Translator.getLanguage()
                end)
                if okLanguage and language then
                    local okName, name = pcall(function()
                        return language:name()
                    end)
                    if okName and name ~= nil and tostring(name) ~= "" then
                        return tostring(name)
                    end
                    return tostring(language)
                end
            end
            return nil
        end

        local function isRussianUI()
            local code = getLanguageCode()
            if not code then return false end
            local normalized = string.lower(tostring(code))
            return normalized == "ru"
                or normalized == "russian"
                or string.find(normalized, "russian", 1, true) ~= nil
                or string.find(normalized, "ru_", 1, true) ~= nil
                or string.find(normalized, "ru-", 1, true) ~= nil
        end

        local russianUI = isRussianUI()
        local languageCode = getLanguageCode() or "?"

        local function getYogaName()
            return lccGetTextOrNull("IGUI_perks_Yoga")
                or lccGetTextOrNull("UI_LSHS_Yoga")
                or "Yoga"
        end

        local function getYogaDescription()
            if russianUI then
                return lccGetTextOrNull("Farming_LCC_Skill_Yoga_Description")
            end
            return lccGetTextOrNull("IGUI_perks_Yoga_Description")
                or lccGetTextOrNull("Tooltip_Yoga_Option")
        end

        local function isYogaPerk(perk)
            if not perk then return false end
            if Perks and Perks.Yoga and perk.getType and perk:getType() == Perks.Yoga then return true end
            if perk.getId and tostring(perk:getId()) == "Yoga" then return true end
            if perk.getName then
                local translatedYoga = lccGetTextOrNull("IGUI_perks_Yoga") or lccGetTextOrNull("UI_LSHS_Yoga")
                if translatedYoga and tostring(perk:getName()) == tostring(translatedYoga) then
                    return true
                end
            end
            return false
        end

        LCCYogaSkillProgressBar = ISSkillProgressBar:derive("LCCYogaSkillProgressBar")

        local function getHiddenYogaSkill(character)
            if not character then return nil end
            local ok, skill = Guard.protect(
                FEATURE,
                "Lifestyle HiddenSkills.getSkill",
                HiddenSkills.getSkill,
                character,
                "Yoga"
            )
            if not ok then return nil end
            return skill
        end

        local function syncHiddenYoga(self)
            local skill = getHiddenYogaSkill(self.char)
            if not skill then
                self.level = 0
                self.xp = 0
                self.xpForLvl = 100
                self._lccYogaInitialized = true
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

            if self._lccYogaInitialized and oldLevel ~= self.level and self.parent then
                self.parent.lastLeveledUpPerk = self.perk
                self.parent.lastLevelUpTime = 1
            end
            self._lccYogaInitialized = true
        end

        function LCCYogaSkillProgressBar:syncHiddenYoga()
            if not Guard.isEnabled(FEATURE) then return end
            Guard.protect(FEATURE, "sync Yoga progress", syncHiddenYoga, self)
        end

        local function renderYoga(self)
            syncHiddenYoga(self)

            local x = 0
            local y = 0

            for _ = 0, self.level - 1 do
                self:drawTextureScaled(self.SkillUnitFilled, x, y, SKILL_POINT_HGT, SKILL_POINT_HGT, 1, FILLED_R, FILLED_G, FILLED_B)
                self:drawTextureScaled(self.SkillUnitBorder, x, y, SKILL_POINT_HGT, SKILL_POINT_HGT, 1, FILLED_R, FILLED_G, FILLED_B)
                x = x + SKILL_POINT_HGT + SKILL_POINT_SPACING
            end

            if self.level < 10 then
                local percentProgress = clamp((self.xp / self.xpForLvl) * 100, 0, 100)
                local sliceWidth = SKILL_POINT_HGT / 100

                self:drawTextureScaled(self.SkillUnitBorder, x, y, SKILL_POINT_HGT, SKILL_POINT_HGT, 1, 0.4, 0.4, 0.4)
                if percentProgress > 0 then
                    self:drawTextureScaled(self.SkillUnitFilled, x, y, sliceWidth * percentProgress, SKILL_POINT_HGT, 1, 0.4, 0.4, 0.4)
                end
                x = x + SKILL_POINT_HGT + SKILL_POINT_SPACING
            end

            for _ = self.level + 1, 9 do
                self:drawTextureScaled(self.SkillUnitBorder, x, y, SKILL_POINT_HGT, SKILL_POINT_HGT, 1, 0.2, 0.2, 0.2)
                x = x + SKILL_POINT_HGT + SKILL_POINT_SPACING
            end
        end

        function LCCYogaSkillProgressBar:renderPerkRect()
            if Guard.isEnabled(FEATURE) then
                local ok = Guard.protect(FEATURE, "render Yoga progress", renderYoga, self)
                if ok then return end
            end
            if originalRenderPerkRect then
                return originalRenderPerkRect(self)
            end
        end

        local function updateYogaTooltip(self, lvlSelected)
            syncHiddenYoga(self)

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

        function LCCYogaSkillProgressBar:updateTooltip(lvlSelected)
            if Guard.isEnabled(FEATURE) then
                local ok = Guard.protect(FEATURE, "update Yoga tooltip", updateYogaTooltip, self, lvlSelected)
                if ok then return end
            end
            if originalUpdateTooltip then
                return originalUpdateTooltip(self, lvlSelected)
            end
        end

        -- Hidden Yoga levels automatically; never call LevelPerk() on the UI proxy.
        function LCCYogaSkillProgressBar:onMouseUp(x, y)
        end

        local function newYogaProgressBar(x, y, width, height, playerNum, perk, parent)
            local o = originalNew(ISSkillProgressBar, x, y, width, height, playerNum, perk, parent)
            setmetatable(o, LCCYogaSkillProgressBar)
            LCCYogaSkillProgressBar.__index = LCCYogaSkillProgressBar
            o._lccYogaInitialized = false
            syncHiddenYoga(o)
            return o
        end

        ISSkillProgressBar.new = function(self, x, y, width, height, playerNum, perk, parent)
            if Guard.isEnabled(FEATURE) then
                local identified, yoga = Guard.protect(FEATURE, "identify Yoga perk", isYogaPerk, perk)
                if identified and yoga then
                    local ok, progressBar = Guard.protect(
                        FEATURE,
                        "create Yoga progress bar",
                        newYogaProgressBar,
                        x,
                        y,
                        width,
                        height,
                        playerNum,
                        perk,
                        parent
                    )
                    if ok and progressBar and Guard.isEnabled(FEATURE) then
                        return progressBar
                    end
                end
            end
            return originalNew(self, x, y, width, height, playerNum, perk, parent)
        end

        LCC_LifestyleYogaProgressInstalled = true
        print("[LCC][YogaProgress][" .. PATCH_VERSION .. "] installed language=" .. tostring(languageCode) .. " russian=" .. tostring(russianUI))
    end,
}
