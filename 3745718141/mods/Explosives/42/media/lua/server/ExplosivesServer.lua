if isClient() then return end

-- Authoritative counterpart to the client-side grenade/flare throw in
-- GrenadeBallistics.lua. The client already removes the weapon locally
-- for instant UI feedback, but that alone doesn't reliably persist to
-- the server's saved state in MP -- this performs the actual removal
-- on the server's own authoritative inventory, driven by the item ID
-- the client sent.
local function onClientCommand(module, command, player, args)
    if module ~= "Explosives" then return end

    if command == "ConsumeThrownWeapon" then
        local inventory = player:getInventory()
        local item = inventory:getItemById(args.itemID)
        if item then
            inventory:Remove(item)
        end
    end

    if command == "SwapHeldFlare" then
        local inventory = player:getInventory()
        local oldItem = inventory:getItemById(args.oldItemID)
        local primaryItem = player:getPrimaryHandItem()
        local secondaryItem = player:getSecondaryHandItem()
        local isPrimary = oldItem ~= nil and primaryItem ~= nil and primaryItem:getID() == oldItem:getID()
        local isSecondary = oldItem ~= nil and secondaryItem ~= nil and secondaryItem:getID() == oldItem:getID()

        if isPrimary then player:setPrimaryHandItem(nil) end
        if isSecondary then player:setSecondaryHandItem(nil) end
        if oldItem then inventory:Remove(oldItem) end

        local newItem = inventory:AddItem(args.newFullType)
        if newItem then
            newItem:getModData().ignitedAtGameHours = args.ignitedAtGameHours
            if isPrimary then player:setPrimaryHandItem(newItem) end
            if isSecondary then player:setSecondaryHandItem(newItem) end
        end
    end

    -- Authoritative counterpart to the placeAsTrap landing path in
    -- GrenadeBallistics.lua. The client already places its own trap
    -- locally for instant visual feedback (matching vanilla's own
    -- ISPlaceTrap:complete()), but that call alone doesn't reliably
    -- persist in MP when it's made outside a real TimedAction context --
    -- the trap needs to exist in the server's own authoritative world too.
    if command == "PlaceTrap" then
        local square = getCell():getGridSquare(args.x, args.y, args.z)
        if square then
            local trapItem = instanceItem(args.weaponType)
            if trapItem then
                trapItem:setRemoteControlID(args.remoteControlID)
                trapItem:setRemoteRange(args.remoteRange)
                local trap = IsoTrap.new(player, trapItem, getCell(), square)
                trap:place()
            end
        end
    end

    -- Authoritative counterpart to the immediate-explosion landing path
    -- in GrenadeBallistics.lua. Same story as PlaceTrap above: the
    -- client's own triggerExplosion() call gives instant local FX/sound,
    -- but side effects like fire-starting don't reliably register
    -- server-side from outside a TimedAction -- confirmed by this mod's
    -- own M14TH3Grenade not igniting fires in MP despite working in SP.
    if command == "TriggerExplosion" then
        local square = getCell():getGridSquare(args.x, args.y, args.z)
        if square then
            local trapItem = instanceItem(args.weaponType)
            if trapItem then
                local trap = IsoTrap.new(player, trapItem, getCell(), square)
                trap:triggerExplosion(false)
            end
            -- Molotov and FlameTrap ("Fire Bomb") have no ExplosionPower,
            -- so triggerExplosion() above doesn't ignite anything -- start
            -- the fire natively, the same call vanilla's own campfire
            -- system uses (server-side only).
            if args.weaponType == "Base.Molotov" or args.weaponType == "Base.FlameTrap" then
                IsoFireManager.StartFire(getCell(), square, true, 100, 500)
            end
        end
    end
end
Events.OnClientCommand.Add(onClientCommand)
