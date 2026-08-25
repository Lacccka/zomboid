LCCQF = LCCQF or {}

local DialogueContent = LCCQF.DialogueContent or {}

local dialogues = {
    test_alexey = {
        start = "start",
        nodes = {
            start = {
                textKey = "IGUI_LCCQF_Dialog_Start",
                choices = {
                    { id = "ask_work", textKey = "IGUI_LCCQF_Choice_AskWork", next = "work" },
                    { id = "ask_identity", textKey = "IGUI_LCCQF_Choice_AskIdentity", next = "who" },
                    { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
                },
            },
            work = {
                textKey = "IGUI_LCCQF_Dialog_Work",
                choices = {
                    { id = "back", textKey = "IGUI_LCCQF_Choice_Understood", next = "start" },
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
