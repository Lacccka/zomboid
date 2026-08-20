-- Lacccka B42 Activity Fixes
-- Build 42.20 translated skill-tooltip repair.
--
-- IMPORTANT: keep this Lua source ASCII-only. Build 42.20 on Windows can
-- corrupt non-ASCII literals embedded directly in mod Lua files. Russian
-- fallback text therefore lives in a normal Translator JSON file and this
-- script references it only by ASCII keys.

local Guard = require "LCC/Guard"
local FEATURE = "ui.skill-descriptions"
local PATCH_VERSION = "1.1.7"

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

        -- These keys are stored in RussianTextFixes/42/.../RU/Farming.json.
        -- Keeping the Lua side ASCII-only avoids the mojibake seen when Russian
        -- literals are parsed directly from a mod Lua file on Windows.
        local RU_DESCRIPTION_KEYS = {
            Lightfoot = "Farming_LCC_Skill_Lightfoot_Description",
            Lightfooted = "Farming_LCC_Skill_Lightfoot_Description",
            Sprinting = "Farming_LCC_Skill_Sprinting_Description",
            Running = "Farming_LCC_Skill_Sprinting_Description",

            Husbandry = "Farming_LCC_Skill_Husbandry_Description",
            AnimalCare = "Farming_LCC_Skill_Husbandry_Description",
            AnimalHusbandry = "Farming_LCC_Skill_Husbandry_Description",
            AnimalHandling = "Farming_LCC_Skill_Husbandry_Description",

            SmallBlade = "Farming_LCC_Skill_SmallBlade_Description",
            ShortBlade = "Farming_LCC_Skill_SmallBlade_Description",
            SmallBlunt = "Farming_LCC_Skill_SmallBlunt_Description",
            ShortBlunt = "Farming_LCC_Skill_SmallBlunt_Description",
            LongBlade = "Farming_LCC_Skill_LongBlade_Description",
            Blunt = "Farming_LCC_Skill_Blunt_Description",
            LongBlunt = "Farming_LCC_Skill_Blunt_Description",

            Doctor = "Farming_LCC_Skill_Doctor_Description",
            FirstAid = "Farming_LCC_Skill_Doctor_Description",
            Medical = "Farming_LCC_Skill_Doctor_Description",
            Medicine = "Farming_LCC_Skill_Doctor_Description",
        }

        local missingLogged = {}
        local overrideLogged = {}

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
        print("[LCC][SkillDescriptions][" .. PATCH_VERSION .. "] installed language=" .. tostring(languageCode) .. " russian=" .. tostring(russianUI))

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
                for i = 1, #KNOWN_DESCRIPTION_IDS do
                    local id = KNOWN_DESCRIPTION_IDS[i]
                    local translatedName = lccGetTextOrNull("IGUI_perks_" .. id)
                    if translatedName and tostring(translatedName) == localizedName then
                        addCandidate(candidates, seen, id)
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

        local function getRussianOverride(candidates)
            if not russianUI or not candidates then return nil, nil end
            for i = 1, #candidates do
                local key = RU_DESCRIPTION_KEYS[candidates[i]]
                if key then
                    local text = lccGetTextOrNull(key)
                    if text and text ~= "" then
                        return text, "translator:" .. tostring(candidates[i])
                    end
                end
            end
            return nil, nil
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

        local function logOverride(perk, source)
            local id = getPerkId(perk) or "?"
            local name = perk and perk.getName and perk:getName() or "?"
            local marker = tostring(id) .. "|" .. tostring(name) .. "|" .. tostring(source)
            if overrideLogged[marker] then return end
            overrideLogged[marker] = true
            print("[LCC][SkillDescriptions][OVERRIDE] source=" .. tostring(source) .. " id=" .. tostring(id) .. " name=" .. tostring(name))
        end

        local function repairSkillDescription(self, lvlSelected)
            if not self or not self.perk or type(self.message) ~= "string" or self.message == "" then return end

            local candidates = getDescriptionCandidates(self.perk)
            local description, overrideSource = getRussianOverride(candidates)
            local explicitOverride = description ~= nil

            if explicitOverride then
                logOverride(self.perk, overrideSource)
            else
                description = findDescription(candidates, "_Description")
            end

            if not description then
                logMissingDescription(self.perk)
                return
            end

            local localizedName = self.perk:getName()
            local replaced = false

            if localizedName ~= nil then
                local wrongKey = "IGUI_perks_" .. tostring(localizedName) .. "_Description"
                local repaired
                repaired, replaced = replacePlain(self.message, wrongKey, description)
                self.message = repaired
            end

            if not replaced and candidates then
                for i = 1, #candidates do
                    local oldDescription = lccGetTextOrNull("IGUI_perks_" .. candidates[i] .. "_Description")
                    if oldDescription and oldDescription ~= "" and oldDescription ~= description then
                        local candidateRepaired, candidateReplaced = replacePlain(self.message, oldDescription, description)
                        if candidateReplaced then
                            self.message = candidateRepaired
                            replaced = true
                            break
                        end
                    end
                end
            end

            if not replaced then
                self.message = appendUnique(self.message, description)
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
