require "LCCQF/LCCQFConstants"
require "LCCQF/Faction/LCCQFKnownFactionsClientState"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local KnownFactions = LCCQF.KnownFactionsClientState
local pendingResync = false
local nextRequestMs = 0

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:LIFECYCLE] " .. tostring(message))
end

local function isLocalPlayer(player)
    if not player then return false end
    if player.getPlayerNum then return player:getPlayerNum() == 0 end
    return player == getSpecificPlayer(0)
end

local function requestSnapshot(player, source)
    if not player or player:isDead() then return false end
    sendClientCommand(player, C.MODULE, C.COMMAND.REQUEST_FACTIONS, {})
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
    KnownFactions.BeginCharacterTransition(source)
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

    if command == C.COMMAND.FACTIONS then
        local count = KnownFactions.Replace(args.factions, args.revision)
        pendingResync = false
        log("snapshot synchronized factions=" .. tostring(count)
            .. " revision=" .. tostring(args.revision))
        return
    end

    if command == C.COMMAND.KNOWN_FACTION_UPSERT then
        if KnownFactions.Apply(args, args.factionKnowledgeRevision) then
            log("known faction upsert factionId=" .. tostring(args.factionId)
                .. " revision=" .. tostring(args.factionKnowledgeRevision))
        end
    end
end

local function onGameStart()
    KnownFactions.Clear()
    requestCurrentPlayerSnapshot("game-start")
end

local function onTick()
    if not pendingResync then return end
    if KnownFactions.IsSynchronized() then
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
