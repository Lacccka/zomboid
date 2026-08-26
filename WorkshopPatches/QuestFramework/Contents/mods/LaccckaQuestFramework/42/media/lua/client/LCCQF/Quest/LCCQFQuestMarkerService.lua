require "ISUI/Maps/ISWorldMap"
require "LCCQF/Quest/LCCQFQuestClientState"

LCCQF = LCCQF or {}

local QuestClientState = LCCQF.QuestClientState
local MarkerService = LCCQF.QuestMarkerService or {}
local hiddenMaps = {}
local activeSymbols = {}
local dirty = true
local nextIntegrityMs = 0
local lastMarkerApiFailure = nil

-- B42.20.3 hides non-user-defined symbols when the map renderer's PlaceNames
-- layer is disabled. The engine flag therefore cannot represent framework
-- ownership. Quest authority remains server-side; this distinctive trailing-
-- space token lets the client adapter remove/rebuild only its own annotations.
local MARKER_TEXT = "!   "
local INTEGRITY_INTERVAL_MS = 1000

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

local function isOwnedSymbol(symbol)
    if not symbol or not symbol.isText or not symbol.getUntranslatedText then return false end

    local okText, isText = pcall(function() return symbol:isText() end)
    if not okText or not isText then return false end

    local okValue, text = pcall(function() return symbol:getUntranslatedText() end)
    return okValue and text == MARKER_TEXT
end

local function clearOwnedSymbols(symbols)
    if not symbols then return 0 end

    local removed = 0
    for index = symbols:getSymbolCount() - 1, 0, -1 do
        local symbol = symbols:getSymbolByIndex(index)
        if isOwnedSymbol(symbol) then
            local ok = pcall(function() symbols:removeSymbolByIndex(index) end)
            if ok then removed = removed + 1 end
        end
    end
    return removed
end

local function countOwnedSymbols(symbols)
    if not symbols then return 0 end

    local count = 0
    for index = symbols:getSymbolCount() - 1, 0, -1 do
        if isOwnedSymbol(symbols:getSymbolByIndex(index)) then
            count = count + 1
        end
    end
    return count
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

local function shouldRender(marker)
    return validMarker(marker) and tostring(marker.mode or "EXACT") ~= "HIDDEN"
end

local function expectedMarkerCount()
    local count = 0
    for _, quest in ipairs(QuestClientState.ListActive()) do
        if shouldRender(quest.marker) then count = count + 1 end
    end
    return count
end

local function addExactMarker(symbols, marker)
    -- getSymbolsAPIv2() exposes WorldMapSymbolsV2. The Lua-facing overload is
    -- addUntranslatedText(text, layer/font, x, y); styling is applied after.
    local layerId = "text-note"
    if symbols.getDefaultTextLayerID then
        layerId = symbols:getDefaultTextLayerID() or layerId
    elseif symbols.getDefaultLayerID then
        layerId = symbols:getDefaultLayerID() or layerId
    end

    local symbol = symbols:addUntranslatedText(
        MARKER_TEXT,
        layerId,
        tonumber(marker.x),
        tonumber(marker.y)
    )
    if not symbol then return nil end

    symbol:setAnchor(0.5, 0.5)
    symbol:setRGBA(1.0, 0.35, 0.10, 1.0)
    symbol:setScale(1.15)
    symbol:setCollide(false)
    symbol:setVisible(true)

    -- This is an engine rendering flag, not quest ownership/authority. B42.20.3
    -- suppresses non-user-defined symbols with PlaceNames disabled. We clean,
    -- restore and lifecycle-manage these symbols ourselves from quest views.
    symbol:setUserDefined(true)

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
        log("marker creation failed; presentation disabled until next rebuild: " .. errorText)
    end
    return nil
end

function MarkerService.Rebuild()
    local player = getSpecificPlayer(0)
    if not player then return false end

    local symbols = getSymbolsAPI(player)
    if not symbols then return false end

    -- Remove both current references and any stale copy restored from map save.
    clearActiveSymbols()
    clearOwnedSymbols(symbols)

    local count = 0
    for _, quest in ipairs(QuestClientState.ListActive()) do
        local marker = quest.marker
        if shouldRender(marker) then
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

    dirty = false
    nextIntegrityMs = getTimestampMs() + INTEGRITY_INTERVAL_MS
    log("rebuilt count=" .. tostring(count))
    return true
end

function MarkerService.MarkDirty()
    dirty = true
    nextIntegrityMs = 0
end

function MarkerService.Reset()
    clearActiveSymbols()
    hiddenMaps = {}
    dirty = true
    nextIntegrityMs = 0
    lastMarkerApiFailure = nil
end

local function onQuestStateChanged()
    MarkerService.MarkDirty()
    MarkerService.Rebuild()
end

local function onTick()
    local now = getTimestampMs()
    if now < nextIntegrityMs then return end
    nextIntegrityMs = now + INTEGRITY_INTERVAL_MS

    local player = getSpecificPlayer(0)
    if not player then return end
    local symbols = getSymbolsAPI(player)
    if not symbols then
        dirty = true
        return
    end

    if countOwnedSymbols(symbols) ~= expectedMarkerCount() then
        dirty = true
    end

    if dirty then MarkerService.Rebuild() end
end

local function onGameStart()
    MarkerService.Reset()
end

QuestClientState.AddListener(onQuestStateChanged)
Events.OnTick.Add(onTick)
Events.OnGameStart.Add(onGameStart)

LCCQF.QuestMarkerService = MarkerService

return MarkerService
