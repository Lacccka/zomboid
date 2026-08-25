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
                        id = "ask_work_completed",
                        textKey = "IGUI_LCCQF_Choice_QuestCompletedTopic",
                        condition = { kind = "questState", questId = C.TEST_QUEST_ID, state = "completed" },
                        next = "work_completed",
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
            work_completed = {
                textKey = "IGUI_LCCQF_Dialog_WorkCompleted",
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
