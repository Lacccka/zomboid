-- LCC observation-only trace for the B42.20.3 normal-zombie -> Bandit custom bite.
--
-- BanditUpdate uses coordinate-only pursuit and starts its manual bite pipeline
-- only after the zombie is <0.8 center distance, unobstructed and facing the
-- Bandit. It then sets Bite/BiteLow and expects action state "bumped".
--
-- This callback is intentionally loaded after BanditUpdate and never mutates
-- target, path, bump state, movement or Bandit health.
if isServer() then return end

local MARKER = "close-range-bite-trace-v1"
LCC_BANDITS_CLOSE_RANGE_BITE_TRACE = MARKER

local stats = {
    updates = 0,
    withBandit = 0,
    near12Samples = 0,
    near08Samples = 0,
    near08Entries = 0,
    zMismatchSamples = 0,
    wallBlockedSamples = 0,
    facingTrueSamples = 0,
    facingFalseSamples = 0,
    overAttackCapSamples = 0,
    staggerSamples = 0,
    eligibleSamples = 0,
    eligibleNotArmedSamples = 0,
    biteArmedSamples = 0,
    biteArmedTransitions = 0,
    bumpedBiteSamples = 0,
    bumpedTransitions = 0,
    armedLostBeforeBumped = 0,
    armedTimeouts = 0,
}

local stateByZombie = setmetatable({}, { __mode = "k" })
local detailBudget = 30

local function isBandit(character)
    if not character or not instanceof(character, "IsoZombie") then return false end
    local ok, value = pcall(function() return character:getVariableBoolean("Bandit") end)
    return ok and value == true
end

local function characterId(character)
    if not character then return "nil" end
    if BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end
    local ok, value = pcall(function() return character:getPersistentOutfitID() end)
    return ok and value ~= nil and tostring(value) or "unknown"
end

local function closestBandit(zombie)
    if not BanditZombie or type(BanditZombie.CacheLightB) ~= "table" then return nil, nil, nil end

    local zx, zy, zz = zombie:getX(), zombie:getY(), zombie:getZ()
    local best, bestId, bestDist2
    for id, cached in pairs(BanditZombie.CacheLightB) do
        if cached and cached.x and cached.y and cached.z and math.abs(zz - cached.z) < 1.5 then
            local dx = cached.x - zx
            local dy = cached.y - zy
            local dist2 = dx * dx + dy * dy
            if bestDist2 == nil or dist2 < bestDist2 then
                best = cached
                bestId = id
                bestDist2 = dist2
            end
        end
    end
    return best, bestId, bestDist2
end

local function actualBandit(id)
    if id == nil or not BanditZombie or type(BanditZombie.Cache) ~= "table" then return nil end
    local bandit = BanditZombie.Cache[id]
    return isBandit(bandit) and bandit or nil
end

local function attackingZombieCount(banditCached)
    if not banditCached or not BanditZombie or type(BanditZombie.CacheLightZ) ~= "table" then return 0 end
    local count = 0
    for _, cached in pairs(BanditZombie.CacheLightZ) do
        if cached and cached.x and cached.y then
            if math.abs(cached.x - banditCached.x) + math.abs(cached.y - banditCached.y) < 1 then
                local dx = cached.x - banditCached.x
                local dy = cached.y - banditCached.y
                if dx * dx + dy * dy < 0.36 then
                    count = count + 1
                    if count > 2 then break end
                end
            end
        end
    end
    return count
end

local function detail(kind, zombie, bandit, dist, asn, bump, wall, facing, attackCount)
    if detailBudget <= 0 then return end
    detailBudget = detailBudget - 1
    print(string.format(
        "[LCC][BanditsBiteTrace][%s] marker=%s zombie=%s bandit=%s dist=%.3f state=%s bump=%s wall=%s facing=%s nearbyAttackers=%d target=%s",
        kind,
        MARKER,
        characterId(zombie),
        characterId(bandit),
        tonumber(dist) or -1,
        tostring(asn or "<none>"),
        tostring(bump or "<none>"),
        tostring(wall),
        tostring(facing),
        tonumber(attackCount) or -1,
        characterId(zombie:getTarget())
    ))
end

local function onZombieUpdate(zombie)
    if not zombie or not zombie:isAlive() or isBandit(zombie) then return end
    stats.updates = stats.updates + 1

    local cached, bid, dist2 = closestBandit(zombie)
    if not cached or dist2 == nil or dist2 >= 1.44 then
        local state = stateByZombie[zombie]
        if state and state.armed and not state.bumped then
            stats.armedLostBeforeBumped = stats.armedLostBeforeBumped + 1
        end
        stateByZombie[zombie] = nil
        return
    end

    stats.withBandit = stats.withBandit + 1
    stats.near12Samples = stats.near12Samples + 1

    local bandit = actualBandit(bid)
    if not bandit then return end

    local dist = math.sqrt(dist2)
    local state = stateByZombie[zombie]
    if not state then
        state = {near08=false, armed=false, bumped=false, armedTicks=0, banditId=bid}
        stateByZombie[zombie] = state
    elseif state.banditId ~= bid then
        if state.armed and not state.bumped then
            stats.armedLostBeforeBumped = stats.armedLostBeforeBumped + 1
        end
        state.near08 = false
        state.armed = false
        state.bumped = false
        state.armedTicks = 0
        state.banditId = bid
    end

    if dist2 >= 0.64 then
        state.near08 = false
        if state.armed and not state.bumped then
            state.armedTicks = state.armedTicks + 1
            if state.armedTicks == 8 then
                stats.armedTimeouts = stats.armedTimeouts + 1
                detail("ARMED_TIMEOUT", zombie, bandit, dist, zombie:getActionStateName(), zombie:getBumpType(), false, false, -1)
            end
        end
        return
    end

    stats.near08Samples = stats.near08Samples + 1
    if not state.near08 then
        state.near08 = true
        stats.near08Entries = stats.near08Entries + 1
    end

    if math.abs(zombie:getZ() - cached.z) >= 0.3 then
        stats.zMismatchSamples = stats.zMismatchSamples + 1
        return
    end

    local wall = false
    local okWall, wallValue = pcall(function()
        local zs = zombie:getSquare()
        local bs = bandit:getSquare()
        return zs and bs and zs:isSomethingTo(bs) or false
    end)
    if okWall then wall = wallValue == true end
    if wall then
        stats.wallBlockedSamples = stats.wallBlockedSamples + 1
        return
    end

    local facing = false
    local okFacing, facingValue = pcall(function() return zombie:isFacingObject(bandit, 0.3) end)
    if okFacing then facing = facingValue == true end
    if facing then
        stats.facingTrueSamples = stats.facingTrueSamples + 1
    else
        stats.facingFalseSamples = stats.facingFalseSamples + 1
    end

    local asn = zombie:getActionStateName()
    local bump = zombie:getBumpType()
    local armed = bump == "Bite" or bump == "BiteLow"
    local bumped = armed and asn == "bumped"

    if armed then
        stats.biteArmedSamples = stats.biteArmedSamples + 1
        if not state.armed then
            state.armed = true
            state.armedTicks = 0
            stats.biteArmedTransitions = stats.biteArmedTransitions + 1
            detail("ARMED", zombie, bandit, dist, asn, bump, wall, facing, attackingZombieCount(cached))
        else
            state.armedTicks = state.armedTicks + 1
        end
    elseif state.armed and not state.bumped then
        stats.armedLostBeforeBumped = stats.armedLostBeforeBumped + 1
        state.armed = false
        state.armedTicks = 0
    end

    if bumped then
        stats.bumpedBiteSamples = stats.bumpedBiteSamples + 1
        if not state.bumped then
            state.bumped = true
            stats.bumpedTransitions = stats.bumpedTransitions + 1
            detail("BUMPED", zombie, bandit, dist, asn, bump, wall, facing, attackingZombieCount(cached))
        end
    end

    if asn == "staggerback" then
        stats.staggerSamples = stats.staggerSamples + 1
        return
    end
    if not facing then return end

    local attackCount = attackingZombieCount(cached)
    if attackCount > 2 then
        stats.overAttackCapSamples = stats.overAttackCapSamples + 1
        return
    end

    stats.eligibleSamples = stats.eligibleSamples + 1
    if not armed then
        stats.eligibleNotArmedSamples = stats.eligibleNotArmedSamples + 1
        detail("ELIGIBLE_NOT_ARMED", zombie, bandit, dist, asn, bump, wall, facing, attackCount)
    end
end

Events.OnZombieUpdate.Add(onZombieUpdate)

Events.EveryOneMinute.Add(function()
    print(string.format(
        "[LCC][BanditsBiteTrace][SUMMARY] marker=%s updates=%d withBandit=%d near12Samples=%d near08Samples=%d near08Entries=%d zMismatchSamples=%d wallBlockedSamples=%d facingTrueSamples=%d facingFalseSamples=%d overAttackCapSamples=%d staggerSamples=%d eligibleSamples=%d eligibleNotArmedSamples=%d biteArmedSamples=%d biteArmedTransitions=%d bumpedBiteSamples=%d bumpedTransitions=%d armedLostBeforeBumped=%d armedTimeouts=%d",
        MARKER,
        stats.updates,
        stats.withBandit,
        stats.near12Samples,
        stats.near08Samples,
        stats.near08Entries,
        stats.zMismatchSamples,
        stats.wallBlockedSamples,
        stats.facingTrueSamples,
        stats.facingFalseSamples,
        stats.overAttackCapSamples,
        stats.staggerSamples,
        stats.eligibleSamples,
        stats.eligibleNotArmedSamples,
        stats.biteArmedSamples,
        stats.biteArmedTransitions,
        stats.bumpedBiteSamples,
        stats.bumpedTransitions,
        stats.armedLostBeforeBumped,
        stats.armedTimeouts
    ))
end)

print(string.format(
    "[LCC][BanditsBiteTrace][BOOT] marker=%s mode=observe-only postBanditUpdate=true mutation=false thresholds=1.2/0.8",
    MARKER
))
