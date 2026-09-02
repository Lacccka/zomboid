-- Lacccka B42 NPC Fixes: loadstring-free Bandits 42.20 compatibility bridge.
--
-- Build 42.20 no longer guarantees runtime Lua source compilation. This file
-- therefore leaves the installed Bandits2 sources untouched and adapts only
-- public runtime seams. The upstream source is read for exact fingerprints only;
-- it is never compiled or executed from text.
--
-- Ordinary zombies are stopped at BanditUpdate's consecutive compatibility
-- predicates before the unsafe upstream UpdateZombies() character-target block.
-- A later LCC OnZombieUpdate callback performs coordinate-only pursuit and the
-- original Bandits bite/dragdown lifecycle. Active Bandits continue through the
-- original OnBanditUpdate implementation. Non-combat quest NPCs use one-tick
-- public Bandit.IsSleeping/Bandit.GetTask gates so generic combat/collision task
-- generation cannot pre-empt their custom ZombieProgram.

if isServer() then return end

require "Bandit"
require "BanditBrain"
require "BanditZombie"
require "BanditUtils"
require "BanditCompatibility"
require "ZombieActions/ZAShoot"

local MARKER = "loadstring-free-predicate-bridge-v2"
local MOD_ID = "Bandits2"
local BANDIT_UPDATE_PATH = "media/lua/client/BanditUpdate.lua"
local ZA_SHOOT_PATH = "media/lua/shared/ZombieActions/ZAShoot.lua"
local PREDICATE_PROBE_TTL_MS = 5
local LCC_PURSUIT_ALIGN_DIST2 = 0.5625 -- 0.75 tile
local LCC_PURSUIT_IDLE_RETRY_MS = 750

LCC_NPCFIXES_BANDITUPDATE_SHIM = MARKER
LCC_NPCFIXES_CALLBACK_BRIDGE = LCC_NPCFIXES_CALLBACK_BRIDGE or {}
local Bridge = LCC_NPCFIXES_CALLBACK_BRIDGE
Bridge.marker = MARKER
Bridge.installed = Bridge.installed or false
Bridge.stats = Bridge.stats or {
    ordinaryUpdateBypasses = 0,
    coordinatePursuits = 0,
    closeBites = 0,
    nonCombatCombatSkips = 0,
    nonCombatCollisionSkips = 0,
    invalidDeathKeysSuppressed = 0,
    shotCoordinateAlerts = 0,
    staleRelationsCleared = 0,
    zombifyTransitions = 0,
}

local predicateProbeAt = setmetatable({}, { __mode = "k" })
local ordinaryTick = setmetatable({}, { __mode = "k" })
local nonCombatTick = setmetatable({}, { __mode = "k" })
local pursuitRetryAt = setmetatable({}, { __mode = "k" })
local biteTab = setmetatable({}, { __mode = "k" })
local warned = {}

local function log(level, message)
    print("[LCC][NPCFixes][PredicateBridge][" .. tostring(level) .. "] marker=" .. MARKER .. " " .. tostring(message))
end

local function warnOnce(key, message)
    if warned[key] then return end
    warned[key] = true
    log("WARN", message)
end

local function readUpstreamSource(path)
    if type(getModFileReader) ~= "function" then
        return nil, "getModFileReader unavailable"
    end

    local reader = getModFileReader(MOD_ID, path, false)
    if not reader then
        return nil, "unable to read " .. tostring(path)
    end

    local lines = {}
    while true do
        local line = reader:readLine()
        if line == nil then break end
        lines[#lines + 1] = line
    end
    pcall(function() reader:close() end)
    return table.concat(lines, "\n") .. "\n"
end

local function containsExactlyOnce(source, needle)
    local first, last = string.find(source, needle, 1, true)
    if not first then return false, "missing: " .. needle end
    if string.find(source, needle, last + 1, true) then
        return false, "not unique: " .. needle
    end
    return true
end

local function validateSource(path, fingerprints)
    local source, readErr = readUpstreamSource(path)
    if not source then return false, readErr end

    for _, fingerprint in ipairs(fingerprints) do
        local ok, reason = containsExactlyOnce(source, fingerprint)
        if not ok then
            return false, tostring(path) .. " fingerprint " .. tostring(reason)
        end
    end
    return true
end

local function validateUpstream()
    local updateOk, updateReason = validateSource(BANDIT_UPDATE_PATH, {
        [[    if BanditCompatibility.IsReanimatedForGrappleOnly(zombie) then return end

    if BanditCompatibility.IsRagdoll(zombie) then return end]],
        "local function UpdateZombies(zombie)",
        "                zombie:pathToCharacter(bandit)",
        [[                    if zombie and bandit  then
                        zombie:spotted(bandit, true)
                        zombie:addAggro(bandit, 1)
                        zombie:setTarget(bandit)
                        zombie:setAttackedBy(bandit)]],
        "local combatTasks = ManageCombat(bandit)",
        "local colissionTasks = ManageCollisions(bandit)",
        [[    local task = Bandit.GetTask(bandit)
    if not task then return {} end
    if not (task.action == "Move" or task.action == "GoTo") then return {} end]],
        "item:setKeyId(brain.key)",
    })
    if not updateOk then return false, updateReason end

    local shootOk, shootReason = validateSource(ZA_SHOOT_PATH, {
        "ZombieActions.Shoot.onComplete = function(bandit, task)",
        [[                    zombie:spottedNew(shooter, true)
                    zombie:addAggro(shooter, 1)
                    zombie:setTarget(shooter)]],
        "BanditUtils.ManageLineOfFire(shooter, enemy, weaponItem)",
    })
    if not shootOk then return false, shootReason end
    return true
end

local function nowMs()
    if type(getTimestampMs) == "function" then return getTimestampMs() end
    return 0
end

local function isBandit(character)
    if not character or not instanceof(character, "IsoZombie") then return false end
    local ok, value = pcall(function() return character:getVariableBoolean("Bandit") end)
    return ok and value == true
end

local function isOrdinaryZombie(character)
    return character ~= nil and instanceof(character, "IsoZombie") and not isBandit(character)
end

local function isNonCombatBrain(brain)
    if type(brain) ~= "table" then return false end
    if brain.lccqNonCombat == true then return true end
    local program = brain.program
    return type(program) == "table" and program.name == "LCCQFQuestGiver"
end

local function isNonCombatBandit(bandit)
    if not bandit or not isBandit(bandit) then return false end
    local brain = BanditBrain and BanditBrain.Get and BanditBrain.Get(bandit) or nil
    if isNonCombatBrain(brain) then return true end

    local okMd, md = pcall(function() return bandit:getModData() end)
    if okMd and md and md.lccqIgnoreZombieAggro == true then return true end

    local okVar, value = pcall(function() return bandit:getVariableBoolean("LCCQFNonCombat") end)
    return okVar and value == true
end

local function getZombieIdSafe(zombie)
    if not zombie or not BanditUtils or type(BanditUtils.GetZombieID) ~= "function" then return nil end
    local ok, id = pcall(BanditUtils.GetZombieID, zombie)
    return ok and id or nil
end

local function getClusterBrain(zombie)
    if type(GetBanditClusterData) ~= "function" then
        return nil, false
    end
    local id = getZombieIdSafe(zombie)
    if id == nil then return nil, false end

    local ok, gmd = pcall(GetBanditClusterData, id)
    if not ok then return nil, false end
    return gmd and gmd[id] or nil, true
end

local function sanitizeUnsafeBanditRelation(zombie)
    if not isOrdinaryZombie(zombie) then return false end
    local changed = false

    local okTarget, target = pcall(function() return zombie:getTarget() end)
    if okTarget and isBandit(target) then
        if pcall(function() zombie:setTarget(nil) end) then changed = true end
    end

    local okAttacked, attackedBy = pcall(function() return zombie:getAttackedBy() end)
    if okAttacked and isBandit(attackedBy) then
        if pcall(function() zombie:setAttackedBy(nil) end) then changed = true end
    end

    local okPfb, pfb = pcall(function() return zombie:getPathFindBehavior2() end)
    if okPfb and pfb then
        local okGoal, goalCharacter = pcall(function() return pfb:isGoalCharacter() end)
        if okGoal and goalCharacter then
            local okGoalTarget, targetChar = pcall(function() return pfb:getTargetChar() end)
            if okGoalTarget and isBandit(targetChar) then
                if pcall(function() pfb:cancel() end) then changed = true end
            end
        end
    end

    if changed then
        Bridge.stats.staleRelationsCleared = Bridge.stats.staleRelationsCleared + 1
    end
    return changed
end

local function updateCacheAfterZombify(zombie)
    local id = getZombieIdSafe(zombie)
    if id == nil or not BanditZombie then return end

    local wasBandit = type(BanditZombie.CacheLightB) == "table" and BanditZombie.CacheLightB[id] ~= nil
    local wasZombie = type(BanditZombie.CacheLightZ) == "table" and BanditZombie.CacheLightZ[id] ~= nil
    local light = type(BanditZombie.CacheLight) == "table" and BanditZombie.CacheLight[id] or nil

    if type(BanditZombie.CacheLightB) == "table" then BanditZombie.CacheLightB[id] = nil end
    if light then
        light.isBandit = false
        light.brain = nil
        light.rid = nil
        if type(BanditZombie.CacheLightZ) == "table" then BanditZombie.CacheLightZ[id] = light end
    end

    if wasBandit and type(BanditZombie.CacheLightBCnt) == "number" then
        BanditZombie.CacheLightBCnt = math.max(0, BanditZombie.CacheLightBCnt - 1)
    end
    if light and not wasZombie and type(BanditZombie.CacheLightZCnt) == "number" then
        BanditZombie.CacheLightZCnt = BanditZombie.CacheLightZCnt + 1
    end
end

local function safeZombify(bandit)
    if not isBandit(bandit) then return false end

    bandit:setNoTeeth(false)
    bandit:setUseless(false)
    bandit:setVariable("Bandit", false)
    bandit:setVariable("BanditPrimary", "")
    bandit:setVariable("BanditSecondary", "")
    bandit:setWalkType("2")
    bandit:setVariable("BanditWalkType", "")
    bandit:setPrimaryHandItem(nil)
    bandit:setSecondaryHandItem(nil)
    bandit:resetEquippedHandsModels()
    bandit:clearAttachedItems()

    local okMd, md = pcall(function() return bandit:getModData() end)
    if okMd and md then md.brainId = nil end
    BanditBrain.Remove(bandit)
    updateCacheAfterZombify(bandit)
    Bridge.stats.zombifyTransitions = Bridge.stats.zombifyTransitions + 1
    return true
end

local function prepareOrdinaryZombie(zombie)
    if isBandit(zombie) then safeZombify(zombie) end
    if not isOrdinaryZombie(zombie) then return false end

    sanitizeUnsafeBanditRelation(zombie)
    BanditBrain.Remove(zombie)

    if zombie:isUseless() then zombie:setUseless(false) end
    if zombie:getPrimaryHandItem() then zombie:setPrimaryHandItem(nil) end
    if zombie:getSecondaryHandItem() then zombie:setSecondaryHandItem(nil) end

    local target = zombie:getTarget()
    if target and instanceof(target, "IsoZombie") then
        zombie:setVariable("ZombieBiteDone", true)
        zombie:setNoTeeth(true)
    else
        zombie:setNoTeeth(false)
    end
    zombie:setVariable("NoLungeAttack", false)
    return true
end

local function pathToCoordinate(zombie, x, y, z)
    if not zombie then return false end
    if BanditUtils and type(BanditUtils.IsController) == "function" then
        local okController, controller = pcall(BanditUtils.IsController, zombie)
        if okController and not controller then return false end
    end

    local pfb = zombie:getPathFindBehavior2()
    if pfb and not pfb:getIsCancelled() and pfb:isGoalLocation() then
        local dx = pfb:getTargetX() - x
        local dy = pfb:getTargetY() - y
        local dz = math.abs(pfb:getTargetZ() - z)
        local aligned = dz < 0.5 and (dx * dx + dy * dy) <= LCC_PURSUIT_ALIGN_DIST2

        if aligned then
            if zombie:getActionStateName() ~= "idle" then return false end
            local now = nowMs()
            local lastRetry = pursuitRetryAt[zombie] or 0
            if now - lastRetry < LCC_PURSUIT_IDLE_RETRY_MS then return false end
            pursuitRetryAt[zombie] = now
        end
    end

    zombie:pathToLocationF(x, y, z)
    Bridge.stats.coordinatePursuits = Bridge.stats.coordinatePursuits + 1
    return true
end

local function selectNearestCombatBandit(zombie)
    local player = getSpecificPlayer(0)
    if not player then return nil end

    local zx, zy, zz = zombie:getX(), zombie:getY(), zombie:getZ()
    local px, py, pz = player:getX(), player:getY(), player:getZ()
    local distPlayer2 = ((px - zx) * (px - zx)) + ((py - zy) * (py - zy))
    if distPlayer2 < 4 and math.abs(pz - zz) < 0.3 then return nil end

    local ownId = getZombieIdSafe(zombie)
    local bestDist2 = math.huge
    local bestBandit, bestLight = nil, nil
    local cache = BanditZombie and BanditZombie.CacheLightB or nil
    if type(cache) ~= "table" then return nil end

    for id, light in pairs(cache) do
        if light and id ~= ownId then
            local realId = light.id or id
            local real = BanditZombie.Cache and BanditZombie.Cache[realId] or nil
            if real and real ~= zombie and real:isAlive() and isBandit(real) and not isNonCombatBandit(real) then
                local dz = math.abs((light.z or zz) - zz)
                if dz < 0.31 then
                    local dx = light.x - zx
                    local dy = light.y - zy
                    local distManhattan = math.abs(dx) + math.abs(dy)
                    if distManhattan <= 28 then
                        local manhattanSq = distManhattan * distManhattan
                        if manhattanSq < (2 * bestDist2) then
                            local dist2 = (dx * dx) + (dy * dy)
                            if dist2 < bestDist2 then
                                bestDist2 = dist2
                                bestBandit = real
                                bestLight = light
                            end
                        end
                    end
                end
            end
        end
    end

    if not bestBandit or bestDist2 >= 400 then return nil end
    return bestBandit, bestLight, bestDist2
end

local function processCoordinateBite(zombie)
    local state = biteTab[zombie]
    if not state then return false end

    local bandit = state.bandit
    if not bandit or not bandit:isAlive() or not isBandit(bandit) or isNonCombatBandit(bandit) then
        biteTab[zombie] = nil
        return false
    end

    local asn = zombie:getActionStateName()
    local bumpType = zombie:getBumpType()
    if (bumpType ~= "Bite" and bumpType ~= "BiteLow") or asn ~= "bumped" then
        return false
    end

    local tick = state.tick or 0
    if tick == 14 then
        local dist = BanditUtils.DistTo(zombie:getX(), zombie:getY(), bandit:getX(), bandit:getY())
        if dist < 0.8 then
            if ZombRand(4) == 1 then zombie:playSound("ZombieBite") else zombie:playSound("ZombieScratch") end

            local teeth = BanditCompatibility.InstanceItem("Base.RollingPin")
            if teeth then
                BanditCompatibility.Splash(bandit, teeth, zombie)
                bandit:setHitFromBehind(zombie:isBehind(bandit))
                bandit:setPlayerAttackPosition(bandit:testDotSide(zombie))

                if not bandit:isOnKillDone() and not Bandit.HasTaskType(bandit, "Die") then
                    Bandit.ClearTasks(bandit)
                    bandit:Hit(teeth, zombie, 1.01, false, 1, false)
                    Bandit.Say(bandit, "DRAGDOWN", true)
                    Bandit.UpdateInfection(bandit, 0.001)

                    local player = getSpecificPlayer(0)
                    if player then
                        sendClientCommand(player, "Sync", "Health", {
                            id = BanditUtils.GetCharacterID(bandit),
                            h = bandit:getHealth(),
                        })
                    end
                end
            end
        end
    elseif tick >= 16 then
        biteTab[zombie] = nil
        return true
    end

    state.tick = tick + 1
    return true
end

local function updateOrdinaryZombieCoordinateCombat(zombie)
    if not zombie or not zombie:isAlive() or not isOrdinaryZombie(zombie) then return end
    if processCoordinateBite(zombie) then return end

    local asn = zombie:getActionStateName()
    if asn == "bumped" or asn == "onground" or asn == "climbfence"
            or asn == "getup" or asn == "turnalerted" then
        return
    end
    if zombie:isProne() then return end

    local bandit, light, dist2 = selectNearestCombatBandit(zombie)
    if not bandit or not light then return end

    if dist2 > 9 then
        if zombie:CanSee(bandit) then pathToCoordinate(zombie, light.x, light.y, light.z) end
        return
    end

    pathToCoordinate(zombie, light.x, light.y, light.z)
    if dist2 >= 0.64 or math.abs(zombie:getZ() - light.z) >= 0.3 then return end

    local zombieSquare = zombie:getSquare()
    local banditSquare = bandit:getSquare()
    if not zombieSquare or not banditSquare or zombieSquare:isSomethingTo(banditSquare) then return end

    if not zombie:isFacingObject(bandit, 0.3) then
        zombie:faceThisObject(bandit)
        return
    end

    local attackingZombiesNumber = 0
    for _, attackingZombie in pairs(BanditZombie.CacheLightZ or {}) do
        if math.abs(attackingZombie.x - light.x) + math.abs(attackingZombie.y - light.y) < 1 then
            local dx = attackingZombie.x - light.x
            local dy = attackingZombie.y - light.y
            if (dx * dx) + (dy * dy) < 0.36 then
                attackingZombiesNumber = attackingZombiesNumber + 1
                if attackingZombiesNumber > 2 then break end
            end
        end
    end

    if attackingZombiesNumber > 2 then
        if bandit:getActionStateName() == "onground" then
            Bandit.Say(bandit, "DRAGDOWN", true)
            zombie:die()
        elseif not Bandit.HasTaskType(bandit, "Die") then
            Bandit.ClearTasks(bandit)
            Bandit.AddTask(bandit, { action = "Die", lock = true, anim = "Die", time = 300 })
        end
        return
    end

    local bumpType = zombie:getBumpType()
    if bumpType ~= "Bite" and bumpType ~= "BiteLow" and asn ~= "staggerback" then
        bandit:setZombiesDontAttack(true)
        if bandit:isProne() or bandit:isCrawling() then zombie:setBumpType("BiteLow") else zombie:setBumpType("Bite") end
        biteTab[zombie] = { bandit = bandit, tick = 0 }
        Bridge.stats.closeBites = Bridge.stats.closeBites + 1
    end
end

local function hasCollision(bandit)
    local ok, value = pcall(function()
        return bandit:isCollidedWithDoor() or bandit:isCollidedThisFrame() or bandit:isCollided()
    end)
    return ok and value == true
end

local function installPredicateBridge()
    if Bridge.originalIsReanimated then return true end
    if type(BanditCompatibility.IsReanimatedForGrappleOnly) ~= "function"
            or type(BanditCompatibility.IsRagdoll) ~= "function" then
        return false, "BanditCompatibility predicate seam unavailable"
    end

    local originalIsReanimated = BanditCompatibility.IsReanimatedForGrappleOnly
    local originalIsRagdoll = BanditCompatibility.IsRagdoll
    Bridge.originalIsReanimated = originalIsReanimated
    Bridge.originalIsRagdoll = originalIsRagdoll

    BanditCompatibility.IsReanimatedForGrappleOnly = function(zombie, ...)
        local result = originalIsReanimated(zombie, ...)
        if zombie and not result then
            predicateProbeAt[zombie] = nowMs()
        elseif zombie then
            predicateProbeAt[zombie] = nil
        end
        return result
    end

    BanditCompatibility.IsRagdoll = function(zombie, ...)
        local result = originalIsRagdoll(zombie, ...)
        if not zombie or result then
            if zombie then predicateProbeAt[zombie] = nil end
            return result
        end

        local armedAt = predicateProbeAt[zombie]
        predicateProbeAt[zombie] = nil
        if armedAt == nil or nowMs() - armedAt > PREDICATE_PROBE_TTL_MS then
            return result
        end

        local clusterBrain, clusterOk = getClusterBrain(zombie)
        if not clusterOk then
            warnOnce("cluster-probe", "GetBanditClusterData failed; preserving upstream OnBanditUpdate behavior")
            return result
        end

        if clusterBrain then
            ordinaryTick[zombie] = nil
            if isNonCombatBrain(clusterBrain) or isNonCombatBandit(zombie) then
                local crawling = false
                pcall(function() crawling = zombie:isCrawling() end)
                nonCombatTick[zombie] = {
                    combatGateSeen = crawling,
                    collisionSuppressed = false,
                }
            else
                nonCombatTick[zombie] = nil
            end
            return result
        end

        -- No Bandits cluster brain: the object must stay/become an ordinary
        -- zombie. Returning true here exits the original OnBanditUpdate before
        -- its unsafe UpdateZombies() character-target logic. Our later event
        -- callback performs the source-clean equivalent.
        ordinaryTick[zombie] = true
        nonCombatTick[zombie] = nil
        Bridge.stats.ordinaryUpdateBypasses = Bridge.stats.ordinaryUpdateBypasses + 1
        return true
    end

    return true
end

local function installNonCombatTaskGates()
    if Bridge.originalIsSleeping then return true end
    if type(Bandit.IsSleeping) ~= "function" or type(Bandit.GetTask) ~= "function" then
        return false, "Bandit task gate seam unavailable"
    end

    local originalIsSleeping = Bandit.IsSleeping
    local originalGetTask = Bandit.GetTask
    Bridge.originalIsSleeping = originalIsSleeping
    Bridge.originalGetTask = originalGetTask

    Bandit.IsSleeping = function(candidate, ...)
        local state = candidate and nonCombatTick[candidate] or nil
        if state and not state.combatGateSeen then
            state.combatGateSeen = true
            Bridge.stats.nonCombatCombatSkips = Bridge.stats.nonCombatCombatSkips + 1
            return true
        end
        return originalIsSleeping(candidate, ...)
    end

    Bandit.GetTask = function(candidate, ...)
        local state = candidate and nonCombatTick[candidate] or nil
        if state and state.combatGateSeen and not state.collisionSuppressed and hasCollision(candidate) then
            state.collisionSuppressed = true
            Bridge.stats.nonCombatCollisionSkips = Bridge.stats.nonCombatCollisionSkips + 1
            return nil
        end
        return originalGetTask(candidate, ...)
    end

    return true
end

local function installDeathKeyGuard()
    if Bridge.originalBrainGet then return true end
    if type(BanditBrain.Get) ~= "function" then return false, "BanditBrain.Get unavailable" end

    local originalBrainGet = BanditBrain.Get
    Bridge.originalBrainGet = originalBrainGet
    BanditBrain.Get = function(zombie, ...)
        local brain = originalBrainGet(zombie, ...)
        if type(brain) == "table" and brain.key ~= nil and type(brain.key) ~= "number"
                and zombie and isBandit(zombie) then
            local dead = false
            local okDead, value = pcall(function() return not zombie:isAlive() or zombie:getHealth() <= 0 end)
            dead = okDead and value == true
            if dead then
                brain.key = nil
                Bridge.stats.invalidDeathKeysSuppressed = Bridge.stats.invalidDeathKeysSuppressed + 1
            end
        end
        return brain
    end
    return true
end

local function alertShotCoordinates(shooter, weapon, cacheLightZ)
    if not shooter or not weapon or type(cacheLightZ) ~= "table" then return end
    local weaponItem = BanditCompatibility.InstanceItem(weapon.name)
    if not weaponItem then return end

    local brain = BanditBrain.Get(shooter)
    if brain then weaponItem = BanditUtils.ModifyWeapon(weaponItem, brain) end
    local radius = weaponItem:getSoundRadius()
    local sx, sy, sz = shooter:getX(), shooter:getY(), shooter:getZ()

    for id, light in pairs(cacheLightZ) do
        local dist = math.abs(sx - light.x) + math.abs(sy - light.y)
        if dist < radius then
            local zombie = BanditZombie.Cache and BanditZombie.Cache[id] or nil
            if isOrdinaryZombie(zombie) and not zombie:isMoving() and zombie:getActionStateName() == "idle" then
                pathToCoordinate(zombie, sx, sy, sz)
                Bridge.stats.shotCoordinateAlerts = Bridge.stats.shotCoordinateAlerts + 1
                local sharedStats = rawget(_G, "LCC_BanditsRelationshipStats")
                if sharedStats then
                    sharedStats.shotCoordinateAlerts = (sharedStats.shotCoordinateAlerts or 0) + 1
                end
            end
        end
    end
end

local function installShootWrapper()
    if Bridge.originalShootOnComplete then return true end
    if type(ZombieActions) ~= "table" or type(ZombieActions.Shoot) ~= "table"
            or type(ZombieActions.Shoot.onComplete) ~= "function" then
        return false, "ZombieActions.Shoot.onComplete unavailable"
    end

    local original = ZombieActions.Shoot.onComplete
    Bridge.originalShootOnComplete = original
    ZombieActions.Shoot.onComplete = function(bandit, task)
        local brain = bandit and BanditBrain.Get(bandit) or nil
        local weapon = brain and brain.weapons and task and brain.weapons[task.slot] or nil
        local bulletsBefore = weapon and weapon.bulletsLeft or nil
        local originalCacheLightZ = BanditZombie.CacheLightZ

        BanditZombie.CacheLightZ = {}
        local ok, result = pcall(original, bandit, task)
        BanditZombie.CacheLightZ = originalCacheLightZ
        if not ok then error(result) end

        local fired = weapon and bulletsBefore ~= nil and weapon.bulletsLeft ~= nil
            and weapon.bulletsLeft < bulletsBefore
        if fired then alertShotCoordinates(bandit, weapon, originalCacheLightZ) end
        return result
    end

    return true
end

local function postBanditUpdate(zombie)
    local wasOrdinary = ordinaryTick[zombie] == true
    ordinaryTick[zombie] = nil
    nonCombatTick[zombie] = nil
    predicateProbeAt[zombie] = nil

    if not wasOrdinary or not zombie then return end
    local alive = false
    local okAlive, value = pcall(function() return zombie:isAlive() end)
    alive = okAlive and value == true
    if not alive then return end

    if not prepareOrdinaryZombie(zombie) then return end
    updateOrdinaryZombieCoordinateCombat(zombie)
end

function Bridge.install()
    if Bridge.installed then return true end

    if type(GetBanditClusterData) ~= "function" then
        log("ERROR", "GetBanditClusterData unavailable")
        return false
    end

    local fingerprintsOk, fingerprintErr = validateUpstream()
    if not fingerprintsOk then
        log("ERROR", "upstream validation failed: " .. tostring(fingerprintErr))
        return false
    end

    local predicateOk, predicateErr = installPredicateBridge()
    if not predicateOk then log("ERROR", predicateErr); return false end

    local taskOk, taskErr = installNonCombatTaskGates()
    if not taskOk then log("ERROR", taskErr); return false end

    local deathOk, deathErr = installDeathKeyGuard()
    if not deathOk then log("ERROR", deathErr); return false end

    local shootOk, shootErr = installShootWrapper()
    if not shootOk then log("ERROR", shootErr); return false end

    Events.OnZombieUpdate.Add(postBanditUpdate)
    Bridge.installed = true
    log("BOOT", "mode=PREDICATE_BRIDGE source=Bandits2 runtimeTransform=false bundledUpstream=false"
        .. " pursuit=coordinate-only nonCombatProgram=LCCQFQuestGiver")
    return true
end

Bridge.install()
