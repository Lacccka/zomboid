-- Late extension that exposes persistent settlement-supply offers through the existing
-- server-authoritative QuestService condition/action surface. It does not create a second
-- quest runtime; all accepted instances still go through QuestService.Accept.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/Quest/LCCQFQuestService"
require "LCCQF/Quest/LCCQFQuestRegistry"
require "LCCQF/FactionWorld/zz_LCCQFFactionSupplyQuestBridge"

LCCQF = LCCQF or {}

local QuestService = LCCQF.QuestService
local QuestRegistry = LCCQF.QuestRegistry
local SupplyBridge = LCCQF.FactionSupplyQuestBridge

if QuestService._factionSupplyQuestExtensionInstalled == true then return QuestService end

local originalEvaluateCondition = QuestService.EvaluateCondition
local originalExecuteAction = QuestService.ExecuteAction

local function contextNpcId(context)
    context = type(context) == "table" and context or {}
    return context.dialogueNpcId or context.npcId or context.giverNpcId
end

local function activeSupplyQuestForNpc(player, npcId)
    if type(npcId) ~= "string" or npcId == "" then return nil end
    for _, view in ipairs(QuestService.ExportViews(player)) do
        if view.state == "active" then
            local definition = QuestRegistry.Get(view.questId)
            if definition and definition.dynamicKind == "settlement_supply" then
                local offer = SupplyBridge.GetOfferByQuestId(view.questId)
                if offer and SupplyBridge.CanNpcHandleOffer(offer, npcId) then
                    return view, definition, offer
                end
            end
        end
    end
    return nil
end

local function refreshOffers()
    local ok = SupplyBridge.RunOnce()
    return ok == true
end

local function canonicalAcceptContext(context, offer, dialogueNpcId)
    local out = {}
    for key, value in pairs(type(context) == "table" and context or {}) do out[key] = value end
    out.dialogueNpcId = dialogueNpcId
    out.npcId = dialogueNpcId
    out.giverNpcId = offer.giverNpcId
    out.giverFactionId = offer.factionId
    out.factionId = out.factionId or offer.factionId
    return out
end

function QuestService.EvaluateCondition(player, condition, context)
    if type(condition) == "table" and condition.kind == "factionSupplyQuestAvailable" then
        refreshOffers()
        local npcId = contextNpcId(context)
        if type(npcId) ~= "string" or activeSupplyQuestForNpc(player, npcId) then return false end
        local offer = SupplyBridge.GetOpenOfferForNpc(npcId)
        return offer ~= nil and QuestService.GetQuestState(player, offer.questId) == "available"
    end

    if type(condition) == "table" and condition.kind == "factionSupplyQuestActive" then
        refreshOffers()
        local npcId = contextNpcId(context)
        return activeSupplyQuestForNpc(player, npcId) ~= nil
    end

    if type(condition) == "table" and condition.kind == "factionSupplyOfferOpen" then
        if type(condition.questId) ~= "string" or condition.questId == "" then return false end
        refreshOffers()
        return SupplyBridge.IsOfferOpen(condition.questId)
    end

    return originalEvaluateCondition(player, condition, context)
end

function QuestService.ExecuteAction(player, action, context)
    if type(action) == "table" and action.kind == "factionSupplyQuestAccept" then
        refreshOffers()
        local npcId = contextNpcId(context)
        if type(npcId) ~= "string" or npcId == "" then return false, "supply quest giver unavailable" end
        if activeSupplyQuestForNpc(player, npcId) then return false, "supply quest already active" end

        local offer = SupplyBridge.GetOpenOfferForNpc(npcId)
        if not offer or not SupplyBridge.IsOfferOpen(offer.questId) then
            return false, "supply quest offer is no longer open"
        end
        if not SupplyBridge.CanNpcHandleOffer(offer, npcId) then
            return false, "supply quest presenter is no longer valid"
        end
        if QuestService.GetQuestState(player, offer.questId) ~= "available" then
            return false, "supply quest is not available"
        end

        local acceptContext = canonicalAcceptContext(context, offer, npcId)
        local instance, errorText = QuestService.Accept(player, offer.questId, acceptContext)
        return instance ~= nil, errorText
    end

    return originalExecuteAction(player, action, context)
end

QuestService._factionSupplyQuestExtensionInstalled = true
return QuestService
