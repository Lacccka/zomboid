require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/Infection"
require "ExtractionMode/Upgrades"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Infection = ExtractionMode.Infection
local Upgrades = ExtractionMode.Upgrades
local Benefits = {}

local healthByPlayer = {}
local infectionByPlayer = {}
local fatigueByPlayer = {}
local lastFatigueSyncAt = {}
local heatSources = {}
local heatCell = nil
local heatSignature = nil

local function playerKey(player)
    local username = Util.username(player)
    if username ~= "" then return username end
    return tostring(player)
end

local function heatingEnabled(state)
    local generator = state and state.generator
    local upgrades = state and state.upgrades
    local running = generator and generator.running == true
    if state and state.generatorRunning ~= nil then running = state.generatorRunning == true end
    local fuel = generator and tonumber(generator.fuel) or tonumber(state and state.generatorFuel)
    return Upgrades.isInstalled(upgrades, "heating") and running and (fuel or 0) > 0.0001
end

function Benefits.refreshHeating(state)
    local cell = getCell and getCell()
    local enabled = cell ~= nil and heatingEnabled(state)
    local hideout = Config.hideout()
    local radius = math.max(5, math.floor(tonumber(Config.value("HideoutHeatingRadius")) or 30))
    local temperature = math.max(20, math.floor(tonumber(Config.value("HideoutHeatingTemperature")) or 35))
    local minimumZ = math.floor(hideout.z or 0)
    local maximumZ = minimumZ
    if cell then
        local anchor = cell:getGridSquare(math.floor(hideout.x), math.floor(hideout.y), minimumZ)
        pcall(function()
            local definition = anchor and anchor:getBuilding() and anchor:getBuilding():getDef()
            if definition then
                minimumZ = math.max(-32, tonumber(definition:getMinLevel()) or minimumZ)
                maximumZ = math.min(31, tonumber(definition:getMaxLevel()) or minimumZ)
            end
        end)
    end
    local signature = tostring(math.floor(hideout.x)) .. ":" .. tostring(math.floor(hideout.y))
        .. ":" .. tostring(minimumZ) .. ":" .. tostring(maximumZ)
        .. ":" .. tostring(radius) .. ":" .. tostring(temperature)

    if #heatSources > 0 and (not enabled or heatCell ~= cell or heatSignature ~= signature) then
        for _, source in ipairs(heatSources) do
            pcall(function() heatCell:removeHeatSource(source) end)
        end
        heatSources = {}
        heatCell = nil
        heatSignature = nil
    end
    if enabled and #heatSources == 0 then
        for z = minimumZ, maximumZ do
            local source = IsoHeatSource.new(math.floor(hideout.x), math.floor(hideout.y),
                z, radius, temperature)
            cell:addHeatSource(source)
            heatSources[#heatSources + 1] = source
        end
        heatCell = cell
        heatSignature = signature
    end
end

-- Server-authoritative medical effects. The healing bonus mirrors positive
-- natural health changes instead of replacing vanilla or other mods' healing.
function Benefits.processServerPlayer(player, data)
    if player == nil then return false end
    local key = playerKey(player)
    if player:isDead() then
        healthByPlayer[key] = nil
        infectionByPlayer[key] = nil
        return false
    end

    local bodyDamage = player:getBodyDamage()
    if bodyDamage == nil then return false end
    local medicalInstalled = Upgrades.isInstalled(data and data.upgrades, "medical_delivery")
    local health = tonumber(bodyDamage:getOverallBodyHealth()) or 0
    local previousHealth = healthByPlayer[key]
    if medicalInstalled and previousHealth ~= nil and health > previousHealth then
        local bonusPercent = math.max(0, tonumber(Config.value("MedicalHealingBonusPercent")) or 20)
        local bonus = (health - previousHealth) * bonusPercent / 100
        if bonus > 0 then
            bodyDamage:AddGeneralHealth(bonus)
            health = tonumber(bodyDamage:getOverallBodyHealth()) or health
        end
    end
    healthByPlayer[key] = health

    local infected = bodyDamage:isInfected() == true
    local previouslyInfected = infectionByPlayer[key]
    local prevented = false
    if medicalInstalled and previouslyInfected == false and infected then
        local prevention = math.max(0, math.min(100,
            tonumber(Config.value("MedicalInfectionPreventionPercent")) or 20))
        if ZombRand(10000) < math.floor(prevention * 100) then
            Infection.cure(player)
            infected = false
            prevented = true
        end
    end
    infectionByPlayer[key] = infected
    return prevented
end

-- Fatigue is owned by the local survivor in multiplayer, so ventilation's
-- sleep bonus is applied on that client and explicitly synchronized.
function Benefits.processLocalPlayer(player, state)
    if player == nil or not player:isLocalPlayer() then return false end
    Benefits.refreshHeating(state)
    local key = playerKey(player)
    local stats = player:getStats()
    if stats == nil then return false end
    local fatigue = tonumber(stats:get(CharacterStat.FATIGUE)) or 0
    local previous = fatigueByPlayer[key]
    local improved = false
    if previous ~= nil and fatigue < previous and player:isAsleep()
        and Infection.playerInsideHideout(player)
        and Upgrades.isInstalled(state and state.upgrades, "ventilation") then
        local bonusPercent = math.max(0, tonumber(Config.value("VentilationSleepBonusPercent")) or 20)
        local improvedFatigue = math.max(0, fatigue - (previous - fatigue) * bonusPercent / 100)
        if improvedFatigue < fatigue then
            stats:set(CharacterStat.FATIGUE, improvedFatigue)
            fatigue = improvedFatigue
            improved = true
        end
    end
    fatigueByPlayer[key] = fatigue

    if improved and isClient and isClient() then
        local now = Util.nowMs()
        if now - (lastFatigueSyncAt[key] or 0) >= 1000 then
            lastFatigueSyncAt[key] = now
            pcall(function() sendPlayerStat(player, CharacterStat.FATIGUE) end)
        end
    end
    return improved
end

ExtractionMode.HideoutBenefits = Benefits
return Benefits
