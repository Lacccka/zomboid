-- QP Survivor Contracts
-- QPSC_B41_DUAL_BUILD_V1
-- Build 41.78.19 compatibility copy for v1.3.0
-- Client-side context menu + readable UI window
-- v1.0.0 Multi-Objective Contracts

require "ISUI/ISContextMenu"
require "ISUI/ISTextBox"
require "ISUI/ISPanel"
require "ISUI/ISButton"

-- QPSC_ISOLATED_I18N_CONNECTION_V1
require "QPSurvivorContracts/QPSC_I18N"
require "QPSurvivorContracts/QPSC_TrackedUI"
require "QPSurvivorContracts/QPSC_MultiObjectiveUI"
require "QPSurvivorContracts/QPSC_MapMarkers"

local MODULE = "QPSurvivorContracts"
local DATA_KEY = "QPSC_Data"

QPSC_Client = QPSC_Client or {}
QPSC_Client.contracts = QPSC_Client.contracts or {}
QPSC_Client.window = QPSC_Client.window or nil
QPSC_Client.timerMarks = QPSC_Client.timerMarks or {}
QPSC_Client.pendingOpenPlayerNum =
    QPSC_Client.pendingOpenPlayerNum or nil
QPSC_Client.announcementPopup =
    QPSC_Client.announcementPopup or nil
QPSC_Client.serverAdminAuthorized = false
QPSC_Client.serverAdminKnown = false
QPSC_Client.pendingInitialServerSync =
    QPSC_Client.pendingInitialServerSync == true
QPSC_Client.nextInitialServerSyncRetryMs =
    tonumber(QPSC_Client.nextInitialServerSyncRetryMs) or 0
QPSC_Client.pendingZombieKillReports =
    type(QPSC_Client.pendingZombieKillReports) == "table"
    and QPSC_Client.pendingZombieKillReports
    or {}
QPSC_Client.nextZombieKillReportSequence =
    tonumber(QPSC_Client.nextZombieKillReportSequence) or 0
QPSC_Client.nextImmediateLocationCheckMs =
    tonumber(QPSC_Client.nextImmediateLocationCheckMs) or 0
QPSC_Client.nextImmediateLocationRetryMs =
    tonumber(QPSC_Client.nextImmediateLocationRetryMs) or 0
QPSC_Client.lastImmediateLocationKey =
    tostring(QPSC_Client.lastImmediateLocationKey or "")

local function QPSC_dummy()
end

local function QPSC_say(player, text)
    if player == nil then player = getPlayer() end
    if player ~= nil then player:Say(tostring(text)) end
end

-- QPSC_LONG_ANNOUNCEMENT_POPUP_V1
local QPSC_ANNOUNCEMENT_POPUP_DURATION_MS = 10000

local function QPSC_realTimeMs()
    if getTimestampMs then
        local ok, value = pcall(getTimestampMs)

        if ok and value ~= nil then
            return tonumber(value) or 0
        end
    end

    return math.floor(os.clock() * 1000)
end

QPSC_AnnouncementPopup =
    ISPanel:derive("QPSC_AnnouncementPopup")

function QPSC_AnnouncementPopup:new(message)
    local screenWidth = getCore():getScreenWidth()
    local width = math.min(900, screenWidth - 40)

    if width < 320 then
        width = math.max(200, screenWidth - 10)
    end

    local height = 58
    local x = math.floor((screenWidth - width) / 2)
    local y = 56
    local o = ISPanel:new(x, y, width, height)

    setmetatable(o, self)
    self.__index = self

    o.message = tostring(message or "")
    o.startedAt = QPSC_realTimeMs()
    o.expiresAt =
        o.startedAt
        + QPSC_ANNOUNCEMENT_POPUP_DURATION_MS
    o.moveWithMouse = false
    o.backgroundColor = {r=0.02, g=0.02, b=0.02, a=0.94}
    o.borderColor = {r=0.84, g=0.66, b=0.20, a=1.0}

    return o
end

function QPSC_AnnouncementPopup:close()
    self:removeFromUIManager()

    if QPSC_Client.announcementPopup == self then
        QPSC_Client.announcementPopup = nil
    end
end

function QPSC_AnnouncementPopup:update()
    if ISPanel.update then
        ISPanel.update(self)
    end

    if QPSC_realTimeMs() >= self.expiresAt then
        self:close()
    end
end

function QPSC_AnnouncementPopup:prerender()
    local remaining =
        self.expiresAt - QPSC_realTimeMs()
    local alpha = 1.0

    if remaining < 1500 then
        alpha = math.max(0, remaining / 1500)
    end

    self:drawRect(
        0,
        0,
        self.width,
        self.height,
        0.94 * alpha,
        0.02,
        0.02,
        0.02
    )
    self:drawRectBorder(
        0,
        0,
        self.width,
        self.height,
        alpha,
        0.84,
        0.66,
        0.20
    )
    self:drawText(
        self.message,
        18,
        17,
        1,
        1,
        1,
        alpha,
        UIFont.Medium
    )
end

local function QPSC_showAnnouncementPopup(message)
    if QPSC_Client.announcementPopup ~= nil then
        QPSC_Client.announcementPopup:removeFromUIManager()
        QPSC_Client.announcementPopup = nil
    end

    local popup = QPSC_AnnouncementPopup:new(message)
    popup:initialise()
    popup:addToUIManager()

    QPSC_Client.announcementPopup = popup
end

-- QPSC_SCREEN_AND_CHAT_ANNOUNCEMENTS_V1
local function QPSC_announce(player, text)
    if player == nil then player = getPlayer() end
    if player == nil then return end

    local message = tostring(text or "")
    QPSC_say(player, message)
    QPSC_showAnnouncementPopup(message)

    -- QPSC_B42_19_HALO_ERROR_HOTFIX_V1
    -- The ColorRGB overload is documented but is not exposed to Lua in
    -- Build 42.19.0. Calling it increments the in-game ERROR counter even
    -- when wrapped in pcall. Use the working two-argument overload only.
    if HaloTextHelper and HaloTextHelper.addText then
        HaloTextHelper.addText(player, message)
    end
end

-- QPSC_V054_RUNTIME_ROUTE_AND_ADMIN_SYNC_V2
-- Keep multiplayer and single-player command routes separate. Dedicated
-- clients must never fall back to local Solo data, even if the GameClient
-- class was unavailable during an earlier startup frame.
local QPSC_gameClientClass = nil
local QPSC_singlePlayerClientClass = nil

local function QPSC_tryBindClass(className)
    if luajava == nil or luajava.bindClass == nil then
        return nil
    end

    local ok, value = pcall(function()
        return luajava.bindClass(className)
    end)

    if ok then return value end
    return nil
end

local function QPSC_resolveRuntimeClasses()
    -- Do not permanently cache a failed early lookup. Some dedicated
    -- clients expose the network classes only after the Lua file loads.
    if GameClient ~= nil then
        QPSC_gameClientClass = GameClient
    elseif QPSC_gameClientClass == nil then
        QPSC_gameClientClass =
            QPSC_tryBindClass("zombie.network.GameClient")
    end

    if SinglePlayerClient ~= nil then
        QPSC_singlePlayerClientClass = SinglePlayerClient
    elseif QPSC_singlePlayerClientClass == nil then
        QPSC_singlePlayerClientClass =
            QPSC_tryBindClass(
                "zombie.spnetwork.SinglePlayerClient"
            )
    end
end

local function QPSC_coreGameMode()
    if getCore == nil then return "" end

    local ok, value = pcall(function()
        return getCore():getGameMode()
    end)

    if ok and value ~= nil then
        return string.lower(tostring(value))
    end

    return ""
end

local function QPSC_modeLooksMultiplayer()
    local gameMode = QPSC_coreGameMode()

    return string.find(
        gameMode,
        "multiplayer",
        1,
        true
    ) ~= nil
end

local function QPSC_safeBooleanFunction(fn)
    if type(fn) ~= "function" then
        return false, false
    end

    local ok, value = pcall(fn)
    if not ok then return false, false end

    return true, value == true
end

local function QPSC_readStaticField(classValue, fieldName)
    if classValue == nil then return false, nil end

    local ok, value = pcall(function()
        return classValue[fieldName]
    end)

    if not ok then return false, nil end
    return true, value
end

local function QPSC_gameClientConnectionState()
    QPSC_resolveRuntimeClasses()

    local known, connection =
        QPSC_readStaticField(
            QPSC_gameClientClass,
            "connection"
        )

    return known, connection ~= nil
end

local function QPSC_hasGameClientConnection()
    local known, connected = QPSC_gameClientConnectionState()
    return known and connected
end

local function QPSC_gameClientFlag()
    QPSC_resolveRuntimeClasses()

    local ok, value =
        QPSC_readStaticField(
            QPSC_gameClientClass,
            "bClient"
        )

    return ok and value == true
end

local function QPSC_hasSinglePlayerBridge()
    QPSC_resolveRuntimeClasses()

    if QPSC_singlePlayerClientClass == nil then
        return false
    end

    local ok, commandMethod = pcall(function()
        return QPSC_singlePlayerClientClass.sendClientCommand
    end)

    return ok and commandMethod ~= nil
end

local function QPSC_playerHasRemoteOnlineId()
    if getPlayer == nil then return false end

    local player = getPlayer()
    if player == nil or player.getOnlineID == nil then
        return false
    end

    local ok, value = pcall(function()
        return player:getOnlineID()
    end)

    if not ok or value == nil then return false end
    return (tonumber(value) or -1) >= 0
end

local function QPSC_isRemoteMultiplayerRuntime()
    if QPSC_hasGameClientConnection() then
        return true
    end

    if QPSC_modeLooksMultiplayer() then
        return true
    end

    -- QPSC_DEDICATED_HOST_RUNTIME_ORDER_V2
    -- The SinglePlayerClient class may exist while connected to a hosted
    -- or dedicated server. Strong multiplayer signals must therefore be
    -- checked before using the bridge as a Solo hint.
    if QPSC_gameClientFlag() then
        return true
    end

    if QPSC_playerHasRemoteOnlineId() then
        return true
    end

    local knownClient, clientValue =
        QPSC_safeBooleanFunction(isClient)

    if knownClient and clientValue then
        -- Build 42 Solo can report isClient() while using the active
        -- SinglePlayerClient bridge. At this point no connection, bClient,
        -- multiplayer game mode, or remote online ID was detected.
        if QPSC_coreGameMode() ~= ""
            and QPSC_hasSinglePlayerBridge() then
            return false
        end

        return true
    end

    return false
end

local function QPSC_hasSinglePlayerCommandRoute()
    if QPSC_hasGameClientConnection()
        or QPSC_modeLooksMultiplayer()
        or QPSC_gameClientFlag()
        or QPSC_playerHasRemoteOnlineId() then
        return false
    end

    if getPlayer == nil or getPlayer() == nil then
        return false
    end

    if not QPSC_hasSinglePlayerBridge() then
        return false
    end

    local gameMode = QPSC_coreGameMode()
    local knownClient, clientValue =
        QPSC_safeBooleanFunction(isClient)

    -- When the mode is still unknown and the engine says client, fail
    -- closed instead of guessing that a dedicated client is Solo.
    if gameMode == "" and knownClient and clientValue then
        return false
    end

    return true
end

local function QPSC_isExplicitSinglePlayerRuntime()
    if QPSC_isRemoteMultiplayerRuntime() then
        return false
    end

    if QPSC_hasSinglePlayerCommandRoute() then
        return true
    end

    local knownClient, clientValue =
        QPSC_safeBooleanFunction(isClient)

    return knownClient and not clientValue
end

-- Kept under the original name because the rest of the UI uses it as
-- "server/bridge-backed runtime" versus the legacy local-data fallback.
local function QPSC_isMultiplayerClient()
    return QPSC_isRemoteMultiplayerRuntime()
        or QPSC_hasSinglePlayerCommandRoute()
end

-- QPSC_MULTIPLAYER_COMMAND_ROUTE_REV3
local function QPSC_sendCommand(command, args)
    command = tostring(command or "")
    args = args or {}

    if command == "" then return false end

    -- Hosted and dedicated clients can be valid multiplayer clients even
    -- when the Java connection field is temporarily unavailable to Lua.
    -- Use the complete runtime detector here instead of requiring direct
    -- access to GameClient.connection. True Solo is excluded by the same
    -- detector and continues through the SinglePlayerClient bridge below.
    if QPSC_isRemoteMultiplayerRuntime() then
        if type(sendClientCommand) ~= "function" then
            print(
                "[QPSC] Multiplayer command route unavailable: "
                    .. command
            )
            return false
        end

        -- QPSC_B42_CONNECTION_READY_GUARD_V1
        -- Build 42 can report multiplayer before GameClient.connection is
        -- assigned. Calling sendClientCommand during that startup window
        -- raises a Java NullPointerException even inside pcall. When the
        -- connection field is visible and still nil, delay the command. If
        -- the field is hidden from Lua, require a valid remote online ID as
        -- the readiness signal before attempting the send.
        local connectionKnown, connectionReady =
            QPSC_gameClientConnectionState()

        if connectionKnown and not connectionReady then
            print(
                "[QPSC] Multiplayer command delayed; connection not ready: "
                    .. command
            )
            return false
        end

        if not connectionKnown
            and not QPSC_playerHasRemoteOnlineId() then
            print(
                "[QPSC] Multiplayer command delayed; client not ready: "
                    .. command
            )
            return false
        end

        local ok, err = pcall(function()
            sendClientCommand(MODULE, command, args)
        end)

        if not ok then
            print(
                "[QPSC] Multiplayer command failed: "
                    .. command
                    .. " | "
                    .. tostring(err)
            )
        end

        return ok
    end

    if QPSC_hasSinglePlayerCommandRoute() then
        QPSC_resolveRuntimeClasses()
        local player = getPlayer()
        local ok, err = pcall(function()
            QPSC_singlePlayerClientClass.sendClientCommand(
                player,
                MODULE,
                command,
                args
            )
        end)

        if not ok then
            print(
                "[QPSC] Single-player command failed: "
                    .. command
                    .. " | "
                    .. tostring(err)
            )
        end

        return ok
    end

    print(
        "[QPSC] No command route available: "
            .. command
    )
    return false
end

-- QPSC_STRICT_ADMIN_PERMISSION_V3
local function QPSC_playerReportsAdmin(player)
    if player == nil then return false end

    if player.isAccessLevel then
        local ok, result = pcall(function()
            return player:isAccessLevel("admin")
        end)

        if ok and result == true then
            return true
        end
    end

    if player.getAccessLevel then
        local ok, access = pcall(function()
            return player:getAccessLevel()
        end)

        if ok and access ~= nil then
            return string.lower(tostring(access)) == "admin"
        end
    end

    return false
end

local function QPSC_isLocalAdmin(player)
    if QPSC_isExplicitSinglePlayerRuntime() then
        return true
    end

    if not QPSC_isRemoteMultiplayerRuntime() then
        return false
    end

    -- The server flag remains authoritative for received contract data.
    -- The synchronized IsoPlayer access level is also accepted for menu
    -- visibility so a real admin is not locked out while the first payload
    -- is still arriving. Every admin action is still verified server-side.
    return QPSC_Client.serverAdminAuthorized == true
        or QPSC_playerReportsAdmin(player)
end

local function QPSC_getLocalData()
    local data = ModData.getOrCreate(DATA_KEY)

    if data.contracts == nil then data.contracts = {} end
    if data.nextId == nil then data.nextId = 1 end

    return data
end

-- QPSC_NUMERIC_TRANSLATION_FORMAT_HOTFIX_V1
-- Keep numeric placeholder values as Lua numbers. Build 42's Java
-- Translator can use integer format specifiers, which reject strings.
-- QPSC_CONTRACT_DEADLINES_V1
local function QPSC_getWorldAgeHours()
    local ok, worldAgeHours = pcall(function()
        return getGameTime():getWorldAgeHours()
    end)

    if ok and worldAgeHours ~= nil then
        return tonumber(worldAgeHours) or 0
    end

    return 0
end

local function QPSC_normalizeTimeLimit(value)
    local number = tonumber(value)

    if number == nil or number ~= number or number <= 0 then
        return 0
    end

    return math.min(number, 8760)
end

local function QPSC_formatDuration(hours)
    local remaining = math.max(0, tonumber(hours) or 0)
    local totalMinutes = math.ceil(remaining * 60)

    if totalMinutes >= 1440 then
        local days = math.floor(totalMinutes / 1440)
        local hoursPart =
            math.floor((totalMinutes % 1440) / 60)

        return QPSC_I18N.getText(
            "UI_QPSC_TimeDaysHours",
            days,
            hoursPart
        )
    end

    local hoursPart = math.floor(totalMinutes / 60)
    local minutesPart = totalMinutes % 60

    if hoursPart > 0 and minutesPart > 0 then
        return QPSC_I18N.getText(
            "UI_QPSC_TimeHoursMinutes",
            hoursPart,
            minutesPart
        )
    end

    if hoursPart > 0 then
        return QPSC_I18N.getText(
            "UI_QPSC_TimeHours",
            hoursPart
        )
    end

    return QPSC_I18N.getText(
        "UI_QPSC_TimeMinutes",
        minutesPart
    )
end

local function QPSC_trim(text)
    text = tostring(text or "")
    return text:match("^%s*(.-)%s*$")
end

-- QPSC_CONTRACT_CATEGORY_BADGES_V1
local QPSC_CATEGORY_KEYS = {
    NONE = true,
    FUEL = true,
    FOOD = true,
    MECHANIC = true,
    MEDICAL = true,
    CONSTRUCTION = true,
    DELIVERY = true,
    DANGER = true
}

local QPSC_CATEGORY_MENU = {
    { key = "NONE", labelKey = "UI_QPSC_CategoryNone" },
    { key = "FUEL", labelKey = "UI_QPSC_CategoryFuel" },
    { key = "FOOD", labelKey = "UI_QPSC_CategoryFood" },
    { key = "MECHANIC", labelKey = "UI_QPSC_CategoryMechanic" },
    { key = "MEDICAL", labelKey = "UI_QPSC_CategoryMedical" },
    { key = "CONSTRUCTION", labelKey = "UI_QPSC_CategoryConstruction" },
    { key = "DELIVERY", labelKey = "UI_QPSC_CategoryDelivery" },
    { key = "DANGER", labelKey = "UI_QPSC_CategoryDanger" }
}

local QPSC_CATEGORY_BADGE_KEYS = {
    FUEL = "UI_QPSC_CategoryBadgeFuel",
    FOOD = "UI_QPSC_CategoryBadgeFood",
    MECHANIC = "UI_QPSC_CategoryBadgeMechanic",
    MEDICAL = "UI_QPSC_CategoryBadgeMedical",
    CONSTRUCTION = "UI_QPSC_CategoryBadgeConstruction",
    DELIVERY = "UI_QPSC_CategoryBadgeDelivery",
    DANGER = "UI_QPSC_CategoryBadgeDanger"
}

-- QPSC_CONTRACT_CATEGORY_ICONS_V1
local QPSC_CATEGORY_ICON_PATHS = {
    FUEL = "media/ui/QPSurvivorContracts/QPSC_Category_Fuel.png",
    FOOD = "media/ui/QPSurvivorContracts/QPSC_Category_Food.png",
    MECHANIC = "media/ui/QPSurvivorContracts/QPSC_Category_Mechanic.png",
    MEDICAL = "media/ui/QPSurvivorContracts/QPSC_Category_Medical.png",
    CONSTRUCTION = "media/ui/QPSurvivorContracts/QPSC_Category_Construction.png",
    DELIVERY = "media/ui/QPSurvivorContracts/QPSC_Category_Delivery.png",
    DANGER = "media/ui/QPSurvivorContracts/QPSC_Category_Danger.png"
}

local QPSC_CATEGORY_ICON_CACHE = {}
local QPSC_CATEGORY_ICON_MISSING = {}

local function QPSC_normalizeCategory(value)
    local category =
        string.upper(tostring(value or "NONE"))

    if QPSC_CATEGORY_KEYS[category] then
        return category
    end

    return "NONE"
end


-- QPSC_CONTRACT_DIFFICULTY_V070
local QPSC_DIFFICULTY_KEYS = {
    UNRATED = true,
    EASY = true,
    MEDIUM = true,
    HARD = true
}

local QPSC_DIFFICULTY_LABEL_KEYS = {
    UNRATED = "UI_QPSC_DifficultyUnrated",
    EASY = "UI_QPSC_DifficultyEasy",
    MEDIUM = "UI_QPSC_DifficultyMedium",
    HARD = "UI_QPSC_DifficultyHard"
}

local function QPSC_normalizeDifficulty(value)
    local difficulty = string.upper(tostring(value or "UNRATED"))

    if QPSC_DIFFICULTY_KEYS[difficulty] then
        return difficulty
    end

    return "UNRATED"
end

local function QPSC_difficultyText(value)
    local difficulty = QPSC_normalizeDifficulty(value)
    return QPSC_I18N.getText(
        QPSC_DIFFICULTY_LABEL_KEYS[difficulty]
        or "UI_QPSC_DifficultyUnrated"
    )
end

local function QPSC_difficultyColor(value)
    local difficulty = QPSC_normalizeDifficulty(value)

    if difficulty == "EASY" then
        return 0.28, 0.78, 0.38
    elseif difficulty == "MEDIUM" then
        return 0.95, 0.72, 0.20
    elseif difficulty == "HARD" then
        return 0.92, 0.28, 0.24
    end

    return 0.58, 0.58, 0.58
end


-- QPSC_TRACKED_OBJECTIVES_REWARDS_V1
local QPSC_OBJECTIVE_KEYS = {
    MANUAL = true,
    DELIVERY = true,
    KILL = true,
    LOCATION = true,
    MULTI = true
}

local function QPSC_normalizeObjectiveType(value)
    local objectiveType =
        string.upper(tostring(value or "MANUAL"))

    if QPSC_OBJECTIVE_KEYS[objectiveType] then
        return objectiveType
    end

    return "MANUAL"
end


-- QPSC_SHARED_TEAM_COMPLETION_V090
local QPSC_COMPLETION_MODE_KEYS = {
    INDIVIDUAL = true,
    GLOBAL = true,
    SHARED_TEAM = true
}

local function QPSC_normalizeCompletionMode(value)
    local mode = string.upper(tostring(value or "INDIVIDUAL"))

    if QPSC_COMPLETION_MODE_KEYS[mode] then
        return mode
    end

    return "INDIVIDUAL"
end

local function QPSC_isGlobalCompletion(contract)
    return QPSC_normalizeCompletionMode(
        contract and contract.completionMode
    ) == "GLOBAL"
end

local function QPSC_isSharedTeamCompletion(contract)
    return QPSC_normalizeCompletionMode(
        contract and contract.completionMode
    ) == "SHARED_TEAM"
end

local function QPSC_completionModeText(value)
    local mode = QPSC_normalizeCompletionMode(value)
    local key = "UI_QPSC_CompletionModeIndividual"

    if mode == "GLOBAL" then
        key = "UI_QPSC_CompletionModeGlobal"
    elseif mode == "SHARED_TEAM" then
        key = "UI_QPSC_CompletionModeSharedTeam"
    end

    return QPSC_I18N.getText(key)
end

local function QPSC_normalizePositiveInteger(value, maximum)
    local number = tonumber(value)

    if number == nil or number ~= number then
        return 0
    end

    number = math.floor(number)

    if number < 0 then return 0 end
    if maximum ~= nil then
        number = math.min(number, maximum)
    end

    return number
end

-- QPSC_V130_MULTIPLE_REWARDS_V1
local QPSC_MAX_REWARD_ITEMS = 5

local function QPSC_getContractRewardItems(contract)
    local rewards = {}
    local rawRewards =
        type(contract and contract.rewardItems) == "table"
        and contract.rewardItems
        or nil

    if rawRewards ~= nil then
        for index = 1, math.min(
            #rawRewards,
            QPSC_MAX_REWARD_ITEMS
        ) do
            local raw = rawRewards[index] or {}
            local fullType = tostring(
                raw.fullType or raw.itemFullType or ""
            )
            local displayName = tostring(
                raw.displayName or raw.itemDisplayName or ""
            )
            local quantity = QPSC_normalizePositiveInteger(
                raw.quantity,
                100
            )

            if fullType ~= "" and quantity > 0 then
                table.insert(rewards, {
                    fullType = fullType,
                    displayName = displayName,
                    quantity = quantity
                })
            end
        end
    end

    if #rewards == 0 and contract ~= nil then
        local fullType =
            tostring(contract.rewardItemFullType or "")
        local quantity =
            QPSC_normalizePositiveInteger(
                contract.rewardQuantity,
                100
            )

        if fullType ~= "" and quantity > 0 then
            table.insert(rewards, {
                fullType = fullType,
                displayName = tostring(
                    contract.rewardItemDisplayName or ""
                ),
                quantity = quantity
            })
        end
    end

    return rewards
end

local function QPSC_rewardItemsFromArgs(args)
    args = args or {}

    local rewards = {}
    local explicitCount =
        args.rewardCount ~= nil
        and tostring(args.rewardCount) ~= ""

    if explicitCount then
        local count = math.min(
            QPSC_MAX_REWARD_ITEMS,
            QPSC_normalizePositiveInteger(
                args.rewardCount,
                QPSC_MAX_REWARD_ITEMS
            )
        )

        for index = 1, count do
            local prefix = "reward" .. tostring(index)
            local fullType =
                tostring(
                    args[prefix .. "ItemFullType"] or ""
                )
            local quantity =
                QPSC_normalizePositiveInteger(
                    args[prefix .. "Quantity"],
                    100
                )

            if fullType ~= "" and quantity > 0 then
                table.insert(rewards, {
                    fullType = fullType,
                    displayName = tostring(
                        args[prefix .. "ItemDisplayName"]
                        or fullType
                    ),
                    quantity = quantity
                })
            end
        end
    else
        local fullType =
            tostring(args.rewardItemFullType or "")
        local quantity =
            QPSC_normalizePositiveInteger(
                args.rewardQuantity,
                100
            )

        if fullType ~= "" and quantity > 0 then
            table.insert(rewards, {
                fullType = fullType,
                displayName = tostring(
                    args.rewardItemDisplayName
                    or fullType
                ),
                quantity = quantity
            })
        end
    end

    return rewards
end

local function QPSC_applyContractRewardItems(
    contract,
    rewards
)
    if contract == nil then return end

    local normalized = {}

    for index = 1, math.min(
        #(rewards or {}),
        QPSC_MAX_REWARD_ITEMS
    ) do
        local reward = rewards[index] or {}
        local fullType =
            tostring(
                reward.fullType
                or reward.itemFullType
                or ""
            )
        local quantity =
            QPSC_normalizePositiveInteger(
                reward.quantity,
                100
            )

        if fullType ~= "" and quantity > 0 then
            table.insert(normalized, {
                fullType = fullType,
                displayName = tostring(
                    reward.displayName
                    or reward.itemDisplayName
                    or fullType
                ),
                quantity = quantity
            })
        end
    end

    contract.rewardItems = normalized

    local first = normalized[1]
    contract.rewardItemFullType =
        first and first.fullType or ""
    contract.rewardItemDisplayName =
        first and first.displayName or ""
    contract.rewardQuantity =
        first and first.quantity or 0
end

local function QPSC_rewardItemTexts(contract)
    local texts = {}

    for _, reward in ipairs(
        QPSC_getContractRewardItems(contract)
    ) do
        table.insert(
            texts,
            tostring(reward.quantity)
                .. " x "
                .. tostring(
                    reward.displayName ~= ""
                    and reward.displayName
                    or reward.fullType
                )
        )
    end

    return texts
end

local function QPSC_normalizeParticipantRewardCounts(
    contract,
    participant
)
    local rewards = QPSC_getContractRewardItems(contract)
    local oldCounts =
        type(participant.rewardGrantedCounts) == "table"
        and participant.rewardGrantedCounts
        or {}
    local normalized = {}

    for index, reward in ipairs(rewards) do
        local count

        if participant.rewardGranted == true then
            count = reward.quantity
        elseif oldCounts[index] ~= nil then
            count = QPSC_normalizePositiveInteger(
                oldCounts[index],
                reward.quantity
            )
        elseif index == 1 then
            count = QPSC_normalizePositiveInteger(
                participant.rewardGrantedCount,
                reward.quantity
            )
        else
            count = 0
        end

        normalized[index] = math.min(
            reward.quantity,
            count
        )
    end

    participant.rewardGrantedCounts = normalized
    participant.rewardGrantedCount =
        tonumber(normalized[1]) or 0

    return normalized
end


-- QPSC_REPUTATION_REWARD_V110
local QPSC_REPUTATION_KEYS = {
    community = true,
    hunter = true,
    explorer = true,
    medic = true,
    mechanic = true,
    builder = true
}

local QPSC_REPUTATION_LABELS = {
    community = "Community",
    hunter = "Hunter",
    explorer = "Explorer",
    medic = "Medic",
    mechanic = "Mechanic",
    builder = "Builder"
}

local function QPSC_normalizeReputationPath(value)
    local path = string.lower(tostring(value or ""))
    path = path:gsub("^%s+", ""):gsub("%s+$", "")

    if QPSC_REPUTATION_KEYS[path] then
        return path
    end

    return ""
end



-- QPSC_MULTI_OBJECTIVE_CONTRACTS_V100
local QPSC_MAX_OBJECTIVES = 5

local function QPSC_isMultiObjective(contract)
    return contract ~= nil
        and contract.multiObjective == true
        and type(contract.objectives) == "table"
        and #contract.objectives > 0
end

local function QPSC_multiObjectiveId(rawId, index)
    local value = tostring(rawId or "")
    if value == "" then value = "OBJ-" .. tostring(index or 1) end
    return string.sub(value, 1, 48)
end

local function QPSC_normalizeMultiObjective(raw, index, fallbackX, fallbackY, fallbackZ)
    raw = type(raw) == "table" and raw or {}
    local objectiveType = QPSC_normalizeObjectiveType(raw.type or raw.objectiveType)
    if objectiveType ~= "DELIVERY" and objectiveType ~= "KILL" and objectiveType ~= "LOCATION" then
        objectiveType = "KILL"
    end
    local target = QPSC_normalizePositiveInteger(raw.target or raw.objectiveTarget, 10000)
    local radius = QPSC_normalizePositiveInteger(raw.radius or raw.objectiveRadius, 1000)
    if objectiveType == "LOCATION" then
        target = 1
        radius = math.max(1, math.min(20, radius > 0 and radius or 3))
    elseif objectiveType == "KILL" then
        target = math.max(1, target)
        radius = math.max(1, math.min(1000, radius > 0 and radius or 100))
    else
        target = math.max(1, target)
        radius = 0
    end
    return {
        id = QPSC_multiObjectiveId(raw.id or raw.objectiveId, index),
        type = objectiveType,
        target = target,
        radius = radius,
        itemFullType = tostring(raw.itemFullType or raw.objectiveItemFullType or ""),
        itemDisplayName = tostring(raw.itemDisplayName or raw.objectiveItemDisplayName or ""),
        targetX = math.floor(tonumber(raw.targetX) or tonumber(fallbackX) or 0),
        targetY = math.floor(tonumber(raw.targetY) or tonumber(fallbackY) or 0),
        targetZ = math.floor(tonumber(raw.targetZ) or tonumber(fallbackZ) or 0)
    }
end

local function QPSC_ensureMultiContractState(contract)
    if contract == nil then return false end
    if type(contract.objectives) == "table" and #contract.objectives > 0 then
        contract.multiObjective = true
    end
    if not QPSC_isMultiObjective(contract) then return false end
    local changed = false
    if type(contract.sharedObjectiveProgress) ~= "table" then
        contract.sharedObjectiveProgress = {}
        changed = true
    end
    for index, raw in ipairs(contract.objectives or {}) do
        local objective = QPSC_normalizeMultiObjective(raw, index, contract.targetX, contract.targetY, contract.targetZ)
        contract.objectives[index] = objective
        if QPSC_isSharedTeamCompletion(contract) then
            contract.sharedObjectiveProgress[objective.id] = QPSC_normalizePositiveInteger(
                contract.sharedObjectiveProgress[objective.id], 1000000
            )
        end
    end
    contract.objectiveType = "MULTI"
    contract.objectiveTarget = #contract.objectives
    contract.objectiveRadius = 0
    contract.objectiveItemFullType = ""
    contract.objectiveItemDisplayName = ""
    return changed
end

local function QPSC_ensureParticipantMultiState(contract, participant)
    if not QPSC_isMultiObjective(contract) or participant == nil then return false end
    local changed = false
    if type(participant.objectiveProgress) ~= "table" then participant.objectiveProgress = {}; changed = true end
    if type(participant.objectiveContributions) ~= "table" then participant.objectiveContributions = {}; changed = true end
    for _, objective in ipairs(contract.objectives or {}) do
        local id = tostring(objective.id or "")
        participant.objectiveProgress[id] = QPSC_normalizePositiveInteger(participant.objectiveProgress[id], 1000000)
        participant.objectiveContributions[id] = QPSC_normalizePositiveInteger(participant.objectiveContributions[id], 1000000)
    end
    return changed
end

local function QPSC_getMultiProgress(contract, participant, objective)
    if objective == nil then return 0 end
    local id = tostring(objective.id or "")
    if QPSC_isSharedTeamCompletion(contract) then
        contract.sharedObjectiveProgress = type(contract.sharedObjectiveProgress) == "table" and contract.sharedObjectiveProgress or {}
        return tonumber(contract.sharedObjectiveProgress[id]) or 0
    end
    QPSC_ensureParticipantMultiState(contract, participant)
    return participant and tonumber(participant.objectiveProgress[id]) or 0
end

local function QPSC_setMultiProgress(contract, participant, objective, value)
    if objective == nil then return 0 end
    local id = tostring(objective.id or "")
    local target = math.max(1, tonumber(objective.target) or 1)
    local progress = math.min(target, QPSC_normalizePositiveInteger(value, 1000000))
    if QPSC_isSharedTeamCompletion(contract) then
        contract.sharedObjectiveProgress = type(contract.sharedObjectiveProgress) == "table" and contract.sharedObjectiveProgress or {}
        contract.sharedObjectiveProgress[id] = progress
    elseif participant ~= nil then
        QPSC_ensureParticipantMultiState(contract, participant)
        participant.objectiveProgress[id] = progress
    end
    return progress
end

local function QPSC_addMultiContribution(participant, objective, amount)
    if participant == nil or objective == nil then return 0 end
    participant.objectiveContributions = type(participant.objectiveContributions) == "table" and participant.objectiveContributions or {}
    local id = tostring(objective.id or "")
    participant.objectiveContributions[id] = (tonumber(participant.objectiveContributions[id]) or 0)
        + math.max(0, math.floor(tonumber(amount) or 0))
    return participant.objectiveContributions[id]
end

local function QPSC_getMultiContributionTotal(participant)
    local total = 0
    for _, value in pairs(type(participant and participant.objectiveContributions) == "table" and participant.objectiveContributions or {}) do
        total = total + math.max(0, tonumber(value) or 0)
    end
    return total
end

local function QPSC_multiObjectiveCounts(contract, participant)
    local completed = 0
    local total = QPSC_isMultiObjective(contract) and #contract.objectives or 0
    for _, objective in ipairs(contract and contract.objectives or {}) do
        if QPSC_getMultiProgress(contract, participant, objective) >= math.max(1, tonumber(objective.target) or 1) then
            completed = completed + 1
        end
    end
    return completed, total
end

local function QPSC_allMultiObjectivesComplete(contract, participant)
    local completed, total = QPSC_multiObjectiveCounts(contract, participant)
    return total > 0 and completed >= total
end

local function QPSC_isPlayerNearMultiObjective(player, objective)
    if player == nil or objective == nil then return false end
    local dx = (tonumber(player:getX()) or 0) - (tonumber(objective.targetX) or 0)
    local dy = (tonumber(player:getY()) or 0) - (tonumber(objective.targetY) or 0)
    local dz = math.abs((tonumber(player:getZ()) or 0) - (tonumber(objective.targetZ) or 0))
    local radius = math.max(1, tonumber(objective.radius) or 3)
    return dz <= 1 and ((dx * dx) + (dy * dy)) <= (radius * radius)
end

local function QPSC_multiObjectiveTypeText(objective)
    local objectiveType = tostring(objective and objective.type or "")
    if objectiveType == "DELIVERY" then return QPSC_I18N.getText("UI_QPSC_ObjectiveDelivery") end
    if objectiveType == "KILL" then return QPSC_I18N.getText("UI_QPSC_ObjectiveKill") end
    return QPSC_I18N.getText("UI_QPSC_ObjectiveLocation")
end

local function QPSC_objectiveTypeText(contract)
    local objectiveType =
        QPSC_normalizeObjectiveType(
            contract and contract.objectiveType
        )

    if objectiveType == "MULTI" then
        return QPSC_I18N.getText("UI_QPSC_ObjectiveMulti")
    elseif objectiveType == "DELIVERY" then
        return QPSC_I18N.getText(
            "UI_QPSC_ObjectiveDelivery"
        )
    elseif objectiveType == "KILL" then
        return QPSC_I18N.getText(
            "UI_QPSC_ObjectiveKill"
        )
    elseif objectiveType == "LOCATION" then
        return QPSC_I18N.getText(
            "UI_QPSC_ObjectiveLocation"
        )
    end

    return QPSC_I18N.getText(
        "UI_QPSC_ObjectiveManual"
    )
end

local function QPSC_getCategoryIcon(value)
    local category = value

    if type(value) == "table" then
        category = value.category
    end

    category = QPSC_normalizeCategory(category)

    if category == "NONE"
        or QPSC_CATEGORY_ICON_MISSING[category] == true then
        return nil
    end

    local cached = QPSC_CATEGORY_ICON_CACHE[category]

    if cached ~= nil then
        return cached
    end

    local path = QPSC_CATEGORY_ICON_PATHS[category]

    if path == nil or getTexture == nil then
        QPSC_CATEGORY_ICON_MISSING[category] = true
        return nil
    end

    local ok, texture = pcall(function()
        return getTexture(path)
    end)

    if not ok or texture == nil then
        QPSC_CATEGORY_ICON_MISSING[category] = true
        return nil
    end

    QPSC_CATEGORY_ICON_CACHE[category] = texture
    return texture
end

local function QPSC_categoryBadge(value)
    local category = value

    if type(value) == "table" then
        category = value.category
    end

    category = QPSC_normalizeCategory(category)

    local key = QPSC_CATEGORY_BADGE_KEYS[category]

    if key == nil then
        return ""
    end

    local badge = QPSC_I18N.getText(key)

    if badge == nil
        or badge == ""
        or badge == key then
        return ""
    end

    return tostring(badge) .. " "
end

local function QPSC_contractPlainTitle(contract)
    return tostring(
        contract and contract.title
        or QPSC_I18N.getText("UI_QPSC_Untitled")
    )
end

local function QPSC_contractTitleText(contract)
    return QPSC_categoryBadge(contract)
        .. QPSC_contractPlainTitle(contract)
end

local function QPSC_getPlayerUsername(player)
    if player and player.getUsername then
        local ok, username = pcall(function()
            return player:getUsername()
        end)

        if ok and username and username ~= "" then
            return tostring(username)
        end
    end

    return "Solo Player"
end

local function QPSC_normalizeUsername(value)
    return string.lower(tostring(value or ""))
end

local function QPSC_sameUsername(left, right)
    return QPSC_normalizeUsername(left)
        == QPSC_normalizeUsername(right)
end


-- QPSC_V122_REPUTATION_DELIVERY_RECOVERY_V1
local function QPSC_awardLocalContractReputation(
    contract,
    participant,
    actor
)
    local username = tostring(
        participant and participant.username or ""
    )

    if username == "" then
        return false, "disabled"
    end

    local primaryPath = QPSC_normalizeReputationPath(
        contract and contract.reputationPath
    )
    local primaryPoints = QPSC_normalizePositiveInteger(
        contract and contract.reputationPoints,
        100000
    )
    local secondaryPath = QPSC_normalizeReputationPath(
        contract and contract.secondaryReputationPath
    )
    local secondaryPoints = QPSC_normalizePositiveInteger(
        contract and contract.secondaryReputationPoints,
        100000
    )

    if secondaryPath == primaryPath then
        secondaryPath = ""
        secondaryPoints = 0
    end

    local primaryConfigured =
        primaryPath ~= "" and primaryPoints > 0
    local secondaryConfigured =
        secondaryPath ~= "" and secondaryPoints > 0
    local configuredCount =
        (primaryConfigured and 1 or 0)
        + (secondaryConfigured and 1 or 0)

    participant.reputationRewardAttempts =
        math.max(
            0,
            math.floor(
                tonumber(
                    participant.reputationRewardAttempts
                ) or 0
            )
        ) + 1

    if configuredCount < 1 then
        participant.reputationRewardPending = false
        participant.reputationRewardResolved = true
        participant.reputationRewardLastResult = "disabled"
        return false, "disabled"
    end

    if QPReputation == nil
        or QPReputation.Server == nil
        or type(
            QPReputation.Server.awardExternal
        ) ~= "function" then
        participant.reputationRewardPending = true
        participant.reputationRewardResolved = false
        participant.reputationRewardLastResult =
            "api_unavailable"
        return false, "api_unavailable"
    end

    local reason =
        "Contract completed: "
        .. tostring(
            contract and contract.title
            or ("#" .. tostring(contract and contract.id or ""))
        )
    local awardedAny = false
    local resolvedCount = 0
    local lastResult = "disabled"

    local function awardOne(
        path,
        points,
        sourceSuffix
    )
        if path == "" or points < 1 then
            return false, true, "disabled"
        end

        local sourceId =
            "qpsc:contract:"
            .. tostring(contract and contract.id or "")
            .. ":participant:"
            .. QPSC_normalizeUsername(username)
            .. tostring(sourceSuffix)

        local ok, awarded, result = pcall(
            QPReputation.Server.awardExternal,
            username,
            path,
            points,
            sourceId,
            reason,
            tostring(actor or "QPSurvivorContracts")
        )

        if not ok then
            return false, false, "integration_error"
        end

        local resultText =
            string.lower(tostring(result or ""))
        local duplicate =
            resultText == "duplicate_award"
            or string.find(
                resultText,
                "duplicate",
                1,
                true
            ) ~= nil

        if awarded == true then
            return true, true, tostring(result or "awarded")
        end

        if duplicate then
            return false, true, tostring(result or "duplicate_award")
        end

        return false, false, tostring(result or "not_awarded")
    end

    if primaryConfigured then
        local awarded, resolved, result =
            awardOne(
                primaryPath,
                primaryPoints,
                ":reputation:v1"
            )

        if awarded then awardedAny = true end
        if resolved then resolvedCount = resolvedCount + 1 end
        lastResult = result
    end

    if secondaryConfigured then
        local awarded, resolved, result =
            awardOne(
                secondaryPath,
                secondaryPoints,
                ":reputation:v1:secondary"
            )

        if awarded then awardedAny = true end
        if resolved then resolvedCount = resolvedCount + 1 end
        lastResult = result
    end

    local allResolved = resolvedCount >= configuredCount

    participant.reputationRewardPending =
        not allResolved
    participant.reputationRewardResolved =
        allResolved
    participant.reputationRewardLastResult =
        tostring(lastResult or "")

    if allResolved then
        participant.reputationRewardResolvedBy =
            tostring(actor or "QPSurvivorContracts")
    end

    return awardedAny, lastResult
end

local function QPSC_normalizeParticipantStatus(status)
    status = tostring(status or "Accepted")

    if status == "Not Completed" then
        return "NotCompleted"
    end

    if status == "Completed"
        or status == "NotCompleted"
        or status == "Expired"
        or status == "Cancelled"
        or status == "ClosedByOther" then
        return status
    end

    return "Accepted"
end

local function QPSC_findParticipant(contract, username)
    for _, participant in ipairs(contract.participants or {}) do
        if QPSC_sameUsername(participant.username, username) then
            return participant
        end
    end

    return nil
end

local function QPSC_timerKey(contractId, username)
    return tostring(contractId)
        .. "|"
        .. QPSC_normalizeUsername(username)
end

local function QPSC_recomputeContractStatus(contract)
    if QPSC_isSharedTeamCompletion(contract)
        and contract.sharedCompleted == true then
        contract.closed = true
        contract.status = "Completed"
        contract.acceptedBy = ""
        contract.completedBy = "Shared Team"
        return
    end

    if contract.globalCompleted == true then
        contract.closed = true
        contract.status = "Completed"
        contract.acceptedBy = ""
        contract.completedBy = tostring(
            contract.globalCompletedBy or ""
        )
        return
    end

    if contract.closed == true then
        contract.status =
            tostring(contract.legacyClosedStatus or "Closed")
        return
    end

    local hasActive = false
    local firstActive = nil

    for _, participant in ipairs(contract.participants or {}) do
        if tostring(participant.status or "") == "Accepted" then
            hasActive = true

            if firstActive == nil then
                firstActive = participant
            end
        end
    end

    contract.status = hasActive and "Accepted" or "Open"
    contract.acceptedBy =
        firstActive
        and tostring(firstActive.username or "")
        or ""

    contract.completedBy = ""

    for _, participant in ipairs(contract.participants or {}) do
        if tostring(participant.status or "") == "Completed"
            and tostring(participant.reviewedBy or "") ~= "" then
            contract.completedBy =
                tostring(participant.reviewedBy)
            break
        end
    end
end

-- QPSC_PARTICIPANT_MODEL_V1
local function QPSC_migrateLocalContract(contract, now)
    local changed = false

    local normalizedCategory =
        QPSC_normalizeCategory(contract.category)

    if tostring(contract.category or "")
        ~= normalizedCategory then
        contract.category = normalizedCategory
        changed = true
    end

    local normalizedDifficulty =
        QPSC_normalizeDifficulty(contract.difficulty)

    if tostring(contract.difficulty or "")
        ~= normalizedDifficulty then
        contract.difficulty = normalizedDifficulty
        changed = true
    else
        contract.difficulty = normalizedDifficulty
    end

    local normalizedCompletionMode =
        QPSC_normalizeCompletionMode(contract.completionMode)

    if tostring(contract.completionMode or "")
        ~= normalizedCompletionMode then
        contract.completionMode = normalizedCompletionMode
        changed = true
    else
        contract.completionMode = normalizedCompletionMode
    end

    contract.globalCompleted = contract.globalCompleted == true
    contract.globalCompletedBy = tostring(
        contract.globalCompletedBy or ""
    )
    contract.globalCompletedAt = tonumber(
        contract.globalCompletedAt
    ) or 0
    contract.globalCompletionSource = tostring(
        contract.globalCompletionSource or ""
    )

    if contract.globalCompleted == true then
        contract.closed = true
        contract.legacyClosedStatus = "Completed"
    end


    -- Shared Team fields are initialized only for Shared Team contracts.
    -- Existing Individual and Global contracts remain byte-for-byte
    -- compatible in their persisted contract model.
    if QPSC_isSharedTeamCompletion(contract) then
        contract.sharedProgress =
            QPSC_normalizePositiveInteger(
                contract.sharedProgress,
                1000000
            )
        contract.sharedCompleted =
            contract.sharedCompleted == true
        contract.sharedCompletedBy = tostring(
            contract.sharedCompletedBy or ""
        )
        contract.sharedCompletedAt = tonumber(
            contract.sharedCompletedAt
        ) or 0
        contract.sharedCompletionSource = tostring(
            contract.sharedCompletionSource or ""
        )

        if contract.sharedCompleted == true then
            contract.closed = true
            contract.legacyClosedStatus = "Completed"
        end
    end

    if type(contract.objectives) == "table"
        and #contract.objectives > 0 then
        if contract.multiObjective ~= true then
            contract.multiObjective = true
            changed = true
        end
        if QPSC_ensureMultiContractState(contract) then changed = true end
    end

    local objectiveType = QPSC_isMultiObjective(contract)
        and "MULTI"
        or QPSC_normalizeObjectiveType(contract.objectiveType)

    if tostring(contract.objectiveType or "MANUAL")
        ~= objectiveType then
        contract.objectiveType = objectiveType
        changed = true
    else
        contract.objectiveType = objectiveType
    end

    local objectiveTarget =
        QPSC_normalizePositiveInteger(
            contract.objectiveTarget,
            10000
        )

    if objectiveType == "MULTI" then
        objectiveTarget = #(contract.objectives or {})
    elseif objectiveType ~= "MANUAL" and objectiveTarget < 1 then
        objectiveTarget = 1
    end

    contract.objectiveTarget = objectiveTarget
    contract.objectiveRadius =
        QPSC_normalizePositiveInteger(
            contract.objectiveRadius,
            1000
        )

    if objectiveType == "LOCATION" then
        if contract.objectiveRadius < 1 then
            contract.objectiveRadius = 3
        elseif contract.objectiveRadius > 20 then
            contract.objectiveRadius = 20
        end
    end

    contract.objectiveItemFullType =
        tostring(contract.objectiveItemFullType or "")
    contract.objectiveItemDisplayName =
        tostring(contract.objectiveItemDisplayName or "")
    contract.targetX = tonumber(contract.targetX) or 0
    contract.targetY = tonumber(contract.targetY) or 0
    contract.targetZ = tonumber(contract.targetZ) or 0
    contract.rewardItemFullType =
        tostring(contract.rewardItemFullType or "")
    contract.rewardItemDisplayName =
        tostring(contract.rewardItemDisplayName or "")
    contract.rewardQuantity =
        QPSC_normalizePositiveInteger(
            contract.rewardQuantity,
            100
        )

    local hadRewardItems =
        type(contract.rewardItems) == "table"
    local normalizedRewardItems =
        QPSC_getContractRewardItems(contract)

    QPSC_applyContractRewardItems(
        contract,
        normalizedRewardItems
    )

    if not hadRewardItems then
        changed = true
    end

    contract.reputationPath =
        QPSC_normalizeReputationPath(
            contract.reputationPath
        )
    contract.reputationPoints =
        QPSC_normalizePositiveInteger(
            contract.reputationPoints,
            100000
        )

    if contract.reputationPath == ""
        or contract.reputationPoints < 1 then
        contract.reputationPath = ""
        contract.reputationPoints = 0
    end
    contract.secondaryReputationPath =
        QPSC_normalizeReputationPath(
            contract.secondaryReputationPath
        )
    contract.secondaryReputationPoints =
        QPSC_normalizePositiveInteger(
            contract.secondaryReputationPoints,
            100000
        )

    if contract.secondaryReputationPath == ""
        or contract.secondaryReputationPoints < 1
        or contract.secondaryReputationPath
            == contract.reputationPath then
        contract.secondaryReputationPath = ""
        contract.secondaryReputationPoints = 0
    end

    contract.firstFinisherBonusItemFullType =
        tostring(
            contract.firstFinisherBonusItemFullType or ""
        )
    contract.firstFinisherBonusItemDisplayName =
        tostring(
            contract.firstFinisherBonusItemDisplayName or ""
        )
    contract.firstFinisherBonusQuantity =
        QPSC_normalizePositiveInteger(
            contract.firstFinisherBonusQuantity,
            100
        )
    contract.firstFinisherWinner =
        tostring(contract.firstFinisherWinner or "")
    contract.firstFinisherWonAt =
        tonumber(contract.firstFinisherWonAt) or 0

    if type(contract.participants) ~= "table" then
        contract.participants = {}
        changed = true
    end

    local timeLimitHours =
        QPSC_normalizeTimeLimit(contract.timeLimitHours)
    contract.timeLimitHours = timeLimitHours

    if #contract.participants == 0 then
        local legacyAcceptedBy =
            tostring(contract.acceptedBy or "")
        local legacyStatus =
            tostring(contract.status or "Open")

        if legacyAcceptedBy ~= "" then
            local participantStatus = "Accepted"

            if legacyStatus == "Completed" then
                participantStatus = "Completed"
            elseif legacyStatus == "Expired" then
                participantStatus = "Expired"
            end

            local remainingHours = 0

            if participantStatus == "Accepted"
                and timeLimitHours > 0 then
                local legacyExpiresAt =
                    tonumber(contract.expiresAt) or 0

                if legacyExpiresAt > 0 then
                    remainingHours =
                        math.max(0, legacyExpiresAt - now)
                else
                    remainingHours = timeLimitHours
                end

                if remainingHours <= 0 then
                    participantStatus = "Expired"
                end
            end

            table.insert(contract.participants, {
                username = legacyAcceptedBy,
                status = participantStatus,
                acceptedAt =
                    tonumber(contract.acceptedAt) or 0,
                remainingHours = remainingHours,
                completedAt = 0,
                expiredAt =
                    tonumber(contract.expiredAt) or 0,
                cancelledAt = 0,
                closedAt = 0,
                closedBy = "",
                reviewedAt = 0,
                reviewedBy =
                    participantStatus == "Completed"
                    and tostring(contract.completedBy or "")
                    or "",
                progress = 0,
                rewardGranted = false,
                rewardGrantedCount = 0,
                rewardGrantedCounts = {},
                rewardGrantedAt = 0,
                rewardPending = false,
                firstFinisherBonusGranted = false,
                firstFinisherBonusGrantedCount = 0,
                firstFinisherBonusGrantedAt = 0,
                firstFinisherBonusPending = false
            })

            if legacyStatus == "Completed"
                or legacyStatus == "Expired" then
                contract.closed = true
                contract.legacyClosedStatus = legacyStatus
            end

            changed = true
        elseif legacyStatus == "Completed"
            or legacyStatus == "Expired"
            or legacyStatus == "Closed" then
            contract.closed = true
            contract.legacyClosedStatus = legacyStatus
            changed = true
        end
    end

    for _, participant in ipairs(contract.participants) do
        participant.username =
            tostring(participant.username or "")
        participant.status =
            QPSC_normalizeParticipantStatus(
                participant.status
            )
        participant.acceptedAt =
            tonumber(participant.acceptedAt) or 0
        participant.completedAt =
            tonumber(participant.completedAt) or 0
        participant.expiredAt =
            tonumber(participant.expiredAt) or 0
        participant.cancelledAt =
            tonumber(participant.cancelledAt) or 0
        participant.closedAt =
            tonumber(participant.closedAt) or 0
        participant.closedBy =
            tostring(participant.closedBy or "")
        participant.reviewedAt =
            tonumber(participant.reviewedAt) or 0
        participant.reviewedBy =
            tostring(participant.reviewedBy or "")
        participant.progress =
            QPSC_normalizePositiveInteger(
                participant.progress,
                1000000
            )
        participant.rewardGranted =
            participant.rewardGranted == true
        participant.rewardGrantedCount =
            QPSC_normalizePositiveInteger(
                participant.rewardGrantedCount,
                100
            )
        participant.rewardGrantedAt =
            tonumber(participant.rewardGrantedAt) or 0
        participant.rewardPending =
            participant.rewardPending == true

        if type(participant.rewardGrantedCounts)
            ~= "table" then
            changed = true
        end

        QPSC_normalizeParticipantRewardCounts(
            contract,
            participant
        )

        participant.firstFinisherBonusGranted =
            participant.firstFinisherBonusGranted == true
        participant.firstFinisherBonusGrantedCount =
            QPSC_normalizePositiveInteger(
                participant.firstFinisherBonusGrantedCount,
                100
            )
        participant.firstFinisherBonusGrantedAt =
            tonumber(
                participant.firstFinisherBonusGrantedAt
            ) or 0
        participant.firstFinisherBonusPending =
            participant.firstFinisherBonusPending == true

        if QPSC_ensureParticipantMultiState(contract, participant) then
            changed = true
        end

        if participant.remainingHours == nil then
            local remainingHours = 0

            if participant.status == "Accepted"
                and timeLimitHours > 0 then
                local legacyExpiresAt =
                    tonumber(contract.expiresAt) or 0

                if legacyExpiresAt > 0 then
                    remainingHours =
                        math.max(0, legacyExpiresAt - now)
                else
                    remainingHours = timeLimitHours
                end
            end

            participant.remainingHours = remainingHours
            changed = true
        else
            participant.remainingHours =
                math.max(
                    0,
                    tonumber(participant.remainingHours) or 0
                )
        end

        if participant.status == "Accepted"
            and timeLimitHours > 0
            and participant.remainingHours <= 0 then
            participant.status = "Expired"
            participant.expiredAt = now
            changed = true
        end
    end

    QPSC_recomputeContractStatus(contract)
    return changed
end

local function QPSC_migrateLocalData(data)
    local now = QPSC_getWorldAgeHours()
    local changed = false

    for _, contract in ipairs(data.contracts or {}) do
        if QPSC_migrateLocalContract(contract, now) then
            changed = true
        end
    end

    data.schemaVersion = 10
    return changed
end

-- QPSC_ONLINE_ONLY_PARTICIPANT_TIMERS_V1
local function QPSC_updateLocalParticipantTimers(data)
    if QPSC_isMultiplayerClient() then return false end

    data = data or QPSC_getLocalData()
    QPSC_migrateLocalData(data)

    local player = getPlayer()
    local username = QPSC_getPlayerUsername(player)
    local now = QPSC_getWorldAgeHours()
    local changed = false

    for _, contract in ipairs(data.contracts or {}) do
        local timeLimitHours =
            QPSC_normalizeTimeLimit(
                contract.timeLimitHours
            )

        for _, participant in ipairs(
            contract.participants or {}
        ) do
            local key = QPSC_timerKey(
                contract.id,
                participant.username
            )

            if participant.status == "Accepted"
                and timeLimitHours > 0
                and QPSC_sameUsername(
                    participant.username,
                    username
                ) then
                local previousMark =
                    tonumber(QPSC_Client.timerMarks[key])

                if previousMark ~= nil
                    and now >= previousMark then
                    local elapsed =
                        math.max(0, now - previousMark)

                    if elapsed > 0 then
                        local previousRemaining =
                            tonumber(
                                participant.remainingHours
                            ) or timeLimitHours
                        local remaining =
                            math.max(
                                0,
                                previousRemaining - elapsed
                            )

                        if math.abs(
                            remaining - previousRemaining
                        ) > 0.000001 then
                            participant.remainingHours = remaining
                            changed = true
                        end
                    end
                end

                QPSC_Client.timerMarks[key] = now

                if (
                    tonumber(participant.remainingHours) or 0
                ) <= 0 then
                    participant.remainingHours = 0
                    participant.status = "Expired"
                    participant.expiredAt = now
                    QPSC_Client.timerMarks[key] = nil
                    changed = true
                end
            else
                QPSC_Client.timerMarks[key] = nil
            end
        end

        QPSC_recomputeContractStatus(contract)
    end

    return changed
end

local function QPSC_loadLocalContracts()
    if QPSC_isMultiplayerClient() then return end

    local data = QPSC_getLocalData()
    QPSC_migrateLocalData(data)
    QPSC_updateLocalParticipantTimers(data)
    QPSC_Client.contracts = data.contracts or {}
end

local function QPSC_saveLocalContracts()
    if QPSC_isMultiplayerClient() then return end

    local data = QPSC_getLocalData()
    data.contracts = QPSC_Client.contracts or {}
    data.schemaVersion = 10
end

-- QPSC_ONE_ACTIVE_CONTRACT_V1
local function QPSC_findActiveContractForPlayer(player)
    local username = QPSC_getPlayerUsername(player)

    for _, contract in ipairs(QPSC_Client.contracts or {}) do
        local participant =
            QPSC_findParticipant(contract, username)

        if participant
            and tostring(participant.status or "") == "Accepted" then
            return contract, participant
        end
    end

    return nil, nil
end

-- QPSC_ACCEPT_FROM_VIEW_V1
local function QPSC_findContractById(contractId)
    if contractId == nil then
        return nil
    end

    for _, contract in ipairs(
        QPSC_Client.contracts or {}
    ) do
        if tostring(contract.id)
            == tostring(contractId) then
            return contract
        end
    end

    return nil
end


-- QPSC_LOCAL_TRACKED_OBJECTIVE_RUNTIME_V1
local function QPSC_getPlayerSquare(player)
    if player == nil then return nil end

    local ok, square = pcall(function()
        return player:getSquare()
    end)

    if ok then return square end
    return nil
end

local function QPSC_getSquareAt(x, y, z)
    local cell = getCell and getCell() or nil
    if cell == nil then return nil end

    local ok, square = pcall(function()
        return cell:getGridSquare(
            math.floor(tonumber(x) or 0),
            math.floor(tonumber(y) or 0),
            math.floor(tonumber(z) or 0)
        )
    end)

    if ok then return square end
    return nil
end

-- QPSC_V053_PLAYER_REWARD_DROP_AND_LOCK_V1
local QPSC_localRewardLocks = {}

local function QPSC_localRewardLockKey(contract, participant)
    return tostring(contract and contract.id or "")
        .. "|"
        .. QPSC_normalizeUsername(
            participant and participant.username or ""
        )
end

local function QPSC_spawnLocalReward(
    contract,
    participant,
    player,
    square
)
    if participant.rewardGranted == true then
        return true
    end

    local lockKey = QPSC_localRewardLockKey(
        contract,
        participant
    )

    if QPSC_localRewardLocks[lockKey] == true then
        return false
    end

    QPSC_localRewardLocks[lockKey] = true

    local rewards =
        QPSC_getContractRewardItems(contract)

    if #rewards == 0 then
        participant.rewardGranted = true
        participant.rewardGrantedCount = 0
        participant.rewardGrantedCounts = {}
        participant.rewardGrantedAt =
            QPSC_getWorldAgeHours()
        participant.rewardPending = false
        QPSC_localRewardLocks[lockKey] = nil
        return true
    end

    local counts =
        QPSC_normalizeParticipantRewardCounts(
            contract,
            participant
        )
    local rewardSquare =
        QPSC_getPlayerSquare(player) or square

    participant.rewardPending = true

    for rewardIndex, reward in ipairs(rewards) do
        local fullType = tostring(reward.fullType or "")
        local quantity =
            QPSC_normalizePositiveInteger(
                reward.quantity,
                100
            )
        local granted = math.min(
            quantity,
            QPSC_normalizePositiveInteger(
                counts[rewardIndex],
                quantity
            )
        )

        if rewardSquare ~= nil
            and rewardSquare.AddWorldInventoryItem ~= nil then
            for itemIndex = granted + 1, quantity do
                local offsetIndex =
                    (
                        ((rewardIndex - 1) * 7)
                        + (itemIndex - 1)
                    ) % 9
                local offset = offsetIndex * 0.045
                local ok = pcall(function()
                    rewardSquare:AddWorldInventoryItem(
                        fullType,
                        0.28 + offset,
                        0.28 + offset,
                        0
                    )
                end)

                if ok then
                    granted = granted + 1
                    counts[rewardIndex] = granted

                    if rewardIndex == 1 then
                        participant.rewardGrantedCount =
                            granted
                    end
                else
                    break
                end
            end
        end

        if granted < quantity then
            participant.rewardGrantedCounts = counts
            participant.rewardPending = true
            QPSC_localRewardLocks[lockKey] = nil
            return false
        end
    end

    participant.rewardGranted = true
    participant.rewardGrantedCounts = counts
    participant.rewardGrantedCount =
        tonumber(counts[1]) or 0
    participant.rewardGrantedAt =
        QPSC_getWorldAgeHours()
    participant.rewardPending = false

    if #rewards == 1 then
        local reward = rewards[1]
        QPSC_say(
            player,
            QPSC_I18N.getText(
                "UI_QPSC_MessageRewardSpawned",
                reward.quantity,
                tostring(
                    reward.displayName ~= ""
                    and reward.displayName
                    or reward.fullType
                )
            )
        )
    else
        QPSC_say(
            player,
            QPSC_I18N.getText(
                "UI_QPSC_MessageRewardsSpawned",
                #rewards
            )
        )
    end

    QPSC_localRewardLocks[lockKey] = nil
    return true
end


local function QPSC_localFirstFinisherBonusLockKey(
    contract,
    participant
)
    return "bonus|"
        .. tostring(contract and contract.id or "")
        .. "|"
        .. QPSC_normalizeUsername(
            participant and participant.username or ""
        )
end

local function QPSC_spawnLocalFirstFinisherBonus(
    contract,
    participant,
    player,
    square
)
    if participant.firstFinisherBonusGranted == true then
        return true
    end

    if not QPSC_sameUsername(
        contract.firstFinisherWinner,
        participant.username
    ) then
        participant.firstFinisherBonusPending = false
        return true
    end

    local lockKey = QPSC_localFirstFinisherBonusLockKey(
        contract,
        participant
    )

    if QPSC_localFirstFinisherBonusLocks[lockKey] == true then
        return false
    end

    QPSC_localFirstFinisherBonusLocks[lockKey] = true

    local fullType = tostring(
        contract.firstFinisherBonusItemFullType or ""
    )
    local quantity = QPSC_normalizePositiveInteger(
        contract.firstFinisherBonusQuantity,
        100
    )

    if fullType == "" or quantity < 1 then
        participant.firstFinisherBonusGranted = true
        participant.firstFinisherBonusGrantedCount = 0
        participant.firstFinisherBonusGrantedAt =
            QPSC_getWorldAgeHours()
        participant.firstFinisherBonusPending = false
        QPSC_localFirstFinisherBonusLocks[lockKey] = nil
        return true
    end

    local granted = math.min(
        quantity,
        QPSC_normalizePositiveInteger(
            participant.firstFinisherBonusGrantedCount,
            100
        )
    )
    local bonusSquare = QPSC_getPlayerSquare(player) or square

    participant.firstFinisherBonusPending = true

    if bonusSquare ~= nil
        and bonusSquare.AddWorldInventoryItem ~= nil then
        for index = granted + 1, quantity do
            local offset = ((index - 1) % 5) * 0.06
            local ok, item = pcall(function()
                return bonusSquare:AddWorldInventoryItem(
                    fullType,
                    0.56 + offset,
                    0.32 + offset,
                    0
                )
            end)

            if ok then
                granted = granted + 1
                participant.firstFinisherBonusGrantedCount =
                    granted
            else
                break
            end
        end
    end

    if granted >= quantity then
        participant.firstFinisherBonusGranted = true
        participant.firstFinisherBonusGrantedCount = quantity
        participant.firstFinisherBonusGrantedAt =
            QPSC_getWorldAgeHours()
        participant.firstFinisherBonusPending = false
        QPSC_say(
            player,
            QPSC_I18N.getText(
                "UI_QPSC_MessageFirstFinisherBonusSpawned",
                quantity,
                tostring(
                    contract.firstFinisherBonusItemDisplayName
                    or fullType
                )
            )
        )
        QPSC_localFirstFinisherBonusLocks[lockKey] = nil
        return true
    end

    participant.firstFinisherBonusPending = true
    QPSC_localFirstFinisherBonusLocks[lockKey] = nil
    return false
end


-- QPSC_SHARED_TEAM_COMPLETION_V090
local function QPSC_closeLocalSharedTeamContract(
    contract,
    participant,
    player,
    rewardSquare,
    completionSource
)
    if contract == nil
        or participant == nil
        or not QPSC_isSharedTeamCompletion(contract)
        or contract.sharedCompleted == true then
        return false
    end

    local target = math.max(
        1,
        tonumber(contract.objectiveTarget) or 1
    )

    if (tonumber(contract.sharedProgress) or 0) < target then
        return false
    end

    local now = QPSC_getWorldAgeHours()
    contract.sharedProgress = target
    contract.sharedCompleted = true
    contract.sharedCompletedBy = tostring(
        participant.username or ""
    )
    contract.sharedCompletedAt = now
    contract.sharedCompletionSource = tostring(
        completionSource or "Zombie Hunt"
    )
    contract.closed = true
    contract.legacyClosedStatus = "Completed"

    participant.status = "Completed"
    participant.completedAt = now
    participant.reviewedAt = now
    participant.reviewedBy = tostring(
        completionSource or "Zombie Hunt"
    )
    participant.firstFinisherBonusPending = false

    QPSC_awardLocalContractReputation(
        contract,
        participant,
        completionSource
    )

    QPSC_Client.timerMarks[
        QPSC_timerKey(contract.id, participant.username)
    ] = nil

    QPSC_spawnLocalReward(
        contract,
        participant,
        player,
        QPSC_getPlayerSquare(player) or rewardSquare
    )

    QPSC_recomputeContractStatus(contract)
    QPSC_saveLocalContracts()
    QPSC_say(
        player,
        QPSC_I18N.getText(
            "UI_QPSC_MessageSharedTeamCompleted",
            contract.id,
            participant.progress
        )
    )

    return true
end

-- QPSC_GLOBAL_CONTRACT_COMPLETION_V080
local function QPSC_closeLocalGlobalContract(
    contract,
    winnerParticipant,
    completionSource
)
    if contract == nil
        or winnerParticipant == nil
        or not QPSC_isGlobalCompletion(contract) then
        return false
    end

    if contract.globalCompleted == true then
        return false
    end

    local now = QPSC_getWorldAgeHours()
    local winner = tostring(winnerParticipant.username or "")

    contract.globalCompleted = true
    contract.globalCompletedBy = winner
    contract.globalCompletedAt = now
    contract.globalCompletionSource = tostring(
        completionSource or "Automatic"
    )
    contract.closed = true
    contract.legacyClosedStatus = "Completed"

    for _, participant in ipairs(contract.participants or {}) do
        if not QPSC_sameUsername(participant.username, winner)
            and tostring(participant.status or "") == "Accepted" then
            participant.status = "ClosedByOther"
            participant.closedAt = now
            participant.closedBy = winner
            participant.rewardPending = false
            participant.firstFinisherBonusPending = false
            QPSC_Client.timerMarks[
                QPSC_timerKey(contract.id, participant.username)
            ] = nil
        end
    end

    return true
end

-- QPSC_V053_LOCAL_COMPLETION_LOCK_V1
local QPSC_localCompletionLocks = {}

local function QPSC_localCompletionLockKey(
    contract,
    participant
)
    return tostring(contract and contract.id or "")
        .. "|"
        .. QPSC_normalizeUsername(
            participant and participant.username or ""
        )
end

local function QPSC_completeLocalTracked(
    contract,
    participant,
    player,
    rewardSquare,
    completionSource
)
    if contract == nil or participant == nil then
        return false
    end

    if tostring(participant.status or "") ~= "Accepted" then
        return false
    end

    if QPSC_isGlobalCompletion(contract)
        and contract.globalCompleted == true then
        return false
    end

    local lockKey = QPSC_isGlobalCompletion(contract)
        and ("GLOBAL|" .. tostring(contract.id or ""))
        or QPSC_localCompletionLockKey(contract, participant)

    if QPSC_localCompletionLocks[lockKey] == true then
        return false
    end

    QPSC_localCompletionLocks[lockKey] = true

    local now = QPSC_getWorldAgeHours()
    participant.status = "Completed"
    participant.completedAt = now
    participant.reviewedAt = now
    participant.reviewedBy =
        tostring(completionSource or "Automatic")
    participant.progress = math.max(
        tonumber(participant.progress) or 0,
        tonumber(contract.objectiveTarget) or 0
    )

    QPSC_awardLocalContractReputation(
        contract,
        participant,
        completionSource
    )

    local hasFirstFinisherBonus =
        tostring(
            contract.firstFinisherBonusItemFullType or ""
        ) ~= ""
        and QPSC_normalizePositiveInteger(
            contract.firstFinisherBonusQuantity,
            100
        ) > 0
    local isFirstFinisher = false

    if hasFirstFinisherBonus then
        if tostring(contract.firstFinisherWinner or "") == "" then
            contract.firstFinisherWinner =
                tostring(participant.username or "")
            contract.firstFinisherWonAt = now
            isFirstFinisher = true
        elseif QPSC_sameUsername(
            contract.firstFinisherWinner,
            participant.username
        ) then
            isFirstFinisher = true
        end
    end

    QPSC_Client.timerMarks[
        QPSC_timerKey(
            contract.id,
            participant.username
        )
    ] = nil

    QPSC_closeLocalGlobalContract(
        contract,
        participant,
        completionSource
    )

    local completionSquare =
        QPSC_getPlayerSquare(player) or rewardSquare

    QPSC_spawnLocalReward(
        contract,
        participant,
        player,
        completionSquare
    )

    if isFirstFinisher then
        QPSC_spawnLocalFirstFinisherBonus(
            contract,
            participant,
            player,
            completionSquare
        )
    end

    QPSC_recomputeContractStatus(contract)
    QPSC_saveLocalContracts()
    QPSC_say(
        player,
        QPSC_I18N.getText(
            "UI_QPSC_MessageTrackedCompleted",
            contract.id
        )
    )

    QPSC_localCompletionLocks[lockKey] = nil
    return true
end



-- QPSC_LOCAL_MULTI_OBJECTIVE_RUNTIME_V100
local function QPSC_closeLocalSharedMultiContract(contract, participant, player, rewardSquare, source)
    if not QPSC_isMultiObjective(contract) or participant == nil
        or not QPSC_isSharedTeamCompletion(contract)
        or contract.sharedCompleted == true
        or not QPSC_allMultiObjectivesComplete(contract, participant) then return false end
    local now=QPSC_getWorldAgeHours(); contract.sharedCompleted=true; contract.sharedCompletedBy=tostring(participant.username or ""); contract.sharedCompletedAt=now; contract.sharedCompletionSource=tostring(source or "Multi-Objective"); contract.sharedProgress=#contract.objectives; contract.closed=true; contract.legacyClosedStatus="Completed"
    participant.status="Completed"; participant.completedAt=now; participant.reviewedAt=now; participant.reviewedBy=tostring(source or "Multi-Objective"); participant.progress=#contract.objectives; participant.firstFinisherBonusPending=false
    QPSC_awardLocalContractReputation(contract,participant,source)
    QPSC_Client.timerMarks[QPSC_timerKey(contract.id,participant.username)]=nil
    QPSC_spawnLocalReward(contract,participant,player,QPSC_getPlayerSquare(player) or rewardSquare)
    QPSC_recomputeContractStatus(contract); QPSC_saveLocalContracts(); QPSC_say(player,QPSC_I18N.getText("UI_QPSC_MessageSharedMultiCompleted",contract.id,QPSC_getMultiContributionTotal(participant))); return true
end

local function QPSC_advanceLocalMultiObjective(contract,participant,objective,amount,player,rewardSquare,source)
    if not QPSC_isMultiObjective(contract) or participant==nil or objective==nil or tostring(participant.status or "")~="Accepted" then return false end
    local target=math.max(1,tonumber(objective.target) or 1); local before=QPSC_getMultiProgress(contract,participant,objective); if before>=target then return false end
    local after=QPSC_setMultiProgress(contract,participant,objective,before+math.max(0,math.floor(tonumber(amount) or 0))); local credited=math.max(0,after-before); if credited>0 then QPSC_addMultiContribution(participant,objective,credited) end
    local completed,total=QPSC_multiObjectiveCounts(contract,participant); participant.progress=completed; if QPSC_isSharedTeamCompletion(contract) then contract.sharedProgress=completed end
    if completed>=total and total>0 then
        if QPSC_isSharedTeamCompletion(contract) then return QPSC_closeLocalSharedMultiContract(contract,participant,player,rewardSquare,source) end
        return QPSC_completeLocalTracked(contract,participant,player,rewardSquare,source or "Multi-Objective")
    end
    QPSC_saveLocalContracts(); QPSC_say(player,QPSC_I18N.getText("UI_QPSC_MessageMultiProgress",after,target,completed)); return true
end

local function QPSC_findLocalMultiDelivery(contract,participant,player)
    for _,objective in ipairs(contract and contract.objectives or {}) do
        if tostring(objective.type or "")=="DELIVERY" and QPSC_getMultiProgress(contract,participant,objective)<math.max(1,tonumber(objective.target) or 1) and QPSC_isPlayerNearMultiObjective(player,objective) then return objective end
    end
    return nil
end

local function QPSC_isPlayerNearTarget(player, contract)
    if player == nil or contract == nil then return false end

    local dx = (tonumber(player:getX()) or 0)
        - (tonumber(contract.targetX) or 0)
    local dy = (tonumber(player:getY()) or 0)
        - (tonumber(contract.targetY) or 0)
    local dz = math.abs(
        (tonumber(player:getZ()) or 0)
        - (tonumber(contract.targetZ) or 0)
    )
    local radius = math.max(
        1,
        tonumber(contract.objectiveRadius) or 3
    )

    return dz < 0.5 and (dx * dx + dy * dy) <= radius * radius
end

-- QPSC_B41_INVENTORY_FULLTYPE_COMPAT_V1
local function QPSC_getInventoryItemsByFullType(
    inventory,
    fullType
)
    local result = {}
    local seen = {}
    local visited = {}
    local target = tostring(fullType or "")
    local normalizedTarget = string.lower(target)
    local shortTarget = normalizedTarget:match("([^%.]+)$")
        or normalizedTarget

    if inventory == nil or normalizedTarget == "" then
        return result
    end

    local function appendItem(item)
        if item == nil or seen[item] then return end
        seen[item] = true
        table.insert(result, item)
    end

    local function appendJavaList(list)
        if list == nil or list.size == nil or list.get == nil then
            return
        end

        local okSize, size = pcall(function()
            return list:size()
        end)

        if not okSize or size == nil then return end

        for index = 0, size - 1 do
            local okItem, item = pcall(function()
                return list:get(index)
            end)

            if okItem and item ~= nil then
                appendItem(item)
            end
        end
    end

    -- Prefer the engine's native full-type query on Build 41.
    -- The boolean overload includes nested inventories when available.
    if inventory.getItemsFromFullType ~= nil then
        local okNested, nestedItems = pcall(function()
            return inventory:getItemsFromFullType(target, true)
        end)

        if okNested then appendJavaList(nestedItems) end

        local okDirect, directItems = pcall(function()
            return inventory:getItemsFromFullType(target)
        end)

        if okDirect then appendJavaList(directItems) end
    end

    local function itemMatches(item)
        local candidates = {}
        local itemType = ""
        local itemModule = ""

        if item.getFullType ~= nil then
            local ok, value = pcall(function()
                return item:getFullType()
            end)
            if ok and value ~= nil then
                table.insert(candidates, tostring(value))
            end
        end

        if item.getType ~= nil then
            local ok, value = pcall(function()
                return item:getType()
            end)
            if ok and value ~= nil then
                itemType = tostring(value)
                table.insert(candidates, itemType)
            end
        end

        if item.getModule ~= nil then
            local ok, value = pcall(function()
                return item:getModule()
            end)
            if ok and value ~= nil then
                itemModule = tostring(value)
            end
        end

        if itemModule ~= "" and itemType ~= "" then
            table.insert(candidates, itemModule .. "." .. itemType)
        end

        if item.getScriptItem ~= nil then
            local okScript, scriptItem = pcall(function()
                return item:getScriptItem()
            end)

            if okScript and scriptItem ~= nil
                and scriptItem.getFullName ~= nil then
                local okName, value = pcall(function()
                    return scriptItem:getFullName()
                end)

                if okName and value ~= nil then
                    table.insert(candidates, tostring(value))
                end
            end
        end

        for _, candidate in ipairs(candidates) do
            local normalized = string.lower(
                tostring(candidate or "")
            )

            if normalized == normalizedTarget
                or normalized == shortTarget
                or normalized:match("([^%.]+)$") == shortTarget then
                return true
            end
        end

        return false
    end

    local function visit(container, depth)
        if container == nil
            or visited[container]
            or depth > 10
            or container.getItems == nil then
            return
        end

        visited[container] = true

        local ok, items = pcall(function()
            return container:getItems()
        end)

        if not ok or items == nil then return end

        for index = 0, items:size() - 1 do
            local item = items:get(index)

            if item ~= nil then
                if itemMatches(item) then
                    appendItem(item)
                end

                local nested = nil

                if item.getInventory ~= nil then
                    local okNested, value = pcall(function()
                        return item:getInventory()
                    end)
                    if okNested then nested = value end
                elseif item.getItemContainer ~= nil then
                    local okNested, value = pcall(function()
                        return item:getItemContainer()
                    end)
                    if okNested then nested = value end
                end

                if nested ~= nil then
                    visit(nested, depth + 1)
                end
            end
        end
    end

    visit(inventory, 0)
    return result
end

-- QPSC_DELIVERY_TWO_PHASE_SYNC_V2
local function QPSC_consumeAuthorizedDelivery(args)
    args = args or {}

    local token = tostring(args.token or "")
    local contractId = tostring(args.contractId or "")
    local fullType = tostring(args.fullType or "")
    local required = math.max(
        0,
        math.floor(tonumber(args.count) or 0)
    )
    local player = getPlayer()

    if token == "" or contractId == ""
        or fullType == "" or required <= 0
        or player == nil then
        return
    end

    local inventory = player:getInventory()
    local items = QPSC_getInventoryItemsByFullType(
        inventory,
        fullType
    )

    if #items < required then
        QPSC_sendCommand("DeliveryConsumptionAck",
            {
                token = token,
                contractId = contractId,
                success = false,
                removed = 0,
                available = #items
            }
        )
        return
    end

    local toRemove = {}

    for index = 1, required do
        table.insert(toRemove, items[index])
    end

    local removed = 0
    local touched = {}

    for _, item in ipairs(toRemove) do
        local container = inventory

        if item ~= nil and item.getContainer ~= nil then
            local okContainer, value = pcall(function()
                return item:getContainer()
            end)

            if okContainer and value ~= nil then
                container = value
            end
        end

        if container ~= nil and item ~= nil then
            local containedBefore = true

            if container.contains ~= nil then
                local okContains, value = pcall(function()
                    return container:contains(item)
                end)

                if okContains then
                    containedBefore = value == true
                end
            end

            if containedBefore then
                local okRemove = pcall(function()
                    container:Remove(item)
                end)

                if okRemove then
                    local containedAfter = false

                    if container.contains ~= nil then
                        local okContains, value = pcall(function()
                            return container:contains(item)
                        end)

                        if okContains then
                            containedAfter = value == true
                        end
                    end

                    if not containedAfter then
                        removed = removed + 1
                        touched[container] = true
                    end
                end
            end
        end
    end

    for container, _ in pairs(touched) do
        if container.setDirty ~= nil then
            pcall(function()
                container:setDirty(true)
            end)
        end

        if container.setDrawDirty ~= nil then
            pcall(function()
                container:setDrawDirty(true)
            end)
        end
    end

    QPSC_sendCommand("DeliveryConsumptionAck",
        {
            token = token,
            contractId = contractId,
            success = removed == required,
            removed = removed,
            available = #items
        }
    )
end

local function QPSC_localSubmitDelivery(player, contractId)
    QPSC_loadLocalContracts()

    local contract, participant =
        QPSC_findActiveContractForPlayer(player)

    if contract ~= nil and participant ~= nil
        and tostring(contract.id) == tostring(contractId)
        and QPSC_isMultiObjective(contract) then
        local objective=QPSC_findLocalMultiDelivery(contract,participant,player)
        if objective==nil then QPSC_say(player,QPSC_I18N.getText("UI_QPSC_MessageNoMultiDeliveryHere")); return end
        local current = QPSC_getMultiProgress(contract, participant, objective)
        local remaining = math.max(0, (tonumber(objective.target) or 1) - current)
        local inventory = player:getInventory()
        local items = QPSC_getInventoryItemsByFullType(
            inventory,
            tostring(objective.itemFullType or "")
        )
        local available = #items
        if available <= 0 then
            QPSC_say(
                player,
                QPSC_I18N.getText(
                    "UI_QPSC_MessageDeliveryMissing",
                    remaining,
                    tostring(objective.itemDisplayName or objective.itemFullType or ""),
                    0
                )
            )
            return
        end
        local submitCount = math.min(remaining, available)
        for index = 1, submitCount do
            local item = items[index]
            local container = inventory
            if item and item.getContainer then
                local ok, value = pcall(function() return item:getContainer() end)
                if ok and value then container = value end
            end
            pcall(function() container:Remove(item) end)
        end
        QPSC_advanceLocalMultiObjective(
            contract,
            participant,
            objective,
            submitCount,
            player,
            QPSC_getSquareAt(objective.targetX, objective.targetY, objective.targetZ)
                or QPSC_getPlayerSquare(player),
            "Multi-Objective Delivery"
        )
        return
    end

    if contract == nil or participant == nil
        or tostring(contract.id) ~= tostring(contractId)
        or QPSC_normalizeObjectiveType(
            contract.objectiveType
        ) ~= "DELIVERY" then
        QPSC_say(
            player,
            QPSC_I18N.getText(
                "UI_QPSC_MessageNoActiveDelivery"
            )
        )
        return
    end

    if not QPSC_isPlayerNearTarget(player, contract) then
        QPSC_say(
            player,
            QPSC_I18N.getText(
                "UI_QPSC_MessageGoToDeliveryLocation"
            )
        )
        return
    end

    local target = math.max(
        1,
        tonumber(contract.objectiveTarget) or 1
    )
    local inventory = player:getInventory()
    local items = QPSC_getInventoryItemsByFullType(
        inventory,
        tostring(contract.objectiveItemFullType or "")
    )
    local available = #items
    participant.progress = math.min(target, available)

    if available < target then
        QPSC_saveLocalContracts()
        QPSC_say(
            player,
            QPSC_I18N.getText(
                "UI_QPSC_MessageDeliveryMissing",
                target,
                tostring(
                    contract.objectiveItemDisplayName
                    or contract.objectiveItemFullType
                    or ""
                ),
                available
            )
        )
        return
    end

    local toRemove = {}
    for index = 1, target do
        table.insert(toRemove, items[index])
    end

    for _, item in ipairs(toRemove) do
        local container = inventory

        if item ~= nil and item.getContainer ~= nil then
            local ok, value = pcall(function()
                return item:getContainer()
            end)
            if ok and value ~= nil then container = value end
        end

        pcall(function()
            container:Remove(item)
        end)
    end

    participant.progress = target
    QPSC_completeLocalTracked(
        contract,
        participant,
        player,
        QPSC_getSquareAt(
            contract.targetX,
            contract.targetY,
            contract.targetZ
        ) or QPSC_getPlayerSquare(player),
        "Delivery"
    )
end


local function QPSC_multiObjectiveLabel(objective)
    if objective == nil then return "" end
    local objectiveType = tostring(objective.type or "")
    local target = math.max(1, tonumber(objective.target) or 1)
    if objectiveType == "DELIVERY" then
        return QPSC_I18N.getText(
            "UI_QPSC_MultiDeliveryLabel",
            target,
            tostring(objective.itemDisplayName or objective.itemFullType or "")
        )
    elseif objectiveType == "KILL" then
        return QPSC_I18N.getText("UI_QPSC_MultiKillLabel", target)
    end
    return QPSC_I18N.getText("UI_QPSC_MultiLocationLabel")
end

local function QPSC_multiObjectiveSummary(contract, player)
    local participant = nil
    if player ~= nil then
        participant = QPSC_findParticipant(contract, QPSC_getPlayerUsername(player))
    end
    local completed, total = QPSC_multiObjectiveCounts(contract, participant)
    local nextLabel = ""
    for _, objective in ipairs(contract.objectives or {}) do
        if QPSC_getMultiProgress(contract, participant, objective)
            < math.max(1, tonumber(objective.target) or 1) then
            nextLabel = QPSC_multiObjectiveLabel(objective)
            break
        end
    end
    if nextLabel == "" then nextLabel = QPSC_I18N.getText("UI_QPSC_Completed") end
    return QPSC_I18N.getText("UI_QPSC_MultiSummary", completed, total, nextLabel)
end

local function QPSC_objectiveSummary(contract, player)
    if QPSC_isMultiObjective(contract) then
        return QPSC_multiObjectiveSummary(contract, player)
    end

    local objectiveType =
        QPSC_normalizeObjectiveType(
            contract and contract.objectiveType
        )

    if objectiveType == "MANUAL" then
        return QPSC_I18N.getText(
            "UI_QPSC_ObjectiveManualSummary"
        )
    end

    local participant = nil
    if player ~= nil then
        participant = QPSC_findParticipant(
            contract,
            QPSC_getPlayerUsername(player)
        )
    end

    local progress = participant
        and tonumber(participant.progress) or 0
    local target = math.max(
        1,
        tonumber(contract.objectiveTarget) or 1
    )

    if objectiveType == "DELIVERY" then
        return QPSC_I18N.getText(
            "UI_QPSC_ObjectiveDeliverySummary",
            target,
            tostring(
                contract.objectiveItemDisplayName
                or contract.objectiveItemFullType
                or ""
            ),
            progress,
            target
        )
    elseif objectiveType == "KILL" then
        local radius = tonumber(
            contract.objectiveRadius
        ) or 0

        if QPSC_isSharedTeamCompletion(contract) then
            local sharedProgress = tonumber(
                contract.sharedProgress
            ) or 0
            local personalContribution = participant
                and (tonumber(participant.progress) or 0)
                or 0

            if radius > 0 then
                return QPSC_I18N.getText(
                    "UI_QPSC_ObjectiveSharedKillAreaSummary",
                    radius,
                    sharedProgress,
                    target,
                    personalContribution
                )
            end

            return QPSC_I18N.getText(
                "UI_QPSC_ObjectiveSharedKillSummary",
                sharedProgress,
                target,
                personalContribution
            )
        end

        if radius > 0 then
            return QPSC_I18N.getText(
                "UI_QPSC_ObjectiveKillAreaSummary",
                radius,
                progress,
                target
            )
        end

        return QPSC_I18N.getText(
            "UI_QPSC_ObjectiveKillSummary",
            progress,
            target
        )
    end

    return QPSC_I18N.getText(
        "UI_QPSC_ObjectiveLocationSummary",
        tonumber(contract.objectiveRadius) or 3
    )
end


local function QPSC_firstFinisherSummary(contract)
    if QPSC_isSharedTeamCompletion(contract) then
        return QPSC_I18N.getText("UI_QPSC_None")
    end

    local fullType = tostring(
        contract and contract.firstFinisherBonusItemFullType
        or ""
    )
    local quantity = tonumber(
        contract and contract.firstFinisherBonusQuantity
    ) or 0

    if fullType == "" or quantity < 1 then
        return QPSC_I18N.getText("UI_QPSC_None")
    end

    local itemName = tostring(
        contract.firstFinisherBonusItemDisplayName
        or fullType
    )
    local winner = tostring(
        contract.firstFinisherWinner or ""
    )

    if winner ~= "" then
        return QPSC_I18N.getText(
            "UI_QPSC_FirstFinisherBonusWon",
            quantity,
            itemName,
            winner
        )
    end

    return QPSC_I18N.getText(
        "UI_QPSC_FirstFinisherBonusAvailable",
        quantity,
        itemName
    )
end

local function QPSC_contractLabel(contract)
    if not contract then
        return QPSC_I18N.getText("UI_QPSC_Unknown")
    end

    return "#"
        .. tostring(contract.id or "?")
        .. " - "
        .. QPSC_contractTitleText(contract)
end

local function QPSC_canPlayerAcceptContract(
    player,
    contract
)
    if contract == nil then
        return false,
            "UI_QPSC_MessageSelectContractFirst"
    end

    if contract.closed == true then
        return false,
            "UI_QPSC_MessageNotOpen"
    end

    local activeContract =
        QPSC_findActiveContractForPlayer(player)

    if activeContract ~= nil then
        return false,
            "UI_QPSC_MessageActiveContractExists",
            QPSC_contractLabel(activeContract)
    end

    local username =
        QPSC_getPlayerUsername(player)
    local participant =
        QPSC_findParticipant(contract, username)

    if participant ~= nil
        and tostring(participant.status or "")
            ~= "Cancelled" then
        return false,
            "UI_QPSC_MessageAlreadyJoined"
    end

    return true, nil, nil
end

local function QPSC_splitContractText(text)
    local source = tostring(text or "")
    local parts = {}
    local startIndex = 1

    while true do
        local separator =
            string.find(source, "|", startIndex, true)

        if separator == nil then
            table.insert(
                parts,
                QPSC_trim(
                    string.sub(source, startIndex)
                )
            )
            break
        end

        table.insert(
            parts,
            QPSC_trim(
                string.sub(
                    source,
                    startIndex,
                    separator - 1
                )
            )
        )

        startIndex = separator + 1
    end

    return {
        title = parts[1] or "",
        location = parts[2] or "",
        reward = parts[3] or "",
        description = parts[4] or "",
        timeLimitText = parts[5] or "",
        timeLimitHours =
            QPSC_normalizeTimeLimit(parts[5]),
        difficulty = QPSC_normalizeDifficulty(parts[6])
    }
end

-- QPSC_WINDOW_LOCALIZATION_V2
local function QPSC_participantStatusText(participant, contract)
    local status = tostring(
        participant and participant.status or "Unknown"
    )

    if status == "Accepted" then
        return QPSC_I18N.getText(
            "UI_QPSC_StatusAccepted"
        )
    end

    if status == "Completed" then
        return QPSC_I18N.getText(
            "UI_QPSC_StatusCompleted"
        )
    end

    if status == "NotCompleted" then
        return QPSC_I18N.getText(
            "UI_QPSC_StatusNotCompleted"
        )
    end

    if status == "Expired" then
        return QPSC_I18N.getText(
            "UI_QPSC_StatusExpired"
        )
    end

    if status == "Cancelled" then
        return QPSC_I18N.getText(
            "UI_QPSC_StatusCancelled"
        )
    end

    if status == "ClosedByOther" then
        return QPSC_I18N.getText(
            "UI_QPSC_StatusClosedByOther",
            tostring(
                participant.closedBy
                or (contract and contract.globalCompletedBy)
                or ""
            )
        )
    end

    return QPSC_I18N.getText(
        "UI_QPSC_StatusUnknown"
    )
end

local function QPSC_contractCounts(contract)
    local counts = {
        total = 0,
        active = 0,
        completed = 0,
        notCompleted = 0,
        expired = 0,
        cancelled = 0,
        closedByOther = 0
    }

    for _, participant in ipairs(
        contract.participants or {}
    ) do
        counts.total = counts.total + 1

        if participant.status == "Accepted" then
            counts.active = counts.active + 1
        elseif participant.status == "Completed" then
            counts.completed = counts.completed + 1
        elseif participant.status == "NotCompleted" then
            counts.notCompleted =
                counts.notCompleted + 1
        elseif participant.status == "Expired" then
            counts.expired = counts.expired + 1
        elseif participant.status == "Cancelled" then
            counts.cancelled = counts.cancelled + 1
        elseif participant.status == "ClosedByOther" then
            counts.closedByOther = counts.closedByOther + 1
        end
    end

    return counts
end

local function QPSC_deadlineSuffix(contract, participant)
    local timeLimitHours =
        QPSC_normalizeTimeLimit(contract.timeLimitHours)

    if participant == nil then
        if timeLimitHours > 0
            and contract.closed ~= true then
            return QPSC_I18N.getText(
                "UI_QPSC_TimeLimit"
            ) .. ": " .. QPSC_formatDuration(timeLimitHours)
        end

        return ""
    end

    if participant.status == "Expired" then
        return QPSC_I18N.getText(
            "UI_QPSC_StatusExpired"
        )
    end

    if participant.status ~= "Accepted"
        or timeLimitHours <= 0 then
        return ""
    end

    return QPSC_I18N.getText(
        "UI_QPSC_TimeRemaining"
    ) .. ": " .. QPSC_formatDuration(
        participant.remainingHours
    )
end

local function QPSC_statusText(contract)
    local player = getPlayer()
    local username = QPSC_getPlayerUsername(player)
    local participant =
        QPSC_findParticipant(contract, username)

    if participant then
        local text =
            QPSC_I18N.getText(
                "UI_QPSC_YourStatus"
            ) .. ": "
            .. QPSC_participantStatusText(participant, contract)
        local deadline =
            QPSC_deadlineSuffix(contract, participant)

        if deadline ~= "" then
            text = text .. " | " .. deadline
        end

        return text
    end

    if contract.closed == true then
        local status =
            tostring(contract.status or "Closed")

        if status == "Completed" then
            if QPSC_isSharedTeamCompletion(contract) then
                return QPSC_I18N.getText(
                    "UI_QPSC_StatusCompletedBySharedTeam"
                )
            end

            local completedBy = tostring(
                contract.globalCompletedBy or ""
            )

            if completedBy == "" then
                completedBy = tostring(contract.completedBy or "")
            end

            if completedBy ~= "" then
                return QPSC_I18N.getText(
                    "UI_QPSC_StatusCompletedBy",
                    completedBy
                )
            end

            return QPSC_I18N.getText(
                "UI_QPSC_StatusCompleted"
            )
        end

        if status == "Expired" then
            return QPSC_I18N.getText(
                "UI_QPSC_StatusExpired"
            )
        end

        return QPSC_I18N.getText(
            "UI_QPSC_StatusClosed"
        )
    end

    local counts = QPSC_contractCounts(contract)
    local baseText

    if counts.active > 0 then
        baseText = QPSC_I18N.getText(
            "UI_QPSC_StatusActive"
        ) .. ": " .. tostring(counts.active)
    else
        baseText = QPSC_I18N.getText(
            "UI_QPSC_StatusOpen"
        )
    end

    local deadline = QPSC_deadlineSuffix(
        contract,
        nil
    )

    if deadline ~= "" then
        baseText = baseText .. " | " .. deadline
    end

    return baseText
end

-- QPSC_PARTICIPANT_SUMMARY_TWO_LINES_V0310
local function QPSC_buildParticipantSummaryLines(contract)
    local counts = QPSC_contractCounts(contract)

    if counts.total == 0 then
        return QPSC_I18N.getText(
            "UI_QPSC_NoParticipants"
        ), nil
    end

    local fullSummary = QPSC_I18N.getText(
        "UI_QPSC_ParticipantCounts",
        counts.total,
        counts.active,
        counts.completed,
        counts.notCompleted,
        counts.expired,
        counts.cancelled,
        counts.closedByOther
    )
    local parts = {}

    for part in string.gmatch(
        tostring(fullSummary or ""),
        "([^|]+)"
    ) do
        table.insert(parts, QPSC_trim(part))
    end

    if #parts == 7 then
        local firstLine = table.concat(
            {parts[1], parts[2], parts[3]},
            " | "
        )

        if QPSC_isSharedTeamCompletion(contract) then
            local contributions = {}

            for _, participant in ipairs(
                contract.participants or {}
            ) do
                local progress = QPSC_isMultiObjective(contract)
                    and QPSC_getMultiContributionTotal(participant)
                    or (tonumber(participant.progress) or 0)

                if progress > 0 then
                    table.insert(contributions, {
                        username = tostring(participant.username or ""),
                        progress = progress
                    })
                end
            end

            table.sort(contributions, function(left, right)
                if left.progress == right.progress then
                    return string.lower(left.username)
                        < string.lower(right.username)
                end

                return left.progress > right.progress
            end)

            local visible = {}
            local maximumVisible = 4

            for index = 1, math.min(
                #contributions,
                maximumVisible
            ) do
                local contribution = contributions[index]
                table.insert(
                    visible,
                    contribution.username
                        .. ": "
                        .. tostring(contribution.progress)
                )
            end

            if #contributions > maximumVisible then
                table.insert(
                    visible,
                    "+" .. tostring(
                        #contributions - maximumVisible
                    )
                )
            end

            local contributionText = #visible > 0
                and table.concat(visible, ", ")
                or QPSC_I18N.getText("UI_QPSC_None")

            return firstLine, QPSC_I18N.getText(
                "UI_QPSC_SharedContributions",
                contributionText
            )
        end

        return firstLine, table.concat(
            {parts[4], parts[5], parts[6], parts[7]},
            " | "
        )
    end

    return tostring(fullSummary or ""), nil
end

local function QPSC_countContracts()
    local count = 0
    for _, _ in pairs(QPSC_Client.contracts or {}) do count = count + 1 end
    return count
end

local function QPSC_addLocalContract(player, contractArgs)
    local data = QPSC_getLocalData()
    QPSC_migrateLocalData(data)

    contractArgs = contractArgs or {}
    local requestedObjectiveType =
        QPSC_normalizeObjectiveType(contractArgs.objectiveType)
    local requestedCompletionMode =
        tostring(contractArgs.completionMode or "") ~= ""
        and QPSC_normalizeCompletionMode(contractArgs.completionMode)
        or (
            (requestedObjectiveType == "KILL"
                or requestedObjectiveType == "LOCATION")
            and "GLOBAL"
            or "INDIVIDUAL"
        )

    if requestedCompletionMode == "SHARED_TEAM"
        and requestedObjectiveType ~= "KILL" then
        QPSC_say(
            player,
            QPSC_I18N.getText(
                "UI_QPSC_MessageSharedTeamKillOnly"
            )
        )
        return false
    end

    local id = data.nextId or 1
    local username = QPSC_getPlayerUsername(player)

    local contract = {
        id = id,
        category =
            QPSC_normalizeCategory(contractArgs.category),
        difficulty =
            QPSC_normalizeDifficulty(contractArgs.difficulty),
        completionMode = requestedCompletionMode,
        globalCompleted = false,
        globalCompletedBy = "",
        globalCompletedAt = 0,
        globalCompletionSource = "",
        sharedProgress = QPSC_normalizeCompletionMode(contractArgs.completionMode) == "SHARED_TEAM" and 0 or nil,
        sharedCompleted = QPSC_normalizeCompletionMode(contractArgs.completionMode) == "SHARED_TEAM" and false or nil,
        sharedCompletedBy = QPSC_normalizeCompletionMode(contractArgs.completionMode) == "SHARED_TEAM" and "" or nil,
        sharedCompletedAt = QPSC_normalizeCompletionMode(contractArgs.completionMode) == "SHARED_TEAM" and 0 or nil,
        sharedCompletionSource = QPSC_normalizeCompletionMode(contractArgs.completionMode) == "SHARED_TEAM" and "" or nil,
        title =
            contractArgs.title or "Untitled Contract",
        description =
            contractArgs.description or "",
        location =
            contractArgs.location or "Unknown",
        reward =
            contractArgs.reward or "Manual reward",
        objectiveType = requestedObjectiveType,
        objectiveTarget =
            QPSC_normalizePositiveInteger(
                contractArgs.objectiveTarget,
                10000
            ),
        objectiveRadius =
            QPSC_normalizePositiveInteger(
                contractArgs.objectiveRadius,
                1000
            ),
        objectiveItemFullType =
            tostring(
                contractArgs.objectiveItemFullType or ""
            ),
        objectiveItemDisplayName =
            tostring(
                contractArgs.objectiveItemDisplayName or ""
            ),
        targetX = math.floor(
            tonumber(player:getX()) or 0
        ),
        targetY = math.floor(
            tonumber(player:getY()) or 0
        ),
        targetZ = math.floor(
            tonumber(player:getZ()) or 0
        ),
        rewardItemFullType =
            tostring(
                contractArgs.rewardItemFullType or ""
            ),
        rewardItemDisplayName =
            tostring(
                contractArgs.rewardItemDisplayName or ""
            ),
        rewardQuantity =
            QPSC_normalizePositiveInteger(
                contractArgs.rewardQuantity,
                100
            ),
        rewardItems =
            QPSC_rewardItemsFromArgs(contractArgs),
        reputationPath =
            QPSC_normalizeReputationPath(
                contractArgs.reputationPath
            ),
        reputationPoints =
            QPSC_normalizePositiveInteger(
                contractArgs.reputationPoints,
                100000
            ),
        secondaryReputationPath =
            QPSC_normalizeReputationPath(
                contractArgs.secondaryReputationPath
            ),
        secondaryReputationPoints =
            QPSC_normalizePositiveInteger(
                contractArgs.secondaryReputationPoints,
                100000
            ),
        firstFinisherBonusItemFullType =
            tostring(
                contractArgs.firstFinisherBonusItemFullType
                or ""
            ),
        firstFinisherBonusItemDisplayName =
            tostring(
                contractArgs.firstFinisherBonusItemDisplayName
                or ""
            ),
        firstFinisherBonusQuantity =
            QPSC_normalizePositiveInteger(
                contractArgs.firstFinisherBonusQuantity,
                100
            ),
        firstFinisherWinner = "",
        firstFinisherWonAt = 0,
        status = "Open",
        postedBy = username,
        acceptedBy = "",
        completedBy = "",
        timeLimitHours =
            QPSC_normalizeTimeLimit(
                contractArgs.timeLimitHours
            ),
        acceptedAt = 0,
        expiresAt = 0,
        expiredAt = 0,
        closed = false,
        participants = {}
    }

    QPSC_applyContractRewardItems(
        contract,
        contract.rewardItems
    )

    local localRewardTexts =
        QPSC_rewardItemTexts(contract)

    if #localRewardTexts > 0 then
        contract.reward =
            table.concat(localRewardTexts, " + ")
    end

    if contract.reputationPath == ""
        or contract.reputationPoints < 1 then
        contract.reputationPath = ""
        contract.reputationPoints = 0
    end

    if contract.secondaryReputationPath == ""
        or contract.secondaryReputationPoints < 1
        or contract.secondaryReputationPath
            == contract.reputationPath then
        contract.secondaryReputationPath = ""
        contract.secondaryReputationPoints = 0
    end

    if QPSC_isSharedTeamCompletion(contract) then
        contract.firstFinisherBonusItemFullType = ""
        contract.firstFinisherBonusItemDisplayName = ""
        contract.firstFinisherBonusQuantity = 0
        contract.firstFinisherWinner = ""
        contract.firstFinisherWonAt = 0
    end

    table.insert(data.contracts, contract)
    data.nextId = id + 1

    QPSC_Client.contracts = data.contracts
    QPSC_saveLocalContracts()

    QPSC_announce(
        player,
        QPSC_I18N.getText(
            "UI_QPSC_AnnouncementNewContract",
            id,
            tostring(contract.title or "")
        )
    )
end



-- QPSC_LOCAL_MULTI_OBJECTIVE_V100
local function QPSC_flatObjectivesFromArgs(args, player)
    local objectives = {}
    local count = math.min(QPSC_MAX_OBJECTIVES, QPSC_normalizePositiveInteger(args.objectiveCount, QPSC_MAX_OBJECTIVES))
    for index = 1, count do
        local prefix = "objective" .. tostring(index)
        table.insert(objectives, QPSC_normalizeMultiObjective({
            id=args[prefix.."Id"], type=args[prefix.."Type"], target=args[prefix.."Target"], radius=args[prefix.."Radius"],
            itemFullType=args[prefix.."ItemFullType"], itemDisplayName=args[prefix.."ItemDisplayName"],
            targetX=args[prefix.."TargetX"], targetY=args[prefix.."TargetY"], targetZ=args[prefix.."TargetZ"]
        }, index, player:getX(), player:getY(), player:getZ()))
    end
    return objectives
end

local function QPSC_applyLocalMultiRewardText(contract)
    local rewards = QPSC_rewardItemTexts(contract)

    local function addReputationReward(pathValue, pointsValue)
        local path = QPSC_normalizeReputationPath(pathValue)
        local points = QPSC_normalizePositiveInteger(
            pointsValue,
            100000
        )

        if path ~= "" and points > 0 then
            table.insert(
                rewards,
                "+"
                    .. tostring(points)
                    .. " "
                    .. tostring(
                        QPSC_REPUTATION_LABELS[path]
                        or path
                    )
                    .. " Reputation"
            )
        end
    end

    addReputationReward(
        contract.reputationPath,
        contract.reputationPoints
    )

    if QPSC_normalizeReputationPath(
        contract.secondaryReputationPath
    ) ~= QPSC_normalizeReputationPath(
        contract.reputationPath
    ) then
        addReputationReward(
            contract.secondaryReputationPath,
            contract.secondaryReputationPoints
        )
    end

    contract.reward =
        #rewards > 0
        and table.concat(rewards, " + ")
        or QPSC_I18N.getText("UI_QPSC_None")
end

local function QPSC_addLocalMultiContract(player, args)
    local data = QPSC_getLocalData(); QPSC_migrateLocalData(data)
    local objectives = QPSC_flatObjectivesFromArgs(args, player)
    if #objectives < 2 then QPSC_say(player,QPSC_I18N.getText("UI_QPSC_MessageMultiNeedsObjectives")); return end
    local id=data.nextId or 1; local mode=QPSC_normalizeCompletionMode(args.completionMode)
    local contract={id=id,category=QPSC_normalizeCategory(args.category),difficulty=QPSC_normalizeDifficulty(args.difficulty),completionMode=mode,multiObjective=true,objectives=objectives,sharedObjectiveProgress={},globalCompleted=false,globalCompletedBy="",globalCompletedAt=0,globalCompletionSource="",sharedProgress=mode=="SHARED_TEAM" and 0 or nil,sharedCompleted=mode=="SHARED_TEAM" and false or nil,sharedCompletedBy=mode=="SHARED_TEAM" and "" or nil,sharedCompletedAt=mode=="SHARED_TEAM" and 0 or nil,sharedCompletionSource=mode=="SHARED_TEAM" and "" or nil,title=tostring(args.title or "Untitled Contract"),description=tostring(args.description or ""),location=tostring(args.location or "Unknown"),reward="None",objectiveType="MULTI",objectiveTarget=#objectives,objectiveRadius=0,objectiveItemFullType="",objectiveItemDisplayName="",targetX=math.floor(player:getX()),targetY=math.floor(player:getY()),targetZ=math.floor(player:getZ()),rewardItemFullType=tostring(args.rewardItemFullType or ""),rewardItemDisplayName=tostring(args.rewardItemDisplayName or ""),rewardQuantity=QPSC_normalizePositiveInteger(args.rewardQuantity,100),rewardItems=QPSC_rewardItemsFromArgs(args),reputationPath=QPSC_normalizeReputationPath(args.reputationPath),reputationPoints=QPSC_normalizePositiveInteger(args.reputationPoints,100000),secondaryReputationPath=QPSC_normalizeReputationPath(args.secondaryReputationPath),secondaryReputationPoints=QPSC_normalizePositiveInteger(args.secondaryReputationPoints,100000),firstFinisherBonusItemFullType=tostring(args.firstFinisherBonusItemFullType or ""),firstFinisherBonusItemDisplayName=tostring(args.firstFinisherBonusItemDisplayName or ""),firstFinisherBonusQuantity=QPSC_normalizePositiveInteger(args.firstFinisherBonusQuantity,100),firstFinisherWinner="",firstFinisherWonAt=0,status="Open",postedBy=QPSC_getPlayerUsername(player),acceptedBy="",completedBy="",timeLimitHours=QPSC_normalizeTimeLimit(args.timeLimitHours),acceptedAt=0,expiresAt=0,expiredAt=0,closed=false,participants={}}
    QPSC_applyContractRewardItems(contract, contract.rewardItems)
    if mode=="SHARED_TEAM" then contract.firstFinisherBonusItemFullType=""; contract.firstFinisherBonusItemDisplayName=""; contract.firstFinisherBonusQuantity=0; for _,o in ipairs(objectives) do contract.sharedObjectiveProgress[o.id]=0 end end
    QPSC_applyLocalMultiRewardText(contract); table.insert(data.contracts,contract); data.nextId=id+1; QPSC_Client.contracts=data.contracts; QPSC_saveLocalContracts(); QPSC_announce(player,QPSC_I18N.getText("UI_QPSC_AnnouncementNewMultiContract",id,contract.title))
end

local function QPSC_updateLocalMultiContract(player,args)
    QPSC_loadLocalContracts(); local contract=QPSC_findContractById(QPSC_Client.contracts,args.contractId)
    if not QPSC_isMultiObjective(contract) then return end
    local locked=#(contract.participants or {})>0
    contract.title=tostring(args.title or contract.title); contract.location=tostring(args.location or contract.location); contract.description=tostring(args.description or contract.description); contract.category=QPSC_normalizeCategory(args.category); contract.difficulty=QPSC_normalizeDifficulty(args.difficulty); contract.timeLimitHours=QPSC_normalizeTimeLimit(args.timeLimitHours)
    if not locked then contract.completionMode=QPSC_normalizeCompletionMode(args.completionMode); contract.objectives=QPSC_flatObjectivesFromArgs(args,player); contract.objectiveTarget=#contract.objectives; contract.sharedObjectiveProgress={}; if QPSC_isSharedTeamCompletion(contract) then for _,o in ipairs(contract.objectives) do contract.sharedObjectiveProgress[o.id]=0 end end end
    QPSC_applyContractRewardItems(contract,QPSC_rewardItemsFromArgs(args)); contract.reputationPath=QPSC_normalizeReputationPath(args.reputationPath); contract.reputationPoints=QPSC_normalizePositiveInteger(args.reputationPoints,100000); contract.secondaryReputationPath=QPSC_normalizeReputationPath(args.secondaryReputationPath); contract.secondaryReputationPoints=QPSC_normalizePositiveInteger(args.secondaryReputationPoints,100000); if contract.secondaryReputationPath==contract.reputationPath then contract.secondaryReputationPath=""; contract.secondaryReputationPoints=0 end; contract.firstFinisherBonusItemFullType=tostring(args.firstFinisherBonusItemFullType or ""); contract.firstFinisherBonusItemDisplayName=tostring(args.firstFinisherBonusItemDisplayName or ""); contract.firstFinisherBonusQuantity=QPSC_normalizePositiveInteger(args.firstFinisherBonusQuantity,100)
    if QPSC_isSharedTeamCompletion(contract) then contract.firstFinisherBonusItemFullType=""; contract.firstFinisherBonusItemDisplayName=""; contract.firstFinisherBonusQuantity=0 end
    QPSC_applyLocalMultiRewardText(contract); QPSC_saveLocalContracts(); QPSC_say(player,QPSC_I18N.getText("UI_QPSC_MessageContractUpdated",contract.id))
end

-- =========================================================
-- CONTRACT WINDOW
-- =========================================================

QPSC_ContractsWindow = ISPanel:derive("QPSC_ContractsWindow")

-- QPSC_RESIZABLE_CONTRACT_WINDOW_V0310
local function QPSC_reputationRewardSummary(contract)
    local rewards = {}
    local labels = {
        community = "Community",
        hunter = "Hunter",
        explorer = "Explorer",
        medic = "Medic",
        mechanic = "Mechanic",
        builder = "Builder"
    }

    local function addReward(pathValue, pointsValue)
        local path = string.lower(tostring(pathValue or ""))
        local points = math.max(
            0,
            math.floor(tonumber(pointsValue) or 0)
        )

        if path ~= "" and points > 0 then
            table.insert(
                rewards,
                "+"
                    .. tostring(points)
                    .. " "
                    .. tostring(labels[path] or path)
            )
        end
    end

    addReward(
        contract and contract.reputationPath,
        contract and contract.reputationPoints
    )

    local primaryPath = string.lower(
        tostring(contract and contract.reputationPath or "")
    )
    local secondaryPath = string.lower(
        tostring(
            contract and contract.secondaryReputationPath
            or ""
        )
    )

    if secondaryPath ~= primaryPath then
        addReward(
            contract and contract.secondaryReputationPath,
            contract and contract.secondaryReputationPoints
        )
    end

    if #rewards == 0 then
        return QPSC_I18N.getText("UI_QPSC_None")
    end

    return table.concat(rewards, " / ")
end

local function QPSC_normalRewardSummary(contract)
    if contract == nil then
        return QPSC_I18N.getText("UI_QPSC_None")
    end

    local itemRewards =
        QPSC_rewardItemTexts(contract)

    if #itemRewards > 0 then
        return table.concat(itemRewards, " + ")
    end

    local reward = tostring(contract.reward or "")

    local path = string.lower(
        tostring(contract.reputationPath or "")
    )
    local points = math.max(
        0,
        math.floor(
            tonumber(contract.reputationPoints) or 0
        )
    )

    if path ~= "" and points > 0 then
        local labels = {
            community = "Community",
            hunter = "Hunter",
            explorer = "Explorer",
            medic = "Medic",
            mechanic = "Mechanic",
            builder = "Builder"
        }

        local label = tostring(labels[path] or path)
        local escapedLabel = label:gsub(
            "([^%w])",
            "%%%1"
        )

        reward = reward:gsub(
            "%s*%+%s*%+"
                .. tostring(points)
                .. "%s+"
                .. escapedLabel
                .. "%s+Reputation%s*$",
            ""
        )

        reward = reward:gsub(
            "%s*%+%s*"
                .. tostring(points)
                .. "%s+"
                .. escapedLabel
                .. "%s+Reputation%s*$",
            ""
        )
    end

    local previousReward = nil

    repeat
        previousReward = reward
        reward = reward:gsub(
            "%s*%+%s*%+%d+%s+[%a]+%s+Reputation%s*$",
            ""
        )
        reward = reward:gsub(
            "%s*%+%s*%d+%s+[%a]+%s+Reputation%s*$",
            ""
        )
    until reward == previousReward
    reward = reward:gsub("^%s+", ""):gsub("%s+$", "")

    if reward == ""
        or reward == "Manual reward"
        or reward == "Manual Reward"
        or reward == QPSC_I18N.getText("UI_QPSC_None") then
        return QPSC_I18N.getText("UI_QPSC_None")
    end

    return reward
end

local QPSC_CONTRACT_CARD_HEIGHT = 212
local QPSC_CONTRACT_CARD_GAP = 10
local QPSC_SELECTED_MULTI_HEADER_HEIGHT = 28
local QPSC_SELECTED_MULTI_LINE_HEIGHT = 22

local function QPSC_contractCardHeight(contract, isSelected)
    if isSelected == true and QPSC_isMultiObjective(contract) then
        return QPSC_CONTRACT_CARD_HEIGHT
            + QPSC_SELECTED_MULTI_HEADER_HEIGHT
            + (#(contract.objectives or {}) * QPSC_SELECTED_MULTI_LINE_HEIGHT)
    end

    return QPSC_CONTRACT_CARD_HEIGHT
end

local QPSC_WINDOW_MIN_WIDTH = 900
local QPSC_WINDOW_MIN_HEIGHT = 440
local QPSC_WINDOW_RESIZE_GRIP = 34

function QPSC_ContractsWindow:new(x, y, width, height, player)
    local o = ISPanel:new(x, y, width, height)
    setmetatable(o, self)
    self.__index = self

    o.player = player
    o.moveWithMouse = true
    o.scrollOffset = 0
    o.maxScroll = 0
    o.selectedContractId = nil
    o.cardHitboxes = {}
    o.resizing = false
    o.resizeGripSize = QPSC_WINDOW_RESIZE_GRIP
    o.minWindowWidth = QPSC_WINDOW_MIN_WIDTH
    o.minWindowHeight = QPSC_WINDOW_MIN_HEIGHT
    o.lastLayoutWidth = nil
    o.lastLayoutHeight = nil
    o.backgroundColor = {r=0, g=0, b=0, a=0.88}
    o.borderColor = {r=0.8, g=0.8, b=0.8, a=0.9}

    return o
end

function QPSC_ContractsWindow:layoutControls()
    if self.refreshButton == nil then
        return
    end

    local buttonY = self.height - 42
    local compact = self.width < 1120
    local gap = compact and 6 or 8
    local refreshX = 16
    local refreshWidth = compact and 90 or 110
    local scrollUpX = refreshX + refreshWidth + gap
    local scrollUpWidth = compact and 80 or 100
    local scrollDownX = scrollUpX + scrollUpWidth + gap
    local scrollDownWidth = compact and 90 or 110
    local acceptX = scrollDownX + scrollDownWidth + gap
    local detailsWidth = compact and 100 or 118
    local editWidth = compact and 112 or 140
    local deleteWidth = compact and 122 or 128
    local closeWidth = compact and 90 or 110
    local closeX = self.width - 16 - closeWidth
    local deleteX = closeX - gap - deleteWidth
    local editX = deleteX - gap - editWidth
    local detailsX = editX - gap - detailsWidth
    local showAdminButtons = QPSC_isLocalAdmin(self.player)
    local acceptEndX = detailsX
    local acceptWidth = math.max(
        compact and 90 or 100,
        acceptEndX - acceptX - gap
    )

    self.refreshButton:setX(refreshX)
    self.refreshButton:setY(buttonY)
    self.refreshButton:setWidth(refreshWidth)

    self.scrollUpButton:setX(scrollUpX)
    self.scrollUpButton:setY(buttonY)
    self.scrollUpButton:setWidth(scrollUpWidth)

    self.scrollDownButton:setX(scrollDownX)
    self.scrollDownButton:setY(buttonY)
    self.scrollDownButton:setWidth(scrollDownWidth)

    self.acceptSelectedButton:setX(acceptX)
    self.acceptSelectedButton:setY(buttonY)
    self.acceptSelectedButton:setWidth(acceptWidth)

    self.objectivesButton:setX(detailsX)
    self.objectivesButton:setY(buttonY)
    self.objectivesButton:setWidth(detailsWidth)

    self.editSelectedButton:setX(editX)
    self.editSelectedButton:setY(buttonY)
    self.editSelectedButton:setWidth(editWidth)
    self.editSelectedButton.visible = showAdminButtons

    self.deleteSelectedButton:setX(deleteX)
    self.deleteSelectedButton:setY(buttonY)
    self.deleteSelectedButton:setWidth(deleteWidth)
    self.deleteSelectedButton.visible = showAdminButtons

    self.closeButton:setX(closeX)
    self.closeButton:setY(buttonY)
    self.closeButton:setWidth(closeWidth)

    self.lastLayoutWidth = self.width
    self.lastLayoutHeight = self.height
end

function QPSC_ContractsWindow:resizeBy(dx, dy)
    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()
    local currentX = tonumber(self:getX()) or 0
    local currentY = tonumber(self:getY()) or 0
    local minWidth = math.min(
        self.minWindowWidth or QPSC_WINDOW_MIN_WIDTH,
        math.max(520, screenWidth - 20)
    )
    local minHeight = math.min(
        self.minWindowHeight or QPSC_WINDOW_MIN_HEIGHT,
        math.max(360, screenHeight - 20)
    )
    local maxWidth = math.max(
        minWidth,
        screenWidth - currentX - 10
    )
    local maxHeight = math.max(
        minHeight,
        screenHeight - currentY - 10
    )
    local newWidth = math.max(
        minWidth,
        math.min(
            maxWidth,
            (tonumber(self.width) or minWidth)
                + (tonumber(dx) or 0)
        )
    )
    local newHeight = math.max(
        minHeight,
        math.min(
            maxHeight,
            (tonumber(self.height) or minHeight)
                + (tonumber(dy) or 0)
        )
    )

    self:setWidth(newWidth)
    self:setHeight(newHeight)
    self:layoutControls()
end

function QPSC_ContractsWindow:initialise()
    ISPanel.initialise(self)

    -- QPSC_POLISHED_WINDOW_LAYOUT_V1
    self.refreshButton = ISButton:new(
        16,
        self.height - 42,
        120,
        28,
        QPSC_I18N.getText("UI_QPSC_Refresh"),
        self,
        QPSC_ContractsWindow.onRefresh
    )
    self.refreshButton:initialise()
    self.refreshButton:instantiate()
    self.refreshButton.backgroundColor = {r=0.14, g=0.14, b=0.14, a=0.95}
    self.refreshButton.backgroundColorMouseOver = {r=0.24, g=0.24, b=0.24, a=1}
    self.refreshButton.borderColor = {r=0.55, g=0.55, b=0.55, a=0.95}
    self.refreshButton.textColor = {r=1, g=1, b=1, a=1}
    self:addChild(self.refreshButton)

    -- QPSC_RELIABLE_SCROLL_CONTROLS_V1
    self.scrollUpButton = ISButton:new(
        146,
        self.height - 42,
        100,
        28,
        QPSC_I18N.getText("UI_QPSC_ScrollUp"),
        self,
        QPSC_ContractsWindow.onScrollUp
    )
    self.scrollUpButton:initialise()
    self.scrollUpButton:instantiate()
    self.scrollUpButton.backgroundColor = {r=0.14, g=0.14, b=0.14, a=0.95}
    self.scrollUpButton.backgroundColorMouseOver = {r=0.24, g=0.24, b=0.24, a=1}
    self.scrollUpButton.borderColor = {r=0.55, g=0.55, b=0.55, a=0.95}
    self.scrollUpButton.textColor = {r=1, g=1, b=1, a=1}
    self:addChild(self.scrollUpButton)

    self.scrollDownButton = ISButton:new(
        256,
        self.height - 42,
        110,
        28,
        QPSC_I18N.getText("UI_QPSC_ScrollDown"),
        self,
        QPSC_ContractsWindow.onScrollDown
    )
    self.scrollDownButton:initialise()
    self.scrollDownButton:instantiate()
    self.scrollDownButton.backgroundColor = {r=0.14, g=0.14, b=0.14, a=0.95}
    self.scrollDownButton.backgroundColorMouseOver = {r=0.24, g=0.24, b=0.24, a=1}
    self.scrollDownButton.borderColor = {r=0.55, g=0.55, b=0.55, a=0.95}
    self.scrollDownButton.textColor = {r=1, g=1, b=1, a=1}
    self:addChild(self.scrollDownButton)

    -- QPSC_ACCEPT_FROM_VIEW_V1
    self.acceptSelectedButton = ISButton:new(
        376,
        self.height - 42,
        math.max(160, self.width - 528),
        28,
        QPSC_I18N.getText(
            "UI_QPSC_AcceptSelectedContract"
        ),
        self,
        QPSC_ContractsWindow.onAcceptSelected
    )
    self.acceptSelectedButton:initialise()
    self.acceptSelectedButton:instantiate()
    self.acceptSelectedButton.backgroundColor = {r=0.12, g=0.24, b=0.14, a=0.95}
    self.acceptSelectedButton.backgroundColorMouseOver = {r=0.18, g=0.38, b=0.22, a=1}
    self.acceptSelectedButton.borderColor = {r=0.48, g=0.78, b=0.52, a=0.95}
    self.acceptSelectedButton.textColor = {r=1, g=1, b=1, a=1}
    self.acceptSelectedButton.enable = false
    self:addChild(self.acceptSelectedButton)

    self.objectivesButton = ISButton:new(
        self.width - 404,
        self.height - 42,
        118,
        28,
        QPSC_I18N.getText("UI_QPSC_Objectives"),
        self,
        QPSC_ContractsWindow.onObjectivesSelected
    )
    self.objectivesButton:initialise()
    self.objectivesButton:instantiate()
    self.objectivesButton.backgroundColor = {r=0.08, g=0.16, b=0.22, a=0.95}
    self.objectivesButton.backgroundColorMouseOver = {r=0.12, g=0.28, b=0.38, a=1}
    self.objectivesButton.borderColor = {r=0.36, g=0.68, b=0.86, a=0.95}
    self.objectivesButton.textColor = {r=1, g=1, b=1, a=1}
    self.objectivesButton.enable = false
    self:addChild(self.objectivesButton)

    self.editSelectedButton = ISButton:new(
        self.width - 276,
        self.height - 42,
        140,
        28,
        QPSC_I18N.getText("UI_QPSC_EditSelectedContract"),
        self,
        QPSC_ContractsWindow.onEditSelected
    )
    self.editSelectedButton:initialise()
    self.editSelectedButton:instantiate()
    self.editSelectedButton.backgroundColor = {r=0.20, g=0.16, b=0.08, a=0.95}
    self.editSelectedButton.backgroundColorMouseOver = {r=0.34, g=0.27, b=0.12, a=1}
    self.editSelectedButton.borderColor = {r=0.82, g=0.66, b=0.24, a=0.95}
    self.editSelectedButton.textColor = {r=1, g=1, b=1, a=1}
    self.editSelectedButton.enable = false
    self:addChild(self.editSelectedButton)

    -- QPSC_DELETE_SELECTED_FROM_WINDOW_V1
    self.deleteSelectedButton = ISButton:new(
        self.width - 264,
        self.height - 42,
        128,
        28,
        QPSC_I18N.getText("UI_QPSC_DeleteContract"),
        self,
        QPSC_ContractsWindow.onDeleteSelected
    )
    self.deleteSelectedButton:initialise()
    self.deleteSelectedButton:instantiate()
    self.deleteSelectedButton.backgroundColor = {r=0.28, g=0.08, b=0.08, a=0.95}
    self.deleteSelectedButton.backgroundColorMouseOver = {r=0.48, g=0.12, b=0.12, a=1}
    self.deleteSelectedButton.borderColor = {r=0.88, g=0.32, b=0.32, a=0.95}
    self.deleteSelectedButton.textColor = {r=1, g=1, b=1, a=1}
    self.deleteSelectedButton.enable = false
    self:addChild(self.deleteSelectedButton)

    self.closeButton = ISButton:new(
        self.width - 156,
        self.height - 42,
        110,
        28,
        QPSC_I18N.getText("UI_QPSC_Close"),
        self,
        QPSC_ContractsWindow.onClose
    )
    self.closeButton:initialise()
    self.closeButton:instantiate()
    self.closeButton.backgroundColor = {r=0.14, g=0.14, b=0.14, a=0.95}
    self.closeButton.backgroundColorMouseOver = {r=0.24, g=0.24, b=0.24, a=1}
    self.closeButton.borderColor = {r=0.55, g=0.55, b=0.55, a=0.95}
    self.closeButton.textColor = {r=1, g=1, b=1, a=1}
    self:addChild(self.closeButton)

    self:layoutControls()
end

function QPSC_ContractsWindow:onRefresh()
    if QPSC_isMultiplayerClient() then
        QPSC_sendCommand("RequestContracts", {})
    else
        QPSC_loadLocalContracts()
    end
end

function QPSC_ContractsWindow:onClose()
    self:removeFromUIManager()
    QPSC_Client.window = nil
end

-- QPSC_SCROLLABLE_CONTRACT_WINDOW_V1
-- QPSC_RELIABLE_SCROLL_CONTROLS_V1
function QPSC_ContractsWindow:scrollBy(amount)
    local current = tonumber(self.scrollOffset) or 0
    local maximum = tonumber(self.maxScroll) or 0

    self.scrollOffset = math.max(
        0,
        math.min(
            maximum,
            current + (tonumber(amount) or 0)
        )
    )
end

function QPSC_ContractsWindow:onScrollUp()
    self:scrollBy(-132)
end

function QPSC_ContractsWindow:onScrollDown()
    self:scrollBy(132)
end

-- QPSC_ACCEPT_FROM_VIEW_V1
function QPSC_ContractsWindow:onMouseDown(x, y)
    local gripSize =
        tonumber(self.resizeGripSize)
        or QPSC_WINDOW_RESIZE_GRIP

    if x >= self.width - gripSize
        and y >= self.height - gripSize then
        self.resizing = true

        if self.setCapture then
            self:setCapture(true)
        end

        return true
    end

    for _, hitbox in ipairs(
        self.cardHitboxes or {}
    ) do
        if x >= hitbox.x
            and x <= hitbox.x + hitbox.width
            and y >= hitbox.y
            and y <= hitbox.y + hitbox.height then
            self.selectedContractId =
                hitbox.contractId
            return true
        end
    end

    if ISPanel.onMouseDown then
        return ISPanel.onMouseDown(self, x, y)
    end

    return false
end

function QPSC_ContractsWindow:onMouseMove(dx, dy)
    if self.resizing then
        self:resizeBy(dx, dy)
        return true
    end

    if ISPanel.onMouseMove then
        return ISPanel.onMouseMove(self, dx, dy)
    end

    return false
end

function QPSC_ContractsWindow:onMouseMoveOutside(dx, dy)
    if self.resizing then
        self:resizeBy(dx, dy)
        return
    end

    if ISPanel.onMouseMoveOutside then
        ISPanel.onMouseMoveOutside(self, dx, dy)
    end
end

function QPSC_ContractsWindow:onMouseUp(x, y)
    if self.resizing then
        self.resizing = false

        if self.setCapture then
            self:setCapture(false)
        end

        return true
    end

    if ISPanel.onMouseUp then
        return ISPanel.onMouseUp(self, x, y)
    end

    return false
end

function QPSC_ContractsWindow:onMouseUpOutside(x, y)
    if self.resizing then
        self.resizing = false

        if self.setCapture then
            self:setCapture(false)
        end

        return
    end

    if ISPanel.onMouseUpOutside then
        ISPanel.onMouseUpOutside(self, x, y)
    end
end

function QPSC_ContractsWindow:onAcceptSelected()
    local contract =
        QPSC_findContractById(
            self.selectedContractId
        )
    local canAccept, messageKey, messageArg =
        QPSC_canPlayerAcceptContract(
            self.player,
            contract
        )

    if not canAccept then
        if messageKey ~= nil then
            if messageArg ~= nil then
                QPSC_say(
                    self.player,
                    QPSC_I18N.getText(
                        messageKey,
                        messageArg
                    )
                )
            else
                QPSC_say(
                    self.player,
                    QPSC_I18N.getText(
                        messageKey
                    )
                )
            end
        end

        return
    end

    local contractId = contract.id
    self.selectedContractId = nil
    QPSC_Client.acceptContract(
        self.player,
        contractId
    )
end


function QPSC_ContractsWindow:onObjectivesSelected()
    local contract = QPSC_findContractById(self.selectedContractId)
    if QPSC_isMultiObjective(contract) then
        QPSC_MultiObjectiveUI.openDetails(self.player, contract)
    end
end


function QPSC_ContractsWindow:onEditSelected()
    if not QPSC_isLocalAdmin(self.player) then
        return
    end

    local contract = QPSC_findContractById(self.selectedContractId)

    if contract == nil then
        QPSC_say(
            self.player,
            QPSC_I18N.getText("UI_QPSC_MessageSelectContractToEdit")
        )
        return
    end

    if QPSC_isMultiObjective(contract) then
        QPSC_MultiObjectiveUI.openEdit(self.player, contract)
    else
        QPSC_TrackedUI.openEdit(self.player, contract)
    end
end

-- QPSC_DELETE_SELECTED_FROM_WINDOW_V1
function QPSC_ContractsWindow:onDeleteSelected()
    if not QPSC_isLocalAdmin(self.player) then
        return
    end

    local contract = QPSC_findContractById(self.selectedContractId)

    if contract == nil then
        return
    end

    local contractId = contract.id
    self.selectedContractId = nil

    QPSC_Client.deleteContract(
        self.player,
        contractId
    )
end

function QPSC_ContractsWindow:onMouseWheel(delta)
    -- Build 42 sends a positive wheel delta when scrolling down.
    self:scrollBy((tonumber(delta) or 0) * 72)
    return true
end

function QPSC_ContractsWindow:prerender()
    ISPanel.prerender(self)

    if self.lastLayoutWidth ~= self.width
        or self.lastLayoutHeight ~= self.height then
        self:layoutControls()
    end

    local panelX = 14
    local panelY = 80
    local panelWidth = self.width - 28
    local panelHeight = self.height - 138

    self:drawRect(0, 0, self.width, self.height, 0.94, 0.015, 0.015, 0.015)
    self:drawRectBorder(0, 0, self.width, self.height, 0.95, 0.55, 0.55, 0.55)

    self:drawRect(1, 1, self.width - 2, 71, 0.98, 0.055, 0.055, 0.055)
    self:drawRect(14, 71, self.width - 28, 2, 0.90, 0.82, 0.63, 0.22)

    self:drawText(
        QPSC_I18N.getText("UI_QPSC_ModName"),
        18,
        12,
        1,
        1,
        1,
        1,
        UIFont.Medium
    )

    local count = QPSC_countContracts()
    local selectedContract =
        QPSC_findContractById(
            self.selectedContractId
        )

    if self.selectedContractId ~= nil
        and selectedContract == nil then
        self.selectedContractId = nil
    end

    local canAcceptSelected = false

    if selectedContract ~= nil then
        canAcceptSelected =
            QPSC_canPlayerAcceptContract(
                self.player,
                selectedContract
            )
    end

    if self.acceptSelectedButton ~= nil then
        self.acceptSelectedButton.enable =
            canAcceptSelected == true

        if selectedContract ~= nil then
            self.acceptSelectedButton.tooltip =
                QPSC_contractLabel(
                    selectedContract
                )
        else
            self.acceptSelectedButton.tooltip = nil
        end
    end

    if self.objectivesButton ~= nil then
        self.objectivesButton.enable = QPSC_isMultiObjective(selectedContract)
        self.objectivesButton.visible = true
        self.objectivesButton.tooltip = QPSC_isMultiObjective(selectedContract)
            and QPSC_I18N.getText("UI_QPSC_ViewObjectiveDetails") or nil
    end

    if self.editSelectedButton ~= nil then
        local canEdit = QPSC_isLocalAdmin(self.player)
        self.editSelectedButton.visible = canEdit
        self.editSelectedButton.enable =
            selectedContract ~= nil and canEdit

        if selectedContract ~= nil then
            self.editSelectedButton.tooltip =
                QPSC_contractLabel(selectedContract)
        else
            self.editSelectedButton.tooltip = nil
        end
    end

    if self.deleteSelectedButton ~= nil then
        local canDelete = QPSC_isLocalAdmin(self.player)
        self.deleteSelectedButton.visible = canDelete
        self.deleteSelectedButton.enable =
            selectedContract ~= nil and canDelete

        if selectedContract ~= nil then
            self.deleteSelectedButton.tooltip =
                QPSC_contractLabel(selectedContract)
        else
            self.deleteSelectedButton.tooltip = nil
        end
    end

    self:drawText(
        QPSC_I18N.getText("UI_QPSC_TotalContracts", count),
        18,
        39,
        0.78,
        0.78,
        0.78,
        1,
        UIFont.Small
    )

    self:drawRect(
        panelX,
        panelY,
        panelWidth,
        panelHeight,
        0.94,
        0.025,
        0.025,
        0.025
    )

    self:drawRectBorder(
        panelX,
        panelY,
        panelWidth,
        panelHeight,
        0.82,
        0.42,
        0.42,
        0.42
    )

    local cardX = panelX + 10
    local cardWidth = panelWidth - 32
    local cardGap = QPSC_CONTRACT_CARD_GAP
    local contentTop = panelY + 10
    local contentBottom = panelY + panelHeight - 10
    local viewportHeight = contentBottom - contentTop
    local totalHeight = 0

    if count > 0 then
        for _, contract in ipairs(QPSC_Client.contracts or {}) do
            local isSelected =
                tostring(self.selectedContractId or "")
                    == tostring(contract.id)

            totalHeight = totalHeight
                + QPSC_contractCardHeight(contract, isSelected)
                + cardGap
        end

        totalHeight = math.max(0, totalHeight - cardGap)
    end

    self.maxScroll = math.max(
        0,
        totalHeight - viewportHeight
    )
    self.scrollOffset = math.max(
        0,
        math.min(
            tonumber(self.scrollOffset) or 0,
            self.maxScroll
        )
    )

    if self.maxScroll > 0 then
        self:drawText(
            QPSC_I18N.getText("UI_QPSC_ScrollHint"),
            250,
            39,
            0.78,
            0.78,
            0.78,
            1,
            UIFont.Small
        )
    end

    self.cardHitboxes = {}

    if count == 0 then
        self:drawText(
            QPSC_I18N.getText("UI_QPSC_NoContracts"),
            cardX + 10,
            contentTop + 12,
            0.78,
            0.78,
            0.78,
            1,
            UIFont.Small
        )
    else
        local stencilActive = false

        if self.setStencilRect and self.clearStencilRect then
            self:setStencilRect(
                panelX + 1,
                panelY + 1,
                panelWidth - 2,
                panelHeight - 2
            )
            stencilActive = true
        end

        local cardY = contentTop - self.scrollOffset

        for _, contract in ipairs(QPSC_Client.contracts or {}) do
            local isSelected =
                tostring(self.selectedContractId or "")
                    == tostring(contract.id)
            local cardHeight =
                QPSC_contractCardHeight(contract, isSelected)

            if cardY > contentBottom then
                break
            end

            if cardY + cardHeight >= contentTop then
                local hitTop =
                    math.max(cardY, contentTop)
                local hitBottom =
                    math.min(
                        cardY + cardHeight,
                        contentBottom
                    )
                table.insert(
                    self.cardHitboxes,
                    {
                        contractId = contract.id,
                        x = cardX,
                        y = hitTop,
                        width = cardWidth,
                        height = math.max(
                            0,
                            hitBottom - hitTop
                        )
                    }
                )

                local counts =
                    QPSC_contractCounts(contract)
                local accentR = 0.82
                local accentG = 0.63
                local accentB = 0.22

                if contract.closed == true then
                    accentR = 0.55
                    accentG = 0.55
                    accentB = 0.55
                elseif counts.active > 0 then
                    accentR = 0.30
                    accentG = 0.68
                    accentB = 0.95
                elseif counts.completed > 0 then
                    accentR = 0.35
                    accentG = 0.82
                    accentB = 0.42
                elseif counts.expired > 0
                    or counts.notCompleted > 0 then
                    accentR = 0.90
                    accentG = 0.38
                    accentB = 0.30
                end

                self:drawRect(
                    cardX,
                    cardY,
                    cardWidth,
                    cardHeight,
                    0.74,
                    0.075,
                    0.075,
                    0.075
                )

                self:drawRectBorder(
                    cardX,
                    cardY,
                    cardWidth,
                    cardHeight,
                    0.60,
                    0.34,
                    0.34,
                    0.34
                )

                if isSelected then
                    self:drawRectBorder(
                        cardX + 1,
                        cardY + 1,
                        cardWidth - 2,
                        cardHeight - 2,
                        1,
                        0.95,
                        0.76,
                        0.22
                    )
                    self:drawRectBorder(
                        cardX + 2,
                        cardY + 2,
                        cardWidth - 4,
                        cardHeight - 4,
                        0.88,
                        0.95,
                        0.76,
                        0.22
                    )
                end

                self:drawRect(
                    cardX,
                    cardY,
                    4,
                    cardHeight,
                    0.95,
                    accentR,
                    accentG,
                    accentB
                )

                local categoryIcon =
                    QPSC_getCategoryIcon(contract)
                local textX = cardX + 18
                local titleBody =
                    QPSC_contractPlainTitle(contract)

                if categoryIcon ~= nil then
                    self:drawTextureScaled(
                        categoryIcon,
                        cardX + 12,
                        cardY + 8,
                        40,
                        40,
                        1
                    )
                    textX = cardX + 60
                else
                    titleBody =
                        QPSC_contractTitleText(contract)
                end

                local title = "#"
                    .. tostring(contract.id)
                    .. " ["
                    .. QPSC_statusText(contract)
                    .. "] "
                    .. titleBody
                local difficultyText =
                    QPSC_difficultyText(contract.difficulty)
                local difficultyR, difficultyG, difficultyB =
                    QPSC_difficultyColor(contract.difficulty)
                local difficultyWidth = 90
                local difficultyX =
                    cardX + cardWidth - difficultyWidth - 10

                self:drawRect(
                    difficultyX,
                    cardY + 6,
                    difficultyWidth,
                    26,
                    0.88,
                    difficultyR * 0.28,
                    difficultyG * 0.28,
                    difficultyB * 0.28
                )
                self:drawRectBorder(
                    difficultyX,
                    cardY + 6,
                    difficultyWidth,
                    26,
                    0.95,
                    difficultyR,
                    difficultyG,
                    difficultyB
                )
                self:drawText(
                    difficultyText,
                    difficultyX + 8,
                    cardY + 9,
                    difficultyR,
                    difficultyG,
                    difficultyB,
                    1,
                    UIFont.Small
                )

                self:drawText(
                    title,
                    textX,
                    cardY + 8,
                    1,
                    1,
                    1,
                    1,
                    UIFont.Small
                )

                self:drawText(
                    QPSC_I18N.getText("UI_QPSC_Location")
                        .. ": "
                        .. tostring(
                            contract.location
                            or QPSC_I18N.getText("UI_QPSC_Unknown")
                        ),
                    textX,
                    cardY + 32,
                    0.70,
                    0.88,
                    1,
                    1,
                    UIFont.Small
                )

                self:drawText(
                    QPSC_I18N.getText("UI_QPSC_Objective")
                        .. ": "
                        .. QPSC_objectiveSummary(
                            contract,
                            self.player
                        )
                        .. " | "
                        .. QPSC_I18N.getText(
                            "UI_QPSC_CompletionMode"
                        )
                        .. ": "
                        .. QPSC_completionModeText(
                            contract.completionMode
                        ),
                    textX,
                    cardY + 52,
                    0.72,
                    0.90,
                    1,
                    1,
                    UIFont.Small
                )

                self:drawText(
                    QPSC_I18N.getText("UI_QPSC_Reward")
                        .. ": "
                        .. QPSC_normalRewardSummary(contract),
                    textX,
                    cardY + 72,
                    0.70,
                    1,
                    0.70,
                    1,
                    UIFont.Small
                )

                self:drawText(
                    QPSC_I18N.getText(
                        "UI_QPSC_ReputationRewards"
                    )
                        .. ": "
                        .. QPSC_reputationRewardSummary(contract),
                    textX,
                    cardY + 94,
                    0.72,
                    0.78,
                    1,
                    1,
                    UIFont.Small
                )

                self:drawText(
                    QPSC_I18N.getText(
                        "UI_QPSC_FirstFinisherBonus"
                    )
                        .. ": "
                        .. QPSC_firstFinisherSummary(contract),
                    textX,
                    cardY + 116,
                    1,
                    0.82,
                    0.38,
                    1,
                    UIFont.Small
                )

                local participantLineOne,
                    participantLineTwo =
                    QPSC_buildParticipantSummaryLines(
                        contract
                    )

                self:drawText(
                    QPSC_I18N.getText("UI_QPSC_Participants")
                        .. ": "
                        .. participantLineOne,
                    textX,
                    cardY + 138,
                    0.92,
                    0.82,
                    0.48,
                    1,
                    UIFont.Small
                )

                if participantLineTwo ~= nil
                    and participantLineTwo ~= "" then
                    self:drawText(
                        participantLineTwo,
                        textX,
                        cardY + 158,
                        0.92,
                        0.82,
                        0.48,
                        1,
                        UIFont.Small
                    )
                end

                self:drawText(
                    QPSC_I18N.getText("UI_QPSC_Description")
                        .. ": "
                        .. tostring(contract.description or ""),
                    textX,
                    cardY + 182,
                    0.84,
                    0.84,
                    0.84,
                    1,
                    UIFont.Small
                )

                if isSelected
                    and QPSC_isMultiObjective(contract) then
                    local participant = QPSC_findParticipant(
                        contract,
                        QPSC_getPlayerUsername(self.player)
                    )
                    local headingY = cardY + 206

                    self:drawRect(
                        textX,
                        headingY - 4,
                        math.max(100, cardWidth - (textX - cardX) - 18),
                        1,
                        0.62,
                        0.32,
                        0.32,
                        0.32
                    )

                    self:drawText(
                        QPSC_I18N.getText("UI_QPSC_Objectives") .. ":",
                        textX,
                        headingY,
                        0.72,
                        0.90,
                        1,
                        1,
                        UIFont.Small
                    )

                    local lineY = headingY + 22

                    for index, objective in ipairs(
                        contract.objectives or {}
                    ) do
                        local target = math.max(
                            1,
                            tonumber(objective.target) or 1
                        )
                        local progress = math.min(
                            target,
                            math.max(
                                0,
                                tonumber(
                                    QPSC_getMultiProgress(
                                        contract,
                                        participant,
                                        objective
                                    )
                                ) or 0
                            )
                        )
                        local completed = progress >= target
                        local marker = completed and "[X] " or "[ ] "
                        local red = completed and 0.42 or 0.76
                        local green = completed and 1.00 or 0.90
                        local blue = completed and 0.42 or 1.00

                        self:drawText(
                            marker
                                .. tostring(index)
                                .. ". "
                                .. QPSC_multiObjectiveLabel(objective)
                                .. "    "
                                .. tostring(progress)
                                .. " / "
                                .. tostring(target),
                            textX,
                            lineY,
                            red,
                            green,
                            blue,
                            1,
                            UIFont.Small
                        )

                        lineY = lineY
                            + QPSC_SELECTED_MULTI_LINE_HEIGHT
                    end
                end
            end

            cardY = cardY + cardHeight + cardGap
        end

        if stencilActive then
            self:clearStencilRect()
        end

        if self.maxScroll > 0 then
            local trackX = panelX + panelWidth - 10
            local trackY = panelY + 5
            local trackHeight = panelHeight - 10
            local thumbHeight = math.max(
                36,
                trackHeight * (viewportHeight / totalHeight)
            )
            local thumbTravel = trackHeight - thumbHeight
            local thumbY = trackY

            if self.maxScroll > 0 then
                thumbY = trackY
                    + (thumbTravel
                    * (self.scrollOffset / self.maxScroll))
            end

            self:drawRect(
                trackX,
                trackY,
                4,
                trackHeight,
                0.60,
                0.12,
                0.12,
                0.12
            )
            self:drawRect(
                trackX,
                thumbY,
                4,
                thumbHeight,
                0.95,
                0.72,
                0.72,
                0.72
            )
        end
    end

    self:drawRect(
        14,
        self.height - 55,
        self.width - 28,
        1,
        0.55,
        0.42,
        0.42,
        0.42
    )

    local gripRight = self.width - 8
    local gripBottom = self.height - 8

    for index = 0, 2 do
        local length = 6 + (index * 5)

        self:drawRect(
            gripRight - length,
            gripBottom - (index * 5),
            length,
            2,
            0.90,
            0.72,
            0.72,
            0.72
        )
    end
end

local function QPSC_openContractsWindowNow(player)
    if QPSC_Client.window ~= nil then
        QPSC_Client.window:removeFromUIManager()
        QPSC_Client.window = nil
    end

    local screenWidth = getCore():getScreenWidth()
    local screenHeight = getCore():getScreenHeight()
    local contractCount = QPSC_countContracts()
    local visibleCount = math.min(math.max(contractCount, 1), 4)
    local availableWidth = math.max(520, screenWidth - 60)
    local width = math.min(1040, availableWidth)
    local desiredHeight = 178
        + (visibleCount * QPSC_CONTRACT_CARD_HEIGHT)
        + (math.max(0, visibleCount - 1)
        * QPSC_CONTRACT_CARD_GAP)
    local height = math.max(
        QPSC_WINDOW_MIN_HEIGHT,
        desiredHeight
    )

    height = math.min(
        height,
        820,
        math.max(360, screenHeight - 60)
    )

    local x = (screenWidth / 2) - (width / 2)
    local y = (screenHeight / 2) - (height / 2)

    local window = QPSC_ContractsWindow:new(x, y, width, height, player)
    window:initialise()
    window:addToUIManager()

    QPSC_Client.window = window
end

-- QPSC_FRESH_WINDOW_DATA_V1
function QPSC_Client.openContractsWindow(player)
    if QPSC_isMultiplayerClient() then
        local playerNum = 0

        if player and player.getPlayerNum then
            local ok, value = pcall(function()
                return player:getPlayerNum()
            end)

            if ok and value ~= nil then
                playerNum = tonumber(value) or 0
            end
        end

        QPSC_Client.pendingOpenPlayerNum = playerNum
        QPSC_sendCommand("RequestContracts", {})
        return
    end

    QPSC_loadLocalContracts()
    QPSC_openContractsWindowNow(player)
end

-- =========================================================
-- ACTIONS
-- =========================================================

function QPSC_Client.requestContracts(player)
    if QPSC_isMultiplayerClient() then
        QPSC_sendCommand("RequestContracts", {})
        QPSC_say(player, QPSC_I18N.getText("UI_QPSC_MessageRefreshing"))
        return
    end

    QPSC_loadLocalContracts()
    QPSC_say(player, QPSC_I18N.getText("UI_QPSC_MessageContractsUpdated"))
end

function QPSC_Client.showContracts(player)
    QPSC_Client.openContractsWindow(player)
end

function QPSC_Client.createContractFromText(
    player,
    text,
    categoryKey
)
    local args = QPSC_splitContractText(text)
    args.category =
        QPSC_normalizeCategory(categoryKey)
    args.difficulty =
        QPSC_normalizeDifficulty(args.difficulty)
    args.completionMode = "INDIVIDUAL"

    if args.timeLimitText ~= ""
        and args.timeLimitHours <= 0 then
        QPSC_say(
            player,
            QPSC_I18N.getText(
                "UI_QPSC_MessageInvalidTimeLimit"
            )
        )
        return
    end

    if args.title == "" then
        QPSC_say(player, QPSC_I18N.getText("UI_QPSC_MessageFormat"))
        return
    end

    if args.location == "" then args.location = "Unknown location" end
    if args.reward == "" then args.reward = "Manual reward" end
    if args.description == "" then args.description = "No description." end

    if QPSC_isMultiplayerClient() then
        local sent = QPSC_sendCommand("AddContract", args)
        if sent then
            QPSC_say(
                player,
                QPSC_I18N.getText("UI_QPSC_MessageCreating")
            )
        else
            QPSC_say(
                player,
                QPSC_I18N.getText(
                    "UI_QPSC_MessageCommandUnavailable"
                )
            )
        end
        return
    end

    QPSC_addLocalContract(player, args)
end


function QPSC_Client.createTrackedContract(player, args)
    args = args or {}
    args.category = QPSC_normalizeCategory(args.category)
    args.difficulty = QPSC_normalizeDifficulty(args.difficulty)
    args.objectiveType =
        QPSC_normalizeObjectiveType(args.objectiveType)
    local completionModeText = tostring(args.completionMode or "")
    args.completionMode = completionModeText ~= ""
        and QPSC_normalizeCompletionMode(completionModeText)
        or (
            (args.objectiveType == "KILL" or args.objectiveType == "LOCATION")
            and "GLOBAL"
            or "INDIVIDUAL"
        )

    if QPSC_isMultiplayerClient() then
        local sent = QPSC_sendCommand("AddContract", args)
        if sent then
            QPSC_say(
                player,
                QPSC_I18N.getText("UI_QPSC_MessageCreating")
            )
        else
            QPSC_say(
                player,
                QPSC_I18N.getText(
                    "UI_QPSC_MessageCommandUnavailable"
                )
            )
        end
        return
    end

    QPSC_addLocalContract(player, args)
end




function QPSC_Client.createMultiObjectiveContract(player, args)
    args = args or {}
    if QPSC_isMultiplayerClient() then
        local sent = QPSC_sendCommand("AddMultiContract", args)
        QPSC_say(player, sent and QPSC_I18N.getText("UI_QPSC_MessageCreating") or QPSC_I18N.getText("UI_QPSC_MessageCommandUnavailable"))
        return
    end
    QPSC_addLocalMultiContract(player, args)
end

function QPSC_Client.updateMultiObjectiveContract(player, args)
    args = args or {}
    if QPSC_isMultiplayerClient() then
        local sent = QPSC_sendCommand("UpdateMultiContract", args)
        QPSC_say(player, sent and QPSC_I18N.getText("UI_QPSC_MessageUpdatingContract") or QPSC_I18N.getText("UI_QPSC_MessageCommandUnavailable"))
        return
    end
    QPSC_updateLocalMultiContract(player, args)
end

-- QPSC_ADMIN_EDIT_CONTRACTS_V070
local function QPSC_localContractHasCompletedParticipant(contract)
    for _, participant in ipairs(contract.participants or {}) do
        if tostring(participant.status or "") == "Completed" then
            return true
        end
    end

    return false
end

local function QPSC_updateLocalContract(player, args)
    args = args or {}
    local contract = QPSC_findContractById(args.contractId)

    if contract == nil then
        QPSC_say(
            player,
            QPSC_I18N.getText("UI_QPSC_MessageContractNotFound")
        )
        return false
    end

    local participantCount = #(contract.participants or {})
    local hasCompleted =
        QPSC_localContractHasCompletedParticipant(contract)
    local oldTimeLimit = tonumber(contract.timeLimitHours) or 0
    local newTimeLimit =
        QPSC_normalizeTimeLimit(args.timeLimitHours)
    local requestedCompletionMode =
        tostring(args.completionMode or "") ~= ""
        and QPSC_normalizeCompletionMode(args.completionMode)
        or QPSC_normalizeCompletionMode(contract.completionMode)
    local requestedObjectiveType =
        QPSC_normalizeObjectiveType(args.objectiveType)
    local requestedObjectiveItemFullType =
        tostring(args.objectiveItemFullType or "")
    local requestedObjectiveItemDisplayName =
        tostring(args.objectiveItemDisplayName or "")

    if requestedCompletionMode == "SHARED_TEAM"
        and requestedObjectiveType ~= "KILL" then
        QPSC_say(
            player,
            QPSC_I18N.getText(
                "UI_QPSC_MessageSharedTeamKillOnly"
            )
        )
        return false
    end
    local requestedTarget = QPSC_normalizePositiveInteger(
        args.objectiveTarget,
        10000
    )
    local requestedRadius = QPSC_normalizePositiveInteger(
        args.objectiveRadius,
        1000
    )

    contract.title = QPSC_trim(args.title)
    if contract.title == "" then contract.title = "Untitled Contract" end
    contract.location = QPSC_trim(args.location)
    if contract.location == "" then contract.location = "Unknown location" end
    contract.description = QPSC_trim(args.description)
    if contract.description == "" then contract.description = "No description." end
    contract.category = QPSC_normalizeCategory(args.category)
    contract.difficulty = QPSC_normalizeDifficulty(args.difficulty)

    if participantCount == 0 then
        contract.completionMode = requestedCompletionMode

        if QPSC_isSharedTeamCompletion(contract) then
            contract.sharedProgress = 0
            contract.sharedCompleted = false
            contract.sharedCompletedBy = ""
            contract.sharedCompletedAt = 0
            contract.sharedCompletionSource = ""
        else
            contract.sharedProgress = nil
            contract.sharedCompleted = nil
            contract.sharedCompletedBy = nil
            contract.sharedCompletedAt = nil
            contract.sharedCompletionSource = nil
        end

        contract.objectiveType = requestedObjectiveType
        contract.objectiveItemFullType = requestedObjectiveItemFullType
        contract.objectiveItemDisplayName = requestedObjectiveItemDisplayName
    end

    if not hasCompleted then
        contract.objectiveTarget = requestedTarget
        contract.objectiveRadius = requestedRadius
    end

    if QPSC_normalizeObjectiveType(contract.objectiveType) == "MANUAL" then
        contract.objectiveTarget = 0
        contract.objectiveRadius = 0
        contract.objectiveItemFullType = ""
        contract.objectiveItemDisplayName = ""
    elseif QPSC_normalizeObjectiveType(contract.objectiveType) == "LOCATION" then
        contract.objectiveTarget = 1
        contract.objectiveRadius = math.max(1, math.min(20, tonumber(contract.objectiveRadius) or 3))
    elseif QPSC_normalizeObjectiveType(contract.objectiveType) == "KILL" then
        contract.objectiveTarget = math.max(1, tonumber(contract.objectiveTarget) or 1)
        local huntRadius = tonumber(contract.objectiveRadius) or 0
        if huntRadius <= 0 then
            contract.objectiveRadius = 0
        else
            contract.objectiveRadius = math.max(1, math.min(1000, huntRadius))
        end
    elseif QPSC_normalizeObjectiveType(contract.objectiveType) == "DELIVERY" then
        contract.objectiveTarget = math.max(1, tonumber(contract.objectiveTarget) or 1)
        contract.objectiveRadius = 0
    end

    QPSC_applyContractRewardItems(
        contract,
        QPSC_rewardItemsFromArgs(args)
    )
    contract.reputationPath =
        QPSC_normalizeReputationPath(args.reputationPath)
    contract.reputationPoints =
        QPSC_normalizePositiveInteger(args.reputationPoints, 100000)
    contract.secondaryReputationPath =
        QPSC_normalizeReputationPath(args.secondaryReputationPath)
    contract.secondaryReputationPoints =
        QPSC_normalizePositiveInteger(
            args.secondaryReputationPoints,
            100000
        )

    if contract.reputationPath == ""
        or contract.reputationPoints < 1 then
        contract.reputationPath = ""
        contract.reputationPoints = 0
    end

    if contract.secondaryReputationPath == ""
        or contract.secondaryReputationPoints < 1
        or contract.secondaryReputationPath
            == contract.reputationPath then
        contract.secondaryReputationPath = ""
        contract.secondaryReputationPoints = 0
    end

    contract.firstFinisherBonusItemFullType = tostring(args.firstFinisherBonusItemFullType or "")
    contract.firstFinisherBonusItemDisplayName = tostring(args.firstFinisherBonusItemDisplayName or "")
    contract.firstFinisherBonusQuantity = QPSC_normalizePositiveInteger(args.firstFinisherBonusQuantity, 100)

    if QPSC_isSharedTeamCompletion(contract) then
        contract.firstFinisherBonusItemFullType = ""
        contract.firstFinisherBonusItemDisplayName = ""
        contract.firstFinisherBonusQuantity = 0
        contract.firstFinisherWinner = ""
        contract.firstFinisherWonAt = 0
    end

    local rewardTexts =
        QPSC_rewardItemTexts(contract)

    if #rewardTexts > 0 then
        contract.reward =
            table.concat(rewardTexts, " + ")
    elseif QPSC_normalizeObjectiveType(
        contract.objectiveType
    ) ~= "MANUAL" then
        contract.reward =
            QPSC_I18N.getText("UI_QPSC_None")
    else
        contract.reward =
            tostring(
                args.reward
                or contract.reward
                or "Manual reward"
            )
    end

    contract.timeLimitHours = newTimeLimit

    if oldTimeLimit ~= newTimeLimit then
        local now = QPSC_getWorldAgeHours()

        for _, participant in ipairs(contract.participants or {}) do
            if tostring(participant.status or "") == "Accepted" then
                participant.remainingHours = newTimeLimit
                local key = QPSC_timerKey(contract.id, participant.username)
                QPSC_Client.timerMarks[key] = newTimeLimit > 0 and now or nil
            end
        end
    end

    local data = QPSC_getLocalData()
    QPSC_migrateLocalData(data)
    QPSC_Client.contracts = data.contracts or QPSC_Client.contracts
    QPSC_saveLocalContracts()
    QPSC_say(
        player,
        QPSC_I18N.getText(
            "UI_QPSC_MessageContractUpdated",
            contract.id
        )
    )
    return true
end

function QPSC_Client.updateContract(player, args)
    args = args or {}

    if QPSC_isMultiplayerClient() then
        local sent = QPSC_sendCommand("UpdateContract", args)

        if not sent then
            QPSC_say(
                player,
                QPSC_I18N.getText(
                    "UI_QPSC_MessageCommandUnavailable"
                )
            )
        end

        return sent
    end

    return QPSC_updateLocalContract(player, args)
end

function QPSC_Client.submitDelivery(player, contractId)
    if QPSC_isMultiplayerClient() then
        QPSC_sendCommand("SubmitDelivery", {
            contractId = contractId
        })
        return
    end

    QPSC_localSubmitDelivery(player, contractId)
end

function QPSC_Client:onCreateContractBox(button, playerNum)
    if button == nil then return end
    if button.internal ~= "OK" then return end

    local text = ""
    if button.parent and button.parent.entry then
        text = button.parent.entry:getText()
    end

    local player = getSpecificPlayer(playerNum or 0)
    if player == nil then player = getPlayer() end

    local categoryKey = "NONE"

    if button.parent
        and button.parent.qpscCategory then
        categoryKey =
            tostring(button.parent.qpscCategory)
    end

    QPSC_Client.createContractFromText(
        player,
        text,
        categoryKey
    )
end

-- QPSC_CREATE_POPUP_V1
function QPSC_Client.openCreateContractBox(
    player,
    categoryKey
)
    local width = 760
    local height = 220
    local x = (getCore():getScreenWidth() / 2) - (width / 2)
    local y = (getCore():getScreenHeight() / 2) - (height / 2)

    local dialogText =
        QPSC_I18N.getText("UI_QPSC_CreateDialogTitle")
        .. ":\n"
        .. QPSC_I18N.getText("UI_QPSC_CreateDialogPrompt")

    local example = QPSC_I18N.getText("UI_QPSC_CreateExample")

    local box = ISTextBox:new(
        x,
        y,
        width,
        height,
        dialogText,
        example,
        QPSC_Client,
        QPSC_Client.onCreateContractBox,
        0
    )

    box.qpscCategory =
        QPSC_normalizeCategory(categoryKey)

    box:initialise()
    box:addToUIManager()
end

function QPSC_Client.acceptContract(player, contractId)
    if QPSC_isMultiplayerClient() then
        QPSC_sendCommand("AcceptContract", {
            contractId = contractId
        })
        return
    end

    QPSC_loadLocalContracts()

    local username = QPSC_getPlayerUsername(player)
    local activeContract =
        QPSC_findActiveContractForPlayer(player)

    if activeContract then
        QPSC_say(
            player,
            QPSC_I18N.getText(
                "UI_QPSC_MessageActiveContractExists",
                QPSC_contractLabel(activeContract)
            )
        )
        return
    end

    for _, contract in pairs(
        QPSC_Client.contracts or {}
    ) do
        if tostring(contract.id)
            == tostring(contractId) then
            if contract.closed == true then
                QPSC_say(
                    player,
                    QPSC_I18N.getText(
                        "UI_QPSC_MessageNotOpen"
                    )
                )
                return
            end

            local participant =
                QPSC_findParticipant(
                    contract,
                    username
                )

            if participant ~= nil
                and tostring(participant.status or "")
                    ~= "Cancelled" then
                QPSC_say(
                    player,
                    QPSC_I18N.getText(
                        "UI_QPSC_MessageAlreadyJoined"
                    )
                )
                return
            end

            local acceptedAt =
                QPSC_getWorldAgeHours()
            local timeLimitHours =
                QPSC_normalizeTimeLimit(
                    contract.timeLimitHours
                )

            if participant == nil then
                participant = {
                    username = username
                }

                table.insert(
                    contract.participants,
                    participant
                )
            end

            participant.status = "Accepted"
            participant.acceptedAt = acceptedAt
            participant.remainingHours = timeLimitHours
            participant.completedAt = 0
            participant.expiredAt = 0
            participant.cancelledAt = 0
            participant.closedAt = 0
            participant.closedBy = ""
            participant.reviewedAt = 0
            participant.reviewedBy = ""
            participant.progress = 0
            if QPSC_isMultiObjective(contract) then
                participant.objectiveProgress = {}
                participant.objectiveContributions = {}
                QPSC_ensureParticipantMultiState(contract, participant)
            end
            participant.rewardGranted = false
            participant.rewardGrantedCount = 0
            participant.rewardGrantedCounts = {}
            participant.rewardGrantedAt = 0
            participant.rewardPending = false
            participant.firstFinisherBonusGranted = false
            participant.firstFinisherBonusGrantedCount = 0
            participant.firstFinisherBonusGrantedAt = 0
            participant.firstFinisherBonusPending = false

            if timeLimitHours > 0 then
                QPSC_Client.timerMarks[
                    QPSC_timerKey(
                        contract.id,
                        username
                    )
                ] = acceptedAt
            end

            QPSC_recomputeContractStatus(contract)
            QPSC_saveLocalContracts()

            QPSC_say(
                player,
                QPSC_I18N.getText(
                    "UI_QPSC_MessageContractAccepted",
                    contract.id
                )
            )
            return
        end
    end

    QPSC_say(
        player,
        QPSC_I18N.getText(
            "UI_QPSC_MessageContractNotFound"
        )
    )
end

-- QPSC_PLAYER_CANCEL_CONTRACT_V1
function QPSC_Client.cancelContract(player, contractId)
    if QPSC_isMultiplayerClient() then
        QPSC_sendCommand("CancelContract", {
            contractId = contractId
        })
        return
    end

    QPSC_loadLocalContracts()

    local contract, participant =
        QPSC_findActiveContractForPlayer(player)

    if contract == nil
        or participant == nil
        or tostring(contract.id) ~= tostring(contractId) then
        QPSC_say(
            player,
            QPSC_I18N.getText(
                "UI_QPSC_MessageNoActiveContract"
            )
        )
        return
    end

    participant.status = "Cancelled"
    participant.cancelledAt = QPSC_getWorldAgeHours()

    QPSC_Client.timerMarks[
        QPSC_timerKey(
            contract.id,
            participant.username
        )
    ] = nil

    QPSC_recomputeContractStatus(contract)
    QPSC_saveLocalContracts()

    QPSC_say(
        player,
        QPSC_I18N.getText(
            "UI_QPSC_MessageContractCancelled",
            contract.id
        )
    )
end

-- QPSC_ADMIN_PARTICIPANT_REVIEW_V1
function QPSC_Client.reviewParticipant(
    player,
    contractId,
    participantUsername,
    resultStatus
)
    if QPSC_isMultiplayerClient() then
        QPSC_sendCommand("ReviewParticipant",
            {
                contractId = contractId,
                participantUsername =
                    participantUsername,
                resultStatus = resultStatus
            }
        )
        return
    end

    QPSC_loadLocalContracts()

    for _, contract in pairs(
        QPSC_Client.contracts or {}
    ) do
        if tostring(contract.id)
            == tostring(contractId) then
            if QPSC_isSharedTeamCompletion(contract) then
                QPSC_say(
                    player,
                    QPSC_I18N.getText(
                        "UI_QPSC_MessageSharedTeamAutomatic"
                    )
                )
                return
            end

            local participant =
                QPSC_findParticipant(
                    contract,
                    participantUsername
                )

            if participant == nil then
                QPSC_say(
                    player,
                    QPSC_I18N.getText(
                        "UI_QPSC_MessageParticipantNotFound"
                    )
                )
                return
            end

            if QPSC_isGlobalCompletion(contract)
                and contract.globalCompleted == true then
                QPSC_say(
                    player,
                    QPSC_I18N.getText(
                        "UI_QPSC_MessageGlobalAlreadyCompleted",
                        tostring(contract.globalCompletedBy or "")
                    )
                )
                return
            end

            local now = QPSC_getWorldAgeHours()
            local reviewer =
                QPSC_getPlayerUsername(player)
            local hasFirstFinisherBonus =
                tostring(
                    contract.firstFinisherBonusItemFullType or ""
                ) ~= ""
                and QPSC_normalizePositiveInteger(
                    contract.firstFinisherBonusQuantity,
                    100
                ) > 0
            local isFirstFinisher = false

            participant.status = resultStatus
            participant.reviewedBy = reviewer
            participant.reviewedAt = now
            participant.completedAt =
                resultStatus == "Completed"
                and now or 0

            if resultStatus == "Completed" then
                -- QPSC_V122_MANUAL_FIRST_FINISHER_COMPLETION_FIX_V1
                if hasFirstFinisherBonus then
                    if tostring(
                        contract.firstFinisherWinner or ""
                    ) == "" then
                        contract.firstFinisherWinner =
                            tostring(participant.username or "")
                        contract.firstFinisherWonAt = now
                        isFirstFinisher = true
                    elseif QPSC_sameUsername(
                        contract.firstFinisherWinner,
                        participant.username
                    ) then
                        isFirstFinisher = true
                    end
                end

                local localUsername =
                    QPSC_getPlayerUsername(getPlayer())

                if QPSC_sameUsername(
                    participant.username,
                    localUsername
                ) then
                    local rewardSquare = QPSC_getSquareAt(
                        contract.targetX,
                        contract.targetY,
                        contract.targetZ
                    )

                    QPSC_spawnLocalReward(
                        contract,
                        participant,
                        getPlayer(),
                        rewardSquare
                    )

                    if isFirstFinisher then
                        QPSC_spawnLocalFirstFinisherBonus(
                            contract,
                            participant,
                            getPlayer(),
                            rewardSquare
                        )
                    end
                else
                    if tostring(
                        contract.rewardItemFullType or ""
                    ) ~= "" then
                        participant.rewardPending = true
                    end

                    if isFirstFinisher then
                        participant.firstFinisherBonusPending = true
                    end
                end

                QPSC_closeLocalGlobalContract(
                    contract,
                    participant,
                    reviewer
                )
            else
                participant.rewardPending = false
                participant.firstFinisherBonusPending = false
            end

            QPSC_Client.timerMarks[
                QPSC_timerKey(
                    contract.id,
                    participant.username
                )
            ] = nil

            QPSC_recomputeContractStatus(contract)
            QPSC_saveLocalContracts()

            local key =
                resultStatus == "Completed"
                and "UI_QPSC_MessageParticipantCompleted"
                or "UI_QPSC_MessageParticipantNotCompleted"

            QPSC_say(
                player,
                QPSC_I18N.getText(
                    key,
                    tostring(participant.username)
                )
            )
            return
        end
    end

    QPSC_say(
        player,
        QPSC_I18N.getText(
            "UI_QPSC_MessageContractNotFound"
        )
    )
end

-- Compatibility function for any old menu or saved callback.
function QPSC_Client.completeContract(player, contractId)
    QPSC_loadLocalContracts()

    for _, contract in pairs(
        QPSC_Client.contracts or {}
    ) do
        if tostring(contract.id)
            == tostring(contractId) then
            for _, participant in ipairs(
                contract.participants or {}
            ) do
                if participant.status == "Accepted" then
                    QPSC_Client.reviewParticipant(
                        player,
                        contractId,
                        participant.username,
                        "Completed"
                    )
                    return
                end
            end

            QPSC_say(
                player,
                QPSC_I18N.getText(
                    "UI_QPSC_MessageParticipantNotFound"
                )
            )
            return
        end
    end

    QPSC_say(
        player,
        QPSC_I18N.getText(
            "UI_QPSC_MessageContractNotFound"
        )
    )
end

function QPSC_Client.deleteContract(player, contractId)
    if QPSC_isMultiplayerClient() then
        QPSC_sendCommand("DeleteContract", {
            contractId = contractId
        })
        return
    end

    QPSC_loadLocalContracts()

    for index, contract in ipairs(QPSC_Client.contracts or {}) do
        if tostring(contract.id) == tostring(contractId) then
            local deletedId = contract.id

            table.remove(QPSC_Client.contracts, index)

            QPSC_saveLocalContracts()
            QPSC_say(
                player,
                QPSC_I18N.getText("UI_QPSC_MessageContractDeleted",
                    deletedId
                )
            )
            return
        end
    end

    QPSC_say(player, QPSC_I18N.getText("UI_QPSC_MessageContractNotFound"))
end

function QPSC_Client.clearContracts(player)
    if QPSC_isMultiplayerClient() then
        QPSC_sendCommand("ClearContracts", {})
        QPSC_say(player, QPSC_I18N.getText("UI_QPSC_MessageClearing"))
        return
    end

    local data = QPSC_getLocalData()
    data.contracts = {}
    data.nextId = 1

    QPSC_Client.contracts = data.contracts
    QPSC_saveLocalContracts()

    QPSC_say(player, QPSC_I18N.getText("UI_QPSC_MessageAllCleared"))
end

local function QPSC_addDisabledOption(menu, text)
    local option = menu:addOption(text, nil, QPSC_dummy)
    option.notAvailable = true
end

function QPSC_Client.onWorldContextMenu(
    playerNum,
    context,
    worldObjects,
    test
)
    if test then return end
    if context == nil then return end

    if not QPSC_isMultiplayerClient() then
        QPSC_loadLocalContracts()
    end

    local player = getSpecificPlayer(playerNum)
    if player == nil then return end

    local username = QPSC_getPlayerUsername(player)

    local mainOption = context:addOption(
        QPSC_I18N.getText(
            "UI_QPSC_MenuSurvivorContracts"
        ),
        player,
        QPSC_dummy
    )
    local mainMenu = ISContextMenu:getNew(context)
    context:addSubMenu(mainOption, mainMenu)

    mainMenu:addOption(
        QPSC_I18N.getText(
            "UI_QPSC_RefreshContracts"
        ),
        player,
        QPSC_Client.requestContracts
    )
    mainMenu:addOption(
        QPSC_I18N.getText(
            "UI_QPSC_ViewContracts"
        ),
        player,
        QPSC_Client.showContracts
    )

    local acceptOption = mainMenu:addOption(
        QPSC_I18N.getText(
            "UI_QPSC_AcceptContract"
        ),
        player,
        QPSC_dummy
    )
    local acceptMenu = ISContextMenu:getNew(context)
    mainMenu:addSubMenu(acceptOption, acceptMenu)

    local activeContract =
        QPSC_findActiveContractForPlayer(player)
    local hasAvailable = false

    if activeContract then
        QPSC_addDisabledOption(
            acceptMenu,
            QPSC_I18N.getText(
                "UI_QPSC_MessageActiveContractExists",
                QPSC_contractLabel(activeContract)
            )
        )
    else
        for _, contract in pairs(
            QPSC_Client.contracts or {}
        ) do
            local existingParticipant =
                QPSC_findParticipant(
                    contract,
                    username
                )
            local alreadyJoined =
                existingParticipant ~= nil
                and tostring(
                    existingParticipant.status or ""
                ) ~= "Cancelled"

            if contract.closed ~= true
                and not alreadyJoined then
                hasAvailable = true

                acceptMenu:addOption(
                    QPSC_contractLabel(contract),
                    player,
                    QPSC_Client.acceptContract,
                    contract.id
                )
            end
        end

        if not hasAvailable then
            QPSC_addDisabledOption(
                acceptMenu,
                QPSC_I18N.getText(
                    "UI_QPSC_NoOpenContracts"
                )
            )
        end
    end

    if activeContract then
        mainMenu:addOption(
            QPSC_I18N.getText(
                "UI_QPSC_CancelActiveContract"
            ) .. ": " .. QPSC_contractLabel(activeContract),
            player,
            QPSC_Client.cancelContract,
            activeContract.id
        )
    else
        QPSC_addDisabledOption(
            mainMenu,
            QPSC_I18N.getText(
                "UI_QPSC_NoActiveContractToCancel"
            )
        )
    end

    local hasActiveDelivery = false
    if activeContract ~= nil then
        if QPSC_isMultiObjective(activeContract) then
            local activeParticipant = QPSC_findParticipant(
                activeContract,
                QPSC_getPlayerUsername(player)
            )
            if activeParticipant ~= nil then
                for _, objective in ipairs(activeContract.objectives or {}) do
                    if tostring(objective.type or "") == "DELIVERY"
                        and QPSC_getMultiProgress(
                            activeContract,
                            activeParticipant,
                            objective
                        ) < math.max(1, tonumber(objective.target) or 1) then
                        hasActiveDelivery = true
                        break
                    end
                end
            end
        elseif QPSC_normalizeObjectiveType(
            activeContract.objectiveType
        ) == "DELIVERY" then
            hasActiveDelivery = true
        end
    end

    if activeContract ~= nil and hasActiveDelivery then
        mainMenu:addOption(
            QPSC_I18N.getText(
                "UI_QPSC_SubmitActiveDelivery"
            ) .. ": " .. QPSC_contractLabel(activeContract),
            player,
            QPSC_Client.submitDelivery,
            activeContract.id
        )
    end

    if QPSC_isLocalAdmin(player) then
        local adminOption = mainMenu:addOption(
            QPSC_I18N.getText(
                "UI_QPSC_AdminTools"
            ),
            player,
            QPSC_dummy
        )
        local adminMenu =
            ISContextMenu:getNew(context)
        mainMenu:addSubMenu(
            adminOption,
            adminMenu
        )

        local reviewOption = adminMenu:addOption(
            QPSC_I18N.getText(
                "UI_QPSC_ReviewParticipants"
            ),
            player,
            QPSC_dummy
        )
        local reviewMenu =
            ISContextMenu:getNew(context)
        adminMenu:addSubMenu(
            reviewOption,
            reviewMenu
        )

        local hasParticipants = false

        for _, contract in pairs(
            QPSC_Client.contracts or {}
        ) do
            for _, participant in ipairs(
                contract.participants or {}
            ) do
                hasParticipants = true

                local participantOption =
                    reviewMenu:addOption(
                        QPSC_contractLabel(contract)
                            .. " | "
                            .. tostring(
                                participant.username
                            )
                            .. " ["
                            .. QPSC_participantStatusText(
                                participant,
                                contract
                            )
                            .. "]",
                        player,
                        QPSC_dummy
                    )

                local participantMenu =
                    ISContextMenu:getNew(context)

                reviewMenu:addSubMenu(
                    participantOption,
                    participantMenu
                )

                if QPSC_isSharedTeamCompletion(contract) then
                    QPSC_addDisabledOption(
                        participantMenu,
                        QPSC_I18N.getText(
                            "UI_QPSC_MessageSharedTeamAutomatic"
                        )
                    )
                else
                    participantMenu:addOption(
                        QPSC_I18N.getText(
                            "UI_QPSC_MarkParticipantCompleted"
                        ),
                        player,
                        QPSC_Client.reviewParticipant,
                        contract.id,
                        participant.username,
                        "Completed"
                    )

                    participantMenu:addOption(
                        QPSC_I18N.getText(
                            "UI_QPSC_MarkParticipantNotCompleted"
                        ),
                        player,
                        QPSC_Client.reviewParticipant,
                        contract.id,
                        participant.username,
                        "NotCompleted"
                    )
                end
            end
        end

        if not hasParticipants then
            QPSC_addDisabledOption(
                reviewMenu,
                QPSC_I18N.getText(
                    "UI_QPSC_NoParticipantsToReview"
                )
            )
        end

        local createOption = adminMenu:addOption(
            QPSC_I18N.getText(
                "UI_QPSC_CreateCustomContract"
            ),
            player,
            QPSC_dummy
        )
        local createMenu =
            ISContextMenu:getNew(context)

        adminMenu:addSubMenu(
            createOption,
            createMenu
        )

        for _, categoryDefinition in ipairs(
            QPSC_CATEGORY_MENU
        ) do
            createMenu:addOption(
                QPSC_I18N.getText(
                    categoryDefinition.labelKey
                ),
                player,
                QPSC_TrackedUI.openCustom,
                categoryDefinition.key
            )
        end

        local trackedOption = adminMenu:addOption(
            QPSC_I18N.getText(
                "UI_QPSC_CreateTrackedContract"
            ),
            player,
            QPSC_dummy
        )
        local trackedMenu = ISContextMenu:getNew(context)
        adminMenu:addSubMenu(
            trackedOption,
            trackedMenu
        )

        for _, categoryDefinition in ipairs(
            QPSC_CATEGORY_MENU
        ) do
            trackedMenu:addOption(
                QPSC_I18N.getText(
                    categoryDefinition.labelKey
                ),
                player,
                QPSC_TrackedUI.open,
                categoryDefinition.key
            )
        end

        local multiOption = adminMenu:addOption(
            QPSC_I18N.getText("UI_QPSC_CreateMultiContract"),
            player,
            QPSC_dummy
        )
        local multiMenu = ISContextMenu:getNew(context)
        adminMenu:addSubMenu(multiOption, multiMenu)
        for _, categoryDefinition in ipairs(QPSC_CATEGORY_MENU) do
            multiMenu:addOption(
                QPSC_I18N.getText(categoryDefinition.labelKey),
                player,
                QPSC_MultiObjectiveUI.open,
                categoryDefinition.key
            )
        end

        local deleteOption = adminMenu:addOption(
            QPSC_I18N.getText(
                "UI_QPSC_DeleteContract"
            ),
            player,
            QPSC_dummy
        )
        local deleteMenu =
            ISContextMenu:getNew(context)
        adminMenu:addSubMenu(
            deleteOption,
            deleteMenu
        )

        local hasDeletable = false

        for _, contract in pairs(
            QPSC_Client.contracts or {}
        ) do
            hasDeletable = true
            deleteMenu:addOption(
                QPSC_contractLabel(contract),
                player,
                QPSC_Client.deleteContract,
                contract.id
            )
        end

        if not hasDeletable then
            QPSC_addDisabledOption(
                deleteMenu,
                QPSC_I18N.getText(
                    "UI_QPSC_NoContractsToDelete"
                )
            )
        end

        adminMenu:addOption(
            QPSC_I18N.getText(
                "UI_QPSC_ClearAllContracts"
            ),
            player,
            QPSC_Client.clearContracts
        )
    end
end

local function QPSC_resolveServerText(args)
    args = args or {}
    local message = args.text

    if args.key and args.key ~= "" then
        if args.arg3 ~= nil then
            message = QPSC_I18N.getText(
                tostring(args.key),
                args.arg1,
                args.arg2,
                args.arg3
            )
        elseif args.arg2 ~= nil then
            message = QPSC_I18N.getText(
                tostring(args.key),
                args.arg1,
                args.arg2
            )
        elseif args.arg1 ~= nil then
            message = QPSC_I18N.getText(
                tostring(args.key),
                args.arg1
            )
        else
            message = QPSC_I18N.getText(
                tostring(args.key)
            )
        end
    end

    return message
        or QPSC_I18N.getText(
            "UI_QPSC_MessageFallback"
        )
end

function QPSC_Client.onServerCommand(module, command, args)
    if module ~= MODULE then return end

    args = args or {}

    if command == "ContractsData" then
        QPSC_Client.serverAdminAuthorized =
            args.adminAuthorized == true
        QPSC_Client.serverAdminKnown = true
        QPSC_Client.contracts = args.contracts or {}

        if QPSC_MapMarkers ~= nil
            and QPSC_MapMarkers.markDirty ~= nil then
            QPSC_MapMarkers.markDirty()
        end

        if QPSC_Client.pendingOpenPlayerNum ~= nil then
            local playerNum =
                tonumber(QPSC_Client.pendingOpenPlayerNum) or 0
            QPSC_Client.pendingOpenPlayerNum = nil

            local player = getSpecificPlayer(playerNum)
            if player == nil then player = getPlayer() end

            QPSC_openContractsWindowNow(player)
        end

        return
    end

    if command == "ConsumeDeliveryItems" then
        QPSC_consumeAuthorizedDelivery(args)
        return
    end

    if command == "Announcement" then
        QPSC_announce(
            getPlayer(),
            QPSC_resolveServerText(args)
        )
        return
    end

    if command == "Message" then
        QPSC_say(
            getPlayer(),
            QPSC_resolveServerText(args)
        )
        return
    end
end


-- QPSC_V122_ZOMBIE_KILL_ATTRIBUTION_HARDENING_V1
local function QPSC_getZombieObjectiveCoordinates(zombie)
    if zombie == nil then return nil, nil end

    if zombie.getSquare ~= nil then
        local squareOk, square = pcall(function()
            return zombie:getSquare()
        end)

        if squareOk
            and square ~= nil
            and square.getX ~= nil
            and square.getY ~= nil then
            local positionOk, squareX, squareY = pcall(function()
                return square:getX(), square:getY()
            end)

            squareX = tonumber(squareX)
            squareY = tonumber(squareY)

            if positionOk
                and squareX ~= nil
                and squareY ~= nil
                and squareX == squareX
                and squareY == squareY then
                return squareX, squareY
            end
        end
    end

    if zombie.getX ~= nil and zombie.getY ~= nil then
        local ok, zombieX, zombieY = pcall(function()
            return zombie:getX(), zombie:getY()
        end)

        zombieX = tonumber(zombieX)
        zombieY = tonumber(zombieY)

        if ok
            and zombieX ~= nil
            and zombieY ~= nil
            and zombieX == zombieX
            and zombieY == zombieY then
            return zombieX, zombieY
        end
    end

    return nil, nil
end

-- QPSC_V053_ZOMBIE_EVENT_DEDUP_V1
local function QPSC_claimZombieContractCredit(zombie)
    if zombie == nil or zombie.getModData == nil then
        return true
    end

    local ok, claimed = pcall(function()
        local modData = zombie:getModData()

        if modData == nil then return false end
        if modData.QPSC_KillCredited_v053 == true then
            return true
        end

        modData.QPSC_KillCredited_v053 = true
        return false
    end)

    if not ok then return true end
    return claimed ~= true
end

local function QPSC_getZombieKiller(zombie)
    if zombie == nil then return nil end

    for _, methodName in ipairs({
        "getAttackedBy",
        "getLastHitCharacter"
    }) do
        local method = zombie[methodName]

        if method ~= nil then
            local ok, candidate = pcall(function()
                return method(zombie)
            end)

            if ok and candidate ~= nil
                and candidate.getUsername ~= nil then
                return candidate
            end
        end
    end

    -- Solo/local fallback: only the local survivor can receive credit,
    -- and only while very close to the dead zombie.
    local localPlayer = getPlayer and getPlayer() or nil
    local zombieX, zombieY =
        QPSC_getZombieObjectiveCoordinates(zombie)

    if localPlayer == nil
        or zombieX == nil
        or zombieY == nil then
        return nil
    end

    local positionOk, playerX, playerY =
        pcall(function()
            return localPlayer:getX(),
                localPlayer:getY()
        end)

    playerX = tonumber(playerX)
    playerY = tonumber(playerY)

    if not positionOk
        or playerX == nil
        or playerY == nil then
        return nil
    end

    local dx = playerX - zombieX
    local dy = playerY - zombieY

    if ((dx * dx) + (dy * dy)) <= 144 then
        return localPlayer
    end

    return nil
end

-- QPSC_V053_SOLO_REWARD_POSITION_SYNC_V1
local function QPSC_reportCurrentRewardPosition(
    player,
    contract,
    participant
)
    if player == nil then return false end

    local ok, x, y, z = pcall(function()
        return player:getX(),
            player:getY(),
            player:getZ()
    end)

    if not ok or x == nil or y == nil or z == nil then
        return false
    end

    local contractId = ""
    local expectedProgress = 0

    if contract ~= nil and participant ~= nil then
        contractId = tostring(contract.id or "")
        expectedProgress =
            (tonumber(participant.progress) or 0) + 1
    end

    return QPSC_sendCommand(
        "ReportRewardPosition",
        {
            x = tonumber(x) or 0,
            y = tonumber(y) or 0,
            z = tonumber(z) or 0,
            contractId = contractId,
            expectedProgress = expectedProgress
        }
    )
end

-- QPSC_V054_LOCATION_RESTRICTED_ZOMBIE_HUNTS_V1
local function QPSC_isZombieInsideKillZone(zombie, contract)
    local radius = tonumber(
        contract and contract.objectiveRadius
    ) or 0

    -- Existing v0.5.3 Zombie Hunt contracts used radius 0.
    -- Keep those contracts unrestricted for save compatibility.
    if radius < 1 then return true end
    if zombie == nil or contract == nil then return false end

    local zombieX, zombieY =
        QPSC_getZombieObjectiveCoordinates(zombie)

    if zombieX == nil or zombieY == nil then
        return false
    end

    local dx = zombieX
        - (tonumber(contract.targetX) or 0)
    local dy = zombieY
        - (tonumber(contract.targetY) or 0)

    return (dx * dx) + (dy * dy)
        <= (radius * radius)
end

-- QPSC_V122_CLIENT_VERIFIED_MULTI_KILL_FALLBACK_V1
local QPSC_MULTI_KILL_REPORT_DELAY_MS = 900
local QPSC_MULTI_KILL_REPORT_RETRY_MS = 1000
local QPSC_MULTI_KILL_REPORT_MAX_ATTEMPTS = 5

local function QPSC_findIncompleteMultiKillObjective(
    contract,
    participant,
    zombie
)
    if contract == nil
        or participant == nil
        or not QPSC_isMultiObjective(contract) then
        return nil
    end

    local zombieX, zombieY =
        QPSC_getZombieObjectiveCoordinates(zombie)

    if zombieX == nil or zombieY == nil then
        return nil
    end

    for _, objective in ipairs(contract.objectives or {}) do
        if tostring(objective.type or "") == "KILL"
            and QPSC_getMultiProgress(
                contract,
                participant,
                objective
            ) < math.max(
                1,
                tonumber(objective.target) or 1
            ) then
            local dx = zombieX
                - (tonumber(objective.targetX) or 0)
            local dy = zombieY
                - (tonumber(objective.targetY) or 0)
            local radius = math.max(
                1,
                tonumber(objective.radius) or 100
            )

            if ((dx * dx) + (dy * dy))
                <= (radius * radius) then
                return objective
            end
        end
    end

    return nil
end

local function QPSC_countQueuedMultiKillReports(
    contractId,
    objectiveId
)
    local count = 0

    for _, report in pairs(
        QPSC_Client.pendingZombieKillReports or {}
    ) do
        if tostring(report.contractId or "")
                == tostring(contractId or "")
            and tostring(report.objectiveId or "")
                == tostring(objectiveId or "") then
            count = count + 1
        end
    end

    return count
end

local function QPSC_queueMultiZombieKillReport(
    player,
    contract,
    participant,
    objective,
    zombie
)
    if player == nil
        or contract == nil
        or participant == nil
        or objective == nil
        or zombie == nil then
        return false
    end

    local zombieX, zombieY =
        QPSC_getZombieObjectiveCoordinates(zombie)
    local zombieZ = 0

    if zombie.getSquare ~= nil then
        pcall(function()
            local square = zombie:getSquare()
            if square ~= nil and square.getZ ~= nil then
                zombieZ = tonumber(square:getZ()) or 0
            end
        end)
    elseif zombie.getZ ~= nil then
        pcall(function()
            zombieZ = tonumber(zombie:getZ()) or 0
        end)
    end

    if zombieX == nil or zombieY == nil then
        return false
    end

    local contractId = tostring(contract.id or "")
    local objectiveId = tostring(objective.id or "")
    local currentProgress =
        QPSC_getMultiProgress(
            contract,
            participant,
            objective
        )
    local queuedAhead =
        QPSC_countQueuedMultiKillReports(
            contractId,
            objectiveId
        )

    QPSC_Client.nextZombieKillReportSequence =
        QPSC_Client.nextZombieKillReportSequence + 1

    local sequence =
        QPSC_Client.nextZombieKillReportSequence
    local reportKey =
        contractId
        .. "|"
        .. objectiveId
        .. "|"
        .. tostring(sequence)

    QPSC_Client.pendingZombieKillReports[reportKey] = {
        reportKey = reportKey,
        contractId = contractId,
        objectiveId = objectiveId,
        expectedProgress =
            math.max(
                0,
                math.floor(
                    tonumber(currentProgress) or 0
                )
            )
            + queuedAhead,
        zombieX = zombieX,
        zombieY = zombieY,
        zombieZ = zombieZ,
        sendAt =
            QPSC_realTimeMs()
            + QPSC_MULTI_KILL_REPORT_DELAY_MS,
        attempts = 0
    }

    return true
end

local function QPSC_processPendingZombieKillReports()
    if not QPSC_isMultiplayerClient() then
        QPSC_Client.pendingZombieKillReports = {}
        return
    end

    local now = QPSC_realTimeMs()
    local removeKeys = {}

    for reportKey, report in pairs(
        QPSC_Client.pendingZombieKillReports or {}
    ) do
        if now >= (tonumber(report.sendAt) or 0) then
            local sent = QPSC_sendCommand(
                "ReportMultiZombieKill",
                {
                    contractId =
                        tostring(report.contractId or ""),
                    objectiveId =
                        tostring(report.objectiveId or ""),
                    expectedProgress =
                        tonumber(report.expectedProgress) or 0,
                    zombieX =
                        tonumber(report.zombieX) or 0,
                    zombieY =
                        tonumber(report.zombieY) or 0,
                    zombieZ =
                        tonumber(report.zombieZ) or 0
                }
            )

            report.attempts =
                (tonumber(report.attempts) or 0) + 1

            if sent
                or report.attempts
                    >= QPSC_MULTI_KILL_REPORT_MAX_ATTEMPTS then
                table.insert(removeKeys, reportKey)
            else
                report.sendAt =
                    now + QPSC_MULTI_KILL_REPORT_RETRY_MS
            end
        end
    end

    for _, reportKey in ipairs(removeKeys) do
        QPSC_Client.pendingZombieKillReports[
            reportKey
        ] = nil
    end
end

local function QPSC_onLocalZombieDead(zombie)
    local player = QPSC_getZombieKiller(zombie)
    if player == nil then return end

    if QPSC_isMultiplayerClient() then
        local localPlayer = getPlayer and getPlayer() or nil

        if localPlayer ~= nil
            and QPSC_sameUsername(
                QPSC_getPlayerUsername(localPlayer),
                QPSC_getPlayerUsername(player)
            ) then
            local contract, participant =
                QPSC_findActiveContractForPlayer(localPlayer)

            if contract ~= nil
                and participant ~= nil
                and QPSC_isMultiObjective(contract) then
                local objective =
                    QPSC_findIncompleteMultiKillObjective(
                        contract,
                        participant,
                        zombie
                    )

                if objective ~= nil then
                    QPSC_queueMultiZombieKillReport(
                        localPlayer,
                        contract,
                        participant,
                        objective,
                        zombie
                    )
                end
            elseif contract ~= nil
                and participant ~= nil
                and QPSC_normalizeObjectiveType(
                    contract.objectiveType
                ) == "KILL" then
                if QPSC_isZombieInsideKillZone(
                    zombie,
                    contract
                ) then
                    QPSC_reportCurrentRewardPosition(
                        localPlayer,
                        contract,
                        participant
                    )
                end
            else
                -- The server may have completed the contract before the
                -- client event runs. A generic fresh position lets the
                -- pending reward finish at the current player square.
                QPSC_reportCurrentRewardPosition(localPlayer)
            end
        end

        return
    end

    QPSC_loadLocalContracts()
    local contract, participant =
        QPSC_findActiveContractForPlayer(player)

    if contract ~= nil and participant ~= nil and QPSC_isMultiObjective(contract) then
        local objective=nil
        for _,candidate in ipairs(contract.objectives or {}) do
            if tostring(candidate.type or "") == "KILL"
                and QPSC_getMultiProgress(
                    contract,
                    participant,
                    candidate
                ) < math.max(
                    1,
                    tonumber(candidate.target) or 1
                ) then
                local zombieX, zombieY =
                    QPSC_getZombieObjectiveCoordinates(zombie)

                if zombieX ~= nil and zombieY ~= nil then
                    local dx = zombieX
                        - (tonumber(candidate.targetX) or 0)
                    local dy = zombieY
                        - (tonumber(candidate.targetY) or 0)
                    local radius = math.max(
                        1,
                        tonumber(candidate.radius) or 100
                    )

                    if ((dx * dx) + (dy * dy))
                        <= (radius * radius) then
                        objective = candidate
                        break
                    end
                end
            end
        end
        if objective==nil or not QPSC_claimZombieContractCredit(zombie) then return end
        local square=nil; if zombie.getSquare then local ok,value=pcall(function() return zombie:getSquare() end); if ok then square=value end end
        QPSC_advanceLocalMultiObjective(contract,participant,objective,1,player,square or QPSC_getPlayerSquare(player),"Multi-Objective Zombie Hunt"); return
    end

    if contract == nil or participant == nil
        or QPSC_normalizeObjectiveType(
            contract.objectiveType
        ) ~= "KILL" then
        return
    end

    if not QPSC_isZombieInsideKillZone(
        zombie,
        contract
    ) then
        return
    end

    if not QPSC_claimZombieContractCredit(zombie) then
        return
    end

    local target = math.max(
        1,
        tonumber(contract.objectiveTarget) or 1
    )

    if QPSC_isSharedTeamCompletion(contract) then
        participant.progress =
            (tonumber(participant.progress) or 0) + 1
        contract.sharedProgress = math.min(
            target,
            (tonumber(contract.sharedProgress) or 0) + 1
        )

        if contract.sharedProgress >= target then
            local square = nil

            if zombie.getSquare ~= nil then
                local ok, value = pcall(function()
                    return zombie:getSquare()
                end)
                if ok then square = value end
            end

            QPSC_closeLocalSharedTeamContract(
                contract,
                participant,
                player,
                square or QPSC_getPlayerSquare(player),
                "Zombie Hunt"
            )
        else
            QPSC_saveLocalContracts()
            QPSC_say(
                player,
                QPSC_I18N.getText(
                    "UI_QPSC_MessageSharedTeamProgress",
                    contract.sharedProgress,
                    target,
                    participant.progress
                )
            )
        end

        return
    end

    participant.progress = math.min(
        target,
        (tonumber(participant.progress) or 0) + 1
    )

    if participant.progress >= target then
        local square = nil

        if zombie.getSquare ~= nil then
            local ok, value = pcall(function()
                return zombie:getSquare()
            end)
            if ok then square = value end
        end

        QPSC_completeLocalTracked(
            contract,
            participant,
            player,
            square or QPSC_getPlayerSquare(player),
            "Zombie Hunt"
        )
    else
        QPSC_saveLocalContracts()
        QPSC_say(
            player,
            QPSC_I18N.getText(
                "UI_QPSC_MessageKillProgress",
                participant.progress,
                target
            )
        )
    end
end

local function QPSC_updateLocalLocationObjective()
    if not QPSC_isExplicitSinglePlayerRuntime() then return false end

    local player = getPlayer()
    if player == nil then return false end

    local contract, participant =
        QPSC_findActiveContractForPlayer(player)

    if contract ~= nil and participant ~= nil and QPSC_isMultiObjective(contract) then
        local changed=false
        for _,objective in ipairs(contract.objectives or {}) do
            if tostring(objective.type or "")=="LOCATION" and QPSC_getMultiProgress(contract,participant,objective)<1 and QPSC_isPlayerNearMultiObjective(player,objective) then
                changed=QPSC_advanceLocalMultiObjective(contract,participant,objective,1,player,QPSC_getPlayerSquare(player),"Multi-Objective Location") or changed
                if tostring(participant.status or "")~="Accepted" then break end
            end
        end
        return changed
    end

    if contract ~= nil and participant ~= nil
        and QPSC_normalizeObjectiveType(
            contract.objectiveType
        ) == "LOCATION"
        and QPSC_isPlayerNearTarget(player, contract) then
        participant.progress = 1
        return QPSC_completeLocalTracked(
            contract,
            participant,
            player,
            QPSC_getPlayerSquare(player),
            "Location"
        )
    end

    return false
end

local function QPSC_onLocalMinute()
    if QPSC_isMultiplayerClient() then return end

    local data = QPSC_getLocalData()

    if QPSC_updateLocalParticipantTimers(data) then
        QPSC_Client.contracts = data.contracts or {}
        QPSC_saveLocalContracts()
    end

    QPSC_updateLocalLocationObjective()

    local player = getPlayer()
    if player ~= nil then
        local username = QPSC_getPlayerUsername(player)

        for _, contract in ipairs(
            QPSC_Client.contracts or {}
        ) do
            local participant =
                QPSC_findParticipant(contract, username)

            if participant ~= nil then
                local changedReward = false

                if participant.rewardPending == true
                    and participant.rewardGranted ~= true then
                    QPSC_spawnLocalReward(
                        contract,
                        participant,
                        player,
                        QPSC_getPlayerSquare(player)
                    )
                    changedReward = true
                end

                if participant.firstFinisherBonusPending == true
                    and participant.firstFinisherBonusGranted ~= true
                    and QPSC_sameUsername(
                        contract.firstFinisherWinner,
                        participant.username
                    ) then
                    QPSC_spawnLocalFirstFinisherBonus(
                        contract,
                        participant,
                        player,
                        QPSC_getPlayerSquare(player)
                    )
                    changedReward = true
                end

                local primaryReputationPath =
                    QPSC_normalizeReputationPath(
                        contract.reputationPath
                    )
                local primaryReputationPoints =
                    QPSC_normalizePositiveInteger(
                        contract.reputationPoints,
                        100000
                    )
                local secondaryReputationPath =
                    QPSC_normalizeReputationPath(
                        contract.secondaryReputationPath
                    )
                local secondaryReputationPoints =
                    QPSC_normalizePositiveInteger(
                        contract.secondaryReputationPoints,
                        100000
                    )
                local hasConfiguredReputation =
                    (
                        primaryReputationPath ~= ""
                        and primaryReputationPoints > 0
                    )
                    or (
                        secondaryReputationPath ~= ""
                        and secondaryReputationPoints > 0
                    )

                if tostring(participant.status or "")
                        == "Completed"
                    and hasConfiguredReputation
                    and participant.reputationRewardResolved
                        ~= true then
                    local beforeAttempts =
                        tonumber(
                            participant.reputationRewardAttempts
                        ) or 0

                    QPSC_awardLocalContractReputation(
                        contract,
                        participant,
                        "QPSurvivorContracts Recovery"
                    )

                    if beforeAttempts
                        ~= (
                            tonumber(
                                participant.reputationRewardAttempts
                            ) or 0
                        ) then
                        changedReward = true
                    end
                end

                if changedReward then
                    QPSC_saveLocalContracts()
                end
            end
        end
    end
end

if QPSC_Client.coreEventsRegisteredV070 ~= true then
    QPSC_Client.coreEventsRegisteredV070 = true
    Events.OnFillWorldObjectContextMenu.Add(QPSC_Client.onWorldContextMenu)
    Events.OnServerCommand.Add(QPSC_Client.onServerCommand)

    if Events.OnZombieDead then
        Events.OnZombieDead.Add(QPSC_onLocalZombieDead)
    end

    if Events.EveryOneMinute then
        Events.EveryOneMinute.Add(QPSC_onLocalMinute)
    end
end

local QPSC_INITIAL_SYNC_RETRY_MS = 1000

local function QPSC_requestInitialServerSync()
    local player = getPlayer and getPlayer() or nil

    if player ~= nil then
        QPSC_reportCurrentRewardPosition(player)
    end

    local sent = QPSC_sendCommand("RequestContracts", {})

    if sent then
        QPSC_Client.pendingInitialServerSync = false
        QPSC_Client.nextInitialServerSyncRetryMs = 0
        return true
    end

    if QPSC_isRemoteMultiplayerRuntime() then
        QPSC_Client.pendingInitialServerSync = true
        QPSC_Client.nextInitialServerSyncRetryMs =
            QPSC_realTimeMs() + QPSC_INITIAL_SYNC_RETRY_MS
    end

    return false
end

-- QPSC_V123_IMMEDIATE_REACH_LOCATION_V1
local QPSC_IMMEDIATE_LOCATION_SCAN_MS = 250
local QPSC_IMMEDIATE_LOCATION_RETRY_MS = 1000

local function QPSC_checkImmediateLocationObjective()
    if not QPSC_isMultiplayerClient() then
        return
    end

    local now = QPSC_realTimeMs()
    local nextCheck =
        tonumber(
            QPSC_Client.nextImmediateLocationCheckMs
        ) or 0

    if now < nextCheck then
        return
    end

    QPSC_Client.nextImmediateLocationCheckMs =
        now + QPSC_IMMEDIATE_LOCATION_SCAN_MS

    local player = getPlayer and getPlayer() or nil

    if player == nil then
        return
    end

    local contract, participant =
        QPSC_findActiveContractForPlayer(player)

    if contract == nil
        or participant == nil
        or tostring(participant.status or "")
            ~= "Accepted" then
        QPSC_Client.lastImmediateLocationKey = ""
        return
    end

    local contractId = tostring(contract.id or "")
    local objectiveId = ""
    local inside = false

    if QPSC_isMultiObjective(contract) then
        for _, objective in ipairs(
            contract.objectives or {}
        ) do
            if tostring(objective.type or "")
                    == "LOCATION"
                and QPSC_getMultiProgress(
                    contract,
                    participant,
                    objective
                ) < math.max(
                    1,
                    tonumber(objective.target) or 1
                )
                and QPSC_isPlayerNearMultiObjective(
                    player,
                    objective
                ) then
                objectiveId =
                    tostring(objective.id or "")
                inside = true
                break
            end
        end
    elseif QPSC_normalizeObjectiveType(
            contract.objectiveType
        ) == "LOCATION"
        and QPSC_isPlayerNearTarget(
            player,
            contract
        ) then
        inside = true
    end

    if not inside or contractId == "" then
        QPSC_Client.lastImmediateLocationKey = ""
        return
    end

    local key =
        contractId .. "|" .. objectiveId

    if QPSC_Client.lastImmediateLocationKey == key
        and now < (
            tonumber(
                QPSC_Client.nextImmediateLocationRetryMs
            ) or 0
        ) then
        return
    end

    local sent = QPSC_sendCommand(
        "ReportImmediateLocation",
        {
            contractId = contractId,
            objectiveId = objectiveId
        }
    )

    if sent then
        QPSC_Client.lastImmediateLocationKey = key
        QPSC_Client.nextImmediateLocationRetryMs =
            now + QPSC_IMMEDIATE_LOCATION_RETRY_MS
    end
end

local function QPSC_retryInitialServerSync()
    if QPSC_Client.pendingInitialServerSync ~= true then
        return
    end

    local now = QPSC_realTimeMs()
    local retryAt =
        tonumber(QPSC_Client.nextInitialServerSyncRetryMs) or 0

    if now < retryAt then return end

    QPSC_Client.nextInitialServerSyncRetryMs =
        now + QPSC_INITIAL_SYNC_RETRY_MS

    QPSC_requestInitialServerSync()
end

local function QPSC_initializeClientRuntime()
    if QPSC_isMultiplayerClient() then
        QPSC_requestInitialServerSync()
    else
        QPSC_Client.pendingInitialServerSync = false
        QPSC_loadLocalContracts()
    end
end

if QPSC_Client.startupEventsRegisteredV070 ~= true then
    QPSC_Client.startupEventsRegisteredV070 = true

    if Events.OnCreatePlayer then
        Events.OnCreatePlayer.Add(QPSC_initializeClientRuntime)
    end

    if Events.OnGameStart then
        Events.OnGameStart.Add(QPSC_initializeClientRuntime)
    end

    if Events.OnTick then
        Events.OnTick.Add(QPSC_retryInitialServerSync)
        Events.OnTick.Add(
            QPSC_processPendingZombieKillReports
        )
        Events.OnTick.Add(
            QPSC_checkImmediateLocationObjective
        )
    end
end

QPSC_initializeClientRuntime()

print("[QPSC] Build 41 client loaded v1.3.2 Production.")
