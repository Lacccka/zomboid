--
-- Bandits NPC - Companions Overhaul - Recruitment requirements (Phase 3)
--
-- Each NPC has a join requirement (none / food / water / a melee weapon),
-- chosen deterministically and cached on the brain. The actual inventory
-- check + consume + recruit lives client-side in BanditsNPCInteract.lua; this
-- file just defines the requirement and its dialogue lines.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Recruit = BanditsNPC.Recruit or {}

-- Dialogue lines per requirement type.
BanditsNPC.Recruit.Defs = {
    none = {
        prompt  = BanditsNPC.T("UI_BN_Rec_none_prompt", "\"Honestly? I've got nowhere else to be. I'll come with you.\""),
        success = BanditsNPC.T("UI_BN_Rec_none_success", "\"Lead the way.\""),
    },
    food = {
        prompt  = BanditsNPC.T("UI_BN_Rec_food_prompt", "\"I'll travel with you, but I'm running on empty. Got any food to spare?\""),
        refusal = BanditsNPC.T("UI_BN_Rec_food_refusal", "\"Sorry... bring me something to eat first.\""),
        success = BanditsNPC.T("UI_BN_Rec_food_success", "\"...thank you. Alright, I'm with you.\""),
    },
    water = {
        prompt  = BanditsNPC.T("UI_BN_Rec_water_prompt", "\"My throat's like sandpaper. Got anything to drink? Then we'll talk.\""),
        refusal = BanditsNPC.T("UI_BN_Rec_water_refusal", "\"Not until I get something to drink.\""),
        success = BanditsNPC.T("UI_BN_Rec_water_success", "\"*drinks* ...okay. I'm in.\""),
    },
    weapon = {
        prompt  = BanditsNPC.T("UI_BN_Rec_weapon_prompt", "\"I'm not stepping out there empty-handed. Spare me a melee weapon and I'm yours.\""),
        refusal = BanditsNPC.T("UI_BN_Rec_weapon_refusal", "\"Find me a weapon first, then we'll talk.\""),
        success = BanditsNPC.T("UI_BN_Rec_weapon_success", "\"Now we're talking. Let's move.\""),
    },
}

-- Possible requirement types and their relative weights (more = likelier).
local POOL = { "none", "food", "water", "weapon", "food", "weapon" }

function BanditsNPC.Recruit.GetRequirementType(brain)
    local seed = math.abs(brain.id or 0) + 101
    if brain.rnd then
        for _, v in ipairs(brain.rnd) do seed = seed + (v or 0) end
    end
    return POOL[(seed % #POOL) + 1]
end

-- Ensures the brain has a cached requirement; returns {type=, def=}.
function BanditsNPC.Recruit.Ensure(zombie)
    local brain = BanditBrain.Get(zombie)
    if not brain then return nil end

    if not brain.joinReq then
        brain.joinReq = { type = BanditsNPC.Recruit.GetRequirementType(brain) }
        BanditBrain.Update(zombie, brain)
        if Bandit and Bandit.ForceSyncPart then
            Bandit.ForceSyncPart(zombie, { id = brain.id, joinReq = brain.joinReq })
        end
    end

    return { type = brain.joinReq.type, def = BanditsNPC.Recruit.Defs[brain.joinReq.type] }
end
