if isServer() then
    local NPC_TradingServerCommands = {}

    function NPC_TradingServerCommands.OnClientCommand(module, command, player, args)
        if module ~= "NPCTrading" then
            return
        end

        if command == "RemoveItems" then
            NPC_TradingServerCommands.removeItemsFromPlayer(player, args)
        elseif command == "AwardItems" then
            NPC_TradingServerCommands.awardItemsToPlayer(player, args)
        end
    end

    function NPC_TradingServerCommands.removeItemsFromPlayer(player, args)
        if not player or not args then
            return
        end

        local itemType = args.itemType
        local quantity = args.quantity or -1

        if not itemType then
            return
        end

        local inventory = player:getInventory()
        if not inventory then
            return
        end

        local items = inventory:getAllType(itemType)

        if items then
            if quantity == -1 then
                for i = items:size() - 1, 0, -1 do
                    inventory:Remove(items:get(i))
                end
            else
                local removed = 0
                for i = items:size() - 1, 0, -1 do
                    if removed >= quantity then
                        break
                    end
                    inventory:Remove(items:get(i))
                    removed = removed + 1
                end
            end
        end

        player:sendObjectChange("inventory")
    end

    function NPC_TradingServerCommands.awardItemsToPlayer(player, args)
        if not player or not args then
            return
        end

        local itemType = args.itemType
        local quantity = args.quantity or 1

        if not itemType then
            return
        end

        local inventory = player:getInventory()
        if not inventory then
            return
        end

        for i = 1, quantity do
            inventory:AddItem(itemType)
        end

        player:sendObjectChange("inventory")
    end

    Events.OnClientCommand.Add(NPC_TradingServerCommands.OnClientCommand)
end
