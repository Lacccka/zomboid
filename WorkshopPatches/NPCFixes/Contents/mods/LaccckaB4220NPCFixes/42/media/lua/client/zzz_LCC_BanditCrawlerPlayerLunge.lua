-- Preserve the small ordinary-crawler -> player lunge seam from Bandits2
-- BanditUpdate when NPCFixes deliberately bypasses upstream UpdateZombies.
-- This contains no third-party implementation file; it mirrors only the public
-- state transition that occurs before the unsafe Bandit character-target block.

if isServer() then return end

local MARKER = "ordinary-crawler-player-lunge-v1"
LCC_NPCFIXES_CRAWLER_PLAYER_LUNGE = MARKER

local function isBandit(character)
    if not character or not instanceof(character, "IsoZombie") then return false end
    local ok, value = pcall(function() return character:getVariableBoolean("Bandit") end)
    return ok and value == true
end

local function onZombieUpdate(zombie)
    if not zombie or isBandit(zombie) or not zombie:isAlive() or not zombie:isCrawling() then return end

    local target = zombie:getTarget()
    if not target or not instanceof(target, "IsoPlayer") or target:getVariableBoolean("Bandit") then return end

    local targetSquare = target:getSquare()
    local zombieSquare = zombie:getSquare()
    if not targetSquare or not zombieSquare then return end

    local zx, zy, zz = zombie:getX(), zombie:getY(), zombie:getZ()
    local px, py, pz = target:getX(), target:getY(), target:getZ()
    local dx, dy = zx - px, zy - py
    local dist2 = (dx * dx) + (dy * dy)
    if dist2 >= 0.64 or math.abs(zz - pz) >= 0.3 or not zombie:CanSee(target) then return end
    if zombieSquare:isSomethingTo(targetSquare) or not zombie:isFacingObject(target, 0.3) then return end

    zombie:changeState(LungeState.instance())
    local pfb = zombie:getPathFindBehavior2()
    if pfb then pfb:cancel() end
    zombie:setPath2(nil)
end

Events.OnZombieUpdate.Add(onZombieUpdate)

print("[LCC][NPCFixes][CrawlerLunge][BOOT] marker=" .. MARKER)
