-- Lacccka B42.20 Compatibility Patch
-- Restore player-facing perk descriptions in the B42.20 Skills tooltip.
--
-- Translation text remains authoritative. This wrapper only appends an
-- IGUI_perks_<perk>_Description string after the normal progress/lock text.

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

        -- Some vanilla perk IDs and player-facing translation keys use
        -- different historical names. Direct ID lookup is always attempted
        -- first; these aliases only cover the known mismatches.
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

        local function appendUnique(message, text)
            if not text or text == "" then return message end
            if string.find(message, text, 1, true) then return message end
            return message .. " <LINE><LINE> " .. text
        end

        local function appendSkillDescription(self, lvlSelected)
            if not self or type(self.message) ~= "string" or self.message == "" then return end

            local candidates = getDescriptionCandidates(self.perk)
            if not candidates then return end

            local description = findDescription(candidates, "_Description")
            self.message = appendUnique(self.message, description)

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
            -- Keep upstream/mod failures visible: only our append logic is
            -- protected by LCCGuard.
            local result = originalUpdateTooltip(self, lvlSelected)
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "append skill description", appendSkillDescription, self, lvlSelected)
            end
            return result
        end

        LCC_SkillDescriptionsInstalled = true
    end,
}
