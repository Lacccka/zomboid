-- From "More Damaged Objects [B41]" mod -- Author = carlesturo

-- ------------------------------------------------------------------------------------------------

-- **************** MORE DAMAGED OBJECTS - SERVER COMMANDS ****************

--[[local function onServerCommand(module, command, args)
    if module == "MDO" and command == "PlaySoundForAll" and args and args.sound then
        local square = getCell():getGridSquare(args.x, args.y, args.z)
        if square then
            local emitter = getWorld():getFreeEmitter(args.x, args.y, args.z)
            emitter:playSound(args.sound)
            emitter:setVolumeAll(args.volume)
        end
    end
end

Events.OnServerCommand.Add(onServerCommand)--]]

-- ------------------------------------------------------------------------------------------------