require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFNPCRegistry"
require "LCCQF/Content/LCCQFFactionDefinitions"

local C = LCCQF.Constants

local ok, err = LCCQF.NPCRegistry.Register({
    npcId = C.TEST_NPC_ID,
    displayNameKey = "IGUI_LCCQF_NPC_Alexey",
    summaryKey = "IGUI_LCCQF_NPC_Alexey_Summary",
    dialogueId = "test_alexey",
    factionId = C.TEST_FACTION_ID,
    revealFactionOnDiscovery = true,
    stationary = true,
    portrait = {
        provider = "live-runtime",
        zoom = 12,
        yOffset = -0.78,
        xOffset = 0,
        direction = "S",
    },
    initialKnowledgeFacts = {
        "met_alexey",
    },
    knowledgeFacts = {
        {
            id = "met_alexey",
            titleKey = "IGUI_LCCQF_NPC_Alexey_Fact_Met_Title",
            textKey = "IGUI_LCCQF_NPC_Alexey_Fact_Met_Text",
        },
        {
            id = "checkpoint_completed",
            titleKey = "IGUI_LCCQF_NPC_Alexey_Fact_Checkpoint_Title",
            textKey = "IGUI_LCCQF_NPC_Alexey_Fact_Checkpoint_Text",
        },
        {
            id = "supply_run_completed",
            titleKey = "IGUI_LCCQF_NPC_Alexey_Fact_Supply_Title",
            textKey = "IGUI_LCCQF_NPC_Alexey_Fact_Supply_Text",
        },
    },
    questKnowledgeFacts = {
        [C.TEST_QUEST_ID] = {
            completed = { "checkpoint_completed" },
        },
        [C.TEST_QUEST_2_ID] = {
            completed = { "supply_run_completed" },
        },
    },
    runtime = {
        adapter = "Bandits",
        profileId = C.TEST_NPC_BID,
        clanId = C.TEST_NPC_CID,
        program = "LCCQFQuestGiver",
    },
})

if not ok and err ~= "duplicate npcId" then
    print(C.LOG_PREFIX .. "[REGISTRY] test NPC registration failed: " .. tostring(err))
end

return LCCQF.NPCRegistry
