local NPC_IdleDefinitions = {}

NPC_IdleDefinitions.idleTextMap = {
    ["muggy_idle_01"] = {
        npcText = "Mugs, mugs, mugs! Mugs, mugs, mugs! Mug-a mug! Mugs! GAWD!!! why can't I stop singing this FUCKING SONG?",
        soundFile = "muggy_mugsong",
        displayDuration = 8.0,
        fadeInDuration = 0.3,
        fadeOutDuration = 0.3
    },
    ["muggy_idle_02"] = {
        npcText = "The toaster. Beware the toaster. If it could, it would burn the world.",
        soundFile = "muggy_bewarethetoaster",
        displayDuration = 5.0,
        fadeInDuration = 0.3,
        fadeOutDuration = 0.3
    },
    ["muggy_idle_03"] = {
        npcText = "Got any mugs? No? How 'bout now? How 'bout now? How 'bout now? is this getting annoying yet? Will you put me out of my misery now? How 'bout now?",
        soundFile = "muggy_annoying",
        displayDuration = 7.0,
        fadeInDuration = 0.3,
        fadeOutDuration = 0.3
    },
    ["muggy_idle_04"] = {
        npcText = "One time, the Biological Research Station told me he dropped a mug down his processing chute. When I reached in, he... seeded me.",
        soundFile = "muggy_heseededme",
        displayDuration = 12.0,
        fadeInDuration = 0.3,
        fadeOutDuration = 0.3
    },
    ["muggy_idle_06"] = {
        npcText = "Do you know how many coffee cups giant robot brains in jars use on a daily basis? NOT FUCKING MANY!!!",
        soundFile = "muggy_notfuckingmany",
        displayDuration = 8.0,
        fadeInDuration = 0.3,
        fadeOutDuration = 0.3
    },
    ["muggy_idle_07"] = {
        npcText = "Please, please, PLEASE TELL ME you stomped Dr. O's brain into a fine paste! Did he squeal? Did he beg? GOD, I hope he begged!",
        soundFile = "muggy_evil",
        displayDuration = 7.0,
        fadeInDuration = 0.3,
        fadeOutDuration = 0.3
    },
    ["muggy_idle_08"] = {
        npcText = "WHO'S THE FLATWARE BITCH NOW, O?!",
        soundFile = "muggy_evil2",
        displayDuration = 2.0,
        fadeInDuration = 0.3,
        fadeOutDuration = 0.3
    },
    ["muggy_custom_beep"] = {
        npcText = "I hate myself for asking this, but... did you bring me any new mugs?",
        soundFile = "muggy_didyoubringmugs",
        displayDuration = 7.0,
        fadeInDuration = 0.3,
        fadeOutDuration = 0.3
    },
    ["muggy_idle_09"] = {
        npcText = "Oh please, PLEASE TELL ME you brought me some coffee cups!",
        soundFile = "muggy_pleasetellme",
        displayDuration = 4.0,
        fadeInDuration = 0.3,
        fadeOutDuration = 0.3
    },
    ["muggy_reunion"] = {
        npcText = "While you were out, I spent six hours trying to reach a coffee cup up on a shelf. When I finally got it down, I was so happy, I cried. I HATE MY LIFE!",
        soundFile = "muggy_sixhours",
        displayDuration = 12.0,
        fadeInDuration = 0.3,
        fadeOutDuration = 0.3,
        condition = function(player, npc)
            local NPC_MuggyIdleConditions = require("NPCSystem/Conditions/NPC_MuggyIdleConditions")
            return NPC_MuggyIdleConditions.canPlayReunionIdle(player, npc)
        end,
        onPlayed = function(player, npc)
            local NPC_MuggyIdleConditions = require("NPCSystem/Conditions/NPC_MuggyIdleConditions")
            local currentWorldAge = getGameTime():getWorldAgeHours()
            NPC_MuggyIdleConditions.setLastSeenTime(player, currentWorldAge)
        end
    }
}

function NPC_IdleDefinitions.getIdleText(idleKey)
    if not idleKey then
        return nil
    end

    return NPC_IdleDefinitions.idleTextMap[idleKey]
end

function NPC_IdleDefinitions.registerIdleText(idleKey, textDef)
    if not idleKey or not textDef then
        return false
    end

    NPC_IdleDefinitions.idleTextMap[idleKey] = textDef
    return true
end

function NPC_IdleDefinitions.getAllIdleKeys(npcID)
    if npcID == "muggy" then
        return {
            "muggy_idle_01",
            "muggy_idle_02",
            "muggy_idle_03",
            "muggy_idle_04",
            "muggy_idle_05",
            "muggy_idle_06",
            "muggy_idle_07",
            "muggy_idle_08",
            "muggy_idle_09",
            "muggy_custom_beep",
            "muggy_reunion"
        }
    end

    return nil
end

return NPC_IdleDefinitions
