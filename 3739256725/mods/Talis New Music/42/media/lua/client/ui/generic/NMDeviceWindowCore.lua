
local env = _G.NMDeviceWindowEnv
setfenv(1, env)

local function getButtonScreenRect(button)
    if not button then
        return nil
    end
    local x = button.getAbsoluteX and button:getAbsoluteX() or nil
    local y = button.getAbsoluteY and button:getAbsoluteY() or nil
    local w = tonumber(button.width) or 0
    local h = tonumber(button.height) or 0
    if x == nil or y == nil or w <= 0 or h <= 0 then
        return nil
    end
    return { x = x, y = y, w = w, h = h }
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

local function getMediaSlotEnv()
    return rawget(_G, "NMMediaSlotEnv") or nil
end

local function supportsSharedMediaSlotTarget(target)
    if not target then
        return false
    end
    local kind = tostring(target.kind or "")
    return kind == "item" or kind == "vehicle"
end

local function resolveVehicleDisplayName(vehicle)
    if not vehicle then
        return NMTranslations.ui("Vehicle", "Vehicle")
    end
    if ISVehicleMenu and ISVehicleMenu.getVehicleDisplayName then
        local ok, displayName = pcall(ISVehicleMenu.getVehicleDisplayName, vehicle)
        if ok and tostring(displayName or "") ~= "" then
            return tostring(displayName)
        end
    end
    local script = vehicle.getScript and vehicle:getScript() or nil
    local carName = script and script.getCarModelName and tostring(script:getCarModelName() or "") or ""
    if carName == "" then
        carName = script and script.getName and tostring(script:getName() or "") or ""
    end
    if carName ~= "" and getTextOrNull then
        local translated = getTextOrNull("IGUI_VehicleName" .. carName)
        if translated and translated ~= "" then
            return translated
        end
    end
    if carName ~= "" then
        return carName
    end
    return NMTranslations.ui("Vehicle", "Vehicle")
end

function DeviceWindow:new(x, y, width, height)
    local o = ISCollapsableWindow.new(self, x, y, width, height)
    o.title = NMTranslations.uiStringFormat("WindowTitleFmt", "New Music - %s", NMTranslations.ui("Device", "Device"))
    o.pin = true
    o.resizable = true
    o.minimumWidth = 450
    o.minimumHeight = 560
    o.maximumWidth = 900
    o.maximumHeight = 700
    o.playerNum = 0
    o.target = nil
    o._nmFrameEpoch = 0
    o._nmFrameReason = "init"
    o._nmFrameResolveCount = 0
    o._nmFrameFallbackCount = 0
    o._nmContextCache = nil
    o._nmContextCacheEpoch = nil
    o._nmContextCacheTargetKey = nil
    o._nmPendingMediaSlotFullType = nil
    o._nmSlotRemoveInFlightByType = {}
    o._nmAwaitingAuthoritativeMediaEject = nil
    o._nmAwaitingAuthoritativeMediaInsert = nil
    o._nmSlotFrameModel = nil
    o._nmSlotFrameModelEpoch = nil
    o._nmRenderModel = nil
    o._nmRenderModelEpoch = nil
    o._nmLastDistanceCheckMs = 0
    o._nmLastHeadphoneWearSyncMs = 0
    o._nmHeadphoneWearSyncActive = false
    return o
end

function getOrCreateWindow(playerNum)
    local key = tostring(tonumber(playerNum) or 0)
    local existing = windowsByPlayer[key]
    if existing and existing.javaObject then
        return existing
    end
    local w, h = 450, 560
    local core = getCore and getCore() or nil
    local sw = core and core:getScreenWidth() or 1280
    local sh = core and core:getScreenHeight() or 720
    local x = math.floor((sw - w) / 2)
    local y = math.floor((sh - h) / 2)
    local win = DeviceWindow:new(x, y, w, h)
    win.playerNum = tonumber(playerNum) or 0
    win:initialise()
    win:addToUIManager()
    windowsByPlayer[key] = win
    return win
end

function NMDeviceWindow.openForItem(playerNum, item)
    local player = getPlayer(playerNum)
    if not (player and item) then return nil end
    local profile = NMDeviceProfiles.getForItem(item)
    if not profile or profile.isMediaContainerOnly == true then return nil end
    local state = NMDeviceState.ensure(item, profile)
    if not state then return nil end
    local win = getOrCreateWindow(playerNum)
    win.target = {
        kind = "item",
        itemId = NMCore.itemId(item),
        uuid = NMInventoryHelpers and NMInventoryHelpers.getItemStateUuid and NMInventoryHelpers.getItemStateUuid(item) or nil,
        itemRef = item
    }
    win:invalidateContextCache()
    NMSlotHostLifecycle.refreshSlotVisibility(win)
    win.title = NMTranslations.uiStringFormat("WindowTitleFmt", "New Music - %s", tostring(resolveWindowDeviceTitle(profile) or ""))
    win:setVisible(true)
    win:bringToTop()
    if NMGamepadWindowTracker and NMGamepadWindowTracker.markWindow then
        NMGamepadWindowTracker.markWindow(win, "generic")
    end
    if NMPortableMediaDropArbiter and NMPortableMediaDropArbiter.markWindowInteraction then
        NMPortableMediaDropArbiter.markWindowInteraction(win, "generic")
    end
    return win
end

function NMDeviceWindow.openForVehicle(playerNum, vehicle, part)
    local player = getPlayer(playerNum)
    if not (player and vehicle and part) then return nil end
    local profile = NMDeviceProfiles.getVehicleProfile(part)
    if not profile then return nil end
    local state = NMDeviceState.ensure(part, profile)
    if not state then return nil end
    local win = getOrCreateWindow(playerNum)
    win.target = {
        kind = "vehicle",
        vehicleId = tostring(vehicle.getId and vehicle:getId() or ""),
        partId = tostring(part.getId and part:getId() or "Radio"),
        vehicleRef = vehicle,
        partRef = part
    }
    win:invalidateContextCache()
    NMSlotHostLifecycle.refreshSlotVisibility(win)
    win.title = NMTranslations.uiStringFormat("WindowTitleFmt", "New Music - %s", resolveVehicleDisplayName(vehicle))
    win:setVisible(true)
    win:bringToTop()
    if NMGamepadWindowTracker and NMGamepadWindowTracker.markWindow then
        NMGamepadWindowTracker.markWindow(win, "generic")
    end
    if NMPortableMediaDropArbiter and NMPortableMediaDropArbiter.markWindowInteraction then
        NMPortableMediaDropArbiter.markWindowInteraction(win, "generic")
    end
    return win
end

function NMDeviceWindow.collectOpenMediaIngressZones(playerNum, dragItems)
    local key = tostring(tonumber(playerNum) or 0)
    local win = windowsByPlayer[key] or nil
    if not isWindowVisible(win) then
        return {}
    end
    if not supportsSharedMediaSlotTarget(win.target) then
        return {}
    end
    local button = win.mediaSlot and win.mediaSlot.button or nil
    local rect = getButtonScreenRect(button)
    if not rect then
        return {}
    end
    local mediaEnv = getMediaSlotEnv()
    local isCompatibleMediaDragFn = mediaEnv and mediaEnv.isCompatibleMediaDrag or nil
    local resolveMediaSlotFullTypeFn = mediaEnv and mediaEnv.resolveMediaSlotFullType or nil
    local queueDraggedMediaInsertFn = mediaEnv and mediaEnv.queueDraggedMediaInsert or nil
    local queueMediaSlotEjectFn = mediaEnv and mediaEnv.queueMediaSlotEject or nil
    if not (isCompatibleMediaDragFn and resolveMediaSlotFullTypeFn and queueDraggedMediaInsertFn and queueMediaSlotEjectFn) then
        return {}
    end
    local resolved = win.resolveContextCached and win:resolveContextCached() or nil
    local state = resolved and resolved.state or nil
    local zone = NMSlotHostLifecycle.buildSharedMediaSlotZone({
        window = win,
        uiFamily = "generic",
        zoneKind = "slot",
        rect = rect,
        priority = 10,
        zOrder = NMPortableMediaDropArbiter and NMPortableMediaDropArbiter.getWindowZOrder and NMPortableMediaDropArbiter.getWindowZOrder(win) or 0,
        dragItems = dragItems,
        canAcceptDraggedMedia = function(items)
            return type(items) == "table"
                and #items > 0
                and isCompatibleMediaDragFn(win, items)
                and resolveMediaSlotFullTypeFn(win, state) == ""
                and not (win.isAwaitingAuthoritativeMediaEject and win:isAwaitingAuthoritativeMediaEject() == true)
        end,
        canEjectMedia = function()
            return resolveMediaSlotFullTypeFn(win, state) ~= ""
        end,
        canStartExtractDrag = function()
            return resolveMediaSlotFullTypeFn(win, state) ~= ""
        end,
        performInsertFromDrag = function(items, sourceTag)
            return queueDraggedMediaInsertFn(win, items, sourceTag or "arbiter") == true
        end,
        performBeginExtract = function()
            local resolvedNow = win.resolveContext and win:resolveContext() or nil
            local stateNow = resolvedNow and resolvedNow.state or nil
            local fullTypeNow = resolveMediaSlotFullTypeFn(win, stateNow)
            if fullTypeNow == "" then
                return false
            end
            if mediaEnv and mediaEnv.beginMediaExtractDrag then
                mediaEnv.beginMediaExtractDrag(win, fullTypeNow, "slot")
                return true
            end
            return false
        end,
        performEject = function(sourceTag)
            return queueMediaSlotEjectFn(win, sourceTag or "arbiter") == true
        end,
        performShowInsertContext = function(btn, xArg, yArg)
            local mediaEnv = getMediaSlotEnv()
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
            local resolvedNow = win.resolveContext and win:resolveContext() or nil
            local stateNow = resolvedNow and resolvedNow.state or nil
            local fullTypeNow = resolveMediaSlotFullTypeFn(win, stateNow)
            if fullTypeNow == "" then
                return false
            end
            if mediaEnv and mediaEnv.beginMediaExtractDrag then
                mediaEnv.beginMediaExtractDrag(win, fullTypeNow, "slot")
                return true
            end
            return false
        end,
        handleRightClick = function()
            local resolvedNow = win.resolveContext and win:resolveContext() or nil
            local stateNow = resolvedNow and resolvedNow.state or nil
            local fullTypeNow = resolveMediaSlotFullTypeFn(win, stateNow)
            if fullTypeNow ~= "" then
                return queueMediaSlotEjectFn(win, "handoff") == true
            end
            return false
        end
    })
    return zone and { zone } or {}
end

return NMDeviceWindow
