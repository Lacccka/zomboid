require "Items/ProceduralDistributions"
require "PPO_LootDefinitions"

PPO = PPO or {}
PPO.LootDistribution = PPO.LootDistribution or {}

local Distribution = PPO.LootDistribution
local Definitions = PPO.LootDefinitions

-- OnPreDistributionMerge is the earliest event that still sees SandboxVars, but
-- nothing promises it fires once. Appending a second copy of every entry would
-- double every spawn chance silently.
Distribution.applied = false

-- SandboxVars is an ordinary table in the shipped game, but a mod that replaces
-- it with a proxy must not be able to take the distribution merge down with it.
local function readField(source, name)
    if type(source) ~= "table" then return nil end
    local ok, value = pcall(function() return source[name] end)
    if ok then return value end
    return nil
end

-- Raw values, not resolved ones: `ladderMultiplier` already falls back to
-- Normal on garbage, and duplicating that decision here would let the two
-- disagree.
local function stepsFrom(sandbox)
    local source = readField(sandbox, "PhysicalProgressionOverhaul")
    local resolved = {}
    for family, entry in pairs(Definitions.families) do
        resolved[family] = readField(source, entry.option)
    end
    return resolved
end

local function modifiersFrom(sandbox)
    local resolved = {}
    for lootType, entry in pairs(Definitions.vanillaOptions) do
        local value = readField(sandbox, entry.name)
        if type(value) ~= "number" then value = entry.default end
        resolved[lootType] = value
    end
    return resolved
end

-- Both arguments are passed in rather than read from the globals, so the whole
-- of the insertion is exercisable without a game.
function Distribution.apply(list, sandbox)
    if type(list) ~= "table" then
        print("[PPO Loot] no distribution list to write into")
        return 0
    end
    if Distribution.applied == true then return 0 end
    Distribution.applied = true

    local steps = stepsFrom(sandbox)
    local modifiers = modifiersFrom(sandbox)
    local inserted = 0
    for location, entries in pairs(Definitions.locations) do
        local target = list[location]
        local items = nil
        if type(target) == "table" then items = target.items end
        if type(items) ~= "table" then
            -- Neither AmmoMaker, AliceMod nor SapphCooking guards this, and a
            -- renamed vanilla table takes such a mod down whole. A named line
            -- in the server log is the difference between a five-minute fix and
            -- a bug report saying the mod stopped working.
            print("[PPO Loot] unknown distribution table, skipped: " .. location)
        else
            for _, pair in ipairs(entries) do
                local item = Definitions.items[pair[1]]
                if item ~= nil then
                    local weight = Definitions.insertedWeight(pair[2],
                        steps[item.family], modifiers[item.lootType])
                    if weight ~= nil then
                        table.insert(items, Definitions.typeName(pair[1]))
                        table.insert(items, weight)
                        inserted = inserted + 1
                    end
                end
            end
        end
    end
    print("[PPO Loot] inserted " .. tostring(inserted) .. " entries")
    return inserted
end

local function onPreDistributionMerge()
    local list = nil
    if ProceduralDistributions ~= nil then list = ProceduralDistributions.list end
    Distribution.apply(list, SandboxVars)
end

if Events ~= nil and Events.OnPreDistributionMerge ~= nil then
    Events.OnPreDistributionMerge.Add(onPreDistributionMerge)
end

return Distribution
