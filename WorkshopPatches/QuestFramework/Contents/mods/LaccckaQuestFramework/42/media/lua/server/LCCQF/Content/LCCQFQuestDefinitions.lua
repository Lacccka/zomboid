require "LCCQF/LCCQFConstants"
require "LCCQF/Quest/LCCQFQuestRegistry"

local C = LCCQF.Constants

local ok, err = LCCQF.QuestRegistry.Register({
    questId = C.TEST_QUEST_ID,
    titleKey = "IGUI_LCCQF_Quest_Checkpoint_Title",
    descriptionKey = "IGUI_LCCQF_Quest_Checkpoint_Description",
    giverNpcId = C.TEST_NPC_ID,
    repeatable = false,
    objectives = {
        {
            id = "reach_checkpoint",
            type = "ReachArea",
            titleKey = "IGUI_LCCQF_Quest_Checkpoint_Objective_Reach",
            radius = 2.25,
            target = {
                kind = "giverOffset",
                dx = 12,
                dy = 0,
                dz = 0,
            },
            marker = {
                visible = true,
                mode = "EXACT",
                labelKey = "IGUI_LCCQF_Quest_Checkpoint_Marker",
                showOnWorldMap = true,
                showOnMiniMap = false,
            },
        },
        {
            id = "return_to_alexey",
            type = "TalkToNPC",
            titleKey = "IGUI_LCCQF_Quest_Checkpoint_Objective_Return",
            npcId = C.TEST_NPC_ID,
        },
    },
})

if not ok and err ~= "duplicate questId" then
    print(C.LOG_PREFIX .. "[QUEST:REGISTRY] test quest registration failed: " .. tostring(err))
end

return LCCQF.QuestRegistry
