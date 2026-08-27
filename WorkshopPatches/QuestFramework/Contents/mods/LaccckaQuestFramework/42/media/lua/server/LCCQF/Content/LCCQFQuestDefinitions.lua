require "LCCQF/LCCQFConstants"
require "LCCQF/Quest/LCCQFQuestRegistry"

local C = LCCQF.Constants

local function register(definition)
    local ok, err = LCCQF.QuestRegistry.Register(definition)
    if not ok and err ~= "duplicate questId" then
        print(C.LOG_PREFIX .. "[QUEST:REGISTRY] registration failed questId="
            .. tostring(definition and definition.questId) .. " error=" .. tostring(err))
    end
end

register({
    questId = C.TEST_QUEST_ID,
    titleKey = "IGUI_LCCQF_Quest_Checkpoint_Title",
    descriptionKey = "IGUI_LCCQF_Quest_Checkpoint_Description",
    giverNpcId = C.TEST_NPC_ID,
    repeatable = false,
    relationshipReward = {
        trust = 8,
        reputation = 10,
        hostility = 0,
    },
    factionReward = {
        reputation = 10,
    },
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

register({
    questId = C.TEST_QUEST_2_ID,
    titleKey = "IGUI_LCCQF_Quest_SupplyRun_Title",
    descriptionKey = "IGUI_LCCQF_Quest_SupplyRun_Description",
    giverNpcId = C.TEST_NPC_ID,
    repeatable = false,
    acceptCondition = {
        kind = "relationship",
        target = "giverNpc",
        stat = "trust",
        op = ">=",
        value = 5,
    },
    relationshipReward = {
        trust = 15,
        reputation = 20,
        hostility = -5,
    },
    factionReward = {
        reputation = 15,
        member = true,
        rankId = "associate",
    },
    objectives = {
        {
            id = "kill_zombies",
            type = "Kill",
            titleKey = "IGUI_LCCQF_Quest_SupplyRun_Objective_Kill",
            required = 3,
        },
        {
            id = "fetch_sheets",
            type = "Fetch",
            titleKey = "IGUI_LCCQF_Quest_SupplyRun_Objective_Fetch",
            itemFullType = "Base.Sheet",
            required = 2,
        },
        {
            id = "clear_roadside",
            type = "ClearArea",
            titleKey = "IGUI_LCCQF_Quest_SupplyRun_Objective_Clear",
            radius = 5.0,
            maxRemaining = 0,
            target = {
                kind = "giverOffset",
                dx = 20,
                dy = 0,
                dz = 0,
            },
            marker = {
                visible = true,
                mode = "AREA",
                labelKey = "IGUI_LCCQF_Quest_SupplyRun_Marker",
                showOnWorldMap = true,
                showOnMiniMap = false,
            },
        },
        {
            id = "deliver_sheets",
            type = "Deliver",
            titleKey = "IGUI_LCCQF_Quest_SupplyRun_Objective_Deliver",
            npcId = C.TEST_NPC_ID,
            itemFullType = "Base.Sheet",
            required = 2,
        },
    },
})

register({
    questId = C.TEST_QUEST_BRANCH_SUPPORT_ID,
    titleKey = "IGUI_LCCQF_Quest_BranchSupport_Title",
    descriptionKey = "IGUI_LCCQF_Quest_BranchSupport_Description",
    giverNpcId = C.TEST_NPC_ID,
    repeatable = false,
    branch = {
        groupId = C.TEST_BRANCH_GROUP_ID,
        optionId = C.TEST_BRANCH_SUPPORT_OPTION,
    },
    acceptCondition = {
        kind = "all",
        conditions = {
            { kind = "questState", questId = C.TEST_QUEST_2_ID, state = "completed" },
            {
                kind = "questBranchAvailable",
                groupId = C.TEST_BRANCH_GROUP_ID,
                optionId = C.TEST_BRANCH_SUPPORT_OPTION,
            },
        },
    },
    relationshipReward = {
        trust = 5,
        reputation = 5,
        hostility = 0,
    },
    factionReward = {
        reputation = 10,
    },
    objectives = {
        {
            id = "confirm_support",
            type = "TalkToNPC",
            titleKey = "IGUI_LCCQF_Quest_BranchSupport_Objective",
            npcId = C.TEST_NPC_ID,
        },
    },
})

register({
    questId = C.TEST_QUEST_BRANCH_INDEPENDENT_ID,
    titleKey = "IGUI_LCCQF_Quest_BranchIndependent_Title",
    descriptionKey = "IGUI_LCCQF_Quest_BranchIndependent_Description",
    giverNpcId = C.TEST_NPC_ID,
    repeatable = false,
    branch = {
        groupId = C.TEST_BRANCH_GROUP_ID,
        optionId = C.TEST_BRANCH_INDEPENDENT_OPTION,
    },
    acceptCondition = {
        kind = "all",
        conditions = {
            { kind = "questState", questId = C.TEST_QUEST_2_ID, state = "completed" },
            {
                kind = "questBranchAvailable",
                groupId = C.TEST_BRANCH_GROUP_ID,
                optionId = C.TEST_BRANCH_INDEPENDENT_OPTION,
            },
        },
    },
    relationshipReward = {
        trust = 2,
        reputation = 1,
        hostility = 0,
    },
    factionReward = {
        reputation = -5,
    },
    objectives = {
        {
            id = "confirm_independence",
            type = "TalkToNPC",
            titleKey = "IGUI_LCCQF_Quest_BranchIndependent_Objective",
            npcId = C.TEST_NPC_ID,
        },
    },
})

return LCCQF.QuestRegistry
