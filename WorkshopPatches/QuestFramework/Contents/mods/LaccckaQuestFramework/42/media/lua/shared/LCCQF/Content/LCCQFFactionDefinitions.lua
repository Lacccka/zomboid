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
})

if not ok and err ~= "duplicate factionId" then
    print(C.LOG_PREFIX .. "[FACTION:REGISTRY] registration failed factionId="
        .. tostring(C.TEST_FACTION_ID) .. " error=" .. tostring(err))
end

return LCCQF.FactionRegistry
