if isServer() then
    local NPC_GiftingServerCommands = {}

    function NPC_GiftingServerCommands.OnClientCommand(module, command, player, args)
        if module ~= "NPCGifting" then
            return
        end

        if command == "AwardGift" then
            NPC_GiftingServerCommands.awardGiftToPlayer(player, args)
        end
    end

    function NPC_GiftingServerCommands.awardGiftToPlayer(player, args)
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

    Events.OnClientCommand.Add(NPC_GiftingServerCommands.OnClientCommand)
end
