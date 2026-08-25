if isServer() then return end

local MOD_ID = "ModpackFestivalSpawn"
local UI_BORDER_SPACING = 10
local FEMALE_GENDER_INDEX = 1 -- Vanilla genderCombo: 1 = Female, 2 = Male
local FEMALE_VOICE_BODY_TYPE = 1 -- Matches CharacterCreationMain:randomVoice

local clientState = {
    stage = 1, -- 1 = editing player. 2 = editing sister.
    playerBuildString = nil,
    sisterBuildString = nil,
    sisterAppearanceData = nil, -- staged until OnGameStart (ModData may not exist during CC)
    sisterForename = nil,
    toggleButton = nil,
    voiceOptionsCache = nil,
    voiceComboFemaleOnly = false,
}

local function getJoypadData()
    return JoypadState and JoypadState.getMainMenuJoypad and JoypadState.getMainMenuJoypad()
        or (CoopCharacterCreation and CoopCharacterCreation.getJoypad and CoopCharacterCreation.getJoypad())
end

local function getCharacterCreationPreviewCharacter(panel)
    if panel and panel.avatarPanel and panel.avatarPanel.avatarPanel
        and panel.avatarPanel.avatarPanel.getCharacter then
        local ch = panel.avatarPanel.avatarPanel:getCharacter()
        if ch then return ch end
    end
    if MainScreen.instance and MainScreen.instance.avatar then
        return MainScreen.instance.avatar
    end
    return nil
end

local function rgb2dec(r, g, b)
    -- Store clothing tint as the same packed RGB value the old applier used.
    local r255 = math.floor(r * 255 + 0.5)
    local g255 = math.floor(g * 255 + 0.5)
    local b255 = math.floor(b * 255 + 0.5)
    return r255 * 65536 + g255 * 256 + b255
end

local function findHairTypeIndex(female, hairModel)
    -- The bare baseline stores the exact hair model string for future rebuild work.
    -- Numeric hairType was only needed by the old NPC visual applier.
    return nil
end

local function findHairColorIndex(desc, immutableColor)
    if not desc or not immutableColor or not desc.getCommonHairColor then return nil end

    local hairColors = desc:getCommonHairColor()
    if not hairColors or not hairColors.size then return nil end

    local r0 = immutableColor:getRedFloat()
    local g0 = immutableColor:getGreenFloat()
    local b0 = immutableColor:getBlueFloat()

    local bestIdx = nil
    local bestD = math.huge

    local size = hairColors:size()
    for i = 1, size do
        local c = hairColors:get(i - 1)
        if c then
            local r = c:getRedFloat()
            local g = c:getGreenFloat()
            local b = c:getBlueFloat()
            local d = (r - r0) * (r - r0) + (g - g0) * (g - g0) + (b - b0) * (b - b0)
            if d < bestD then
                bestD = d
                bestIdx = i
            end
        end
    end

    return bestIdx
end

local function extractSisterAppearanceForBrain(panel)
    local desc = MainScreen and MainScreen.instance and MainScreen.instance.desc
    if not desc then return nil end
    if not desc.getHumanVisual then return nil end

    local human = desc:getHumanVisual()
    if not human then return nil end

    local forename = panel and panel.forenameEntry and panel.forenameEntry:getText() or nil
    if forename == "" then forename = nil end

    local a = {
        female = true,
        skin = human:getSkinTextureIndex() + 1, -- 1-based skin index for appearance apply
        hairType = nil,
        hairModel = nil,
        hairColor = nil,
        hairColorRgb = nil,
        forename = forename,
        clothing = {},
        tint = {},
    }

    local hairModel = human.getHairModel and human:getHairModel() or nil
    if hairModel and hairModel ~= "" then
        a.hairModel = hairModel
    end
    a.hairType = findHairTypeIndex(true, hairModel)
    local hairImmutable = human.getHairColor and human:getHairColor() or nil
    a.hairColor = findHairColorIndex(desc, hairImmutable)
    if hairImmutable and hairImmutable.getRedFloat then
        a.hairColorRgb = {
            r = hairImmutable:getRedFloat(),
            g = hairImmutable:getGreenFloat(),
            b = hairImmutable:getBlueFloat(),
        }
    end

    -- Clothing + tint (read UI combos — worn-items on desc can lag behind the picker)
    if panel.clothingCombo then
        for bodyLocation, combo in pairs(panel.clothingCombo) do
            local itemType = combo:getOptionData(combo.selected)
            if itemType ~= nil and itemType ~= "" then
                a.clothing[bodyLocation] = itemType
            else
                local item = desc:getWornItem(ItemBodyLocation.get(ResourceLocation.of(bodyLocation)))
                if item then
                    a.clothing[bodyLocation] = item:getFullType()
                end
            end
            if panel.clothingColorBtn
                and panel.clothingColorBtn[bodyLocation]
                and panel.clothingColorBtn[bodyLocation]:isVisible() then
                local c = panel.clothingColorBtn[bodyLocation].backgroundColor
                if c then
                    a.tint[bodyLocation] = rgb2dec(c.r, c.g, c.b)
                end
            end
        end
    end

    -- Fallbacks for future appearance rebuild work.
    if a.hairType == nil then a.hairType = 1 end
    if a.hairColor == nil then a.hairColor = 1 end

    return a
end

local function getVoiceStyleName(voiceStyle)
    if voiceStyle and voiceStyle.getName then
        return voiceStyle:getName()
    end
    return nil
end

local function getVoiceStyleBodyType(voiceStyle)
    if voiceStyle and voiceStyle.getBodyTypeDefault then
        return voiceStyle:getBodyTypeDefault()
    end
    return nil
end

local function isFemaleVoiceStyle(voiceStyle, optionText)
    local bodyType = getVoiceStyleBodyType(voiceStyle)
    if bodyType then
        return bodyType == FEMALE_VOICE_BODY_TYPE
    end
    if optionText and type(optionText) == "string" then
        local lower = string.lower(optionText)
        if string.find(lower, "female", 1, true) or string.find(lower, "woman", 1, true) then
            return true
        end
    end
    return false
end

local function applyVoiceSelectionFromCombo(panel)
    local combo = panel and panel.voiceTypeCombo
    local desc = MainScreen.instance and MainScreen.instance.desc
    if not combo or not desc or combo:getOptionCount() == 0 then
        return false
    end

    local idx = combo.selected
    if not idx or idx < 1 then
        idx = 1
        combo.selected = 1
    end

    local data = combo:getOptionData(idx)
    local voiceTypeInt = nil
    local voicePrefix = nil

    if type(data) == "number" then
        voiceTypeInt = data
    elseif data then
        if data.getVoiceType then
            voiceTypeInt = data:getVoiceType()
        end
        if data.getPrefix then
            voicePrefix = data:getPrefix()
        end
    end

    local ok = pcall(function()
        if voicePrefix and desc.setVoicePrefix then
            desc:setVoicePrefix(voicePrefix)
        end
        if voiceTypeInt ~= nil and desc.setVoiceType then
            desc:setVoiceType(voiceTypeInt)
        elseif panel.onVoiceTypeSelected then
            panel:onVoiceTypeSelected()
            return
        end
        if panel.getVoicePitch and desc.setVoicePitch then
            local pitch = panel:getVoicePitch()
            if pitch ~= nil then
                desc:setVoicePitch(pitch)
            end
        end
    end)

    if not ok and panel.onVoiceTypeSelected then
        pcall(function()
            panel:onVoiceTypeSelected()
        end)
    end
    return ok
end

local function safeOnVoiceTypeSelected(panel)
    if not panel then return end
    applyVoiceSelectionFromCombo(panel)
end

local function ensureVoiceOptionsCache(panel)
    if clientState.voiceOptionsCache or not panel or not panel.voiceTypeCombo then
        return
    end
    local cache = {}
    local combo = panel.voiceTypeCombo
    for i = 1, combo:getOptionCount() do
        local data = combo:getOptionData(i)
        cache[#cache + 1] = {
            text = combo:getOptionText(i),
            data = data,
            name = getVoiceStyleName(data),
            bodyType = getVoiceStyleBodyType(data),
        }
    end
    clientState.voiceOptionsCache = cache
end

local function selectVoiceComboIndex(combo, index)
    if not combo or not index or index < 1 then return false end
    local data = combo:getOptionData(index)
    if combo.selectData and data ~= nil then
        combo:selectData(data)
        return true
    end
    combo.selected = index
    return true
end

local function rebuildVoiceCombo(panel, femaleOnly)
    local combo = panel and panel.voiceTypeCombo
    local cache = clientState.voiceOptionsCache
    if not combo or not cache then return false end

    local selectedName = nil
    if combo.selected and combo.selected > 0 then
        selectedName = getVoiceStyleName(combo:getOptionData(combo.selected))
    end

    combo:clear()
    for _, item in ipairs(cache) do
        if not femaleOnly or isFemaleVoiceStyle(item.data, item.text) then
            combo:addOptionWithData(item.text, item.data)
        end
    end

    if femaleOnly and combo:getOptionCount() == 0 then
        for _, item in ipairs(cache) do
            combo:addOptionWithData(item.text, item.data)
        end
    end

    if combo:getOptionCount() == 0 then
        return false
    end

    local found = false
    if selectedName then
        for i = 1, combo:getOptionCount() do
            if getVoiceStyleName(combo:getOptionData(i)) == selectedName then
                selectVoiceComboIndex(combo, i)
                found = true
                break
            end
        end
    end

    if not found then
        selectVoiceComboIndex(combo, 1)
    end

    safeOnVoiceTypeSelected(panel)
    return found
end

local function applySisterVoiceFilter(panel, pickRandomIfInvalid)
    ensureVoiceOptionsCache(panel)
    local found = rebuildVoiceCombo(panel, true)
    clientState.voiceComboFemaleOnly = true
    if (not found or pickRandomIfInvalid) and panel.randomVoice then
        pcall(function()
            panel:randomVoice()
        end)
        safeOnVoiceTypeSelected(panel)
    end
end

local function restorePlayerVoiceCombo(panel)
    if not panel then return end
    ensureVoiceOptionsCache(panel)
    if clientState.voiceComboFemaleOnly then
        rebuildVoiceCombo(panel, false)
        clientState.voiceComboFemaleOnly = false
        safeOnVoiceTypeSelected(panel)
    end
end

local function makeBuildStringFromUI(panel)
    local desc = MainScreen.instance.desc
    if not desc then return nil end

    local genderIndex = panel.genderCombo.selected
    if clientState.stage == 2 then
        genderIndex = FEMALE_GENDER_INDEX
    end
    local savestring = "gender=" .. tostring(genderIndex) .. ";"
    local skin = panel.skinColorButton.backgroundColor
    savestring = savestring .. "skincolor="
        .. tostring(skin.r) .. "," .. tostring(skin.g) .. "," .. tostring(skin.b) .. ";"

    savestring = savestring .. "name="
        .. panel.forenameEntry:getText() .. "|" .. panel.surnameEntry:getText() .. ";"

    local hairStyle = panel.hairTypeCombo:getOptionData(panel.hairTypeCombo.selected)
    local hairColor = panel.hairColorButton.backgroundColor
    savestring = savestring .. "hair="
        .. tostring(hairStyle) .. "|"
        .. tostring(hairColor.r) .. "," .. tostring(hairColor.g) .. "," .. tostring(hairColor.b) .. ";"

    savestring = savestring .. "stubble="
        .. (panel.hairStubbleTickBox:isSelected(1) and "1" or "2") .. ";"

    if not desc:isFemale() then
        savestring = savestring .. "chesthair="
            .. (panel.chestHairTickBox:isSelected(1) and "1" or "2") .. ";"

        local beardStyle = panel.beardTypeCombo:getOptionData(panel.beardTypeCombo.selected)
        savestring = savestring .. "beard=" .. tostring(beardStyle) .. ";"
        savestring = savestring .. "beardstubble="
            .. (panel.beardStubbleTickBox:isSelected(1) and "1" or "2") .. ";"
    end

    local voiceStyleName = ""
    local voicePitch = 0
    if panel.voiceTypeCombo and panel.voiceTypeCombo.selected and panel.voiceTypeCombo.selected > 0 then
        local voiceIdx = panel.voiceTypeCombo.selected
        local voiceStyleObj = panel.voiceTypeCombo:getOptionData(voiceIdx)
        voiceStyleName = getVoiceStyleName(voiceStyleObj) or ""
        if voiceStyleName == "" and panel.voiceTypeCombo.getOptionText then
            voiceStyleName = panel.voiceTypeCombo:getOptionText(voiceIdx) or ""
        end
    end
    if panel.getVoicePitch then
        pcall(function()
            voicePitch = panel:getVoicePitch()
        end)
    end
    savestring = savestring .. "voiceStyle=" .. tostring(voiceStyleName) .. ";"
    savestring = savestring .. "voicePitch=" .. tostring(voicePitch) .. ";"

    if panel.clothingCombo then
        for bodyLocation, combo in pairs(panel.clothingCombo) do
            local itemType = combo:getOptionData(combo.selected)
            if itemType ~= nil then
                savestring = savestring .. bodyLocation .. "=" .. tostring(itemType)
                if panel.clothingColorBtn
                    and panel.clothingColorBtn[bodyLocation]
                    and panel.clothingColorBtn[bodyLocation]:isVisible() then
                    local cc = panel.clothingColorBtn[bodyLocation].backgroundColor
                    savestring = savestring .. "|" .. cc.r .. "," .. cc.g .. "," .. cc.b
                end
                if panel.clothingTextureCombo
                    and panel.clothingTextureCombo[bodyLocation]
                    and panel.clothingTextureCombo[bodyLocation]:isVisible() then
                    savestring = savestring .. "|" .. tostring(panel.clothingTextureCombo[bodyLocation].selected)
                end
                savestring = savestring .. ";"
            end
        end
    end

    return savestring
end

local function stageSisterForename(forename)
    if not forename or forename == "" then return end
    clientState.sisterForename = forename
    pcall(function()
        if ModpackFestivalQuests and ModpackFestivalQuests.rememberSisterForename then
            ModpackFestivalQuests.rememberSisterForename(forename)
        end
    end)
end

local function persistSisterAppearance(panel)
    if panel then
        clientState.sisterBuildString = makeBuildStringFromUI(panel)
    end
    local appearance = extractSisterAppearanceForBrain(panel)
    if not appearance then return end
    clientState.sisterAppearanceData = appearance
    pcall(function()
        if ModpackFestivalSister and ModpackFestivalSister.storeSisterAppearance then
            ModpackFestivalSister.storeSisterAppearance(appearance, clientState.sisterBuildString)
        end
    end)
    local forename = appearance.forename
    if (not forename or forename == "") and clientState.sisterBuildString
        and ModpackFestivalQuests and ModpackFestivalQuests.parseSisterForenameFromBuildString then
        forename = ModpackFestivalQuests.parseSisterForenameFromBuildString(clientState.sisterBuildString)
    end
    stageSisterForename(forename)
end

local function syncSisterAppearanceToServer()
    local player = getSpecificPlayer and getSpecificPlayer(0)
    if not player or not sendClientCommand or not clientState.sisterAppearanceData then
        return
    end
    sendClientCommand(player, MOD_ID, "SisterAppearance", {
        appearance = clientState.sisterAppearanceData,
        buildString = clientState.sisterBuildString,
    })
end

local function commitSisterAppearanceAfterLoad()
    if ModpackFestivalSister and ModpackFestivalSister.getState then
        local st = ModpackFestivalSister.getState()
        if not clientState.sisterAppearanceData and st.sisterAppearanceData then
            clientState.sisterAppearanceData = st.sisterAppearanceData
        end
        if not clientState.sisterBuildString and st.sisterBuildString then
            clientState.sisterBuildString = st.sisterBuildString
        end
        if not clientState.sisterForename and st.sisterForename then
            clientState.sisterForename = st.sisterForename
        end
    end
    if clientState.sisterBuildString and ModpackFestivalQuests
        and ModpackFestivalQuests.parseSisterForenameFromBuildString then
        local fromBuild = ModpackFestivalQuests.parseSisterForenameFromBuildString(clientState.sisterBuildString)
        if fromBuild then
            clientState.sisterForename = fromBuild
        end
    end
    if clientState.sisterAppearanceData and ModpackFestivalSister and ModpackFestivalSister.storeSisterAppearance then
        ModpackFestivalSister.storeSisterAppearance(clientState.sisterAppearanceData, clientState.sisterBuildString)
    end
    stageSisterForename(clientState.sisterForename)
    syncSisterAppearanceToServer()
end

local function setGenderComboEnabled(panel, enabled)
    if not panel or not panel.genderCombo then return end
    if panel.genderCombo.setEnabled then
        panel.genderCombo:setEnabled(enabled)
    elseif panel.genderCombo.setEnable then
        panel.genderCombo:setEnable(enabled)
    end
end

local function forceFemale(panel, runGenderHandler)
    local desc = MainScreen.instance and MainScreen.instance.desc
    if not desc or not panel or not panel.genderCombo then return end

    local wasFemale = desc:isFemale()
    panel.genderCombo.selected = FEMALE_GENDER_INDEX

    if runGenderHandler and not wasFemale and panel.onGenderSelected then
        panel:onGenderSelected(panel.genderCombo)
        return
    end

    if wasFemale and panel.genderCombo.selected == FEMALE_GENDER_INDEX then
        return
    end

    if MainScreen.instance.avatar then
        MainScreen.instance.avatar:setFemale(true)
    end
    desc:setFemale(true)
    desc:getHumanVisual():removeBodyVisualFromItemType("Base.M_Hair_Stubble")
    desc:getHumanVisual():removeBodyVisualFromItemType("Base.M_Beard_Stubble")
    if panel.disableBtn then
        panel:disableBtn()
    end
end

local function setGenderComboLocked(panel, locked)
    if not panel or not panel.genderCombo then return end
    if locked then
        if not clientState.genderComboLocked then
            forceFemale(panel, false)
            setGenderComboEnabled(panel, false)
            clientState.genderComboLocked = true
        end
    elseif clientState.genderComboLocked then
        setGenderComboEnabled(panel, true)
        clientState.genderComboLocked = false
        if panel.disableBtn then
            panel:disableBtn()
        end
    end
end

local function applyBuildStringToUI(panel, build)
    local desc = MainScreen.instance.desc
    if not desc or not build then return end

    desc:getWornItems():clear()
    local items = luautils.split(build, ";")

    for _, v in pairs(items) do
        if v and v ~= "" then
            local location = luautils.split(v, "=")
            if location[1] and location[1] ~= "" then
                local options = nil
                if location[2] then
                    options = luautils.split(location[2], "|")
                end

                if location[1] == "gender" then
                    if clientState.stage ~= 2 then
                        panel.genderCombo.selected = tonumber(options[1])
                        panel:onGenderSelected(panel.genderCombo)
                        desc:getWornItems():clear()
                    else
                        forceFemale(panel, false)
                    end
                elseif location[1] == "skincolor" then
                    local color = luautils.split(options[1], ",")
                    local colorRGB = {
                        r = tonumber(color[1]),
                        g = tonumber(color[2]),
                        b = tonumber(color[3]),
                    }
                    panel.colorPickerSkin:setInitialColor(ColorInfo.new(colorRGB.r, colorRGB.g, colorRGB.b, 1.0))
                    panel:onSkinColorPicked(colorRGB, true)
                elseif location[1] == "name" then
                    desc:setForename(options[1])
                    panel.forenameEntry:setText(options[1])
                    desc:setSurname(options[2])
                    panel.surnameEntry:setText(options[2])
                elseif location[1] == "hair" then
                    local color = luautils.split(options[2], ",")
                    local colorRGB = { r = tonumber(color[1]), g = tonumber(color[2]), b = tonumber(color[3]) }
                    panel:onHairColorPicked(colorRGB, true)
                    panel.hairTypeCombo.selected = 1
                    panel.hairTypeCombo:selectData(options[1])
                    panel:onHairTypeSelected(panel.hairTypeCombo)
                elseif location[1] == "stubble" then
                    local stubble = tonumber(options[1]) == 1
                    panel.hairStubbleTickBox:setSelected(1, stubble)
                    panel:onShavedHairSelected(1, stubble)
                elseif location[1] == "chesthair" then
                    local chestHair = tonumber(options[1]) == 1
                    panel.chestHairTickBox:setSelected(1, chestHair)
                    panel:onChestHairSelected(1, chestHair)
                elseif location[1] == "beard" then
                    panel.beardTypeCombo.selected = 1
                    panel.beardTypeCombo:selectData(options and options[1] or "")
                    panel:onBeardTypeSelected(panel.beardTypeCombo)
                elseif location[1] == "beardstubble" then
                    local stubble = tonumber(options[1]) == 1
                    panel.beardStubbleTickBox:setSelected(1, stubble)
                    panel:onBeardStubbleSelected(1, stubble)
                elseif location[1] == "voiceStyle" then
                    local foundVoice = false
                    local voiceCombo = panel.voiceTypeCombo
                    for i = 1, voiceCombo:getOptionCount() do
                        local voiceOption = voiceCombo:getOptionData(i)
                        local voiceName = getVoiceStyleName(voiceOption)
                        if voiceName == options[1] then
                            selectVoiceComboIndex(voiceCombo, i)
                            foundVoice = true
                            break
                        end
                    end
                    if not foundVoice and panel.randomVoice then
                        pcall(function() panel:randomVoice() end)
                    end
                    safeOnVoiceTypeSelected(panel)
                elseif location[1] == "voicePitch" then
                    panel.voicePitchSlider:setCurrentValue(options and tonumber(options[1]) or 0.0, true)
                    safeOnVoiceTypeSelected(panel)
                elseif panel.clothingCombo and panel.clothingCombo[location[1]] then
                    local bodyLocation = location[1]
                    local itemType = options[1]
                    panel.clothingCombo[bodyLocation].selected = 1
                    panel.clothingCombo[bodyLocation]:selectData(itemType)
                    panel:onClothingComboSelected(panel.clothingCombo[bodyLocation], bodyLocation)

                    if options[2] then
                        local comboTexture = panel.clothingTextureCombo[bodyLocation]
                        local color = luautils.split(options[2], ",")
                        if (#color == 3) and panel.clothingColorBtn and panel.clothingColorBtn[bodyLocation] then
                            local colorRGB = { r = tonumber(color[1]), g = tonumber(color[2]), b = tonumber(color[3]) }
                            panel:onClothingColorPicked(colorRGB, true, bodyLocation)
                        elseif comboTexture and comboTexture.options[tonumber(color[1])] then
                            comboTexture.selected = tonumber(color[1])
                            panel:onClothingTextureComboSelected(comboTexture, bodyLocation)
                        end
                    end
                end
            end
        end
    end

    panel:updateSelectedClothingCombo()
    panel:arrangeClothingUI()
end

local function randomizeSisterFemaleName(panel)
    local desc = MainScreen.instance and MainScreen.instance.desc
    if not desc then return end

    desc:setFemale(true)
    if SurvivorFactory and SurvivorFactory.randomName then
        SurvivorFactory.randomName(desc)
    end
    if panel and panel.forenameEntry then
        panel.forenameEntry:setText(desc:getForename())
    end
    if panel and panel.surnameEntry then
        panel.surnameEntry:setText(desc:getSurname())
    end
end

local function randomizeSisterAppearance(panel)
    local desc = MainScreen.instance and MainScreen.instance.desc
    if not desc or not panel then return end

    desc:getExtras():clear()
    desc:getWornItems():clear()

    if MainScreen.instance.avatar then
        MainScreen.instance.avatar:setFemale(true)
    end
    desc:setFemale(true)
    desc:getHumanVisual():clear()

    if panel.setAvatarFromUI then
        panel:setAvatarFromUI()
    end
    if panel.randomGenericOutfit then
        panel:randomGenericOutfit()
    end
    if panel.disableBtn then
        panel:disableBtn()
    end

    randomizeSisterFemaleName(panel)
    applySisterVoiceFilter(panel, true)

    if panel.updateSelectedClothingCombo then
        panel:updateSelectedClothingCombo()
    end
    if panel.arrangeClothingUI then
        panel:arrangeClothingUI()
    end
    if panel.loadJoypadButtons then
        panel:loadJoypadButtons()
    end
end

local function getSisterForenameForUi(panel)
    if panel and panel.forenameEntry and clientState.stage == 2 then
        local live = panel.forenameEntry:getText()
        if live and live ~= "" then
            return live
        end
    end
    if clientState.sisterBuildString and ModpackFestivalSister
        and ModpackFestivalSister.parseForenameFromBuildString then
        local fromBuild = ModpackFestivalSister.parseForenameFromBuildString(clientState.sisterBuildString)
        if fromBuild then
            return fromBuild
        end
    end
    if ModpackFestivalSister and ModpackFestivalSister.getSisterForename then
        return ModpackFestivalSister.getSisterForename()
    end
    return ModpackFestivalSister and ModpackFestivalSister.DEFAULT_SISTER_NAME or "Alyssa"
end

local function getSisterToggleLabel(panel)
    return string.upper(getSisterForenameForUi(panel))
end

local function startSisterSetup(panel)
    if not MainScreen.instance or not MainScreen.instance.desc then return end

    clientState.voiceOptionsCache = nil
    setGenderComboLocked(panel, true)
    randomizeSisterAppearance(panel)
end

local function updateButtons(panel)
    if not panel then return end
    if panel.playButton and panel.playButton.setTitle then
        panel.playButton:setTitle(getText(clientState.stage == 1 and "UI_btn_next" or "UI_btn_play"))
    end
    if clientState.toggleButton and clientState.toggleButton.setTitle then
        local label = clientState.stage == 1 and getSisterToggleLabel(panel) or "PLAYER"
        clientState.toggleButton:setTitle(label)
    end
end

local function enterPlayerStage(panel)
    clientState.stage = 1
    setGenderComboLocked(panel, false)
    restorePlayerVoiceCombo(panel)
    updateButtons(panel)
end

local function enterSisterStage(panel)
    clientState.stage = 2
    clientState.voiceOptionsCache = nil
    if clientState.sisterBuildString then
        applyBuildStringToUI(panel, clientState.sisterBuildString)
        setGenderComboLocked(panel, true)
        applySisterVoiceFilter(panel, true)
    else
        startSisterSetup(panel)
    end
    updateButtons(panel)
end

local function ensureToggleButton(panel)
    if not panel or clientState.toggleButton then return end
    if not ISButton then return end

    -- Place between Random and Next/Play (same row as vanilla buttons).
    local w = math.max(90, math.min(140, panel.playButton and panel.playButton:getWidth() or 120))
    local x = (panel.playButton and panel.playButton:getX() or (panel.width - w - 10)) - 10 - w
    if panel.randomButton then
        x = panel.randomButton:getX() - 10 - w
    end
    local y = panel.backButton and panel.backButton:getY() or (panel.height - 40)

    local btn = ISButton:new(x, y, w, panel.playButton and panel.playButton:getHeight() or 24,
        getSisterToggleLabel(panel), panel, function(selfBtn)
            local p = panel
            if clientState.stage == 1 then
                clientState.playerBuildString = makeBuildStringFromUI(p)
                enterSisterStage(p)
            else
                clientState.sisterBuildString = makeBuildStringFromUI(p)
                enterPlayerStage(p)
                if clientState.playerBuildString then
                    applyBuildStringToUI(p, clientState.playerBuildString)
                end
                restorePlayerVoiceCombo(p)
            end
        end)

    btn:initialise()
    btn:instantiate()
    btn:setAnchorLeft(false)
    btn:setAnchorRight(true)
    btn:setAnchorTop(false)
    btn:setAnchorBottom(true)
    panel:addChild(btn)
    clientState.toggleButton = btn
    updateButtons(panel)
end

local function patchCharacterCreation()
    if not CharacterCreationMain or not CharacterCreationMain.onOptionMouseDown then
        return
    end
    if clientState._patched then return end

    clientState._patched = true

    local origOnOptionMouseDown = CharacterCreationMain.onOptionMouseDown
    local origSetVisible = CharacterCreationMain.setVisible
    local origOnGenderSelected = CharacterCreationMain.onGenderSelected
    local origOnRandomCharacter = CharacterCreationMain.onRandomCharacter

    CharacterCreationMain.onGenderSelected = function(self, combo)
        if clientState.stage == 2 then
            if combo.selected ~= FEMALE_GENDER_INDEX then
                combo.selected = FEMALE_GENDER_INDEX
            end
            if not MainScreen.instance.desc:isFemale() then
                return origOnGenderSelected(self, combo)
            end
            forceFemale(self, false)
            return
        end
        return origOnGenderSelected(self, combo)
    end

    CharacterCreationMain.onRandomCharacter = function(self)
        if clientState.stage == 2 then
            randomizeSisterAppearance(self)
            forceFemale(self, false)
            applySisterVoiceFilter(self, true)
            return
        end
        return origOnRandomCharacter(self)
    end

    CharacterCreationMain.setVisible = function(self, bVisible, joypadData)
        local ret = origSetVisible and origSetVisible(self, bVisible, joypadData)

        if bVisible then
            ensureToggleButton(self)
            updateButtons(self)
            if clientState.stage == 2 then
                setGenderComboLocked(self, true)
            end
        else
            clientState.stage = 1
            clientState.genderComboLocked = false
            clientState.voiceComboFemaleOnly = false
        end

        return ret
    end

    CharacterCreationMain.onOptionMouseDown = function(self, button, x, y)
        if button and button.internal == "BACK" and clientState.stage == 2 then
            -- Expected UX: when editing sister, BACK returns to player customization.
            clientState.sisterBuildString = makeBuildStringFromUI(self)
            enterPlayerStage(self)
            if clientState.playerBuildString then
                applyBuildStringToUI(self, clientState.playerBuildString)
            end
            restorePlayerVoiceCombo(self)
            return
        end
        if not button or button.internal ~= "NEXT" then
            return origOnOptionMouseDown(self, button, x, y)
        end

        if clientState.stage == 1 then
            -- Save the player's chosen appearance, then switch the UI to sister setup.
            local ok, err = pcall(function()
                clientState.playerBuildString = makeBuildStringFromUI(self)
            end)
            if not ok then
                print("[" .. MOD_ID .. "] failed to save player build string: " .. tostring(err))
                return
            end
            enterSisterStage(self)
            return
        end

        -- Stage 2 (sister chosen): save sister appearance, then restore player before vanilla starts the game.
        clientState.sisterBuildString = makeBuildStringFromUI(self)
        persistSisterAppearance(self)

        -- Stage must be 1 before applyBuildStringToUI or gender= male is ignored (stage 2 forces female).
        clientState.stage = 1
        setGenderComboLocked(self, false)
        restorePlayerVoiceCombo(self)
        if clientState.playerBuildString then
            applyBuildStringToUI(self, clientState.playerBuildString)
        end

        return origOnOptionMouseDown(self, button, x, y)
    end

    CharacterCreationMain.prerender = function(self)
        CharacterCreationMain.instance = self
        -- Vanilla title ("CUSTOMISE CHARACTER") is skipped; we draw our own below.
        if ISPanelJoypad and ISPanelJoypad.prerender then
            ISPanelJoypad.prerender(self)
        elseif ISPanel and ISPanel.prerender then
            ISPanel.prerender(self)
        end

        local title = "Customise Self"
        if clientState.stage == 2 then
            title = "Customise Sister"
        end
        if self.drawTextCentre then
            self:drawTextCentre(title, self.width / 2, UI_BORDER_SPACING + 1, 1, 1, 1, 1, UIFont.Large)
        end

        if self.avatarPanel and self.hairTypeCombo and self.beardTypeCombo then
            local facePreview = self.hairTypeCombo.expanded or self.beardTypeCombo.expanded
            self.avatarPanel:setFacePreview(facePreview)
        end
        if clientState.stage == 2 then
            updateButtons(self)
        end
        if self.deleteBuildButton and self.savedBuilds and self.savedBuilds.options and self.savedBuilds.selected then
            self.deleteBuildButton:setEnable(self.savedBuilds.options[self.savedBuilds.selected] ~= nil)
        end
    end
end

Events.OnGameBoot.Add(function()
    pcall(function()
        patchCharacterCreation()
    end)
end)

local patchAttemptTick = 0
local function tryPatchLater()
    patchAttemptTick = patchAttemptTick + 1
    if not ModpackFestivalTick.every(patchAttemptTick, ModpackFestivalTick.GAME) then return end
    if clientState._patched then return end
    pcall(function()
        patchCharacterCreation()
    end)
end
Events.OnTick.Add(tryPatchLater)

Events.OnGameStart.Add(function()
    commitSisterAppearanceAfterLoad()
end)

local createPlayerCommitTicks = 0
local function onCreatePlayerCommitAppearance(_index)
    createPlayerCommitTicks = createPlayerCommitTicks + 1
    if createPlayerCommitTicks > 30 then return end
    commitSisterAppearanceAfterLoad()
end
Events.OnCreatePlayer.Add(onCreatePlayerCommitAppearance)

print("[" .. MOD_ID .. "] sister character-creation pass enabled")

