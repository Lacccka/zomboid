ExtractionMode = ExtractionMode or {}
if isClient and isClient() then
    ExtractionMode.RaidThreatAuthority = ExtractionMode.RaidThreatAuthority or {}
    return ExtractionMode.RaidThreatAuthority
end

require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/Upgrades"
require "ExtractionMode/BanditsIntegration"
require "ExtractionMode/ProjectRemnantsIntegration"
require "ExtractionMode/ModCompatibility"

local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Upgrades = ExtractionMode.Upgrades
local BanditsIntegration = ExtractionMode.BanditsIntegration
local ProjectRemnantsIntegration = ExtractionMode.ProjectRemnantsIntegration
local Compatibility = ExtractionMode.ModCompatibility
local Runtime = ExtractionMode.RaidRuntime
local Threats = {}
local helicopterOwners = {}
local vanillaZombieSpeedLogged = false
local SPEED_SPRINTER = 1
local SPEED_FAST_SHAMBLER = 2
local VANILLA_DEFAULT_ZOMBIE_SPEED = 4
local VANILLA_DEFAULT_SPRINTER_PERCENT = 0

local function activePlayers()
    local result = {}
    for _, player in ipairs(Util.players()) do
        if player and not player:isDead() and Util.username(player) ~= "" then
            result[#result + 1] = player
        end
    end
    return result
end

local function tableHasEntries(values)
    if values == nil then return false end
    for _ in pairs(values) do return true end
    return false
end

local function clampedPercent(value)
    return math.max(0, math.min(100, tonumber(value) or 0))
end

local function vanillaZombieSpeedCustomized()
    local lore = SandboxVars and SandboxVars.ZombieLore
    if lore == nil then return false end
    local speed = tonumber(lore.Speed)
    local sprinterPercent = tonumber(lore.SprinterPercentage)
    local customized = (speed ~= nil and speed ~= VANILLA_DEFAULT_ZOMBIE_SPEED)
        or (sprinterPercent ~= nil
            and sprinterPercent ~= VANILLA_DEFAULT_SPRINTER_PERCENT)
    if customized and not vanillaZombieSpeedLogged then
        vanillaZombieSpeedLogged = true
        Util.log("Custom vanilla zombie speed settings detected; Extraction Mode ambient and horde speed overrides disabled"
            .. " (Speed=" .. tostring(speed)
            .. ", SprinterPercentage=" .. tostring(sprinterPercent) .. ")")
    end
    return customized
end

local function ambientSprinterStage()
    if vanillaZombieSpeedCustomized() then return "VANILLA", nil end
    if Util.worldHours() < 7 * 24 then return "WEEK1", 0 end
    return "WEEK2_PLUS", clampedPercent(Config.value("AmbientSprinterPercentWeek2"))
end

local function assignZombieSpeed(zombie, percent, assignmentKey)
    if zombie == nil then return false end
    local modData = zombie:getModData()
    local readOk, currentSpeed = pcall(function() return zombie:getSpeedType() end)
    if not readOk then return false end
    if modData.ExtractionModeSpeedAssignment == assignmentKey then
        local shouldSprint = modData.ExtractionModeIsSprinter == true
        if shouldSprint and tonumber(currentSpeed) ~= SPEED_SPRINTER then
            return pcall(function() zombie:doZombieSpeed(SPEED_SPRINTER) end)
        elseif not shouldSprint and tonumber(currentSpeed) == SPEED_SPRINTER then
            return pcall(function() zombie:doZombieSpeed(SPEED_FAST_SHAMBLER) end)
        end
        return false
    end

    local sprinter = ZombRand(10000) < math.floor(clampedPercent(percent) * 100)
    local changed = false
    local changeOk = true
    if sprinter then
        changeOk = pcall(function() zombie:doZombieSpeed(SPEED_SPRINTER) end)
        changed = true
    elseif tonumber(currentSpeed) == SPEED_SPRINTER then
        changeOk = pcall(function() zombie:doZombieSpeed(SPEED_FAST_SHAMBLER) end)
        changed = true
    end
    if not changeOk then return false end

    modData.ExtractionModeSpeedAssignment = assignmentKey
    modData.ExtractionModeIsSprinter = sprinter
    return changed
end

function Threats.applyAmbientZombieSpeed(zombie)
    if zombie == nil then return end
    local modData = zombie:getModData()
    if vanillaZombieSpeedCustomized() then
        if modData.ExtractionModeSpeedAssignment ~= nil then
            pcall(function() zombie:doZombieSpeed() end)
            modData.ExtractionModeSpeedAssignment = nil
            modData.ExtractionModeIsSprinter = nil
        end
        return
    end
    if modData.ExtractionModeRaidId ~= nil then return end
    local stage, percent = ambientSprinterStage()
    if percent ~= nil then assignZombieSpeed(zombie, percent, "AMBIENT:" .. stage) end
end

function Threats.refreshAmbientZombieSpeeds()
    local zombieList = getCell and getCell() and getCell():getZombieList()
    if zombieList == nil then return end
    for index = 0, zombieList:size() - 1 do
        Threats.applyAmbientZombieSpeed(zombieList:get(index))
    end
end

local function zombieInsideHideoutCell(zombie)
    if zombie == nil then return false end
    local bounds = Config.hideoutCellBounds()
    local x = tonumber(zombie:getX())
    local y = tonumber(zombie:getY())
    return x ~= nil and y ~= nil
        and x >= bounds.minX and x < bounds.maxXExclusive
        and y >= bounds.minY and y < bounds.maxYExclusive
end

function Threats.playerInsideHideoutCell(player)
    if player == nil then return false end
    local bounds = Config.hideoutCellBounds()
    local point = Compatibility.playerPosition(player)
    local x = point and tonumber(point.x) or nil
    local y = point and tonumber(point.y) or nil
    return x ~= nil and y ~= nil
        and x >= bounds.minX and x < bounds.maxXExclusive
        and y >= bounds.minY and y < bounds.maxYExclusive
end

function Threats.purgeHideoutZombies()
    local cell = getCell and getCell()
    local zombieList = cell and cell:getZombieList()
    if zombieList == nil then return 0 end
    local remove = {}
    for index = 0, zombieList:size() - 1 do
        local zombie = zombieList:get(index)
        if zombieInsideHideoutCell(zombie) then remove[#remove + 1] = zombie end
    end
    for _, zombie in ipairs(remove) do
        BanditsIntegration.detachZombieBrain(zombie)
        pcall(function()
            zombie:removeFromWorld()
            zombie:removeFromSquare()
        end)
    end
    if #remove > 0 then
        Util.log("Removed " .. tostring(#remove) .. " zombie(s) from protected hideout cell 90,75")
    end
    return #remove
end

function Threats.hordeDelayBounds(data)
    local minimum = math.floor((tonumber(Config.value("HordeDelayMinimumHours")) or 8) * 60)
    local maximum = math.floor((tonumber(Config.value("HordeDelayMaximumHours")) or 11) * 60)
    if Upgrades.isInstalled(data and data.upgrades, "intel_center") then
        local bonus = math.max(0, math.floor((tonumber(Config.value("IntelHordeDelayBonusHours")) or 3) * 60))
        minimum = minimum + bonus
        maximum = maximum + bonus
    end
    if maximum < minimum then minimum, maximum = maximum, minimum end
    return minimum / 60, maximum / 60, minimum, maximum
end

function Threats.hordeMinimumTimeLabel(data)
    local startHour = tonumber(data.hordeWindowStartHour)
    if startHour == nil then return nil end
    local minimumTimeOfDay = (Util.timeOfDay() + startHour - Util.worldHours()) % 24
    local minimumHourOfDay = math.floor(minimumTimeOfDay + 0.0001)
    return string.format("%02d:00", minimumHourOfDay)
end

local function tagSpawnedZombies(list, raidId, target, sprinterPercent, speedAssignmentKey)
    if list == nil then return 0 end
    local count = 0
    for index = 0, list:size() - 1 do
        local zombie = list:get(index)
        if zombie then
            zombie:getModData().ExtractionModeRaidId = raidId
            if sprinterPercent ~= nil then
                assignZombieSpeed(zombie, sprinterPercent, speedAssignmentKey)
            end
            if target then pcall(function() zombie:pathToCharacter(target) end) end
            count = count + 1
        end
    end
    return count
end

function Threats.spawnHorde(force)
    local data = Runtime.currentStore()
    if data.hordeSpawned and force ~= true then return 0 end
    data.hordeSpawned = true
    local targets = {}
    for _, player in ipairs(activePlayers()) do
        if data.participants[Util.username(player)] == true then targets[#targets + 1] = player end
    end
    if #targets == 0 then return 0 end
    local total = math.max(1, math.floor(tonumber(Config.value("HordeSize")) or 90))
    local radius = math.max(15, math.floor(tonumber(Config.value("HordeSpawnRadius")) or 38))
    local sprinterPercent = nil
    if not vanillaZombieSpeedCustomized() then
        sprinterPercent = Config.value("HordeSprinterPercent")
    end
    local spawned = 0
    local attempts = 0
    while spawned < total and attempts < total * 3 do
        attempts = attempts + 1
        local target = targets[(attempts - 1) % #targets + 1]
        local angle = (attempts / math.max(1, total)) * math.pi * 2
        local jitter = ZombRand(9) - 4
        local x = math.floor(target:getX() + math.cos(angle) * (radius + jitter))
        local y = math.floor(target:getY() + math.sin(angle) * (radius + jitter))
        local square = getCell():getGridSquare(x, y, math.floor(target:getZ()))
        if square then
            local batch = math.min(5, total - spawned)
            local zombies = addZombiesInOutfit(x, y, math.floor(target:getZ()), batch, nil, 50)
            spawned = spawned + tagSpawnedZombies(zombies, data.raidId, target,
                sprinterPercent, "HORDE:" .. tostring(data.raidId))
        end
    end
    Util.log("Spawned " .. tostring(spawned) .. " raid zombies")
    Runtime.broadcastState()
    Runtime.announceLocalized("IGUI_ExtractionMode_Message_HordeArrived",
        "The horde has arrived. Move now.", {}, { participantsOnly = true })
    return spawned
end

function Threats.spawnExtractionHorde(target)
    if target == nil then return 0 end
    if Config.value("EasierExtractions") == true then
        Util.log("Easier Extractions suppressed the initial flare-response zombies")
        return 0
    end
    local data = Runtime.currentStore()
    local participantCount = 0
    for _, player in ipairs(activePlayers()) do
        if not player:isDead() and data.participants[Util.username(player)] == true then
            participantCount = participantCount + 1
        end
    end
    participantCount = math.max(1, participantCount)
    local perPlayer = math.max(0,
        math.floor(tonumber(Config.value("ExtractionHordePerPlayer")) or 5))
    local scalingCount = participantCount + ProjectRemnantsIntegration.scalingContribution()
    local total = math.floor((perPlayer * scalingCount) + 0.5)
    if total == 0 then return 0 end
    local radius = math.max(15, math.floor(tonumber(Config.value("ExtractionHordeSpawnRadius")) or 35))
    local stage, ambientPercent = ambientSprinterStage()
    local spawned = 0
    local attempts = 0
    while spawned < total and attempts < total * 3 do
        attempts = attempts + 1
        local angle = (ZombRand(10000) / 10000) * math.pi * 2
        local jitter = ZombRand(9) - 4
        local x = math.floor(target:getX() + math.cos(angle) * (radius + jitter))
        local y = math.floor(target:getY() + math.sin(angle) * (radius + jitter))
        local z = math.floor(target:getZ())
        local square = getCell():getGridSquare(x, y, z)
        if Util.isSafeOutdoorLandSquare(square) then
            local batch = math.min(4, total - spawned)
            local zombies = addZombiesInOutfit(x, y, z, batch, nil, 50)
            spawned = spawned + tagSpawnedZombies(zombies, data.raidId, target,
                ambientPercent, "AMBIENT:" .. stage)
        end
    end
    Util.log("Spawned " .. tostring(spawned) .. " extraction-response zombies")
    return spawned
end

function Threats.cleanupRaidZombies()
    local data = Runtime.currentStore()
    local zombieList = getCell() and getCell():getZombieList()
    if zombieList == nil then return 0 end
    local remove = {}
    for index = 0, zombieList:size() - 1 do
        local zombie = zombieList:get(index)
        if zombie and tonumber(zombie:getModData().ExtractionModeRaidId) == tonumber(data.raidId) then
            remove[#remove + 1] = zombie
        end
    end
    for _, zombie in ipairs(remove) do
        BanditsIntegration.detachZombieBrain(zombie)
        pcall(function()
            zombie:removeFromWorld()
            zombie:removeFromSquare()
        end)
    end
    Util.log("Cleaned up " .. tostring(#remove) .. " raid zombies; world loot was untouched")
    return #remove
end

local function helicopter()
    local world = getWorld and getWorld()
    if world == nil then return nil end
    local ok, value = pcall(function() return world.helicopter end)
    if ok then return value end
    return nil
end

function Threats.suppressVanillaHelicopter()
    if SandboxVars then SandboxVars.Helicopter = 1 end
    local options = getSandboxOptions and getSandboxOptions()
    if options then pcall(function() options:set("Helicopter", 1) end) end
    local chopper = helicopter()
    if not tableHasEntries(helicopterOwners) and chopper then
        local active = false
        pcall(function() active = chopper:isActive() end)
        if active then
            pcall(function() chopper:deactivate() end)
            Util.log("Suppressed a vanilla helicopter event")
        end
    end
end

function Threats.stopOwnedHelicopter()
    local data = Runtime.currentStore()
    helicopterOwners[tostring(data.raidId or 0)] = nil
    if tableHasEntries(helicopterOwners) then return end
    if endHelicopter then
        pcall(function() endHelicopter() end)
    else
        local chopper = helicopter()
        if chopper then pcall(function() chopper:deactivate() end) end
    end
end

function Threats.helicopterArrivalSeconds(data)
    local baseSeconds = math.max(5, tonumber(Config.value("HelicopterArrivalSeconds")) or 90)
    if not Upgrades.isInstalled(data and data.upgrades, "comm_array") then return baseSeconds end
    local upgradedSeconds = math.max(5,
        tonumber(Config.value("CommArrayHelicopterArrivalSeconds")) or 60)
    return math.min(baseSeconds, upgradedSeconds)
end

function Threats.startExtractionHelicopter(data, preferredTarget)
    if data == nil or data.extractionHelicopterStarted == true then return false end
    local target = preferredTarget
    if target == nil or target:isDead()
        or data.participants[Util.username(target)] ~= true then
        target = nil
        for _, player in ipairs(activePlayers()) do
            if not player:isDead() and data.participants[Util.username(player)] == true then
                target = player
                break
            end
        end
    end
    if target == nil then return false end
    Threats.suppressVanillaHelicopter()
    local chopper = helicopter()
    if chopper == nil or not chopper:isActive() then
        if type(testHelicopter) == "function" then
            testHelicopter()
            chopper = helicopter()
        end
    end
    if chopper then
        helicopterOwners[tostring(data.raidId or 0)] = true
        chopper:setTarget(target)
        data.extractionHelicopterStarted = true
        Util.log("Extraction helicopter began its final approach")
        return true
    end
    return false
end

ExtractionMode.RaidThreatAuthority = Threats
return Threats
