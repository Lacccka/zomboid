require "ExtractionMode/Config"
require "ExtractionMode/ModCompatibility"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Compatibility = ExtractionMode.ModCompatibility
local Utilities = {}
local isolationZone = nil

-- Build 42 checks NoPowerOrWater before both the municipal-water path and an
-- unmoved fixture's reserve-water path. Keep the isolation zone only while the
-- generator is off; removing it while powered lets the native mapped-plumbing
-- logic consume the reserve maintained below.
local function findIsolationZone(metaGrid, bounds)
    -- The authority and local client share the Java meta grid in singleplayer
    -- but keep separate Lua caches. Always resolve the live zone so a removal
    -- by either environment cannot leave the other holding a stale reference.
    isolationZone = nil
    local zones = metaGrid:getZonesAt(bounds.minX, bounds.minY, 0)
    if zones ~= nil then
        for index = 0, zones:size() - 1 do
            local zone = zones:get(index)
            if zone and zone:getName() == "ExtractionModeHideoutGridIsolation" then
                isolationZone = zone
                return zone
            end
        end
    end
    return nil
end

function Utilities.ensureGridIsolation(powered)
    local world = getWorld and getWorld()
    if world == nil then return false end
    local bounds = Config.hideoutCellBounds()
    local width = bounds.maxXExclusive - bounds.minX
    local height = bounds.maxYExclusive - bounds.minY
    local ok = pcall(function()
        local metaGrid = world:getMetaGrid()
        if metaGrid == nil then error("hideout meta grid is unavailable") end
        local zone = findIsolationZone(metaGrid, bounds)
        if powered == true then
            if zone ~= nil then metaGrid:removeZone(zone) end
            isolationZone = nil
        elseif zone == nil then
            zone = world:registerZone("ExtractionModeHideoutGridIsolation", "NoPowerOrWater",
                bounds.minX, bounds.minY, 0, width, height)
            isolationZone = zone
        elseif zone:getType() ~= "NoPowerOrWater" then
            zone:setType("NoPowerOrWater")
        end
    end)
    return ok
end

-- IsoGridSquare:setHaveElectricity() only refreshes attached objects in Build
-- 42; actual non-grid power is determined from generator positions registered
-- on the chunk. Registering an invisible position provides generator semantics
-- without placing a real, destructible IsoGenerator or invoking its fuel logic.
-- One position per loaded chunk/level covers the complete mapped building.
local function setVirtualPowerChunk(chunk, chunkX, chunkY, z, powered, visited)
    if chunk == nil then return end
    local key = tostring(chunkX) .. ":" .. tostring(chunkY) .. ":" .. tostring(z)
    if visited and visited[key] then return end
    if visited then visited[key] = true end

    local virtualX = chunkX * 10 + 5
    local virtualY = chunkY * 10 + 5
    if powered then
        chunk:addGeneratorPos(virtualX, virtualY, z)
    else
        chunk:removeGeneratorPos(virtualX, virtualY, z)
    end
end

local function loadedChunk(cell, chunkX, chunkY, probeZ)
    local chunk = nil
    pcall(function() chunk = cell:getChunk(chunkX, chunkY) end)
    if chunk == nil then
        -- Dedicated servers do not use a local player's IsoChunkMap, so resolve
        -- the same world chunk through ServerMap when that authority is active.
        pcall(function()
            if ServerMap and ServerMap.instance then
                chunk = ServerMap.instance:getChunk(chunkX, chunkY)
            end
        end)
    end
    if chunk == nil then
        -- Compatibility fallback for runtimes that do not expose ServerMap.
        local square = cell:getGridSquare(chunkX * 10 + 5, chunkY * 10 + 5, probeZ)
        if square then chunk = square:getChunk() end
    end
    return chunk
end

function Utilities.setVirtualPower(square, powered, visited)
    if square == nil then return end
    local chunk = square:getChunk()
    if chunk == nil then return end
    local chunkX = math.floor(square:getX() / 10)
    local chunkY = math.floor(square:getY() / 10)
    local z = math.floor(square:getZ())
    setVirtualPowerChunk(chunk, chunkX, chunkY, z, powered, visited)
end

-- Electrical coverage is deliberately wider than the gameplay hideout radius.
-- Some authored hideout sections can belong to a different IsoBuilding than
-- the spawn-room anchor, so discovering chunks only through that building can
-- leave annexes unpowered. Register the pseudo-generator in every loaded chunk
-- within the dedicated coverage area; object, plumbing, and fixed-light updates
-- remain restricted to the actual hideout building elsewhere.
function Utilities.setVirtualPowerArea(cell, hideout, powered, visited, minimumZ, maximumZ)
    if cell == nil or hideout == nil then return end
    local cellBounds = Config.hideoutCellBounds()
    local radius = math.max(1, math.floor(tonumber(Config.HIDEOUT_POWER_RADIUS)
        or tonumber(Config.MAP_CELL_SIZE) or 256))
    local minimumX = math.max(cellBounds.minX, math.floor(hideout.x) - radius)
    local maximumX = math.min(cellBounds.maxXExclusive - 1, math.floor(hideout.x) + radius)
    local minimumY = math.max(cellBounds.minY, math.floor(hideout.y) - radius)
    local maximumY = math.min(cellBounds.maxYExclusive - 1, math.floor(hideout.y) + radius)
    local lowZ = math.max(-32, math.floor(tonumber(minimumZ) or tonumber(hideout.z) or 0))
    local highZ = math.min(31, math.floor(tonumber(maximumZ) or lowZ))
    if highZ < lowZ then highZ = lowZ end

    for chunkX = math.floor(minimumX / 10), math.floor(maximumX / 10) do
        for chunkY = math.floor(minimumY / 10), math.floor(maximumY / 10) do
            local chunk = loadedChunk(cell, chunkX, chunkY, lowZ)
            local chunkLowZ = lowZ
            local chunkHighZ = highZ
            if chunk then
                pcall(function()
                    chunkLowZ = math.max(lowZ, tonumber(chunk:getMinLevel()) or lowZ)
                    chunkHighZ = math.min(highZ, tonumber(chunk:getMaxLevel()) or highZ)
                end)
            end
            for z = chunkLowZ, chunkHighZ do
                if chunk then
                    -- Do not depend on the chunk's center coordinate having an
                    -- IsoGridSquare. Empty center tiles previously caused an
                    -- otherwise loaded room/chunk to miss virtual power.
                    setVirtualPowerChunk(chunk, chunkX, chunkY, z, powered, visited)
                end
            end
        end
    end
end

function Utilities.squareInsideVirtualPowerArea(square)
    if square == nil then return false end
    local hideout = Config.hideout()
    local bounds = Config.hideoutCellBounds()
    local radius = math.max(1, math.floor(tonumber(Config.HIDEOUT_POWER_RADIUS)
        or tonumber(Config.MAP_CELL_SIZE) or 256))
    local x, y = square:getX(), square:getY()
    return x >= math.max(bounds.minX, math.floor(hideout.x) - radius)
        and x <= math.min(bounds.maxXExclusive - 1, math.floor(hideout.x) + radius)
        and y >= math.max(bounds.minY, math.floor(hideout.y) - radius)
        and y <= math.min(bounds.maxYExclusive - 1, math.floor(hideout.y) + radius)
end

-- Keep the hideout's mapped plumbing tied to the pseudo-generator. Build 42's
-- unmoved waterPiped fixtures use waterAmount/waterMaxAmount reserve mod data;
-- they intentionally have no FluidContainer. This runs locally and on the
-- authority so context menus and timed actions see the same reserve state.
function Utilities.setPipedWaterAvailable(square, available, transmitChanges)
    if square == nil then return end
    local objects = square:getObjects()
    if objects == nil then return end

    for index = 0, objects:size() - 1 do
        local object = objects:get(index)
        local piped = false
        pcall(function()
            local properties = object and object:getProperties()
            piped = properties ~= nil and properties:has(IsoFlagType.waterPiped)
        end)
        if piped then
            if Compatibility.extendedPlumbingOwnsFixture(object) then
                -- LG Extended Plumbing routes this fixture through its invisible
                -- external collector. Remove only the reserve fields known to
                -- have been written by Extraction Mode, then leave the fixture
                -- completely under LGEP authority.
                local changed = false
                pcall(function()
                    local modData = object:getModData()
                    if modData and modData.ExtractionModeWaterPowered ~= nil then
                        modData.ExtractionModeWaterPowered = nil
                        modData.waterAmount = nil
                        modData.waterMaxAmount = nil
                        changed = true
                    end
                end)
                if changed and transmitChanges ~= false then
                    pcall(function() object:transmitModData() end)
                end
            else
                -- Keep mapped fixtures on their native reserve path instead of
                -- searching for rain collectors or other external sources.
                pcall(function() object:setUsesExternalWaterSource(false) end)

                local changed = false
                pcall(function()
                    local modData = object:getModData()
                    if modData then
                        local poweredBefore = modData.ExtractionModeWaterPowered == true
                        local maximum = math.max(10000, tonumber(modData.waterMaxAmount) or 0)
                        if modData.waterMaxAmount ~= maximum then
                            modData.waterMaxAmount = maximum
                            changed = true
                        end
                        if poweredBefore ~= (available == true) then
                            modData.ExtractionModeWaterPowered = available == true
                            modData.waterAmount = available and maximum or 0
                            changed = true
                        elseif not available and tonumber(modData.waterAmount) ~= 0 then
                            modData.waterAmount = 0
                            changed = true
                        end
                    end
                end)

                if changed and transmitChanges ~= false then
                    pcall(function() object:transmitModData() end)
                end
            end
        end
    end
end

ExtractionMode.HideoutUtilities = Utilities
return Utilities
