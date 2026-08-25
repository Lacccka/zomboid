ExtractionMode = ExtractionMode or {}
if isClient and isClient() then
    ExtractionMode.RaidFlareAuthority = ExtractionMode.RaidFlareAuthority or {}
    return ExtractionMode.RaidFlareAuthority
end

require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/Infection"

local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Infection = ExtractionMode.Infection
local Runtime = ExtractionMode.RaidRuntime
local Flares = {}
local waterRescueAt = {}

local function activePlayers()
    local result = {}
    for _, player in ipairs(Util.players()) do
        if player and not player:isDead() and Util.username(player) ~= "" then
            result[#result + 1] = player
        end
    end
    return result
end

local function activeExtractionSites(data)
    if data.extractionSites and #data.extractionSites > 0 then return data.extractionSites end
    return Config.extractionSites()
end

function Flares.giveFlare(player)
    local inventory = player and player:getInventory()
    if inventory == nil then return end
    local existing = inventory:getItemFromType(Config.FLARE_FULL_TYPE, true, true)
    if existing then
        Config.applyExtractionFlareNoise(existing)
        local usable = pcall(function() existing:setCurrentAmmoCount(1) end)
        if usable then
            existing:getModData().ExtractionModeRaidId = Runtime.currentStore().raidId
            if syncHandWeaponFields then pcall(function() syncHandWeaponFields(player, existing) end) end
            return
        end
        inventory:Remove(existing)
        if sendRemoveItemFromContainer then sendRemoveItemFromContainer(inventory, existing) end
    end
    local item = inventory:AddItem(Config.FLARE_FULL_TYPE)
    if item then
        Config.applyExtractionFlareNoise(item)
        item:getModData().ExtractionModeRaidId = Runtime.currentStore().raidId
        pcall(function() item:setCurrentAmmoCount(1) end)
    end
    if item and sendAddItemToContainer then sendAddItemToContainer(inventory, item) end
end

local function purgeFlareGuns(player)
    local inventory = player and player:getInventory()
    if inventory == nil then return 0 end
    local found = inventory:getAllTypeRecurse(Config.FLARE_FULL_TYPE)
    if found == nil or found:size() == 0 then return 0 end
    local flares = {}
    for index = 0, found:size() - 1 do flares[#flares + 1] = found:get(index) end
    local removed = 0
    for _, item in ipairs(flares) do
        if item then
            pcall(function() player:removeFromHands(item) end)
            local container = item:getContainer() or inventory
            if sendRemoveItemFromContainer then
                pcall(function() sendRemoveItemFromContainer(container, item) end)
            end
            local ok = pcall(function() container:Remove(item) end)
            if ok then removed = removed + 1 end
        end
    end
    return removed
end

function Flares.purgeHideoutPlayerFlares(data)
    for _, player in ipairs(Util.players()) do
        local username = Util.username(player)
        local outboundParticipant = data.state == Config.STATE_TRANSIT and data.participants[username] == true
        if Runtime.dataForPlayer(player) == data and not outboundParticipant
            and Infection.playerInsideHideout(player) then
            local removed = purgeFlareGuns(player)
            if removed > 0 then
                Util.log("Removed " .. tostring(removed) .. " extraction flare gun(s) from "
                    .. username .. " inside the hideout")
            end
        end
    end
end

local function equippedFlare(player)
    local item = player and player:getPrimaryHandItem()
    if item and item:getFullType() == Config.FLARE_FULL_TYPE then return item end
    return nil
end

function Flares.equippedFlare(player)
    return equippedFlare(player)
end

function Flares.reloadFlare(player)
    local item = equippedFlare(player)
    if item == nil then return end
    Config.applyExtractionFlareNoise(item)
    pcall(function() item:setCurrentAmmoCount(1) end)
    if syncHandWeaponFields then pcall(function() syncHandWeaponFields(player, item) end) end
end

function Flares.nearestExtractionSite(player)
    local nearest = nil
    local nearestDistance = nil
    for _, site in ipairs(activeExtractionSites(Runtime.currentStore())) do
        local distance = math.sqrt(Util.distanceSquaredXY(
            { x = player:getX(), y = player:getY() }, site))
        if nearestDistance == nil or distance < nearestDistance then
            nearest = site
            nearestDistance = distance
        end
    end
    return nearest, nearestDistance
end

function Flares.consumeFlare(player)
    local item = equippedFlare(player)
    if item == nil then return false end
    pcall(function() item:setCurrentAmmoCount(0) end)
    item:getModData().ExtractionModeSpentFlareRaidId = Runtime.currentStore().raidId
    if syncHandWeaponFields then pcall(function() syncHandWeaponFields(player, item) end) end
    return true
end

local function outdoorSquareNear(point)
    if point == nil then return nil end
    local maximumRadius = math.min(6, math.max(0, math.floor(tonumber(point.radius) or 0)))
    return Util.safeOutdoorLandSquareNear(point, maximumRadius)
end

local function resolveLoadedRoutePoint(point, maximumRadius)
    if point == nil or point.landValidated == true then return false end
    local clearance = math.max(0, tonumber(Config.value("RoutePointClearanceRadius")) or 6)
    local square = Util.openOutdoorLandSquareNear(point, maximumRadius, clearance)
    if square == nil then return false end
    local oldX = tonumber(point.x)
    local oldY = tonumber(point.y)
    point.x = square:getX()
    point.y = square:getY()
    point.z = square:getZ()
    point.landValidated = true
    return oldX ~= point.x or oldY ~= point.y
end

function Flares.resolveLoadedRaidRoute()
    local data = Runtime.currentStore()
    if data.state ~= Config.STATE_TRANSIT and data.state ~= Config.STATE_RAID
        and data.state ~= Config.STATE_EXTRACTING and data.state ~= Config.STATE_BOARDING then return false end
    local spawnChanged = resolveLoadedRoutePoint(data.raidSpawn, 48)
    local changed = spawnChanged
    for _, site in ipairs(data.extractionSites or {}) do
        if resolveLoadedRoutePoint(site, 48) then
            changed = true
            data.extractionFlareSpawned[tostring(site.id)] = nil
            Util.log("Moved extraction E" .. tostring(site.id) .. " onto reachable outdoor land")
        end
    end
    if spawnChanged and data.state == Config.STATE_TRANSIT and data.transitPhase == "ARRIVING" then
        local index = 0
        for _, player in ipairs(activePlayers()) do
            if data.participants[Util.username(player)] == true then
                index = index + 1
                Runtime.teleport(player, data.raidSpawn, index, true)
            end
        end
        Util.log("Moved arriving raid participants onto a clear insertion area")
    end
    if changed then Runtime.broadcastState() end
    return changed
end

function Flares.rescueRaidParticipantsFromWater()
    local data = Runtime.currentStore()
    if data.state ~= Config.STATE_TRANSIT and data.state ~= Config.STATE_RAID
        and data.state ~= Config.STATE_EXTRACTING and data.state ~= Config.STATE_BOARDING then return end
    local now = Util.timerNowMs()
    for _, player in ipairs(activePlayers()) do
        local username = Util.username(player)
        local square = player:getCurrentSquare()
        if data.participants[username] == true and square and square:has(IsoFlagType.water)
            and now - (tonumber(waterRescueAt[username]) or 0) >= 5000 then
            local safeSquare = Util.safeOutdoorLandSquareNear({
                x = player:getX(), y = player:getY(), z = player:getZ(),
            }, 64)
            if safeSquare then
                waterRescueAt[username] = now
                Runtime.teleport(player, {
                    x = safeSquare:getX(), y = safeSquare:getY(), z = safeSquare:getZ(),
                }, 1, true)
                Runtime.deliver(player, "Announcement", {
                    message = "Unsafe water landing corrected. You were moved to reachable ground.",
                    messageKey = "IGUI_ExtractionMode_Message_WaterLandingCorrected",
                })
            end
        end
    end
end

function Flares.ensureExtractionSiteFlares()
    local data = Runtime.currentStore()
    if data.state ~= Config.STATE_TRANSIT and data.state ~= Config.STATE_RAID
        and data.state ~= Config.STATE_EXTRACTING and data.state ~= Config.STATE_BOARDING then return end
    for _, site in ipairs(activeExtractionSites(data)) do
        local key = tostring(site.id)
        if tonumber(data.extractionFlareSpawned[key]) ~= tonumber(data.raidId) then
            local square = outdoorSquareNear(site)
            if square then
                local flare = square:AddWorldInventoryItem(Config.FLARE_FULL_TYPE, 0.5, 0.5, 0)
                if flare then
                    pcall(function() flare:setCurrentAmmoCount(1) end)
                    flare:getModData().ExtractionModeEmergencyFlare = true
                    flare:getModData().ExtractionModeRaidId = data.raidId
                    local fullySynced = false
                    pcall(function()
                        local worldItem = flare:getWorldItem()
                        if worldItem then
                            worldItem:setIgnoreRemoveSandbox(true)
                            worldItem:transmitCompleteItemToClients()
                            fullySynced = true
                        end
                    end)
                    if not fullySynced then pcall(function() flare:SynchSpawn() end) end
                    data.extractionFlareSpawned[key] = data.raidId
                    Util.log("Spawned emergency flare at extraction site E" .. key)
                end
            end
        end
    end
end

ExtractionMode.RaidFlareAuthority = Flares
return Flares
