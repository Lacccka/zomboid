require "BanditBrain"
require "LCCQF/LCCQFConstants"
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

local function getDefinitionFromZombie(zombie)
    if not zombie then return nil, nil end

    local brain = BanditBrain.Get(zombie)
    if not brain or not brain.key then return nil, brain end

    return LCCQF.GetNPCDefinition(brain.key), brain
end

local function scanNearestQuestNPC()
    state.target = nil

    local player = getSpecificPlayer(0)
    if not player or player:isDead() or player:getVehicle() then return end

    local cell = player:getCell()
    if not cell then return end

    local px = player:getX()
    local py = player:getY()
    local pz = player:getZ()
    local range = C.INTERACTION_RANGE
    local rangeSq = range * range
    local tileRange = math.ceil(range) + 1
    local bestDistSq = rangeSq + 0.001

    for x = math.floor(px) - tileRange, math.floor(px) + tileRange do
        for y = math.floor(py) - tileRange, math.floor(py) + tileRange do
            local square = cell:getGridSquare(x, y, math.floor(pz))
            if square then
                local moving = square:getMovingObjects()
                for i = 0, moving:size() - 1 do
                    local object = moving:get(i)
                    if object and instanceof(object, "IsoZombie") and not object:isDead() then
                        local definition, brain = getDefinitionFromZombie(object)
                        if definition and brain and brain.id ~= nil then
                            local dz = math.abs(object:getZ() - pz)
                            if dz < 0.5 then
                                local dx = object:getX() - px
                                local dy = object:getY() - py
                                local distSq = dx * dx + dy * dy
                                if distSq <= rangeSq and distSq < bestDistSq then
                                    bestDistSq = distSq
                                    state.target = {
                                        zombie = object,
                                        npcKey = brain.key,
                                        runtimeId = brain.id,
                                        name = brain.fullname or definition.displayName or "NPC",
                                        distanceSq = distSq,
                                    }
                                end
                            end
                        end
                    end
                end
            end
        end
    end
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

    sendClientCommand(player, C.MODULE, "RequestDialogue", {
        npcKey = target.npcKey,
        npcRuntimeId = target.runtimeId,
    })
end

local function onKeyPressed(key)
    if key ~= Keyboard.KEY_E then return end
    if not state.target then return end
    if LCCQFDialoguePanel.instance then return end

    requestDialogue()
end

local function onPostUIDraw()
    local now = getTimestampMs()
    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()
    local textManager = getTextManager()

    if state.target and not LCCQFDialoguePanel.instance then
        local text = "[E] Поговорить — " .. tostring(state.target.name)
        textManager:DrawStringCentre(UIFont.Medium, screenWidth / 2, screenHeight - 150, text, 1, 1, 1, 1)
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
    sendClientCommand(player, C.MODULE, "SpawnTestNPC", {})
end

local function onFillWorldObjectContextMenu(playerNum, context, worldobjects, test)
    if test then return end

    local player = getSpecificPlayer(playerNum)
    if not isPrivilegedClient(player) then return end

    context:addOption("[Quest Framework] Создать тестового NPC", player, spawnTestNPC)
end

local function onServerCommand(module, command, args)
    if module ~= C.MODULE then return end
    args = args or {}

    if command == "OpenDialogue" then
        local definition = LCCQF.GetNPCDefinition(args.npcKey)
        if not definition then
            setStatus("Неизвестный NPC: " .. tostring(args.npcKey))
            return
        end

        LCCQFDialoguePanel.open(
            args.npcName or definition.displayName,
            args.dialogueId or definition.dialogueId,
            args.sessionId
        )
        log("dialogue opened session=" .. tostring(args.sessionId) .. " npc=" .. tostring(args.npcKey))
        return
    end

    if command == "Status" then
        setStatus(args.message or "Quest Framework")
        log(args.message or "status")
    end
end

local function onGameStart()
    log("loaded version=" .. tostring(C.VERSION) .. " interactKey=E range=" .. tostring(C.INTERACTION_RANGE))
end

Events.OnGameStart.Add(onGameStart)
Events.OnTick.Add(onTick)
Events.OnKeyPressed.Add(onKeyPressed)
Events.OnPostUIDraw.Add(onPostUIDraw)
Events.OnServerCommand.Add(onServerCommand)
Events.OnFillWorldObjectContextMenu.Add(onFillWorldObjectContextMenu)

return LCCQFInteractionClient
