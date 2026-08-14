local env = _G.NMCDPlayerWindowEnv
setfenv(1, env)

local FRONT_VARIANTS = {
    CDPlayerBlack = "Black",
    CDPlayerBlue = "Blue",
    CDPlayerCow = "Cow",
    CDPlayerCyan = "Cyan",
    CDPlayerGreen = "Green",
    CDPlayerMagenta = "Magenta",
    CDPlayerOrange = "Orange",
    CDPlayerPink = "Pink",
    CDPlayerPurple = "Purple",
    CDPlayerRed = "Red",
    CDPlayerWhite = "White",
    CDPlayerYellow = "Yellow",
}

local function ensureFrontTextureCache()
    UI_TEXTURES.frontByVariant = UI_TEXTURES.frontByVariant or {}
    return UI_TEXTURES.frontByVariant
end

local function ensureButtonTextureCache()
    UI_TEXTURES.buttonByKind = UI_TEXTURES.buttonByKind or {}
    return UI_TEXTURES.buttonByKind
end

local function ensureSideTextureCache()
    UI_TEXTURES.sideButton = UI_TEXTURES.sideButton or {}
    return UI_TEXTURES.sideButton
end

local function ensureLidTextureCache()
    UI_TEXTURES.lidByVariant = UI_TEXTURES.lidByVariant or {}
    return UI_TEXTURES.lidByVariant
end

local function ensureDisplayIconTextureCache()
    UI_TEXTURES.displayIconByPolicy = UI_TEXTURES.displayIconByPolicy or {}
    return UI_TEXTURES.displayIconByPolicy
end

local function ensureDisplayBatteryTextureCache()
    UI_TEXTURES.displayBatteryByStage = UI_TEXTURES.displayBatteryByStage or {}
    return UI_TEXTURES.displayBatteryByStage
end

local function getDisplayTexture(window)
    local cacheKey = "displayOff"
    local texturePath = DISPLAY_TEXTURE_PATH
    if window and window.isDisplayPoweredOn and window:isDisplayPoweredOn() == true then
        cacheKey = "displayOn"
        texturePath = DISPLAY_TEXTURE_ON_PATH
    end
    UI_TEXTURES.displayByState = UI_TEXTURES.displayByState or {}
    local cache = UI_TEXTURES.displayByState
    if cache[cacheKey] == nil and getTexture then
        cache[cacheKey] = getTexture(texturePath) or false
    end
    if cache[cacheKey] == false then
        return nil
    end
    return cache[cacheKey]
end

local function getAuxIngressTexture()
    if UI_TEXTURES.auxIngress == nil and getTexture then
        UI_TEXTURES.auxIngress = getTexture(AUX_INGRESS_TEXTURE_PATH) or false
    end
    if UI_TEXTURES.auxIngress == false then
        return nil
    end
    return UI_TEXTURES.auxIngress
end

local function getDisplayModeIconTexture(policy)
    local key = tostring(policy or "autoplay")
    local texturePath = DISPLAY_MODE_ICON_AUTOPLAY_TEXTURE_PATH
    if key == "loop_song" then
        texturePath = DISPLAY_MODE_ICON_LOOP_SONG_TEXTURE_PATH
    elseif key == "loop_album" then
        texturePath = DISPLAY_MODE_ICON_LOOP_ALBUM_TEXTURE_PATH
    elseif key == "shuffle" then
        texturePath = DISPLAY_MODE_ICON_SHUFFLE_TEXTURE_PATH
    end
    local cache = ensureDisplayIconTextureCache()
    if cache[key] == nil and getTexture then
        cache[key] = getTexture(texturePath) or false
    end
    if cache[key] == false then
        return nil
    end
    return cache[key]
end

local function getDisplayBatteryTexture(stageKey)
    local key = tostring(stageKey or "00")
    local cache = ensureDisplayBatteryTextureCache()
    if cache[key] == nil and getTexture then
        cache[key] = getTexture("media/textures/UI/CDPlayer/NM_UI_CDPlayer_Display_Battery_" .. key .. ".png") or false
    end
    if cache[key] == false then
        return nil
    end
    return cache[key]
end

local function formatDisplayClockText()
    local gameTime = getGameTime and getGameTime() or nil
    if not gameTime then
        return nil
    end

    local hour = nil
    if gameTime.getHour then
        hour = tonumber(gameTime:getHour())
    elseif gameTime.getHours then
        hour = tonumber(gameTime:getHours())
    end

    local minute = nil
    if gameTime.getMinutes then
        minute = tonumber(gameTime:getMinutes())
    elseif gameTime.getMinute then
        minute = tonumber(gameTime:getMinute())
    end

    if minute == nil and gameTime.getTimeOfDay then
        local timeOfDay = tonumber(gameTime:getTimeOfDay())
        if timeOfDay then
            local wholeHour = math.floor(timeOfDay)
            hour = hour or wholeHour
            minute = math.floor((timeOfDay - wholeHour) * 60 + 0.5)
        end
    end

    hour = math.floor(tonumber(hour) or 0)
    minute = math.floor(tonumber(minute) or 0)

    if hour < 0 then
        hour = 0
    elseif hour >= 24 then
        hour = hour % 24
    end
    if minute < 0 then
        minute = 0
    elseif minute >= 60 then
        minute = minute % 60
    end

    local meridiem = hour >= 12 and "PM" or "AM"
    local hour12 = hour % 12
    if hour12 == 0 then
        hour12 = 12
    end
    return string.format("%d:%02d %s", hour12, minute, meridiem)
end

local function getRectCenterForOddSizedTexture(rect)
    local x = tonumber(rect and rect.x) or 0
    local y = tonumber(rect and rect.y) or 0
    local w = tonumber(rect and rect.w) or 0
    local h = tonumber(rect and rect.h) or 0
    return x + math.floor(math.max(0, w - 1) * 0.5 + 0.5), y + math.floor(math.max(0, h - 1) * 0.5 + 0.5)
end

local function getSideTexture(key, path)
    local cache = ensureSideTextureCache()
    local cacheKey = tostring(key or "") .. "|" .. tostring(path or "")
    if cache[cacheKey] == nil and getTexture then
        cache[cacheKey] = getTexture(path) or false
    end
    if cache[cacheKey] == false then
        return nil
    end
    return cache[cacheKey]
end

local function resolveFrontVariantFromFullType(fullType)
    local key = tostring(fullType or "")
    if key == "" then
        return FRONT_TEXTURE_FALLBACK_VARIANT
    end
    local short = key:match("([^%.]+)$") or key
    return FRONT_VARIANTS[short] or FRONT_TEXTURE_FALLBACK_VARIANT
end

local function getFrontTexture(variant)
    local resolvedVariant = tostring(variant or FRONT_TEXTURE_FALLBACK_VARIANT or "Cyan")
    local cache = ensureFrontTextureCache()
    if cache[resolvedVariant] == nil and getTexture then
        local path = tostring(FRONT_TEXTURE_PATH_PREFIX or "") .. resolvedVariant .. ".png"
        cache[resolvedVariant] = getTexture(path) or false
    end
    if cache[resolvedVariant] == false then
        return nil
    end
    return cache[resolvedVariant]
end

local function getLidTexture(variant)
    local resolvedVariant = tostring(variant or FRONT_TEXTURE_FALLBACK_VARIANT or "Cyan")
    local cache = ensureLidTextureCache()
    if cache[resolvedVariant] == nil and getTexture then
        local path = tostring(LID_TEXTURE_PATH_PREFIX or "") .. resolvedVariant .. ".png"
        cache[resolvedVariant] = getTexture(path) or false
    end
    if cache[resolvedVariant] == false then
        return nil
    end
    return cache[resolvedVariant]
end

local function resolveCloseTintForVariant(variant)
    local resolvedVariant = tostring(variant or FRONT_TEXTURE_FALLBACK_VARIANT or "Cyan")
    if resolvedVariant == "Cow" or resolvedVariant == "Black" then
        return 1.0, 1.0, 1.0, 1.0
    end
    return CLOSE_TEXTURE_ALPHA, CLOSE_TEXTURE_TINT_R, CLOSE_TEXTURE_TINT_G, CLOSE_TEXTURE_TINT_B
end

function CDPlayerWindow:getFrontVariant()
    local item = self.target and self.target.itemRef or nil
    if item and item.getFullType then
        return resolveFrontVariantFromFullType(item:getFullType())
    end

    local resolved = self.resolveContextCached and self:resolveContextCached() or nil
    local resolvedItem = resolved and resolved.item or nil
    if resolvedItem and resolvedItem.getFullType then
        return resolveFrontVariantFromFullType(resolvedItem:getFullType())
    end

    return FRONT_TEXTURE_FALLBACK_VARIANT
end

function CDPlayerWindow:usesTealButtonVariant()
    return self:getFrontVariant() == "White"
end

function CDPlayerWindow:getButtonTexturePath(kind)
    if self:usesTealButtonVariant() == true then
        if kind == "prev" then
            return "media/textures/UI/CDPlayer/NM_UI_CDPlayer_Teal_Button_Prev.png"
        elseif kind == "next" then
            return "media/textures/UI/CDPlayer/NM_UI_CDPlayer_Teal_Button_Next.png"
        elseif kind == "vol_up" then
            return "media/textures/UI/CDPlayer/NM_UI_CDPlayer_Teal_Button_Vol_Up.png"
        elseif kind == "vol_down" then
            return "media/textures/UI/CDPlayer/NM_UI_CDPlayer_Teal_Button_Vol_Down.png"
        elseif kind == "play_stop" then
            return "media/textures/UI/CDPlayer/NM_UI_CDPlayer_Teal_Button_Play_Stop.png"
        end
    end

    if kind == "prev" then
        return BUTTON_TEXTURE_PREV_PATH
    elseif kind == "next" then
        return BUTTON_TEXTURE_NEXT_PATH
    elseif kind == "vol_up" then
        return BUTTON_TEXTURE_VOL_UP_PATH
    elseif kind == "vol_down" then
        return BUTTON_TEXTURE_VOL_DOWN_PATH
    elseif kind == "play_stop" then
        return BUTTON_TEXTURE_PLAY_STOP_PATH
    end
    return nil
end

function CDPlayerWindow:getButtonTexture(kind)
    local cache = ensureButtonTextureCache()
    local path = self:getButtonTexturePath(kind)
    local cacheKey = tostring(kind or "") .. "|" .. tostring(path or "")
    if cache[cacheKey] == nil and getTexture then
        cache[cacheKey] = path and (getTexture(path) or false) or false
    end
    if cache[cacheKey] == false then
        return nil
    end
    return cache[cacheKey]
end

function CDPlayerWindow:getSideButtonTexture()
    local path = SIDE_BUTTON_TEXTURE_PATH
    if self:usesTealButtonVariant() == true then
        path = "media/textures/UI/CDPlayer/NM_UI_CDPlayer_Teal_Button_Power.png"
    end
    return getSideTexture("fg", path)
end

function CDPlayerWindow:getSideButtonBgTexture()
    return getSideTexture("bg", SIDE_BUTTON_BG_TEXTURE_PATH)
end

function CDPlayerWindow:getPowerIndicatorTexture()
    local state = self:getPowerIndicatorState()
    if state == "on" then
        return getSideTexture("indicator_on", POWER_INDICATOR_TEXTURE_ON_PATH)
    end
    if state == "standby" then
        return getSideTexture("indicator_standby", POWER_INDICATOR_TEXTURE_STANDBY_PATH)
    end
    return getSideTexture("indicator_off", POWER_INDICATOR_TEXTURE_OFF_PATH)
end

function CDPlayerWindow:getHoldButtonTexture()
    local path = HOLD_BUTTON_TEXTURE_PATH
    if self:usesTealButtonVariant() == true then
        path = "media/textures/UI/CDPlayer/NM_UI_CDPlayer_Teal_Button_Hold.png"
    end
    return getSideTexture("hold", path)
end

function CDPlayerWindow:getOpenButtonTexture()
    local path = OPEN_BUTTON_TEXTURE_PATH
    if self:usesTealButtonVariant() == true then
        path = "media/textures/UI/CDPlayer/NM_UI_CDPlayer_Teal_Button_Open.png"
    end
    return getSideTexture("open", path)
end

function CDPlayerWindow:getModeLabelTexture()
    return getSideTexture("label_mode", MODE_LABEL_TEXTURE_PATH)
end

function CDPlayerWindow:getHoldLabelTexture()
    return getSideTexture("label_hold", HOLD_LABEL_TEXTURE_PATH)
end

function CDPlayerWindow:getPowerLabelTexture()
    return getSideTexture("label_power", POWER_LABEL_TEXTURE_PATH)
end

function CDPlayerWindow:getDisplaySongLabelRenderState()
    if self:isDisplayPoweredOn() ~= true then
        return nil
    end

    local resolved = self:resolveContextCached()
    local state = resolved and resolved.state or nil
    local fullText = NMReadoutTextResolver and NMReadoutTextResolver.resolveReadoutText
        and NMReadoutTextResolver.resolveReadoutText(state)
        or ""
    fullText = tostring(fullText or "")
    if fullText == "" then
        return nil
    end

    local viewport = self:getDisplayViewportRect()
    local contentW = math.max(1, viewport.w - DISPLAY_TEXT_PAD_LEFT - DISPLAY_TEXT_PAD_RIGHT)
    local nowMs = tonumber(self._nmFrameNowMs) or getNowMs()
    local text = NMReadoutOverflowPager and NMReadoutOverflowPager.resolvePagedText
        and NMReadoutOverflowPager.resolvePagedText(self, fullText, contentW, nowMs)
        or fullText
    local tm = getTextManager and getTextManager() or nil
    local textH = tm and tm.MeasureStringY and tm:MeasureStringY(UIFont.Small, "Ag") or 10
    local textY = viewport.y + viewport.h - DISPLAY_TEXT_PAD_BOTTOM - textH
    local absWindowY = self.getAbsoluteY and self:getAbsoluteY() or self.getY and self:getY() or 0
    local _, screenH = getScreenSize()
    local maxAbsTextY = math.max(0, (tonumber(screenH) or 0) - textH - 2)
    local absTextY = absWindowY + textY
    if absTextY > maxAbsTextY then
        textY = textY - (absTextY - maxAbsTextY)
    end
    local minTextY = viewport.y + DISPLAY_TEXT_PAD_TOP
    if textY < minTextY then
        textY = minTextY
    end
    return {
        text = tostring(text or ""),
        x = viewport.x + DISPLAY_TEXT_PAD_LEFT,
        y = textY,
        fullText = fullText,
        contentW = contentW,
        color = DISPLAY_TEXT_COLOR,
    }
end

function CDPlayerWindow:getDisplayModeIconRenderState()
    if self:isDisplayPoweredOn() ~= true then
        return nil
    end

    local transport = self:buildTransportState()
    local texture = getDisplayModeIconTexture(transport.playbackPolicy)
    if not texture then
        return nil
    end

    local viewport = self:getDisplayViewportRect()
    return {
        texture = texture,
        x = viewport.x + DISPLAY_MODE_ICON_PAD_LEFT,
        y = viewport.y + DISPLAY_MODE_ICON_PAD_TOP,
        w = DISPLAY_MODE_ICON_TARGET_W,
        h = DISPLAY_MODE_ICON_TARGET_H,
        alpha = DISPLAY_MODE_ICON_ALPHA,
    }
end

function CDPlayerWindow:getDisplayBatteryStageKey()
    if self:isDisplayPoweredOn() ~= true then
        return nil
    end

    local resolved = self:resolveContextCached()
    local state = resolved and resolved.state or nil
    local profile = resolved and resolved.profile or nil
    if not (profile and profile.supportsBattery == true) then
        return nil
    end
    if not (state and state.batteryPresent == true) then
        return nil
    end

    local charge = clamp01(tonumber(state.batteryCharge) or 0.0)
    if charge < DISPLAY_BATTERY_FLASH_THRESHOLD then
        local nowMs = tonumber(self._nmFrameNowMs) or getNowMs()
        local phase = math.floor(nowMs / DISPLAY_BATTERY_FLASH_INTERVAL_MS) % 2
        if phase == 0 then
            return "20"
        end
        return "00"
    end
    if charge > 0.80 then
        return "100"
    end
    if charge > 0.60 then
        return "80"
    end
    if charge > 0.40 then
        return "60"
    end
    if charge > 0.20 then
        return "40"
    end
    return "20"
end

function CDPlayerWindow:getDisplayBatteryIndicatorRenderState()
    local stageKey = self:getDisplayBatteryStageKey()
    if not stageKey then
        return nil
    end

    local texture = getDisplayBatteryTexture(stageKey)
    if not texture then
        return nil
    end

    local viewport = self:getDisplayViewportRect()
    return {
        texture = texture,
        x = viewport.x + viewport.w - DISPLAY_BATTERY_PAD_RIGHT - DISPLAY_BATTERY_W,
        y = viewport.y + DISPLAY_BATTERY_PAD_TOP,
        w = DISPLAY_BATTERY_W,
        h = DISPLAY_BATTERY_H,
    }
end

function CDPlayerWindow:getDisplayClockRenderState()
    if self:isDisplayPoweredOn() ~= true then
        return nil
    end

    local text = formatDisplayClockText()
    if not text or text == "" then
        return nil
    end

    local viewport = self:getDisplayViewportRect()
    local tm = getTextManager and getTextManager() or nil
    local textW = tm and tm.MeasureStringX and tm:MeasureStringX(UIFont.Small, text) or (#text * 6)
    return {
        text = text,
        x = viewport.x + math.floor((viewport.w - textW) * 0.5 + 0.5) + DISPLAY_CLOCK_OFFSET_X,
        y = viewport.y + DISPLAY_CLOCK_PAD_TOP,
        color = DISPLAY_TEXT_COLOR,
    }
end

function CDPlayerWindow:shouldSpinInsertedCD()
    local timed = self._nmMediaSlotTimedProgress
    if timed and timed.active == true then
        return false
    end
    local transport = self:buildTransportState()
    return transport.isPlaying == true and self:hasInsertedMedia() == true
end

function CDPlayerWindow:prerender()
    self:beginFrameEpoch("prerender")
    local resolved = self:resolveContextCached()
    local nowMs = getNowMs()
    local lastHeadphoneSyncMs = tonumber(self._nmLastHeadphoneWearSyncMs) or 0
    if (nowMs - lastHeadphoneSyncMs) >= 250 then
        self._nmLastHeadphoneWearSyncMs = nowMs
        if self:shouldShowClosedLidSlots() == true
            and NMHeadphoneSlot
            and NMHeadphoneSlot.tickWearSync
            and self.headphoneSlot
            and self.headphoneSlot.button then
            self._nmHeadphoneWearSyncActive = (NMHeadphoneSlot.tickWearSync(self, resolved) == true)
        else
            self._nmHeadphoneWearSyncActive = false
        end
    end
    NMSlotHostLifecycle.refreshSlotVisibility(self)
    ISPanel.prerender(self)
    self:syncHoldButtonAnimation(false)
    if self.backgroundColor then
        self:drawRect(0, 0, self.width, self.height, self.backgroundColor.a, self.backgroundColor.r, self.backgroundColor.g, self.backgroundColor.b)
    end
    if not UI_TEXTURES.base and getTexture then
        UI_TEXTURES.base = getTexture(BASE_TEXTURE_PATH)
    end
    if UI_TEXTURES.base then
        self:drawTextureScaled(UI_TEXTURES.base, 0, 0, self.width, self.height, 1.0, 1.0, 1.0, 1.0)
    end
    local frontTexture = getFrontTexture(self:getFrontVariant())
    if frontTexture then
        local frontRect = self:getFrontRect()
        self:drawTextureScaled(frontTexture, frontRect.x, frontRect.y, frontRect.w, frontRect.h, 1.0, 1.0, 1.0, 1.0)
    end
    local lidTexture = getLidTexture(self:getFrontVariant())
    if self:shouldShowAuxIngressHighlight() == true then
        local auxIngressTexture = getAuxIngressTexture()
        if auxIngressTexture then
            self:drawTextureScaled(auxIngressTexture, 0, 0, self.width, self.height, 1.0, 1.0, 1.0, 1.0)
        end
    end
    local displayTexture = getDisplayTexture(self)
    if displayTexture then
        local displayRect = self:getDisplayRect()
        self:drawTextureScaled(displayTexture, displayRect.x, displayRect.y, displayRect.w, displayRect.h, 1.0, 1.0, 1.0, 1.0)
    end
    local sideBgTexture = self:getSideButtonBgTexture()
    if sideBgTexture then
        local modeBgRect = self:getSideButtonBgRect("mode")
        local powerBgRect = self:getSideButtonBgRect("power")
        self:drawTextureScaled(sideBgTexture, modeBgRect.x, modeBgRect.y, modeBgRect.w, modeBgRect.h, 1.0, 1.0, 1.0, 1.0)
        self:drawTextureScaled(sideBgTexture, powerBgRect.x, powerBgRect.y, powerBgRect.w, powerBgRect.h, 1.0, 1.0, 1.0, 1.0)
    end
    local sideFgTexture = self:getSideButtonTexture()
    if sideFgTexture then
        local sideRenderOrder = { "mode", "power" }
        for i = 1, #sideRenderOrder do
            local kind = sideRenderOrder[i]
            local rect = self:getSideButtonRect(kind)
            if rect then
                local r = 1.0
                local g = 1.0
                local b = 1.0
                if self:isButtonVisuallyPressed(kind) then
                    r = BUTTON_PRESSED_TINT_R
                    g = BUTTON_PRESSED_TINT_G
                    b = BUTTON_PRESSED_TINT_B
                end
                self:drawTextureScaled(sideFgTexture, rect.x, rect.y, rect.w, rect.h, 1.0, r, g, b)
            end
        end
    end
    local indicatorTexture = self:getPowerIndicatorTexture()
    if indicatorTexture then
        local indicatorRect = self:getPowerIndicatorRect()
        self:drawTextureScaled(indicatorTexture, indicatorRect.x, indicatorRect.y, indicatorRect.w, indicatorRect.h, 1.0, 1.0, 1.0, 1.0)
    end
    local holdTexture = self:getHoldButtonTexture()
    if holdTexture then
        local holdRect = self:getHoldButtonRect()
        self:DrawTextureAngle(
            holdTexture,
            holdRect.x + (holdRect.w / 2),
            holdRect.y + (holdRect.h / 2),
            tonumber(self._nmHoldButtonAngle) or HOLD_BUTTON_ANGLE_UPRIGHT
        )
    end
    local openTexture = self:getOpenButtonTexture()
    if openTexture then
        local openRect = self:getOpenButtonRect()
        local r = 1.0
        local g = 1.0
        local b = 1.0
        if self:isButtonVisuallyPressed("open") then
            r = BUTTON_PRESSED_TINT_R
            g = BUTTON_PRESSED_TINT_G
            b = BUTTON_PRESSED_TINT_B
        end
        self:drawTextureScaled(openTexture, openRect.x, openRect.y, openRect.w, openRect.h, 1.0, r, g, b)
    end
    local modeLabelTexture = self:getModeLabelTexture()
    if modeLabelTexture then
        local rect = self:getModeLabelRect()
        self:drawTextureScaled(modeLabelTexture, rect.x, rect.y, rect.w, rect.h, 1.0, 1.0, 1.0, 1.0)
    end
    local holdLabelTexture = self:getHoldLabelTexture()
    if holdLabelTexture then
        local rect = self:getHoldLabelRect()
        self:drawTextureScaled(holdLabelTexture, rect.x, rect.y, rect.w, rect.h, 1.0, 1.0, 1.0, 1.0)
    end
    local powerLabelTexture = self:getPowerLabelTexture()
    if powerLabelTexture then
        local rect = self:getPowerLabelRect()
        self:drawTextureScaled(powerLabelTexture, rect.x, rect.y, rect.w, rect.h, 1.0, 1.0, 1.0, 1.0)
    end
    local timedCDState = self:getTimedCDAnimationState()
    if timedCDState and timedCDState.visible == true and timedCDState.texture then
        self:drawTextureScaled(
            timedCDState.texture,
            timedCDState.x,
            timedCDState.y,
            timedCDState.w,
            timedCDState.h,
            clamp01(tonumber(timedCDState.alpha) or 0.0),
            1.0,
            1.0,
            1.0
        )
    else
        local insertedCDTexture = self:getInsertedCDWorldTexture()
        if insertedCDTexture then
            local cdRect = self:getWorldCDRect()
            if self:shouldSpinInsertedCD() == true then
                local centerX, centerY = getRectCenterForOddSizedTexture(cdRect)
                self:DrawTextureAngle(
                    insertedCDTexture,
                    centerX,
                    centerY,
                    tonumber(self._nmWorldCDSpinAngle) or 0.0
                )
            else
                self:drawTextureScaled(insertedCDTexture, cdRect.x, cdRect.y, cdRect.w, cdRect.h, 1.0, 1.0, 1.0, 1.0)
            end
        end
    end
    if lidTexture then
        local lidState = self:getLidRenderState()
        self:drawTextureScaled(lidTexture, lidState.x, lidState.y, lidState.w, lidState.h, 1.0, 1.0, 1.0, 1.0)
    end
    local renderOrder = { "vol_up", "prev", "next", "vol_down", "play_stop" }
    for i = 1, #renderOrder do
        local kind = renderOrder[i]
        local texture = self:getButtonTexture(kind)
        local rect = self:getButtonRenderRect(kind)
        if texture and rect then
            local r = 1.0
            local g = 1.0
            local b = 1.0
            if self.isButtonVisuallyPressed and self:isButtonVisuallyPressed(kind) then
                r = BUTTON_PRESSED_TINT_R
                g = BUTTON_PRESSED_TINT_G
                b = BUTTON_PRESSED_TINT_B
            end
            self:drawTextureScaled(texture, rect.x, rect.y, rect.w, rect.h, 1.0, r, g, b)
        end
    end
end

function CDPlayerWindow:render()
    if not UI_TEXTURES.close and getTexture then
        UI_TEXTURES.close = getTexture(CLOSE_TEXTURE_PATH)
    end
    ISPanel.render(self)
    local displayBattery = self:getDisplayBatteryIndicatorRenderState()
    if displayBattery then
        self:drawTextureScaled(
            displayBattery.texture,
            displayBattery.x,
            displayBattery.y,
            displayBattery.w,
            displayBattery.h,
            1.0,
            1.0,
            1.0,
            1.0
        )
    end
    local displayModeIcon = self:getDisplayModeIconRenderState()
    if displayModeIcon then
        self:drawTextureScaled(
            displayModeIcon.texture,
            displayModeIcon.x,
            displayModeIcon.y,
            displayModeIcon.w,
            displayModeIcon.h,
            displayModeIcon.alpha,
            1.0,
            1.0,
            1.0
        )
    end
    if UI_TEXTURES.close then
        local closeRect = self:getCloseRect()
        local closeAlpha, closeR, closeG, closeB = resolveCloseTintForVariant(self:getFrontVariant())
        self:drawTextureScaled(
            UI_TEXTURES.close,
            closeRect.x,
            closeRect.y,
            closeRect.w,
            closeRect.h,
            closeAlpha,
            closeR,
            closeG,
            closeB
        )
    end
end
