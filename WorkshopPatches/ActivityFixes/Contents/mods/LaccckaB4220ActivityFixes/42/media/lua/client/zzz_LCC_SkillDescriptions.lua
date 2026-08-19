-- Lacccka B42 Activity Fixes
-- Fix Build 42.20 skill-tooltip description lookup for translated perk names.
--
-- Vanilla ISSkillProgressBar.updateTooltip() builds description keys from
-- perk:getName(). In a translated UI getName() is already localized, so Russian
-- produces keys such as IGUI_perks_Искусство_Description instead of the stable
-- IGUI_perks_Art_Description. Keep the upstream tooltip logic intact, then
-- repair that lookup using Perk:getId().

local Guard = require "LCC/Guard"
local FEATURE = "ui.skill-descriptions"

Guard.safeRequire(FEATURE, "XpSystem/ISUI/ISSkillProgressBar")
if not Guard.isEnabled(FEATURE) then return end
if LCC_SkillDescriptionsInstalled then return end

Guard.install {
    id = FEATURE,
    validate = function()
        return type(ISSkillProgressBar) == "table" and type(ISSkillProgressBar.updateTooltip) == "function",
            "ISSkillProgressBar.updateTooltip is unavailable"
    end,
    install = function()
        local originalUpdateTooltip = ISSkillProgressBar.updateTooltip

        local DESCRIPTION_ALIASES = {
            Sneak = { "Sneaking" },
            PlantScavenging = { "Foraging" },
            Woodwork = { "Carpentry" },
            Farming = { "Agriculture" },
            Doctor = { "FirstAid" },
            Electricity = { "Electrical" },
            MetalWelding = { "Welding", "Metalworking" },
            Blacksmith = { "Blacksmithing" },
            FlintKnapping = { "Knapping" },
            Husbandry = { "AnimalCare" },
            Sprinting = { "Running" },
        }

        local function lccGetTextOrNull(key)
            if getTextOrNull then
                return getTextOrNull(key)
            end
            local value = getText(key)
            if value == key then return nil end
            return value
        end

        local function getPerkId(perk)
            if not perk or not perk.getId then return nil end
            local value = perk:getId()
            if value == nil then return nil end
            value = tostring(value)
            if value == "" then return nil end
            return value
        end

        local function getDescriptionCandidates(perk)
            local perkId = getPerkId(perk)
            if not perkId then return nil end

            local candidates = { perkId }
            local aliases = DESCRIPTION_ALIASES[perkId]
            if aliases then
                for i = 1, #aliases do
                    candidates[#candidates + 1] = aliases[i]
                end
            end
            return candidates
        end

        local function findDescription(candidates, suffix)
            if not candidates then return nil end
            for i = 1, #candidates do
                local text = lccGetTextOrNull("IGUI_perks_" .. candidates[i] .. suffix)
                if text and text ~= "" then
                    return text
                end
            end
            return nil
        end

        local function replacePlain(text, needle, replacement)
            if type(text) ~= "string" or type(needle) ~= "string" or needle == "" then
                return text, false
            end
            local first, last = string.find(text, needle, 1, true)
            if not first then return text, false end
            return string.sub(text, 1, first - 1) .. replacement .. string.sub(text, last + 1), true
        end

        local function appendUnique(message, text)
            if not text or text == "" then return message end
            if string.find(message, text, 1, true) then return message end
            return message .. " <LINE><LINE> " .. text
        end

        local function repairSkillDescription(self, lvlSelected)
            if not self or not self.perk or type(self.message) ~= "string" or self.message == "" then return end

            local candidates = getDescriptionCandidates(self.perk)
            if not candidates then return end

            local description = findDescription(candidates, "_Description")
            if description then
                local localizedName = self.perk:getName()
                if localizedName ~= nil then
                    local wrongKey = "IGUI_perks_" .. tostring(localizedName) .. "_Description"
                    local wrongTranslation = lccGetTextOrNull(wrongKey)

                    if not wrongTranslation or wrongTranslation == "" then
                        local repaired, replaced = replacePlain(self.message, wrongKey, description)
                        self.message = repaired
                        if not replaced then
                            self.message = appendUnique(self.message, description)
                        end
                    end
                end
            end

            local selected = tonumber(lvlSelected)
            if selected then
                local level = math.floor(selected) + 1
                if level >= 1 and level <= 10 then
                    local levelDescription = findDescription(candidates, "_Description" .. tostring(level))
                    self.message = appendUnique(self.message, levelDescription)
                end
            end
        end

        ISSkillProgressBar.updateTooltip = function(self, lvlSelected)
            local result = originalUpdateTooltip(self, lvlSelected)
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "repair translated skill description", repairSkillDescription, self, lvlSelected)
            end
            return result
        end

        LCC_SkillDescriptionsInstalled = true
    end,
}
