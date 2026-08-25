local NPC_SoundEmitter = {}

function NPC_SoundEmitter.playVocals(animal, soundName)
    if not animal or not animal:isExistInTheWorld() then
        return nil
    end

    local emitter = animal:getEmitter()
    if not emitter then
        return nil
    end

    return emitter:playVocals(soundName)
end

function NPC_SoundEmitter.isPlaying(animal, soundHandle)
    if not animal or not soundHandle then
        return false
    end

    local emitter = animal:getEmitter()
    if not emitter then
        return false
    end

    return emitter:isPlaying(soundHandle)
end

function NPC_SoundEmitter.stopSound(animal, soundHandle)
    if not animal or not soundHandle then
        return
    end

    local emitter = animal:getEmitter()
    if emitter then
        emitter:stopSound(soundHandle)
    end
end

function NPC_SoundEmitter.stopAllSounds(animal)
    if not animal then
        return
    end

    local emitter = animal:getEmitter()
    if emitter then
        emitter:stopAll()
    end
end

function NPC_SoundEmitter.setVolume(animal, soundHandle, volume)
    if not animal or not soundHandle or not volume then
        return
    end

    local emitter = animal:getEmitter()
    if emitter then
        emitter:setVolume(soundHandle, volume)
    end
end

return NPC_SoundEmitter
