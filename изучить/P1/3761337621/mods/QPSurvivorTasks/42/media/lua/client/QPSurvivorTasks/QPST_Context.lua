-- QPST_BUILD41_BACKPORT_V1
-- Build 41 runtime synchronized with the v0.5.1 dual-build implementation.
-- QP Survivor Tasks
-- QPST_CUSTOM_TASK_UI_REFLOW_V060_TC1
-- QPST_UI_I18N_LAYOUT_HOTFIX_V060_TC2
-- QPST_RESIZABLE_CUSTOM_TASK_V060_TC3
-- QPST_SAY_LOCALIZED_LOAD_ORDER_FIX_V060_TC4
-- QPST_WORKFLOW_LABEL_LOAD_ORDER_FIX_V060_TC5
-- v0.5.2 resizable scrollable task board UI
-- QPST_LOCALIZATION_V1
-- QPST_NATIVE_TRANSLATIONS_V1
-- QPST_BOARD_METADATA_V2
-- QPST_METADATA_UI_LAYOUT_V1
-- QPST_SAFEHOUSE_PLACEMENT_V1
-- QPST_SERVER_AUTH_CREATE_V1
-- QPST_ADMIN_ANYWHERE_V1
-- QPST_SERVER_AUTHORITATIVE_MUTATIONS_V1
-- QPST_BOARD_STATE_SYNC_V1
-- QPST_FIRST_COMPLETION_WINS_V1
-- QPST_ADMIN_BOARD_PROTECTION_V1
-- QPST_COMPLETE_SELECTED_TASK_UI_V1
-- QPST_REPUTATION_WORKFLOW_UI_V060
-- QPST_PUBLIC_BOARD_V061_TC1
-- QPST_PUBLIC_BOARD_UI_TC2
-- QPST_B41_I18N_HARDENING_V061
-- QPST_WEEKLY_COMMUNITY_V070_TC1_FOUNDATION
-- QPST_WEEKLY_VERIFICATION_V070_TC2

require "ISUI/ISCollapsableWindow"
require "ISUI/ISButton"
require "ISUI/ISTextEntryBox"
require "ISUI/ISContextMenu"
require "ISUI/ISScrollingListBox"
require "ISUI/ISComboBox"

QPSurvivorTasks = QPSurvivorTasks or {}

local MOD_NAME_KEY = "UI_QPST_ModName"
local DATA_KEY = "QPST_Tasks"
local BOARD_KEY = "QPST_IsBoard"
local BOARD_INFO_KEY = "QPST_BoardInfo"

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

local TASK_SEQUENCE_KEY = "QPST_NextTaskId"
local STATE_REVISION_KEY = "QPST_StateRevision"

local BOARD_SCHEMA_VERSION = 3

local BOARD_TYPE_SAFEHOUSE = "Safehouse"
local BOARD_TYPE_ADMIN = "Admin"

local PERMISSION_MODE_EVERYONE = "Everyone"
local PERMISSION_MODE_SAFEHOUSE = "SafehouseMembers"
local PERMISSION_MODE_ADMIN = "AdminOnly"

QPSurvivorTasks.BoardSchemaVersion = BOARD_SCHEMA_VERSION
QPSurvivorTasks.BoardTypes = {
    Safehouse = BOARD_TYPE_SAFEHOUSE,
    Admin = BOARD_TYPE_ADMIN
}
QPSurvivorTasks.PermissionModes = {
    Everyone = PERMISSION_MODE_EVERYONE,
    SafehouseMembers = PERMISSION_MODE_SAFEHOUSE,
    AdminOnly = PERMISSION_MODE_ADMIN
}

QPSurvivorTasks.BoardStateCache = QPSurvivorTasks.BoardStateCache or {}
QPSurvivorTasks.PendingStateRequests = QPSurvivorTasks.PendingStateRequests or {}
QPSurvivorTasks.LastNearbySyncChunk = QPSurvivorTasks.LastNearbySyncChunk or {}

-- QPST_MULTILINGUAL_PRESETS_V1
local QUICK_TASKS = {
    {
        key = "UI_QPST_PresetNeedFuel",
        fallback = "Need fuel"
    },
    {
        key = "UI_QPST_PresetNeedFood",
        fallback = "Need food"
    },
    {
        key = "UI_QPST_PresetNeedMedicine",
        fallback = "Need medicine"
    },
    {
        key = "UI_QPST_PresetRepairGenerator",
        fallback = "Repair generator"
    },
    {
        key = "UI_QPST_PresetClearZombies",
        fallback = "Clear zombies nearby"
    },
    {
        key = "UI_QPST_PresetAreaNotSafe",
        fallback = "Area not safe"
    }
}

local QUICK_TASK_KEY_BY_FALLBACK = {}

for i = 1, #QUICK_TASKS do
    local preset = QUICK_TASKS[i]
    QUICK_TASK_KEY_BY_FALLBACK[preset.fallback] = preset.key
end

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

local function getBoardTarget(worldobjects)
    if not worldobjects then return nil end

    for i = 1, #worldobjects do
        local obj = worldobjects[i]

        if obj and isValidBoardTarget(obj) then
            return obj
        end
    end

    return nil
end

local function getPlayerName(playerObj)
    if not playerObj then return "Unknown" end

    if playerObj.getUsername then
        local username = playerObj:getUsername()
        if username and username ~= "" then
            return username
        end
    end

    if playerObj.getDescriptor and playerObj:getDescriptor() then
        local forename = playerObj:getDescriptor():getForename() or ""
        local surname = playerObj:getDescriptor():getSurname() or ""
        local fullName = forename .. " " .. surname

        if fullName ~= " " then
            return fullName
        end
    end

    return "Unknown"
end

local function getPlayerUsername(playerObj)
    if not playerObj then return "" end

    if playerObj.getUsername then
        local username = playerObj:getUsername()

        if username and username ~= "" then
            return tostring(username)
        end
    end

    return getPlayerName(playerObj)
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

local function getObjectLocation(worldObj)
    if not worldObj or not worldObj.getSquare then
        return 0, 0, 0
    end

    local square = worldObj:getSquare()

    if not square then
        return 0, 0, 0
    end

    local x = square.getX and square:getX() or 0
    local y = square.getY and square:getY() or 0
    local z = square.getZ and square:getZ() or 0

    return x, y, z
end


local function isMultiplayerClient()
    if not isClient then
        return false
    end

    local ok, result = pcall(function()
        return isClient()
    end)

    return ok and result == true
end

local function getObjectIndex(worldObj)
    if not worldObj or not worldObj.getObjectIndex then
        return -1
    end

    local ok, index = pcall(function()
        return worldObj:getObjectIndex()
    end)

    if not ok or tonumber(index) == nil then
        return -1
    end

    return math.floor(tonumber(index))
end

local function getObjectCommandArgs(worldObj)
    local x, y, z = getObjectLocation(worldObj)

    return {
        x = x,
        y = y,
        z = z,
        objectIndex = getObjectIndex(worldObj),
        spriteName = getSpriteName(worldObj)
    }
end

local function getObjectStateKeyFromValues(x, y, z, objectIndex, spriteName)
    return table.concat({
        tostring(math.floor(tonumber(x) or 0)),
        tostring(math.floor(tonumber(y) or 0)),
        tostring(math.floor(tonumber(z) or 0)),
        tostring(math.floor(tonumber(objectIndex) or -1)),
        tostring(spriteName or "")
    }, ":")
end

local function getObjectStateKey(worldObj)
    local args = getObjectCommandArgs(worldObj)

    return getObjectStateKeyFromValues(
        args.x,
        args.y,
        args.z,
        args.objectIndex,
        args.spriteName
    )
end

local function mergeCommandArgs(worldObj, extra)
    local args = getObjectCommandArgs(worldObj)

    if type(extra) == "table" then
        for key, value in pairs(extra) do
            args[key] = value
        end
    end

    return args
end

local function sendBoardCommand(playerObj, command, worldObj, extra)
    if not playerObj or not worldObj or not command then
        return false
    end

    local args = mergeCommandArgs(worldObj, extra)

    if tonumber(args.objectIndex) == nil or tonumber(args.objectIndex) < 0 then
        if playerObj and playerObj.Say then
            playerObj:Say(getText("UI_QPST_CreateBoardFailed"))
        end
        return false
    end

    sendClientCommand(playerObj, NETWORK_MODULE, command, args)
    return true
end

local function getObjectAtLocation(x, y, z, objectIndex, expectedSprite)
    if not getCell or not getCell() then
        return nil
    end

    local square = getCell():getGridSquare(
        math.floor(tonumber(x) or 0),
        math.floor(tonumber(y) or 0),
        math.floor(tonumber(z) or 0)
    )

    if not square or not square.getObjects then
        return nil
    end

    local objects = square:getObjects()
    local index = math.floor(tonumber(objectIndex) or -1)

    if not objects or index < 0 or index >= objects:size() then
        return nil
    end

    local worldObj = objects:get(index)

    if not worldObj then
        return nil
    end

    local spriteName = tostring(expectedSprite or "")

    if spriteName ~= "" and getSpriteName(worldObj) ~= spriteName then
        return nil
    end

    return worldObj
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
        local okOwner, isOwnerResult = pcall(function()
            return safehouse:isOwner(playerObj)
        end)

        if okOwner and isOwnerResult == true then
            return true
        end
    end

    if safehouse.playerAllowed then
        local okPlayer, playerAllowedResult = pcall(function()
            return safehouse:playerAllowed(playerObj)
        end)

        if okPlayer and playerAllowedResult == true then
            return true
        end

        local username = getPlayerUsername(playerObj)

        if username ~= "" then
            local okName, nameAllowedResult = pcall(function()
                return safehouse:playerAllowed(username)
            end)

            if okName and nameAllowedResult == true then
                return true
            end
        end
    end

    return false
end

local function getSafehousePlacementState(playerObj, worldObj)
    if not playerObj or not worldObj or not worldObj.getSquare then
        return false, "UI_QPST_CreateBoardFailed", nil
    end

    local square = worldObj:getSquare()
    local safehouse = getSafehouseForSquare(square)

    -- Administrators may create boards anywhere. The server performs
    -- the same check and remains authoritative in multiplayer.
    if isAdminPlayer(playerObj) then
        return true, nil, safehouse
    end

    if not safehouse then
        return false, "UI_QPST_ErrorSafehouseRequired", nil
    end

    if not isPlayerAllowedInSafehouse(playerObj, safehouse) then
        return false, "UI_QPST_ErrorSafehousePermission", safehouse
    end

    return true, nil, safehouse
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

local function sayLocalized(playerObj, key)
    if playerObj == nil or key == nil or key == "" then
        return
    end

    local value = getText and getText(key) or nil

    if value == nil or value == "" or value == key then
        local fallbacks = {
            UI_QPST_WeeklyButtonClaim =
                "Claim Weekly Task",
            UI_QPST_WeeklyButtonSubmit =
                "Submit Weekly Task for Review",
            UI_QPST_WeeklyButtonValidate =
                "Validate Weekly Task",
            UI_QPST_WeeklyUseWorkflow =
                "Weekly tasks must be claimed, submitted, and validated before completion.",
            UI_QPST_WeeklyCreatorCannotClaim =
                "The weekly task creator cannot claim their own task.",
            UI_QPST_WeeklyOnlyClaimantSubmit =
                "Only the survivor who claimed this weekly task can submit it.",
            UI_QPST_WeeklyAdminValidateOnly =
                "Only an administrator can validate a Weekly Community task.",
            UI_QPST_WeeklySelfValidation =
                "You cannot validate your own Weekly Community submission.",            UI_QPST_EnableWeeklyCommunity =
                "Enable Weekly Community Mode",
            UI_QPST_DisableWeeklyCommunity =
                "Disable Weekly Community Mode",
            UI_QPST_WeeklyCommunityEnabled =
                "Weekly Community mode enabled. Only administrators can post weekly tasks.",
            UI_QPST_WeeklyCommunityDisabled =
                "Weekly Community mode disabled.",
            UI_QPST_WeeklyAdminOnly =
                "Only administrators can manage Weekly Community mode.",
            UI_QPST_WeeklyPublicConflict =
                "Disable Weekly Community mode before enabling Public Task Creation.",
            UI_QPST_WeeklyTaskLimitReached =
                "This week's Community task limit has been reached.",
            UI_QPST_MoneyClaimed =
                "Money reward claimed.",
            UI_QPST_MoneyAlreadyClaimed =
                "This money reward was already claimed.",
            UI_QPST_MoneyNotCompleter =
                "Only the survivor who completed this task can claim its reward.",
            UI_QPST_MoneyRewardUnavailable =
                "This task has no claimable money reward.",
            UI_QPST_MoneyPayoutFailed =
                "Money reward could not be added to your inventory.",            UI_QPST_EnablePublicBoard =
                "Enable Public Task Creation",
            UI_QPST_DisablePublicBoard =
                "Disable Public Task Creation",
            UI_QPST_PublicBoardEnabled =
                "Public task creation enabled. Any player may add standard tasks.",
            UI_QPST_PublicBoardDisabled =
                "Public task creation disabled.",
            UI_QPST_PublicStandardOnly =
                "Public contributors can create standard tasks only.",
            UI_QPST_ErrorPublicBoardAdminOnly =
                "Only administrators can change public board mode.",            UI_QPST_ErrorInvalidReputation =
                "Invalid Reputation task settings.",
            UI_QPST_ErrorReputationPermission =
                "Only a safehouse owner or administrator may create Reputation tasks.",
            UI_QPST_ErrorUseWorkflow =
                "Use Claim, Submit and Validate for Reputation tasks.",
            UI_QPST_ErrorInvalidWorkflowState =
                "This task is not in a valid workflow state.",
            UI_QPST_ErrorCreatorCannotClaim =
                "The task creator cannot claim their own Reputation task.",
            UI_QPST_ErrorOnlyClaimantSubmit =
                "Only the recorded claimant can submit this task.",
            UI_QPST_ErrorSelfValidation =
                "You cannot validate your own task submission.",
            UI_QPST_ErrorCreatorValidation =
                "The task creator cannot validate this Reputation task."
        }

        value = fallbacks[key] or key
    end

    playerObj:Say(value)
end

local function getWorldAgeHours()
    if getGameTime and getGameTime() then
        return getGameTime():getWorldAgeHours()
    end

    return 0
end

local function formatAge(createdAt)
    local now = getWorldAgeHours()
    local diff = math.floor(now - (createdAt or now))

    if diff <= 0 then
        return getText("UI_QPST_AgeJustNow")
    end

    if diff == 1 then
        return getText("UI_QPST_AgeOneHour")
    end

    if diff < 24 then
        return getText(
            "UI_QPST_AgeHours",
            tostring(diff)
        )
    end

    local days = math.floor(diff / 24)

    if days == 1 then
        return getText("UI_QPST_AgeOneDay")
    end

    return getText(
        "UI_QPST_AgeDays",
        tostring(days)
    )
end

local function getDisplayName(value)
    local name = tostring(value or "")

    if name == "" or name == "Unknown" then
        return getText("UI_QPST_Unknown")
    end

    return name
end

local function saveObject(worldObj)
    if worldObj and worldObj.transmitModData then
        worldObj:transmitModData()
    end
end

local function getData(worldObj)
    if not worldObj then return nil end
    return worldObj:getModData()
end

local function isKnownBoardType(value)
    return value == BOARD_TYPE_SAFEHOUSE
        or value == BOARD_TYPE_ADMIN
end

local function isKnownPermissionMode(value)
    return value == PERMISSION_MODE_EVERYONE
        or value == PERMISSION_MODE_SAFEHOUSE
        or value == PERMISSION_MODE_ADMIN
end

local function createBoardInfo(playerObj, worldObj, safehouse)
    local ownerDisplayName = getPlayerName(playerObj)
    local ownerUsername = getPlayerUsername(playerObj)
    local x, y, z = getObjectLocation(worldObj)
    local adminBoard = isAdminPlayer(playerObj)

    return {
        schemaVersion = BOARD_SCHEMA_VERSION,
        boardType = adminBoard and BOARD_TYPE_ADMIN or BOARD_TYPE_SAFEHOUSE,
        permissionMode = adminBoard and PERMISSION_MODE_ADMIN or PERMISSION_MODE_SAFEHOUSE,

        ownerUsername = ownerUsername,
        ownerDisplayName = ownerDisplayName,

        createdBy = ownerDisplayName,
        createdAt = getWorldAgeHours(),

        locationX = x,
        locationY = y,
        locationZ = z,

        safehouseOwner = getSafehouseOwner(safehouse),
        safehouseTitle = getSafehouseTitle(safehouse),

        legacyImported = false
    }
end

local function ensureBoardInfo(worldObj, playerObj)
    local data = getData(worldObj)

    if not data or data[BOARD_KEY] ~= true then
        return nil
    end

    local changed = false
    local info = data[BOARD_INFO_KEY]

    if type(info) ~= "table" then
        info = {}
        data[BOARD_INFO_KEY] = info
        changed = true
    end

    local wasLegacy = info.schemaVersion == nil

    if info.schemaVersion ~= BOARD_SCHEMA_VERSION then
        info.schemaVersion = BOARD_SCHEMA_VERSION
        changed = true
    end

    if not isKnownBoardType(info.boardType) then
        info.boardType = BOARD_TYPE_SAFEHOUSE
        changed = true
    end

    if info.boardType == BOARD_TYPE_ADMIN then
        if info.permissionMode ~= PERMISSION_MODE_ADMIN then
            info.permissionMode = PERMISSION_MODE_ADMIN
            changed = true
        end
    elseif not isKnownPermissionMode(info.permissionMode) then
        info.permissionMode = PERMISSION_MODE_SAFEHOUSE
        changed = true
    end

    local fallbackDisplayName = tostring(info.createdBy or "")

    if fallbackDisplayName == "" and playerObj then
        fallbackDisplayName = getPlayerName(playerObj)
    end

    if tostring(info.ownerDisplayName or "") == "" then
        info.ownerDisplayName = fallbackDisplayName
        changed = true
    end

    if tostring(info.ownerUsername or "") == "" then
        if playerObj then
            info.ownerUsername = getPlayerUsername(playerObj)
        else
            info.ownerUsername = fallbackDisplayName
        end

        changed = true
    end

    if tostring(info.createdBy or "") == "" then
        info.createdBy = tostring(info.ownerDisplayName or "")
        changed = true
    end

    if type(info.createdAt) ~= "number" then
        info.createdAt = getWorldAgeHours()
        changed = true
    end

    local x, y, z = getObjectLocation(worldObj)

    if type(info.locationX) ~= "number" then
        info.locationX = x
        changed = true
    end

    if type(info.locationY) ~= "number" then
        info.locationY = y
        changed = true
    end

    if type(info.locationZ) ~= "number" then
        info.locationZ = z
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
        info.legacyImported = wasLegacy
        changed = true
    end

    if changed and not isMultiplayerClient() then
        saveObject(worldObj)
    end

    return info
end

QPSurvivorTasks.getBoardInfo = ensureBoardInfo

local function isBoard(worldObj)
    local data = getData(worldObj)
    if not data then return false end
    return data[BOARD_KEY] == true
end

local function getTasks(worldObj)
    local data = getData(worldObj)
    if not data then return nil end

    if not data[DATA_KEY] then
        data[DATA_KEY] = {}
    end

    return data[DATA_KEY]
end

local function getTaskId(task)
    if not task then return "" end
    return tostring(task.id or task.taskId or "")
end

local function nextLocalTaskId(worldObj, data)
    local sequence = math.floor(tonumber(data[TASK_SEQUENCE_KEY]) or 0) + 1
    data[TASK_SEQUENCE_KEY] = sequence

    local x, y, z = getObjectLocation(worldObj)
    return table.concat({tostring(x), tostring(y), tostring(z), tostring(sequence)}, "-")
end

local function ensureLocalTaskIds(worldObj)
    if isMultiplayerClient() then
        return false
    end

    local data = getData(worldObj)
    local tasks = getTasks(worldObj)

    if not data or not tasks then return false end

    local changed = false

    for i = 1, #tasks do
        local task = tasks[i]

        if task and getTaskId(task) == "" then
            task.id = nextLocalTaskId(worldObj, data)
            changed = true
        elseif task and task.id == nil then
            task.id = getTaskId(task)
            changed = true
        end
    end

    if changed and not isMultiplayerClient() then
        saveObject(worldObj)
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

local function canManageBoard(playerObj, worldObj)
    if not playerObj or not worldObj or not isBoard(worldObj) then
        return false
    end

    if isAdminPlayer(playerObj) then
        return true
    end

    local info = ensureBoardInfo(worldObj, playerObj)

    if not info then
        return false
    end

    if info.boardType == BOARD_TYPE_ADMIN or info.permissionMode == PERMISSION_MODE_ADMIN then
        return false
    end

    local square = worldObj:getSquare()
    local safehouse = getSafehouseForSquare(square)

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

local function canAddTask(playerObj, worldObj)
    if not playerObj or not worldObj or not isBoard(worldObj) then
        return false
    end

    if isAdminPlayer(playerObj) then
        return true
    end

    local info = ensureBoardInfo(worldObj, playerObj)

    if not info then
        return false
    end

    if isPublicBoardInfo(info) then
        return true
    end

    return canManageBoard(playerObj, worldObj)
end

local function canCompleteBoard(playerObj, worldObj)
    if not playerObj or not worldObj or not isBoard(worldObj) then
        return false
    end

    if isAdminPlayer(playerObj) then
        return true
    end

    local info = ensureBoardInfo(worldObj, playerObj)

    if not info then
        return false
    end

    -- Public administrator boards are readable and completable by everyone,
    -- but only administrators may add, edit, clear, or delete content.
    if info.boardType == BOARD_TYPE_ADMIN or info.permissionMode == PERMISSION_MODE_ADMIN then
        return true
    end

    local square = worldObj:getSquare()
    local safehouse = getSafehouseForSquare(square)

    if safehouse then
        return isPlayerAllowedInSafehouse(playerObj, safehouse)
    end

    -- Existing boards remain usable after a safehouse is released.
    return true
end

QPSurvivorTasks.canManageBoard = canManageBoard
QPSurvivorTasks.canAddTask = canAddTask
QPSurvivorTasks.canCompleteBoard = canCompleteBoard

local function getTaskDisplayText(task)
    if not task then return "" end

    local translationKey = tostring(task.translationKey or "")

    if translationKey == "" then
        translationKey =
            QUICK_TASK_KEY_BY_FALLBACK[tostring(task.text or "")]
            or ""
    end

    local text = translationKey ~= "" and getText(translationKey) or tostring(task.text or "")
    if task.reputationEnabled == true then
        local status = tostring(task.status or "Open")
        local who = tostring(task.claimedBy or task.completedBy or "")
        local reward = string.upper(tostring(task.reputationPath or "")) .. " +" .. tostring(task.reputationPoints or 0)
        if who ~= "" then
            text = text .. " [" .. status .. ": " .. who .. "] [" .. reward .. "]"
        else
            text = text .. " [" .. status .. "] [" .. reward .. "]"
        end
    end
    return text
end

local function countOpenTasks(tasks)
    local count = 0

    if not tasks then return count end

    for i = 1, #tasks do
        if tasks[i] and tasks[i].status ~= "Done" then
            count = count + 1
        end
    end

    return count
end

local function countDoneTasks(tasks)
    local count = 0

    if not tasks then return count end

    for i = 1, #tasks do
        if tasks[i] and tasks[i].status == "Done" then
            count = count + 1
        end
    end

    return count
end

local function addTaskData(playerObj, worldObj, taskText, translationKey, reputationEnabled, reputationDifficulty, reputationPath)
    if not playerObj or not worldObj or not taskText then return false end
    if not canAddTask(playerObj, worldObj) then return false end

    taskText = tostring(taskText)
    taskText = string.gsub(taskText, "^%s+", "")
    taskText = string.gsub(taskText, "%s+$", "")

    if taskText == "" then return false end

    if string.len(taskText) > 80 then
        taskText = string.sub(taskText, 1, 80)
    end

    local data = getData(worldObj)
    local tasks = getTasks(worldObj)

    if not data or not tasks then return false end

    table.insert(tasks, {
        id = nextLocalTaskId(worldObj, data),
        text = taskText,
        status = "Open",
        createdBy = getPlayerName(playerObj),
        createdAt = getWorldAgeHours(),
        completedBy = "",
        completedAt = 0,
        translationKey = tostring(translationKey or ""),
        reputationEnabled = reputationEnabled == true,
        reputationDifficulty = tostring(reputationDifficulty or ""),
        reputationPath = string.lower(tostring(reputationPath or "")),
        reputationPoints = ({Easy=1, Normal=2, Hard=3})[tostring(reputationDifficulty or "")] or 0,
        claimedBy = "", claimedAt = 0,
        submittedBy = "", submittedAt = 0,
        validatedBy = "", validatedAt = 0,
        reputationAwarded = false, reputationResult = ""
    })

    data[STATE_REVISION_KEY] = math.floor(tonumber(data[STATE_REVISION_KEY]) or 0) + 1
    saveObject(worldObj)
    return true
end

local function markTaskDoneData(playerObj, worldObj, taskId, taskIndex)
    if not playerObj or not worldObj then return false end
    if not canCompleteBoard(playerObj, worldObj) then return false end

    ensureLocalTaskIds(worldObj)

    local tasks = getTasks(worldObj)
    local index = findTaskIndex(tasks, taskId, taskIndex)
    local task = index and tasks[index] or nil

    if not task or task.status == "Done" then
        return false
    end

    task.status = "Done"
    task.completedBy = getPlayerName(playerObj)
    task.completedAt = getWorldAgeHours()

    local data = getData(worldObj)
    data[STATE_REVISION_KEY] = math.floor(tonumber(data[STATE_REVISION_KEY]) or 0) + 1
    saveObject(worldObj)
    return true
end

local function removeTaskData(playerObj, worldObj, taskId, taskIndex)
    if not playerObj or not worldObj then return false end
    if not canManageBoard(playerObj, worldObj) then return false end

    ensureLocalTaskIds(worldObj)

    local tasks = getTasks(worldObj)
    local index = findTaskIndex(tasks, taskId, taskIndex)

    if not index then return false end

    table.remove(tasks, index)

    local data = getData(worldObj)
    data[STATE_REVISION_KEY] = math.floor(tonumber(data[STATE_REVISION_KEY]) or 0) + 1
    saveObject(worldObj)
    return true
end

local function taskWorkflowCommand(playerObj, task)
    if not task then return nil end
    if task.reputationEnabled ~= true then return COMMAND_COMPLETE_TASK end
    local username = string.lower(getPlayerUsername(playerObj))
    local status = tostring(task.status or "Open")
    if status == "Open" then return COMMAND_CLAIM_TASK end
    if status == "Claimed" and string.lower(tostring(task.claimedBy or "")) == username then return COMMAND_SUBMIT_TASK end
    if status == "Submitted" and string.lower(tostring(task.completedBy or "")) ~= username and string.lower(tostring(task.createdBy or "")) ~= username then return COMMAND_VALIDATE_TASK end
    return nil
end

local function taskWorkflowLabel(playerObj, task)
    local function label(key, fallback)
        local value = getText and getText(key) or nil

        if value == nil or value == "" or value == key then
            return fallback
        end

        return value
    end

    local command = taskWorkflowCommand(playerObj, task)

    if command == COMMAND_CLAIM_TASK then
        return label("UI_QPST_ButtonClaim", "Claim Selected Task")
    end

    if command == COMMAND_SUBMIT_TASK then
        return label("UI_QPST_ButtonSubmit", "Submit for Review")
    end

    if command == COMMAND_VALIDATE_TASK then
        return label("UI_QPST_ButtonValidate", "Validate Selected Task")
    end

    if command == COMMAND_COMPLETE_TASK then
        return getText("UI_QPST_ButtonDone")
    end

    return label("UI_QPST_ButtonUnavailable", "Action Unavailable")
end

local QPST_TaskBoardWindow = ISCollapsableWindow:derive("QPST_TaskBoardWindow")

-- QPST_RESIZABLE_SCROLLABLE_BOARD_UI_V1

local function fitQPSTText(text, font, maxWidth)
    text = tostring(text or "")
    maxWidth = math.max(40, tonumber(maxWidth) or 40)

    if getTextManager():MeasureStringX(font, text) <= maxWidth then
        return text
    end

    local suffix = "..."
    local low = 0
    local high = string.len(text)

    while low < high do
        local mid = math.ceil((low + high) / 2)
        local candidate = string.sub(text, 1, mid) .. suffix

        if getTextManager():MeasureStringX(font, candidate) <= maxWidth then
            low = mid
        else
            high = mid - 1
        end
    end

    return string.sub(text, 1, low) .. suffix
end

function QPST_TaskBoardWindow:new(x, y, width, height, playerObj, worldObj)
    local o = ISCollapsableWindow:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self

    o.playerObj = playerObj
    o.worldObj = worldObj
    o.title = getText("UI_QPST_ModName")
    o.resizable = true
    o.minimumWidth = 760
    o.minimumHeight = 480

    return o
end

local function styleQPSTButton(button, kind)
    if not button then return end

    button.borderColor = {r=0.9, g=0.9, b=0.9, a=1}
    button.textColor = {r=1, g=1, b=1, a=1}

    if kind == "done" then
        button.backgroundColor = {r=0.05, g=0.45, b=0.12, a=0.95}
        button.backgroundColorMouseOver = {r=0.10, g=0.65, b=0.18, a=1}
    elseif kind == "remove" then
        button.backgroundColor = {r=0.50, g=0.08, b=0.06, a=0.95}
        button.backgroundColorMouseOver = {r=0.75, g=0.12, b=0.08, a=1}
    else
        button.backgroundColor = {r=0.18, g=0.18, b=0.18, a=0.95}
        button.backgroundColorMouseOver = {r=0.32, g=0.32, b=0.32, a=1}
    end
end

function QPST_TaskBoardWindow.drawOpenItem(list, y, item, alt)
    local height = list.itemheight or 42
    local task = item and item.item or nil

    if list.selected == item.index then
        list:drawRect(0, y, list:getWidth(), height - 1, 0.32, 0.32, 0.32, 0.32)
    elseif alt then
        list:drawRect(0, y, list:getWidth(), height - 1, 0.12, 0.12, 0.12, 0.12)
    end

    list:drawRectBorder(0, y + height - 1, list:getWidth(), 1, 0.30, 0.55, 0.55, 0.55)

    if task then
        local line = getText(
            "UI_QPST_TaskLine",
            getTaskDisplayText(task),
            getDisplayName(task.createdBy),
            formatAge(task.createdAt)
        )
        line = fitQPSTText(line, UIFont.Small, list:getWidth() - 28)
        list:drawText(line, 12, y + 12, 1, 1, 1, 1, UIFont.Small)
    end

    return y + height
end

function QPST_TaskBoardWindow.drawDoneItem(list, y, item, alt)
    local height = list.itemheight or 42
    local task = item and item.item or nil

    if list.selected == item.index then
        list:drawRect(0, y, list:getWidth(), height - 1, 0.20, 0.30, 0.20, 0.34)
    elseif alt then
        list:drawRect(0, y, list:getWidth(), height - 1, 0.08, 0.14, 0.08, 0.16)
    end

    list:drawRectBorder(0, y + height - 1, list:getWidth(), 1, 0.28, 0.45, 0.65, 0.45)

    if task then
        local line = getText(
            "UI_QPST_DoneTaskLine",
            getTaskDisplayText(task),
            getDisplayName(task.completedBy)
        )
        line = fitQPSTText(line, UIFont.Small, list:getWidth() - 28)
        list:drawText(line, 12, y + 12, 0.65, 0.95, 0.65, 1, UIFont.Small)
    end

    return y + height
end

function QPST_TaskBoardWindow:populateTaskLists()
    self.openList:clear()
    self.doneList:clear()

    ensureLocalTaskIds(self.worldObj)

    local tasks = getTasks(self.worldObj)
    if not tasks then return end

    for i = 1, #tasks do
        local task = tasks[i]
        if task then
            task.qpstTaskIndex = i
            if task.status == "Done" then
                self.doneList:addItem(getTaskDisplayText(task), task)
            else
                self.openList:addItem(getTaskDisplayText(task), task)
            end
        end
    end

    self.openList.selected = 0
    self.doneList.selected = 0
end

function QPST_TaskBoardWindow:createChildren()
    ISCollapsableWindow.createChildren(self)

    require "ISUI/ISScrollingListBox"

    self.openList = ISScrollingListBox:new(24, 166, 420, 280)
    self.openList:initialise()
    self.openList:instantiate()
    self.openList.itemheight = 42
    self.openList.doDrawItem = QPST_TaskBoardWindow.drawOpenItem
    self.openList.drawBorder = true
    self:addChild(self.openList)

    self.doneList = ISScrollingListBox:new(468, 166, 420, 280)
    self.doneList:initialise()
    self.doneList:instantiate()
    self.doneList.itemheight = 42
    self.doneList.doDrawItem = QPST_TaskBoardWindow.drawDoneItem
    self.doneList.drawBorder = true
    self:addChild(self.doneList)

    local mayManage = canManageBoard(self.playerObj, self.worldObj)
    local mayComplete = canCompleteBoard(self.playerObj, self.worldObj)

    self.doneButton = ISButton:new(24, self.height - 76, 230, 28, getText("UI_QPST_ButtonDone"), self, QPST_TaskBoardWindow.onDoneSelected)
    self.doneButton:initialise()
    self.doneButton:instantiate()
    self.doneButton:setVisible(mayComplete)
    styleQPSTButton(self.doneButton, "done")
    self:addChild(self.doneButton)

    self.removeOpenButton = ISButton:new(266, self.height - 76, 95, 28, getText("UI_QPST_ButtonRemove"), self, QPST_TaskBoardWindow.onRemoveOpenSelected)
    self.removeOpenButton:initialise()
    self.removeOpenButton:instantiate()
    self.removeOpenButton:setVisible(mayManage)
    styleQPSTButton(self.removeOpenButton, "remove")
    self:addChild(self.removeOpenButton)

    self.removeDoneButton = ISButton:new(self.width - 224, self.height - 76, 95, 26, getText("UI_QPST_ButtonRemove"), self, QPST_TaskBoardWindow.onRemoveDoneSelected)
    self.removeDoneButton:initialise()
    self.removeDoneButton:instantiate()
    self.removeDoneButton:setVisible(mayManage)
    styleQPSTButton(self.removeDoneButton, "remove")
    self:addChild(self.removeDoneButton)

    self.closeButton = ISButton:new(self.width - 125, self.height - 38, 95, 26, getText("UI_QPST_ButtonClose"), self, QPST_TaskBoardWindow.onCloseClicked)
    self.closeButton:initialise()
    self.closeButton:instantiate()
    styleQPSTButton(self.closeButton, "default")
    self:addChild(self.closeButton)

    self:populateTaskLists()
end

function QPST_TaskBoardWindow:getSelectedTask(list)
    if not list or not list.items then return nil end
    local selected = tonumber(list.selected) or 0
    if selected < 1 or selected > #list.items then return nil end
    return list.items[selected] and list.items[selected].item or nil
end

function QPST_TaskBoardWindow:completeTask(task)
    if not task then return end

    local taskId = tostring(getTaskId(task) or "")
    local taskIndex = tonumber(task.qpstTaskIndex) or -1

    local command = taskWorkflowCommand(self.playerObj, task)
    if not command then return end
    if isMultiplayerClient() then
        sendBoardCommand(self.playerObj, command, self.worldObj, {taskId = taskId, taskIndex = taskIndex})
        return
    end
    if command == COMMAND_COMPLETE_TASK then
        markTaskDoneData(self.playerObj, self.worldObj, taskId, taskIndex)
    end
    QPSurvivorTasks.openWindow(self.playerObj, self.worldObj)
end

function QPST_TaskBoardWindow:removeTask(task)
    if not task then return end

    local taskId = tostring(getTaskId(task) or "")
    local taskIndex = tonumber(task.qpstTaskIndex) or -1

    if isMultiplayerClient() then
        sendBoardCommand(self.playerObj, COMMAND_REMOVE_TASK, self.worldObj, {taskId = taskId, taskIndex = taskIndex})
        return
    end

    removeTaskData(self.playerObj, self.worldObj, taskId, taskIndex)
    QPSurvivorTasks.openWindow(self.playerObj, self.worldObj)
end

function QPST_TaskBoardWindow:onDoneSelected()
    self:completeTask(self:getSelectedTask(self.openList))
end

function QPST_TaskBoardWindow:onRemoveOpenSelected()
    self:removeTask(self:getSelectedTask(self.openList))
end

function QPST_TaskBoardWindow:onRemoveDoneSelected()
    self:removeTask(self:getSelectedTask(self.doneList))
end

function QPST_TaskBoardWindow:onCloseClicked()
    self:close()
end

function QPST_TaskBoardWindow:close()
    self:setVisible(false)
    self:removeFromUIManager()

    if QPSurvivorTasks.ActiveWindow == self then
        QPSurvivorTasks.ActiveWindow = nil
    end
end

function QPST_TaskBoardWindow:layoutTaskBoard()
    local panelX = 14
    local panelY = 36
    local panelW = self.width - 28
    local listY = 166
    local gap = 18
    local innerX = panelX + 10
    local innerW = panelW - 20
    local columnW = math.floor((innerW - gap) / 2)
    local listH = math.max(180, self.height - listY - 92)
    local rightX = innerX + columnW + gap
    local actionY = listY + listH + 8

    self.openList:setX(innerX)
    self.openList:setY(listY)
    self.openList:setWidth(columnW)
    self.openList:setHeight(listH)

    self.doneList:setX(rightX)
    self.doneList:setY(listY)
    self.doneList:setWidth(columnW)
    self.doneList:setHeight(listH)

    self.doneButton:setX(innerX)
    self.doneButton:setY(actionY)

    local actionGap = 12
    local doneWidth = math.min(230, math.max(170, columnW - 115))
    self.doneButton:setWidth(doneWidth)
    self.removeOpenButton:setX(innerX + doneWidth + actionGap)
    self.removeOpenButton:setY(actionY)

    self.removeDoneButton:setX(rightX + columnW - 95)
    self.removeDoneButton:setY(actionY)

    self.closeButton:setX(self.width - 125)
    self.closeButton:setY(self.height - 38)
end

function QPST_TaskBoardWindow:prerender()
    if self.width < 760 then self:setWidth(760) end
    if self.height < 480 then self:setHeight(480) end

    self:layoutTaskBoard()

    local hasOpenSelection = self:getSelectedTask(self.openList) ~= nil
    local hasDoneSelection = self:getSelectedTask(self.doneList) ~= nil

    local selectedOpenTask = self:getSelectedTask(self.openList)
    self.doneButton.enable = selectedOpenTask ~= nil and taskWorkflowCommand(self.playerObj, selectedOpenTask) ~= nil
    if selectedOpenTask then
        self.doneButton:setTitle(taskWorkflowLabel(self.playerObj, selectedOpenTask))
    else
        self.doneButton:setTitle(getText("UI_QPST_ButtonDone"))
    end
    self.removeOpenButton.enable = hasOpenSelection
    self.removeDoneButton.enable = hasDoneSelection

    ISCollapsableWindow.prerender(self)

    local tasks = getTasks(self.worldObj)
    local boardInfo = ensureBoardInfo(self.worldObj, self.playerObj)
    local openCount = countOpenTasks(tasks)
    local doneCount = countDoneTasks(tasks)

    local panelX = 14
    local panelY = 36
    local panelW = self.width - 28
    local panelH = self.height - 86

    self:drawRect(panelX, panelY, panelW, panelH, 0.92, 0.02, 0.02, 0.02)
    self:drawRectBorder(panelX, panelY, panelW, panelH, 0.9, 0.6, 0.6, 0.6)

    self:drawText(getText("UI_QPST_TaskBoardSummary"), panelX + 16, panelY + 12, 1, 1, 1, 1, UIFont.Medium)

    local openLabel = getText("UI_QPST_OpenCount", tostring(openCount))
    local doneLabel = getText("UI_QPST_DoneCount", tostring(doneCount))
    local openLabelX = panelX + 16
    local openLabelWidth = getTextManager():MeasureStringX(UIFont.Small, openLabel)
    local doneLabelX = openLabelX + openLabelWidth + 28

    self:drawText(openLabel, openLabelX, panelY + 48, 0.95, 0.85, 0.45, 1, UIFont.Small)
    self:drawText(doneLabel, doneLabelX, panelY + 48, 0.55, 0.9, 0.55, 1, UIFont.Small)

    if boardInfo and boardInfo.createdBy then
        self:drawText(
            getText("UI_QPST_CreatedByAge", getDisplayName(boardInfo.createdBy), formatAge(boardInfo.createdAt)),
            panelX + 16,
            panelY + 72,
            0.75, 0.75, 0.75, 1,
            UIFont.Small
        )
    end

    self:drawRect(panelX + 12, panelY + 100, panelW - 24, 1, 0.45, 0.55, 0.55, 0.55)

    local gap = 18
    local innerW = panelW - 20
    local columnW = math.floor((innerW - gap) / 2)
    local leftX = panelX + 10
    local rightX = leftX + columnW + gap

    self:drawText(getText("UI_QPST_OpenTasks"), leftX, 142, 0.95, 0.85, 0.45, 1, UIFont.Small)
    self:drawText(getText("UI_QPST_DoneTasks"), rightX, 142, 0.55, 0.95, 0.55, 1, UIFont.Small)
end

function QPSurvivorTasks.openWindow(playerObj, worldObj)
    if QPSurvivorTasks.ActiveWindow then
        QPSurvivorTasks.ActiveWindow:removeFromUIManager()
        QPSurvivorTasks.ActiveWindow = nil
    end

    local screenWidth = getCore() and getCore():getScreenWidth() or 1200
    local screenHeight = getCore() and getCore():getScreenHeight() or 800
    local width = math.min(1040, math.max(760, screenWidth - 120))
    local height = math.min(700, math.max(480, screenHeight - 140))
    local x = math.max(20, math.floor((screenWidth - width) / 2))
    local y = math.max(20, math.floor((screenHeight - height) / 2))

    local window = QPST_TaskBoardWindow:new(x, y, width, height, playerObj, worldObj)
    window:initialise()
    window:addToUIManager()
    window:setVisible(true)

    QPSurvivorTasks.ActiveWindow = window
end

local QPST_CustomTaskWindow = ISCollapsableWindow:derive("QPST_CustomTaskWindow")

function QPST_CustomTaskWindow:new(x, y, width, height, playerObj, worldObj)
    local o = ISCollapsableWindow:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self

    o.playerObj = playerObj
    o.worldObj = worldObj
    o.title = getText("UI_QPST_AddCustomTaskTitle")
    o.resizable = true
    o.minimumWidth = 480
    o.minimumHeight = 330

    return o
end

local QPST_TEXT_FALLBACKS = {
    UI_QPST_WeeklyButtonClaim = "Claim Weekly Task",
    UI_QPST_WeeklyButtonSubmit = "Submit Weekly Task for Review",
    UI_QPST_WeeklyButtonValidate = "Validate Weekly Task",
    UI_QPST_WeeklyUseWorkflow =
        "Weekly tasks must be claimed, submitted, and validated before completion.",
    UI_QPST_WeeklyCreatorCannotClaim =
        "The weekly task creator cannot claim their own task.",
    UI_QPST_WeeklyOnlyClaimantSubmit =
        "Only the survivor who claimed this weekly task can submit it.",
    UI_QPST_WeeklyAdminValidateOnly =
        "Only an administrator can validate a Weekly Community task.",
    UI_QPST_WeeklySelfValidation =
        "You cannot validate your own Weekly Community submission.",    UI_QPST_EnableWeeklyCommunity = "Enable Weekly Community Mode",
    UI_QPST_DisableWeeklyCommunity = "Disable Weekly Community Mode",
    UI_QPST_WeeklyCommunityEnabled =
        "Weekly Community mode enabled. Only administrators can post weekly tasks.",
    UI_QPST_WeeklyCommunityDisabled =
        "Weekly Community mode disabled.",
    UI_QPST_WeeklyAdminOnly =
        "Only administrators can manage Weekly Community mode.",
    UI_QPST_WeeklyPublicConflict =
        "Disable Weekly Community mode before enabling Public Task Creation.",
    UI_QPST_WeeklyAddTask = "Add Weekly Community Task",
    UI_QPST_WeeklyTaskProgress = "Weekly tasks",
    UI_QPST_WeeklyTaskLimitReached =
        "This week's Community task limit has been reached.",
    UI_QPST_WeeklyTaskText = "Weekly task",
    UI_QPST_WeeklyMoneyReward = "Money reward",
    UI_QPST_WeeklyMoneyHint =
        "Reward: 0-1000 Base.Money items. Claimable after completion.",
    UI_QPST_WeeklyTag = "WEEKLY",
    UI_QPST_ButtonClaimMoney = "Claim Money Reward",
    UI_QPST_MoneyClaimed = "Money reward claimed.",
    UI_QPST_MoneyAlreadyClaimed =
        "This money reward was already claimed.",
    UI_QPST_MoneyNotCompleter =
        "Only the survivor who completed this task can claim its reward.",
    UI_QPST_MoneyRewardUnavailable =
        "This task has no claimable money reward.",
    UI_QPST_MoneyPayoutFailed =
        "Money reward could not be added to your inventory.",    UI_QPST_EnablePublicBoard = "Enable Public Task Creation",
    UI_QPST_DisablePublicBoard = "Disable Public Task Creation",
    UI_QPST_PublicBoardEnabled =
        "Public task creation enabled. Any player may add standard tasks.",
    UI_QPST_PublicBoardDisabled =
        "Public task creation disabled.",
    UI_QPST_PublicStandardOnly =
        "Public contributors can create standard tasks only.",
    UI_QPST_ErrorPublicBoardAdminOnly =
        "Only administrators can change public board mode.",    UI_QPST_ButtonClaim = "Claim Selected Task",
    UI_QPST_ButtonSubmit = "Submit for Review",
    UI_QPST_ButtonValidate = "Validate Selected Task",
    UI_QPST_ButtonUnavailable = "Action Unavailable",
    UI_QPST_RewardNone = "No Reputation",
    UI_QPST_RewardEasy = "Easy (+1)",
    UI_QPST_RewardNormal = "Normal (+2)",
    UI_QPST_RewardHard = "Hard (+3)",
    UI_QPST_ReputationReward = "Optional Reputation Reward",
    UI_QPST_ReputationPath = "Reputation Path",
    UI_QPST_ErrorInvalidReputation = "Invalid Reputation task settings.",
    UI_QPST_ErrorReputationPermission =
        "Only a safehouse owner or administrator may create Reputation tasks.",
    UI_QPST_ErrorUseWorkflow =
        "Use Claim, Submit and Validate for Reputation tasks.",
    UI_QPST_ErrorInvalidWorkflowState =
        "This task is not in a valid workflow state.",
    UI_QPST_ErrorCreatorCannotClaim =
        "The task creator cannot claim their own Reputation task.",
    UI_QPST_ErrorOnlyClaimantSubmit =
        "Only the recorded claimant can submit this task.",
    UI_QPST_ErrorSelfValidation =
        "You cannot validate your own task submission.",
    UI_QPST_ErrorCreatorValidation =
        "The task creator cannot validate this Reputation task."
}

local function qpstTextOrFallback(key, fallback)
    local value = getText and getText(key) or nil

    if value == nil or value == "" or value == key then
        return fallback or QPST_TEXT_FALLBACKS[key] or key
    end

    return value
end

function QPST_CustomTaskWindow:createChildren()
    ISCollapsableWindow.createChildren(self)

    self.publicContributor =
        canAddTask(self.playerObj, self.worldObj)
        and not canManageBoard(self.playerObj, self.worldObj)

    self.entry = ISTextEntryBox:new("", 20, 72, self.width - 40, 30)
    self.entry:initialise()
    self.entry:instantiate()
    self:addChild(self.entry)

    self.rewardCombo = ISComboBox:new(20, 164, self.width - 40, 28, self, nil)
    self.rewardCombo:initialise()
    self.rewardCombo:instantiate()
    self.rewardCombo:addOption(qpstTextOrFallback("UI_QPST_RewardNone"))

    if not self.publicContributor then
        self.rewardCombo:addOption(qpstTextOrFallback("UI_QPST_RewardEasy"))
        self.rewardCombo:addOption(qpstTextOrFallback("UI_QPST_RewardNormal"))

        if isAdminPlayer(self.playerObj) then
            self.rewardCombo:addOption(qpstTextOrFallback("UI_QPST_RewardHard"))
        end
    end
    self.rewardCombo.selected = 1
    self:addChild(self.rewardCombo)

    self.pathCombo = ISComboBox:new(20, 230, self.width - 40, 28, self, nil)
    self.pathCombo:initialise()
    self.pathCombo:instantiate()
    for _, path in ipairs({
        "Community", "Hunter", "Explorer",
        "Medic", "Mechanic", "Builder"
    }) do
        self.pathCombo:addOption(path)
    end
    self.pathCombo.selected = 1
    self.pathCombo:setVisible(false)
    self:addChild(self.pathCombo)

    self.addButton = ISButton:new(
        self.width - 220,
        self.height - 42,
        95,
        28,
        getText("UI_QPST_ButtonAdd"),
        self,
        QPST_CustomTaskWindow.onAddClicked
    )
    self.addButton:initialise()
    self.addButton:instantiate()
    styleQPSTButton(self.addButton, "done")
    self:addChild(self.addButton)

    self.cancelButton = ISButton:new(
        self.width - 115,
        self.height - 42,
        95,
        28,
        getText("UI_QPST_ButtonCancel"),
        self,
        QPST_CustomTaskWindow.onCancelClicked
    )
    self.cancelButton:initialise()
    self.cancelButton:instantiate()
    styleQPSTButton(self.cancelButton, "default")
    self:addChild(self.cancelButton)
end

function QPST_CustomTaskWindow:layoutControls()
    local margin = 20
    local contentWidth = math.max(300, self.width - (margin * 2))

    self.entry:setX(margin)
    self.entry:setY(72)
    self.entry:setWidth(contentWidth)

    self.rewardCombo:setX(margin)
    self.rewardCombo:setY(164)
    self.rewardCombo:setWidth(contentWidth)

    self.pathCombo:setX(margin)
    self.pathCombo:setY(230)
    self.pathCombo:setWidth(contentWidth)

    local buttonY = self.height - 42
    self.addButton:setX(self.width - 220)
    self.addButton:setY(buttonY)
    self.cancelButton:setX(self.width - 115)
    self.cancelButton:setY(buttonY)
end

function QPST_CustomTaskWindow:prerender()
    if self.width < self.minimumWidth then
        self:setWidth(self.minimumWidth)
    end
    if self.height < self.minimumHeight then
        self:setHeight(self.minimumHeight)
    end

    self:layoutControls()
    ISCollapsableWindow.prerender(self)

    local reputationEnabled =
        not self.publicContributor
        and self.rewardCombo
        and tonumber(self.rewardCombo.selected) > 1

    if self.pathCombo then
        self.pathCombo:setVisible(reputationEnabled)
    end

    self:drawRect(
        12, 34, self.width - 24, self.height - 88,
        0.92, 0.02, 0.02, 0.02
    )
    self:drawRectBorder(
        12, 34, self.width - 24, self.height - 88,
        0.9, 0.6, 0.6, 0.6
    )

    self:drawText(
        getText("UI_QPST_CustomTaskPrompt"),
        22, 50, 1, 1, 1, 1, UIFont.Small
    )
    self:drawText(
        getText("UI_QPST_CustomTaskExample"),
        22, 110, 0.65, 0.65, 0.65, 1, UIFont.Small
    )
    self:drawText(
        qpstTextOrFallback("UI_QPST_ReputationReward"),
        22, 140, 0.9, 0.9, 0.9, 1, UIFont.Small
    )

    if self.publicContributor then
        self:drawText(
            qpstTextOrFallback("UI_QPST_PublicStandardOnly"),
            22, 206, 0.65, 0.65, 0.65, 1, UIFont.Small
        )
    elseif reputationEnabled then
        self:drawText(
            qpstTextOrFallback("UI_QPST_ReputationPath"),
            22, 206, 0.9, 0.9, 0.9, 1, UIFont.Small
        )
    else
        self:drawText(
            "Select Easy, Normal or Hard to enable a Reputation reward.",
            22, 206, 0.65, 0.65, 0.65, 1, UIFont.Small
        )
    end
end

function QPST_CustomTaskWindow:onAddClicked()
    if not self.entry then return end

    local taskText = self.entry:getText() or ""
    local selection = self.rewardCombo and tonumber(self.rewardCombo.selected) or 1
    local difficulty = ({[2]="Easy", [3]="Normal", [4]="Hard"})[selection] or ""
    local reputationEnabled = difficulty ~= ""
    local path = self.pathCombo and tostring(self.pathCombo.options[self.pathCombo.selected] or "Community") or "Community"

    if self.publicContributor then
        reputationEnabled = false
        difficulty = ""
        path = "Community"
    end

    if isMultiplayerClient() then
        sendBoardCommand(
            self.playerObj,
            COMMAND_ADD_TASK,
            self.worldObj,
            {taskText = taskText, translationKey = "", reputationEnabled = reputationEnabled,
             reputationDifficulty = difficulty, reputationPath = string.lower(path)}
        )
    else
        addTaskData(self.playerObj, self.worldObj, taskText, "", reputationEnabled, difficulty, path)
        QPSurvivorTasks.openWindow(self.playerObj, self.worldObj)
    end

    self:close()
end

function QPST_CustomTaskWindow:onCancelClicked()
    self:close()
end

function QPST_CustomTaskWindow:close()
    self:setVisible(false)
    self:removeFromUIManager()

    if QPSurvivorTasks.ActiveCustomWindow == self then
        QPSurvivorTasks.ActiveCustomWindow = nil
    end
end

local function openCustomTaskWindow(playerObj, worldObj)
    if not playerObj or not worldObj then return end

    if QPSurvivorTasks.ActiveCustomWindow then
        QPSurvivorTasks.ActiveCustomWindow:removeFromUIManager()
        QPSurvivorTasks.ActiveCustomWindow = nil
    end

    local width = 540
    local height = 360
    local x = 100
    local y = 100

    if getCore then
        x = (getCore():getScreenWidth() - width) / 2
        y = (getCore():getScreenHeight() - height) / 2
    end

    local window = QPST_CustomTaskWindow:new(x, y, width, height, playerObj, worldObj)
    window:initialise()
    window:addToUIManager()
    window:setVisible(true)

    QPSurvivorTasks.ActiveCustomWindow = window
end

local function applySafehouseBoardLocally(playerObj, worldObj, safehouse, serverArgs)
    if not playerObj or not worldObj then return false end

    local data = getData(worldObj)
    if not data then return false end

    local info = createBoardInfo(playerObj, worldObj, safehouse)

    if serverArgs then
        info.ownerUsername = tostring(serverArgs.ownerUsername or info.ownerUsername or "")
        info.ownerDisplayName = tostring(serverArgs.ownerDisplayName or info.ownerDisplayName or "")
        info.createdBy = tostring(serverArgs.createdBy or info.createdBy or "")
        info.createdAt = tonumber(serverArgs.createdAt) or info.createdAt
        info.safehouseOwner = tostring(serverArgs.safehouseOwner or info.safehouseOwner or "")
        info.safehouseTitle = tostring(serverArgs.safehouseTitle or info.safehouseTitle or "")

        if isKnownBoardType(serverArgs.boardType) then
            info.boardType = serverArgs.boardType
        end

        if isKnownPermissionMode(serverArgs.permissionMode) then
            info.permissionMode = serverArgs.permissionMode
        end
    elseif isAdminPlayer(playerObj) then
        info.boardType = BOARD_TYPE_ADMIN
        info.permissionMode = PERMISSION_MODE_ADMIN
    else
        info.boardType = BOARD_TYPE_SAFEHOUSE
        info.permissionMode = PERMISSION_MODE_SAFEHOUSE
    end

    data[BOARD_KEY] = true
    data[BOARD_INFO_KEY] = info

    if not data[DATA_KEY] then
        data[DATA_KEY] = {}
    end

    return true
end

local function createBoard(playerObj, worldObj)
    if not playerObj or not worldObj then return end

    local allowed, messageKey, safehouse = getSafehousePlacementState(playerObj, worldObj)

    if not allowed then
        sayLocalized(playerObj, messageKey)
        return
    end

    if isMultiplayerClient() then
        if QPSurvivorTasks.PendingBoardCreate then
            sayLocalized(playerObj, "UI_QPST_CreateBoardPending")
            return
        end

        local x, y, z = getObjectLocation(worldObj)
        local objectIndex = getObjectIndex(worldObj)

        if objectIndex < 0 then
            sayLocalized(playerObj, "UI_QPST_CreateBoardFailed")
            return
        end

        QPSurvivorTasks.PendingBoardCreate = {
            worldObj = worldObj,
            x = x,
            y = y,
            z = z,
            objectIndex = objectIndex,
            spriteName = getSpriteName(worldObj)
        }

        sendClientCommand(
            playerObj,
            NETWORK_MODULE,
            COMMAND_CREATE_SAFEHOUSE_BOARD,
            {
                x = x,
                y = y,
                z = z,
                objectIndex = objectIndex,
                spriteName = getSpriteName(worldObj)
            }
        )

        sayLocalized(playerObj, "UI_QPST_CreateBoardPending")
        return
    end

    if applySafehouseBoardLocally(playerObj, worldObj, safehouse, nil) then
        saveObject(worldObj)
        QPSurvivorTasks.openWindow(playerObj, worldObj)
        sayLocalized(playerObj, "UI_QPST_BoardCreated")
    end
end

local function requestBoardState(playerObj, worldObj)
    if not isMultiplayerClient() or not playerObj or not worldObj then
        return false
    end

    return sendBoardCommand(
        playerObj,
        COMMAND_REQUEST_BOARD_STATE,
        worldObj,
        {requestKey = getObjectStateKey(worldObj)}
    )
end

local function setPublicBoardMode(playerObj, worldObj, enabled)
    if not playerObj or not worldObj then return end

    if isMultiplayerClient() then
        sendBoardCommand(
            playerObj,
            COMMAND_SET_PUBLIC_BOARD,
            worldObj,
            {enabled = enabled == true}
        )
        return
    end

    local info = ensureBoardInfo(worldObj, playerObj)

    if not isAdminPlayer(playerObj)
        or not info
        or info.boardType ~= BOARD_TYPE_ADMIN then
        sayLocalized(playerObj, "UI_QPST_ErrorPublicBoardAdminOnly")
        return
    end

    info.publicCreate = enabled == true

    local data = getData(worldObj)
    data[STATE_REVISION_KEY] =
        math.floor(tonumber(data[STATE_REVISION_KEY]) or 0) + 1

    saveObject(worldObj)

    sayLocalized(
        playerObj,
        enabled == true
            and "UI_QPST_PublicBoardEnabled"
            or "UI_QPST_PublicBoardDisabled"
    )
end

local function removeBoard(playerObj, worldObj)
    if not playerObj or not worldObj then return end

    if isMultiplayerClient() then
        sendBoardCommand(playerObj, COMMAND_REMOVE_BOARD, worldObj, nil)
        return
    end

    if not canManageBoard(playerObj, worldObj) then return end

    local data = getData(worldObj)
    if not data then return end

    data[BOARD_KEY] = false
    data[BOARD_INFO_KEY] = nil
    data[DATA_KEY] = {}
    data[STATE_REVISION_KEY] = math.floor(tonumber(data[STATE_REVISION_KEY]) or 0) + 1

    saveObject(worldObj)

    if QPSurvivorTasks.ActiveWindow then
        QPSurvivorTasks.ActiveWindow:close()
    end
end

local function addTask(playerObj, worldObj, taskText, translationKey)
    if isMultiplayerClient() then
        sendBoardCommand(
            playerObj,
            COMMAND_ADD_TASK,
            worldObj,
            {
                taskText = tostring(taskText or ""),
                translationKey = tostring(translationKey or "")
            }
        )
        return
    end

    addTaskData(playerObj, worldObj, taskText, translationKey)
    QPSurvivorTasks.openWindow(playerObj, worldObj)
end

local function openTaskBoard(playerObj, worldObj)
    if not playerObj or not worldObj then return end

    ensureBoardInfo(worldObj, playerObj)
    ensureLocalTaskIds(worldObj)
    requestBoardState(playerObj, worldObj)
    QPSurvivorTasks.openWindow(playerObj, worldObj)
end

local function markTaskDone(playerObj, worldObj, taskId, taskIndex)
    local tasks = getTasks(worldObj)
    local index = findTaskIndex(tasks, taskId, taskIndex)
    local task = index and tasks[index] or nil
    local command = taskWorkflowCommand(playerObj, task)
    if not command then return end
    if isMultiplayerClient() then
        sendBoardCommand(playerObj, command, worldObj,
            {taskId = tostring(taskId or ""), taskIndex = tonumber(taskIndex) or -1})
        return
    end
    if command == COMMAND_COMPLETE_TASK then markTaskDoneData(playerObj, worldObj, taskId, taskIndex) end
    QPSurvivorTasks.openWindow(playerObj, worldObj)
end

local function removeTask(playerObj, worldObj, taskId, taskIndex)
    if isMultiplayerClient() then
        sendBoardCommand(
            playerObj,
            COMMAND_REMOVE_TASK,
            worldObj,
            {taskId = tostring(taskId or ""), taskIndex = tonumber(taskIndex) or -1}
        )
        return
    end

    removeTaskData(playerObj, worldObj, taskId, taskIndex)
    QPSurvivorTasks.openWindow(playerObj, worldObj)
end

local function clearBoard(playerObj, worldObj)
    if not playerObj or not worldObj then return end

    if isMultiplayerClient() then
        sendBoardCommand(playerObj, COMMAND_CLEAR_BOARD, worldObj, nil)
        return
    end

    if not canManageBoard(playerObj, worldObj) then return end

    local data = getData(worldObj)
    if not data then return end

    data[DATA_KEY] = {}
    data[STATE_REVISION_KEY] = math.floor(tonumber(data[STATE_REVISION_KEY]) or 0) + 1

    saveObject(worldObj)
    QPSurvivorTasks.openWindow(playerObj, worldObj)
end

local function addDisabledMenuOption(menu, label)
    local option = menu:addOption(label, nil)
    option.notAvailable = true
    return option
end

local function getCreateBoardMenuLabel(messageKey)
    if not messageKey or messageKey == "" then
        return getText("UI_QPST_CreateTaskBoard")
    end

    local reasonKey = "UI_QPST_ReasonSafehouseRequired"

    if messageKey == "UI_QPST_ErrorSafehousePermission" then
        reasonKey = "UI_QPST_ReasonSafehousePermission"
    elseif messageKey == "UI_QPST_CreateBoardFailed" then
        reasonKey = "UI_QPST_ReasonCreateUnavailable"
    end

    return getText(
        "UI_QPST_UnavailableWithReason",
        getText("UI_QPST_CreateTaskBoard"),
        getText(reasonKey)
    )
end

local function copyNetworkTable(value)
    if type(value) ~= "table" then
        return value
    end

    local result = {}

    for key, child in pairs(value) do
        result[key] = copyNetworkTable(child)
    end

    return result
end

local function applyBoardState(args)
    args = args or {}

    local stateKey = getObjectStateKeyFromValues(
        args.x,
        args.y,
        args.z,
        args.objectIndex,
        args.spriteName
    )

    local incomingRevision = math.floor(tonumber(args.revision) or 0)
    local cached = QPSurvivorTasks.BoardStateCache[stateKey]
    local cachedRevision = cached and math.floor(tonumber(cached.revision) or 0) or -1

    if incomingRevision >= cachedRevision then
        QPSurvivorTasks.BoardStateCache[stateKey] = copyNetworkTable(args)
    elseif cached then
        args = cached
        incomingRevision = cachedRevision
    end

    local worldObj = getObjectAtLocation(
        args.x,
        args.y,
        args.z,
        args.objectIndex,
        args.spriteName
    )

    if not worldObj then
        return nil
    end

    local data = getData(worldObj)
    if not data then return nil end

    local currentRevision = math.floor(tonumber(data[STATE_REVISION_KEY]) or -1)

    if incomingRevision < currentRevision then
        return worldObj
    end

    if args.isBoard == true then
        data[BOARD_KEY] = true
        data[BOARD_INFO_KEY] = copyNetworkTable(args.boardInfo or {})
        data[DATA_KEY] = copyNetworkTable(args.tasks or {})
        data[TASK_SEQUENCE_KEY] = math.floor(tonumber(args.nextTaskId) or 0)
        data[STATE_REVISION_KEY] = math.floor(tonumber(args.revision) or 0)
    else
        data[BOARD_KEY] = false
        data[BOARD_INFO_KEY] = nil
        data[DATA_KEY] = {}
        data[TASK_SEQUENCE_KEY] = math.floor(tonumber(args.nextTaskId) or 0)
        data[STATE_REVISION_KEY] = math.floor(tonumber(args.revision) or 0)
    end

    return worldObj
end

local function applyCachedBoardState(worldObj)
    if not worldObj then return end

    local cached = QPSurvivorTasks.BoardStateCache[getObjectStateKey(worldObj)]

    if cached then
        applyBoardState(cached)
    end
end

local function refreshActiveWindow(worldObj)
    local active = QPSurvivorTasks.ActiveWindow

    if not active or not worldObj or active.worldObj ~= worldObj then
        return
    end

    local playerObj = active.playerObj

    if isBoard(worldObj) then
        QPSurvivorTasks.openWindow(playerObj, worldObj)
    else
        active:close()
    end
end

local function onPlayerUpdateForBoardSync(playerObj)
    if not isMultiplayerClient() or not playerObj then
        return
    end

    local x = math.floor(tonumber(playerObj:getX()) or 0)
    local y = math.floor(tonumber(playerObj:getY()) or 0)
    local z = math.floor(tonumber(playerObj:getZ()) or 0)
    local chunkX = math.floor(x / 10)
    local chunkY = math.floor(y / 10)
    local playerKey = getPlayerUsername(playerObj)

    if playerKey == "" then
        playerKey = tostring(playerObj)
    end

    local syncKey = table.concat({tostring(chunkX), tostring(chunkY), tostring(z)}, ":")

    if QPSurvivorTasks.LastNearbySyncChunk[playerKey] == syncKey then
        return
    end

    QPSurvivorTasks.LastNearbySyncChunk[playerKey] = syncKey

    sendClientCommand(
        playerObj,
        NETWORK_MODULE,
        COMMAND_REQUEST_NEARBY_BOARDS,
        {x = x, y = y, z = z}
    )
end

local function onServerCommand(module, command, args)
    if module ~= NETWORK_MODULE then return end

    args = args or {}

    if command == COMMAND_BOARD_STATE then
        local worldObj = applyBoardState(args)
        refreshActiveWindow(worldObj)

        if args.messageKey and args.messageKey ~= "" then
            sayLocalized(getPlayer(), tostring(args.messageKey))
        end

        return
    end

    if command ~= COMMAND_CREATE_BOARD_RESULT then return end

    local playerObj = getPlayer()
    local pending = QPSurvivorTasks.PendingBoardCreate
    QPSurvivorTasks.PendingBoardCreate = nil

    if args.success ~= true then
        sayLocalized(
            playerObj,
            tostring(args.messageKey or "UI_QPST_CreateBoardFailed")
        )
        return
    end

    local worldObj = nil

    if pending and
       pending.worldObj and
       tonumber(pending.x) == tonumber(args.x) and
       tonumber(pending.y) == tonumber(args.y) and
       tonumber(pending.z) == tonumber(args.z) and
       tonumber(pending.objectIndex) == tonumber(args.objectIndex) then
        worldObj = pending.worldObj
    end

    if not worldObj then
        worldObj = getObjectAtLocation(
            args.x,
            args.y,
            args.z,
            args.objectIndex,
            args.spriteName
        )
    end

    if not worldObj then
        sayLocalized(playerObj, "UI_QPST_BoardCreatedReopen")
        return
    end

    local safehouse = getSafehouseForSquare(worldObj:getSquare())

    if applySafehouseBoardLocally(playerObj, worldObj, safehouse, args) then
        requestBoardState(playerObj, worldObj)
        QPSurvivorTasks.openWindow(playerObj, worldObj)
        sayLocalized(playerObj, "UI_QPST_BoardCreated")
    end
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

local function qpstRefreshWeeklyInfoLocal(info)
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

local qpstV070OriginalCanAddTask = canAddTask

canAddTask = function(playerObj, worldObj)
    if not playerObj or not worldObj or not isBoard(worldObj) then
        return false
    end

    local info = ensureBoardInfo(worldObj, playerObj)

    if qpstIsWeeklyCommunityInfo(info) then
        -- Weekly tasks use the dedicated admin-only creation flow.
        return false
    end

    return qpstV070OriginalCanAddTask(playerObj, worldObj)
end

QPSurvivorTasks.canAddTask = canAddTask

local qpstV070OriginalTaskDisplayText = getTaskDisplayText

getTaskDisplayText = function(task)
    local text = qpstV070OriginalTaskDisplayText(task)

    if task and task.weeklyCommunity == true then
        local tag = qpstTextOrFallback(
            "UI_QPST_WeeklyTag",
            "WEEKLY"
        )
        local reward = qpstWeeklyClampInteger(
            task.moneyReward,
            0,
            QPST_WEEKLY_MAX_MONEY,
            0
        )

        text = text .. " [" .. tag

        if reward > 0 then
            text = text .. " | $" .. tostring(reward)

            if task.moneyClaimed == true then
                text = text .. " | CLAIMED"
            end
        end

        text = text .. "]"
    end

    return text
end

local function qpstSameUsername(left, right)
    return string.lower(tostring(left or ""))
        == string.lower(tostring(right or ""))
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

    for i = 1, amount do
        local ok, item = pcall(function()
            return inventory:AddItem("Base.Money")
        end)

        if not ok or not item then
            for _, createdItem in ipairs(created) do
                pcall(function()
                    if not inventory.contains
                        or inventory:contains(createdItem) then
                        inventory:Remove(createdItem)
                    end
                end)
            end

            return false
        end

        table.insert(created, item)
    end

    return #created == amount
end

local function qpstAddWeeklyTaskLocal(playerObj, worldObj, taskText, moneyReward)
    if not playerObj or not worldObj then
        return false
    end

    local info = ensureBoardInfo(worldObj, playerObj)

    if not isAdminPlayer(playerObj)
        or not qpstIsWeeklyCommunityInfo(info) then
        sayLocalized(playerObj, "UI_QPST_WeeklyAdminOnly")
        return false
    end

    qpstRefreshWeeklyInfoLocal(info)

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
        sayLocalized(playerObj, "UI_QPST_WeeklyTaskLimitReached")
        return false
    end

    taskText = tostring(taskText or "")
    taskText = string.gsub(taskText, "^%s+", "")
    taskText = string.gsub(taskText, "%s+$", "")

    if taskText == "" then
        return false
    end

    if string.len(taskText) > 80 then
        taskText = string.sub(taskText, 1, 80)
    end

    local data = getData(worldObj)
    local tasks = getTasks(worldObj)

    if not data or not tasks then
        return false
    end

    moneyReward = qpstWeeklyClampInteger(
        moneyReward,
        0,
        QPST_WEEKLY_MAX_MONEY,
        0
    )

    table.insert(tasks, {
        id = nextLocalTaskId(worldObj, data),
        text = taskText,
        status = "Open",
        createdBy = getPlayerName(playerObj),
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
    data[STATE_REVISION_KEY] =
        math.floor(tonumber(data[STATE_REVISION_KEY]) or 0) + 1

    saveObject(worldObj)
    return true
end

local function qpstClaimMoneyLocal(playerObj, worldObj, task)
    if not playerObj or not worldObj or not task then
        return false
    end

    if task.weeklyCommunity ~= true
        or tostring(task.status or "") ~= "Done" then
        sayLocalized(playerObj, "UI_QPST_MoneyRewardUnavailable")
        return false
    end

    if task.weeklyValidated ~= true then
        sayLocalized(playerObj, "UI_QPST_WeeklyUseWorkflow")
        return false
    end

    local reward = qpstWeeklyClampInteger(
        task.moneyReward,
        0,
        QPST_WEEKLY_MAX_MONEY,
        0
    )

    if reward <= 0 then
        sayLocalized(playerObj, "UI_QPST_MoneyRewardUnavailable")
        return false
    end

    if task.moneyClaimed == true then
        sayLocalized(playerObj, "UI_QPST_MoneyAlreadyClaimed")
        return false
    end

    if not qpstSameUsername(
        task.completedBy,
        getPlayerUsername(playerObj)
    ) then
        sayLocalized(playerObj, "UI_QPST_MoneyNotCompleter")
        return false
    end

    if not qpstAddMoneyToInventory(playerObj, reward) then
        sayLocalized(playerObj, "UI_QPST_MoneyPayoutFailed")
        return false
    end

    task.moneyClaimed = true
    task.moneyClaimedBy = getPlayerUsername(playerObj)
    task.moneyClaimedAt = getWorldAgeHours()

    local data = getData(worldObj)
    data[STATE_REVISION_KEY] =
        math.floor(tonumber(data[STATE_REVISION_KEY]) or 0) + 1

    saveObject(worldObj)
    sayLocalized(playerObj, "UI_QPST_MoneyClaimed")
    return true
end

-- QPST_WEEKLY_VERIFICATION_V070_TC2_WORKFLOW
local qpstTC2OriginalTaskWorkflowCommand = taskWorkflowCommand

taskWorkflowCommand = function(playerObj, task)
    if task and task.weeklyCommunity == true then
        local username = string.lower(
            tostring(getPlayerUsername(playerObj) or "")
        )
        local createdBy = string.lower(
            tostring(task.createdBy or "")
        )
        local claimedBy = string.lower(
            tostring(task.claimedBy or "")
        )
        local completedBy = string.lower(
            tostring(task.completedBy or "")
        )
        local status = tostring(task.status or "Open")

        if status == "Open" then
            if username ~= "" and username ~= createdBy then
                return COMMAND_WEEKLY_CLAIM_TASK
            end

            return nil
        end

        if status == "Claimed"
            and username ~= ""
            and claimedBy == username then
            return COMMAND_WEEKLY_SUBMIT_TASK
        end

        if status == "Submitted"
            and isAdminPlayer(playerObj)
            and username ~= ""
            and completedBy ~= username then
            return COMMAND_WEEKLY_VALIDATE_TASK
        end

        return nil
    end

    return qpstTC2OriginalTaskWorkflowCommand(
        playerObj,
        task
    )
end

local qpstTC2OriginalTaskWorkflowLabel = taskWorkflowLabel

taskWorkflowLabel = function(playerObj, task)
    local command = taskWorkflowCommand(playerObj, task)

    if command == COMMAND_WEEKLY_CLAIM_TASK then
        return qpstTextOrFallback(
            "UI_QPST_WeeklyButtonClaim",
            "Claim Weekly Task"
        )
    end

    if command == COMMAND_WEEKLY_SUBMIT_TASK then
        return qpstTextOrFallback(
            "UI_QPST_WeeklyButtonSubmit",
            "Submit Weekly Task for Review"
        )
    end

    if command == COMMAND_WEEKLY_VALIDATE_TASK then
        return qpstTextOrFallback(
            "UI_QPST_WeeklyButtonValidate",
            "Validate Weekly Task"
        )
    end

    return qpstTC2OriginalTaskWorkflowLabel(
        playerObj,
        task
    )
end

local qpstTC2OriginalMarkTaskDoneData = markTaskDoneData

markTaskDoneData = function(
    playerObj,
    worldObj,
    taskId,
    taskIndex
)
    ensureLocalTaskIds(worldObj)

    local tasks = getTasks(worldObj)
    local index = findTaskIndex(
        tasks,
        taskId,
        taskIndex
    )
    local task = index and tasks[index] or nil

    if task and task.weeklyCommunity == true then
        sayLocalized(
            playerObj,
            "UI_QPST_WeeklyUseWorkflow"
        )
        return false
    end

    return qpstTC2OriginalMarkTaskDoneData(
        playerObj,
        worldObj,
        taskId,
        taskIndex
    )
end

local qpstTC2OriginalBoardCompleteTask =
    QPST_TaskBoardWindow.completeTask

function QPST_TaskBoardWindow:completeTask(task)
    if not task or task.weeklyCommunity ~= true then
        return qpstTC2OriginalBoardCompleteTask(
            self,
            task
        )
    end

    local command =
        taskWorkflowCommand(self.playerObj, task)

    if not command then
        return
    end

    local taskId = tostring(getTaskId(task) or "")
    local taskIndex =
        tonumber(task.qpstTaskIndex) or -1

    if isMultiplayerClient() then
        sendBoardCommand(
            self.playerObj,
            command,
            self.worldObj,
            {
                taskId = taskId,
                taskIndex = taskIndex
            }
        )
        return
    end

    local actor =
        tostring(getPlayerUsername(self.playerObj) or "")

    if actor == "" then
        actor = tostring(getPlayerName(self.playerObj) or "")
    end

    if command == COMMAND_WEEKLY_CLAIM_TASK then
        if qpstSameUsername(task.createdBy, actor) then
            sayLocalized(
                self.playerObj,
                "UI_QPST_WeeklyCreatorCannotClaim"
            )
            return
        end

        task.status = "Claimed"
        task.claimedBy = actor
        task.claimedAt = getWorldAgeHours()
        task.weeklyValidated = false
    elseif command == COMMAND_WEEKLY_SUBMIT_TASK then
        if not qpstSameUsername(task.claimedBy, actor) then
            sayLocalized(
                self.playerObj,
                "UI_QPST_WeeklyOnlyClaimantSubmit"
            )
            return
        end

        task.status = "Submitted"
        task.submittedBy = actor
        task.submittedAt = getWorldAgeHours()
        task.completedBy = actor
        task.weeklyValidated = false
    elseif command == COMMAND_WEEKLY_VALIDATE_TASK then
        if not isAdminPlayer(self.playerObj) then
            sayLocalized(
                self.playerObj,
                "UI_QPST_WeeklyAdminValidateOnly"
            )
            return
        end

        if qpstSameUsername(task.completedBy, actor) then
            sayLocalized(
                self.playerObj,
                "UI_QPST_WeeklySelfValidation"
            )
            return
        end

        task.status = "Done"
        task.completedAt = getWorldAgeHours()
        task.validatedBy = actor
        task.validatedAt = getWorldAgeHours()
        task.weeklyValidated = true
    else
        return
    end

    task.weeklyWorkflowVersion = 2

    local data = getData(self.worldObj)
    data[STATE_REVISION_KEY] =
        math.floor(
            tonumber(data[STATE_REVISION_KEY]) or 0
        ) + 1

    saveObject(self.worldObj)

    QPSurvivorTasks.openWindow(
        self.playerObj,
        self.worldObj
    )
end
local QPST_WeeklyTaskWindow =
    ISCollapsableWindow:derive("QPST_WeeklyTaskWindow")

function QPST_WeeklyTaskWindow:new(x, y, width, height, playerObj, worldObj)
    local o = ISCollapsableWindow:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self

    o.playerObj = playerObj
    o.worldObj = worldObj
    o.title = qpstTextOrFallback(
        "UI_QPST_WeeklyAddTask",
        "Add Weekly Community Task"
    )
    o.resizable = false

    return o
end

function QPST_WeeklyTaskWindow:createChildren()
    ISCollapsableWindow.createChildren(self)

    self.taskEntry = ISTextEntryBox:new(
        "",
        20,
        78,
        self.width - 40,
        30
    )
    self.taskEntry:initialise()
    self.taskEntry:instantiate()
    self:addChild(self.taskEntry)

    self.moneyEntry = ISTextEntryBox:new(
        "0",
        20,
        150,
        160,
        30
    )
    self.moneyEntry:initialise()
    self.moneyEntry:instantiate()

    if self.moneyEntry.setOnlyNumbers then
        self.moneyEntry:setOnlyNumbers(true)
    end

    self:addChild(self.moneyEntry)

    self.addButton = ISButton:new(
        self.width - 220,
        self.height - 42,
        95,
        28,
        getText("UI_QPST_ButtonAdd"),
        self,
        QPST_WeeklyTaskWindow.onAddClicked
    )
    self.addButton:initialise()
    self.addButton:instantiate()
    styleQPSTButton(self.addButton, "done")
    self:addChild(self.addButton)

    self.cancelButton = ISButton:new(
        self.width - 115,
        self.height - 42,
        95,
        28,
        getText("UI_QPST_ButtonCancel"),
        self,
        QPST_WeeklyTaskWindow.onCancelClicked
    )
    self.cancelButton:initialise()
    self.cancelButton:instantiate()
    styleQPSTButton(self.cancelButton, "default")
    self:addChild(self.cancelButton)
end

function QPST_WeeklyTaskWindow:prerender()
    ISCollapsableWindow.prerender(self)

    self:drawRect(
        12,
        34,
        self.width - 24,
        self.height - 88,
        0.92,
        0.02,
        0.02,
        0.02
    )

    self:drawRectBorder(
        12,
        34,
        self.width - 24,
        self.height - 88,
        0.9,
        0.6,
        0.6,
        0.6
    )

    self:drawText(
        qpstTextOrFallback("UI_QPST_WeeklyTaskText", "Weekly task"),
        20,
        52,
        1,
        1,
        1,
        1,
        UIFont.Small
    )

    self:drawText(
        qpstTextOrFallback("UI_QPST_WeeklyMoneyReward", "Money reward"),
        20,
        124,
        0.9,
        0.9,
        0.9,
        1,
        UIFont.Small
    )

    self:drawText(
        qpstTextOrFallback(
            "UI_QPST_WeeklyMoneyHint",
            "Reward: 0-1000 Base.Money items. Claimable after completion."
        ),
        20,
        188,
        0.65,
        0.65,
        0.65,
        1,
        UIFont.Small
    )
end

function QPST_WeeklyTaskWindow:onAddClicked()
    local taskText = self.taskEntry and self.taskEntry:getText() or ""
    local rewardText = self.moneyEntry and self.moneyEntry:getText() or "0"
    local reward = qpstWeeklyClampInteger(
        rewardText,
        0,
        QPST_WEEKLY_MAX_MONEY,
        0
    )

    if isMultiplayerClient() then
        sendBoardCommand(
            self.playerObj,
            COMMAND_ADD_WEEKLY_TASK,
            self.worldObj,
            {
                taskText = tostring(taskText or ""),
                moneyReward = reward
            }
        )
    else
        if qpstAddWeeklyTaskLocal(
            self.playerObj,
            self.worldObj,
            taskText,
            reward
        ) then
            QPSurvivorTasks.openWindow(
                self.playerObj,
                self.worldObj
            )
        end
    end

    self:close()
end

function QPST_WeeklyTaskWindow:onCancelClicked()
    self:close()
end

function QPST_WeeklyTaskWindow:close()
    self:setVisible(false)
    self:removeFromUIManager()

    if QPSurvivorTasks.ActiveWeeklyTaskWindow == self then
        QPSurvivorTasks.ActiveWeeklyTaskWindow = nil
    end
end

local function qpstOpenWeeklyTaskWindow(playerObj, worldObj)
    if not playerObj or not worldObj then
        return
    end

    if QPSurvivorTasks.ActiveWeeklyTaskWindow then
        QPSurvivorTasks.ActiveWeeklyTaskWindow:removeFromUIManager()
        QPSurvivorTasks.ActiveWeeklyTaskWindow = nil
    end

    local width = 560
    local height = 300
    local screenWidth = getCore() and getCore():getScreenWidth() or 1200
    local screenHeight = getCore() and getCore():getScreenHeight() or 800
    local x = math.max(20, math.floor((screenWidth - width) / 2))
    local y = math.max(20, math.floor((screenHeight - height) / 2))

    local window = QPST_WeeklyTaskWindow:new(
        x,
        y,
        width,
        height,
        playerObj,
        worldObj
    )

    window:initialise()
    window:addToUIManager()
    window:setVisible(true)

    QPSurvivorTasks.ActiveWeeklyTaskWindow = window
end

local function qpstSetWeeklyCommunityMode(playerObj, worldObj, enabled)
    if not playerObj or not worldObj then
        return
    end

    if isMultiplayerClient() then
        sendBoardCommand(
            playerObj,
            COMMAND_SET_WEEKLY_COMMUNITY,
            worldObj,
            {enabled = enabled == true}
        )
        return
    end

    local info = ensureBoardInfo(worldObj, playerObj)

    if not isAdminPlayer(playerObj)
        or not info
        or info.boardType ~= BOARD_TYPE_ADMIN then
        sayLocalized(playerObj, "UI_QPST_WeeklyAdminOnly")
        return
    end

    if enabled == true then
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
    data[STATE_REVISION_KEY] =
        math.floor(tonumber(data[STATE_REVISION_KEY]) or 0) + 1

    saveObject(worldObj)

    sayLocalized(
        playerObj,
        enabled == true
            and "UI_QPST_WeeklyCommunityEnabled"
            or "UI_QPST_WeeklyCommunityDisabled"
    )
end

local qpstV070OriginalSetPublicBoardMode = setPublicBoardMode

setPublicBoardMode = function(playerObj, worldObj, enabled)
    local info = ensureBoardInfo(worldObj, playerObj)

    if enabled == true and qpstIsWeeklyCommunityInfo(info) then
        sayLocalized(playerObj, "UI_QPST_WeeklyPublicConflict")
        return
    end

    return qpstV070OriginalSetPublicBoardMode(
        playerObj,
        worldObj,
        enabled
    )
end

local qpstV070OriginalBoardCreateChildren =
    QPST_TaskBoardWindow.createChildren

function QPST_TaskBoardWindow:createChildren()
    qpstV070OriginalBoardCreateChildren(self)

    self.claimMoneyButton = ISButton:new(
        24,
        self.height - 76,
        200,
        28,
        qpstTextOrFallback(
            "UI_QPST_ButtonClaimMoney",
            "Claim Money Reward"
        ),
        self,
        QPST_TaskBoardWindow.onClaimMoneySelected
    )

    self.claimMoneyButton:initialise()
    self.claimMoneyButton:instantiate()
    self.claimMoneyButton:setVisible(false)
    styleQPSTButton(self.claimMoneyButton, "done")
    self:addChild(self.claimMoneyButton)
end

local qpstV070OriginalBoardLayout =
    QPST_TaskBoardWindow.layoutTaskBoard

function QPST_TaskBoardWindow:layoutTaskBoard()
    qpstV070OriginalBoardLayout(self)

    if self.claimMoneyButton and self.doneList then
        self.claimMoneyButton:setX(self.doneList:getX())
        self.claimMoneyButton:setY(
            self.doneList:getY() + self.doneList:getHeight() + 8
        )
        self.claimMoneyButton:setWidth(
            math.min(210, math.max(150, self.doneList:getWidth() - 110))
        )
    end
end

local qpstV070OriginalBoardPrerender =
    QPST_TaskBoardWindow.prerender

function QPST_TaskBoardWindow:prerender()
    qpstV070OriginalBoardPrerender(self)

    if not self.claimMoneyButton then
        return
    end

    local task = self:getSelectedTask(self.doneList)
    local show = task
        and task.weeklyCommunity == true
        and qpstWeeklyClampInteger(
            task.moneyReward,
            0,
            QPST_WEEKLY_MAX_MONEY,
            0
        ) > 0

    self.claimMoneyButton:setVisible(show == true)

    if show then
        local canClaim =
            tostring(task.status or "") == "Done"
            and task.weeklyValidated == true
            and task.moneyClaimed ~= true
            and qpstSameUsername(
                task.completedBy,
                getPlayerUsername(self.playerObj)
            )

        if self.claimMoneyButton.setEnable then
            self.claimMoneyButton:setEnable(canClaim)
        else
            self.claimMoneyButton.enable = canClaim
        end
    end
end

function QPST_TaskBoardWindow:onClaimMoneySelected()
    local task = self:getSelectedTask(self.doneList)

    if not task then
        return
    end

    local taskId = tostring(getTaskId(task) or "")
    local taskIndex = tonumber(task.qpstTaskIndex) or -1

    if isMultiplayerClient() then
        sendBoardCommand(
            self.playerObj,
            COMMAND_CLAIM_MONEY_REWARD,
            self.worldObj,
            {
                taskId = taskId,
                taskIndex = taskIndex
            }
        )
        return
    end

    if qpstClaimMoneyLocal(
        self.playerObj,
        self.worldObj,
        task
    ) then
        QPSurvivorTasks.openWindow(
            self.playerObj,
            self.worldObj
        )
    end
end
local function onFillWorldObjectContextMenu(playerIndex, context, worldobjects, test)
    if test then return end
    if not context then return end

    local playerObj = getSpecificPlayer(playerIndex)
    if not playerObj then return end

    local worldObj = getBoardTarget(worldobjects)
    if not worldObj then return end

    applyCachedBoardState(worldObj)
    requestBoardState(playerObj, worldObj)

    if isBoard(worldObj) then
        ensureBoardInfo(worldObj, playerObj)
        ensureLocalTaskIds(worldObj)
    end

    local tasks = getTasks(worldObj)
    local openCount = countOpenTasks(tasks)

    local menuName = getText(MOD_NAME_KEY)

    if isBoard(worldObj) then
        menuName = getText(
            "UI_QPST_MenuWithOpenCount",
            tostring(openCount)
        )
    end

    local mainOption = context:addOption(menuName, nil)
    local subMenu = ISContextMenu:getNew(context)
    context:addSubMenu(mainOption, subMenu)

    if not isBoard(worldObj) then
        local allowed, messageKey = getSafehousePlacementState(playerObj, worldObj)

        if allowed then
            subMenu:addOption(
                getText("UI_QPST_CreateTaskBoard"),
                playerObj,
                createBoard,
                worldObj
            )
        else
            addDisabledMenuOption(
                subMenu,
                getCreateBoardMenuLabel(messageKey)
            )
        end

        return
    end

    subMenu:addOption(getText("UI_QPST_OpenTaskBoard"), playerObj, openTaskBoard, worldObj)

    local boardInfo = ensureBoardInfo(worldObj, playerObj)
    local mayManage = canManageBoard(playerObj, worldObj)
    local mayAdd = canAddTask(playerObj, worldObj)
    local mayComplete = canCompleteBoard(playerObj, worldObj)

    if isAdminPlayer(playerObj)
        and boardInfo
        and boardInfo.boardType == BOARD_TYPE_ADMIN then
        local publicEnabled = isPublicBoardInfo(boardInfo)

        subMenu:addOption(
            qpstTextOrFallback(
                publicEnabled
                    and "UI_QPST_DisablePublicBoard"
                    or "UI_QPST_EnablePublicBoard"
            ),
            playerObj,
            setPublicBoardMode,
            worldObj,
            not publicEnabled
        )
    end

    if isAdminPlayer(playerObj)
        and boardInfo
        and boardInfo.boardType == BOARD_TYPE_ADMIN then
        local weeklyEnabled = qpstIsWeeklyCommunityInfo(boardInfo)

        subMenu:addOption(
            qpstTextOrFallback(
                weeklyEnabled
                    and "UI_QPST_DisableWeeklyCommunity"
                    or "UI_QPST_EnableWeeklyCommunity",
                weeklyEnabled
                    and "Disable Weekly Community Mode"
                    or "Enable Weekly Community Mode"
            ),
            playerObj,
            qpstSetWeeklyCommunityMode,
            worldObj,
            not weeklyEnabled
        )

        if weeklyEnabled then
            qpstRefreshWeeklyInfoLocal(boardInfo)

            local weeklyLimit = qpstWeeklyClampInteger(
                boardInfo.weeklyTaskLimit,
                1,
                QPST_WEEKLY_MAX_LIMIT,
                QPST_WEEKLY_DEFAULT_LIMIT
            )
            local weeklyCreated = math.max(
                0,
                math.floor(
                    tonumber(boardInfo.weeklyCreatedThisCycle) or 0
                )
            )

            addDisabledMenuOption(
                subMenu,
                qpstTextOrFallback(
                    "UI_QPST_WeeklyTaskProgress",
                    "Weekly tasks"
                )
                    .. ": "
                    .. tostring(weeklyCreated)
                    .. "/"
                    .. tostring(weeklyLimit)
            )

            if weeklyCreated < weeklyLimit then
                subMenu:addOption(
                    qpstTextOrFallback(
                        "UI_QPST_WeeklyAddTask",
                        "Add Weekly Community Task"
                    ),
                    playerObj,
                    qpstOpenWeeklyTaskWindow,
                    worldObj
                )
            else
                addDisabledMenuOption(
                    subMenu,
                    qpstTextOrFallback(
                        "UI_QPST_WeeklyTaskLimitReached",
                        "This week's Community task limit has been reached."
                    )
                )
            end
        end
    end
    if mayAdd then
        local addOption = subMenu:addOption(getText("UI_QPST_AddTaskMenu"), nil)
        local addSubMenu = ISContextMenu:getNew(subMenu)
        subMenu:addSubMenu(addOption, addSubMenu)

        addSubMenu:addOption(getText("UI_QPST_CustomTask"), playerObj, openCustomTaskWindow, worldObj)

        local quickOption = addSubMenu:addOption(getText("UI_QPST_QuickPresets"), nil)
        local quickSubMenu = ISContextMenu:getNew(addSubMenu)
        addSubMenu:addSubMenu(quickOption, quickSubMenu)

        for i = 1, #QUICK_TASKS do
            local preset = QUICK_TASKS[i]

            quickSubMenu:addOption(
                getText(preset.key),
                playerObj,
                addTask,
                worldObj,
                preset.fallback,
                preset.key
            )
        end
    end

    if mayComplete and openCount > 0 then
        local doneOption = subMenu:addOption(getText("UI_QPST_MarkTaskDone"), nil)
        local doneSubMenu = ISContextMenu:getNew(subMenu)
        subMenu:addSubMenu(doneOption, doneSubMenu)

        for i = 1, #tasks do
            local task = tasks[i]

            if task and task.status ~= "Done" then
                doneSubMenu:addOption(
                    getTaskDisplayText(task),
                    playerObj,
                    markTaskDone,
                    worldObj,
                    getTaskId(task),
                    i
                )
            end
        end
    end

    if mayManage and tasks and #tasks > 0 then
        local removeOption = subMenu:addOption(getText("UI_QPST_RemoveTask"), nil)
        local removeSubMenu = ISContextMenu:getNew(subMenu)
        subMenu:addSubMenu(removeOption, removeSubMenu)

        for i = 1, #tasks do
            local task = tasks[i]

            if task then
                local label = getTaskDisplayText(task)

                if task.status == "Done" then
                    label = getText(
                        "UI_QPST_DoneLabel",
                        label
                    )
                end

                removeSubMenu:addOption(
                    label,
                    playerObj,
                    removeTask,
                    worldObj,
                    getTaskId(task),
                    i
                )
            end
        end

        subMenu:addOption(getText("UI_QPST_ClearBoard"), playerObj, clearBoard, worldObj)
    end

    if mayManage then
        subMenu:addOption(getText("UI_QPST_RemoveTaskBoard"), playerObj, removeBoard, worldObj)
    end
end

Events.OnFillWorldObjectContextMenu.Add(onFillWorldObjectContextMenu)
Events.OnServerCommand.Add(onServerCommand)
Events.OnPlayerUpdate.Add(onPlayerUpdateForBoardSync)

print("[QPST-DEV] v0.6.1 B41 localization hardening loaded.")

-- QPST_WEEKLY_TC22_UI_POLISH
QPSurvivorTasks.PendingWeeklyTaskSelection =
    QPSurvivorTasks.PendingWeeklyTaskSelection or nil
QPSurvivorTasks.PendingWeeklyWorkflowAction =
    QPSurvivorTasks.PendingWeeklyWorkflowAction or false
QPSurvivorTasks.PendingWeeklyMoneyClaims =
    QPSurvivorTasks.PendingWeeklyMoneyClaims or {}

local function qpstTC22TaskId(task)
    return tostring(getTaskId(task) or "")
end

local function qpstTC22Select(list, taskId)
    if not list or tostring(taskId or "") == "" then return false end
    for i, row in ipairs(list.items or {}) do
        local task = row and row.item or nil
        if task and qpstTC22TaskId(task) == tostring(taskId) then
            list.selected = i
            return true
        end
    end
    return false
end

local qpstTC22OldRefreshActiveWindow = refreshActiveWindow
refreshActiveWindow = function(worldObj)
    local pending = QPSurvivorTasks.PendingWeeklyTaskSelection

    qpstTC22OldRefreshActiveWindow(worldObj)

    local active = QPSurvivorTasks.ActiveWindow
    if active and active.worldObj == worldObj and tostring(pending or "") ~= "" then
        if not qpstTC22Select(active.openList, pending) then
            qpstTC22Select(active.doneList, pending)
        end
    end

    if tostring(pending or "") ~= "" then
        QPSurvivorTasks.PendingWeeklyMoneyClaims[tostring(pending)] = nil
    end

    QPSurvivorTasks.PendingWeeklyWorkflowAction = false
    QPSurvivorTasks.PendingWeeklyTaskSelection = nil
end

local qpstTC22OldCompleteTask = QPST_TaskBoardWindow.completeTask
function QPST_TaskBoardWindow:completeTask(task)
    if task and task.weeklyCommunity == true and isMultiplayerClient() then
        if QPSurvivorTasks.PendingWeeklyWorkflowAction == true then return end
        QPSurvivorTasks.PendingWeeklyWorkflowAction = true
        QPSurvivorTasks.PendingWeeklyTaskSelection = qpstTC22TaskId(task)

        if self.doneButton then
            if self.doneButton.setEnable then self.doneButton:setEnable(false)
            else self.doneButton.enable = false end
        end
    end

    return qpstTC22OldCompleteTask(self, task)
end

local qpstTC22OldPrerender = QPST_TaskBoardWindow.prerender
function QPST_TaskBoardWindow:prerender()
    qpstTC22OldPrerender(self)

    local info = ensureBoardInfo(self.worldObj, self.playerObj)
    if not qpstIsWeeklyCommunityInfo(info) then
        if self.doneButton then self.doneButton:setVisible(true) end
        return
    end

    local task = self:getSelectedTask(self.openList)
    local command = task
        and task.weeklyCommunity == true
        and taskWorkflowCommand(self.playerObj, task)
        or nil

    if self.doneButton then
        local visible = task ~= nil
            and task.weeklyCommunity == true
            and command ~= nil

        self.doneButton:setVisible(visible)

        if visible then
            self.doneButton:setTitle(taskWorkflowLabel(self.playerObj, task))
            local enabled = QPSurvivorTasks.PendingWeeklyWorkflowAction ~= true
            if self.doneButton.setEnable then self.doneButton:setEnable(enabled)
            else self.doneButton.enable = enabled end
        end
    end
end

local qpstTC22OldClaimMoney = QPST_TaskBoardWindow.onClaimMoneySelected
function QPST_TaskBoardWindow:onClaimMoneySelected()
    local task = self:getSelectedTask(self.doneList)
    if not task or task.moneyClaimed == true then return end

    local taskId = qpstTC22TaskId(task)

    if isMultiplayerClient() then
        if QPSurvivorTasks.PendingWeeklyMoneyClaims[taskId] == true then return end

        QPSurvivorTasks.PendingWeeklyMoneyClaims[taskId] = true
        QPSurvivorTasks.PendingWeeklyTaskSelection = taskId

        if self.claimMoneyButton then
            if self.claimMoneyButton.setEnable then self.claimMoneyButton:setEnable(false)
            else self.claimMoneyButton.enable = false end
        end
    end

    return qpstTC22OldClaimMoney(self)
end

-- QPST_WEEKLY_TC23_IN_PLACE_BOARD_REFRESH
local function qpstTC23TaskId(task)
    return tostring(task and getTaskId(task) or "")
end

local function qpstTC23SelectedTaskId(window)
    if not window then return "" end

    local task =
        window:getSelectedTask(window.openList)
        or window:getSelectedTask(window.doneList)

    local taskId = qpstTC23TaskId(task)

    if taskId ~= "" then
        return taskId
    end

    return tostring(
        QPSurvivorTasks.PendingWeeklyTaskSelection
        or ""
    )
end

local function qpstTC23SelectTaskById(list, taskId)
    taskId = tostring(taskId or "")

    if taskId == ""
        or not list
        or not list.items then
        return false
    end

    for index, row in ipairs(list.items) do
        local task = row and row.item or nil

        if task
            and qpstTC23TaskId(task) == taskId then
            list.selected = index
            return true
        end
    end

    return false
end

local function qpstTC23RestoreSelection(window, taskId)
    if not window then return false end

    if qpstTC23SelectTaskById(
        window.openList,
        taskId
    ) then
        return true
    end

    return qpstTC23SelectTaskById(
        window.doneList,
        taskId
    )
end

local qpstTC23OldRefreshActiveWindow =
    refreshActiveWindow

refreshActiveWindow = function(worldObj)
    local active =
        QPSurvivorTasks.ActiveWindow

    if not active
        or not worldObj
        or active.worldObj ~= worldObj then
        return qpstTC23OldRefreshActiveWindow(
            worldObj
        )
    end

    if not isBoard(worldObj) then
        active:close()
        return
    end

    local selectedTaskId =
        qpstTC23SelectedTaskId(active)

    local pendingTaskId =
        tostring(
            QPSurvivorTasks.PendingWeeklyTaskSelection
            or ""
        )

    if pendingTaskId ~= "" then
        selectedTaskId = pendingTaskId
    end

    active:populateTaskLists()

    qpstTC23RestoreSelection(
        active,
        selectedTaskId
    )

    if pendingTaskId ~= "" then
        QPSurvivorTasks.PendingWeeklyMoneyClaims[
            pendingTaskId
        ] = nil
    end

    QPSurvivorTasks.PendingWeeklyWorkflowAction =
        false

    QPSurvivorTasks.PendingWeeklyTaskSelection =
        nil
end

local qpstTC23OldOpenWindow =
    QPSurvivorTasks.openWindow

QPSurvivorTasks.openWindow =
    function(playerObj, worldObj)
        local previous =
            QPSurvivorTasks.ActiveWindow

        local selectedTaskId =
            previous
            and previous.worldObj == worldObj
            and qpstTC23SelectedTaskId(previous)
            or ""

        qpstTC23OldOpenWindow(
            playerObj,
            worldObj
        )

        local active =
            QPSurvivorTasks.ActiveWindow

        if active
            and active.worldObj == worldObj
            and selectedTaskId ~= "" then
            qpstTC23RestoreSelection(
                active,
                selectedTaskId
            )
        end
    end

-- QPST_WEEKLY_TC26_HIDE_MANUAL_REWARD_BUTTON
--
-- Weekly Community rewards are automatic from TC2.5 onward.
-- The old manual Claim Money Reward button is no longer part of the normal
-- Weekly workflow.  Server-side TC2.6 guards also reject manual claims for
-- automatically managed tasks.

local qpstTC26OldPrerender =
    QPST_TaskBoardWindow.prerender

function QPST_TaskBoardWindow:prerender()
    qpstTC26OldPrerender(self)

    if not self.claimMoneyButton then
        return
    end

    local info =
        ensureBoardInfo(
            self.worldObj,
            self.playerObj
        )

    if qpstIsWeeklyCommunityInfo(info) then
        self.claimMoneyButton:setVisible(false)

        if self.claimMoneyButton.setEnable then
            self.claimMoneyButton:setEnable(false)
        else
            self.claimMoneyButton.enable = false
        end
    end
end

-- QPST_WEEKLY_TC27_ADMIN_STATUS_VISIBILITY
--
-- Weekly rows now expose workflow ownership/status directly in the Task Board.
-- This lets an administrator see WHO claimed/submitted a task before pressing
-- Validate Weekly Task instead of seeing only the original task creator.

local function qpstTC27Text(key, fallback, ...)
    local value = nil

    if getText then
        local ok, translated = pcall(
            getText,
            key,
            ...
        )

        if ok then
            value = translated
        end
    end

    if value == nil
        or value == ""
        or value == key then

        value = tostring(fallback or key)

        local count = select("#", ...)

        for index = 1, count do
            local token = "%%" .. tostring(index)
            local replacement =
                tostring(select(index, ...))

            value = string.gsub(
                value,
                token,
                function()
                    return replacement
                end
            )
        end
    end

    return tostring(value or "")
end

local function qpstTC27WeeklyActor(task)
    if not task then
        return ""
    end

    local submittedBy =
        tostring(task.submittedBy or "")

    if submittedBy ~= "" then
        return submittedBy
    end

    local completedBy =
        tostring(task.completedBy or "")

    if completedBy ~= "" then
        return completedBy
    end

    return tostring(task.claimedBy or "")
end

local function qpstTC27WeeklyStatusText(task)
    if not task
        or task.weeklyCommunity ~= true then
        return ""
    end

    local status =
        tostring(task.status or "Open")

    if status == "Open" then
        return qpstTC27Text(
            "UI_QPST_WeeklyStateOpen",
            "OPEN - waiting for survivor"
        )
    end

    if status == "Claimed" then
        return qpstTC27Text(
            "UI_QPST_WeeklyStateClaimedBy",
            "CLAIMED by %1",
            tostring(task.claimedBy or "")
        )
    end

    if status == "Submitted" then
        return qpstTC27Text(
            "UI_QPST_WeeklyStateSubmittedBy",
            "SUBMITTED by %1 - awaiting admin validation",
            qpstTC27WeeklyActor(task)
        )
    end

    if status == "Done" then
        local actor =
            qpstTC27WeeklyActor(task)

        local base =
            qpstTC27Text(
                "UI_QPST_WeeklyStateCompletedBy",
                "COMPLETED by %1",
                actor
            )

        if task.moneyClaimed == true
            or task.moneyAutoPaid == true then

            local paidTo =
                tostring(
                    task.moneyClaimedBy
                    or actor
                    or ""
                )

            return base
                .. "  |  "
                .. qpstTC27Text(
                    "UI_QPST_WeeklyStateRewardPaid",
                    "Reward paid to %1",
                    paidTo
                )
        end

        if task.moneyRewardPending == true then
            return base
                .. "  |  "
                .. qpstTC27Text(
                    "UI_QPST_WeeklyStateRewardPending",
                    "Reward pending for %1",
                    actor
                )
        end

        local validator =
            tostring(task.validatedBy or "")

        if validator ~= "" then
            return base
                .. "  |  "
                .. qpstTC27Text(
                    "UI_QPST_WeeklyStateValidatedBy",
                    "Validated by %1",
                    validator
                )
        end

        return base
    end

    return status
end

local qpstTC27OriginalDrawOpenItem =
    QPST_TaskBoardWindow.drawOpenItem

function QPST_TaskBoardWindow.drawOpenItem(
    list,
    y,
    item,
    alt
)
    local task =
        item and item.item or nil

    if not task
        or task.weeklyCommunity ~= true then
        return qpstTC27OriginalDrawOpenItem(
            list,
            y,
            item,
            alt
        )
    end

    local height =
        list.itemheight or 42

    if list.selected == item.index then
        list:drawRect(
            0,
            y,
            list:getWidth(),
            height - 1,
            0.20,
            0.30,
            0.20,
            0.34
        )
    elseif alt then
        list:drawRect(
            0,
            y,
            list:getWidth(),
            height - 1,
            0.08,
            0.14,
            0.08,
            0.16
        )
    end

    list:drawRectBorder(
        0,
        y + height - 1,
        list:getWidth(),
        1,
        0.30,
        0.55,
        0.55,
        0.55
    )

    local firstLine =
        fitQPSTText(
            getTaskDisplayText(task),
            UIFont.Small,
            list:getWidth() - 28
        )

    local secondLine =
        fitQPSTText(
            qpstTC27WeeklyStatusText(task),
            UIFont.Small,
            list:getWidth() - 28
        )

    list:drawText(
        firstLine,
        12,
        y + 4,
        1,
        1,
        1,
        1,
        UIFont.Small
    )

    list:drawText(
        secondLine,
        12,
        y + 21,
        0.72,
        0.88,
        0.72,
        1,
        UIFont.Small
    )

    return y + height
end

local qpstTC27OriginalDrawDoneItem =
    QPST_TaskBoardWindow.drawDoneItem

function QPST_TaskBoardWindow.drawDoneItem(
    list,
    y,
    item,
    alt
)
    local task =
        item and item.item or nil

    if not task
        or task.weeklyCommunity ~= true then
        return qpstTC27OriginalDrawDoneItem(
            list,
            y,
            item,
            alt
        )
    end

    local height =
        list.itemheight or 42

    if list.selected == item.index then
        list:drawRect(
            0,
            y,
            list:getWidth(),
            height - 1,
            0.20,
            0.30,
            0.20,
            0.34
        )
    elseif alt then
        list:drawRect(
            0,
            y,
            list:getWidth(),
            height - 1,
            0.08,
            0.14,
            0.08,
            0.16
        )
    end

    list:drawRectBorder(
        0,
        y + height - 1,
        list:getWidth(),
        1,
        0.28,
        0.45,
        0.65,
        0.45
    )

    local firstLine =
        fitQPSTText(
            getTaskDisplayText(task),
            UIFont.Small,
            list:getWidth() - 28
        )

    local secondLine =
        fitQPSTText(
            qpstTC27WeeklyStatusText(task),
            UIFont.Small,
            list:getWidth() - 28
        )

    list:drawText(
        firstLine,
        12,
        y + 4,
        0.82,
        1,
        0.82,
        1,
        UIFont.Small
    )

    list:drawText(
        secondLine,
        12,
        y + 21,
        0.65,
        0.95,
        0.65,
        1,
        UIFont.Small
    )

    return y + height
end
