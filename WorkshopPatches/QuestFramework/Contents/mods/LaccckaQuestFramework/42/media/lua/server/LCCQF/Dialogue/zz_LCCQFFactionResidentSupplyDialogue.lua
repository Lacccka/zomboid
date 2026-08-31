-- Adds autonomous settlement supply work to the reusable faction-resident dialogue.
-- Conditions and acceptance are resolved server-side by QuestService on every render/choice.
if isClient and isClient() and not (isServer and isServer()) then return {} end

require "LCCQF/Dialogue/LCCQFFactionResidentDialogue"
require "LCCQF/Dialogue/LCCQFDialogueRegistry"
require "LCCQF/Quest/zz_LCCQFFactionSupplyQuestServiceExtension"

local Registry = LCCQF.DialogueRegistry
local dialogue = Registry.Get("lccq_faction_resident_generic")

if type(dialogue) == "table" and dialogue._factionSupplyQuestExtended ~= true then
    local work = dialogue.nodes and dialogue.nodes.work or nil
    if type(work) == "table" then
        work.choices = {
            {
                id = "ask_supply_need",
                textKey = "IGUI_LCCQF_Choice_FactionResident_SupplyNeed",
                condition = { kind = "factionSupplyQuestAvailable" },
                next = "supply_offer",
            },
            {
                id = "supply_need_active",
                textKey = "IGUI_LCCQF_Choice_FactionResident_SupplyInProgress",
                condition = { kind = "factionSupplyQuestActive" },
                next = "supply_active",
            },
            { id = "back", textKey = "IGUI_LCCQF_Choice_Back", next = "start" },
        }
    end

    dialogue.nodes.supply_offer = {
        textKey = "IGUI_LCCQF_Dialog_FactionResident_SupplyOffer",
        choices = {
            {
                id = "accept_supply_need",
                textKey = "IGUI_LCCQF_Choice_FactionResident_AcceptSupply",
                condition = { kind = "factionSupplyQuestAvailable" },
                action = { kind = "factionSupplyQuestAccept" },
                next = "supply_accepted",
            },
            { id = "back", textKey = "IGUI_LCCQF_Choice_Back", next = "work" },
            { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
        },
    }

    dialogue.nodes.supply_accepted = {
        textKey = "IGUI_LCCQF_Dialog_FactionResident_SupplyAccepted",
        choices = {
            { id = "back", textKey = "IGUI_LCCQF_Choice_Understood", next = "start" },
            { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
        },
    }

    dialogue.nodes.supply_active = {
        textKey = "IGUI_LCCQF_Dialog_FactionResident_SupplyActive",
        choices = {
            { id = "back", textKey = "IGUI_LCCQF_Choice_Back", next = "work" },
            { id = "leave", textKey = "IGUI_LCCQF_Choice_Leave", close = true },
        },
    }

    dialogue._factionSupplyQuestExtended = true
end

return Registry
