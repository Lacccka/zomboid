require "LCCQF/LCCQFConstants"
require "LCCQF/Content/LCCQFDialogueContent"

local C = LCCQF.Constants
local DialogueContent = LCCQF.DialogueContent
local DialogueSession = LCCQF.DialogueSession or {}
local sessions = {}

local function getPlayerKey(player)
    if not player then return nil end
    if player.getOnlineID then return tostring(player:getOnlineID()) end
    if player.getUsername then return tostring(player:getUsername()) end
    return nil
end

local function getNode(session)
    local dialogue = DialogueContent.Get(session.dialogueId)
    if not dialogue or not dialogue.nodes then return nil end
    return dialogue.nodes[session.nodeId]
end

local function makeView(session)
    local node = getNode(session)
    if not node then return nil end

    local choices = {}
    for i, choice in ipairs(node.choices or {}) do
        if i > C.MAX_DIALOGUE_CHOICES then break end
        choices[i] = {
            choiceId = choice.id,
            textKey = choice.textKey,
        }
    end

    return {
        sessionId = session.id,
        npcId = session.npcId,
        runtimeId = session.runtimeId,
        npcNameKey = session.npcNameKey,
        nodeId = session.nodeId,
        textKey = node.textKey,
        choices = choices,
    }
end

function DialogueSession.Open(player, handle, definition)
    local playerKey = getPlayerKey(player)
    local dialogue = definition and DialogueContent.Get(definition.dialogueId) or nil
    if not playerKey or not handle or not dialogue or not dialogue.nodes or not dialogue.nodes[dialogue.start] then
        return nil, "dialogue content unavailable"
    end

    local now = getTimestampMs()
    local session = {
        id = tostring(getRandomUUID()),
        playerKey = playerKey,
        npcId = definition.npcId,
        runtimeId = tostring(handle.runtimeId),
        npcNameKey = definition.displayNameKey,
        dialogueId = definition.dialogueId,
        nodeId = dialogue.start,
        openedMs = now,
        touchedMs = now,
    }
    sessions[playerKey] = session
    return makeView(session)
end

function DialogueSession.Get(player, sessionId)
    local playerKey = getPlayerKey(player)
    local session = playerKey and sessions[playerKey] or nil
    if not session or tostring(session.id) ~= tostring(sessionId) then return nil end
    return session
end

function DialogueSession.Choose(player, sessionId, choiceId)
    local session = DialogueSession.Get(player, sessionId)
    if not session then return nil, "unknown session" end

    local node = getNode(session)
    if not node then
        sessions[session.playerKey] = nil
        return nil, "invalid dialogue node"
    end

    local selected = nil
    for _, choice in ipairs(node.choices or {}) do
        if tostring(choice.id) == tostring(choiceId) then
            selected = choice
            break
        end
    end
    if not selected then return nil, "choice not allowed for current node" end

    if selected.close then
        sessions[session.playerKey] = nil
        return { closed = true, sessionId = session.id }
    end

    local dialogue = DialogueContent.Get(session.dialogueId)
    if not selected.next or not dialogue.nodes[selected.next] then
        sessions[session.playerKey] = nil
        return nil, "choice target missing"
    end

    session.nodeId = selected.next
    session.touchedMs = getTimestampMs()
    return makeView(session)
end

function DialogueSession.Close(player, sessionId)
    local session = DialogueSession.Get(player, sessionId)
    if not session then return false end
    sessions[session.playerKey] = nil
    return true
end

local function expireSessions()
    local now = getTimestampMs()
    for playerKey, session in pairs(sessions) do
        if now - session.touchedMs >= C.SESSION_TIMEOUT_MS then
            sessions[playerKey] = nil
        end
    end
end

Events.EveryOneMinute.Add(expireSessions)

LCCQF.DialogueSession = DialogueSession

return DialogueSession
