-- Lacccka B42 NPC Fixes: protect already-materialized Bandits from the
-- BanditServer.Wanderers devirtualization cleanup.
--
-- Current Bandits2 destroys every IsoZombie inside a 30-tile circle before a
-- virtual wanderer group is materialized. Physical Bandits are IsoZombie too,
-- so the untyped cleanup can setHealth(0) + die() an unrelated NPC. This guard
-- leaves the upstream server file untouched and narrowly defers devirtualization
-- when that destructive circle overlaps an already-live Bandit.

if not (isServer and isServer()) then return end

require "BanditUtils"
require "BanditBrain"

local MARKER = "wanderer-devirtualization-bandit-preservation-v1"
local PURGE_RADIUS = 30
local PURGE_RADIUS2 = PURGE_RADIUS * PURGE_RADIUS
local DEFAULT_CONTACT_RANGE = 70
local COORD_EPSILON = 0.01
local COORD_EPSILON2 = COORD_EPSILON * COORD_EPSILON
local LOG_THROTTLE_MS = 10000

LCC_NPCFIXES_WANDERER_DEVIRT_GUARD = MARKER

local originalDistTo = BanditUtils and BanditUtils.DistTo or nil
local lastLogAt = setmetatable({}, { __mode = "k" })

local function log(level, message)
    print("[LCC][NPCFixes][WandererDevirtGuard][" .. tostring(level) .. "] marker="
        .. MARKER .. " " .. tostring(message))
end

local function nowMs()
    if type(getTimestampMs) == "function" then return getTimestampMs() end
    return 0
end

local function coordsMatch(ax, ay, bx, by)
    if type(ax) ~= "number" or type(ay) ~= "number"
            or type(bx) ~= "number" or type(by) ~= "number" then
        return false
    end
    local dx = ax - bx
    local dy = ay - by
    return (dx * dx) + (dy * dy) <= COORD_EPSILON2
end

local function getContactRange()
    if BanditServer and BanditServer.Wanderers
            and type(BanditServer.Wanderers.contactRange) == "number" then
        return BanditServer.Wanderers.contactRange
    end
    return DEFAULT_CONTACT_RANGE
end

local function getWanderers()
    if type(GetBanditModData) ~= "function" then return nil end
    local ok, gmd = pcall(GetBanditModData)
    if not ok or type(gmd) ~= "table" or type(gmd.Wanderers) ~= "table" then
        return nil
    end
    return gmd.Wanderers
end

local function findGroupAt(x, y)
    local wanderers = getWanderers()
    if not wanderers then return nil end

    for _, group in ipairs(wanderers) do
        if type(group) == "table" and coordsMatch(group.x, group.y, x, y) then
            return group
        end
    end
    return nil
end

local function matchesOnlinePlayer(x, y)
    if type(getOnlinePlayers) ~= "function" then return false end
    local players = getOnlinePlayers()
    if not players then return false end

    for i = 0, players:size() - 1 do
        local player = players:get(i)
        if player and coordsMatch(player:getX(), player:getY(), x, y) then
            return true
        end
    end
    return false
end

local function isLiveBandit(zombie)
    if not zombie or not instanceof(zombie, "IsoZombie") then return false, nil end

    local okAlive, alive = pcall(function()
        return not zombie:isDead() and zombie:isAlive() and zombie:getHealth() > 0
    end)
    if not okAlive or not alive then return false, nil end

    local brain = nil
    if BanditBrain and type(BanditBrain.Get) == "function" then
        local okBrain, value = pcall(BanditBrain.Get, zombie)
        if okBrain and type(value) == "table" then brain = value end
    end
    if brain then return true, brain end

    local okBandit, bandit = pcall(function()
        return zombie:getVariableBoolean("Bandit")
    end)
    return okBandit and bandit == true, nil
end

local function findBanditInPurgeCircle(groupX, groupY)
    local cell = getCell()
    if not cell then return nil, nil end
    local zombieList = cell:getZombieList()
    if not zombieList then return nil, nil end

    for i = 0, zombieList:size() - 1 do
        local zombie = zombieList:get(i)
        local protected, brain = isLiveBandit(zombie)
        if protected then
            local dx = zombie:getX() - groupX
            local dy = zombie:getY() - groupY
            if (dx * dx) + (dy * dy) <= PURGE_RADIUS2 then
                return zombie, brain
            end
        end
    end
    return nil, nil
end

local function logDeferred(group, zombie, brain, actualDistance)
    local now = nowMs()
    local last = lastLogAt[group] or 0
    if now - last < LOG_THROTTLE_MS then return end
    lastLogAt[group] = now

    local runtimeId = nil
    if type(brain) == "table" then runtimeId = brain.id end
    if runtimeId == nil and BanditUtils and type(BanditUtils.GetZombieID) == "function" then
        local okId, value = pcall(BanditUtils.GetZombieID, zombie)
        if okId then runtimeId = value end
    end

    log("DEFER", "groupCid=" .. tostring(group and group.cid)
        .. " group=" .. tostring(group and group.x) .. "," .. tostring(group and group.y)
        .. " protectedRuntimeId=" .. tostring(runtimeId)
        .. " protectedName=" .. tostring(brain and brain.fullname)
        .. " playerDistance=" .. tostring(actualDistance)
        .. " purgeRadius=" .. tostring(PURGE_RADIUS))
end

if type(originalDistTo) ~= "function" then
    log("ERROR", "BanditUtils.DistTo unavailable; guard not installed")
    return
end

BanditUtils.DistTo = function(x1, y1, x2, y2, ...)
    local actualDistance = originalDistTo(x1, y1, x2, y2, ...)
    local contactRange = getContactRange()

    -- The destructive devirtualization branch can only be entered from a
    -- wanderer-group -> online-player distance check that is already in range.
    -- Everything else must retain the exact upstream distance result.
    if type(actualDistance) ~= "number" or actualDistance >= contactRange then
        return actualDistance
    end

    local group = findGroupAt(x1, y1)
    if not group or not matchesOnlinePlayer(x2, y2) then
        return actualDistance
    end

    local protected, brain = findBanditInPurgeCircle(group.x, group.y)
    if not protected then return actualDistance end

    logDeferred(group, protected, brain, actualDistance)

    -- Upstream tests `dist < contactRange`. Returning the boundary keeps the
    -- virtual group alive for another update instead of executing its untyped
    -- setHealth(0)/die() purge over an existing physical NPC.
    return contactRange
end

log("BOOT", "mode=DEFER_ON_BANDIT_OVERLAP purgeRadius=" .. tostring(PURGE_RADIUS)
    .. " contactRange=" .. tostring(getContactRange()) .. " sourceClean=true")

return true
