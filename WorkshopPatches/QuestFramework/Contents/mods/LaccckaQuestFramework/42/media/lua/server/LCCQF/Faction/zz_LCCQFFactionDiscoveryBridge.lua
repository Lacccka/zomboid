require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFNPCRegistry"
require "LCCQF/Content/LCCQFFactionDefinitions"
require "LCCQF/Persistence/LCCQFCharacterKnowledge"
require "LCCQF/Persistence/LCCQFCharacterFactionKnowledge"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local NPCRegistry = LCCQF.NPCRegistry
local CharacterKnowledge = LCCQF.CharacterKnowledge
local CharacterFactionKnowledge = LCCQF.CharacterFactionKnowledge
local installed = false

local function log(message)
    print(C.LOG_PREFIX .. "[FACTION:DISCOVERY:BRIDGE] " .. tostring(message))
end

local function discoverFactionFromNPC(player, npcId, source)
    local definition = type(npcId) == "string" and NPCRegistry.Get(npcId) or nil
    if not definition or definition.revealFactionOnDiscovery ~= true then return false, "not revealed" end
    if type(definition.factionId) ~= "string" then return false, "no faction" end

    return CharacterFactionKnowledge.DiscoverFaction(
        player,
        definition.factionId,
        "npc-discovery:" .. tostring(source or npcId)
    )
end

local function installNPCDiscoveryBridge()
    if installed then return true end
    if CharacterKnowledge.__LCCQFFactionDiscoveryWrapped then
        installed = true
        return true
    end
    if type(CharacterKnowledge.DiscoverNPC) ~= "function" then return false end

    local originalDiscoverNPC = CharacterKnowledge.DiscoverNPC
    CharacterKnowledge.DiscoverNPC = function(player, npcId, source)
        local ok, createdOrErr = originalDiscoverNPC(player, npcId, source)
        if ok then
            local factionOk, factionResult = discoverFactionFromNPC(player, npcId, source)
            if factionOk and factionResult == true then
                log("faction revealed by npc npcId=" .. tostring(npcId)
                    .. " source=" .. tostring(source or "unknown"))
            end
        end
        return ok, createdOrErr
    end

    CharacterKnowledge.__LCCQFFactionDiscoveryWrapped = true
    installed = true
    log("NPC discovery faction reveal installed")
    return true
end

local function sendSnapshot(player)
    if not player then return end
    local factions, revision = CharacterFactionKnowledge.ExportViews(player)
    sendServerCommand(player, C.MODULE, C.COMMAND.FACTIONS, {
        factions = factions,
        revision = revision,
    })
    log("snapshot sent player=" .. tostring(player:getUsername())
        .. " factions=" .. tostring(#factions)
        .. " revision=" .. tostring(revision))
end

local function onFactionEvent(kind, player, payload)
    if kind ~= "upsert" or not player or type(payload) ~= "table" then return end
    sendServerCommand(player, C.MODULE, C.COMMAND.KNOWN_FACTION_UPSERT, payload)
end

local function onClientCommand(module, command, player, args)
    if module ~= C.MODULE or command ~= C.COMMAND.REQUEST_FACTIONS then return end
    sendSnapshot(player)
end

local function onServerStarted()
    local ok, err = CharacterFactionKnowledge.Initialize()
    CharacterFactionKnowledge.SetEventSink(onFactionEvent)
    local bridged = installNPCDiscoveryBridge()
    log("loaded version=" .. tostring(C.VERSION)
        .. " persistence=" .. tostring(ok and "ready" or err or "unavailable")
        .. " npcDiscoveryBridge=" .. tostring(bridged))
end

installNPCDiscoveryBridge()

if isServer and isServer() then
    Events.OnClientCommand.Add(onClientCommand)
    Events.OnServerStarted.Add(onServerStarted)
end

return true
