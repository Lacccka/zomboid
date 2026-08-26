require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFNPCRegistry"
require "LCCQF/Persistence/LCCQFCharacterKnowledge"
require "LCCQF/Dialogue/LCCQFDialogueSession"
require "LCCQF/Quest/LCCQFQuestService"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local NPCRegistry = LCCQF.NPCRegistry
local CharacterKnowledge = LCCQF.CharacterKnowledge
local DialogueSession = LCCQF.DialogueSession
local QuestService = LCCQF.QuestService
local installed = false

local function log(message)
    print(C.LOG_PREFIX .. "[KNOWLEDGE:BRIDGE] " .. tostring(message))
end

local function reconcileQuestKnowledge(player)
    if not player or not QuestService or type(QuestService.ExportViews) ~= "function" then return 0 end

    local changed = 0
    for _, quest in ipairs(QuestService.ExportViews(player)) do
        local npcId = type(quest) == "table" and quest.giverNpcId or nil
        local definition = npcId and NPCRegistry.Get(npcId) or nil
        if definition then
            local ok, discovered = CharacterKnowledge.DiscoverNPC(
                player,
                npcId,
                "quest-history-migration"
            )
            if ok and discovered then changed = changed + 1 end

            local byQuest = type(definition.questKnowledgeFacts) == "table"
                and definition.questKnowledgeFacts[quest.questId]
                or nil
            local facts = type(byQuest) == "table" and byQuest[quest.state] or nil
            for _, factId in ipairs(type(facts) == "table" and facts or {}) do
                local factOk, unlocked = CharacterKnowledge.UnlockFact(
                    player,
                    npcId,
                    factId,
                    "quest-state:" .. tostring(quest.questId) .. ":" .. tostring(quest.state)
                )
                if factOk and unlocked then changed = changed + 1 end
            end
        end
    end
    return changed
end

local function sendSnapshot(player)
    if not player then return end
    local reconciled = reconcileQuestKnowledge(player)
    local people, revision = CharacterKnowledge.ExportViews(player)
    sendServerCommand(player, C.MODULE, C.COMMAND.KNOWLEDGE, {
        people = people,
        revision = revision,
    })
    log("snapshot sent player=" .. tostring(player:getUsername())
        .. " people=" .. tostring(#people)
        .. " revision=" .. tostring(revision)
        .. " reconciled=" .. tostring(reconciled))
end

local function onKnowledgeEvent(kind, player, payload)
    if kind ~= "upsert" or not player or type(payload) ~= "table" then return end
    local _, revision = CharacterKnowledge.ExportViews(player)
    payload.knowledgeRevision = revision
    sendServerCommand(player, C.MODULE, C.COMMAND.KNOWN_PERSON_UPSERT, payload)
end

local function installDialogueDiscovery()
    if installed then return true end
    if type(DialogueSession) ~= "table" or type(DialogueSession.Open) ~= "function" then
        return false
    end
    if DialogueSession.__LCCQFCharacterKnowledgeWrapped then
        installed = true
        return true
    end

    local originalOpen = DialogueSession.Open
    DialogueSession.Open = function(player, handle, definition, hooks)
        local view, err = originalOpen(player, handle, definition, hooks)
        if view and definition and type(definition.npcId) == "string" then
            local ok, discoveryErr = CharacterKnowledge.DiscoverNPC(
                player,
                definition.npcId,
                "validated-dialogue"
            )
            if not ok then
                log("discovery failed npcId=" .. tostring(definition.npcId)
                    .. " error=" .. tostring(discoveryErr))
            end
        end
        return view, err
    end
    DialogueSession.__LCCQFCharacterKnowledgeWrapped = true
    installed = true
    log("validated-dialogue discovery installed")
    return true
end

local function onClientCommand(module, command, player, args)
    if module ~= C.MODULE or command ~= C.COMMAND.REQUEST_KNOWLEDGE then return end
    sendSnapshot(player)
end

local function onServerStarted()
    local ok, err = CharacterKnowledge.Initialize()
    CharacterKnowledge.SetEventSink(onKnowledgeEvent)
    local wrapped = installDialogueDiscovery()
    log("loaded version=" .. tostring(C.VERSION)
        .. " persistence=" .. tostring(ok and "ready" or err or "unavailable")
        .. " dialogueDiscovery=" .. tostring(wrapped)
        .. " questHistoryReconcile=true")
end

if isServer and isServer() then
    Events.OnClientCommand.Add(onClientCommand)
    Events.OnServerStarted.Add(onServerStarted)
end

return true
