--
-- Bandits NPC - Companions Overhaul - Affinity (Phase: Quests)
--
-- A single 0..100 value on the brain. The companion is on the ROMANCE TRACK when they
-- are the opposite gender to the player, or when the player picked a track in dialogue
-- (brain.npcRomance == true forces romance -- that's what enables same-gender romance;
-- == false forces platonic). Raised by completing quests (and, later, gifts).
--
-- Affinity.IsRomance is the behavioural primitive; Affinity.GetLabel is a string for
-- humans and nothing branches on it. See the rule block above IsRomance before touching
-- either -- they were entangled until v0.48 and it was a live translation landmine.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Affinity = {}

-- ===== the ROMANCE TRACK (behaviour) vs its LABEL (display) =====
-- These MUST stay separate, and the dependency MUST point this way: label -> track,
-- never track -> label.
--
-- Until v0.48 IsRomance was literally `GetLabel(brain) == "Affection"` -- a behavioural
-- gate keyed by its own DISPLAY STRING. Every romance gate in the mod runs through
-- IsRomance (Talk.GetState's affectionOnly/platonicOnly, the rom_start opt-in, the
-- jealousy rival scan, the partner milestone, the pink bond bar), so the moment anyone
-- translated that one label -- which is exactly what the remaining i18n pass is for --
-- the comparison would stop matching and the ENTIRE romance system would switch itself
-- off for every non-English player, silently. Same class of bug as the v0.47 outfit
-- slots, which vanished because EQUIP_LAYOUT was indexed by a translated label.
-- RULE (HANDOFF section 3): never derive behaviour from a string a translator can edit.

-- TRUE if this companion is on the romance track. Reads STATE only -- no strings, and
-- no dependency on the label. This is the primitive; everything gates on it.
--   brain.npcRomance == true  -> opted IN  (rom_start -- this is what makes same-gender
--                                romance work)
--   brain.npcRomance == false -> opted OUT (rom_stop -- platonic by choice)
--   nil                       -> gender default (opposite gender = romance track)
-- Note: an EXPLICIT opt-in/opt-out no longer needs getSpecificPlayer(0) to resolve.
-- The old chain returned "Relationship" (i.e. not-romance) whenever there was no local
-- player, which quietly overrode a stored choice; only the gender DEFAULT needs a
-- player to compare against.
function BanditsNPC.Affinity.IsRomance(brain)
    if not brain then return false end
    if brain.npcRomance == true then return true end
    if brain.npcRomance == false then return false end
    -- THE OWNER, not whoever is running this client (v0.75.15, audit R6). The default
    -- romance track is "opposite gender to the player", and it was asked of
    -- getSpecificPlayer(0) -- so on another player's screen the same companion could be
    -- on a different track, and the dialogue offered to her differed by who was looking.
    -- GetOwner resolves the recruiter properly; falling back to player 0 only when the
    -- owner is not present keeps single player identical.
    local player
    if BanditsNPC.GetOwnerOf then player = BanditsNPC.GetOwnerOf(brain) end
    player = player or getSpecificPlayer(0)
    if not player then return false end
    return player:isFemale() ~= (brain.female and true or false)
end

-- DISPLAY ONLY, and now translated (Q1). This is only safe BECAUSE of the split above --
-- nothing in the mod branches on the returned string any more. If you ever find yourself
-- writing `GetLabel(x) == "something"`, stop: use IsRomance.
function BanditsNPC.Affinity.GetLabel(brain)
    if not brain or not getSpecificPlayer(0) then
        return BanditsNPC.T("UI_BN_Bond_Relationship", "Relationship")
    end
    if BanditsNPC.Affinity.IsRomance(brain) then
        return BanditsNPC.T("UI_BN_Bond_Affection", "Affection")
    end
    return BanditsNPC.T("UI_BN_Bond_Friendship", "Friendship")
end

function BanditsNPC.Affinity.Get(brain)
    return (brain and brain.affinity) or 0
end

-- 5 tiers (0..4) keyed by affinity value.
BanditsNPC.Affinity.Tiers = { 0, 25, 50, 75, 100 }
-- English fallback text. Kept as plain data (NOT pre-translated) so the tables stay
-- readable and the T() lookup happens at CALL time in GetTierLabel -- see the parallel
-- key tables below. Index = tier 0..4; the two sets are the platonic and romance ladders.
BanditsNPC.Affinity.TierLabel    = { [0]="Wary", [1]="Warming", [2]="Friendly", [3]="Close",   [4]="Devoted" }
BanditsNPC.Affinity.TierLabelRom = { [0]="Wary", [1]="Fond",    [2]="Smitten",  [3]="In love", [4]="Partner" }
-- translation keys, parallel to the two tables above (display only -- nothing branches
-- on a tier LABEL; behaviour uses GetTier's number and IsRomance's boolean)
local TIER_KEY = { [0]="UI_BN_Tier_Wary", [1]="UI_BN_Tier_Warming", [2]="UI_BN_Tier_Friendly",
                   [3]="UI_BN_Tier_Close", [4]="UI_BN_Tier_Devoted" }
local TIER_KEY_ROM = { [0]="UI_BN_TierRom_Wary", [1]="UI_BN_TierRom_Fond", [2]="UI_BN_TierRom_Smitten",
                       [3]="UI_BN_TierRom_InLove", [4]="UI_BN_TierRom_Partner" }

function BanditsNPC.Affinity.GetTier(brain)
    local v = (brain and brain.affinity) or 0
    local t = 0
    for i, minV in ipairs(BanditsNPC.Affinity.Tiers) do if v >= minV then t = i - 1 end end
    return t
end

function BanditsNPC.Affinity.GetTierLabel(brain)
    local t = BanditsNPC.Affinity.GetTier(brain)
    local rom = BanditsNPC.Affinity.IsRomance(brain)
    local tbl = rom and BanditsNPC.Affinity.TierLabelRom or BanditsNPC.Affinity.TierLabel
    local keys = rom and TIER_KEY_ROM or TIER_KEY
    if tbl[t] == nil then return "?" end
    return BanditsNPC.T(keys[t], tbl[t])
end

-- PERK: a happier (higher-tier) companion lets their needs rise more slowly.
-- Returns a multiplier (<=1) applied to need decay in Needs.Update.
function BanditsNPC.Affinity.NeedsFactor(brain)
    local t = BanditsNPC.Affinity.GetTier(brain)
    return ({ [0]=1.0, [1]=1.0, [2]=0.85, [3]=0.7, [4]=0.6 })[t] or 1.0
end

-- Fire a one-time line when a tier is reached (romance lines for opposite gender).
function BanditsNPC.Affinity.CheckMilestones(zombie, brain, prev, v)
    local function tierFor(x)
        local t = 0
        for i, minV in ipairs(BanditsNPC.Affinity.Tiers) do if x >= minV then t = i - 1 end end
        return t
    end
    local pt, nt = tierFor(prev), tierFor(v)
    if nt <= pt then return end

    local romance = BanditsNPC.Affinity.IsRomance(brain)
    local function say(txt, r, g, b)
        if zombie.addLineChatElement then pcall(function() zombie:addLineChatElement(txt, r or 0.9, g or 0.8, b or 0.6) end) end
    end

    if romance then
        if nt == 2 then say(BanditsNPC.T("UI_BN_Mile_Rom2", "\"...I'm really glad you found me.\""), 0.95, 0.6, 0.65)
        elseif nt == 3 then say(BanditsNPC.T("UI_BN_Mile_Rom3", "\"I feel safe with you. That's rare now.\""), 0.95, 0.55, 0.6)
        elseif nt == 4 then brain.partner = true; say(BanditsNPC.T("UI_BN_Mile_Rom4", "\"...I'm yours. For whatever's left of this world.\""), 0.95, 0.45, 0.6) end
    else
        if nt == 2 then say(BanditsNPC.T("UI_BN_Mile_Pla2", "\"You're alright. I trust you.\""), 0.6, 0.9, 0.6)
        elseif nt == 3 then say(BanditsNPC.T("UI_BN_Mile_Pla3", "\"I've got your back. Always.\""), 0.6, 0.9, 0.6)
        elseif nt == 4 then say(BanditsNPC.T("UI_BN_Mile_Pla4", "\"You're family now. I mean it.\""), 0.55, 0.95, 0.55) end
    end

    brain.affinityTier = nt
    BanditBrain.Update(zombie, brain)
    if Bandit and Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(zombie, { id = brain.id, affinityTier = nt, partner = brain.partner })
    end
end

function BanditsNPC.Affinity.Add(zombie, amount)
    local brain = BanditBrain.Get(zombie)
    if not brain then return end
    local mult = (BanditsNPC.Opt and BanditsNPC.Opt("AffinityMult", 1.0)) or 1.0
    local v = (brain.affinity or 0) + amount * mult
    if v < 0 then v = 0 end
    if v > 100 then v = 100 end
    local prev = brain.affinity or 0
    brain.affinity = v
    BanditBrain.Update(zombie, brain)
    if Bandit and Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(zombie, { id = brain.id, affinity = v })
    end
    -- fire tier / romance milestones on the way up
    if v > prev and BanditsNPC.Affinity.CheckMilestones then
        BanditsNPC.Affinity.CheckMilestones(zombie, brain, prev, v)
    end
end
