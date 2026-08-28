require "ISUI/Maps/ISWorldMap"
require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local DebugClient = LCCQF.FactionSiteDebugClient or {}
local MARKER_PREFIX = "[LCCQF-FS] "
local hiddenMaps = {}
local activeSymbols = {}
local sites = {}
local visible = false
local requestShow = false
local revision = 0

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:DEBUG:CLIENT] " .. tostring(message))
end

local function isPrivileged(player)
    if isDebugEnabled and isDebugEnabled() then return true end
    if not player or not player.getAccessLevel then return false end
    local access = tostring(player:getAccessLevel() or ""):lower()
    return access ~= "" and access ~= "none"
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

local function symbolsAPI(player)
    local map = ensureHiddenMap(player)
    if not map or not map.mapAPI then return nil end
    local symbols = map.mapAPI:getSymbolsAPIv2()
    if symbols and symbols.initDefaultAnnotations and not map.lccqfFactionSiteAnnotationsReady then
        symbols:initDefaultAnnotations()
        map.lccqfFactionSiteAnnotationsReady = true
    end
    return symbols
end

local function symbolText(symbol)
    if not symbol or not symbol.isText or not symbol.getUntranslatedText then return nil end
    local okText, isText = pcall(function() return symbol:isText() end)
    if not okText or not isText then return nil end
    local okValue, text = pcall(function() return symbol:getUntranslatedText() end)
    if not okValue then return nil end
    return text
end

local function isOwnedSymbol(symbol)
    local text = symbolText(symbol)
    return type(text) == "string" and string.sub(text, 1, #MARKER_PREFIX) == MARKER_PREFIX
end

local function clearOwnedSymbols(symbols)
    if not symbols then return end
    for index = symbols:getSymbolCount() - 1, 0, -1 do
        if isOwnedSymbol(symbols:getSymbolByIndex(index)) then
            pcall(function() symbols:removeSymbolByIndex(index) end)
        end
    end
end

local function clearActiveSymbols()
    for _, entry in ipairs(activeSymbols) do
        if entry.symbols and entry.symbol then
            pcall(function() entry.symbols:removeSymbol(entry.symbol) end)
        end
    end
    activeSymbols = {}
end

local function resourceSummary(site)
    local counts = site.resources and site.resources.counts or {}
    if not site.resources then return "resources=unscanned" end
    return "beds=" .. tostring(counts.beds or 0)
        .. " water=" .. tostring(counts.water or 0)
        .. " storage=" .. tostring(counts.storage or 0)
        .. " food=" .. tostring(counts.food or 0)
        .. " spawn=" .. tostring(counts.freeSpawnPoints or 0)
end

local function markerLabel(site)
    return MARKER_PREFIX .. tostring(site.siteId or "site")
        .. " " .. tostring(site.state or "UNKNOWN")
end

local function addMarker(symbols, site)
    local anchor = site and site.anchor
    if not symbols or type(anchor) ~= "table" or tonumber(anchor.x) == nil or tonumber(anchor.y) == nil then
        return nil
    end

    local layerId = "text-note"
    if symbols.getDefaultTextLayerID then
        layerId = symbols:getDefaultTextLayerID() or layerId
    elseif symbols.getDefaultLayerID then
        layerId = symbols:getDefaultLayerID() or layerId
    end

    local symbol = symbols:addUntranslatedText(
        markerLabel(site),
        layerId,
        tonumber(anchor.x),
        tonumber(anchor.y)
    )
    if not symbol then return nil end

    symbol:setAnchor(0.5, 0.5)
    symbol:setRGBA(0.20, 0.85, 1.00, 1.0)
    symbol:setScale(0.85)
    symbol:setCollide(false)
    symbol:setVisible(true)
    symbol:setUserDefined(true)
    if symbol.setApplyZoom then symbol:setApplyZoom(false) end
    return symbol
end

function DebugClient.Hide()
    local player = getSpecificPlayer(0)
    local symbols = player and symbolsAPI(player) or nil
    clearActiveSymbols()
    clearOwnedSymbols(symbols)
    visible = false
    requestShow = false
    log("markers hidden")
end

function DebugClient.Rebuild()
    if not visible then return false end
    local player = getSpecificPlayer(0)
    if not player then return false end
    local symbols = symbolsAPI(player)
    if not symbols then return false end

    clearActiveSymbols()
    clearOwnedSymbols(symbols)

    local count = 0
    for _, site in ipairs(sites) do
        local ok, symbol = pcall(addMarker, symbols, site)
        if ok and symbol then
            activeSymbols[#activeSymbols + 1] = {
                siteId = site.siteId,
                symbols = symbols,
                symbol = symbol,
            }
            count = count + 1
        end

        local anchor = site.anchor or {}
        log("siteId=" .. tostring(site.siteId)
            .. " factionId=" .. tostring(site.factionId)
            .. " state=" .. tostring(site.state)
            .. " anchor=" .. tostring(anchor.x) .. "," .. tostring(anchor.y) .. "," .. tostring(anchor.z)
            .. " zone=" .. tostring(site.zoneType)
            .. " rooms=" .. tostring(site.roomCount)
            .. " score=" .. string.format("%.2f", tonumber(site.score) or 0)
            .. " " .. resourceSummary(site)
            .. " reason=" .. tostring(site.lastReason or "none"))
    end
    log("markers rebuilt revision=" .. tostring(revision) .. " count=" .. tostring(count))
    return true
end

function DebugClient.Request(showAfterResponse)
    local player = getSpecificPlayer(0)
    if not player or not isPrivileged(player) then return false end
    requestShow = showAfterResponse == true or visible
    sendClientCommand(player, C.MODULE, C.COMMAND.REQUEST_FACTION_SITES_DEBUG, {})
    log("snapshot requested show=" .. tostring(requestShow))
    return true
end

local function showSites(player)
    requestShow = true
    DebugClient.Request(true)
end

local function refreshSites(player)
    DebugClient.Request(visible)
end

local function hideSites(player)
    DebugClient.Hide()
end

local function onFillWorldObjectContextMenu(playerNum, context, worldObjects, test)
    if test then return end
    local player = getSpecificPlayer(playerNum)
    if not isPrivileged(player) then return end

    if visible then
        context:addOption("[Quest Framework] Refresh faction sites (debug)", player, refreshSites)
        context:addOption("[Quest Framework] Hide faction sites (debug)", player, hideSites)
    else
        context:addOption("[Quest Framework] Show faction sites on map (debug)", player, showSites)
    end
end

local function onServerCommand(module, command, args)
    if module ~= C.MODULE or command ~= C.COMMAND.FACTION_SITES_DEBUG then return end
    args = args or {}
    sites = type(args.sites) == "table" and args.sites or {}
    revision = math.max(0, tonumber(args.revision) or 0)
    if requestShow then visible = true end
    requestShow = false
    log("snapshot received revision=" .. tostring(revision) .. " sites=" .. tostring(#sites))
    if visible then DebugClient.Rebuild() end
end

local function onGameStart()
    DebugClient.Hide()
    sites = {}
    revision = 0
end

if Events and Events.OnFillWorldObjectContextMenu then
    Events.OnFillWorldObjectContextMenu.Add(onFillWorldObjectContextMenu)
end
if Events and Events.OnServerCommand then Events.OnServerCommand.Add(onServerCommand) end
if Events and Events.OnGameStart then Events.OnGameStart.Add(onGameStart) end

DebugClient.GetSites = function() return sites end
DebugClient.IsVisible = function() return visible end
LCCQF.FactionSiteDebugClient = DebugClient
return DebugClient
