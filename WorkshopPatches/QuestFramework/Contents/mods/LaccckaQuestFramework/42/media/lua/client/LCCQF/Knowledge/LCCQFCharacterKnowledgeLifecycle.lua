require "LCCQF/LCCQFConstants"
require "LCCQF/Knowledge/LCCQFKnownPeopleClientState"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local KnownPeople = LCCQF.KnownPeopleClientState
local pendingResync = false
local nextRequestMs = 0

local function log(message)
    print(C.LOG_PREFIX .. "[KNOWLEDGE:LIFECYCLE] " .. tostring(message))
end

local function isLocalPlayer(player)
    if not player then return false end
    if player.getPlayerNum then return player:getPlayerNum() == 0 end
    return player == getSpecificPlayer(0)
end

local function requestSnapshot(player, source)
    if not player or player:isDead() then return false end
    sendClientCommand(player, C.MODULE, C.COMMAND.REQUEST_KNOWLEDGE, {})
    nextRequestMs = getTimestampMs() + 2000
    log("snapshot requested source=" .. tostring(source))
    return true
end

local function requestCurrentPlayerSnapshot(source)
    local player = getSpecificPlayer(0)
    if not player or player:isDead() then return false end
    pendingResync = true
    return requestSnapshot(player, source)
end

local function beginTransition(source)
    KnownPeople.BeginCharacterTransition(source)
    pendingResync = false
    nextRequestMs = 0
    log("projection cleared source=" .. tostring(source))
end

local function onPlayerDeath(player)
    if not isLocalPlayer(player) then return end
    beginTransition("death")
end

local function onCreatePlayer(playerNum, player)
    if tonumber(playerNum) ~= 0 or not player then return end
    beginTransition("create-player")
    pendingResync = true
    requestSnapshot(player, "create-player")
end

local function onServerCommand(module, command, args)
    if module ~= C.MODULE then return end
    args = args or {}

    if command == C.COMMAND.KNOWLEDGE then
        local count = KnownPeople.Replace(args.people, args.revision)
        pendingResync = false
        log("snapshot synchronized people=" .. tostring(count)
            .. " revision=" .. tostring(args.revision))
        return
    end

    if command == C.COMMAND.KNOWN_PERSON_UPSERT then
        if KnownPeople.Apply(args, args.knowledgeRevision) then
            log("known person upsert npcId=" .. tostring(args.npcId)
                .. " revision=" .. tostring(args.knowledgeRevision))
        end
        return
    end

    -- A completed authoritative quest may unlock new biography/history facts.
    -- Re-request the sanitized knowledge projection instead of deriving those
    -- facts from client quest state.
    if command == C.COMMAND.QUEST_EVENT
        and args.state == "completed"
        and args.messageKey == "IGUI_LCCQF_QuestEvent_Completed"
    then
        requestCurrentPlayerSnapshot("quest-completed")
    end
end

local function onGameStart()
    KnownPeople.Clear()
    requestCurrentPlayerSnapshot("game-start")
end

local function onTick()
    if not pendingResync then return end
    if KnownPeople.IsSynchronized() then
        pendingResync = false
        return
    end

    local player = getSpecificPlayer(0)
    if not player or player:isDead() then return end
    if getTimestampMs() >= nextRequestMs then
        requestSnapshot(player, "retry")
    end
end

if not isServer or not isServer() then
    Events.OnPlayerDeath.Add(onPlayerDeath)
    Events.OnCreatePlayer.Add(onCreatePlayer)
    Events.OnServerCommand.Add(onServerCommand)
    Events.OnGameStart.Add(onGameStart)
    Events.OnTick.Add(onTick)
end

return true
