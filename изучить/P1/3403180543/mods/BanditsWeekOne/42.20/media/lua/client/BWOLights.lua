BWOLights = BWOLights or {}

BWOLights.cache = {}
BWOLights.tick = 0
BWOLights.flicker = {}
BWOLights.rotator = {}

local MAX_TICK  = 64          -- full cycle
local STEP_DIV  = 2            -- how many ticks per step
local MAX_STEP  = MAX_TICK / STEP_DIV  -- 32 steps

BWOLights.AddFlicker = function(tab)
    table.insert(BWOLights.flicker, tab)
end

local function onTick()
    if isServer() then return end

    local player = getSpecificPlayer(0)
    if not player then return end

    local px, py, pz = player:getX(), player:getY(), player:getZ()
    
    BWOLights.tick = (BWOLights.tick + 1) % MAX_TICK

    for _, effect in ipairs(BWOLights.flicker) do
        local id = effect.x .. "-" .. effect.y .. "-" .. effect.z

        local lightSwitch = BWOLights.cache[id]

        if not lightSwitch then
            lightSwitch = BanditUtils.GetLightSwitch(effect.x, effect.y, effect.z)
            if lightSwitch then
                BWOLights.cache[id] = lightSwitch
            end
        end
        
        if lightSwitch then
            local active = false
            for i, bit in pairs(effect.prg) do
                if i == (BWOLights.tick % MAX_STEP) then
                    active = bit
                    break
                end
            end
            lightSwitch:setActive(active, false, true)
        end
    end

end

Events.OnTick.Remove(onTick)
Events.OnTick.Add(onTick)