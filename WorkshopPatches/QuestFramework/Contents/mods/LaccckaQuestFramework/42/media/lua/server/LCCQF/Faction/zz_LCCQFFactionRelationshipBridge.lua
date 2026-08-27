require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFNPCRegistry"
require "LCCQF/Quest/LCCQFQuestRegistry"
require "LCCQF/Quest/LCCQFQuestService"
require "LCCQF/Persistence/LCCQFCharacterFactionKnowledge"
require "LCCQF/Persistence/LCCQFCharacterFactionRelationships"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local NPCRegistry = LCCQF.NPCRegistry
local QuestRegistry = LCCQF.QuestRegistry
local QuestService = LCCQF.QuestService
local CharacterFactionKnowledge = LCCQF.CharacterFactionKnowledge
local CharacterFactionRelationships = LCCQF.CharacterFactionRelationships
local observerInstalled = false

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:RELATIONSHIP:BRIDGE] " .. tostring(message))
end

local function rewardForQuest(questId)
    local definition = type(questId) == "string" and QuestRegistry.Get(questId) or nil
    if not definition or type(definition.factionReward) ~= "table" then return nil, nil end

    local reward = definition.factionReward
    local factionId = type(reward.factionId) == "string" and reward.factionId or nil
    if not factionId then
        local giver = type(definition.giverNpcId) == "string" and NPCRegistry.Get(definition.giverNpcId) or nil
        factionId = giver and giver.factionId or nil
    end
    if type(factionId) ~= "string" then return nil, nil end
    return factionId, reward
end

local function applyQuestCompletionReward(player, questId, source)
    local factionId, reward = rewardForQuest(questId)
    if not factionId then return false, "no faction reward" end

    local changed = false
    if tonumber(reward.reputation) ~= nil then
        local ok, reputationChangedOrErr = CharacterFactionRelationships.AdjustReputation(
            player,
            factionId,
            reward.reputation,
            source or "quest-faction-completed",
            "quest-faction-completed:" .. tostring(questId)
        )
        if not ok then return false, reputationChangedOrErr end
        changed = changed or reputationChangedOrErr == true
    end

    if reward.member == true or reward.member == false then
        local ok, membershipChangedOrErr = CharacterFactionRelationships.SetMembership(
            player,
            factionId,
            reward.member,
            reward.rankId,
            source or "quest-faction-completed"
        )
        if not ok then return false, membershipChangedOrErr end
        changed = changed or membershipChangedOrErr == true
    end

    return true, changed
end

local function reconcileQuestHistory(player)
    if not player or type(QuestService.ExportViews) ~= "function" then return 0 end
    local applied = 0
    for _, quest in ipairs(QuestService.ExportViews(player)) do
        if type(quest) == "table" and quest.state == "completed" and type(quest.questId) == "string" then
            local ok, changed = applyQuestCompletionReward(
                player,
                quest.questId,
                "quest-faction-history-reconcile"
            )
            if ok and changed then applied = applied + 1 end
        end
    end
    return applied
end

local function onQuestEvent(kind, player, payload)
    if kind ~= "event" or not player or type(payload) ~= "table" then return end
    if payload.messageKey ~= "IGUI_LCCQF_QuestEvent_Completed" or payload.state ~= "completed" then return end

    local ok, changed = applyQuestCompletionReward(
        player,
        payload.questId,
        "quest-faction-completed-live"
    )
    if ok then
        log("quest faction reward questId=" .. tostring(payload.questId)
            .. " changed=" .. tostring(changed))
    end
end

local function installQuestObserver()
    if observerInstalled then return true end
    if QuestService.__LCCQFFactionRelationshipEventObserver then
        observerInstalled = true
        return true
    end
    if type(QuestService.AddEventListener) ~= "function" then return false end

    QuestService.AddEventListener(onQuestEvent)
    QuestService.__LCCQFFactionRelationshipEventObserver = true
    observerInstalled = true
    log("quest completion listener installed")
    return true
end

local function onRelationshipEvent(kind, player, payload)
    if kind ~= "upsert" or not player or type(payload) ~= "table" then return end
    local factionId = payload.factionId
    if type(factionId) ~= "string" or not CharacterFactionKnowledge.IsKnown(player, factionId) then return end

    local view = CharacterFactionKnowledge.GetView(player, factionId)
    if not view then return end
    view.factionKnowledgeRevision = CharacterFactionKnowledge.GetRevision(player)
    view.factionRelationshipRevision = payload.factionRelationshipRevision
    sendServerCommand(player, C.MODULE, C.COMMAND.KNOWN_FACTION_UPSERT, view)
end

local function onClientCommand(module, command, player, args)
    if module ~= C.MODULE or command ~= C.COMMAND.REQUEST_FACTIONS then return end
    local applied = reconcileQuestHistory(player)
    if applied > 0 then
        log("history reconciled player=" .. tostring(player and player:getUsername())
            .. " rewards=" .. tostring(applied))
    end
end

local function onServerStarted()
    local ok, err = CharacterFactionRelationships.Initialize()
    CharacterFactionRelationships.SetEventSink(onRelationshipEvent)
    local observer = installQuestObserver()
    log("loaded version=" .. tostring(C.VERSION)
        .. " persistence=" .. tostring(ok and "ready" or err or "unavailable")
        .. " questObserver=" .. tostring(observer)
        .. " historyReconcile=true")
end

installQuestObserver()

if isServer and isServer() then
    Events.OnClientCommand.Add(onClientCommand)
    Events.OnServerStarted.Add(onServerStarted)
end

return true
