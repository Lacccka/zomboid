-- Source-clean B42.20 cache/update guard for survivor/NPC runtime code.
--
-- The installed mod keeps its own OnZombieUpdate/EveryOneMinute handlers. LCC
-- adds two narrow protections around their existing behavior:
--   1) a global compatibility-predicate gate makes squareless/transient zombies
--      leave the later update consumer before combat code dereferences a square;
--   2) post-update/post-flush cleanup removes those transient references from
--      the installed caches without replacing the original cache implementation.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.zombie-square-guard"

-- Resolve runtime tables from the separately installed dependency. BanditZombie
-- localizes the original compatibility predicate while loading, whereas the later
-- BanditUpdate consumer calls BanditCompatibility.IsReanimatedForGrappleOnly
-- through the global table at runtime. That lets this split guard the consumer
-- without changing or redistributing either upstream file.
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
            -- Remove an already-cached transient reference immediately when the
            -- later update consumer reaches its early compatibility predicate.
            removeTransientZombie(zombie)

            -- The installed update consumer already treats true here as a clean
            -- early-return condition. We reuse that public compatibility seam so
            -- it exits before ManageCombat/getSquare() paths are reached.
            return true
        end

        -- Keep the installed compatibility predicate authoritative for every
        -- normal zombie and expose its errors rather than swallowing them.
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

        Events.OnZombieUpdate.Add(function(zombie)
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "post-update cleanup", cleanupZombie, zombie)
            end
        end)

        -- The installed mod rebuilds all caches every minute. The later LCC
        -- sweep removes any transient objects that were present in that rebuild.
        Events.EveryOneMinute.Add(function()
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "post-flush sweep", sweepCaches)
            end
        end)
    end,
}
