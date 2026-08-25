--
-- Bandits NPC - Companions Overhaul - Interaction (Phase 1)
--
-- Registers a rebindable "Talk to NPC" key (Options -> Key Bindings) that opens
-- the dialogue window for the closest friendly/neutral NPC the player is facing.
-- Also holds the recruit / order / stance actions the dialogue calls.
--
-- Brain changes are written to the live brain (immediate effect on this
-- client's AI loop) AND synced via Bandit.ForceSyncPart -> 'BanditUpdatePart',
-- which copies the fields into the persistent cluster brain.
--

require "ISUI/BanditsNPCDialogWindow"
require "BanditsNPCTileCursor"

BanditsNPC = BanditsNPC or {}
BanditsNPC.Interact = BanditsNPC.Interact or {}

-- THE BINDING NAME IS THE IDENTITY, AND THAT IS WHY IT CHANGES BACK (v0.77.13).
--
-- PZ writes each binding to Lua/keysB42.ini the first time it sees the NAME, and from
-- then on the saved value wins over the default forever. v0.76.3 changed the default V->E
-- and nothing happened to anyone already playing, because their file still said
-- "Talk to NPC=key:47". v0.77.1 worked around that by renaming the action to
-- "Talk to companion", which PZ had never seen, so the E default finally applied.
--
-- Reverting the name is therefore the whole revert, and it lands better than a fresh name
-- would for every group at once:
--   * anyone from before v0.77.1 still has "Talk to NPC" in keysB42.ini and gets their own
--     saved key back -- V, or whatever they had deliberately rebound it to;
--   * anyone who only ever played v0.77.x has no "Talk to NPC" line, so PZ takes the
--     default below, which is V again;
--   * new players get V.
-- The orphaned "Talk to companion=key:18" line is simply never read again.
BanditsNPC.Interact.KEYBIND = "Talk to NPC"
BanditsNPC.Interact.MAX_RANGE = 3.5   -- tiles

-- ===== targeting =====

-- Closest friendly/neutral bandit-NPC the player is roughly facing, in range.
function BanditsNPC.Interact.FindTarget(player)
    if not BanditBrain then return nil end

    local px, py, pz = player:getX(), player:getY(), player:getZ()
    local fwd = player:getForwardDirection()
    local fx, fy = fwd:getX(), fwd:getY()

    local zlist = getCell():getZombieList()
    local best, bestScore

    for i = 0, zlist:size() - 1 do
        local z = zlist:get(i)
        if z and z:isAlive() and z:getVariableBoolean("Bandit") then
            local brain = BanditBrain.Get(z)
            if brain and not (brain.hostile or brain.hostileP) then
                local zx, zy, zz = z:getX(), z:getY(), z:getZ()
                if math.abs(zz - pz) < 1 then
                    local dx, dy = zx - px, zy - py
                    local dist = math.sqrt(dx * dx + dy * dy)
                    if dist <= BanditsNPC.Interact.MAX_RANGE then
                        local nd = dist > 0 and dist or 1
                        local dot = fx * (dx / nd) + fy * (dy / nd)
                        -- if right next to us, ignore facing; else must be in front
                        if dist < 1.5 or dot > 0.2 then
                            local score = dist - dot     -- nearer + more in-front wins
                            if not bestScore or score < bestScore then
                                bestScore, best = score, z
                            end
                        end
                    end
                end
            end
        end
    end
    return best
end

-- WHICHEVER LOCAL PLAYER IS ACTUALLY LOOKING AT SOMEONE (v0.75.51).
--
-- This hardcoded getSpecificPlayer(0), so in SPLIT-SCREEN player 2 could never open the
-- dialog: the target search ran from player 1's position and found her companion or
-- nobody. Threading "who pressed the key" through is not possible -- OnKeyPressed is a
-- global key event and does not say which local player it came from -- so the acting
-- player is INFERRED instead: try each local player in turn and use the first who
-- actually has a friendly NPC in front of them.
--
-- That is a proxy, and an honest one: in single player it is player 0 and the behaviour is
-- byte-identical, and in split-screen the player standing in front of a companion is
-- overwhelmingly the one who pressed the key. If both are looking at someone, player 1
-- wins -- which is the old behaviour, so nothing regresses.
-- WHICH LOCAL PLAYER IS ACTING ON THIS NPC? (v0.75.52)
--
-- Split-screen has up to four local players and none of the entry points is told which one
-- clicked -- the dialog window is opened from a global keybind and its buttons carry no
-- player. So the acting player is inferred as the NEAREST living local player, which in
-- single player is always player 0 (byte-identical behaviour) and in split-screen is the
-- one standing at the companion.
--
-- Used for anything that RECORDS an owner or takes items from a person, where guessing
-- player 0 is not a cosmetic error: it credits the wrong player with the recruit and takes
-- the gift out of the wrong inventory.
function BanditsNPC.Interact.ActingPlayerFor(zombie)
    local best, bestD
    for i = 0, 3 do
        local p = getSpecificPlayer(i)
        if p and not p:isDead() then
            local d = 0
            if zombie then
                local ok, dd = pcall(function()
                    return BanditUtils.DistTo(p:getX(), p:getY(), zombie:getX(), zombie:getY())
                end)
                d = (ok and dd) or math.huge
            end
            if bestD == nil or d < bestD then best, bestD = p, d end
        end
    end
    return best or getSpecificPlayer(0)
end

function BanditsNPC.Interact.Open()
    local acting, zombie
    for i = 0, 3 do
        local p = getSpecificPlayer(i)
        if p and not p:isDead() then
            local z = BanditsNPC.Interact.FindTarget(p)
            if z then acting, zombie = p, z; break end
            acting = acting or p          -- remember someone alive for the message below
        end
    end
    if not acting then return end
    if not zombie then
        -- SILENT (v0.76.3). This used to print, which was fine on a dedicated key but is
        -- not on E: E is also the vanilla interact key, so with the new default every
        -- door, window and light switch in the game would put a line in the console.
        -- Nothing has gone wrong when there is nobody to talk to.
        return
    end
    BanditsNPCDialogWindow.OpenFor(zombie)
end

-- ===== actions (operate on a bandit zombie's brain) =====

local function syncBrain(zombie, brain, fields)
    BanditBrain.Update(zombie, brain)
    fields.id = brain.id
    Bandit.ForceSyncPart(zombie, fields)
end

-- defined up here so every function below can see it (locals are only visible to
-- functions defined AFTER them -- the stepOffFurniture lesson)
local function confirmOverHead(zombie, text)
    if zombie and zombie:isAlive() then
        zombie:addLineChatElement(text, 0.3, 0.9, 0.3)
    end
end

-- ===== handing an item over =====

-- Release an item the PLAYER still has on their body, before it changes owner.
-- VANILLA IDIOM, verbatim from ISTransferAction:removeItemOnCharacter
-- (media/lua/shared/TimedActions/ISTransferAction.lua:85):
--     character:removeAttachedItem(item)
--     if not character:isEquipped(item) then return true end
--     character:removeFromHands(item)
--     character:removeWornItem(item, false)
--     triggerEvent("OnClothingUpdated", character)
-- We never did any of this. Every hand-over was a bare `container:Remove(item)`, which
-- pulls the item out of the inventory while WornItems, the hand slots and the hotbar still
-- hold references to it -- so the player model went on drawing a bag that now belonged to
-- the companion (30 Jul playtest). Dropping those references is what rebuilds the model;
-- the OnClothingUpdated event is what repaints the hotbar and the character panel.
function BanditsNPC.Interact.UnequipFromPlayer(player, item)
    if not (player and item) then return end
    pcall(function()
        -- hotbar first: removeItem also clears the attached-slot bookkeeping and reloads
        -- the hotbar icons, which a bare removeAttachedItem leaves stale
        local hb = getPlayerHotbar and getPlayerHotbar(player:getPlayerNum())
        if hb and hb.isInHotbar and hb:isInHotbar(item) then hb:removeItem(item, false) end
        player:removeAttachedItem(item)
        if not player:isEquipped(item) then return end
        player:removeFromHands(item)
        player:removeWornItem(item, false)
        triggerEvent("OnClothingUpdated", player)
    end)
end

-- Unequip, then pull the item out of whatever container holds it. Use this for EVERY
-- player -> companion hand-over instead of a bare getContainer():Remove().
function BanditsNPC.Interact.TakeFromPlayer(player, item)
    if not item then return false end
    BanditsNPC.Interact.UnequipFromPlayer(player, item)
    local c
    pcall(function() c = item:getContainer() end)
    if c then pcall(function() c:Remove(item) end) end
    return true
end

-- ===== recruitment requirement matching =====

local function isFood(item)
    return item.IsFood and item:IsFood()
end

local function isWater(item)
    -- a drink that quenches thirst...
    if item.IsFood and item:IsFood() then
        local ok, tc = pcall(function() return item:getThirstChange() end)
        if ok and tc and tc < 0 then return true end
    end
    -- ...or a fluid container holding something...
    if item.getFluidContainer then
        local ok, fc = pcall(function() return item:getFluidContainer() end)
        if ok and fc then
            local ok2, amt = pcall(function() return fc:getAmount() end)
            if ok2 and amt and amt > 0 then return true end
        end
    end
    -- ...or just named like water.
    local t = item:getFullType() or ""
    return t:find("Water") ~= nil
end

local function isMeleeWeapon(item)
    if not (item.IsWeapon and item:IsWeapon()) then return false end
    local ok, ranged = pcall(function() return item:isRanged() end)
    if ok and ranged then return false end
    return true
end

BanditsNPC.Interact.REQ_PREDICATE = { food = isFood, water = isWater, weapon = isMeleeWeapon }

-- Are there any items in the player's inventory matching the predicate?
-- Gear on the player's body DOES count. The only flow this would ever serve is the
-- recruitment requirement, and that Give picker is opened with hideEquipped = false, so it
-- lists worn/held items too -- excluding them here would make this answer "no" about an item
-- the picker happily offers. Currently unused; kept in step so it cannot become that trap.
function BanditsNPC.Interact.HasMatch(player, predicate)
    if not (player and predicate) then return false end
    local list = ArrayList.new()
    player:getInventory():getAllEvalRecurse(predicate, list)
    return list:size() > 0
end

-- Recruit at no item cost (for the "none" requirement). Returns success line.
function BanditsNPC.Interact.RecruitFree(zombie)
    if not BanditsNPC.Interact.Recruit(zombie) then
        return BanditsNPC.T("UI_BN_Say_AlreadyTaken", "\"I'm already with someone else.\"")   -- owned by another player
    end
    local req = BanditsNPC.Recruit.Ensure(zombie)
    return (req and req.def and req.def.success) or BanditsNPC.T("UI_BN_Say_LeadTheWay", "\"Lead the way.\"")
end

-- Consume the PLAYER-CHOSEN item to satisfy the requirement, then recruit.
-- A melee weapon becomes the NPC's weapon; anything else is simply handed over.
-- Returns the NPC's success line.
function BanditsNPC.Interact.FulfillWith(zombie, item)
    local brain = BanditBrain.Get(zombie)
    if not brain then return "" end
    -- ownership guard BEFORE consuming the item, so a refused recruit doesn't eat the gift
    if brain.recruited and brain.master and not BanditsNPC.IsMine(brain) then
        return BanditsNPC.T("UI_BN_Say_AlreadyTaken", "\"I'm already with someone else.\"")
    end
    local req = BanditsNPC.Recruit.Ensure(zombie)

    if item then
        -- TakeFromPlayer, not a bare Remove: the gift may be the weapon in your hands or a
        -- canned drink clipped to your belt, and leaving those references behind repaints
        -- nothing (see UnequipFromPlayer)
        -- the local player at the NPC, not player 0 -- this TAKES THE GIFT from their
        -- inventory, so guessing wrong in split-screen charges the wrong person (v0.75.52)
        local player = BanditsNPC.Interact.ActingPlayerFor(zombie)
        if req and req.type == "weapon" then
            local ft = item:getFullType()
            BanditsNPC.Interact.TakeFromPlayer(player, item)
            brain.weapons = brain.weapons or { primary = {bulletsLeft=0, magCount=0}, secondary = {bulletsLeft=0, magCount=0} }
            brain.weapons.melee = ft
            brain.npcMelee = ft   -- weapon-watchdog shadow (see Guardian)
            Bandit.SetWeapons(zombie, brain.weapons)
            Bandit.ForceSyncPart(zombie, { id = brain.id, weapons = brain.weapons })
        else
            BanditsNPC.Interact.TakeFromPlayer(player, item)
        end
    end

    BanditsNPC.Interact.Recruit(zombie)
    return (req and req.def and req.def.success) or BanditsNPC.T("UI_BN_Say_ImWithYou", "\"Alright, I'm with you.\"")
end

function BanditsNPC.Interact.Recruit(zombie)
    -- the local player at the NPC, not player 0 -- see ActingPlayerFor (v0.75.52)
    local player = BanditsNPC.Interact.ActingPlayerFor(zombie)
    local brain = BanditBrain.Get(zombie)
    if not (player and brain) then return false end

    -- MP ownership guard: never let a player claim a companion that already belongs to
    -- someone else. Recruiting overwrites brain.master, which would also redirect the
    -- companion's follow target to the new "owner" -- i.e. stealing. Refuse if it's
    -- recruited and not ours.
    if brain.recruited and brain.master and not BanditsNPC.IsMine(brain) then
        return false
    end

    brain.master = BanditUtils.GetCharacterID(player)
    -- Also store the recruiter's USERNAME: online ids change across reconnects, so the
    -- username is the stable owner key we match on in MP (see BanditsNPC.GetOwner).
    -- A SILENT FAILURE HERE COSTS OWNERSHIP (v0.75.20). masterName is the stable owner
    -- key -- ownerMatches falls back to the reusable online id without it -- and this was
    -- a bare pcall, so a nil or empty username left the field unset with no trace and the
    -- companion permanently on the weaker test. Say so instead: it is one line in the log
    -- against a bug that is otherwise invisible until two players collide.
    local un
    local okName = pcall(function() un = player:getUsername() end)
    if okName and type(un) == "string" and un ~= "" then
        brain.masterName = un
    else
        print("[BanditsNPC] WARNING: could not read the recruiter's username; '"
            .. tostring(brain.fullname or "companion")
            .. "' will fall back to the online id for ownership, which is reused across"
            .. " reconnects. Please report this line.")
    end
    brain.recruited = true   -- explicit recruitment flag (master alone is set on spawn)
    brain.hostile = false
    brain.hostileP = false
    brain.program = {name = "NPCCompanion", stage = "Prepare"}
    brain.npcStance = brain.npcStance or "defensive"
    brain.affinity = brain.affinity or 0
    -- THE FIRST QUEST IS AVAILABLE THE MOMENT SHE JOINS. It used to be one to twelve
    -- DAYS away, which meant a new companion had nothing to say and no reason to be
    -- talked to for the entire first week -- the part of the relationship that most
    -- needs something in it. Later quests still take the random delay (Quests.lua sets
    -- it again on completion); only the first one is immediate.
    brain.questUnlockHour = brain.questUnlockHour or getGameTime():getWorldAgeHours()

    -- ===== THE THINGS THE PROFILE PANEL WANTS TO SAY ABOUT HER (v0.77.0) =====
    --
    -- "Met - Rosewood, day 12" and "Together - 9 days" are two of the lines the reworked
    -- Profile tab carries, and they cost nothing IF they are recorded at the one moment
    -- they can be: now. Derived later they would be guesses, and the rule for this rework
    -- is that no panel invents a number.
    --
    -- Set once and never overwritten -- re-recruiting a dismissed companion should not
    -- rewrite the day you first met her.
    if brain.metHour == nil then
        pcall(function() brain.metHour = getGameTime():getWorldAgeHours() end)
    end
    -- NO PLACE NAME. The mock-up reads "Met - Rosewood, day 12", but there is no cheap
    -- API for the town: IsoMetaGrid has getCell/getZone and no getRegionName, and nothing
    -- in vanilla Lua turns a coordinate into "Rosewood". Rather than invent one or guess
    -- from coordinates, the panel says "Met - day 12", which is true.
    -- Age is hers, rolled once. Nothing in the engine gives a bandit an age, so it is
    -- stored rather than derived; a companion whose age changed between openings of the
    -- panel would be worse than no age at all.
    if brain.age == nil then brain.age = 19 + ZombRand(28) end
    brain.kills = brain.kills or 0

    -- Roll this companion a trade profession (once). Keeps an existing trade if they
    -- already have one; otherwise picks a random one from our full trade list.
    if BanditsNPC.AssignRandomProfession then BanditsNPC.AssignRandomProfession(brain) end

    -- stable persistence id (used by the roster / lost-companion restore)
    if BanditsNPC.Persistence then BanditsNPC.Persistence.EnsureUid(brain) end

    syncBrain(zombie, brain, {
        master = brain.master, masterName = brain.masterName, npcUid = brain.npcUid, recruited = true, hostile = false, hostileP = false,
        program = brain.program, npcStance = brain.npcStance,
        affinity = brain.affinity, questUnlockHour = brain.questUnlockHour,
        exp = brain.exp, npcProfRolled = brain.npcProfRolled,
    })
    -- roster her immediately: the periodic record runs once a minute, and a save/crash
    -- inside that window would otherwise leave a fresh recruit unprotected by the restore
    if BanditsNPC.Persistence and BanditsNPC.Persistence.Record then
        pcall(function() BanditsNPC.Persistence.Record(zombie, brain) end)
    end
    return true
end

-- Active healing: bandage a hurt companion back to full and stop the bleeding. Requires (and
-- consumes) one bandage-type item from the player. Passive regen (the Guardian) handles slow
-- recovery; this is the instant patch-up, and the main way to heal when "Companions can't be
-- killed" is OFF. Returns a status line for the dialog.
-- ASK THE ITEM, DON'T LIST THE IDS (v0.75.29). This was four hardcoded Base.* ids, so a
-- modded bandage -- or any vanilla one added later -- was simply not a bandage as far as a
-- companion was concerned, and the player was told "you don't have a bandage" while
-- holding one. The engine already answers this: a bandage is an item whose script marks it
-- as one (`Bandage = TRUE` -> IsBandage()/canBandage), which is what vanilla's own
-- ISHealthPanel tests. The id list stays as a PREFERENCE ORDER -- plain bandage before
-- ripped sheets before a dirty one -- so behaviour for vanilla items is unchanged; it just
-- no longer decides what counts.
local BANDAGE_PREFERENCE = { "Base.Bandage", "Base.RippedSheets", "Base.RippedSheetsDirty", "Base.Bandaid" }

-- isCanBandage() -- VERIFIED against a dump of zombie/inventory/InventoryItem, which is
-- the only such method on the class. My first attempt here guarded `IsBandage` and
-- `canBandage`; NEITHER EXISTS, so both would have resolved to nil, the guards would have
-- skipped them, and this whole fallback would have been dead code that looked like a fix.
-- That is the exact failure mode this audit spent its time on, which is why the name is
-- checked rather than assumed.
local function isBandageItem(it)
    if not it then return false end
    local ok = false
    pcall(function()
        if it.isCanBandage and it:isCanBandage() then ok = true end
    end)
    return ok
end

local function takeBandage(player)
    local inv = player and player:getInventory()
    if not inv then return false end
    -- preferred vanilla ids first, so the cheapest suitable one goes first as before
    for _, ft in ipairs(BANDAGE_PREFERENCE) do
        local it = nil
        pcall(function() it = inv:getFirstTypeRecurse(ft) end)
        if it then
            pcall(function() local c = it:getContainer(); if c then c:Remove(it) end end)
            return true
        end
    end
    -- then anything the ENGINE calls a bandage, which is where modded ones live
    local found
    pcall(function()
        local list = ArrayList.new()
        inv:getAllEvalRecurse(function(i) return isBandageItem(i) end, list)
        if list:size() > 0 then found = list:get(0) end
    end)
    if found then
        pcall(function() local c = found:getContainer(); if c then c:Remove(found) end end)
        return true
    end
    return false
end

function BanditsNPC.Interact.Treat(zombie)
    if not BanditsNPC.MayCommand(zombie) then return end
    local player = getSpecificPlayer(0)
    local brain = BanditBrain.Get(zombie)
    if not (player and brain) then return "" end
    local maxh = (brain.health and brain.health > 0) and brain.health or 2.0
    local cur = maxh; pcall(function() cur = zombie:getHealth() end)
    if cur >= maxh - 0.001 then return BanditsNPC.T("UI_BN_Say_NotHurt", "\"I'm not hurt.\"") end
    if not takeBandage(player) then return BanditsNPC.T("UI_BN_Say_NoBandage", "\"You don't have a bandage.\"") end

    pcall(function() zombie:setHealth(maxh) end)
    -- infection must travel with the heal (v0.75.21): it was cleared locally and
    -- left out of the payload, so the cluster kept the old value and a reseed on
    -- re-banditize brought the infection back -- the bite you just treated.
    brain.infection = 0
    BanditBrain.Update(zombie, brain)
    if Bandit and Bandit.ForceSyncPart then
        pcall(function() Bandit.ForceSyncPart(zombie, { id = brain.id, health = maxh, infection = 0 }) end)
    end
    pcall(function() zombie:addLineChatElement(BanditsNPC.T("UI_BN_Bark_PatchedUp", "(patched up)"), 0.6, 0.95, 0.6) end)
    return BanditsNPC.T("UI_BN_Say_Patched", "\"Thanks, that's better.\"")
end

-- Restore a companion's looks. Combat splatters blood and dirt on her body and clothes
-- (ZASmack.addBlood: +0.1 per part per hit -- full face blood reads as "she lost her
-- eyes"), a floor-kill can leave an enemy's weapon VISUAL stuck in her, and none of it
-- ever washes off on its own. One action clears it all: body blood+dirt to zero, stuck
-- weapon visuals pulled out, and a full re-dress -- recreating the clothing ItemVisuals
-- drops their blood decals and holes too.
local STUCK_LOCATIONS = { "MeatCleaver in Back", "Axe Back", "Knife in Back",
                          "Knife Left Leg", "Knife Right Leg", "Knife Shoulder", "Knife Stomach" }
function BanditsNPC.Interact.CleanUp(zombie)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie)
    if not brain then return "" end
    pcall(function()
        local visual = zombie:getHumanVisual()
        -- AGGREGATE blood/dirt layers first: the engine's own provisioning cleanup
        -- (Bandit.ApplyVisuals, Bandit.lua:93-94) calls these BESIDES the per-part
        -- zeroing below -- with only the per-part loop she stayed visibly gory
        -- (the "clean up did nothing" report, 11 Jul).
        if visual.removeBlood then visual:removeBlood() end
        if visual.removeDirt then visual:removeDirt() end
        for i = 0, BloodBodyPartType.MAX:index() - 1 do
            local part = BloodBodyPartType.FromIndex(i)
            visual:setBlood(part, 0)
            visual:setDirt(part, 0)
        end
        -- ZOMBIE GORE OVERLAYS (ZedDmg body visuals: torn flesh, missing nose/chin,
        -- exposed ribs...): separate "body visual" items a zombie carries from spawn --
        -- no amount of blood removal touches them. Remove exactly the set the engine
        -- itself strips at provisioning (Bandit.lua:127-139, BanditUtils.ItemVisuals).
        if visual.getBodyVisuals and BanditUtils and type(BanditUtils.ItemVisuals) == "table" then
            local bodyVisuals = visual:getBodyVisuals()
            local toRemove = {}
            for i = 0, bodyVisuals:size() - 1 do
                local bv = bodyVisuals:get(i)
                if bv and BanditUtils.ItemVisuals[bv:getItemType()] then
                    toRemove[#toRemove + 1] = bv:getItemType()
                end
            end
            for _, t in ipairs(toRemove) do visual:removeBodyVisualFromItemType(t) end
        end
        -- FACE GORE (missing eye / bared teeth): head hits swap the zombie's skin
        -- texture to a damaged variant; blood removal can't fix that. Re-provision
        -- the clean skin the same way the engine does (Bandit.ApplyVisuals) -- this
        -- is also why a respawned companion comes back with a normal face.
        if brain.skin ~= nil and Bandit and Bandit.GetSkinTexture then
            local tex = Bandit.GetSkinTexture(brain.female, brain.skin)
            if tex then visual:setSkinTextureName(tex) end
        elseif brain.skinTexture then
            visual:setSkinTextureName(brain.skinTexture)
        end
    end)
    for _, loc in ipairs(STUCK_LOCATIONS) do
        pcall(function() zombie:setAttachedItem(loc, nil) end)
    end
    if BanditsNPC.Interact.ApplyClothingVisuals then
        BanditsNPC.Interact.ApplyClothingVisuals(zombie, brain)   -- also resetModel()s her
        -- SYNC the appearance record it just captured. Clean up used to call the redress
        -- without any sync at all, so the record never left this client -- which is half of
        -- why repeated clicks kept re-rolling her colours.
        pcall(function() syncBrain(zombie, brain, { npcClothVar = brain.npcClothVar }) end)
    end
    pcall(function() zombie:resetModelNextFrame() end)
    pcall(function() zombie:addLineChatElement(BanditsNPC.T("UI_BN_Bark_CleanedUp", "(cleaned up)"), 0.6, 0.85, 0.95) end)
    return BanditsNPC.T("UI_BN_Say_Cleaned", "\"Ahh, much better.\"")
end

function BanditsNPC.Interact.Dismiss(zombie)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie)
    if not brain then return end

    -- Neutral + hold position + defensive: a dismissed NPC stays put and defends
    -- only at close range, instead of the Looter AI charging out to die.
    if BanditsNPC.Persistence then BanditsNPC.Persistence.Forget(brain) end  -- drop from restore roster
    -- THE UID DELIBERATELY STAYS ON HER (v0.77.10). Clearing it looks like the tidy thing to
    -- do -- cut every thread back to who she was -- and it is exactly wrong. Forget only
    -- empties the store on THIS machine; the entry that causes the re-adoption is the one
    -- that survived somewhere else, and the uid on her body is the only thing that lets the
    -- restore sweep recognise "she is standing right here and her real brain says she is not
    -- recruited" and drop that entry. Strip the uid and she becomes unmatchable, so the
    -- stale snapshot looks like a lost companion and gets rebuilt as a duplicate instead.
    -- masterName goes WITH master (v0.75.19). Clearing only the id left the
    -- username behind, so a dismissed companion still matched her old owner in
    -- ownerMatches -- she was 'not recruited' but still demonstrably his, which
    -- is the sort of half-state that makes a later ownership test disagree with
    -- itself.
    brain.master = false
    brain.masterName = false
    brain.recruited = false
    brain.hostile = false
    brain.hostileP = false
    brain.npcStance = "defensive"
    brain.program = {name = "NPCNeutral", stage = "Prepare"}
    syncBrain(zombie, brain, {
        master = false, masterName = false, recruited = false, hostile = false,
        hostileP = false, npcStance = "defensive", program = brain.program,
    })
end

function BanditsNPC.Interact.SetOrder(zombie, programName)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie)
    if not brain then return end

    brain.program = {name = programName, stage = "Prepare"}
    syncBrain(zombie, brain, {program = brain.program})
end

function BanditsNPC.Interact.SetStance(zombie, stance)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie)
    if not brain then return end

    brain.npcStance = stance
    syncBrain(zombie, brain, {npcStance = stance})
end

-- "Hide" = hold position + go passive (don't fight, lay low).
function BanditsNPC.Interact.OrderHide(zombie)
    if not BanditsNPC.MayCommand(zombie) then return end
    BanditsNPC.Interact.SetOrder(zombie, "NPCStay")
    BanditsNPC.Interact.SetStance(zombie, "passive")
end

-- ===== weapons: melee AND firearms =====
--
-- Bandits drives combat from brain.weapons, NOT from real inventory items:
--   weapons.melee              = item TYPE STRING, swung with Smack tasks
--   weapons.primary/.secondary = gun SPEC TABLES (long guns primary, handguns
--                                secondary) with VIRTUAL ammo counters
--                                (bulletsLeft/magCount/ammoCount) -- the engine
--                                fires and reloads from these numbers only and
--                                NEVER reads magazines out of her bag.
-- A gun parked in the melee slot (what we did pre-v0.42) gets SWUNG: melee
-- damage plus the gun's swing sound = "she hits zombies and it makes a shot
-- sound", and it never fires. The spec builder below mirrors the engine's own
-- gun-looting code (ZALootWeapons.lua) so companions use guns like native bandits.

-- slot + spec for a firearm item; nil for melee weapons / non-weapons
local function buildGunSpec(weaponItem)
    if not (weaponItem and weaponItem.IsWeapon and weaponItem:IsWeapon()) then return nil end
    local isRanged = false
    pcall(function() isRanged = weaponItem:isRanged() == true end)
    if not isRanged then return nil end

    local slot = "primary"
    pcall(function()
        if WeaponType.getWeaponType(weaponItem) == WeaponType.HANDGUN then slot = "secondary" end
    end)

    local spec = { name = weaponItem:getFullType(), racked = false }
    local loaded = 0
    pcall(function() loaded = weaponItem:getCurrentAmmoCount() or 0 end)
    if BanditCompatibility.UsesExternalMagazine(weaponItem) then
        local magazineType = weaponItem:getMagazineType()
        if not magazineType then return nil end   -- exotic modded gun: fall back to melee
        local magSize = 30
        local mag = BanditCompatibility.InstanceItem(magazineType)
        if mag then magSize = mag:getMaxAmmo() end
        spec.type = "mag"
        spec.magName = magazineType
        spec.magSize = magSize
        spec.magCount = 0
        spec.bulletsLeft = loaded
        spec.clipIn = loaded > 0   -- rounds in the given gun = its mag is seated
    else
        local ammoName
        pcall(function() ammoName = weaponItem:getAmmoType():getItemKey() end)
        if not ammoName then return nil end       -- no ammo type: fall back to melee
        spec.type = "nomag"
        spec.ammoSize = weaponItem:getMaxAmmo()
        spec.ammoName = ammoName
        spec.ammoCount = 0
        spec.bulletsLeft = loaded
    end
    return slot, spec
end
BanditsNPC.Interact.BuildGunSpec = buildGunSpec   -- the Guardian migrates old saves with it

-- Turn a gun spec's VIRTUAL ammo back into real items in the given container:
-- full/partial magazines (vanilla-proven AddItem():setCurrentAmmoCount) or loose
-- shells. Used when taking her weapon and when a new gun replaces an old one.
local function refundGunAmmo(inv, w)
    pcall(function()
        if w.type == "mag" and w.magName then
            local magSize = w.magSize or 30
            local rounds = (w.magCount or 0) * magSize + (w.bulletsLeft or 0) + (w.poolRounds or 0)
            local mags = 0
            while rounds > 0 and mags < 40 do   -- sanity cap
                local fill = math.min(rounds, magSize)
                local m = inv:AddItem(w.magName)
                if m and m.setCurrentAmmoCount then m:setCurrentAmmoCount(fill) end
                rounds = rounds - fill
                mags = mags + 1
            end
        elseif w.type == "nomag" and w.ammoName then
            local rounds = (w.ammoCount or 0) + (w.bulletsLeft or 0)
            if rounds > 0 then inv:AddItems(w.ammoName, math.min(rounds, 500)) end
        end
    end)
end

-- Vanilla B42 ammo BOXES and CARTONS -> loose rounds. Counts come from the game's
-- OWN unboxing recipes (OpenBoxOfBullets50/20, OpenBoxOfShotgunShells, OpenCarton12
-- in media/scripts/.../recipes_ammunition.txt + recipes_packing.txt); every id
-- verified against the B42 scripts (ITEM-ID AUDIT). A carton holds 12 boxes.
local AMMO_BOXES = {
    ["Base.Bullets9mmBox"]    = { round = "Base.Bullets9mm",  count = 50 },
    ["Base.Bullets45Box"]     = { round = "Base.Bullets45",   count = 50 },
    ["Base.Bullets38Box"]     = { round = "Base.Bullets38",   count = 50 },
    ["Base.Bullets357Box"]    = { round = "Base.Bullets357",  count = 50 },
    ["Base.Bullets44Box"]     = { round = "Base.Bullets44",   count = 20 },
    ["Base.308Box"]           = { round = "Base.308Bullets",  count = 20 },
    ["Base.556Box"]           = { round = "Base.556Bullets",  count = 20 },
    ["Base.3030Box"]          = { round = "Base.3030Bullets", count = 20 },
    ["Base.ShotgunShellsBox"] = { round = "Base.ShotgunShells", count = 25 },
}
local AMMO_CARTONS = {
    ["Base.Bullets9mmCarton"]    = "Base.Bullets9mmBox",
    ["Base.Bullets45Carton"]     = "Base.Bullets45Box",
    ["Base.Bullets38Carton"]     = "Base.Bullets38Box",
    ["Base.Bullets357Carton"]    = "Base.Bullets357Box",
    ["Base.Bullets44Carton"]     = "Base.Bullets44Box",
    ["Base.308Carton"]           = "Base.308Box",
    ["Base.556Carton"]           = "Base.556Box",
    ["Base.3030Carton"]          = "Base.3030Box",
    ["Base.ShotgunShellsCarton"] = "Base.ShotgunShellsBox",
}

-- The loose-round item a gun spec consumes. nomag specs carry it directly; for mag
-- guns read it off the MAGAZINE's script AmmoType (same getAmmoType():getItemKey()
-- call buildGunSpec proves on guns; existence-guarded + cached, it never changes).
local magRoundCache = {}
local function roundNameFor(w)
    if w.type == "nomag" then return w.ammoName end
    if w.type == "mag" and w.magName then
        local cached = magRoundCache[w.magName]
        if cached ~= nil then return cached or nil end
        local name = false
        pcall(function()
            local mag = BanditCompatibility.InstanceItem(w.magName)
            if mag and mag.getAmmoType then
                local at = mag:getAmmoType()
                if at and at.getItemKey then name = at:getItemKey() end
            end
        end)
        magRoundCache[w.magName] = name
        return name or nil
    end
    return nil
end

-- FAILSAFE AMMO SUPPLY: convert magazines, loose rounds AND ammo boxes/cartons
-- carried in her REAL bag into the virtual counters the engine fires from. Runs
-- when items are given, when a gun is equipped, and on a slow Guardian sweep -- so
-- ammo "just works" no matter how it got into her bag. (Tester report 11 Jul: "gave
-- mags and a box of ammo, they don't reload" -- boxes were never unpacked and loose
-- rounds never counted for mag-fed guns; and EMPTY mags were silently consumed at
-- zero value -- now only mags that hold rounds are taken.) Partial amounts pool
-- into w.poolRounds until a full virtual mag accumulates. Returns rounds banked.
function BanditsNPC.Interact.SyncAmmo(zombie, brain)
    brain = brain or (BanditBrain and BanditBrain.Get(zombie))
    if not (zombie and brain and type(brain.weapons) == "table") then return 0 end
    -- OWNER'S CLIENT ONLY (v0.75.18). This is not a player command -- it is automatic
    -- maintenance -- but it is DESTRUCTIVE and NOT IDEMPOTENT: it removes real rounds,
    -- boxes and magazines from her inventory and increments counters on the brain. Its
    -- caller is the Guardian's per-zombie handler, which is ownership-blind, and client/
    -- runs on every connected machine -- so with two players near the same companion both
    -- swept her, and the 10s throttle (brain.npcAmmoSyncMs) is written locally and cannot
    -- coordinate them. Depending on how the engine replicates a zombie's container that is
    -- either ammo counted twice or ammo removed and lost. One owner, one sweep.
    --
    -- MayCommand is permissive for an UNRECRUITED NPC, which is correct: a wild survivor
    -- has no owner to defer to and her ammo still needs banking.
    if BanditsNPC.MayCommand and not BanditsNPC.MayCommand(zombie) then return 0 end
    local banked = 0
    for _, slotName in ipairs({ "primary", "secondary" }) do
        local w = brain.weapons[slotName]
        if type(w) == "table" and w.name and w.type then
            pcall(function()
                local inv = zombie:getInventory()
                if not inv then return end
                local roundName = roundNameFor(w)
                local rounds = 0   -- loose + unboxed rounds gathered this sweep

                if roundName then
                    -- loose rounds (for mag guns these pool into virtual mags below)
                    local list = ArrayList.new()
                    inv:getAllEvalRecurse(function(it) return it:getFullType() == roundName end, list)
                    for i = 0, list:size() - 1 do
                        local it = list:get(i)
                        local c = it:getContainer(); if c then c:Remove(it) end
                        rounds = rounds + 1
                    end
                    -- ammo boxes/cartons matching this gun's round type
                    local boxlist = ArrayList.new()
                    inv:getAllEvalRecurse(function(it)
                        local ft = it:getFullType()
                        local box = AMMO_BOXES[ft]
                        if box and box.round == roundName then return true end
                        local carton = AMMO_CARTONS[ft]
                        return (carton ~= nil and AMMO_BOXES[carton].round == roundName) or false
                    end, boxlist)
                    for i = 0, boxlist:size() - 1 do
                        local it = boxlist:get(i)
                        local ft = it:getFullType()
                        local n = 0
                        if AMMO_BOXES[ft] then n = AMMO_BOXES[ft].count
                        elseif AMMO_CARTONS[ft] then n = AMMO_BOXES[AMMO_CARTONS[ft]].count * 12 end
                        if n > 0 then
                            local c = it:getContainer(); if c then c:Remove(it) end
                            rounds = rounds + n
                        end
                    end
                end

                if w.type == "mag" and w.magName then
                    -- magazines: only take mags that HOLD rounds; empty ones stay in
                    -- her bag (they used to be consumed silently at zero value)
                    local list = ArrayList.new()
                    inv:getAllEvalRecurse(function(it) return it:getFullType() == w.magName end, list)
                    local pool = (w.poolRounds or 0) + rounds
                    for i = 0, list:size() - 1 do
                        local it = list:get(i)
                        local r = it:getCurrentAmmoCount() or 0
                        if r > 0 then
                            pool = pool + r
                            banked = banked + r
                            local c = it:getContainer(); if c then c:Remove(it) end
                        end
                    end
                    local magSize = w.magSize or 30
                    local mags = math.floor(pool / magSize)
                    w.magCount = (w.magCount or 0) + mags
                    w.poolRounds = pool - mags * magSize
                    banked = banked + rounds
                elseif w.type == "nomag" then
                    w.ammoCount = (w.ammoCount or 0) + rounds
                    banked = banked + rounds
                end
            end)
        end
    end
    if banked > 0 then
        -- keep death drops honest (the engine spawns virtual ammo back as items)
        pcall(function() Bandit.UpdateItemsToSpawnAtDeath(zombie, brain) end)
        if Bandit.ForceSyncPart then
            pcall(function() Bandit.ForceSyncPart(zombie, { id = brain.id, weapons = brain.weapons }) end)
        end
        pcall(function() zombie:addLineChatElement(BanditsNPC.TF("UI_BN_Bark_Stocked", "(stocked %1 rounds)", banked), 0.6, 0.85, 0.95) end)
    end
    return banked
end

-- Make the companion actually WIELD a weapon. Melee goes to weapons.melee;
-- firearms go to their proper gun slot with an engine-style spec (see above).
-- Returns success plus which slot it landed in ("melee"/"primary"/"secondary").
function BanditsNPC.Interact.EquipWeapon(zombie, itemType, item)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie)
    if not brain or not brain.weapons or not itemType then return false end

    -- prefer the REAL given item so its loaded rounds carry over; fall back to a
    -- fresh instance for type-only callers
    local weaponItem = item
    if not weaponItem then
        pcall(function() weaponItem = BanditCompatibility.InstanceItem(itemType) end)
    end
    local slot, spec
    if weaponItem then slot, spec = buildGunSpec(weaponItem) end
    if slot and spec then
        -- handing her a gun is an explicit "use firearms": lift melee-only mode first
        -- (also un-stashes any parked guns so the occupied-slot logic below sees them)
        if brain.npcNoGuns then
            BanditsNPC.Interact.SetFirearmsEnabled(zombie, true)
        end
        -- slot already occupied: the old gun becomes a spare in her bag. Compatible
        -- ammo counters carry straight over; incompatible ones come back as real
        -- mags/shells in her bag (the SyncAmmo below re-banks compatible mags anyway).
        local old = brain.weapons[slot]
        if type(old) == "table" and old.name then
            pcall(function() zombie:getInventory():AddItem(old.name) end)
            if spec.type == "mag" and old.type == "mag" and old.magName == spec.magName then
                local magSize = spec.magSize or 30
                local pool = (old.poolRounds or 0) + (old.bulletsLeft or 0)
                spec.magCount = (old.magCount or 0) + math.floor(pool / magSize)
                spec.poolRounds = pool % magSize
            elseif spec.type == "nomag" and old.type == "nomag" and old.ammoName == spec.ammoName then
                spec.ammoCount = (old.ammoCount or 0) + (old.bulletsLeft or 0)
            else
                pcall(function() refundGunAmmo(zombie:getInventory(), old) end)
            end
        end
        brain.weapons[slot] = spec
        if Bandit.SetWeapons then Bandit.SetWeapons(zombie, brain.weapons) end
        if Bandit.SetHands then pcall(function() Bandit.SetHands(zombie, itemType) end) end
        BanditsNPC.Interact.SyncAmmo(zombie, brain)   -- mags already in her bag count now
        syncBrain(zombie, brain, { weapons = brain.weapons })
        return true, slot
    end

    -- A RANGED weapon we could not build a spec for must NOT land in the melee slot.
    -- weapons.melee is SWUNG by the engine (ZASmack), and ZASmack plays the weapon's own
    -- getSwingSound() -- so a modded gun parked here becomes "she melees zombies and it makes
    -- a gunshot noise", which is exactly the v0.42 bug the gun-spec rewrite fixed for vanilla
    -- guns. buildGunSpec returns nil whenever a firearm's magazine type or ammo type can't be
    -- resolved, which is common for modded/exotic guns, and the old code silently fell through
    -- to melee. Refuse instead and tell the player, so it reads as unsupported rather than
    -- broken. (Melee weapons -- modded ones included -- are unaffected: they never reach here
    -- as ranged.)
    local wasRanged = false
    if weaponItem then pcall(function() wasRanged = weaponItem:isRanged() == true end) end
    if wasRanged then return false, "unsupported-firearm" end

    brain.weapons.melee = itemType
    -- KEEP THE ITEM'S STATE, NOT JUST ITS TYPE (v0.75.40). The spec stores a type STRING
    -- and the real item is consumed, so handing the weapon back later did AddItem(type)
    -- and the engine built a FRESH one at full condition -- a worn axe came back pristine
    -- every time she was disarmed and re-armed, which is a quiet repair exploit and loses
    -- any appearance the player had on it. Same fix as the carried-items manifest.
    brain.npcMeleeState = nil
    if weaponItem then
        local st = {}
        pcall(function()
            if weaponItem.getConditionMax and weaponItem:getConditionMax() > 0 then
                st.c = weaponItem:getCondition()
            end
        end)
        pcall(function()
            local iv = weaponItem.getVisual and weaponItem:getVisual()
            if iv and BanditsNPC.Interact.CaptureLook then
                st.v = BanditsNPC.Interact.CaptureLook(iv, {})
            end
        end)
        local any = false
        for _ in pairs(st) do any = true; break end
        if any then brain.npcMeleeState = st end
    end
    brain.npcMelee = itemType   -- weapon-watchdog shadow (see Guardian)
    if Bandit.SetWeapons then Bandit.SetWeapons(zombie, brain.weapons) end
    if Bandit.SetHands then pcall(function() Bandit.SetHands(zombie, itemType) end) end
    syncBrain(zombie, brain, { weapons = brain.weapons, npcMeleeState = brain.npcMeleeState })
    return true, "melee"
end

-- Take every weapon she has (melee, guns in both slots, or a clan-default she
-- came with) and leave her bare-handed. Guns come back WITH their unspent ammo:
-- virtual mags/rounds turn back into real magazines (vanilla-proven
-- AddItem():setCurrentAmmoCount pattern) and loose shells, so nothing the player
-- invested is lost.
function BanditsNPC.Interact.TakeWeapon(zombie)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie)
    if not brain then return false end

    local player = getSpecificPlayer(0)
    local pinv = player and player:getInventory()
    if not pinv then return false end
    local gave = false

    -- gun slots: the gun item + its remaining ammo as real items
    for _, slotName in ipairs({ "primary", "secondary" }) do
        local w = brain.weapons and brain.weapons[slotName]
        if type(w) == "table" and w.name then
            pcall(function() pinv:AddItem(w.name) end)
            gave = true
            refundGunAmmo(pinv, w)
        end
    end

    -- guns parked by melee-only mode come back the same way
    if type(brain.npcStashedGuns) == "table" then
        for _, slotName in ipairs({ "primary", "secondary" }) do
            local s = brain.npcStashedGuns[slotName]
            if type(s) == "table" and s.name then
                pcall(function() pinv:AddItem(s.name) end)
                gave = true
                refundGunAmmo(pinv, s)
            end
        end
        brain.npcStashedGuns = false
    end

    -- melee: what she physically holds (or the melee slot), unless it's one of
    -- the guns we already returned above
    local held
    pcall(function() held = zombie:getPrimaryHandItem() end)
    local mtype = (held and held.getFullType and held:getFullType())
                  or (brain.weapons and brain.weapons.melee)
    if mtype and mtype ~= "" and mtype ~= "Base.BareHands" then
        local isReturnedGun = false
        for _, slotName in ipairs({ "primary", "secondary" }) do
            local w = brain.weapons and brain.weapons[slotName]
            if type(w) == "table" and w.name == mtype then isReturnedGun = true end
        end
        if not isReturnedGun then
            -- restore the condition and appearance captured at equip time, so the axe you
            -- get back is the axe you handed over (v0.75.40)
            pcall(function()
                local it = pinv:AddItem(mtype)
                local st = brain.npcMeleeState
                if it and type(st) == "table" then
                    if st.c then pcall(function() it:setCondition(st.c) end) end
                    if st.v and BanditsNPC.Interact.ApplyLook then
                        pcall(function()
                            local iv = it.getVisual and it:getVisual()
                            if iv then BanditsNPC.Interact.ApplyLook(iv, st.v) end
                        end)
                    end
                end
            end)
        end
        gave = true
    end
    if not gave then return false end

    -- fully disarm: clear every weapon slot so GetBestWeapon returns bare hands
    if brain.weapons then
        local function disarm(w)
            if w then
                w.name = nil; w.bulletsLeft = 0; w.magCount = 0; w.ammoCount = 0; w.poolRounds = 0
            end
        end
        disarm(brain.weapons.primary)
        disarm(brain.weapons.secondary)
        brain.weapons.melee = "Base.BareHands"
    end
    brain.npcMelee = "Base.BareHands"   -- deliberate unequip: the watchdog must not re-arm her
    pcall(function() zombie:setPrimaryHandItem(nil); zombie:setSecondaryHandItem(nil) end)
    if Bandit.SetWeapons then Bandit.SetWeapons(zombie, brain.weapons) end
    if Bandit.SetHands then pcall(function() Bandit.SetHands(zombie, "Base.BareHands") end) end
    syncBrain(zombie, brain, { weapons = brain.weapons, npcStashedGuns = brain.npcStashedGuns })
    return true
end

-- "Use firearms" per-companion toggle (tester ask, 8 Jul: a companion holding a gun
-- with ammo ALWAYS fights with it -- the engine prefers a loaded gun spec over
-- weapons.melee -- so a melee weapon you hand her "turns back into a gun" in combat).
-- OFF: park the live gun specs on brain.npcStashedGuns; with no spec in the slots the
-- engine's combat loop never enters its firing branch (the proven no-gun bandit path)
-- and she fights melee-only. ON: put the specs back and re-bank ammo. Nothing is lost
-- either way; Take weapon returns stashed guns too.
function BanditsNPC.Interact.SetFirearmsEnabled(zombie, enabled)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie)
    if not (brain and brain.weapons) then return false end

    if enabled then
        brain.npcNoGuns = false
        local stash = brain.npcStashedGuns
        brain.npcStashedGuns = false
        if type(stash) == "table" then
            for _, slotName in ipairs({ "primary", "secondary" }) do
                local s = stash[slotName]
                if type(s) == "table" and s.name then
                    local cur = brain.weapons[slotName]
                    if type(cur) == "table" and cur.name then
                        -- a new gun landed in the slot while stashed: old one becomes a
                        -- spare in her bag, its ammo comes back as real mags/shells
                        pcall(function()
                            local inv = zombie:getInventory()
                            inv:AddItem(s.name)
                            refundGunAmmo(inv, s)
                        end)
                    else
                        brain.weapons[slotName] = s
                    end
                end
            end
        end
        if Bandit.SetWeapons then Bandit.SetWeapons(zombie, brain.weapons) end
        BanditsNPC.Interact.SyncAmmo(zombie, brain)
        local g = (type(brain.weapons.primary) == "table" and brain.weapons.primary.name)
               or (type(brain.weapons.secondary) == "table" and brain.weapons.secondary.name)
        if g and Bandit.SetHands then pcall(function() Bandit.SetHands(zombie, g) end) end
        confirmOverHead(zombie, BanditsNPC.T("UI_BN_Say_GunsOn", "I'll use my gun again."))
    else
        brain.npcNoGuns = true
        local stash = (type(brain.npcStashedGuns) == "table") and brain.npcStashedGuns or {}
        for _, slotName in ipairs({ "primary", "secondary" }) do
            local w = brain.weapons[slotName]
            if type(w) == "table" and w.name then
                stash[slotName] = {
                    name = w.name, type = w.type, racked = w.racked, clipIn = w.clipIn,
                    magName = w.magName, magSize = w.magSize, magCount = w.magCount,
                    ammoName = w.ammoName, ammoSize = w.ammoSize, ammoCount = w.ammoCount,
                    bulletsLeft = w.bulletsLeft, poolRounds = w.poolRounds,
                }
                -- blank the live slot the same way TakeWeapon's disarm does (keep the
                -- table itself -- the engine indexes it)
                w.name = nil; w.bulletsLeft = 0; w.magCount = 0; w.ammoCount = 0; w.poolRounds = 0
            end
        end
        brain.npcStashedGuns = stash
        if Bandit.SetWeapons then Bandit.SetWeapons(zombie, brain.weapons) end
        local m = brain.weapons.melee
        if Bandit.SetHands then
            pcall(function() Bandit.SetHands(zombie, (m and m ~= "" and m) or "Base.BareHands") end)
        end
        confirmOverHead(zombie, BanditsNPC.T("UI_BN_Say_GunsOff", "Melee only -- got it."))
    end
    syncBrain(zombie, brain, { weapons = brain.weapons, npcNoGuns = brain.npcNoGuns,
                               npcStashedGuns = brain.npcStashedGuns })
    return true
end

-- Animation Player: pick a tile, NPC walks there and performs the anim.
function BanditsNPC.Interact.PlayAnimAt(zombie, anim)
    if not BanditsNPC.MayCommand(zombie) then return end
    local player = getSpecificPlayer(0)
    if not player then return end
    BanditsNPCTileCursor.Begin(player:getPlayerNum(), 1, function(picks)
        local p = picks[1]
        local brain = BanditBrain.Get(zombie)
        if not brain or not p then return end
        if (brain.program and brain.program.name) ~= "NPCAnimPlay" then
            brain.prevProgram = brain.program
        end
        brain.animPlay = { anim = anim, x = math.floor(p.x), y = math.floor(p.y), z = p.z or 0, count = 3 }
        brain.program = { name = "NPCAnimPlay", stage = "Prepare" }
        syncBrain(zombie, brain, { program = brain.program, prevProgram = brain.prevProgram, animPlay = brain.animPlay })
    -- FIX(TASK-010): she PERFORMS the animation standing on this tile, and
    -- NPCAnimPlay walks straight onto it -- Programs.lua:1295-1302 takes the
    -- picked square and moves to ssq:getX()+0.5 with no AdjacentFreeTileFinder
    -- step. An unstandable pick livelocks exactly as BUG-017 did.
    end, BanditsNPC.Base and BanditsNPC.Base.STAND_OPTS)
end

-- Stop a playing animation and hand control back to the previous program.
function BanditsNPC.Interact.StopAnim(zombie)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie)
    if not brain then return end
    local prev = brain.prevProgram or { name = "NPCCompanion" }
    brain.prevProgram = nil
    brain.animPlay = false
    brain.program = { name = prev.name or "NPCCompanion", stage = "Prepare" }
    syncBrain(zombie, brain, { program = brain.program, animPlay = false })
end

-- FIX(BUG-017) / TASK-010: the "can she actually stand here" predicate and its opts
-- table USED TO LIVE HERE as locals. They moved to BanditsNPCBase.lua (Base.IsStandable
-- / Base.STAND_OPTS) once three more callers in two other files needed them -- the note
-- at the SQUARE TESTS section below says these are defined once in Base and delegated
-- to, and this was the exception. The full rationale (why isFree(false), why the
-- boolean is false, the stairs quirk, and the flooring analysis) moved with it; it is
-- not lost, it is at the definition.
--
-- Referenced as `BanditsNPC.Base and BanditsNPC.Base.STAND_OPTS` at each call site
-- rather than cached in a local here: a local would bind at LOAD time and this file is
-- client/ while Base is shared/. If Base ever fails to load, the call sites pass nil,
-- which is exactly the pre-fix behaviour rather than a crash. Same defensive idiom as
-- Programs.lua:631.

-- Stay: pick one tile; the NPC walks there and holds position.
function BanditsNPC.Interact.OrderStay(zombie)
    if not BanditsNPC.MayCommand(zombie) then return end
    local player = getSpecificPlayer(0)
    if not player then return end
    BanditsNPCTileCursor.Begin(player:getPlayerNum(), 1, function(picks)
        local brain = BanditBrain.Get(zombie)
        if not brain or not picks[1] then return end
        brain.stayPos = { x = math.floor(picks[1].x), y = math.floor(picks[1].y), z = picks[1].z or 0 }
        brain.program = { name = "NPCStay", stage = "Prepare" }
        syncBrain(zombie, brain, { program = brain.program, stayPos = brain.stayPos })
        confirmOverHead(zombie, BanditsNPC.T("UI_BN_Say_Stay", "Staying here."))
    end, BanditsNPC.Base and BanditsNPC.Base.STAND_OPTS)
end

-- THE SQUARE TESTS BELOW ARE DEFINED ONCE, IN BanditsNPCBase.lua, and delegated to
-- from here. They used to be duplicated: the base scanner needs exactly the same
-- questions ("is there a bed here", "is there water here") that these cursor
-- validators ask, and two copies of a rule that must agree is how they stop agreeing.
-- Everything hard-won about them -- that B42 moved world-object water off
-- getWaterAmount, that Is("waterPiped") is an unproven call whose dispatch error
-- bypasses pcall -- is documented at the definitions.

-- True if you could sleep on this square. NOTE this is IsSleepable, not IsBed:
-- chairs carry the engine's bed flag too, and a player who points the cursor at an
-- armchair and says "that is her bed" has said what they meant. The automatic scan
-- uses the stricter Base.IsBed, which excludes chairs, because guessing that an
-- armchair is somebody's bed would be wrong.
local function isBedSquare(square)
    return BanditsNPC.Base.IsSleepable(square)
end

-- True if the square holds any object with a container (crate, fridge, shelf...).
local function hasContainerSquare(square)
    return BanditsNPC.Base.HasContainer(square)
end

-- True if the square holds a water source (sink/bath/toilet/barrel...).
local function isWaterSquare(square)
    return BanditsNPC.Base.IsWater(square)
end

-- per-spot cursor behavior: which spots take a facing (rotate key + blue tile)
-- and which tiles they accept at all. Bed is strict (only real bed furniture);
-- chair/tv are deliberately validator-free so ANY modded sofa/seat tile works.
local SPOT_OPTS = {
    bed      = { direction = true, validate = isBedSquare },
    chair    = { direction = true },
    tv       = { direction = true },
    food     = { validate = hasContainerSquare },
    reading  = { validate = hasContainerSquare },
    weapon   = { validate = hasContainerSquare },
    healing  = { validate = hasContainerSquare },
    bathroom = { validate = isWaterSquare },
}

-- "Relax at Base": she lives around the base -- wanders, sits, runs her needs
-- proactively, goes to bed on time.
--
-- WITH A BASE AREA DRAWN THERE IS NOTHING TO PICK. The area already says where the
-- base is and what shape it is, and the furniture scan already knows where the beds
-- and chairs are, so asking the player to place another cursor is asking them to
-- repeat themselves. Without an area, the tile picker is still the only way to say
-- where "here" is, so it stays as the fallback -- this is a shortcut, not a
-- replacement.
function BanditsNPC.Interact.OrderRelax(zombie)
    if not BanditsNPC.MayCommand(zombie) then return end
    local player = getSpecificPlayer(0)
    if not player then return end

    local site = BanditsNPC.Base and BanditsNPC.Base.SiteFor(zombie)
    if site and BanditsNPC.Base.Zone(site) then
        local brain = BanditBrain.Get(zombie)
        if brain then
            -- nil anchor on purpose: NPCRelax settles her somewhere inside the area,
            -- preferring indoors, the first time it runs.
            brain.relaxPos = BanditsNPC.Base.Anchor(site)
            brain.relaxGoal = nil
            brain.program = { name = "NPCRelax", stage = "Prepare" }
            syncBrain(zombie, brain, { program = brain.program, relaxPos = brain.relaxPos })
            confirmOverHead(zombie, BanditsNPC.T("UI_BN_Say_RelaxBase", "I'll make myself at home."))
        end
        return
    end

    BanditsNPCTileCursor.Begin(player:getPlayerNum(), 1, function(picks)
        local brain = BanditBrain.Get(zombie)
        if not brain or not picks[1] then return end
        brain.relaxPos = { x = math.floor(picks[1].x), y = math.floor(picks[1].y), z = picks[1].z or 0 }
        brain.relaxGoal = nil
        brain.program = { name = "NPCRelax", stage = "Prepare" }
        syncBrain(zombie, brain, { program = brain.program, relaxPos = brain.relaxPos })
        confirmOverHead(zombie, BanditsNPC.T("UI_BN_Say_Relax", "I'll make myself at home here."))
    -- FIX(TASK-010): relaxPos becomes a Move target at Programs.lua:625, walked
    -- onto directly. NOTE this validates only the ANCHOR the player picks -- the
    -- pastime spots NPCRelax picks for ITSELF are a separate defect (BUG-021) and
    -- are NOT fixed by this. Do not read a green tile here as "Relax is safe".
    end, BanditsNPC.Base and BanditsNPC.Base.STAND_OPTS)
end

-- Assign a routine spot/container (bed, chair, TV, or a container tile) by
-- tile-highlight. Stored on brain.spots[key] for the routine AI to use.
function BanditsNPC.Interact.BeginSetSpot(zombie, key, label)
    if not BanditsNPC.MayCommand(zombie) then return end
    local player = getSpecificPlayer(0)
    if not player then return end
    BanditsNPCTileCursor.Begin(player:getPlayerNum(), 1, function(picks)
        local p = picks[1]
        local brain = BanditBrain.Get(zombie)
        if not brain or not p then return end
        brain.spots = brain.spots or {}
        brain.spots[key] = { x = math.floor(p.x), y = math.floor(p.y), z = p.z or 0, dir = p.dir }
        syncBrain(zombie, brain, { spots = brain.spots })
        confirmOverHead(zombie, BanditsNPC.T("UI_BN_Say_NotedMy", "Noted - my") .. " " .. (label or key) .. ".")
        -- QOL: reopen the NPC window on the Orders tab so you can assign the next spot
        if BanditsNPCDialogWindow then
            BanditsNPCDialogWindow.OpenFor(zombie)
            if BanditsNPCDialogWindow.instance then BanditsNPCDialogWindow.instance:selectSection("orders") end
        end
    end, SPOT_OPTS[key])
end

-- ===== clothing =====
-- Bandits dresses NPCs from brain.clothing (bodyLocation -> itemType) as visuals;
-- Bandit.ApplyVisuals re-applies it every tick. So we edit brain.clothing, sync,
-- and force an immediate re-dress.

-- B42's item:getBodyLocation() returns an ItemBodyLocation OBJECT (tostring like
-- "base:maskeyes"), NOT the plain "MaskEyes" string Bandits compares brain.clothing keys
-- against (Bandit.ApplyVisuals matches keys to BanditCompatibility.GetBodyLocationsOrdered()
-- entries verbatim). A raw object key means: the garment never renders, string concats on it
-- throw ("__concat not defined for operands: base:maskeyes" -- the Workshop clothing bug),
-- and the object poisons the brain for GlobalModData serialization. Funnel EVERY location
-- through this: returns the canonical Bandits key string, or nil if unresolvable.
local canonLocs = nil
function BanditsNPC.Interact.NormalizeBodyLocation(loc)
    if loc == nil then return nil end
    if not canonLocs then
        -- PUBLISH ONLY ON SUCCESS (v0.75.26). canonLocs was assigned BEFORE the pcall and
        -- the rebuild test is "is it non-nil?", so one transient failure of
        -- GetBodyLocationsOrdered() cached an EMPTY table forever -- after which every
        -- body location fell through to its raw lowercase string, garments missed the
        -- ordered layering pass, and nothing ever retried.
        local built = {}
        pcall(function()
            for _, name in pairs(BanditCompatibility.GetBodyLocationsOrdered()) do
                built[string.lower(name)] = name
            end
        end)
        local any = false
        for _ in pairs(built) do any = true; break end
        if any then canonLocs = built end
        -- Empty build: do NOT cache it, and answer this one call with the raw value so
        -- the caller still gets something usable. The next call retries the build.
        if not canonLocs then
            return (type(loc) == "string") and loc or tostring(loc)
        end
    end
    local s
    if type(loc) == "string" then
        s = loc
    else
        pcall(function() if loc.getId then s = loc:getId() end end)
        if type(s) ~= "string" then s = nil end
        if not s then
            local ok, ts = pcall(tostring, loc)
            if ok and type(ts) == "string" then s = ts end
        end
    end
    if type(s) ~= "string" or s == "" then return nil end
    -- strip any namespace prefix: "base:maskeyes" / "Base.MaskEyes" -> "maskeyes"
    local bare = s:match("[:%.]([^:%.]+)$") or s
    -- unmatched = a MODDED/custom body location Bandits' curated list doesn't know
    -- (clothing mods register their own, e.g. blazers). Accept it as-is: our redress
    -- renders non-list locations after the ordered pass, so they work anyway.
    return canonLocs[string.lower(bare)] or canonLocs[string.lower(s)] or bare
end

-- Re-apply clothing/bag visuals from brain.clothing UNCONDITIONALLY, then sync.
-- Bandit.ApplyVisuals can NOT be used for re-dressing: its first act is a guard that
-- returns when the skin texture starts with "FemaleBody"/"MaleBody" -- which is exactly
-- what Bandit.GetSkinTexture SETS on first provisioning. So ApplyVisuals works exactly
-- once per bandit and every later call is a silent no-op (why wear/undress never changed
-- the sprite). It also resets health as a side effect. This mirrors just its clothing
-- mechanism (ItemVisuals + resetModel) with no guard and no side effects.
-- ===== garment appearance capture / restore (30 Jul) =====
--
-- WHY: PZ clothing has randomized appearance -- an ItemVisual carries hue, tint,
-- baseTexture, textureChoice and decal, and the engine ROLLS whichever of those are still
-- unset the first time it renders the garment. Our redress path clears the visual list and
-- builds fresh ItemVisuals, so every re-dress handed the engine a blank slate and it rolled
-- again: "Clean up randomizes her clothing colours on EVERY click", and her outfit came back
-- a different colour after a reload. v0.53 captured only textureChoice and baseTexture and
-- explicitly gave up on the rest ("rare hue-randomized garments may still shift shade") --
-- that judgement was wrong. It is not rare, and hue is the field you actually SEE.
--
-- The field list is not guesswork: ItemVisual.copyVisualFrom in the 42.20 bytecode copies
-- exactly hue, tint, baseTexture, textureChoice and decal, and nothing else. That is the
-- complete appearance set.
--
-- ONLY ZERO-ARG GETTERS ARE USED, deliberately. The ONE-ARG overloads (getHue(ClothingItem)
-- etc.) are the engine's lazy roll-and-cache -- pickUninitializedValues does nothing but
-- call those five and throw the results away -- but it guards them with ClothingItem.isReady(),
-- which is NOT public in 42.20 (javap) and therefore uncallable from Lua, so we cannot
-- reproduce its guard. We read the values off visuals the engine has already rendered
-- instead, where the fields are populated. Decal is the one exception: it has no zero-arg
-- getter, so it goes through getDecal(getClothingItem()) -- both public, arity matched.
--
-- NULL_HUE is +Infinity (javap -constants), which is how "not rolled yet" is spotted.
-- Capture is FIRST-WINS PER FIELD, not per item: a value that was not rolled yet gets
-- picked up on a later pass instead of being locked out by a half-filled record.
local function captureLook(iv, rec)
    rec = rec or {}
    pcall(function()
        if rec.hue == nil and iv.getHue then
            local h = iv:getHue()
            -- h == h rejects NaN; < math.huge rejects NULL_HUE
            if type(h) == "number" and h == h and h < math.huge then rec.hue = h end
        end
        if rec.bt == nil and iv.getBaseTexture then
            local v = iv:getBaseTexture()
            if type(v) == "number" and v >= 0 then rec.bt = v end
        end
        if rec.tc == nil and iv.getTextureChoice then
            local v = iv:getTextureChoice()
            if type(v) == "number" and v >= 0 then rec.tc = v end
        end
        if rec.tr == nil and iv.getTint then
            local t = iv:getTint()
            if t and t.getRedFloat then
                rec.tr, rec.tg, rec.tb = t:getRedFloat(), t:getGreenFloat(), t:getBlueFloat()
            end
        end
        if rec.decal == nil and iv.getClothingItem and iv.getDecal then
            pcall(function()
                local ci = iv:getClothingItem()
                if ci then
                    local d = iv:getDecal(ci)
                    if type(d) == "string" and d ~= "" then rec.decal = d end
                end
            end)
        end
    end)
    return rec
end

local function applyLook(iv, rec)
    if not rec then return end
    pcall(function()
        if rec.bt and iv.setBaseTexture then iv:setBaseTexture(rec.bt) end
        if rec.tc and iv.setTextureChoice then iv:setTextureChoice(rec.tc) end
        if rec.hue and iv.setHue then iv:setHue(rec.hue) end
        if rec.decal and iv.setDecal then iv:setDecal(rec.decal) end
        if rec.tr and iv.setTint then
            iv:setTint(ImmutableColor.new(rec.tr, rec.tg, rec.tb, 1))
        end
    end)
end

-- Record what a REAL item looks like, so the companion wears THAT garment rather than a
-- re-rolled copy of its type. Called as the item is handed over and consumed: brain.clothing
-- only ever stores a type string, so without this the blue shirt you gave her could be
-- painted on as the red one. Overwrites any earlier record for the type -- this is a
-- different physical garment now.
-- Exported so the persistence manifest can use the same five-field capture the clothing
-- system uses. Keeping one implementation matters here: these five are the whole of an
-- item's rolled appearance, and a second copy would drift (v0.75.17).
function BanditsNPC.Interact.CaptureLook(iv, rec) return captureLook(iv, rec) end
function BanditsNPC.Interact.ApplyLook(iv, rec) return applyLook(iv, rec) end

function BanditsNPC.Interact.CaptureItemLook(item, brain, itemType)
    if not (item and brain) then return end
    local t = itemType or (item.getFullType and item:getFullType())
    if not t then return end
    pcall(function()
        local iv = item.getVisual and item:getVisual()
        if not iv then return end
        brain.npcClothVar = brain.npcClothVar or {}
        brain.npcClothVar[t] = captureLook(iv, nil)
    end)
end

-- Stamp a companion's saved appearance onto a REAL item. Taking a garment off a companion
-- creates a fresh item from a type string (brain.clothing stores types, not items), and a
-- fresh item is re-rolled by the engine -- so a shirt you lent her came back a different
-- colour. This is the fix for that; the record is the same one the visuals use.
function BanditsNPC.Interact.RestoreItemLook(item, brain, itemType)
    if not (item and brain and brain.npcClothVar) then return item end
    local rec = brain.npcClothVar[itemType or (item.getFullType and item:getFullType())]
    if not rec then return item end
    pcall(function()
        local iv = item.getVisual and item:getVisual()
        if iv then applyLook(iv, rec) end
    end)
    return item
end

function BanditsNPC.Interact.ApplyClothingVisuals(zombie, brain)
    pcall(function()
        local itemVisuals = zombie:getItemVisuals()
        local vars = brain.npcClothVar or {}
        brain.npcClothVar = vars
        pcall(function()
            for i = 0, itemVisuals:size() - 1 do
                local iv = itemVisuals:get(i)
                local t = iv and iv.getItemType and iv:getItemType()
                if t then vars[t] = captureLook(iv, vars[t]) end
            end
        end)
        itemVisuals:clear()
        zombie:getWornItems():clear()
        local clothing = brain.clothing or {}
        local tint = brain.tint or {}
        local done = {}
        local function addVisual(loc, itype)
            local iv = ItemVisual.new()
            iv:setItemType(itype)
            iv:setClothingItemName(itype)
            applyLook(iv, vars[itype])
            -- an EXPLICIT tint (editor/profile colour) wins over the captured one
            if tint[loc] then
                local c = BanditUtils.dec2rgb(tint[loc])
                iv:setTint(ImmutableColor.new(c.r, c.g, c.b, 1))
            end
            itemVisuals:add(iv)
            done[loc] = true
        end
        -- Bandits' curated order first (correct layering), then any MODDED/custom
        -- locations its list doesn't know (blazers etc.) on top
        for _, loc in pairs(BanditCompatibility.GetBodyLocationsOrdered()) do
            if clothing[loc] then addVisual(loc, clothing[loc]) end
        end
        for loc, itype in pairs(clothing) do
            if type(loc) == "string" and not done[loc] then addVisual(loc, itype) end
        end
        -- the carried-bag visual is also an ItemVisual (our clear() wiped it) -- re-add.
        -- It goes through applyLook like every garment: the bag was the ONE visual with no
        -- capture at all, so its texture variant re-rolled on every redress -- "even her
        -- backpack colours change on every click". The fixed dark tint is Bandits' own
        -- (Bandit.lua:242-244) and is kept, so only the variant is pinned.
        if brain.bag and brain.bag.name then
            local iv = ItemVisual.new()
            iv:setItemType(brain.bag.name)
            iv:setClothingItemName(brain.bag.name)
            applyLook(iv, vars[brain.bag.name])
            iv:setTint(ImmutableColor.new(0.1, 0.1, 0.1, 1))
            itemVisuals:add(iv)
        end

        -- HAIR CHOSEN AT THE APPEARANCE WORKSTATION, re-applied on every restore.
        --
        -- Bandit.ApplyVisuals does read brain.hairStyle and friends, but only inside an
        -- `else` branch that does not run on every path, and a companion whose haircut
        -- reverted after a reload would be worse than never offering the feature.
        -- Belt and braces on something the player explicitly chose.
        if BanditsNPC.Appearance and BanditsNPC.Appearance.Apply then
            pcall(function() BanditsNPC.Appearance.Apply(zombie, brain) end)
        end

        zombie:resetModelNextFrame()
        zombie:resetModel()
    end)
end

function BanditsNPC.Interact.Redress(zombie, brain)
    BanditsNPC.Interact.ApplyClothingVisuals(zombie, brain)
    -- npcClothVar must go in the SAME sync as clothing/tint. It is what stops a re-roll, and
    -- ApplyClothingVisuals is what fills it in -- so it is synced AFTER the redress, not
    -- before, or the record we just captured would be left out of the cluster copy and a
    -- restore would come back with a re-rolled outfit.
    syncBrain(zombie, brain, {
        clothing = brain.clothing, tint = brain.tint, npcClothVar = brain.npcClothVar,
    })
end

local function redress(zombie, brain)
    BanditsNPC.Interact.Redress(zombie, brain)
end

-- Where does this item go? VANILLA IDIOM, verbatim from ISInventoryPaneContextMenu.lua:1690:
--     (item:IsClothing() or item:IsInventoryContainer()) and item:getBodyLocation()
--         or item:canBeEquipped()
-- We only ever called getBodyLocation(), which is nil for a whole class of equippables --
-- most importantly BACKPACKS (InventoryContainer extends InventoryItem directly, NOT
-- Clothing, so IsClothing() is false and getBodyLocation() is empty; the location lives on
-- canBeEquipped()). Some modded garments register the same way. Returns a normalized
-- location string, or nil.
function BanditsNPC.Interact.LocationOf(item)
    if not item then return nil end
    local loc
    pcall(function()
        local isWearable = (item.IsClothing and item:IsClothing())
                        or (item.IsInventoryContainer and item:IsInventoryContainer())
        if isWearable and item.getBodyLocation then loc = item:getBodyLocation() end
        if (loc == nil or loc == "") and item.canBeEquipped then loc = item:canBeEquipped() end
    end)
    if loc == nil or loc == "" then return nil end
    return BanditsNPC.Interact.NormalizeBodyLocation(loc)
end

-- True if this item is a BAG (backpack/duffel/satchel...). Bags are not Clothing and are
-- painted from brain.bag, not brain.clothing -- see WearBag.
function BanditsNPC.Interact.IsBag(item)
    local r = false
    pcall(function() r = (item.IsInventoryContainer and item:IsInventoryContainer()) == true end)
    return r
end

-- Put a BAG on the companion. Bandits paints the carried bag from brain.bag.name as its own
-- ItemVisual (Bandit.lua:234-243, mirrored in ApplyClothingVisuals) -- there was simply no
-- way to SET it, so "give her a backpack" was impossible: the Wear path gated on
-- IsClothing(), which is false for every container, and answered "That isn't a clothing
-- item." Any bag she was already wearing goes back into her inventory, never deleted.
function BanditsNPC.Interact.WearBag(zombie, itemType)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie)
    if not (brain and itemType) then return false end
    local prev = brain.bag and brain.bag.name
    if prev and prev ~= itemType then
        -- AddItem builds a FRESH item from the type string, which the engine re-rolls; stamp
        -- the saved appearance back on so the bag she hands back is the one she was wearing
        pcall(function()
            BanditsNPC.Interact.RestoreItemLook(zombie:getInventory():AddItem(prev), brain, prev)
        end)
    end
    brain.bag = { name = itemType }
    -- APPLY FIRST, THEN SYNC (v0.75.7, audit Cluster C). ApplyClothingVisuals is what
    -- FILLS brain.npcClothVar, so syncing before it shipped the old table and left the
    -- freshly-captured appearance on this client only -- every other client re-rolled
    -- the garment's colour. Redress:1231 already had the right order; these two did not.
    BanditsNPC.Interact.ApplyClothingVisuals(zombie, brain)
    syncBrain(zombie, brain, { bag = brain.bag, npcClothVar = brain.npcClothVar })
    return true
end

-- Take the bag off. Returns the removed type (so the caller can hand it over), or nil.
function BanditsNPC.Interact.RemoveBag(zombie)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie)
    local prev = brain and brain.bag and brain.bag.name
    if not prev then return nil end
    brain.bag = false
    -- Apply first, then sync -- same reason as WearBag above.
    BanditsNPC.Interact.ApplyClothingVisuals(zombie, brain)
    syncBrain(zombie, brain, { bag = false, npcClothVar = brain.npcClothVar })
    return prev
end

-- Force-wear a clothing item at its body location. Returns true only if the garment was
-- actually applied (callers must NOT consume the item otherwise).
function BanditsNPC.Interact.WearClothing(zombie, itemType, bodyLocation)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie)
    bodyLocation = BanditsNPC.Interact.NormalizeBodyLocation(bodyLocation)
    if not brain or not itemType or not bodyLocation then return false end
    brain.clothing = brain.clothing or {}
    -- slot already occupied: the old garment goes into her bag, never silently deleted --
    -- and keeps the colour it had on her (AddItem re-rolls a fresh item, see RestoreItemLook)
    local prev = brain.clothing[bodyLocation]
    if prev and prev ~= itemType then
        pcall(function()
            BanditsNPC.Interact.RestoreItemLook(zombie:getInventory():AddItem(prev), brain, prev)
        end)
    end
    brain.clothing[bodyLocation] = itemType
    brain.tint = brain.tint or {}
    brain.tint[bodyLocation] = nil   -- new garment shows its default colour
    redress(zombie, brain)
    return true
end

-- Take off whatever occupies a body location.
function BanditsNPC.Interact.RemoveClothing(zombie, bodyLocation)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie)
    bodyLocation = BanditsNPC.Interact.NormalizeBodyLocation(bodyLocation)
    if not brain or not brain.clothing or not bodyLocation then return false end
    if brain.clothing[bodyLocation] == nil then return false end
    brain.clothing[bodyLocation] = nil
    if brain.tint then brain.tint[bodyLocation] = nil end
    redress(zombie, brain)
    return true
end

-- Clear an assigned routine spot (bed/chair/tv/...).
function BanditsNPC.Interact.ClearSpot(zombie, key)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie)
    if not brain or not brain.spots then return end
    brain.spots[key] = nil
    syncBrain(zombie, brain, { spots = brain.spots })
end

-- Clear the designated workstation tile.
function BanditsNPC.Interact.ClearWorkstation(zombie)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie)
    if not brain then return end
    brain.workstation = nil
    syncBrain(zombie, brain, { workstation = false })
end

-- Guard: pick two tiles; the NPC patrols between them with random pauses.
function BanditsNPC.Interact.OrderGuard(zombie)
    if not BanditsNPC.MayCommand(zombie) then return end
    local player = getSpecificPlayer(0)
    if not player then return end
    BanditsNPCTileCursor.Begin(player:getPlayerNum(), 2, function(picks)
        local brain = BanditBrain.Get(zombie)
        if not brain or not picks[1] or not picks[2] then return end
        brain.guardA = { x = math.floor(picks[1].x), y = math.floor(picks[1].y), z = picks[1].z or 0 }
        brain.guardB = { x = math.floor(picks[2].x), y = math.floor(picks[2].y), z = picks[2].z or 0 }
        brain.guardTarget = "a"
        brain.program = { name = "NPCGuard", stage = "Prepare" }
        syncBrain(zombie, brain, { program = brain.program, guardA = brain.guardA, guardB = brain.guardB })
        confirmOverHead(zombie, BanditsNPC.T("UI_BN_Say_Guard", "Guarding this area."))
    -- FIX(TASK-010): BOTH patrol tiles become Move targets (Programs.lua:866), so
    -- both must be standable. This is the case the author reproduced deliberately
    -- in logs/guard-livelock-trace.txt -- one tile next to furniture, green
    -- confirmation, then 44 seconds frozen.
    --
    -- ONE VALID TILE AND ONE INVALID IS NOT A HALF-APPLIED ORDER. Validation is
    -- per tile at click time: tryBuild returns early on an invalid square
    -- (TileCursor.lua:95) so it never enters self.picks, and the callback only
    -- runs once #picks >= count (:99). The first tile is banked, the bad second
    -- click does nothing, the cursor stays live until they pick a reachable one.
    -- The `not picks[1] or not picks[2]` guard above covers an abandoned cursor.
    end, BanditsNPC.Base and BanditsNPC.Base.STAND_OPTS)
end

-- ===== keybind =====

table.insert(keyBinding, { value = "[Bandits NPC]" })
-- BACK TO V (v0.77.13). The E experiment cost players their lives, which is not a figure
-- of speech: three separate Workshop reporters described opening a door onto a zombie and
-- having the profile window take the screen at that exact moment. The "it only fires with
-- a companion in front of you" reasoning below was true and still wrong -- standing beside
-- BOTH a companion and a door is not the rare case, it is the normal case at a base, and
-- the cost of the overlap is paid precisely when you can least afford it.
--
-- V is contextually taken by vanilla (VehicleRadialMenu, AnimalRadialMenu -- keyBinding.lua
-- 132 and 187), which is exactly the arrangement this mod shipped with for its whole life
-- before v0.76.3, and nobody reported it.
table.insert(keyBinding, { value = BanditsNPC.Interact.KEYBIND, key = Keyboard.KEY_V })

local function onKeyPressed(key)
    -- Not while typing in chat. V is an ordinary letter, so without this every chat
    -- message containing one opened the companion dialog (v0.75.7, audit Cluster D).
    if ISChat and ISChat.focused then return end
    if key == getCore():getKey(BanditsNPC.Interact.KEYBIND) then
        BanditsNPC.Interact.Open()
    end
end

Events.OnKeyPressed.Add(onKeyPressed)

-- ===== right-click "Talk to <Name>" (in addition to the key) =====

-- Closest friendly bandit-NPC to the clicked square (within ~2 tiles). Uses the
-- live zombie list rather than square:getZombie() (which only returns one zombie
-- per square and is unreliable for bandits).
local function findFriendlyNpcNear(square)
    if not square then return nil end
    local sx, sy, sz = square:getX(), square:getY(), square:getZ()
    local zl = getCell():getZombieList()
    local best, bestD
    for i = 0, zl:size() - 1 do
        local z = zl:get(i)
        if z and z:isAlive() and z:getVariableBoolean("Bandit") and math.abs(z:getZ() - sz) < 1 then
            local brain = BanditBrain.Get(z)
            if brain and not (brain.hostile or brain.hostileP) then
                local d = BanditUtils.DistTo(z:getX(), z:getY(), sx + 0.5, sy + 0.5)
                if d <= 2.0 and (not bestD or d < bestD) then bestD = d; best = z end
            end
        end
    end
    return best
end

local function onTalkContextMenu(playerID, context, worldobjects, test)
    if test then return end
    local player = getSpecificPlayer(playerID)
    if not player then return end

    local square
    if BanditCompatibility and BanditCompatibility.GetClickedSquare then
        square = BanditCompatibility.GetClickedSquare()
    end
    if not square and worldobjects then
        for _, o in ipairs(worldobjects) do
            if o.getSquare and o:getSquare() then square = o:getSquare(); break end
        end
    end

    local zombie = findFriendlyNpcNear(square)
    if not zombie then return end

    local brain = BanditBrain.Get(zombie)
    local name = (brain and brain.fullname) or "Survivor"
    context:addOption(BanditsNPC.T("UI_BN_Ctx_TalkTo", "Talk to") .. " " .. name, zombie, BanditsNPCDialogWindow.OpenFor)
    -- quick access to the wash-up without opening the dialog (owner only)
    if brain and brain.recruited and BanditsNPC.IsMine(brain) then
        context:addOption(BanditsNPC.T("UI_BN_Ctx_Tell", "Tell") .. " " .. name .. " " .. BanditsNPC.T("UI_BN_Ctx_ToCleanUp", "to clean up"), zombie, BanditsNPC.Interact.CleanUp)
    end
end

Events.OnFillWorldObjectContextMenu.Add(onTalkContextMenu)
