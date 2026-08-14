local env = _G.NMCDPlayerWindowEnv
setfenv(1, env)

function CDPlayerWindow:invalidateContextCache()
    NMSlotHostLifecycle.invalidateContextCache(self)
end

function CDPlayerWindow:markAwaitingAuthoritativeMediaEject(fullType)
    NMSlotHostLifecycle.markAwaitingAuthoritativeMediaEject(self, fullType)
end

function CDPlayerWindow:clearAwaitingAuthoritativeMediaEject()
    NMSlotHostLifecycle.clearAwaitingAuthoritativeMediaEject(self)
end

function CDPlayerWindow:isAwaitingAuthoritativeMediaEject(fullType)
    return NMSlotHostLifecycle.isAwaitingAuthoritativeMediaEject(self, fullType)
end

function CDPlayerWindow:markAwaitingAuthoritativeMediaInsert(fullType)
    NMSlotHostLifecycle.markAwaitingAuthoritativeMediaInsert(self, fullType)
end

function CDPlayerWindow:clearAwaitingAuthoritativeMediaInsert()
    NMSlotHostLifecycle.clearAwaitingAuthoritativeMediaInsert(self)
end

function CDPlayerWindow:isAwaitingAuthoritativeMediaInsert(fullType)
    return NMSlotHostLifecycle.isAwaitingAuthoritativeMediaInsert(self, fullType)
end

function CDPlayerWindow:resolveContextFreshUncached()
    return resolveContextFreshUncached(self)
end

function CDPlayerWindow:resolveContext()
    return NMSlotHostLifecycle.resolveContext(self)
end

function CDPlayerWindow:beginFrameEpoch(reason)
    NMSlotHostLifecycle.beginFrameEpoch(self, reason)
end

function CDPlayerWindow:invalidateSlotFrameModel()
    NMSlotHostLifecycle.invalidateSlotFrameModel(self)
end

function CDPlayerWindow:buildSlotFrameModel()
    return NMSlotHostLifecycle.buildSlotFrameModel(self)
end

function CDPlayerWindow:getSlotRenderState(slotKey)
    return NMSlotHostLifecycle.getSlotRenderState(self, slotKey)
end

function CDPlayerWindow:resolveContextCached()
    return self:resolveContext()
end

function CDPlayerWindow:resolveContextFresh()
    return NMSlotHostLifecycle.resolveContextFresh(self)
end

function CDPlayerWindow:dispatch(action, args)
    local resolved = self:resolveContextFresh()
    if not resolved then
        return false, "missing_context"
    end
    local ok, reason = NMClientIntentDispatch.performIntent(resolved.player, resolved.item, action, args or {})
    if ok == true then
        self:invalidateContextCache()
        self:invalidateSlotFrameModel()
    end
    return ok, reason
end
