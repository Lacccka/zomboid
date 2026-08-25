-- QPST_BUILD41_BACKPORT_V1
-- Build 41 runtime synchronized with the v0.5.1 dual-build implementation.
-- QP Survivor Tasks
-- v0.5.1 synchronized server-authoritative board actions
-- QPST_SAFEHOUSE_PLACEMENT_V1
-- QPST_SERVER_AUTH_CREATE_V1
-- QPST_ADMIN_ANYWHERE_V1
-- QPST_SERVER_AUTHORITATIVE_MUTATIONS_V1
-- QPST_BOARD_STATE_SYNC_V1
-- QPST_FIRST_COMPLETION_WINS_V1
-- QPST_ADMIN_BOARD_PROTECTION_V1
-- QPST_REPUTATION_WORKFLOW_V060
-- QPST_PUBLIC_BOARD_V061_TC1
-- QPST_WEEKLY_COMMUNITY_V070_TC1_FOUNDATION
-- QPST_WEEKLY_VERIFICATION_V070_TC2

local NETWORK_MODULE = "QPSurvivorTasks"
local COMMAND_CREATE_SAFEHOUSE_BOARD = "CreateSafehouseBoard"
local COMMAND_CREATE_BOARD_RESULT = "CreateBoardResult"
local COMMAND_REQUEST_BOARD_STATE = "RequestBoardState"
local COMMAND_REQUEST_NEARBY_BOARDS = "RequestNearbyBoards"
local COMMAND_BOARD_STATE = "BoardState"
local COMMAND_ADD_TASK = "AddTask"
local COMMAND_COMPLETE_TASK = "CompleteTask"
local COMMAND_CLAIM_TASK = "ClaimTask"
local COMMAND_SUBMIT_TASK = "SubmitTask"
local COMMAND_VALIDATE_TASK = "ValidateTask"
local COMMAND_REMOVE_TASK = "RemoveTask"
local COMMAND_CLEAR_BOARD = "ClearBoard"
local COMMAND_REMOVE_BOARD = "RemoveBoard"
local COMMAND_SET_PUBLIC_BOARD = "SetPublicBoard"

local DATA_KEY = "QPST_Tasks"
local BOARD_KEY = "QPST_IsBoard"
local BOARD_INFO_KEY = "QPST_BoardInfo"
local TASK_SEQUENCE_KEY = "QPST_NextTaskId"
local STATE_REVISION_KEY = "QPST_StateRevision"

local BOARD_SCHEMA_VERSION = 3
local BOARD_TYPE_SAFEHOUSE = "Safehouse"
local BOARD_TYPE_ADMIN = "Admin"
local PERMISSION_MODE_EVERYONE = "Everyone"
local PERMISSION_MODE_SAFEHOUSE = "SafehouseMembers"
local PERMISSION_MODE_ADMIN = "AdminOnly"

local function getSpriteName(worldObj)
    if not worldObj or not worldObj.getSprite or not worldObj:getSprite() then
        return ""
    end

    if worldObj:getSprite().getName then
        return worldObj:getSprite():getName() or ""
    end

    return ""
end

local function isValidBoardTarget(worldObj)
    if not worldObj or not worldObj.getSquare or not worldObj:getSquare() then
        return false
    end

    local spriteName = string.lower(getSpriteName(worldObj))

    if spriteName == "" then
        return false
    end

    local blockedWords = {
        "door", "doors", "window", "windows", "light", "switch",
        "floor", "wall", "stairs", "curtain", "vegetation", "tree", "trash"
    }

    for i = 1, #blockedWords do
        if string.find(spriteName, blockedWords[i], 1, true) then
            return false
        end
    end

    if worldObj.getContainer and worldObj:getContainer() then
        return true
    end

    local allowedWords = {
        "shelf", "shelves", "table", "desk", "counter", "crate",
        "cabinet", "locker", "dresser", "wardrobe", "filing",
        "display", "sign", "board", "school", "office", "furniture"
    }

    for i = 1, #allowedWords do
        if string.find(spriteName, allowedWords[i], 1, true) then
            return true
        end
    end

    return false
end

local function getPlayerUsername(playerObj)
    if not playerObj or not playerObj.getUsername then
        return ""
    end

    local username = playerObj:getUsername()
    return username and tostring(username) or ""
end

local function isAdminPlayer(playerObj)
    if not playerObj or not playerObj.getAccessLevel then
        return false
    end

    local ok, accessLevel = pcall(function()
        return playerObj:getAccessLevel()
    end)

    if not ok or accessLevel == nil then
        return false
    end

    return string.lower(tostring(accessLevel)) == "admin"
end

local function getPlayerDisplayName(playerObj)
    local username = getPlayerUsername(playerObj)

    if username ~= "" then
        return username
    end

    return "Unknown"
end

local function getWorldAgeHours()
    if getGameTime and getGameTime() then
        return getGameTime():getWorldAgeHours()
    end

    return 0
end

local function getSafehouseForSquare(square)
    if not square or not SafeHouse or not SafeHouse.getSafeHouse then
        return nil
    end

    local ok, safehouse = pcall(function()
        return SafeHouse.getSafeHouse(square)
    end)

    if not ok then
        return nil
    end

    return safehouse
end

local function isPlayerAllowedInSafehouse(playerObj, safehouse)
    if not playerObj or not safehouse then
        return false
    end

    if safehouse.isOwner then
        local okOwner, ownerResult = pcall(function()
            return safehouse:isOwner(playerObj)
        end)

        if okOwner and ownerResult == true then
            return true
        end
    end

    if safehouse.playerAllowed then
        local okPlayer, playerResult = pcall(function()
            return safehouse:playerAllowed(playerObj)
        end)

        if okPlayer and playerResult == true then
            return true
        end

        local username = getPlayerUsername(playerObj)

        if username ~= "" then
            local okName, nameResult = pcall(function()
                return safehouse:playerAllowed(username)
            end)

            if okName and nameResult == true then
                return true
            end
        end
    end

    return false
end

local function getSafehouseOwner(safehouse)
    if not safehouse or not safehouse.getOwner then
        return ""
    end

    local ok, value = pcall(function()
        return safehouse:getOwner()
    end)

    if not ok or value == nil then
        return ""
    end

    return tostring(value)
end

local function getSafehouseTitle(safehouse)
    if not safehouse or not safehouse.getTitle then
        return ""
    end

    local ok, value = pcall(function()
        return safehouse:getTitle()
    end)

    if not ok or value == nil then
        return ""
    end

    return tostring(value)
end

local REPUTATION_POINTS = { Easy = 1, Normal = 2, Hard = 3 }
local REPUTATION_PATHS = {
    community = true, hunter = true, explorer = true,
    medic = true, mechanic = true, builder = true
}

local function isSafehouseOwner(playerObj, safehouse)
    if not playerObj or not safehouse then return false end
    local username = string.lower(getPlayerUsername(playerObj))
    local owner = string.lower(getSafehouseOwner(safehouse))
    if username ~= "" and owner ~= "" and username == owner then return true end
    if safehouse.isOwner then
        local ok, result = pcall(function() return safehouse:isOwner(playerObj) end)
        if ok and result == true then return true end
    end
    return false
end

local function canCreateReputationTask(playerObj, worldObj, info, difficulty)
    if isAdminPlayer(playerObj) then return true end
    if difficulty == "Hard" then return false end
    if not info or info.boardType ~= BOARD_TYPE_SAFEHOUSE then return false end
    return isSafehouseOwner(playerObj, getSafehouseForSquare(worldObj:getSquare()))
end

local function isReputationTask(task)
    return task and task.reputationEnabled == true
end

local function makeTaskEventId(worldObj, task)
    local square = worldObj and worldObj:getSquare() or nil
    local x = square and math.floor(tonumber(square:getX()) or 0) or 0
    local y = square and math.floor(tonumber(square:getY()) or 0) or 0
    local z = square and math.floor(tonumber(square:getZ()) or 0) or 0
    local index = worldObj and worldObj.getObjectIndex and worldObj:getObjectIndex() or -1
    return table.concat({"qpst", x, y, z, index, tostring(getTaskId(task) or "")}, ":")
end

local function awardTaskReputation(worldObj, task, validator)
    if not isReputationTask(task) then return false, "not_reputation_task" end
    if task.reputationAwarded == true then return false, "duplicate_award" end
    local api = QPReputation and QPReputation.Server and QPReputation.Server.awardSurvivorTask
    if type(api) ~= "function" then
        task.reputationResult = "qpsr_unavailable"
        return false, "qpsr_unavailable"
    end
    local ok, result = api(
        tostring(task.completedBy or ""),
        tostring(task.reputationPath or ""),
        math.floor(tonumber(task.reputationPoints) or 0),
        makeTaskEventId(worldObj, task),
        "QP Survivor Task validated",
        getPlayerDisplayName(validator),
        {
            creatorUsername = tostring(task.createdBy or ""),
            validatorUsername = getPlayerUsername(validator),
            taskText = tostring(task.text or ""),
            difficulty = tostring(task.reputationDifficulty or "")
        }
    )
    task.reputationResult = tostring(result or "")
    if ok then
        task.reputationAwarded = true
        task.reputationAwardedAt = getWorldAgeHours()
    end
    return ok, result
end

local function resolveWorldObject(args)
    if not args or not getCell or not getCell() then
        return nil, nil
    end

    local x = math.floor(tonumber(args.x) or 0)
    local y = math.floor(tonumber(args.y) or 0)
    local z = math.floor(tonumber(args.z) or 0)
    local objectIndex = math.floor(tonumber(args.objectIndex) or -1)

    local square = getCell():getGridSquare(x, y, z)

    if not square or not square.getObjects then
        return nil, nil
    end

    local objects = square:getObjects()

    if not objects or objectIndex < 0 or objectIndex >= objects:size() then
        return nil, square
    end

    local worldObj = objects:get(objectIndex)

    if not worldObj then
        return nil, square
    end

    local expectedSprite = tostring(args.spriteName or "")

    if expectedSprite ~= "" and getSpriteName(worldObj) ~= expectedSprite then
        return nil, square
    end

    return worldObj, square
end

local function shallowCopyTable(source)
    local result = {}

    if type(source) ~= "table" then
        return result
    end

    for key, value in pairs(source) do
        result[key] = value
    end

    return result
end

local function copyTasks(tasks)
    local result = {}

    if type(tasks) ~= "table" then
        return result
    end

    for i = 1, #tasks do
        if type(tasks[i]) == "table" then
            result[i] = shallowCopyTable(tasks[i])
        end
    end

    return result
end

local function getTaskId(task)
    if not task then return "" end
    return tostring(task.id or task.taskId or "")
end

local function getData(worldObj)
    if not worldObj then return nil end
    return worldObj:getModData()
end

local function getTasks(worldObj)
    local data = getData(worldObj)
    if not data then return nil end

    if type(data[DATA_KEY]) ~= "table" then
        data[DATA_KEY] = {}
    end

    return data[DATA_KEY]
end

local function nextTaskId(worldObj, data)
    local sequence = math.floor(tonumber(data[TASK_SEQUENCE_KEY]) or 0) + 1
    data[TASK_SEQUENCE_KEY] = sequence

    local square = worldObj:getSquare()
    return table.concat({
        tostring(square:getX()),
        tostring(square:getY()),
        tostring(square:getZ()),
        tostring(sequence)
    }, "-")
end

local function ensureTaskIds(worldObj)
    local data = getData(worldObj)
    local tasks = getTasks(worldObj)

    if not data or not tasks then return false end

    local changed = false

    for i = 1, #tasks do
        local task = tasks[i]

        if task and getTaskId(task) == "" then
            task.id = nextTaskId(worldObj, data)
            changed = true
        elseif task and task.id == nil then
            task.id = getTaskId(task)
            changed = true
        end
    end

    return changed
end

local function findTaskIndex(tasks, taskId, fallbackIndex)
    local wanted = tostring(taskId or "")

    if wanted ~= "" and tasks then
        for i = 1, #tasks do
            if tasks[i] and getTaskId(tasks[i]) == wanted then
                return i
            end
        end
    end

    local index = math.floor(tonumber(fallbackIndex) or -1)

    if tasks and index >= 1 and tasks[index] then
        return index
    end

    return nil
end

local function ensureBoardInfo(worldObj)
    local data = getData(worldObj)

    if not data or data[BOARD_KEY] ~= true then
        return nil, false
    end

    local changed = false
    local info = data[BOARD_INFO_KEY]

    if type(info) ~= "table" then
        info = {}
        data[BOARD_INFO_KEY] = info
        changed = true
    end

    if info.schemaVersion ~= BOARD_SCHEMA_VERSION then
        info.schemaVersion = BOARD_SCHEMA_VERSION
        changed = true
    end

    if info.boardType ~= BOARD_TYPE_ADMIN and info.boardType ~= BOARD_TYPE_SAFEHOUSE then
        info.boardType = BOARD_TYPE_SAFEHOUSE
        changed = true
    end

    if info.boardType == BOARD_TYPE_ADMIN then
        if info.permissionMode ~= PERMISSION_MODE_ADMIN then
            info.permissionMode = PERMISSION_MODE_ADMIN
            changed = true
        end
    elseif info.permissionMode ~= PERMISSION_MODE_SAFEHOUSE and
           info.permissionMode ~= PERMISSION_MODE_EVERYONE then
        info.permissionMode = PERMISSION_MODE_SAFEHOUSE
        changed = true
    end

    if tostring(info.ownerUsername or "") == "" then
        info.ownerUsername = tostring(info.createdBy or "")
        changed = true
    end

    if tostring(info.ownerDisplayName or "") == "" then
        info.ownerDisplayName = tostring(info.createdBy or info.ownerUsername or "")
        changed = true
    end

    if tostring(info.createdBy or "") == "" then
        info.createdBy = tostring(info.ownerDisplayName or info.ownerUsername or "")
        changed = true
    end

    if type(info.createdAt) ~= "number" then
        info.createdAt = getWorldAgeHours()
        changed = true
    end

    local square = worldObj:getSquare()

    if type(info.locationX) ~= "number" then
        info.locationX = square:getX()
        changed = true
    end

    if type(info.locationY) ~= "number" then
        info.locationY = square:getY()
        changed = true
    end

    if type(info.locationZ) ~= "number" then
        info.locationZ = square:getZ()
        changed = true
    end

    if info.safehouseOwner == nil then
        info.safehouseOwner = ""
        changed = true
    end

    if info.safehouseTitle == nil then
        info.safehouseTitle = ""
        changed = true
    end

    if info.legacyImported == nil then
        info.legacyImported = true
        changed = true
    end

    if ensureTaskIds(worldObj) then
        changed = true
    end

    return info, changed
end

local function canManageBoard(playerObj, worldObj, info)
    if not playerObj or not worldObj or not info then
        return false
    end

    if isAdminPlayer(playerObj) then
        return true
    end

    if info.boardType == BOARD_TYPE_ADMIN or info.permissionMode == PERMISSION_MODE_ADMIN then
        return false
    end

    local safehouse = getSafehouseForSquare(worldObj:getSquare())

    if safehouse then
        return isPlayerAllowedInSafehouse(playerObj, safehouse)
    end

    local username = getPlayerUsername(playerObj)
    local ownerUsername = tostring(info.ownerUsername or "")

    if username ~= "" and ownerUsername ~= "" and username == ownerUsername then
        return true
    end

    return info.permissionMode == PERMISSION_MODE_EVERYONE
end

local function isPublicBoardInfo(info)
    return type(info) == "table"
        and info.boardType == BOARD_TYPE_ADMIN
        and info.publicCreate == true
end

local function canAddTask(playerObj, worldObj, info)
    if not playerObj or not worldObj or not info then
        return false
    end

    if isAdminPlayer(playerObj) then
        return true
    end

    if isPublicBoardInfo(info) then
        return true
    end

    return canManageBoard(playerObj, worldObj, info)
end

local function canCompleteBoard(playerObj, worldObj, info)
    if not playerObj or not worldObj or not info then
        return false
    end

    if isAdminPlayer(playerObj) then
        return true
    end

    if info.boardType == BOARD_TYPE_ADMIN or info.permissionMode == PERMISSION_MODE_ADMIN then
        return true
    end

    local safehouse = getSafehouseForSquare(worldObj:getSquare())

    if safehouse then
        return isPlayerAllowedInSafehouse(playerObj, safehouse)
    end

    return true
end

local function bumpRevision(data)
    data[STATE_REVISION_KEY] = math.floor(tonumber(data[STATE_REVISION_KEY]) or 0) + 1
    return data[STATE_REVISION_KEY]
end

local function transmit(worldObj)
    if worldObj and worldObj.transmitModData then
        worldObj:transmitModData()
    end
end

local function makeLocationResponse(args)
    return {
        x = tonumber(args and args.x) or 0,
        y = tonumber(args and args.y) or 0,
        z = tonumber(args and args.z) or 0,
        objectIndex = tonumber(args and args.objectIndex) or -1,
        spriteName = tostring(args and args.spriteName or "")
    }
end

local function makeBoardState(worldObj, args, success, messageKey)
    local response = makeLocationResponse(args)
    response.success = success ~= false
    response.messageKey = tostring(messageKey or "")
    response.requestKey = tostring(args and args.requestKey or "")

    if not worldObj then
        response.isBoard = false
        response.boardInfo = {}
        response.tasks = {}
        response.nextTaskId = 0
        response.revision = 0
        return response
    end

    local data = getData(worldObj)
    response.isBoard = data and data[BOARD_KEY] == true or false
    response.nextTaskId = data and math.floor(tonumber(data[TASK_SEQUENCE_KEY]) or 0) or 0
    response.revision = data and math.floor(tonumber(data[STATE_REVISION_KEY]) or 0) or 0

    if response.isBoard then
        local info, changed = ensureBoardInfo(worldObj)

        if changed then
            bumpRevision(data)
            transmit(worldObj)
            response.revision = math.floor(tonumber(data[STATE_REVISION_KEY]) or 0)
        end

        response.boardInfo = shallowCopyTable(info)
        response.tasks = copyTasks(getTasks(worldObj))
        response.nextTaskId = math.floor(tonumber(data[TASK_SEQUENCE_KEY]) or 0)
    else
        response.boardInfo = {}
        response.tasks = {}
    end

    return response
end

local function sendStateToPlayer(playerObj, worldObj, args, success, messageKey)
    sendServerCommand(
        playerObj,
        NETWORK_MODULE,
        COMMAND_BOARD_STATE,
        makeBoardState(worldObj, args, success, messageKey)
    )
end

local function broadcastState(worldObj, args)
    sendServerCommand(
        NETWORK_MODULE,
        COMMAND_BOARD_STATE,
        makeBoardState(worldObj, args, true, "")
    )
end

local function sendCreateResult(playerObj, success, messageKey, args, info)
    local response = makeLocationResponse(args)
    response.success = success == true
    response.messageKey = tostring(messageKey or "")

    if info then
        response.ownerUsername = tostring(info.ownerUsername or "")
        response.ownerDisplayName = tostring(info.ownerDisplayName or "")
        response.createdBy = tostring(info.createdBy or "")
        response.createdAt = tonumber(info.createdAt) or 0
        response.safehouseOwner = tostring(info.safehouseOwner or "")
        response.safehouseTitle = tostring(info.safehouseTitle or "")
        response.boardType = tostring(info.boardType or "")
        response.permissionMode = tostring(info.permissionMode or "")
    end

    sendServerCommand(playerObj, NETWORK_MODULE, COMMAND_CREATE_BOARD_RESULT, response)
end

local function createBoard(playerObj, args)
    local worldObj, square = resolveWorldObject(args)

    if not worldObj or not square or not isValidBoardTarget(worldObj) then
        sendCreateResult(playerObj, false, "UI_QPST_CreateBoardFailed", args, nil)
        return
    end

    local data = getData(worldObj)

    if not data or data[BOARD_KEY] == true then
        sendCreateResult(playerObj, false, "UI_QPST_CreateBoardAlreadyExists", args, nil)
        sendStateToPlayer(playerObj, worldObj, args, false, "")
        return
    end

    local safehouse = getSafehouseForSquare(square)
    local isAdmin = isAdminPlayer(playerObj)

    if not isAdmin then
        if not safehouse then
            sendCreateResult(playerObj, false, "UI_QPST_ErrorSafehouseRequired", args, nil)
            return
        end

        if not isPlayerAllowedInSafehouse(playerObj, safehouse) then
            sendCreateResult(playerObj, false, "UI_QPST_ErrorSafehousePermission", args, nil)
            return
        end
    end

    local username = getPlayerUsername(playerObj)
    local displayName = getPlayerDisplayName(playerObj)

    local info = {
        schemaVersion = BOARD_SCHEMA_VERSION,
        boardType = isAdmin and BOARD_TYPE_ADMIN or BOARD_TYPE_SAFEHOUSE,
        permissionMode = isAdmin and PERMISSION_MODE_ADMIN or PERMISSION_MODE_SAFEHOUSE,
        ownerUsername = username,
        ownerDisplayName = displayName,
        createdBy = displayName,
        createdAt = getWorldAgeHours(),
        locationX = square:getX(),
        locationY = square:getY(),
        locationZ = square:getZ(),
        safehouseOwner = getSafehouseOwner(safehouse),
        safehouseTitle = getSafehouseTitle(safehouse),
        legacyImported = false
    }

    data[BOARD_KEY] = true
    data[BOARD_INFO_KEY] = info
    data[DATA_KEY] = {}
    data[TASK_SEQUENCE_KEY] = 0
    bumpRevision(data)
    transmit(worldObj)

    sendCreateResult(playerObj, true, "UI_QPST_BoardCreated", args, info)
    broadcastState(worldObj, args)
end

local function requestBoardState(playerObj, args)
    local worldObj = resolveWorldObject(args)
    sendStateToPlayer(playerObj, worldObj, args, true, "")
end

local function makeObjectArgs(worldObj)
    local square = worldObj and worldObj:getSquare() or nil

    if not worldObj or not square then
        return nil
    end

    local objectIndex = worldObj.getObjectIndex and worldObj:getObjectIndex() or -1

    return {
        x = square:getX(),
        y = square:getY(),
        z = square:getZ(),
        objectIndex = objectIndex,
        spriteName = getSpriteName(worldObj)
    }
end

local function requestNearbyBoards(playerObj, args)
    if not playerObj or not getCell or not getCell() then
        return
    end

    local playerX = math.floor(tonumber(playerObj:getX()) or 0)
    local playerY = math.floor(tonumber(playerObj:getY()) or 0)
    local playerZ = math.floor(tonumber(playerObj:getZ()) or 0)
    local radius = 12

    for x = playerX - radius, playerX + radius do
        for y = playerY - radius, playerY + radius do
            local square = getCell():getGridSquare(x, y, playerZ)

            if square and square.getObjects then
                local objects = square:getObjects()

                if objects then
                    for index = 0, objects:size() - 1 do
                        local worldObj = objects:get(index)
                        local data = getData(worldObj)

                        if data and data[BOARD_KEY] == true then
                            local objectArgs = makeObjectArgs(worldObj)

                            if objectArgs then
                                sendStateToPlayer(playerObj, worldObj, objectArgs, true, "")
                            end
                        end
                    end
                end
            end
        end
    end
end

local function addTask(playerObj, args)
    local worldObj = resolveWorldObject(args)

    if not worldObj then
        sendStateToPlayer(playerObj, nil, args, false, "UI_QPST_CreateBoardFailed")
        return
    end

    local info = ensureBoardInfo(worldObj)

    if not info or not canAddTask(playerObj, worldObj, info) then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorBoardPermission")
        return
    end

    local taskText = tostring(args.taskText or "")
    taskText = string.gsub(taskText, "^%s+", "")
    taskText = string.gsub(taskText, "%s+$", "")

    if taskText == "" then
        sendStateToPlayer(playerObj, worldObj, args, false, "")
        return
    end

    if string.len(taskText) > 80 then
        taskText = string.sub(taskText, 1, 80)
    end

    local data = getData(worldObj)
    local tasks = getTasks(worldObj)

    local reputationEnabled = args.reputationEnabled == true
    local difficulty = tostring(args.reputationDifficulty or "")
    local path = string.lower(tostring(args.reputationPath or ""))
    local points = REPUTATION_POINTS[difficulty] or 0

    if reputationEnabled then
        if not REPUTATION_PATHS[path] or points <= 0 then
            sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorInvalidReputation")
            return
        end
        if not canCreateReputationTask(playerObj, worldObj, info, difficulty) then
            sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorReputationPermission")
            return
        end
    end

    table.insert(tasks, {
        id = nextTaskId(worldObj, data),
        text = taskText,
        status = "Open",
        createdBy = getPlayerDisplayName(playerObj),
        createdAt = getWorldAgeHours(),
        completedBy = "",
        completedAt = 0,
        translationKey = tostring(args.translationKey or ""),
        reputationEnabled = reputationEnabled,
        reputationDifficulty = reputationEnabled and difficulty or "",
        reputationPath = reputationEnabled and path or "",
        reputationPoints = reputationEnabled and points or 0,
        claimedBy = "",
        claimedAt = 0,
        submittedBy = "",
        submittedAt = 0,
        validatedBy = "",
        validatedAt = 0,
        reputationAwarded = false,
        reputationResult = ""
    })

    bumpRevision(data)
    transmit(worldObj)
    broadcastState(worldObj, args)
end

local function completeTask(playerObj, args)
    local worldObj = resolveWorldObject(args)

    if not worldObj then
        sendStateToPlayer(playerObj, nil, args, false, "UI_QPST_CreateBoardFailed")
        return
    end

    local info = ensureBoardInfo(worldObj)

    if not info or not canCompleteBoard(playerObj, worldObj, info) then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorBoardPermission")
        return
    end

    local tasks = getTasks(worldObj)
    local index = findTaskIndex(tasks, args.taskId, args.taskIndex)
    local task = index and tasks[index] or nil

    if task and isReputationTask(task) then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorUseWorkflow")
        return
    end

    -- First completion wins for legacy tasks.
    if task and task.status ~= "Done" then
        task.status = "Done"
        task.completedBy = getPlayerDisplayName(playerObj)
        task.completedAt = getWorldAgeHours()

        local data = getData(worldObj)
        bumpRevision(data)
        transmit(worldObj)
        broadcastState(worldObj, args)
        return
    end

    sendStateToPlayer(playerObj, worldObj, args, true, "")
end

local function resolveWorkflowTask(playerObj, args)
    local worldObj = resolveWorldObject(args)
    if not worldObj then return nil, nil, nil, "UI_QPST_CreateBoardFailed" end
    local info = ensureBoardInfo(worldObj)
    if not info or not canCompleteBoard(playerObj, worldObj, info) then
        return worldObj, info, nil, "UI_QPST_ErrorBoardPermission"
    end
    local tasks = getTasks(worldObj)
    local index = findTaskIndex(tasks, args.taskId, args.taskIndex)
    return worldObj, info, index and tasks[index] or nil, ""
end

local function commitWorkflow(worldObj, args)
    local data = getData(worldObj)
    bumpRevision(data)
    transmit(worldObj)
    broadcastState(worldObj, args)
end

local function claimTask(playerObj, args)
    local worldObj, info, task, errorKey = resolveWorkflowTask(playerObj, args)
    if errorKey ~= "" then sendStateToPlayer(playerObj, worldObj, args, false, errorKey); return end
    if not task or not isReputationTask(task) or task.status ~= "Open" then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorInvalidWorkflowState"); return
    end
    local username = getPlayerDisplayName(playerObj)
    if string.lower(username) == string.lower(tostring(task.createdBy or "")) then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorCreatorCannotClaim"); return
    end
    task.status = "Claimed"
    task.claimedBy = username
    task.claimedAt = getWorldAgeHours()
    commitWorkflow(worldObj, args)
end

local function submitTask(playerObj, args)
    local worldObj, info, task, errorKey = resolveWorkflowTask(playerObj, args)
    if errorKey ~= "" then sendStateToPlayer(playerObj, worldObj, args, false, errorKey); return end
    local username = getPlayerDisplayName(playerObj)
    if not task or not isReputationTask(task) or task.status ~= "Claimed" or
       string.lower(tostring(task.claimedBy or "")) ~= string.lower(username) then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorOnlyClaimantSubmit"); return
    end
    task.status = "Submitted"
    task.submittedBy = username
    task.submittedAt = getWorldAgeHours()
    task.completedBy = username
    commitWorkflow(worldObj, args)
end

local function validateTask(playerObj, args)
    local worldObj, info, task, errorKey = resolveWorkflowTask(playerObj, args)
    if errorKey ~= "" then sendStateToPlayer(playerObj, worldObj, args, false, errorKey); return end
    local validator = getPlayerDisplayName(playerObj)
    if not task or not isReputationTask(task) or task.status ~= "Submitted" then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorInvalidWorkflowState"); return
    end
    if string.lower(validator) == string.lower(tostring(task.completedBy or "")) then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorSelfValidation"); return
    end
    if string.lower(validator) == string.lower(tostring(task.createdBy or "")) then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorCreatorValidation"); return
    end
    task.status = "Done"
    task.completedAt = getWorldAgeHours()
    task.validatedBy = validator
    task.validatedAt = getWorldAgeHours()
    awardTaskReputation(worldObj, task, playerObj)
    commitWorkflow(worldObj, args)
end

local function setPublicBoardMode(playerObj, args)
    local worldObj = resolveWorldObject(args)

    if not worldObj then
        sendStateToPlayer(
            playerObj,
            nil,
            args,
            false,
            "UI_QPST_CreateBoardFailed"
        )
        return
    end

    local info = ensureBoardInfo(worldObj)

    if not isAdminPlayer(playerObj)
        or not info
        or info.boardType ~= BOARD_TYPE_ADMIN then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_ErrorPublicBoardAdminOnly"
        )
        return
    end

    info.publicCreate = args.enabled == true

    local data = getData(worldObj)
    bumpRevision(data)
    transmit(worldObj)

    sendStateToPlayer(
        playerObj,
        worldObj,
        args,
        true,
        info.publicCreate
            and "UI_QPST_PublicBoardEnabled"
            or "UI_QPST_PublicBoardDisabled"
    )

    broadcastState(worldObj, args)
end

-- QPST_WEEKLY_COMMUNITY_V070_TC1
local COMMAND_SET_WEEKLY_COMMUNITY = "SetWeeklyCommunity"
local COMMAND_ADD_WEEKLY_TASK = "AddWeeklyTask"
local COMMAND_CLAIM_MONEY_REWARD = "ClaimMoneyReward"
local COMMAND_WEEKLY_CLAIM_TASK = "WeeklyClaimTask"
local COMMAND_WEEKLY_SUBMIT_TASK = "WeeklySubmitTask"
local COMMAND_WEEKLY_VALIDATE_TASK = "WeeklyValidateTask"

local QPST_WEEKLY_DEFAULT_LIMIT = 5
local QPST_WEEKLY_MAX_LIMIT = 20
local QPST_WEEKLY_MAX_MONEY = 1000

local function qpstWeeklyClampInteger(value, minimum, maximum, fallback)
    local number = tonumber(value)

    if number == nil then
        number = tonumber(fallback) or minimum
    end

    number = math.floor(number)

    if number < minimum then
        return minimum
    end

    if number > maximum then
        return maximum
    end

    return number
end

local function qpstWeeklyCycleId()
    if os and os.date then
        local ok, value = pcall(function()
            return os.date("%Y-%W")
        end)

        if ok and value and value ~= "" then
            return tostring(value)
        end
    end

    return "world-" .. tostring(
        math.floor((tonumber(getWorldAgeHours()) or 0) / 168)
    )
end

local function qpstIsWeeklyCommunityInfo(info)
    return type(info) == "table"
        and info.boardType == BOARD_TYPE_ADMIN
        and info.weeklyCommunity == true
end

local function qpstRefreshWeeklyInfo(info)
    if not qpstIsWeeklyCommunityInfo(info) then
        return false
    end

    local changed = false
    local cycleId = qpstWeeklyCycleId()

    if tostring(info.weeklyCycleId or "") ~= cycleId then
        info.weeklyCycleId = cycleId
        info.weeklyCreatedThisCycle = 0
        changed = true
    end

    local limit = qpstWeeklyClampInteger(
        info.weeklyTaskLimit,
        1,
        QPST_WEEKLY_MAX_LIMIT,
        QPST_WEEKLY_DEFAULT_LIMIT
    )

    if tonumber(info.weeklyTaskLimit) ~= limit then
        info.weeklyTaskLimit = limit
        changed = true
    end

    local created = math.max(
        0,
        math.floor(tonumber(info.weeklyCreatedThisCycle) or 0)
    )

    if tonumber(info.weeklyCreatedThisCycle) ~= created then
        info.weeklyCreatedThisCycle = created
        changed = true
    end

    return changed
end

local qpstV070OriginalEnsureBoardInfo = ensureBoardInfo

ensureBoardInfo = function(worldObj)
    local info, changed = qpstV070OriginalEnsureBoardInfo(worldObj)

    if not info then
        return info, changed
    end

    local weeklyChanged = qpstRefreshWeeklyInfo(info)

    return info, changed == true or weeklyChanged == true
end

local qpstV070OriginalCanAddTask = canAddTask

canAddTask = function(playerObj, worldObj, info)
    if qpstIsWeeklyCommunityInfo(info) then
        -- Dedicated AddWeeklyTask is the only creation route in this mode.
        return false
    end

    return qpstV070OriginalCanAddTask(
        playerObj,
        worldObj,
        info
    )
end

local qpstV070OriginalSetPublicBoardMode = setPublicBoardMode

setPublicBoardMode = function(playerObj, args)
    local worldObj = resolveWorldObject(args)
    local info = worldObj and ensureBoardInfo(worldObj) or nil

    if args
        and args.enabled == true
        and qpstIsWeeklyCommunityInfo(info) then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_WeeklyPublicConflict"
        )
        return
    end

    return qpstV070OriginalSetPublicBoardMode(
        playerObj,
        args
    )
end

-- QPST_WEEKLY_MONEY_SYNC_V070_TC21
local function qpstAddMoneyToInventory(playerObj, amount)
    if not playerObj or not playerObj.getInventory then
        return false
    end

    local inventory = playerObj:getInventory()

    if not inventory or not inventory.AddItem then
        return false
    end

    amount = qpstWeeklyClampInteger(
        amount,
        1,
        QPST_WEEKLY_MAX_MONEY,
        1
    )

    local created = {}
    local synced = {}

    local function rollback()
        if sendRemoveItemFromContainer then
            for _, syncedItem in ipairs(synced) do
                pcall(function()
                    sendRemoveItemFromContainer(inventory, syncedItem)
                end)
            end
        end

        for _, createdItem in ipairs(created) do
            pcall(function()
                if not inventory.contains
                    or inventory:contains(createdItem) then
                    inventory:Remove(createdItem)
                end
            end)
        end
    end

    for i = 1, amount do
        local ok, item = pcall(function()
            return inventory:AddItem("Base.Money")
        end)

        if not ok or not item then
            rollback()
            return false
        end

        table.insert(created, item)
    end

    local multiplayerServer =
        isServer
        and isServer() == true

    if multiplayerServer then
        if not sendAddItemToContainer then
            rollback()
            return false
        end

        for _, item in ipairs(created) do
            local ok = pcall(function()
                sendAddItemToContainer(inventory, item)
            end)

            if not ok then
                rollback()
                return false
            end

            table.insert(synced, item)
        end
    end

    print(
        "[QPST-DEV] Weekly money payout granted: "
        .. tostring(amount)
        .. " Base.Money to "
        .. tostring(getPlayerName(playerObj))
    )

    return #created == amount
end

local function setWeeklyCommunityMode(playerObj, args)
    local worldObj = resolveWorldObject(args)

    if not worldObj then
        sendStateToPlayer(
            playerObj,
            nil,
            args,
            false,
            "UI_QPST_CreateBoardFailed"
        )
        return
    end

    local info = ensureBoardInfo(worldObj)

    if not isAdminPlayer(playerObj)
        or not info
        or info.boardType ~= BOARD_TYPE_ADMIN then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_WeeklyAdminOnly"
        )
        return
    end

    if args.enabled == true then
        local cycleId = qpstWeeklyCycleId()

        if tostring(info.weeklyCycleId or "") ~= cycleId then
            info.weeklyCycleId = cycleId
            info.weeklyCreatedThisCycle = 0
        end

        info.weeklyTaskLimit = qpstWeeklyClampInteger(
            info.weeklyTaskLimit,
            1,
            QPST_WEEKLY_MAX_LIMIT,
            QPST_WEEKLY_DEFAULT_LIMIT
        )
        info.weeklyCommunity = true
        info.publicCreate = false
    else
        info.weeklyCommunity = false
    end

    local data = getData(worldObj)
    bumpRevision(data)
    transmit(worldObj)

    sendStateToPlayer(
        playerObj,
        worldObj,
        args,
        true,
        args.enabled == true
            and "UI_QPST_WeeklyCommunityEnabled"
            or "UI_QPST_WeeklyCommunityDisabled"
    )

    broadcastState(worldObj, args)
end

local function addWeeklyTask(playerObj, args)
    local worldObj = resolveWorldObject(args)

    if not worldObj then
        sendStateToPlayer(
            playerObj,
            nil,
            args,
            false,
            "UI_QPST_CreateBoardFailed"
        )
        return
    end

    local info = ensureBoardInfo(worldObj)

    if not isAdminPlayer(playerObj)
        or not qpstIsWeeklyCommunityInfo(info) then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_WeeklyAdminOnly"
        )
        return
    end

    qpstRefreshWeeklyInfo(info)

    local limit = qpstWeeklyClampInteger(
        info.weeklyTaskLimit,
        1,
        QPST_WEEKLY_MAX_LIMIT,
        QPST_WEEKLY_DEFAULT_LIMIT
    )
    local created = math.max(
        0,
        math.floor(tonumber(info.weeklyCreatedThisCycle) or 0)
    )

    if created >= limit then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_WeeklyTaskLimitReached"
        )
        return
    end

    local taskText = tostring(args.taskText or "")
    taskText = string.gsub(taskText, "^%s+", "")
    taskText = string.gsub(taskText, "%s+$", "")

    if taskText == "" then
        sendStateToPlayer(playerObj, worldObj, args, false, "")
        return
    end

    if string.len(taskText) > 80 then
        taskText = string.sub(taskText, 1, 80)
    end

    local moneyReward = qpstWeeklyClampInteger(
        args.moneyReward,
        0,
        QPST_WEEKLY_MAX_MONEY,
        0
    )

    local data = getData(worldObj)
    local tasks = getTasks(worldObj)

    table.insert(tasks, {
        id = nextTaskId(worldObj, data),
        text = taskText,
        status = "Open",
        createdBy = getPlayerDisplayName(playerObj),
        createdAt = getWorldAgeHours(),
        completedBy = "",
        completedAt = 0,
        translationKey = "",
        reputationEnabled = false,
        reputationDifficulty = "",
        reputationPath = "",
        reputationPoints = 0,
        claimedBy = "",
        claimedAt = 0,
        submittedBy = "",
        submittedAt = 0,
        validatedBy = "",
        validatedAt = 0,
        reputationAwarded = false,
        reputationResult = "",
        weeklyCommunity = true,
        weeklyWorkflowVersion = 2,
        weeklyValidated = false,
        weeklyCycleId = tostring(info.weeklyCycleId or qpstWeeklyCycleId()),
        moneyReward = moneyReward,
        moneyClaimed = false,
        moneyClaimedBy = "",
        moneyClaimedAt = 0
    })

    info.weeklyCreatedThisCycle = created + 1

    bumpRevision(data)
    transmit(worldObj)
    broadcastState(worldObj, args)
end

-- QPST_WEEKLY_VERIFICATION_V070_TC2_WORKFLOW
local function qpstWeeklySameUsername(left, right)
    return string.lower(tostring(left or ""))
        == string.lower(tostring(right or ""))
end

local function qpstResolveWeeklyTask(playerObj, args)
    local worldObj = resolveWorldObject(args)

    if not worldObj then
        return nil, nil, nil,
            "UI_QPST_CreateBoardFailed"
    end

    local info = ensureBoardInfo(worldObj)

    if not info
        or not canCompleteBoard(
            playerObj,
            worldObj,
            info
        ) then
        return worldObj, info, nil,
            "UI_QPST_ErrorBoardPermission"
    end

    local tasks = getTasks(worldObj)
    local index = findTaskIndex(
        tasks,
        args.taskId,
        args.taskIndex
    )
    local task = index and tasks[index] or nil

    if not task or task.weeklyCommunity ~= true then
        return worldObj, info, nil,
            "UI_QPST_ErrorInvalidWorkflowState"
    end

    return worldObj, info, task, ""
end

local function qpstCommitWeeklyWorkflow(
    playerObj,
    worldObj,
    args
)
    local data = getData(worldObj)

    bumpRevision(data)
    transmit(worldObj)

    sendStateToPlayer(
        playerObj,
        worldObj,
        args,
        true,
        ""
    )

    broadcastState(worldObj, args)
end

local qpstTC2OriginalCompleteTask = completeTask

completeTask = function(playerObj, args)
    local worldObj = resolveWorldObject(args)

    if worldObj then
        local tasks = getTasks(worldObj)
        local index = findTaskIndex(
            tasks,
            args.taskId,
            args.taskIndex
        )
        local task = index and tasks[index] or nil

        if task and task.weeklyCommunity == true then
            sendStateToPlayer(
                playerObj,
                worldObj,
                args,
                false,
                "UI_QPST_WeeklyUseWorkflow"
            )
            return
        end
    end

    return qpstTC2OriginalCompleteTask(
        playerObj,
        args
    )
end

local function weeklyClaimTask(playerObj, args)
    local worldObj, info, task, errorKey =
        qpstResolveWeeklyTask(playerObj, args)

    if errorKey ~= "" then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            errorKey
        )
        return
    end

    if tostring(task.status or "Open") ~= "Open" then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_ErrorInvalidWorkflowState"
        )
        return
    end

    local username = getPlayerDisplayName(playerObj)

    if qpstWeeklySameUsername(
        task.createdBy,
        username
    ) then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_WeeklyCreatorCannotClaim"
        )
        return
    end

    task.status = "Claimed"
    task.claimedBy = username
    task.claimedAt = getWorldAgeHours()
    task.submittedBy = ""
    task.submittedAt = 0
    task.completedBy = ""
    task.completedAt = 0
    task.validatedBy = ""
    task.validatedAt = 0
    task.weeklyWorkflowVersion = 2
    task.weeklyValidated = false

    qpstCommitWeeklyWorkflow(
        playerObj,
        worldObj,
        args
    )
end

local function weeklySubmitTask(playerObj, args)
    local worldObj, info, task, errorKey =
        qpstResolveWeeklyTask(playerObj, args)

    if errorKey ~= "" then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            errorKey
        )
        return
    end

    local username = getPlayerDisplayName(playerObj)

    if tostring(task.status or "") ~= "Claimed"
        or not qpstWeeklySameUsername(
            task.claimedBy,
            username
        ) then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_WeeklyOnlyClaimantSubmit"
        )
        return
    end

    task.status = "Submitted"
    task.submittedBy = username
    task.submittedAt = getWorldAgeHours()
    task.completedBy = username
    task.completedAt = 0
    task.validatedBy = ""
    task.validatedAt = 0
    task.weeklyWorkflowVersion = 2
    task.weeklyValidated = false

    qpstCommitWeeklyWorkflow(
        playerObj,
        worldObj,
        args
    )
end

local function weeklyValidateTask(playerObj, args)
    local worldObj, info, task, errorKey =
        qpstResolveWeeklyTask(playerObj, args)

    if errorKey ~= "" then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            errorKey
        )
        return
    end

    if not isAdminPlayer(playerObj) then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_WeeklyAdminValidateOnly"
        )
        return
    end

    if tostring(task.status or "") ~= "Submitted" then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_ErrorInvalidWorkflowState"
        )
        return
    end

    local validator = getPlayerDisplayName(playerObj)

    if qpstWeeklySameUsername(
        task.completedBy,
        validator
    ) then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_WeeklySelfValidation"
        )
        return
    end

    task.status = "Done"
    task.completedAt = getWorldAgeHours()
    task.validatedBy = validator
    task.validatedAt = getWorldAgeHours()
    task.weeklyWorkflowVersion = 2
    task.weeklyValidated = true

    qpstCommitWeeklyWorkflow(
        playerObj,
        worldObj,
        args
    )
end
local function claimMoneyReward(playerObj, args)
    local worldObj = resolveWorldObject(args)

    if not worldObj then
        sendStateToPlayer(
            playerObj,
            nil,
            args,
            false,
            "UI_QPST_CreateBoardFailed"
        )
        return
    end

    local info = ensureBoardInfo(worldObj)

    if not info or not canCompleteBoard(playerObj, worldObj, info) then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_ErrorBoardPermission"
        )
        return
    end

    local tasks = getTasks(worldObj)
    local index = findTaskIndex(
        tasks,
        args.taskId,
        args.taskIndex
    )
    local task = index and tasks[index] or nil

    if not task
        or task.weeklyCommunity ~= true
        or tostring(task.status or "") ~= "Done" then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_MoneyRewardUnavailable"
        )
        return
    end

    if task.weeklyValidated ~= true then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_WeeklyUseWorkflow"
        )
        return
    end

    local reward = qpstWeeklyClampInteger(
        task.moneyReward,
        0,
        QPST_WEEKLY_MAX_MONEY,
        0
    )

    if reward <= 0 then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_MoneyRewardUnavailable"
        )
        return
    end

    if task.moneyClaimed == true then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_MoneyAlreadyClaimed"
        )
        return
    end

    local username = getPlayerUsername(playerObj)

    if string.lower(tostring(task.completedBy or ""))
        ~= string.lower(tostring(username or "")) then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_MoneyNotCompleter"
        )
        return
    end

    if not qpstAddMoneyToInventory(playerObj, reward) then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_MoneyPayoutFailed"
        )
        return
    end

    task.moneyClaimed = true
    task.moneyClaimedBy = username
    task.moneyClaimedAt = getWorldAgeHours()

    local data = getData(worldObj)
    bumpRevision(data)
    transmit(worldObj)

    sendStateToPlayer(
        playerObj,
        worldObj,
        args,
        true,
        "UI_QPST_MoneyClaimed"
    )

    broadcastState(worldObj, args)
end
local function removeTask(playerObj, args)
    local worldObj = resolveWorldObject(args)

    if not worldObj then
        sendStateToPlayer(playerObj, nil, args, false, "UI_QPST_CreateBoardFailed")
        return
    end

    local info = ensureBoardInfo(worldObj)

    if not info or not canManageBoard(playerObj, worldObj, info) then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorBoardPermission")
        return
    end

    local tasks = getTasks(worldObj)
    local index = findTaskIndex(tasks, args.taskId, args.taskIndex)

    if index then
        table.remove(tasks, index)
        local data = getData(worldObj)
        bumpRevision(data)
        transmit(worldObj)
        broadcastState(worldObj, args)
        return
    end

    sendStateToPlayer(playerObj, worldObj, args, true, "")
end

local function clearBoard(playerObj, args)
    local worldObj = resolveWorldObject(args)

    if not worldObj then
        sendStateToPlayer(playerObj, nil, args, false, "UI_QPST_CreateBoardFailed")
        return
    end

    local info = ensureBoardInfo(worldObj)

    if not info or not canManageBoard(playerObj, worldObj, info) then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorBoardPermission")
        return
    end

    local data = getData(worldObj)
    data[DATA_KEY] = {}
    bumpRevision(data)
    transmit(worldObj)
    broadcastState(worldObj, args)
end

local function removeBoard(playerObj, args)
    local worldObj = resolveWorldObject(args)

    if not worldObj then
        sendStateToPlayer(playerObj, nil, args, false, "UI_QPST_CreateBoardFailed")
        return
    end

    local info = ensureBoardInfo(worldObj)

    if not info or not canManageBoard(playerObj, worldObj, info) then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorBoardPermission")
        return
    end

    local data = getData(worldObj)
    data[BOARD_KEY] = false
    data[BOARD_INFO_KEY] = nil
    data[DATA_KEY] = {}
    data[TASK_SEQUENCE_KEY] = 0
    bumpRevision(data)
    transmit(worldObj)
    broadcastState(worldObj, args)
end

local COMMAND_HANDLERS = {
    [COMMAND_CREATE_SAFEHOUSE_BOARD] = createBoard,
    [COMMAND_REQUEST_BOARD_STATE] = requestBoardState,
    [COMMAND_REQUEST_NEARBY_BOARDS] = requestNearbyBoards,
    [COMMAND_ADD_TASK] = addTask,
    [COMMAND_COMPLETE_TASK] = completeTask,
    [COMMAND_CLAIM_TASK] = claimTask,
    [COMMAND_SUBMIT_TASK] = submitTask,
    [COMMAND_VALIDATE_TASK] = validateTask,
    [COMMAND_REMOVE_TASK] = removeTask,
    [COMMAND_CLEAR_BOARD] = clearBoard,
    [COMMAND_REMOVE_BOARD] = removeBoard,
    [COMMAND_SET_PUBLIC_BOARD] = setPublicBoardMode,
    [COMMAND_SET_WEEKLY_COMMUNITY] = setWeeklyCommunityMode,
    [COMMAND_ADD_WEEKLY_TASK] = addWeeklyTask,
    [COMMAND_CLAIM_MONEY_REWARD] = claimMoneyReward,
    [COMMAND_WEEKLY_CLAIM_TASK] = weeklyClaimTask,
    [COMMAND_WEEKLY_SUBMIT_TASK] = weeklySubmitTask,
    [COMMAND_WEEKLY_VALIDATE_TASK] = weeklyValidateTask
}

local function onClientCommand(module, command, playerObj, args)
    if module ~= NETWORK_MODULE then return end
    if not playerObj then return end

    local handler = COMMAND_HANDLERS[command]

    if handler then
        handler(playerObj, args or {})
    end
end

Events.OnClientCommand.Add(onClientCommand)

print("[QPST-DEV] v0.6.1 TC Public Board server loaded.")

-- QPST_WEEKLY_TC22_ONE_TIME_REWARD_GUARD
local QPST_TC22_LEDGER_KEY = "QPSurvivorTasks_WeeklyMoneyClaims_v1"
local qpstTC22RewardLocks = {}

local function qpstTC22Ledger()
    if ModData and ModData.getOrCreate then
        local ok, data = pcall(function()
            return ModData.getOrCreate(QPST_TC22_LEDGER_KEY)
        end)
        if ok and type(data) == "table" then return data end
    end

    QPSurvivorTasks_WeeklyMoneyClaims_Runtime =
        QPSurvivorTasks_WeeklyMoneyClaims_Runtime or {}
    return QPSurvivorTasks_WeeklyMoneyClaims_Runtime
end

local function qpstTC22RewardKey(worldObj, task, playerObj)
    local square = worldObj and worldObj:getSquare() or nil
    local x = square and math.floor(tonumber(square:getX()) or 0) or 0
    local y = square and math.floor(tonumber(square:getY()) or 0) or 0
    local z = square and math.floor(tonumber(square:getZ()) or 0) or 0
    local objectIndex = worldObj and worldObj.getObjectIndex
        and worldObj:getObjectIndex() or -1

    local actor = getPlayerUsername(playerObj)
    if tostring(actor or "") == "" then
        actor = getPlayerDisplayName(playerObj)
    end

    return table.concat({
        tostring(x), tostring(y), tostring(z), tostring(objectIndex),
        tostring(getTaskId(task) or ""),
        tostring(task.weeklyCycleId or ""),
        tostring(task.createdAt or ""),
        string.lower(tostring(actor or ""))
    }, "|")
end

local qpstTC22OldClaimMoneyReward = claimMoneyReward

claimMoneyReward = function(playerObj, args)
    local worldObj = resolveWorldObject(args)

    if not worldObj then
        return qpstTC22OldClaimMoneyReward(playerObj, args)
    end

    local tasks = getTasks(worldObj)
    local index = findTaskIndex(tasks, args.taskId, args.taskIndex)
    local task = index and tasks[index] or nil

    if not task or task.weeklyCommunity ~= true then
        return qpstTC22OldClaimMoneyReward(playerObj, args)
    end

    local key = qpstTC22RewardKey(worldObj, task, playerObj)
    local ledger = qpstTC22Ledger()
    local entry = ledger[key]

    if task.moneyClaimed == true
        or entry == true
        or (type(entry) == "table" and entry.claimed == true)
        or qpstTC22RewardLocks[key] == true then

        if task.moneyClaimed ~= true
            and (entry == true or (type(entry) == "table" and entry.claimed == true)) then
            task.moneyClaimed = true
            task.moneyClaimedBy = type(entry) == "table"
                and tostring(entry.claimedBy or "")
                or tostring(getPlayerUsername(playerObj) or "")
            task.moneyClaimedAt = type(entry) == "table"
                and (tonumber(entry.claimedAt) or 0)
                or 0

            local data = getData(worldObj)
            bumpRevision(data)
            transmit(worldObj)
            broadcastState(worldObj, args)
        end

        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_MoneyAlreadyClaimed"
        )
        return
    end

    qpstTC22RewardLocks[key] = true

    qpstTC22OldClaimMoneyReward(playerObj, args)

    if task.moneyClaimed == true then
        ledger[key] = {
            claimed = true,
            claimedBy = tostring(task.moneyClaimedBy or ""),
            claimedAt = tonumber(task.moneyClaimedAt) or 0,
            reward = tonumber(task.moneyReward) or 0,
            taskId = tostring(getTaskId(task) or "")
        }

        if ModData and ModData.transmit then
            pcall(function()
                ModData.transmit(QPST_TC22_LEDGER_KEY)
            end)
        end

        print(
            "[QPST-DEV] Weekly money reward locked: "
            .. tostring(task.moneyReward or 0)
            .. " for "
            .. tostring(task.moneyClaimedBy or "")
        )
    else
        qpstTC22RewardLocks[key] = nil
    end
end

COMMAND_HANDLERS[COMMAND_CLAIM_MONEY_REWARD] = claimMoneyReward

-- QPST_WEEKLY_TC25_AUTOMATIC_REWARD
local QPST_TC25_LEDGER_KEY = "QPSurvivorTasks_WeeklyAutoRewards_v1"
local qpstTC25Locks = {}

local function qpstTC25Norm(v)
    v = tostring(v or "")
    v = string.gsub(v, "^%s+", "")
    v = string.gsub(v, "%s+$", "")
    return string.lower(v)
end

local function qpstTC25Matches(playerObj, expected)
    if not playerObj then return false end
    expected = qpstTC25Norm(expected)
    if expected == "" then return false end

    local username = ""
    local displayName = ""

    if playerObj.getUsername then
        local ok, value = pcall(function() return playerObj:getUsername() end)
        if ok then username = tostring(value or "") end
    end

    if playerObj.getDisplayName then
        local ok, value = pcall(function() return playerObj:getDisplayName() end)
        if ok then displayName = tostring(value or "") end
    end

    if displayName == "" then
        displayName = tostring(getPlayerDisplayName(playerObj) or "")
    end

    return qpstTC25Norm(username) == expected
        or qpstTC25Norm(displayName) == expected
end

local function qpstTC25FindOnline(expected)
    if not getOnlinePlayers then return nil end
    local players = getOnlinePlayers()
    if not players then return nil end

    local okSize, size = pcall(function() return players:size() end)
    if not okSize then return nil end

    for i = 0, (tonumber(size) or 0) - 1 do
        local okPlayer, playerObj = pcall(function() return players:get(i) end)
        if okPlayer and playerObj and qpstTC25Matches(playerObj, expected) then
            return playerObj
        end
    end

    return nil
end

local function qpstTC25Ledger()
    if ModData and ModData.getOrCreate then
        local ok, data = pcall(function() return ModData.getOrCreate(QPST_TC25_LEDGER_KEY) end)
        if ok and type(data) == "table" then return data end
    end

    QPSurvivorTasks_WeeklyAutoRewards_Runtime = QPSurvivorTasks_WeeklyAutoRewards_Runtime or {}
    return QPSurvivorTasks_WeeklyAutoRewards_Runtime
end

local function qpstTC25TransmitLedger()
    if ModData and ModData.transmit then
        pcall(function() ModData.transmit(QPST_TC25_LEDGER_KEY) end)
    end
end

local function qpstTC25TaskKey(worldObj, task)
    local square = worldObj and worldObj:getSquare() or nil
    local x = square and math.floor(tonumber(square:getX()) or 0) or 0
    local y = square and math.floor(tonumber(square:getY()) or 0) or 0
    local z = square and math.floor(tonumber(square:getZ()) or 0) or 0
    local objectIndex = worldObj and worldObj.getObjectIndex and worldObj:getObjectIndex() or -1

    return table.concat({
        tostring(x), tostring(y), tostring(z), tostring(objectIndex),
        tostring(getTaskId(task) or ""),
        tostring(task.weeklyCycleId or ""),
        tostring(task.createdAt or "")
    }, "|")
end

local function qpstTC25Claimant(task)
    local name = tostring(task and task.completedBy or "")
    if name == "" then name = tostring(task and task.claimedBy or "") end
    return name
end

local function qpstTC25Reward(task)
    return qpstWeeklyClampInteger(task and task.moneyReward or 0, 0, QPST_WEEKLY_MAX_MONEY, 0)
end

local function qpstTC25MarkPaid(task, record)
    if not task or type(record) ~= "table" or tostring(record.state or "") ~= "paid" then return false end
    local changed = task.moneyClaimed ~= true or task.moneyRewardPending == true
    task.moneyClaimed = true
    task.moneyClaimedBy = tostring(record.paidTo or record.claimant or "")
    task.moneyClaimedAt = tonumber(record.paidAt) or 0
    task.moneyRewardPending = false
    task.moneyAutoPaid = true
    return changed
end

local function qpstTC25Pay(playerObj, key, record)
    if not playerObj or not key or type(record) ~= "table" then return false end
    if tostring(record.state or "") == "paid" then return true end
    if qpstTC25Locks[key] == true then return false end

    qpstTC25Locks[key] = true
    local reward = qpstWeeklyClampInteger(record.reward, 1, QPST_WEEKLY_MAX_MONEY, 1)
    local paid = qpstAddMoneyToInventory(playerObj, reward)

    if not paid then
        qpstTC25Locks[key] = nil
        return false
    end

    local paidTo = ""
    if playerObj.getUsername then
        local ok, value = pcall(function() return playerObj:getUsername() end)
        if ok then paidTo = tostring(value or "") end
    end
    if paidTo == "" then paidTo = tostring(getPlayerDisplayName(playerObj) or "") end

    record.state = "paid"
    record.paidAt = getWorldAgeHours()
    record.paidTo = paidTo
    qpstTC25TransmitLedger()
    qpstTC25Locks[key] = nil

    print("[QPST-DEV] Weekly automatic reward paid: " .. tostring(reward) .. " Base.Money to " .. tostring(paidTo))
    return true
end

local function qpstTC25Record(worldObj, task)
    local ledger = qpstTC25Ledger()
    local key = qpstTC25TaskKey(worldObj, task)
    local record = ledger[key]

    if type(record) ~= "table" then
        record = {
            state = "pending",
            claimant = qpstTC25Claimant(task),
            reward = qpstTC25Reward(task),
            taskId = tostring(getTaskId(task) or ""),
            weeklyCycleId = tostring(task.weeklyCycleId or ""),
            createdAt = tonumber(task.createdAt) or 0,
            pendingAt = getWorldAgeHours()
        }
        ledger[key] = record
        qpstTC25TransmitLedger()
    end

    return key, record
end

local function qpstTC25ReconcileBoard(worldObj)
    if not worldObj then return false end
    local changed = false
    local ledger = qpstTC25Ledger()

    for _, task in ipairs(getTasks(worldObj)) do
        if task and task.weeklyCommunity == true and tostring(task.status or "") == "Done" then
            local record = ledger[qpstTC25TaskKey(worldObj, task)]
            if qpstTC25MarkPaid(task, record) then changed = true end
        end
    end

    if changed then
        local data = getData(worldObj)
        bumpRevision(data)
        transmit(worldObj)
    end

    return changed
end

local function qpstTC25WeeklyValidateTask(playerObj, args)
    local worldObj, info, task, errorKey = qpstResolveWeeklyTask(playerObj, args)

    if errorKey ~= "" then
        sendStateToPlayer(playerObj, worldObj, args, false, errorKey)
        return
    end

    if not isAdminPlayer(playerObj) then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_WeeklyAdminValidateOnly")
        return
    end

    if tostring(task.status or "") ~= "Submitted" then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_ErrorInvalidWorkflowState")
        return
    end

    local validator = getPlayerDisplayName(playerObj)
    if qpstWeeklySameUsername(task.completedBy, validator) then
        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_WeeklySelfValidation")
        return
    end

    task.status = "Done"
    task.completedAt = getWorldAgeHours()
    task.validatedBy = validator
    task.validatedAt = getWorldAgeHours()
    task.weeklyWorkflowVersion = 2
    task.weeklyValidated = true

    local reward = qpstTC25Reward(task)
    local claimant = qpstTC25Claimant(task)

    if reward > 0 and claimant ~= "" then
        local key, record = qpstTC25Record(worldObj, task)
        local target = qpstTC25FindOnline(claimant)

        if target and qpstTC25Pay(target, key, record) then
            qpstTC25MarkPaid(task, record)
        else
            record.state = "pending"
            record.claimant = claimant
            record.reward = reward
            task.moneyRewardPending = true
            qpstTC25TransmitLedger()

            if target then
                print("[QPST-DEV] Weekly reward remains pending after automatic payout failure for " .. tostring(claimant))
            else
                print("[QPST-DEV] Weekly reward queued for offline survivor: " .. tostring(reward) .. " Base.Money to " .. tostring(claimant))
            end
        end
    else
        task.moneyRewardPending = false
    end

    qpstCommitWeeklyWorkflow(playerObj, worldObj, args)
end

COMMAND_HANDLERS[COMMAND_WEEKLY_VALIDATE_TASK] = qpstTC25WeeklyValidateTask

local function qpstTC25DeliverPendingForPlayer(index, playerObj)
    if not playerObj then return end
    local ledger = qpstTC25Ledger()

    for key, record in pairs(ledger) do
        if type(record) == "table"
            and tostring(record.state or "") == "pending"
            and qpstTC25Matches(playerObj, record.claimant) then
            qpstTC25Pay(playerObj, key, record)
        end
    end
end

if Events and Events.OnCreatePlayer then
    Events.OnCreatePlayer.Add(qpstTC25DeliverPendingForPlayer)
end

local function qpstTC25RetryPendingRewards()
    if not getOnlinePlayers then return end
    local players = getOnlinePlayers()
    if not players then return end
    local okSize, size = pcall(function() return players:size() end)
    if not okSize then return end

    for i = 0, (tonumber(size) or 0) - 1 do
        local okPlayer, playerObj = pcall(function() return players:get(i) end)
        if okPlayer and playerObj then qpstTC25DeliverPendingForPlayer(i, playerObj) end
    end
end

if Events and Events.EveryOneMinute then
    Events.EveryOneMinute.Add(qpstTC25RetryPendingRewards)
end

local qpstTC25OldRequestBoardState = COMMAND_HANDLERS[COMMAND_REQUEST_BOARD_STATE]
COMMAND_HANDLERS[COMMAND_REQUEST_BOARD_STATE] = function(playerObj, args)
    local worldObj = resolveWorldObject(args)
    if worldObj then qpstTC25ReconcileBoard(worldObj) end
    return qpstTC25OldRequestBoardState(playerObj, args)
end

local qpstTC25OldClaimHandler = COMMAND_HANDLERS[COMMAND_CLAIM_MONEY_REWARD]
COMMAND_HANDLERS[COMMAND_CLAIM_MONEY_REWARD] = function(playerObj, args)
    local worldObj = resolveWorldObject(args)
    if not worldObj then return qpstTC25OldClaimHandler(playerObj, args) end

    local tasks = getTasks(worldObj)
    local index = findTaskIndex(tasks, args.taskId, args.taskIndex)
    local task = index and tasks[index] or nil

    if not task or task.weeklyCommunity ~= true then
        return qpstTC25OldClaimHandler(playerObj, args)
    end

    local key = qpstTC25TaskKey(worldObj, task)
    local ledger = qpstTC25Ledger()
    local record = ledger[key]

    if type(record) == "table" and tostring(record.state or "") == "paid" then
        if qpstTC25MarkPaid(task, record) then
            local data = getData(worldObj)
            bumpRevision(data)
            transmit(worldObj)
            broadcastState(worldObj, args)
        end

        sendStateToPlayer(playerObj, worldObj, args, false, "UI_QPST_MoneyAlreadyClaimed")
        return
    end

    qpstTC25OldClaimHandler(playerObj, args)

    if task.moneyClaimed == true then
        if type(record) ~= "table" then
            record = {
                claimant = qpstTC25Claimant(task),
                reward = qpstTC25Reward(task),
                taskId = tostring(getTaskId(task) or "")
            }
            ledger[key] = record
        end

        record.state = "paid"
        record.paidAt = tonumber(task.moneyClaimedAt) or getWorldAgeHours()
        record.paidTo = tostring(task.moneyClaimedBy or "")
        record.reward = qpstTC25Reward(task)
        task.moneyRewardPending = false
        qpstTC25TransmitLedger()
    end
end

-- QPST_WEEKLY_TC26_AUTOREWARD_HARDENING
--
-- Fixes the TC2.5 edge case where an offline claimant could receive the
-- automatic reward on login and still see/use the legacy manual reward
-- command once.  TC2.6 gives every auto-managed reward a stable task-level
-- reward ID, uses a dedicated v2 ledger keyed by stable board coordinates +
-- task identity, and rejects all manual claims for auto-managed Weekly tasks.

local QPST_TC26_LEDGER_KEY =
    "QPSurvivorTasks_WeeklyAutoRewards_v2"

local qpstTC26PayoutLocks = {}

local function qpstTC26Normalize(value)
    value = tostring(value or "")
    value = string.gsub(value, "^%s+", "")
    value = string.gsub(value, "%s+$", "")
    return string.lower(value)
end

local function qpstTC26Claimant(task)
    local value =
        tostring(task and task.completedBy or "")

    if value == "" then
        value =
            tostring(task and task.claimedBy or "")
    end

    return value
end

local function qpstTC26PlayerMatches(
    playerObj,
    expected
)
    if not playerObj then
        return false
    end

    expected =
        qpstTC26Normalize(expected)

    if expected == "" then
        return false
    end

    local username = ""
    local displayName = ""

    if playerObj.getUsername then
        local ok, value = pcall(function()
            return playerObj:getUsername()
        end)

        if ok then
            username = tostring(value or "")
        end
    end

    if playerObj.getDisplayName then
        local ok, value = pcall(function()
            return playerObj:getDisplayName()
        end)

        if ok then
            displayName = tostring(value or "")
        end
    end

    if displayName == "" then
        displayName =
            tostring(
                getPlayerDisplayName(playerObj)
                or ""
            )
    end

    return qpstTC26Normalize(username) == expected
        or qpstTC26Normalize(displayName) == expected
end

local function qpstTC26FindOnlinePlayer(
    expected
)
    if not getOnlinePlayers then
        return nil
    end

    local players =
        getOnlinePlayers()

    if not players then
        return nil
    end

    local okSize, size = pcall(function()
        return players:size()
    end)

    if not okSize then
        return nil
    end

    for index = 0, (tonumber(size) or 0) - 1 do
        local okPlayer, playerObj = pcall(function()
            return players:get(index)
        end)

        if okPlayer
            and playerObj
            and qpstTC26PlayerMatches(
                playerObj,
                expected
            ) then
            return playerObj
        end
    end

    return nil
end

local function qpstTC26Ledger()
    if ModData
        and ModData.getOrCreate then

        local ok, data = pcall(function()
            return ModData.getOrCreate(
                QPST_TC26_LEDGER_KEY
            )
        end)

        if ok
            and type(data) == "table" then
            return data
        end
    end

    QPSurvivorTasks_WeeklyAutoRewardsV2_Runtime =
        QPSurvivorTasks_WeeklyAutoRewardsV2_Runtime
        or {}

    return QPSurvivorTasks_WeeklyAutoRewardsV2_Runtime
end

local function qpstTC26TransmitLedger()
    if ModData
        and ModData.transmit then

        pcall(function()
            ModData.transmit(
                QPST_TC26_LEDGER_KEY
            )
        end)
    end
end

local function qpstTC26RewardAmount(task)
    return qpstWeeklyClampInteger(
        task and task.moneyReward or 0,
        0,
        QPST_WEEKLY_MAX_MONEY,
        0
    )
end

local function qpstTC26StableRewardId(
    worldObj,
    task
)
    if task
        and tostring(
            task.moneyRewardId or ""
        ) ~= "" then
        return tostring(
            task.moneyRewardId
        )
    end

    local square =
        worldObj and worldObj:getSquare() or nil

    local x =
        square and math.floor(
            tonumber(square:getX()) or 0
        ) or 0

    local y =
        square and math.floor(
            tonumber(square:getY()) or 0
        ) or 0

    local z =
        square and math.floor(
            tonumber(square:getZ()) or 0
        ) or 0

    local rewardId =
        table.concat(
            {
                tostring(x),
                tostring(y),
                tostring(z),
                tostring(getTaskId(task) or ""),
                tostring(task.weeklyCycleId or ""),
                tostring(task.createdAt or ""),
                qpstTC26Normalize(
                    qpstTC26Claimant(task)
                )
            },
            "|"
        )

    if task then
        task.moneyRewardId = rewardId
    end

    return rewardId
end

local function qpstTC26MarkPaid(
    task,
    record
)
    if not task
        or type(record) ~= "table"
        or tostring(record.state or "")
            ~= "paid" then
        return false
    end

    local changed =
        task.moneyClaimed ~= true
        or task.moneyRewardPending == true
        or task.moneyAutoPaid ~= true

    task.moneyClaimed = true
    task.moneyClaimedBy =
        tostring(
            record.paidTo
            or record.claimant
            or ""
        )
    task.moneyClaimedAt =
        tonumber(record.paidAt) or 0

    task.moneyRewardPending = false
    task.moneyAutoPaid = true
    task.moneyAutoManaged = true
    task.moneyRewardVersion = 2
    task.moneyRewardId =
        tostring(record.rewardId or task.moneyRewardId or "")

    return changed
end

local function qpstTC26EnsureRecord(
    worldObj,
    task
)
    local ledger =
        qpstTC26Ledger()

    local rewardId =
        qpstTC26StableRewardId(
            worldObj,
            task
        )

    local record =
        ledger[rewardId]

    if type(record) ~= "table" then
        record = {
            rewardId = rewardId,
            state = "pending",
            claimant =
                qpstTC26Claimant(task),
            reward =
                qpstTC26RewardAmount(task),
            taskId =
                tostring(getTaskId(task) or ""),
            weeklyCycleId =
                tostring(task.weeklyCycleId or ""),
            createdAt =
                tonumber(task.createdAt) or 0,
            pendingAt =
                getWorldAgeHours()
        }

        ledger[rewardId] = record
    end

    task.moneyAutoManaged = true
    task.moneyRewardVersion = 2
    task.moneyRewardId = rewardId

    return rewardId, record
end

local function qpstTC26Pay(
    playerObj,
    rewardId,
    record
)
    if not playerObj
        or not rewardId
        or type(record) ~= "table" then
        return false
    end

    if tostring(record.state or "")
        == "paid" then
        return true
    end

    if qpstTC26PayoutLocks[
        rewardId
    ] == true then
        return false
    end

    local reward =
        qpstWeeklyClampInteger(
            record.reward,
            1,
            QPST_WEEKLY_MAX_MONEY,
            1
        )

    qpstTC26PayoutLocks[
        rewardId
    ] = true

    local paid =
        qpstAddMoneyToInventory(
            playerObj,
            reward
        )

    if not paid then
        qpstTC26PayoutLocks[
            rewardId
        ] = nil
        return false
    end

    local paidTo = ""

    if playerObj.getUsername then
        local ok, value = pcall(function()
            return playerObj:getUsername()
        end)

        if ok then
            paidTo =
                tostring(value or "")
        end
    end

    if paidTo == "" then
        paidTo =
            tostring(
                getPlayerDisplayName(playerObj)
                or ""
            )
    end

    record.state = "paid"
    record.paidAt =
        getWorldAgeHours()
    record.paidTo = paidTo

    qpstTC26TransmitLedger()

    qpstTC26PayoutLocks[
        rewardId
    ] = nil

    print(
        "[QPST-DEV] Weekly automatic reward paid TC2.6: "
        .. tostring(reward)
        .. " Base.Money to "
        .. tostring(paidTo)
    )

    return true
end

local function qpstTC26ReconcileBoard(
    worldObj
)
    if not worldObj then
        return false
    end

    local ledger =
        qpstTC26Ledger()

    local changed = false

    for _, task in ipairs(
        getTasks(worldObj)
    ) do
        if task
            and task.weeklyCommunity == true
            and tostring(task.moneyRewardId or "")
                ~= "" then

            local record =
                ledger[
                    tostring(
                        task.moneyRewardId
                    )
                ]

            if qpstTC26MarkPaid(
                task,
                record
            ) then
                changed = true
            end
        end
    end

    if changed then
        local data =
            getData(worldObj)

        bumpRevision(data)
        transmit(worldObj)
    end

    return changed
end

local function qpstTC26WeeklyValidateTask(
    playerObj,
    args
)
    local worldObj, info, task, errorKey =
        qpstResolveWeeklyTask(
            playerObj,
            args
        )

    if errorKey ~= "" then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            errorKey
        )
        return
    end

    if not isAdminPlayer(playerObj) then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_WeeklyAdminValidateOnly"
        )
        return
    end

    if tostring(task.status or "")
        ~= "Submitted" then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_ErrorInvalidWorkflowState"
        )
        return
    end

    local validator =
        getPlayerDisplayName(playerObj)

    if qpstWeeklySameUsername(
        task.completedBy,
        validator
    ) then
        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_WeeklySelfValidation"
        )
        return
    end

    task.status = "Done"
    task.completedAt =
        getWorldAgeHours()
    task.validatedBy =
        validator
    task.validatedAt =
        getWorldAgeHours()
    task.weeklyWorkflowVersion = 2
    task.weeklyValidated = true

    local reward =
        qpstTC26RewardAmount(task)

    local claimant =
        qpstTC26Claimant(task)

    if reward > 0
        and claimant ~= "" then

        local rewardId, record =
            qpstTC26EnsureRecord(
                worldObj,
                task
            )

        record.state =
            tostring(record.state or "pending")
        record.claimant = claimant
        record.reward = reward
        record.rewardId = rewardId

        local target =
            qpstTC26FindOnlinePlayer(
                claimant
            )

        if target then
            if qpstTC26Pay(
                target,
                rewardId,
                record
            ) then
                qpstTC26MarkPaid(
                    task,
                    record
                )
            else
                task.moneyRewardPending = true
            end
        else
            record.state = "pending"
            task.moneyRewardPending = true
            task.moneyClaimed = false
            task.moneyAutoPaid = false

            qpstTC26TransmitLedger()

            print(
                "[QPST-DEV] Weekly reward queued TC2.6 for offline survivor: "
                .. tostring(reward)
                .. " Base.Money to "
                .. tostring(claimant)
            )
        end
    else
        task.moneyRewardPending = false
        task.moneyAutoManaged = true
        task.moneyRewardVersion = 2
    end

    qpstCommitWeeklyWorkflow(
        playerObj,
        worldObj,
        args
    )
end

COMMAND_HANDLERS[
    COMMAND_WEEKLY_VALIDATE_TASK
] = qpstTC26WeeklyValidateTask

local function qpstTC26DeliverPendingForPlayer(
    index,
    playerObj
)
    if not playerObj then
        return
    end

    local ledger =
        qpstTC26Ledger()

    for rewardId, record in pairs(ledger) do
        if type(record) == "table"
            and tostring(record.state or "")
                == "pending"
            and qpstTC26PlayerMatches(
                playerObj,
                record.claimant
            ) then

            qpstTC26Pay(
                playerObj,
                rewardId,
                record
            )
        end
    end
end

if Events
    and Events.OnCreatePlayer then

    Events.OnCreatePlayer.Add(
        qpstTC26DeliverPendingForPlayer
    )
end

local function qpstTC26RetryPending()
    if not getOnlinePlayers then
        return
    end

    local players =
        getOnlinePlayers()

    if not players then
        return
    end

    local okSize, size =
        pcall(function()
            return players:size()
        end)

    if not okSize then
        return
    end

    for index = 0, (tonumber(size) or 0) - 1 do
        local okPlayer, playerObj =
            pcall(function()
                return players:get(index)
            end)

        if okPlayer
            and playerObj then

            qpstTC26DeliverPendingForPlayer(
                index,
                playerObj
            )
        end
    end
end

if Events
    and Events.EveryOneMinute then

    Events.EveryOneMinute.Add(
        qpstTC26RetryPending
    )
end

local qpstTC26OldRequestBoardState =
    COMMAND_HANDLERS[
        COMMAND_REQUEST_BOARD_STATE
    ]

COMMAND_HANDLERS[
    COMMAND_REQUEST_BOARD_STATE
] = function(playerObj, args)
    local worldObj =
        resolveWorldObject(args)

    if worldObj then
        qpstTC26ReconcileBoard(
            worldObj
        )
    end

    return qpstTC26OldRequestBoardState(
        playerObj,
        args
    )
end

local qpstTC26OldClaimHandler =
    COMMAND_HANDLERS[
        COMMAND_CLAIM_MONEY_REWARD
    ]

COMMAND_HANDLERS[
    COMMAND_CLAIM_MONEY_REWARD
] = function(playerObj, args)
    local worldObj =
        resolveWorldObject(args)

    if not worldObj then
        return qpstTC26OldClaimHandler(
            playerObj,
            args
        )
    end

    local tasks =
        getTasks(worldObj)

    local taskIndex =
        findTaskIndex(
            tasks,
            args.taskId,
            args.taskIndex
        )

    local task =
        taskIndex and tasks[taskIndex] or nil

    if not task
        or task.weeklyCommunity ~= true then

        return qpstTC26OldClaimHandler(
            playerObj,
            args
        )
    end

    local autoManaged =
        task.moneyAutoManaged == true
        or task.moneyAutoPaid == true
        or task.moneyRewardPending == true
        or tonumber(task.moneyRewardVersion or 0) >= 2
        or tostring(task.moneyRewardId or "") ~= ""

    if not autoManaged then
        return qpstTC26OldClaimHandler(
            playerObj,
            args
        )
    end

    local rewardId =
        tostring(
            task.moneyRewardId or ""
        )

    local record = nil

    if rewardId ~= "" then
        record =
            qpstTC26Ledger()[
                rewardId
            ]
    end

    if type(record) == "table"
        and tostring(record.state or "")
            == "paid" then

        if qpstTC26MarkPaid(
            task,
            record
        ) then
            local data =
                getData(worldObj)

            bumpRevision(data)
            transmit(worldObj)
            broadcastState(
                worldObj,
                args
            )
        end

        sendStateToPlayer(
            playerObj,
            worldObj,
            args,
            false,
            "UI_QPST_MoneyAlreadyClaimed"
        )

        return
    end

    -- Pending automatic rewards are never paid by the legacy board button.
    -- They are delivered by the automatic login/retry path only.
    sendStateToPlayer(
        playerObj,
        worldObj,
        args,
        false,
        "UI_QPST_MoneyRewardUnavailable"
    )
end
