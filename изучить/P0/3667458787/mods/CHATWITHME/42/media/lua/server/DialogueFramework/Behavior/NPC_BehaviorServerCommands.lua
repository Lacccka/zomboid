if isServer() then
    local NPC_BehaviorServerCommands = {}

    local NPC_BehaviorNPCRegistry = require("DialogueFramework/Behavior/NPC_BehaviorNPCRegistry")
    local NPC_BehaviorDefinitionRegistry = require("DialogueFramework/Behavior/NPC_BehaviorDefinitionRegistry")

    function NPC_BehaviorServerCommands.OnClientCommand(module, command, player, args)
        if module ~= "NPCBehavior" then
            return
        end

        if command == "ExecuteBehavior" then
            NPC_BehaviorServerCommands.executeBehavior(player, args)
        elseif command == "CancelBehavior" then
            NPC_BehaviorServerCommands.cancelBehavior(player, args)
        end
    end

    function NPC_BehaviorServerCommands.executeBehavior(player, args)
        if not player or not args then return end

        local npcID = args.npcID
        local behaviorID = args.behaviorID
        local params = args.params

        if not NPC_BehaviorDefinitionRegistry.isRegistered(behaviorID) then
            return
        end

        local npc = NPC_BehaviorNPCRegistry.getNPCByID(npcID)
        if not npc then
            return
        end

        local behaviorDef = NPC_BehaviorDefinitionRegistry.get(behaviorID)
        local success, behaviorModule = pcall(function()
            return require(behaviorDef.moduleFile)
        end)

        if success and behaviorModule and behaviorModule.execute then
            local execSuccess, result = pcall(function()
                return behaviorModule.execute(npc, params, player)
            end)

            local NPC_BehaviorController = require("DialogueFramework/Behavior/NPC_BehaviorController")
            local queue = NPC_BehaviorNPCRegistry.getQueue(npcID)

            if queue then
                for _, behaviorEntry in ipairs(queue) do
                    if behaviorEntry.behaviorID == behaviorID and behaviorEntry.status == "executing" then
                        if execSuccess and result then
                            NPC_BehaviorController.onBehaviorComplete(npc, behaviorEntry, result)
                        else
                            NPC_BehaviorController.onBehaviorFailed(npc, behaviorEntry, result)
                        end
                        break
                    end
                end
            end
        end
    end

    function NPC_BehaviorServerCommands.cancelBehavior(player, args)
        if not player or not args then return end

        local npcID = args.npcID
        local behaviorID = args.behaviorID

        local npc = NPC_BehaviorNPCRegistry.getNPCByID(npcID)
        if not npc then
            return
        end

        local NPC_BehaviorController = require("DialogueFramework/Behavior/NPC_BehaviorController")
        NPC_BehaviorController.cancelBehavior(npc, behaviorID)
    end

    Events.OnClientCommand.Add(NPC_BehaviorServerCommands.OnClientCommand)
end
