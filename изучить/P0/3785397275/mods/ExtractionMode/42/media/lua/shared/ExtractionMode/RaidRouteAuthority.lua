ExtractionMode = ExtractionMode or {}
if isClient and isClient() then
    ExtractionMode.RaidRouteAuthority = ExtractionMode.RaidRouteAuthority or {}
    return ExtractionMode.RaidRouteAuthority
end

require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/Upgrades"
require "ExtractionMode/BanditsIntegration"

local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Upgrades = ExtractionMode.Upgrades
local BanditsIntegration = ExtractionMode.BanditsIntegration
local Runtime = ExtractionMode.RaidRuntime
local Routes = {}

function Routes.rollHordeDelayHours(minimumMinutes, maximumMinutes)
    local minutes = minimumMinutes
    if maximumMinutes > minimumMinutes then
        minutes = minimumMinutes + ZombRand(maximumMinutes - minimumMinutes + 1)
    end
    return minutes / 60
end

local function shuffledPointIndices(points)
    local indices = {}
    for index = 1, #points do indices[index] = index end
    for index = #indices, 2, -1 do
        local other = ZombRand(index) + 1
        indices[index], indices[other] = indices[other], indices[index]
    end
    return indices
end

local function farEnough(point, selected, minimumDistance)
    local minimumSquared = minimumDistance * minimumDistance
    for _, other in ipairs(selected) do
        if Util.distanceSquaredXY(point, other) < minimumSquared then return false end
    end
    return true
end

local function withinRouteDistance(point, insertion, maximumDistance)
    if maximumDistance == nil or maximumDistance <= 0 then return true end
    return Util.distanceSquaredXY(point, insertion) <= maximumDistance * maximumDistance
end

function Routes.chooseRaidRoute(town, vehicleInsertion)
    local points = town and town.points or {}
    local vehiclePoints = town and town.vehicleInsertionPoints or {}
    local useDedicatedVehiclePoint = vehicleInsertion == true and #vehiclePoints > 0
    local minimumExtractions = math.max(1,
        math.floor(tonumber(town and town.minimumExtractionSites) or 2))
    local maximumExtractions = math.max(minimumExtractions,
        math.floor(tonumber(town and town.maximumExtractionSites) or 3))
    local requiredPoints = minimumExtractions + (useDedicatedVehiclePoint and 0 or 1)
    if #points < requiredPoints then return nil, nil end
    local order = shuffledPointIndices(points)
    local insertion = nil
    local firstExtractionOffset = 2
    if useDedicatedVehiclePoint then
        insertion = vehiclePoints[ZombRand(#vehiclePoints) + 1]
        firstExtractionOffset = 1
    else
        insertion = points[order[1]]
    end
    local extractionCount = minimumExtractions
    if maximumExtractions > minimumExtractions then
        extractionCount = minimumExtractions + ZombRand(maximumExtractions - minimumExtractions + 1)
    end
    extractionCount = math.min(extractionCount,
        useDedicatedVehiclePoint and #points or (#points - 1))
    local minimumDistance = math.max(50, tonumber(Config.value("RoutePointMinimumDistance")) or 120)
    local maximumDistance = tonumber(town.maximumRouteDistance)
    local chosen = { insertion }
    local extractions = {}
    for offset = firstExtractionOffset, #order do
        local point = points[order[offset]]
        if withinRouteDistance(point, insertion, maximumDistance)
            and farEnough(point, chosen, minimumDistance) then
            extractions[#extractions + 1] = {
                id = #extractions + 1,
                x = point.x, y = point.y, z = point.z or 0,
                radius = tonumber(Config.value("ExtractionRadius")) or 12,
            }
            chosen[#chosen + 1] = point
            if #extractions >= extractionCount then break end
        end
    end
    if #extractions < minimumExtractions then
        for offset = firstExtractionOffset, #order do
            local point = points[order[offset]]
            local alreadyChosen = point.x == insertion.x and point.y == insertion.y
            for _, site in ipairs(extractions) do
                if site.x == point.x and site.y == point.y then alreadyChosen = true; break end
            end
            if not alreadyChosen and withinRouteDistance(point, insertion, maximumDistance) then
                extractions[#extractions + 1] = {
                    id = #extractions + 1,
                    x = point.x, y = point.y, z = point.z or 0,
                    radius = tonumber(Config.value("ExtractionRadius")) or 12,
                }
                if #extractions >= minimumExtractions then break end
            end
        end
    end
    if #extractions < minimumExtractions then
        for offset = firstExtractionOffset, #order do
            local point = points[order[offset]]
            local alreadyChosen = point.x == insertion.x and point.y == insertion.y
            for _, site in ipairs(extractions) do
                if site.x == point.x and site.y == point.y then alreadyChosen = true; break end
            end
            if not alreadyChosen then
                extractions[#extractions + 1] = {
                    id = #extractions + 1,
                    x = point.x, y = point.y, z = point.z or 0,
                    radius = tonumber(Config.value("ExtractionRadius")) or 12,
                }
                if #extractions >= minimumExtractions then break end
            end
        end
    end
    return {
        x = insertion.x,
        y = insertion.y,
        z = insertion.z or 0,
        angleY = tonumber(insertion.angleY),
        dedicatedVehicleInsertion = useDedicatedVehiclePoint,
    }, extractions
end

function Routes.clearInsertionZombies(point)
    local zombieList = getCell() and getCell():getZombieList()
    if zombieList == nil or point == nil then return 0 end
    local radius = math.max(10, tonumber(Config.value("InsertionClearRadius")) or 35)
    if Upgrades.isInstalled(Runtime.currentStore().upgrades, "intel_center") then
        radius = radius + math.max(0, tonumber(Config.value("IntelInsertionClearBonus")) or 20)
    end
    local radiusSquared = radius * radius
    local remove = {}
    for index = 0, zombieList:size() - 1 do
        local zombie = zombieList:get(index)
        if zombie and Util.distanceSquaredXY({ x = zombie:getX(), y = zombie:getY() }, point) <= radiusSquared then
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
    if #remove > 0 then
        Util.log("Cleared " .. tostring(#remove) .. " zombie(s) near raid insertion")
    end
    return #remove
end

ExtractionMode.RaidRouteAuthority = Routes
return Routes
