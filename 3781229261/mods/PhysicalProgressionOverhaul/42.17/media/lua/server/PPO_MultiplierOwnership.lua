PPO = PPO or {}
PPO.MultiplierOwnership = PPO.MultiplierOwnership or {}

local Ownership = PPO.MultiplierOwnership

function Ownership.new()
    return { records = {} }
end

local function recordsFor(manager, character, create)
    local records = manager.records[character]
    if records == nil and create then
        records = {}
        manager.records[character] = records
    end
    return records
end

function Ownership.acquire(manager, character, perk, level)
    if manager == nil or character == nil or perk == nil then return false end
    local records = recordsFor(manager, character, true)
    if records[perk] ~= nil then return true end

    local map = nil
    local original = nil
    local originalPresent = false
    local ok, record = pcall(function()
        local xp = character:getXp()
        map = xp:getMultiplierMap()
        original = map:get(perk)
        originalPresent = original ~= nil
        local originalMultiplier = nil
        if originalPresent then
            originalMultiplier = xp:getMultiplier(perk)
            map:remove(perk)
        end
        local targetLevel = level >= 10 and 10 or level + 1
        addXpMultiplier(character, perk, 1, targetLevel, targetLevel)
        local owned = map:get(perk)
        if owned == nil or owned == original then
            error("distinct x1 multiplier entry was not installed")
        end
        return {
            map = map,
            original = original,
            originalPresent = originalPresent,
            originalMultiplier = originalMultiplier,
            targetLevel = targetLevel,
            owned = owned,
        }
    end)
    if not ok then
        if map ~= nil then
            if map:get(perk) ~= original then map:remove(perk) end
            if originalPresent then map:put(perk, original) end
        end
        records[perk] = nil
        return false
    end
    records[perk] = record
    return true
end

function Ownership.release(manager, character, perk)
    if manager == nil or character == nil or perk == nil then return false end
    local records = recordsFor(manager, character, false)
    if records == nil then return false end
    local record = records[perk]
    if record == nil then return false end
    records[perk] = nil

    local ok, restored = pcall(function()
        if record.map:get(perk) ~= record.owned then return false end
        if record.originalPresent then
            local synchronized = pcall(
                addXpMultiplier,
                character,
                perk,
                record.originalMultiplier,
                record.targetLevel,
                record.targetLevel)
            record.map:put(perk, record.original)
            return synchronized
        else
            record.map:remove(perk)
        end
        return true
    end)
    local hasRecords = false
    for _ in pairs(records) do
        hasRecords = true
        break
    end
    if not hasRecords then manager.records[character] = nil end
    return ok and restored == true
end

function Ownership.releaseAll(manager, character)
    if manager == nil or character == nil then return false end
    local records = recordsFor(manager, character, false)
    if records == nil then return false end

    local perks = {}
    for perk in pairs(records) do table.insert(perks, perk) end
    local restored = false
    for _, perk in ipairs(perks) do
        if Ownership.release(manager, character, perk) then restored = true end
    end
    return restored
end
