-- Lacccka B42 NPC Fixes: Build 42.20.4 callback bridge for Bandits2.
--
-- B42.20.4 removed runtime Lua source compilation. This bridge leaves the
-- installed Bandits2 sources untouched, identifies Bandits' registered event
-- callbacks through LuaEventManager, and wraps only the unsafe seams.
-- No third-party implementation file is bundled or executed from text.

if isServer() then return end

require "Bandit"
require "BanditBrain"
require "BanditZombie"
require "BanditUtils"
require "BanditCompatibility"
require "ZombieActions/ZAShoot"

local MARKER = "loadstring-free-callback-bridge-v1"
local MOD_ID = "Bandits2"
local BANDIT_UPDATE_PATH = "media/lua/client/BanditUpdate.lua"
local ZA_SHOOT_PATH = "media/lua/shared/ZombieActions/ZAShoot.lua"
local LCC_PURSUIT_ALIGN_DIST2 = 0.5625 -- 0.75 tile
local LCC_PURSUIT_IDLE_RETRY_MS = 750

LCC_NPCFIXES_CALLBACK_BRIDGE = LCC_NPCFIXES_CALLBACK_BRIDGE or {}
local Bridge = LCC_NPCFIXES_CALLBACK_BRIDGE
Bridge.marker = MARKER
Bridge.originalCallbacks = Bridge.originalCallbacks or {}
Bridge.installed = Bridge.installed or false
Bridge.installAttempts = Bridge.installAttempts or 0
Bridge.stats = Bridge.stats or {
    coordinatePursuits = 0,
    closeBites = 0,
    nonCombatCombatSkips = 0,
    nonCombatCollisionSkips = 0,
    nonCombatHitSkips = 0,
    invalidDeathKeysSuppressed = 0,
    shotCoordinateAlerts = 0,
    staleRelationsCleared = 0,
}

local pursuitRetryAt = setmetatable({}, { __mode = "k" })
local biteTab = setmetatable({}, { __mode = "k" })
local warned = {}

local function log(level, message)
    print("[LCC][NPCFixes][CallbackBridge][" .. tostring(level) .. "] marker=" .. MARKER .. " " .. tostring(message))
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
        "local function OnBanditUpdate(zombie)",
        "local function OnHitZombie(zombie, attacker, bodyPartType, handWeapon)",
        "local function OnZombieDead(bandit)",
        "                zombie:pathToCharacter(bandit)",
        [[                    if zombie and bandit  then
                        zombie:spotted(bandit, true)
                        zombie:addAggro(bandit, 1)
                        zombie:setTarget(bandit)
                        zombie:setAttackedBy(bandit)]],
        "local combatTasks = ManageCombat(bandit)",
        "local colissionTasks = ManageCollisions(bandit)",
        "item:setKeyId(brain.key)",
        "Events.OnZombieUpdate.Add(OnBanditUpdate)",
        "Events.OnHitZombie.Add(OnHitZombie)",
        "Events.OnZombieDead.Add(OnZombieDead)",
    })
    if not updateOk then return false, updateReason end

    local shootOk, shootReason = validateSource(ZA_SHOOT_PATH, {
        "ZombieActions.Shoot.onComplete = function(bandit, task)",
        [[                    zombie:spottedNew(shooter, true)
                    zombie:addAggro(shooter, 1)
                    zombie:setTarget(shooter)]],
    })
    if not shootOk then return false, shootReason end

    return true
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

local function isPendingNonCombatBandit(zombie)
    if not zombie or not BanditUtils or type(BanditUtils.GetZombieID) ~= "function"
            or not BanditZombie or type(BanditZombie.CacheLightB) ~= "table" then
        return false
    end
    local okId, id = pcall(BanditUtils.GetZombieID, zombie)
    if not okId or id == nil then return false end
    local light = BanditZombie.CacheLightB[id]
    return light ~= nil and isNonCombatBrain(light.brain)
end

local function isNonCombatLight(light)
    if not light then return false end
    if isNonCombatBrain(light.brain) then return true end
    local id = light.id
    local real = id and BanditZombie and BanditZombie.Cache and BanditZombie.Cache[id] or nil
    return real ~= nil and isNonCombatBandit(real)
end

local function currentBanditTarget(zombie)
    local ok, target = pcall(function() return zombie:getTarget() end)
    if ok and isBandit(target) then return target end
    return nil
end

local function sanitizeUnsafeBanditRelation(zombie)
    if not isOrdinaryZombie(zombie) then return false end
    local changed = false

    local target = currentBanditTarget(zombie)
    if target then
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
            local okTarget, targetChar = pcall(function() return pfb:getTargetChar() end)
            if okTarget and isBandit(targetChar) then
                if pcall(function() pfb:cancel() end) then changed = true end
            end
        end
    end

    local okSpotted, spotted = pcall(function() return zombie.spottedLast end)
    if okSpotted and isBandit(spotted) then
        pcall(function() zombie.spottedLast = nil end)
        changed = true
    end

    if changed then
        Bridge.stats.staleRelationsCleared = Bridge.stats.staleRelationsCleared + 1
    end
    return changed
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
            if zombie:getActionStateName() ~= "idle" then
                return false
            end

            local now = getTimestampMs()
            local lastRetry = pursuitRetryAt[zombie] or 0
            if now - lastRetry < LCC_PURSUIT_IDLE_RETRY_MS then
                return false
            end
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
    if distPlayer2 < 4 and math.abs(pz - zz) < 0.3 then
        return nil
    end

    local bestDist2 = math.huge
    local bestLight, bestId = nil, nil
    local cache = BanditZombie and BanditZombie.CacheLightB or nil
    if type(cache) ~= "table" then return nil end

    for id, light in pairs(cache) do
        if light and not isNonCombatLight(light) then
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
                            bestLight = light
                            bestId = light.id or id
                        end
                    end
                end
            end
        end
    end

    if not bestLight or bestDist2 >= 400 then return nil end
    local bandit = bestId and BanditZombie.Cache and BanditZombie.Cache[bestId] or nil
    if not bandit or not bandit:isAlive() or isNonCombatBandit(bandit) then return nil end
    return bandit, bestLight, bestDist2
end

local function processCoordinateBite(zombie)
    local state = biteTab[zombie]
    if not state then return false end

    local bandit = state.bandit
    if not bandit or not bandit:isAlive() or isNonCombatBandit(bandit) then
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
            if ZombRand(4) == 1 then
                zombie:playSound("ZombieBite")
            else
                zombie:playSound("ZombieScratch")
            end

            local teeth = BanditCompatibility.InstanceItem("Base.RollingPin")
            if teeth then
                BanditCompatibility.Splash(bandit, teeth, zombie)
                bandit:setHitFromBehind(zombie:isBehind(bandit))
                if instanceof(bandit, "IsoZombie") then
                    bandit:setPlayerAttackPosition(bandit:testDotSide(zombie))
                end

                if not bandit:isOnKillDone() and not Bandit.HasTaskType(bandit, "Die") then
                    Bandit.ClearTasks(bandit)
                    bandit:Hit(teeth, zombie, 1.01, false, 1, false)
                    Bandit.Say(bandit, "DRAGDOWN", true)
                    Bandit.UpdateInfection(bandit, 0.001)

                    local player = getSpecificPlayer(0)
                    if player then
                        local args = {
                            id = BanditUtils.GetCharacterID(bandit),
                            h = bandit:getHealth(),
                        }
                        sendClientCommand(player, "Sync", "Health", args)
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
    if not isOrdinaryZombie(zombie) or not zombie:isAlive() then return end
    if processCoordinateBite(zombie) then return end

    local asn = zombie:getActionStateName()
    if asn == "bumped" or asn == "onground" or asn == "climbfence"
            or asn == "getup" or asn == "turnalerted" then
        return
    end
    if zombie:isProne() then return end

    local bandit, light, dist2 = selectNearestCombatBandit(zombie)
    if not bandit or not light then return end

    local zz = zombie:getZ()
    if dist2 > 9 then
        if zombie:CanSee(bandit) then
            pathToCoordinate(zombie, light.x, light.y, light.z)
        end
        return
    end

    pathToCoordinate(zombie, light.x, light.y, light.z)

    if dist2 >= 0.64 or math.abs(zz - light.z) >= 0.3 then return end
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
        if bandit:isProne() or bandit:isCrawling() then
            zombie:setBumpType("BiteLow")
        else
            zombie:setBumpType("Bite")
        end
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

local function callNonCombatBanditUpdate(original, bandit)
    local originalIsSleeping = Bandit.IsSleeping
    local originalGetTask = Bandit.GetTask
    local combatGateSeen = false
    local collisionSuppressed = false

    Bandit.IsSleeping = function(candidate, ...)
        if candidate == bandit and not combatGateSeen then
            combatGateSeen = true
            Bridge.stats.nonCombatCombatSkips = Bridge.stats.nonCombatCombatSkips + 1
            return true
        end
        return originalIsSleeping(candidate, ...)
    end

    Bandit.GetTask = function(candidate, ...)
        if candidate == bandit and combatGateSeen and not collisionSuppressed and hasCollision(bandit) then
            collisionSuppressed = true
            Bridge.stats.nonCombatCollisionSkips = Bridge.stats.nonCombatCollisionSkips + 1
            return nil
        end
        return originalGetTask(candidate, ...)
    end

    local ok, err = pcall(original, bandit)
    Bandit.IsSleeping = originalIsSleeping
    Bandit.GetTask = originalGetTask

    if not ok then error(err) end
end

local function callOrdinaryZombieUpdate(original, zombie)
    sanitizeUnsafeBanditRelation(zombie)

    local originalCacheLightB = BanditZombie.CacheLightB
    BanditZombie.CacheLightB = {}
    local ok, err = pcall(original, zombie)
    BanditZombie.CacheLightB = originalCacheLightB

    if not ok then error(err) end

    sanitizeUnsafeBanditRelation(zombie)
    updateOrdinaryZombieCoordinateCombat(zombie)
end

local function wrapOnBanditUpdate(original)
    return function(zombie)
        if not zombie then return original(zombie) end
        if isNonCombatBandit(zombie) or isPendingNonCombatBandit(zombie) then
            return callNonCombatBanditUpdate(original, zombie)
        end
        if isOrdinaryZombie(zombie) then
            return callOrdinaryZombieUpdate(original, zombie)
        end
        return original(zombie)
    end
end

local function wrapOnHitZombie(original)
    return function(zombie, attacker, bodyPartType, handWeapon)
        if isNonCombatBandit(zombie) then
            Bridge.stats.nonCombatHitSkips = Bridge.stats.nonCombatHitSkips + 1
            return
        end
        return original(zombie, attacker, bodyPartType, handWeapon)
    end
end

local function wrapOnZombieDead(original)
    return function(bandit)
        local brain = bandit and BanditBrain.Get(bandit) or nil
        local invalidKey = type(brain) == "table" and brain.key ~= nil and type(brain.key) ~= "number"
        local oldKey = invalidKey and brain.key or nil
        if invalidKey then
            brain.key = nil
            Bridge.stats.invalidDeathKeysSuppressed = Bridge.stats.invalidDeathKeysSuppressed + 1
        end

        local ok, err = pcall(original, bandit)
        if not ok then
            if invalidKey and type(brain) == "table" then brain.key = oldKey end
            error(err)
        end
    end
end

local function callbackDescription(callback)
    if not KahluaUtil or not KahluaUtil.rawTostring2 then return nil end
    local ok, description = pcall(function() return KahluaUtil.rawTostring2(callback) end)
    return ok and description or nil
end

local function getEventCallbacks(eventName)
    if not LuaEventManager or not LuaEventManager.AddEvent then
        return nil, "LuaEventManager.AddEvent unavailable"
    end
    local ok, event = pcall(function() return LuaEventManager.AddEvent(eventName) end)
    if not ok or not event or not event.callbacks then
        return nil, "callbacks unavailable for " .. tostring(eventName)
    end
    return event.callbacks
end

local function findBanditsCallback(eventName, functionName)
    local callbacks, callbacksErr = getEventCallbacks(eventName)
    if not callbacks then return nil, nil, callbacksErr end

    local foundIndex, foundCallback, foundDescription = nil, nil, nil
    local nearMatches = {}
    for i = 0, callbacks:size() - 1 do
        local callback = callbacks:get(i)
        local description = callbackDescription(callback)
        if description and string.find(description, functionName, 1, true)
                and string.find(description, "BanditUpdate.lua", 1, true) then
            nearMatches[#nearMatches + 1] = description
            if string.find(description, "MOD: Bandits", 1, true) then
                if foundCallback ~= nil then
                    return nil, nil, "multiple Bandits callbacks for " .. tostring(functionName)
                end
                foundIndex = i
                foundCallback = callback
                foundDescription = description
            end
        end
    end

    if not foundCallback then
        local suffix = #nearMatches > 0 and (" candidates=" .. table.concat(nearMatches, " || ")) or ""
        return nil, nil, "Bandits callback not found: " .. tostring(functionName) .. suffix
    end
    return callbacks, foundIndex, foundCallback, foundDescription
end

local function replaceBanditsCallback(eventName, functionName, wrapperFactory)
    local key = eventName .. ":" .. functionName
    if Bridge.originalCallbacks[key] then return true end

    local callbacks, index, original, descriptionOrErr = findBanditsCallback(eventName, functionName)
    if not callbacks then return false, descriptionOrErr end

    local wrapper = wrapperFactory(original)
    callbacks:set(index, wrapper)
    Bridge.originalCallbacks[key] = original
    log("PATCH", "event=" .. eventName .. " callback=" .. functionName .. " index=" .. tostring(index)
        .. " source=" .. tostring(descriptionOrErr))
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
        if fired then
            alertShotCoordinates(bandit, weapon, originalCacheLightZ)
        end
        return result
    end

    log("PATCH", "ZombieActions.Shoot.onComplete coordinate-alert wrapper installed")
    return true
end

function Bridge.install()
    if Bridge.installed then return true end
    Bridge.installAttempts = Bridge.installAttempts + 1

    local fingerprintsOk, fingerprintErr = validateUpstream()
    if not fingerprintsOk then
        log("ERROR", "upstream validation failed: " .. tostring(fingerprintErr))
        return false
    end

    local updateOk, updateErr = replaceBanditsCallback("OnZombieUpdate", "OnBanditUpdate", wrapOnBanditUpdate)
    if not updateOk then
        log("ERROR", "OnBanditUpdate bridge failed: " .. tostring(updateErr))
        return false
    end

    local hitOk, hitErr = replaceBanditsCallback("OnHitZombie", "OnHitZombie", wrapOnHitZombie)
    if not hitOk then
        log("ERROR", "OnHitZombie bridge failed: " .. tostring(hitErr))
        return false
    end

    local deadOk, deadErr = replaceBanditsCallback("OnZombieDead", "OnZombieDead", wrapOnZombieDead)
    if not deadOk then
        log("ERROR", "OnZombieDead bridge failed: " .. tostring(deadErr))
        return false
    end

    local shootOk, shootErr = installShootWrapper()
    if not shootOk then
        log("ERROR", "ZAShoot bridge failed: " .. tostring(shootErr))
        return false
    end

    Bridge.installed = true
    log("BOOT", "mode=CALLBACK_BRIDGE source=Bandits2 runtimeTransform=false bundledUpstream=false"
        .. " pursuit=coordinate-only nonCombatProgram=LCCQFQuestGiver")
    return true
end

local function retryInstall()
    if not Bridge.installed then Bridge.install() end
end

Bridge.install()
if not Bridge._retryRegistered then
    Events.OnGameStart.Add(retryInstall)
    Events.OnCreatePlayer.Add(function() retryInstall() end)
    Bridge._retryRegistered = true
end
