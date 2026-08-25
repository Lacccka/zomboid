-- ============================================================
-- PagerMod_Distribution.lua
-- Seeds the "Pager Tower Schematics" magazine into electronics/comms
-- loot — but ONLY when a tower is actually needed: a powered-tower signal
-- mode, OR receive-only pagers (then the tower is the only way to send).
-- The tower recipe can't be learnt any other way, so otherwise the magazine
-- never spawns and the tower can't be built — exactly when it isn't needed.
-- ============================================================

require "PagerMod_Shared"

-- Procedural-distribution lists to seed. B41 and B42 use slightly different
-- list names (e.g. "ElectronicStore..." vs "ElectronicsStore..."), so we list
-- both and silently skip any that don't exist in the running build.
local TARGET_LISTS = {
    -- B42
    "ElectronicStoreMagazines", "ElectronicStoreHAMRadio", "ElectronicStoreMisc",
    "RadioFactoryComponents", "ArmyStorageElectronics", "MechanicShelfElectric",
    "StoreShelfElectronics", "CrateElectronics", "GigamartHouseElectronics",
    -- B41 / older spellings
    "ElectronicsStoreMagazines", "ElectronicsStoreElectronics", "ElectronicsStoreMisc",
    "GarageElectronics",
}

local SCHEMATIC = "PagerMod.TowerSchematic"
local WEIGHT    = 8   -- modest rarity, in line with other recipe magazines

local function towerNeeded()
    local sv = SandboxVars and SandboxVars.PagerMod
    if not sv then return false end
    -- A tower is needed (so its recipe must be obtainable) when EITHER:
    --   * the network requires a tower (a powered-tower signal mode), or
    --   * pagers are receive-only — then the tower console is the only way to
    --     SEND a page, so players have to be able to craft one.
    if PagerMod.isTowerMode(sv.SignalMode) then return true end
    if sv.PagerMode == PagerMod.PagerMode.RECEIVE_ONLY then return true end
    return false
end

local function seedSchematic()
    if not towerNeeded() then return end
    local lists = ProceduralDistributions and ProceduralDistributions.list
    if not lists then return end

    local added = 0
    local seen = {}
    for _, name in ipairs(TARGET_LISTS) do
        local entry = lists[name]
        if entry and entry.items and not seen[name] then
            seen[name] = true
            table.insert(entry.items, SCHEMATIC)
            table.insert(entry.items, WEIGHT)
            added = added + 1
        end
    end
    print(string.format("[PagerMod] Tower mode on: seeded %s into %d loot list(s).", SCHEMATIC, added))
end

-- ── Pager spawn abundance (sandbox PagerMod.SpawnAbundance) ──────────────
-- B42's vanilla loot tables already scatter Base.Pager through electronics,
-- police and comms containers, so "abundance" is a multiplier on the weights
-- that are already there: the pager stays in the places it makes sense to find
-- one, there are just more (or fewer) of them. B41 has no vanilla pager, so at
-- the higher settings we also seed the mod's own item into the curated lists
-- below — that is the only way it reaches loot in that build.
--
-- The default (Vanilla) multiplier is exactly 1 and returns before touching a
-- single table, so an unconfigured game keeps the loot it has always had.

local PAGER_ITEM   = "Base.Pager"
local SEED_WEIGHT  = 4   -- vanilla's typical pager weight in an electronics list
local WALK_DEPTH   = 12  -- Distributions nests room -> container -> items

local function isPagerName(name)
    return name == "Pager" or name == PAGER_ITEM
end

-- Loot lists are flat { name, weight, name, weight, ... } arrays.
local function listHasPager(items)
    for i = 1, #items - 1, 2 do
        if isPagerName(items[i]) then return true end
    end
    return false
end

local function scaleList(items, mult)
    local scaled = 0
    for i = 1, #items - 1, 2 do
        local name, weight = items[i], items[i + 1]
        -- Anything that isn't a clean name/weight pair means this list doesn't
        -- follow the convention; leave it completely alone rather than guess.
        if type(name) ~= "string" or type(weight) ~= "number" then return scaled end
        if isPagerName(name) then
            local w = math.floor(weight * mult + 0.5)
            if w < 1 then w = 1 end   -- a weight of 0 would delete the spawn outright
            items[i + 1] = w
            scaled = scaled + 1
        end
    end
    return scaled
end

-- Walk a distribution tree and scale every pager entry in it. `seen` guards
-- against the shared sub-tables these files reuse (and any accidental cycle).
local function scaleTree(tbl, mult, seen, depth)
    if type(tbl) ~= "table" or seen[tbl] or depth > WALK_DEPTH then return 0 end
    seen[tbl] = true
    local scaled = 0
    local items = rawget(tbl, "items")
    if type(items) == "table" then scaled = scaled + scaleList(items, mult) end
    for _, v in pairs(tbl) do
        if type(v) == "table" and v ~= items then
            scaled = scaled + scaleTree(v, mult, seen, depth + 1)
        end
    end
    return scaled
end

-- Put pagers in the curated electronics/comms lists that don't already have
-- one. No-op in B42 (vanilla already stocks most of these); in B41 it is what
-- makes the setting do anything at all.
local function seedPagers(mult)
    local lists = ProceduralDistributions and ProceduralDistributions.list
    if not lists then return 0 end
    local weight = math.max(1, math.floor(SEED_WEIGHT * mult + 0.5))
    local added = 0
    for _, name in ipairs(TARGET_LISTS) do
        local entry = lists[name]
        if entry and entry.items and not listHasPager(entry.items) then
            table.insert(entry.items, PAGER_ITEM)
            table.insert(entry.items, weight)
            added = added + 1
        end
    end
    return added
end

local function applyAbundance()
    local sv = SandboxVars and SandboxVars.PagerMod
    local mode = (sv and sv.SpawnAbundance) or PagerMod.SpawnAbundance.VANILLA
    local mult = PagerMod.spawnAbundanceMult(mode)
    if mult == 1 then return end   -- Vanilla: touch nothing

    local roots, seen = {}, {}
    if ProceduralDistributions then table.insert(roots, ProceduralDistributions) end
    if Distributions           then table.insert(roots, Distributions) end
    if SuburbsDistributions    then table.insert(roots, SuburbsDistributions) end
    if VehicleDistributions    then table.insert(roots, VehicleDistributions) end

    local scaled = 0
    for _, root in ipairs(roots) do
        scaled = scaled + scaleTree(root, mult, seen, 1)
    end
    local added = mult > 1 and seedPagers(mult) or 0
    print(string.format("[PagerMod] Spawn abundance x%s: scaled %d pager loot entr(ies), seeded %d new.",
        tostring(mult), scaled, added))
end

Events.OnPreDistributionMerge.Add(seedSchematic)
Events.OnPreDistributionMerge.Add(applyAbundance)
