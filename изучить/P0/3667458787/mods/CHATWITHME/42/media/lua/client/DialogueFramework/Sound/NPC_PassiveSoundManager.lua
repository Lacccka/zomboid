local NPC_PassiveSoundManager = {}

local NPC_IdleSoundSystem = require("DialogueFramework/Sound/NPC_IdleSoundSystem")
local NPC_ProximityDetector = require("DialogueFramework/Proximity/NPC_ProximityDetector")

local initialized = false

local function onTick()
    NPC_IdleSoundSystem.update()
    NPC_ProximityDetector.checkProximity()
end

function NPC_PassiveSoundManager.initialize()
    if initialized then
        return
    end

    Events.OnTick.Add(onTick)

    local NPC_IdleTextManager = require("DialogueFramework/UI/NPC_IdleTextManager")
    NPC_IdleTextManager.initialize()

    initialized = true
end

function NPC_PassiveSoundManager.shutdown()
    if not initialized then
        return
    end

    Events.OnTick.Remove(onTick)

    NPC_IdleSoundSystem.clearAll()
    NPC_ProximityDetector.clearAll()

    local NPC_IdleTextManager = require("DialogueFramework/UI/NPC_IdleTextManager")
    NPC_IdleTextManager.shutdown()

    initialized = false
end

return NPC_PassiveSoundManager
