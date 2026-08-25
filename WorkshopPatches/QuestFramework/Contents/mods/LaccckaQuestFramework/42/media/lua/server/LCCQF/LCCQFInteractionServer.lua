require "Bandit"
require "BanditBrain"
require "BanditCustom"
require "LCCQF/LCCQFConstants"

LCCQFInteractionServer = LCCQFInteractionServer or {}

local C = LCCQF.Constants
local sessions = {}

local function log(message)
    print(C.LOG_PREFIX .. "[SERVER] " .. tostring(message))
end

local function sendStatus(player, message)
    if not player then return end
    sendServerCommand(player, C.MODULE, "Status", { message = tostring(message or "") })
end

local function getSessionKey(player)
    if not player then return nil end
    if player.getOnlineID then
        return tostring(player:getOnlineID())
    end
    if player.getUsername then
        return tostring(player:getUsername())
    end
    return tostring(player)
end

local function getZombieList(player)
    local cell = player and player:getCell() or getCell()
    if not cell then return nil end
    return cell:getZombieList()
end

local function findQuestNPC(player, runtimeId, npcKey)
    local zombies = getZombieList(player)
    if not zombies then return nil, nil end

    for i = 0, zombies:size() - 1 do
        local zombie = zombies:get(i)
        if zombie and not zombie:isDead() then
            local brain = BanditBrain.Get(zombie)
            if brain and brain.key == npcKey then
                local runtimeMatches = runtimeId == nil or tostring(brain.id) == tostring(runtimeId)
                if runtimeMatches then
                    return zombie, brain
                end
            end
        end
    end

    return nil, nil
end

local function isInInteractionRange(player, npc)
    if not player or not npc then return false end
    if player:isDead() or npc:isDead() then return false end

    local dz = math.abs(player:getZ() - npc:getZ())
    if dz >= 0.5 then return false end

    local dx = player:getX() - npc:getX()
    local dy = player:getY() - npc:getY()
    local maxRange = C.SERVER_INTERACTION_RANGE
    return (dx * dx + dy * dy) <= (maxRange * maxRange)
end

local function isPrivileged(player)
    if not player then return false end

    if isDebugEnabled and isDebugEnabled() then
        return true
    end

    if not player.getAccessLevel then return false end
    local access = tostring(player:getAccessLevel() or ""):lower()
    return access ~= "" and access ~= "none"
end

local function applyQuestNPCState(zombie, brain, definition)
    if not zombie or not brain or not definition then return end

    brain.hostile = false
    brain.hostileP = false
    brain.permanent = true

    if definition.stationary then
        Bandit.ForceStationary(zombie, true)
    end

    BanditBrain.Update(zombie, brain)

    if TransmitBanditCluster and brain.id ~= nil then
        TransmitBanditCluster(brain.id)
    end
end

local function findSpawnSquare(player)
    if not player then return nil end
    local cell = player:getCell()
    if not cell then return nil end

    local px = math.floor(player:getX())
    local py = math.floor(player:getY())
    local pz = math.floor(player:getZ())
    local offsets = {
        { 2, 0 }, { -2, 0 }, { 0, 2 }, { 0, -2 },
        { 2, 1 }, { 2, -1 }, { -2, 1 }, { -2, -1 },
        { 1, 2 }, { -1, 2 }, { 1, -2 }, { -1, -2 },
    }

    for _, offset in ipairs(offsets) do
        local x = px + offset[1]
        local y = py + offset[2]
        local square = cell:getGridSquare(x, y, pz)
        if square and square:isFree(false) then
            return { x = x, y = y, z = pz }
        end
    end

    return nil
end

local function spawnTestNPC(player)
    if not isPrivileged(player) then
        sendStatus(player, "Недостаточно прав для создания тестового NPC.")
        log("spawn rejected: insufficient privileges player=" .. tostring(player and player:getUsername()))
        return
    end

    local definition = LCCQF.GetNPCDefinition(C.TEST_NPC_KEY)
    if not definition then
        sendStatus(player, "Не найдена конфигурация тестового NPC.")
        return
    end

    local existing, existingBrain = findQuestNPC(player, nil, definition.key)
    if existing and existingBrain then
        applyQuestNPCState(existing, existingBrain, definition)
        sendStatus(player, "Алексей уже существует в загруженном мире.")
        log("spawn skipped: existing npc id=" .. tostring(existingBrain.id))
        return
    end

    if not BanditServer or not BanditServer.Spawner or not BanditServer.Spawner.Individual then
        sendStatus(player, "Bandits2 Spawner.Individual недоступен.")
        log("spawn failed: BanditServer.Spawner.Individual unavailable")
        return
    end

    local profile = BanditCustom.GetById(definition.bid)
    if not profile or not profile.general then
        sendStatus(player, "Профиль тестового NPC не загрузился из common/bandits.")
        log("spawn failed: profile missing bid=" .. tostring(definition.bid))
        return
    end

    -- Bandits2 Individual currently reads bandit.cid directly while the file loader stores it in general.cid.
    -- Supplying the alias here keeps our integration source-clean and avoids patching Bandits2.
    profile.cid = profile.general.cid

    local spawn = findSpawnSquare(player)
    if not spawn then
        sendStatus(player, "Рядом нет свободной клетки для тестового NPC.")
        return
    end

    BanditServer.Spawner.Individual(player, {
        bid = definition.bid,
        x = spawn.x,
        y = spawn.y,
        z = spawn.z,
        program = "Defend",
        permanent = true,
        key = definition.key,
        hostile = false,
        hostileP = false,
        fullname = definition.displayName,
    })

    local zombie, brain = findQuestNPC(player, nil, definition.key)
    if not zombie or not brain then
        sendStatus(player, "Bandits2 не вернул созданного NPC. Проверь серверный лог.")
        log("spawn failed after Individual bid=" .. tostring(definition.bid))
        return
    end

    applyQuestNPCState(zombie, brain, definition)
    sendStatus(player, "Тестовый NPC Алексей создан.")
    log("spawned npc key=" .. tostring(definition.key) .. " id=" .. tostring(brain.id) .. " x=" .. tostring(zombie:getX()) .. " y=" .. tostring(zombie:getY()))
end

local function requestDialogue(player, args)
    args = args or {}

    local definition = LCCQF.GetNPCDefinition(args.npcKey)
    if not definition then
        sendStatus(player, "Этот NPC не зарегистрирован в Quest Framework.")
        log("dialogue rejected: unknown npc key=" .. tostring(args.npcKey))
        return
    end

    local npc, brain = findQuestNPC(player, args.npcRuntimeId, args.npcKey)
    if not npc or not brain then
        sendStatus(player, "NPC больше не найден рядом.")
        log("dialogue rejected: npc missing key=" .. tostring(args.npcKey) .. " runtimeId=" .. tostring(args.npcRuntimeId))
        return
    end

    if not isInInteractionRange(player, npc) then
        sendStatus(player, "Подойди ближе к NPC.")
        log("dialogue rejected: out of range player=" .. tostring(player:getUsername()) .. " npc=" .. tostring(args.npcKey))
        return
    end

    local sessionId = getRandomUUID()
    local key = getSessionKey(player)
    sessions[key] = {
        id = sessionId,
        npcKey = args.npcKey,
        npcRuntimeId = brain.id,
        openedWorldAge = getGameTime():getWorldAgeHours(),
    }

    sendServerCommand(player, C.MODULE, "OpenDialogue", {
        sessionId = sessionId,
        npcKey = args.npcKey,
        npcRuntimeId = brain.id,
        npcName = brain.fullname or definition.displayName,
        dialogueId = definition.dialogueId,
    })

    log("dialogue opened session=" .. tostring(sessionId) .. " player=" .. tostring(player:getUsername()) .. " npc=" .. tostring(args.npcKey))
end

local function closeDialogue(player, args)
    args = args or {}
    local key = getSessionKey(player)
    local session = key and sessions[key] or nil
    if not session then return end

    if args.sessionId == nil or tostring(args.sessionId) == tostring(session.id) then
        log("dialogue closed session=" .. tostring(session.id) .. " player=" .. tostring(player:getUsername()))
        sessions[key] = nil
    end
end

local function onClientCommand(module, command, player, args)
    if module ~= C.MODULE then return end

    if command == "SpawnTestNPC" then
        spawnTestNPC(player)
    elseif command == "RequestDialogue" then
        requestDialogue(player, args)
    elseif command == "CloseDialogue" then
        closeDialogue(player, args)
    end
end

local function enforceQuestNPCState()
    local zombies = getZombieList(nil)
    if not zombies then return end

    for i = 0, zombies:size() - 1 do
        local zombie = zombies:get(i)
        if zombie and not zombie:isDead() then
            local brain = BanditBrain.Get(zombie)
            if brain and brain.key then
                local definition = LCCQF.GetNPCDefinition(brain.key)
                if definition then
                    local changed = false

                    if brain.hostile then
                        brain.hostile = false
                        changed = true
                    end
                    if brain.hostileP then
                        brain.hostileP = false
                        changed = true
                    end
                    if not brain.permanent then
                        brain.permanent = true
                        changed = true
                    end
                    if definition.stationary and not brain.stationary then
                        Bandit.ForceStationary(zombie, true)
                        changed = true
                    end

                    if changed then
                        BanditBrain.Update(zombie, brain)
                        if TransmitBanditCluster and brain.id ~= nil then
                            TransmitBanditCluster(brain.id)
                        end
                    end
                end
            end
        end
    end
end

local function onServerStarted()
    log("loaded version=" .. tostring(C.VERSION) .. " serverRange=" .. tostring(C.SERVER_INTERACTION_RANGE))
end

Events.OnClientCommand.Add(onClientCommand)
Events.EveryOneMinute.Add(enforceQuestNPCState)
Events.OnServerStarted.Add(onServerStarted)

return LCCQFInteractionServer
