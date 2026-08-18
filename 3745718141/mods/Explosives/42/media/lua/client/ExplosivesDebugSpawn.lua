require "PZAPI/ExplosivesOptions"

-- Lets testers spawn a flashbang without full debug mode, for trajectory/collision testing.
-- Flashbang specifically: harmless to the tester and surroundings.
local function onKeyPressed(key)
    if not (SandboxVars.Explosives and SandboxVars.Explosives.DebugSpawnKeyEnabled) then return end
    if not ExplosivesOptions or not ExplosivesOptions.debugSpawnKey then return end
    if key ~= ExplosivesOptions.debugSpawnKey:getValue() then return end
    if not (isKeyDown(Keyboard.KEY_LSHIFT) or isKeyDown(Keyboard.KEY_RSHIFT)) then return end

    local player = getPlayer()
    if not player or player:isDead() then return end

    player:getInventory():AddItem("Explosives.M84Flashbang")
    if player.Say then
        player:Say(getText("UI_Explosives_DebugSpawnGiven"))
    end
end

Events.OnKeyPressed.Add(onKeyPressed)
