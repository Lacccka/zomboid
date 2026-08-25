--
-- Bandits NPC - Companions Overhaul - Quests
--
-- A recruited companion offers a quest after a delay. Quests are PERSONAL where
-- possible -- tied to the NPC's personality (a smoker wants cigarettes; someone
-- who misses gaming wants a disc) or their profession (a medic's grim research) --
-- and reward affinity, items, and/or skill XP. Some are gated by the NPC's
-- expertise/personality or by how much they trust you.
--
-- Template fields:
--   type ("fetch" | "hunt"), itemType, name, min, max,
--   itemTypes = {..} -- OPTIONAL list of fullTypes that ALL satisfy the quest. Quests
--     that ask for a CATEGORY ("cans of soda", "something strong", "any magazines")
--     must list every matching vanilla variant: accepting only ONE exact fullType made
--     turn-ins fail with a bag full of visually-identical items (player report 11 Jul
--     -- "can not turn in items that were asked for"). Ids verified vs B42 scripts.
--   desc (use {count}/{item}), success (completion line),
--   reward (affinity delta), rewardItem = {type, count}, rewardXp = {perk, amount},
--   npcExp (only if NPC has this expertise), reqPersonality (brain.personality flag),
--   minAffinity (only once trust is high enough).
--
-- Turn-in counts the PLAYER's inventory AND the companion's own bag: players put the
-- fetched items into her inventory expecting that to work (same report), so it does.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Quests = {}

-- Each template carries a stable `tkey`: the quest's desc / success / name are
-- translated at generation via UI_BN_Quest_<tkey>_desc / _success / _name, with
-- the English literals below as the built-in fallback. (Quests regenerate every
-- 1-12 days, so translating at generation is fine; the literal stays the fallback.)
BanditsNPC.Quests.Templates = {
    -- ===== personal: nostalgia / coping (anyone) =====
    { type="fetch", tkey="soda", itemType="Base.Pop", name="cans of soda", min=2, max=4, reward=10,
      itemTypes={"Base.Pop", "Base.Pop2", "Base.Pop3", "Base.PopBottle", "Base.SodaCan"},
      desc="Bit silly, but... could you find me {count} cans of soda? I just miss the taste. Normal things, you know?",
      success="\"...ice cold would've been perfect. Thank you. Really.\"" },
    { type="fetch", tkey="doll", itemType="Base.Doll", name="a child's doll", min=1, max=1, reward=16, minAffinity=20,
      desc="If you ever come across a little doll out there... I lost my daughter's when we ran. I'd just like to hold one again.",
      success="\"...yeah. That's the one. Thank you. I won't forget this.\"" },

    -- ===== personal: gated by personality =====
    { type="fetch", tkey="cigs", itemType="Base.CigarettePack", name="packs of cigarettes", min=2, max=4, reward=10, reqPersonality="smoker",
      desc="I'd trade an arm for a smoke. Bring me {count} packs and I'm yours.",
      success="\"Oh thank god. Don't lecture me.\"" },
    { type="fetch", tkey="drink", itemType="Base.Whiskey", name="a bottle of something strong", min=1, max=1, reward=10, reqPersonality="alcoholic",
      itemTypes={"Base.Whiskey", "Base.Vodka", "Base.Rum", "Base.Tequila"},
      desc="Find me a bottle of something strong. Just one. Helps me sleep without the dreams.",
      success="\"...I'll make it last. Probably won't. Thanks.\"" },
    { type="fetch", tkey="games", itemType="Base.VideoGame", name="old video games", min=1, max=2, reward=12, reqPersonality="gameCollector",
      desc="Stupid thing to miss, but I'd kill to play something again. Find me {count} old video games? Even if there's no power to run them yet.",
      success="\"Ha! Look at that. Maybe one day we hook up a generator and a TV. Deal?\"" },
    { type="fetch", tkey="mags", itemType="Base.Magazine", name="any magazines or comics", min=2, max=5, reward=10, reqPersonality="comicsCollector",
      itemTypes={"Base.Magazine", "Base.ComicBook", "Base.HottieZ"},
      desc="Anything to read would help. Bring me {count} magazines or comics and I'll be a happy idiot.",
      success="\"You're a lifesaver. Don't spoil the endings.\"" },

    -- ===== profession: gated by NPC expertise, teaches XP / gives gear =====
    { type="fetch", tkey="antibiotics", itemType="Base.Antibiotics", name="antibiotics", min=2, max=4, reward=14, npcExp="Medic", minAffinity=15,
      rewardXp={perk="Doctor", amount=30},
      desc="I want to understand how this thing really spreads. Bring me {count} courses of antibiotics for the research -- and I'll teach you a thing or two about patching people up.",
      success="\"Fascinating, and horrifying. Here -- let me show you what I've learned.\"" },
    { type="fetch", tkey="scalpel", itemType="Base.Scalpel", name="a scalpel", min=1, max=1, reward=16, npcExp="Medic", minAffinity=35,
      rewardXp={perk="Doctor", amount=60},
      desc="For the next stage I need a proper scalpel. What I'm studying... it's not pretty. But it might matter. Help me and I'll pass on what I know.",
      success="\"...the things in their blood shouldn't move like that. Anyway. You earned this knowledge.\"" },
    { type="fetch", tkey="escrap", itemType="Base.ElectronicsScrap", name="electronic scrap", min=4, max=8, reward=12, npcExp="Electrician",
      rewardXp={perk="Electricity", amount=30},
      desc="Get me {count} bits of electronic scrap and I'll show you how to keep the lights on after everyone else is in the dark.",
      success="\"See? Power's just patience and not electrocuting yourself. Your turn.\"" },
    { type="fetch", tkey="screwdrivers", itemType="Base.Screwdriver", name="screwdrivers", min=1, max=2, reward=12, npcExp="Mechanic",
      rewardXp={perk="Mechanics", amount=30},
      desc="Bring me {count} screwdrivers and I'll teach you how to keep a car breathing. We don't get far on foot.",
      success="\"Engines don't lie to you. People do. Here, this'll help you.\"" },
    { type="fetch", tkey="saucepan", itemType="Base.Saucepan", name="a saucepan", min=1, max=1, reward=12, npcExp="Cook",
      rewardItem={type="Base.Pot", count=1}, rewardXp={perk="Cooking", amount=25},
      desc="Find me a saucepan and I'll turn our sad cans into something that tastes like a memory. I'll teach you, too.",
      success="\"A hot meal changes everything out here. Take this pot -- and what I showed you.\"" },

    -- ===== lore / scary =====
    { type="fetch", tkey="fuel", itemType="Base.PetrolCan", name="a can of fuel", min=1, max=1, reward=14, minAffinity=25,
      rewardItem={type="Base.Torch", count=1},
      desc="There's a place I keep dreaming about -- the dark down past the warehouses. I'm not ready to go back. Bring me a can of fuel and I'll tell you what I saw there. Take this torch; you'll want it.",
      success="\"...the doors were chained from the inside. Whatever did that wasn't trying to keep things OUT.\"" },

    -- ===== combat =====
    { type="hunt", tkey="hunt", name="zombies", min=10, max=25, reward=18,
      desc="I keep hearing them at night. Thin the herd for me -- {count} of them -- and I'll sleep a little easier.",
      success="\"Quieter now. Thank you. Get some rest.\"" },
}

local function randomDelayHours()
    return (1 + ZombRand(12)) * 24      -- 1..12 days
end

local function playerKills(player)
    local ok, n = pcall(function() return player:getZombieKills() end)
    return (ok and n) or 0
end

-- accepted-fullType set for a quest: new quests carry q.itemTypes (variant list);
-- quests saved by older versions only have the single q.itemType -- support both.
local function questTypeSet(q)
    local set = {}
    if type(q.itemTypes) == "table" then
        for _, t in ipairs(q.itemTypes) do set[t] = true end
    end
    if q.itemType then set[q.itemType] = true end
    return set
end

local function countItemsIn(character, typeSet)
    local n = 0
    pcall(function()
        local list = ArrayList.new()
        character:getInventory():getAllEvalRecurse(function(it) return typeSet[it:getFullType()] == true end, list)
        n = list:size()
    end)
    return n
end

-- fetch-quest count = player's inventory + the companion's own bag (players stash the
-- fetched items on her and expect that to count -- see header note)
local function countItems(player, zombie, typeSet)
    local n = countItemsIn(player, typeSet)
    if zombie then n = n + countItemsIn(zombie, typeSet) end
    return n
end

function BanditsNPC.Quests.removeItems(character, typeSet, count)
    local removed = 0
    pcall(function()
        local list = ArrayList.new()
        character:getInventory():getAllEvalRecurse(function(it) return typeSet[it:getFullType()] == true end, list)
        for i = 0, list:size() - 1 do
            if removed >= count then break end
            local item = list:get(i)
            local c = item:getContainer()
            if c then c:Remove(item); removed = removed + 1 end
        end
    end)
    return removed
end

local function hasExp(brain, expName)
    if not expName then return true end
    local id = Bandit and Bandit.Expertise and Bandit.Expertise[expName]
    if not id then return false end
    if brain.exp then for _, e in ipairs(brain.exp) do if e == id then return true end end end
    return false
end

local function eligible(brain, t)
    if t.npcExp and not hasExp(brain, t.npcExp) then return false end
    if t.reqPersonality and not (brain.personality and brain.personality[t.reqPersonality]) then return false end
    if t.minAffinity and (brain.affinity or 0) < t.minAffinity then return false end
    return true
end

function BanditsNPC.Quests.Generate(brain)
    local player = getSpecificPlayer(0)
    local all = BanditsNPC.Quests.Templates or {}
    local pool = {}
    for _, t in ipairs(all) do if eligible(brain, t) then table.insert(pool, t) end end
    if #pool == 0 then
        pool = { { type="fetch", tkey="soup", itemType="Base.TinnedSoup", name="cans of soup", min=2, max=4, reward=8,
                   desc="Food's been thin. Could you bring me {count} cans of soup?" } }
    end

    local t = pool[ZombRand(#pool) + 1]
    local lo = t.min or 1
    local count = lo + ZombRand(((t.max or lo) - lo) + 1)
    -- translate template text via its tkey (English literals are the fallback)
    local kstem = "UI_BN_Quest_" .. (t.tkey or "x")
    local name = t.name and BanditsNPC.T(kstem .. "_name", t.name) or t.name
    local desc = t.desc and BanditsNPC.T(kstem .. "_desc", t.desc)
        or (t.type == "hunt" and BanditsNPC.T("UI_BN_Quest_generic_hunt", "Thin the herd -- take down {count} zombies.")
            or BanditsNPC.TF("UI_BN_Quest_generic_fetch", "Bring me {count} %1.", name or BanditsNPC.T("UI_BN_Quest_items", "items")))
    desc = desc:gsub("{count}", tostring(count)):gsub("{item}", name or BanditsNPC.T("UI_BN_Quest_items", "items"))
    local success = t.success and BanditsNPC.T(kstem .. "_success", t.success) or t.success

    local q = {
        type = t.type or "fetch", itemType = t.itemType, itemTypes = t.itemTypes, count = count,
        reward = t.reward or 8, desc = desc, success = success,
        rewardItem = t.rewardItem, rewardXp = t.rewardXp,
    }
    if q.type == "hunt" then q.startKills = playerKills(player) end
    return q
end

local function isCompanionOf(brain, player)
    -- IsMine matches by id OR stable username -- the raw-id compare alone broke after
    -- an MP reconnect reassigned online ids (owner rule: never trust the raw id)
    if BanditsNPC.IsMine then return BanditsNPC.IsMine(brain) end
    return brain.recruited and brain.master and brain.master == BanditUtils.GetCharacterID(player)
end

function BanditsNPC.Quests.Ensure(zombie)
    local brain = BanditBrain.Get(zombie)
    if not brain then return nil end
    local player = getSpecificPlayer(0)
    if not player or not isCompanionOf(brain, player) then return nil end

    if brain.quest then return brain.quest end

    local nowH = getGameTime():getWorldAgeHours()
    if not brain.questUnlockHour then
        brain.questUnlockHour = nowH + randomDelayHours()
        BanditBrain.Update(zombie, brain)
        if Bandit.ForceSyncPart then Bandit.ForceSyncPart(zombie, { id = brain.id, questUnlockHour = brain.questUnlockHour }) end
        return nil
    end
    if nowH < brain.questUnlockHour then return nil end

    local quest = BanditsNPC.Quests.Generate(brain)
    brain.quest = quest
    BanditBrain.Update(zombie, brain)
    if Bandit.ForceSyncPart then Bandit.ForceSyncPart(zombie, { id = brain.id, quest = quest }) end
    return quest
end

function BanditsNPC.Quests.Progress(brain, player, zombie)
    local q = brain and brain.quest
    if not q or q == false then return 0, 0 end
    if q.type == "fetch" then
        return math.min(q.count, countItems(player, zombie, questTypeSet(q))), q.count
    elseif q.type == "hunt" then
        return math.min(q.count, playerKills(player) - (q.startKills or 0)), q.count
    end
    return 0, 0
end

function BanditsNPC.Quests.CanComplete(brain, player, zombie)
    local cur, total = BanditsNPC.Quests.Progress(brain, player, zombie)
    return total > 0 and cur >= total
end

-- Returns success(boolean), message(string).
--
-- OWNER ONLY. Quest GENERATION was already gated (Ensure:203 -> isCompanionOf -> IsMine)
-- but completion was not, so another player could turn in your companion's quest --
-- spending their own items at :252, then clearing your quest and taking the reward.
-- Gated at one end of the lifecycle and not the other (audit Cluster A).
function BanditsNPC.Quests.Complete(zombie)
    if not BanditsNPC.MayCommand(zombie) then return false, "" end
    local brain = BanditBrain.Get(zombie)
    local player = getSpecificPlayer(0)
    if not (brain and player and brain.quest and brain.quest ~= false) then return false, "" end
    if not BanditsNPC.Quests.CanComplete(brain, player, zombie) then
        return false, "\"You're not done yet.\""
    end

    local q = brain.quest
    if q.type == "fetch" then
        -- take from the player's bags first, then whatever is stashed on her
        local typeSet = questTypeSet(q)
        local removed = BanditsNPC.Quests.removeItems(player, typeSet, q.count)
        if removed < q.count then
            BanditsNPC.Quests.removeItems(zombie, typeSet, q.count - removed)
        end
    end

    -- rewards
    if q.reward and q.reward ~= 0 then BanditsNPC.Affinity.Add(zombie, q.reward) end
    if q.rewardItem and q.rewardItem.type then
        for _ = 1, (q.rewardItem.count or 1) do pcall(function() player:getInventory():AddItem(q.rewardItem.type) end) end
    end
    if q.rewardXp and q.rewardXp.perk then
        pcall(function()
            local p = Perks[q.rewardXp.perk]
            if p then player:getXp():AddXP(p, q.rewardXp.amount or 15) end
        end)
    end

    brain.quest = false
    brain.questUnlockHour = getGameTime():getWorldAgeHours() + randomDelayHours()
    BanditBrain.Update(zombie, brain)
    if Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(zombie, { id = brain.id, quest = false, questUnlockHour = brain.questUnlockHour })
    end

    return true, q.success or "\"Thank you. This means a lot.\""
end
