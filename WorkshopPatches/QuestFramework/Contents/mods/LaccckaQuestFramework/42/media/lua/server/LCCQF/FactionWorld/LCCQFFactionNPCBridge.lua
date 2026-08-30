-- Logical bridge from autonomous faction population records into the common NPC registry
-- and runtime-binding protocol. No Bandits/world objects are referenced here.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFNPCRegistry"
require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/FactionWorld/LCCQFFactionSiteRegistry"
require "LCCQF/FactionWorld/LCCQFFactionSitePopulation"
require "LCCQF/Dialogue/LCCQFFactionResidentDialogue"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local NPCRegistry = LCCQF.NPCRegistry
local Runtime = LCCQF.NPCRuntime
local Sites = LCCQF.FactionSiteRegistry
local Population = LCCQF.FactionSitePopulation
local Bridge = LCCQF.FactionNPCBridge or {}
local lastPublished = Bridge.lastPublished or {}

local ROLE_NAME_KEYS = {
    leader = "IGUI_LCCQF_FactionNPC_Leader",
    guard = "IGUI_LCCQF_FactionNPC_Guard",
    trader = "IGUI_LCCQF_FactionNPC_Trader",
    medic = "IGUI_LCCQF_FactionNPC_Medic",
    worker = "IGUI_LCCQF_FactionNPC_Worker",
    member = "IGUI_LCCQF_FactionNPC_Member",
}

local function displayNameKey(member)
    return ROLE_NAME_KEYS[member.roleId] or ROLE_NAME_KEYS.member
end

function Bridge.EnsureDefinition(site, member)
    if type(site) ~= "table" or type(member) ~= "table" or type(member.npcId) ~= "string" then
        return false, "invalid faction NPC"
    end
    return NPCRegistry.RegisterGenerated({
        npcId = member.npcId,
        displayNameKey = displayNameKey(member),
        dialogueId = "lccq_faction_resident_generic",
        interactive = true,
        generated = true,
        spawnable = false,
        factionId = site.factionId,
        factionSiteId = site.siteId,
        factionRoleId = member.roleId,
        runtime = { adapter = tostring(site.population and site.population.materializer or "Bandits") },
    })
end

function Bridge.EnsureSiteDefinitions(site)
    if type(site) ~= "table" then return 0 end
    local count = 0
    for _, member in ipairs(Population.ListMembers(site)) do
        local ok = Bridge.EnsureDefinition(site, member)
        if ok then count = count + 1 end
    end
    return count
end

local function publicPayload(handle)
    local definition = NPCRegistry.MakePublicDefinition(handle.npcId)
    return {
        runtimeId = tostring(handle.runtimeId),
        npcId = tostring(handle.npcId),
        x = tonumber(handle.x), y = tonumber(handle.y), z = tonumber(handle.z),
        publicDefinition = definition,
    }
end

local function signature(handle)
    return table.concat({
        tostring(handle.runtimeId),
        string.format("%.1f", tonumber(handle.x) or 0),
        string.format("%.1f", tonumber(handle.y) or 0),
        string.format("%.1f", tonumber(handle.z) or 0),
    }, "|")
end

function Bridge.BindPhysical(site, member, handle)
    if type(handle) ~= "table" or handle.runtimeId == nil then return false, "invalid physical handle" end
    local ok, errorText = Bridge.EnsureDefinition(site, member)
    if not ok then return false, errorText end

    local payload = publicPayload({
        runtimeId = handle.runtimeId,
        npcId = member.npcId,
        x = handle.x, y = handle.y, z = handle.z,
    })
    if not Runtime.BindRuntime(payload.runtimeId, payload.npcId, payload) then
        return false, "common NPC runtime rejected faction binding"
    end

    local nextSignature = signature(payload)
    if lastPublished[member.npcId] ~= nextSignature then
        lastPublished[member.npcId] = nextSignature
        sendServerCommand(C.MODULE, C.COMMAND.RUNTIME_BINDING_UPSERT, payload)
    end
    return true
end

function Bridge.UnbindPhysical(member, reason)
    if type(member) ~= "table" or member.runtimeId == nil then return false end
    local runtimeId = tostring(member.runtimeId)
    local npcId = tostring(member.npcId or "")
    if npcId == "" then return false end
    local removed = Runtime.UnbindRuntime(runtimeId, npcId)
    if removed or lastPublished[npcId] ~= nil then
        lastPublished[npcId] = nil
        sendServerCommand(C.MODULE, C.COMMAND.RUNTIME_BINDING_REMOVE, {
            runtimeId = runtimeId,
            npcId = npcId,
            reason = tostring(reason or "faction-runtime-unavailable"),
        })
    end
    return removed
end

function Bridge.RunDefinitionsPass()
    local total = 0
    for _, site in ipairs(Sites.ListSites()) do
        if site.population and site.state ~= "ABANDONED" then
            total = total + Bridge.EnsureSiteDefinitions(site)
        end
    end
    return total
end

if isServer and isServer() then
    if Events.OnServerStarted then Events.OnServerStarted.Add(Bridge.RunDefinitionsPass) end
    if Events.EveryOneMinute then Events.EveryOneMinute.Add(Bridge.RunDefinitionsPass) end
end

Bridge.lastPublished = lastPublished
LCCQF.FactionNPCBridge = Bridge
return Bridge
