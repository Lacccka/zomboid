-- Replace the two legacy loaded/empty physical-part loot pairs with the rebuilt
-- single-state attachments. Existing old items remain valid for saves and the
-- legacy runtime stays available until final cutover; this changes only future
-- procedural loot generation.

require "Items/ProceduralDistributions"

MFSUnderbarrelLootMigration = MFSUnderbarrelLootMigration or {}
local Migration = MFSUnderbarrelLootMigration

Migration.VERSION = "1.0.0"

local REPLACEMENTS = {
    ["Gunpart.M203_cat"] = "Gunpart.MFS_M203",
    ["Gunpart.M203_cat_empty"] = "Gunpart.MFS_M203",
    ["Gunpart.GP25_cat"] = "Gunpart.MFS_GP25",
    ["Gunpart.GP25_cat_empty"] = "Gunpart.MFS_GP25",
}

local function migrateItems(items)
    if type(items) ~= "table" then return 0, nil end
    local weightsByReplacement = {}
    local removedPairs = 0
    local index = 1

    -- ProceduralDistribution items are flat item/weight pairs. Removing both
    -- old variants and summing their weights preserves the original chance of
    -- finding a launcher while producing one maintained physical part type.
    while index <= #items do
        local oldType = items[index]
        local replacement = REPLACEMENTS[oldType]
        if replacement then
            local weight = tonumber(items[index + 1]) or 0
            weightsByReplacement[replacement] =
                (weightsByReplacement[replacement] or 0) + weight
            table.remove(items, index + 1)
            table.remove(items, index)
            removedPairs = removedPairs + 1
        else
            index = index + 2
        end
    end

    for replacement, weight in pairs(weightsByReplacement) do
        table.insert(items, replacement)
        table.insert(items, weight)
    end
    return removedPairs, weightsByReplacement
end

local function migrateLoot()
    local lists = ProceduralDistributions and ProceduralDistributions.list
    if type(lists) ~= "table" then return end

    local changedLists = 0
    local removedPairs = 0
    local m203Weight = 0
    local gp25Weight = 0
    for _, distribution in pairs(lists) do
        local removed, weights = migrateItems(distribution and distribution.items)
        if removed > 0 then
            changedLists = changedLists + 1
            removedPairs = removedPairs + removed
            m203Weight = m203Weight + (weights["Gunpart.MFS_M203"] or 0)
            gp25Weight = gp25Weight + (weights["Gunpart.MFS_GP25"] or 0)
        end
    end

    print("[MFSUnderbarrelLoot] version " .. Migration.VERSION
        .. " migrated lists=" .. tostring(changedLists)
        .. " oldPairs=" .. tostring(removedPairs)
        .. " M203Weight=" .. tostring(m203Weight)
        .. " GP25Weight=" .. tostring(gp25Weight))
end

if Migration._callback then
    Events.OnPreDistributionMerge.Remove(Migration._callback)
end
Migration._callback = migrateLoot
Events.OnPreDistributionMerge.Add(Migration._callback)

