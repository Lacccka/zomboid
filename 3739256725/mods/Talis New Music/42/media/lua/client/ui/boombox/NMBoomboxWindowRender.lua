local env = _G.NMBoomboxWindowEnv
setfenv(1, env)

local FancySettingsWindow = require "ui/shared/host/NMFancySettingsWindow"

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

local function drawTextureScaledAngleSafe(window, texture, rect, angle, alpha, r, g, b)
    if NMFancyDeviceUiScale and NMFancyDeviceUiScale.drawTextureScaledAngle then
        return NMFancyDeviceUiScale.drawTextureScaledAngle(window, texture, rect, angle, alpha, r, g, b)
    end
    return drawTextureScaledSafe(window, texture, rect, alpha, r, g, b)
end

local function offsetRectY(rect, offsetY)
    if not rect or offsetY == 0 then
        return rect
    end
    return {
        x = rect.x,
        y = rect.y + offsetY,
        w = rect.w,
        h = rect.h,
    }
end

local function offsetStateRectY(state, offsetY)
    if not state or offsetY == 0 then
        return state
    end
    local copy = {}
    for k, v in pairs(state) do
        copy[k] = v
    end
    if copy.y ~= nil then
        copy.y = copy.y + offsetY
    end
    if copy.rect then
        copy.rect = offsetRectY(copy.rect, offsetY)
    end
    return copy
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
        local pulse = window._nmMainButtonPulseByKind[kind]
        return window._nmMainButtonDownByKind.play == true or (pulse and pulse.phase == "down")
    end
    local pulse = window._nmMainButtonPulseByKind[kind]
    return pulse and pulse.phase == "down"
end

local function drawMainButton(window, textures, kind, canvasOffsetY)
    local rect = offsetRectY(window:getMainButtonRect(kind), canvasOffsetY)
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
    local rect = window:getTopCollapsedButtonVisualRect(kind)
    drawTextureScaledSafe(window, textures[TOP_BUTTON_TEXTURE_BY_KIND[kind]], rect, 1.0, 1.0, 1.0, 1.0)
end

local function drawCassetteLabel(window, labelState, canvasOffsetY)
    if not labelState then
        return
    end
    local labelRect = offsetRectY(labelState.rect, canvasOffsetY)
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

local function drawCassetteAssembly(window, model, textures, canvasOffsetY)
    local cassetteState, timedVisible = resolveVisibleCassetteState(model)
    cassetteState = offsetStateRectY(cassetteState, canvasOffsetY)
    if cassetteState and cassetteState.visible == true and cassetteState.texture then
        drawTextureScaledSafe(window, cassetteState.texture, cassetteState, cassetteState.alpha or 1.0)
    end
    if timedVisible ~= true then
        drawCassetteLabel(window, model and model.cassetteLabelState or nil, canvasOffsetY)
    end
    if cassetteState and cassetteState.visible == true and textures.spool then
        local leftRect = window:getCassetteSpoolRect(1, cassetteState)
        local rightRect = window:getCassetteSpoolRect(2, cassetteState)
        drawTextureScaledAngleSafe(window, textures.spool, leftRect, tonumber(window._nmLeftSpoolAngle) or 0.0)
        drawTextureScaledAngleSafe(window, textures.spool, rightRect, tonumber(window._nmRightSpoolAngle) or 0.0)
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
    local canvasOffsetY = self:getCanvasOffsetY()

    drawTopCollapsedButton(self, textures, "play")
    drawTopCollapsedButton(self, textures, "stop")
    drawTopCollapsedButton(self, textures, "prev")
    drawTopCollapsedButton(self, textures, "next")

    drawTextureScaledSafe(self, textures.base, { x = BASE_X, y = BASE_Y + canvasOffsetY, w = BASE_W, h = BASE_H })
    drawTextureScaledSafe(self, textures.front, { x = FRONT_X, y = FRONT_Y + canvasOffsetY, w = FRONT_W, h = FRONT_H })
    drawTextureScaledSafe(self, textures.powerBg, self:getPowerSwitchBgRect())
    drawTextureScaledSafe(self, textures.powerSlide, self:getPowerSwitchSlideRect(model and model.powerSwitchOn == true))
    drawCassetteAssembly(self, model, textures, canvasOffsetY)
end

function BoomboxWindow:render()
    ISPanel.render(self)
    local model = self:getRenderModel()
    local textures = model and model.textures or self:resolveBoomboxUITextures()
    local canvasOffsetY = self:getCanvasOffsetY()
    local volumeBgRect = offsetRectY(self:getVolumeBgRect(), canvasOffsetY)
    drawTextureScaledSafe(self, textures.volumeBg, volumeBgRect)
    if textures.volumeKnob then
        local knobRect = offsetRectY(self:getVolumeKnobRect(), canvasOffsetY)
        drawTextureScaledAngleSafe(self, textures.volumeKnob, knobRect, tonumber(model and model.wheelAngle) or 0.0)
    end

    if model and model.lidEdgeState and model.lidEdgeState.visible == true then
        local lidEdgeState = offsetStateRectY(model.lidEdgeState, canvasOffsetY)
        drawTextureScaledSafe(self, lidEdgeState.texture, lidEdgeState)
    end
    if model and model.lidState then
        local lidState = offsetStateRectY(model.lidState, canvasOffsetY)
        drawTextureScaledSafe(self, lidState.texture, lidState)
    end
    if model and model.lidIngressVisible == true then
        local ingressRect = offsetRectY(self:getLidIngressZoneRect(), canvasOffsetY)
        self:drawRectBorder(ingressRect.x, ingressRect.y, ingressRect.w, ingressRect.h, LID_INGRESS_BORDER.a, LID_INGRESS_BORDER.r, LID_INGRESS_BORDER.g, LID_INGRESS_BORDER.b)
    end

    drawMainButton(self, textures, "play", canvasOffsetY)
    drawMainButton(self, textures, "stop", canvasOffsetY)
    drawMainButton(self, textures, "prev", canvasOffsetY)
    drawMainButton(self, textures, "next", canvasOffsetY)
    drawMainButton(self, textures, "eject", canvasOffsetY)

    local closeR, closeG, closeB = resolveCloseTint(model)
    drawTextureScaledSafe(self, textures.close, self:getCloseRect(), 1.0, closeR, closeG, closeB)
    local gearTexture = FancySettingsWindow.getGearTexture and FancySettingsWindow.getGearTexture() or nil
    if gearTexture then
        local gearRect = self:getSettingsRect()
        local gearDrawRect = FancySettingsWindow.getGearDrawRect and FancySettingsWindow.getGearDrawRect(gearRect) or gearRect
        local gearAlpha, gearR, gearG, gearB = FancySettingsWindow.resolveGearTint(self, model)
        drawTextureScaledSafe(self, gearTexture, gearDrawRect, gearAlpha, gearR, gearG, gearB)
    end

    for i = 1, 3 do
        local rect = offsetRectY(self:getModeButtonRect(i), canvasOffsetY)
        drawTextureScaledSafe(self, textures.modeBg, rect)
        local selected = self:getSelectedModeIndex() == i
        local buttonRect = selected and { x = rect.x + 1, y = rect.y + 1, w = rect.w - 2, h = rect.h - 2 } or rect
        local tint = selected and MODE_BUTTON_SELECTED_TINT or 1.0
        drawTextureScaledSafe(self, textures.modeButton, buttonRect, 1.0, tint, tint, tint)
        local icon = textures.modeIcons and textures.modeIcons[i] or nil
        if icon then
            local texW = icon.getWidthOrig and icon:getWidthOrig() or 16
            local texH = icon.getHeightOrig and icon:getHeightOrig() or 16
            local pad = math.max(1, math.floor((2 * getFancyDeviceUiScale()) + 0.5))
            local maxW = math.max(1, rect.w - (pad * 2))
            local maxH = math.max(1, rect.h - (pad * 2))
            local fit = math.min(maxW / math.max(1, texW), maxH / math.max(1, texH))
            local iw = math.floor((texW * fit) + 0.5)
            local ih = math.floor((texH * fit) + 0.5)
            local ix = rect.x + math.floor(((rect.w - iw) * 0.5) + 0.5)
            local iy = rect.y + math.floor(((rect.h - ih) * 0.5) + 0.5)
            local iconAlpha = selected and math.min(1.0, MODE_ICON_ALPHA + 0.08) or MODE_ICON_ALPHA
            local iconTint = selected and 0.0 or 0.08
            self:drawTextureScaled(icon, ix, iy, iw, ih, iconAlpha, iconTint, iconTint, iconTint)
        end
    end

    if model and model.volumeLabelVisible == true then
        local labelRect = offsetRectY(model.volumeLabelRect, canvasOffsetY)
        self:drawText(model.volumeLabelText, labelRect.x, labelRect.y, 1.0, 1.0, 1.0, 1.0, UIFont.Small)
    end
end
