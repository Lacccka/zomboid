-- Publication-oriented Bandits B42.20 cache guard.
--
-- Bandits' own OnZombieUpdate/EveryOneMinute handlers remain authoritative.
-- This late listener only removes transient/despawned IsoZombie references after
-- Bandits has updated its caches. No Bandits source file is replaced here.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.zombie-square-guard"

local function removeFromCaches(id)
    if not BanditZombie or id == nil then return end
    if type(BanditZombie.Cache) == "table" then BanditZombie.Cache[id] = nil end
    if type(BanditZombie.CacheLight) == "table" then BanditZombie.CacheLight[id] = nil end
    if type(BanditZombie.CacheLightB) == "table" then BanditZombie.CacheLightB[id] = nil end
    if type(BanditZombie.CacheLightZ) == "table" then BanditZombie.CacheLightZ[id] = nil end
end

local function getSquareSafe(zombie)
    if not zombie then return nil end
    local ok, square = pcall(function()
        return zombie:getSquare()
    end)
    if not ok then return nil end
    return square
end

local function getZombieIdSafe(zombie)
    local getId = BanditUtils and BanditUtils.GetZombieID
    if type(getId) ~= "function" then return nil end
    local ok, id = pcall(getId, zombie)
    if not ok then return nil end
    return id
end

local function recountLightCaches()
    local bcnt, zcnt = 0, 0
    if type(BanditZombie.CacheLightB) == "table" then
        for _ in pairs(BanditZombie.CacheLightB) do bcnt = bcnt + 1 end
    end
    if type(BanditZombie.CacheLightZ) == "table" then
        for _ in pairs(BanditZombie.CacheLightZ) do zcnt = zcnt + 1 end
    end
    BanditZombie.CacheLightBCnt = bcnt
    BanditZombie.CacheLightZCnt = zcnt
end

local function cleanupZombie(zombie)
    if getSquareSafe(zombie) then return end
    local id = getZombieIdSafe(zombie)
    if id ~= nil then removeFromCaches(id) end
end

local function sweepCaches()
    if not BanditZombie or type(BanditZombie.Cache) ~= "table" then return end

    local stale = {}
    for id, zombie in pairs(BanditZombie.Cache) do
        if not getSquareSafe(zombie) then
            stale[#stale + 1] = id
        end
    end

    for i = 1, #stale do
        removeFromCaches(stale[i])
    end

    if #stale > 0 then
        recountLightCaches()
    end
end

Guard.install {
    id = FEATURE,
    validate = function()
        if type(BanditZombie) ~= "table" then
            return false, "BanditZombie table is unavailable"
        end
        if type(BanditUtils) ~= "table" or type(BanditUtils.GetZombieID) ~= "function" then
            return false, "BanditUtils.GetZombieID is unavailable"
        end
        if not Events or not Events.OnZombieUpdate or not Events.EveryOneMinute then
            return false, "required zombie events are unavailable"
        end
        return true
    end,
    install = function()
        Events.OnZombieUpdate.Add(function(zombie)
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "post-update cleanup", cleanupZombie, zombie)
            end
        end)

        -- Bandits rebuilds all caches every minute. Registering after Bandits
        -- makes this sweep run after that rebuild and prevents reintroduction of
        -- squareless objects that did not receive another OnZombieUpdate tick.
        Events.EveryOneMinute.Add(function()
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "post-flush sweep", sweepCaches)
            end
        end)
    end,
}
