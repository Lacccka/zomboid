if isServer() then
    local NPC_AttackServerCommands = {}

    local NPC_GuardConfig = require("DialogueFramework/Behavior/NPC_GuardConfig")
    local NPC_AttackExecutor = require("DialogueFramework/Behavior/NPC_AttackExecutor")
    local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
    local NPC_BehaviorController = require("DialogueFramework/Behavior/NPC_BehaviorController")

    function NPC_AttackServerCommands.OnClientCommand(module, command, player, args)
        if module ~= NPC_GuardConfig.COMMANDS.ATTACK_SYSTEM.MODULE then
            return
        end

        if command == NPC_GuardConfig.COMMANDS.ATTACK_SYSTEM.INITIATE then
            NPC_AttackServerCommands.initiateAttack(player, args)
        elseif command == NPC_GuardConfig.COMMANDS.ATTACK_SYSTEM.EXECUTE then
            NPC_AttackServerCommands.executeAttack(player, args)
        elseif command == NPC_GuardConfig.COMMANDS.ATTACK_SYSTEM.TARGET_ELIMINATED then
            NPC_AttackServerCommands.targetEliminated(player, args)
        elseif command == NPC_GuardConfig.COMMANDS.ATTACK_SYSTEM.ALL_CLEARED then
            NPC_AttackServerCommands.allTargetsCleared(player, args)
        elseif command == NPC_GuardConfig.COMMANDS.ATTACK_SYSTEM.SET_ATTACKING_STATE then
            NPC_AttackServerCommands.setAttackingState(player, args)
        end
    end

    function NPC_AttackServerCommands.initiateAttack(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID
        local zoneID = args.zoneID

        if not npcID or not zoneID then
            return
        end
    end

    function NPC_AttackServerCommands.executeAttack(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID
        local targetID = args.targetID

        if not npcID or not targetID then
            return
        end

        local npc = NPC_BehaviorNPCRegistry.getNPCByID(npcID)
        if not npc then
            return
        end
    end

    function NPC_AttackServerCommands.targetEliminated(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID
        local targetID = args.targetID

        if not npcID then
            return
        end
    end

    function NPC_AttackServerCommands.allTargetsCleared(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID

        if not npcID then
            return
        end
    end

    function NPC_AttackServerCommands.setAttackingState(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID
        local isAttacking = args.isAttacking

        if not npcID then
            return
        end

        local npc = NPC_BehaviorNPCRegistry.getNPCByID(npcID)
        if not npc then
            return
        end

        local Muggy_VariableBridge = require("MuggyMod/Muggy_VariableBridge")
        Muggy_VariableBridge.setMuggyAttacking(npc, isAttacking)
    end

    Events.OnClientCommand.Add(NPC_AttackServerCommands.OnClientCommand)
end
