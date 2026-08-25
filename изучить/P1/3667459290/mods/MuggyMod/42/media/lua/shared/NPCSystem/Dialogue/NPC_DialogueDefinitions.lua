local NPC_DialogueDefinitions = {}

local NPC_DialogueConditions = require("DialogueFramework/Dialogue/NPC_DialogueConditions")
local NPC_DialogueActions = require("DialogueFramework/Dialogue/NPC_DialogueActions")
local NPC_MuggyConditions = require("NPCSystem/Conditions/NPC_MuggyConditions")


NPC_DialogueDefinitions.sessions = {}

NPC_DialogueDefinitions.sessions.muggy_greeting_first = {
    sessionID = "muggy_greeting_first",
    npcID = "muggy",
    displayName = "Muggy",

    greetingSound = "Muggy_Greeting_First",

    resumable = true,

    rootNode = "greeting_response_1",

    nodes = {

        greeting_response_1 = {
            nodeType = "npc_response",
            npcText = "You! Hey, you! Yeah you! Got any mugs?",
            soundFile = "muggy_gotanymugs",
            nextNode = "initial_greeting_main",
            displayDuration = 4.0
        },

        initial_greeting_main = {
            -- npcText = "Well?",
            soundFile = "Muggy_Greeting_First",

            options = {
                {
                    id = "here_take1",
                    playerText = "Here, take whatever you want.",
                    nextNode = "here_take1",
                    condition = NPC_MuggyConditions.yes_mugs
                },
                {
                    id = "here_take2",
                    playerText = "Sorry, I don't have any.",
                    nextNode = "here_take2",
                    condition = NPC_MuggyConditions.no_mugs
                },
                {
                    id = "ask_whymugs1",
                    playerText = "Mugs? Why do you want mugs?",
                    nextNode = "intro_self1_response_1",
                    condition = nil
                },
                {
                    id = "who_areyou1",
                    playerText = "Who are you?",
                    nextNode = "backstory1_response_1",
                    condition = nil
                },
                {
                    id = "goodbye1",
                    playerText = "Got to go.",
                    nextNode = "goodbye1",
                    condition = nil
                }
            }
        },

        here_take2 = {
            nodeType = "npc_response",
            npcText = "Of course you don't. Why would you? You're not an INSANE robot obsessed with coffee cups. To you they're just worthless junk.",
            soundFile = "muggy_cupstoyou",
            displayDuration = 9.0,
            completesSession = true
        },

        goodbye1 = {
            nodeType = "npc_response",
            npcText = "Sure. Nobody wants to hang out with Muggy. I get it. So long, pal.",
            soundFile = "muggy_goodbye",
            displayDuration = 6.0,
            completesSession = true
        },

        intro_self1_response_1 = {
            nodeType = "npc_response",
            npcText = "Why do you want mugs? Huh? You some kind of sick mug hoarder? OH GOD, give me the coffee cup, please! It's sitting there in your pack. Taunting me!",
            soundFile = "muggy_yousomesickmughoarder",
            nextNode = "intro_self1_response_2",
            displayDuration = 7.0
        },

        intro_self1_response_2 = {
            nodeType = "npc_response",
            npcText = "Sorry.. I'm sorry. I got a little carried away. It's just all those GODDAMN DIRTY DISHES out there with no one to clean them! ... it breaks my heart!",
            soundFile = "muggy_dirtydishes",
            nextNode = "intro_self1_main",
            displayDuration = 10.0
        },

        intro_self1_main = {
            -- npcText = "...",
            soundFile = "Muggy_Greeting_First",

            options = {
                {
                    id = "mugs_obsessed1",
                    playerText = "You seem really obsessed with mugs.",
                    nextNode = "mugs_obsessed1_response_1",
                    condition = nil
                },
                {
                    id = "ask_cando1",
                    playerText = "Can you do anything else?",
                    nextNode = "ask_cando1_response_1",
                    condition = nil
                },
                {
                    id = "ask_whatdomugs1",
                    playerText = "What do you do with the mugs?",
                    nextNode = "ask_whatdomugs1_response_1",
                    condition = nil
                },
                {
                    id = "goodbye1",
                    playerText = "Got to go.",
                    nextNode = "goodbye1",
                    condition = nil
                }
            }
        },

        mugs_obsessed1_response_1 = {
            nodeType = "npc_response",
            npcText = "Of course I'm obsessed! They made this this way! You think I don't know how CRAZY I SOUND? Of course I do! They programmed me to know that, too!",
            soundFile = "muggy_theyprogrammedme",
            nextNode = "mugs_obsessed1_response_2",
            displayDuration = 11.0
        },

        mugs_obsessed1_response_2 = {
            nodeType = "npc_response",
            npcText = "They made me just to torture me. But you know, it's the neglect that hurts the most. \"Hey everybody, let's turn ourselves into robot brains in jars!\"",
            soundFile = "muggy_theymademetotortureme",
            nextNode = "mugs_obsessed1_response_3",
            displayDuration = 10.0
        },

        mugs_obsessed1_response_3 = {
            nodeType = "npc_response",
            npcText = "Do you know how many coffee cups giant robot brains in jars use on a daily basis..? NOT FUCKING MANY!!",
            soundFile = "muggy_notfuckingmany",
            nextNode = "mugs_obsessed1_main",
            displayDuration = 8.0
        },

        mugs_obsessed1_main = {
            -- npcText = "...",
            soundFile = "Muggy_Greeting_First",

            completesSession = false,

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
                NPC_DialogueActions.setModDataFlag(player, "muggy_met_first_time", true)
                NPC_DialogueActions.setModDataFlag(player, "muggy_heard_obsession", true)
            end,

            options = {
                {
                    id = "ask_whatdomugs1",
                    playerText = "What do you do with the mugs?",
                    nextNode = "ask_whatdomugs1_response_1",
                    condition = nil
                },
                {
                    id = "ask_cando1",
                    playerText = "Can you do anything else?",
                    nextNode = "ask_cando1_response_1",
                    condition = nil
                },
                {
                    id = "change_subject1",
                    playerText = "Let's change the subject.",
                    nextNode = "change_subject1",
                    condition = nil
                },
                {
                    id = "goodbye1",
                    playerText = "Got to go.",
                    nextNode = "goodbye1",
                    condition = nil
                }
            }
        },

        ask_whatdomugs1_response_1 = {
            nodeType = "npc_response",
            npcText = "I'm supposed to keep them clean - Oh, GOD, the thought of all those dirty dishes out there MAKES ME CRAZY!",
            soundFile = "muggy_keepthemclean",
            nextNode = "ask_whatdomugs1_response_2",
            displayDuration = 7.0
        },

        ask_whatdomugs1_response_2 = {
            nodeType = "npc_response",
            npcText = "Most of them are probably beyond saving now.. The only thing left is break them down and process them for raw materials. I guess you could have these.",
            soundFile = "muggy_beyondsaving",
            nextNode = "ask_whatdomugs1_main",
            displayDuration = 10.0
        },

        ask_whatdomugs1_main = {
            -- npcText = "...",
            soundFile = "Muggy_Greeting_First",

            completesSession = false,

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
                NPC_DialogueActions.setModDataFlag(player, "muggy_met_first_time", true)
                NPC_DialogueActions.setModDataFlag(player, "muggy_heard_whatdomugs", true)
            end,

            options = {
                {
                    id = "mugs_obsessed1",
                    playerText = "You seem really obsessed with mugs.",
                    nextNode = "mugs_obsessed1_response_1",
                    condition = nil
                },
                {
                    id = "ask_cando1",
                    playerText = "Can you do anything else?",
                    nextNode = "ask_cando1_response_1",
                    condition = nil
                },
                {
                    id = "change_subject1",
                    playerText = "Let's change the subject.",
                    nextNode = "change_subject1",
                    condition = nil
                },
                {
                    id = "goodbye1",
                    playerText = "Got to go.",
                    nextNode = "goodbye1",
                    condition = nil
                }
            }
        },

        ask_cando1_response_1 = {
            nodeType = "npc_response",
            npcText = "Anything else, you ask.. Like I don't long for the chance to be more than a neurotic busboy.",
            soundFile = "muggy_neuroticbussboy",
            nextNode = "ask_cando1_response_2",
            displayDuration = 5.0
        },

        ask_cando1_response_2 = {
            nodeType = "npc_response",
            npcText = "If you must know, at one point Dr. Mobius programmed me to manufacture electronic components in my central chassis.",
            soundFile = "muggy_centralchassis",
            nextNode = "ask_cando1_response_3",
            displayDuration = 8.0
        },

        ask_cando1_response_3 = {
            nodeType = "npc_response",
            npcText = "That module got corroded when the Toaster spilled hot crumbs down my vents. though. If you find a back-up somewhere. I could maybe do that for you.",
            soundFile = "muggy_findmymodule",
            nextNode = "ask_cando1_main",
            displayDuration = 7.0
        },

        ask_cando1_main = {
            -- npcText = "...",
            soundFile = "Muggy_Greeting_First",

            completesSession = false,

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
                NPC_DialogueActions.setModDataFlag(player, "muggy_met_first_time", true)
                NPC_DialogueActions.setModDataFlag(player, "muggy_heard_cando", true)
            end,

            options = {
                {
                    id = "mugs_obsessed1",
                    playerText = "You seem really obsessed with mugs.",
                    nextNode = "mugs_obsessed1_response_1",
                    condition = nil
                },
                {
                    id = "ask_cando1",
                    playerText = "Can you do anything else?",
                    nextNode = "ask_cando1_response_1",
                    condition = nil
                },
                {
                    id = "change_subject1",
                    playerText = "Let's change the subject.",
                    nextNode = "change_subject1",
                    condition = nil
                },
                {
                    id = "goodbye1",
                    playerText = "Got to go.",
                    nextNode = "goodbye1",
                    condition = nil
                }
            }
        },



        change_subject1 = {
            nodeType = "npc_response",
            npcText = "Is the new subject mugs?",
            soundFile = "muggy_newsubjectmugs",
            nextNode = "change_subject2",
            displayDuration = 3.0,
            completesSession = false,
        },

        change_subject2 = {
            -- npcText = "Is the new subject mugs?",
            soundFile = "Muggy_Greeting_First",

            completesSession = false,

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
                NPC_DialogueActions.setModDataFlag(player, "muggy_met_first_time", true)
                NPC_DialogueActions.setModDataFlag(player, "muggy_heard_changesubject", true)
            end,

            options = {
                {
                    id = "here_take1",
                    playerText = "Here, take whatever you want.",
                    nextNode = "here_take1",
                    condition = NPC_MuggyConditions.yes_mugs
                },
                {
                    id = "here_take2",
                    playerText = "Sorry, I don't have any.",
                    nextNode = "here_take1",
                    condition = NPC_MuggyConditions.no_mugs
                },
                {
                    id = "ask_whymugs1",
                    playerText = "Mugs? Why do you want mugs?",
                    nextNode = "intro_self1_response_1",
                    condition = nil
                },
                {
                    id = "who_areyou1",
                    playerText = "Who are you?",
                    nextNode = "backstory1_response_1",
                    condition = nil
                },
                {
                    id = "goodbye1",
                    playerText = "Got to go.",
                    nextNode = "goodbye1",
                    condition = nil
                }
            }
        },

        backstory1_response_1 = {
            nodeType = "npc_response",
            npcText = "You... you really want to know about... me? No one ever asks about Muggy! You've made me SO HAPPY!",
            soundFile = "muggy_youwannaknow",
            nextNode = "backstory1_response_2",
            displayDuration = 11.0
        },

        backstory1_response_2 = {
            nodeType = "npc_response",
            npcText = "Maybe you've seen some of those big, imposing Securitrons with their lovely laser guns and rocket launchers and scary faces? I'm not one of those.",
            soundFile = "muggy_imnotabigsecuritron",
            nextNode = "backstory1_response_3",
            displayDuration = 10.0
        },

        backstory1_response_3 = {
            nodeType = "npc_response",
            npcText = "Dr. O was always jealous of House Industries, and he thought it would be fucking hilarious to build a tiny neurotic Securitron. Big fucking laugh.",
            soundFile = "muggy_bigfuckinglaugh",
            nextNode = "backstory1_response_4",
            displayDuration = 11.0
        },

        backstory1_response_4 = {
            nodeType = "npc_response",
            npcText = "So, umm... you got any coffee cups for me now?",
            soundFile = "muggy_soumgotanymug",
            nextNode = "backstory1_main",
            displayDuration = 4.0
        },

        backstory1_main = {
            -- npcText = "...",
            soundFile = "Muggy_Greeting_First",

            completesSession = true,

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
                NPC_DialogueActions.setModDataFlag(player, "muggy_met_first_time", true)
                NPC_DialogueActions.setModDataFlag(player, "muggy_heard_backstory", true)
            end,

            options = {
                {
                    id = "here_take1",
                    playerText = "Here, take whatever you want.",
                    nextNode = "here_take1",
                    condition = NPC_MuggyConditions.yes_mugs
                },
                {
                    id = "here_take2",
                    playerText = "Sorry, I don't have any.",
                    nextNode = "here_take1",
                    condition = NPC_MuggyConditions.no_mugs
                },
                {
                    id = "ask_whymugs1",
                    playerText = "Mugs? Why do you want mugs?",
                    nextNode = "intro_self1_response_1",
                    condition = nil
                },
                {
                    id = "who_areyou1",
                    playerText = "Who are you?",
                    nextNode = "backstory1_response_1",
                    condition = nil
                },
                {
                    id = "goodbye1",
                    playerText = "Got to go.",
                    nextNode = "goodbye1",
                    condition = nil
                }
            }
        },

        here_take1 = {
            npcText = "Oh, sweet, sweet fulfillment! I'll break these down for you just as fast as I can!",
            soundFile = "muggy_sweetfulfillment",
            nodeType = "npc_response",
            nextNode = "here_take1_trading",

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
                NPC_DialogueActions.setModDataFlag(player, "muggy_met_first_time", true)
                NPC_DialogueActions.setModDataFlag(player, "muggy_gave_mugs", true)
            end,
            options = {
            }
        },

        here_take1_trading = {
            nodeType = "npc_trading",
            options = {
            }
        },

    }
}



NPC_DialogueDefinitions.sessions.muggy_greeting_repeat = {
    sessionID = "muggy_greeting_repeat",
    npcID = "muggy",
    displayName = "Muggy",

    greetingSound = "Muggy_Greeting_Repeat",

    rootNode = "greeting_response_2",

    nodes = {

        greeting_response_2 = {
            nodeType = "npc_response",
            npcText = "Oh please, PLEASE tell me you brought me some coffee cups!",
            soundFile = "muggy_gotanymugs",
            nextNode = "greeting_again",
            displayDuration = 4.0
        },


        greeting_again = {

            soundFile = "Muggy_Greeting_Repeat",

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
            end,

            options = {
                {
                    id = "check_elect",
                    playerText = "Have you made any electronic components?",
                    nextNode = "made_elect",
                    nextNodeX = "module_notready",
                    conditionX = NPC_MuggyConditions.gift_on_cooldown,
                    conditionY = NPC_MuggyConditions.can_receive_daily_gift,
                    condition = NPC_MuggyConditions.has_module
                },
                {
                    id = "here_take1",
                    playerText = "Here, take whatever you want.",
                    nextNode = "here_take1",
                    condition = NPC_MuggyConditions.yes_mugs
                },
                {
                    id = "here_take2",
                    playerText = "Sorry, I don't have any.",
                    nextNode = "here_take1",
                    condition = NPC_MuggyConditions.no_mugs
                },
                {
                    id = "ask_whymugs1",
                    playerText = "Mugs? Why do you want mugs?",
                    nextNode = "intro_self1_response_1",
                    condition = nil
                },
                {
                    id = "who_areyou1",
                    playerText = "Who are you?",
                    nextNode = "backstory1_response_1",
                    condition = nil
                },
                {
                    id = "goodbye1",
                    playerText = "Got to go.",
                    nextNode = "goodbye1",
                    condition = nil
                }
            }
        },

        made_elect = {
            npcText = "Why yes! I've managed to produce some scrap electronics. Here, take this!",
            soundFile = "muggy_confirmtrade",
            nodeType = "npc_response",
            nextNode = "greeting_again",

            onEnter = function(player, npc, session)
                local NPC_GiftingEngine = require("DialogueFramework/Gifting/NPC_GiftingEngine")

                local canGift, reason = NPC_GiftingEngine.canStartGifting(player, "muggy")
                if not canGift then
                    return
                end

                local giftItems = NPC_GiftingEngine.getGiftItems("muggy")
                if not giftItems then
                    return
                end

                local NPC_ReceiveGiftTimedAction = require("DialogueFramework/TimedActions/NPC_ReceiveGiftTimedAction")
                local giftAction = NPC_ReceiveGiftTimedAction:new(player, npc, "muggy", giftItems)
                ISTimedActionQueue.add(giftAction)
            end
        },

        module_notready = {
            npcText = "Not yet! Give me some time. Come back in a day or so.",
            soundFile = "muggy_reject",
            nodeType = "npc_response",
            nextNode = "greeting_again"
        },
    }
}

NPC_DialogueDefinitions.sessions.muggy_foundmodule = {
    sessionID = "muggy_special_mug_quest",
    npcID = "muggy",
    displayName = "Muggy",

    requiredItem = "Base.MuggyHolotape",
    itemOverrideEnabled = true,
    overridePriority = 10,
    removeItemOnComplete = false,
    itemRemovedFlag = "muggy_found_module",

    sessionCondition = function(player, npc)
        return not NPC_DialogueConditions.hasModDataFlag(player, "muggy_found_module")
    end,

    rootNode = "muggy_foundmymodule1",
    nodes = {

        muggy_foundmymodule1 = {

            soundFile = "Muggy_Greeting_Repeat",

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
            end,

            options = {
                {
                    id = "foundyourmodule1",
                    playerText = "I found that upgrade disk you were looking for.",
                    nextNode = "foundmymodule1",
                    condition = nil
                },
            }
        },

        foundmymodule1 = {
            npcText = "Ho-ho, THANK GOD! Finally, something resembling a useful purpose!!",
            soundFile = "muggy_confirmtrade",
            nodeType = "npc_response",
            nextNode = "module_upgrade_info",

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")

                local NPC_RemoveQuestItemTimedAction = require("DialogueFramework/TimedActions/NPC_RemoveQuestItemTimedAction")
                local removeAction = NPC_RemoveQuestItemTimedAction:new(
                    player,
                    npc,
                    "Base.MuggyHolotape",
                    "muggy_found_module"
                )
                ISTimedActionQueue.add(removeAction)
            end
        },

        module_upgrade_info = {
            nodeType = "npc_info",
            headerText = "Parts Production:",
            bodyText = "Upgrade holotape received... upgrading 'Parts Production' functionality (supply of Scrap Electronics available once per day).",
        },
    },
}

function NPC_DialogueDefinitions.getSession(sessionID)
    return NPC_DialogueDefinitions.sessions[sessionID]
end

function NPC_DialogueDefinitions.getSessionForNPC(npcID, player)
    local hasMetBefore = NPC_DialogueConditions.hasMetNPCBefore(player, npcID)

    if npcID == "muggy" then
        if hasMetBefore then
            return NPC_DialogueDefinitions.sessions.muggy_greeting_repeat
        else
            return NPC_DialogueDefinitions.sessions.muggy_greeting_first
        end
    end

    return nil
end

function NPC_DialogueDefinitions.getAllSessionsForNPC(npcID)
    if npcID == "muggy" then
        return {
            NPC_DialogueDefinitions.sessions.muggy_greeting_first,
            NPC_DialogueDefinitions.sessions.muggy_greeting_repeat,
            NPC_DialogueDefinitions.sessions.muggy_foundmodule
        }
    end

    return {}
end

return NPC_DialogueDefinitions










--[[ ORPHANED SESSION - Nodes moved to muggy_greeting_first
NPC_DialogueDefinitions.sessions.initialgreetingmain = {


 initial_greeting1 = {
            npcText = "You! Hey, you! Yeah you! Got any mugs?",
            options = { ... }
        },
        rootNode = "greeting_response_1",
        greeting_response_1 = {
            nodeType = "npc_response",
            npcText = "You! Hey, you!",
            soundFile = "Muggy_Greeting_Part1",
            nextNode = "greeting_response_2",
            displayDuration = 3.0,
            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
                NPC_DialogueActions.setModDataFlag(player, "muggy_met_first_time", true)
            end
        },

        greeting_response_1 = {
            nodeType = "npc_response",
            npcText = "You! Hey, you!",
            soundFile = "Muggy_Greeting_First",
            nextNode = "greeting_response_2",
            displayDuration = 3.0
        },

        greeting_response_2 = {
            nodeType = "npc_response",
            npcText = "Yeah you!",
            soundFile = "Muggy_Greeting_First",
            nextNode = "greeting_response_3",
            displayDuration = 3.0
        },

        greeting_response_3 = {
            nodeType = "npc_response",
            npcText = "Got any mugs?",
            soundFile = "Muggy_Greeting_First",
            nextNode = "initial_greeting_main",
            displayDuration = 3.0
        },

        initial_greeting_main = {
            -- npcText = "Well?",
            soundFile = "Muggy_Greeting_First",

            options = {
                {
                    id = "here_take1",
                    playerText = "Here, take whatever you want.",
                    nextNode = "here_take1",
                    condition = nil
                },
                {
                    id = "ask_whymugs1",
                    playerText = "Mugs? Why do you want mugs?",
                    nextNode = "intro_self1_response_1",
                    condition = nil
                },
                {
                    id = "who_areyou1",
                    playerText = "Who are you?",
                    nextNode = "backstory1_response_1",
                    condition = nil
                },
                {
                    id = "goodbye",
                    playerText = "Got to go.",
                    nextNode = "EXIT",
                    condition = nil
                }
            }
        },





        intro_self1_response_1 = {
            nodeType = "npc_response",
            npcText = "Why do you want mugs? Huh? You some kind of sick mug hoarder? OH GOD, give me the coffe cup, please! It's sitting there in your pack. Taunting me!",
            soundFile = "Muggy_Greeting_First",
            nextNode = "intro_self1_response_2",
            displayDuration = 3.0
        },

        intro_self1_response_2 = {
            nodeType = "npc_response",
            npcText = "Sorry.. I'm sorry. I got a little carried away. It's just all those GODDAMN DIRTY DISHES out there with no one to clean them! .. it breaks my heart!",
            soundFile = "Muggy_Greeting_First",
            nextNode = "intro_self1_main",
            displayDuration = 3.0
        },

        intro_self1_main = {
            -- npcText = "...",
            soundFile = "Muggy_Greeting_First",

            options = {
                {
                    id = "mugs_obsessed1",
                    playerText = "You seem really obsessed with mugs.",
                    nextNode = "mugs_obsessed1_response_1",
                    condition = nil
                },
                {
                    id = "ask_cando1",
                    playerText = "Can you do anything else?",
                    nextNode = "ask_cando1_response_1",
                    condition = nil
                },
                {
                    id = "ask_whatdomugs1",
                    playerText = "What do you do with the mugs?",
                    nextNode = "ask_whatdomugs1_response_1",
                    condition = nil
                },
                {
                    id = "goodbye1",
                    playerText = "Got to go.",
                    nextNode = "EXIT",
                    condition = nil
                }
            }
        },



        mugs_obsessed1_response_1 = {
            nodeType = "npc_response",
            npcText = "Of course I'm obsessed! They made this this way! You think I don't know how CRAZY I SOUND? Of course I do! They programmed me to know that, too!",
            soundFile = "Muggy_Greeting_First",
            nextNode = "mugs_obsessed1_response_2",
            displayDuration = 3.0
        },

        mugs_obsessed1_response_2 = {
            nodeType = "npc_response",
            npcText = "They made me just to tortune me. But you know, it's the neglect that hurts the most. \"Hey everybody, let's turn ourselves into robot brains in jars!\"",
            soundFile = "Muggy_Greeting_First",
            nextNode = "mugs_obsessed1_response_3",
            displayDuration = 3.0
        },

        mugs_obsessed1_response_3 = {
            nodeType = "npc_response",
            npcText = "Do you know how many coffee cups giant robot brains in jars use on a daily basis..? NOT FUCKING MANY!!",
            soundFile = "Muggy_Greeting_First",
            nextNode = "mugs_obsessed1_main",
            displayDuration = 3.0
        },

        mugs_obsessed1_main = {
            -- npcText = "...",
            soundFile = "Muggy_Greeting_First",

            completesSession = true,

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
                NPC_DialogueActions.setModDataFlag(player, "muggy_met_first_time", true)
                NPC_DialogueActions.setModDataFlag(player, "muggy_heard_obsession", true)
            end,

            options = {
                {
                    id = "ask_whatdomugs1",
                    playerText = "What do you do with the mugs?",
                    nextNode = "EXIT",
                    condition = nil
                },
                {
                    id = "ask_cando1",
                    playerText = "Can you do anything else?",
                    nextNode = "EXIT",
                    condition = nil
                },
                {
                    id = "change_subject1",
                    playerText = "Let's change the subject.",
                    nextNode = "EXIT",
                    condition = nil
                }
            }
        },


        ask_whatdomugs1_response_1 = {
            nodeType = "npc_response",
            npcText = "I'm supposed to keep them clean - Oh, GOD, the thought of all those dirty dishes out there MAKES ME CRAZY!",
            soundFile = "Muggy_Greeting_First",
            nextNode = "ask_whatdomugs1_response_2",
            displayDuration = 3.0
        },

        ask_whatdomugs1_response_2 = {
            nodeType = "npc_response",
            npcText = "Most of them are probably beyond saving now.. The only thing left is break them down and process them for raw materials. I guess you could have these.",
            soundFile = "Muggy_Greeting_First",
            nextNode = "ask_whatdomugs1_main",
            displayDuration = 3.0
        },

        ask_whatdomugs1_main = {
            -- npcText = "...",
            soundFile = "Muggy_Greeting_First",

            completesSession = true,

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
                NPC_DialogueActions.setModDataFlag(player, "muggy_met_first_time", true)
                NPC_DialogueActions.setModDataFlag(player, "muggy_heard_whatdomugs", true)
            end,

            options = {
                {
                    id = "mugs_obsessed1",
                    playerText = "You seem really obsessed with mugs.",
                    nextNode = "EXIT",
                    condition = nil
                },
                {
                    id = "ask_cando1",
                    playerText = "Can you do anything else?",
                    nextNode = "EXIT",
                    condition = nil
                },
                {
                    id = "change_subject1",
                    playerText = "Let's change the subject.",
                    nextNode = "EXIT",
                    condition = nil
                }
            }
        },


        ask_cando1_response_1 = {
            nodeType = "npc_response",
            npcText = "Anything else, you ask.. Like I don't long for the chance to be more than a neurotic busboy.",
            soundFile = "Muggy_Greeting_First",
            nextNode = "ask_cando1_response_2",
            displayDuration = 3.0
        },

        ask_cando1_response_2 = {
            nodeType = "npc_response",
            npcText = "If you must know, at one point Dr. Mobius programmed me to manufacture electronic components in my central chassis.",
            soundFile = "Muggy_Greeting_First",
            nextNode = "ask_cando1_response_3",
            displayDuration = 3.0
        },

        ask_cando1_response_3 = {
            nodeType = "npc_response",
            npcText = "That module got corroded when the Toaster spilled hot crumbs down my vents. though. If you find a back-up somewhere. I could maybe do that for you.",
            soundFile = "Muggy_Greeting_First",
            nextNode = "ask_cando1_main",
            displayDuration = 3.0
        },

        ask_cando1_main = {
            -- npcText = "...",
            soundFile = "Muggy_Greeting_First",

            completesSession = true,

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
                NPC_DialogueActions.setModDataFlag(player, "muggy_met_first_time", true)
                NPC_DialogueActions.setModDataFlag(player, "muggy_heard_cando", true)
            end,

            options = {
                {
                    id = "mugs_obsessed1",
                    playerText = "You seem really obsessed with mugs.",
                    nextNode = "EXIT",
                    condition = nil
                },
                {
                    id = "ask_whatdomugs1",
                    playerText = "What do you do with the mugs?",
                    nextNode = "EXIT",
                    condition = nil
                },
                {
                    id = "change_subject1",
                    playerText = "Let's change the subject.",
                    nextNode = "EXIT",
                    condition = nil
                }
            }
        },

        change_subject1 = {
            -- npcText = "Is the new subject mugs?",
            soundFile = "Muggy_Greeting_First",

            completesSession = true,

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
                NPC_DialogueActions.setModDataFlag(player, "muggy_met_first_time", true)
                NPC_DialogueActions.setModDataFlag(player, "muggy_heard_changesubject", true)
            end,

            options = {
                {
                    id = "here_take1",
                    playerText = "Here, take whatever you want.",
                    nextNode = "here_take1",
                    condition = nil
                },
                {
                    id = "ask_whymugs1",
                    playerText = "Mugs? Why do you want mugs?",
                    nextNode = "intro_self1_response_1",
                    condition = nil
                },
                {
                    id = "who_areyou1",
                    playerText = "Who are you?",
                    nextNode = "backstory1_response_1",
                    condition = nil
                },
            }
        },


        --[[
        NOTE: The commented continuation lines below can be implemented as
        npc_response nodes. For example:

        backstory1 = {
            nodeType = "npc_response",
            npcText = "You... you really want to know about... me? No one ever asks about Muggy! You've made me SO HAPPY!",
            soundFile = "Muggy_Backstory_Part1",
            nextNode = "backstory2",
            displayDuration = 4.0,
            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
                NPC_DialogueActions.setModDataFlag(player, "muggy_met_first_time", true)
            end
        },

        backstory2 = {
            nodeType = "npc_response",
            npcText = "Maybe you've seen some of those big, imposing Securitrons with their lovely laser guns and rocket launchers and scary faces? I'm not one of those.",
            soundFile = "Muggy_Backstory_Part2",
            nextNode = "backstory3",
            displayDuration = 4.5
        },

        backstory3 = {
            nodeType = "npc_response",
            npcText = "Dr. O was always jealous of House Industries, and he thought it would be fucking hilarious to build a tiny neurotic Securitron. Big fucking laugh.",
            soundFile = "Muggy_Backstory_Part3",
            nextNode = "backstory4",
            displayDuration = 5.0
        },

        backstory4 = {
            nodeType = "npc_response",
            npcText = "So, umm... you got any coffee cups for me now?",
            soundFile = "Muggy_Backstory_Part4",
            nextNode = "backstory_main",
            displayDuration = 3.0
        },

        backstory_main = {
            npcText = "Well?",
            options = { ... }
        }

        This creates a complete backstory sequence that plays cinematically
        before giving the player options to respond.
        --]]
--[[
        backstory1_response_1 = {
            nodeType = "npc_response",
            npcText = "You... you really want to know about... me? No one ever asks about Muggy! You've made me SO HAPPY!",
            soundFile = "Muggy_Greeting_First",
            nextNode = "backstory1_response_2",
            displayDuration = 3.0
        },

        backstory1_response_2 = {
            nodeType = "npc_response",
            npcText = "Maybe you've seen some of those big, imposing Securitrons with their lovely laser guns and rocket launchers and scary faces? I'm not one of those.",
            soundFile = "Muggy_Greeting_First",
            nextNode = "backstory1_response_3",
            displayDuration = 3.0
        },

        backstory1_response_3 = {
            nodeType = "npc_response",
            npcText = "Dr. O was always jealous of House Industries, and he thought it would be fucking hilarious to build a tiny neurotic Securitron. Big fucking laugh.",
            soundFile = "Muggy_Greeting_First",
            nextNode = "backstory1_response_4",
            displayDuration = 3.0
        },

        backstory1_response_4 = {
            nodeType = "npc_response",
            npcText = "So, umm... you got any coffee cups for me now?",
            soundFile = "Muggy_Greeting_First",
            nextNode = "backstory1_main",
            displayDuration = 3.0
        },

        backstory1_main = {
            -- npcText = "...",
            soundFile = "Muggy_Greeting_First",

            completesSession = true,

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
                NPC_DialogueActions.setModDataFlag(player, "muggy_met_first_time", true)
                NPC_DialogueActions.setModDataFlag(player, "muggy_heard_backstory", true)
            end,

            options = {
                {
                    id = "here_take1",
                    playerText = "Here, take whatever you want.",
                    nextNode = "here_take1",
                    condition = nil
                },
                {
                    id = "ask_whymugs1",
                    playerText = "Mugs? Why do you want mugs?",
                    nextNode = "intro_self1_response_1",
                    condition = nil
                },
                {
                    id = "who_areyou1",
                    playerText = "Who are you?",
                    nextNode = "EXIT",
                    condition = nil
                },
            }
        },


        here_take1 = {
            -- npcText = "Oh, sweet, sweet fulfillment! I'll break these down for you just as fast as I can!",
            soundFile = "Muggy_Greeting_First",
            nodeType = "npc_response",
            nextNode = "here_take1_trading",

            onEnter = function(player, npc, session)
                NPC_DialogueActions.recordNPCMeeting(player, "muggy")
                NPC_DialogueActions.setModDataFlag(player, "muggy_met_first_time", true)
                NPC_DialogueActions.setModDataFlag(player, "muggy_gave_mugs", true)
            end,

            options = {
            }
        },

        here_take1_trading = {
            nodeType = "npc_trading",
            options = {
            }
        },


-- end of initial session 


--]]