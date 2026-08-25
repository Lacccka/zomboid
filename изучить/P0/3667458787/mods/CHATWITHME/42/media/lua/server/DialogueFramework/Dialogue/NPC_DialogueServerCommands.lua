if isServer() then
    local NPC_DialogueServerCommands = {}

    function NPC_DialogueServerCommands.OnClientCommand(module, command, player, args)
        if module ~= "NPCDialogue" then
            return
        end

        if command == "RemoveRequiredItem" then
            NPC_DialogueServerCommands.removeRequiredItem(player, args)
        elseif command == "StartApproach" then
            NPC_DialogueServerCommands.startApproach(player, args)
        end
    end

    function NPC_DialogueServerCommands.removeRequiredItem(player, args)
        if not player or not args or not args.itemType then
            return
        end

        local inventory = player:getInventory()
        if not inventory then
            return
        end

        local item = inventory:getFirstTypeRecurse(args.itemType)
        if item then
            inventory:Remove(item)
            player:sendObjectChange("inventory")

            if args.flagKey then
                local modData = player:getModData()
                if modData then
                    modData[args.flagKey] = true
                end
            end
        end
    end

    function NPC_DialogueServerCommands.startApproach(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID
        if not npcID then
            return
        end

        local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
        local npc = NPC_BehaviorNPCRegistry.getNPCByID(npcID)

        if not npc then
            return
        end

        local NPC_Behavior_Talking = require("DialogueFramework/Behavior/Behaviors/NPC_Behavior_Talking")
        NPC_Behavior_Talking.startApproach(npc, player)
    end

    Events.OnClientCommand.Add(NPC_DialogueServerCommands.OnClientCommand)
end
