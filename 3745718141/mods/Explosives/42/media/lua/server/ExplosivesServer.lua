if isClient() then return end

-- Authoritative counterpart to the client-side throw in GrenadeBallistics.lua:
-- client removal is instant local feedback only, this does the real removal server-side.
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

    -- Authoritative counterpart to placeAsTrap in GrenadeBallistics.lua: client trap
    -- placement is local feedback only, doesn't persist in MP outside a TimedAction.
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

    -- Authoritative counterpart to explodeGrenadeAt in GrenadeBallistics.lua: local
    -- triggerExplosion() is FX/sound only, side effects like fire-starting need this
    -- (confirmed via M14TH3Grenade not igniting fires in MP).
    if command == "TriggerExplosion" then
        local square = getCell():getGridSquare(args.x, args.y, args.z)
        if square then
            local trapItem = instanceItem(args.weaponType)
            if trapItem then
                local trap = IsoTrap.new(player, trapItem, getCell(), square)
                trap:triggerExplosion(false)
            end
            -- Molotov/FlameTrap have no ExplosionPower, so triggerExplosion() doesn't
            -- ignite; start the fire natively (same call as vanilla campfires).
            if args.weaponType == "Base.Molotov" or args.weaponType == "Base.FlameTrap" then
                IsoFireManager.StartFire(getCell(), square, true, 100, 500)
            end
        end
    end
end
Events.OnClientCommand.Add(onClientCommand)
