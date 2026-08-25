require "ExtractionMode/Config"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local LoadFade = {}

local SETTLE_TIME_MS = 1250
local FADE_IN_SECONDS = 0.75
local stateByPlayer = {}

local function nowMs()
    return getTimestampMs and getTimestampMs() or 0
end

local function insideHideoutCell(player)
    if player == nil or player:getSquare() == nil then return false end
    local bounds = Config.hideoutCellBounds()
    local x, y = player:getX(), player:getY()
    return x >= bounds.minX and x < bounds.maxXExclusive
        and y >= bounds.minY and y < bounds.maxYExclusive
end

local function begin(playerIndex, player)
    playerIndex = math.max(0, tonumber(playerIndex) or 0)
    if stateByPlayer[playerIndex] ~= nil or player == nil or player:getSquare() == nil then return end

    local state = { active = false, readyAt = nil }
    stateByPlayer[playerIndex] = state
    if not insideHideoutCell(player) then return end

    state.active = true
    pcall(function()
        -- Fade before UI blacks out only the world. Menus and the raid HUD stay
        -- visible while the loaded lighting/occlusion data gets its first update.
        UIManager.setFadeBeforeUI(playerIndex, true)
        UIManager.FadeOut(playerIndex, 0.01)
    end)
end

local function onCreatePlayer(playerIndex, player)
    if player == nil and getSpecificPlayer then player = getSpecificPlayer(playerIndex) end
    begin(playerIndex, player)
end

local function onTick()
    local count = getNumActivePlayers and getNumActivePlayers() or 1
    local now = nowMs()
    for playerIndex = 0, count - 1 do
        local player = getSpecificPlayer and getSpecificPlayer(playerIndex)
        if stateByPlayer[playerIndex] == nil then begin(playerIndex, player) end
        local state = stateByPlayer[playerIndex]
        if state and state.active then
            if player == nil or not insideHideoutCell(player) then
                pcall(function() UIManager.FadeIn(playerIndex, 0.1) end)
                state.active = false
            elseif isGamePaused and isGamePaused() then
                -- Single-player commonly opens in a paused state. Do not reveal
                -- the world until the simulation has actually had time to settle.
                state.readyAt = nil
            else
                state.readyAt = state.readyAt or (now + SETTLE_TIME_MS)
                if now >= state.readyAt then
                    pcall(function() UIManager.FadeIn(playerIndex, FADE_IN_SECONDS) end)
                    state.active = false
                end
            end
        end
    end
end

Events.OnCreatePlayer.Add(onCreatePlayer)
Events.OnTick.Add(onTick)

ExtractionMode.HideoutLoadFade = LoadFade
return LoadFade
