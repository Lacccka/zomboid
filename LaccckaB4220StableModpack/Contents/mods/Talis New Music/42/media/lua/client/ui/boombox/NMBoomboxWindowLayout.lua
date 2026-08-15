local env = _G.NMBoomboxWindowEnv
setfenv(1, env)

local MAIN_BUTTON_ORDER = { "play", "stop", "prev", "next", "eject" }
local TOP_BUTTON_ORDER = { "play", "stop", "prev", "next" }

function BoomboxWindow:getHeaderRect()
    return { x = HEADER_X, y = HEADER_Y, w = HEADER_W, h = HEADER_H }
end

function BoomboxWindow:getHeaderLeftStripRect()
    return {
        x = BASE_X,
        y = HEADER_Y,
        w = HEADER_SIDE_STRIP_W,
        h = math.max(0, (BASE_Y + BASE_H) - HEADER_Y)
    }
end

function BoomboxWindow:getHeaderRightStripRect()
    return {
        x = (BASE_X + BASE_W) - HEADER_SIDE_STRIP_W,
        y = HEADER_Y,
        w = HEADER_SIDE_STRIP_W,
        h = math.max(0, (BASE_Y + BASE_H) - HEADER_Y)
    }
end

function BoomboxWindow:getCloseRect()
    return { x = CLOSE_X, y = CLOSE_Y, w = CLOSE_W, h = CLOSE_H }
end

function BoomboxWindow:getPowerSwitchBgRect()
    return { x = POWER_BG_X, y = POWER_BG_Y, w = POWER_BG_W, h = POWER_BG_H }
end

function BoomboxWindow:getPowerSwitchSlideRect(isOn)
    local slideY = POWER_BG_Y + POWER_SLIDE_OFFSET_Y - ((isOn == true) and POWER_SLIDE_TRAVEL_Y or 0)
    return {
        x = POWER_BG_X + POWER_SLIDE_OFFSET_X,
        y = slideY,
        w = POWER_SLIDE_W,
        h = POWER_SLIDE_H,
    }
end

function BoomboxWindow:getCollapsedVisibleHeight()
    local baseVisibleHeight = 30
    local topButtonVisibleHeight = TOP_BUTTON_CANVAS_INSET_Y + TOP_BUTTON_H + 2
    return math.max(baseVisibleHeight, topButtonVisibleHeight)
end

function BoomboxWindow:getDefaultExpandedY()
    local _, screenH = getScreenSize()
    return math.max(0, screenH - EXPANDED_BOTTOM_MARGIN - self.height)
end

function BoomboxWindow:isHeaderHit(x, y)
    return pointInRect(x, y, self:getHeaderRect())
        or pointInRect(x, y, self:getHeaderLeftStripRect())
        or pointInRect(x, y, self:getHeaderRightStripRect())
end

function BoomboxWindow:clampWindowY(value)
    local _, screenH = getScreenSize()
    local maxY = math.max(0, screenH - self.height)
    return math.max(0, math.min(math.floor((tonumber(value) or 0) + 0.5), maxY))
end

function BoomboxWindow:getExpandedY()
    local stored = tonumber(self._nmExpandedY)
    if stored == nil then
        stored = self:getDefaultExpandedY()
    end
    return self:clampWindowY(stored)
end

function BoomboxWindow:getCollapsedY()
    local _, screenH = getScreenSize()
    return math.max(0, screenH - self:getCollapsedVisibleHeight())
end

function BoomboxWindow:getStateY(collapsed)
    return collapsed == true and self:getCollapsedY() or self:getExpandedY()
end

function BoomboxWindow:clampWindowX(value)
    local screenW = getScreenSize()
    local maxX = math.max(0, screenW - self.width)
    return math.max(0, math.min(math.floor((tonumber(value) or 0) + 0.5), maxX))
end

function BoomboxWindow:snapToState(collapsed)
    NMFancyWindowChrome.snapToState(self, collapsed, persistWindowState)
end

function BoomboxWindow:startCollapseAnimation(collapsed)
    NMFancyWindowChrome.startCollapseAnimation(self, collapsed, persistWindowState)
end

function BoomboxWindow:toggleCollapsed()
    NMFancyWindowChrome.toggleCollapsed(self, persistWindowState)
end

function BoomboxWindow:finishHeaderInteraction()
    NMFancyWindowChrome.finishHeaderInteraction(self)
end

function BoomboxWindow:updateHeaderDrag()
    return NMFancyWindowChrome.updateHeaderDrag(self, DRAG_THRESHOLD_X, persistWindowState)
end

function BoomboxWindow:getSlotRect(index)
    local idx = math.max(1, math.floor(tonumber(index) or 1))
    return { x = SLOT_X + ((idx - 1) * (SLOT_SIZE + SLOT_GAP_X)), y = SLOT_Y, w = SLOT_SIZE, h = SLOT_SIZE }
end

function BoomboxWindow:getVolumeBgRect()
    return { x = VOLUME_BG_X, y = VOLUME_BG_Y, w = VOLUME_BG_W, h = VOLUME_BG_H }
end

function BoomboxWindow:getVolumeKnobRect()
    return { x = VOLUME_KNOB_X, y = VOLUME_KNOB_Y, w = VOLUME_KNOB_W, h = VOLUME_KNOB_H }
end

function BoomboxWindow:getVolumeLabelText()
    local volume = self._nmKnobDragging == true and self._nmKnobPreviewVolume or self._nmKnobStableVolume
    return string.format("%d%%", volumeToPercent(volume or 1.0))
end

function BoomboxWindow:getVolumeLabelRect()
    local knobRect = self:getVolumeBgRect()
    local text = self:getVolumeLabelText()
    local tm = getTextManager and getTextManager() or nil
    local textW = tm and tm.MeasureStringX and tm:MeasureStringX(UIFont.Small, text) or (#text * 6)
    local textH = tm and tm.MeasureStringY and tm:MeasureStringY(UIFont.Small, "Ag") or 10
    return {
        x = knobRect.x + math.floor(((knobRect.w - textW) * 0.5) + 0.5),
        y = knobRect.y - textH - VOLUME_LABEL_GAP_Y,
        w = textW,
        h = textH
    }
end

function BoomboxWindow:getVolumeKnobAngle(volume)
    local pct = clamp01(volume or 0.0)
    local startAngle = tonumber(VOLUME_MIN_ANGLE) or 76
    local endAngle = tonumber(VOLUME_MAX_ANGLE) or 10
    if endAngle <= startAngle then
        endAngle = endAngle + 360
    end
    local angle = startAngle + ((endAngle - startAngle) * pct)
    if angle >= 360 then
        angle = angle - 360
    end
    return angle
end

function BoomboxWindow:getMainButtonIndex(kind)
    for i = 1, #MAIN_BUTTON_ORDER do
        if MAIN_BUTTON_ORDER[i] == kind then
            return i
        end
    end
    return 1
end

function BoomboxWindow:getTopButtonIndex(kind)
    for i = 1, #TOP_BUTTON_ORDER do
        if TOP_BUTTON_ORDER[i] == kind then
            return i
        end
    end
    return 1
end

function BoomboxWindow:getMainButtonRect(kind)
    local idx = self:getMainButtonIndex(kind)
    return {
        x = MAIN_BUTTON_X + ((idx - 1) * (MAIN_BUTTON_W + MAIN_BUTTON_GAP_X)),
        y = MAIN_BUTTON_Y,
        w = MAIN_BUTTON_W,
        h = MAIN_BUTTON_H
    }
end

function BoomboxWindow:getTopCollapsedButtonRect(kind)
    local idx = self:getTopButtonIndex(kind)
    local offset = tonumber(self._nmTopButtonCurrentOffsetByKind and self._nmTopButtonCurrentOffsetByKind[kind]) or TOP_BUTTON_RETRACT_Y
    local press = tonumber(self._nmTopButtonPressOffsetByKind and self._nmTopButtonPressOffsetByKind[kind]) or 0
    return {
        x = TOP_BUTTON_X + ((idx - 1) * (TOP_BUTTON_W + TOP_BUTTON_GAP_X)),
        y = TOP_BUTTON_CANVAS_INSET_Y + offset + press,
        w = TOP_BUTTON_W,
        h = TOP_BUTTON_H
    }
end

function BoomboxWindow:getModeButtonRect(index)
    local idx = math.max(1, math.min(3, math.floor(tonumber(index) or 1)))
    return { x = MODE_X, y = MODE_Y + ((idx - 1) * (MODE_H + MODE_GAP_Y)), w = MODE_W, h = MODE_H }
end

function BoomboxWindow:getCassetteDisplayRect()
    return { x = CASSETTE_X, y = CASSETTE_Y, w = CASSETTE_W, h = CASSETTE_H }
end

function BoomboxWindow:getCassetteLabelRect()
    return { x = CASSETTE_LABEL_X, y = CASSETTE_LABEL_Y, w = CASSETTE_LABEL_W, h = CASSETTE_LABEL_H }
end

function BoomboxWindow:getCassetteSpoolRect(index, timedState)
    local state = timedState or self:getTimedCassetteAnimationState()
    local cassetteX = tonumber(state and state.x) or CASSETTE_X
    local cassetteY = tonumber(state and state.y) or CASSETTE_Y
    if math.floor(tonumber(index) or 1) == 2 then
        return { x = cassetteX + (CASSETTE_SPOOL_B_X - CASSETTE_X), y = cassetteY + (CASSETTE_SPOOL_B_Y - CASSETTE_Y), w = CASSETTE_SPOOL_W, h = CASSETTE_SPOOL_H }
    end
    return { x = cassetteX + (CASSETTE_SPOOL_A_X - CASSETTE_X), y = cassetteY + (CASSETTE_SPOOL_A_Y - CASSETTE_Y), w = CASSETTE_SPOOL_W, h = CASSETTE_SPOOL_H }
end
