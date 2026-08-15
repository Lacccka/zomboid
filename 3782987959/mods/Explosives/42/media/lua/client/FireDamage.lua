local burnTickCounter = {}
local burnStartHealth = {}
local burnStartTime = {}
local isDebug = getDebug()

Events.OnZombieUpdate.Add(function(zombie)
    if not SandboxVars.Explosives.FireDamageEnabled then return end

    if zombie:isOnFire() then
        local id = tostring(zombie)

        if not burnStartHealth[id] then
            burnStartHealth[id] = zombie:getHealth()
            burnStartTime[id] = os.time()
            if isDebug and burnStartHealth[id] > 0 then
                print("[FireDamage] Zombie started burning, health: " .. string.format("%.3f", burnStartHealth[id]) .. " at " .. os.date("%H:%M:%S", os.time()))
            end
        end

        if zombie:getHealth() <= 0 and burnStartHealth[id] and burnStartHealth[id] > 0 and isDebug then
            local elapsed = os.time() - (burnStartTime[id] or os.time())
            if isDebug then
                print("[FireDamage] Zombie burned to death at " .. os.date("%H:%M:%S", os.time()) .. " Start health: " .. string.format("%.3f", burnStartHealth[id]) .. " Time: " .. tostring(elapsed) .. "s")
            end
            burnStartHealth[id] = nil
            burnStartTime[id] = nil
        end

        burnTickCounter[id] = (burnTickCounter[id] or 0) + 1
        if burnTickCounter[id] >= 300 then
            burnTickCounter[id] = 0
            local health = zombie:getHealth()
            zombie:setHealth(health - 0.02 * SandboxVars.Explosives.FireDamageMultiplier)
            if isDebug then
                print("[FireDamage] Health: " .. string.format("%.3f", zombie:getHealth()) .. " timestamp: " .. os.date("%H:%M:%S", os.time()))
            end
        end
    else
        local id = tostring(zombie)
        burnTickCounter[id] = nil
        burnStartHealth[id] = nil
        burnStartTime[id] = nil
    end
end)