require "LCCQF/LCCQFConstants"
require "LCCQF/Quest/LCCQFQuestClientState"
require "LCCQF/UI/LCCQFDialoguePanel"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local QuestClientState = LCCQF.QuestClientState
local pendingResync = false
local nextRequestMs = 0

local function log(message)
    print(C.LOG_PREFIX .. "[LIFECYCLE:CLIENT] " .. tostring(message))
end

local function isLocalPlayer(player)
    if not player then return false end
    if player.getPlayerNum then return player:getPlayerNum() == 0 end
    return player == getSpecificPlayer(0)
end

local function closeTransientDialogue()
    local panel = LCCQFDialoguePanel and LCCQFDialoguePanel.instance or nil
    if panel then panel:close(false) end
end

local function beginCharacterTransition(source)
    QuestClientState.BeginCharacterTransition(source)
    closeTransientDialogue()
    pendingResync = false
    nextRequestMs = 0
    log("character projection cleared source=" .. tostring(source))
end

local function requestQuestSnapshot(player, source)
    if not player or player:isDead() then return false end
    sendClientCommand(player, C.MODULE, C.COMMAND.REQUEST_QUESTS, {})
    nextRequestMs = getTimestampMs() + 2000
    log("quest snapshot requested source=" .. tostring(source))
    return true
end

local function onPlayerDeath(player)
    if not isLocalPlayer(player) then return end
    beginCharacterTransition("death")
end

local function onCreatePlayer(playerNum, player)
    if tonumber(playerNum) ~= 0 or not player then return end

    beginCharacterTransition("create-player")
    pendingResync = true
    requestQuestSnapshot(player, "create-player")
end

local function onTick()
    if not pendingResync then return end
    if QuestClientState.IsSynchronized() then
        pendingResync = false
        log("character projection synchronized")
        return
    end

    local player = getSpecificPlayer(0)
    if not player or player:isDead() then return end

    local now = getTimestampMs()
    if now >= nextRequestMs then
        requestQuestSnapshot(player, "retry")
    end
end

if not isServer or not isServer() then
    Events.OnPlayerDeath.Add(onPlayerDeath)
    Events.OnCreatePlayer.Add(onCreatePlayer)
    Events.OnTick.Add(onTick)
end

return true
