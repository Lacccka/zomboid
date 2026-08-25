-- QP Survivor Contracts
-- v1.0.0 accepted-contract world map markers - multi-objective support

require "QPSurvivorContracts/QPSC_I18N"

QPSC_MapMarkers = QPSC_MapMarkers or {}
QPSC_MapMarkers.lastSignature = QPSC_MapMarkers.lastSignature or ""
QPSC_MapMarkers.lastMapObject = QPSC_MapMarkers.lastMapObject or nil
QPSC_MapMarkers.tickCounter = QPSC_MapMarkers.tickCounter or 0
QPSC_MapMarkers.warnedUnavailable = QPSC_MapMarkers.warnedUnavailable or false
QPSC_MapMarkers.wasMapVisible = QPSC_MapMarkers.wasMapVisible == true
QPSC_MapMarkers.ownedGridMarkers = QPSC_MapMarkers.ownedGridMarkers or {}
QPSC_MapMarkers.symbolsReady = QPSC_MapMarkers.symbolsReady == true
QPSC_MapMarkers.symbolRegistrationFailed = QPSC_MapMarkers.symbolRegistrationFailed == true
QPSC_MapMarkers.warnedSymbolRegistration = QPSC_MapMarkers.warnedSymbolRegistration == true

local QPSC_MAP_SYMBOL_IDS = {
    qpsc_contract_delivery = true,
    qpsc_contract_hunt = true,
    qpsc_contract_location = true
}

local function QPSC_MM_normalizeUsername(value)
    return string.lower(
        tostring(value or "")
            :gsub("^%s+", "")
            :gsub("%s+$", "")
    )
end

local function QPSC_MM_getUsername(player)
    if player == nil then return "" end

    for _, methodName in ipairs({"getUsername", "getDisplayName"}) do
        if player[methodName] ~= nil then
            local ok, value = pcall(function()
                return player[methodName](player)
            end)

            if ok and value ~= nil and tostring(value) ~= "" then
                return tostring(value)
            end
        end
    end

    return ""
end

local function QPSC_MM_findParticipant(contract, username)
    local wanted = QPSC_MM_normalizeUsername(username)

    for _, participant in ipairs(contract.participants or {}) do
        if QPSC_MM_normalizeUsername(participant.username) == wanted then
            return participant
        end
    end

    return nil
end

local function QPSC_MM_getActiveContracts()
    local player = getPlayer and getPlayer() or nil
    if player == nil then return {} end

    local username = QPSC_MM_getUsername(player)
    local result = {}

    for _, contract in ipairs(QPSC_Client and QPSC_Client.contracts or {}) do
        local participant = QPSC_MM_findParticipant(contract, username)
        local objectiveType = string.upper(
            tostring(contract.objectiveType or "MANUAL")
        )

        if participant ~= nil
            and tostring(participant.status or "") == "Accepted"
            and objectiveType ~= "MANUAL"
            and contract.closed ~= true then
            table.insert(result, contract)
        end
    end

    table.sort(result, function(left, right)
        return (tonumber(left.id) or 0) < (tonumber(right.id) or 0)
    end)

    return result
end

local function QPSC_MM_buildSignature(contracts)
    local pieces = {}

    for _, contract in ipairs(contracts or {}) do
        local contractPieces = {
            tostring(contract.id or ""),
            tostring(contract.objectiveType or ""),
            tostring(contract.targetX or ""),
            tostring(contract.targetY or ""),
            tostring(contract.objectiveRadius or ""),
            tostring(contract.title or "")
        }

        if contract.multiObjective == true
            or string.upper(tostring(contract.objectiveType or "")) == "MULTI" then
            for _, objective in ipairs(contract.objectives or {}) do
                table.insert(contractPieces, table.concat({
                    tostring(objective.id or ""),
                    tostring(objective.type or ""),
                    tostring(objective.targetX or ""),
                    tostring(objective.targetY or ""),
                    tostring(objective.radius or ""),
                    tostring(objective.target or "")
                }, ":"))
            end
        end

        table.insert(pieces, table.concat(contractPieces, "|"))
    end

    return table.concat(pieces, ";")
end

local function QPSC_MM_getMapInstance()
    local instance = rawget(_G, "ISWorldMap_instance")
    if instance == nil then return nil end

    if instance.isVisible ~= nil then
        local ok, visible = pcall(function()
            return instance:isVisible()
        end)
        if ok and visible == false then return nil end
    end

    return instance
end

local function QPSC_MM_getMapAPI(instance)
    if instance == nil then return nil end

    local candidates = {instance.mapAPI, instance.api}

    if instance.javaObject ~= nil then
        local ok, api = pcall(function()
            if instance.javaObject.getAPI ~= nil then
                return instance.javaObject:getAPI()
            end
            if instance.javaObject.getAPIv1 ~= nil then
                return instance.javaObject:getAPIv1()
            end
            return nil
        end)
        if ok and api ~= nil then table.insert(candidates, api) end
    end

    for _, api in ipairs(candidates) do
        if api ~= nil then return api end
    end

    return nil
end

local function QPSC_MM_getSymbolsAPI(api)
    if api == nil or api.getSymbolsAPI == nil then return nil end
    local ok, symbols = pcall(function() return api:getSymbolsAPI() end)
    if ok then return symbols end
    return nil
end

local function QPSC_MM_getMarkersAPI(api)
    if api == nil or api.getMarkersAPI == nil then return nil end
    local ok, markers = pcall(function() return api:getMarkersAPI() end)
    if ok then return markers end
    return nil
end

local function QPSC_MM_registerSymbols()
    -- Custom texture symbols are intentionally disabled. In B42 they can
    -- render as oversized or opaque white shapes depending on the map
    -- symbol renderer. The stable presentation is the colored contract
    -- zone/point marker only. Existing QPSC texture symbols are still
    -- removed during sync by QPSC_MM_clearOwnedSymbols().
    QPSC_MapMarkers.symbolsReady = false
    QPSC_MapMarkers.symbolRegistrationFailed = true
    return false
end

local function QPSC_MM_symbolID(symbol)
    if symbol == nil or symbol.isTexture == nil then return "" end
    local ok, isTexture = pcall(function() return symbol:isTexture() end)
    if not ok or not isTexture then return "" end
    local idOk, symbolID = pcall(function() return symbol:getSymbolID() end)
    if idOk then return tostring(symbolID or "") end
    return ""
end

local function QPSC_MM_symbolText(symbol)
    if symbol == nil or symbol.isText == nil then return "" end
    local ok, isText = pcall(function() return symbol:isText() end)
    if not ok or not isText then return "" end
    local textOk, text = pcall(function()
        if symbol.getUntranslatedText ~= nil then
            return symbol:getUntranslatedText()
        end
        return ""
    end)
    if textOk then return tostring(text or "") end
    return ""
end

local function QPSC_MM_clearOwnedSymbols(symbols)
    if symbols == nil
        or symbols.getSymbolCount == nil
        or symbols.removeSymbolByIndex == nil then return end

    local ok, count = pcall(function() return symbols:getSymbolCount() end)
    if not ok then return end

    for index = (tonumber(count) or 0) - 1, 0, -1 do
        local symbolOk, symbol = pcall(function()
            return symbols:getSymbolByIndex(index)
        end)

        if symbolOk and symbol ~= nil then
            local symbolID = QPSC_MM_symbolID(symbol)
            local text = QPSC_MM_symbolText(symbol)
            local owned = QPSC_MAP_SYMBOL_IDS[symbolID] == true
                or string.sub(text, 1, 6) == "[QPSC]"

            if owned then
                pcall(function() symbols:removeSymbolByIndex(index) end)
            end
        end
    end
end

local function QPSC_MM_clearOwnedGridMarkers(markers)
    if markers ~= nil and markers.removeMarker ~= nil then
        for _, marker in ipairs(QPSC_MapMarkers.ownedGridMarkers or {}) do
            pcall(function() markers:removeMarker(marker) end)
        end
    end
    QPSC_MapMarkers.ownedGridMarkers = {}
end

local function QPSC_MM_centerSymbolID(contract)
    local objectiveType = string.upper(tostring(contract.objectiveType or "MANUAL"))
    if objectiveType == "DELIVERY" then return "qpsc_contract_delivery" end
    if objectiveType == "KILL" then return "qpsc_contract_hunt" end
    return "qpsc_contract_location"
end

local function QPSC_MM_markerStyle(contract)
    local objectiveType = string.upper(tostring(contract.objectiveType or "MANUAL"))

    if objectiveType == "DELIVERY" then
        return 4, 0.98, 0.58, 0.08, 0.72, 18
    end

    if objectiveType == "KILL" then
        return math.max(1, math.floor(tonumber(contract.objectiveRadius) or 1)),
            1.00, 0.12, 0.12, 0.46, 22
    end

    return math.max(1, math.floor(tonumber(contract.objectiveRadius) or 1)),
        0.12, 0.58, 1.00, 0.46, 22
end

local function QPSC_MM_addGridMarker(
    markers, x, y, radius, r, g, b, a, minScreenRadius
)
    if markers == nil or markers.addGridSquareMarker == nil then return end

    local ok, marker = pcall(function()
        return markers:addGridSquareMarker(x, y, radius, r, g, b, a)
    end)

    if not ok or marker == nil then return end

    if marker.setBlink ~= nil then
        pcall(function() marker:setBlink(false) end)
    end

    if marker.setMinScreenRadius ~= nil then
        pcall(function()
            marker:setMinScreenRadius(
                math.max(1, math.floor(tonumber(minScreenRadius) or 1))
            )
        end)
    end

    table.insert(QPSC_MapMarkers.ownedGridMarkers, marker)
end

local function QPSC_MM_addObjectiveMarker(markers, objective)
    local markerContract = {
        objectiveType = tostring(objective.type or "LOCATION"),
        objectiveRadius = tonumber(objective.radius) or 1
    }
    local x = math.floor(tonumber(objective.targetX) or 0)
    local y = math.floor(tonumber(objective.targetY) or 0)
    local radius, r, g, b, a, minScreenRadius =
        QPSC_MM_markerStyle(markerContract)

    QPSC_MM_addGridMarker(
        markers, x, y, radius, r, g, b, a, minScreenRadius
    )
    QPSC_MM_addGridMarker(
        markers, x, y, 1, r, g, b, 0.95, 7
    )
end

local function QPSC_MM_addContract(symbols, markers, contract)
    if contract.multiObjective == true
        or string.upper(tostring(contract.objectiveType or "")) == "MULTI" then
        for _, objective in ipairs(contract.objectives or {}) do
            local objectiveType = string.upper(tostring(objective.type or ""))
            if objectiveType == "DELIVERY"
                or objectiveType == "KILL"
                or objectiveType == "LOCATION" then
                QPSC_MM_addObjectiveMarker(markers, objective)
            end
        end
        return
    end

    QPSC_MM_addObjectiveMarker(markers, {
        type = contract.objectiveType,
        targetX = contract.targetX,
        targetY = contract.targetY,
        radius = contract.objectiveRadius
    })

    -- No texture or text symbol is created. The static zone plus centre
    -- locator are the accepted-contract map indicators.
end

function QPSC_MapMarkers.markDirty()
    QPSC_MapMarkers.lastSignature = "__DIRTY__"
end

function QPSC_MapMarkers.syncNow()
    local instance = QPSC_MM_getMapInstance()
    if instance == nil then
        QPSC_MapMarkers.wasMapVisible = false
        return false
    end

    local mapJustOpened = not QPSC_MapMarkers.wasMapVisible
    QPSC_MapMarkers.wasMapVisible = true

    local api = QPSC_MM_getMapAPI(instance)
    local symbols = QPSC_MM_getSymbolsAPI(api)
    local markers = QPSC_MM_getMarkersAPI(api)

    if symbols == nil and markers == nil then
        if not QPSC_MapMarkers.warnedUnavailable then
            print("[QPSC] World map marker APIs unavailable; contract marker skipped safely.")
            QPSC_MapMarkers.warnedUnavailable = true
        end
        return false
    end

    local contracts = QPSC_MM_getActiveContracts()
    local signature = QPSC_MM_buildSignature(contracts)
    local mapChanged = QPSC_MapMarkers.lastMapObject ~= instance

    if not mapJustOpened
        and not mapChanged
        and signature == QPSC_MapMarkers.lastSignature then
        return true
    end

    QPSC_MM_clearOwnedSymbols(symbols)
    QPSC_MM_clearOwnedGridMarkers(markers)
    QPSC_MM_registerSymbols()

    for _, contract in ipairs(contracts) do
        QPSC_MM_addContract(symbols, markers, contract)
    end

    QPSC_MapMarkers.lastSignature = signature
    QPSC_MapMarkers.lastMapObject = instance
    QPSC_MapMarkers.warnedUnavailable = false

    print("[QPSC] Synced " .. tostring(#contracts) .. " accepted contract map marker(s).")
    return true
end

local function QPSC_MM_onTick()
    QPSC_MapMarkers.tickCounter = (QPSC_MapMarkers.tickCounter or 0) + 1
    if QPSC_MapMarkers.tickCounter < 20 then return end
    QPSC_MapMarkers.tickCounter = 0
    QPSC_MapMarkers.syncNow()
end

if Events.OnTick then Events.OnTick.Add(QPSC_MM_onTick) end
if Events.OnGameStart then
    Events.OnGameStart.Add(function()
        QPSC_MapMarkers.markDirty()
    end)
end

return QPSC_MapMarkers
