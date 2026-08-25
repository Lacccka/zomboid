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

local function isChoiceAvailable(player, session, choice, hooks)
    if choice.condition == nil then return true end
    if not hooks or type(hooks.IsChoiceAvailable) ~= "function" then return false end

    local ok, allowed = pcall(hooks.IsChoiceAvailable, player, session, choice)
    return ok and allowed == true
end

local function makeView(player, session, hooks)
    local node = getNode(session)
    if not node then return nil end

    local choices = {}
    for _, choice in ipairs(node.choices or {}) do
        if isChoiceAvailable(player, session, choice, hooks) then
            choices[#choices + 1] = {
                choiceId = choice.id,
                textKey = choice.textKey,
            }
            if #choices >= C.MAX_DIALOGUE_CHOICES then break end
        end
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

function DialogueSession.Open(player, handle, definition, hooks)
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
    return makeView(player, session, hooks)
end

function DialogueSession.Get(player, sessionId)
    local playerKey = getPlayerKey(player)
    local session = playerKey and sessions[playerKey] or nil
    if not session or tostring(session.id) ~= tostring(sessionId) then return nil end
    return session
end

function DialogueSession.Choose(player, sessionId, choiceId, hooks)
    local session = DialogueSession.Get(player, sessionId)
    if not session then return nil, "unknown session" end

    local node = getNode(session)
    if not node then
        sessions[session.playerKey] = nil
        return nil, "invalid dialogue node"
    end

    local selected = nil
    for _, choice in ipairs(node.choices or {}) do
        if tostring(choice.id) == tostring(choiceId)
            and isChoiceAvailable(player, session, choice, hooks)
        then
            selected = choice
            break
        end
    end
    if not selected then return nil, "choice not allowed for current node" end

    if selected.action ~= nil then
        if not hooks or type(hooks.ExecuteAction) ~= "function" then
            return nil, "dialogue action handler unavailable"
        end
        local ok, actionResult, actionErr = pcall(hooks.ExecuteAction, player, session, selected)
        if not ok then return nil, "dialogue action failed" end
        if actionResult ~= true then return nil, actionErr or "dialogue action rejected" end
    end

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
    return makeView(player, session, hooks)
end

function DialogueSession.Close(player, sessionId)
    local session = DialogueSession.Get(player, sessionId)
    if not session then return false end
    sessions[session.playerKey] = nil
    return true
end

function DialogueSession.InvalidateRuntime(runtimeId)
    if runtimeId == nil then return 0 end
    local key = tostring(runtimeId)
    local closed = 0

    for playerKey, session in pairs(sessions) do
        if tostring(session.runtimeId) == key then
            sessions[playerKey] = nil
            closed = closed + 1
        end
    end

    return closed
end

local function expireSessions()
    local now = getTimestampMs()
    for playerKey, session in pairs(sessions) do
        if now - session.touchedMs >= C.SESSION_TIMEOUT_MS then
            sessions[playerKey] = nil
        end
    end
end

if isServer and isServer() then
    Events.EveryOneMinute.Add(expireSessions)
end

LCCQF.DialogueSession = DialogueSession

return DialogueSession
