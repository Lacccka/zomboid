-- Client-side guard for the Bandits zombie -> NPC vanilla AttackState path.
--
-- B42.20.3 testing proved that bAttack is a read-only animation callback
-- variable, so the former zombie:setVariable("bAttack", false) experiment could
-- never block the Java AttackState transition. The dangerous condition is a
-- normal IsoZombie holding a Bandit (also an IsoZombie) as its vanilla target.
--
-- Bandits already has its own zombie -> Bandit navigation and Bite/BiteLow
-- simulation through BanditZombie.CacheLightB. It also calls
-- bandit:setZombiesDontAttack(true), but only once close-range custom bite logic
-- is already running. This guard moves that target-side engine flag earlier and
-- keeps it asserted for the Bandit's lifetime. It does not rewrite zombie
-- targets, action states, bump types, bAttack, damage, infection, or Bandits'
-- custom bite bookkeeping.
if isServer() then return end

local Guard = require "LCC/Guard"
local FEATURE = "bandits.attack-state-guard"
local HEARTBEAT_MS = 15000
local HEARTBEAT_CHECK_EVERY_UPDATES = 512

Guard.safeRequire(FEATURE, "Bandit")
Guard.safeRequire(FEATURE, "BanditUtils")
if not Guard.isEnabled(FEATURE) then return end

local trackedAttackers = setmetatable({}, { __mode = "k" })
local protectedBandits = setmetatable({}, { __mode = "k" })
local stats = {
    zombieUpdates = 0,
    protectedBandits = 0,
    targetLeaks = 0,
    bAttackObserved = 0,
    attackStateObserved = 0,
}
local lastHeartbeat = 0

local function nowMs()
    if type(getTimestampMs) == "function" then
        return getTimestampMs()
    end

    local gameTime = getGameTime and getGameTime()
    if gameTime then
        return math.floor(gameTime:getWorldAgeHours() * 3600000)
    end

    return 0
end

local function boolString(value)
    return value and "true" or "false"
end

local function characterId(character)
    if not character then return "nil" end

    if BanditUtils and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, character)
        if ok and value ~= nil then return tostring(value) end
    end

    local ok, value = pcall(function()
        return character:getPersistentOutfitID()
    end)
    if ok and value ~= nil then return tostring(value) end

    return tostring(character)
end

local function isBandit(character)
    if not character or not instanceof(character, "IsoZombie") then return false end
    local ok, value = pcall(function()
        return character:getVariableBoolean("Bandit")
    end)
    return ok and value == true
end

local function attackStateName(zombie)
    local asn = zombie:getActionStateName()
    if not asn or tostring(asn) == "" then return "<none>" end
    return tostring(asn)
end

local function isAttackState(asn)
    return asn == "attack" or asn == "attack-network"
end

local function bumpType(zombie)
    local bump = zombie:getBumpType()
    if not bump or tostring(bump) == "" then return "<none>" end
    return tostring(bump)
end

local function isCustomBite(zombie, bump)
    local md = zombie:getModData()
    return md ~= nil
        and md.zid ~= nil
        and (bump == "Bite" or bump == "BiteLow")
end

local function readProtectedFlag(bandit)
    local ok, value = pcall(function()
        return bandit:isZombiesDontAttack()
    end)
    if ok then return value == true end
    return nil
end

local function protectionString(bandit)
    local value = readProtectedFlag(bandit)
    if value == nil then
        return protectedBandits[bandit] and "asserted-unreadable" or "unknown"
    end
    return boolString(value)
end

local function protectBandit(bandit)
    if not isBandit(bandit) then return false end

    local before = readProtectedFlag(bandit)
    local alreadyAsserted = protectedBandits[bandit] == true

    -- If the getter is not exposed to Lua, trust the successful setter call and
    -- avoid calling it on every zombie update. If the getter is exposed and
    -- reports false later, reassert the flag.
    if before == false or (before == nil and not alreadyAsserted) then
        local ok, err = pcall(function()
            bandit:setZombiesDontAttack(true)
        end)
        if not ok then
            error("setZombiesDontAttack(true) failed for Bandit " .. characterId(bandit) .. ": " .. tostring(err))
        end
    end

    local after = readProtectedFlag(bandit)
    if after == false then
        error("setZombiesDontAttack(true) remained false on Bandit " .. characterId(bandit))
    end

    if not alreadyAsserted then
        protectedBandits[bandit] = true
        stats.protectedBandits = stats.protectedBandits + 1
        print(string.format(
            "[LCC][BanditsAttackGuard][PROTECT_TARGET] target=%s before=%s after=%s",
            characterId(bandit),
            before == nil and "unreadable" or boolString(before),
            after == nil and "unreadable" or boolString(after)
        ))
    end

    return true
end

local function maybeHeartbeat()
    local now = nowMs()
    if lastHeartbeat == 0 then
        lastHeartbeat = now
        return
    end
    if now - lastHeartbeat < HEARTBEAT_MS then return end
    lastHeartbeat = now

    print(string.format(
        "[LCC][BanditsAttackGuard][SUMMARY] updates=%d protectedBandits=%d targetLeaks=%d bAttackObserved=%d attackStateObserved=%d",
        stats.zombieUpdates,
        stats.protectedBandits,
        stats.targetLeaks,
        stats.bAttackObserved,
        stats.attackStateObserved
    ))
end

local function observeZombie(zombie)
    stats.zombieUpdates = stats.zombieUpdates + 1
    if stats.zombieUpdates % HEARTBEAT_CHECK_EVERY_UPDATES == 0 then
        maybeHeartbeat()
    end

    if not zombie then return end

    if isBandit(zombie) then
        protectBandit(zombie)
        return
    end

    local target = zombie:getTarget()
    if not isBandit(target) then
        trackedAttackers[zombie] = nil
        return
    end

    -- Assert the flag again before collecting a leak. This handles a Bandit
    -- that was spawned/reused before our own OnZombieUpdate saw it.
    protectBandit(target)

    local asn = attackStateName(zombie)
    local bump = bumpType(zombie)
    local customBite = isCustomBite(zombie, bump)
    local bAttack = zombie:getVariableBoolean("bAttack")
    local noLungeAttack = zombie:getVariableBoolean("NoLungeAttack")
    local attackState = isAttackState(asn)
    local previous = trackedAttackers[zombie]

    if not previous or previous.target ~= target then
        stats.targetLeaks = stats.targetLeaks + 1
        print(string.format(
            "[LCC][BanditsAttackGuard][TARGET_LEAK] attacker=%s target=%s state=%s bAttack=%s noLunge=%s bump=%s customBite=%s targetProtected=%s",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(bAttack),
            boolString(noLungeAttack),
            bump,
            boolString(customBite),
            protectionString(target)
        ))
    end

    if bAttack and (
            not previous
            or not previous.bAttack
            or previous.target ~= target
        ) then
        stats.bAttackObserved = stats.bAttackObserved + 1
        print(string.format(
            "[LCC][BanditsAttackGuard][READ_ONLY_BATTACK] attacker=%s target=%s state=%s bAttack=true noLunge=%s bump=%s customBite=%s targetProtected=%s",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(noLungeAttack),
            bump,
            boolString(customBite),
            protectionString(target)
        ))
    end

    if attackState and (
            not previous
            or not previous.attackState
            or previous.target ~= target
        ) then
        stats.attackStateObserved = stats.attackStateObserved + 1
        print(string.format(
            "[LCC][BanditsAttackGuard][ESCAPED_ATTACK_STATE] attacker=%s target=%s state=%s bAttack=%s noLunge=%s bump=%s customBite=%s targetProtected=%s diagnosticOnly=true",
            characterId(zombie),
            characterId(target),
            asn,
            boolString(bAttack),
            boolString(noLungeAttack),
            bump,
            boolString(customBite),
            protectionString(target)
        ))
    end

    trackedAttackers[zombie] = {
        target = target,
        bAttack = bAttack,
        attackState = attackState,
    }
end

Guard.install {
    id = FEATURE,
    validate = function()
        if type(Bandit) ~= "table" or type(Bandit.ApplyVisuals) ~= "function" then
            return false, "Bandit.ApplyVisuals is unavailable"
        end
        if type(BanditUtils) ~= "table" or type(BanditUtils.GetCharacterID) ~= "function" then
            return false, "BanditUtils.GetCharacterID is unavailable"
        end
        if not Events or not Events.OnZombieUpdate then
            return false, "OnZombieUpdate event is unavailable"
        end
        return true
    end,
    install = function()
        Guard.wrapBefore(FEATURE, Bandit, "ApplyVisuals", function(bandit)
            protectBandit(bandit)
        end)

        Events.OnZombieUpdate.Add(function(zombie)
            if Guard.isEnabled(FEATURE) then
                Guard.protect(FEATURE, "protect/observe zombie attack state", observeZombie, zombie)
            end
        end)

        print(string.format(
            "[LCC][BanditsAttackGuard][INIT] target-side setZombiesDontAttack guard active; bAttack remains observe-only; heartbeat=%dms",
            HEARTBEAT_MS
        ))
    end,
}
