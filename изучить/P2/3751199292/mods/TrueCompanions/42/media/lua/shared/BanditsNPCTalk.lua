--
-- Bandits NPC - Companions Overhaul - Conversation (nested dialogue)
--
-- Dialogue is a tree: top-level CATEGORIES open into sub-questions (Fallout
-- style). Storage is a single flat list (so the editor stays simple); the tree
-- is rebuilt from each entry's `parent` at display time.
--
-- An option may be gated by affinity, by how long you've known them, by story
-- progress, or by a Fallout-style stat (reqPerk) / trait (reqTrait) CHECK shown
-- as a [requirement] prefix. Choosing a question raises/lowers affinity and gives
-- a reply; some reveal a piece of backstory (which then shows on the Story tab).
--
-- Already-asked questions go grey while on cooldown but stay clickable (you can
-- re-read the reply), and re-reading does NOT change affinity again.
--
-- Topic fields:
--   id, parent (category id | nil = top level), isCategory (true = a submenu),
--   text (the player's line / category label), response (string | {variants}),
--   affinity (delta), cooldown (game-hours), minAffinity, minDaysKnown,
--   affectionOnly (romance track only -- Affinity.IsRomance, which is opposite
--   gender by default but overridable per companion via brain.npcRomance),
--   platonicOnly (hidden once on the romance track -- the opt-in line),
--   setRomance (true/false: choosing this line switches the track),
--   needsUnrevealed (only if story remains), reveals (reveal a backstory part),
--   reqPerk = { perk="Strength", level=5 }, reqTrait = "TraitName"
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Talk = {}

BanditsNPC.Talk.Topics = {
    -- ===== top-level categories =====
    { id="about",   isCategory=true, text=BanditsNPC.T("UI_BN_Topic_about", "Tell me about yourself.") },
    { id="small",   isCategory=true, text=BanditsNPC.T("UI_BN_Topic_small", "Let's just talk.") },
    { id="special", isCategory=true, text=BanditsNPC.T("UI_BN_Topic_special", "[ Special ]") },

    -- ===== under "about" =====
    { id="past",    parent="about", text=BanditsNPC.T("UI_BN_Topic_past", "Where are you from?"),       affinity=1, cooldown=6, needsUnrevealed=true, reveals=true,
      response=BanditsNPC.T("UI_BN_TopicR_past", "\"Alright... I'll tell you a little.\"") },
    { id="checkin", parent="about", text=BanditsNPC.T("UI_BN_Topic_checkin", "How are you holding up?"),   affinity=1, cooldown=6,
      response={BanditsNPC.T("UI_BN_TopicR_checkin_1", "\"Getting by. Thanks for asking.\""), BanditsNPC.T("UI_BN_TopicR_checkin_2", "\"Tired. But still standing.\"")} },
    { id="deep",    parent="about", text=BanditsNPC.T("UI_BN_Topic_deep", "Do you think we'll make it?"), minAffinity=25, affinity=3, cooldown=24,
      response=BanditsNPC.T("UI_BN_TopicR_deep", "\"With you? Maybe. For the first time, I think maybe.\"") },

    -- ===== under "small" =====
    { id="compliment", parent="small", text=BanditsNPC.T("UI_BN_Topic_compliment", "You're good to have around."), affinity=2, cooldown=12,
      response=BanditsNPC.T("UI_BN_TopicR_compliment", "\"...thanks. That means a lot.\"") },
    { id="joke",       parent="small", text=BanditsNPC.T("UI_BN_Topic_joke", "Want to hear a bad joke?"),     affinity=1, cooldown=12,
      response=BanditsNPC.T("UI_BN_TopicR_joke", "\"Heh. That was awful. I needed it.\"") },
    { id="tease",      parent="small", text=BanditsNPC.T("UI_BN_Topic_tease", "You snore like a chainsaw, you know."), affinity=-1, cooldown=6,
      response=BanditsNPC.T("UI_BN_TopicR_tease", "\"...wow. Rude.\"") },
    { id="flirt",      parent="small", text=BanditsNPC.T("UI_BN_Topic_flirt", "I've grown fond of you."),      minAffinity=40, affectionOnly=true, affinity=4, cooldown=24,
      response=BanditsNPC.T("UI_BN_TopicR_flirt", "\"...I feel the same. Have for a while now.\"") },
    -- the romance OPT-IN: only visible while the relationship is platonic (any
    -- gender pairing), so same-gender companions can be courted too. Choosing it
    -- sets brain.npcRomance = true and the whole romance category unlocks.
    { id="rom_start",  parent="small", text=BanditsNPC.T("UI_BN_Topic_rom_start", "I'd like us to be more than friends."), minAffinity=40, platonicOnly=true, setRomance=true, affinity=3, cooldown=24,
      response=BanditsNPC.T("UI_BN_TopicR_rom_start", "\"...I was hoping you'd say that. Yes. Whatever this is -- let's find out together.\"") },

    -- ===== under "special" (stat / trait checks; modded traits work too) =====
    { id="intimidate", parent="special", text=BanditsNPC.T("UI_BN_Topic_intimidate", "You really don't want to test me."), reqPerk={perk="Strength", level=7}, affinity=-1, cooldown=18,
      response=BanditsNPC.T("UI_BN_TopicR_intimidate", "\"...okay. No need for that.\"") },
    { id="reassure",   parent="special", text=BanditsNPC.T("UI_BN_Topic_reassure", "We'll make it through this. I've faced worse."), reqTrait="Brave", affinity=2, cooldown=18,
      response=BanditsNPC.T("UI_BN_TopicR_reassure", "\"...you actually believe that. It helps.\"") },

    -- ===== sharp / rude lines (they push back) -- added under "small" =====
    { id="nag",   parent="small", text=BanditsNPC.T("UI_BN_Topic_nag", "You could carry more, you know."), affinity=-2, cooldown=12,
      response={BanditsNPC.T("UI_BN_TopicR_nag_1", "\"...wow. I'm doing my best out here, same as you.\""), BanditsNPC.T("UI_BN_TopicR_nag_2", "\"Charming. Want to trade packs and find out how heavy it is?\"")} },
    { id="boss",  parent="small", text=BanditsNPC.T("UI_BN_Topic_boss", "Just do what I say."), affinity=-2, cooldown=12,
      response=BanditsNPC.T("UI_BN_TopicR_boss", "\"I'm not your dog. Watch your tone with me.\"") },
    { id="doubt", parent="small", text=BanditsNPC.T("UI_BN_Topic_doubt", "You're not pulling your weight."), affinity=-3, cooldown=24,
      response=BanditsNPC.T("UI_BN_TopicR_doubt", "\"That's rich, coming from you. Fine. Whatever you say, boss.\"") },

    -- ===== local knowledge / rumors (real Knox County flavor) =====
    { id="info",  isCategory=true, text=BanditsNPC.T("UI_BN_Topic_info", "What do you know about the area?") },
    { id="rumor_mil",    parent="info", text=BanditsNPC.T("UI_BN_Topic_rumor_mil", "Heard anything about the military?"), affinity=1, cooldown=24,
      response={BanditsNPC.T("UI_BN_TopicR_rumor_mil_1", "\"The Knox facility up north was ground zero. Whole region got sealed off -- nobody who went to look came back.\""),
                BanditsNPC.T("UI_BN_TopicR_rumor_mil_2", "\"There was a checkpoint on the bridge to West Point. By the end they were turning survivors away at gunpoint.\"")},
      responseExp={
        Assasin=BanditsNPC.T("UI_BN_TopicRX_rumor_mil_Assasin", "\"I wore the uniform. The Knox facility wasn't a containment site -- it was a research lab. They knew weeks before they warned anyone. We were ordered to hold a line we were never told the truth about.\""),
        Recon=BanditsNPC.T("UI_BN_TopicRX_rumor_mil_Recon", "\"I scouted that perimeter before the fences went up. Heavy vehicles, comms towers, men who didn't answer questions. Whatever this is, it started behind those gates.\"")} },
    { id="rumor_safe",   parent="info", text=BanditsNPC.T("UI_BN_Topic_rumor_safe", "Are there any safe places left?"), minAffinity=20, affinity=1, cooldown=24,
      response={BanditsNPC.T("UI_BN_TopicR_rumor_safe_1", "\"People whispered about a prepper's bunker out in the forest. Could be a myth -- could be the last dry bed in the county.\""),
                BanditsNPC.T("UI_BN_TopicR_rumor_safe_2", "\"The Rosewood fire station had a solid setup before it got overrun. Might still be worth a look, if you're brave.\"")},
      responseExp={
        Recon=BanditsNPC.T("UI_BN_TopicRX_rumor_safe_Recon", "\"That forest bunker's no myth -- I've stood on its hatch. Concealed entry off an old logging trail northeast of the lake. Whether anyone's still home is another question.\""),
        Tracker=BanditsNPC.T("UI_BN_TopicRX_rumor_safe_Tracker", "\"Safest place is wherever the herds aren't, and that changes daily. But the deep woods past the ridge? They don't wander up there. I'd build there if I were settling down.\"")} },
    { id="rumor_loot",   parent="info", text=BanditsNPC.T("UI_BN_Topic_rumor_loot", "Where would you look for supplies?"), affinity=1, cooldown=18,
      response={BanditsNPC.T("UI_BN_TopicR_rumor_loot_1", "\"Warehouses down by the river. Everyone hit the pharmacies first -- nobody thinks about the loading docks.\""),
                BanditsNPC.T("UI_BN_TopicR_rumor_loot_2", "\"The gun store in town was stripped day one. But the houses behind it? People left in a hurry. Doors still unlocked.\"")} },
    { id="rumor_dark",   parent="info", text=BanditsNPC.T("UI_BN_Topic_rumor_dark", "What's the worst thing you've seen?"), minAffinity=30, affinity=2, cooldown=48,
      response={BanditsNPC.T("UI_BN_TopicR_rumor_dark_1", "\"A school. I won't describe it. Just... don't go to the one off the highway. Promise me.\""),
                BanditsNPC.T("UI_BN_TopicR_rumor_dark_2", "\"They move different at night. In packs. Almost like they remember how to hunt. I don't sleep much.\"")} },

    -- ===== teach me your trade (high trust; gated by their expertise) =====
    { id="learn", isCategory=true, text=BanditsNPC.T("UI_BN_Topic_learn", "Can you teach me something?") },
    { id="learn_fight", parent="learn", text=BanditsNPC.T("UI_BN_Topic_learn_fight", "Teach me to handle myself in a fight."), minAffinity=30, cooldown=99999,
      teach={kind="xp", perk="Aiming", amount=50},
      response=BanditsNPC.T("UI_BN_TopicR_learn_fight", "\"Alright. Breathe out when you swing, don't flail, and never back into a corner. Again.\"") },
    { id="learn_med",  parent="learn", text=BanditsNPC.T("UI_BN_Topic_learn_med", "Teach me first aid."), reqNpcExp="Medic", minAffinity=40, cooldown=99999,
      teach={kind="xp", perk="Doctor", amount=80},
      response=BanditsNPC.T("UI_BN_TopicR_learn_med", "\"Sit. First rule: stop the bleeding, then worry about the pain. Watch my hands.\"") },
    { id="learn_mech", parent="learn", text=BanditsNPC.T("UI_BN_Topic_learn_mech", "Show me how engines work."), reqNpcExp="Mechanic", minAffinity=40, cooldown=99999,
      teach={kind="xp", perk="Mechanics", amount=80},
      response=BanditsNPC.T("UI_BN_TopicR_learn_mech", "\"Pop the hood. A car's just a thousand small problems that trust each other. Here.\"") },
    { id="learn_elec", parent="learn", text=BanditsNPC.T("UI_BN_Topic_learn_elec", "Teach me about wiring."), reqNpcExp="Electrician", minAffinity=40, cooldown=99999,
      teach={kind="xp", perk="Electricity", amount=80},
      response=BanditsNPC.T("UI_BN_TopicR_learn_elec", "\"Rule one: assume every wire is live and wants you dead. Now, slowly...\"") },
    { id="learn_cook", parent="learn", text=BanditsNPC.T("UI_BN_Topic_learn_cook", "Teach me to cook properly."), reqNpcExp="Cook", minAffinity=35, cooldown=99999,
      teach={kind="xp", perk="Cooking", amount=70},
      response=BanditsNPC.T("UI_BN_TopicR_learn_cook", "\"Heat, timing, and never wasting a scrap. Pay attention.\"") },

    -- ===== romance arc (romance track -- opposite gender by default, any gender
    -- after the rom_start opt-in; escalates with affection) =====
    { id="romance", isCategory=true, affectionOnly=true, minAffinity=35, text=BanditsNPC.T("UI_BN_Topic_romance", "Can we talk about... us?") },
    { id="rom_notice", parent="romance", affectionOnly=true, minAffinity=35, affinity=3, cooldown=24,
      text=BanditsNPC.T("UI_BN_Topic_rom_notice", "I catch myself watching you sometimes."),
      response={BanditsNPC.T("UI_BN_TopicR_rom_notice_1", "\"...I've noticed. I don't mind. Bit of warmth in all this -- I'll take it.\""),
                BanditsNPC.T("UI_BN_TopicR_rom_notice_2", "\"Yeah? And here I thought I was being subtle about watching you back.\"")} },
    { id="rom_open", parent="romance", affectionOnly=true, minAffinity=50, affinity=4, cooldown=24,
      text=BanditsNPC.T("UI_BN_Topic_rom_open", "I don't want to pretend this is just survival."),
      response=BanditsNPC.T("UI_BN_TopicR_rom_open", "\"...then don't. I'm tired of pretending too. Whatever this is -- I want it.\"") },
    { id="rom_close", parent="romance", affectionOnly=true, minAffinity=65, affinity=4, cooldown=24,
      text=BanditsNPC.T("UI_BN_Topic_rom_close", "Come here."),
      response=BanditsNPC.T("UI_BN_TopicR_rom_close", "\"...about time. Don't let go, alright? Not tonight.\"") },
    { id="rom_love", parent="romance", affectionOnly=true, minAffinity=75, affinity=5, cooldown=36,
      text=BanditsNPC.T("UI_BN_Topic_rom_love", "I love you. I needed to say it out loud."),
      response=BanditsNPC.T("UI_BN_TopicR_rom_love", "\"...I love you too. Saying it feels dangerous, like it can be taken away. But it's true.\"") },
    { id="rom_fear", parent="romance", affectionOnly=true, minAffinity=80, affinity=2, cooldown=48,
      text=BanditsNPC.T("UI_BN_Topic_rom_fear", "Aren't you scared of losing me out there?"),
      response=BanditsNPC.T("UI_BN_TopicR_rom_fear", "\"Every single day. That's why I watch your back like I do. Don't you dare die on me.\"") },
    { id="rom_future", parent="romance", affectionOnly=true, minAffinity=90, affinity=3, cooldown=48,
      text=BanditsNPC.T("UI_BN_Topic_rom_future", "If the world ever settles... what would you want?"),
      response={BanditsNPC.T("UI_BN_TopicR_rom_future_1", "\"A house with a door that locks for the right reasons. A garden. You, complaining about my cooking. That's the whole dream.\""),
                BanditsNPC.T("UI_BN_TopicR_rom_future_2", "\"Quiet. Just quiet, and you next to me. I stopped wanting more than that a long time ago.\"")} },
    { id="rom_partner", parent="romance", affectionOnly=true, minAffinity=100, affinity=1, cooldown=72,
      text=BanditsNPC.T("UI_BN_Topic_rom_partner", "It's you and me. Whatever comes."),
      response=BanditsNPC.T("UI_BN_TopicR_rom_partner", "\"You and me. To the end of it. I'm not going anywhere -- you're stuck with me now.\"") },
    -- the romance OPT-OUT: steps back to friendship (clears partner status).
    -- rom_start reappears afterwards, so the choice is reversible both ways.
    { id="rom_stop", parent="romance", affectionOnly=true, setRomance=false, affinity=-3, cooldown=24,
      text=BanditsNPC.T("UI_BN_Topic_rom_stop", "I think we work better as friends."),
      response=BanditsNPC.T("UI_BN_TopicR_rom_stop", "\"...oh. Right. No, I get it. Friends, then -- I'd rather have that than lose you.\"") },

    -- ===== more Knox County rumors (real towns / locations) =====
    { id="rumor_loux", parent="info", text=BanditsNPC.T("UI_BN_Topic_rumor_loux", "What do you know about Louisville?"), minAffinity=15, affinity=1, cooldown=24,
      response={BanditsNPC.T("UI_BN_TopicR_rumor_loux_1", "\"The big city? They walled it and it didn't matter. Millions behind that concrete. If you go in, you don't come out -- but the mall never got picked clean...\""),
                BanditsNPC.T("UI_BN_TopicR_rumor_loux_2", "\"Louisville's a graveyard the size of a county. Army dropped the bridges to slow them. Whatever's behind the wall is still in there, pacing.\"")} },
    { id="rumor_muld", parent="info", text=BanditsNPC.T("UI_BN_Topic_rumor_muld", "Is Muldraugh worth a look?"), affinity=1, cooldown=24,
      response={BanditsNPC.T("UI_BN_TopicR_rumor_muld_1", "\"Muldraugh's a one-road town -- gas station, warehouses, a sawmill. The horde rolls down Main Street like a tide. Loot the edges, never the middle.\""),
                BanditsNPC.T("UI_BN_TopicR_rumor_muld_2", "\"There's a storage lot in Muldraugh nobody bothered with. Padlocks, mostly. If you've got a sledge and nerve, might be worth it.\"")} },
    { id="rumor_west", parent="info", text=BanditsNPC.T("UI_BN_Topic_rumor_west", "What about West Point?"), affinity=1, cooldown=24,
      response=BanditsNPC.T("UI_BN_TopicR_rumor_west", "\"West Point's got everything and ten thousand of them to guard it. The pharmacy and the gun store are gold -- if you can stand the noise you'll make getting out.\"") },
    { id="rumor_river", parent="info", text=BanditsNPC.T("UI_BN_Topic_rumor_river", "Heard anything about Riverside?"), affinity=1, cooldown=24,
      response=BanditsNPC.T("UI_BN_TopicR_rumor_river", "\"Riverside's quieter -- rich little river town. Big houses, full pantries, folks who locked up tight and never made it back. Watch the gated streets.\"") },
    { id="rumor_march", parent="info", text=BanditsNPC.T("UI_BN_Topic_rumor_march", "Any safe little towns?"), minAffinity=15, affinity=1, cooldown=24,
      response=BanditsNPC.T("UI_BN_TopicR_rumor_march", "\"March Ridge, maybe. Small, fenced. There were army trailers parked at the south end before the end -- supplies, if the looters left any.\"") },
    { id="rumor_farm", parent="info", text=BanditsNPC.T("UI_BN_Topic_rumor_farm", "Where's there still food growing?"), affinity=1, cooldown=18,
      response={BanditsNPC.T("UI_BN_TopicR_rumor_farm_1", "\"The farms out past the treeline. Animals gone feral, fields gone wild, but a smart forager eats like a king out there.\""),
                BanditsNPC.T("UI_BN_TopicR_rumor_farm_2", "\"McCoy land had logging cabins with stocked larders. Far from the roads. Far from help, too.\"")},
      responseExp={
        Trapper=BanditsNPC.T("UI_BN_TopicRX_rumor_farm_Trapper", "\"You don't need a farm -- you need a snare line and patience. Set traps along the game trails by the creek and you'll never go hungry. I'll show you the spots.\""),
        Cook=BanditsNPC.T("UI_BN_TopicRX_rumor_farm_Cook", "\"Forget growing it -- it's about not wasting it. A root cellar, some salt, and I can stretch one deer through a whole winter. Find the food; I'll make it last.\""),
        Tracker=BanditsNPC.T("UI_BN_TopicRX_rumor_farm_Tracker", "\"Follow the deer. Where they graze, the land's still good. There's an orchard gone wild south of the farms -- apples for the taking if the bears don't beat you to it.\"")} },
    { id="rumor_signal", parent="info", text=BanditsNPC.T("UI_BN_Topic_rumor_signal", "Has anyone heard a radio broadcast?"), minAffinity=25, affinity=2, cooldown=48,
      response={BanditsNPC.T("UI_BN_TopicR_rumor_signal_1", "\"A looping emergency message, mostly. But one night I swear I heard a real voice -- a woman, giving coordinates. Then static. I've wondered ever since.\""),
                BanditsNPC.T("UI_BN_TopicR_rumor_signal_2", "\"The official channels went dead weeks ago. Anything you hear now is either a recording... or someone who shouldn't still be out there talking.\"")} },

    -- ===== profession banter (only shows with that trade's NPC) =====
    { id="ban_mech",  parent="small", reqNpcExp="Mechanic", text=BanditsNPC.T("UI_BN_Topic_ban_mech", "How's my driving?"), affinity=1, cooldown=18,
      response={BanditsNPC.T("UI_BN_TopicR_ban_mech_1", "\"You treat the clutch like it owes you money. But you keep us moving, so I'll allow it.\""),
                BanditsNPC.T("UI_BN_TopicR_ban_mech_2", "\"Every time you grind the gears, a little piece of me dies. Otherwise? Not bad.\"")} },
    { id="ban_med",   parent="small", reqNpcExp="Medic", text=BanditsNPC.T("UI_BN_Topic_ban_med", "Quit fussing, I'm fine."), affinity=1, cooldown=12,
      response={BanditsNPC.T("UI_BN_TopicR_ban_med_1", "\"That scratch says otherwise. Sit. Let me look before it turns -- they ALL turn now.\""),
                BanditsNPC.T("UI_BN_TopicR_ban_med_2", "\"'Fine' is what people say right before they spike a fever. Humor me and roll up your sleeve.\"")} },
    { id="ban_elec",  parent="small", reqNpcExp="Electrician", text=BanditsNPC.T("UI_BN_Topic_ban_elec", "Think you could get us some power?"), affinity=1, cooldown=18,
      response=BanditsNPC.T("UI_BN_TopicR_ban_elec", "\"Give me a generator, some fuel, and ten minutes where nothing's trying to eat me. Hot showers again -- imagine that.\"") },
    { id="ban_cook",  parent="small", reqNpcExp="Cook", text=BanditsNPC.T("UI_BN_Topic_ban_cook", "What I'd give for a real meal."), affinity=1, cooldown=18,
      response=BanditsNPC.T("UI_BN_TopicR_ban_cook", "\"Find me an onion and a little heat and I'll make you forget it's the end of the world. For one plate, anyway.\"") },
    { id="ban_recon", parent="small", reqNpcExp="Recon", text=BanditsNPC.T("UI_BN_Topic_ban_recon", "You always know which way's north."), affinity=1, cooldown=24,
      response=BanditsNPC.T("UI_BN_TopicR_ban_recon", "\"Spent years where the only map was the sky and the moss on the trees. Towns turn me around. The wild never does.\"") },
    { id="ban_trap",  parent="small", reqNpcExp="Trapper", text=BanditsNPC.T("UI_BN_Topic_ban_trap", "Catch anything good lately?"), affinity=1, cooldown=24,
      response=BanditsNPC.T("UI_BN_TopicR_ban_trap", "\"Rabbit, mostly. Fat one yesterday. Out here a snare line beats a gun -- quiet, and it works while you sleep.\"") },
    { id="ban_thief", parent="small", reqNpcExp="Thief", text=BanditsNPC.T("UI_BN_Topic_ban_thief", "Should I be counting my supplies?"), affinity=-1, cooldown=24,
      response=BanditsNPC.T("UI_BN_TopicR_ban_thief", "\"Ha. Relax. Old habits, sure -- but I don't steal from the people who watch my back. ...Probably.\"") },
    { id="ban_sold",  parent="small", reqNpcExp="Assasin", text=BanditsNPC.T("UI_BN_Topic_ban_sold", "You never talk about before."), affinity=0, cooldown=48,
      response=BanditsNPC.T("UI_BN_TopicR_ban_sold", "\"No. I don't. Some doors you don't open out here. Just know I'm good in a fight, and leave it at that.\"") },

    -- ===== jealousy / conflict (only if you're romancing more than one) =====
    { id="jeal_confront", parent="romance", affectionOnly=true, reqRival=true, minAffinity=50, affinity=0, cooldown=48,
      text=BanditsNPC.T("UI_BN_Topic_jeal_confront", "I know there's someone else, isn't there?"),
      response={BanditsNPC.T("UI_BN_TopicR_jeal_confront_1", "\"...you didn't deny it. I don't know whether to be grateful you're honest or furious you did it. Both, probably.\""),
                BanditsNPC.T("UI_BN_TopicR_jeal_confront_2", "\"I won't ask you to choose -- the world's too small and cruel for that. Just don't make me feel like a fool.\"")} },
    { id="jeal_reassure", parent="romance", affectionOnly=true, reqRival=true, minAffinity=50, affinity=2, cooldown=48,
      text=BanditsNPC.T("UI_BN_Topic_jeal_reassure", "You're the one I trust most. That's the truth."),
      response=BanditsNPC.T("UI_BN_TopicR_jeal_reassure", "\"...words are cheap, but I want to believe you. So don't make a liar of me. Please.\"") },
    { id="jeal_cold", parent="romance", affectionOnly=true, reqRival=true, minAffinity=50, affinity=-4, cooldown=48,
      text=BanditsNPC.T("UI_BN_Topic_jeal_cold", "Don't be so clingy."),
      response=BanditsNPC.T("UI_BN_TopicR_jeal_cold", "\"...wow. Noted. I'll keep my feelings to myself from now on, then. Lesson learned.\"") },
}

-- Children of a category (parentId nil/"" => top level), in list order.
function BanditsNPC.Talk.GetChildren(parentId)
    local out = {}
    for _, t in ipairs(BanditsNPC.Talk.Topics) do
        local p = t.parent
        if (not parentId and (p == nil or p == "")) or (parentId and p == parentId) then
            table.insert(out, t)
        end
    end
    return out
end

function BanditsNPC.Talk.FindById(id)
    if not id then return nil end
    for _, t in ipairs(BanditsNPC.Talk.Topics) do if t.id == id then return t end end
    return nil
end

-- Records when you first met them (for "minDaysKnown" gating).
function BanditsNPC.Talk.EnsureMet(zombie)
    local brain = BanditBrain.Get(zombie)
    if not brain then return end
    if not brain.metHour then
        brain.metHour = getGameTime():getWorldAgeHours()
        BanditBrain.Update(zombie, brain)
        if Bandit.ForceSyncPart then Bandit.ForceSyncPart(zombie, { id = brain.id, metHour = brain.metHour }) end
    end
end

-- Robust checks. We check that each method EXISTS (field access is nil if not)
-- before calling it, so a missing Java method is never invoked (calling a nil
-- Java method pops PZ's error box even inside pcall). If we can't determine the
-- requirement, we DON'T gate (return true) rather than crash.
local function meetsPerk(player, rp)
    if not (rp and rp.perk and rp.perk ~= "") then return true end
    if not player.getPerkLevel then return true end
    local ok, lvl = pcall(function()
        local p = Perks[rp.perk]
        if not p then return 999 end           -- unknown perk name -> don't gate
        return player:getPerkLevel(p)
    end)
    if not ok then return true end
    return (lvl or 0) >= (rp.level or 1)
end

-- Resolve a stored trait NAME to the engine's CharacterTrait constant. B42's
-- constants are UPPER_SNAKE_CASE ("Brave" -> BRAVE, "FastLearner" -> FAST_LEARNER);
-- vanilla only ever writes them literally (playerObj:hasTrait(CharacterTrait.ILLITERATE),
-- ISCraftingUI.lua:15) and the class exposes no name-lookup helper, so we map it here.
-- Returns nil for anything the engine doesn't define (modded traits live outside
-- CharacterTrait) -- callers must treat nil as "can't evaluate", never as "absent".
local function traitConst(name)
    if not CharacterTrait then return nil end
    local direct = name:upper()
    if CharacterTrait[direct] ~= nil then return CharacterTrait[direct] end
    local snake = name:gsub("(%l)(%u)", "%1_%2"):upper()
    if CharacterTrait[snake] ~= nil then return CharacterTrait[snake] end
    return nil
end

-- TRAIT CHECK (rewritten for B42.20 -- the old version never gated anything).
-- IsoGameCharacter exposes ONLY hasTrait(CharacterTrait): there is no getTraits() and
-- no HasTrait(string) on the character in 42.20 (verified against projectzomboid.jar;
-- CharacterTraitDefinition.getTraits() is an unrelated static on another class). So both
-- old probes failed their existence guards and every call fell through to the
-- "can't determine -> don't gate" return -- i.e. reqTrait topics showed for everyone.
-- Same failure policy as before, deliberately: an UNKNOWN trait name still fails OPEN,
-- because silently hiding a line forever is worse than showing it.
local function meetsTrait(player, name)
    if not (name and name ~= "") then return true end
    if not player.hasTrait then return true end   -- can't determine -> don't gate
    local ok, res = pcall(function()
        local c = traitConst(name)
        if c == nil then return nil end           -- unknown/modded trait -> don't gate
        return player:hasTrait(c) and true or false
    end)
    if not ok or res == nil then return true end
    return res
end

-- Returns state, reason (reason is plain text; the UI brackets/colours it):
--   "available"  -> normal, clickable, full effect
--   "asked"      -> grey, still clickable (re-read only, no affinity)
--   "locked"     -> NOT clickable, shows the [requirement] reason
--   "hidden"     -> not shown at all
-- True if the NPC has the named Bandit expertise (for "teach me X" options).
local function npcHasExp(brain, expName)
    local id = Bandit and Bandit.Expertise and Bandit.Expertise[expName]
    if not id or not brain.exp then return false end
    for _, e in ipairs(brain.exp) do if e == id then return true end end
    return false
end

-- True if the player has ANOTHER recruited companion on the romance track at
-- smitten+ affinity (i.e. a romantic rival to this one). Drives jealousy lines.
-- Romance-track test (not raw gender) so opted-in same-gender loves count too.
function BanditsNPC.Talk.HasRivalRomance(brain)
    local player = getSpecificPlayer(0)
    if not player then return false end
    local cell = getCell()
    local zl = cell and cell:getZombieList()
    if not zl then return false end
    for i = 0, zl:size() - 1 do
        local z = zl:get(i)
        if z and z:isAlive() and z.getVariableBoolean and z:getVariableBoolean("Bandit") then
            local b = BanditBrain and BanditBrain.Get(z)
            -- BanditsNPC.IsOwnedBy, not `b.master == pid`: the raw id is a reused
            -- per-connection number in MP, so comparing it made another player's
            -- companion count as one of yours (audit BLOCKER 2).
            if b and b.recruited and BanditsNPC.IsOwnedBy(b, player) and b.id ~= brain.id then
                local rom = BanditsNPC.Affinity and BanditsNPC.Affinity.IsRomance
                    and BanditsNPC.Affinity.IsRomance(b)
                if rom and (b.affinity or 0) >= 50 then return true end
            end
        end
    end
    return false
end

function BanditsNPC.Talk.GetState(brain, topic)
    if not brain then return "hidden" end

    -- only show "teach me X" lines the NPC could actually teach
    if topic.reqNpcExp and not npcHasExp(brain, topic.reqNpcExp) then return "hidden" end

    -- jealousy lines only when there's actually another love interest
    if topic.reqRival and not BanditsNPC.Talk.HasRivalRomance(brain) then return "hidden" end

    -- romance gating goes through Affinity.IsRomance (gender default, overridable
    -- per companion via brain.npcRomance) so same-gender romance works too
    local onRomanceTrack = BanditsNPC.Affinity and BanditsNPC.Affinity.IsRomance
        and BanditsNPC.Affinity.IsRomance(brain)
    if topic.affectionOnly and not onRomanceTrack then return "hidden" end
    if topic.platonicOnly and onRomanceTrack then return "hidden" end

    if topic.needsUnrevealed then
        local total = brain.storyParts and #brain.storyParts or 0
        if (brain.storyRevealed or 0) >= total then return "hidden" end
    end

    local aff = brain.affinity or 0
    if topic.minAffinity and aff < topic.minAffinity then
        return "locked", BanditsNPC.T("UI_BN_Lock_MoreTrust", "More Trust")
    end

    local now = getGameTime():getWorldAgeHours()
    if topic.minDaysKnown then
        local days = (now - (brain.metHour or now)) / 24
        if days < topic.minDaysKnown then return "locked", BanditsNPC.T("UI_BN_Lock_Patience", "Patience") end
    end

    -- stat / trait checks (Fallout-style). Works with modded traits too.
    local player = getSpecificPlayer(0)
    if player and topic.reqPerk and not meetsPerk(player, topic.reqPerk) then
        return "locked", topic.reqPerk.perk .. " " .. (topic.reqPerk.level or 1)
    end
    if player and topic.reqTrait and not meetsTrait(player, topic.reqTrait) then
        return "locked", topic.reqTrait
    end

    -- categories never go on cooldown
    if topic.isCategory then return "available" end

    -- already asked at least once -> grey, but still clickable (re-reads the
    -- reply; it only changes affinity again once the cooldown has elapsed, which
    -- Talk.Do enforces).
    if brain.talkUsed and brain.talkUsed[topic.id] then return "asked" end

    return "available"
end

local function pickVariant(resp)
    if type(resp) == "table" then return resp[ZombRand(#resp) + 1] end
    return resp
end

-- The reply text, with no side effects. If the topic has profession-specific
-- answers (responseExp = { ExpName = reply | {variants} }) and the NPC has that
-- expertise, that answer is used; otherwise the generic response.
function BanditsNPC.Talk.GetResponse(topic, brain)
    if brain and topic.responseExp and brain.exp and Bandit and Bandit.Expertise then
        for _, e in ipairs(brain.exp) do
            for expName, resp in pairs(topic.responseExp) do
                if Bandit.Expertise[expName] == e then
                    local r = pickVariant(resp)
                    if r and r ~= "" then return r end
                end
            end
        end
    end
    return pickVariant(topic.response) or "\"...\""
end

-- Grants the player a skill XP / recipe / trait when a "teach" topic is chosen.
-- All calls are pcall-guarded so a bad perk/recipe name can't crash the dialogue.
function BanditsNPC.Talk.GrantTeach(teach)
    local player = getSpecificPlayer(0)
    if not (player and teach) then return end
    if teach.kind == "xp" and teach.perk then
        pcall(function()
            local p = Perks[teach.perk]
            if p then player:getXp():AddXP(p, teach.amount or 25) end
        end)
    elseif teach.kind == "trait" and teach.trait then
        -- same 42.20 correction as meetsTrait: there is no character getTraits(). The
        -- proven runtime add is getCharacterTraits():add(CharacterTrait.X)
        -- (vanilla FenrisScenario.lua:294), and it takes the CONSTANT, not a string.
        -- No shipped topic uses teach=trait, but the in-game editor lets authors write
        -- one -- it used to fail silently inside the pcall.
        pcall(function()
            local c = traitConst(teach.trait)
            if c ~= nil and player.getCharacterTraits then
                player:getCharacterTraits():add(c)
            else
                print("[BanditsNPC] teach trait ignored, unknown CharacterTrait: " .. tostring(teach.trait))
            end
        end)
    elseif teach.kind == "recipe" and teach.recipe then
        pcall(function() if player.learnRecipe then player:learnRecipe(teach.recipe) end end)
    end
end

-- Applies a topic. The first time (or after cooldown) it changes affinity,
-- stamps the cooldown and may reveal backstory ("fresh"); re-clicking a recently
-- asked question just re-reads the reply with no side effects.
-- Returns reply, fresh, revealedStorySentence(or nil).
function BanditsNPC.Talk.Do(zombie, topic)
    local brain = BanditBrain.Get(zombie)
    if not brain then return "", false end

    local now = getGameTime():getWorldAgeHours()
    local used = brain.talkUsed and brain.talkUsed[topic.id]
    local fresh = (not used) or (not topic.cooldown) or ((now - used) >= topic.cooldown)
    local revealed = nil

    -- track switch applies on EVERY click (not just when fresh): re-choosing the
    -- opt-in/opt-out inside its cooldown must still flip the track, or the click
    -- would show the reply and silently do nothing. partner=false (not nil) so
    -- the cleared flag still syncs in MP.
    if topic.setRomance ~= nil and brain.npcRomance ~= topic.setRomance then
        brain.npcRomance = topic.setRomance
        if topic.setRomance == false then brain.partner = false end
        BanditBrain.Update(zombie, brain)
        if Bandit.ForceSyncPart then
            Bandit.ForceSyncPart(zombie, { id = brain.id, npcRomance = brain.npcRomance, partner = brain.partner })
        end
    end

    -- A TOPIC PAYS AFFINITY ONCE, not once per cooldown.
    --
    -- There are 21 topics worth 41 points between them, every one of them repeatable
    -- on a cooldown -- so a player who sat and re-asked the same questions could reach
    -- 100 trust in a few in-game hours, which is the reported "he already had 100%
    -- trust with them before any quest was even given". Asking someone the same
    -- question a second time does not deepen a friendship.
    --
    -- The conversation itself is unchanged: the topic still opens on its cooldown and
    -- still gives its reply. Only the payment is first-time-only, which leaves talking
    -- worth about 41 of the 100 and makes quests the way to the rest -- which is what
    -- quests are for.
    local firstTime = (used == nil)

    if fresh then
        brain.talkUsed = brain.talkUsed or {}
        brain.talkUsed[topic.id] = now

        if firstTime and topic.affinity and topic.affinity ~= 0 and BanditsNPC.Affinity then
            BanditsNPC.Affinity.Add(zombie, topic.affinity)
        end
        if topic.reveals and BanditsNPC.Backstory then
            revealed = BanditsNPC.Backstory.Reveal(zombie)   -- also updates the Story tab
        end
        -- FIRST TIME ONLY, like the affinity above it (v0.75.44). This was gated on
        -- `fresh` -- once per COOLDOWN -- while the affinity payment two branches up is
        -- gated on `firstTime` (once EVER). The comment already said "once"; only the
        -- affinity actually was. No shipped topic exploits the gap: all five teach entries
        -- grant a recipe or a trait, and both of those are idempotent -- you cannot learn
        -- a recipe twice. But `teach.kind == "xp"` calls AddXP unconditionally, so the
        -- first XP-teaching topic anyone adds (in this file OR in the in-game editor,
        -- which can set teach freely) would be farmable on a timer. Closing it now costs
        -- one condition; discovering it later costs a save.
        if topic.teach and firstTime then
            BanditsNPC.Talk.GrantTeach(topic.teach)          -- grant the player a skill/recipe/trait once
        end

        BanditBrain.Update(zombie, brain)
        if Bandit.ForceSyncPart then Bandit.ForceSyncPart(zombie, { id = brain.id, talkUsed = brain.talkUsed }) end
    end

    return BanditsNPC.Talk.GetResponse(topic, brain), fresh, revealed
end
