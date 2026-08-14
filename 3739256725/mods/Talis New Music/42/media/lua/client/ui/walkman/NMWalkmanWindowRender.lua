local env = _G.NMWalkmanWindowEnv
setfenv(1, env)

local function ensureTextureSlot(key, path)
    UI_TEXTURES[key] = UI_TEXTURES[key] or nil
    if UI_TEXTURES[key] == nil and getTexture then
        UI_TEXTURES[key] = getTexture(path) or false
    end
    if UI_TEXTURES[key] == false then
        return nil
    end
    return UI_TEXTURES[key]
end

local function getWalkmanChromeTextures()
    return {
        backplateBase = ensureTextureSlot("backplateBase", BACKPLATE_BASE_TEXTURE_PATH),
        backplateSlotBg = ensureTextureSlot("backplateSlotBg", BACKPLATE_SLOT_BG_TEXTURE_PATH),
        lidEdge = ensureTextureSlot("lidEdge", LID_EDGE_TEXTURE_PATH),
        playButton = ensureTextureSlot("playButton", PLAY_BUTTON_TEXTURE_PATH),
        prevButton = ensureTextureSlot("prevButton", PREV_BUTTON_TEXTURE_PATH),
        nextButton = ensureTextureSlot("nextButton", NEXT_BUTTON_TEXTURE_PATH),
        volumeWheel = ensureTextureSlot("volumeWheel", VOLUME_WHEEL_TEXTURE_PATH),
        loopButton = ensureTextureSlot("loopButton", LOOP_BUTTON_TEXTURE_PATH),
        spool = ensureTextureSlot("spool", SPOOL_TEXTURE_PATH),
    }
end

local function getWalkmanOverlayTextures()
    return {
        close = ensureTextureSlot("close", CLOSE_TEXTURE_PATH),
        stopButton = ensureTextureSlot("stopButton", STOP_BUTTON_TEXTURE_PATH),
    }
end

function WalkmanWindow:prerender()
    local perfStart = NMUIRenderProbe and NMUIRenderProbe.beginWindow and NMUIRenderProbe.beginWindow(self) or nil
    self:beginFrameEpoch("prerender")
    local resolved = self:resolveContextCached()
    local model = self:getRenderModel()
    self:syncPlayButtonFromTransport(resolved, true, false)
    local nowMs = getNowMs()
    if NMHeadphoneSlot and NMHeadphoneSlot.tickWearSyncWindow then
        NMHeadphoneSlot.tickWearSyncWindow(self, {
            nowMs = nowMs,
            resolved = resolved,
            visible = self.headphoneSlot and self.headphoneSlot.button ~= nil,
            idlePollMs = 1000,
            activePollMs = 250,
        })
    else
        self._nmHeadphoneWearSyncActive = false
    end
    ISPanel.prerender(self)
    if self.backgroundColor then
        self:drawRect(0, 0, self.width, self.height, self.backgroundColor.a, self.backgroundColor.r, self.backgroundColor.g, self.backgroundColor.b)
    end
    local chromeTextures = getWalkmanChromeTextures()
    if chromeTextures.volumeWheel then
        local wheelRect = self:getVolumeWheelRect()
        local centerX = wheelRect.x + (wheelRect.w / 2)
        local centerY = wheelRect.y + (wheelRect.h / 2)
        self:DrawTextureAngle(chromeTextures.volumeWheel, centerX, centerY, tonumber(model and model.wheelAngle) or 0.0)
    end
    if chromeTextures.loopButton then
        local loopRect = self:getLoopButtonRect()
        self:drawTextureScaled(chromeTextures.loopButton, loopRect.x, loopRect.y, loopRect.w, loopRect.h, 1.0, 1.0, 1.0, 1.0)
    end
    local walkmanTextures = model and model.textures or nil
    local playButtonTexture = walkmanTextures and walkmanTextures.play or chromeTextures.playButton
    local prevButtonTexture = walkmanTextures and walkmanTextures.prev or chromeTextures.prevButton
    local nextButtonTexture = walkmanTextures and walkmanTextures.next or chromeTextures.nextButton
    if playButtonTexture then
        local playRect = self:getPlayButtonRect()
        self:drawTextureScaled(playButtonTexture, playRect.x, playRect.y, playRect.w, playRect.h, 1.0, 1.0, 1.0, 1.0)
    end
    if prevButtonTexture then
        local prevRect = self:getPrevButtonRect()
        self:drawTextureScaled(prevButtonTexture, prevRect.x, prevRect.y, prevRect.w, prevRect.h, 1.0, 1.0, 1.0, 1.0)
    end
    if nextButtonTexture then
        local nextRect = self:getNextButtonRect()
        self:drawTextureScaled(nextButtonTexture, nextRect.x, nextRect.y, nextRect.w, nextRect.h, 1.0, 1.0, 1.0, 1.0)
    end
    if walkmanTextures and walkmanTextures.base then
        self:drawTextureScaled(walkmanTextures.base, 0, 0, self.width, self.height, 1.0, 1.0, 1.0, 1.0)
    elseif chromeTextures.backplateBase then
        self:drawTextureScaled(chromeTextures.backplateBase, 0, 0, self.width, self.height, 1.0, 1.0, 1.0, 1.0)
    end
    if walkmanTextures and walkmanTextures.side then
        local sideRect = self:getWalkmanSideRect()
        self:drawTextureScaled(walkmanTextures.side, sideRect.x, sideRect.y, sideRect.w, sideRect.h, 1.0, 1.0, 1.0, 1.0)
    end
    if chromeTextures.backplateSlotBg then
        self:drawTextureScaled(chromeTextures.backplateSlotBg, 0, 0, self.width, self.height, 1.0, 1.0, 1.0, 1.0)
    end
    local timedCassetteState = model and model.timedCassetteState or nil
    if timedCassetteState and timedCassetteState.visible == true and timedCassetteState.texture then
        self:drawTextureScaled(timedCassetteState.texture, timedCassetteState.x, timedCassetteState.y, timedCassetteState.w, timedCassetteState.h, timedCassetteState.alpha, 1.0, 1.0, 1.0)
    else
        local cassetteMediaState = model and model.cassetteMediaState or nil
        if cassetteMediaState and cassetteMediaState.visible == true and cassetteMediaState.texture then
            local cassetteRect = self:getCassetteDisplayRect()
            self:drawTextureScaled(cassetteMediaState.texture, cassetteRect.x, cassetteRect.y, cassetteRect.w, cassetteRect.h, 1.0, 1.0, 1.0, 1.0)
            local labelState = model and model.cassetteLabelState or nil
            if labelState then
                local labelRect = labelState.rect
                self:drawRect(labelRect.x, labelRect.y, labelRect.w, labelRect.h, CASSETTE_LABEL_BG.a, CASSETTE_LABEL_BG.r, CASSETTE_LABEL_BG.g, CASSETTE_LABEL_BG.b)
                local tm = getTextManager and getTextManager() or nil
                local textH = tm and tm.MeasureStringY and tm:MeasureStringY(UIFont.Small, "Ag") or 10
                local textY = labelRect.y + math.floor(((labelRect.h - textH) * 0.5) + 0.5)
                self:drawText(labelState.text, labelRect.x + CASSETTE_LABEL_TEXT_PAD_X, textY, CASSETTE_LABEL_TEXT_COLOR.r, CASSETTE_LABEL_TEXT_COLOR.g, CASSETTE_LABEL_TEXT_COLOR.b, CASSETTE_LABEL_TEXT_COLOR.a, UIFont.Small)
            end
            if chromeTextures.spool then
                local leftRect = self:getCassetteSpoolRect(1)
                local rightRect = self:getCassetteSpoolRect(2)
                self:DrawTextureAngle(chromeTextures.spool, leftRect.x + (leftRect.w / 2), leftRect.y + (leftRect.h / 2), tonumber(self._nmLeftSpoolAngle) or 0.0)
                self:DrawTextureAngle(chromeTextures.spool, rightRect.x + (rightRect.w / 2), rightRect.y + (rightRect.h / 2), tonumber(self._nmRightSpoolAngle) or 0.0)
            end
        end
    end
    if NMUIRenderProbe and NMUIRenderProbe.endWindow then
        NMUIRenderProbe.endWindow(self, "device.prerender", perfStart)
    end
end

function WalkmanWindow:render()
    local perfFrame = NMUIRenderProbe and NMUIRenderProbe.beginWindow and NMUIRenderProbe.beginWindow(self) or nil
    local perfRender = NMUIRenderProbe and NMUIRenderProbe.beginWindow and NMUIRenderProbe.beginWindow(self) or nil
    local overlayTextures = getWalkmanOverlayTextures()
    ISPanel.render(self)
    local model = self:getRenderModel()
    local walkmanTextures = model and model.textures or nil
    local loopIconTexture = model and model.loopIconTexture or nil
    if loopIconTexture and model and model.loopVisible == true then
        local loopIconRect = model.loopIconRect
        self:drawTextureScaled(loopIconTexture, loopIconRect.x, loopIconRect.y, loopIconRect.w, loopIconRect.h, LOOP_ICON_ALPHA, 1.0, 1.0, 1.0)
    end
    local stopButtonTexture = walkmanTextures and walkmanTextures.stop or overlayTextures.stopButton
    if self.isPlayButtonDown == true and stopButtonTexture then
        local stopRect = self:getStopButtonRect()
        self:drawTextureScaled(stopButtonTexture, stopRect.x, stopRect.y, stopRect.w, stopRect.h, 1.0, 1.0, 1.0, 1.0)
    end
    if overlayTextures.close then
        local closeRect = self:getCloseRect()
        local closeR, closeG, closeB = 0.78, 0.78, 0.78
        if model and type(model.closeTint) == "table" then
            closeR, closeG, closeB = model.closeTint[1], model.closeTint[2], model.closeTint[3]
        end
        self:drawTextureScaled(overlayTextures.close, closeRect.x, closeRect.y, closeRect.w, closeRect.h, 1.0, closeR, closeG, closeB)
    end
    if model and model.volumeLabelVisible == true then
        local labelRect = model.volumeLabelRect
        self:drawText(model.volumeLabelText, labelRect.x, labelRect.y, 1.0, 1.0, 1.0, 1.0, UIFont.Small)
    end
    local lidEdgeState = model and model.lidEdgeState or nil
    if lidEdgeState and lidEdgeState.texture and lidEdgeState.visible == true then
        self:drawTextureScaled(lidEdgeState.texture, lidEdgeState.x, lidEdgeState.y, lidEdgeState.w, lidEdgeState.h, 1.0, 1.0, 1.0, 1.0)
    end
    if model and model.lidIngressVisible == true then
        local ingressRect = self:getLidIngressZoneRect()
        self:drawRectBorder(ingressRect.x, ingressRect.y, ingressRect.w, ingressRect.h, LID_INGRESS_BORDER.a, LID_INGRESS_BORDER.r, LID_INGRESS_BORDER.g, LID_INGRESS_BORDER.b)
    end
    local lidState = model and model.lidState or nil
    if lidState and lidState.texture then
        self:drawTextureScaled(lidState.texture, lidState.x, lidState.y, lidState.w, lidState.h, 1.0, 1.0, 1.0, 1.0)
    end
    if NMUIRenderProbe and NMUIRenderProbe.endWindow then
        NMUIRenderProbe.endWindow(self, "device.render", perfRender)
        NMUIRenderProbe.endWindow(self, "device.frame", perfFrame)
    end
    if NMUIRenderProbe and NMUIRenderProbe.flush then
        NMUIRenderProbe.flush(self)
    end
end

