require "LCCQF/LCCQFConstants"
require "LCCQF/Persistence/LCCQFCharacterKnowledge"
require "LCCQF/Dialogue/LCCQFDialogueSession"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local CharacterKnowledge = LCCQF.CharacterKnowledge
local DialogueSession = LCCQF.DialogueSession
local installed = false

local function log(message)
    print(C.LOG_PREFIX .. "[KNOWLEDGE:BRIDGE] " .. tostring(message))
end

local function sendSnapshot(player)
    if not player then return end
    local people, revision = CharacterKnowledge.ExportViews(player)
    sendServerCommand(player, C.MODULE, C.COMMAND.KNOWLEDGE, {
        people = people,
        revision = revision,
    })
    log("snapshot sent player=" .. tostring(player:getUsername())
        .. " people=" .. tostring(#people)
        .. " revision=" .. tostring(revision))
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
        .. " dialogueDiscovery=" .. tostring(wrapped))
end

if isServer and isServer() then
    Events.OnClientCommand.Add(onClientCommand)
    Events.OnServerStarted.Add(onServerStarted)
end

return true
