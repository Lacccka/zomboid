-- Lacccka B42 Activity Fixes
-- Fix Build 42.20 skill-tooltip description lookup for translated perk names.
--
-- Vanilla ISSkillProgressBar.updateTooltip() builds description keys from
-- perk:getName(). In a translated UI getName() is already localized, so Russian
-- produces keys such as IGUI_perks_Искусство_Description instead of the stable
-- IGUI_perks_Art_Description. Keep the upstream tooltip logic intact, then
-- repair that lookup using stable perk ids, known B42 aliases and translated
-- display names.

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
            Sneaking = { "Sneak" },
            PlantScavenging = { "Foraging" },
            Foraging = { "PlantScavenging" },
            Woodwork = { "Carpentry" },
            Carpentry = { "Woodwork" },
            Farming = { "Agriculture" },
            Agriculture = { "Farming" },
            Doctor = { "FirstAid", "Medical", "Medicine" },
            FirstAid = { "Doctor", "Medical", "Medicine" },
            Medical = { "Doctor", "FirstAid", "Medicine" },
            Medicine = { "Doctor", "FirstAid", "Medical" },
            Electricity = { "Electrical" },
            Electrical = { "Electricity" },
            MetalWelding = { "Welding", "Metalworking" },
            Welding = { "MetalWelding", "Metalworking" },
            Metalworking = { "MetalWelding", "Welding" },
            Blacksmith = { "Blacksmithing" },
            Blacksmithing = { "Blacksmith" },
            FlintKnapping = { "Knapping" },
            Knapping = { "FlintKnapping" },
            Husbandry = { "AnimalCare", "AnimalHusbandry", "AnimalHandling" },
            AnimalCare = { "Husbandry", "AnimalHusbandry", "AnimalHandling" },
            AnimalHusbandry = { "Husbandry", "AnimalCare", "AnimalHandling" },
            AnimalHandling = { "Husbandry", "AnimalCare", "AnimalHusbandry" },
            Sprinting = { "Running" },
            Running = { "Sprinting" },
            Lightfoot = { "Lightfooted" },
            Lightfooted = { "Lightfoot" },
            SmallBlade = { "ShortBlade" },
            ShortBlade = { "SmallBlade" },
            SmallBlunt = { "ShortBlunt" },
            ShortBlunt = { "SmallBlunt" },
            Blunt = { "LongBlunt" },
            LongBlunt = { "Blunt" },
        }

        -- IDs for which RussianTextFixes provides (or intentionally aliases)
        -- descriptions. Matching their translated IGUI_perks_* names gives us a
        -- stable fallback even when B42 changes the Java-side Perk id.
        local KNOWN_DESCRIPTION_IDS = {
            "Fitness", "Strength",
            "Sprinting", "Running", "Lightfoot", "Lightfooted", "Nimble", "Sneak", "Sneaking",
            "Blunt", "LongBlunt", "SmallBlunt", "ShortBlunt", "LongBlade", "SmallBlade", "ShortBlade",
            "Axe", "Spear", "Maintenance", "Aiming", "Reloading",
            "Carpentry", "Woodwork", "Cooking", "Farming", "Agriculture",
            "Doctor", "FirstAid", "Medical", "Medicine", "Electricity", "Electrical",
            "Mechanics", "MetalWelding", "Welding", "Metalworking", "Tailoring",
            "Fishing", "Trapping", "Foraging", "PlantScavenging",
            "Blacksmith", "Blacksmithing", "Masonry", "Pottery", "FlintKnapping", "Knapping",
            "Carving", "Glassmaking", "Husbandry", "AnimalCare", "AnimalHusbandry", "AnimalHandling",
            "Butchering", "Tracking",
            "Art", "Cleaning", "Dancing", "Meditation", "Music", "Yoga",
        }

        -- Explicit fallback for the Russian B42 names that have historically
        -- moved between internal ids. This is only a fallback after id and
        -- translation-based matching, not the primary lookup path.
        local DISPLAY_NAME_ALIASES = {
            ["Лёгкий шаг"] = { "Lightfoot", "Lightfooted" },
            ["Бег"] = { "Sprinting", "Running" },
            ["Уход за животными"] = { "Husbandry", "AnimalCare", "AnimalHusbandry", "AnimalHandling" },
            ["Короткое режущее"] = { "SmallBlade", "ShortBlade" },
            ["Короткое дробящее"] = { "SmallBlunt", "ShortBlunt" },
            ["Длинное режущее"] = { "LongBlade" },
            ["Длинное дробящее"] = { "Blunt", "LongBlunt" },
            ["Медицина"] = { "Doctor", "FirstAid", "Medical", "Medicine" },
        }

        local missingLogged = {}

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

        local function addCandidate(candidates, seen, id)
            if id == nil then return end
            id = tostring(id)
            if id == "" or seen[id] then return end

            seen[id] = true
            candidates[#candidates + 1] = id

            local aliases = DESCRIPTION_ALIASES[id]
            if aliases then
                for i = 1, #aliases do
                    addCandidate(candidates, seen, aliases[i])
                end
            end
        end

        local function addNormalizedIdCandidates(candidates, seen, perkId)
            if not perkId then return end
            addCandidate(candidates, seen, perkId)

            -- Be tolerant of enum/debug string forms such as Perks.Lightfoot or
            -- Base:Lightfoot without assuming that B42 always uses one format.
            local dotName = string.match(perkId, "([^%.]+)$")
            if dotName and dotName ~= perkId then
                addCandidate(candidates, seen, dotName)
            end
            local colonName = string.match(perkId, "([^:]+)$")
            if colonName and colonName ~= perkId then
                addCandidate(candidates, seen, colonName)
            end
        end

        local function getDescriptionCandidates(perk)
            if not perk then return nil end

            local candidates = {}
            local seen = {}
            addNormalizedIdCandidates(candidates, seen, getPerkId(perk))

            local localizedName = perk.getName and perk:getName() or nil
            if localizedName ~= nil then
                localizedName = tostring(localizedName)

                -- Match the displayed localized name back to the translation id.
                -- This survives B42 Perk id renames as long as the visible skill
                -- name remains mapped through IGUI_perks_*.
                for i = 1, #KNOWN_DESCRIPTION_IDS do
                    local id = KNOWN_DESCRIPTION_IDS[i]
                    local translatedName = lccGetTextOrNull("IGUI_perks_" .. id)
                    if translatedName and tostring(translatedName) == localizedName then
                        addCandidate(candidates, seen, id)
                    end
                end

                local displayAliases = DISPLAY_NAME_ALIASES[localizedName]
                if displayAliases then
                    for i = 1, #displayAliases do
                        addCandidate(candidates, seen, displayAliases[i])
                    end
                end
            end

            if #candidates == 0 then return nil end
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

        local function logMissingDescription(perk)
            local id = getPerkId(perk) or "?"
            local name = perk and perk.getName and perk:getName() or "?"
            local marker = tostring(id) .. "|" .. tostring(name)
            if missingLogged[marker] then return end
            missingLogged[marker] = true
            print("[LCC][SkillDescriptions][MISS] id=" .. tostring(id) .. " name=" .. tostring(name))
        end

        local function repairSkillDescription(self, lvlSelected)
            if not self or not self.perk or type(self.message) ~= "string" or self.message == "" then return end

            local candidates = getDescriptionCandidates(self.perk)
            local description = findDescription(candidates, "_Description")
            if not description then
                logMissingDescription(self.perk)
                return
            end

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
