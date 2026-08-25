local NPC_DialogueMigration = {}

local NPC_DialogueSessionManager = require("DialogueFramework/Dialogue/NPC_DialogueSessionManager")

local migrationCompleted = {}

local function onGameStart()
    local player = getPlayer()
    if not player then
        return
    end

    local playerID = player:getOnlineID()
    if migrationCompleted[playerID] then
        return
    end

    local success, error = pcall(function()
        NPC_DialogueSessionManager.migrateOldSaveData(player)
    end)

    if success then
        migrationCompleted[playerID] = true
    end
end

Events.OnGameStart.Add(onGameStart)

return NPC_DialogueMigration
