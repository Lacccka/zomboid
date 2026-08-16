local env = _G.NMDeviceWindowEnv
setfenv(1, env)

local UiLifecycleItemWindows = require "ui/shared/host/NMUiLifecycleItemWindows"

function DeviceWindow:invalidateContextCache()
    NMSlotHostLifecycle.invalidateContextCache(self)
end

local function resolveNowMs()
    return (getTimestampMs and tonumber(getTimestampMs()))
        or (getTimeInMillis and tonumber(getTimeInMillis()))
        or 0
end

function DeviceWindow:markAwaitingAuthoritativeMediaEject(fullType)
    NMSlotHostLifecycle.markAwaitingAuthoritativeMediaEject(self, fullType)
end

function DeviceWindow:clearAwaitingAuthoritativeMediaEject()
    NMSlotHostLifecycle.clearAwaitingAuthoritativeMediaEject(self)
end

function DeviceWindow:isAwaitingAuthoritativeMediaEject(fullType)
    return NMSlotHostLifecycle.isAwaitingAuthoritativeMediaEject(self, fullType)
end

function DeviceWindow:markAwaitingAuthoritativeMediaInsert(fullType)
    NMSlotHostLifecycle.markAwaitingAuthoritativeMediaInsert(self, fullType)
end

function DeviceWindow:clearAwaitingAuthoritativeMediaInsert()
    NMSlotHostLifecycle.clearAwaitingAuthoritativeMediaInsert(self)
end

function DeviceWindow:isAwaitingAuthoritativeMediaInsert(fullType)
    return NMSlotHostLifecycle.isAwaitingAuthoritativeMediaInsert(self, fullType)
end

function DeviceWindow:beginFrameEpoch(reason)
    NMSlotHostLifecycle.beginFrameEpoch(self, reason)
end

function DeviceWindow:recordFallback(label)
    self._nmFrameFallbackCount = (tonumber(self._nmFrameFallbackCount) or 0) + 1
    if NMUIRenderProbe and NMUIRenderProbe.count then
        NMUIRenderProbe.count(self, "context.fallback", 1)
    end
    if NMCore and NMCore.logChannel and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("runtime")
        and NMCore.shouldLogEvery and NMCore.shouldLogEvery("runtimeProbe.uiContextFallback." .. tostring(label or "unknown"), self._nmFrameEpoch or 0, 240) then
        NMCore.logChannel("runtime", "ui_context_fallback", string.format("reason=%s epoch=%s", tostring(label or "unknown"), tostring(self._nmFrameEpoch or 0)))
    end
end

function DeviceWindow:logFrameDiagnostics()
    if not (NMCore and NMCore.logChannel and NMCore.isSubsystemDebugEnabled and NMCore.isSubsystemDebugEnabled("runtime")) then
        return
    end
    if not (NMCore.shouldLogEvery and NMCore.shouldLogEvery("runtimeProbe.uiContextFrame." .. tostring(self.playerNum or 0), self._nmFrameEpoch or 0, 900)) then
        return
    end
    local target = self.target
    local targetKey = WindowRegistry and WindowRegistry.targetKey and WindowRegistry.targetKey(target) or ""
    NMCore.logChannel(
        "runtime",
        "ui_context_frame",
        string.format(
            "player=%s epoch=%s reason=%s resolves=%s fallbacks=%s target=%s",
            tostring(self.playerNum or 0),
            tostring(self._nmFrameEpoch or 0),
            tostring(self._nmFrameReason or "frame"),
            tostring(self._nmFrameResolveCount or 0),
            tostring(self._nmFrameFallbackCount or 0),
            tostring(targetKey)
        )
    )
end

function isItemRefValid(item, itemId)
    if not (item and item.getID and itemId) then
        return false
    end
    if tostring(item:getID() or "") ~= tostring(itemId) then
        return false
    end
    local hasContainer = item.getContainer and item:getContainer() ~= nil
    local hasWorldItem = item.getWorldItem and item:getWorldItem() ~= nil
    return hasContainer or hasWorldItem
end

function resolveItemContext(self, player)
    if not self.target or not self.target.itemId then
        return nil
    end
    local itemId = self.target.itemId
    local item = self.target.itemRef
    if not isItemRefValid(item, itemId) then
        item = nil
    end
    if item then
        local profileFast = NMDeviceProfiles.getForItem(item)
        local stateFast = profileFast and NMDeviceState.ensure(item, profileFast) or nil
        if profileFast and stateFast then
            return { player = player, item = item, profile = profileFast, state = stateFast, kind = "item" }
        end
    end

    local inv = player.getInventory and player:getInventory() or nil
    if inv then
        self:recordFallback("inventory_find")
        item = NMInventoryHelpers and NMInventoryHelpers.findItemById and NMInventoryHelpers.findItemById(inv, itemId) or nil
    end
    if not item and NMInventoryHelpers and NMInventoryHelpers.findWorldItemByIdNearPlayer then
        self:recordFallback("world_find")
        item = NMInventoryHelpers.findWorldItemByIdNearPlayer(player, itemId, 8)
    end
    if not item then
        return nil
    end
    self.target.itemRef = item

    local profile = NMDeviceProfiles.getForItem(item)
    local state = profile and NMDeviceState.ensure(item, profile) or nil
    if not (profile and state) then
        return nil
    end

    return { player = player, item = item, profile = profile, state = state, kind = "item" }
end

function resolveVehicleById(vehicleId)
    local id = tonumber(vehicleId)
    if not id or not getVehicleById then
        return nil
    end
    return getVehicleById(id)
end

function resolveVehicleContext(self, player)
    if not self.target then
        return nil
    end
    local vehicle = self.target.vehicleRef
    if vehicle and vehicle.getId and self.target.vehicleId and tostring(vehicle:getId()) ~= tostring(self.target.vehicleId) then
        vehicle = nil
    end
    if not vehicle then
        self:recordFallback("vehicle_find")
        vehicle = resolveVehicleById(self.target.vehicleId)
    end
    if not vehicle and player and player.getVehicle then
        local playerVehicle = player:getVehicle()
        if playerVehicle and playerVehicle.getId and tostring(playerVehicle:getId()) == tostring(self.target.vehicleId or "") then
            vehicle = playerVehicle
        end
    end
    if not vehicle then
        return nil
    end
    self.target.vehicleRef = vehicle

    local partId = tostring(self.target.partId or "Radio")
    local part = self.target.partRef
    if part and part.getId and tostring(part:getId() or "") ~= partId then
        part = nil
    end
    if not part then
        self:recordFallback("vehicle_part_find")
        part = vehicle.getPartById and vehicle:getPartById(partId) or nil
    end
    if not part then
        return nil
    end
    self.target.partRef = part

    local profile = NMDeviceProfiles.getVehicleProfile(part)
    local state = profile and NMDeviceState.ensure(part, profile) or nil
    if not (profile and state) then
        return nil
    end

    return { player = player, vehicle = vehicle, part = part, profile = profile, state = state, kind = "vehicle" }
end

function targetCacheKey(target)
    if not target then
        return ""
    end
    return WindowRegistry and WindowRegistry.targetKey and WindowRegistry.targetKey(target) or ""
end

function resolveContextUncached(self)
    local player = getPlayer(self.playerNum)
    if not player or not self.target then
        return nil
    end
    self._nmFrameResolveCount = (tonumber(self._nmFrameResolveCount) or 0) + 1
    if self.target.kind == "vehicle" then
        return resolveVehicleContext(self, player)
    end
    if self.target.kind == "item" then
        return resolveItemContext(self, player)
    end
    return nil
end

function DeviceWindow:resolveContextFreshUncached()
    return resolveContextUncached(self)
end

function DeviceWindow:resolveContext()
    return NMSlotHostLifecycle.resolveContext(self)
end

function resolveLiveItemByTarget(player, target)
    return UiLifecycleItemWindows.resolveLiveItemByTarget(player, target)
end

function DeviceWindow:resolveContextCached()
    return self:resolveContext()
end

function DeviceWindow:resolveContextFresh()
    return NMSlotHostLifecycle.resolveContextFresh(self)
end

function DeviceWindow:invalidateSlotFrameModel()
    NMSlotHostLifecycle.invalidateSlotFrameModel(self)
end

function DeviceWindow:invalidateRenderModel()
    NMSlotHostLifecycle.invalidateRenderModel(self)
end

function NMDeviceWindow.invalidateOpenItemWindow(itemId, uuid)
    return UiLifecycleItemWindows.applyOpenItemWindowChange(env, itemId, uuid, {
        mode = "invalidate",
        resolveWindowUuid = function(win)
            local resolved = win.resolveContextFresh and win:resolveContextFresh() or (win.resolveContext and win:resolveContext()) or nil
            local state = resolved and resolved.state or nil
            return tostring(state and state.deviceUUID or "")
        end,
        deferInvalidate = function(win)
            return NMSlotHostLifecycle.deferUiRefreshUntilDragEnd
                and NMSlotHostLifecycle.deferUiRefreshUntilDragEnd(win, {
                    cause = "open_item_window_invalidate_deferred",
                    visibility = false,
                }) == true
        end,
        invalidateWindow = function(win)
            NMSlotHostLifecycle.invalidateForUiChange(win, {
                cause = "open_item_window_invalidate",
                visibility = false,
            })
        end,
    })
end

function NMDeviceWindow.rebindOpenPortableItemWindow(itemId, uuid)
    return UiLifecycleItemWindows.applyOpenItemWindowChange(env, itemId, uuid, {
        mode = "rebind",
        deferInvalidate = function(win)
            return NMSlotHostLifecycle.deferUiRefreshUntilDragEnd
                and NMSlotHostLifecycle.deferUiRefreshUntilDragEnd(win, {
                    cause = "open_item_window_rebind_deferred",
                    visibility = false,
                }) == true
        end,
        invalidateWindow = function(win)
            NMSlotHostLifecycle.invalidateForUiChange(win, {
                cause = "open_item_window_rebind",
                visibility = false,
            })
        end,
    })
end

function NMDeviceWindow.inspectOpenItemWindowTarget(playerNum)
    return UiLifecycleItemWindows.inspectOpenItemWindowTarget(env, playerNum)
end

function NMDeviceWindow.closeOpenForItemTarget(playerNum, itemId, uuid)
    return UiLifecycleItemWindows.closeOpenForItemTarget(env, playerNum, itemId, uuid)
end
