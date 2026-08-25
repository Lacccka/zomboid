if isServer() then
    local NPC_GuardServerCommands = {}

    local NPC_GuardConfig = require("DialogueFramework/Behavior/NPC_GuardConfig")
    local NPC_GuardStateManager = require("DialogueFramework/Behavior/NPC_GuardStateManager")
    local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
    local NPC_Behavior_Guarding = require("DialogueFramework/Behavior/Behaviors/NPC_Behavior_Guarding")

    function NPC_GuardServerCommands.OnClientCommand(module, command, player, args)
        if module ~= NPC_GuardConfig.COMMANDS.GUARD_SYSTEM.MODULE then
            return
        end

        if command == NPC_GuardConfig.COMMANDS.GUARD_SYSTEM.ACTIVATE then
            NPC_GuardServerCommands.activateGuarding(player, args)
        elseif command == NPC_GuardConfig.COMMANDS.GUARD_SYSTEM.REFRESH then
            NPC_GuardServerCommands.refreshGuarding(player, args)
        elseif command == NPC_GuardConfig.COMMANDS.GUARD_SYSTEM.UPDATE_BOUNDARY then
            NPC_GuardServerCommands.updateBoundary(player, args)
        elseif command == NPC_GuardConfig.COMMANDS.GUARD_SYSTEM.NPC_EXITED_ZONE then
            NPC_GuardServerCommands.npcExitedZone(player, args)
        elseif command == NPC_GuardConfig.COMMANDS.GUARD_SYSTEM.NPC_RETURNED_ZONE then
            NPC_GuardServerCommands.npcReturnedZone(player, args)
        end
    end

    function NPC_GuardServerCommands.activateGuarding(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID
        local zoneID = args.zoneID

        if not npcID or not zoneID then
            return
        end

        NPC_GuardStateManager.activateGuarding(player, npcID, zoneID)
    end

    function NPC_GuardServerCommands.refreshGuarding(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID
        local zoneID = args.zoneID

        if not npcID or not zoneID then
            return
        end

        local npc = NPC_BehaviorNPCRegistry.getNPCByID(npcID)
        if not npc then
            return
        end

        NPC_Behavior_Guarding.refresh(npc, player)
    end

    function NPC_GuardServerCommands.updateBoundary(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID

        if not npcID then
            return
        end

        NPC_GuardStateManager.updateBoundaryCheckTime(player, npcID)
    end

    function NPC_GuardServerCommands.npcExitedZone(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID
        local zoneID = args.zoneID

        if not npcID or not zoneID then
            return
        end
    end

    function NPC_GuardServerCommands.npcReturnedZone(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID
        local zoneID = args.zoneID

        if not npcID or not zoneID then
            return
        end
    end

    Events.OnClientCommand.Add(NPC_GuardServerCommands.OnClientCommand)
end
