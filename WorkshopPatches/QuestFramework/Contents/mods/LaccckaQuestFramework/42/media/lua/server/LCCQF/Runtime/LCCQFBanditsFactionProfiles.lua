-- Bandits2 provider configuration. Raw clan/profile IDs are intentionally isolated
-- here and are never part of faction content or logical population persistence.
if isClient and isClient() and not (isServer and isServer()) then
    return {}
end

require "BanditCustom"
require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local Profiles = LCCQF.BanditsFactionProfiles or {}

local DEFINITIONS = {
    checkpoint_survivors_v1 = {
        cid = "7fbd141e-18bd-4a8c-b4b7-a1c15f100001",
        name = "LCCQF_CheckpointSurvivors",
        members = {
            { bid = "7fbd141e-18bd-4a8c-b4b7-a1c15f110001", name = "Checkpoint Survivor A", female = false, skin = 1, hairType = 2, beardType = 2, hairColor = 2 },
            { bid = "7fbd141e-18bd-4a8c-b4b7-a1c15f110002", name = "Checkpoint Survivor B", female = true,  skin = 1, hairType = 3, beardType = 1, hairColor = 3 },
            { bid = "7fbd141e-18bd-4a8c-b4b7-a1c15f110003", name = "Checkpoint Survivor C", female = false, skin = 2, hairType = 4, beardType = 3, hairColor = 1 },
            { bid = "7fbd141e-18bd-4a8c-b4b7-a1c15f110004", name = "Checkpoint Survivor D", female = true,  skin = 2, hairType = 2, beardType = 1, hairColor = 2 },
            { bid = "7fbd141e-18bd-4a8c-b4b7-a1c15f110005", name = "Checkpoint Survivor E", female = false, skin = 1, hairType = 5, beardType = 2, hairColor = 3 },
        },
    },
}

local function log(message)
    print(C.LOG_PREFIX .. "[RUNTIME:BANDITS:FACTION:PROFILES] " .. tostring(message))
end

local function ensureClan(definition)
    local clan = BanditCustom.ClanGet(definition.cid)
    if not clan then clan = BanditCustom.ClanCreate(definition.cid) end
    if not clan then return nil, "BanditCustom clan creation failed" end

    clan.general = clan.general or {}
    clan.general.name = definition.name
    clan.spawn = clan.spawn or {}
    clan.spawn.friendly = true
    clan.spawn.companion = false
    clan.spawn.defenders = true
    clan.spawn.campers = false
    clan.spawn.assault = false
    clan.spawn.wanderer = false
    clan.spawn.roadblock = false
    clan.spawn.dayStart = 0
    clan.spawn.dayEnd = 10000
    clan.spawn.spawnChance = 0 -- never enter Bandits autonomous random spawn tables
    clan.spawn.groupMin = 1
    clan.spawn.groupMax = #definition.members
    clan.spawn.zone = 0
    return clan
end

local function ensureBandit(definition, memberDefinition)
    local bandit = BanditCustom.GetById(memberDefinition.bid)
    if not bandit then bandit = BanditCustom.Create(memberDefinition.bid) end
    if not bandit then return nil, "BanditCustom profile creation failed" end

    -- Bandits2 currently reads general.cid for Clan() and top-level cid for
    -- Individual(). Keep both provider details local to this adapter-owned profile.
    bandit.cid = definition.cid
    bandit.general = bandit.general or {}
    bandit.general.cid = definition.cid
    bandit.general.name = memberDefinition.name
    bandit.general.female = memberDefinition.female == true
    bandit.general.skin = memberDefinition.skin or 1
    bandit.general.hairType = memberDefinition.hairType or 1
    bandit.general.beardType = memberDefinition.beardType or 1
    bandit.general.hairColor = memberDefinition.hairColor or 1
    bandit.general.health = 5
    bandit.general.sight = 5
    bandit.general.endurance = 5
    bandit.general.strength = 5

    bandit.clothing = bandit.clothing or {}
    bandit.tint = bandit.tint or {}
    bandit.weapons = bandit.weapons or {}
    bandit.ammo = bandit.ammo or {}
    bandit.bag = bandit.bag or {}
    return bandit
end

function Profiles.Get(profileId)
    return DEFINITIONS[profileId]
end

function Profiles.ListProviderIds(profileId)
    local definition = Profiles.Get(profileId)
    local out = {}
    if not definition then return out end
    for _, member in ipairs(definition.members) do out[#out + 1] = member.bid end
    return out
end

function Profiles.Ensure(profileId)
    local definition = Profiles.Get(profileId)
    if not definition then return nil, "unknown Bandits provider profile" end

    local clan, clanError = ensureClan(definition)
    if not clan then return nil, clanError end

    for _, memberDefinition in ipairs(definition.members) do
        local bandit, banditError = ensureBandit(definition, memberDefinition)
        if not bandit then return nil, banditError end
    end

    local available = BanditCustom.GetFromClan(definition.cid)
    local count = 0
    for _ in pairs(available or {}) do count = count + 1 end
    if count < #definition.members then
        return nil, "provider pool registration incomplete"
    end

    log("ready providerProfile=" .. tostring(profileId)
        .. " cid=" .. tostring(definition.cid)
        .. " profiles=" .. tostring(count))
    return {
        providerProfile = profileId,
        cid = definition.cid,
        profileCount = count,
        providerIds = Profiles.ListProviderIds(profileId),
    }
end

LCCQF.BanditsFactionProfiles = Profiles
return Profiles
