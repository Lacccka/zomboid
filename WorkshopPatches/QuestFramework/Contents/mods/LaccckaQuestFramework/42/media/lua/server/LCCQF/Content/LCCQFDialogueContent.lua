require "LCCQF/LCCQFConstants"

LCCQF = LCCQF or {}

local C = LCCQF.Constants
local DialogueContent = LCCQF.DialogueContent or {}

local dialogues = {
    test_alexey = {
        start = "start",
        nodes = {
            start = {
                textKey = "IGUI_LCCQF_Dialog_Start",
                choices = {
                    {
                        id = "ask_work_available",
                        textKey = "IGUI_LCCQF_Choice_AskWork",
                        condition = { kind = "questState", questId = C.TEST_QUEST_ID, state = "available" },
                        next = "work_offer",
                    },
                    {
                        id = "ask_work_active",
                        textKey = "IGUI_LCCQF_Choice_QuestInProgress",
                        condition = { kind = "questState", questId = C.TEST_QUEST_ID, state = "active" },
                        next = "work_active",
                    },
                    {
                        id = "ask_supply_available",
                        textKey = "IGUI_LCCQF_Choice_AskSupplyWork",
                        condition = {
                            kind = "all",
                            conditions = {
                                { kind = "questState", questId = C.TEST_QUEST_ID, state = "completed" },
                                { kind = "questState", questId = C.TEST_QUEST_2_ID, state = "available" },
                            },
                        },
                        next = "supply_offer",
                    },
                    {
                        id = "ask_supply_active",
                        textKey = "IGUI_LCCQF_Choice_SupplyInProgress",
                        condition = { kind = "questState", questId = C.TEST_QUEST_2_ID, state = "active" },
                        next = "supply_active",
                    },
                    {
                        id = "ask_supply_completed",
                        textKey = "IGUI_LCCQF_Choice_SupplyCompletedTopic",
                        condition = { kind = "questState", questId = C.TEST_QUEST_2_ID, state = "completed" },
                        next = "supply_completed",
                    },
                    { id = "ask_identity", textKey = "IGUI_LCCQF_Choice_AskIdentity", next = "who" },
                    { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
                },
            },
            work_offer = {
                textKey = "IGUI_LCCQF_Dialog_WorkOffer",
                choices = {
                    {
                        id = "accept_checkpoint",
                        textKey = "IGUI_LCCQF_Choice_AcceptQuest",
                        condition = { kind = "questState", questId = C.TEST_QUEST_ID, state = "available" },
                        action = { kind = "questAccept", questId = C.TEST_QUEST_ID },
                        next = "work_accepted",
                    },
                    { id = "decline_checkpoint", textKey = "IGUI_LCCQF_Choice_DeclineQuest", next = "start" },
                    { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
                },
            },
            work_accepted = {
                textKey = "IGUI_LCCQF_Dialog_WorkAccepted",
                choices = {
                    { id = "back", textKey = "IGUI_LCCQF_Choice_Understood", next = "start" },
                    { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
                },
            },
            work_active = {
                textKey = "IGUI_LCCQF_Dialog_WorkActive",
                choices = {
                    { id = "back", textKey = "IGUI_LCCQF_Choice_Back", next = "start" },
                    { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
                },
            },
            supply_offer = {
                textKey = "IGUI_LCCQF_Dialog_SupplyOffer",
                choices = {
                    {
                        id = "accept_supply_run",
                        textKey = "IGUI_LCCQF_Choice_AcceptSupplyQuest",
                        condition = {
                            kind = "all",
                            conditions = {
                                { kind = "questState", questId = C.TEST_QUEST_ID, state = "completed" },
                                { kind = "questState", questId = C.TEST_QUEST_2_ID, state = "available" },
                            },
                        },
                        action = { kind = "questAccept", questId = C.TEST_QUEST_2_ID },
                        next = "supply_accepted",
                    },
                    { id = "decline_supply", textKey = "IGUI_LCCQF_Choice_DeclineQuest", next = "start" },
                    { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
                },
            },
            supply_accepted = {
                textKey = "IGUI_LCCQF_Dialog_SupplyAccepted",
                choices = {
                    { id = "back", textKey = "IGUI_LCCQF_Choice_Understood", next = "start" },
                    { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
                },
            },
            supply_active = {
                textKey = "IGUI_LCCQF_Dialog_SupplyActive",
                choices = {
                    { id = "back", textKey = "IGUI_LCCQF_Choice_Back", next = "start" },
                    { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
                },
            },
            supply_completed = {
                textKey = "IGUI_LCCQF_Dialog_SupplyCompleted",
                choices = {
                    { id = "back", textKey = "IGUI_LCCQF_Choice_Back", next = "start" },
                    { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
                },
            },
            who = {
                textKey = "IGUI_LCCQF_Dialog_Who",
                choices = {
                    { id = "back", textKey = "IGUI_LCCQF_Choice_Back", next = "start" },
                    { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
                },
            },
        },
    },
}

function DialogueContent.Get(dialogueId)
    if type(dialogueId) ~= "string" then return nil end
    return dialogues[dialogueId]
end

LCCQF.DialogueContent = DialogueContent

return DialogueContent
