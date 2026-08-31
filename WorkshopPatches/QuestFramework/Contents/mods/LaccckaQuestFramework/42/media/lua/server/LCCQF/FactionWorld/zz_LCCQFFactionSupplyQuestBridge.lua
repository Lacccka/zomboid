-- Persistent bridge from settlement supply-need episodes into the existing QuestRegistry.
-- This creates quest definitions only; character quest instances remain owned by QuestService.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFFactionDefinitions"
require "LCCQF/Quest/LCCQFQuestRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSitePopulation"
require "LCCQF/FactionWorld/LCCQFFactionSiteOperations"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local QuestRegistry = LCCQF.QuestRegistry
local Sites = LCCQF.FactionSiteRegistry
local Population = LCCQF.FactionSitePopulation
local Operations = LCCQF.FactionSiteOperations
local Bridge = LCCQF.FactionSupplyQuestBridge or {}

local TITLE_KEY = "IGUI_LCCQF_Quest_SettlementSupply_Title"
local DESCRIPTION_KEY = "IGUI_LCCQF_Quest_SettlementSupply_Description"
local OBJECTIVE_KEY = "IGUI_LCCQF_Quest_SettlementSupply_Objective"

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:SUPPLY:QUEST] " .. tostring(message))
end

local function worldHours()
    if not getGameTime then return 0 end
    local gameTime = getGameTime()
    return gameTime and gameTime.getWorldAgeHours and (tonumber(gameTime:getWorldAgeHours()) or 0) or 0
end

local function rollingHash(text)
    local hash = 17
    text = tostring(text or "")
    for index = 1, #text do
        hash = ((hash * 131) + string.byte(text, index)) % 2147483647
    end
    return math.floor(hash)
end

local function questIdFor(siteId, supplyId, openEpoch)
    local identity = tostring(siteId) .. "|" .. tostring(supplyId)
    return "lccqf_supply_" .. tostring(rollingHash(identity)) .. "_" .. tostring(openEpoch)
end

local function offerKeyFor(signalId, openEpoch)
    return tostring(signalId) .. "@" .. tostring(openEpoch)
end

local function ensureOffers(site)
    local operations = Operations.Ensure(site)
    if not operations then return nil end
    if type(operations.questOffers) ~= "table" then operations.questOffers = {} end
    return operations.questOffers
end

local function currentMember(site, npcId)
    if type(site) ~= "table" or type(npcId) ~= "string" or npcId == "" then return nil end
    for _, member in ipairs(Population.ListMembers(site)) do
        if tostring(member.npcId or "") == npcId then return member end
    end
    return nil
end

local function materializedMember(site, npcId)
    local member = currentMember(site, npcId)
    return member and member.state == "MATERIALIZED" and member or nil
end

local function livingGiver(site)
    local fallback = nil
    for _, member in ipairs(Population.ListMembers(site)) do
        fallback = fallback or member
        if member.roleId == "leader" then return member end
    end
    return fallback
end

local function offerMatchesDefinition(offer, definition)
    return type(definition) == "table"
        and tostring(definition.questId or "") == tostring(offer.questId or "")
        and tostring(definition.giverNpcId or "") == tostring(offer.giverNpcId or "")
        and tostring(definition.dynamicOfferKey or "") == tostring(offer.offerKey or "")
end

function Bridge.RegisterDefinition(offer)
    if type(offer) ~= "table" or type(offer.questId) ~= "string" then
        return false, "invalid supply offer"
    end

    local existing = QuestRegistry.Get(offer.questId)
    if existing then
        if offerMatchesDefinition(offer, existing) then return true, "already registered" end
        return false, "dynamic questId collision"
    end

    local definition = {
        questId = offer.questId,
        titleKey = offer.titleKey or TITLE_KEY,
        descriptionKey = offer.descriptionKey or DESCRIPTION_KEY,
        giverNpcId = offer.giverNpcId,
        repeatable = false,
        generated = true,
        dynamicKind = "settlement_supply",
        dynamicOfferKey = offer.offerKey,
        factionId = offer.factionId,
        factionSiteId = offer.siteId,
        objectives = {
            {
                id = "settlement_supply",
                type = "SettlementSupply",
                titleKey = offer.objectiveTitleKey or OBJECTIVE_KEY,
                siteId = offer.siteId,
                supplyId = offer.supplyId,
                category = offer.category,
                signalId = offer.signalId,
                openEpoch = offer.openEpoch,
                minimumContribution = 1,
                available = offer.available,
                target = offer.target,
            },
        },
    }
    local ok, errorText = QuestRegistry.Register(definition)
    if not ok then return false, errorText end
    return true, "registered"
end

local function normalizeOffer(site, key, offer)
    if type(offer) ~= "table" then return false end
    offer.schemaVersion = 1
    offer.offerKey = tostring(offer.offerKey or key)
    offer.kind = "settlement_supply"
    offer.siteId = tostring(offer.siteId or site.siteId)
    offer.factionId = tostring(offer.factionId or site.factionId)
    offer.titleKey = tostring(offer.titleKey or TITLE_KEY)
    offer.descriptionKey = tostring(offer.descriptionKey or DESCRIPTION_KEY)
    offer.objectiveTitleKey = tostring(offer.objectiveTitleKey or OBJECTIVE_KEY)
    offer.status = offer.status == "RESOLVED" and "RESOLVED" or "OPEN"
    return type(offer.questId) == "string"
        and offer.questId ~= ""
        and type(offer.giverNpcId) == "string"
        and offer.giverNpcId ~= ""
        and type(offer.signalId) == "string"
        and offer.signalId ~= ""
        and type(offer.supplyId) == "string"
        and offer.supplyId ~= ""
        and type(offer.category) == "string"
        and offer.category ~= ""
        and tonumber(offer.openEpoch) ~= nil
end

local function registerHistoricalDefinitions(site, offers)
    local registered = 0
    for key, offer in pairs(offers) do
        if normalizeOffer(site, key, offer) then
            local ok, result = Bridge.RegisterDefinition(offer)
            if ok then
                registered = registered + 1
            else
                log("definition rejected siteId=" .. tostring(site.siteId)
                    .. " questId=" .. tostring(offer.questId)
                    .. " error=" .. tostring(result))
            end
        end
    end
    return registered
end

local function createOffer(site, signal)
    local epoch = math.max(1, math.floor(tonumber(signal.openEpoch) or 1))
    local giver = livingGiver(site)
    if not giver or type(giver.npcId) ~= "string" then return nil, "living quest giver unavailable" end

    local offerKey = offerKeyFor(signal.signalId, epoch)
    return {
        schemaVersion = 1,
        offerKey = offerKey,
        kind = "settlement_supply",
        questId = questIdFor(site.siteId, signal.supplyId, epoch),
        siteId = site.siteId,
        factionId = site.factionId,
        signalId = signal.signalId,
        supplyId = signal.supplyId,
        category = signal.category,
        openEpoch = epoch,
        giverNpcId = giver.npcId,
        titleKey = TITLE_KEY,
        descriptionKey = DESCRIPTION_KEY,
        objectiveTitleKey = OBJECTIVE_KEY,
        available = math.max(0, math.floor(tonumber(signal.available) or 0)),
        target = math.max(0, math.floor(tonumber(signal.target) or 0)),
        status = signal.status == "OPEN" and "OPEN" or "RESOLVED",
        createdWorldHours = worldHours(),
    }
end

local function closeOlderOffers(offers, signal, nowHours)
    local changed = false
    local currentEpoch = math.max(0, math.floor(tonumber(signal.openEpoch) or 0))
    for _, offer in pairs(offers) do
        if type(offer) == "table"
            and tostring(offer.signalId or "") == tostring(signal.signalId or "")
            and offer.status == "OPEN"
        then
            local offerEpoch = math.max(0, math.floor(tonumber(offer.openEpoch) or 0))
            local closed = offerEpoch < currentEpoch
                or (offerEpoch == currentEpoch and signal.status == "RESOLVED")
            if closed then
                offer.status = "RESOLVED"
                offer.resolvedWorldHours = nowHours
                changed = true
            end
        end
    end
    return changed
end

local function processSite(site)
    local offers = ensureOffers(site)
    if not offers then return false, 0, "operations unavailable" end
    local registered = registerHistoricalDefinitions(site, offers)
    local changed = false
    local nowHours = worldHours()
    local signals = site.operations and site.operations.signals or {}

    for _, signal in pairs(type(signals) == "table" and signals or {}) do
        if type(signal) == "table" and signal.kind == "supply" and tonumber(signal.openEpoch) then
            if closeOlderOffers(offers, signal, nowHours) then changed = true end

            local epoch = math.max(1, math.floor(tonumber(signal.openEpoch) or 1))
            local offerKey = offerKeyFor(signal.signalId, epoch)
            local offer = offers[offerKey]
            if signal.status == "OPEN" and type(offer) ~= "table" then
                local created, errorText = createOffer(site, signal)
                if created then
                    offers[offerKey] = created
                    offer = created
                    changed = true
                    log("offer created siteId=" .. tostring(site.siteId)
                        .. " questId=" .. tostring(created.questId)
                        .. " supplyId=" .. tostring(created.supplyId)
                        .. " openEpoch=" .. tostring(created.openEpoch)
                        .. " giverNpcId=" .. tostring(created.giverNpcId))
                else
                    log("offer deferred siteId=" .. tostring(site.siteId)
                        .. " signalId=" .. tostring(signal.signalId)
                        .. " reason=" .. tostring(errorText))
                end
            end

            if type(offer) == "table" then
                if offer.status ~= signal.status then
                    offer.status = signal.status == "OPEN" and "OPEN" or "RESOLVED"
                    if offer.status == "RESOLVED" then offer.resolvedWorldHours = nowHours end
                    changed = true
                end
                local ok, result = Bridge.RegisterDefinition(offer)
                if ok then registered = registered + 1 else
                    log("definition rejected siteId=" .. tostring(site.siteId)
                        .. " questId=" .. tostring(offer.questId)
                        .. " error=" .. tostring(result))
                end
            end
        end
    end

    if changed then Sites.MarkDirty(site.siteId, "settlement supply quest offers updated") end
    return changed, registered
end

function Bridge.RunOnce()
    local changedSites, registered = 0, 0
    for _, site in ipairs(Sites.ListSites()) do
        if site.state == "VALIDATING" or site.state == "ACTIVE" or site.state == "DORMANT" then
            local changed, count = processSite(site)
            if changed then changedSites = changedSites + 1 end
            registered = registered + (tonumber(count) or 0)
        end
    end
    return true, changedSites, registered
end

function Bridge.GetOfferByQuestId(questId)
    if type(questId) ~= "string" or questId == "" then return nil end
    for _, site in ipairs(Sites.ListSites()) do
        local offers = site.operations and site.operations.questOffers or {}
        for _, offer in pairs(type(offers) == "table" and offers or {}) do
            if type(offer) == "table" and offer.questId == questId then return offer end
        end
    end
    return nil
end

-- Historical giver identity remains immutable, but a currently materialized resident of
-- the same site may represent an open offer when the historical giver is not materialized.
-- This is a server-only delegation rule; it never changes quest identity or persistence.
function Bridge.CanNpcHandleOffer(offer, npcId)
    if type(offer) ~= "table" or type(npcId) ~= "string" or npcId == "" then return false end
    local site = Sites.GetSite(offer.siteId)
    if not site then return false end

    local presenter = materializedMember(site, npcId)
    if not presenter then return false end
    if tostring(offer.giverNpcId or "") == npcId then return true end

    local historicalGiver = materializedMember(site, tostring(offer.giverNpcId or ""))
    return historicalGiver == nil
end

function Bridge.GetOpenOfferForNpc(npcId)
    if type(npcId) ~= "string" or npcId == "" then return nil end
    local matches = {}
    for _, site in ipairs(Sites.ListSites()) do
        local offers = site.operations and site.operations.questOffers or {}
        for _, offer in pairs(type(offers) == "table" and offers or {}) do
            if type(offer) == "table"
                and offer.status == "OPEN"
                and Bridge.CanNpcHandleOffer(offer, npcId)
            then
                matches[#matches + 1] = offer
            end
        end
    end
    table.sort(matches, function(a, b)
        if tostring(a.siteId) ~= tostring(b.siteId) then return tostring(a.siteId) < tostring(b.siteId) end
        if tostring(a.supplyId) ~= tostring(b.supplyId) then return tostring(a.supplyId) < tostring(b.supplyId) end
        return (tonumber(a.openEpoch) or 0) < (tonumber(b.openEpoch) or 0)
    end)
    return matches[1]
end

function Bridge.IsOfferOpen(questId)
    local offer = Bridge.GetOfferByQuestId(questId)
    return offer ~= nil and offer.status == "OPEN"
end

local function onServerStarted()
    local ok, changed, registered = Bridge.RunOnce()
    log("initial pass ok=" .. tostring(ok)
        .. " changedSites=" .. tostring(changed)
        .. " definitions=" .. tostring(registered))
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(onServerStarted) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(Bridge.RunOnce) end
end

LCCQF.FactionSupplyQuestBridge = Bridge
return Bridge
