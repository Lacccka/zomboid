-- Source-clean dedicated-server lookup bridge for Bandits 42.20.
--
-- BanditServerCommands calls BanditZombie.GetInstanceById(), but BanditZombie.lua
-- is a client script and therefore does not provide that API on a dedicated
-- server. Bandits also defines BanditServerZombie.Cache, but its own cache update
-- event is currently disabled. Provide the missing contract without importing
-- either upstream file: use the server cache if populated, otherwise resolve the
-- active IsoZombie on demand from the server cell.
if not isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.dedicated-zombie-lookup"

Guard.safeRequire(FEATURE, "BanditUtils")
if not Guard.isEnabled(FEATURE) then return end

local lastId
local lastZombie

local function getZombieIdSafe(zombie)
    if not zombie then return nil end
    local getId = BanditUtils and BanditUtils.GetZombieID
    if type(getId) ~= "function" then return nil end

    -- A cached Java object can become invalid between server updates during
    -- devirtualization. A stale fast-path reference must become a cache miss,
    -- never a new compatibility-patch crash.
    local ok, id = pcall(getId, zombie)
    if not ok then return nil end
    return id
end

local function lookupZombie(id)
    if id == nil then return nil end

    if lastId == id and lastZombie then
        if getZombieIdSafe(lastZombie) == id then
            return lastZombie
        end
        lastId, lastZombie = nil, nil
    end

    -- Future-proof against Bandits re-enabling its own server cache.
    if type(BanditServerZombie) == "table" and type(BanditServerZombie.Cache) == "table" then
        local cached = BanditServerZombie.Cache[id]
        if cached and getZombieIdSafe(cached) == id then
            lastId, lastZombie = id, cached
            return cached
        end
    end

    local cell = getCell()
    if not cell then return nil end

    local zombies = cell:getZombieList()
    if not zombies then return nil end

    for i = 0, zombies:size() - 1 do
        local zombie = zombies:get(i)
        if zombie and getZombieIdSafe(zombie) == id then
            lastId, lastZombie = id, zombie
            return zombie
        end
    end

    return nil
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
        if type(getCell) ~= "function" then
            return false, "getCell is unavailable"
        end
        return true
    end,
    install = function()
        BanditZombie = BanditZombie or {}

        -- Leave a future native server implementation authoritative.
        if not BanditZombie.GetInstanceById then
            BanditZombie.GetInstanceById = lookupZombie
        end
    end,
}
