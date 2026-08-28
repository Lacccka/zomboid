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
        minDistanceFromPlayerSafehouses = 120,
        minDistanceFromOtherFactionSites = 180,
        minScore = 2,
        maxSites = 1,
        wantsIndoor = true,
        wantsWater = true,
        wantsBeds = true,
        wantsRoadAccess = true,
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
})

if not ok and err ~= "duplicate factionId" then
    print(C.LOG_PREFIX .. "[FACTION:REGISTRY] registration failed factionId="
        .. tostring(C.TEST_FACTION_ID) .. " error=" .. tostring(err))
end

return LCCQF.FactionRegistry
