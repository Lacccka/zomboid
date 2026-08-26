require "ISUI/Maps/ISWorldMap"
require "LCCQF/Quest/LCCQFQuestClientState"

LCCQF = LCCQF or {}

local QuestClientState = LCCQF.QuestClientState
local MarkerService = LCCQF.QuestMarkerService or {}
local hiddenMaps = {}
local activeSymbols = {}
local dirty = true
local nextRetryMs = 0
local lastMarkerApiFailure = nil

local function log(message)
    print("[LCCQF][MARKER:CLIENT] " .. tostring(message))
end

local function ensureHiddenMap(player)
    if not player or not ISWorldMap then return nil end

    local playerNum = player:getPlayerNum()
    local existing = hiddenMaps[playerNum]
    if existing and existing.javaObject then return existing end

    local map = ISWorldMap:new(0, 0, 10, 10)
    map:initialise()
    map:instantiate()
    map.character = player
    map.playerNum = playerNum
    map:initDataAndStyle()
    map:setVisible(false)

    hiddenMaps[playerNum] = map
    return map
end

local function getSymbolsAPI(player)
    local map = ensureHiddenMap(player)
    if not map or not map.mapAPI then return nil end

    local symbols = map.mapAPI:getSymbolsAPIv2()
    if symbols and symbols.initDefaultAnnotations and not map.lccqfAnnotationsReady then
        symbols:initDefaultAnnotations()
        map.lccqfAnnotationsReady = true
    end
    return symbols
end

local function clearActiveSymbols()
    for _, entry in ipairs(activeSymbols) do
        if entry.symbols and entry.symbol then
            pcall(function()
                entry.symbols:removeSymbol(entry.symbol)
            end)
        end
    end
    activeSymbols = {}
end

local function validMarker(marker)
    return type(marker) == "table"
        and marker.visible ~= false
        and marker.showOnWorldMap ~= false
        and type(marker.markerId) == "string"
        and tonumber(marker.x) ~= nil
        and tonumber(marker.y) ~= nil
end

local function addExactMarker(symbols, marker)
    -- Build 42.20.3 getSymbolsAPIv2() returns WorldMapSymbolsV2. Its Lua-facing
    -- addUntranslatedText overload accepts only (text, layer/font, x, y).
    -- Presentation properties belong to the returned WorldMapTextSymbolV2.
    local layerId = "text-note"
    if symbols.getDefaultTextLayerID then
        layerId = symbols:getDefaultTextLayerID() or layerId
    elseif symbols.getDefaultLayerID then
        layerId = symbols:getDefaultLayerID() or layerId
    end

    local symbol = symbols:addUntranslatedText(
        "!",
        layerId,
        tonumber(marker.x),
        tonumber(marker.y)
    )
    if not symbol then return nil end

    symbol:setAnchor(0.5, 0.5)
    symbol:setRGBA(1.0, 0.35, 0.10, 1.0)
    symbol:setScale(1.15)
    symbol:setCollide(false)
    symbol:setUserDefined(false)
    if symbol.setApplyZoom then symbol:setApplyZoom(false) end
    if marker.minZoom and symbol.setMinZoom then symbol:setMinZoom(tonumber(marker.minZoom) or 0) end
    if marker.maxZoom and symbol.setMaxZoom then symbol:setMaxZoom(tonumber(marker.maxZoom) or 24) end
    return symbol
end

local function safeAddExactMarker(symbols, marker)
    local ok, symbolOrError = pcall(addExactMarker, symbols, marker)
    if ok then
        lastMarkerApiFailure = nil
        return symbolOrError
    end

    local errorText = tostring(symbolOrError)
    if errorText ~= lastMarkerApiFailure then
        lastMarkerApiFailure = errorText
        log("marker creation failed; marker disabled until quest state changes: " .. errorText)
    end
    return nil
end

function MarkerService.Rebuild()
    local player = getSpecificPlayer(0)
    if not player then return false end

    local symbols = getSymbolsAPI(player)
    if not symbols then return false end

    clearActiveSymbols()

    local count = 0
    for _, quest in ipairs(QuestClientState.ListActive()) do
        local marker = quest.marker
        if validMarker(marker) and tostring(marker.mode or "EXACT") ~= "HIDDEN" then
            local symbol = safeAddExactMarker(symbols, marker)
            if symbol then
                activeSymbols[#activeSymbols + 1] = {
                    markerId = marker.markerId,
                    symbols = symbols,
                    symbol = symbol,
                }
                count = count + 1
            end
        end
    end

    -- A Java/Lua API mismatch is a presentation failure, not a reason to throw
    -- once per retry tick. A later quest-state change can request another build.
    dirty = false
    log("rebuilt count=" .. tostring(count))
    return true
end

function MarkerService.MarkDirty()
    dirty = true
    nextRetryMs = 0
end

function MarkerService.Reset()
    clearActiveSymbols()
    hiddenMaps = {}
    dirty = true
    nextRetryMs = 0
    lastMarkerApiFailure = nil
end

local function onQuestStateChanged()
    MarkerService.MarkDirty()
    MarkerService.Rebuild()
end

local function onTick()
    if not dirty then return end
    local now = getTimestampMs()
    if now < nextRetryMs then return end
    nextRetryMs = now + 1000
    MarkerService.Rebuild()
end

local function onGameStart()
    MarkerService.Reset()
end

QuestClientState.AddListener(onQuestStateChanged)
Events.OnTick.Add(onTick)
Events.OnGameStart.Add(onGameStart)

LCCQF.QuestMarkerService = MarkerService

return MarkerService
