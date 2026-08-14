local env = _G.NMWalkmanWindowEnv
setfenv(1, env)

local function logPortableUiProbe(tag, detail)
    if not (NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("portableUiProbe")) then
        return
    end
    NMCore.logChannel("portableUiProbe", tostring(tag or "portable_ui"), tostring(detail or ""))
end

function WalkmanWindow:hasInsertedCassette()
    local renderState = self:getSlotRenderState("media")
    return tostring(renderState and renderState.fullType or "") ~= ""
end

function WalkmanWindow:getCassetteDisplayMediaState()
    local renderState = self:getSlotRenderState("media")
    local fullType = tostring(renderState and renderState.fullType or "")
    local epoch = tonumber(self._nmFrameEpoch) or 0
    local cached = self._nmCassetteDisplayMediaState
    if cached and cached.epoch == epoch and tostring(cached.fullType or "") == fullType then
        return cached
    end

    local mediaState = nil
    if fullType == "" then
        mediaState = {
            fullType = "",
            texture = nil,
            texturePath = nil,
            visible = false,
        }
    elseif not NMMediaWorldTextureResolver or not NMMediaWorldTextureResolver.resolveTexture then
        mediaState = {
            fullType = fullType,
            texture = nil,
            texturePath = nil,
            visible = false,
        }
    else
        local texture, texturePath = NMMediaWorldTextureResolver.resolveTexture(fullType)
        mediaState = {
            fullType = fullType,
            texture = texture,
            texturePath = texturePath,
            visible = texture ~= nil,
        }
    end
    mediaState.epoch = epoch
    self._nmCassetteDisplayMediaState = mediaState
    return mediaState
end

function WalkmanWindow:getCassetteDisplayTexture()
    local mediaState = self:getCassetteDisplayMediaState()
    return mediaState.texture, mediaState.texturePath
end

function WalkmanWindow:markAwaitingAuthoritativeMediaEject(fullType)
    local pendingFullType = tostring(fullType or "")
    if pendingFullType == "" then
        self._nmAwaitingAuthoritativeMediaEject = nil
        return
    end
    self._nmAwaitingAuthoritativeMediaInsert = nil
    self._nmAwaitingAuthoritativeMediaEject = {
        active = true,
        fullType = pendingFullType,
        startedAtMs = getNowMs()
    }
end

function WalkmanWindow:clearAwaitingAuthoritativeMediaEject()
    self._nmAwaitingAuthoritativeMediaEject = nil
end

function WalkmanWindow:isAwaitingAuthoritativeMediaEject(fullType)
    local awaiting = self._nmAwaitingAuthoritativeMediaEject
    if not (awaiting and awaiting.active == true) then
        return false
    end
    local awaitingFullType = tostring(awaiting.fullType or "")
    if tostring(fullType or "") == "" then
        return awaitingFullType ~= ""
    end
    return awaitingFullType ~= "" and awaitingFullType == tostring(fullType or "")
end

function WalkmanWindow:markAwaitingAuthoritativeMediaInsert(fullType)
    local pendingFullType = tostring(fullType or "")
    if pendingFullType == "" then
        self._nmAwaitingAuthoritativeMediaInsert = nil
        return
    end
    self._nmAwaitingAuthoritativeMediaEject = nil
    self._nmAwaitingAuthoritativeMediaInsert = {
        active = true,
        fullType = pendingFullType,
        startedAtMs = getNowMs()
    }
end

function WalkmanWindow:clearAwaitingAuthoritativeMediaInsert()
    self._nmAwaitingAuthoritativeMediaInsert = nil
end

function WalkmanWindow:isAwaitingAuthoritativeMediaInsert(fullType)
    local awaiting = self._nmAwaitingAuthoritativeMediaInsert
    if not (awaiting and awaiting.active == true) then
        return false
    end
    local awaitingFullType = tostring(awaiting.fullType or "")
    if tostring(fullType or "") == "" then
        return awaitingFullType ~= ""
    end
    return awaitingFullType ~= "" and awaitingFullType == tostring(fullType or "")
end

function WalkmanWindow:invalidateContextCache()
    NMSlotHostLifecycle.invalidateContextCache(self)
end

function WalkmanWindow:onInvalidateSlotHostContextCache()
    self._nmCassetteDisplayMediaState = nil
end

function WalkmanWindow:beginFrameEpoch(reason)
    NMSlotHostLifecycle.beginFrameEpoch(self, reason)
end

function WalkmanWindow:resolveContextFreshUncached()
    return resolveContextFreshUncached(self)
end

function WalkmanWindow:resolveContext()
    return NMSlotHostLifecycle.resolveContext(self)
end

function WalkmanWindow:invalidateSlotFrameModel()
    NMSlotHostLifecycle.invalidateSlotFrameModel(self)
end

function WalkmanWindow:buildSlotFrameModel()
    return NMSlotHostLifecycle.buildSlotFrameModel(self)
end

function WalkmanWindow:getSlotRenderState(slotKey)
    return NMSlotHostLifecycle.getSlotRenderState(self, slotKey)
end

function WalkmanWindow:dispatch(action, args)
    local resolved = self:resolveContextFresh()
    if not resolved then
        logPortableUiProbe(
            "walkman_dispatch_missing_context",
            string.format(
                "action=%s targetItemId=%s targetUuid=%s mediaItemId=%s slotTraceId=%s",
                tostring(action or ""),
                tostring(self.target and self.target.itemId or ""),
                tostring(self.target and self.target.uuid or ""),
                tostring(args and args.mediaItemId or ""),
                tostring(args and args.slotTraceId or "")
            )
        )
        return false, "missing_context"
    end
    logPortableUiProbe(
        "walkman_dispatch",
        string.format(
            "action=%s targetItemId=%s targetUuid=%s resolvedItemId=%s mediaItemId=%s mediaFullType=%s pending=%s slotTraceId=%s",
            tostring(action or ""),
            tostring(self.target and self.target.itemId or ""),
            tostring(self.target and self.target.uuid or ""),
            tostring(resolved.item and NMCore and NMCore.itemId and NMCore.itemId(resolved.item) or ""),
            tostring(args and args.mediaItemId or ""),
            tostring(args and (args.mediaEjectFullType or args.mediaFullType) or ""),
            tostring(self._nmPendingMediaSlotFullType or ""),
            tostring(args and args.slotTraceId or "")
        )
    )
    local ok, reason = NMClientIntentDispatch.performIntent(resolved.player, resolved.item, action, args or {})
    if ok == true then
        self:invalidateContextCache()
        self:invalidateSlotFrameModel()
    end
    logPortableUiProbe(
        "walkman_dispatch_result",
        string.format(
            "action=%s ok=%s reason=%s targetItemId=%s targetUuid=%s pending=%s slotTraceId=%s",
            tostring(action or ""),
            tostring(ok == true),
            tostring(reason or ""),
            tostring(self.target and self.target.itemId or ""),
            tostring(self.target and self.target.uuid or ""),
            tostring(self._nmPendingMediaSlotFullType or ""),
            tostring(args and args.slotTraceId or "")
        )
    )
    return ok, reason
end

function WalkmanWindow:resolveContextCached()
    return self:resolveContext()
end

function WalkmanWindow:resolveContextFresh()
    return NMSlotHostLifecycle.resolveContextFresh(self)
end
