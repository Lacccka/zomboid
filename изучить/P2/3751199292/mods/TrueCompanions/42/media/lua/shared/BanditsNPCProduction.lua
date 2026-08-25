--
-- Bandits NPC - Companions Overhaul - Production (v1)
--
-- A recruited companion can convert materials into products based on their
-- profession, at a matching workstation.
--
-- v1 model (simplified per design): inputs are consumed from the PLAYER's
-- inventory; a timed job runs (NOT instant); when it finishes the outputs are
-- placed in the NPC's own inventory (collect them via the Items tab). The NPC
-- walking to the station and playing a work animation is a later pass.
--
-- Workstations are world objects tagged in modData (object.getModData()
-- .npcWorkstation = "<type>"); production scans near the NPC for a match.
-- Sprites below are placeholders that are guaranteed to render -- swap them for
-- nicer stove/lab/bench sprites once we confirm the names you want.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Production = {}

-- global multiplier on recipe times (user wanted production ~2/3 faster)
BanditsNPC.Production.TimeMult = 1 / 3

-- Higher-tier stations finish jobs faster (multiplies the job time).
BanditsNPC.Production.TierSpeed = { [1] = 1.0, [2] = 0.7, [3] = 0.45 }

-- Global output multiplier (user feedback: yields were a little low).
BanditsNPC.Production.OutputMult = 1.5

local E = Bandit and Bandit.Expertise or {}

-- expertise -> station type
--
-- BUILT KEY BY KEY, NOT AS A LITERAL (v0.75.7, audit R10). This was a table constructor
-- using [E.Cook] etc. directly, and when Bandits is absent E is {} -- so every key is nil
-- and `[nil] = "cooking"` raises "table index is nil", a hard load error with no call
-- frame to guard. Worse, the mod HAS a friendly message for exactly that case
-- (CheckDependencies: "requires the Bandits mod... enabled and loaded first") but it runs
-- on OnGameBoot, AFTER every file has loaded -- so in the one scenario it was written for
-- it could never be reached. Skipping nil keys lets the file load and lets that message
-- do its job. mod.info's require=Bandits2 makes this rare, but a broken install is
-- precisely when a clear message is worth most.
local STATION_BY_EXP = {}
local function mapExp(id, station)
    if id ~= nil then STATION_BY_EXP[id] = station end
end
mapExp(E.Cook,        "cooking")
mapExp(E.Medic,       "chemistry")
mapExp(E.Mechanic,    "mechanical")
mapExp(E.Repairman,   "mechanical")
mapExp(E.Electrician, "electronics")

-- Display labels: the KEY (cooking, chemistry...) is the internal station id and
-- must never change; the VALUE is translatable. Wrapped at load via BanditsNPC.T
-- (like the other UI tables) -- PZ requires a restart to change language, so the
-- baked value is always in the active language.
BanditsNPC.Production.StationLabel = {
    cooking     = BanditsNPC.T("UI_BN_Station_cooking", "Cooking Station"),
    chemistry   = BanditsNPC.T("UI_BN_Station_chemistry", "Medical Station"),
    mechanical  = BanditsNPC.T("UI_BN_Station_mechanical", "Mechanical Workbench"),
    electronics = BanditsNPC.T("UI_BN_Station_electronics", "Electronics Bench"),
    science     = BanditsNPC.T("UI_BN_Station_science", "Science Lab"),
    farming     = BanditsNPC.T("UI_BN_Station_farming", "Farming Station"),
    carpentry   = BanditsNPC.T("UI_BN_Station_carpentry", "Carpentry Bench"),
    tailoring   = BanditsNPC.T("UI_BN_Station_tailoring", "Tailoring Station"),
    reloading   = BanditsNPC.T("UI_BN_Station_reloading", "Reloading Bench"),
    smithing    = BanditsNPC.T("UI_BN_Station_smithing", "Forge & Anvil"),
    hunting     = BanditsNPC.T("UI_BN_Station_hunting", "Butchering Bench"),
}

-- Translated DISPLAY name for a recipe. recipe.name doubles as an internal job
-- key (the v0.42 Stew lesson -- never translate the stored name), so translation
-- happens only at render time: key = UI_BN_Recipe_<sanitized name>, English name
-- as fallback. Unknown/modded recipes fall back to their raw name.
function BanditsNPC.Production.RecipeName(rawName)
    if not rawName then return "" end
    local k = tostring(rawName):gsub("[^%w]+", "_"):gsub("^_", ""):gsub("_$", "")
    return BanditsNPC.T("UI_BN_Recipe_" .. k, rawName)
end

-- work animations per station, picked at random each cycle. Uses existing
-- "bump" anims (reaching/pouring/tool motions); truly bespoke stir/butcher
-- motions would need custom-authored animations.
BanditsNPC.Production.WorkAnims = {
    cooking     = {"Loot", "LootLow", "Refuel"},
    chemistry   = {"Loot", "LootLow", "Refuel"},
    mechanical  = {"LootLow", "DigShovel", "Rake"},
    electronics = {"Loot", "LootLow"},
}

function BanditsNPC.Production.GetWorkAnim(stationType)
    local set = BanditsNPC.Production.WorkAnims[stationType]
    if not set or #set == 0 then return "Loot" end
    return set[ZombRand(#set) + 1]
end

-- sprite-name fragments that mark an object as a valid station of each type
local STATION_OBJ_PATTERNS = {
    cooking     = {"stove","oven","grill","bbq","barbecue","cooking","campfire","hotplate","range","cooker"},
    chemistry   = {"chem","lab","alchemy","still","alembic","distill","medical"},
    mechanical  = {"workbench","carpentry","metalwork","metal_work","forge","anvil","tablesaw","grind","crafted_metal"},
    electronics = {"electronic","television","radio","computer","arcade","amplifier"},
}

-- Is this world object an acceptable station for the given profession?
function BanditsNPC.Production.IsValidStationObject(object, stationType)
    if not object or not object.getSprite then return false end
    -- already one of our tagged workstations of this type
    if object:getModData().npcWorkstation == stationType then return true end
    -- cooking accepts any real stove/oven/microwave
    if stationType == "cooking" and instanceof(object, "IsoStove") then return true end

    local sprite = object:getSprite()
    local name = sprite and sprite:getName()
    if not name then return false end
    name = string.lower(name)
    for _, frag in ipairs(STATION_OBJ_PATTERNS[stationType] or {}) do
        if string.find(name, frag, 1, true) then return true end
    end
    return false
end

-- placeholder sprites (confirmed to render); change freely
BanditsNPC.Production.StationSprite = {
    cooking     = "camping_01_0",
    chemistry   = "camping_01_2",
    mechanical  = "construction_01_8",
    electronics = "trash_01_11",
}

-- recipes per station. inputs = {fullType = count}. Times are in game-hours.
-- (Item strings are data -- if any are invalid they just won't produce; tell
--  me and I'll correct them.)
-- Recipes turn basic/common materials into scarcer, genuinely-useful items, so a
-- companion's trade is worth keeping them fed and housed. Times are pre-multiplier
-- (TimeMult shortens them); rarer outputs take longer.
BanditsNPC.Production.Recipes = {
    -- "Base.Stew" does not exist in B42 (the pot/bowl items are PotOfStew/StewBowl)
    -- -- the three stew recipes ate the inputs and produced NOTHING ("craft jobs do
    -- nothing"). Bowl of Stew is the correct ready-to-eat item.
    cooking = {
        { name = "Hearty Stew",     inputs = {["Base.TinnedBeans"]=1, ["Base.CannedCarrots2"]=1}, output = "Base.StewBowl",    outCount = 1, time = 2 },
        { name = "Cooked Meal",     inputs = {["Base.TinnedSoup"]=2},                             output = "Base.StewBowl",    outCount = 1, time = 2 },
        { name = "Smoked Jerky",    inputs = {["Base.Steak"]=2, ["Base.Salt"]=1},                 output = "Base.BeefJerky",   outCount = 3, time = 4 },
        { name = "Salted Rations",  inputs = {["Base.MeatPatty"]=2, ["Base.Salt"]=1},             output = "Base.BeefJerky",   outCount = 2, time = 3 },
        { name = "Big Cookout",     inputs = {["Base.Steak"]=2, ["Base.TinnedBeans"]=1},          output = "Base.StewBowl",    outCount = 3, time = 2, minTier = 3 },
    },
    chemistry = {
        { name = "Clean Bandages",  inputs = {["Base.RippedSheets"]=2},                           output = "Base.Bandage",        outCount = 2, time = 1 },
        { name = "Alcohol Bandages",inputs = {["Base.Bandage"]=2, ["Base.Disinfectant"]=1},       output = "Base.AlcoholBandage", outCount = 3, time = 2 },
        { name = "Painkillers",     inputs = {["Base.Disinfectant"]=2},                           output = "Base.Pills",          outCount = 2, time = 3 },
        { name = "Suture Needles",  inputs = {["Base.Wire"]=1, ["Base.Thread"]=1},                output = "Base.SutureNeedle",   outCount = 2, time = 2 },
        { name = "Suture Kit",      inputs = {["Base.SutureNeedle"]=1, ["Base.Thread"]=2},        output = "Base.SutureNeedleHolder", outCount = 1, time = 2 },
        { name = "Antibiotics",     inputs = {["Base.Disinfectant"]=2, ["Base.Pills"]=2},         output = "Base.Antibiotics",    outCount = 1, time = 9 },
    },
    mechanical = {
        { name = "Salvage Screws",  inputs = {["Base.ScrewsBox"]=1},                              output = "Base.Screws",         outCount = 5, time = 1 },
        { name = "Forge Nails",     inputs = {["Base.ScrapMetal"]=2},                             output = "Base.Nails",          outCount = 8, time = 1 },
        { name = "Repair Glue",     inputs = {["Base.DuctTape"]=2},                               output = "Base.Glue",           outCount = 1, time = 1 },
        { name = "Metal Pipe",      inputs = {["Base.ScrapMetal"]=2},                             output = "Base.MetalPipe",      outCount = 1, time = 1 },
        { name = "Sheet Metal",     inputs = {["Base.ScrapMetal"]=3},                             output = "Base.SmallSheetMetal",outCount = 1, time = 2 },
        { name = "Metal Bar",       inputs = {["Base.ScrapMetal"]=3},                             output = "Base.MetalBar",       outCount = 1, time = 2 },
        { name = "Welding Rods",    inputs = {["Base.ScrapMetal"]=2},                             output = "Base.WeldingRods",    outCount = 2, time = 2 },
    },
    electronics = {
        { name = "Reclaim Battery", inputs = {["Base.ElectronicsScrap"]=2},                       output = "Base.Battery",        outCount = 1, time = 2 },
        { name = "Make Wire",       inputs = {["Base.ElectronicsScrap"]=1},                       output = "Base.ElectricWire",   outCount = 2, time = 1 },
        { name = "Light Bulbs",     inputs = {["Base.ElectronicsScrap"]=2},                       output = "Base.LightBulb",      outCount = 2, time = 1 },
        { name = "Amplifier",       inputs = {["Base.ElectronicsScrap"]=3, ["Base.ElectricWire"]=2}, output = "Base.Amplifier",   outCount = 1, time = 6 },
    },
    -- ===== new trades (Stage 3) -- all item types script-verified =====
    science = {
        { name = "Painkillers",      inputs = {["Base.Disinfectant"]=2},                          output = "Base.Pills",       outCount = 2, time = 2, minTier = 1 },
        -- was Base.WaterDrop: a HIDDEN technical item players can never hold, so the
        -- recipe could never start ("You don't have the materials" with a full bag)
        { name = "Purified Pills",   inputs = {["Base.Disinfectant"]=1, ["Base.WaterPurificationTablets"]=1}, output = "Base.Pills", outCount = 2, time = 2, minTier = 1 },
        { name = "Antibiotics",      inputs = {["Base.Disinfectant"]=2, ["Base.Pills"]=2},         output = "Base.Antibiotics", outCount = 1, time = 6, minTier = 2 },
        { name = "Gunpowder Batch",  inputs = {["Base.Disinfectant"]=3, ["Base.ScrapMetal"]=1},    output = "Base.GunPowder",   outCount = 2, time = 4, minTier = 3 },
    },
    farming = {
        { name = "Fertilizer",       inputs = {["Base.CompostBag"]=1},                            output = "Base.Fertilizer",  outCount = 2, time = 1, minTier = 1 },
        { name = "Dig Worms",        inputs = {["Base.CompostBag"]=1},                            output = "Base.Worm",        outCount = 3, time = 1, minTier = 1 },
        { name = "Bulk Fertilizer",  inputs = {["Base.CompostBag"]=2},                            output = "Base.Fertilizer",  outCount = 5, time = 2, minTier = 2 },
        { name = "Rich Compost",     inputs = {["Base.CompostBag"]=3, ["Base.Worm"]=4},           output = "Base.Fertilizer",  outCount = 8, time = 3, minTier = 3 },
    },
    carpentry = {
        { name = "Cut Planks",       inputs = {["Base.Log"]=1},                                   output = "Base.Plank",       outCount = 3, time = 1, minTier = 1 },
        { name = "Forge Nails",      inputs = {["Base.ScrapMetal"]=2},                            output = "Base.Nails",       outCount = 8, time = 1, minTier = 1 },
        { name = "Salvage Screws",   inputs = {["Base.ScrewsBox"]=1},                             output = "Base.Screws",      outCount = 6, time = 1, minTier = 2 },
        { name = "Bulk Lumber",      inputs = {["Base.Log"]=3, ["Base.Nails"]=2},                 output = "Base.Plank",       outCount = 12, time = 2, minTier = 3 },
    },
    tailoring = {
        { name = "Make Thread",      inputs = {["Base.RippedSheets"]=2},                          output = "Base.Thread",      outCount = 2, time = 1, minTier = 1 },
        { name = "Sew Bandages",     inputs = {["Base.RippedSheets"]=2},                          output = "Base.Bandage",     outCount = 2, time = 1, minTier = 1 },
        { name = "Twine Cord",       inputs = {["Base.RippedSheets"]=3},                          output = "Base.Twine",       outCount = 3, time = 1, minTier = 2 },
        { name = "Sewing Needle",    inputs = {["Base.Wire"]=1},                                  output = "Base.Needle",      outCount = 1, time = 1, minTier = 2 },
        { name = "Bulk Thread",      inputs = {["Base.RippedSheets"]=4, ["Base.Twine"]=1},        output = "Base.Thread",      outCount = 6, time = 2, minTier = 3 },
    },
    reloading = {
        { name = "9mm Rounds",       inputs = {["Base.GunPowder"]=1, ["Base.ScrapMetal"]=1},      output = "Base.Bullets9mm",    outCount = 10, time = 2, minTier = 1 },
        { name = ".38 Special",      inputs = {["Base.GunPowder"]=1, ["Base.ScrapMetal"]=1},      output = "Base.Bullets38",     outCount = 8,  time = 2, minTier = 1 },
        { name = ".45 ACP",          inputs = {["Base.GunPowder"]=2, ["Base.ScrapMetal"]=1},      output = "Base.Bullets45",     outCount = 8,  time = 2, minTier = 2 },
        { name = ".44 Magnum",       inputs = {["Base.GunPowder"]=2, ["Base.ScrapMetal"]=1},      output = "Base.Bullets44",     outCount = 6,  time = 2, minTier = 2 },
        { name = "Shotgun Shells",   inputs = {["Base.GunPowder"]=2, ["Base.ScrapMetal"]=1},      output = "Base.ShotgunShells", outCount = 6,  time = 2, minTier = 2 },
        { name = ".357 Magnum",      inputs = {["Base.GunPowder"]=2, ["Base.SmallSheetMetal"]=1}, output = "Base.Bullets357",    outCount = 6,  time = 3, minTier = 3 },
        { name = "Bulk 9mm",         inputs = {["Base.GunPowder"]=3, ["Base.SmallSheetMetal"]=1}, output = "Base.Bullets9mm",    outCount = 30, time = 3, minTier = 3 },
        { name = "Bulk Shotgun",     inputs = {["Base.GunPowder"]=4, ["Base.SmallSheetMetal"]=1}, output = "Base.ShotgunShells", outCount = 18, time = 3, minTier = 3 },
    },
    smithing = {
        { name = "Forge Nails",      inputs = {["Base.ScrapMetal"]=2},                            output = "Base.Nails",          outCount = 8, time = 1, minTier = 1 },
        { name = "Metal Bar",        inputs = {["Base.ScrapMetal"]=3},                            output = "Base.MetalBar",       outCount = 1, time = 1, minTier = 1 },
        { name = "Metal Pipe",       inputs = {["Base.ScrapMetal"]=2},                            output = "Base.MetalPipe",      outCount = 1, time = 1, minTier = 2 },
        { name = "Sheet Metal",      inputs = {["Base.ScrapMetal"]=3},                            output = "Base.SmallSheetMetal",outCount = 1, time = 2, minTier = 2 },
        { name = "Welding Rods",     inputs = {["Base.ScrapMetal"]=2},                            output = "Base.WeldingRods",    outCount = 2, time = 2, minTier = 3 },
    },
    hunting = {
        { name = "Smoked Jerky",     inputs = {["Base.Steak"]=2, ["Base.Salt"]=1},                output = "Base.BeefJerky", outCount = 3, time = 3, minTier = 1 },
        { name = "Salted Rations",   inputs = {["Base.MeatPatty"]=2, ["Base.Salt"]=1},            output = "Base.BeefJerky", outCount = 2, time = 2, minTier = 1 },
        { name = "Trapper's Twine",  inputs = {["Base.RippedSheets"]=2},                          output = "Base.Twine",     outCount = 2, time = 1, minTier = 2 },
        { name = "Preserved Meat",   inputs = {["Base.Steak"]=3, ["Base.Salt"]=2},                output = "Base.BeefJerky", outCount = 6, time = 4, minTier = 3 },
    },
}

-- Tier-gate some of the original recipes too (done here so the aligned literal
-- table above stays untouched). minTier defaults to 1 for anything not listed.
local TIER_OVERRIDES = {
    cooking     = { ["Smoked Jerky"]=2, ["Salted Rations"]=2 },
    chemistry   = { ["Alcohol Bandages"]=2, ["Suture Needles"]=2, ["Suture Kit"]=3, ["Antibiotics"]=3 },
    mechanical  = { ["Metal Pipe"]=2, ["Sheet Metal"]=2, ["Metal Bar"]=2, ["Welding Rods"]=3 },
    electronics = { ["Light Bulbs"]=2, ["Amplifier"]=3 },
}
for st, m in pairs(TIER_OVERRIDES) do
    for _, r in ipairs(BanditsNPC.Production.Recipes[st] or {}) do
        if m[r.name] then r.minTier = m[r.name] end
    end
end

-- ===== helpers =====

function BanditsNPC.Production.GetStationType(brain)
    -- Stations registry covers all 11 trades (incl. the new ones); prefer it.
    if BanditsNPC.Stations and BanditsNPC.Stations.GetType then
        local st = BanditsNPC.Stations.GetType(brain)
        if st then return st end
    end
    if not brain or not brain.exp then return nil end
    for _, id in ipairs(brain.exp) do
        if id and STATION_BY_EXP[id] then return STATION_BY_EXP[id] end
    end
    return nil
end

local function countItem(player, fullType)
    local list = ArrayList.new()
    player:getInventory():getAllEvalRecurse(function(it) return it:getFullType() == fullType end, list)
    return list:size()
end

local function removeItem(player, fullType, n)
    local list = ArrayList.new()
    player:getInventory():getAllEvalRecurse(function(it) return it:getFullType() == fullType end, list)
    local r = 0
    for i = 0, list:size() - 1 do
        if r >= n then break end
        local it = list:get(i)
        local c = it:getContainer()
        if c then c:Remove(it); r = r + 1 end
    end
end

-- Returns the workstation object near the NPC matching stationType, or nil.
function BanditsNPC.Production.FindStation(zombie, stationType)
    local cell = getCell()
    local zx, zy, zz = math.floor(zombie:getX()), math.floor(zombie:getY()), zombie:getZ()
    local r = 8
    local function scan(list)
        if not list then return nil end
        for i = 0, list:size() - 1 do
            local o = list:get(i)
            if o and o.getModData and o:getModData().npcWorkstation == stationType then
                return o
            end
        end
        return nil
    end
    for dy = -r, r do
        for dx = -r, r do
            local sq = cell:getGridSquare(zx + dx, zy + dy, zz)
            if sq then
                -- built workstations are IsoThumpables (special objects); designated
                -- real furniture lives in the normal object list -- scan both.
                local found = scan(sq:getObjects()) or scan(sq:getSpecialObjects())
                if found then return found end
            end
        end
    end
    return nil
end

function BanditsNPC.Production.HasInputs(player, recipe, qty)
    for t, c in pairs(recipe.inputs) do
        if countItem(player, t) < c * qty then return false end
    end
    return true
end

-- The same question HasInputs asks, but ANSWERED PER INGREDIENT so the Job tab can show
-- have / need instead of one silent "you can't". It shares countItem with HasInputs and
-- Start rather than counting its own way -- a requirements panel that disagreed with the
-- check that actually runs would be worse than no panel.
--
-- Sorted by name so the list does not reshuffle between frames: recipe.inputs is a hash and
-- pairs() gives no order.
function BanditsNPC.Production.InputStatus(player, recipe, qty)
    local out = {}
    if not (player and recipe and recipe.inputs) then return out end
    qty = math.max(1, qty or 1)
    for t, c in pairs(recipe.inputs) do
        local need = c * qty
        local have = countItem(player, t)
        out[#out + 1] = { type = t, need = need, have = have, ok = (have >= need) }
    end
    table.sort(out, function(a, b) return a.type < b.type end)
    return out
end

-- How many of this recipe the player has the ingredients for right now. The qty stepper
-- shows it as "max N" so the answer arrives before the failed Produce rather than after.
function BanditsNPC.Production.MaxQty(player, recipe)
    if not (player and recipe and recipe.inputs) then return 0 end
    local best
    for t, c in pairs(recipe.inputs) do
        if c > 0 then
            local n = math.floor(countItem(player, t) / c)
            if best == nil or n < best then best = n end
        end
    end
    return best or 0
end

-- Begins a production job. Returns success(boolean), message(string).
function BanditsNPC.Production.Start(zombie, stationType, recipe, qty)
    -- Production writes brain.prodJob, which makes it a companion mutator, and every one of
    -- those goes through MayCommand ("gating the buttons would mean auditing every handler
    -- in three windows"). This module was missed when that rule was applied: without it
    -- another player could put YOUR companion to work -- spending their own ingredients,
    -- but occupying her and claiming the output. The Job tab is owner-only, but that is the
    -- UI being the gate, which is the arrangement MayCommand exists to replace.
    if not BanditsNPC.MayCommand(zombie) then return false, "" end
    local player = getSpecificPlayer(0)
    local brain = BanditBrain.Get(zombie)
    if not (player and brain and recipe) then return false, "" end
    qty = math.max(1, qty or 1)

    if brain.prodJob and brain.prodJob ~= false then
        return false, BanditsNPC.T("UI_BN_Prod_Busy", "They're already working on something.")
    end
    local station = BanditsNPC.Production.FindStation(zombie, stationType)
    if not station then
        return false, BanditsNPC.TF("UI_BN_Prod_NoStation", "No %1 nearby.", (BanditsNPC.Production.StationLabel[stationType] or stationType))
    end
    local tier = 1
    pcall(function() tier = station:getModData().npcWorkstationTier or 1 end)
    if (recipe.minTier or 1) > tier then
        return false, BanditsNPC.TF("UI_BN_Prod_NeedTier", "That recipe needs a tier-%1 station (this one is tier %2).", (recipe.minTier or 1), tier)
    end
    local needsPower = BanditsNPC.Stations and BanditsNPC.Stations.TierNeedsPower and BanditsNPC.Stations.TierNeedsPower(stationType, tier)
    if needsPower and BanditsNPC.Stations.HasPower then
        if not BanditsNPC.Stations.HasPower(station:getSquare()) then
            return false, BanditsNPC.T("UI_BN_Prod_NeedPower", "This station needs power -- it runs on grid power, or a generator nearby.")
        end
    end
    if not BanditsNPC.Production.HasInputs(player, recipe, qty) then
        return false, BanditsNPC.T("UI_BN_Prod_NoMaterials", "You don't have the materials.")
    end
    -- never eat inputs for an output the game can't create (B42 renamed items;
    -- an unknown output would silently produce nothing) -- fail LOUD, consume nothing
    if not BanditCompatibility.InstanceItem(recipe.output) then
        return false, BanditsNPC.TF("UI_BN_Prod_UnknownItem", "Recipe error: unknown item %1 -- please report this.", tostring(recipe.output))
    end

    for t, c in pairs(recipe.inputs) do
        removeItem(player, t, c * qty)
    end

    -- remember exactly where the station is so the NPC can walk back to it
    local ssq = station:getSquare()
    if ssq then brain.workstation = { x = ssq:getX(), y = ssq:getY(), z = ssq:getZ() } end

    local now = getGameTime():getWorldAgeHours()
    brain.prodJob = {
        output = recipe.output,
        count = math.ceil(recipe.outCount * qty * (BanditsNPC.Production.OutputMult or 1)),
        name = recipe.name,
        finishHour = now + (recipe.time or 1) * qty * BanditsNPC.Production.TimeMult * ((BanditsNPC.Production.TierSpeed and BanditsNPC.Production.TierSpeed[tier]) or 1),
    }

    -- send the NPC to work at the station; remember current orders to restore later
    if (brain.program and brain.program.name) ~= "NPCWork" then
        brain.prevProgram = brain.program
    end
    brain.program = { name = "NPCWork", stage = "Prepare" }

    BanditBrain.Update(zombie, brain)
    if Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(zombie, {
            id = brain.id, prodJob = brain.prodJob, workstation = brain.workstation,
            program = brain.program, prevProgram = brain.prevProgram,
        })
    end
    return true, BanditsNPC.TF("UI_BN_Prod_OnItStation", "On it -- heading to the %1.", (BanditsNPC.Production.StationLabel[stationType] or BanditsNPC.T("UI_BN_Prod_StationWord", "station")))
end

-- Completes a finished job: deposits outputs into the NPC inventory. Lazy-checked.
function BanditsNPC.Production.Update(zombie)
    local brain = BanditBrain.Get(zombie)
    if not brain then return end
    local job = brain.prodJob
    if not job or job == false then return end

    if getGameTime():getWorldAgeHours() >= job.finishHour then
        -- jobs started before v0.42 may still carry the invalid pre-fix output
        if job.output == "Base.Stew" then job.output = "Base.StewBowl" end
        local inv = zombie:getInventory()
        local made = 0
        for _ = 1, job.count do
            local item = BanditCompatibility.InstanceItem(job.output)
            if item then inv:AddItem(item); made = made + 1 end
        end
        if made == 0 then
            print("[BanditsNPC] production output unknown, job dropped: " .. tostring(job.output))
        end
        brain.prodJob = false
        BanditBrain.Update(zombie, brain)
        if Bandit.ForceSyncPart then Bandit.ForceSyncPart(zombie, { id = brain.id, prodJob = false }) end
    end
end

-- "Making 3x Battery -- ready in ~2h" / nil if idle.
function BanditsNPC.Production.StatusText(brain)
    local job = brain and brain.prodJob
    if not job or job == false then return nil end
    local remain = math.ceil(job.finishHour - getGameTime():getWorldAgeHours())
    if remain < 0 then remain = 0 end
    return BanditsNPC.TF("UI_BN_Prod_Making", "Making %1x %2 -- ready in ~%3h",
        job.count, BanditsNPC.Production.RecipeName(job.name or BanditsNPC.T("UI_BN_Prod_ItemWord", "item")), remain)
end

-- ===== workstation placement (client) =====

function BanditsNPC.Production.PlaceWorkstation(player, square, stationType)
    if not square then return end
    local sprite = BanditsNPC.Production.StationSprite[stationType]
    if not sprite then return end

    local obj = IsoObject.new(square, sprite, "")
    square:AddSpecialObject(obj)
    obj:getModData().npcWorkstation = stationType
    obj:transmitCompleteItemToClients()
    if player then player:Say(BanditsNPC.TF("UI_BN_Prod_Placed", "Placed %1.", (BanditsNPC.Production.StationLabel[stationType] or stationType))) end
end

-- Enter tile-select mode, then place the workstation on the chosen tile.
function BanditsNPC.Production.BeginPlace(player, stationType)
    if not (player and BanditsNPCTileCursor) then return end
    BanditsNPCTileCursor.Begin(player:getPlayerNum(), 1, function(picks)
        local p = picks[1]
        if not p then return end
        local sq = getCell():getGridSquare(p.x, p.y, p.z)
        BanditsNPC.Production.PlaceWorkstation(player, sq, stationType)
    end)
end

-- Work-tab "Select Workstation": tile-highlight, then tag the object on the
-- chosen tile as this NPC's station type (and remember the spot on the brain).
function BanditsNPC.Production.BeginSelectWorkstation(zombie)
    local player = getSpecificPlayer(0)
    local brain = BanditBrain.Get(zombie)
    if not (player and brain and BanditsNPCTileCursor) then return end
    local st = BanditsNPC.Production.GetStationType(brain)
    if not st then return end

    -- TASK-010 DELIBERATELY SKIPPED THIS CURSOR. DO NOT "COMPLETE THE SET".
    --
    -- Every other destination cursor now passes Base.STAND_OPTS, which rejects a
    -- tile that fails isFree(false). This one MUST NOT: the player is pointing at
    -- the workstation itself, and a workstation is a solid object, so the
    -- predicate would turn every single valid choice red and make the feature
    -- impossible to use. The tile being occupied is the POINT here, not a fault.
    --
    -- It is also not the livelock shape TASK-010 fixes. NPCWork does not walk onto
    -- this square -- Programs.lua:942 routes her to a free neighbour first:
    --     local stand = AdjacentFreeTileFinder.Find(ssq, bandit) or ssq
    -- which is the correct pattern the other program paths are missing (BUG-021).
    -- So the destination she actually walks to is already guaranteed standable.
    BanditsNPCTileCursor.Begin(player:getPlayerNum(), 1, function(picks)
        local p = picks[1]
        if not p then return end
        local sq = getCell():getGridSquare(p.x, p.y, p.z)
        BanditsNPC.Production.DesignateExisting(player, sq, st)
        brain.workstation = { x = p.x, y = p.y, z = p.z }
        BanditBrain.Update(zombie, brain)
        if Bandit.ForceSyncPart then Bandit.ForceSyncPart(zombie, { id = brain.id, workstation = brain.workstation }) end
    end)
end

-- Tag an EXISTING world object (e.g. a real stove/workbench) as a workstation,
-- so it has the correct in-game look. Tags the topmost non-floor object.
function BanditsNPC.Production.DesignateExisting(player, square, stationType)
    if not square then return end
    local objs = square:getObjects()
    local target
    for i = objs:size() - 1, 0, -1 do
        local o = objs:get(i)
        if BanditsNPC.Production.IsValidStationObject(o, stationType) then
            target = o
            break
        end
    end
    if not target then
        if player then player:Say(BanditsNPC.TF("UI_BN_Prod_NotValid", "That's not a valid %1.", (BanditsNPC.Production.StationLabel[stationType] or stationType))) end
        return
    end
    target:getModData().npcWorkstation = stationType
    target:transmitCompleteItemToClients()
    if player then player:Say(BanditsNPC.TF("UI_BN_Prod_SetAs", "Set as %1.", (BanditsNPC.Production.StationLabel[stationType] or stationType))) end
end
