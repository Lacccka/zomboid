require "PZAPI/ExplosivesOptions"

-- Lets testers spawn a single flashbang directly into their inventory without
-- switching to full debug mode, so trajectory/collision testing (walls,
-- fences, vegetation whitelist reports) doesn't require burning a rare,
-- non-craftable grenade found through normal loot. Flashbang specifically,
-- since it can't hurt the tester or damage the surroundings while testing.
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
