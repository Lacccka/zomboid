-- Starting world-map knowledge: top-right Louisville only; active-quest objective marker.

if isServer() then return end

require "ISUI/ISPanel"
require "ISUI/Maps/ISMapDefinitions"

local MOD_ID = ModpackFestivalMap.MOD_ID
local MARKER_UPDATE_TICKS = ModpackFestivalTick.SLOW

local tickCount = 0
local mapBridge = nil
local questSymbol = nil
local questSymbolQuestId = nil

local function getPlayer()
    return getSpecificPlayer(0)
end

local function forgetWorldMap(player)
    if WorldMapVisited and WorldMapVisited.getInstance then
        WorldMapVisited.getInstance():forget()
    end
    if player and sendClientCommand then
        sendClientCommand(player, "map", "forget", {})
    end
end

local function revealKnownLouisville(player)
    if not WorldMapVisited or not WorldMapVisited.getInstance then return end

    WorldMapVisited.getInstance():setKnownInSquares(
        ModpackFestivalMap.LOUISVILLE_KNOWN_X1,
        ModpackFestivalMap.LOUISVILLE_KNOWN_Y1,
        ModpackFestivalMap.LOUISVILLE_KNOWN_X2,
        ModpackFestivalMap.LOUISVILLE_KNOWN_Y2
    )

    if player and sendClientCommand then
        sendClientCommand(player, "map", "setKnownInSquares", {
            x1 = ModpackFestivalMap.LOUISVILLE_KNOWN_X1,
            y1 = ModpackFestivalMap.LOUISVILLE_KNOWN_Y1,
            x2 = ModpackFestivalMap.LOUISVILLE_KNOWN_X2,
            y2 = ModpackFestivalMap.LOUISVILLE_KNOWN_Y2,
        })
    end
end

-- Once per character: wipe default map memory, then grant only northeast Louisville.
local function applyStartingMapKnowledge(player)
    local pmd = ModpackFestivalMap.getPlayerMapData(player)
    if not pmd or pmd.startingMapKnowledgeApplied then return end

    forgetWorldMap(player)
    revealKnownLouisville(player)

    pmd.startingMapKnowledgeApplied = true
    pmd.louisvilleRevealed = true
    print("[" .. MOD_ID .. "] world map: top-right Louisville only ("
        .. ModpackFestivalMap.LOUISVILLE_KNOWN_X1 .. "," .. ModpackFestivalMap.LOUISVILLE_KNOWN_Y1
        .. " to " .. ModpackFestivalMap.LOUISVILLE_KNOWN_X2 .. "," .. ModpackFestivalMap.LOUISVILLE_KNOWN_Y2 .. ")")
end

local function ensureMapBridge()
    if mapBridge and mapBridge.mapAPI then return true end
    if not UIWorldMap or not MapItem or not MapItem.getSingleton then return false end

    local panel = ISPanel:new(-10000, -10000, 32, 32)
    panel.javaObject = UIWorldMap.new(panel)
    panel.mapAPI = panel.javaObject:getAPIv3()
    panel.mapAPI:setMapItem(MapItem.getSingleton())
    if MapUtils and MapUtils.initDefaultMapData then
        MapUtils.initDefaultMapData(panel)
    end

    mapBridge = panel
    return true
end

local function getSymbolsAPI()
    if ISWorldMap_instance and ISWorldMap_instance.mapAPI then
        return ISWorldMap_instance.mapAPI:getSymbolsAPIv2()
    end
    if ensureMapBridge() then
        return mapBridge.mapAPI:getSymbolsAPIv2()
    end
    return nil
end

local function symbolMatchesMarker(symbol, cfg)
    if not symbol or not cfg or not symbol.isTexture or not symbol:isTexture() then return false end
    if symbol:getSymbolID() ~= cfg.symbolId then return false end
    return math.abs(symbol:getRed() - cfg.r) < 0.05
        and math.abs(symbol:getGreen() - cfg.g) < 0.05
        and math.abs(symbol:getBlue() - cfg.b) < 0.05
end

local function isModQuestSymbol(symbol)
    if not symbol or not ModpackFestivalMap.QUEST_MARKERS then return false end
    for questId, cfg in pairs(ModpackFestivalMap.QUEST_MARKERS) do
        if symbolMatchesMarker(symbol, cfg) then
            return true, questId
        end
    end
    return false
end

local function findQuestSymbol(symbolsAPI, cfg)
    if not symbolsAPI or not symbolsAPI.getSymbolCount or not cfg then return nil end
    for i = 1, symbolsAPI:getSymbolCount() do
        local symbol = symbolsAPI:getSymbolByIndex(i - 1)
        if symbolMatchesMarker(symbol, cfg) then
            return symbol
        end
    end
    return nil
end

local function removeAllModQuestSymbols(symbolsAPI)
    if questSymbol and symbolsAPI and symbolsAPI.removeSymbol then
        pcall(function()
            symbolsAPI:removeSymbol(questSymbol)
        end)
    elseif symbolsAPI and symbolsAPI.getSymbolCount and symbolsAPI.removeSymbolByIndex then
        for i = symbolsAPI:getSymbolCount(), 1, -1 do
            local symbol = symbolsAPI:getSymbolByIndex(i - 1)
            if isModQuestSymbol(symbol) then
                symbolsAPI:removeSymbolByIndex(i - 1)
            end
        end
    end
    questSymbol = nil
    questSymbolQuestId = nil
end

local function vehicleMapRotation(vehicle)
    if not vehicle then return 0 end
    if vehicle.getDirectionAngle then
        local angle = vehicle:getDirectionAngle()
        if angle and PZMath and PZMath.radToDeg then
            return PZMath.radToDeg(angle)
        end
        return angle
    end
    if vehicle.getAngleZ then
        return vehicle:getAngleZ()
    end
    return 0
end

local function ensureQuestSymbol(symbolsAPI, cfg, x, y)
    if questSymbol and symbolMatchesMarker(questSymbol, cfg) then
        return questSymbol
    end

    questSymbol = findQuestSymbol(symbolsAPI, cfg)
    if questSymbol then
        return questSymbol
    end

    questSymbol = symbolsAPI:addTexture(cfg.symbolId, x, y)
    if not questSymbol then return nil end

    questSymbol:setRGBA(cfg.r, cfg.g, cfg.b, 1.0)
    questSymbol:setAnchor(0.5, 0.5)
    questSymbol:setScale(ISMap and ISMap.SCALE or 1.0)
    questSymbol:setApplyZoom(true)
    questSymbol:setUserDefined(false)
    return questSymbol
end

local function updateQuestObjectiveMarker()
    local player = getPlayer()
    local symbolsAPI = getSymbolsAPI()
    if not symbolsAPI then return end

    local cfg, questId = ModpackFestivalMap.getActiveQuestMarker()
    if not cfg or not questId or not player or not ModpackFestivalQuests then
        removeAllModQuestSymbols(symbolsAPI)
        return
    end

    if questSymbolQuestId ~= questId then
        removeAllModQuestSymbols(symbolsAPI)
        questSymbolQuestId = questId
    end

    local quest = ModpackFestivalQuests.getDefinition(questId)
    local tx, ty = ModpackFestivalQuests.getQuestTargetXY(player, quest)
    if not tx or not ty then
        removeAllModQuestSymbols(symbolsAPI)
        return
    end

    local symbol = ensureQuestSymbol(symbolsAPI, cfg, tx, ty)
    if not symbol then return end

    symbol:setPosition(tx, ty)

    if cfg.rotateWithVehicle and ModpackFestivalQuests.findFestivalVehicle then
        local vehicle = ModpackFestivalQuests.findFestivalVehicle(getCell())
        if vehicle then
            symbol:setRotation(vehicleMapRotation(vehicle))
        else
            symbol:setRotation(0)
        end
    else
        symbol:setRotation(0)
    end
end

local function onTick()
    local player = getPlayer()
    if not player or not player:getSquare() then return end

    tickCount = tickCount + 1
    if tickCount == 1 or ModpackFestivalTick.every(tickCount, ModpackFestivalTick.MAINT) then
        applyStartingMapKnowledge(player)
    end

    if not ModpackFestivalTick.every(tickCount, MARKER_UPDATE_TICKS) then return end
    updateQuestObjectiveMarker()
end

local function onGameStart()
    questSymbol = nil
    questSymbolQuestId = nil
    tickCount = 0
    local player = getPlayer()
    if player then
        applyStartingMapKnowledge(player)
        updateQuestObjectiveMarker()
    end
end

local function onWorldMapReady()
    questSymbol = nil
    updateQuestObjectiveMarker()
end

local function hookWorldMapOpen()
    if not ISWorldMap or ISWorldMap._ModpackFestivalMapHooked then return end

    local origShow = ISWorldMap.ShowWorldMap
    ISWorldMap.ShowWorldMap = function(playerNum, centerX, centerY, zoom)
        origShow(playerNum, centerX, centerY, zoom)
        onWorldMapReady()
    end
    ISWorldMap._ModpackFestivalMapHooked = true
end

local function onCreatePlayer(playerIndex, player)
    if playerIndex ~= 0 or not player then return end
    applyStartingMapKnowledge(player)
    updateQuestObjectiveMarker()
end

Events.OnTick.Add(onTick)
Events.OnGameStart.Add(onGameStart)
Events.OnCreatePlayer.Add(onCreatePlayer)
hookWorldMapOpen()

print("[" .. MOD_ID .. "] map client ready (top-right Louisville + quest objective markers)")
