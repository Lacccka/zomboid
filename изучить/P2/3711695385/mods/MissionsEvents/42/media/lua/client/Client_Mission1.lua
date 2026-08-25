MissionsEvents = MissionsEvents or {}

function MissionsEvents.startM1()
    local player = getPlayer()
    if not player then return end

    sendClientCommand("MissionsEvents", "StartM1", {})
end

local function onServerCommand(module, command, args)

    if module ~= "MissionsEvents" then return end

    if command == "HaloStartM1" then

        local player = getPlayer()
        if not player then return end

        HaloTextHelper.addTextWithArrow(
            player,
            getText(args.text or "IGUI_SpawnHorde"),
            true,
            255, 50, 50
        )
    end
end

Events.OnServerCommand.Add(onServerCommand)