if isServer() then
    local NPC_ZoneScanServerCommands = {}

    local NPC_GuardConfig = require("DialogueFramework/Behavior/NPC_GuardConfig")
    local NPC_GuardStateManager = require("DialogueFramework/Behavior/NPC_GuardStateManager")
    local NPC_GuardZoneScanner = require("DialogueFramework/Behavior/NPC_GuardZoneScanner")

    function NPC_ZoneScanServerCommands.OnClientCommand(module, command, player, args)
        if module ~= NPC_GuardConfig.COMMANDS.SCAN_SYSTEM.MODULE then
            return
        end

        if command == NPC_GuardConfig.COMMANDS.SCAN_SYSTEM.ACTIVATE then
            NPC_ZoneScanServerCommands.activateScan(player, args)
        elseif command == NPC_GuardConfig.COMMANDS.SCAN_SYSTEM.NPC_DETECTED then
            NPC_ZoneScanServerCommands.npcDetected(player, args)
        elseif command == NPC_GuardConfig.COMMANDS.SCAN_SYSTEM.SCAN_DORMANT then
            NPC_ZoneScanServerCommands.scanDormant(player, args)
        elseif command == NPC_GuardConfig.COMMANDS.SCAN_SYSTEM.REFRESH then
            NPC_ZoneScanServerCommands.refreshScan(player, args)
        end
    end

    function NPC_ZoneScanServerCommands.activateScan(player, args)
        if not player or not args then
            return
        end
    end

    function NPC_ZoneScanServerCommands.npcDetected(player, args)
        if not player or not args then
            return
        end

        local npcID = args.npcID
        local zoneID = args.zoneID
        local playerID = args.playerID

        if not npcID or not zoneID or not playerID then
            return
        end

        NPC_GuardStateManager.activateGuarding(player, npcID, zoneID)
    end

    function NPC_ZoneScanServerCommands.scanDormant(player, args)
        if not player or not args then
            return
        end
    end

    function NPC_ZoneScanServerCommands.refreshScan(player, args)
        if not player or not args then
            return
        end
    end

    Events.OnClientCommand.Add(NPC_ZoneScanServerCommands.OnClientCommand)
end
