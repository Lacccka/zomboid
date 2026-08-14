local env = _G.NMCDPlayerWindowEnv
setfenv(1, env)

local function autoCloseThresholdSq()
    return NMDeviceUIRange.getWorldInteractionRangeSq and NMDeviceUIRange.getWorldInteractionRangeSq() or (2.8 * 2.8)
end

local function updateWorldCDSpin(self, nowMs)
    local lastMs = tonumber(self._nmWorldCDSpinLastMs)
    if lastMs == nil then
        self._nmWorldCDSpinLastMs = nowMs
        self._nmWorldCDSpinAngle = tonumber(self._nmWorldCDSpinAngle) or 0.0
        return
    end

    local deltaMs = math.max(0, nowMs - lastMs)
    self._nmWorldCDSpinLastMs = nowMs
    self._nmWorldCDSpinAngle = tonumber(self._nmWorldCDSpinAngle) or 0.0

    if deltaMs <= 0 then
        return
    end

    local timed = self._nmMediaSlotTimedProgress
    local timedActive = timed and timed.active == true or false
    local transport = self:buildTransportState()
    if timedActive == true or transport.isPlaying ~= true or self:hasInsertedMedia() ~= true then
        return
    end

    local spinDelta = (WORLD_CD_SPIN_DEG_PER_SEC * deltaMs) / 1000.0
    local nextAngle = self._nmWorldCDSpinAngle + spinDelta
    self._nmWorldCDSpinAngle = nextAngle % 360.0
end

local function updateLidAnimation(self, nowMs)
    if self.isLidAnimating ~= true then
        self:syncLidFromMedia(false)
        return
    end
    local elapsed = math.max(0, nowMs - (tonumber(self.lidAnimStartTime) or 0))
    local t = math.min(1.0, elapsed / LID_ANIMATION_DURATION_MS)
    local eased = 1.0 - ((1.0 - t) * (1.0 - t))
    local startY = tonumber(self.lidAnimStartY) or LID_CLOSED_Y
    local startH = tonumber(self.lidAnimStartH) or LID_CLOSED_H
    local targetY = tonumber(self.lidAnimTargetY) or LID_CLOSED_Y
    local targetH = tonumber(self.lidAnimTargetH) or LID_CLOSED_H
    self.lidCurrentY = startY + ((targetY - startY) * eased)
    self.lidCurrentH = startH + ((targetH - startH) * eased)
    if t >= 1.0 then
        local wasOpening = self.isLidOpen == true
        self.isLidAnimating = false
        self.lidCurrentY = targetY
        self.lidCurrentH = targetH
        if wasOpening ~= true then
            playCDPlayerGenericClick(self, false)
        end
    end
end

local function updatePendingContextMediaInsert(self)
    local pending = self._nmPendingContextMediaInsert
    if not pending then
        return
    end
    if self.isLidAnimating == true or self.isLidOpen ~= true then
        return
    end
    local timed = self._nmMediaSlotTimedProgress
    if timed and timed.active == true then
        return
    end
    local mediaEnv = rawget(_G, "NMMediaSlotEnv") or nil
    local queueMediaSlotActionFn = mediaEnv and mediaEnv.queueMediaSlotAction or nil
    self._nmPendingContextMediaInsert = nil
    if not queueMediaSlotActionFn then
        self._nmPendingMediaSlotFullType = nil
        self._nmCloseLidAfterContextInsert = false
        self.isLidManuallyOpen = false
        self:syncLidFromMedia(false)
        return
    end
    local ok = queueMediaSlotActionFn(self, "insert_media", pending.args or {})
    if ok == true then
        self._nmCloseLidAfterContextInsert = true
        return
    end
    self._nmPendingMediaSlotFullType = nil
    self._nmCloseLidAfterContextInsert = false
    self.isLidManuallyOpen = false
    self:syncLidFromMedia(false)
end

local function updateContextInsertCloseState(self)
    if self._nmCloseLidAfterContextInsert ~= true then
        return
    end
    local timed = self._nmMediaSlotTimedProgress
    local timedAction = timed and timed.active == true and tostring(timed.action or "") or ""
    if timedAction == "insert_media" then
        self.isLidManuallyOpen = false
        self:syncLidFromMedia(false)
        return
    end
    local awaitingInsert = self.isAwaitingAuthoritativeMediaInsert and self:isAwaitingAuthoritativeMediaInsert() == true or false
    if awaitingInsert == true then
        self.isLidManuallyOpen = false
        self:syncLidFromMedia(false)
        return
    end
    if self.isLidManuallyOpen ~= true then
        self._nmCloseLidAfterContextInsert = false
    end
end

local function updateHoldButtonRotation(self, nowMs)
    self:syncHoldButtonAnimation(false)

    local targetAngle = tonumber(self._nmHoldButtonTargetAngle)
    if targetAngle == nil then
        targetAngle = self:getHoldRotationTargetAngle()
        self._nmHoldButtonTargetAngle = targetAngle
    end

    local currentAngle = tonumber(self._nmHoldButtonAngle)
    if currentAngle == nil then
        self._nmHoldButtonAngle = targetAngle
        self._nmHoldButtonAnimStartAngle = targetAngle
        self._nmHoldButtonAnimating = false
        return
    end

    if self._nmHoldButtonAnimating ~= true then
        self._nmHoldButtonAngle = targetAngle
        return
    end

    local startAngle = tonumber(self._nmHoldButtonAnimStartAngle) or currentAngle
    local startTime = tonumber(self._nmHoldButtonAnimStartTime) or nowMs
    local elapsed = math.max(0, nowMs - startTime)
    local t = math.min(1.0, elapsed / HOLD_BUTTON_ANIMATION_DURATION_MS)
    local eased = 1.0 - ((1.0 - t) * (1.0 - t))
    self._nmHoldButtonAngle = startAngle + ((targetAngle - startAngle) * eased)
    if t >= 1.0 then
        self._nmHoldButtonAngle = targetAngle
        self._nmHoldButtonAnimStartAngle = targetAngle
        self._nmHoldButtonAnimStartTime = nil
        self._nmHoldButtonAnimating = false
    end
end

function CDPlayerWindow:shouldAutoCloseForDistance()
    local player = getPlayer(self.playerNum)
    if not (player and player.DistToSquared) then
        return false
    end

    local target = self.target
    if target and target.kind == "item" then
        local item = target.itemRef
        local location = NMDeviceUIRange.resolvePortableTargetLocation and NMDeviceUIRange.resolvePortableTargetLocation(target, item) or nil
        if location and location.mode == "detached_placed" then
            local distSqDetached = tonumber(player:DistToSquared(location.x, location.y)) or 0
            return distSqDetached > autoCloseThresholdSq()
        end
        if location and location.mode == "placed_world" then
            local distSqFast = tonumber(player:DistToSquared(location.x, location.y)) or 0
            return distSqFast > autoCloseThresholdSq()
        end
        if location and location.mode == "inventory" then
            return false
        end
    end

    local resolved = self:resolveContext()
    if not resolved then
        return true
    end
    local location = NMDeviceUIRange.resolvePortableTargetLocation and NMDeviceUIRange.resolvePortableTargetLocation(target, resolved.item) or nil
    if location and location.mode == "inventory" then
        return false
    end
    if not location or not location.requiresDistanceCheck then
        return false
    end
    local distSq = tonumber(player:DistToSquared(location.x, location.y)) or 0
    return distSq > autoCloseThresholdSq()
end

function CDPlayerWindow:update()
    ISPanel.update(self)

    local nowMs = getNowMs()
    updateWorldCDSpin(self, nowMs)
    updateHoldButtonRotation(self, nowMs)
    updateLidAnimation(self, nowMs)
    updatePendingContextMediaInsert(self)
    updateContextInsertCloseState(self)
    NMSlotHostLifecycle.refreshSlotVisibility(self)
    local lastDistanceCheckMs = tonumber(self._nmLastDistanceCheckMs) or 0
    if (nowMs - lastDistanceCheckMs) >= 250 then
        self._nmLastDistanceCheckMs = nowMs
        if self:shouldAutoCloseForDistance() then
            self:close()
            return
        end
    end

    self:updateHoverTooltip()
    self:updateTooltipUI()
    self:setX(self:clampWindowX(self:getX()))

    if self.draggingHeader == true then
        if self.headerDragMode == "collapsed" then
            self:setY(self:getCollapsedY())
        else
            self:setY(self:getExpandedY())
        end
        return
    end

    if self.isAnimating == true then
        local elapsed = math.max(0, nowMs - (tonumber(self.animStartTime) or 0))
        local t = math.min(1.0, elapsed / ANIMATION_DURATION_MS)
        local eased = 1.0 - ((1.0 - t) * (1.0 - t))
        local startY = tonumber(self.animStartY) or self:getY()
        local targetY = tonumber(self.animTargetY) or self:getStateY(self.isCollapsed)
        local y = startY + ((targetY - startY) * eased)
        self:setY(math.floor(y + 0.5))
        if t >= 1.0 then
            self.isAnimating = false
            self:setY(targetY)
        end
        return
    end

    self._nmExpandedY = self:getExpandedY()
    self:setY(self:getStateY(self.isCollapsed))
end
