-- Temporarily mutes vanilla background music while New Music is audibly playing for the local client.
NMClientVanillaMusicSuppressor = NMClientVanillaMusicSuppressor or {}
NMClientVanillaMusicSuppressor._tickAudible = false
NMClientVanillaMusicSuppressor._suppressed = NMClientVanillaMusicSuppressor._suppressed or false
NMClientVanillaMusicSuppressor._capturedVolume = NMClientVanillaMusicSuppressor._capturedVolume
NMClientVanillaMusicSuppressor._lastKnownUserVolume = NMClientVanillaMusicSuppressor._lastKnownUserVolume
NMClientVanillaMusicSuppressor._lastRefreshTick = NMClientVanillaMusicSuppressor._lastRefreshTick or 0

local function logRuntimeProbe(tag, detail)
    if NMCore and NMCore.logChannel and NMCore.isDebugKnobOn and NMCore.isDebugKnobOn("runtimeProbe") then
        NMCore.logChannel("runtimeProbe", tag, detail)
    end
end

local function getMusicManager()
    if not getSoundManager then
        return nil
    end
    return getSoundManager()
end

local function clampVolume(value)
    return NMCore and NMCore.clamp and NMCore.clamp(tonumber(value) or 0, 0, 1) or math.max(0, math.min(1, tonumber(value) or 0))
end

local function getCurrentMusicVolume(sm)
    if not (sm and sm.getMusicVolume) then
        return nil
    end
    local ok, value = pcall(function()
        return sm:getMusicVolume()
    end)
    if not ok then
        return nil
    end
    return clampVolume(value)
end

local function setMusicVolume(sm, value)
    if not (sm and sm.setMusicVolume) then
        return false
    end
    local ok = pcall(function()
        sm:setMusicVolume(clampVolume(value))
    end)
    return ok == true
end

function NMClientVanillaMusicSuppressor.beginTick()
    NMClientVanillaMusicSuppressor._tickAudible = false
end

function NMClientVanillaMusicSuppressor.observeAudibility(player, profile, state, source)
    if NMClientVanillaMusicSuppressor._tickAudible == true then
        return
    end
    if not (NMPlaybackRuntime and NMPlaybackRuntime.computeLocalListenerAudibility) then
        return
    end
    local audibility = NMPlaybackRuntime.computeLocalListenerAudibility(player, profile, state, source)
    if audibility and audibility.shouldSuppressVanillaMusic == true then
        NMClientVanillaMusicSuppressor._tickAudible = true
    end
end

function NMClientVanillaMusicSuppressor.endTick(tickCount)
    local sm = getMusicManager()
    if not sm then
        return
    end

    local audible = NMClientVanillaMusicSuppressor._tickAudible == true
    local suppressed = NMClientVanillaMusicSuppressor._suppressed == true
    local currentVolume = getCurrentMusicVolume(sm)

    if currentVolume and currentVolume > 0 then
        NMClientVanillaMusicSuppressor._lastKnownUserVolume = currentVolume
        if not suppressed then
            NMClientVanillaMusicSuppressor._capturedVolume = currentVolume
        end
    end

    if audible then
        if not suppressed then
            local capture = NMClientVanillaMusicSuppressor._capturedVolume
            if capture == nil then
                capture = currentVolume
            end
            if capture == nil then
                capture = NMClientVanillaMusicSuppressor._lastKnownUserVolume
            end
            if capture == nil then
                capture = 0
            end
            NMClientVanillaMusicSuppressor._capturedVolume = clampVolume(capture)
            setMusicVolume(sm, 0)
            NMClientVanillaMusicSuppressor._suppressed = true
            NMClientVanillaMusicSuppressor._lastRefreshTick = tonumber(tickCount) or 0
            logRuntimeProbe(
                "vanilla_music_suppress_start",
                string.format(
                    "captured=%.3f current=%s",
                    tonumber(NMClientVanillaMusicSuppressor._capturedVolume) or 0,
                    tostring(currentVolume ~= nil and string.format("%.3f", currentVolume) or "nil")
                )
            )
            return
        end

        if currentVolume and currentVolume > 0 then
            NMClientVanillaMusicSuppressor._capturedVolume = currentVolume
            NMClientVanillaMusicSuppressor._lastKnownUserVolume = currentVolume
        elseif currentVolume and currentVolume < 0 then
            currentVolume = 0
        end

        if currentVolume ~= 0 then
            setMusicVolume(sm, 0)
        end

        local tickNow = tonumber(tickCount) or 0
        local lastRefreshTick = tonumber(NMClientVanillaMusicSuppressor._lastRefreshTick) or 0
        if (tickNow - lastRefreshTick) >= 600 then
            NMClientVanillaMusicSuppressor._lastRefreshTick = tickNow
            logRuntimeProbe(
                "vanilla_music_suppress_refresh",
                string.format(
                    "captured=%.3f current=%s",
                    tonumber(NMClientVanillaMusicSuppressor._capturedVolume) or 0,
                    tostring(currentVolume ~= nil and string.format("%.3f", currentVolume) or "nil")
                )
            )
        end
        return
    end

    if not suppressed then
        return
    end

    local restore = NMClientVanillaMusicSuppressor._capturedVolume
    if restore == nil then
        restore = NMClientVanillaMusicSuppressor._lastKnownUserVolume
    end
    if restore == nil then
        restore = currentVolume
    end
    restore = clampVolume(restore)
    setMusicVolume(sm, restore)
    NMClientVanillaMusicSuppressor._suppressed = false
    NMClientVanillaMusicSuppressor._capturedVolume = nil
    NMClientVanillaMusicSuppressor._lastRefreshTick = tonumber(tickCount) or 0
    logRuntimeProbe(
        "vanilla_music_suppress_end",
        string.format(
            "restored=%.3f current=%s",
            tonumber(restore) or 0,
            tostring(currentVolume ~= nil and string.format("%.3f", currentVolume) or "nil")
        )
    )
end
