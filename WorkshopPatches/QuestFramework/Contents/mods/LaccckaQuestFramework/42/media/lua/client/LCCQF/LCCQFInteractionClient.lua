require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFNPCRegistry"
require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/Content/LCCQFNPCDefinitions"
require "LCCQF/Runtime/LCCQFBanditsRuntime"
require "LCCQF/UI/LCCQFDialoguePanel"

LCCQFInteractionClient = LCCQFInteractionClient or {}

local C = LCCQF.Constants
local state = {
    target = nil,
    tick = 0,
    lastRequestMs = 0,
    statusText = nil,
    statusUntilMs = 0,
}

local function log(message)
    print(C.LOG_PREFIX .. "[CLIENT] " .. tostring(message))
end

local function setStatus(message, durationMs)
    state.statusText = tostring(message or "")
    state.statusUntilMs = getTimestampMs() + (durationMs or 3500)
end

local function scanNearestQuestNPC()
    local player = getSpecificPlayer(0)
    state.target = LCCQF.NPCRuntime.FindNearestInteractive(player, C.INTERACTION_RANGE)
end

local function onTick()
    state.tick = state.tick + 1
    if state.tick >= 10 then
        state.tick = 0
        scanNearestQuestNPC()
    end
end

local function requestDialogue()
    local player = getSpecificPlayer(0)
    local target = state.target
    if not player or not target then return end

    local now = getTimestampMs()
    if now - state.lastRequestMs < 500 then return end
    state.lastRequestMs = now

    sendClientCommand(player, C.MODULE, C.COMMAND.REQUEST_DIALOGUE, {
        npcId = target.npcId,
        runtimeId = tostring(target.runtimeId),
    })
end

local function onKeyPressed(key)
    if key ~= Keyboard.KEY_E then return end
    if not state.target or LCCQFDialoguePanel.instance then return end
    requestDialogue()
end

local function onPostUIDraw()
    local now = getTimestampMs()
    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()
    local textManager = getTextManager()

    if state.target and not LCCQFDialoguePanel.instance then
        local prompt = "[E] Поговорить — " .. tostring(state.target.displayName)
        textManager:DrawStringCentre(UIFont.Medium, screenWidth / 2, screenHeight - 150, prompt, 1, 1, 1, 1)
    end

    if state.statusText and now <= state.statusUntilMs then
        textManager:DrawStringCentre(UIFont.Small, screenWidth / 2, screenHeight - 118, state.statusText, 0.9, 0.9, 0.9, 1)
    elseif state.statusText then
        state.statusText = nil
    end
end

local function isPrivilegedClient(player)
    if isDebugEnabled and isDebugEnabled() then return true end
    if not player or not player.getAccessLevel then return false end

    local access = tostring(player:getAccessLevel() or ""):lower()
    return access ~= "" and access ~= "none"
end

local function spawnTestNPC(player)
    if not player then return end
    sendClientCommand(player, C.MODULE, C.COMMAND.SPAWN_TEST_NPC, {})
end

local function onFillWorldObjectContextMenu(playerNum, context, worldobjects, test)
    if test then return end
    local player = getSpecificPlayer(playerNum)
    if not isPrivilegedClient(player) then return end
    context:addOption("[Quest Framework] Создать тестового NPC", player, spawnTestNPC)
end

local function sendChoice(sessionId, choiceId)
    local player = getSpecificPlayer(0)
    if not player then return end
    sendClientCommand(player, C.MODULE, C.COMMAND.CHOOSE_DIALOGUE, {
        sessionId = tostring(sessionId),
        choiceId = tostring(choiceId),
    })
end

local function sendClose(sessionId)
    local player = getSpecificPlayer(0)
    if not player then return end
    sendClientCommand(player, C.MODULE, C.COMMAND.CLOSE_DIALOGUE, {
        sessionId = tostring(sessionId),
    })
end

local function onServerCommand(module, command, args)
    if module ~= C.MODULE then return end
    args = args or {}

    if command == C.COMMAND.DIALOGUE_STATE then
        local npcId = type(args.npcId) == "string" and args.npcId or nil
        if not npcId or not LCCQF.NPCRegistry.IsRegistered(npcId) then
            setStatus("Сервер прислал неизвестного NPC.")
            return
        end
        LCCQFDialoguePanel.open(args, sendChoice, sendClose)
        log("dialogue state session=" .. tostring(args.sessionId) .. " node=" .. tostring(args.nodeId))
        return
    end

    if command == C.COMMAND.DIALOGUE_CLOSED then
        local panel = LCCQFDialoguePanel.instance
        if panel and tostring(panel.sessionId) == tostring(args.sessionId) then
            panel:close(false)
        end
        return
    end

    if command == C.COMMAND.STATUS then
        setStatus(args.message or "Quest Framework")
        log(args.message or "status")
    end
end

local function onGameStart()
    log("loaded version=" .. tostring(C.VERSION) .. " runtime=Bandits interactKey=E range=" .. tostring(C.INTERACTION_RANGE))
end

Events.OnGameStart.Add(onGameStart)
Events.OnTick.Add(onTick)
Events.OnKeyPressed.Add(onKeyPressed)
Events.OnPostUIDraw.Add(onPostUIDraw)
Events.OnServerCommand.Add(onServerCommand)
Events.OnFillWorldObjectContextMenu.Add(onFillWorldObjectContextMenu)

return LCCQFInteractionClient
