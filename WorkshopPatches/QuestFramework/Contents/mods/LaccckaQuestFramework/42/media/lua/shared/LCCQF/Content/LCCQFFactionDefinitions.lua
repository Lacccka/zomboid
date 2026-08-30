require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFFactionRegistry"

local C = LCCQF.Constants

local ok, err = LCCQF.FactionRegistry.Register({
    factionId = C.TEST_FACTION_ID,
    displayNameKey = "IGUI_LCCQF_Faction_CheckpointSurvivors",
    summaryKey = "IGUI_LCCQF_Faction_CheckpointSurvivors_Summary",
    ranks = {
        {
            rankId = "associate",
            displayNameKey = "IGUI_LCCQF_Faction_CheckpointSurvivors_Rank_Associate",
        },
    },
    initialKnowledgeFacts = {
        "checkpoint_group_identified",
    },
    knowledgeFacts = {
        {
            id = "checkpoint_group_identified",
            titleKey = "IGUI_LCCQF_Faction_CheckpointSurvivors_Fact_Identified_Title",
            textKey = "IGUI_LCCQF_Faction_CheckpointSurvivors_Fact_Identified_Text",
        },
    },
    siteProfile = {
        enabled = true,
        kind = "settlement",
        minRooms = 3,
        minDistanceFromPlayers = 80,
        minDistanceFromOtherFactionSites = 180,
        minScore = 2,
        maxSites = 1,
        wantsIndoor = true,

        -- Expensive live-world requirements are checked only after the cheap
        -- metadata scorer has reserved a candidate. A failed reservation is
        -- abandoned and the allocator is free to try the next building.
        wantsBeds = true,
        wantsWater = true,
        minStorageContainers = 1,
        minFreeSpawnPoints = 3,

        preferredZones = {
            TownZone = 5,
            TrailerPark = 2,
            Ranch = 3,
            Farm = 2,
            FarmLand = 1,
            LootZone = 1,
        },
        avoidedZones = {
            Forest = 2,
            DeepForest = 4,
            Vegitation = 2,
        },
    },

    -- Provider-neutral population intent. Raw Bandits clan/profile IDs deliberately
    -- live in the Bandits adapter, not in faction content or persistence.
    populationProfile = {
        enabled = true,
        initialPopulation = 3,
        maxPopulation = 5,
        materializer = "Bandits",
        providerProfile = "checkpoint_survivors_v1",
        program = "LCCQFFactionGuard",

        -- Site allocation keeps players 80 tiles away from a newly claimed base, while
        -- physical pop-in/recovery uses a smaller independent safety radius. Keeping the
        -- two policies separate avoids a loaded-chunk/proximity deadlock.
        minMaterializationDistanceFromPlayers = 24,

        -- Server-owned home/guard intent. The Bandits adapter maps this generic policy
        -- to runtime brain tags/program state; faction core never calls Bandits APIs.
        homeRadius = 10,
        returnRadius = 24,
        guardRadius = 18,

        -- Logical deaths remain historical identities. Population maintenance may plan
        -- a new npcId after this delay instead of resurrecting the dead identity.
        replaceDead = true,
        replacementDelayHours = 24,

        roles = {
            { roleId = "leader", count = 1 },
            { roleId = "guard", count = 2 },
        },
    },
})

if not ok and err ~= "duplicate factionId" then
    print(C.LOG_PREFIX .. "[FACTION:REGISTRY] registration failed factionId="
        .. tostring(C.TEST_FACTION_ID) .. " error=" .. tostring(err))
end

return LCCQF.FactionRegistry
