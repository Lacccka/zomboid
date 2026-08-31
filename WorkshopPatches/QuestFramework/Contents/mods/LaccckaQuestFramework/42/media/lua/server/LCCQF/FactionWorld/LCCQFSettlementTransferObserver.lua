-- Server-authoritative observer for vanilla item transfers into faction settlement stock.
-- The client can only announce an intent before the engine transaction. We accept it only
-- when the server sees that exact item ID in that player's inventory and the resolved
-- destination is one of the exact containers in a current settlement stock snapshot.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/World/LCCQFWorldContainerResolver"
require "LCCQF/Content/LCCQFFactionDefinitions"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteStock"
require "LCCQF/FactionWorld/LCCQFFactionSiteOperations"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Resolver = LCCQF.WorldContainerResolver
local Factions = LCCQF.FactionRegistry
local Sites = LCCQF.FactionSiteRegistry
local Stock = LCCQF.FactionSiteStock
local Operations = LCCQF.FactionSiteOperations
local Observer = LCCQF.SettlementTransferServerObserver or {}
local pending = Observer.pending or {}
local dirtySites = Observer.dirtySites or {}
local listeners = Observer.listeners or {}

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SITE:TRANSFER:SERVER] " .. tostring(message))
end

local function nowMs()
    return getTimestampMs and (tonumber(getTimestampMs()) or 0) or 0
end

local function playerKey(player)
    if not player then return nil end
    if player.getOnlineID then
        local ok, value = pcall(function() return player:getOnlineID() end)
        if ok and value ~= nil then return "id:" .. tostring(value) end
    end
    if player.getUsername then
        local ok, value = pcall(function() return player:getUsername() end)
        if ok and value ~= nil then return "user:" .. tostring(value) end
    end
    return nil
end

local function playerUsername(player)
    if not player or not player.getUsername then return "" end
    local ok, value = pcall(function() return player:getUsername() end)
    return ok and value and tostring(value) or ""
end

local function playerOnlineId(player)
    if not player or not player.getOnlineID then return nil end
    local ok, value = pcall(function() return player:getOnlineID() end)
    return ok and value ~= nil and tonumber(value) or nil
end

local function resolvePlayer(entry)
    if not getOnlinePlayers then return nil end
    local players = getOnlinePlayers()
    if not players then return nil end
    for index = 0, players:size() - 1 do
        local player = players:get(index)
        local id = playerOnlineId(player)
        if entry.playerOnlineId ~= nil and id ~= nil and id == entry.playerOnlineId then return player end
        if entry.playerUsername ~= "" and playerUsername(player) == entry.playerUsername then return player end
    end
    return nil
end

local function finiteInteger(value, minimum, maximum)
    local number = tonumber(value)
    if number == nil or number ~= number or number == math.huge or number == -math.huge then return nil end
    number = math.floor(number)
    if minimum ~= nil and number < minimum then return nil end
    if maximum ~= nil and number > maximum then return nil end
    return number
end

local function limitedString(value, maximum)
    if value == nil then return "" end
    value = tostring(value)
    if #value > (maximum or 128) then return nil end
    return value
end

local function readLocator(args)
    if type(args) ~= "table" then return nil end
    local x = finiteInteger(args.x, -10000000, 10000000)
    local y = finiteInteger(args.y, -10000000, 10000000)
    local z = finiteInteger(args.z, -64, 64)
    local collectionIndex = finiteInteger(args.collectionIndex, -1, 4096)
    local objectIndex = finiteInteger(args.objectIndex, -1, 4096)
    local containerIndex = finiteInteger(args.containerIndex, 0, 128)
    local collection = limitedString(args.objectCollection, 16)
    local containerType = limitedString(args.containerType, 128)
    local spriteName = limitedString(args.spriteName, 192)
    if not x or not y or not z or collectionIndex == nil or objectIndex == nil or not containerIndex
        or not collection or not containerType or not spriteName
    then
        return nil
    end
    if collection ~= "objects" and collection ~= "special" and collection ~= "unknown" then return nil end
    return {
        schemaVersion = 1,
        x = x, y = y, z = z,
        objectCollection = collection,
        collectionIndex = collectionIndex,
        objectIndex = objectIndex,
        containerIndex = containerIndex,
        containerType = containerType,
        spriteName = spriteName,
    }
end

local function itemId(item)
    if not item or not item.getID then return nil end
    local ok, value = pcall(function() return item:getID() end)
    value = ok and tonumber(value) or nil
    return value and math.floor(value) or nil
end

local function itemFullType(item)
    if not item or not item.getFullType then return nil end
    local ok, value = pcall(function() return item:getFullType() end)
    return ok and type(value) == "string" and value ~= "" and value or nil
end

local function nestedInventory(item)
    if not item or not item.getInventory then return nil end
    local ok, value = pcall(function() return item:getInventory() end)
    return ok and value or nil
end

local function findOwnedItem(player, wantedId)
    if not player or not player.getInventory then return nil end
    local root
    pcall(function() root = player:getInventory() end)
    if not root then return nil end

    local remaining = math.max(64, math.floor(tonumber(C.FACTION_SITE_TRANSFER_OWNERSHIP_SCAN_MAX_ITEMS) or 4096))
    local seen = {}
    local function visit(container)
        if not container or seen[container] or remaining <= 0 then return nil end
        seen[container] = true
        local items
        pcall(function() items = container:getItems() end)
        local size = items and items:size() or 0
        for index = 0, size - 1 do
            if remaining <= 0 then return nil end
            remaining = remaining - 1
            local item = items:get(index)
            if itemId(item) == wantedId then return item end
            local nested = nestedInventory(item)
            local found = nested and visit(nested) or nil
            if found then return found end
        end
        return nil
    end
    return visit(root)
end

local function findDirectItem(container, wantedId)
    if not container then return nil end
    local items
    pcall(function() items = container:getItems() end)
    local size = items and items:size() or 0
    for index = 0, size - 1 do
        local item = items:get(index)
        if itemId(item) == wantedId then return item end
    end
    return nil
end

local function siteForLocatorKey(locatorKey)
    if locatorKey == "" then return nil end
    for _, site in ipairs(Sites.ListSites()) do
        if site.state == "ACTIVE" or site.state == "DORMANT" then
            for _, row in ipairs(site.stock and site.stock.containers or {}) do
                if tostring(row.locatorKey or "") == locatorKey then return site end
            end
        end
    end
    return nil
end

local function resolveDestination(locator)
    local object, container = Resolver.Resolve(locator)
    if not object or not container then return nil, nil, nil, "destination unavailable" end
    local canonical = Resolver.MakeLocator(object, container)
    if not canonical then return nil, nil, nil, "destination locator unavailable" end
    local key = Resolver.LocatorKey(canonical)
    local site = siteForLocatorKey(key)
    if not site then return nil, nil, nil, "destination is not settlement stock" end
    return site, container, canonical, nil
end

local function playerNearLocator(player, locator)
    if not player or type(locator) ~= "table" then return false end
    local px, py, pz
    pcall(function()
        px = tonumber(player:getX())
        py = tonumber(player:getY())
        pz = tonumber(player:getZ())
    end)
    if not px or not py or not pz then return false end
    local dx = px - (tonumber(locator.x) or 0)
    local dy = py - (tonumber(locator.y) or 0)
    local dz = math.abs(pz - (tonumber(locator.z) or 0))
    local range = math.max(1, tonumber(C.FACTION_SITE_TRANSFER_MAX_DISTANCE) or 6)
    return dz <= 1 and (dx * dx + dy * dy) <= (range * range)
end

local function pendingCountForPlayer(key)
    local count = 0
    for _, entry in pairs(pending) do
        if entry.playerKey == key then count = count + 1 end
    end
    return count
end

local function eventPayload(entry, site, item)
    return {
        siteId = site.siteId,
        factionId = site.factionId,
        itemId = entry.itemId,
        fullType = itemFullType(item) or entry.fullType,
        locatorKey = entry.locatorKey,
        playerUsername = entry.playerUsername,
        playerOnlineId = entry.playerOnlineId,
        confirmedMs = nowMs(),
    }
end

local function queueRefresh(site, event)
    local debounce = math.max(50, math.floor(tonumber(C.FACTION_SITE_TRANSFER_REFRESH_DEBOUNCE_MS) or 500))
    local row = dirtySites[site.siteId]
    if type(row) ~= "table" then
        row = { dueMs = nowMs() + debounce, events = {} }
        dirtySites[site.siteId] = row
    end
    row.dueMs = math.min(tonumber(row.dueMs) or (nowMs() + debounce), nowMs() + debounce)
    row.events[#row.events + 1] = event
end

local function emit(event)
    for _, listener in ipairs(listeners) do
        local ok, errorText = pcall(listener, event)
        if not ok then log("listener failed error=" .. tostring(errorText)) end
    end
end

local function refreshDirtySite(siteId, row)
    local site = Sites.GetSite(siteId)
    local refreshOk, refreshResult = false, "site unavailable"
    if site then
        refreshOk, refreshResult = Stock.Refresh(site)
        if refreshOk then
            local definition = Factions.Get(site.factionId)
            if definition then Operations.UpdateSite(site, definition) end
        end
    end

    for _, event in ipairs(row.events or {}) do
        event.stockRefreshOk = refreshOk == true
        event.stockRefreshDetail = tostring(refreshResult)
        event.stockRevision = site and site.stock and tonumber(site.stock.revision) or nil
        emit(event)
    end

    log("siteId=" .. tostring(siteId)
        .. " confirmedTransfers=" .. tostring(#(row.events or {}))
        .. " stockRefresh=" .. tostring(refreshOk)
        .. " detail=" .. tostring(refreshResult))
    dirtySites[siteId] = nil
end

local function registerIntent(player, args)
    local key = playerKey(player)
    if not key then return end
    local itemIdValue = finiteInteger(args and args.itemId, 0, 2147483647)
    local locator = readLocator(args)
    if itemIdValue == nil or not locator then return end

    local maxPending = math.max(1, math.floor(tonumber(C.FACTION_SITE_TRANSFER_MAX_PENDING_PER_PLAYER) or 64))
    if pendingCountForPlayer(key) >= maxPending then
        log("intent rejected player=" .. playerUsername(player) .. " reason=pending-limit")
        return
    end

    local site, _, canonical, errorText = resolveDestination(locator)
    if not site then
        log("intent rejected player=" .. playerUsername(player) .. " reason=" .. tostring(errorText))
        return
    end
    if not playerNearLocator(player, canonical) then
        log("intent rejected player=" .. playerUsername(player) .. " siteId=" .. tostring(site.siteId)
            .. " reason=too-far")
        return
    end

    local owned = findOwnedItem(player, itemIdValue)
    if not owned then
        log("intent rejected player=" .. playerUsername(player) .. " siteId=" .. tostring(site.siteId)
            .. " itemId=" .. tostring(itemIdValue) .. " reason=not-owned-before-transfer")
        return
    end

    local locatorKey = Resolver.LocatorKey(canonical)
    local pendingKey = key .. "|" .. tostring(itemIdValue) .. "|" .. locatorKey
    local now = nowMs()
    pending[pendingKey] = {
        pendingKey = pendingKey,
        playerKey = key,
        playerOnlineId = playerOnlineId(player),
        playerUsername = playerUsername(player),
        siteId = site.siteId,
        factionId = site.factionId,
        itemId = itemIdValue,
        fullType = itemFullType(owned),
        locator = canonical,
        locatorKey = locatorKey,
        createdMs = now,
        expiresMs = now + math.max(1000, math.floor(tonumber(C.FACTION_SITE_TRANSFER_INTENT_TTL_MS) or 15000)),
        nextPollMs = now,
    }
end

local function reconcilePending(entry, now)
    if now >= entry.expiresMs then return true, "expired" end
    if now < (tonumber(entry.nextPollMs) or 0) then return false end
    entry.nextPollMs = now + math.max(25, math.floor(tonumber(C.FACTION_SITE_TRANSFER_POLL_INTERVAL_MS) or 100))

    local player = resolvePlayer(entry)
    if not player then return false end
    local site, container, canonical = resolveDestination(entry.locator)
    if not site or tostring(site.siteId) ~= tostring(entry.siteId) then return true, "destination-changed" end
    if Resolver.LocatorKey(canonical) ~= entry.locatorKey then return true, "destination-changed" end

    local delivered = findDirectItem(container, entry.itemId)
    if not delivered then return false end
    if findOwnedItem(player, entry.itemId) then return false end

    queueRefresh(site, eventPayload(entry, site, delivered))
    log("confirmed player=" .. entry.playerUsername
        .. " siteId=" .. tostring(site.siteId)
        .. " itemId=" .. tostring(entry.itemId)
        .. " fullType=" .. tostring(itemFullType(delivered) or entry.fullType)
        .. " locator=" .. tostring(entry.locatorKey))
    return true, "confirmed"
end

local function onTick()
    local now = nowMs()
    for key, entry in pairs(pending) do
        local finished, reason = reconcilePending(entry, now)
        if finished then
            pending[key] = nil
            if reason ~= "confirmed" then
                log("intent closed player=" .. tostring(entry.playerUsername)
                    .. " itemId=" .. tostring(entry.itemId)
                    .. " reason=" .. tostring(reason))
            end
        end
    end
    for siteId, row in pairs(dirtySites) do
        if now >= (tonumber(row.dueMs) or 0) then refreshDirtySite(siteId, row) end
    end
end

local function onClientCommand(module, command, player, args)
    if module ~= C.MODULE or command ~= C.COMMAND.REPORT_SETTLEMENT_TRANSFER_INTENT then return end
    registerIntent(player, args)
end

function Observer.AddListener(listener)
    if type(listener) ~= "function" then return false end
    for _, current in ipairs(listeners) do if current == listener then return true end end
    listeners[#listeners + 1] = listener
    Observer.listeners = listeners
    return true
end

function Observer.RemoveListener(listener)
    for index = #listeners, 1, -1 do
        if listeners[index] == listener then
            table.remove(listeners, index)
            Observer.listeners = listeners
            return true
        end
    end
    return false
end

function Observer.GetPendingCount()
    local count = 0
    for _ in pairs(pending) do count = count + 1 end
    return count
end

if isServer and isServer() then
    if Events.OnClientCommand then Events.OnClientCommand.Add(onClientCommand) end
    if Events.OnTick then Events.OnTick.Add(onTick) end
end

Observer.pending = pending
Observer.dirtySites = dirtySites
Observer.listeners = listeners
LCCQF.SettlementTransferServerObserver = Observer
return Observer
