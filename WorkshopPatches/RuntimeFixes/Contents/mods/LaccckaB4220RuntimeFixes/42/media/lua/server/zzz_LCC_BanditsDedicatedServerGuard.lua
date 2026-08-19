-- Source-clean dedicated-server lookup bridge for Bandits 42.20.
--
-- BanditServerCommands calls BanditZombie.GetInstanceById(), but BanditZombie.lua
-- is a client script and therefore does not provide that API on a dedicated
-- server. Bandits contains an experimental BanditServerZombie cache, but its
-- updater performs a full getZombieList() rebuild and is disabled upstream.
--
-- Do not restore that global scan here. Keep an O(1) registry containing only
-- live Bandits that pass through existing server-side Bandit functions.
if not isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.dedicated-zombie-lookup"

Guard.safeRequire(FEATURE, "BanditUtils")
if not Guard.isEnabled(FEATURE) then return end

local registry = {}

local function keyOf(id)
    if id == nil then return nil end
    return tostring(id)
end

local function getZombieIdSafe(zombie)
    if not zombie then return nil end
    local getId = BanditUtils and BanditUtils.GetZombieID
    if type(getId) ~= "function" then return nil end

    local ok, id = pcall(getId, zombie)
    if not ok then return nil end
    return id
end

local function getPersistentOutfitIdSafe(zombie)
    if not zombie then return nil end
    local ok, id = pcall(function()
        return zombie:getPersistentOutfitID()
    end)
    if not ok then return nil end
    return id
end

local function getSquareSafe(zombie)
    if not zombie then return nil end
    local ok, square = pcall(function()
        return zombie:getSquare()
    end)
    if not ok then return nil end
    return square
end

local function rememberKey(id, zombie)
    local key = keyOf(id)
    if key then
        registry[key] = zombie
    end
end

local function registerZombie(zombie, brain)
    if not zombie then return end

    -- Keep all ID representations used by Bandits. BanditServerSpawner stores
    -- brain.id from getPersistentOutfitID(), while BanditUtils.GetZombieID()
    -- normalizes the outfit bit used by some zombie descriptors.
    rememberKey(getPersistentOutfitIdSafe(zombie), zombie)
    rememberKey(getZombieIdSafe(zombie), zombie)
    if type(brain) == "table" then
        rememberKey(brain.id, zombie)
    end
end

local function zombieMatchesId(zombie, id)
    local requested = keyOf(id)
    if not requested or not zombie then return false end

    local normalized = keyOf(getZombieIdSafe(zombie))
    if normalized == requested then return true end

    local persistent = keyOf(getPersistentOutfitIdSafe(zombie))
    return persistent == requested
end

local function removeZombieAliases(zombie)
    if not zombie then return end
    for key, cached in pairs(registry) do
        if cached == zombie then
            registry[key] = nil
        end
    end
end

local function lookupZombie(id)
    if not Guard.isEnabled(FEATURE) then return nil end

    local key = keyOf(id)
    if not key then return nil end

    local zombie = registry[key]
    if zombie then
        if zombieMatchesId(zombie, id) and getSquareSafe(zombie) then
            return zombie
        end
        removeZombieAliases(zombie)
    end

    -- Future-proof against Bandits re-enabling its own server cache. Reading a
    -- populated upstream cache is O(1); LCC never starts its disabled updater.
    if type(BanditServerZombie) == "table" and type(BanditServerZombie.Cache) == "table" then
        local cached = BanditServerZombie.Cache[id]
        if not cached then
            local numericId = tonumber(key)
            if numericId ~= nil then
                cached = BanditServerZombie.Cache[numericId]
            end
        end
        if cached and zombieMatchesId(cached, id) and getSquareSafe(cached) then
            registerZombie(cached)
            return cached
        end
    end

    Guard.warnOnce(
        "bandits.dedicated-zombie-lookup:miss",
        FEATURE,
        "No live registered server Bandit for requested id; death-item refresh skipped"
    )
    return nil
end

local function pruneRegistry()
    if not Guard.isEnabled(FEATURE) then return end

    for key, zombie in pairs(registry) do
        if not getSquareSafe(zombie) then
            registry[key] = nil
        end
    end
end

local function onClientCommand(module, command, player, args)
    if module ~= "Commands" then return end

    if command == "BanditFlush" then
        registry = {}
        return
    end

    if command == "BanditRemove" and type(args) == "table" and args.id ~= nil then
        local zombie = registry[keyOf(args.id)]
        if zombie then
            removeZombieAliases(zombie)
        else
            registry[keyOf(args.id)] = nil
        end
    end
end

Guard.install {
    id = FEATURE,
    validate = function()
        if BanditZombie ~= nil and type(BanditZombie) ~= "table" then
            return false, "BanditZombie exists but is not a table"
        end
        if type(BanditUtils) ~= "table" or type(BanditUtils.GetZombieID) ~= "function" then
            return false, "BanditUtils.GetZombieID is unavailable"
        end
        if type(Bandit) ~= "table" then
            return false, "Bandit table is unavailable"
        end
        if type(Bandit.ApplyVisuals) ~= "function" then
            return false, "Bandit.ApplyVisuals is unavailable"
        end
        if type(Bandit.UpdateItemsToSpawnAtDeath) ~= "function" then
            return false, "Bandit.UpdateItemsToSpawnAtDeath is unavailable"
        end
        return true
    end,
    install = function()
        BanditZombie = BanditZombie or {}

        -- Register only actual Bandits while Bandits already owns the live
        -- IsoZombie reference. No global zombie-list walk is required.
        Guard.wrapBefore(FEATURE, Bandit, "ApplyVisuals", registerZombie)
        Guard.wrapBefore(FEATURE, Bandit, "UpdateItemsToSpawnAtDeath", registerZombie)

        -- Leave a future native server implementation authoritative.
        if not BanditZombie.GetInstanceById then
            BanditZombie.GetInstanceById = lookupZombie
        end

        Events.OnClientCommand.Add(onClientCommand)
        Events.EveryOneMinute.Add(pruneRegistry)
    end,
}
