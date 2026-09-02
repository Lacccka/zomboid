-- ============================================================================
-- 自动装弹机构 (auto-loading device) — CLIENT side.
--
-- The authoritative bullet -> magazine transfer is done by the server
-- (AutoReloadSyncServer.lua) and replicated to every client. This file only
-- reacts to the server's "Loaded" notification to play the magazine-insert
-- sound once per load burst.
-- ============================================================================

local function playInsertSound()
    if not isIngameState() then return end

    local player = getSpecificPlayer(0)
    if player == nil or player:isDead() then return end

    if not player:getEmitter():isPlaying("MagazineInsertAmmo") then
        local sound = player:getEmitter():playSound("MagazineInsertAmmo")
        player:getEmitter():setVolume(sound, 1.5)
    end
end

local function onServerCommand(module, command, args)
    if module ~= AutoReloadSync.MODULE then return end
    if command == AutoReloadSync.CMD_LOADED then
        playInsertSound()
    end
end

Events.OnServerCommand.Add(onServerCommand)
