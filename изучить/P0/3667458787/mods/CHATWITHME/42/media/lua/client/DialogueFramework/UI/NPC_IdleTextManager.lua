local NPC_IdleTextManager = {}

local NPC_IdleTextUI = require("DialogueFramework/UI/NPC_IdleTextUI")
local NPC_DialogueEngine = require("DialogueFramework/Dialogue/NPC_DialogueEngine")

local activeIdleUI = nil
local activeIdleSoundHandle = nil

local function onNPCIdleTriggered(npc, npcIDString, idleKey)
    if not npc or not npcIDString or not idleKey then
        return
    end

    local player = getPlayer()
    if not player then
        return
    end

    if NPC_DialogueEngine.hasActiveSession(player) then
        return
    end

    if activeIdleUI and activeIdleUI.fadeState ~= "COMPLETE" then
        return
    end

    local success, NPC_IdleDefinitions = pcall(require, "NPCSystem/Idle/NPC_IdleDefinitions")
    if not success or not NPC_IdleDefinitions then
        return
    end

    local idleTextDef = NPC_IdleDefinitions.getIdleText(idleKey)
    if not idleTextDef or not idleTextDef.npcText then
        return
    end

    if idleTextDef.condition then
        if not idleTextDef.condition(player, npc) then
            return
        end
    end

    if idleTextDef.soundFile then
        local NPC_SoundEmitter = require("DialogueFramework/Sound/NPC_SoundEmitter")
        activeIdleSoundHandle = NPC_SoundEmitter.playVocals(npc, idleTextDef.soundFile)
    end

    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()

    local uiWidth = 400
    local uiHeight = 100
    local uiX = (screenWidth - uiWidth) / 2
    local uiY = screenHeight - uiHeight - 150

    activeIdleUI = NPC_IdleTextUI:new(
        uiX,
        uiY,
        uiWidth,
        uiHeight,
        idleTextDef.npcText,
        idleTextDef.displayDuration,
        idleTextDef.fadeInDuration,
        idleTextDef.fadeOutDuration
    )

    activeIdleUI:initialise()
    activeIdleUI:addToUIManager()

    if idleTextDef.onPlayed then
        idleTextDef.onPlayed(player, npc)
    end
end

local function onDialogueStarted(npc, player, session)
    if npc and player then
        local success, NPC_MuggyIdleConditions = pcall(require, "NPCSystem/Conditions/NPC_MuggyIdleConditions")
        if success and NPC_MuggyIdleConditions then
            local currentWorldAge = getGameTime():getWorldAgeHours()
            NPC_MuggyIdleConditions.setLastSeenTime(player, currentWorldAge)
        end
    end

    if activeIdleSoundHandle and npc then
        local NPC_SoundEmitter = require("DialogueFramework/Sound/NPC_SoundEmitter")
        NPC_SoundEmitter.stopSound(npc, activeIdleSoundHandle)
        activeIdleSoundHandle = nil
    end

    if activeIdleUI then
        activeIdleUI:removeFromUIManager()
        activeIdleUI = nil
    end
end

function NPC_IdleTextManager.initialize()
    Events.OnNPCIdleTriggered.Add(onNPCIdleTriggered)
    Events.OnNPCDialogueStarted.Add(onDialogueStarted)
end

function NPC_IdleTextManager.shutdown()
    Events.OnNPCIdleTriggered.Remove(onNPCIdleTriggered)
    Events.OnNPCDialogueStarted.Remove(onDialogueStarted)

    if activeIdleUI then
        activeIdleUI:removeFromUIManager()
        activeIdleUI = nil
    end

    activeIdleSoundHandle = nil
end

return NPC_IdleTextManager
