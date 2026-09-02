-- ============================================================================
-- 自动装弹机构 (auto-loading device) — SERVER side.
--
-- The original implementation ran on the client inside Events.OnTick and
-- mutated magazine ammo counts / removed bullets directly. In multiplayer those
-- client-side mutations are never seen by the server, so the loaded magazines
-- desynchronise. This server tick does the same work on the authoritative side,
-- so every change replicates to all clients. It also runs in singleplayer
-- (isServer() is true there), preserving singleplayer behaviour.
-- ============================================================================

local MODULE = AutoReloadSync.MODULE

local function log(message)
    print("[AutoReloadSyncServer] " .. tostring(message))
end

local function getWornAutoReloadBag(player)
    if not player or player:isDead() then
        return nil
    end
    local ok, bag = pcall(function()
        return player:getWornItem(ItemBodyLocation.LEFT_MIDDLE_FINGER)
    end)
    if not ok or not bag then
        return nil
    end
    if AutoReloadSync.IsAutoReloadBag(bag) then
        return bag
    end
    return nil
end

-- Move loose bullets into compatible magazines inside the bag's inventory.
-- Returns true when at least one bullet was transferred.
local function loadBulletsIntoMagazines(container)
    local items = container:getItems()
    if not items then
        return false
    end

    local magazines, bullets = {}, {}
    for i = 0, items:size() - 1 do
        local item = items:get(i)
        if item then
            local okAmmo, ammoType = pcall(function() return item:getAmmoType() end)
            if okAmmo and ammoType ~= nil then
                magazines[#magazines + 1] = item
            else
                bullets[#bullets + 1] = item
            end
        end
    end

    local loaded = false
    for _, mag in ipairs(magazines) do
        local maxAmmo = mag:getMaxAmmo()
        if maxAmmo and maxAmmo > 0 and mag:getCurrentAmmoCount() < maxAmmo then
            local okKey, magKey = pcall(function() return mag:getAmmoType():getItemKey() end)
            if okKey and magKey then
                -- Walk backwards so a consumed bullet can be dropped from the list
                -- without disturbing the remaining indices.
                for bi = #bullets, 1, -1 do
                    local bullet = bullets[bi]
                    if bullet and (bullet:getModule() .. "." .. bullet:getType()) == magKey then
                        mag:setCurrentAmmoCount(mag:getCurrentAmmoCount() + 1)
                        container:DoRemoveItem(bullet)
                        bullets[bi] = nil
                        loaded = true
                        if mag:getCurrentAmmoCount() >= maxAmmo then
                            break
                        end
                    end
                end
            end
        end
    end
    return loaded
end

local function processPlayer(player)
    local bag = getWornAutoReloadBag(player)
    if not bag then
        return
    end
    local ok, loaded = pcall(loadBulletsIntoMagazines, bag:getInventory())
    if ok and loaded then
        pcall(sendServerCommand, player, MODULE, AutoReloadSync.CMD_LOADED, {})
    end
end

local function onTick()
    if not isServer() then
        return
    end

    local handled = 0
    local ok, players = pcall(function() return getOnlinePlayers() end)
    if ok and players and players.size and players:size() > 0 then
        for i = 0, players:size() - 1 do
            local player = players:get(i)
            if player then
                processPlayer(player)
                handled = handled + 1
            end
        end
    end

    -- Singleplayer integrated server: the online-player list may be empty; fall
    -- back to the local player so the device still works without networking.
    if handled == 0 and isClient() then
        local localPlayer = getSpecificPlayer(0)
        if localPlayer then
            processPlayer(localPlayer)
        end
    end
end

if not AutoReloadSync._serverRegistered then
    Events.OnTick.Add(onTick)
    AutoReloadSync._serverRegistered = true
end

log("loaded")
