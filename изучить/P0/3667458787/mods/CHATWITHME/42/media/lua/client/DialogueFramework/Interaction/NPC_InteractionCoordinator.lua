local NPC_InteractionCoordinator = {}

local NPC_InteractionScanner = require("DialogueFramework/Interaction/NPC_InteractionScanner")

local NPC_ApproachTimedAction
local interactionState = {
    active = false,
    approaching = false
}

local function getPlayer()
    return getSpecificPlayer(0)
end

local function canInitiateInteraction(player)
    if not player then
        return false
    end

    if player:isDead() then
        return false
    end

    if player:isAsleep() then
        return false
    end

    if interactionState.active or interactionState.approaching then
        return false
    end

    return true
end

local function startApproach(player, npc)
    if not NPC_ApproachTimedAction then
        NPC_ApproachTimedAction = require("DialogueFramework/Interaction/NPC_ApproachTimedAction")
    end

    interactionState.approaching = true

    local action = NPC_ApproachTimedAction:new(player, npc)
    ISTimedActionQueue.add(action)
end

local function onKeyPressed(key)
    if key ~= Keyboard.KEY_E then
        return
    end

    local player = getPlayer()
    if not canInitiateInteraction(player) then
        return
    end

    if not NPC_InteractionScanner.canScanNow() then
        return
    end

    NPC_InteractionScanner.updateScanTime()

    local npc, distance = NPC_InteractionScanner.scanForNPC(player)

    if not npc then
        return
    end

    startApproach(player, npc)
end

function NPC_InteractionCoordinator.setInteractionActive(active)
    interactionState.active = active
end

function NPC_InteractionCoordinator.setApproaching(approaching)
    interactionState.approaching = approaching
end

function NPC_InteractionCoordinator.isInInteraction()
    return interactionState.active
end

function NPC_InteractionCoordinator.isApproaching()
    return interactionState.approaching
end

function NPC_InteractionCoordinator.initialize()
    if NPC_InteractionCoordinator.initialized then
        return
    end

    Events.OnKeyPressed.Add(onKeyPressed)

    NPC_InteractionCoordinator.initialized = true
end

return NPC_InteractionCoordinator
