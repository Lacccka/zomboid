local NPC_SoundManager = {}

local NPC_SoundEmitter = require("DialogueFramework/Sound/NPC_SoundEmitter")

local activeSounds = {}

function NPC_SoundManager.playDialogueSound(npc, soundName, sessionID)
    if not npc or not soundName then
        return nil
    end

    if activeSounds[sessionID] and activeSounds[sessionID].currentHandle then
        NPC_SoundEmitter.stopSound(npc, activeSounds[sessionID].currentHandle)
    end

    local soundHandle = NPC_SoundEmitter.playVocals(npc, soundName)

    if soundHandle then
        if not activeSounds[sessionID] then
            activeSounds[sessionID] = {}
        end

        activeSounds[sessionID].currentHandle = soundHandle
        activeSounds[sessionID].currentSound = soundName
        activeSounds[sessionID].startTime = getTimestampMs() / 1000.0
    end

    return soundHandle
end

function NPC_SoundManager.stopDialogueSound(npc, sessionID)
    if not sessionID or not activeSounds[sessionID] then
        return
    end

    local soundHandle = activeSounds[sessionID].currentHandle

    if soundHandle then
        NPC_SoundEmitter.stopSound(npc, soundHandle)
    end

    activeSounds[sessionID] = nil
end

function NPC_SoundManager.isDialoguePlaying(npc, sessionID)
    if not sessionID or not activeSounds[sessionID] then
        return false
    end

    local soundHandle = activeSounds[sessionID].currentHandle

    if not soundHandle then
        return false
    end

    local isPlaying = NPC_SoundEmitter.isPlaying(npc, soundHandle)

    if not isPlaying then
        activeSounds[sessionID] = nil
    end

    return isPlaying
end

function NPC_SoundManager.getCurrentSound(sessionID)
    if not sessionID or not activeSounds[sessionID] then
        return nil
    end

    return activeSounds[sessionID].currentSound
end

function NPC_SoundManager.cleanupSession(sessionID)
    if not sessionID then
        return
    end

    activeSounds[sessionID] = nil
end

return NPC_SoundManager
