-- Copyright (c) 2022-2025 Oneline/D.Borovsky
-- All rights reserved

Kamisama = {}

Kamisama.getIsoDirectionFromString = function(d)
    if d == "N" then
        return IsoDirections.N
    elseif d == "NW" then
        return IsoDirections.NW
    elseif d == "W" then
        return IsoDirections.W
    elseif d == "SW" then
        return IsoDirections.SW
    elseif d == "S" then
        return IsoDirections.S
    elseif d == "SE" then
        return IsoDirections.SE
    elseif d == "E" then
        return IsoDirections.E
    elseif d == "NE" then
        return IsoDirections.NE
    end
end

-- Client side

Kamisama.requestTeleport = function (sender, x, y, z, callback)
    triggerEvent("OnTeleport", nil);
    if isClient() then
        SyncClient.request('QSystem', 'teleport', {x, y, z}, sender, callback);
    elseif not isServer() then
        local player = getPlayer();
        player:setX(x); player:setY(y); player:setZ(z);
        player:setLastX(x); player:setLastY(y); player:setLastZ(z);
        if callback then
            SSRTimer.add_ms(callback, 100, false);
        end
    end
end

Kamisama.requestVehicle = function (sender, vehicleType, x, y, z, d, skin, keyName, callback)
    if isClient() then
        SyncClient.request('QSystem', 'spawnVehicle', {vehicleType, x, y, z, d, skin, keyName}, sender, callback);
    elseif not isServer() then
        local square = getCell():getGridSquare(x, y, z)
        local direction = Kamisama.getIsoDirectionFromString(d);

        if not square then
            print(string.format("[QSystem] (Error) Unable to get square at coordinates x=%s y=%s z=%s.", x, y, z))
        else
            local v = addVehicleDebug(vehicleType, direction, skin or -1, square)

            if v then
                local key = v:createVehicleKey()

                if key then
                    if keyName then
                        key:setName(keyName)
                    end
                    getPlayer():getInventory():AddItem(key);
                else
                    v:removeFromWorld();
                    print(string.format("[QSystem] (Error) Unable to create the key. Removing vehicle %s (x=%s y=%s z=%s) from world...", v:getId(), x, y, z))
                end
            else
                print("[QSystem] (Error) Failed to create vehicle.")
            end
        end
        if callback then
            SSRTimer.add_ms(callback, 100, false);
        end
    end
end

Kamisama.sendXP = function (player, username, perks)
    for i=1, #perks do
        local perk_id = tostring(perks[i].name);
        local perk = Perks.FromString(perk_id);
        if perk then
            local level = player:getPerkLevel(perk);
            if level < 10 then
                if perks[i].amount then
                    addXpNoMultiplier(player, perk, perks[i].amount)
                    print(string.format("[QSystem] Kamisama: '%s' gained %i EXP for perk - %s.", username, perks[i].amount, perk_id));
                else
                    local required_xp = perk:getTotalXpForLevel(level+1) - player:getXp():getXP(perk)
                    addXpNoMultiplier(player, perk, required_xp);
                    print(string.format("[QSystem] Kamisama: '%s' gained level up for perk '%s' by adding %i EXP", username, perk_id, required_xp));
                end
            else
                print(string.format("[QSystem] Kamisama: '%s' has already maxed perk '%s'.", username, perk_id));
            end
        end
    end

    return true;
end

Kamisama.addEXP = function (sender, perks, callback)
    local player = getPlayer();
    if isClient() then
        SyncClient.request('QSystem', 'addXP', perks, sender, callback);
    else
        for i=1, #perks do
            local perk_id = tostring(perks[i].name);
            local perk = Perks.FromString(perk_id);
            if perk then
                local level = player:getPerkLevel(perk);
                if level < 10 then
                    if perks[i].amount then
                        player:getXp():AddXP(perk, perks[i].amount, true, false, false);
                        QuestLogger.print(string.format("[QSystem] Kamisama: Gained %i EXP for perk - %s.", perks[i].amount, perk_id));
                    else
                        local required_xp = perk:getTotalXpForLevel(level+1) - player:getXp():getXP(perk)
                        player:getXp():AddXP(perk, required_xp, true, false, false);
                        QuestLogger.print(string.format("[QSystem] Kamisama: Gained level up for perk '%s' by adding %i EXP", perk_id, required_xp));
                    end
                else
                    QuestLogger.print(string.format("[QSystem] Kamisama: Perk '%s' is already maxed.", perk_id));
                end
            end
        end
        if callback then
            SSRTimer.add_ms(callback, 100, false);
        end
    end
end

Kamisama.getTraitById = function(id)
    local traits = CharacterTraitDefinition.getTraits();
    for i=0, traits:size()-1 do
        local trait = traits:get(i);
        if trait:getType():toString() == tostring(id) or trait:getType():getName() == tostring(id):lower() then
            return trait;
        end
    end
end

Kamisama.addTraits = function (sender, traits, callback)
    local player = getPlayer();
    if isClient() then sendClientCommand(player,'QSystem', 'log', { 1, sender }) end
    for i=1, #traits do
        local trait = Kamisama.getTraitById(traits[i]);
        if trait and not player:hasTrait(trait:getType()) then
            local continue = true;
            if trait:hasMutuallyExclusiveTraits() then
                local excluded = trait:getMutuallyExclusiveTraits();
                for j=0, excluded:size()-1 do
                    local t = excluded:get(j);
                    if player:hasTrait(t) then
                        QuestLogger.print("[QSystem] Kamisama: Removed mutually exclusive trait - "..tostring(t:toString()));
                        player:getCharacterTraits():remove(t);
                        player:modifyTraitXPBoost(t, true);
                        continue = false;
                    end
                end
            end

            if continue then
                QuestLogger.print("[QSystem] Kamisama: Added trait - "..tostring(trait:getType():toString()));
                player:getCharacterTraits():add(trait:getType());
                player:modifyTraitXPBoost(trait:getType(), false);
            end
            SyncXp(player);
        end
    end
    if callback then
        SSRTimer.add_ms(callback, 100, false);
    end
end

-- Server side

Kamisama.teleport = function (username, x, y, z)
    return jm_teleport(username, x, y, z)
end

Kamisama.spawnVehicle = function (player, vehicleType, x, y, z, d, skin, keyName) -- Thanks to Loner_Wolf1498 for figuring out a proper way to do that in MP 
    local square = getCell():getGridSquare(x, y, z)
    local direction = Kamisama.getIsoDirectionFromString(d);

    if not square then
        print(string.format("[QSystem] (Error) Unable to get square at coordinates x=%s y=%s z=%s.", x, y, z))
    elseif not direction then
        print(string.format("[QSystem] (Error) Invalid direction '%s'.", tostring(d)))
    else
        local v = addVehicleDebug(vehicleType, direction, skin or -1, square)

        if v then
            local key = v:createVehicleKey()

            if key and player then
                if keyName then
                    key:setName(keyName)
                end
                player:getInventory():AddItem(key);
                sendAddItemToContainer(player:getInventory(), key)
                print(string.format("[QSystem] Created vehicle (%s) at coordinates x=%s y=%s z=%s and sent the key to player `%s`.", v:getId(), x, y, z, player:getUsername()))
                return true
            else
                v:removeFromWorld();
                print(string.format("[QSystem] (Error) Unable to send the key to player. Removing vehicle %s (x=%s y=%s z=%s) from world...", v:getId(), x, y, z))
            end
        else
            print("[QSystem] (Error) Failed to create vehicle.")
        end
    end
end