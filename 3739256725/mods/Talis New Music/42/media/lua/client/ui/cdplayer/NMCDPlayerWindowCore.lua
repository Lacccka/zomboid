local env = _G.NMCDPlayerWindowEnv
setfenv(1, env)

local function isFancyUIEnabled()
    if NMRuntimeConfig and NMRuntimeConfig.getFancyUIEnabled then
        return NMRuntimeConfig.getFancyUIEnabled() == true
    end
    return true
end

local function getWindowScreenRect(window, rect)
    if not (window and rect) then
        return nil
    end
    local ax = window.getAbsoluteX and window:getAbsoluteX() or nil
    local ay = window.getAbsoluteY and window:getAbsoluteY() or nil
    local w = tonumber(rect.w) or 0
    local h = tonumber(rect.h) or 0
    if ax == nil or ay == nil or w <= 0 or h <= 0 then
        return nil
    end
    return {
        x = ax + (tonumber(rect.x) or 0),
        y = ay + (tonumber(rect.y) or 0),
        w = w,
        h = h
    }
end

local function ensureDisplaySongLabelPanel(window)
    if not window then
        return
    end
    local existing = window.displaySongLabelPanel
    if existing and existing.javaObject then
        return
    end

    local viewport = window.getDisplayViewportRect and window:getDisplayViewportRect() or nil
    if not viewport then
        return
    end

    local panel = ISPanel:new(viewport.x, viewport.y, viewport.w, viewport.h)
    panel:initialise()
    panel:instantiate()
    panel:noBackground()
    panel.backgroundColor = { a = 0.0, r = 0.0, g = 0.0, b = 0.0 }
    panel.borderColor = { a = 0.0, r = 0.0, g = 0.0, b = 0.0 }
    panel.ownerWindow = window

    panel.render = function(self)
        local owner = self.ownerWindow
        if not owner or owner.isDisplayPoweredOn == nil or owner:isDisplayPoweredOn() ~= true then
            return
        end

        local resolved = owner.resolveContextCached and owner:resolveContextCached() or nil
        local state = resolved and resolved.state or nil
        local fullText = NMReadoutTextResolver and NMReadoutTextResolver.resolveReadoutText
            and NMReadoutTextResolver.resolveReadoutText(state)
            or ""
        fullText = tostring(fullText or "")
        if fullText == "" then
            return
        end

        local contentW = math.max(1, (tonumber(self.width) or 0) - DISPLAY_TEXT_PAD_LEFT - DISPLAY_TEXT_PAD_RIGHT)
        local nowMs = tonumber(owner._nmFrameNowMs) or getNowMs()
        local text = NMReadoutOverflowPager and NMReadoutOverflowPager.resolvePagedText
            and NMReadoutOverflowPager.resolvePagedText(self, fullText, contentW, nowMs)
            or fullText
        local tm = getTextManager and getTextManager() or nil
        local textH = tm and tm.MeasureStringY and tm:MeasureStringY(UIFont.Small, "Ag") or 10
        local textY = math.max(DISPLAY_TEXT_PAD_TOP, (tonumber(self.height) or 0) - DISPLAY_TEXT_PAD_BOTTOM - textH)
        local color = DISPLAY_TEXT_COLOR
        self:drawText(
            tostring(text or ""),
            DISPLAY_TEXT_PAD_LEFT,
            textY,
            color.r,
            color.g,
            color.b,
            color.a,
            UIFont.Small
        )
    end

    window:addChild(panel)
    window.displaySongLabelPanel = panel
end

local function ensureDisplayClockPanel(window)
    if not window then
        return
    end
    local existing = window.displayClockPanel
    if existing and existing.javaObject then
        return
    end

    local viewport = window.getDisplayViewportRect and window:getDisplayViewportRect() or nil
    if not viewport then
        return
    end

    local panel = ISPanel:new(viewport.x, viewport.y, viewport.w, viewport.h)
    panel:initialise()
    panel:instantiate()
    panel:noBackground()
    panel.backgroundColor = { a = 0.0, r = 0.0, g = 0.0, b = 0.0 }
    panel.borderColor = { a = 0.0, r = 0.0, g = 0.0, b = 0.0 }
    panel.ownerWindow = window

    panel.render = function(self)
        local owner = self.ownerWindow
        if not owner or owner.getDisplayClockRenderState == nil then
            return
        end
        local renderState = owner:getDisplayClockRenderState()
        if not renderState then
            return
        end

        local viewportRect = owner.getDisplayViewportRect and owner:getDisplayViewportRect() or nil
        local originX = viewportRect and tonumber(viewportRect.x) or 0
        local originY = viewportRect and tonumber(viewportRect.y) or 0
        local color = renderState.color or DISPLAY_TEXT_COLOR
        self:drawText(
            tostring(renderState.text or ""),
            (tonumber(renderState.x) or 0) - originX,
            (tonumber(renderState.y) or 0) - originY,
            color.r,
            color.g,
            color.b,
            color.a,
            UIFont.Small
        )
    end

    window:addChild(panel)
    window.displayClockPanel = panel
end

local function isWindowVisible(window)
    if not (window and window.javaObject) then
        return false
    end
    if window.getIsVisible then
        return window:getIsVisible() == true
    end
    if window.isVisible then
        return window:isVisible() == true
    end
    return true
end

function getOrCreateWindow(playerNum)
    local key = tostring(tonumber(playerNum) or 0)
    local existing = windowsByPlayer[key]
    if existing and existing.javaObject then
        return existing
    end

    local screenW, screenH = getScreenSize()
    local x = math.max(0, screenW - PANEL_W - DEFAULT_RIGHT_MARGIN)
    local y = math.max(0, screenH - PANEL_H + EXPANDED_BOTTOM_OVERSHOOT - EXPANDED_BOTTOM_MARGIN)
    local win = CDPlayerWindow:new(x, y, PANEL_W, PANEL_H)
    win.playerNum = tonumber(playerNum) or 0
    win:initialise()
    ensureDisplaySongLabelPanel(win)
    ensureDisplayClockPanel(win)
    attachCDPlayerSlots(win)
    win:addToUIManager()
    windowsByPlayer[key] = win
    return win
end

function NMCDPlayerWindow.openForItem(playerNum, item)
    if not isFancyUIEnabled() then
        return nil
    end
    local player = getPlayer(playerNum)
    if not (player and item) then
        return nil
    end
    local profile = NMDeviceProfiles and NMDeviceProfiles.getForItem and NMDeviceProfiles.getForItem(item) or nil
    if not profile or tostring(profile.deviceType or "") ~= "cdplayer" then
        return nil
    end

    local win = getOrCreateWindow(playerNum)
    local currentX = tonumber(win:getX())
    local currentY = tonumber(win._nmExpandedY)
    local screenW, screenH = getScreenSize()
    if currentX == nil then
        currentX = math.max(0, screenW - PANEL_W - DEFAULT_RIGHT_MARGIN)
    end
    if currentY == nil then
        currentY = math.max(0, screenH - PANEL_H + EXPANDED_BOTTOM_OVERSHOOT - EXPANDED_BOTTOM_MARGIN)
    end
    win:setX(win:clampWindowX(currentX))
    win._nmExpandedY = win:clampWindowY(currentY)
    win:snapToState(false)
    win.isLidOpen = false
    win.isLidAnimating = false
    win.lidAnimStartY = nil
    win.lidAnimStartH = nil
    win.lidAnimTargetY = nil
    win.lidAnimTargetH = nil
    win.lidAnimStartTime = nil
    win.lidCurrentY = LID_CLOSED_Y
    win.lidCurrentH = LID_CLOSED_H
    win:syncLidFromMedia(true)
    NMSlotHostLifecycle.refreshSlotVisibility(win)
    win.target = {
        kind = "item",
        itemId = NMCore and NMCore.itemId and NMCore.itemId(item) or nil,
        uuid = NMInventoryHelpers and NMInventoryHelpers.getItemStateUuid and NMInventoryHelpers.getItemStateUuid(item) or nil,
        itemRef = item
    }
    win:invalidateContextCache()
    NMSlotHostLifecycle.refreshSlotVisibility(win)
    win:setVisible(true)
    win:bringToTop()
    if NMGamepadWindowTracker and NMGamepadWindowTracker.markWindow then
        NMGamepadWindowTracker.markWindow(win, "cdplayer")
    end
    if NMPortableMediaDropArbiter and NMPortableMediaDropArbiter.markWindowInteraction then
        NMPortableMediaDropArbiter.markWindowInteraction(win, "cdplayer")
    end
    persistWindowState(win, true)
    return win
end

function NMCDPlayerWindow.findOpenForItem(playerNum, item)
    local itemId = tostring(NMCore and NMCore.itemId and NMCore.itemId(item) or "")
    if itemId == "" then
        return nil
    end
    local win = windowsByPlayer[tostring(playerNum or 0)] or nil
    if not win then
        return nil
    end
    local target = win.target or nil
    local targetItemId = tostring(target and target.itemId or "")
    if targetItemId ~= itemId then
        return nil
    end
    return win
end

function NMCDPlayerWindow.collectOpenMediaIngressZones(playerNum, dragItems)
    local key = tostring(tonumber(playerNum) or 0)
    local win = windowsByPlayer[key] or nil
    if not isWindowVisible(win) then
        return {}
    end
    if not (win.target and win.target.kind == "item") then
        return {}
    end

    local mediaEnv = rawget(_G, "NMMediaSlotEnv") or nil
    local isCompatibleMediaDragFn = mediaEnv and mediaEnv.isCompatibleMediaDrag or nil
    local resolveMediaSlotFullTypeFn = mediaEnv and mediaEnv.resolveMediaSlotFullType or nil
    local queueDraggedMediaInsertFn = mediaEnv and mediaEnv.queueDraggedMediaInsert or nil
    local queueMediaSlotEjectFn = mediaEnv and mediaEnv.queueMediaSlotEject or nil
    local beginMediaExtractDragFn = mediaEnv and (mediaEnv.beginExtractDrag or mediaEnv.beginMediaExtractDrag) or nil
    if not (isCompatibleMediaDragFn and resolveMediaSlotFullTypeFn and queueDraggedMediaInsertFn and queueMediaSlotEjectFn) then
        return {}
    end

    local zones = {}
    local resolved = win.resolveContextCached and win:resolveContextCached() or nil
    local state = resolved and resolved.state or nil
    local mediaSlotRect = win:shouldShowClosedLidSlots() == true and getWindowScreenRect(win, win:getMediaSlotRect()) or nil
    local mediaFullType = resolveMediaSlotFullTypeFn(win, state)

    if mediaSlotRect then
        local zone = NMSlotHostLifecycle.buildSharedMediaSlotZone({
            window = win,
            uiFamily = "cdplayer",
            zoneKind = "slot",
            rect = mediaSlotRect,
            priority = 10,
            zOrder = NMPortableMediaDropArbiter and NMPortableMediaDropArbiter.getWindowZOrder and NMPortableMediaDropArbiter.getWindowZOrder(win) or 0,
            dragItems = dragItems,
            canAcceptDraggedMedia = function(items)
                return win:shouldShowClosedLidSlots() == true
                    and type(items) == "table"
                    and #items > 0
                    and isCompatibleMediaDragFn(win, items)
                    and resolveMediaSlotFullTypeFn(win, state) == ""
            end,
            canEjectMedia = function()
                return mediaFullType ~= ""
            end,
            canStartExtractDrag = function()
                return mediaFullType ~= ""
            end,
            performInsertFromDrag = function(items, sourceTag)
                return queueDraggedMediaInsertFn(win, items, sourceTag or "arbiter") == true
            end,
            performBeginExtract = function()
                if mediaFullType == "" or not beginMediaExtractDragFn then
                    return false
                end
                beginMediaExtractDragFn(win, mediaFullType, "slot")
                return true
            end,
            performEject = function(sourceTag)
                return win:ejectClosedSlotMedia(sourceTag or "arbiter") == true
            end,
            performShowInsertContext = function(btn, xArg, yArg)
                local showMediaInsertContextMenuFn = mediaEnv and mediaEnv.showMediaInsertContextMenu or nil
                if not showMediaInsertContextMenuFn then
                    return false
                end
                return showMediaInsertContextMenuFn(win, btn, xArg, yArg) == true
            end,
            consumeDraggedMediaInsert = function(items, sourceDescriptor)
                return queueDraggedMediaInsertFn(win, items, sourceDescriptor and sourceDescriptor.uiFamily or "handoff") == true
            end,
            beginMediaExtractDrag = function()
                if mediaFullType == "" or not beginMediaExtractDragFn then
                    return false
                end
                beginMediaExtractDragFn(win, mediaFullType, "slot")
                return true
            end,
            handleRightClick = function()
                return win:ejectClosedSlotMedia("handoff") == true
            end
        })
        if zone then
            zones[#zones + 1] = zone
        end
    end

    if win.isLidOpen ~= true or win.isLidAnimating == true then
        return zones
    end

    local auxRect = getWindowScreenRect(win, win:getOpenLidAuxZoneRect())
    if not auxRect then
        return zones
    end

    local canAccept = type(dragItems) == "table"
        and #dragItems > 0
        and isCompatibleMediaDragFn(win, dragItems)
        and mediaFullType == ""

    zones[#zones + 1] = {
            uiFamily = "cdplayer",
            zoneKind = "aux",
            priority = 20,
            zOrder = NMPortableMediaDropArbiter and NMPortableMediaDropArbiter.getWindowZOrder and NMPortableMediaDropArbiter.getWindowZOrder(win) or 0,
            playerNum = tonumber(win.playerNum) or 0,
            itemId = win.target.itemId,
            uuid = win.target.uuid,
            window = win,
            rect = auxRect,
            visible = true,
            enabled = true,
            interactive = true,
            canAccept = canAccept == true,
            containsPoint = function(mx, my)
                local ax = win.getAbsoluteX and win:getAbsoluteX() or 0
                local ay = win.getAbsoluteY and win:getAbsoluteY() or 0
                return win:isOpenLidAuxZoneHit((tonumber(mx) or 0) - ax, (tonumber(my) or 0) - ay)
            end,
            canAcceptDraggedMedia = function(items)
                return win.isLidOpen == true
                    and win.isLidAnimating ~= true
                    and type(items) == "table"
                    and #items > 0
                    and isCompatibleMediaDragFn(win, items)
                    and resolveMediaSlotFullTypeFn(win, state) == ""
            end,
            performInsertFromDrag = function(items, sourceTag)
                return queueDraggedMediaInsertFn(win, items, sourceTag or "arbiter") == true
            end,
            consumeDraggedMediaInsert = function(items, sourceDescriptor)
                return queueDraggedMediaInsertFn(win, items, sourceDescriptor and sourceDescriptor.uiFamily or "handoff") == true
            end,
            performBeginExtract = function()
                if mediaFullType == "" or not beginMediaExtractDragFn then
                    return false
                end
                beginMediaExtractDragFn(win, mediaFullType, "aux")
                return true
            end,
            canEjectMedia = function()
                return mediaFullType ~= ""
            end,
            canStartExtractDrag = function()
                return mediaFullType ~= ""
            end,
            performEject = function(sourceTag)
                return win:ejectOpenLidMediaViaAux(sourceTag or "arbiter") == true
            end,
            performShowInsertContext = function(btn, xArg, yArg)
                local showMediaInsertContextMenuFn = mediaEnv and mediaEnv.showMediaInsertContextMenu or nil
                if not showMediaInsertContextMenuFn then
                    return false
                end
                return showMediaInsertContextMenuFn(win, btn, xArg, yArg) == true
            end,
            beginMediaExtractDrag = function()
                if mediaFullType == "" or not beginMediaExtractDragFn then
                    return false
                end
                beginMediaExtractDragFn(win, mediaFullType, "aux")
                return true
            end,
            handleRightClick = function(btn, xArg, yArg)
                if mediaFullType ~= "" then
                    return win:ejectOpenLidMediaViaAux("handoff") == true
                end
                local showMediaInsertContextMenuFn = mediaEnv and mediaEnv.showMediaInsertContextMenu or nil
                if not showMediaInsertContextMenuFn then
                    return false
                end
                return showMediaInsertContextMenuFn(win, btn, xArg, yArg) == true
            end
        }
    return zones
end

function NMCDPlayerWindow.invalidateOpenItemWindow(itemId, uuid)
    local incomingItemId = tostring(itemId or "")
    local incomingUuid = tostring(uuid or "")
    local invalidated = false
    for _, win in pairs(windowsByPlayer) do
        if win and win.javaObject and win.target and win.target.kind == "item" then
            local targetItemId = tostring(win.target.itemId or "")
            if incomingItemId ~= "" and targetItemId ~= "" and incomingItemId == targetItemId then
                local resolved = win.resolveContextFresh and win:resolveContextFresh() or (win.resolveContext and win:resolveContext()) or nil
                local state = resolved and resolved.state or nil
                local targetUuid = tostring(state and state.deviceUUID or "")
                if incomingUuid == "" or targetUuid == "" or incomingUuid == targetUuid then
                    win:invalidateContextCache()
                    invalidated = true
                end
            end
        end
    end
    return invalidated
end

function NMCDPlayerWindow.rebindOpenPortableItemWindow(itemId, uuid)
    local incomingItemId = tostring(itemId or "")
    local incomingUuid = tostring(uuid or "")
    if incomingItemId == "" and incomingUuid == "" then
        return false
    end
    local rebound = false
    for _, win in pairs(windowsByPlayer) do
        if win and win.javaObject and win.target and win.target.kind == "item" then
            local targetItemId = tostring(win.target.itemId or "")
            local targetUuid = tostring(win.target.uuid or "")
            if targetItemId == "" or incomingItemId == "" or targetItemId == incomingItemId or targetUuid == incomingUuid then
                local player = getPlayer(win.playerNum)
                if player then
                    local item = resolveLiveItemByTarget(player, {
                        itemId = incomingItemId ~= "" and incomingItemId or targetItemId,
                        uuid = incomingUuid ~= "" and incomingUuid or targetUuid
                    })
                    if item then
                        win.target.itemId = NMCore.itemId(item)
                        win.target.uuid = NMInventoryHelpers and NMInventoryHelpers.getItemStateUuid and NMInventoryHelpers.getItemStateUuid(item) or incomingUuid
                        win.target.itemRef = item
                        win:invalidateContextCache()
                        rebound = true
                    end
                end
            end
        end
    end
    return rebound
end

function NMCDPlayerWindow.inspectOpenItemWindowTarget(playerNum)
    local key = tostring(playerNum or 0)
    local win = windowsByPlayer[key] or nil
    if not (win and win.javaObject and win.target and win.target.kind == "item") then
        return nil
    end
    return {
        playerNum = tonumber(win.playerNum) or 0,
        itemId = tostring(win.target.itemId or ""),
        uuid = tostring(win.target.uuid or ""),
        hasItemRef = win.target.itemRef ~= nil,
        mediaTimedAction = tostring(win._nmMediaSlotTimedProgress and win._nmMediaSlotTimedProgress.action or ""),
        pendingMediaFullType = tostring(win._nmPendingMediaSlotFullType or ""),
        awaitingMediaInsert = win:isAwaitingAuthoritativeMediaInsert() == true,
        awaitingMediaEject = win:isAwaitingAuthoritativeMediaEject() == true
    }
end

function NMCDPlayerWindow.closeOpenForItemTarget(playerNum, itemId, uuid)
    local win = windowsByPlayer[tostring(playerNum or 0)] or nil
    if not (win and win.target and win.target.kind == "item") then
        return false
    end

    local targetItemId = tostring(win.target.itemId or "")
    local incomingItemId = tostring(itemId or "")
    if incomingItemId == "" or incomingItemId ~= targetItemId then
        return false
    end

    local targetUuid = tostring(win.target.uuid or "")
    local incomingUuid = tostring(uuid or "")
    if incomingUuid ~= "" and targetUuid ~= "" and incomingUuid ~= targetUuid then
        return false
    end

    NMSlotHostLifecycle.cancelSharedSlotDrags(win)
    persistWindowState(win, false)
    if win.setVisible then
        win:setVisible(false)
    end
    if win.removeFromUIManager then
        win:removeFromUIManager()
    end
    windowsByPlayer[tostring(playerNum or 0)] = nil
    return true
end

function CDPlayerWindow:close()
    NMSlotHostLifecycle.cancelSharedSlotDrags(self)
    persistWindowState(self, false)
    if self.setVisible then
        self:setVisible(false)
    end
    if self.removeFromUIManager then
        self:removeFromUIManager()
    end
    if NMGamepadWindowTracker and NMGamepadWindowTracker.unregisterWindow then
        NMGamepadWindowTracker.unregisterWindow(self)
    end
    windowsByPlayer[tostring(self.playerNum or 0)] = nil
end

function CDPlayerWindow:new(x, y, width, height)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self
    o:noBackground()
    o.keepOnScreen = false
    o.anchorRight = false
    o.anchorBottom = false
    o.moveWithMouse = false
    o.backgroundColor = { a = 0.0, r = 0.0, g = 0.0, b = 0.0 }
    o.borderColor = { a = 0.0, r = 0.0, g = 0.0, b = 0.0 }
    o.playerNum = 0
    o.target = nil
    o._nmClosePressed = false
    o._nmPressedButtonKind = nil
    o._nmButtonPulseUntilByKind = {}
    o._nmLastCDPlayerBeepSound = nil
    o._nmHoldButtonAngle = nil
    o._nmHoldButtonTargetAngle = nil
    o._nmHoldButtonAnimStartAngle = nil
    o._nmHoldButtonAnimStartTime = nil
    o._nmHoldButtonAnimating = false
    o._nmLidZonePressed = nil
    o.tooltip = nil
    o.tooltipUI = nil
    o._nmLastHeadphoneWearSyncMs = 0
    o._nmHeadphoneWearSyncActive = false
    o._nmFrameEpoch = 0
    o._nmFrameNowMs = 0
    o.isCollapsed = false
    o.isAnimating = false
    o.animStartY = nil
    o.animTargetY = nil
    o.animStartTime = nil
    o.headerPressed = false
    o.headerPressStartedCollapsed = nil
    o.headerDragMode = nil
    o.draggingHeader = false
    o.headerPressX = nil
    o.headerPressY = nil
    o.dragStartWindowX = nil
    o.dragStartWindowY = nil
    o.interactionSuppressedToggle = false
    o._nmExpandedY = nil
    o._nmLastDistanceCheckMs = 0
    o._nmContextCache = nil
    o._nmContextCacheTargetKey = nil
    o._nmSlotFrameModel = nil
    o._nmSlotFrameModelEpoch = nil
    o._nmPendingMediaSlotFullType = nil
    o._nmPendingContextMediaInsert = nil
    o._nmCloseLidAfterContextInsert = false
    o._nmPendingHeadphoneSlotFullType = nil
    o._nmPendingBatterySlotFullType = nil
    o._nmSlotRemoveInFlightByType = {}
    o._nmAwaitingAuthoritativeMediaEject = nil
    o._nmAwaitingAuthoritativeMediaInsert = nil
    o._nmSlotsAttached = false
    o._nmSlotsVisible = false
    o.mediaSlot = nil
    o.headphoneSlot = nil
    o.batterySlot = nil
    o.cdplayerBatteryMeter = nil
    o.isLidAnimating = false
    o.lidAnimStartY = nil
    o.lidAnimStartH = nil
    o.lidAnimTargetY = nil
    o.lidAnimTargetH = nil
    o.lidAnimStartTime = nil
    o.lidCurrentY = LID_CLOSED_Y
    o.lidCurrentH = LID_CLOSED_H
    o.isLidOpen = false
    return o
end

return NMCDPlayerWindow
