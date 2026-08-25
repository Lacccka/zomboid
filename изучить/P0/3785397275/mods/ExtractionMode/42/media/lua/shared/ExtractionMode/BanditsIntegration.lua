require "ExtractionMode/Config"
require "ExtractionMode/Util"

ExtractionMode = ExtractionMode or {}

local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Integration = {}
local originalRaidChances = {}
local lastApiAttemptMs = -1
local lastSuppressionMs = -1
local LEGACY_RAID_KEY_PREFIX = "ExtractionModeRaid:"
local apiAvailable = false
local unavailableLogged = false
local banditsActivated = nil

local function enabled()
    return Config.value("EnableBanditsIntegration") == true
end

local function suppressAutonomousRaidsEnabled()
    return Config.value("SuppressBanditsAutonomousRaids") == true
end

local function activatedModsContain(modId)
    if modId == "Bandits2" and banditsActivated ~= nil then return banditsActivated end
    local mods = getActivatedMods and getActivatedMods()
    if mods == nil then return false end
    for index = 0, mods:size() - 1 do
        local activeId = tostring(mods:get(index) or ""):gsub("^\\", "")
        if activeId == modId then
            if modId == "Bandits2" then banditsActivated = true end
            return true
        end
    end
    if modId == "Bandits2" then banditsActivated = false end
    return false
end

local function globalsReady()
    return type(BanditCustom) == "table"
        and type(BanditCustom.ClanGetAll) == "function"
        and type(BanditCustom.GetFromClan) == "function"
        and type(BanditServer) == "table"
        and type(BanditServer.Spawner) == "table"
        and type(BanditServer.Spawner.Clan) == "function"
        and type(GetBanditClusterData) == "function"
        and type(TransmitBanditCluster) == "function"
end

local function loadApi()
    if not enabled() then return false end
    if globalsReady() then
        apiAvailable = true
        return true
    end
    if not activatedModsContain("Bandits2") then return false end

    local now = Util.nowMs()
    if lastApiAttemptMs >= 0 and now - lastApiAttemptMs < 5000 then return apiAvailable end
    lastApiAttemptMs = now

    -- Bandits auto-loads its own files. We deliberately wait for its public
    -- globals instead of requiring its scripts and risking duplicate event hooks.
    apiAvailable = globalsReady()
    if not apiAvailable and not unavailableLogged then
        unavailableLogged = true
        Util.log("Bandits2 is enabled, but its server spawning API is not ready; integration will retry")
    end
    return apiAvailable
end

local function isHostileAssaultClan(clan)
    local spawn = clan and clan.spawn
    return spawn ~= nil and spawn.assault == true and spawn.friendly ~= true
end

local function profileCount(cid)
    if type(BanditCustom) ~= "table" or type(BanditCustom.GetFromClan) ~= "function" then return 0 end
    local profiles = BanditCustom.GetFromClan(cid) or {}
    local count = 0
    for _ in pairs(profiles) do count = count + 1 end
    return count
end

local function restoreAutonomousRaidChances()
    if type(BanditCustom) ~= "table" or type(BanditCustom.ClanGetAll) ~= "function" then return end
    local clans = BanditCustom.ClanGetAll() or {}
    for cid, chance in pairs(originalRaidChances) do
        local clan = clans[cid]
        if clan and clan.spawn and tonumber(clan.spawn.spawnChance) == 0 then
            clan.spawn.spawnChance = chance
        end
    end
end

function Integration.enforceSuppression(force)
    if not enabled() then
        restoreAutonomousRaidChances()
        return false
    end
    if not loadApi() then return false end

    local now = Util.nowMs()
    if force ~= true and lastSuppressionMs >= 0 and now - lastSuppressionMs < 1000 then
        return true
    end
    lastSuppressionMs = now

    if not suppressAutonomousRaidsEnabled() then
        restoreAutonomousRaidChances()
        return true
    end

    local suppressed = 0
    for cid, clan in pairs(BanditCustom.ClanGetAll() or {}) do
        local configuredChance = tonumber(originalRaidChances[cid])
            or tonumber(clan and clan.spawn and clan.spawn.spawnChance) or 0
        if isHostileAssaultClan(clan) and configuredChance > 0 then
            local currentChance = tonumber(clan.spawn.spawnChance) or 0
            -- A positive value means Bandits reloaded or another configuration
            -- source intentionally changed the clan; preserve that latest weight
            -- before suppressing the next autonomous roll.
            if currentChance > 0 or originalRaidChances[cid] == nil then
                originalRaidChances[cid] = currentChance
            end
            clan.spawn.spawnChance = 0
            suppressed = suppressed + 1
        end
    end
    if suppressed > 0 and force == true then
        Util.log("Extraction Mode now controls " .. tostring(suppressed)
            .. " hostile Bandits raid clan(s); their autonomous rolls are disabled")
    end
    return true
end

local function clearEncounterFields(data)
    data.banditEncounterRaidId = nil
    data.banditEncounterState = nil
    data.banditEncounterAtHour = nil
    data.banditEncounterAttempts = nil
    data.banditEncounterClan = nil
    data.banditEncounterSpawnedCount = nil
    data.banditRaidIds = {}
end

local function scheduleNextEncounter(data, reason)
    local minimumMinutes = math.max(0,
        math.floor((tonumber(Config.value("BanditAttackWindowMinimumHours")) or 1) * 60))
    local maximumMinutes = math.max(0,
        math.floor((tonumber(Config.value("BanditAttackWindowMaximumHours")) or 2) * 60))
    if maximumMinutes < minimumMinutes then minimumMinutes, maximumMinutes = maximumMinutes, minimumMinutes end

    local delayMinutes = minimumMinutes
    if maximumMinutes > minimumMinutes then
        delayMinutes = minimumMinutes + ZombRand(maximumMinutes - minimumMinutes + 1)
    end

    data.banditEncounterAtHour = Util.worldHours() + delayMinutes / 60
    data.banditEncounterAttempts = 0
    data.banditEncounterState = "SCHEDULED"
    Util.log("Next Bandits attack roll scheduled for raid " .. tostring(data.raidId)
        .. " in " .. tostring(delayMinutes) .. " in-game minute(s)"
        .. (reason and (" after " .. tostring(reason)) or ""))
end

function Integration.beginRaid(data)
    if data == nil then return end
    clearEncounterFields(data)
    data.banditEncounterRaidId = tonumber(data.raidId) or 0
    data.banditEncounterAttempts = 0

    if not enabled() then
        data.banditEncounterState = "DISABLED"
        return
    end

    Integration.enforceSuppression(true)
    scheduleNextEncounter(data)
end

local function raidParticipants(data, players)
    local result = {}
    for _, player in ipairs(players or {}) do
        if player and not player:isDead()
            and data.participants[Util.username(player)] == true then
            result[#result + 1] = player
        end
    end
    return result
end

local function clanCandidates(day)
    local active = {}
    local nearest = {}
    local nearestDistance = nil
    for cid, clan in pairs(BanditCustom.ClanGetAll() or {}) do
        local availableProfiles = profileCount(cid)
        if isHostileAssaultClan(clan) and availableProfiles > 0 then
            local spawn = clan.spawn
            if originalRaidChances[cid] == nil then
                originalRaidChances[cid] = tonumber(spawn.spawnChance) or 0
            end
            local originalChance = tonumber(originalRaidChances[cid]) or 0
            -- A zero-chance clan was deliberately disabled in Bandits' own
            -- configuration, so do not re-enable it through this integration.
            if originalChance > 0 then
                local startDay = tonumber(spawn.dayStart) or 0
                local endDay = tonumber(spawn.dayEnd) or startDay
                if endDay < startDay then startDay, endDay = endDay, startDay end
                local candidate = {
                    cid = cid,
                    weight = math.max(1, math.floor(originalChance * 100)),
                    profileCount = availableProfiles,
                }
                if day >= startDay and day <= endDay then
                    active[#active + 1] = candidate
                else
                    local distance = day < startDay and (startDay - day) or (day - endDay)
                    if nearestDistance == nil or distance < nearestDistance then
                        nearestDistance = distance
                        nearest = { candidate }
                    elseif distance == nearestDistance then
                        nearest[#nearest + 1] = candidate
                    end
                end
            end
        end
    end
    if #active > 0 then return active end
    return nearest
end

local function weightedCandidate(candidates)
    local total = 0
    for _, candidate in ipairs(candidates or {}) do total = total + candidate.weight end
    if total <= 0 then return nil end
    local roll = ZombRand(total)
    for _, candidate in ipairs(candidates) do
        if roll < candidate.weight then return candidate end
        roll = roll - candidate.weight
    end
    return candidates[#candidates]
end

local function tableSize(value)
    local count = 0
    for _ in pairs(type(value) == "table" and value or {}) do count = count + 1 end
    return count
end

local function lowTierHordeCandidate()
    if not loadApi() then return nil end
    local minimum = math.max(6, math.floor(tonumber(Config.value("BanditHordeGroupMinimum")) or 8))
    local maximum = math.max(minimum, math.floor(tonumber(Config.value("BanditHordeGroupMaximum")) or 12))
    local best = nil

    for cid, clan in pairs(BanditCustom.ClanGetAll() or {}) do
        if isHostileAssaultClan(clan) then
            local profiles = BanditCustom.GetFromClan(cid) or {}
            local count = tableSize(profiles)
            local firearmCount = 0
            local meleeOnlyCount = 0
            local gearScore = 0

            for _, profile in pairs(profiles) do
                local weapons = profile.weapons or {}
                local ammo = profile.ammo or {}
                local firearm = weapons.primary ~= nil or weapons.secondary ~= nil
                if firearm then firearmCount = firearmCount + 1 else meleeOnlyCount = meleeOnlyCount + 1 end

                gearScore = gearScore + tableSize(profile.clothing) * 0.08
                    + tableSize(profile.bag) * 0.25
                    + (tonumber(ammo.primary) or 0) * 0.7
                    + (tonumber(ammo.secondary) or 0) * 0.5
                for _, slot in ipairs({ "primary", "secondary" }) do
                    local weapon = tostring(weapons[slot] or ""):lower()
                    if weapon ~= "" then
                        gearScore = gearScore + 2
                        if weapon:find("assaultrifle", 1, true) or weapon:find("huntingrifle", 1, true)
                            or weapon:find("varmintrifle", 1, true) then
                            gearScore = gearScore + 6
                        elseif weapon:find("shotgun", 1, true) then
                            gearScore = gearScore + 2
                        end
                    end
                end
                local general = profile.general or {}
                gearScore = gearScore + ((tonumber(general.health) or 5)
                    + (tonumber(general.sight) or 5)
                    + (tonumber(general.endurance) or 5)
                    + (tonumber(general.strength) or 5)) * 0.05
            end

            if count >= minimum and firearmCount > 0 and meleeOnlyCount > 0 then
                local groupSize = math.min(maximum, count)
                local sizePenalty = math.abs(groupSize - minimum) * 0.35
                if count > maximum then sizePenalty = sizePenalty + 5 end
                local score = gearScore / math.max(1, count) + sizePenalty
                if best == nil or score < best.score then
                    best = { cid = cid, weight = 1, profileCount = count,
                        requestedCount = groupSize, score = score }
                end
            end
        end
    end
    return best
end

local function candidateForClan(cid)
    local clan = BanditCustom.ClanGetAll() and BanditCustom.ClanGetAll()[cid]
    local count = profileCount(cid)
    if not isHostileAssaultClan(clan) or count <= 0 then return nil end
    return {
        cid = cid,
        weight = 1,
        profileCount = count,
    }
end

local function clanDisplayName(cid, clan)
    local name = clan and clan.general and clan.general.name or tostring(cid)
    return tostring(name):gsub("^%d+_", "")
end

function Integration.debugClanSummaries()
    local result = {}
    if not loadApi() then return result end
    Integration.enforceSuppression(false)
    for cid, clan in pairs(BanditCustom.ClanGetAll() or {}) do
        if isHostileAssaultClan(clan) and profileCount(cid) > 0 then
            result[#result + 1] = {
                cid = tostring(cid),
                name = clanDisplayName(cid, clan),
                dayStart = tonumber(clan.spawn.dayStart) or 0,
                dayEnd = tonumber(clan.spawn.dayEnd) or 0,
            }
        end
    end
    table.sort(result, function(a, b)
        if a.dayStart == b.dayStart then return a.name < b.name end
        return a.dayStart < b.dayStart
    end)
    return result
end

local function farEnoughFromPlayers(square, players, minimumDistance)
    local point = { x = square:getX(), y = square:getY() }
    local minimumSquared = minimumDistance * minimumDistance
    for _, player in ipairs(players) do
        if math.floor(player:getZ()) == math.floor(square:getZ())
            and Util.distanceSquaredXY(point, { x = player:getX(), y = player:getY() }) < minimumSquared then
            return false
        end
    end
    return true
end

local function spawnPointsNear(target, players, amount)
    local result = {}
    local occupied = {}
    local radius = math.max(25, math.floor(tonumber(Config.value("BanditSpawnRadius")) or 55))
    local minimumPlayerDistance = math.max(20, math.floor(radius * 0.55))
    local z = math.floor(target:getZ())
    local baseAngle = (ZombRand(10000) / 10000) * math.pi * 2
    local attempts = 0
    while #result < amount and attempts < amount * 30 do
        attempts = attempts + 1
        local angle = baseAngle + ((ZombRand(2001) - 1000) / 1000) * 0.24
        local distance = radius + ZombRand(13) - 6
        local x = math.floor(target:getX() + math.cos(angle) * distance)
        local y = math.floor(target:getY() + math.sin(angle) * distance)
        local key = tostring(x) .. ":" .. tostring(y) .. ":" .. tostring(z)
        local square = getCell() and getCell():getGridSquare(x, y, z)
        if not occupied[key] and Util.isSafeOutdoorLandSquare(square)
            and farEnoughFromPlayers(square, players, minimumPlayerDistance) then
            occupied[key] = true
            result[#result + 1] = { x = x, y = y, z = z }
        end
    end
    return result
end

local function allBanditBrains()
    local result = {}
    if type(BanditClusters) ~= "table" then return result end
    for _, cluster in pairs(BanditClusters) do
        if type(cluster) == "table" then
            for id, brain in pairs(cluster) do
                if type(brain) == "table" and brain.id ~= nil then
                    result[tostring(id)] = { id = id, brain = brain, cluster = cluster }
                end
            end
        end
    end
    return result
end

-- Older Extraction Mode builds passed a textual raid marker through Bandits'
-- `key` spawn argument. Bandits reserves that field for numeric building-key
-- IDs and calls InventoryItem:setKeyId(brain.key) when an NPC dies. Remove the
-- legacy marker from both live and persisted brains before Bandits can use it.
local function clearLegacyRaidKey(brain)
    local value = brain and brain.key
    if type(value) ~= "string" or value:sub(1, #LEGACY_RAID_KEY_PREFIX) ~= LEGACY_RAID_KEY_PREFIX then
        return false
    end
    brain.key = nil
    return true
end

local function repairLegacyRaidKeys(data)
    if not loadApi() then return 0 end
    local ownedIds = data and data.banditRaidIds or {}
    local repaired = 0
    for key, entry in pairs(allBanditBrains()) do
        local owned = ownedIds[key] == true
            or (entry.brain and entry.brain.ExtractionModeRaidId ~= nil)
        if owned and clearLegacyRaidKey(entry.brain) then
            entry.cluster[entry.id] = entry.brain
            pcall(function() TransmitBanditCluster(entry.id) end)
            repaired = repaired + 1
        end
    end
    if repaired > 0 then
        Util.log("Repaired " .. tostring(repaired) .. " legacy Bandits building-key value(s)")
    end
    return repaired
end

local function pointNearSpawn(brain, spawnPoints)
    local born = brain and brain.bornCoords
    if born == nil then return false end
    for _, point in ipairs(spawnPoints) do
        if math.floor(tonumber(born.z) or 0) == math.floor(tonumber(point.z) or 0)
            and Util.distanceSquaredXY(born, point) <= 16 then
            return true
        end
    end
    return false
end

local function tagSpawnedBandits(data, before, spawnPoints, target)
    local after = allBanditBrains()
    local targetId = nil
    if type(BanditUtils) == "table" and type(BanditUtils.GetCharacterID) == "function" then
        local ok, value = pcall(BanditUtils.GetCharacterID, target)
        if ok then targetId = value end
    end
    local newIds = {}
    local count = 0
    for key, entry in pairs(after) do
        local brain = entry.brain
        if before[key] == nil and brain.program and brain.program.name == "Bandit"
            and brain.hostileP == true and pointNearSpawn(brain, spawnPoints)
            and (targetId == nil or brain.master == targetId) then
            clearLegacyRaidKey(brain)
            brain.ExtractionModeRaidId = tonumber(data.raidId) or 0
            entry.cluster[entry.id] = brain
            data.banditRaidIds[key] = true
            newIds[key] = true
            count = count + 1
            pcall(function() TransmitBanditCluster(entry.id) end)
        end
    end

    local zombies = getCell() and getCell():getZombieList()
    if zombies then
        for index = 0, zombies:size() - 1 do
            local zombie = zombies:get(index)
            local id = zombie and tostring(zombie:getPersistentOutfitID()) or nil
            if id and newIds[id] then
                zombie:getModData().ExtractionModeRaidId = tonumber(data.raidId) or 0
                zombie:getModData().ExtractionModeBandit = true
            end
        end
    end
    return count
end

local function retryEncounter(data, reason)
    data.banditEncounterAttempts = (tonumber(data.banditEncounterAttempts) or 0) + 1
    if data.banditEncounterAttempts >= 6 then
        Util.log("Bandits attack window abandoned after repeated spawn failures: " .. tostring(reason))
        scheduleNextEncounter(data, "spawn retries were exhausted")
    else
        data.banditEncounterAtHour = Util.worldHours() + 5 / 60
        data.banditEncounterState = "RETRY"
        Util.log("Bandits attack spawn deferred: " .. tostring(reason))
    end
end

local function spawnBanditGroup(data, players, candidate, requestedCount)
    if not loadApi() then return false, "server API unavailable" end
    Integration.enforceSuppression(true)

    local participants = raidParticipants(data, players)
    if #participants == 0 then return false, "no living raid participants" end
    local target = participants[ZombRand(#participants) + 1]
    if candidate == nil then return false, "no hostile assault clan profiles available" end

    local minimum = math.max(1, math.floor(tonumber(Config.value("BanditGroupMinimum")) or 2))
    local maximum = math.max(1, math.floor(tonumber(Config.value("BanditGroupMaximum")) or 5))
    if maximum < minimum then minimum, maximum = maximum, minimum end
    local requested = tonumber(requestedCount)
    if requested == nil then
        requested = minimum
        if maximum > minimum then requested = minimum + ZombRand(maximum - minimum + 1) end
    end
    requested = math.max(1, math.floor(requested))
    requested = math.min(requested, candidate.profileCount)
    local spawnPoints = spawnPointsNear(target, participants, requested)
    if #spawnPoints == 0 then return false, "no loaded outdoor approach tiles" end

    local before = allBanditBrains()
    local args = {
        cid = candidate.cid,
        size = #spawnPoints,
        spawnPoints = spawnPoints,
        program = "Bandit",
        permanent = false,
        hostile = true,
        hostileP = true,
    }
    local ok, errorMessage = pcall(BanditServer.Spawner.Clan, target, args)
    if not ok then return false, tostring(errorMessage) end

    local spawned = tagSpawnedBandits(data, before, spawnPoints, target)
    if spawned <= 0 then return false, "Bandits spawner created no trackable NPCs" end
    return true, spawned
end

function Integration.spawnHordeReplacement(data, players)
    if data == nil then return false, "raid state is unavailable" end
    if not loadApi() then return false, "Bandits2 server API is unavailable" end
    Integration.enforceSuppression(true)
    local candidate = lowTierHordeCandidate()
    if candidate == nil then return false, "no large low-tier mixed-weapon clan is available" end
    local ok, result = spawnBanditGroup(data, players, candidate, candidate.requestedCount)
    if not ok then return false, result end
    data.banditEncounterSpawnedCount = (tonumber(data.banditEncounterSpawnedCount) or 0) + result
    return true, result
end

local function spawnEncounter(data, players)
    if not loadApi() then retryEncounter(data, "server API unavailable"); return false end
    local day = math.max(0, Util.worldHours() / 24)
    local candidate = weightedCandidate(clanCandidates(day))
    local ok, result = spawnBanditGroup(data, players, candidate)
    if not ok then retryEncounter(data, result); return false end
    local spawned = result
    data.banditEncounterClan = tostring(candidate.cid)
    data.banditEncounterSpawnedCount = (tonumber(data.banditEncounterSpawnedCount) or 0) + spawned
    Util.log("Spawned " .. tostring(spawned) .. " hostile Bandits NPC(s) for raid "
        .. tostring(data.raidId))
    return true
end

function Integration.spawnDebugRaid(data, players, requestedClanId)
    if data == nil then return false, "raid state is unavailable" end
    if not loadApi() then return false, "Bandits2 server API is unavailable" end
    Integration.enforceSuppression(true)

    local candidate = nil
    local clanId = tostring(requestedClanId or "")
    if clanId ~= "" then
        candidate = candidateForClan(clanId)
        if candidate == nil then return false, "the selected hostile clan is unavailable" end
    else
        candidate = weightedCandidate(clanCandidates(math.max(0, Util.worldHours() / 24)))
        if candidate == nil then return false, "no era-appropriate hostile clan is available" end
    end

    local ok, result = spawnBanditGroup(data, players, candidate)
    if not ok then return false, result end
    data.banditEncounterSpawnedCount = (tonumber(data.banditEncounterSpawnedCount) or 0) + result
    Util.log("Debug-spawned " .. tostring(result) .. " Bandits NPC(s) from clan "
        .. clanDisplayName(candidate.cid, BanditCustom.ClanGetAll()[candidate.cid]))
    return true, result, clanDisplayName(candidate.cid, BanditCustom.ClanGetAll()[candidate.cid])
end

function Integration.process(data, players)
    Integration.enforceSuppression(false)
    if data == nil then return end
    repairLegacyRaidKeys(data)
    local activeRaid = data.state == Config.STATE_RAID or data.state == Config.STATE_EXTRACTING
        or data.state == Config.STATE_BOARDING
    if not activeRaid then return end
    if tonumber(data.banditEncounterRaidId) ~= tonumber(data.raidId) then
        Integration.beginRaid(data)
    end

    local state = data.banditEncounterState
    if state == "NONE" or state == "SPAWNED" or state == "FAILED" then
        -- Continue older in-progress saves whose one-shot encounter had already
        -- reached a terminal state before repeating attack windows were added.
        scheduleNextEncounter(data, "migrating the previous one-shot state")
        return
    elseif state == "SPAWNING" then
        -- A save made during the due-window tick should retry instead of leaving
        -- the recurring scheduler permanently wedged.
        data.banditEncounterState = "RETRY"
        data.banditEncounterAtHour = Util.worldHours()
        state = "RETRY"
    end

    local dueHour = tonumber(data.banditEncounterAtHour)
    if (state == "SCHEDULED" or state == "RETRY")
        and dueHour and Util.worldHours() >= dueHour then
        if state == "SCHEDULED" then
            local chance = math.max(0, math.min(100,
                tonumber(Config.value("BanditAttackWindowChancePercent")) or 15))
            if ZombRand(10000) >= math.floor(chance * 100) then
                Util.log("No Bandits attack was rolled for the current raid window")
                scheduleNextEncounter(data, "the roll did not trigger an attack")
                return
            end
        end

        data.banditEncounterState = "SPAWNING"
        if spawnEncounter(data, players) then
            scheduleNextEncounter(data, "a successful attack")
        end
    end
end

function Integration.cleanupRaid(data)
    if data == nil then return 0 end
    repairLegacyRaidKeys(data)
    local ids = data.banditRaidIds or {}
    local removed = 0
    local zombies = getCell() and getCell():getZombieList()
    if zombies then
        local remove = {}
        for index = 0, zombies:size() - 1 do
            local zombie = zombies:get(index)
            local id = zombie and tostring(zombie:getPersistentOutfitID()) or nil
            if id and ids[id] == true then remove[#remove + 1] = zombie end
        end
        for _, zombie in ipairs(remove) do
            pcall(function()
                zombie:removeFromWorld()
                zombie:removeFromSquare()
            end)
            removed = removed + 1
        end
    end

    if loadApi() then
        for key, owned in pairs(ids) do
            if owned == true then
                local id = tonumber(key) or key
                local ok, cluster = pcall(GetBanditClusterData, id)
                if ok and cluster and cluster[id] ~= nil then
                    cluster[id] = nil
                    pcall(function() TransmitBanditCluster(id) end)
                end
            end
        end
    end
    clearEncounterFields(data)
    if removed > 0 then Util.log("Removed " .. tostring(removed) .. " surviving raid Bandits NPC(s)") end
    return removed
end

-- Extraction Mode deliberately removes every zombie entity from the hideout
-- and insertion safety radius. Bandits represents NPCs with zombie entities,
-- so clear its matching brain record before those generic safety removals to
-- prevent an erased NPC from being restored later as a ghost bandit.
function Integration.detachZombieBrain(zombie)
    if zombie == nil or not loadApi() then return false end
    local id = zombie:getPersistentOutfitID()
    local ok, cluster = pcall(GetBanditClusterData, id)
    if not ok or cluster == nil or cluster[id] == nil then return false end
    cluster[id] = nil
    pcall(function() TransmitBanditCluster(id) end)
    return true
end

function Integration.isBanditZombie(zombie)
    if zombie == nil or not loadApi() then return false end
    local id = zombie:getPersistentOutfitID()
    local ok, cluster = pcall(GetBanditClusterData, id)
    return ok and cluster ~= nil and cluster[id] ~= nil
end

function Integration.isAvailable()
    return loadApi()
end

ExtractionMode.BanditsIntegration = Integration
return Integration
