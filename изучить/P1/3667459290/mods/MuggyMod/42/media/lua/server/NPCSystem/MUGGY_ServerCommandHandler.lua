local MUGGY_ServerCommandHandler = {}

local MUGGY_ZoneResponseSystem = nil

local function getZoneResponseSystem()
    if not MUGGY_ZoneResponseSystem then
        local success, result = pcall(function()
            return require("NPCSystem/MUGGY_ZoneResponseSystem")
        end)
        if success and result then
            MUGGY_ZoneResponseSystem = result
        end
    end
    return MUGGY_ZoneResponseSystem
end

local function onClientCommand(module, command, player, args)
    if module ~= "MUGGY_ZoneSystem" then
        return
    end

    print("[MUGGY_ServerCommand] Received command: " .. command .. " from player " .. tostring(player:getOnlineID()))

    local zoneSystem = getZoneResponseSystem()
    if not zoneSystem then
        print("[MUGGY_ServerCommand] ERROR: Zone response system not available")
        return
    end

    if command == "playerZoneEntry" then
        if not args or not args.zoneId then
            print("[MUGGY_ServerCommand] ERROR: Invalid args for playerZoneEntry")
            return
        end

        local success, error = pcall(function()
            zoneSystem.onPlayerEnterZone(player, args)
        end)

        if not success then
            print("[MUGGY_ServerCommand] ERROR: playerZoneEntry failed: " .. tostring(error))
        end

    elseif command == "playerZoneExit" then
        if not args or not args.zoneId then
            print("[MUGGY_ServerCommand] ERROR: Invalid args for playerZoneExit")
            return
        end

        local success, error = pcall(function()
            zoneSystem.onPlayerExitZone(player, args)
        end)

        if not success then
            print("[MUGGY_ServerCommand] ERROR: playerZoneExit failed: " .. tostring(error))
        end
    else
        print("[MUGGY_ServerCommand] WARNING: Unknown command: " .. command)
    end
end

function MUGGY_ServerCommandHandler.initialize()
    print("[MUGGY_ServerCommand] Initializing server command handler")

    if Events and Events.OnClientCommand then
        Events.OnClientCommand.Add(onClientCommand)
        print("[MUGGY_ServerCommand] Registered OnClientCommand handler")
    else
        print("[MUGGY_ServerCommand] WARNING: OnClientCommand event not available")
    end

    print("[MUGGY_ServerCommand] Server command handler initialized")
end

function MUGGY_ServerCommandHandler.shutdown()
    print("[MUGGY_ServerCommand] Shutting down server command handler")

    if Events and Events.OnClientCommand then
        Events.OnClientCommand.Remove(onClientCommand)
    end
end

if Events and Events.OnGameEnd then
    Events.OnGameEnd.Add(MUGGY_ServerCommandHandler.shutdown)
end

return MUGGY_ServerCommandHandler
