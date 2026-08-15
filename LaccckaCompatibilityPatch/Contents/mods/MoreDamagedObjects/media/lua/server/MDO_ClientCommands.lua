-- From "More Damaged Objects [B41]" mod -- Author = carlesturo

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - CLIENT COMMANDS ****************

local function onClientCommand(module, command, player, args)
    if module == "MDO" then
        
		--if command == "RequestPlaySound" and args and args.sound then
			--local square = getCell():getGridSquare(args.x, args.y, args.z)
			--if square then
				--sendServerCommand("MDO", "PlaySoundForAll", args)
			--end
		--end

        if command == "RemoveLampPost" and args then
            local square = getCell():getGridSquare(args.x, args.y, args.z)
            if square then
                local lightSource = getCell():getLightSourceAt(args.x, args.y, args.z)
                if lightSource then
                    getCell():removeLamppost(lightSource)
                end
            end
        end

        if command == "AddWorldItems" and args and args.items then
            local square = getCell():getGridSquare(args.x, args.y, args.z)
            if square and type(args.items) == "table" then
                for _, item in ipairs(args.items) do
                    if type(item) == "string" and item ~= "" then
                        square:AddWorldInventoryItem(item, 0, 0, 0)
                    end
                end
            end
        end
    end
end

Events.OnClientCommand.Add(onClientCommand)

-- ------------------------------------------------------------------------------------------------