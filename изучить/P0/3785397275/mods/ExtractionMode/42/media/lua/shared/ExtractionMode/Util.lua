require "ExtractionMode/Config"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Util = {}
local timerClockMs = nil
local timerClockWallMs = nil
local MAX_OBSERVED_FRAME_MS = 1000

local function accountUsername(player)
    if player == nil then return "" end
    local integration = ExtractionMode and ExtractionMode.ProjectRemnantsIntegration
    if integration and integration.canonicalPlayer then
        player = integration.canonicalPlayer(player)
    end
    local ok, value = pcall(function() return player:getUsername() end)
    if ok and value and tostring(value) ~= "" then return tostring(value) end
    if not (isClient and isClient()) and not (isServer and isServer()) then return "singleplayer" end
    return ""
end

function Util.accountUsername(player)
    return accountUsername(player)
end

-- Raid lifecycle state must distinguish every local survivor. Keep player 0 on
-- the historical single-player key for save compatibility, while additional
-- split-screen characters receive a stable viewport suffix. Shared ownership
-- systems (factions, quests, upgrades, and garages) use accountUsername() or
-- garageUsername() and therefore continue to belong to the same couch group.
function Util.username(player)
    local username = accountUsername(player)
    if username == "" or player == nil then return username end
    if not (isClient and isClient()) and not (isServer and isServer())
        and getNumActivePlayers ~= nil then
        local count = 0
        local playerNum = 0
        pcall(function() count = tonumber(getNumActivePlayers()) or 0 end)
        pcall(function() playerNum = math.max(0, tonumber(player:getPlayerNum()) or 0) end)
        if count > 1 and playerNum > 0 then
            return username .. "#split" .. tostring(playerNum)
        end
    end
    return username
end

-- Local split-screen characters share player 0's personal garage. Multiplayer
-- clients and dedicated-server players retain their own account usernames.
function Util.garageUsername(player)
    if getSpecificPlayer ~= nil and getNumActivePlayers ~= nil then
        local count = 0
        pcall(function() count = tonumber(getNumActivePlayers()) or 0 end)
        local localCharacter = not (isClient and isClient()) and not (isServer and isServer())
        if not localCharacter and player ~= nil then
            pcall(function() localCharacter = player:isLocalPlayer() == true end)
        end
        if count > 1 and localCharacter then
            local primary = getSpecificPlayer(0)
            local primaryName = Util.accountUsername(primary)
            if primaryName ~= "" then return primaryName end
        end
    end
    return Util.accountUsername(player)
end

function Util.players()
    local result = {}
    local seen = {}
    local function addPlayer(player)
        if player ~= nil and seen[player] ~= true then
            seen[player] = true
            result[#result + 1] = player
        end
    end

    if getOnlinePlayers then
        local players = getOnlinePlayers()
        if players then
            for index = 0, players:size() - 1 do
                addPlayer(players:get(index))
            end
        end
    end

    -- Local split-screen survivors are separate IsoPlayers but are not reliably
    -- included in getOnlinePlayers(). Enumerate all active viewport slots so the
    -- authority can ready, teleport, protect, and extract each local character.
    if getSpecificPlayer then
        local count = 4
        if getNumActivePlayers then
            local ok, activeCount = pcall(getNumActivePlayers)
            if ok then count = math.max(0, math.min(4, tonumber(activeCount) or 0)) end
        end
        for playerNum = 0, count - 1 do addPlayer(getSpecificPlayer(playerNum)) end
    end
    if #result == 0 and getPlayer then addPlayer(getPlayer()) end
    return result
end

function Util.distanceSquaredXY(a, b)
    local dx = tonumber(a.x) - tonumber(b.x)
    local dy = tonumber(a.y) - tonumber(b.y)
    return dx * dx + dy * dy
end

function Util.playerNear(player, point, radius)
    if player == nil or point == nil then return false end
    if math.floor(player:getZ()) ~= math.floor(tonumber(point.z) or 0) then return false end
    return Util.distanceSquaredXY({ x = player:getX(), y = player:getY() }, point) <= radius * radius
end

function Util.isSafeOutdoorLandSquare(square)
    if square == nil or not square:isOutside() then return false end
    if square:has(IsoFlagType.water) then return false end
    if square:getFloor() == nil or not square:TreatAsSolidFloor() then return false end
    return not square:isSolid() and not square:isSolidTrans()
end

function Util.isOpenOutdoorLandSquare(square, clearanceRadius)
    if not Util.isSafeOutdoorLandSquare(square) then return false end
    local cell = getCell and getCell()
    if cell == nil then return false end
    local radius = math.max(0, math.floor(tonumber(clearanceRadius) or 0))
    local centerX = square:getX()
    local centerY = square:getY()
    local z = square:getZ()
    local radiusSquared = radius * radius

    for dx = -radius, radius do
        for dy = -radius, radius do
            if dx * dx + dy * dy <= radiusSquared then
                local nearby = cell:getGridSquare(centerX + dx, centerY + dy, z)
                -- An unloaded neighbor cannot be certified as a clear landing area.
                if nearby == nil or nearby:has(IsoFlagType.water) or nearby:HasTree()
                    or not nearby:isOutside() or nearby:isSolid() or nearby:isSolidTrans() then
                    return false
                end
            end
        end
    end
    return true
end

function Util.safeOutdoorLandSquareNear(point, maximumRadius)
    local cell = getCell and getCell()
    if cell == nil or point == nil then return nil end
    local centerX = math.floor(tonumber(point.x) or 0)
    local centerY = math.floor(tonumber(point.y) or 0)
    local z = math.floor(tonumber(point.z) or 0)
    if cell:getGridSquare(centerX, centerY, z) == nil then return nil end

    maximumRadius = math.max(0, math.floor(tonumber(maximumRadius) or 0))
    for radius = 0, maximumRadius do
        for x = centerX - radius, centerX + radius do
            for y = centerY - radius, centerY + radius do
                if radius == 0 or x == centerX - radius or x == centerX + radius
                    or y == centerY - radius or y == centerY + radius then
                    local square = cell:getGridSquare(x, y, z)
                    if Util.isSafeOutdoorLandSquare(square) then return square end
                end
            end
        end
    end
    return nil
end

function Util.openOutdoorLandSquareNear(point, maximumRadius, clearanceRadius)
    local cell = getCell and getCell()
    if cell == nil or point == nil then return nil end
    local centerX = math.floor(tonumber(point.x) or 0)
    local centerY = math.floor(tonumber(point.y) or 0)
    local z = math.floor(tonumber(point.z) or 0)
    if cell:getGridSquare(centerX, centerY, z) == nil then return nil end

    maximumRadius = math.max(0, math.floor(tonumber(maximumRadius) or 0))
    for radius = 0, maximumRadius do
        for x = centerX - radius, centerX + radius do
            for y = centerY - radius, centerY + radius do
                if radius == 0 or x == centerX - radius or x == centerX + radius
                    or y == centerY - radius or y == centerY + radius then
                    local square = cell:getGridSquare(x, y, z)
                    if Util.isOpenOutdoorLandSquare(square, clearanceRadius) then return square end
                end
            end
        end
    end
    return nil
end

function Util.worldHours()
    return getGameTime and getGameTime():getWorldAgeHours() or 0
end

function Util.timeOfDay()
    return getGameTime and getGameTime():getTimeOfDay() or 0
end

function Util.nowMs()
    return getTimestampMs and getTimestampMs() or 0
end

-- Raid and other simulation deadlines use wall time in multiplayer, where the
-- server always runs at normal speed. In single-player, advance a local clock
-- by Zomboid's actual simulation multiplier (1x/5x/20x/40x), and only while
-- gameplay is unpaused. OnTick normally keeps this sampled every frame;
-- discarding a large unobserved gap also covers pause screens or suspended
-- windows that stop delivering ticks entirely.
function Util.timerNowMs()
    local wallNow = Util.nowMs()
    if (isClient and isClient()) or (isServer and isServer()) then return wallNow end

    if timerClockMs == nil or timerClockWallMs == nil then
        timerClockMs = wallNow
        timerClockWallMs = wallNow
        return timerClockMs
    end

    local elapsed = math.max(0, wallNow - timerClockWallMs)
    timerClockWallMs = wallNow
    local paused = (isGamePaused and isGamePaused())
        or (getGameSpeed and getGameSpeed() == 0)
    if not paused and elapsed <= MAX_OBSERVED_FRAME_MS then
        local speedMultiplier = 1
        if getGameTime then
            local ok, value = pcall(function()
                return getGameTime():getTrueMultiplier()
            end)
            value = ok and tonumber(value) or nil
            if value and value >= 0 then speedMultiplier = value end
        end
        timerClockMs = timerClockMs + elapsed * speedMultiplier
    end
    return timerClockMs
end

function Util.log(message)
    if Config.value("DebugLogging") == true then
        print("[ExtractionMode] " .. tostring(message))
    end
end

ExtractionMode.Util = Util
return Util
