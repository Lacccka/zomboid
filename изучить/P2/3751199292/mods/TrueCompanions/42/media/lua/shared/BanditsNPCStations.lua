--
-- True Companions - Workstations (parts/group model + registry)
--
-- A companion's trade maps to a STATION TYPE. A station is a GROUP of one or more
-- PARTS (placed tiles) that share a type, a tier, and a hidden group id. Each tier is
-- a full composition of parts keyed by "slot"; upgrading from one tier to the next is
-- the diff:
--   * a slot whose piece changed  -> REPLACE in place (rebuild that piece, swap sprite)
--   * a slot that's new at the next tier -> ADD (place a new piece next to the group)
-- Both go through a walk-over + hammer build action (see BanditsNPCWorkstationBuild).
--
-- Pieces are real game furniture/craft tiles (verified single- or two-tile). Two-tile
-- pieces store their layout per orientation (h = east-west, v = north-south) taken from
-- the game's own SpriteConfig so they place WHOLE, not "one half".
--
-- This file is pure data + helpers (no placement); the client build flow and the world
-- right-click menu live in their own files.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Stations = {}
local S = BanditsNPC.Stations

-- trade (Bandit.Expertise name) -> station type.
local TYPE_BY_NAME = {
    Cook = "cooking", Medic = "chemistry", Mechanic = "mechanical",
    Repairman = "mechanical", Electrician = "electronics",
    Scientist = "science", Farmer = "farming", Builder = "carpentry",
    Tailor = "tailoring", Reloader = "reloading", Blacksmith = "smithing",
    Hunter = "hunting",
}

-- ===== PIECE catalog =====
-- tiles=1: faces = up to 4 facing sprites (rotation cycles them).
-- tiles=2: ori   = { h = {west, east}, v = {north, south} } (placed on 2 adjacent tiles).
-- skill = internal perk -> required level. materials = fullType -> count. power = needs
-- electricity to OPERATE (gates production, and marks the tier as powered).
S.Pieces = {
    -- wood (Carpentry)
    campfire        = { label=BanditsNPC.T("UI_BN_Piece_campfire", "Campfire"),          tiles=1, faces={"camping_03_16","camping_03_17","camping_03_18","camping_03_19"}, skill={Woodwork=1}, materials={["Base.Plank"]=2, ["Base.Stone2"]=3} },
    composter       = { label=BanditsNPC.T("UI_BN_Piece_composter", "Composter"),         tiles=1, faces={"camping_01_19","camping_01_20"},                                 skill={Woodwork=1}, materials={["Base.Plank"]=4, ["Base.Nails"]=4} },
    spinning_wheel  = { label=BanditsNPC.T("UI_BN_Piece_spinning_wheel", "Spinning Wheel"),    tiles=1, faces={"crafted_04_37","crafted_04_36","crafted_04_38","crafted_04_39"}, skill={Woodwork=2}, materials={["Base.Plank"]=4, ["Base.Nails"]=4} },
    -- v0.75.17: these three were declared tiles=1 while using MULTI-TILE sprites
    -- (SpriteGridPos present), so the build placed one half of a two-tile object and
    -- left the other half missing -- carpentry_01_24..27 is even declared BOTH ways in
    -- this same table. There is no single-tile equivalent in the tile data, so the
    -- pieces are what is wrong, not the sprites. Pairs are read straight off
    -- SpriteGridPos: x differs -> horizontal (0,0 + 1,0), y differs -> vertical
    -- (0,0 + 0,1), the same shape herb_rack already uses.
    sewing_bench    = { label=BanditsNPC.T("UI_BN_Piece_sewing_bench", "Sewing Bench"),      tiles=2, ori={ h={"carpentry_01_24","carpentry_01_25"}, v={"carpentry_01_27","carpentry_01_26"} }, skill={Woodwork=2}, materials={["Base.Plank"]=4, ["Base.Nails"]=6} },
    medical_counter = { label=BanditsNPC.T("UI_BN_Piece_medical_counter", "Medical Counter"),   tiles=1, faces={"location_community_medical_01_48","location_community_medical_01_49","location_community_medical_01_50","location_community_medical_01_51"}, skill={Woodwork=2}, materials={["Base.Plank"]=4, ["Base.Nails"]=6} },
    med_workstation = { label=BanditsNPC.T("UI_BN_Piece_med_workstation", "Medical Workstation"),tiles=2, ori={ h={"location_community_medical_01_116","location_community_medical_01_117"}, v={"location_community_medical_01_119","location_community_medical_01_118"} }, skill={Woodwork=3}, materials={["Base.Plank"]=4, ["Base.ScrapMetal"]=2, ["Base.Nails"]=6} },
    lab_desk        = { label=BanditsNPC.T("UI_BN_Piece_lab_desk", "Lab Desk"),          tiles=2, ori={ h={"location_community_medical_01_116","location_community_medical_01_117"}, v={"location_community_medical_01_119","location_community_medical_01_118"} }, skill={Woodwork=2}, materials={["Base.Plank"]=4, ["Base.ScrapMetal"]=2, ["Base.Nails"]=6} },
    stone_quern     = { label=BanditsNPC.T("UI_BN_Piece_stone_quern", "Stone Quern"),       tiles=1, faces={"crafted_02_40"},                                                 skill={Woodwork=2}, materials={["Base.Stone2"]=6} },
    anvil           = { label=BanditsNPC.T("UI_BN_Piece_anvil", "Anvil"),             tiles=1, faces={"crafted_01_18","crafted_01_19","crafted_01_20","crafted_01_21"}, skill={MetalWelding=1}, materials={["Base.ScrapMetal"]=4} },

    -- 2-tile benches (broad work surfaces)
    wood_bench      = { label=BanditsNPC.T("UI_BN_Piece_wood_bench", "Wood Workbench"),    tiles=2, ori={ h={"carpentry_01_24","carpentry_01_25"}, v={"carpentry_01_26","carpentry_01_27"} }, skill={Woodwork=2}, materials={["Base.Plank"]=6, ["Base.Nails"]=8} },
    steel_bench     = { label=BanditsNPC.T("UI_BN_Piece_steel_bench", "Steel Workbench"),   tiles=2, ori={ h={"location_business_machinery_01_16","location_business_machinery_01_17"}, v={"location_business_machinery_01_18","location_business_machinery_01_19"} }, skill={Woodwork=3, MetalWelding=2}, materials={["Base.ScrapMetal"]=4, ["Base.SmallSheetMetal"]=2, ["Base.Screws"]=6} },
    forge           = { label=BanditsNPC.T("UI_BN_Piece_forge", "Forge"),             tiles=2, ori={ h={"crafted_01_61","crafted_01_20"}, v={"crafted_01_21","crafted_01_62"} }, skill={Woodwork=2, MetalWelding=2}, materials={["Base.Stone2"]=8, ["Base.ScrapMetal"]=3, ["Base.SmallSheetMetal"]=1} },
    loom            = { label=BanditsNPC.T("UI_BN_Piece_loom", "Loom"),              tiles=2, ori={ h={"crafted_04_83","crafted_04_84"}, v={"crafted_04_81","crafted_04_80"} }, skill={Woodwork=3}, materials={["Base.Plank"]=6, ["Base.Nails"]=6, ["Base.Twine"]=2} },
    herb_rack       = { label=BanditsNPC.T("UI_BN_Piece_herb_rack", "Herb Drying Rack"),  tiles=2, ori={ h={"vegetation_drying_01_8","vegetation_drying_01_9"}, v={"vegetation_drying_01_1","vegetation_drying_01_0"} }, skill={Woodwork=2}, materials={["Base.Plank"]=4, ["Base.Nails"]=4, ["Base.Twine"]=2} },

    -- powered (Electrical) -- need electricity to operate
    electric_range  = { label=BanditsNPC.T("UI_BN_Piece_electric_range", "Electric Range"),    tiles=1, faces={"appliances_cooking_01_0","appliances_cooking_01_1","appliances_cooking_01_2","appliances_cooking_01_3"}, skill={MetalWelding=2, Electricity=2}, materials={["Base.SmallSheetMetal"]=2, ["Base.ElectricWire"]=2, ["Base.ElectronicsScrap"]=1}, power=true },
    charcoal_bbq    = { label=BanditsNPC.T("UI_BN_Piece_charcoal_bbq", "Charcoal Barbecue"), tiles=1, faces={"appliances_cooking_01_35","appliances_cooking_01_36","appliances_cooking_01_37","appliances_cooking_01_38"}, skill={Woodwork=2, MetalWelding=1}, materials={["Base.ScrapMetal"]=3, ["Base.SmallSheetMetal"]=1} },
    microscope      = { label=BanditsNPC.T("UI_BN_Piece_microscope", "Microscope"),        tiles=1, onBench=true, faces={"location_community_medical_01_136","location_community_medical_01_137","location_community_medical_01_138","location_community_medical_01_139"}, skill={Electricity=3}, materials={["Base.ElectronicsScrap"]=2, ["Base.ElectricWire"]=2, ["Base.SmallSheetMetal"]=1}, power=true },
    computer        = { label=BanditsNPC.T("UI_BN_Piece_computer", "Computer"),          tiles=1, onBench=true, faces={"appliances_com_01_72","appliances_com_01_73","appliances_com_01_74","appliances_com_01_75"}, skill={Electricity=2}, materials={["Base.ElectronicsScrap"]=3, ["Base.ElectricWire"]=2}, power=true },
    electronics_set = { label=BanditsNPC.T("UI_BN_Piece_electronics_set", "Electronics Unit"),  tiles=1, onBench=true, faces={"appliances_com_01_0","appliances_com_01_1","appliances_com_01_2","appliances_com_01_3"}, skill={Electricity=3}, materials={["Base.ElectronicsScrap"]=3, ["Base.ElectricWire"]=3}, power=true },
    metal_bandsaw   = { label=BanditsNPC.T("UI_BN_Piece_metal_bandsaw", "Metal Bandsaw"),     tiles=1, onBench=true, faces={"industry_02_265","industry_02_264","industry_02_267","industry_02_266"}, skill={MetalWelding=3, Electricity=2}, materials={["Base.ScrapMetal"]=3, ["Base.SmallSheetMetal"]=2, ["Base.ElectricWire"]=2, ["Base.ElectronicsScrap"]=1}, power=true },
    powered_grinder = { label=BanditsNPC.T("UI_BN_Piece_powered_grinder", "Powered Grinder"),   tiles=1, faces={"crafted_01_120","crafted_01_121","crafted_01_122","crafted_01_123"}, skill={MetalWelding=3, Electricity=2}, materials={["Base.ScrapMetal"]=3, ["Base.ElectricWire"]=2, ["Base.ElectronicsScrap"]=1}, power=true },
    reloading_press = { label=BanditsNPC.T("UI_BN_Piece_reloading_press", "Reloading Press"),   tiles=1, onBench=true, faces={"crafted_01_72","crafted_01_73"}, skill={MetalWelding=3, Electricity=2}, materials={["Base.SmallSheetMetal"]=2, ["Base.ElectricWire"]=2, ["Base.Screws"]=6}, power=true },
    bench_sander    = { label=BanditsNPC.T("UI_BN_Piece_bench_sander", "Bench Sander"),      tiles=1, onBench=true, faces={"industry_04_20","industry_04_21","industry_04_22","industry_04_23"}, skill={Woodwork=3, Electricity=2}, materials={["Base.ScrapMetal"]=2, ["Base.ElectricWire"]=2, ["Base.ElectronicsScrap"]=1}, power=true },
    chest_freezer   = { label=BanditsNPC.T("UI_BN_Piece_chest_freezer", "Chest Freezer"),     tiles=2, ori={ h={"appliances_refrigeration_01_20","appliances_refrigeration_01_21"}, v={"appliances_refrigeration_01_38","appliances_refrigeration_01_39"} }, skill={MetalWelding=2, Electricity=2}, materials={["Base.SmallSheetMetal"]=2, ["Base.ElectricWire"]=2, ["Base.ElectronicsScrap"]=2}, power=true },
}

-- ===== STATION registry =====
-- per type: label + tiers[1..3], each tier a list of { slot, piece } parts.
S.Defs = {
    cooking = { label=BanditsNPC.T("UI_BN_Station_cooking", "Cooking Station"), tiers = {
        {{slot="main", piece="campfire"}},
        {{slot="main", piece="charcoal_bbq"}},
        {{slot="main", piece="electric_range"}},
    }},
    chemistry = { label=BanditsNPC.T("UI_BN_Station_chemistry", "Medical Station"), tiers = {
        {{slot="main", piece="medical_counter"}},
        {{slot="main", piece="med_workstation"}},
        {{slot="main", piece="med_workstation"}, {slot="extra", piece="microscope"}},
    }},
    mechanical = { label=BanditsNPC.T("UI_BN_Station_mechanical", "Mechanical Workbench"), tiers = {
        {{slot="main", piece="wood_bench"}},
        {{slot="main", piece="steel_bench"}},
        {{slot="main", piece="steel_bench"}, {slot="extra", piece="metal_bandsaw"}},
    }},
    electronics = { label=BanditsNPC.T("UI_BN_Station_electronics", "Electronics Bench"), tiers = {
        {{slot="main", piece="wood_bench"}},
        {{slot="main", piece="steel_bench"}},
        {{slot="main", piece="steel_bench"}, {slot="extra", piece="electronics_set"}},
    }},
    science = { label=BanditsNPC.T("UI_BN_Station_science", "Science Lab"), tiers = {
        {{slot="main", piece="lab_desk"}},
        {{slot="main", piece="lab_desk"}, {slot="extra", piece="computer"}},
        {{slot="main", piece="lab_desk"}, {slot="extra", piece="microscope"}},
    }},
    farming = { label=BanditsNPC.T("UI_BN_Station_farming", "Farming Station"), tiers = {
        {{slot="main", piece="composter"}},
        {{slot="main", piece="composter"}, {slot="extra", piece="stone_quern"}},
        {{slot="main", piece="composter"}, {slot="extra", piece="stone_quern"}, {slot="extra2", piece="herb_rack"}},
    }},
    carpentry = { label=BanditsNPC.T("UI_BN_Station_carpentry", "Carpentry Bench"), tiers = {
        {{slot="main", piece="wood_bench"}},
        {{slot="main", piece="steel_bench"}},
        {{slot="main", piece="steel_bench"}, {slot="extra", piece="bench_sander"}},
    }},
    tailoring = { label=BanditsNPC.T("UI_BN_Station_tailoring", "Tailoring Station"), tiers = {
        {{slot="main", piece="spinning_wheel"}},
        {{slot="main", piece="spinning_wheel"}, {slot="extra", piece="loom"}},
        {{slot="main", piece="sewing_bench"}, {slot="extra", piece="loom"}},
    }},
    reloading = { label=BanditsNPC.T("UI_BN_Station_reloading", "Reloading Bench"), tiers = {
        {{slot="main", piece="wood_bench"}},
        {{slot="main", piece="steel_bench"}},
        {{slot="main", piece="steel_bench"}, {slot="extra", piece="reloading_press"}},
    }},
    smithing = { label=BanditsNPC.T("UI_BN_Station_smithing", "Forge & Anvil"), tiers = {
        {{slot="main", piece="anvil"}},
        {{slot="main", piece="anvil"}, {slot="extra", piece="forge"}},
        {{slot="main", piece="powered_grinder"}, {slot="extra", piece="forge"}},
    }},
    hunting = { label=BanditsNPC.T("UI_BN_Station_hunting", "Butchering Bench"), tiers = {
        {{slot="main", piece="wood_bench"}},
        {{slot="main", piece="steel_bench"}},
        {{slot="main", piece="steel_bench"}, {slot="extra", piece="chest_freezer"}},
    }},
}

S.MAX_TIER = 3

-- ===== type resolution =====
function S.BuildTypeMap()
    S.TypeByExp = {}
    if not (Bandit and Bandit.Expertise) then return end
    for name, st in pairs(TYPE_BY_NAME) do
        local id = Bandit.Expertise[name]
        if id then S.TypeByExp[id] = st end
    end
end

function S.GetType(brain)
    if not (brain and brain.exp) then return nil end
    if not S.TypeByExp then S.BuildTypeMap() end
    for _, id in ipairs(brain.exp) do
        local st = S.TypeByExp[id]
        if st then return st end
    end
    return nil
end

-- ONE SOURCE FOR THE STATION NAME (v0.75.34). There were TWO label tables --
-- Production.StationLabel and the `label` on each S.Defs entry -- and the same dialog
-- window read both, a few hundred lines apart. They agreed only because every pair
-- happened to resolve the same UI_BN_Station_* key, i.e. by coincidence maintained by
-- hand: rename a station in one table and the mod calls it two different things
-- depending on which code path the player reached it through.
--
-- Production loads BEFORE Stations (alphabetical, shared/), so this direction is the one
-- that works -- reading Production's table here rather than the reverse. The per-def
-- `label` stays as the fallback so a station type that only exists in S.Defs still names
-- itself, and nothing breaks if the load order ever changes.
function S.Label(stationType)
    local shared = BanditsNPC.Production and BanditsNPC.Production.StationLabel
    if shared and shared[stationType] then return shared[stationType] end
    return (S.Defs[stationType] and S.Defs[stationType].label) or stationType
end

function S.Piece(id) return S.Pieces[id] end

-- parts list for a (type, tier), or nil.
function S.TierParts(stationType, tier)
    local def = S.Defs[stationType]
    if not def or not def.tiers[tier] then return nil end
    return def.tiers[tier]
end

-- does operating at this tier require power (any powered part)?
function S.TierNeedsPower(stationType, tier)
    local parts = S.TierParts(stationType, tier)
    if not parts then return false end
    for _, p in ipairs(parts) do
        local pc = S.Pieces[p.piece]
        if pc and pc.power then return true end
    end
    return false
end

-- The diff to go from fromTier -> toTier: parts to REPLACE (slot piece changed) and to
-- ADD (new slot). Returns { replace = { {slot,piece} }, add = { {slot,piece} } }.
function S.UpgradeDiff(stationType, fromTier, toTier)
    local from, to = S.TierParts(stationType, fromTier), S.TierParts(stationType, toTier)
    local res = { replace = {}, add = {} }
    if not to then return res end
    local fromBySlot = {}
    for _, p in ipairs(from or {}) do fromBySlot[p.slot] = p.piece end
    for _, p in ipairs(to) do
        if fromBySlot[p.slot] == nil then
            table.insert(res.add, { slot = p.slot, piece = p.piece })
        elseif fromBySlot[p.slot] ~= p.piece then
            table.insert(res.replace, { slot = p.slot, piece = p.piece })
        end
    end
    return res
end

-- combined material/skill summary for a set of parts (for a tooltip).
function S.PartsCost(parts)
    local mats, skill = {}, {}
    for _, p in ipairs(parts or {}) do
        local pc = S.Pieces[p.piece]
        if pc then
            for ft, n in pairs(pc.materials or {}) do mats[ft] = (mats[ft] or 0) + n end
            for sk, lv in pairs(pc.skill or {}) do skill[sk] = math.max(skill[sk] or 0, lv) end
        end
    end
    return mats, skill
end

-- ===== skill / material checks =====
local function perkLevel(player, name)
    local p = Perks and Perks[name]
    if not p then return nil end
    local ok, lvl = pcall(function() return player:getPerkLevel(p) end)
    return ok and lvl or 0
end

function S.MeetsSkill(player, skill)
    if not skill then return true, "" end
    local miss = {}
    for name, lvl in pairs(skill) do
        local have = perkLevel(player, name)
        if have ~= nil and have < lvl then
            table.insert(miss, S.SkillLabel(name) .. " " .. lvl)
        end
    end
    return (#miss == 0), table.concat(miss, ", ")
end

local function countItem(player, fullType)
    local list = ArrayList.new()
    player:getInventory():getAllEvalRecurse(function(it) return it:getFullType() == fullType end, list)
    return list:size()
end

function S.HasMaterials(player, materials)
    local missing = {}
    for ft, n in pairs(materials or {}) do
        local have = countItem(player, ft)
        if have < n then
            table.insert(missing, (ft:gsub("^Base%.", "")) .. " " .. have .. "/" .. n)
        end
    end
    return (#missing == 0), table.concat(missing, ", ")
end

-- Consume materials from the player (recursing bags). Returns ok(boolean),
-- missing(string) so a caller can refuse to hand over the goods.
--
-- CHECKS FIRST, AND TAKES NOTHING IF ANYTHING IS SHORT (v0.75.6, audit Cluster E). It
-- used to remove "up to n" of each line and return nothing at all, so two different
-- callers went wrong in two different directions:
--   * the workstation build called it at the END of a timed action with no re-check,
--     so dropping the planks during the walk-and-hammer still produced the station;
--   * and any caller that DID want to refuse had no way to find out it had come up short.
-- Doing the check inside makes the operation atomic: a partial payment can no longer
-- happen, which is the failure that would be worse than either original bug -- materials
-- gone AND nothing built.
--
-- S.HasMaterials counts with getAllEvalRecurse, and the removal below walks the same
-- recursive view and removes from each item's OWN container, so the count and the take
-- agree at any nesting depth. That agreement is the entire fix for the beacon, whose
-- upgrade counted recursively but consumed with a top-level-only FindAndReturn -- so
-- materials kept in a bag were counted, never removed, and the upgrade was free.
function S.TakeMaterials(player, materials)
    if not player then return false, "" end
    local ok, missing = S.HasMaterials(player, materials)
    if not ok then return false, missing end

    for ft, n in pairs(materials or {}) do
        local list = ArrayList.new()
        player:getInventory():getAllEvalRecurse(function(it) return it:getFullType() == ft end, list)
        local removed = 0
        for i = 0, list:size() - 1 do
            if removed >= n then break end
            local it = list:get(i)
            local c = it:getContainer()
            if c then c:Remove(it); removed = removed + 1 end
        end
    end
    return true, ""
end

function S.MaterialText(materials)
    local parts = {}
    for ft, n in pairs(materials or {}) do
        table.insert(parts, (ft:gsub("^Base%.", "")) .. " x" .. n)
    end
    table.sort(parts)
    return table.concat(parts, ", ")
end

-- in-game display name for an internal perk ("Woodwork" -> "Carpentry").
local PERK_LABEL = { Woodwork="Carpentry", MetalWelding="Welding", Electricity="Electrical",
    Farming="Agriculture", Cooking="Cooking", Doctor="First Aid", Mechanics="Mechanics",
    Tailoring="Tailoring", Aiming="Aiming", Trapping="Trapping" }
function S.SkillLabel(name)
    local t = getText and getText("IGUI_perks_" .. name)
    if t and t ~= "" and not t:find("IGUI_perks_") then return t end
    return PERK_LABEL[name] or name
end

function S.SkillText(skill)
    local parts = {}
    for name, lvl in pairs(skill or {}) do table.insert(parts, S.SkillLabel(name) .. " " .. lvl) end
    table.sort(parts)
    return table.concat(parts, ", ")
end

-- ===== group / world scanning =====
local function eachSquareObject(sq, fn)
    if not sq then return end
    local function scan(list)
        if not list then return end
        for i = 0, list:size() - 1 do fn(list:get(i)) end
    end
    scan(sq:getObjects()); scan(sq:getSpecialObjects())
end

-- Returns the first workstation tile of stationType within radius of (x,y,z), or nil.
function S.FindStationTile(stationType, x, y, z, radius)
    local cell = getCell()
    if not cell then return nil end
    radius = radius or 12
    for dy = -radius, radius do
        for dx = -radius, radius do
            local sq = cell:getGridSquare(x + dx, y + dy, z)
            local found
            eachSquareObject(sq, function(o)
                if not found and o and o.getModData and o:getModData().npcWorkstation == stationType then found = o end
            end)
            if found then return found end
        end
    end
    return nil
end

-- All tiles belonging to a group id (scans a box around an origin tile).
function S.FindGroupTiles(groupId, x, y, z, radius)
    local out = {}
    local cell = getCell()
    if not (cell and groupId) then return out end
    radius = radius or 6
    for dy = -radius, radius do
        for dx = -radius, radius do
            eachSquareObject(cell:getGridSquare(x + dx, y + dy, z), function(o)
                if o and o.getModData and o:getModData().npcWorkstationGroup == groupId then table.insert(out, o) end
            end)
        end
    end
    return out
end

-- The tile of a given slot within a group, or nil.
function S.FindGroupSlotTile(groupId, slot, x, y, z)
    for _, o in ipairs(S.FindGroupTiles(groupId, x, y, z, 6)) do
        if o:getModData().npcWorkstationSlot == slot and o:getModData().npcWorkstationPrimary then return o end
    end
    for _, o in ipairs(S.FindGroupTiles(groupId, x, y, z, 6)) do
        if o:getModData().npcWorkstationSlot == slot then return o end
    end
    return nil
end

-- Is (sqx,sqy) [and its secondary tile] adjacent (4-dir) to any tile of group?
function S.IsAdjacentToGroup(x, y, z, secOffX, secOffY, groupId)
    local cell = getCell()
    if not cell then return false end
    local tiles = { {x, y} }
    if secOffX ~= 0 or secOffY ~= 0 then table.insert(tiles, { x + secOffX, y + secOffY }) end
    local function tileInGroup(tx, ty)
        local hit = false
        eachSquareObject(cell:getGridSquare(tx, ty, z), function(o)
            if o and o.getModData and o:getModData().npcWorkstationGroup == groupId then hit = true end
        end)
        return hit
    end
    for _, t in ipairs(tiles) do
        for _, d in ipairs({{1,0},{-1,0},{0,1},{0,-1}}) do
            if tileInGroup(t[1] + d[1], t[2] + d[2]) then return true end
        end
    end
    return false
end

-- ===== power =====
function S.HasPower(square)
    if not square then return false end
    local ok, powered = pcall(function() return square:haveElectricity() end)
    return ok and powered or false
end

-- ===== setup =====
S.BuildTypeMap()
if Events and Events.OnGameBoot then Events.OnGameBoot.Add(S.BuildTypeMap) end
