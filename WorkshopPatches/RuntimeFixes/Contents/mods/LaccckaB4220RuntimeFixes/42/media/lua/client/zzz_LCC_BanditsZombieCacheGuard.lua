-- Source-clean B42.20 cache/update guard for Bandits.
--
-- BanditZombie keeps its original OnZombieUpdate/EveryOneMinute handlers. LCC
-- does not replace BanditZombie.lua or BanditUpdate.lua. Instead it uses the
-- public BanditCompatibility predicate that BanditUpdate calls before its main
-- AI work, then cleans transient squareless references from Bandits' caches.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.zombie-square-guard"

Guard.safeRequire(FEATURE, "BanditUtils")
Guard.safeRequire(FEATURE, "BanditCompatibility")
Guard.safeRequire(FEATURE, "BanditZombie")
if not Guard.isEnabled(FEATURE) then return end

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

local function removeTransientZombie(zombie)
    local id = getZombieIdSafe(zombie)
    if id ~= nil then removeFromCaches(id) end
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
    removeTransientZombie(zombie)
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

local function installSquarelessConsumerGate()
    if BanditCompatibility.__LCCSquarelessUpdateGate then return end

    local originalIsReanimated = BanditCompatibility.IsReanimatedForGrappleOnly
    BanditCompatibility.IsReanimatedForGrappleOnly = function(zombie, ...)
        if Guard.isEnabled(FEATURE) and zombie and not getSquareSafe(zombie) then
            removeTransientZombie(zombie)

            -- BanditUpdate already treats true as an immediate clean return.
            -- Reuse that seam only for a transient object that no longer has a
            -- square, before later AI paths can dereference one.
            return true
        end

        return originalIsReanimated(zombie, ...)
    end

    BanditCompatibility.__LCCSquarelessUpdateGate = true
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
        if type(BanditCompatibility) ~= "table"
                or type(BanditCompatibility.IsReanimatedForGrappleOnly) ~= "function" then
            return false, "BanditCompatibility.IsReanimatedForGrappleOnly is unavailable"
        end
        if not Events or not Events.OnZombieUpdate or not Events.EveryOneMinute then
            return false, "required zombie events are unavailable"
        end
        return true
    end,
    install = function()
        installSquarelessConsumerGate()

        -- BanditZombie localizes the original compatibility predicate while it
        -- loads. Its handler can therefore still insert a squareless object into
        -- the cache for this event; our later handler removes it again.
        Events.OnZombieUpdate.Add(function(zombie)
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "post-update cleanup", cleanupZombie, zombie)
            end
        end)

        -- Upstream rebuilds all caches every minute and calls the global
        -- compatibility predicate there. The sweep is retained as a second
        -- lifecycle barrier for transient objects and future upstream changes.
        Events.EveryOneMinute.Add(function()
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "post-flush sweep", sweepCaches)
            end
        end)
    end,
}
