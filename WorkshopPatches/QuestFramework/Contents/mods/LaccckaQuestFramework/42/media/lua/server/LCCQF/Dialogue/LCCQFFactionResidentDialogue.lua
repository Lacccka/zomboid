require "LCCQF/Dialogue/LCCQFDialogueRegistry"

local Registry = LCCQF.DialogueRegistry

Registry.Register("lccq_faction_resident_generic", {
    start = "start",
    nodes = {
        start = {
            textKey = "IGUI_LCCQF_Dialog_FactionResident_Start",
            choices = {
                {
                    id = "ask_settlement",
                    textKey = "IGUI_LCCQF_Choice_FactionResident_Settlement",
                    next = "settlement",
                },
                {
                    id = "ask_work",
                    textKey = "IGUI_LCCQF_Choice_FactionResident_Work",
                    next = "work",
                },
                { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
            },
        },
        settlement = {
            textKey = "IGUI_LCCQF_Dialog_FactionResident_Settlement",
            choices = {
                { id = "back", textKey = "IGUI_LCCQF_Choice_Back", next = "start" },
                { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
            },
        },
        work = {
            textKey = "IGUI_LCCQF_Dialog_FactionResident_Work",
            choices = {
                { id = "back", textKey = "IGUI_LCCQF_Choice_Back", next = "start" },
                { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
            },
        },
    },
})

return Registry
