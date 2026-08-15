local env = _G.NMBoomboxWindowEnv
setfenv(1, env)

local BUTTON_TEXTURE_BY_KIND = {
    play = "play",
    stop = "stop",
    prev = "prev",
    next = "next",
    eject = "eject",
}

local TOP_BUTTON_TEXTURE_BY_KIND = {
    play = "topPlay",
    stop = "topStop",
    prev = "topPrev",
    next = "topNext",
}

local function drawTextureScaledSafe(window, texture, rect, alpha, r, g, b)
    if texture and rect then
        window:drawTextureScaled(texture, rect.x, rect.y, rect.w, rect.h, alpha or 1.0, r or 1.0, g or 1.0, b or 1.0)
    end
end

local function resolveCloseTint(model)
    local variant = tostring(model and model.variant or "")
    if variant == "Oddbolt" or variant == "Black" then
        return 1.0, 1.0, 1.0
    end
    return CLOSE_TINT.r, CLOSE_TINT.g, CLOSE_TINT.b
end

local function mainButtonPressed(window, kind)
    if kind == "play" then
        return window._nmMainButtonDownByKind.play == true
    end
    local pulse = window._nmMainButtonPulseByKind[kind]
    return pulse and pulse.phase == "down"
end

local function drawMainButton(window, textures, kind)
    local rect = window:getMainButtonRect(kind)
    local isPressed = mainButtonPressed(window, kind)
    local topRect = { x = rect.x, y = rect.y, w = rect.w, h = MAIN_BUTTON_TOP_H }
    local midH = isPressed and MAIN_BUTTON_MID_PRESSED_H or MAIN_BUTTON_MID_H
    local midRect = { x = rect.x, y = rect.y + MAIN_BUTTON_TOP_H, w = rect.w, h = midH }
    local bottomRect = { x = rect.x, y = rect.y + MAIN_BUTTON_TOP_H + MAIN_BUTTON_MID_H, w = rect.w, h = isPressed and 0 or MAIN_BUTTON_BOTTOM_H }
    local tint = isPressed and 0.78 or 1.0
    local topTexture = kind == "prev" and textures.buttonTopPrev or textures.buttonTop
    local bottomTexture = kind == "prev" and textures.buttonBottomPrev or textures.buttonBottom
    drawTextureScaledSafe(window, topTexture, topRect, 1.0, tint, tint, tint)
    drawTextureScaledSafe(window, textures[BUTTON_TEXTURE_BY_KIND[kind]], midRect, 1.0, tint, tint, tint)
    if bottomRect.h > 0 then
        drawTextureScaledSafe(window, bottomTexture, bottomRect, 1.0, 1.0, 1.0, 1.0)
    end
end

local function drawTopCollapsedButton(window, textures, kind)
    if window.isCollapsed ~= true then
        return
    end
    local rect = window:getTopCollapsedButtonRect(kind)
    drawTextureScaledSafe(window, textures[TOP_BUTTON_TEXTURE_BY_KIND[kind]], rect, 1.0, 1.0, 1.0, 1.0)
end

local function drawCassetteLabel(window, labelState)
    if not labelState then
        return
    end
    local labelRect = labelState.rect
    window:drawRect(labelRect.x, labelRect.y, labelRect.w, labelRect.h, CASSETTE_LABEL_BG.a, CASSETTE_LABEL_BG.r, CASSETTE_LABEL_BG.g, CASSETTE_LABEL_BG.b)
    local tm = getTextManager and getTextManager() or nil
    local textH = tm and tm.MeasureStringY and tm:MeasureStringY(UIFont.Small, "Ag") or 10
    local textY = labelRect.y + math.floor(((labelRect.h - textH) * 0.5) + 0.5)
    window:drawText(labelState.text, labelRect.x + CASSETTE_LABEL_TEXT_PAD_X, textY, CASSETTE_LABEL_TEXT_COLOR.r, CASSETTE_LABEL_TEXT_COLOR.g, CASSETTE_LABEL_TEXT_COLOR.b, CASSETTE_LABEL_TEXT_COLOR.a, UIFont.Small)
end

local function resolveVisibleCassetteState(model)
    local timedCassetteState = model and model.timedCassetteState or nil
    if timedCassetteState and timedCassetteState.visible == true then
        return timedCassetteState, true
    end
    return model and model.cassetteMediaState or nil, false
end

local function drawCassetteAssembly(window, model, textures)
    local cassetteState, timedVisible = resolveVisibleCassetteState(model)
    if cassetteState and cassetteState.visible == true and cassetteState.texture then
        drawTextureScaledSafe(window, cassetteState.texture, cassetteState, cassetteState.alpha or 1.0)
    end
    if timedVisible ~= true then
        drawCassetteLabel(window, model and model.cassetteLabelState or nil)
    end
    if cassetteState and cassetteState.visible == true and textures.spool then
        local leftRect = window:getCassetteSpoolRect(1, cassetteState)
        local rightRect = window:getCassetteSpoolRect(2, cassetteState)
        window:DrawTextureAngle(textures.spool, leftRect.x + (leftRect.w / 2), leftRect.y + (leftRect.h / 2), tonumber(window._nmLeftSpoolAngle) or 0.0)
        window:DrawTextureAngle(textures.spool, rightRect.x + (rightRect.w / 2), rightRect.y + (rightRect.h / 2), tonumber(window._nmRightSpoolAngle) or 0.0)
    end
end

function BoomboxWindow:prerender()
    self:beginFrameEpoch("prerender")
    local resolved = self:resolveContextCached()
    self:syncPowerSwitchFromTransport(resolved, false)
    local model = self:getRenderModel()
    self:syncPlayButtonFromTransport(resolved, false)
    ISPanel.prerender(self)
    local textures = model and model.textures or self:resolveBoomboxUITextures()

    drawTopCollapsedButton(self, textures, "play")
    drawTopCollapsedButton(self, textures, "stop")
    drawTopCollapsedButton(self, textures, "prev")
    drawTopCollapsedButton(self, textures, "next")

    drawTextureScaledSafe(self, textures.base, { x = BASE_X, y = BASE_Y, w = BASE_W, h = BASE_H })
    drawTextureScaledSafe(self, textures.front, { x = FRONT_X, y = FRONT_Y, w = FRONT_W, h = FRONT_H })
    drawTextureScaledSafe(self, textures.powerBg, self:getPowerSwitchBgRect())
    drawTextureScaledSafe(self, textures.powerSlide, self:getPowerSwitchSlideRect(model and model.powerSwitchOn == true))
    drawCassetteAssembly(self, model, textures)
end

function BoomboxWindow:render()
    ISPanel.render(self)
    local model = self:getRenderModel()
    local textures = model and model.textures or self:resolveBoomboxUITextures()
    local volumeBgRect = self:getVolumeBgRect()
    drawTextureScaledSafe(self, textures.volumeBg, volumeBgRect)
    if textures.volumeKnob then
        local knobRect = self:getVolumeKnobRect()
        self:DrawTextureAngle(textures.volumeKnob, knobRect.x + (knobRect.w / 2), knobRect.y + (knobRect.h / 2), tonumber(model and model.wheelAngle) or 0.0)
    end

    if model and model.lidEdgeState and model.lidEdgeState.visible == true then
        drawTextureScaledSafe(self, model.lidEdgeState.texture, model.lidEdgeState)
    end
    if model and model.lidState then
        drawTextureScaledSafe(self, model.lidState.texture, model.lidState)
    end
    if model and model.lidIngressVisible == true then
        local ingressRect = self:getLidIngressZoneRect()
        self:drawRectBorder(ingressRect.x, ingressRect.y, ingressRect.w, ingressRect.h, LID_INGRESS_BORDER.a, LID_INGRESS_BORDER.r, LID_INGRESS_BORDER.g, LID_INGRESS_BORDER.b)
    end

    drawMainButton(self, textures, "play")
    drawMainButton(self, textures, "stop")
    drawMainButton(self, textures, "prev")
    drawMainButton(self, textures, "next")
    drawMainButton(self, textures, "eject")

    local closeR, closeG, closeB = resolveCloseTint(model)
    drawTextureScaledSafe(self, textures.close, self:getCloseRect(), 1.0, closeR, closeG, closeB)

    for i = 1, 3 do
        local rect = self:getModeButtonRect(i)
        drawTextureScaledSafe(self, textures.modeBg, rect)
        local selected = self:getSelectedModeIndex() == i
        local buttonRect = selected and { x = rect.x + 1, y = rect.y + 1, w = rect.w - 2, h = rect.h - 2 } or rect
        local tint = selected and MODE_BUTTON_SELECTED_TINT or 1.0
        drawTextureScaledSafe(self, textures.modeButton, buttonRect, 1.0, tint, tint, tint)
        local icon = textures.modeIcons and textures.modeIcons[i] or nil
        if icon then
            local iw = icon.getWidthOrig and icon:getWidthOrig() or 16
            local ih = icon.getHeightOrig and icon:getHeightOrig() or 16
            local ix = rect.x + math.floor(((rect.w - iw) * 0.5) + 0.5)
            local iy = rect.y + math.floor(((rect.h - ih) * 0.5) + 0.5)
            local iconAlpha = selected and math.min(1.0, MODE_ICON_ALPHA + 0.08) or MODE_ICON_ALPHA
            local iconTint = selected and 0.0 or 0.08
            self:drawTextureScaled(icon, ix, iy, iw, ih, iconAlpha, iconTint, iconTint, iconTint)
        end
    end

    if model and model.volumeLabelVisible == true then
        local labelRect = model.volumeLabelRect
        self:drawText(model.volumeLabelText, labelRect.x, labelRect.y, 1.0, 1.0, 1.0, 1.0, UIFont.Small)
    end
end
