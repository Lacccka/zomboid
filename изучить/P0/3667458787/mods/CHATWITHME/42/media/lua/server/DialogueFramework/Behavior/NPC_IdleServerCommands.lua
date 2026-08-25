if isServer() then
    local NPC_IdleServerCommands = {}

    local NPC_GuardConfig = require("DialogueFramework/Behavior/NPC_GuardConfig")
    local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")

    function NPC_IdleServerCommands.OnClientCommand(module, command, player, args)
        if module ~= NPC_GuardConfig.COMMANDS.IDLE_SYSTEM.MODULE then
            return
        end

        if command == NPC_GuardConfig.COMMANDS.IDLE_SYSTEM.ACTIVATE then
            NPC_IdleServerCommands.activateIdleChilling(player, args)
        elseif command == NPC_GuardConfig.COMMANDS.IDLE_SYSTEM.EXECUTE_MOVE then
            NPC_IdleServerCommands.executeIdleMove(player, args)
        elseif command == NPC_GuardConfig.COMMANDS.IDLE_SYSTEM.DEACTIVATE then
            NPC_IdleServerCommands.deactivateIdleChilling(player, args)
        end
    end

    function NPC_IdleServerCommands.activateIdleChilling(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID
        local zoneID = args.zoneID

        if not npcID or not zoneID then
            return
        end
    end

    function NPC_IdleServerCommands.executeIdleMove(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID
        local targetX = args.targetX
        local targetY = args.targetY

        if not npcID or not targetX or not targetY then
            return
        end

        local npc = NPC_BehaviorNPCRegistry.getNPCByID(npcID)
        if not npc then
            return
        end

        local success, error = pcall(function()
            npc:setX(targetX)
            npc:setY(targetY)
        end)
    end

    function NPC_IdleServerCommands.deactivateIdleChilling(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID

        if not npcID then
            return
        end
    end

    Events.OnClientCommand.Add(NPC_IdleServerCommands.OnClientCommand)
end
