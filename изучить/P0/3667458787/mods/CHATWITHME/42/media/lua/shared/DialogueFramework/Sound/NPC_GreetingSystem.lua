local NPC_GreetingSystem = {}

local NPC_SoundEmitter = require("DialogueFramework/Sound/NPC_SoundEmitter")

local registeredNPCs = {}
local greetingCooldowns = {}

local MIN_GREETING_COOLDOWN_HOURS = 1.0
local MAX_GREETING_COOLDOWN_HOURS = 3.0

function NPC_GreetingSystem.registerNPC(npc, greetingSound)
    if not npc or not greetingSound then
        return false
    end

    local npcID = tostring(npc)

    registeredNPCs[npcID] = {
        npc = npc,
        greetingSound = greetingSound,
        lastGreetingWorldAge = nil
    }

    NPC_GreetingSystem.loadGreetingCooldown(npcID)

    return true
end

function NPC_GreetingSystem.unregisterNPC(npc)
    if not npc then
        return
    end

    local npcID = tostring(npc)
    registeredNPCs[npcID] = nil
end

function NPC_GreetingSystem.loadGreetingCooldown(npcID)
    local player = getPlayer()
    if not player then
        return
    end

    local modData = player:getModData()
    if not modData.NPC_GreetingCooldowns then
        modData.NPC_GreetingCooldowns = {}
    end

    local cooldownData = modData.NPC_GreetingCooldowns[npcID]
    if cooldownData then
        greetingCooldowns[npcID] = cooldownData
    end
end

function NPC_GreetingSystem.saveGreetingCooldown(npcID, worldAge)
    local player = getPlayer()
    if not player then
        return
    end

    local modData = player:getModData()
    if not modData.NPC_GreetingCooldowns then
        modData.NPC_GreetingCooldowns = {}
    end

    modData.NPC_GreetingCooldowns[npcID] = worldAge
    greetingCooldowns[npcID] = worldAge
end

function NPC_GreetingSystem.canPlayGreeting(npcID)
    local lastGreeting = greetingCooldowns[npcID]
    if not lastGreeting then
        return true
    end

    local currentWorldAge = getGameTime():getWorldAgeHours()
    local timeSinceLastGreeting = currentWorldAge - lastGreeting

    local randomCooldown = MIN_GREETING_COOLDOWN_HOURS + (ZombRand(1000) / 1000.0) * (MAX_GREETING_COOLDOWN_HOURS - MIN_GREETING_COOLDOWN_HOURS)

    return timeSinceLastGreeting >= randomCooldown
end

function NPC_GreetingSystem.playGreeting(npc)
    if not npc then
        return false
    end

    local npcID = tostring(npc)
    local npcData = registeredNPCs[npcID]

    if not npcData then
        return false
    end

    if not NPC_GreetingSystem.canPlayGreeting(npcID) then
        return false
    end

    local soundHandle = NPC_SoundEmitter.playVocals(npc, npcData.greetingSound)

    if soundHandle then
        local currentWorldAge = getGameTime():getWorldAgeHours()
        NPC_GreetingSystem.saveGreetingCooldown(npcID, currentWorldAge)
        return true
    end

    return false
end

function NPC_GreetingSystem.clearAll()
    registeredNPCs = {}
    greetingCooldowns = {}
end

return NPC_GreetingSystem
