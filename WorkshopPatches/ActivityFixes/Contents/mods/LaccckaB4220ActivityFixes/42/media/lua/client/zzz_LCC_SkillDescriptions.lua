-- Lacccka B42 Activity Fixes
-- Build 42.20 translated skill-tooltip repair.
--
-- Vanilla ISSkillProgressBar.updateTooltip() builds description keys from
-- perk:getName(). With a translated UI that name is already localized, so
-- e.g. Russian "Медицина" produces a non-existent
-- IGUI_perks_Медицина_Description key. Resolve descriptions from stable perk
-- ids and only fall back to translated display names when needed.

local Guard = require "LCC/Guard"
local FEATURE = "ui.skill-descriptions"
local PATCH_VERSION = "1.1.6"

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

        local RU_LIGHTFOOT = "Уменьшает шум от передвижения. <LINE> Чем выше навык, тем тише обычные шаги и тем меньше вероятность привлечь зомби звуком движения."
        local RU_SPRINTING = "Повышает эффективность бега. <LINE> Прокачивается во время бега. <LINE> С ростом навыка персонаж быстрее передвигается бегом и эффективнее расходует выносливость."
        local RU_HUSBANDRY = "Повышает навыки обращения с домашними животными. <LINE> Прокачивается при уходе за животными и выполнении связанных действий. <LINE> С ростом навыка становится проще оценивать их состояние и эффективнее использовать возможности животноводства."
        local RU_SMALL_BLADE = "Повышает владение коротким режущим оружием. <LINE> Прокачивается успешными атаками оружием этого типа. <LINE> С ростом навыка увеличивается эффективность и урон атак."
        local RU_SMALL_BLUNT = "Повышает владение коротким дробящим оружием. <LINE> Прокачивается успешными атаками оружием этого типа. <LINE> С ростом навыка увеличивается эффективность и урон атак."
        local RU_LONG_BLADE = "Повышает владение длинным режущим оружием. <LINE> Прокачивается успешными атаками оружием этого типа. <LINE> С ростом навыка увеличивается эффективность и урон атак."
        local RU_LONG_BLUNT = "Повышает владение длинным дробящим оружием. <LINE> Прокачивается успешными атаками оружием этого типа. <LINE> С ростом навыка увеличивается эффективность и урон атак."
        local RU_DOCTOR = "Повышает эффективность медицинской помощи. <LINE> Прокачивается при лечении травм и выполнении медицинских процедур. <LINE> С ростом навыка персонаж лучше определяет состояние ран и эффективнее оказывает помощь."

        -- Exact Build 42 ids observed in the live 42.20.3 client log are
        -- intentionally first-class here. Aliases are retained for mods/build
        -- variations, but these entries no longer depend on Translator keys.
        local ID_DESCRIPTION_OVERRIDES_RU = {
            Lightfoot = RU_LIGHTFOOT,
            Lightfooted = RU_LIGHTFOOT,
            Sprinting = RU_SPRINTING,
            Running = RU_SPRINTING,

            Husbandry = RU_HUSBANDRY,
            AnimalCare = RU_HUSBANDRY,
            AnimalHusbandry = RU_HUSBANDRY,
            AnimalHandling = RU_HUSBANDRY,

            SmallBlade = RU_SMALL_BLADE,
            ShortBlade = RU_SMALL_BLADE,
            SmallBlunt = RU_SMALL_BLUNT,
            ShortBlunt = RU_SMALL_BLUNT,
            LongBlade = RU_LONG_BLADE,
            Blunt = RU_LONG_BLUNT,
            LongBlunt = RU_LONG_BLUNT,

            Doctor = RU_DOCTOR,
            FirstAid = RU_DOCTOR,
            Medical = RU_DOCTOR,
            Medicine = RU_DOCTOR,
        }

        local DISPLAY_NAME_ALIASES = {
            ["Лёгкий шаг"] = { "Lightfoot", "Lightfooted" },
            ["Легкий шаг"] = { "Lightfoot", "Lightfooted" },
            ["Бег"] = { "Sprinting", "Running" },
            ["Уход за животными"] = { "Husbandry", "AnimalCare", "AnimalHusbandry", "AnimalHandling" },
            ["Короткое режущее"] = { "SmallBlade", "ShortBlade" },
            ["Короткое дробящее"] = { "SmallBlunt", "ShortBlunt" },
            ["Длинное режущее"] = { "LongBlade" },
            ["Длинное дробящее"] = { "Blunt", "LongBlunt" },
            ["Медицина"] = { "Doctor", "FirstAid", "Medical", "Medicine" },
        }

        local DISPLAY_DESCRIPTION_OVERRIDES_RU = {
            ["Лёгкий шаг"] = RU_LIGHTFOOT,
            ["Легкий шаг"] = RU_LIGHTFOOT,
            ["Бег"] = RU_SPRINTING,
            ["Уход за животными"] = RU_HUSBANDRY,
            ["Короткое режущее"] = RU_SMALL_BLADE,
            ["Короткое дробящее"] = RU_SMALL_BLUNT,
            ["Длинное режущее"] = RU_LONG_BLADE,
            ["Длинное дробящее"] = RU_LONG_BLUNT,
            ["Медицина"] = RU_DOCTOR,
        }

        local missingLogged = {}
        local overrideLogged = {}

        local function lccGetTextOrNull(key)
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
            if code then
                local normalized = string.lower(tostring(code))
                if normalized == "ru" or normalized == "russian" then
                    return true
                end
            end

            -- Fallback for environments where the Java Translator class is not
            -- exposed to Lua. Use stable existing translations, not the old
            -- "Фитнес" assumption (B42 RU commonly calls it "Физподготовка").
            local doctor = lccGetTextOrNull("IGUI_perks_Doctor")
            local meditation = lccGetTextOrNull("IGUI_perks_Meditation")
            local fitness = lccGetTextOrNull("IGUI_perks_Fitness")
            return doctor == "Медицина"
                or meditation == "Медитация"
                or fitness == "Физподготовка"
                or fitness == "Фитнес"
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

        local function getExplicitDescriptionOverride(perk, candidates)
            if not russianUI then
                return nil, nil
            end

            -- Prefer the exact Java perk id from the live runtime.
            local exactId = getPerkId(perk)
            if exactId then
                local direct = ID_DESCRIPTION_OVERRIDES_RU[exactId]
                if direct then
                    return direct, "id:" .. exactId
                end

                local dotName = string.match(exactId, "([^%.]+)$")
                if dotName and ID_DESCRIPTION_OVERRIDES_RU[dotName] then
                    return ID_DESCRIPTION_OVERRIDES_RU[dotName], "id:" .. dotName
                end

                local colonName = string.match(exactId, "([^:]+)$")
                if colonName and ID_DESCRIPTION_OVERRIDES_RU[colonName] then
                    return ID_DESCRIPTION_OVERRIDES_RU[colonName], "id:" .. colonName
                end
            end

            if candidates then
                for i = 1, #candidates do
                    local description = ID_DESCRIPTION_OVERRIDES_RU[candidates[i]]
                    if description then
                        return description, "alias:" .. tostring(candidates[i])
                    end
                end
            end

            if perk and perk.getName then
                local localizedName = perk:getName()
                if localizedName ~= nil then
                    local description = DISPLAY_DESCRIPTION_OVERRIDES_RU[tostring(localizedName)]
                    if description then
                        return description, "name"
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
            local description, overrideSource = getExplicitDescriptionOverride(self.perk, candidates)
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

            -- If another translation already supplied a canonical description,
            -- replace it when an explicit B42.20 override is authoritative.
            if not replaced and explicitOverride and candidates then
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
