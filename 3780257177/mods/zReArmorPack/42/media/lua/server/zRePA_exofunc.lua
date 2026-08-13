local function zReOnClientCommands(module, command, player, args)
    if module ~= 'zRePA' then
        return
    end
	if command == 'zRe_updateWeight' then
        player:setMaxWeightBase(args.weight)
    end
end

Events.OnClientCommand.Add(zReOnClientCommands)