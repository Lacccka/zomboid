require "LCCQF/LCCQFConstants"
require "LCCQF/Core/LCCQFNPCRegistry"
require "LCCQF/Core/LCCQFNPCRuntime"
require "LCCQF/Content/LCCQFNPCDefinitions"
require "LCCQF/UI/LCCQFDialoguePanel"

LCCQFInteractionClient = LCCQFInteractionClient or {}

local C = LCCQF.Constants
local TRANSLATION_PREFIX = "IGUI_LCCQF_"
local TARGET_SCAN_INTERVAL_MS = 100
local state = {
    target = nil,
    lastRequestMs = 0,
    statusText = nil,
    statusUntilMs = 0,
    loggedTargetRuntimeId = nil,
    bindingsSynchronized = false,
    nextBindingRequestMs = 0,
    nextTargetScanMs = 0,
    lastNoTargetDiagnosticMs = 0,
}

local function log(message)
    print(C.LOG_PREFIX .. "[CLIENT] " .. tostring(message))
end

local function setStatus(message, durationMs)
    state.statusText = tostring(message or "")
    state.statusUntilMs = getTimestampMs() + (durationMs or 3500)
end

local function updateLoggedTarget()
    local runtimeId = state.target and tostring(state.target.runtimeId) or nil
    if runtimeId == state.loggedTargetRuntimeId then return end

    if runtimeId then
        log("interaction target acquired npcId=" .. tostring(state.target.npcId)
            .. " runtimeId=" .. runtimeId
            .. " distance=" .. tostring(math.sqrt(state.target.distanceSq or 0)))
    elseif state.loggedTargetRuntimeId then
        log("interaction target lost runtimeId=" .. tostring(state.loggedTargetRuntimeId))
    end
    state.loggedTargetRuntimeId = runtimeId
end

local function refreshTarget(force)
    local now = getTimestampMs()
    if not force and now < state.nextTargetScanMs then return state.target end
    state.nextTargetScanMs = now + TARGET_SCAN_INTERVAL_MS

    local player = getSpecificPlayer(0)
    state.target = LCCQF.NPCRuntime.FindNearestInteractive(player, C.INTERACTION_RANGE)
    updateLoggedTarget()
    return state.target
end

local function logBindingDiagnostics(source)
    local player = getSpecificPlayer(0)
    if not player then
        log("binding diagnostic source=" .. tostring(source) .. " player=unavailable")
        return
    end

    local px, py, pz = player:getX(), player:getY(), player:getZ()
    local bindings = LCCQF.NPCRuntime.ExportRuntimeBindings()
    local inVehicle = player:getVehicle() ~= nil

    if #bindings == 0 then
        log("binding diagnostic source=" .. tostring(source)
            .. " count=0 vehicle=" .. tostring(inVehicle))
        return
    end

    for _, binding in ipairs(bindings) do
        local definition = LCCQF.NPCRegistry.Get(binding.npcId)
        local anchor = LCCQF.NPCRuntime.GetRuntimeAnchor(binding.runtimeId)

        if anchor then
            local dx = anchor.x - px
            local dy = anchor.y - py
            local distance = math.sqrt(dx * dx + dy * dy)
            local sameZ = math.abs(anchor.z - pz) < 0.5
            local registered = definition ~= nil
            local interactive = definition ~= nil and definition.interactive ~= false
            local eligible = registered and interactive and sameZ and distance <= C.INTERACTION_RANGE

            log("binding diagnostic source=" .. tostring(source)
                .. " npcId=" .. tostring(binding.npcId)
                .. " runtimeId=" .. tostring(binding.runtimeId)
                .. " anchor=" .. tostring(anchor.x) .. "," .. tostring(anchor.y) .. "," .. tostring(anchor.z)
                .. " player=" .. tostring(px) .. "," .. tostring(py) .. "," .. tostring(pz)
                .. " distance=" .. tostring(distance)
                .. " sameZ=" .. tostring(sameZ)
                .. " registered=" .. tostring(registered)
                .. " interactive=" .. tostring(interactive)
                .. " vehicle=" .. tostring(inVehicle)
                .. " eligible=" .. tostring(eligible))
        else
            log("binding diagnostic source=" .. tostring(source)
                .. " npcId=" .. tostring(binding.npcId)
                .. " runtimeId=" .. tostring(binding.runtimeId)
                .. " anchor=missing"
                .. " vehicle=" .. tostring(inVehicle))
        end
    end
end

local function requestRuntimeBindings()
    local player = getSpecificPlayer(0)
    if not player then return false end
    sendClientCommand(player, C.MODULE, C.COMMAND.REQUEST_RUNTIME_BINDINGS, {})
    return true
end

local function maintainRuntimeBindingSync()
    if state.bindingsSynchronized then return end

    local now = getTimestampMs()
    if now < state.nextBindingRequestMs then return end
    state.nextBindingRequestMs = now + 2000
    requestRuntimeBindings()
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
    if LCCQFDialoguePanel.instance then return end

    -- The key path reuses the same provider-neutral framework-anchor query used
    -- by the prompt. No Bandits client adapter or physical IsoZombie lookup is
    -- involved in deciding whether E may request a dialogue.
    refreshTarget(true)
    if not state.target then
        local now = getTimestampMs()
        if now - state.lastNoTargetDiagnosticMs >= 2000 then
            state.lastNoTargetDiagnosticMs = now
            logBindingDiagnostics("key-no-target")
        end
        return
    end

    log("interaction requested npcId=" .. tostring(state.target.npcId)
        .. " runtimeId=" .. tostring(state.target.runtimeId))
    requestDialogue()
end

local function localize(key, fallback)
    if type(key) ~= "string" or #key > C.MAX_IDENTIFIER_LENGTH
        or string.sub(key, 1, #TRANSLATION_PREFIX) ~= TRANSLATION_PREFIX
    then
        return fallback or "Quest Framework"
    end

    local value = getText(key)
    if not value or value == key then return fallback or key end
    return value
end

local function localizeDialogueState(args)
    args.npcName = localize(args.npcNameKey, "NPC")
    args.text = localize(args.textKey, "...")
    for _, choice in ipairs(args.choices or {}) do
        choice.text = localize(choice.textKey, "...")
    end
    return args
end

local function onPostUIDraw()
    maintainRuntimeBindingSync()
    refreshTarget(false)

    local now = getTimestampMs()
    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()
    local textManager = getTextManager()

    if state.target and not LCCQFDialoguePanel.instance then
        local prompt = "[E] " .. localize("IGUI_LCCQF_Prompt_Talk", "Talk")
            .. " - " .. localize(state.target.displayNameKey, "NPC")
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
    context:addOption("[Quest Framework] "
        .. localize("IGUI_LCCQF_Context_SpawnTestNPC", "Spawn test NPC"), player, spawnTestNPC)
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

    if command == C.COMMAND.RUNTIME_BINDINGS then
        local count = LCCQF.NPCRuntime.ReplaceRuntimeBindings(args.bindings)
        state.bindingsSynchronized = true
        log("runtime bindings synchronized count=" .. tostring(count))
        logBindingDiagnostics("full-sync")
        refreshTarget(true)
        return
    end

    if command == C.COMMAND.RUNTIME_BINDING_UPSERT then
        if LCCQF.NPCRuntime.BindRuntime(args.runtimeId, args.npcId, args) then
            log("runtime binding received npcId=" .. tostring(args.npcId)
                .. " runtimeId=" .. tostring(args.runtimeId)
                .. " anchor=" .. tostring(args.x) .. "," .. tostring(args.y) .. "," .. tostring(args.z))
            logBindingDiagnostics("upsert")
            refreshTarget(true)
        end
        return
    end

    if command == C.COMMAND.DIALOGUE_STATE then
        local npcId = type(args.npcId) == "string" and args.npcId or nil
        if not npcId or not LCCQF.NPCRegistry.IsRegistered(npcId) then
            setStatus(localize("IGUI_LCCQF_Status_UnknownNPCFromServer", "Unknown NPC from server"))
            return
        end
        LCCQFDialoguePanel.open(localizeDialogueState(args), sendChoice, sendClose)
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
        local message = localize(args.messageKey, "Quest Framework")
        setStatus(message)
        log("status key=" .. tostring(args.messageKey))
    end
end

local function onGameStart()
    log("loaded version=" .. tostring(C.VERSION)
        .. " runtime=Bandits interactKey=E range=" .. tostring(C.INTERACTION_RANGE)
        .. " discovery=framework-anchor")
    state.bindingsSynchronized = false
    state.nextBindingRequestMs = 0
    state.nextTargetScanMs = 0
    state.lastNoTargetDiagnosticMs = 0
    state.target = nil
    state.loggedTargetRuntimeId = nil
    maintainRuntimeBindingSync()
end

Events.OnGameStart.Add(onGameStart)
Events.OnKeyPressed.Add(onKeyPressed)
Events.OnPostUIDraw.Add(onPostUIDraw)
Events.OnServerCommand.Add(onServerCommand)
Events.OnFillWorldObjectContextMenu.Add(onFillWorldObjectContextMenu)

return LCCQFInteractionClient
