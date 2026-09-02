-- Client half of the climate pins: the admin layer is computed per
-- machine, so every client applies the pinned values itself. The state
-- arrives as a broadcast on change and on request after boot.
require "Aegis/AegisTheme"

AegisClimateClient = AegisClimateClient or {}
AegisClimateClient.state = { tempOn = false, temp = 30, snowOn = false }

local function apply(s)
    local cm = getClimateManager()
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

Events.OnServerCommand.Add(function(module, command, args)
    if module ~= AegisShared.MODULE or command ~= "climateSync" then return end
    if type(args) ~= "table" then return end
    local s = AegisClimateClient.state
    s.tempOn = args.tempOn == true
    s.temp = math.floor(tonumber(args.temp) or s.temp)
    s.snowOn = args.snowOn == true
    -- in solo the server file already pinned the shared manager, applying
    -- again is the same write with the same values
    apply(s)
end)

Events.OnGameStart.Add(function()
    if not isClient() then return end
    local p = getPlayer()
    if p then
        sendClientCommand(p, AegisShared.MODULE, "climateReq", {})
    end
end)
