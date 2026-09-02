-- Climate pins: the admin layer of the game's climate system, the same
-- one behind the vanilla climate debug panel. Temperature and snow can be
-- pinned server wide, the state survives a restart, and every client gets
-- it pushed because the layer is applied per machine, not sent with the
-- normal weather sync.
if isClient() then return end

require "Aegis_Roles"
require "Aegis_Log"

AegisClimate = AegisClimate or {}

local FILE = AegisStore.ROOT .. "/Status/climate.txt"
local TEMP_MIN, TEMP_MAX = -50, 50

-- tempOn/snowOn are the enable switches, temp is degrees
local state = { tempOn = false, temp = 30, snowOn = false }
local loaded = false

local function load()
    if loaded then return end
    loaded = true
    local lines = AegisStore.readLines(FILE, 8)
    for _, line in ipairs(lines or {}) do
        local k, v = line:match("^(%w+)=(.*)$")
        if k == "tempOn" then state.tempOn = v == "1" end
        if k == "temp" then state.temp = math.max(TEMP_MIN, math.min(TEMP_MAX, tonumber(v) or 30)) end
        if k == "snowOn" then state.snowOn = v == "1" end
    end
end

local function save()
    AegisStore.write(FILE, "tempOn=" .. (state.tempOn and "1" or "0")
        .. "\ntemp=" .. tostring(state.temp)
        .. "\nsnowOn=" .. (state.snowOn and "1" or "0") .. "\n")
end

-- the same application runs on the server here and on every client in
-- AegisClimateClient, so both machines derive the same weather
function AegisClimate.applyTo(cm, s)
    local t = cm:getClimateFloat(ClimateManager.FLOAT_TEMPERATURE)
    if s.tempOn then
        t:setAdminValue(s.temp)
        t:setEnableAdmin(true)
    else
        t:setEnableAdmin(false)
    end
    -- the bool only turns precipitation INTO snow, it does not create
    -- any; without rain nothing falls, so the switch pins the intensity
    -- along with it
    local snow = cm:getClimateBool(ClimateManager.BOOL_IS_SNOW)
    local rain = cm:getClimateFloat(ClimateManager.FLOAT_PRECIPITATION_INTENSITY)
    if s.snowOn then
        snow:setAdminValue(true)
        snow:setEnableAdmin(true)
        rain:setAdminValue(0.65)
        rain:setEnableAdmin(true)
    else
        snow:setEnableAdmin(false)
        rain:setEnableAdmin(false)
    end
end

local function applyHere()
    AegisClimate.applyTo(getClimateManager(), state)
end

local function push(target)
    local payload = { tempOn = state.tempOn, temp = state.temp, snowOn = state.snowOn }
    if isServer() then
        if target then
            sendServerCommand(target, AegisShared.MODULE, "climateSync", payload)
        else
            sendServerCommand(AegisShared.MODULE, "climateSync", payload)
        end
    else
        triggerEvent("OnServerCommand", AegisShared.MODULE, "climateSync", payload)
    end
end

local Commands = {}

-- the whole pin state travels in one piece, partial writes would leave
-- server and file disagreeing after a lost packet
Commands.climateSet = function(player, args)
    if not AegisRoles.canArea(player, "world") then return end
    if type(args) ~= "table" then return end
    load()
    state.tempOn = args.tempOn == true
    state.temp = math.max(TEMP_MIN, math.min(TEMP_MAX, math.floor(tonumber(args.temp) or state.temp)))
    state.snowOn = args.snowOn == true
    save()
    applyHere()
    push(nil)
    local parts = {}
    if state.tempOn then table.insert(parts, "temperature " .. state.temp) end
    if state.snowOn then table.insert(parts, "snow on") end
    AegisLog.write("Actions", player:getUsername() or "?", "world",
        "Climate pinned: " .. (#parts > 0 and table.concat(parts, ", ") or "all off"))
end

Commands.climateReset = function(player, args)
    if not AegisRoles.canArea(player, "world") then return end
    load()
    state.tempOn = false
    state.snowOn = false
    save()
    -- resetAdmin also clears pins other tools may have set; the two
    -- switches above are the panel's own share of it
    getClimateManager():resetAdmin()
    applyHere()
    push(nil)
    AegisLog.write("Actions", player:getUsername() or "?", "world", "Climate returned to nature")
end

-- clients ask on boot, so late joiners get the pins without a connect hook
Commands.climateReq = function(player, args)
    load()
    if state.tempOn or state.snowOn then push(player) end
end

Events.OnClientCommand.Add(function(module, command, player, args)
    if module ~= AegisShared.MODULE then return end
    if AegisModeration and AegisModeration.isSuspended and AegisModeration.isSuspended(player) then return end
    if Commands[command] then Commands[command](player, args) end
end)

-- boot: the file is the memory, one early tick puts it back into force
local booted = false
Events.OnTick.Add(function()
    if booted then return end
    booted = true
    load()
    if state.tempOn or state.snowOn then applyHere() end
end)
