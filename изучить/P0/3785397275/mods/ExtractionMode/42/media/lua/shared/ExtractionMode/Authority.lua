require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/Groups"
require "ExtractionMode/Infection"
require "ExtractionMode/RaidOutcomes"
require "ExtractionMode/Upgrades"
require "ExtractionMode/Quests"
require "ExtractionMode/Barters"
require "ExtractionMode/Generator"
require "ExtractionMode/HideoutUtilities"
require "ExtractionMode/Logistics"
require "ExtractionMode/HideoutBenefits"
require "ExtractionMode/BookXP"
require "ExtractionMode/HideoutReading"
require "ExtractionMode/ProjectRemnantsIntegration"
require "ExtractionMode/AnimalExtraction"
require "ExtractionMode/ProgressionBackup"
require "ExtractionMode/Garage"
require "ExtractionMode/ModCompatibility"

-- Multiplayer clients receive snapshots only. Dedicated/listen servers and
-- single-player own all state transitions and validation below.
if isClient and isClient() then return end

require "ExtractionMode/BanditsIntegration"
require "ExtractionMode/GarageAuthority"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Groups = ExtractionMode.Groups
local Infection = ExtractionMode.Infection
local RaidOutcomes = ExtractionMode.RaidOutcomes
local Upgrades = ExtractionMode.Upgrades
local Quests = ExtractionMode.Quests
local Barters = ExtractionMode.Barters
local Generator = ExtractionMode.Generator
local HideoutUtilities = ExtractionMode.HideoutUtilities
local Logistics = ExtractionMode.Logistics
local HideoutBenefits = ExtractionMode.HideoutBenefits
local BookXP = ExtractionMode.BookXP
local BanditsIntegration = ExtractionMode.BanditsIntegration
local ProjectRemnantsIntegration = ExtractionMode.ProjectRemnantsIntegration
local AnimalExtraction = ExtractionMode.AnimalExtraction
local ModCompatibility = ExtractionMode.ModCompatibility
local Server = {}
local store = nil
local RaidRuntime = {
    context = nil,
    contextKey = nil,
    views = {},
    initialized = false,
    teleportReservations = {},
    progressionRecoveryPending = false,
    groundExtractionVehicleRefs = {},
    extractionShoveAt = {},
}
local lastSecond = -1
local lastUtilityRefresh = -1
local lastStateHeartbeat = -1
local boardingProtections = {}
local raidQuestItemRefs = {}
local singleplayerRaidResumePending = false
local singleplayerRaidResumeDeadlineMs = 0
local singleplayerRaidResumeLogged = false
local HIDEOUT_RECONCILE_GRACE_MS = 10000
local DISCONNECTED_RAID_RECONNECT_GRACE_MS = 10 * 60 * 1000

-- Hideout progression and infrastructure remain server-wide. Everything else
-- in this list is intentionally absent: lifecycle writes are routed to the
-- current faction/solo raid instance by the lightweight proxy below.
RaidRuntime.sharedKeys = {
    version = true,
    progressionGeneration = true,
    raidSchemaVersion = true,
    raids = true,
    playerRaidKeys = true,
    factionStagingRaidKeys = true,
    nextRaidId = true,
    nextFactionStagingId = true,
    closedRaidIds = true,
    upgrades = true,
    questProgress = true,
    questLegacyCompletions = true,
    groupRegistry = true,
    questObjectiveProgress = true,
    questVisitRaidIds = true,
    questVisitSuccessfulRaidIds = true,
    questContactTrust = true,
    generatorFuel = true,
    generatorRunning = true,
    generatorLastWorldHour = true,
    availableTownKeys = true,
    pendingDeliveries = true,
    townRotationDay = true,
    townChoicesChangedPending = true,
    ammoDeliveryDay = true,
    medicalDeliveryDay = true,
    deliveryLockerPoint = true,
    coopWelcomeAcknowledged = true,
    personalGarages = true,
    nextGarageVehicleId = true,
    activeHideoutVehicle = true,
    pendingGarageVehicleRemovals = true,
    garageTransactions = true,
    garageBackupGeneration = true,
    garageDoorUnlocked = true,
}

RaidRuntime.lifecycleKeys = {
    "state", "factionKey", "ready", "optedOut", "participants", "extractedPlayers", "returnPending",
    "disconnectedRaidPlayers", "boardingPending", "groundExtractionPending",
    "groundExtractionVehicles",
    "deathRescuePending", "extractionFlareSpawned", "raidQuestItems", "raidId",
    "hordeSpawned", "hordeEventType", "banditRaidIds", "banditEncounterAttempts",
    "flareUpgradePending", "singleplayerRaidPositions", "selectedTownKey",
    "selectedTownBy", "selectedJoinRaidKey", "countdownEndMs", "raidStartHour", "raidStartMs",
    "hordeWindowStartHour", "hordeWindowEndHour", "hordeAtHour", "raidSpawn",
    "extractionSites", "transitPhase", "transitDeadlineMs", "activeExtraction",
    "extractionEndMs", "extractionHelicopterStarted", "boardingEndMs",
    "extractionRope", "boardingExtractedCount", "insertionCleanupUntilMs",
    "lateJoinPending", "lateJoinVehicles", "lateInsertionCleanup", "vehicleInsertion",
    "campaignHandoff", "banditEncounterRaidId", "banditEncounterState",
    "banditEncounterAtHour", "banditEncounterClan", "banditEncounterSpawnedCount",
}

-- Feature modules attach to this authority-only runtime. They are loaded after
-- the client guard above and register no events of their own; Authority.lua
-- remains the sole coordinator for tick and command ordering.
ExtractionMode.RaidRuntime = RaidRuntime
require "ExtractionMode/RaidThreatAuthority"
local Threats = ExtractionMode.RaidThreatAuthority
require "ExtractionMode/HideoutAuthority"
local HideoutAuthority = ExtractionMode.HideoutAuthority
require "ExtractionMode/RaidFlareAuthority"
local RaidFlares = ExtractionMode.RaidFlareAuthority
require "ExtractionMode/RaidLossAuthority"
local RaidLoss = ExtractionMode.RaidLossAuthority
require "ExtractionMode/RaidRouteAuthority"
local RaidRoutes = ExtractionMode.RaidRouteAuthority
require "ExtractionMode/RaidQuestAuthority"
local RaidQuests = ExtractionMode.RaidQuestAuthority
require "ExtractionMode/GarageDoorAuthority"
local GarageDoorAuthority = ExtractionMode.GarageDoorAuthority

local function singleplayerAuthority()
    return not (isServer and isServer()) and not (isClient and isClient())
end

local function activeRaidSpawn(data)
    return data.raidSpawn or Config.raidSpawn()
end

local function activeExtractionSites(data)
    if data.extractionSites and #data.extractionSites > 0 then return data.extractionSites end
    return Config.extractionSites()
end

function RaidRuntime.ensureRaidState(raid, teamKey)
    raid.teamKey = raid.teamKey or teamKey
    raid.state = raid.state or Config.STATE_HIDEOUT
    raid.ready = raid.ready or {}
    raid.optedOut = raid.optedOut or {}
    raid.participants = raid.participants or {}
    raid.extractedPlayers = raid.extractedPlayers or {}
    raid.returnPending = raid.returnPending or {}
    raid.disconnectedRaidPlayers = raid.disconnectedRaidPlayers or {}
    raid.boardingPending = raid.boardingPending or {}
    raid.groundExtractionPending = raid.groundExtractionPending or {}
    raid.groundExtractionVehicles = raid.groundExtractionVehicles or {}
    raid.deathRescuePending = raid.deathRescuePending or {}
    raid.extractionFlareSpawned = raid.extractionFlareSpawned or {}
    raid.raidQuestItems = raid.raidQuestItems or {}
    raid.raidId = tonumber(raid.raidId) or 0
    raid.hordeSpawned = raid.hordeSpawned == true
    if raid.hordeEventType ~= "BANDITS" then raid.hordeEventType = "ZOMBIES" end
    raid.banditRaidIds = raid.banditRaidIds or {}
    raid.banditEncounterAttempts = tonumber(raid.banditEncounterAttempts) or 0
    raid.flareUpgradePending = raid.flareUpgradePending or {}
    raid.singleplayerRaidPositions = raid.singleplayerRaidPositions or {}
    raid.lateJoinPending = raid.lateJoinPending or {}
    raid.lateJoinVehicles = raid.lateJoinVehicles or {}
    raid.lateInsertionCleanup = raid.lateInsertionCleanup or {}
    if type(raid.vehicleInsertion) ~= "table"
        or raid.vehicleInsertion.vehicleId == nil then raid.vehicleInsertion = nil end
    if raid.selectedTownKey and Config.town(raid.selectedTownKey) == nil then
        raid.selectedTownKey = nil
        raid.selectedTownBy = nil
        raid.selectedJoinRaidKey = nil
    end
    if (raid.state == Config.STATE_RAID or raid.state == Config.STATE_EXTRACTING
        or raid.state == Config.STATE_BOARDING) and tonumber(raid.raidStartHour) then
        raid.raidStartMs = tonumber(raid.raidStartMs) or Util.timerNowMs()
        local minimum, maximum = Threats.hordeDelayBounds(raid)
        raid.hordeWindowStartHour = tonumber(raid.hordeWindowStartHour)
            or (raid.raidStartHour + minimum)
        raid.hordeWindowEndHour = tonumber(raid.hordeWindowEndHour)
            or (raid.raidStartHour + maximum)
    end
    return raid
end

function RaidRuntime.migrateLegacyRaid(root, previousVersion)
    if tonumber(root.raidSchemaVersion) and tonumber(root.raidSchemaVersion) >= 1 then return end
    local legacy = {}
    for _, key in ipairs(RaidRuntime.lifecycleKeys) do
        legacy[key] = rawget(root, key)
    end
    RaidRuntime.ensureRaidState(legacy, "legacy")
    local preserve = legacy.state ~= Config.STATE_HIDEOUT
        or legacy.selectedTownKey ~= nil
    -- Build 42's Kahlua environment can expose the Lua global next() as nil.
    -- Scan with pairs() instead so an existing singleton raid is preserved
    -- without making save migration depend on that optional global.
    if not preserve then
        for _ in pairs(legacy.ready) do preserve = true break end
    end
    if not preserve then
        for _ in pairs(legacy.participants) do preserve = true break end
    end
    if not preserve then
        for _ in pairs(legacy.disconnectedRaidPlayers) do preserve = true break end
    end
    -- Do not mutate the persisted singleton until its replacement is complete.
    -- If validation above ever fails, the original save data remains intact for
    -- the next load instead of being left half-migrated.
    for _, key in ipairs(RaidRuntime.lifecycleKeys) do
        rawset(root, key, nil)
    end
    if preserve then
        root.raids.legacy = legacy
        for username, participating in pairs(legacy.participants) do
            if participating == true then root.playerRaidKeys[username] = "legacy" end
        end
        for username, ready in pairs(legacy.ready) do
            if ready == true then root.playerRaidKeys[username] = "legacy" end
        end
        for username in pairs(legacy.disconnectedRaidPlayers) do
            root.playerRaidKeys[username] = "legacy"
        end
        if previousVersion < 2 then
            for username, participating in pairs(legacy.participants) do
                if participating == true then legacy.flareUpgradePending[username] = true end
            end
        end
        Util.log("Migrated legacy singleton raid state into isolated raid instance")
    end
    root.nextRaidId = math.max(tonumber(root.nextRaidId) or 0, tonumber(legacy.raidId) or 0)
    root.raidSchemaVersion = 1
end

function RaidRuntime.getRootStore()
    if store ~= nil and RaidRuntime.initialized then return store end
    if store == nil then
        local existing = ModData.get and ModData.get(Config.DATA_KEY) or nil
        RaidRuntime.progressionRecoveryPending = existing == nil
        store = existing or ModData.getOrCreate(Config.DATA_KEY)
    end
    local previousVersion = tonumber(store.version) or 1
    store.raids = store.raids or {}
    store.playerRaidKeys = store.playerRaidKeys or {}
    store.factionStagingRaidKeys = store.factionStagingRaidKeys or {}
    store.closedRaidIds = store.closedRaidIds or {}
    local legacyLastClosedRaidId = math.max(0, math.floor(tonumber(store.lastClosedRaidId) or 0))
    store.closedRaidIds._through = math.max(
        math.floor(tonumber(store.closedRaidIds._through) or 0), legacyLastClosedRaidId)
    store.lastClosedRaidId = nil
    store.nextRaidId = math.max(0, tonumber(store.nextRaidId) or tonumber(store.raidId) or 0)
    store.nextFactionStagingId = math.max(0, tonumber(store.nextFactionStagingId) or 0)
    store.progressionGeneration = math.max(0,
        math.floor(tonumber(store.progressionGeneration) or 0))
    store.upgrades = store.upgrades or {}
    if store.questProgress == nil then
        store.questProgress = {}
        store.questLegacyCompletions = {}
        for questId, completed in pairs(store.quests or {}) do
            if completed == true then store.questLegacyCompletions[questId] = true end
        end
    end
    store.questLegacyCompletions = store.questLegacyCompletions or {}
    store.quests = nil
    store.groupRegistry = store.groupRegistry or { factionNames = {}, factions = {} }
    store.questObjectiveProgress = store.questObjectiveProgress or {}
    store.questVisitRaidIds = store.questVisitRaidIds or {}
    store.questVisitSuccessfulRaidIds = store.questVisitSuccessfulRaidIds or {}
    store.questContactTrust = store.questContactTrust or {}
    store.coopWelcomeAcknowledged = store.coopWelcomeAcknowledged or {}
    ExtractionMode.Garage.ensureState(store)
    ExtractionMode.GarageAuthority.ensureState(store)
    store.generatorFuel = math.max(0, math.min(Generator.capacity(), tonumber(store.generatorFuel) or 0))
    store.generatorRunning = store.generatorRunning == true and store.generatorFuel > 0.0001
    store.generatorLastWorldHour = tonumber(store.generatorLastWorldHour) or Util.worldHours()
    Logistics.ensureState(store)
    RaidRuntime.migrateLegacyRaid(store, previousVersion)
    for key, raid in pairs(store.raids) do RaidRuntime.ensureRaidState(raid, key) end
    store.version = Config.VERSION
    RaidRuntime.initialized = true
    return store
end

function RaidRuntime.raidView(raid)
    local view = RaidRuntime.views[raid]
    if view ~= nil then return view end
    view = setmetatable({}, {
        __index = function(_, key)
            if RaidRuntime.sharedKeys[key] then return RaidRuntime.getRootStore()[key] end
            return raid[key]
        end,
        __newindex = function(_, key, value)
            if RaidRuntime.sharedKeys[key] then
                RaidRuntime.getRootStore()[key] = value
            else
                raid[key] = value
            end
        end,
    })
    RaidRuntime.views[raid] = view
    return view
end

function RaidRuntime.raidForKey(key)
    local root = RaidRuntime.getRootStore()
    key = tostring(key or "player:singleplayer")
    local raid = root.raids[key]
    if raid == nil then
        raid = RaidRuntime.ensureRaidState({}, key)
        root.raids[key] = raid
    end
    return raid, key
end

function RaidRuntime.isActiveRaid(raid)
    return raid ~= nil and (raid.state == Config.STATE_TRANSIT or raid.state == Config.STATE_RAID
        or raid.state == Config.STATE_EXTRACTING or raid.state == Config.STATE_BOARDING)
end

function RaidRuntime.isJoinableRaid(raid)
    return raid ~= nil and (raid.state == Config.STATE_RAID
        or raid.state == Config.STATE_EXTRACTING)
end

function RaidRuntime.createFactionStagingRaid(factionKey)
    local root = RaidRuntime.getRootStore()
    root.nextFactionStagingId = (tonumber(root.nextFactionStagingId) or 0) + 1
    local key = tostring(factionKey) .. ":staging:" .. tostring(root.nextFactionStagingId)
    local raid = RaidRuntime.raidForKey(key)
    raid.factionKey = tostring(factionKey)
    root.factionStagingRaidKeys[tostring(factionKey)] = key
    Util.log("Opened faction staging lobby " .. tostring(key))
    return raid, key
end

function RaidRuntime.stagingRaidForFaction(factionKey)
    factionKey = tostring(factionKey)
    local root = RaidRuntime.getRootStore()
    local key = root.factionStagingRaidKeys[factionKey]
    local raid = key and root.raids[key] or nil
    if raid == nil then
        local primary = root.raids[factionKey]
        if primary == nil or not RaidRuntime.isActiveRaid(primary) then
            raid, key = RaidRuntime.raidForKey(factionKey)
            raid.factionKey = factionKey
            root.factionStagingRaidKeys[factionKey] = key
        else
            raid, key = RaidRuntime.createFactionStagingRaid(factionKey)
        end
    elseif RaidRuntime.isActiveRaid(raid) then
        raid, key = RaidRuntime.createFactionStagingRaid(factionKey)
    else
        raid.factionKey = factionKey
    end
    return raid, key
end

function RaidRuntime.rotateFactionStagingRaid(data)
    local factionKey = data and data.factionKey
    if factionKey == nil then return end
    local root = RaidRuntime.getRootStore()
    if tostring(root.factionStagingRaidKeys[tostring(factionKey)])
        == tostring(data.teamKey) then
        RaidRuntime.createFactionStagingRaid(factionKey)
    end
end

function RaidRuntime.raidForPlayer(player)
    local root = RaidRuntime.getRootStore()
    local username = Util.username(player)
    local owner = Groups.forPlayer(player, root.groupRegistry)
    local assignedKey = username ~= "" and root.playerRaidKeys[username] or nil
    local assignedRaid = assignedKey ~= nil and root.raids[assignedKey] or nil

    -- Global mod data can contain several historical raid records. During a
    -- single-player load, an old hideout assignment may survive even though the
    -- same username is still a participant in one active raid. Resolve that
    -- active membership first so RequestState cannot teleport a correctly loaded
    -- field position back to the hideout.
    if singleplayerAuthority() and username ~= ""
        and (assignedRaid == nil or assignedRaid.state == Config.STATE_HIDEOUT) then
        local activeRaid, activeKey = nil, nil
        for key, candidate in pairs(root.raids) do
            local active = candidate.state == Config.STATE_RAID
                or candidate.state == Config.STATE_EXTRACTING
                or candidate.state == Config.STATE_BOARDING
            if active and candidate.participants and candidate.participants[username] == true then
                if activeRaid ~= nil and activeRaid ~= candidate then
                    activeRaid, activeKey = nil, nil
                    break
                end
                activeRaid, activeKey = candidate, key
            end
        end
        if activeRaid ~= nil then
            root.playerRaidKeys[username] = tostring(activeKey)
            Util.log("Recovered active single-player raid assignment for " .. tostring(username)
                .. " (" .. tostring(assignedKey) .. " -> " .. tostring(activeKey) .. ")")
            return activeRaid, activeKey
        end
    end

    if assignedRaid ~= nil then
        -- A persisted assignment is only authoritative while its raid is active.
        -- Once it is back in the hideout, current faction/local-coop ownership must
        -- win. Otherwise split-screen players can resolve to different raid tables:
        -- SelectTown updates one and the next broadcast overwrites the shared HUD
        -- with the other table's nil destination.
        if assignedRaid.state ~= Config.STATE_HIDEOUT then
            if owner.kind == "faction" then assignedRaid.factionKey = tostring(owner.key) end
            return assignedRaid, assignedKey
        elseif owner.kind ~= "faction" and tostring(assignedKey) == tostring(owner.key) then
            return assignedRaid, assignedKey
        end
        root.playerRaidKeys[username] = nil
        Util.log("Released stale hideout raid assignment for " .. tostring(username)
            .. " (" .. tostring(assignedKey) .. " -> " .. tostring(owner.key) .. ")")
    elseif assignedKey ~= nil then
        root.playerRaidKeys[username] = nil
    end
    if owner.kind == "faction" then
        -- Every unassigned faction member shares the current staging lobby. When
        -- that lobby deploys, it rotates immediately so opt-outs, returning raid
        -- teams, and first-time late joiners can form the next party together.
        return RaidRuntime.stagingRaidForFaction(owner.key)
    end
    return RaidRuntime.raidForKey(owner.key)
end

function RaidRuntime.dataForPlayer(player)
    local raid = RaidRuntime.raidForPlayer(player)
    return RaidRuntime.raidView(raid)
end

function RaidRuntime.activeFactionRaids(player)
    local result = {}
    local root = RaidRuntime.getRootStore()
    local username = Util.username(player)
    local owner = Groups.forPlayer(player, root.groupRegistry)
    if owner.kind ~= "faction" then return result end
    for key, raid in pairs(root.raids) do
        local factionKey = raid.factionKey
        if factionKey == nil and tostring(key) == tostring(owner.key) then
            factionKey = owner.key
            raid.factionKey = tostring(owner.key)
        end
        if tostring(factionKey) == tostring(owner.key)
            and RaidRuntime.isJoinableRaid(raid)
            and raid.participants[username] ~= true then
            result[#result + 1] = { key = tostring(key), raid = raid }
        end
    end
    table.sort(result, function(left, right)
        local leftId = tonumber(left.raid.raidId) or 0
        local rightId = tonumber(right.raid.raidId) or 0
        if leftId == rightId then return left.key < right.key end
        return leftId < rightId
    end)
    return result
end

local function getStore()
    if RaidRuntime.context ~= nil then return RaidRuntime.raidView(RaidRuntime.context) end
    local raid = RaidRuntime.raidForKey("server")
    return RaidRuntime.raidView(raid)
end

function RaidRuntime.currentStore()
    return getStore()
end

local function questStateFor(data, player)
    local owner = Groups.forPlayer(player, data.groupRegistry)
    local completions = data.questProgress[owner.key]
    if completions == nil then
        completions = {}
        for questId, completed in pairs(data.questLegacyCompletions or {}) do
            if completed == true then completions[questId] = true end
        end
        data.questProgress[owner.key] = completions
    end
    local objectives = data.questObjectiveProgress[owner.key]
    if objectives == nil then
        objectives = {}
        data.questObjectiveProgress[owner.key] = objectives
    end
    local trust = data.questContactTrust[owner.key]
    if trust == nil then
        trust = {}
        data.questContactTrust[owner.key] = trust
    end
    return completions, objectives, trust, owner
end

function RaidRuntime.questStateFor(data, player)
    return questStateFor(data, player)
end

local function questTownUnlockedFor(data, player, town)
    if town == nil then return false end
    if town.unlockQuestId == nil then return true end
    local completions = questStateFor(data, player)
    return Quests.isCompleted(completions, town.unlockQuestId)
end

local function townChoicesFor(data, player)
    local result = {}
    for _, summary in ipairs(Logistics.townChoices(data)) do
        local town = Config.town(summary.key)
        if questTownUnlockedFor(data, player, town) then
            result[#result + 1] = {
                key = summary.key,
                name = summary.name,
                size = summary.size,
            }
        end
    end
    for _, entry in ipairs(RaidRuntime.activeFactionRaids(player)) do
        local factionTownKey = entry.raid.selectedTownKey
        local found = false
        for _, choice in ipairs(result) do
            if tostring(choice.key) == tostring(factionTownKey)
                and choice.activeFactionRaid ~= true then
                choice.activeFactionRaid = true
                choice.activeRaidKey = entry.key
                choice.activeRaidId = entry.raid.raidId
                found = true
                break
            end
        end
        if not found then
            local town = Config.town(factionTownKey)
            if town ~= nil then
                result[#result + 1] = {
                    key = town.key,
                    name = town.name,
                    size = town.size,
                    activeFactionRaid = true,
                    activeRaidKey = entry.key,
                    activeRaidId = entry.raid.raidId,
                }
            end
        end
    end
    return result
end

local function deliver(player, command, args)
    if player == nil then return end
    if isServer and isServer() then
        local payload = {}
        for key, value in pairs(args or {}) do payload[key] = value end
        payload._targetIdentity = Util.username(player)
        pcall(function() payload._targetPlayerNum = tonumber(player:getPlayerNum()) or 0 end)
        sendServerCommand(player, Config.COMMAND_MODULE, command, payload)
    elseif ExtractionMode.Client and ExtractionMode.Client.receiveServerCommand then
        local ok, err = pcall(function()
            -- Single-player authority and client code share one Lua runtime.
            -- Preserve the addressed IsoPlayer so split-screen commands do not
            -- silently fall back to player 0 after crossing this local boundary.
            ExtractionMode.Client.receiveServerCommand(
                Config.COMMAND_MODULE, command, args or {}, player)
        end)
        if not ok then Util.log("Client delivery failed for " .. tostring(command) .. ": " .. tostring(err)) end
    end
end

function RaidRuntime.deliver(player, command, args)
    return deliver(player, command, args)
end

local function activePlayers()
    local result = {}
    for _, player in ipairs(Util.players()) do
        if player and not player:isDead() and Util.username(player) ~= "" then
            result[#result + 1] = player
        end
    end
    return result
end

function RaidRuntime.playerByUsername(username)
    for _, player in ipairs(activePlayers()) do
        if Util.username(player) == tostring(username or "") then return player end
    end
    return nil
end

function RaidRuntime.playersForRaid(data)
    local result = {}
    local root = RaidRuntime.getRootStore()
    local teamKey = tostring(data and data.teamKey or RaidRuntime.contextKey or "")
    for _, player in ipairs(activePlayers()) do
        local username = Util.username(player)
        local assigned = root.playerRaidKeys[username]
        local active = data and (data.state == Config.STATE_TRANSIT or data.state == Config.STATE_RAID
            or data.state == Config.STATE_EXTRACTING or data.state == Config.STATE_BOARDING)
        local belongs = data and data.participants[username] == true
        if not active and not belongs and assigned == nil and teamKey ~= "legacy" and teamKey ~= "server" then
            local ownerKey = tostring(Groups.forPlayer(player, root.groupRegistry).key)
            belongs = teamKey == "player:" .. username
                or (data.factionKey == nil and ownerKey == teamKey)
                or (data.factionKey ~= nil and ownerKey == tostring(data.factionKey)
                    and tostring(root.factionStagingRaidKeys[ownerKey]) == teamKey)
        elseif not active and not belongs and assigned ~= nil then
            belongs = tostring(assigned) == teamKey
        end
        if belongs then result[#result + 1] = player end
    end
    return result
end

function RaidRuntime.eligibleLobbyPlayers(data)
    local result = {}
    if type(data and data.vehicleInsertion) == "table" then
        local vehicle = RaidRuntime.insertionVehicle(data)
        if vehicle == nil then return result end
        local vehicleId = tostring(data.vehicleInsertion.vehicleId)
        for _, player in ipairs(activePlayers()) do
            local current = player:getVehicle()
            if current ~= nil and tostring(current:getId()) == vehicleId then
                result[#result + 1] = player
            end
        end
        return result
    end
    for _, player in ipairs(RaidRuntime.playersForRaid(data)) do
        if data.optedOut[Util.username(player)] ~= true then result[#result + 1] = player end
    end
    return result
end

function RaidRuntime.vehicleById(vehicleId)
    if vehicleId == nil then return nil end
    vehicleId = tostring(vehicleId)
    local direct = nil
    if getVehicleById then
        pcall(function() direct = getVehicleById(tonumber(vehicleId)) end)
    end
    if direct ~= nil and tostring(direct:getId()) == vehicleId then return direct end
    for _, player in ipairs(activePlayers()) do
        local vehicle = player:getVehicle()
        if vehicle ~= nil and tostring(vehicle:getId()) == vehicleId then return vehicle end
    end
    local vehicles, count = nil, 0
    pcall(function()
        local collection = getCell():getVehicles()
        count = collection:size()
        vehicles = collection:toArray()
    end)
    if vehicles ~= nil then
        for index = 1, count do
            local vehicle = vehicles[index]
            if vehicle ~= nil and tostring(vehicle:getId()) == vehicleId then return vehicle end
        end
    end
    return nil
end

function RaidRuntime.insertionVehicle(data)
    local manifest = data and data.vehicleInsertion
    if type(manifest) ~= "table" or manifest.vehicleId == nil then return nil end
    return RaidRuntime.vehicleById(manifest.vehicleId)
end

function RaidRuntime.vehicleFuelDetails(vehicle)
    if vehicle == nil then return 0, 0, nil end
    local tank = nil
    local available = 0
    local quality = 100
    local mass = 1000
    pcall(function()
        tank = vehicle:getPartById("GasTank")
        if tank ~= nil then available = math.max(0, tonumber(tank:getContainerContentAmount()) or 0) end
        quality = math.max(0, math.min(100, tonumber(vehicle:getEngineQuality()) or 100))
        local script = vehicle:getScript()
        if script ~= nil then mass = math.max(1, tonumber(script:getMass()) or 1000) end
    end)
    local base = math.max(0, tonumber(Config.value("VehicleRaidBaseFuelLiters")) or 4)
    local qualityMultiplier = ((100 - quality) / 200) + 1
    local massMultiplier = (math.abs(1000 - mass) / 300) + 1
    local sandboxMultiplier = math.max(0, tonumber(SandboxVars and SandboxVars.CarGasConsumption) or 1)
    local required = math.ceil(base * qualityMultiplier * massMultiplier * sandboxMultiplier * 100) / 100
    return available, required, tank
end

function RaidRuntime.chargeVehicleRaidFuel(vehicle, allowPartial, requestedCost)
    local available, required, tank = RaidRuntime.vehicleFuelDetails(vehicle)
    if requestedCost ~= nil then required = math.max(0, tonumber(requestedCost) or required) end
    if required <= 0 then return true, required, available, available end
    if tank == nil or (allowPartial ~= true and available + 0.0001 < required) then
        return false, required, available, available
    end
    local remaining = math.max(0, available - required)
    local charged = pcall(function()
        tank:setContainerContentAmount(remaining, false, true)
        vehicle:transmitPartModData(tank)
    end)
    return charged, required, remaining, available
end

function RaidRuntime.reconcileVehicleInsertion(data)
    local manifest = data and data.vehicleInsertion
    if type(manifest) ~= "table" then return false end
    if data.state ~= Config.STATE_HIDEOUT and data.state ~= Config.STATE_COUNTDOWN then return false end
    local vehicle = RaidRuntime.insertionVehicle(data)
    local current = {}
    if vehicle ~= nil then
        local vehicleId = tostring(manifest.vehicleId)
        for _, player in ipairs(activePlayers()) do
            local occupied = player:getVehicle()
            if occupied ~= nil and tostring(occupied:getId()) == vehicleId then
                current[Util.username(player)] = true
            end
        end
    end

    local previous = type(manifest.occupants) == "table" and manifest.occupants or {}
    local changed = false
    local somebodyLeft = false
    for username in pairs(previous) do
        if current[username] ~= true then somebodyLeft = true break end
    end
    local hasDriver = false
    if vehicle ~= nil then pcall(function() hasDriver = vehicle:getDriver() ~= nil end) end
    local driverLeftSeat = manifest.hasDriver == true and not hasDriver
    if somebodyLeft or driverLeftSeat then
        data.ready = {}
        changed = true
    else
        for username in pairs(data.ready or {}) do
            if current[username] ~= true then data.ready[username] = nil; changed = true end
        end
    end
    for username in pairs(current) do
        if previous[username] ~= true then changed = true end
    end
    for username in pairs(previous) do
        if current[username] ~= true then changed = true end
    end
    manifest.occupants = current
    if manifest.hasDriver ~= hasDriver then changed = true end
    manifest.hasDriver = hasDriver
    local available, required = RaidRuntime.vehicleFuelDetails(vehicle)
    if tonumber(manifest.availableFuel) ~= available or tonumber(manifest.requiredFuel) ~= required then
        changed = true
    end
    manifest.availableFuel = available
    manifest.requiredFuel = required
    return changed
end

function RaidRuntime.vehicleLateJoinFor(data, username)
    for _, pending in pairs(data and data.lateJoinVehicles or {}) do
        if type(pending.participantNames) == "table"
            and pending.participantNames[username] == true then return pending end
    end
    return nil
end

function RaidRuntime.releaseVehicleReservation(data, vehicleId, raidKey)
    local manifest = data and data.vehicleInsertion
    local resolvedVehicleId = vehicleId or (manifest and manifest.vehicleId) or nil
    local resolvedRaidKey = raidKey or (data and data.teamKey) or RaidRuntime.contextKey
    if resolvedVehicleId == nil then return false end
    return ExtractionMode.GarageAuthority.releaseRaidReservation(
        RaidRuntime.getRootStore(), resolvedVehicleId, resolvedRaidKey)
end

function RaidRuntime.readyEntryCount(data)
    local count = 0
    for _, ready in pairs(data.ready or {}) do if ready == true then count = count + 1 end end
    return count
end

local function hideoutOccupied()
    local hideout = Config.hideout()
    for _, player in ipairs(activePlayers()) do
        if Util.playerNear(player, hideout, hideout.radius) then return true end
    end
    return false
end

local function generatorUsageMultiplier()
    if hideoutOccupied() then return 1 end
    return math.max(0, math.min(1,
        tonumber(Config.value("GeneratorEmptyHideoutMultiplier")) or 0.25))
end

function RaidRuntime.generatorUsageMultiplier()
    return generatorUsageMultiplier()
end

local function debugAuthorized(player)
    if Config.value("DebugLogging") ~= true or player == nil then return false end
    if not (isServer and isServer()) then return true end

    local authorized = false
    pcall(function()
        local role = player:getRole()
        authorized = role ~= nil and Capability ~= nil
            and Capability.UseDebugContextMenu ~= nil
            and role:hasCapability(Capability.UseDebugContextMenu)
    end)
    return authorized
end

-- Use the server-owned role capability that powers the vanilla scoreboard's
-- "Teleport To" action. The visible admin tag is cosmetic and can be hidden
-- or imitated, so it must not grant a containment bypass by itself.
function RaidRuntime.canBypassHideoutEnforcement(player)
    if player == nil or not (isServer and isServer()) then return false end

    local authorized = false
    pcall(function()
        local role = player:getRole()
        authorized = role ~= nil and Capability ~= nil
            and Capability.TeleportToPlayer ~= nil
            and role:hasCapability(Capability.TeleportToPlayer)
    end)
    return authorized
end

function RaidRuntime.shouldEnforceHideout(player)
    if not RaidRuntime.canBypassHideoutEnforcement(player) then return true end
    local playerData = player and player:getModData()
    -- An administrator still needs the normal first-arrival correction. Once the
    -- character has reached the hideout, preserve intentional admin teleports.
    return playerData == nil or playerData.ExtractionModeHideoutSpawnRandomized ~= true
end

local function sortedNames(values)
    local names = {}
    for name, enabled in pairs(values or {}) do
        if enabled == true then names[#names + 1] = tostring(name) end
    end
    table.sort(names)
    return names
end

local function tableHasEntries(values)
    if values == nil then return false end
    for _ in pairs(values) do return true end
    return false
end

local function secondsRemaining(deadline)
    if not deadline then return 0 end
    return math.max(0, math.ceil((tonumber(deadline) - Util.timerNowMs()) / 1000))
end

local function snapshotFor(player)
    local data = RaidRuntime.dataForPlayer(player)
    Logistics.refreshTownChoices(data)
    local username = Util.username(player)
    local garageOwner = Util.garageUsername(player)
    local readyCount = 0
    local requiredCount = 0
    local teamPlayerCount = #RaidRuntime.playersForRaid(data)
    for _, candidate in ipairs(RaidRuntime.eligibleLobbyPlayers(data)) do
        requiredCount = requiredCount + 1
        if data.ready[Util.username(candidate)] == true then readyCount = readyCount + 1 end
    end
    local town = Config.town(data.selectedTownKey)
    local fuel = math.max(0, tonumber(data.generatorFuel) or 0)
    local generatorRunning = data.generatorRunning == true and fuel > 0.0001
    local debugEnabled = debugAuthorized(player)
    local debugBanditClans = debugEnabled and BanditsIntegration.debugClanSummaries() or {}
    local usageMultiplier = generatorUsageMultiplier()
    local questProgress, questObjectives, questTrust, questOwner = questStateFor(data, player)
    local root = RaidRuntime.getRootStore()
    local playerData = player:getModData()
    local welcomeAcknowledged = math.max(
        tonumber(root.coopWelcomeAcknowledged[username]) or 0,
        tonumber(playerData and playerData.ExtractionModeCoopWelcomeVersion) or 0)
    if welcomeAcknowledged >= Config.COOP_WELCOME_VERSION then
        root.coopWelcomeAcknowledged[username] = welcomeAcknowledged
    end
    local transitionProtection = boardingProtections[username]
    local groundExtraction = data.groundExtractionPending[username]
    local deathRescue = data.deathRescuePending[username]
    local lateJoin = data.lateJoinPending[username]
        or RaidRuntime.vehicleLateJoinFor(data, username)
    local owner = Groups.forPlayer(player, root.groupRegistry)
    local selectedJoinRaidKey = data.selectedJoinRaidKey
    local factionRaid = selectedJoinRaidKey and root.raids[selectedJoinRaidKey] or nil
    local factionRaidOwner = factionRaid and (factionRaid.factionKey
        or (tostring(selectedJoinRaidKey) == tostring(owner.key) and owner.key)) or nil
    local selectedJoinValid = owner.kind == "faction"
        and tostring(factionRaidOwner) == tostring(owner.key)
        and RaidRuntime.isJoinableRaid(factionRaid)
        and factionRaid.participants[username] ~= true
        and tostring(factionRaid.selectedTownKey) == tostring(data.selectedTownKey)
    if not selectedJoinValid then
        data.selectedJoinRaidKey = nil
        selectedJoinRaidKey = nil
        factionRaid = nil
    end
    local joinableFactionTownKey = factionRaid and factionRaid.selectedTownKey or nil
    local vehicleInsertion = type(data.vehicleInsertion) == "table" and data.vehicleInsertion or nil
    local playerVehicle = player:getVehicle()
    local vehicleInsertionMember = vehicleInsertion ~= nil and playerVehicle ~= nil
        and tostring(playerVehicle:getId()) == tostring(vehicleInsertion.vehicleId)
    local canOptOut = isServer and isServer() and teamPlayerCount > 1 and readyCount > 0
        and (data.state == Config.STATE_HIDEOUT or data.state == Config.STATE_COUNTDOWN)
        and vehicleInsertion == nil
    return {
        version = Config.VERSION,
        state = data.state,
        raidId = data.raidId,
        selfReady = data.ready[username] == true,
        selfOptedOut = data.optedOut[username] == true,
        canOptOut = canOptOut,
        canReady = vehicleInsertion == nil
            or (vehicleInsertionMember and vehicleInsertion.hasDriver == true),
        vehicleInsertionActive = vehicleInsertion ~= nil,
        vehicleInsertionMember = vehicleInsertionMember,
        vehicleInsertionHasDriver = vehicleInsertion and vehicleInsertion.hasDriver == true or false,
        vehicleInsertionFuel = vehicleInsertion and tonumber(vehicleInsertion.availableFuel) or nil,
        vehicleInsertionFuelRequired = vehicleInsertion and tonumber(vehicleInsertion.requiredFuel) or nil,
        teamPlayerCount = teamPlayerCount,
        isParticipant = data.participants[username] == true,
        hasExtracted = data.extractedPlayers[username] == true,
        readyNames = sortedNames(data.ready),
        participantNames = sortedNames(data.participants),
        readyCount = readyCount,
        requiredCount = requiredCount,
        countdownSeconds = secondsRemaining(data.countdownEndMs),
        extractionSeconds = secondsRemaining(data.extractionEndMs),
        boardingSeconds = secondsRemaining(data.boardingEndMs),
        boardingPendingSelf = data.boardingPending[username] ~= nil,
        deathRescuePendingSelf = deathRescue ~= nil,
        deathRescuePhaseSelf = deathRescue and deathRescue.phase or nil,
        deathRescueSeconds = deathRescue and secondsRemaining(
            deathRescue.phase == "CINEMATIC" and deathRescue.cinematicDeadlineMs
                or deathRescue.deadlineMs) or 0,
        lateJoinPendingSelf = lateJoin ~= nil,
        lateJoinPhaseSelf = lateJoin and lateJoin.phase or nil,
        lateJoinSeconds = lateJoin and secondsRemaining(lateJoin.deadlineMs) or 0,
        joinableFactionTownKey = joinableFactionTownKey,
        selectedJoinRaidKey = selectedJoinRaidKey,
        joinableFactionRaidId = factionRaid and factionRaid.raidId or nil,
        canJoinFactionRaid = data.state == Config.STATE_HIDEOUT
            and joinableFactionTownKey ~= nil
            and tostring(data.selectedTownKey) == tostring(joinableFactionTownKey)
            and vehicleInsertion == nil,
        groundExtractionPendingSelf = groundExtraction ~= nil,
        groundExtractionPhaseSelf = groundExtraction and groundExtraction.phase or nil,
        groundExtractionSeconds = groundExtraction
            and secondsRemaining(groundExtraction.deadlineMs) or 0,
        campaignHandoffActive = data.campaignHandoff ~= nil,
        campaignHandoffSeconds = data.campaignHandoff
            and secondsRemaining(data.campaignHandoff.endMs) or 0,
        campaignHandoffPoint = Config.CAMPAIGN_HANDOFF_POINT,
        transitionProtectionSelf = transitionProtection ~= nil,
        transitionProtectionInvincible = transitionProtection ~= nil
            and transitionProtection.wasInvincible == true,
        transitionProtectionZombiesDontAttack = transitionProtection ~= nil
            and transitionProtection.wasIgnoredByZombies == true,
        transitionProtectionInvisible = transitionProtection ~= nil
            and transitionProtection.wasInvisible == true,
        transitionProtectionForceInvisible = transitionProtection ~= nil
            and transitionProtection.forceInvisible == true,
        transitionProtectionKeepZombieTargeting = transitionProtection ~= nil
            and transitionProtection.keepZombieTargeting == true,
        activeExtraction = data.activeExtraction,
        extractionRope = data.extractionRope,
        hordeWindowLabel = Threats.hordeMinimumTimeLabel(data),
        hordeSpawned = data.hordeSpawned,
        hordeEventType = data.hordeSpawned and data.hordeEventType or nil,
        selectedTownKey = data.selectedTownKey,
        selectedTownName = town and town.name or nil,
        selectedTownBy = data.selectedTownBy,
        raidBounds = town and town.raidBounds or nil,
        townChoices = townChoicesFor(data, player),
        upgrades = Upgrades.completionSnapshot(data.upgrades),
        quests = Quests.completionSnapshot(questProgress),
        questObjectives = Quests.objectiveSnapshot(questObjectives),
        raidVisitSuccessful = tonumber(data.questVisitRaidIds[questOwner.key]) ~= nil
            and tonumber(data.questVisitSuccessfulRaidIds[questOwner.key])
                == tonumber(data.questVisitRaidIds[questOwner.key]),
        contactTrust = Quests.trustSnapshot(questTrust),
        questOwnerKind = questOwner.kind,
        questOwnerName = questOwner.name,
        generator = {
            fuel = fuel,
            capacity = Generator.capacity(),
            running = generatorRunning,
            fuelPerDay = Generator.fuelPerDay(data.upgrades) * usageMultiplier,
            hoursRemaining = Generator.hoursRemaining(fuel, data.upgrades, usageMultiplier),
            standby = usageMultiplier < 0.999,
            transferLiters = Generator.transferLimit(),
        },
        hideout = Config.hideout(),
        raidSpawn = activeRaidSpawn(data),
        extractionSites = activeExtractionSites(data),
        debugEnabled = debugEnabled,
        debugBanditsAvailable = debugEnabled and BanditsIntegration.isAvailable() or false,
        debugBanditClans = debugBanditClans,
        garageOwner = garageOwner,
        garageVehicles = ExtractionMode.Garage.summaries(root, garageOwner),
        garageDoorUnlocked = root.garageDoorUnlocked == true,
        -- Every client needs the active vehicle id so the hideout immobilizer
        -- can apply on the client currently simulating the driver's controls.
        activeHideoutVehicle = ExtractionMode.GarageAuthority.activeSummary(root),
        garageTransition = ExtractionMode.GarageAuthority.transitionSummary(root),
        showCoopWelcome = isServer and isServer()
            and welcomeAcknowledged < Config.COOP_WELCOME_VERSION,
    }
end

local function sendState(player)
    deliver(player, "State", snapshotFor(player))
end

local function broadcastState()
    for _, player in ipairs(Util.players()) do sendState(player) end
end

local function messagePayload(key, fallback, arguments, audioCue)
    return {
        messageKey = tostring(key or ""),
        message = tostring(fallback or ""),
        messageArgs = arguments or {},
        audioCue = audioCue,
    }
end

local function deliverLocalized(player, command, key, fallback, ...)
    deliver(player, command, messagePayload(key, fallback, { ... }))
end

function RaidRuntime.deliverLocalized(player, command, key, fallback, ...)
    return deliverLocalized(player, command, key, fallback, ...)
end

local function deliverExtendedLocalized(player, command, key, fallback, ...)
    local payload = messagePayload(key, fallback, { ... })
    payload.extendedHalo = true
    deliver(player, command, payload)
end

local function sendStateToQuestGroup(owner)
    local data = getStore()
    for _, player in ipairs(Util.players()) do
        if Groups.same(owner, Groups.forPlayer(player, data.groupRegistry)) then sendState(player) end
    end
end

function RaidRuntime.sendStateToQuestGroup(owner)
    return sendStateToQuestGroup(owner)
end

local function announce(message, options)
    options = options or {}
    local data = getStore()
    for _, player in ipairs(Util.players()) do
        local correctTeam = options.teamOnly ~= true or RaidRuntime.dataForPlayer(player) == data
        if correctTeam and (options.participantsOnly ~= true
            or data.participants[Util.username(player)] == true) then
            deliver(player, "Announcement", {
                message = tostring(message),
                messageKey = options.messageKey,
                messageArgs = options.messageArgs,
                audioCue = options.audioCue,
                extendedHalo = options.extendedHalo == true,
            })
        end
    end
end


local function announceLocalized(key, fallback, arguments, options)
    options = options or {}
    options.messageKey = key
    options.messageArgs = arguments or {}
    announce(fallback, options)
end

function RaidRuntime.broadcastState()
    return broadcastState()
end

function RaidRuntime.announceLocalized(key, fallback, arguments, options)
    return announceLocalized(key, fallback, arguments, options)
end

local function applyExtractionHealing(player)
    local mode = RaidOutcomes.healingMode()
    if mode <= 1 or player == nil or player:isDead() then return false end
    RaidOutcomes.applyExtractionHealing(player, mode)
    deliver(player, "ApplyExtractionHealing", { mode = mode })
    return true
end

local function announceLogisticsResult(result, initial)
    if result == nil then return end
    local ammo = math.max(0, math.floor(tonumber(result.ammo) or 0))
    local medical = math.max(0, math.floor(tonumber(result.medical) or 0))
    local delivered = math.max(0, math.floor(tonumber(result.delivered) or 0))
    local failed = math.max(0, math.floor(tonumber(result.failed) or 0))
    if delivered > 0 and (ammo > 0 or medical > 0) then
        local shipment = ammo > 0 and medical > 0 and "ammunition and medical shipments"
            or (ammo > 0 and "ammunition shipment" or "medical shipment")
        local key = initial and "IGUI_ExtractionMode_Message_InitialDelivery"
            or "IGUI_ExtractionMode_Message_DailyDelivery"
        local shipmentKey = ammo > 0 and medical > 0
            and "IGUI_ExtractionMode_Shipment_AmmoMedical"
            or (ammo > 0 and "IGUI_ExtractionMode_Shipment_Ammo"
                or "IGUI_ExtractionMode_Shipment_Medical")
        announceLocalized(key, (initial and "The initial " or "Today's ") .. shipment .. " placed "
            .. tostring(delivered) .. " item(s) in the Secure Delivery Crate.",
            { { key = shipmentKey, fallback = shipment }, tostring(delivered) })
    end
    if failed > 0 then
        announceLocalized("IGUI_ExtractionMode_Message_DeliveryRejected",
            tostring(failed) .. " delivery item(s) were rejected because the Secure Delivery Crate is full.",
            { tostring(failed) })
    end
end


local function questItemRaidId(item)
    if item == nil then return nil end
    local value = nil
    pcall(function() value = tonumber(item:getModData().ExtractionModeQuestRaidId) end)
    return value
end

local function spentFlareRaidId(item)
    if item == nil or item:getFullType() ~= Config.FLARE_FULL_TYPE then return nil end
    local value = nil
    pcall(function() value = tonumber(item:getModData().ExtractionModeSpentFlareRaidId) end)
    return value
end

function RaidRuntime.raidIdIsClosed(closedRaidIds, raidId)
    if raidId == nil then return false end
    local numericRaidId = math.max(0, math.floor(tonumber(raidId) or 0))
    if numericRaidId <= 0 then return false end
    if type(closedRaidIds) == "table" then
        return numericRaidId <= math.max(0,
            math.floor(tonumber(closedRaidIds._through) or 0))
            or closedRaidIds[tostring(numericRaidId)] == true
            or closedRaidIds[numericRaidId] == true
    end
    return numericRaidId <= (tonumber(closedRaidIds) or 0)
end

function RaidRuntime.hasClosedRaidIds(closedRaidIds)
    if type(closedRaidIds) ~= "table" then
        return (tonumber(closedRaidIds) or 0) > 0
    end
    if (tonumber(closedRaidIds._through) or 0) > 0 then return true end
    for raidId, closed in pairs(closedRaidIds) do
        if raidId ~= "_through" and closed == true and (tonumber(raidId) or 0) > 0 then
            return true
        end
    end
    return false
end

function RaidRuntime.compactClosedRaidIds(root)
    root = root or RaidRuntime.getRootStore()
    local closed = root.closedRaidIds
    if type(closed) ~= "table" then return 0 end
    local through = math.max(0, math.floor(tonumber(closed._through) or 0))
    local advanced = 0
    while closed[tostring(through + 1)] == true or closed[through + 1] == true do
        through = through + 1
        closed[tostring(through)] = nil
        closed[through] = nil
        advanced = advanced + 1
    end
    closed._through = through
    return advanced
end

function RaidRuntime.pruneDormantRaidRecords(root)
    root = root or RaidRuntime.getRootStore()
    local protected = { server = true }
    for _, key in pairs(root.factionStagingRaidKeys or {}) do
        protected[tostring(key)] = true
    end
    for _, key in pairs(root.playerRaidKeys or {}) do
        protected[tostring(key)] = true
    end
    local removed = 0
    for key, raid in pairs(root.raids or {}) do
        local factionLifecycle = raid and raid.factionKey ~= nil
        local empty = raid and raid.state == Config.STATE_HIDEOUT
            and not tableHasEntries(raid.ready)
            and not tableHasEntries(raid.participants)
            and not tableHasEntries(raid.lateJoinPending)
            and not tableHasEntries(raid.lateJoinVehicles)
            and not tableHasEntries(raid.groundExtractionPending)
            and not tableHasEntries(raid.deathRescuePending)
        if factionLifecycle and empty and protected[tostring(key)] ~= true then
            root.raids[key] = nil
            RaidRuntime.views[raid] = nil
            removed = removed + 1
        end
    end
    if removed > 0 then
        Util.log("Pruned " .. tostring(removed) .. " completed faction raid record(s)")
    end
    RaidRuntime.compactClosedRaidIds(root)
    return removed
end

local function expiredRaidLifecycleItem(item, closedRaidIds)
    local questRaidId = questItemRaidId(item)
    local flareRaidId = spentFlareRaidId(item)
    return RaidRuntime.raidIdIsClosed(closedRaidIds, questRaidId)
        or RaidRuntime.raidIdIsClosed(closedRaidIds, flareRaidId)
end

local function questItemId(item)
    if item == nil then return nil end
    local value = nil
    pcall(function() value = tonumber(item:getID()) end)
    return value
end

local function groupHasItem(members, fullType)
    for _, player in ipairs(members or {}) do
        local inventory = player and player:getInventory()
        local found = inventory and inventory:getAllTypeRecurse(fullType)
        if found and found:size() > 0 then return true end
    end
    return false
end

local function prepareRaidQuestItems(data)
    data.raidQuestItems = {}
    raidQuestItemRefs = {}
    local groups = {}

    for _, player in ipairs(activePlayers()) do
        if data.participants[Util.username(player)] == true then
            local completions, _, _, owner = questStateFor(data, player)
            local entry = groups[owner.key]
            if entry == nil then
                entry = { owner = owner, completions = completions, members = {} }
                groups[owner.key] = entry
            end
            entry.members[#entry.members + 1] = player
        end
    end

    for ownerKey, entry in pairs(groups) do
        for _, definition in ipairs(Quests.definitions()) do
            if Quests.isAcquired(entry.completions, definition)
                and not Quests.isCompleted(entry.completions, definition.id) then
                for requirementIndex, requirement in ipairs(definition.requirements or {}) do
                    local point = requirement.raidSpawnPoint
                    local grantOnInsertion = requirement.raidGrantOnInsertion == true
                    local fullType = tostring((requirement.types or {})[1] or "")
                    local destinationMatches = requirement.locationTownKey == nil
                        or tostring(requirement.locationTownKey) == tostring(data.selectedTownKey)
                    if (point or grantOnInsertion) and destinationMatches and fullType ~= ""
                        and (grantOnInsertion or not groupHasItem(entry.members, fullType)) then
                        data.raidQuestItems[#data.raidQuestItems + 1] = {
                            key = tostring(data.raidId) .. ":" .. tostring(ownerKey) .. ":"
                                .. tostring(definition.id) .. ":" .. tostring(requirementIndex),
                            raidId = data.raidId,
                            questId = definition.id,
                            ownerKey = ownerKey,
                            fullType = fullType,
                            point = point and { x = point.x, y = point.y, z = point.z or 0 } or nil,
                            grantOnInsertion = grantOnInsertion,
                            spawned = false,
                        }
                    end
                end
            end
        end
    end
end

local function raidQuestGrantPlayer(data, record)
    for _, player in ipairs(activePlayers()) do
        if data.participants[Util.username(player)] == true
            and Groups.forPlayer(player, data.groupRegistry).key == tostring(record.ownerKey or "") then
            return player
        end
    end
    return nil
end

local function raidQuestItemCarrier(record)
    if record == nil then return nil, nil end
    for _, player in ipairs(activePlayers()) do
        local inventory = player:getInventory()
        local candidates = inventory and inventory:getAllTypeRecurse(tostring(record.fullType or ""))
        if candidates then
            for index = 0, candidates:size() - 1 do
                local item = candidates:get(index)
                local modData = item and item:getModData()
                if modData and tostring(modData.ExtractionModeQuestSpawnKey or "")
                    == tostring(record.key or "") then
                    return player, item
                end
            end
        end
    end
    return nil, nil
end

local function findQuestItemOnSpawnSquare(record)
    local cell = getCell and getCell()
    local point = record and (record.spawnPoint or record.point)
    if cell == nil or point == nil then return nil end
    local square = cell:getGridSquare(math.floor(point.x), math.floor(point.y), math.floor(point.z or 0))
    local worldObjects = square and square:getWorldObjects()
    if worldObjects == nil then return nil end
    for index = 0, worldObjects:size() - 1 do
        local item = nil
        pcall(function() item = worldObjects:get(index):getItem() end)
        if item and questItemRaidId(item) == tonumber(record.raidId) then
            local modData = item:getModData()
            if tostring(modData.ExtractionModeQuestSpawnKey or "") == tostring(record.key or "") then
                return item
            end
        end
    end
    return nil
end

function RaidRuntime.usableQuestItemSquare(square)
    if square == nil then return false end
    local usable = false
    pcall(function()
        usable = square:getFloor() ~= nil and square:TreatAsSolidFloor()
            and not square:isSolid() and not square:isSolidTrans()
    end)
    return usable
end

function RaidRuntime.questItemSpawnSquare(cell, record)
    local point = record and record.point
    if cell == nil or point == nil then return nil, false end
    local centerX = math.floor(tonumber(point.x) or 0)
    local centerY = math.floor(tonumber(point.y) or 0)
    local z = math.floor(tonumber(point.z) or 0)
    local exact = cell:getGridSquare(centerX, centerY, z)
    if RaidRuntime.usableQuestItemSquare(exact) then return exact, false end

    -- Map replacements can move a wall, counter, or room boundary over an
    -- authored coordinate. Keep the item at the intended landmark by choosing
    -- the nearest usable loaded square instead of permanently blocking the quest.
    for radius = 1, 4 do
        for x = centerX - radius, centerX + radius do
            for y = centerY - radius, centerY + radius do
                if x == centerX - radius or x == centerX + radius
                    or y == centerY - radius or y == centerY + radius then
                    local candidate = cell:getGridSquare(x, y, z)
                    if RaidRuntime.usableQuestItemSquare(candidate) then return candidate, true end
                end
            end
        end
    end
    return nil, false
end

function RaidRuntime.logQuestItemSpawnFailure(record, reason)
    local now = Util.timerNowMs()
    record.spawnFailureCount = math.max(0, tonumber(record.spawnFailureCount) or 0) + 1
    if record.lastSpawnFailureLogMs == nil
        or now - tonumber(record.lastSpawnFailureLogMs) >= 30000 then
        record.lastSpawnFailureLogMs = now
        print("[ExtractionMode] Raid quest item spawn is waiting: quest="
            .. tostring(record.questId) .. " raid=" .. tostring(record.raidId)
            .. " type=" .. tostring(record.fullType) .. " reason=" .. tostring(reason)
            .. " attempts=" .. tostring(record.spawnFailureCount))
    end
end

local function ensureRaidQuestItems()
    local data = getStore()
    if data.state ~= Config.STATE_RAID and data.state ~= Config.STATE_EXTRACTING
        and data.state ~= Config.STATE_BOARDING then return 0 end
    local spawned = 0
    local cell = getCell and getCell()
    if cell == nil then return 0 end

    for _, record in ipairs(data.raidQuestItems or {}) do
        if tonumber(record.raidId) == tonumber(data.raidId) then
            if record.spawned == true then
                local item = raidQuestItemRefs[tostring(record.key)]
                    or findQuestItemOnSpawnSquare(record)
                if item then raidQuestItemRefs[tostring(record.key)] = item end
                if record.pickupLogged ~= true and record.grantOnInsertion ~= true then
                    local carrier, carriedItem = raidQuestItemCarrier(record)
                    if carrier and carriedItem then
                        record.pickupLogged = true
                        record.pickedUpBy = Util.username(carrier)
                        record.pickedUpAtMs = Util.timerNowMs()
                        raidQuestItemRefs[tostring(record.key)] = carriedItem
                        Util.log("Raid quest item picked up: player=" .. tostring(record.pickedUpBy)
                            .. " quest=" .. tostring(record.questId)
                            .. " owner=" .. tostring(record.ownerKey)
                            .. " raid=" .. tostring(record.raidId)
                            .. " type=" .. tostring(record.fullType)
                            .. " itemId=" .. tostring(questItemId(carriedItem))
                            .. " spawnKey=" .. tostring(record.key))
                    end
                end
            else
                local point = record.point
                local item = nil
                local recipient = nil
                if record.grantOnInsertion == true then
                    recipient = raidQuestGrantPlayer(data, record)
                    local inventory = recipient and recipient:getInventory()
                    if inventory then
                        pcall(function() item = inventory:AddItem(record.fullType) end)
                        if item and sendAddItemToContainer then sendAddItemToContainer(inventory, item) end
                    end
                    if recipient == nil then
                        RaidRuntime.logQuestItemSpawnFailure(record,
                            "eligible raid participant is unavailable")
                    elseif item == nil then
                        RaidRuntime.logQuestItemSpawnFailure(record,
                            "inventory rejected or could not create the item")
                    end
                else
                    local square, usedFallback = RaidRuntime.questItemSpawnSquare(cell, record)
                    if square then
                        pcall(function()
                            item = square:AddWorldInventoryItem(record.fullType, 0.5, 0.5, 0)
                        end)
                        if item then
                            record.spawnPoint = {
                                x = square:getX(), y = square:getY(), z = square:getZ(),
                            }
                            record.usedFallbackSpawn = usedFallback == true
                        else
                            RaidRuntime.logQuestItemSpawnFailure(record,
                                "world square rejected or could not create the item")
                        end
                    else
                        RaidRuntime.logQuestItemSpawnFailure(record,
                            "authored square and nearby fallback squares are unavailable")
                    end
                end
                if item then
                    local modData = item:getModData()
                    modData.ExtractionModeQuestRaidId = data.raidId
                    modData.ExtractionModeQuestId = record.questId
                    modData.ExtractionModeQuestOwnerKey = record.ownerKey
                    modData.ExtractionModeQuestSpawnKey = record.key
                    record.itemId = questItemId(item)
                    record.spawned = true
                    record.pickupLogged = record.grantOnInsertion == true
                    raidQuestItemRefs[tostring(record.key)] = item
                    if record.grantOnInsertion ~= true then
                        pcall(function() item:SynchSpawn() end)
                    end
                    spawned = spawned + 1
                    Util.log("Raid quest item "
                        .. (record.grantOnInsertion == true and "granted" or "spawned")
                        .. ": quest=" .. tostring(record.questId)
                        .. " owner=" .. tostring(record.ownerKey)
                        .. " raid=" .. tostring(record.raidId)
                        .. " type=" .. tostring(record.fullType)
                        .. " itemId=" .. tostring(record.itemId)
                        .. " spawnKey=" .. tostring(record.key)
                        .. (record.grantOnInsertion == true
                            and (" recipient=" .. tostring(recipient and Util.username(recipient) or "none"))
                            or (" position=" .. tostring(record.spawnPoint and record.spawnPoint.x or point.x)
                                .. "," .. tostring(record.spawnPoint and record.spawnPoint.y or point.y)
                                .. "," .. tostring(record.spawnPoint and record.spawnPoint.z or point.z or 0)
                                .. (record.usedFallbackSpawn == true and " fallback=true" or ""))))
                end
            end
        end
    end
    return spawned
end

local function removeQuestLifecycleItem(item)
    if item == nil then return false end
    local container = nil
    pcall(function() container = item:getContainer() end)
    if container then
        if sendRemoveItemFromContainer then
            pcall(function() sendRemoveItemFromContainer(container, item) end)
        end
        return pcall(function() container:Remove(item) end)
    end

    local worldObject = nil
    pcall(function() worldObject = item:getWorldItem() end)
    if worldObject == nil then return false end
    local square = nil
    pcall(function() square = worldObject:getSquare() end)
    if square then pcall(function() square:transmitRemoveItemFromSquare(worldObject) end) end
    pcall(function() worldObject:removeFromWorld() end)
    pcall(function() worldObject:removeFromSquare() end)
    return true
end

local function retainCarriedRaidQuestItems(raidId)
    local retained = {}
    local count = 0
    for _, player in ipairs(Util.players()) do
        local inventory = player and player:getInventory()
        local found = inventory and inventory:getAllEvalRecurse(function(item)
            return questItemRaidId(item) == tonumber(raidId)
        end)
        if found then
            for index = 0, found:size() - 1 do
                local item = found:get(index)
                local id = questItemId(item)
                if id then retained[tostring(id)] = true end
                local modData = item:getModData()
                modData.ExtractionModeQuestRaidId = nil
                modData.ExtractionModeQuestId = nil
                modData.ExtractionModeQuestOwnerKey = nil
                modData.ExtractionModeQuestSpawnKey = nil
                count = count + 1
            end
        end
    end
    return retained, count
end

local function retainExpiredCarriedQuestItems(player, closedRaidIds)
    local inventory = player and player:getInventory()
    if inventory == nil or not RaidRuntime.hasClosedRaidIds(closedRaidIds) then return 0 end
    local found = inventory:getAllEvalRecurse(function(item)
        return RaidRuntime.raidIdIsClosed(closedRaidIds, questItemRaidId(item))
    end)
    local count = 0
    if found then
        for index = 0, found:size() - 1 do
            local modData = found:get(index):getModData()
            modData.ExtractionModeQuestRaidId = nil
            modData.ExtractionModeQuestId = nil
            modData.ExtractionModeQuestOwnerKey = nil
            modData.ExtractionModeQuestSpawnKey = nil
            count = count + 1
        end
    end
    return count
end

local function cleanupRaidQuestItems(data)
    local closedRaidId = tonumber(data and data.raidId) or 0
    local retained, retainedCount = retainCarriedRaidQuestItems(closedRaidId)
    local removed = 0
    for _, record in ipairs((data and data.raidQuestItems) or {}) do
        if tonumber(record.raidId) == closedRaidId
            and not retained[tostring(record.itemId or "")] then
            local item = raidQuestItemRefs[tostring(record.key)]
                or findQuestItemOnSpawnSquare(record)
            if item and removeQuestLifecycleItem(item) then removed = removed + 1 end
        end
        if tonumber(record.raidId) == closedRaidId then
            raidQuestItemRefs[tostring(record.key)] = nil
        end
    end
    if closedRaidId > 0 then
        data.closedRaidIds[tostring(closedRaidId)] = true
        RaidRuntime.compactClosedRaidIds(RaidRuntime.getRootStore())
    end
    data.raidQuestItems = {}
    if removed > 0 or retainedCount > 0 then
        Util.log("Closed raid quest items: retained " .. tostring(retainedCount)
            .. " carried item(s), removed " .. tostring(removed) .. " loose item(s)")
    end
end

local function collectExpiredQuestItems(container, closedRaidIds, output)
    if container == nil then return end
    local items = container:getItems()
    if items == nil then return end
    for index = 0, items:size() - 1 do
        local item = items:get(index)
        if expiredRaidLifecycleItem(item, closedRaidIds) then output[#output + 1] = item end
        if item and item:IsInventoryContainer() then
            collectExpiredQuestItems(item:getInventory(), closedRaidIds, output)
        end
    end
end

local function collectExpiredQuestItemsFromObject(object, closedRaidIds, output)
    if object == nil then return end
    local containerCount = 0
    pcall(function() containerCount = math.max(0, tonumber(object:getContainerCount()) or 0) end)
    for index = 0, containerCount - 1 do
        local container = nil
        pcall(function() container = object:getContainerByIndex(index) end)
        collectExpiredQuestItems(container, closedRaidIds, output)
    end

    local vehicle = false
    pcall(function() vehicle = instanceof(object, "BaseVehicle") end)
    if vehicle then
        local partCount = 0
        pcall(function() partCount = math.max(0, tonumber(object:getPartCount()) or 0) end)
        for index = 0, partCount - 1 do
            local container = nil
            pcall(function()
                local part = object:getPartByIndex(index)
                container = part and part:getItemContainer()
            end)
            collectExpiredQuestItems(container, closedRaidIds, output)
        end
    end
end

local function purgeExpiredQuestItemsOnSquare(square)
    if square == nil then return 0 end
    local data = getStore()
    local closedRaidIds = data.closedRaidIds
    if not RaidRuntime.hasClosedRaidIds(closedRaidIds) then return 0 end
    local expired = {}
    local worldObjects = square:getWorldObjects()
    if worldObjects then
        for index = 0, worldObjects:size() - 1 do
            local item = nil
            pcall(function() item = worldObjects:get(index):getItem() end)
            if expiredRaidLifecycleItem(item, closedRaidIds) then expired[#expired + 1] = item end
        end
    end
    local objects = square:getObjects()
    if objects then
        for index = 0, objects:size() - 1 do
            collectExpiredQuestItemsFromObject(objects:get(index), closedRaidIds, expired)
        end
    end
    local movingObjects = square:getMovingObjects()
    if movingObjects then
        for index = 0, movingObjects:size() - 1 do
            collectExpiredQuestItemsFromObject(movingObjects:get(index), closedRaidIds, expired)
        end
    end
    local removed = 0
    for _, item in ipairs(expired) do
        if removeQuestLifecycleItem(item) then removed = removed + 1 end
    end
    return removed
end

local function zombieKiller(zombie)
    if zombie == nil then return nil end
    local attacker = nil
    pcall(function() attacker = zombie:getAttackedBy() end)
    if attacker == nil then return nil end
    local isPlayer = false
    pcall(function() isPlayer = instanceof(attacker, "IsoPlayer") end)
    return isPlayer and attacker or nil
end

local function onQuestZombieDead(zombie)
    local player = zombieKiller(zombie)
    player = ProjectRemnantsIntegration.questCreditPlayer(player)
    if player == nil or player:isDead() or Util.username(player) == "" then return end
    local data = RaidRuntime.dataForPlayer(player)
    local completions, objectives, _, owner = questStateFor(data, player)
    local changed = false
    for _, definition in ipairs(Quests.definitions()) do
        if Quests.isAcquired(completions, definition)
            and not Quests.isCompleted(completions, definition.id) then
            for _, objective in ipairs(definition.objectives or {}) do
                local destinationMatches = objective.townKey == nil
                    or (data.selectedTownKey == objective.townKey
                        and (data.state == Config.STATE_RAID or data.state == Config.STATE_EXTRACTING
                            or data.state == Config.STATE_BOARDING)
                        and data.participants[Util.username(player)] == true)
                if objective.type == "zombie_kills" and destinationMatches then
                    local before = Quests.objectiveCount(objectives, definition, objective)
                    local after = Quests.incrementObjective(objectives, definition, objective, 1)
                    if after > before then changed = true end
                end
            end
        end
    end
    if changed then sendStateToQuestGroup(owner) end
end

function RaidRuntime.randomizedTeleportPoint(point, offsetIndex, requireOutdoorLand)
    local baseX = math.floor(tonumber(point.x) or 0)
    local baseY = math.floor(tonumber(point.y) or 0)
    local baseZ = math.floor(tonumber(point.z) or 0)
    local hideout = Config.hideout()
    local hideoutArrival = baseX == math.floor(tonumber(hideout.x) or 0)
        and baseY == math.floor(tonumber(hideout.y) or 0)
        and baseZ == math.floor(tonumber(hideout.z) or 0)

    -- Preserve exact/deterministic offsets for special teleports such as raid
    -- resume and reconnect-near-teammate. Only normal hideout and outdoor raid
    -- arrivals need randomized spawn separation.
    if not hideoutArrival and requireOutdoorLand ~= true then
        local offsets = {
            { 0, 0 }, { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 },
            { 1, 1 }, { -1, 1 }, { 1, -1 }, { -1, -1 },
        }
        local offset = offsets[((tonumber(offsetIndex) or 1) - 1) % #offsets + 1]
        return { x = baseX + offset[1], y = baseY + offset[2], z = baseZ }
    end

    local now = Util.timerNowMs()
    for key, expiresAt in pairs(RaidRuntime.teleportReservations) do
        if now >= (tonumber(expiresAt) or 0) then RaidRuntime.teleportReservations[key] = nil end
    end
    local cell = getCell and getCell()
    for _ = 1, 64 do
        -- The authored hideout corridor spans four tiles on either side of its
        -- center anchor. Raid insertion uses a compact seven-by-seven area.
        local dx = hideoutArrival and (ZombRand(9) - 4) or (ZombRand(7) - 3)
        local dy = hideoutArrival and 0 or (ZombRand(7) - 3)
        local x, y = baseX + dx, baseY + dy
        local key = tostring(x) .. ":" .. tostring(y) .. ":" .. tostring(baseZ)
        local available = RaidRuntime.teleportReservations[key] == nil
        if available then
            for _, other in ipairs(activePlayers()) do
                if not other:isDead() and math.floor(other:getX()) == x
                    and math.floor(other:getY()) == y and math.floor(other:getZ()) == baseZ then
                    available = false
                    break
                end
            end
        end
        if available and cell then
            local square = cell:getGridSquare(x, y, baseZ)
            if square ~= nil then
                if requireOutdoorLand == true then
                    available = Util.isSafeOutdoorLandSquare(square)
                else
                    available = square:getFloor() ~= nil and square:TreatAsSolidFloor()
                        and not square:isSolid() and not square:isSolidTrans()
                end
            end
        end
        if available then
            RaidRuntime.teleportReservations[key] = now + 5000
            return { x = x, y = y, z = baseZ }
        end
    end

    -- A fully occupied or not-yet-streamed destination still gets the legacy
    -- deterministic offset; the client's outdoor landing validator remains the
    -- final fallback for raid terrain.
    local offsets = { { 0, 0 }, { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } }
    local offset = offsets[((tonumber(offsetIndex) or 1) - 1) % #offsets + 1]
    return { x = baseX + offset[1], y = baseY + offset[2], z = baseZ }
end

local function teleport(player, point, offsetIndex, requireOutdoorLand, leaveVehicle)
    -- Moving a corpse interrupts Build 42's multiplayer death/corpse transition
    -- and can leave the client on a black screen without the death UI. The new
    -- living character will be returned to the hideout after respawn instead.
    if player == nil or player:isDead() or point == nil then return nil end
    local target = RaidRuntime.randomizedTeleportPoint(point, offsetIndex, requireOutdoorLand)
    local destination = {
        x = target.x + 0.5,
        y = target.y + 0.5,
        z = target.z,
        safeOutdoor = requireOutdoorLand == true,
        leaveVehicle = leaveVehicle == true,
    }
    deliver(player, "Teleport", destination)
    return destination
end

function RaidRuntime.teleport(player, point, offsetIndex, requireOutdoorLand, leaveVehicle)
    return teleport(player, point, offsetIndex, requireOutdoorLand, leaveVehicle)
end

local function pointInsideHideoutCell(point)
    if point == nil then return false end
    local bounds = Config.hideoutCellBounds()
    local x = tonumber(point.x)
    local y = tonumber(point.y)
    return x ~= nil and y ~= nil
        and x >= bounds.minX and x < bounds.maxXExclusive
        and y >= bounds.minY and y < bounds.maxYExclusive
end

local function validRaidResumePoint(point)
    return point ~= nil and tonumber(point.x) ~= nil and tonumber(point.y) ~= nil
        and not pointInsideHideoutCell(point)
end

function RaidRuntime.resumePointLabel(point)
    if point == nil then return "nil" end
    return tostring(tonumber(point.x) or "?") .. ","
        .. tostring(tonumber(point.y) or "?") .. ","
        .. tostring(tonumber(point.z) or 0)
end

function RaidRuntime.resumeTrace(status, message)
    if RaidRuntime.resumeStatus == status then return end
    RaidRuntime.resumeStatus = status
    Util.log("[RaidResume] " .. tostring(message))
end

function RaidRuntime.rememberSingleplayerRaidPosition(data, player, username, reason)
    if data == nil or player == nil or username == "" or player:isDead()
        or data.participants[username] ~= true or Threats.playerInsideHideoutCell(player) then return false end
    local point = {
        x = tonumber(player:getX()) or 0,
        y = tonumber(player:getY()) or 0,
        z = tonumber(player:getZ()) or 0,
        raidId = tonumber(data.raidId) or 0,
        teamKey = tostring(data.teamKey or RaidRuntime.contextKey or ""),
    }
    data.singleplayerRaidPositions = data.singleplayerRaidPositions or {}
    local firstCapture = data.singleplayerRaidPositions[username] == nil
    data.singleplayerRaidPositions[username] = point

    -- Keep the same point with the character as a second recovery source. Global
    -- raid data remains authoritative, but Build 42 saves the character and global
    -- mod-data through different paths during a quit-to-menu save.
    local playerData = player:getModData()
    playerData.ExtractionModeRaidResumePoint = point
    if reason ~= nil then
        pcall(function() player:transmitModData() end)
        Util.log("[RaidResume] " .. tostring(reason) .. " raid=" .. tostring(point.raidId)
            .. " team=" .. tostring(point.teamKey) .. " player=" .. tostring(username)
            .. " point=" .. RaidRuntime.resumePointLabel(point))
    elseif firstCapture then
        Util.log("[RaidResume] first field-position capture raid=" .. tostring(point.raidId)
            .. " team=" .. tostring(point.teamKey) .. " player=" .. tostring(username)
            .. " point=" .. RaidRuntime.resumePointLabel(point))
    end
    return true
end

-- Build 42 can occasionally reload a teleported single-player character at the
-- map's hideout spawn even though global raid data was saved correctly. Keep an
-- independent field position in global mod data and repair that mismatch only
-- during the short startup-resume window. Outside that window, entering the
-- hideout retains its normal extraction/reconciliation meaning.
local function processSingleplayerRaidPosition(data)
    if not singleplayerAuthority() then return false end
    local raidActive = data.state == Config.STATE_RAID
        or data.state == Config.STATE_EXTRACTING or data.state == Config.STATE_BOARDING
    -- Historical hideout raid records share this runtime with the one active
    -- raid. They must not cancel its global startup-resume window when pairs()
    -- happens to visit them first.
    if not raidActive then return false end

    data.singleplayerRaidPositions = data.singleplayerRaidPositions or {}
    local nowMs = Util.timerNowMs()
    local matchedParticipant = false
    for _, player in ipairs(Util.players()) do
        local username = Util.username(player)
        if username ~= "" and data.participants[username] == true and not player:isDead() then
            matchedParticipant = true
            if singleplayerRaidResumePending and Threats.playerInsideHideoutCell(player) then
                local savedPoint = data.singleplayerRaidPositions[username]
                local characterPoint = player:getModData().ExtractionModeRaidResumePoint
                if not validRaidResumePoint(savedPoint) and validRaidResumePoint(characterPoint)
                    and tonumber(characterPoint.raidId) == tonumber(data.raidId) then
                    savedPoint = characterPoint
                    data.singleplayerRaidPositions[username] = characterPoint
                    Util.log("[RaidResume] recovered missing global point from character mod-data"
                        .. " raid=" .. tostring(data.raidId) .. " player=" .. tostring(username)
                        .. " point=" .. RaidRuntime.resumePointLabel(characterPoint))
                end
                local restorePoint = validRaidResumePoint(savedPoint) and savedPoint or activeRaidSpawn(data)
                if validRaidResumePoint(restorePoint) and nowMs < singleplayerRaidResumeDeadlineMs then
                    teleport(player, restorePoint, 1, false)
                    RaidRuntime.resumeTrace("restore:" .. tostring(username) .. ":"
                        .. RaidRuntime.resumePointLabel(restorePoint),
                        "restore requested raid=" .. tostring(data.raidId)
                            .. " team=" .. tostring(data.teamKey) .. " player=" .. tostring(username)
                            .. " loaded=" .. RaidRuntime.resumePointLabel({
                                x = player:getX(), y = player:getY(), z = player:getZ(),
                            }) .. " target=" .. RaidRuntime.resumePointLabel(restorePoint)
                            .. (restorePoint == savedPoint and " source=field-position" or " source=insertion-fallback"))
                    if not singleplayerRaidResumeLogged then
                        singleplayerRaidResumeLogged = true
                        Util.log("Restoring single-player raid participant " .. tostring(username)
                            .. " from hideout spawn to " .. tostring(math.floor(tonumber(restorePoint.x)))
                            .. "," .. tostring(math.floor(tonumber(restorePoint.y)))
                            .. "," .. tostring(math.floor(tonumber(restorePoint.z) or 0))
                            .. (restorePoint == savedPoint and " (saved field position)" or " (raid insertion fallback)"))
                    end
                    -- Keep the resume window pending until a later tick observes
                    -- the character outside the hideout. This safely retries if
                    -- the first client-side teleport arrives before loading ends.
                    return true
                end
            elseif singleplayerRaidResumePending and not Threats.playerInsideHideoutCell(player) then
                -- The game restored the character correctly; no repair is needed.
                singleplayerRaidResumePending = false
                singleplayerRaidResumeLogged = false
                RaidRuntime.resumeTrace("complete:" .. tostring(username),
                    "resume complete raid=" .. tostring(data.raidId)
                        .. " team=" .. tostring(data.teamKey) .. " player=" .. tostring(username)
                        .. " live=" .. RaidRuntime.resumePointLabel({
                            x = player:getX(), y = player:getY(), z = player:getZ(),
                        }))
            end

            if not Threats.playerInsideHideoutCell(player) then
                RaidRuntime.rememberSingleplayerRaidPosition(data, player, username, nil)
            end
        end
    end

    if singleplayerRaidResumePending and not matchedParticipant then
        local liveNames = {}
        for _, player in ipairs(Util.players()) do
            liveNames[#liveNames + 1] = Util.username(player)
        end
        RaidRuntime.resumeTrace("waiting-identity:" .. table.concat(liveNames, ","),
            "waiting for saved participant identity raid=" .. tostring(data.raidId)
                .. " team=" .. tostring(data.teamKey)
                .. " savedParticipants=" .. table.concat(sortedNames(data.participants), ",")
                .. " livePlayers=" .. table.concat(liveNames, ","))
    end

    if singleplayerRaidResumePending and nowMs >= singleplayerRaidResumeDeadlineMs then
        singleplayerRaidResumePending = false
        singleplayerRaidResumeLogged = false
        Util.log("[RaidResume] resume window expired raid=" .. tostring(data.raidId)
            .. " team=" .. tostring(data.teamKey)
            .. " savedParticipants=" .. table.concat(sortedNames(data.participants), ","))
    end
    return false
end

function RaidRuntime.deferSingleplayerResumeStateRequest(data, player, username)
    if not singleplayerAuthority() or not singleplayerRaidResumePending
        or Threats.playerInsideHideoutCell(player) then return false end
    RaidRuntime.resumeTrace("defer-request:" .. tostring(username),
        "deferred hideout enforcement during state request"
            .. " routedTeam=" .. tostring(data.teamKey)
            .. " routedState=" .. tostring(data.state)
            .. " livePlayer=" .. tostring(username)
            .. " live=" .. RaidRuntime.resumePointLabel({
                x = player:getX(), y = player:getY(), z = player:getZ(),
            }))
    return true
end

function RaidRuntime.clearTransitionZombieTargets(player)
    if player == nil then return end
    local zombies = getCell and getCell() and getCell():getZombieList()
    if zombies == nil then return end
    for index = 0, zombies:size() - 1 do
        local zombie = zombies:get(index)
        if zombie ~= nil then
            local targeted = false
            pcall(function()
                targeted = zombie:getTarget() == player or zombie:getEatBodyTarget() == player
            end)
            if targeted then
                pcall(function() zombie:setTarget(nil) end)
                pcall(function() zombie:setEatBodyTarget(nil, false) end)
            end
        end
    end
end

function RaidRuntime.applyExtractionShove(player, point)
    if player == nil or point == nil then return 0 end
    local zombies = getCell and getCell() and getCell():getZombieList()
    if zombies == nil then return 0 end

    -- Use a private bare-hands instance so the engine can run its real shove
    -- consequences without granting the extracting player Strength XP.  A
    -- player's own BareHands object must not be mutated because it can be in
    -- use by another combat update on multiplayer servers.
    local shoveWeapon = nil
    if instanceItem ~= nil then
        pcall(function()
            shoveWeapon = instanceItem("Base.BareHands")
            if shoveWeapon ~= nil then
                shoveWeapon:setKnockBackOnNoDeath(false)
            end
        end)
    end

    local now = Util.timerNowMs()
    local recent = RaidRuntime.extractionShoveAt
    for zombie, pushedAt in pairs(recent) do
        if now - (tonumber(pushedAt) or 0) > 1000 then recent[zombie] = nil end
    end

    local centerX = tonumber(point.x) or tonumber(player:getX()) or 0
    local centerY = tonumber(point.y) or tonumber(player:getY()) or 0
    local centerZ = tonumber(point.z) or tonumber(player:getZ()) or 0
    local pushed = 0
    local networkRecords = {}
    for index = 0, zombies:size() - 1 do
        local zombie = zombies:get(index)
        local eligible = zombie ~= nil and recent[zombie] == nil
        if eligible then
            local checked, withinRadius = pcall(function()
                local aliveAndLevel = not zombie:isDead()
                    and not zombie:isOnFloor()
                    and math.abs((tonumber(zombie:getZ()) or centerZ) - centerZ) < 0.5
                local dx = (tonumber(zombie:getX()) or centerX) - centerX
                local dy = (tonumber(zombie:getY()) or centerY) - centerY
                return aliveAndLevel and dx * dx + dy * dy <= 4
            end)
            eligible = checked and withinRadius == true
        end
        if eligible then
            local success = pcall(function()
                local dx = (tonumber(zombie:getX()) or centerX) - centerX
                local dy = (tonumber(zombie:getY()) or centerY) - centerY
                local length = math.sqrt(dx * dx + dy * dy)
                if length < 0.001 then
                    dx = tonumber(player:getForwardDirectionX()) or 1
                    dy = tonumber(player:getForwardDirectionY()) or 0
                    length = math.sqrt(dx * dx + dy * dy)
                end
                if length < 0.001 then dx, dy, length = 1, 0, 1 end
                dx = dx / length
                dy = dy / length

                local victim = zombie:getTarget()
                zombie:setTarget(nil)
                zombie:setEatBodyTarget(nil, false)
                if victim ~= nil then
                    pcall(function()
                        if victim:getAttackedBy() == zombie then victim:setAttackedBy(nil) end
                        victim:setHitReaction("")
                        victim:setDeathDragDown(false)
                    end)
                end

                if not zombie:isOnFloor() and shoveWeapon ~= nil then
                    -- Hit() is synchronous. Temporarily flag the attacker as
                    -- shoving so Build 42 selects the same no-damage reaction,
                    -- force multiplier, deferred movement, and grab break used
                    -- by a normal player shove. Calling changeState directly
                    -- only selected StaggerBackState; that state does not move
                    -- the zombie on its own.
                    local wasShoving = player:isDoShove()
                    player:setDoShove(true)
                    local hitApplied, hitError = pcall(function()
                        zombie:Hit(shoveWeapon, player, 0, true, 1.0, false)
                    end)
                    player:setDoShove(wasShoving)
                    if not hitApplied then error(hitError) end
                elseif not zombie:isOnFloor() then
                    -- Compatibility fallback for runtimes or custom zombies
                    -- that cannot instantiate/use BareHands. Report the action
                    -- event instead of forcing the legacy AI state directly.
                    zombie:setAttackedBy(player)
                    zombie:getHitDir():set(dx, dy)
                    zombie:setHitForce(1.5)
                    zombie:setStaggerTimeMod(1.0)
                    zombie:setHitFromBehind(false)
                    zombie:setHitReaction("")
                    zombie:setStaggerBack(true)
                    zombie:reportEvent("wasHit")
                end
                -- Build 42 exposes NetworkZombieAI to Lua, but does not expose
                -- its extraUpdate method through Kahlua. The explicit client
                -- broadcast below is the supported synchronization path here.
                local onlineId = tonumber(zombie:getOnlineID()) or -1
                if onlineId >= 0 then
                    networkRecords[#networkRecords + 1] = {
                        id = onlineId,
                        dx = dx,
                        dy = dy,
                    }
                end
            end)
            if success then
                recent[zombie] = now
                pushed = pushed + 1
            end
        end
    end
    if pushed > 0 then
        Util.log("Extraction shove applied engine reaction to " .. tostring(pushed) .. " zombie(s) for "
            .. tostring(Util.username(player)))
    end
    if isServer and isServer() and #networkRecords > 0 then
        local payload = { token = now, zombies = networkRecords }
        -- Zombie simulation can be owned by a nearby client. Have every client
        -- with the zombie loaded apply the same reaction so its former attack
        -- state cannot immediately overwrite the server-side rescue.
        for _, listener in ipairs(activePlayers()) do
            deliver(listener, "ExtractionShove", payload)
        end
    end
    return pushed
end

function RaidRuntime.transitionProtectionActive(data, username)
    return data ~= nil and ((data.state == Config.STATE_TRANSIT
            and data.participants[username] == true)
        or (data.state == Config.STATE_BOARDING and data.boardingPending[username] ~= nil)
        or (data.groundExtractionPending[username] ~= nil
            and data.groundExtractionPending[username].phase == "FADING")
        or data.deathRescuePending[username] ~= nil
        or data.lateJoinPending[username] ~= nil
        or RaidRuntime.vehicleLateJoinFor(data, username) ~= nil)
end

local function protectBoardingPlayer(player, forceInvisible, keepZombieTargeting)
    if player == nil then return end
    local username = Util.username(player)
    if username == "" then return end
    local protection = boardingProtections[username]
    if protection == nil then
        protection = {
            wasInvincible = player:isInvincible(),
            wasIgnoredByZombies = player:isZombiesDontAttack(),
            wasInvisible = player:isInvisible(),
            wasAvoidDamage = player:avoidDamage(),
            failsafeAt = Util.timerNowMs() + 15000,
        }
        boardingProtections[username] = protection
    end
    protection.keepZombieTargeting = keepZombieTargeting == true
    if forceInvisible == true then protection.forceInvisible = true end
    pcall(function() player:setAvoidDamage(true) end)
    player:setInvincible(true)
    if protection.keepZombieTargeting then
        player:setZombiesDontAttack(protection.wasIgnoredByZombies == true)
        player:setInvisible(protection.wasInvisible == true, true)
    else
        player:setZombiesDontAttack(true)
        player:setInvisible(true, true)
        RaidRuntime.clearTransitionZombieTargets(player)
    end
    deliver(player, "BoardingProtection", {
        enabled = true,
        invincible = protection.wasInvincible == true,
        zombiesDontAttack = protection.wasIgnoredByZombies == true,
        invisible = protection.wasInvisible == true,
        forceInvisible = protection.forceInvisible == true,
        keepZombieTargeting = protection.keepZombieTargeting == true,
    })
end

local function releaseBoardingProtection(player, username)
    username = username or Util.username(player)
    local protection = boardingProtections[username]
    if protection == nil then return end
    if player then
        player:setInvincible(protection.wasInvincible == true)
        player:setZombiesDontAttack(protection.wasIgnoredByZombies == true)
        player:setInvisible(protection.wasInvisible == true, true)
        player:setAvoidDamage(protection.wasAvoidDamage == true)
        deliver(player, "BoardingProtection", {
            enabled = false,
            invincible = protection.wasInvincible == true,
            zombiesDontAttack = protection.wasIgnoredByZombies == true,
            invisible = protection.wasInvisible == true,
        })
    end
    if protection.context == "campaign_epilogue" then
        Util.log("Final quest epilogue protection released: player=" .. tostring(username)
            .. " connected=" .. tostring(player ~= nil)
            .. " restoredInvisible=" .. tostring(protection.wasInvisible == true)
            .. " restoredInvincible=" .. tostring(protection.wasInvincible == true)
            .. " restoredZombieIgnore=" .. tostring(protection.wasIgnoredByZombies == true))
    end
    boardingProtections[username] = nil
end

local function processBoardingProtections()
    if not tableHasEntries(boardingProtections) then return end
    local now = Util.timerNowMs()
    local playersByName = {}
    for _, player in ipairs(activePlayers()) do playersByName[Util.username(player)] = player end
    local hideout = Config.hideout()
    for username, protection in pairs(boardingProtections) do
        local player = playersByName[username]
        local data = player and RaidRuntime.dataForPlayer(player) or getStore()
        local releaseAt = tonumber(protection.releaseAt)
        local failsafeAt = tonumber(protection.failsafeAt) or now
        local transitionStillActive = player
            and RaidRuntime.transitionProtectionActive(data, username)
        if transitionStillActive then
            -- A lag spike must not let the old failsafe expire in the middle of
            -- a fade or teleport. Reassert authority until state advances.
            pcall(function() player:setAvoidDamage(true) end)
            player:setInvincible(true)
            if protection.keepZombieTargeting == true then
                player:setZombiesDontAttack(protection.wasIgnoredByZombies == true)
                player:setInvisible(protection.wasInvisible == true, true)
            else
                player:setZombiesDontAttack(true)
                player:setInvisible(true, true)
                RaidRuntime.clearTransitionZombieTargets(player)
            end
            protection.failsafeAt = now + 15000
        elseif player and ((releaseAt and now >= releaseAt
            and (protection.releaseAnywhere == true
                or Util.playerNear(player, hideout, (tonumber(hideout.radius) or 14) + 8)))
            or now >= failsafeAt) then
            releaseBoardingProtection(player, username)
        elseif player == nil and now >= failsafeAt then
            boardingProtections[username] = nil
        end
    end
end

local function allPlayersReady()
    local data = getStore()
    if data.selectedJoinRaidKey ~= nil then
        local target = RaidRuntime.getRootStore().raids[tostring(data.selectedJoinRaidKey)]
        if not RaidRuntime.isJoinableRaid(target) then return false end
    end
    local players = RaidRuntime.eligibleLobbyPlayers(data)
    if #players == 0 then return false end
    if type(data.vehicleInsertion) == "table" then
        local vehicle = RaidRuntime.insertionVehicle(data)
        local available, required, tank = RaidRuntime.vehicleFuelDetails(vehicle)
        local driver = nil
        if vehicle ~= nil then pcall(function() driver = vehicle:getDriver() end) end
        if vehicle == nil or driver == nil
            or (required > 0 and (tank == nil or available + 0.0001 < required)) then
            return false
        end
    end
    for _, player in ipairs(players) do
        if data.ready[Util.username(player)] ~= true then return false end
    end
    return true
end

local function startCountdown()
    local data = getStore()
    local target = data.selectedJoinRaidKey
        and RaidRuntime.getRootStore().raids[tostring(data.selectedJoinRaidKey)] or nil
    local joiningActiveRaid = RaidRuntime.isJoinableRaid(target)
    if Config.town(data.selectedTownKey) == nil
        or (not joiningActiveRaid and not Logistics.isTownAvailable(data, data.selectedTownKey)) then return end
    if type(data.vehicleInsertion) == "table" then
        local reserved, reserveError = ExtractionMode.GarageAuthority.reserveForRaid(
            RaidRuntime.getRootStore(), data.vehicleInsertion.vehicleId, data.teamKey)
        if not reserved then
            broadcastState()
            announceLocalized("IGUI_ExtractionMode_Error_VehicleReservationFailed",
                "Vehicle deployment cannot begin: " .. tostring(reserveError), {}, { teamOnly = true })
            return
        end
    end
    data.state = Config.STATE_COUNTDOWN
    data.countdownEndMs = Util.timerNowMs() + math.max(3, tonumber(Config.value("ReadyCountdownSeconds")) or 10) * 1000
    broadcastState()
    announceLocalized("IGUI_ExtractionMode_Message_DeploymentBegun",
        "All participating team members are ready. Raid deployment has begun.", {}, { teamOnly = true })
end

local function cancelCountdown()
    local data = getStore()
    RaidRuntime.releaseVehicleReservation(data)
    data.state = Config.STATE_HIDEOUT
    data.countdownEndMs = nil
    if RaidRuntime.readyEntryCount(data) == 0 then data.optedOut = {} end
    broadcastState()
    announceLocalized("IGUI_ExtractionMode_Message_DeploymentCancelledNotReady",
        "Raid deployment cancelled: a team member is no longer ready.", {}, { teamOnly = true })
end

function RaidRuntime.routePointUsedForExtraction(data, point)
    for _, site in ipairs(activeExtractionSites(data)) do
        if math.floor(tonumber(site.x) or 0) == math.floor(tonumber(point.x) or 0)
            and math.floor(tonumber(site.y) or 0) == math.floor(tonumber(point.y) or 0) then
            return true
        end
    end
    return false
end

function RaidRuntime.chooseLateJoinInsertion(data, vehicleInsertion)
    local town = Config.town(data.selectedTownKey)
    local candidates = {}
    local vehiclePoints = town and town.vehicleInsertionPoints or {}
    local points = vehicleInsertion == true and #vehiclePoints > 0
        and vehiclePoints or (town and town.points or {})
    for _, point in ipairs(points) do
        if not RaidRuntime.routePointUsedForExtraction(data, point) then candidates[#candidates + 1] = point end
    end
    if #candidates == 0 then return activeRaidSpawn(data) end
    local point = candidates[ZombRand(#candidates) + 1]
    return {
        x = point.x,
        y = point.y,
        z = point.z or 0,
        angleY = tonumber(point.angleY),
    }
end

function RaidRuntime.cancelLateJoin(data, username, player, messageKey, fallback)
    data.lateJoinPending[username] = nil
    data.lateInsertionCleanup[username] = nil
    if data.participants[username] ~= true then
        local root = RaidRuntime.getRootStore()
        if tostring(root.playerRaidKeys[username]) == tostring(data.teamKey) then
            root.playerRaidKeys[username] = nil
        end
    end
    releaseBoardingProtection(player, username)
    if player then
        deliver(player, "FadeIn", {
            seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
        })
        if messageKey then deliverLocalized(player, "Error", messageKey, fallback) end
    end
end

function RaidRuntime.processLateJoins(data)
    local changed = false
    local now = Util.timerNowMs()
    local playersByName = {}
    for _, player in ipairs(activePlayers()) do playersByName[Util.username(player)] = player end

    for username, cleanup in pairs(data.lateInsertionCleanup or {}) do
        if now <= (tonumber(cleanup.untilMs) or 0) then
            RaidRoutes.clearInsertionZombies(cleanup.point)
        else
            data.lateInsertionCleanup[username] = nil
        end
    end

    for username, pending in pairs(data.lateJoinPending or {}) do
        local player = playersByName[username]
        local validRaid = (data.state == Config.STATE_RAID or data.state == Config.STATE_EXTRACTING
            or (data.state == Config.STATE_BOARDING and pending.phase == "ARRIVING"
                and data.participants[username] == true))
            and tonumber(pending.raidId) == tonumber(data.raidId)
        if not validRaid then
            RaidRuntime.cancelLateJoin(data, username, player,
                "IGUI_ExtractionMode_Error_JoinRaidClosed",
                "That raid can no longer be joined.")
            changed = true
        elseif player == nil or player:isDead() then
            RaidRuntime.cancelLateJoin(data, username, player, nil, nil)
            changed = true
        else
            if pending.phase == "WAITING" and now >= (tonumber(pending.fadeAtMs) or now) then
                protectBoardingPlayer(player, true)
                deliver(player, "FadeOut", {
                    seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
                    leaveVehicle = true,
                })
                pending.phase = "FADING"
                changed = true
            end
            if pending.phase ~= "ARRIVING" and now >= (tonumber(pending.deadlineMs) or now) then
                if not Threats.playerInsideHideoutCell(player) then
                    RaidRuntime.cancelLateJoin(data, username, player,
                        "IGUI_ExtractionMode_Error_JoinRaidHideoutOnly",
                        "Remain inside the hideout until insertion.")
                else
                    local point = RaidRuntime.chooseLateJoinInsertion(data)
                    RaidRoutes.clearInsertionZombies(point)
                    data.participants[username] = true
                    data.extractedPlayers[username] = nil
                    data.returnPending[username] = nil
                    data.disconnectedRaidPlayers[username] = nil
                    data.optedOut[username] = nil
                    RaidFlares.giveFlare(player)
                    teleport(player, point, ZombRand(8) + 1, true, true)
                    data.lateInsertionCleanup[username] = {
                        point = point,
                        untilMs = now + 5000,
                    }
                    local protection = boardingProtections[username]
                    if protection then
                        protection.releaseAt = now + 5500
                        protection.releaseAnywhere = true
                    end
                    pending.phase = "ARRIVING"
                    pending.arrivalAtMs = now + 1500
                    changed = true
                end
            elseif pending.phase == "ARRIVING"
                and now >= (tonumber(pending.arrivalAtMs) or now) then
                data.lateJoinPending[username] = nil
                deliver(player, "FadeIn", {
                    seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
                })
                deliverLocalized(player, "Announcement", "IGUI_ExtractionMode_Message_JoinedRaid",
                    "Joined the active faction raid in "
                        .. tostring((Config.town(data.selectedTownKey) or {}).name or "the raid zone") .. ".",
                    { key = "IGUI_ExtractionMode_Town_" .. tostring(data.selectedTownKey),
                        fallback = tostring((Config.town(data.selectedTownKey) or {}).name or "the raid zone") })
                changed = true
            end
        end
    end
    return changed or tableHasEntries(data.lateJoinPending)
end

function RaidRuntime.cancelVehicleLateJoin(data, vehicleId, pending, messageKey, fallback)
    local root = RaidRuntime.getRootStore()
    ExtractionMode.GarageAuthority.releaseRaidReservation(
        root, vehicleId, pending and pending.reservationRaidKey)
    for username in pairs(pending and pending.participantNames or {}) do
        local player = RaidRuntime.playerByUsername(username)
        if data.participants[username] ~= true
            and tostring(root.playerRaidKeys[username]) == tostring(data.teamKey) then
            root.playerRaidKeys[username] = nil
        end
        releaseBoardingProtection(player, username)
        if player ~= nil then
            deliver(player, "FadeIn", { seconds = tonumber(Config.value("TransitFadeSeconds")) or 1 })
            if messageKey ~= nil then deliverLocalized(player, "Error", messageKey, fallback) end
        end
    end
    data.lateJoinVehicles[tostring(vehicleId)] = nil
end

local function captureRaidVehicleRecord(root, vehicle)
    local snapshot, captureError = ExtractionMode.Garage.captureVehicle(vehicle)
    if snapshot == nil then return nil, captureError end
    local active = root and root.activeHideoutVehicle or nil
    if type(active) == "table" and tostring(active.vehicleId) == tostring(vehicle:getId())
        and type(active.record) == "table" then
        snapshot = ExtractionMode.Garage.mergeRecord(snapshot, active.record)
        -- The hideout movement lock temporarily sets engine power to zero. Keep
        -- the real stored engine features when constructing the raid copy.
        snapshot.engineQuality = active.record.engineQuality
        snapshot.engineLoudness = active.record.engineLoudness
        snapshot.enginePower = active.record.enginePower
    end
    if (tonumber(snapshot.enginePower) or 0) <= 0 then
        pcall(function()
            local script = vehicle:getScript()
            if script ~= nil then
                snapshot.enginePower = math.max(1,
                    math.floor(tonumber(script:getEngineForce()) or 0))
                if type(active) == "table" and type(active.record) == "table" then
                    active.record.enginePower = snapshot.enginePower
                end
            end
        end)
    end
    return snapshot
end

-- Capture and remove the source vehicle only after its complete persistent
-- state and seat manifest are safe. Players stream the distant road while the
-- screen is black; processRaidVehicleRebuild creates a fresh network vehicle in
-- that loaded cell and asks each owning client to reclaim its original seat.
function RaidRuntime.stageRaidVehicleRebuild(data, pending, vehicle, point)
    if type(pending) ~= "table" or vehicle == nil or point == nil then
        return false, "Vehicle reconstruction could not begin."
    end
    local hasInteriorOccupants = ModCompatibility.hasRemoteRVOccupants(
        vehicle, activePlayers())
    if hasInteriorOccupants then
        return false, "The RV cannot enter a raid while someone is inside its interior."
    end
    local root = RaidRuntime.getRootStore()
    local active = root.activeHideoutVehicle
    local owner = type(active) == "table"
        and tostring(active.vehicleId) == tostring(vehicle:getId()) and active.owner or nil
    local driver = nil
    pcall(function() driver = vehicle:getDriver() end)
    owner = tostring(owner or (driver and Util.garageUsername(driver)) or "")

    -- A key left in the ignition belongs to the driver, not to the disposable
    -- network entity. Return it before taking the snapshot/removing the source.
    if driver ~= nil then
        local keyReturned, keyResult = ExtractionMode.Garage.returnIgnitionKeyToDriver(vehicle, driver)
        if not keyReturned then
            Util.log("Raid vehicle rebuild could not retrieve ignition key vehicle="
                .. tostring(vehicle:getId()) .. " error=" .. tostring(keyResult))
        end
    end

    local record, captureError = captureRaidVehicleRecord(root, vehicle)
    if record == nil then return false, captureError end
    local seats = {}
    local participantIndex = 0
    for username in pairs(pending.participantNames or {}) do
        local participant = RaidRuntime.playerByUsername(username)
        local seat = -1
        if participant ~= nil and participant:getVehicle() == vehicle then
            pcall(function() seat = vehicle:getSeat(participant) end)
        end
        seats[username] = seat
        participantIndex = participantIndex + 1
    end

    local preloadPoint = {
        x = (tonumber(point.x) or 0) + 6,
        y = (tonumber(point.y) or 0) + 6,
        z = tonumber(point.z) or 0,
    }
    participantIndex = 0
    for username in pairs(pending.participantNames or {}) do
        local participant = RaidRuntime.playerByUsername(username)
        if participant ~= nil then
            participantIndex = participantIndex + 1
            pcall(function() vehicle:exit(participant) end)
            teleport(participant, preloadPoint, participantIndex, true, true)
        end
    end
    local originalVehicleId = tostring(vehicle:getId())
    local sourceToken = tostring(data.teamKey or RaidRuntime.contextKey or "raid") .. ":"
        .. tostring(data.raidId or 0) .. ":" .. originalVehicleId .. ":"
        .. tostring(Util.timerNowMs())
    -- Keep the source registered when its chunk remains loaded until the
    -- destination copy receives an ID. If streaming unloads it first, the token
    -- below distinguishes its serialized body even when VehicleIDMap recycles
    -- the same short ID for the raid copy.
    pcall(function()
        vehicle:getModData().ExtractionModeRaidRebuildSource = true
        vehicle:getModData().ExtractionModeRaidRebuildSourceToken = sourceToken
        if vehicle.transmitModData then vehicle:transmitModData() end
        vehicle:setForceBrake()
    end)
    pending.rebuild = {
        record = record,
        owner = owner,
        seats = seats,
        point = {
            x = math.floor(tonumber(point.x) or 0),
            y = math.floor(tonumber(point.y) or 0),
            z = math.floor(tonumber(point.z) or 0),
            angleY = tonumber(pending.angleY) or tonumber(pending.angleZ) or 0,
        },
        originalVehicleId = originalVehicleId,
        sourceToken = sourceToken,
        startedAtMs = Util.timerNowMs(),
        deadlineMs = Util.timerNowMs() + 15000,
        lastError = nil,
    }
    Util.log("Captured raid vehicle for destination reconstruction vehicle="
        .. originalVehicleId .. " occupants=" .. tostring(participantIndex)
        .. " target=" .. tostring(pending.rebuild.point.x) .. ","
        .. tostring(pending.rebuild.point.y) .. "," .. tostring(pending.rebuild.point.z)
        .. " heading=" .. tostring(pending.rebuild.point.angleY))
    return true
end

function RaidRuntime.processRaidVehicleRebuild(data, pending)
    local rebuild = pending and pending.rebuild
    if type(rebuild) ~= "table" or type(rebuild.record) ~= "table" then
        return false, "Vehicle reconstruction state is unavailable.", true
    end
    local now = Util.timerNowMs()
    if now < (tonumber(rebuild.nextAttemptMs) or 0) then
        return false, rebuild.lastError, false
    end
    rebuild.nextAttemptMs = now + 250
    local vehicle, spawnError = ExtractionMode.GarageAuthority.spawnSnapshotAt(
        rebuild.record, rebuild.point, { deferEngineNetwork = true })
    if vehicle == nil then
        rebuild.lastError = tostring(spawnError or "vehicle creation failed")
        local expired = now >= (tonumber(rebuild.deadlineMs) or 0)
        return false, rebuild.lastError, expired
    end

    local newVehicleId = tostring(vehicle:getId())
    local sourceVehicle = RaidRuntime.vehicleById(rebuild.originalVehicleId)
    -- Once the source chunk unloads, PZ may recycle its short ID for this new
    -- destination copy. Never pass the destination back as the removal target,
    -- but always commit/clear the active hideout source even when the IDs match.
    local removalTarget = sourceVehicle ~= vehicle and sourceVehicle or nil
    local sourceReleased = ExtractionMode.GarageAuthority.releaseRaidRebuildSource(
        RaidRuntime.getRootStore(), rebuild.originalVehicleId, removalTarget,
        rebuild.sourceToken)
    if not sourceReleased and sourceVehicle ~= nil and sourceVehicle ~= vehicle then
        local removed, removeError = ExtractionMode.GarageAuthority.removeVehicleNow(sourceVehicle)
        if not removed then
            pcall(function() sourceVehicle:permanentlyRemove() end)
            Util.log("Raid vehicle source removal needed fallback vehicle="
                .. tostring(rebuild.originalVehicleId) .. " error=" .. tostring(removeError))
        end
    end
    pcall(function()
        -- Restore the drivetrain without sending an engine-state packet before
        -- clients have initialized sounds and their local Bullet controller.
        ExtractionMode.GarageAuthority.releaseMovementLock(
            vehicle, rebuild.record, false)
        vehicle:getModData().ExtractionModeRaidFuelCost = tonumber(pending.fuelSpent) or 0
    end)
    pending.vehicleId = newVehicleId
    pending.spawnedVehicleId = newVehicleId
    for username, seat in pairs(rebuild.seats or {}) do
        local participant = RaidRuntime.playerByUsername(username)
        if participant ~= nil then
            deliver(participant, "SeatRaidVehicle", {
                vehicleId = newVehicleId,
                seat = tonumber(seat) or -1,
                isDriver = tonumber(seat) == 0,
                engineQuality = tonumber(rebuild.record.engineQuality) or 0,
                engineLoudness = tonumber(rebuild.record.engineLoudness) or 0,
                enginePower = tonumber(rebuild.record.enginePower) or 0,
            })
        end
    end
    Util.log("Reconstructed raid vehicle old=" .. tostring(rebuild.originalVehicleId)
        .. " new=" .. newVehicleId .. " target=" .. tostring(rebuild.point.x)
        .. "," .. tostring(rebuild.point.y) .. "," .. tostring(rebuild.point.z)
        .. " enginePower=" .. tostring(vehicle:getEnginePower()))
    rebuild.record = nil
    return true, vehicle, false
end

function RaidRuntime.recoverFailedRaidVehicleRebuild(pending)
    local rebuild = pending and pending.rebuild
    if type(rebuild) ~= "table" or type(rebuild.record) ~= "table" then return false end
    local owner = tostring(rebuild.owner or "")
    if owner == "" then return false end
    local root = RaidRuntime.getRootStore()
    local sourceVehicle = RaidRuntime.vehicleById(rebuild.originalVehicleId)
    if sourceVehicle ~= nil and pending ~= nil
        and tostring(sourceVehicle:getId()) == tostring(pending.spawnedVehicleId or "") then
        sourceVehicle = nil
    end
    local sourceReleased = ExtractionMode.GarageAuthority.releaseRaidRebuildSource(
        root, rebuild.originalVehicleId, sourceVehicle, rebuild.sourceToken)
    if not sourceReleased and sourceVehicle ~= nil then
        local removed = ExtractionMode.GarageAuthority.removeVehicleNow(sourceVehicle)
        if not removed then pcall(function() sourceVehicle:permanentlyRemove() end) end
    end
    local restored = ExtractionMode.Garage.put(root, owner, rebuild.record)
    if restored then
        ExtractionMode.Garage.refreshBackup(root, "failed raid vehicle reconstruction recovered")
        Util.log("Returned failed raid vehicle reconstruction to " .. owner
            .. "'s garage error=" .. tostring(rebuild.lastError))
        rebuild.record = nil
    end
    return restored == true
end

function RaidRuntime.processVehicleLateJoins(data)
    local now = Util.timerNowMs()
    local changed = false
    local root = RaidRuntime.getRootStore()
    for vehicleId, pending in pairs(data.lateJoinVehicles or {}) do
        local validRaid = (data.state == Config.STATE_RAID or data.state == Config.STATE_EXTRACTING
            or (data.state == Config.STATE_BOARDING
                and (pending.phase == "REBUILDING" or pending.phase == "ARRIVING")))
            and tonumber(pending.raidId) == tonumber(data.raidId)
        if not validRaid then
            RaidRuntime.cancelVehicleLateJoin(data, vehicleId, pending,
                "IGUI_ExtractionMode_Error_JoinRaidClosed", "That raid can no longer be joined.")
            changed = true
        elseif pending.phase == "FADING" and now >= (tonumber(pending.deadlineMs) or now) then
            local vehicle = RaidRuntime.insertionVehicle({
                vehicleInsertion = { vehicleId = vehicleId },
            })
            local currentOccupants = {}
            if vehicle ~= nil then
                for _, candidate in ipairs(activePlayers()) do
                    if candidate:getVehicle() == vehicle then
                        currentOccupants[Util.username(candidate)] = true
                    end
                end
            end
            local rosterMatches = vehicle ~= nil
            for username in pairs(pending.participantNames or {}) do
                if currentOccupants[username] ~= true then rosterMatches = false break end
            end
            if rosterMatches then
                for username in pairs(currentOccupants) do
                    if pending.participantNames[username] ~= true then rosterMatches = false break end
                end
            end
            local driver = nil
            if vehicle ~= nil then pcall(function() driver = vehicle:getDriver() end) end
            if driver == nil or type(pending.participantNames) ~= "table"
                or pending.participantNames[Util.username(driver)] ~= true then
                rosterMatches = false
            end

            local available, required, tank = RaidRuntime.vehicleFuelDetails(vehicle)
            if not rosterMatches then
                RaidRuntime.cancelVehicleLateJoin(data, vehicleId, pending,
                    "IGUI_ExtractionMode_Error_VehicleRosterChanged",
                    "Vehicle raid join cancelled: the driver or occupant roster changed.")
                changed = true
            elseif required > 0 and (tank == nil or available + 0.0001 < required) then
                RaidRuntime.cancelVehicleLateJoin(data, vehicleId, pending,
                    "IGUI_ExtractionMode_Error_NotEnoughGas", "Not enough gas!")
                changed = true
            else
                local point = RaidRuntime.chooseLateJoinInsertion(data, true)
                pending.angleY = tonumber(point and point.angleY)
                    or tonumber(pending.angleY) or tonumber(pending.angleZ) or 0
                RaidRoutes.clearInsertionZombies(point)
                local charged = RaidRuntime.chargeVehicleRaidFuel(vehicle, false)
                pending.fuelSpent = required
                local staged, stageError = false, nil
                if charged then
                    staged, stageError = RaidRuntime.stageRaidVehicleRebuild(
                        data, pending, vehicle, point)
                end
                if (not charged or not staged) and tank ~= nil then
                    pcall(function()
                        tank:setContainerContentAmount(available, false, true)
                        vehicle:transmitPartModData(tank)
                    end)
                end
                if not charged or not staged then
                    RaidRuntime.cancelVehicleLateJoin(data, vehicleId, pending,
                        "IGUI_ExtractionMode_Error_VehicleInsertionFailed",
                        "Vehicle raid join cancelled: the vehicle could not be inserted.")
                    Util.log("Vehicle late-join reconstruction staging failed vehicle="
                        .. tostring(vehicleId) .. " error=" .. tostring(stageError))
                    changed = true
                else
                    for username in pairs(pending.participantNames) do
                        local participant = RaidRuntime.playerByUsername(username)
                        data.participants[username] = true
                        data.extractedPlayers[username] = nil
                        data.returnPending[username] = nil
                        data.disconnectedRaidPlayers[username] = nil
                        data.optedOut[username] = nil
                        if participant ~= nil then RaidFlares.giveFlare(participant) end
                        local protection = boardingProtections[username]
                        if protection then
                            protection.releaseAt = now + 5500
                            protection.releaseAnywhere = true
                        end
                    end
                    pending.phase = "REBUILDING"
                    pending.deadlineMs = now + 15000
                    pending.point = point
                    Util.log("Vehicle late join staged for reconstruction vehicle="
                        .. tostring(vehicleId)
                        .. " raid=" .. tostring(data.raidId)
                        .. " fuel=" .. tostring(required))
                    changed = true
                end
            end
        elseif pending.phase == "REBUILDING" then
            local rebuilt, rebuildResult, rebuildExpired = RaidRuntime.processRaidVehicleRebuild(
                data, pending)
            if rebuilt then
                pending.phase = "ARRIVING"
                pending.deadlineMs = now + 5000
                data.lateInsertionCleanup["vehicle:" .. tostring(pending.vehicleId)] = {
                    point = pending.point,
                    untilMs = now + 5000,
                }
                Util.log("Vehicle joined active raid after reconstruction vehicle="
                    .. tostring(pending.vehicleId) .. " raid=" .. tostring(data.raidId)
                    .. " fuel=" .. tostring(pending.fuelSpent))
                changed = true
            elseif rebuildExpired then
                RaidRuntime.recoverFailedRaidVehicleRebuild(pending)
                pending.phase = "ARRIVING"
                pending.deadlineMs = now + 1000
                for username in pairs(pending.participantNames or {}) do
                    local player = RaidRuntime.playerByUsername(username)
                    if player ~= nil then
                        deliverLocalized(player, "Error",
                            "IGUI_ExtractionMode_Error_VehicleInsertionFailed",
                            "The vehicle could not be inserted and was returned to its owner's garage.")
                    end
                end
                Util.log("Vehicle late-join reconstruction timed out vehicle="
                    .. tostring(vehicleId) .. " error=" .. tostring(rebuildResult))
                changed = true
            end
        elseif pending.phase == "ARRIVING" and now >= (tonumber(pending.deadlineMs) or now) then
            for username in pairs(pending.participantNames or {}) do
                local player = RaidRuntime.playerByUsername(username)
                if player ~= nil then
                    deliver(player, "FadeIn", {
                        seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
                    })
                    deliverLocalized(player, "Announcement", "IGUI_ExtractionMode_Message_JoinedRaid",
                        "Joined the active faction raid in "
                            .. tostring((Config.town(data.selectedTownKey) or {}).name or "the raid zone") .. ".",
                        { key = "IGUI_ExtractionMode_Town_" .. tostring(data.selectedTownKey),
                            fallback = tostring((Config.town(data.selectedTownKey) or {}).name or "the raid zone") })
                end
            end
            data.lateJoinVehicles[tostring(vehicleId)] = nil
            changed = true
        end
    end
    return changed or tableHasEntries(data.lateJoinVehicles)
end

function RaidRuntime.beginVehicleLateJoin(data, players)
    local root = RaidRuntime.getRootStore()
    local targetKey = data.selectedJoinRaidKey and tostring(data.selectedJoinRaidKey) or nil
    local target = targetKey and root.raids[targetKey] or nil
    local vehicle = RaidRuntime.insertionVehicle(data)
    local driver = nil
    if vehicle ~= nil then pcall(function() driver = vehicle:getDriver() end) end
    if not RaidRuntime.isJoinableRaid(target) then
        RaidRuntime.releaseVehicleReservation(data)
        data.state = Config.STATE_HIDEOUT
        data.ready = {}
        data.countdownEndMs = nil
        data.selectedJoinRaidKey = nil
        data.vehicleInsertion = nil
        broadcastState()
        announceLocalized("IGUI_ExtractionMode_Error_JoinRaidClosed",
            "That raid can no longer be joined.", {}, { teamOnly = true })
        return false
    end
    if vehicle == nil or driver == nil or #players == 0 then
        cancelCountdown()
        return false
    end
    local available, required, tank = RaidRuntime.vehicleFuelDetails(vehicle)
    if required > 0 and (tank == nil or available + 0.0001 < required) then
        RaidRuntime.releaseVehicleReservation(data)
        data.state = Config.STATE_HIDEOUT
        data.ready = {}
        data.countdownEndMs = nil
        broadcastState()
        announceLocalized("IGUI_ExtractionMode_Error_NotEnoughGas", "Not enough gas!", {},
            { teamOnly = true })
        return false
    end

    target.lateJoinVehicles = target.lateJoinVehicles or {}
    local vehicleId = tostring(vehicle:getId())
    if target.lateJoinVehicles[vehicleId] ~= nil then
        RaidRuntime.releaseVehicleReservation(data)
        data.state = Config.STATE_HIDEOUT
        data.ready = {}
        data.countdownEndMs = nil
        broadcastState()
        announceLocalized("IGUI_ExtractionMode_Error_VehicleInsertionFailed",
            "That vehicle already has a pending raid insertion.", {}, { teamOnly = true })
        return false
    end
    local participantNames = {}
    for _, participant in ipairs(players) do
        participantNames[Util.username(participant)] = true
    end
    target.lateJoinVehicles[vehicleId] = {
        raidId = target.raidId,
        vehicleId = vehicleId,
        reservationRaidKey = tostring(data.teamKey),
        participantNames = participantNames,
        phase = "FADING",
        deadlineMs = Util.timerNowMs()
            + math.max(1, tonumber(Config.value("TransitFadeSeconds")) or 1) * 1000,
        angleY = tonumber(vehicle:getAngleY()) or 0,
    }
    for _, participant in ipairs(players) do
        local participantName = Util.username(participant)
        protectBoardingPlayer(participant, true)
        root.playerRaidKeys[participantName] = targetKey
        deliver(participant, "FadeOut", {
            seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
            vehicleId = tostring(vehicleId),
        })
    end
    data.state = Config.STATE_HIDEOUT
    data.ready = {}
    data.optedOut = {}
    data.countdownEndMs = nil
    data.selectedTownKey = nil
    data.selectedTownBy = nil
    data.selectedJoinRaidKey = nil
    data.vehicleInsertion = nil
    broadcastState()
    return true
end

local function finishRaidTransit()
    local data = getStore()
    data.state = Config.STATE_RAID
    data.ready = {}
    data.transitPhase = nil
    data.transitDeadlineMs = nil
    data.raidStartHour = Util.worldHours()
    data.raidStartMs = Util.timerNowMs()
    local minimum, maximum, minimumMinutes, maximumMinutes = Threats.hordeDelayBounds(data)
    data.hordeWindowStartHour = data.raidStartHour + minimum
    data.hordeWindowEndHour = data.raidStartHour + maximum
    data.hordeAtHour = data.raidStartHour
        + RaidRoutes.rollHordeDelayHours(minimumMinutes, maximumMinutes)
    data.hordeSpawned = false
    BanditsIntegration.beginRaid(data)
    RaidQuests.resetRaidVisitObjectivesForParticipants(data)
    prepareRaidQuestItems(data)
    ensureRaidQuestItems()
    local replacementChance = math.max(0, math.min(100,
        tonumber(Config.value("BanditHordeReplacementChancePercent")) or 20))
    data.hordeEventType = BanditsIntegration.isAvailable()
        and ZombRand(10000) < math.floor(replacementChance * 100)
        and "BANDITS" or "ZOMBIES"
    data.insertionCleanupUntilMs = Util.timerNowMs() + 5000
    -- Keep participants invisible and invulnerable until the full streamed-in
    -- zombie cleanup window has elapsed. Releasing protection after only the
    -- fade allowed a newly loaded zombie to begin a grapple between one-second
    -- cleanup passes.
    local protectionReleaseAt = data.insertionCleanupUntilMs + 500
    for _, player in ipairs(activePlayers()) do
        local username = Util.username(player)
        if data.participants[username] == true then
            deliver(player, "FadeIn", { seconds = tonumber(Config.value("TransitFadeSeconds")) or 1 })
            local protection = boardingProtections[username]
            if protection then
                protection.releaseAt = protectionReleaseAt
                protection.releaseAnywhere = true
            end
        end
    end
    RaidRoutes.clearInsertionZombies(activeRaidSpawn(data))
    broadcastState()
    local townName = tostring((Config.town(data.selectedTownKey) or {}).name or "the selected town")
    announceLocalized("IGUI_ExtractionMode_Message_RaidStarted",
        "Raid started in " .. townName
        .. ". Extraction sites and the outer raid boundary are marked on the map. Cross the boundary to escape without the helicopter. Vehicles are left behind. A major horde is approaching.",
        { { key = "IGUI_ExtractionMode_Town_" .. tostring(data.selectedTownKey), fallback = townName } }, {
        audioCue = "raid_start",
        participantsOnly = true,
        extendedHalo = true,
    })
end

function RaidRuntime.relocateInsertionVehicle(vehicle, point, heading)
    if vehicle == nil or point == nil then return false end
    -- The owning player must stream the distant destination before its local
    -- Bullet simulation can accept a persistent teleport. The client performs
    -- the actual transform after that stream completes; authority only releases
    -- the garage movement lock and prepares the running vehicle here.
    local moved, moveError = pcall(function()
        ExtractionMode.GarageAuthority.releaseMovementLock(vehicle)
        vehicle:setForceBrake()
        vehicle:setSpeedKmHour(0)
        -- Vehicle insertion is presented as a rolling road arrival. Put the
        -- authoritative engine into its running state while the screen is black;
        -- the owning client applies brief forward throttle immediately before
        -- FadeIn and releases it to the driver a moment later.
        if vehicle:isEngineRunning() ~= true then vehicle:engineDoRunning() end
        vehicle:transmitEngine()
        vehicle:updatePhysicsNetwork()
    end)
    if not moved then
        Util.log("Vehicle raid relocation failed vehicle=" .. tostring(vehicle:getId())
            .. " error=" .. tostring(moveError))
        return false
    end
    return true
end

local function beginRaidTransit()
    local data = getStore()
    RaidRuntime.reconcileVehicleInsertion(data)
    local players = RaidRuntime.eligibleLobbyPlayers(data)
    if #players == 0 or not allPlayersReady() then cancelCountdown(); return end
    if data.selectedJoinRaidKey ~= nil and type(data.vehicleInsertion) == "table" then
        RaidRuntime.beginVehicleLateJoin(data, players)
        return
    end

    local town = Config.town(data.selectedTownKey)
    if town == nil then cancelCountdown(); return end
    local spawn, extractionSites = RaidRoutes.chooseRaidRoute(
        town, type(data.vehicleInsertion) == "table")
    local minimumExtractions = math.max(1,
        math.floor(tonumber(town.minimumExtractionSites) or 2))
    if spawn == nil or extractionSites == nil or #extractionSites < minimumExtractions then
        RaidRuntime.releaseVehicleReservation(data)
        data.state = Config.STATE_HIDEOUT
        data.ready = {}
        data.countdownEndMs = nil
        broadcastState()
        announceLocalized("IGUI_ExtractionMode_Message_DeploymentCancelledNoRoute",
            "Raid deployment cancelled: no valid route points were available.", {}, { teamOnly = true })
        return
    end

    local root = RaidRuntime.getRootStore()
    local insertionVehicle = nil
    if type(data.vehicleInsertion) == "table" then
        insertionVehicle = RaidRuntime.insertionVehicle(data)
        local available, required, tank = RaidRuntime.vehicleFuelDetails(insertionVehicle)
        if insertionVehicle == nil
            or (required > 0 and (tank == nil or available + 0.0001 < required)) then
            RaidRuntime.releaseVehicleReservation(data)
            data.state = Config.STATE_HIDEOUT
            data.ready = {}
            data.countdownEndMs = nil
            broadcastState()
            announceLocalized("IGUI_ExtractionMode_Error_NotEnoughGas", "Not enough gas!", {},
                { teamOnly = true })
            return
        end
        local charged = true
        if required > 0 then
            charged = pcall(function()
                tank:setContainerContentAmount(math.max(0, available - required), false, true)
                insertionVehicle:transmitPartModData(tank)
            end)
        end
        if not charged then
            RaidRuntime.releaseVehicleReservation(data)
            data.state = Config.STATE_HIDEOUT
            data.ready = {}
            data.countdownEndMs = nil
            broadcastState()
            announceLocalized("IGUI_ExtractionMode_Error_VehicleFuelCharge",
                "Vehicle deployment cancelled: the fuel charge could not be applied.", {},
                { teamOnly = true })
            return
        end
        data.vehicleInsertion.fuelSpent = required
        data.vehicleInsertion.availableFuel = math.max(0, available - required)
        data.vehicleInsertion.angleY = tonumber(spawn.angleY)
            or tonumber(insertionVehicle:getAngleY()) or 0
        data.vehicleInsertion.angleZ = nil
        data.vehicleInsertion.participantNames = {}
        pcall(function()
            local modData = insertionVehicle:getModData()
            modData.ExtractionModeRaidFuelCost = required
        end)
        Util.log("Vehicle raid fuel charged vehicle=" .. tostring(insertionVehicle:getId())
            .. " liters=" .. tostring(required)
            .. " remaining=" .. tostring(data.vehicleInsertion.availableFuel))
    end

    -- Establish server/client immunity and invisibility before changing state.
    -- Invisibility prevents an already-loaded zombie from beginning a grapple
    -- during the teleport frame, before the destination's streamed zombies can
    -- be enumerated and removed.
    for _, player in ipairs(players) do protectBoardingPlayer(player, true) end

    root.nextRaidId = (tonumber(root.nextRaidId) or 0) + 1
    data.raidId = root.nextRaidId
    data.state = Config.STATE_TRANSIT
    RaidRuntime.rotateFactionStagingRaid(data)
    data.optedOut = {}
    data.participants = {}
    data.lateJoinPending = {}
    data.lateJoinVehicles = {}
    data.lateInsertionCleanup = {}
    data.extractedPlayers = {}
    data.groundExtractionPending = {}
    data.groundExtractionVehicles = {}
    data.deathRescuePending = {}
    data.singleplayerRaidPositions = {}
    data.flareUpgradePending = {}
    data.countdownEndMs = nil
    data.raidSpawn = spawn
    data.extractionSites = extractionSites
    data.extractionFlareSpawned = {}
    data.transitPhase = "FADE_OUT"
    data.transitDeadlineMs = Util.timerNowMs() + math.max(1, tonumber(Config.value("TransitFadeSeconds")) or 1) * 1000
    data.activeExtraction = nil
    data.extractionEndMs = nil
    data.banditEncounterRaidId = nil
    data.banditEncounterState = nil
    data.banditEncounterAtHour = nil
    data.banditEncounterAttempts = nil
    data.banditEncounterClan = nil
    data.banditEncounterSpawnedCount = nil
    data.banditRaidIds = {}

    for _, player in ipairs(players) do
        local username = Util.username(player)
        root.playerRaidKeys[username] = tostring(data.teamKey)
        data.disconnectedRaidPlayers[username] = nil
        data.participants[username] = true
        if type(data.vehicleInsertion) == "table" then
            data.vehicleInsertion.participantNames[username] = true
        end
        -- A delayed respawn/reconnect request from an earlier death must not
        -- return a newly deployed participant to the hideout mid-raid.
        data.returnPending[username] = nil
        RaidFlares.giveFlare(player)
        deliver(player, "FadeOut", {
            seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
            vehicleId = type(data.vehicleInsertion) == "table"
                and tostring(data.vehicleInsertion.vehicleId) or nil,
        })
    end
    broadcastState()
    announceLocalized("IGUI_ExtractionMode_Message_Deploying",
        "Deploying to " .. town.name .. ". Stand by for insertion.",
        { { key = "IGUI_ExtractionMode_Town_" .. tostring(town.key), fallback = town.name } }, {
        extendedHalo = true,
        teamOnly = true,
    })
end

local function campaignSample(player)
    local inventory = player and player:getInventory()
    local found = inventory and inventory:getAllTypeRecurse("ExtractionMode.VaccineSample")
    return found and found:size() > 0 and found:get(0) or nil
end

local function atCampaignHandoff(player)
    local point = Config.CAMPAIGN_HANDOFF_POINT
    if player == nil or point == nil then return false end
    return math.abs((tonumber(player:getZ()) or 0) - (tonumber(point.z) or 0)) <= 0.5
        and Util.distanceSquaredXY({ x = player:getX(), y = player:getY() }, point)
            <= (math.max(1, tonumber(point.radius) or 4) ^ 2)
end

local function campaignCarrier(data, ownerKey)
    for _, player in ipairs(activePlayers()) do
        if data.participants[Util.username(player)] == true and not player:isDead()
            and Groups.forPlayer(player, data.groupRegistry).key == tostring(ownerKey or "")
            and atCampaignHandoff(player) then
            local sample = campaignSample(player)
            if sample then return player, sample end
        end
    end
    return nil, nil
end

local function attractCampaignZombies(carrier)
    local point = Config.CAMPAIGN_HANDOFF_POINT
    local zombieList = getCell() and getCell():getZombieList()
    if point == nil or zombieList == nil then return 0 end
    local radius = math.max(10, tonumber(Config.CAMPAIGN_ZOMBIE_ATTRACTION_RADIUS) or 150)
    local radiusSquared = radius * radius
    local targetX = math.floor(tonumber(point.x) or 0)
    local targetY = math.floor(tonumber(point.y) or 0)
    local targetZ = math.floor(tonumber(point.z) or 0)
    local attracted = 0

    for index = 0, zombieList:size() - 1 do
        local zombie = zombieList:get(index)
        -- Bandits NPCs are represented by IsoZombie and share this list. Only
        -- redirect actual infected; distance is deliberately horizontal so
        -- zombies on every loaded mall floor hear the vaccine event.
        if zombie and not zombie:isDead() and not BanditsIntegration.isBanditZombie(zombie)
            and Util.distanceSquaredXY({ x = zombie:getX(), y = zombie:getY() }, point)
                <= radiusSquared then
            local behavior = zombie:getPathFindBehavior2()
            local alreadyOrdered = false
            if behavior and carrier then
                local checked, matches = pcall(function()
                    return behavior:isGoalCharacter() and behavior:getTargetChar() == carrier
                end)
                alreadyOrdered = checked and matches == true
            elseif behavior then
                local checked, matches = pcall(function()
                    return behavior:isGoalLocation()
                        and behavior:isTargetLocation(targetX + 0.5, targetY + 0.5, targetZ)
                end)
                alreadyOrdered = checked and matches == true
            end

            local ordered = alreadyOrdered
            if not ordered and carrier then
                ordered = pcall(function() zombie:pathToCharacter(carrier) end)
            elseif not ordered then
                ordered = pcall(function() zombie:pathToLocation(targetX, targetY, targetZ) end)
                if not ordered and behavior then
                    ordered = pcall(function()
                        behavior:pathToLocation(targetX, targetY, targetZ)
                    end)
                end
            end
            if ordered then attracted = attracted + 1 end
        end
    end
    return attracted
end

local function clearCampaignRoofZombies()
    local point = Config.CAMPAIGN_HANDOFF_POINT
    local zombieList = getCell() and getCell():getZombieList()
    if point == nil or zombieList == nil then return 0 end
    local radius = math.max(10, tonumber(Config.CAMPAIGN_ROOF_CLEAR_RADIUS) or 85)
    local helipadZ = tonumber(point.z) or 5
    local lowerRoofZ = helipadZ - 1
    local targets = {}
    for index = 0, zombieList:size() - 1 do
        local zombie = zombieList:get(index)
        local zombieZ = zombie and (tonumber(zombie:getZ()) or -1000) or -1000
        if zombie and not zombie:isDead()
            and (math.abs(zombieZ - helipadZ) <= 0.5
                or math.abs(zombieZ - lowerRoofZ) <= 0.5)
            and Util.distanceSquaredXY({ x = zombie:getX(), y = zombie:getY() }, point)
                <= radius * radius then
            targets[#targets + 1] = zombie
        end
    end
    local killed = 0
    for _, zombie in ipairs(targets) do
        BanditsIntegration.detachZombieBrain(zombie)
        pcall(function() zombie:setHealth(0) end)
        local ok = pcall(function() zombie:Kill(nil) end)
        if not ok then pcall(function() zombie:Kill(nil, false) end) end
        killed = killed + 1
    end
    Util.log("Campaign handoff killed " .. tostring(killed)
        .. " zombie(s) on mall roof levels z=" .. tostring(lowerRoofZ)
        .. " and z=" .. tostring(helipadZ))
    return killed
end

local function completeCampaignHandoff(data)
    local handoff = data and data.campaignHandoff
    if handoff == nil then return false end
    Util.log("Final quest hold timer expired: raid=" .. tostring(data.raidId)
        .. " owner=" .. tostring(handoff.ownerKey)
        .. " startedBy=" .. tostring(handoff.startedBy))
    local player, sample = campaignCarrier(data, handoff.ownerKey)
    if player == nil or sample == nil then
        Util.log("Final quest handoff aborted: no living group member remained on the helipad with the sample"
            .. " raid=" .. tostring(data.raidId) .. " owner=" .. tostring(handoff.ownerKey))
        Threats.stopOwnedHelicopter()
        data.extractionHelicopterStarted = nil
        data.campaignHandoff = nil
        broadcastState()
        announceLocalized("IGUI_ExtractionMode_Message_CampaignHandoffAborted",
            "The vaccine handoff was aborted. The Vaccine Sample carrier must remain alive and on the helipad.",
            nil, { participantsOnly = true })
        return false
    end

    if not removeQuestLifecycleItem(sample) then
        Util.log("Final quest handoff aborted: Vaccine Sample removal failed"
            .. " raid=" .. tostring(data.raidId)
            .. " carrier=" .. tostring(Util.username(player))
            .. " itemId=" .. tostring(questItemId(sample)))
        Threats.stopOwnedHelicopter()
        data.extractionHelicopterStarted = nil
        data.campaignHandoff = nil
        broadcastState()
        deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_VaccineHandoffFailed",
            "The Vaccine Sample could not be handed off.")
        return false
    end
    Util.log("Final quest Vaccine Sample handed off: raid=" .. tostring(data.raidId)
        .. " owner=" .. tostring(handoff.ownerKey)
        .. " carrier=" .. tostring(Util.username(player))
        .. " itemId=" .. tostring(questItemId(sample)))

    local definition = Quests.definition("one_last_flight")
    local completions, objectives, trust, owner = questStateFor(data, player)
    if definition and Quests.isAcquired(completions, definition)
        and not Quests.isCompleted(completions, definition.id) then
        for _, objective in ipairs(definition.objectives or {}) do
            if objective.type == "campaign_handoff" then
                Quests.incrementObjective(objectives, definition, objective, 1)
            end
        end
        completions[definition.id] = true
        Quests.applyRewards(trust, definition)
        RaidQuests.grantQuestItemRewards(data, owner, definition)
        ExtractionMode.ProgressionBackup.write(RaidRuntime,
            "quest completed: " .. tostring(definition.id))
    end

    local now = Util.timerNowMs()
    handoff.phase = "FADING"
    handoff.blackAtMs = now + math.max(1, tonumber(Config.CAMPAIGN_FADE_TO_BLACK_MS) or 1500)
    Util.log("Final quest epilogue fade started: raid=" .. tostring(data.raidId)
        .. " fadeToBlackMs=" .. tostring(Config.CAMPAIGN_FADE_TO_BLACK_MS)
        .. " blackAtMs=" .. tostring(handoff.blackAtMs)
        .. " epilogueAfterBlackMs=" .. tostring(Config.CAMPAIGN_EPILOGUE_AFTER_BLACK_MS))
    for _, participant in ipairs(activePlayers()) do
        if data.participants[Util.username(participant)] == true then
            deliver(participant, "CampaignEpilogue", {})
        end
    end
    broadcastState()
    if definition then
        Util.log(Util.username(player) .. " completed the " .. owner.kind
            .. " campaign quest: " .. definition.name)
    end
    return true
end

local function finishCampaignEpilogueTransition(data)
    local handoff = data and data.campaignHandoff
    if handoff == nil or handoff.phase ~= "FADING" then return false end
    local now = Util.timerNowMs()
    local protectionMs = math.max(1,
        tonumber(Config.CAMPAIGN_EPILOGUE_AFTER_BLACK_MS) or 25000) + 2000
    local protectedCount = 0
    Util.log("Final quest reached full black: raid=" .. tostring(data.raidId)
        .. " owner=" .. tostring(handoff.ownerKey)
        .. " applying concealment and clearing the rooftop")
    for _, participant in ipairs(activePlayers()) do
        if data.participants[Util.username(participant)] == true then
            protectBoardingPlayer(participant, true)
            local protection = boardingProtections[Util.username(participant)]
            if protection then
                protection.releaseAt = now + protectionMs
                protection.failsafeAt = now + protectionMs + 2000
                protection.releaseAnywhere = true
                protection.context = "campaign_epilogue"
                protectedCount = protectedCount + 1
            end
        end
    end
    Util.log("Final quest epilogue protection applied: raid=" .. tostring(data.raidId)
        .. " players=" .. tostring(protectedCount)
        .. " releaseInMs=" .. tostring(protectionMs))
    local killed = clearCampaignRoofZombies()
    Threats.stopOwnedHelicopter()
    Util.log("Final quest blackout cleanup complete: raid=" .. tostring(data.raidId)
        .. " roofZombiesKilled=" .. tostring(killed)
        .. " helicopterStopped=true")
    data.extractionHelicopterStarted = nil
    data.campaignHandoff = nil
    broadcastState()
    return true
end

local function processCampaignHandoff(data)
    local handoff = data and data.campaignHandoff
    if handoff == nil then return end
    if tonumber(handoff.raidId) ~= tonumber(data.raidId) or data.state ~= Config.STATE_RAID then
        Util.log("Final quest event canceled because raid state changed: handoffRaid="
            .. tostring(handoff.raidId) .. " currentRaid=" .. tostring(data.raidId)
            .. " state=" .. tostring(data.state))
        Threats.stopOwnedHelicopter()
        data.extractionHelicopterStarted = nil
        data.campaignHandoff = nil
        broadcastState()
        return
    end
    if handoff.phase == "FADING" then
        if Util.timerNowMs() >= (tonumber(handoff.blackAtMs) or 0) then
            finishCampaignEpilogueTransition(data)
        end
        return
    end
    local now = Util.timerNowMs()
    if now >= (tonumber(handoff.nextZombieAttractAtMs) or 0) then
        local attractionTarget = campaignCarrier(data, handoff.ownerKey)
        local attracted = attractCampaignZombies(attractionTarget)
        handoff.nextZombieAttractAtMs = now
            + math.max(1000, tonumber(Config.CAMPAIGN_ZOMBIE_REPATH_INTERVAL_MS) or 5000)
        if handoff.attractionLogged ~= true then
            handoff.attractionLogged = true
            Util.log("Final quest directed " .. tostring(attracted)
                .. " infected toward the helipad within "
                .. tostring(Config.CAMPAIGN_ZOMBIE_ATTRACTION_RADIUS) .. " horizontal tiles")
        end
    end
    local remaining = secondsRemaining(handoff.endMs)
    if remaining <= 0 then
        completeCampaignHandoff(data)
        return
    end
    if remaining <= 60 and handoff.sixtySecondLog ~= true then
        handoff.sixtySecondLog = true
        Util.log("Final quest hold milestone: raid=" .. tostring(data.raidId)
            .. " owner=" .. tostring(handoff.ownerKey) .. " remainingSeconds=" .. tostring(remaining))
    end
    if remaining <= Config.CAMPAIGN_HELICOPTER_ACTIVE_APPROACH_SECONDS
        and data.extractionHelicopterStarted ~= true then
        local target = campaignCarrier(data, handoff.ownerKey)
        local started = Threats.startExtractionHelicopter(data, target)
        if started and handoff.approachLog ~= true then
            handoff.approachLog = true
            Util.log("Final quest helicopter approach started: raid=" .. tostring(data.raidId)
                .. " owner=" .. tostring(handoff.ownerKey)
                .. " target=" .. tostring(target and Util.username(target) or "none")
                .. " remainingSeconds=" .. tostring(remaining))
        elseif not started and handoff.approachFailureLog ~= true then
            handoff.approachFailureLog = true
            Util.log("Final quest helicopter approach could not start yet: raid=" .. tostring(data.raidId)
                .. " owner=" .. tostring(handoff.ownerKey)
                .. " carrierOnHelipad=" .. tostring(target ~= nil))
        end
    end
    broadcastState()
end

local function beginExtraction(player, site, options)
    local data = getStore()
    options = options or {}
    Threats.suppressVanillaHelicopter()
    if options.skipFlare ~= true and not RaidFlares.consumeFlare(player) then
        deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_NeedFlareGun",
            "You need an extraction flare gun.")
        return false
    end
    data.state = Config.STATE_EXTRACTING
    data.activeExtraction = site.id
    local arrivalSeconds = Threats.helicopterArrivalSeconds(data)
    data.extractionEndMs = Util.timerNowMs() + arrivalSeconds * 1000
    data.extractionHelicopterStarted = false
    data.boardingEndMs = nil
    data.extractionRope = nil
    data.boardingPending = {}
    data.boardingExtractedCount = nil

    Threats.spawnExtractionHorde(player)
    if arrivalSeconds <= Config.HELICOPTER_ACTIVE_APPROACH_SECONDS then
        Threats.startExtractionHelicopter(data, player)
    end
    broadcastState()
    local easier = Config.value("EasierExtractions") == true
    local extractionMessage = nil
    local extractionKey = nil
    if options.debug == true then
        extractionMessage = "Debug extraction started at current location as E" .. tostring(site.id)
            .. ". Helicopter inbound."
        extractionKey = easier and "IGUI_ExtractionMode_Message_DebugExtractionEasy"
            or "IGUI_ExtractionMode_Message_DebugExtraction"
        if not easier then extractionMessage = extractionMessage .. " The noise is drawing infected." end
    else
        extractionMessage = "Extraction flare fired at site E" .. tostring(site.id)
            .. ". Helicopter inbound."
        extractionKey = easier and "IGUI_ExtractionMode_Message_FlareFiredEasy"
            or "IGUI_ExtractionMode_Message_FlareFired"
        if not easier then extractionMessage = extractionMessage .. " The noise is drawing infected." end
    end
    announceLocalized(extractionKey, extractionMessage,
        { tostring(site.id) }, {
        audioCue = "extraction_music",
        participantsOnly = true,
    })
    return true
end

local function debugExtractionSite(player)
    local square = player and player:getCurrentSquare()
    if not Util.isSafeOutdoorLandSquare(square) then return nil end
    local sites = {}
    for _, site in ipairs(activeExtractionSites(getStore())) do
        if site.debugSite ~= true then sites[#sites + 1] = site end
    end
    local site = {
        id = #sites + 1,
        x = square:getX(),
        y = square:getY(),
        z = square:getZ(),
        radius = tonumber(Config.value("ExtractionRadius")) or 12,
        debugSite = true,
    }
    sites[#sites + 1] = site
    local data = getStore()
    data.extractionSites = sites
    data.extractionFlareSpawned = data.extractionFlareSpawned or {}
    -- The debug action bypasses flare use, so do not create a permanent
    -- emergency flare cache at this temporary test site.
    data.extractionFlareSpawned[tostring(site.id)] = data.raidId
    return site
end

local function participantCount()
    local count = 0
    for _, active in pairs(getStore().participants) do if active == true then count = count + 1 end end
    return count
end


local function hasReconnectReservation(data)
    local now = Util.nowMs()
    local activeRaidId = tonumber(data and data.raidId)
    for username, disconnected in pairs(data and data.disconnectedRaidPlayers or {}) do
        local disconnectedAtMs = tonumber(disconnected and disconnected.disconnectedAtMs)
        if tonumber(disconnected and disconnected.raidId) == activeRaidId
            and disconnectedAtMs ~= nil
            and now - disconnectedAtMs <= DISCONNECTED_RAID_RECONNECT_GRACE_MS then
            return true
        end
    end
    return false
end

local function resetToHideout()
    local data = getStore()
    local root = RaidRuntime.getRootStore()
    RaidRuntime.releaseVehicleReservation(data)
    local closingTeamKey = tostring(data.teamKey or RaidRuntime.contextKey or "")
    for username in pairs(data.lateJoinPending or {}) do
        local pendingPlayer = nil
        for _, player in ipairs(activePlayers()) do
            if Util.username(player) == username then
                pendingPlayer = player
                deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_JoinRaidClosed",
                    "The raid closed before your insertion countdown finished.")
                deliver(player, "FadeIn", {
                    seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
                })
                break
            end
        end
        releaseBoardingProtection(pendingPlayer, username)
    end
    for vehicleId, pending in pairs(data.lateJoinVehicles or {}) do
        RaidRuntime.cancelVehicleLateJoin(data, vehicleId, pending,
            "IGUI_ExtractionMode_Error_JoinRaidClosed",
            "The raid closed before your vehicle insertion finished.")
    end
    RaidQuests.resetUnsecuredRaidVisitProgress(data)
    Threats.stopOwnedHelicopter()
    BanditsIntegration.cleanupRaid(data)
    Threats.cleanupRaidZombies()
    cleanupRaidQuestItems(data)
    data.state = Config.STATE_HIDEOUT
    data.ready = {}
    data.optedOut = {}
    data.participants = {}
    data.extractedPlayers = {}
    data.groundExtractionPending = {}
    data.groundExtractionVehicles = {}
    data.deathRescuePending = {}
    data.singleplayerRaidPositions = {}
    data.countdownEndMs = nil
    data.raidStartHour = nil
    data.raidStartMs = nil
    data.hordeWindowStartHour = nil
    data.hordeWindowEndHour = nil
    data.hordeAtHour = nil
    data.hordeSpawned = false
    data.hordeEventType = nil
    data.activeExtraction = nil
    data.extractionEndMs = nil
    data.extractionHelicopterStarted = nil
    data.boardingEndMs = nil
    data.extractionRope = nil
    data.boardingPending = {}
    data.boardingExtractedCount = nil
    data.selectedTownKey = nil
    data.selectedTownBy = nil
    data.selectedJoinRaidKey = nil
    data.raidSpawn = nil
    data.extractionSites = nil
    data.extractionFlareSpawned = {}
    data.transitPhase = nil
    data.transitDeadlineMs = nil
    data.insertionCleanupUntilMs = nil
    data.lateJoinPending = {}
    data.lateJoinVehicles = {}
    data.lateInsertionCleanup = {}
    data.vehicleInsertion = nil
    data.campaignHandoff = nil
    -- Deliver directly while the closing assignments still identify this party.
    -- Calling the normal team router would release those now-hideout assignments
    -- and resolve faction members into the newer staging lobby first.
    for _, player in ipairs(activePlayers()) do
        if tostring(root.playerRaidKeys[Util.username(player)]) == closingTeamKey then
            deliverLocalized(player, "Announcement", "IGUI_ExtractionMode_Message_RaidClosed",
                "Raid closed. Spawned horde units were reset; world loot remains in place.")
        end
    end
    for username, key in pairs(root.playerRaidKeys) do
        if tostring(key) == closingTeamKey then root.playerRaidKeys[username] = nil end
    end
    ExtractionMode.ProgressionBackup.write(RaidRuntime,
        "raid " .. tostring(data.raidId) .. " closed")
    broadcastState()
end

local function beginBoarding()
    local data = getStore()
    local site = activeExtractionSites(data)[tonumber(data.activeExtraction) or 0]
    if site == nil then data.state = Config.STATE_RAID; broadcastState(); return end
    data.state = Config.STATE_BOARDING
    data.extractionEndMs = nil
    data.extractionHelicopterStarted = nil
    data.boardingEndMs = Util.timerNowMs()
        + math.max(10, tonumber(Config.value("BoardingWindowSeconds")) or 30) * 1000
    data.extractionRope = {
        x = site.x, y = site.y, z = site.z or 0,
        radius = tonumber(Config.value("BoardingInteractionRadius")) or 3,
        siteId = site.id,
    }
    data.boardingPending = {}
    data.boardingExtractedCount = 0
    broadcastState()
    announceLocalized("IGUI_ExtractionMode_Message_HelicopterOnStation",
        "Helicopter on station at E" .. tostring(site.id)
        .. ". Extraction line deployed: approach it and board individually.", { tostring(site.id) },
        { participantsOnly = true })
end

local function completePendingBoardings()
    local data = getStore()
    local playersByName = {}
    for _, player in ipairs(activePlayers()) do playersByName[Util.username(player)] = player end
    local completed = {}
    local abandoned = {}
    for username, deadline in pairs(data.boardingPending or {}) do
        if Util.timerNowMs() >= tonumber(deadline) then
            local player = playersByName[username]
            if player and data.participants[username] == true then
                completed[#completed + 1] = { username = username, player = player }
            else
                -- Do not let a disconnect during the fade hold the helicopter forever.
                abandoned[#abandoned + 1] = username
            end
        end
    end
    for _, username in ipairs(abandoned) do data.boardingPending[username] = nil end
    if #completed > 0 and data.extractionRope then
        AnimalExtraction.queueNearbyTamedAnimals(data.extractionRope,
            Config.TAMED_ANIMAL_EXTRACTION_RADIUS)
    end
    for _, entry in ipairs(completed) do
        applyExtractionHealing(entry.player)
        RaidQuests.markSuccessfulRaidVisitExtraction(data, entry.player)
        data.participants[entry.username] = nil
        data.extractedPlayers[entry.username] = true
        data.returnPending[entry.username] = nil
        data.boardingPending[entry.username] = nil
        data.boardingExtractedCount = (tonumber(data.boardingExtractedCount) or 0) + 1
        teleport(entry.player, Config.hideout(), data.boardingExtractedCount)
        deliver(entry.player, "FadeIn", { seconds = tonumber(Config.value("TransitFadeSeconds")) or 1 })
        local protection = boardingProtections[entry.username]
        if protection then
            protection.releaseAt = Util.timerNowMs()
                + (math.max(1, tonumber(Config.value("TransitFadeSeconds")) or 1) + 1) * 1000
        end
        announceLocalized("IGUI_ExtractionMode_Message_PlayerBoarded",
            entry.username .. " boarded the extraction helicopter.", { entry.username }, { teamOnly = true })
    end
    return #completed
end

local function endBoarding()
    local data = getStore()
    Threats.stopOwnedHelicopter()
    data.state = Config.STATE_RAID
    data.activeExtraction = nil
    data.extractionEndMs = nil
    data.extractionHelicopterStarted = nil
    data.boardingEndMs = nil
    data.extractionRope = nil
    data.boardingPending = {}
    data.boardingExtractedCount = nil
    for _, player in ipairs(activePlayers()) do
        if data.participants[Util.username(player)] == true then RaidFlares.giveFlare(player) end
    end
    broadcastState()
    announceLocalized("IGUI_ExtractionMode_Message_HelicopterDeparted",
        "The extraction helicopter departed. Survivors left behind must signal another pickup.",
        {}, { participantsOnly = true })
end

local function syncDeathRescueObservers(username, destination)
    local args = { username = username }
    if destination then
        args.x = destination.x
        args.y = destination.y
        args.z = destination.z
    end
    for _, listener in ipairs(Util.players()) do
        deliver(listener, "SyncDeathRescueObserver", args)
    end
end

local function syncDeathRescueEquipment(player)
    if player == nil or not (isServer and isServer()) then return end

    -- Item-loss packets are first sent while the owner is still in a terminal
    -- animation. That client can subsequently rebuild its clothing/model from
    -- the stale death state, leaving retained clothing visible to observers but
    -- missing or unusable for its owner. Re-send the finalized authoritative
    -- worn-item, item-field, and hand-equipment state after death is cancelled.
    if syncVisuals then pcall(function() syncVisuals(player) end) end
    if syncClothingFields then pcall(function() syncClothingFields(player) end) end
    if sendEquip then pcall(function() sendEquip(player) end) end
end

local function finalizeDeathRescue(data, player, username, pending)
    if player == nil or pending == nil or pending.phase == "FADING" then return false end
    pending.phase = "FADING"
    pending.deadlineMs = Util.timerNowMs()
        + math.max(1, tonumber(Config.value("TransitFadeSeconds")) or 1) * 1000
    pending.cinematicDeadlineMs = nil
    RaidOutcomes.releaseDeathRescueState(player)
    deliver(player, "ApplyDeathRescueHealth", {})
    syncDeathRescueEquipment(player)
    -- The normal player update stream does not reliably reverse a terminal
    -- remote-player death state. Explicitly clear it for every observer so the
    -- rescued survivor is not left rendered as a named body on other clients.
    syncDeathRescueObservers(username)
    for _, listener in ipairs(Util.players()) do
        deliver(listener, "DeathRescueVoice", { username = username })
    end
    protectBoardingPlayer(player, true, false)
    deliver(player, "FadeOut", {
        seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
        leaveVehicle = true,
    })
    Util.log("Finalized raid death rescue for " .. tostring(username)
        .. ": mode=" .. tostring(pending.mode) .. ", dropped=" .. tostring(pending.dropped)
        .. ", percent=" .. tostring(pending.percent))
    return true
end

local function beginDeathRescue(data, player, args)
    local username = Util.username(player)
    local mode = RaidOutcomes.deathHandlingMode()
    args = args or {}
    if mode <= 1 or username == "" or player == nil
        or (player:isDead() and args.deathEvent ~= true and args.cinematic ~= true)
        or data.participants[username] ~= true or data.deathRescuePending[username] ~= nil then
        return false
    end

    local dropped, percent
    if mode == 2 then
        dropped, percent = RaidLoss.dropAllDeathEquipment(player)
    else
        dropped, percent = RaidLoss.dropPartialDeathEquipment(player)
    end
    data.boardingPending[username] = nil
    data.groundExtractionPending[username] = nil
    -- isDead() is health-based and becomes true before the terminal animation
    -- event. The client's drag-down flag is therefore the meaningful signal.
    local cinematic = args.cinematic == true
    local pending = {
        phase = cinematic and "CINEMATIC" or "PENDING",
        mode = mode,
        dropped = tonumber(dropped) or 0,
        percent = tonumber(percent) or 0,
    }
    data.deathRescuePending[username] = pending
    if cinematic then
        -- Do not change invincibility, targeting, visibility, health, or the hit
        -- reaction here. Any of those can suppress the paired grapple animation.
        -- The client requests finalization halfway through the EndDeath clip.
        pending.cinematicDeadlineMs = Util.timerNowMs() + 6000
        Util.log("Staged drag-down death rescue for " .. tostring(username))
    else
        finalizeDeathRescue(data, player, username, pending)
    end
    Util.log("Accepted raid death rescue for " .. tostring(username)
        .. ": mode=" .. tostring(mode) .. ", dropped=" .. tostring(dropped)
        .. ", percent=" .. tostring(percent))
    broadcastState()
    return true
end

local function processDeathRescues(data)
    data.deathRescuePending = data.deathRescuePending or {}
    local now = Util.timerNowMs()
    local online = {}
    -- Include a just-killed local player so the death-event fallback can still
    -- clear the terminal state instead of abandoning an accepted rescue.
    for _, player in ipairs(Util.players()) do online[Util.username(player)] = player end
    local completedIndex = 0
    local changed = false

    for username, pending in pairs(data.deathRescuePending) do
        local player = online[username]
        if data.participants[username] ~= true or player == nil then
            data.deathRescuePending[username] = nil
            releaseBoardingProtection(player, username)
            changed = true
        elseif pending.phase == "CINEMATIC" then
            -- Do not use isDead() here: zero health is expected throughout the
            -- drag-down animation. OnPlayerDeath requests immediate finalization
            -- if the terminal event wins the race; this deadline is the fallback.
            if now >= (tonumber(pending.cinematicDeadlineMs) or now) then
                finalizeDeathRescue(data, player, username, pending)
                changed = true
            end
        elseif now >= (tonumber(pending.deadlineMs) or now) then
            if player:isDead() then
                RaidOutcomes.releaseDeathRescueState(player)
                deliver(player, "ApplyDeathRescueHealth", {})
            end
            local exitPoint = { x = player:getX(), y = player:getY(), z = player:getZ() }
            AnimalExtraction.queueNearbyTamedAnimals(exitPoint,
                Config.TAMED_ANIMAL_EXTRACTION_RADIUS)
            data.participants[username] = nil
            data.extractedPlayers[username] = true
            data.returnPending[username] = nil
            data.deathRescuePending[username] = nil
            data.flareUpgradePending[username] = nil
            completedIndex = completedIndex + 1
            local destination = teleport(player, Config.hideout(), completedIndex, false, true)
            -- Supply the same randomized destination to observers. Otherwise a
            -- proxy which entered the terminal death state can retain its last
            -- interpolated position even after the owning client moves home.
            syncDeathRescueObservers(username, destination)
            -- Repeat after the teleport has completed. This is late enough that
            -- the rescued owner's post-death UI/model can no longer overwrite
            -- the authoritative equipment state sent during finalization.
            syncDeathRescueEquipment(player)
            deliver(player, "RefreshDeathRescueEquipment", {})
            deliver(player, "FadeIn", {
                seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
            })
            local protection = boardingProtections[username]
            if protection then
                protection.releaseAt = now
                    + (math.max(1, tonumber(Config.value("TransitFadeSeconds")) or 1) + 1) * 1000
            end
            if tonumber(pending.mode) == 2 then
                deliverExtendedLocalized(player, "Announcement",
                    "IGUI_ExtractionMode_Message_DeathRescueBarely",
                    "You escaped... Barely. Everything you were carrying was left behind.")
            else
                deliverExtendedLocalized(player, "Announcement",
                    "IGUI_ExtractionMode_Message_DeathRescueEquipmentLoss",
                    "You escaped... But you lost a lot of equipment. "
                        .. tostring(pending.dropped or 0) .. " item(s) were left behind ("
                        .. tostring(pending.percent or 0)
                        .. "% equipment loss, plus anything held).",
                    tostring(pending.dropped or 0), tostring(pending.percent or 0))
            end
            changed = true
        end
    end
    return changed or tableHasEntries(data.deathRescuePending)
end

local function eligibleForGroundExtraction(data, player)
    if player == nil or player:isDead() then return false end
    local username = Util.username(player)
    return username ~= "" and data.participants[username] == true
        and data.deathRescuePending[username] == nil
        and not RaidRuntime.transitionProtectionActive(data, username)
        and not Config.pointInsideRaidBounds(data.selectedTownKey,
            ModCompatibility.playerPosition(player))
end

function RaidRuntime.vehicleOutsideRaidBounds(data, vehicle)
    if vehicle == nil then return false end
    local outside = false
    pcall(function()
        outside = not Config.pointInsideRaidBounds(data.selectedTownKey, {
            x = vehicle:getX(), y = vehicle:getY(),
        })
    end)
    return outside
end

function RaidRuntime.pendingVehicle(data, pending, online)
    if pending == nil or pending.vehicleId == nil then return nil end
    for _, player in pairs(online) do
        local vehicle = player and player:getVehicle()
        if vehicle ~= nil and tostring(vehicle:getId()) == tostring(pending.vehicleId) then return vehicle end
    end
    return nil
end

function RaidRuntime.vehicleGroupStillPending(data, vehicleId)
    for _, pending in pairs(data.groundExtractionPending or {}) do
        if tostring(pending.vehicleId or "") == tostring(vehicleId) then return true end
    end
    return false
end

function RaidRuntime.groundVehicleRefKey(data, vehicleId)
    return tostring(data and data.raidId or 0) .. ":" .. tostring(vehicleId)
end

function RaidRuntime.prepareGroundExtractionVehicle(data, vehicle, garageOwner)
    if data == nil or vehicle == nil then return nil, "vehicle is unavailable" end
    data.groundExtractionVehicles = data.groundExtractionVehicles or {}
    local vehicleKey = tostring(vehicle:getId())
    local existing = data.groundExtractionVehicles[vehicleKey]
    if type(existing) == "table" then return existing end

    local hasInteriorOccupants = ModCompatibility.hasRemoteRVOccupants(
        vehicle, activePlayers())
    if hasInteriorOccupants then
        local blocked = {
            owner = tostring(garageOwner or ""),
            fuelCost = 0,
            fuelSpent = 0,
            fuelBefore = 0,
            captureFailed = true,
            captureError = "the RV still has a player inside its interior",
        }
        data.groundExtractionVehicles[vehicleKey] = blocked
        return blocked
    end

    local storedFuelCost = nil
    pcall(function()
        storedFuelCost = tonumber(vehicle:getModData().ExtractionModeRaidFuelCost)
    end)
    local fuelCharged, fuelCost, fuelRemaining, fuelBefore =
        RaidRuntime.chargeVehicleRaidFuel(vehicle, true, storedFuelCost)
    pcall(function() vehicle:getModData().ExtractionModeRaidFuelCost = nil end)

    local owner = tostring(garageOwner or "")
    local vehicleRecord = {
        owner = owner,
        fuelCost = fuelCost,
        fuelSpent = fuelCharged and math.min(fuelBefore, fuelCost) or 0,
        fuelBefore = fuelBefore,
        storedFuelCost = storedFuelCost,
    }
    data.groundExtractionVehicles[vehicleKey] = vehicleRecord
    if fuelCharged then
        Util.log("Vehicle extraction fuel charged vehicle=" .. vehicleKey
            .. " liters=" .. tostring(vehicleRecord.fuelSpent)
            .. " remaining=" .. tostring(fuelRemaining))
    else
        Util.log("Vehicle extraction fuel charge failed vehicle=" .. vehicleKey)
    end

    if owner == "" then
        vehicleRecord.captureFailed = true
        vehicleRecord.captureError = "the vehicle driver has no garage owner"
        return vehicleRecord
    end

    local keyReturned, keyResult = ExtractionMode.Garage.returnIgnitionKeyToDriver(vehicle)
    if not keyReturned then
        vehicleRecord.captureFailed = true
        vehicleRecord.captureError = keyResult
        Util.log("Garage snapshot cancelled for vehicle " .. vehicleKey
            .. " before key loss: " .. tostring(keyResult))
        return vehicleRecord
    elseif type(keyResult) == "table" then
        Util.log("Returned ignition key " .. tostring(keyResult.keyId or "unknown")
            .. " to the driver via " .. tostring(keyResult.method or "native")
            .. " before capturing raid vehicle " .. vehicleKey)
    end

    local snapshot, captureError = ExtractionMode.Garage.captureVehicle(vehicle)
    if snapshot == nil then
        vehicleRecord.captureFailed = true
        vehicleRecord.captureError = captureError
        Util.log("Garage snapshot failed for vehicle " .. vehicleKey
            .. ": " .. tostring(captureError))
        return vehicleRecord
    end
    if Config.value("HotwireExtractedVehicles") == true then
        snapshot.hotwired = true
        snapshot.hotwiredBroken = false
    end

    vehicleRecord.snapshot = snapshot
    local transactionId = "extract:" .. tostring(data.raidId) .. ":" .. vehicleKey
    local root = RaidRuntime.getRootStore()
    local transactionExisted = type(root.garageTransactions) == "table"
        and root.garageTransactions[transactionId] ~= nil
    local transaction, transactionError = ExtractionMode.Garage.beginExtractionTransaction(
        root, transactionId, owner, snapshot, {
            vehicleId = vehicleKey,
            scriptName = snapshot.scriptName,
            x = vehicle:getX(), y = vehicle:getY(), z = vehicle:getZ(),
        })
    if transaction ~= nil then
        vehicleRecord.transactionId = transactionId
        vehicleRecord.transactionCreated = not transactionExisted
        pcall(function()
            vehicle:getModData().ExtractionModeGarageRemovalTransactionId = transactionId
        end)
        RaidRuntime.groundExtractionVehicleRefs[
            RaidRuntime.groundVehicleRefKey(data, vehicleKey)] = vehicle
    else
        vehicleRecord.snapshot = nil
        vehicleRecord.captureFailed = true
        vehicleRecord.captureError = transactionError
        Util.log("Garage transaction failed for vehicle " .. vehicleKey
            .. ": " .. tostring(transactionError))
    end
    return vehicleRecord
end

function RaidRuntime.cancelPreparedGroundExtractionVehicle(data, vehicle, vehicleRecord)
    if data == nil or vehicle == nil or type(vehicleRecord) ~= "table" then return end
    local vehicleKey = tostring(vehicle:getId())
    if (tonumber(vehicleRecord.fuelSpent) or 0) > 0 then
        local _, _, refundTank = RaidRuntime.vehicleFuelDetails(vehicle)
        if refundTank ~= nil then
            pcall(function()
                refundTank:setContainerContentAmount(
                    tonumber(vehicleRecord.fuelBefore) or 0, false, true)
                vehicle:transmitPartModData(refundTank)
            end)
        end
    end
    pcall(function()
        vehicle:getModData().ExtractionModeRaidFuelCost = vehicleRecord.storedFuelCost
        if tostring(vehicle:getModData().ExtractionModeGarageRemovalTransactionId or "")
            == tostring(vehicleRecord.transactionId or "") then
            vehicle:getModData().ExtractionModeGarageRemovalTransactionId = nil
        end
    end)
    if vehicleRecord.transactionCreated == true and vehicleRecord.transactionId ~= nil then
        ExtractionMode.Garage.finishTransaction(RaidRuntime.getRootStore(),
            vehicleRecord.transactionId, "vehicle extraction preparation cancelled")
    end
    RaidRuntime.groundExtractionVehicleRefs[
        RaidRuntime.groundVehicleRefKey(data, vehicleKey)] = nil
    data.groundExtractionVehicles[vehicleKey] = nil
end

function RaidRuntime.finalizeGroundExtractionVehicles(data, online)
    local changed = false
    local root = RaidRuntime.getRootStore()
    ExtractionMode.Garage.ensureState(root)
    for vehicleId, pendingVehicleRecord in pairs(data.groundExtractionVehicles or {}) do
        if not RaidRuntime.vehicleGroupStillPending(data, vehicleId) then
            local snapshot = pendingVehicleRecord.snapshot
            local owner = tostring(pendingVehicleRecord.owner or "")
            local referenceKey = RaidRuntime.groundVehicleRefKey(data, vehicleId)
            local vehicle = RaidRuntime.groundExtractionVehicleRefs[referenceKey]
            for _, player in pairs(online) do
                local candidate = player and player:getVehicle()
                if candidate ~= nil and tostring(candidate:getId()) == tostring(vehicleId) then
                    vehicle = candidate
                    break
                end
            end
            if snapshot ~= nil and owner ~= "" then
                local remoteOccupied = ModCompatibility.hasRemoteRVOccupants(
                    vehicle, activePlayers())
                if remoteOccupied then
                    if pendingVehicleRecord.transactionId ~= nil then
                        ExtractionMode.Garage.finishTransaction(root,
                            pendingVehicleRecord.transactionId,
                            "RV extraction cancelled while interior occupied")
                    end
                    RaidRuntime.groundExtractionVehicleRefs[referenceKey] = nil
                    data.groundExtractionVehicles[vehicleId] = nil
                    Util.log("Cancelled storage of extracted RV " .. tostring(vehicleId)
                        .. " because a player entered its interior during extraction")
                    local driver = online[owner]
                    if driver then
                        deliver(driver, "Error", {
                            message = "The RV was left in the raid because someone entered its interior during extraction.",
                        })
                    end
                    changed = true
                else
                    local transactionId = pendingVehicleRecord.transactionId
                        or ("extract:" .. tostring(data.raidId) .. ":" .. tostring(vehicleId))
                    if root.garageTransactions[transactionId] == nil then
                        ExtractionMode.Garage.beginExtractionTransaction(root, transactionId,
                            owner, snapshot, {
                                vehicleId = vehicleId,
                                scriptName = snapshot.scriptName,
                                x = vehicle and vehicle:getX() or nil,
                                y = vehicle and vehicle:getY() or nil,
                                z = vehicle and vehicle:getZ() or nil,
                            })
                    end
                    local garageId, recordOrError =
                        ExtractionMode.Garage.commitExtractionTransaction(root, transactionId)
                    if garageId ~= nil then
                        local removed = false
                        local removeError = nil
                        if vehicle ~= nil then
                            removed, removeError = ExtractionMode.GarageAuthority.removeVehicleNow(vehicle)
                        end
                        if removed then
                            root.pendingGarageVehicleRemovals[tostring(transactionId)] = nil
                            local legacy = root.pendingGarageVehicleRemovals[tostring(vehicleId)]
                            if type(legacy) == "table"
                                and tostring(legacy.transactionId or "") == tostring(transactionId) then
                                root.pendingGarageVehicleRemovals[tostring(vehicleId)] = nil
                            end
                            ExtractionMode.Garage.finishTransaction(root, transactionId,
                                "raid vehicle extraction completed")
                            Util.log("Stored raid vehicle " .. tostring(vehicleId) .. " as "
                                .. tostring(garageId) .. " in " .. owner .. "'s personal garage")
                            local driver = online[owner]
                            if driver then
                                deliver(driver, "Announcement", {
                                    message = tostring(snapshot.modelKey or snapshot.scriptName or "Vehicle")
                                        .. " was saved to your personal garage.",
                                })
                            end
                        else
                            Util.log("Garage vehicle removal failed for " .. tostring(vehicleId)
                                .. "; the committed removal will retry: " .. tostring(removeError))
                        end
                    else
                        Util.log("Garage vehicle save failed for " .. tostring(vehicleId)
                            .. ": " .. tostring(recordOrError))
                    end
                end
            end
            if data.groundExtractionVehicles[vehicleId] ~= nil then
                RaidRuntime.groundExtractionVehicleRefs[referenceKey] = nil
                data.groundExtractionVehicles[vehicleId] = nil
                changed = true
            end
        end
    end
    return changed
end

local function processGroundExtractions(data)
    data.groundExtractionPending = data.groundExtractionPending or {}
    data.groundExtractionVehicles = data.groundExtractionVehicles or {}
    if singleplayerAuthority() and singleplayerRaidResumePending then return false end
    if Config.raidBounds(data.selectedTownKey) == nil then
        for username in pairs(data.groundExtractionPending) do
            data.groundExtractionPending[username] = nil
            releaseBoardingProtection(nil, username)
        end
        data.groundExtractionVehicles = {}
        return false
    end

    local now = Util.timerNowMs()
    local changed = false
    local completedIndex = 0
    local online = {}
    local cancelledVehicleExtractions = {}
    for _, player in ipairs(activePlayers()) do online[Util.username(player)] = player end

    for username, pending in pairs(data.groundExtractionPending) do
        local player = online[username]
        if pending.vehicleId ~= nil
            and cancelledVehicleExtractions[tostring(pending.vehicleId)] == true then
            data.groundExtractionPending[username] = nil
            releaseBoardingProtection(player, username)
            changed = true
        elseif data.participants[username] ~= true or player == nil or player:isDead() then
            data.groundExtractionPending[username] = nil
            releaseBoardingProtection(player, username)
            changed = true
        elseif pending.phase == "FADING" then
            if now >= (tonumber(pending.deadlineMs) or now) then
                local exitPoint = { x = player:getX(), y = player:getY(), z = player:getZ() }
                AnimalExtraction.queueNearbyTamedAnimals(exitPoint,
                    Config.TAMED_ANIMAL_EXTRACTION_RADIUS)
                data.participants[username] = nil
                data.extractedPlayers[username] = true
                data.returnPending[username] = nil
                data.groundExtractionPending[username] = nil
                data.flareUpgradePending[username] = nil
                completedIndex = completedIndex + 1
                applyExtractionHealing(player)
                RaidQuests.markSuccessfulRaidVisitExtraction(data, player)
                teleport(player, Config.hideout(), completedIndex, false, true)
                deliver(player, "FadeIn", {
                    seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
                })
                local protection = boardingProtections[username]
                if protection then
                    protection.releaseAt = now
                        + (math.max(1, tonumber(Config.value("TransitFadeSeconds")) or 1) + 1) * 1000
                end
                announceLocalized("IGUI_ExtractionMode_Message_PlayerEscapedBoundary",
                    username .. " escaped across the raid boundary.", { username }, { teamOnly = true })
                changed = true
            end
        elseif pending.vehicleId ~= nil then
            local vehicle = RaidRuntime.pendingVehicle(data, pending, online)
            if vehicle == nil or not RaidRuntime.vehicleOutsideRaidBounds(data, vehicle) then
                data.groundExtractionPending[username] = nil
                changed = true
            elseif now >= (tonumber(pending.deadlineMs) or now) then
                local vehicleKey = tostring(pending.vehicleId)
                if data.groundExtractionVehicles[vehicleKey] == nil then
                    RaidRuntime.prepareGroundExtractionVehicle(
                        data, vehicle, pending.garageOwner)
                end
                local vehicleRecord = data.groundExtractionVehicles[vehicleKey]
                if vehicleRecord and vehicleRecord.captureFailed == true then
                    if vehicleRecord.fuelSpent > 0 then
                        local _, _, refundTank = RaidRuntime.vehicleFuelDetails(vehicle)
                        if refundTank ~= nil then
                            pcall(function()
                                refundTank:setContainerContentAmount(
                                    tonumber(vehicleRecord.fuelBefore) or 0, false, true)
                                vehicle:transmitPartModData(refundTank)
                                vehicle:getModData().ExtractionModeRaidFuelCost =
                                    vehicleRecord.storedFuelCost
                            end)
                        end
                    end
                    for occupantUsername, occupantPending in pairs(data.groundExtractionPending) do
                        if tostring(occupantPending.vehicleId or "") == vehicleKey then
                            data.groundExtractionPending[occupantUsername] = nil
                            releaseBoardingProtection(online[occupantUsername], occupantUsername)
                        end
                    end
                    data.groundExtractionVehicles[vehicleKey] = nil
                    cancelledVehicleExtractions[vehicleKey] = true
                    local driverPlayer = online[tostring(pending.driverUsername or "")] or player
                    deliver(driverPlayer, "Error", {
                        message = "Vehicle extraction was cancelled because its vehicle or cargo data could not be saved safely: "
                            .. tostring(vehicleRecord.captureError or "unknown capture error"),
                    })
                else
                    pcall(function() vehicle:setForceBrake() end)
                    pending.phase = "FADING"
                    pending.deadlineMs = now
                        + math.max(1, tonumber(Config.value("TransitFadeSeconds")) or 1) * 1000
                    protectBoardingPlayer(player)
                    RaidRuntime.applyExtractionShove(player, {
                        x = player:getX(), y = player:getY(), z = player:getZ(),
                    })
                    deliver(player, "FadeOut", {
                        seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
                        leaveVehicle = true,
                    })
                end
                changed = true
            end
        elseif not eligibleForGroundExtraction(data, player) then
            data.groundExtractionPending[username] = nil
            changed = true
        elseif now >= (tonumber(pending.deadlineMs) or now) then
            pending.phase = "FADING"
            pending.deadlineMs = now
                + math.max(1, tonumber(Config.value("TransitFadeSeconds")) or 1) * 1000
            protectBoardingPlayer(player)
            RaidRuntime.applyExtractionShove(player, {
                x = player:getX(), y = player:getY(), z = player:getZ(),
            })
            deliver(player, "FadeOut", {
                seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
                leaveVehicle = true,
            })
            changed = true
        end
    end

    if RaidRuntime.finalizeGroundExtractionVehicles(data, online) then changed = true end

    for username, participating in pairs(data.participants) do
        local player = online[username]
        if participating == true and data.groundExtractionPending[username] == nil
            and eligibleForGroundExtraction(data, player) then
            local vehicle = player and player:getVehicle()
            if vehicle == nil then
                data.groundExtractionPending[username] = {
                    phase = "LEAVING",
                    deadlineMs = now + Config.GROUND_EXTRACTION_SECONDS * 1000,
                }
            else
                local driver = nil
                pcall(function() driver = vehicle:getDriver() end)
                if driver == player and RaidRuntime.vehicleOutsideRaidBounds(data, vehicle) then
                    local vehicleId = tostring(vehicle:getId())
                    local driverUsername = Util.username(player)
                    local garageOwner = Util.garageUsername(player)
                    local deadline = now + Config.GROUND_EXTRACTION_SECONDS * 1000
                    for occupantUsername, occupant in pairs(online) do
                        if data.participants[occupantUsername] == true
                            and occupant:getVehicle() == vehicle then
                            data.groundExtractionPending[occupantUsername] = {
                                phase = "LEAVING",
                                deadlineMs = deadline,
                                vehicleId = vehicleId,
                                driverUsername = driverUsername,
                                garageOwner = garageOwner,
                            }
                        end
                    end
                end
            end
            changed = true
        end
    end

    return changed or tableHasEntries(data.groundExtractionPending)
end

local function moveIdentityKey(values, oldUsername, newUsername)
    if values == nil or oldUsername == newUsername or values[oldUsername] == nil then return end
    if values[newUsername] == nil then values[newUsername] = values[oldUsername] end
    values[oldUsername] = nil
end

-- Project Remnants' permanent-death failover promotes the successor NPC into
-- Project Zomboid's primary player slot. That changes getUsername() instead of
-- leaving the game in the normal possession state, so the old participant looks
-- disconnected unless its raid identity is explicitly handed to the successor.
local function reconcileRemnantsParticipantIdentity(data, online)
    local raidActive = data.state == Config.STATE_TRANSIT or data.state == Config.STATE_RAID
        or data.state == Config.STATE_EXTRACTING or data.state == Config.STATE_BOARDING
    if not raidActive or not ProjectRemnantsIntegration.isAvailable() then return false end

    local missingParticipants = {}
    for username, participating in pairs(data.participants) do
        if participating == true and online[username] == nil then
            missingParticipants[#missingParticipants + 1] = username
        end
    end

    local livingSuccessors = {}
    for username, player in pairs(online) do
        if data.participants[username] ~= true and player and not player:isDead() then
            livingSuccessors[#livingSuccessors + 1] = { username = username, player = player }
        end
    end

    -- Project Remnants is singleplayer-only. Requiring exactly one old identity
    -- and one living replacement avoids masking an unrelated participant error.
    if #missingParticipants ~= 1 or #livingSuccessors ~= 1 then return false end
    local oldUsername = missingParticipants[1]
    local successor = livingSuccessors[1]
    local newUsername = successor.username

    data.participants[oldUsername] = nil
    data.participants[newUsername] = true
    moveIdentityKey(data.ready, oldUsername, newUsername)
    moveIdentityKey(data.optedOut, oldUsername, newUsername)
    moveIdentityKey(data.extractedPlayers, oldUsername, newUsername)
    moveIdentityKey(data.returnPending, oldUsername, newUsername)
    moveIdentityKey(data.boardingPending, oldUsername, newUsername)
    moveIdentityKey(data.groundExtractionPending, oldUsername, newUsername)
    moveIdentityKey(data.deathRescuePending, oldUsername, newUsername)
    moveIdentityKey(data.flareUpgradePending, oldUsername, newUsername)
    moveIdentityKey(data.singleplayerRaidPositions, oldUsername, newUsername)
    moveIdentityKey(data.lateJoinPending, oldUsername, newUsername)
    moveIdentityKey(data.lateInsertionCleanup, oldUsername, newUsername)
    moveIdentityKey(boardingProtections, oldUsername, newUsername)
    if data.selectedTownBy == oldUsername then data.selectedTownBy = newUsername end

    RaidFlares.giveFlare(successor.player)
    Util.log("Project Remnants raid control transferred from " .. tostring(oldUsername)
        .. " to " .. tostring(newUsername) .. "; participant identity preserved")
    return true
end

local function pruneAndReconcileParticipants()
    local data = getStore()
    local online = {}
    local changed = false
    for _, player in ipairs(Util.players()) do
        local username = Util.username(player)
        if username ~= "" then
            online[username] = player
            local dead = player:isDead()
            local awaitingRemnantsSuccessor = dead
                and data.participants[username] == true
                and ProjectRemnantsIntegration.hasLivingSuccessor(player)

            -- Illness, infection, and some scripted damage paths can invoke the
            -- terminal death event without giving the owning client enough time
            -- to submit RescueFromDeath. Catch that race authoritatively before
            -- ordinary dead-participant pruning removes the raid membership.
            if dead and data.participants[username] == true
                and not awaitingRemnantsSuccessor
                and data.deathRescuePending[username] == nil
                and RaidOutcomes.deathHandlingMode() > 1
                and beginDeathRescue(data, player, {
                    deathEvent = true,
                    cinematic = false,
                    serverFallback = true,
                }) then
                Util.log("Server caught terminal raid death for " .. tostring(username)
                    .. " before the client rescue request.")
            end

            local awaitingDeathRescue = dead
                and data.participants[username] == true
                and data.deathRescuePending[username] ~= nil
            if dead and data.participants[username] == true
                and not awaitingRemnantsSuccessor and not awaitingDeathRescue then
                data.participants[username] = nil
                data.boardingPending[username] = nil
                data.groundExtractionPending[username] = nil
                data.deathRescuePending[username] = nil
                data.returnPending[username] = true
                releaseBoardingProtection(player, username)
                changed = true
            end
        end
    end
    if reconcileRemnantsParticipantIdentity(data, online) then changed = true end
    for username in pairs(data.ready) do
        if not online[username] or online[username]:isDead() then
            data.ready[username] = nil
            changed = true
        end
    end
    if RaidRuntime.readyEntryCount(data) == 0 and tableHasEntries(data.optedOut) then
        data.optedOut = {}
        changed = true
    end

    local raidActive = data.state == Config.STATE_RAID
        or data.state == Config.STATE_EXTRACTING
        or data.state == Config.STATE_BOARDING
    -- A single-player save loads global mod data before the character is always
    -- available through getPlayer(). Keep the saved participant during that
    -- startup gap; multiplayer still removes genuinely disconnected players.
    local preserveOfflineParticipants = singleplayerAuthority()
        and raidActive and not tableHasEntries(online)
    local raidStartMs = tonumber(data.raidStartMs) or Util.timerNowMs()
    local hideoutReconciliationReady = raidActive
        and Util.timerNowMs() - raidStartMs >= HIDEOUT_RECONCILE_GRACE_MS
        and not (singleplayerAuthority() and singleplayerRaidResumePending)
    local disconnected = {}
    local returnedToHideout = {}
    for username, participating in pairs(data.participants) do
        if participating == true then
            local player = online[username]
            if player == nil and not preserveOfflineParticipants then
                disconnected[#disconnected + 1] = username
            elseif player ~= nil and hideoutReconciliationReady and not player:isDead()
                and data.lateJoinPending[username] == nil
                and data.lateInsertionCleanup[username] == nil
                and Threats.playerInsideHideoutCell(player) then
                returnedToHideout[#returnedToHideout + 1] = { username = username, player = player }
            end
        end
    end

    -- Participation represents living players who are currently connected. A
    -- multiplayer survivor is removed so they cannot hold their raid instance open,
    -- while a persisted raid ID lets reconnect recovery distinguish the same
    -- active raid from one which ended while they were away.
    for _, username in ipairs(disconnected) do
        if not singleplayerAuthority() then
            data.disconnectedRaidPlayers[username] = {
                raidId = data.raidId,
                disconnectedAtHour = Util.worldHours(),
                disconnectedAtMs = Util.nowMs(),
                x = tonumber(data.singleplayerRaidPositions[username]
                    and data.singleplayerRaidPositions[username].x),
                y = tonumber(data.singleplayerRaidPositions[username]
                    and data.singleplayerRaidPositions[username].y),
                z = tonumber(data.singleplayerRaidPositions[username]
                    and data.singleplayerRaidPositions[username].z),
            }
            local record = data.disconnectedRaidPlayers[username]
            local previous = online[username]
            if previous ~= nil then
                record.x, record.y, record.z = previous:getX(), previous:getY(), previous:getZ()
            end
        end
        data.participants[username] = nil
        data.boardingPending[username] = nil
        data.groundExtractionPending[username] = nil
        data.deathRescuePending[username] = nil
        data.flareUpgradePending[username] = nil
        releaseBoardingProtection(nil, username)
        Util.log("Removed disconnected raid participant " .. tostring(username)
            .. " and recorded raid " .. tostring(data.raidId) .. " for reconnect recovery")
        changed = true
    end

    -- Teleports can be performed by admins, other mods, reconnect recovery, or
    -- future game code. Physical presence in the protected hideout cell is the
    -- final authority: a living participant who gets home has extracted.
    for _, entry in ipairs(returnedToHideout) do
        applyExtractionHealing(entry.player)
        RaidQuests.markSuccessfulRaidVisitExtraction(data, entry.player)
        data.participants[entry.username] = nil
        data.extractedPlayers[entry.username] = true
        data.returnPending[entry.username] = nil
        data.boardingPending[entry.username] = nil
        data.groundExtractionPending[entry.username] = nil
        data.deathRescuePending[entry.username] = nil
        data.flareUpgradePending[entry.username] = nil
        releaseBoardingProtection(entry.player, entry.username)
        announceLocalized("IGUI_ExtractionMode_Message_PlayerReturned",
            entry.username .. " returned to the hideout and was counted as extracted.",
            { entry.username }, { teamOnly = true })
        Util.log("Reconciled hideout return for raid participant " .. tostring(entry.username))
        changed = true
    end

    return changed
end

local function ensureStarterFlashlight(player)
    if player == nil or player:isDead() then return end
    local playerData = player:getModData()
    if playerData.ExtractionModeStarterFlashlightGranted == true then return end

    local inventory = player:getInventory()
    if inventory == nil then return end
    local flashlight = inventory:getFirstTagRecurse(ItemTag.FLASHLIGHT)
    if flashlight == nil then
        flashlight = inventory:AddItem("Base.HandTorch")
        if flashlight == nil then return end
        pcall(function() flashlight:setUsedDelta(1.0) end)
        if sendAddItemToContainer then sendAddItemToContainer(inventory, flashlight) end
    end

    -- Character mod data persists through reconnects but starts fresh for a new
    -- survivor, giving replacements one light without enabling item farming.
    playerData.ExtractionModeStarterFlashlightGranted = true
    pcall(function() player:transmitModData() end)
end

local function ensureStarterM9Kit(player)
    if player == nil or player:isDead() or Config.value("GrantStarterM9Kit") == false then return end
    local playerData = player:getModData()
    local inventory = player:getInventory()
    if playerData == nil or inventory == nil then return end
    local changed = false

    if playerData.ExtractionModeStarterM9Granted ~= true then
        -- Commit the claim before creating the item. This closes the window in
        -- which a delayed multiplayer inventory/mod-data update could cause a
        -- later tick to grant the same starter pistol again. Also reconcile an
        -- already-present M9 in case an older server already duplicated a call.
        playerData.ExtractionModeStarterM9Granted = true
        changed = true
        pcall(function() player:transmitModData() end)
        local pistol = inventory:getItemFromType("Base.Pistol", true, true)
        if pistol == nil then
            pistol = inventory:AddItem("Base.Pistol")
            if pistol and sendAddItemToContainer then sendAddItemToContainer(inventory, pistol) end
        end
        if pistol == nil then
            playerData.ExtractionModeStarterM9Granted = nil
            changed = true
        end
    end

    local magazineCount = math.max(0,
        math.floor(tonumber(playerData.ExtractionModeStarter9mmMagazinesGranted) or 0))
    if magazineCount < 2 then
        local existing = inventory:getAllTypeRecurse("Base.9mmClip")
        magazineCount = math.max(magazineCount, math.min(2, existing and existing:size() or 0))
    end
    local committedMagazineCount = magazineCount
    if magazineCount < 2 then
        -- Reserve both grants before adding either magazine so even a repeated
        -- or lag-delayed call sees a completed claim. If creation fails, roll
        -- the counter back only to the number actually granted.
        playerData.ExtractionModeStarter9mmMagazinesGranted = 2
        changed = true
        pcall(function() player:transmitModData() end)
    end
    while magazineCount < 2 do
        local magazine = inventory:AddItem("Base.9mmClip")
        if magazine == nil then break end
        magazine:setCurrentAmmoCount(magazine:getMaxAmmo())
        if sendAddItemToContainer then sendAddItemToContainer(inventory, magazine) end
        if sendItemStats then sendItemStats(magazine) end
        magazineCount = magazineCount + 1
        playerData.ExtractionModeStarter9mmMagazinesGranted = magazineCount
        changed = true
    end

    if magazineCount < 2 then
        playerData.ExtractionModeStarter9mmMagazinesGranted = math.max(committedMagazineCount, magazineCount)
        changed = true
    elseif playerData.ExtractionModeStarter9mmMagazinesGranted ~= 2 then
        playerData.ExtractionModeStarter9mmMagazinesGranted = 2
        changed = true
    end

    if changed then pcall(function() player:transmitModData() end) end
end

function RaidRuntime.ensureStarterNightstick(player)
    if player == nil or player:isDead()
        or Config.value("GrantStarterNightstick") == false then return end
    local playerData = player:getModData()
    local inventory = player:getInventory()
    if playerData == nil or inventory == nil
        or playerData.ExtractionModeStarterNightstickGranted == true then return end

    -- Claim the per-survivor grant before changing inventory so repeated server
    -- ticks cannot duplicate the item while multiplayer inventory updates settle.
    playerData.ExtractionModeStarterNightstickGranted = true
    pcall(function() player:transmitModData() end)

    local nightstick = inventory:getItemFromType("Base.Nightstick", true, true)
    if nightstick == nil then
        nightstick = inventory:AddItem("Base.Nightstick")
        if nightstick and sendAddItemToContainer then
            sendAddItemToContainer(inventory, nightstick)
        end
    end
    if nightstick == nil then
        playerData.ExtractionModeStarterNightstickGranted = nil
        pcall(function() player:transmitModData() end)
        return
    end

    local equipped = pcall(function()
        local primary = player:getPrimaryHandItem()
        local secondary = player:getSecondaryHandItem()
        if secondary == primary or secondary == nightstick then
            player:setSecondaryHandItem(nil)
        end
        player:setPrimaryHandItem(nightstick)
    end)
    if not equipped then
        -- Keep the item, but retry equipping it next tick without granting another.
        playerData.ExtractionModeStarterNightstickGranted = nil
    elseif sendEquip then
        pcall(function() sendEquip(player) end)
    end
    pcall(function() player:transmitModData() end)
end

function RaidRuntime.onTickForRaid(nowSecond, runShared)
    if runShared then
        if ExtractionMode.ProgressionBackup.recover(RaidRuntime) then broadcastState() end
        ExtractionMode.ProgressionBackup.syncPlayers(RaidRuntime)
        Threats.purgeHideoutZombies()
        Threats.refreshAmbientZombieSpeeds()
        Threats.suppressVanillaHelicopter()
    end
    local data = getStore()
    processSingleplayerRaidPosition(data)
    if not singleplayerAuthority() then
        -- Keep a server-owned last-known field point so a disconnected lone
        -- participant can resume without requiring an online teammate as an
        -- insertion anchor.
        for _, player in ipairs(Util.players()) do
            RaidRuntime.rememberSingleplayerRaidPosition(
                data, player, Util.username(player), nil)
        end
    end
    -- Reassert transition protection before participant reconciliation can
    -- interpret a protected survivor as dead or remove them from the raid.
    if runShared then processBoardingProtections() end
    local vehicleLobbyChanged = RaidRuntime.reconcileVehicleInsertion(data)
    local participantsChanged = pruneAndReconcileParticipants()
    data = getStore()
    local raidActive = data.state == Config.STATE_RAID
        or data.state == Config.STATE_EXTRACTING or data.state == Config.STATE_BOARDING
    local lateJoinsChanged = RaidRuntime.processLateJoins(data)
    local vehicleLateJoinsChanged = RaidRuntime.processVehicleLateJoins(data)
    if singleplayerAuthority() and raidActive and #Util.players() == 0 then
        -- Do not advance horde or extraction state before the saved character
        -- and its raid position have finished loading.
        return
    end
    -- Hideout state is otherwise mostly idle, so a client that joins after an
    -- earlier snapshot was dropped may never receive another one. A low-rate
    -- authoritative heartbeat makes joining and UI recovery self-healing.
    if runShared and nowSecond - lastStateHeartbeat >= 5 then
        lastStateHeartbeat = nowSecond
        broadcastState()
    elseif vehicleLobbyChanged or participantsChanged or lateJoinsChanged
        or vehicleLateJoinsChanged then
        broadcastState()
    end
    if runShared then
        AnimalExtraction.processPending()
    end
    BanditsIntegration.process(data, RaidRuntime.playersForRaid(data))
    for _, player in ipairs(Util.players()) do
        if RaidRuntime.dataForPlayer(player) == data then
            local username = Util.username(player)
            local reconnectResult = RaidLoss.handleDisconnectedRaidReconnect(data, player)
            retainExpiredCarriedQuestItems(player, data.closedRaidIds)
            local playerBelongsInHideout = reconnectResult == nil
                and (data.state == Config.STATE_HIDEOUT or data.state == Config.STATE_COUNTDOWN
                    or data.participants[username] ~= true)
        -- A listen-server host can finish spawning before its first client
        -- command is accepted. Enforce the authoritative safe cell until the
        -- position arrives, which also protects late joiners during a raid.
            if playerBelongsInHideout and not Threats.playerInsideHideoutCell(player)
                and not (singleplayerAuthority() and singleplayerRaidResumePending)
                and RaidRuntime.shouldEnforceHideout(player) then
                teleport(player, Config.hideout(), 1)
            end
            ensureStarterFlashlight(player)
            ensureStarterM9Kit(player)
            RaidRuntime.ensureStarterNightstick(player)
            local bookRewards = BookXP.processPlayer(player, nowSecond)
            for _, reward in ipairs(bookRewards or {}) do
                deliver(player, "Announcement", {
                    message = "Study complete: gained " .. tostring(reward.amount) .. " "
                        .. tostring(reward.skill) .. " XP (training through level "
                        .. tostring(reward.level) .. ").",
                    messageKey = "IGUI_ExtractionMode_Message_StudyComplete",
                    messageArgs = { tostring(reward.amount),
                        { key = reward.skillKey, fallback = tostring(reward.skill) }, tostring(reward.level) },
                })
            end
            Infection.clampInHideout(player)
            if HideoutBenefits.processServerPlayer(player, data) then
                deliver(player, "InfectionPrevented", {})
                deliver(player, "Announcement", {
                    message = "Medical support prevented a Knox infection from taking hold.",
                    messageKey = "IGUI_ExtractionMode_Message_InfectionPrevented",
                })
            end
        end
    end

    if runShared then
        HideoutAuthority.processGenerator(nowSecond)
        HideoutBenefits.refreshHeating(data)
        local daily = Logistics.processDaily(data)
        announceLogisticsResult(daily, false)
        if daily.townsChanged then
            for _, raid in pairs(RaidRuntime.getRootStore().raids) do
                if raid.state == Config.STATE_HIDEOUT and raid.selectedTownKey
                    and not Logistics.isTownAvailable(RaidRuntime.raidView(raid), raid.selectedTownKey) then
                    raid.selectedTownKey = nil
                    raid.selectedTownBy = nil
                    raid.selectedJoinRaidKey = nil
                    raid.ready = {}
                    raid.optedOut = {}
                    raid.vehicleInsertion = nil
                end
            end
            broadcastState()
            announceLocalized("IGUI_ExtractionMode_Message_DestinationsRotated",
                "The day's available raid destinations have rotated.")
        end
    end
    RaidFlares.purgeHideoutPlayerFlares(data)
    RaidFlares.resolveLoadedRaidRoute()
    RaidFlares.rescueRaidParticipantsFromWater()
    RaidFlares.ensureExtractionSiteFlares()
    ensureRaidQuestItems()
    RaidQuests.processQuestVisitObjectives(data, nowSecond)
    processCampaignHandoff(data)
    if data.insertionCleanupUntilMs then
        if Util.timerNowMs() <= tonumber(data.insertionCleanupUntilMs) then
            RaidRoutes.clearInsertionZombies(activeRaidSpawn(data))
        else
            data.insertionCleanupUntilMs = nil
        end
    end
    if runShared and nowSecond - lastUtilityRefresh >= 2 then
        lastUtilityRefresh = nowSecond
        HideoutAuthority.refreshUtilities()
    end

    if data.state == Config.STATE_COUNTDOWN then
        if not allPlayersReady() then
            cancelCountdown()
        elseif secondsRemaining(data.countdownEndMs) <= 0 then
            beginRaidTransit()
        else
            broadcastState()
        end
    elseif data.state == Config.STATE_TRANSIT then
        if participantCount() == 0 and not hasReconnectReservation(data) then
            resetToHideout()
            return
        end
        if data.transitPhase == "VEHICLE_REBUILD" then
            local rebuilt, rebuildResult, rebuildExpired = RaidRuntime.processRaidVehicleRebuild(
                data, data.vehicleInsertion)
            if rebuilt then
                data.transitPhase = "ARRIVING"
                data.transitDeadlineMs = Util.timerNowMs() + 5000
                broadcastState()
            elseif rebuildExpired then
                RaidRuntime.recoverFailedRaidVehicleRebuild(data.vehicleInsertion)
                data.transitPhase = "ARRIVING"
                data.transitDeadlineMs = Util.timerNowMs() + 1500
                announceLocalized("IGUI_ExtractionMode_Error_VehicleInsertionFailed",
                    "The vehicle could not be inserted and was returned to its owner's garage.", {},
                    { teamOnly = true })
                Util.log("Vehicle raid reconstruction timed out raid=" .. tostring(data.raidId)
                    .. " error=" .. tostring(rebuildResult))
                broadcastState()
            end
        end
        if secondsRemaining(data.transitDeadlineMs) <= 0 then
            if data.transitPhase == "FADE_OUT" then
                local spawn = activeRaidSpawn(data)
                RaidRoutes.clearInsertionZombies(spawn)
                local vehicleRebuildStaged = false
                local manifest = type(data.vehicleInsertion) == "table" and data.vehicleInsertion or nil
                local vehicle = manifest and RaidRuntime.insertionVehicle(data) or nil
                if vehicle ~= nil then
                    local staged, stageError = RaidRuntime.stageRaidVehicleRebuild(
                        data, manifest, vehicle, spawn)
                    vehicleRebuildStaged = staged == true
                    if not vehicleRebuildStaged then
                        RaidRuntime.releaseVehicleReservation(data)
                        Util.log("Vehicle raid reconstruction staging failed vehicle="
                            .. tostring(manifest.vehicleId) .. " error=" .. tostring(stageError))
                    end
                    Util.log("Vehicle raid reconstruction staging "
                        .. (vehicleRebuildStaged and "accepted" or "failed")
                        .. " vehicle=" .. tostring(manifest.vehicleId)
                        .. " raid=" .. tostring(data.raidId))
                elseif manifest ~= nil then
                    RaidRuntime.releaseVehicleReservation(data)
                end
                local index = 0
                for _, player in ipairs(activePlayers()) do
                    local participantName = Util.username(player)
                    if data.participants[participantName] == true then
                        index = index + 1
                        local vehicleParticipant = manifest ~= nil
                            and type(manifest.participantNames) == "table"
                            and manifest.participantNames[participantName] == true
                        if not (vehicleRebuildStaged and vehicleParticipant) then
                            teleport(player, spawn, index, true, vehicleParticipant)
                        end
                    end
                end
                RaidRoutes.clearInsertionZombies(spawn)
                data.insertionCleanupUntilMs = Util.timerNowMs() + 5000
                data.transitPhase = vehicleRebuildStaged and "VEHICLE_REBUILD" or "ARRIVING"
                -- Players preload the destination under the fade. The authority
                -- tick reconstructs the vehicle once that road square is loaded.
                data.transitDeadlineMs = Util.timerNowMs()
                    + (vehicleRebuildStaged and 15000 or 1500)
                broadcastState()
            elseif data.transitPhase == "ARRIVING" then
                finishRaidTransit()
            else
                resetToHideout()
            end
        else
            broadcastState()
        end
    elseif data.state == Config.STATE_RAID or data.state == Config.STATE_EXTRACTING
        or data.state == Config.STATE_BOARDING then
        local deathRescueActive = processDeathRescues(data)
        local groundExtractionActive = processGroundExtractions(data)
        if participantCount() == 0 and not hasReconnectReservation(data) then
            resetToHideout()
            return
        end
        if deathRescueActive or groundExtractionActive then broadcastState() end
        if not data.hordeSpawned and tonumber(data.hordeAtHour) and Util.worldHours() >= tonumber(data.hordeAtHour) then
            local banditsSpawned = false
            if data.hordeEventType == "BANDITS" then
                local ok, count = BanditsIntegration.spawnHordeReplacement(data, activePlayers())
                if ok then
                    data.hordeSpawned = true
                    banditsSpawned = true
                    broadcastState()
                    announceLocalized("IGUI_ExtractionMode_Message_BanditForceArrived",
                        "A large, lightly equipped bandit force has arrived. Move now.",
                        {}, { participantsOnly = true })
                    Util.log("Bandit force replaced the raid horde with " .. tostring(count) .. " attacker(s)")
                else
                    Util.log("Bandit horde replacement failed; spawning the zombie horde instead: " .. tostring(count))
                end
            end
            if not banditsSpawned then
                data.hordeEventType = "ZOMBIES"
                Threats.spawnHorde()
            end
        end
        if data.state == Config.STATE_EXTRACTING then
            local remaining = secondsRemaining(data.extractionEndMs)
            if remaining <= Config.HELICOPTER_ACTIVE_APPROACH_SECONDS
                and data.extractionHelicopterStarted ~= true then
                Threats.startExtractionHelicopter(data)
            end
            if remaining <= 0 then
                beginBoarding()
            else
                broadcastState()
            end
        elseif data.state == Config.STATE_BOARDING then
            completePendingBoardings()
            if participantCount() == 0 and not hasReconnectReservation(data) then
                resetToHideout()
                return
            elseif secondsRemaining(data.boardingEndMs) <= 0
                and not tableHasEntries(data.boardingPending) then
                endBoarding()
            else
                broadcastState()
            end
        end
    end
end

local function onTick()
    local root = RaidRuntime.getRootStore()
    -- Campaign blackout timing is frame-sensitive; each raid still owns its own
    -- handoff even though the underlying world clock is shared.
    for key, raid in pairs(root.raids) do
        if raid.campaignHandoff and raid.campaignHandoff.phase == "FADING" then
            local previousRaid, previousKey = RaidRuntime.context, RaidRuntime.contextKey
            RaidRuntime.context, RaidRuntime.contextKey = raid, key
            processCampaignHandoff(RaidRuntime.raidView(raid))
            RaidRuntime.context, RaidRuntime.contextKey = previousRaid, previousKey
        end
    end
    local nowSecond = math.floor(Util.timerNowMs() / 1000)
    if nowSecond == lastSecond then return end
    lastSecond = nowSecond
    local garageChanged = ExtractionMode.GarageAuthority.tick(root, Util.players(), deliver)
    GarageDoorAuthority.tick(root)
    if garageChanged then broadcastState() end
    local first = true
    for key, raid in pairs(root.raids) do
        local previousRaid, previousKey = RaidRuntime.context, RaidRuntime.contextKey
        RaidRuntime.context, RaidRuntime.contextKey = raid, key
        RaidRuntime.onTickForRaid(nowSecond, first)
        RaidRuntime.context, RaidRuntime.contextKey = previousRaid, previousKey
        first = false
    end
    RaidRuntime.pruneDormantRaidRecords(root)
end

local function sampleTimerClockWhilePaused()
    Util.timerNowMs()
end

local function completeUpgradeState(data, definition)
    data.upgrades[definition.id] = true
    if definition.id == "lighting" then HideoutAuthority.activateInstalledLighting() end
    local logisticsResult = Logistics.onUpgradeInstalled(data, definition.id)
    HideoutBenefits.refreshHeating(data)
    ExtractionMode.ProgressionBackup.write(RaidRuntime,
        "upgrade installed: " .. tostring(definition.id))
    return logisticsResult
end

local function clearLuaTable(values)
    for key in pairs(values or {}) do values[key] = nil end
end

local function debugResetToFinalQuest(data, player)
    local finalQuestId = "one_last_flight"
    local questProgress, questObjectives, questTrust, questOwner = questStateFor(data, player)
    clearLuaTable(questProgress)
    clearLuaTable(questObjectives)
    clearLuaTable(questTrust)
    for _, definition in ipairs(Quests.definitions()) do
        if definition.id ~= finalQuestId then
            questProgress[definition.id] = true
            Quests.applyRewards(questTrust, definition)
        end
    end
    data.campaignHandoff = nil
    return questOwner
end

local function debugExtractAllPlayers(data)
    local extracted = 0
    data.groundExtractionPending = data.groundExtractionPending or {}
    local playersByName = {}
    for _, candidate in ipairs(activePlayers()) do
        playersByName[Util.username(candidate)] = candidate
    end
    local vehicleGroups = {}
    for participantName, participating in pairs(data.participants or {}) do
        local participant = participating == true and playersByName[participantName] or nil
        if participant ~= nil and not participant:isDead() then
            local vehicle = participant:getVehicle()
            if vehicle ~= nil then
                local vehicleId = tostring(vehicle:getId())
                local group = vehicleGroups[vehicleId]
                if group == nil then
                    group = { vehicle = vehicle }
                    vehicleGroups[vehicleId] = group
                end
            end
        end
    end

    local preparedVehicles = {}
    for vehicleId, group in pairs(vehicleGroups) do
        local driver = nil
        pcall(function() driver = group.vehicle:getDriver() end)
        local driverUsername = driver and Util.username(driver) or ""
        if driver == nil or driver:isDead() or data.participants[driverUsername] ~= true then
            for _, prepared in ipairs(preparedVehicles) do
                RaidRuntime.cancelPreparedGroundExtractionVehicle(
                    data, prepared.vehicle, prepared.record)
            end
            return 0, "Vehicle " .. vehicleId
                .. " cannot be extracted because it has no living raid-participant driver."
        end
        local record = RaidRuntime.prepareGroundExtractionVehicle(
            data, group.vehicle, Util.garageUsername(driver))
        if record == nil or record.captureFailed == true then
            if record ~= nil then
                RaidRuntime.cancelPreparedGroundExtractionVehicle(data, group.vehicle, record)
            end
            for _, prepared in ipairs(preparedVehicles) do
                RaidRuntime.cancelPreparedGroundExtractionVehicle(
                    data, prepared.vehicle, prepared.record)
            end
            return 0, "Vehicle " .. vehicleId
                .. " could not be saved safely: "
                .. tostring(record and record.captureError or "unknown capture error")
        end
        preparedVehicles[#preparedVehicles + 1] = {
            vehicle = group.vehicle,
            record = record,
        }
    end
    for _, prepared in ipairs(preparedVehicles) do
        AnimalExtraction.queueNearbyTamedAnimals({
            x = prepared.vehicle:getX(),
            y = prepared.vehicle:getY(),
            z = prepared.vehicle:getZ(),
        }, Config.TAMED_ANIMAL_EXTRACTION_RADIUS)
    end

    local index = 0
    for participantName, participating in pairs(data.participants or {}) do
        if participating == true then
            local participant = playersByName[participantName]
            if participant and not participant:isDead() then
                index = index + 1
                applyExtractionHealing(participant)
                RaidQuests.markSuccessfulRaidVisitExtraction(data, participant)
                data.groundExtractionPending[participantName] = nil
                teleport(participant, Config.hideout(), index, false, true)
                extracted = extracted + 1
            else
                data.returnPending[participantName] = true
            end
        end
    end
    data.groundExtractionPending = {}
    RaidRuntime.finalizeGroundExtractionVehicles(data, playersByName)
    resetToHideout()
    return extracted, nil, #preparedVehicles
end

local function onClientCommandInRaid(module, command, player, args)
    if module ~= Config.COMMAND_MODULE or player == nil then return end
    args = args or {}
    local data = getStore()
    local username = Util.username(player)
    if username == "" then return end

    if command == "AcknowledgeCoopWelcome" then
        local root = RaidRuntime.getRootStore()
        root.coopWelcomeAcknowledged[username] = Config.COOP_WELCOME_VERSION
        local playerData = player:getModData()
        playerData.ExtractionModeCoopWelcomeVersion = Config.COOP_WELCOME_VERSION
        pcall(function() player:transmitModData() end)
        sendState(player)
        return
    end

    if command == "GarageActivity" then
        local root = RaidRuntime.getRootStore()
        local active = root.activeHideoutVehicle
        local closeEnough = false
        if type(active) == "table" and tostring(active.vehicleId) == tostring(args.vehicleId) then
            local currentVehicle = player:getVehicle()
            closeEnough = currentVehicle ~= nil
                and tostring(currentVehicle:getId()) == tostring(active.vehicleId)
            if not closeEnough then
                local dx = player:getX() - (tonumber(active.x) or player:getX())
                local dy = player:getY() - (tonumber(active.y) or player:getY())
                closeEnough = dx * dx + dy * dy <= 144
            end
        end
        if closeEnough then
            ExtractionMode.GarageAuthority.markInteraction(root, args.vehicleId,
                Util.garageUsername(player))
        end
        return
    end

    local garageCommand = command == "GarageDeploy" or command == "GarageStoreActive"
        or command == "GarageRename"
        or command == "GarageTransferActiveToDriver" or command == "GarageDelete"
    if garageCommand then
        local root = RaidRuntime.getRootStore()
        local garageOwner = Util.garageUsername(player)
        if data.state ~= Config.STATE_HIDEOUT or player:isDead()
            or not Util.playerNear(player, Config.hideout(), Config.hideout().radius) then
            deliver(player, "Error", { message = "Garage controls are only available while alive in the hideout." })
            return
        end
        if command == "GarageDeploy" then
            local ok, result = ExtractionMode.GarageAuthority.spawn(root, player,
                args.garageId, Util.players(), deliver)
            if not ok then
                deliver(player, "Error", { message = tostring(result) })
                sendState(player)
                return
            end
            broadcastState()
            deliver(player, "Announcement", {
                message = type(result) == "table" and result.queued == true
                    and "Vehicle swap started. The active vehicle is being returned before yours is deployed."
                    or "Your vehicle was deployed in the hideout garage.",
            })
            return
        end
        if command == "GarageStoreActive" then
            local active = root.activeHideoutVehicle
            if type(active) ~= "table" then
                deliver(player, "Error", { message = "There is no active hideout vehicle." })
                return
            end
            if tostring(active.owner) ~= tostring(garageOwner) then
                deliver(player, "Error", { message = "Only the active vehicle's owner can return it." })
                return
            end
            local ok, result = ExtractionMode.GarageAuthority.beginStore(root,
                "returned by owner", garageOwner, Util.players(), deliver)
            if not ok then
                deliver(player, "Error", { message = tostring(result) })
            else
                deliver(player, "Announcement", { message = "Returning your active vehicle to the garage." })
                broadcastState()
            end
            return
        end
        if command == "GarageRename" then
            local ok, message = ExtractionMode.Garage.rename(root, garageOwner,
                args.garageId, args.name)
            if not ok then deliver(player, "Error", { message = tostring(message) }) end
            sendState(player)
            return
        end
        if command == "GarageTransferActiveToDriver" then
            local ok, result = ExtractionMode.GarageAuthority.transferActiveToDriver(
                root, garageOwner, args.expectedDriverUsername)
            if not ok then
                deliver(player, "Error", { message = tostring(result) })
                sendState(player)
            else
                local vehicleName = tostring(result.vehicleName or "vehicle")
                local driverName = tostring(result.driverUsername or result.owner or "the driver")
                deliver(player, "Announcement", {
                    message = "Transferred " .. vehicleName .. " to " .. driverName .. ".",
                })
                for _, candidate in ipairs(Util.players()) do
                    if Util.garageUsername(candidate) == tostring(result.owner) then
                        deliver(candidate, "Announcement", {
                            message = tostring(result.previousOwner)
                                .. " transferred ownership of " .. vehicleName .. " to you.",
                        })
                    end
                end
                broadcastState()
            end
            return
        end
        if command == "GarageDelete" then
            local active = root.activeHideoutVehicle
            if type(active) == "table" and type(active.storePending) == "table" then
                deliver(player, "Error", {
                    message = "Wait for the current vehicle return or swap to finish before deleting a vehicle.",
                })
                return
            end
            if type(active) == "table" and active.record ~= nil
                and tostring(active.record.id) == tostring(args.garageId) then
                deliver(player, "Error", {
                    message = "Return the active vehicle to the garage before deleting it.",
                })
                return
            end
            local ok, result = ExtractionMode.Garage.delete(root, garageOwner, args.garageId)
            if not ok then
                deliver(player, "Error", { message = tostring(result) })
            else
                deliver(player, "Announcement", {
                    message = "Deleted " .. tostring(result.name or result.modelKey or "vehicle")
                        .. " and all of its stored cargo.",
                })
            end
            sendState(player)
            return
        end
    end

    local debugCommand = command == "DebugSpawnExtractionHorde"
        or command == "DebugSpawnLateRaidHorde"
        or command == "DebugSpawnBanditRaid"
        or command == "DebugBeginExtractionHere"
        or command == "DebugCompleteUpgrade"
        or command == "DebugCompleteQuest"
        or command == "DebugFillGenerator"
        or command == "DebugExtractAllPlayers"
        or command == "DebugResetFinalQuest"
        or command == "DebugRefreshTownChoices"
        or command == "DebugRenameGarageVehicle"
        or command == "DebugDeleteGarageVehicle"
        or command == "DebugStoreActiveHideoutVehicle"
        or command == "DebugGiveGarageVehicle"
        or command == "DebugGiveActiveHideoutVehicle"
    if debugCommand then
        if not debugAuthorized(player) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_DebugUnauthorized",
                "Extraction Mode debug tools are not authorized.")
            return
        end
        if command == "DebugRenameGarageVehicle" then
            local ok, message = ExtractionMode.Garage.rename(RaidRuntime.getRootStore(),
                Util.garageUsername(player),
                args.garageId, args.name)
            if not ok then
                deliver(player, "Error", { message = message })
            else
                sendState(player)
            end
            return
        end
        if command == "DebugDeleteGarageVehicle" then
            local ok, result = ExtractionMode.Garage.delete(
                RaidRuntime.getRootStore(), Util.garageUsername(player), args.garageId)
            if not ok then
                deliver(player, "Error", { message = result })
            else
                sendState(player)
                deliver(player, "Announcement", {
                    message = "Deleted " .. tostring(result.name or result.modelKey or "vehicle")
                        .. " from your personal garage.",
                })
            end
            return
        end
        if command == "DebugStoreActiveHideoutVehicle" then
            local root = RaidRuntime.getRootStore()
            local active = root.activeHideoutVehicle
            local ok, message = false, "There is no active hideout vehicle."
            if type(active) == "table"
                and tostring(active.owner) ~= Util.garageUsername(player) then
                message = "Only the active vehicle's owner can store it."
            elseif type(active) == "table" then
                ok, message = ExtractionMode.GarageAuthority.beginStore(
                    root, "stored by owner", username, Util.players(), deliver)
            end
            if not ok then deliver(player, "Error", { message = message }) end
            sendState(player)
            return
        end
        if command == "DebugGiveGarageVehicle" or command == "DebugGiveActiveHideoutVehicle" then
            local recipientName = tostring(args.recipient or "")
            local recipient = nil
            for _, candidate in ipairs(Util.players()) do
                if Util.username(candidate) == recipientName then recipient = candidate break end
            end
            if recipient == nil or recipientName == username then
                deliver(player, "Error", { message = "Choose another player who is currently online." })
                return
            end
            local ok, result = false, nil
            if command == "DebugGiveGarageVehicle" then
                ok, result = ExtractionMode.GarageAuthority.transferStored(
                    RaidRuntime.getRootStore(), username, recipientName, args.garageId)
            else
                ok, result = ExtractionMode.GarageAuthority.giveActive(
                    RaidRuntime.getRootStore(), username, recipientName, Util.players(), deliver)
            end
            if not ok then
                deliver(player, "Error", { message = result })
            else
                sendState(player)
                sendState(recipient)
                deliver(player, "Announcement", {
                    message = command == "DebugGiveActiveHideoutVehicle"
                        and "Vehicle transfer to " .. recipientName .. " has started."
                        or "Vehicle transferred to " .. recipientName .. ".",
                })
                deliver(recipient, "Announcement", {
                    message = command == "DebugGiveActiveHideoutVehicle"
                        and username .. " is storing a vehicle in your personal garage."
                        or username .. " gave you a vehicle for your personal garage.",
                })
            end
            return
        end
        if command == "DebugCompleteUpgrade" or command == "DebugCompleteQuest"
            or command == "DebugFillGenerator" or command == "DebugResetFinalQuest"
            or command == "DebugRefreshTownChoices" then
            if data.state ~= Config.STATE_HIDEOUT or player:isDead()
                or not Infection.playerInsideHideout(player) then
                deliver(player, "Error", {
                    message = "Debug auto-complete controls require a living survivor in the idle hideout.",
                })
                return
            end

            if command == "DebugFillGenerator" then
                local capacity = Generator.capacity()
                if (tonumber(data.generatorFuel) or 0) >= capacity - 0.0001 then
                    deliver(player, "Error", { message = "The generator tank is already full." })
                    sendState(player)
                    return
                end
                data.generatorFuel = capacity
                broadcastState()
                announce("DEBUG: " .. username .. " filled the hideout generator tank to "
                    .. string.format("%.1f", capacity) .. " L.")
                return
            end

            if command == "DebugResetFinalQuest" then
                local questOwner = debugResetToFinalQuest(data, player)
                broadcastState()
                RaidQuests.announceQuestGroup(questOwner, "DEBUG: " .. username
                    .. " reset the " .. (questOwner.kind == "faction" and "faction" or "personal")
                    .. " campaign to the final quest: One Last Flight.")
                return
            end

            if command == "DebugRefreshTownChoices" then
                Logistics.forceRefreshTownChoices(data)
                broadcastState()
                announce("DEBUG: " .. username .. " refreshed today's raid destinations.")
                return
            end

            if command == "DebugCompleteUpgrade" then
                local definition = Upgrades.definition(args.upgradeId)
                if definition == nil then
                    deliver(player, "Error", { message = "That hideout upgrade does not exist." })
                    return
                end
                if Upgrades.isInstalled(data.upgrades, definition.id) then
                    deliver(player, "Error", { message = definition.name .. " is already installed." })
                    sendState(player)
                    return
                end
                local logisticsResult = completeUpgradeState(data, definition)
                broadcastState()
                announce("DEBUG: " .. username .. " auto-completed " .. definition.name .. ".", {
                    audioCue = "upgrade_installed",
                })
                announceLogisticsResult(logisticsResult, true)
                return
            end

            local definition = Quests.definition(args.questId)
            if definition == nil then
                deliver(player, "Error", { message = "That quest does not exist." })
                return
            end
            local questProgress, _, questTrust, questOwner = questStateFor(data, player)
            if Quests.isCompleted(questProgress, definition.id) then
                deliver(player, "Error", { message = definition.name .. " is already completed." })
                sendState(player)
                return
            end
            questProgress[definition.id] = true
            Quests.applyRewards(questTrust, definition)
            RaidQuests.grantQuestItemRewards(data, questOwner, definition)
            ExtractionMode.ProgressionBackup.write(RaidRuntime,
                "debug quest completed: " .. tostring(definition.id))
            broadcastState()
            RaidQuests.announceQuestGroup(questOwner, "DEBUG: " .. username .. " auto-completed the "
                .. (questOwner.kind == "faction" and "faction" or "personal")
                .. " quest: " .. definition.name .. ".")
            return
        end

        local raidActive = data.state == Config.STATE_RAID or data.state == Config.STATE_EXTRACTING
            or data.state == Config.STATE_BOARDING
        if not raidActive or data.participants[username] ~= true or player:isDead() then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_DebugRaidParticipant",
                "Debug raid tools require a living raid participant.")
            return
        end

        if command == "DebugSpawnExtractionHorde" then
            local spawned = Threats.spawnExtractionHorde(player)
            deliver(player, "Announcement", {
                message = "DEBUG: Spawned " .. tostring(spawned) .. " extraction-response zombie(s).",
            })
        elseif command == "DebugExtractAllPlayers" then
            local extracted, extractionError, extractedVehicles = debugExtractAllPlayers(data)
            if extractionError ~= nil then
                deliver(player, "Error", { message = "DEBUG extraction cancelled: " .. extractionError })
            else
                announce("DEBUG: " .. username .. " immediately extracted "
                    .. tostring(extracted) .. " active player(s) and stored "
                    .. tostring(extractedVehicles or 0) .. " raid vehicle(s).")
            end
        elseif command == "DebugSpawnLateRaidHorde" then
            local spawned = Threats.spawnHorde(true)
            deliver(player, "Announcement", {
                message = "DEBUG: Spawned " .. tostring(spawned) .. " late-raid horde zombie(s).",
            })
        elseif command == "DebugSpawnBanditRaid" then
            local ok, result, clanName = BanditsIntegration.spawnDebugRaid(
                data, activePlayers(), args.clanId)
            if ok then
                announce("DEBUG: Spawned " .. tostring(result) .. " hostile "
                    .. tostring(clanName) .. " bandit(s).", { participantsOnly = true })
            else
                deliver(player, "Error", { message = "Bandit raid failed: " .. tostring(result) })
            end
        elseif command == "DebugBeginExtractionHere" then
            if data.state ~= Config.STATE_RAID then
                deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_ExtractionAlreadyActive",
                    "Finish the current extraction event before starting another.")
                return
            end
            local site = debugExtractionSite(player)
            if site == nil then
                deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_DebugExtractionOutdoors",
                    "Stand on an outdoor land tile to start a debug extraction.")
                return
            end
            beginExtraction(player, site, { skipFlare = true, debug = true })
        end
        return
    end

    if command == "RescueFromDeath" then
        local raidActive = data.state == Config.STATE_RAID
            or data.state == Config.STATE_EXTRACTING or data.state == Config.STATE_BOARDING
        if not raidActive or RaidOutcomes.deathHandlingMode() <= 1
            or data.participants[username] ~= true then
            deliver(player, "DeathRescueRejected", {})
        elseif data.deathRescuePending[username] ~= nil then
            -- A server-side terminal-death fallback may win the race against
            -- this command. The rescue is already accepted; do not reject it or
            -- repeat the equipment loss.
            sendState(player)
        elseif not beginDeathRescue(data, player, args) then
            deliver(player, "DeathRescueRejected", {})
        end
        return
    end

    if command == "FinalizeDeathRescue" then
        local pending = data.deathRescuePending[username]
        if pending and pending.phase == "CINEMATIC" and data.participants[username] == true then
            if finalizeDeathRescue(data, player, username, pending) then broadcastState() end
        end
        return
    end

    if command == "RequestState" then
        local reconnectResult = RaidLoss.handleDisconnectedRaidReconnect(data, player)
        -- Project Remnants can expose the original body name for a few startup
        -- ticks before the controlled survivor identity settles. A RequestState
        -- routed through that temporary owner's hideout record must not move a
        -- correctly loaded raid character before resume reconciliation runs.
        if RaidRuntime.deferSingleplayerResumeStateRequest(data, player, username) then
            sendState(player)
            return
        end
        if reconnectResult ~= nil then
            sendState(player)
            return
        end
        if (data.state == Config.STATE_TRANSIT or data.state == Config.STATE_RAID
            or data.state == Config.STATE_EXTRACTING or data.state == Config.STATE_BOARDING)
            and data.participants[username] == true then
            data.flareUpgradePending[username] = nil
            RaidFlares.giveFlare(player)
        end
        if data.returnPending[username] == true and not player:isDead() then
            data.returnPending[username] = nil
            data.participants[username] = nil
            data.boardingPending[username] = nil
            data.groundExtractionPending[username] = nil
            data.deathRescuePending[username] = nil
            data.flareUpgradePending[username] = nil
            releaseBoardingProtection(player, username)
            teleport(player, Config.hideout(), 1)
        elseif data.state == Config.STATE_HIDEOUT or data.state == Config.STATE_COUNTDOWN then
            -- State synchronization is informational for a player who is
            -- already anywhere inside the protected hideout cell. Reapplying
            -- the anchor teleport here caused pause/resume and load-time state
            -- requests to snap them back to the original spawn square.
            if not Threats.playerInsideHideoutCell(player) then
                if RaidRuntime.shouldEnforceHideout(player) then
                    Util.log("State request returned " .. tostring(username)
                        .. " to the hideout from " .. tostring(math.floor(tonumber(player:getX()) or 0))
                        .. "," .. tostring(math.floor(tonumber(player:getY()) or 0)))
                    teleport(player, Config.hideout(), 1)
                    local playerData = player:getModData()
                    if playerData then
                        playerData.ExtractionModeHideoutSpawnRandomized = true
                        pcall(function() player:transmitModData() end)
                    end
                end
            else
                local playerData = player:getModData()
                if playerData.ExtractionModeHideoutSpawnRandomized ~= true then
                    playerData.ExtractionModeHideoutSpawnRandomized = true
                    pcall(function() player:transmitModData() end)
                    -- A newly created character is already inside the hideout,
                    -- so it bypasses the return teleport above. Scatter only a
                    -- first request made near the authored spawn corridor; an
                    -- established survivor elsewhere in the building is marked
                    -- without being unexpectedly moved.
                    if Util.playerNear(player, Config.hideout(), 6) then
                        teleport(player, Config.hideout(), 1)
                    end
                end
            end
        elseif data.state == Config.STATE_TRANSIT and data.participants[username] == true then
            if data.transitPhase == "ARRIVING" then
                teleport(player, activeRaidSpawn(data), 1, true)
                deliver(player, "FadeIn", { seconds = tonumber(Config.value("TransitFadeSeconds")) or 1 })
            else
                deliver(player, "FadeOut", { seconds = tonumber(Config.value("TransitFadeSeconds")) or 1 })
            end
        elseif data.state == Config.STATE_BOARDING and data.boardingPending[username] then
            deliver(player, "FadeOut", { seconds = tonumber(Config.value("TransitFadeSeconds")) or 1 })
        elseif data.deathRescuePending[username]
            and data.deathRescuePending[username].phase ~= "CINEMATIC" then
            deliver(player, "FadeOut", {
                seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
                leaveVehicle = true,
            })
        elseif data.groundExtractionPending[username]
            and data.groundExtractionPending[username].phase == "FADING" then
            deliver(player, "FadeOut", {
                seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
                leaveVehicle = true,
            })
        elseif data.participants[username] ~= true then
            -- OnCreatePlayer can request state before a Project Remnants
            -- successor identity has been reconciled. During single-player raid
            -- resume, leave the loaded position untouched until that pass runs.
            if not (singleplayerAuthority() and singleplayerRaidResumePending)
                and RaidRuntime.shouldEnforceHideout(player) then
                teleport(player, Config.hideout(), 1)
            end
        end
        sendState(player)
        return
    end

    if command == "BoardExtraction" then
        if data.state ~= Config.STATE_BOARDING then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_HelicopterNotReady",
                "The helicopter is not ready for boarding.")
            return
        end
        if data.participants[username] ~= true or player:isDead() then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_OnlyParticipantBoard",
                "Only a living raid participant can board.")
            return
        end
        if player:getVehicle() ~= nil then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_BoardVehicle",
                "Exit the vehicle before boarding the extraction helicopter.")
            return
        end
        if data.boardingPending[username] then return end
        local rope = data.extractionRope
        local square = player:getCurrentSquare()
        if rope == nil or square == nil or not square:isOutside()
            or not Util.playerNear(player, rope, tonumber(rope.radius) or 3) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_MoveToLine",
                "Move up to the extraction line before boarding.")
            return
        end
        data.boardingPending[username] = Util.timerNowMs()
            + math.max(1, tonumber(Config.value("TransitFadeSeconds")) or 1) * 1000
        protectBoardingPlayer(player)
        RaidRuntime.applyExtractionShove(player, rope)
        deliver(player, "FadeOut", { seconds = tonumber(Config.value("TransitFadeSeconds")) or 1 })
        broadcastState()
        return
    end

    if command == "UseInfectionCure" then
        local inventory = player:getInventory()
        local itemId = tonumber(args.itemId)
        local item = nil
        if inventory and itemId then
            pcall(function() item = inventory:getItemById(itemId) end)
            if item == nil then
                pcall(function() item = inventory:getItemWithIDRecursiv(math.floor(itemId)) end)
            end
        end
        if inventory == nil or item == nil or item:getFullType() ~= Config.INFECTION_CURE_TYPE
            or not inventory:containsID(item:getID()) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_CureMissing",
                "The infection cure is no longer in your inventory.")
            return
        end

        local container = item:getContainer()
        if container == nil then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_CureFailed",
                "The infection cure could not be used.")
            return
        end
        container:Remove(item)
        if sendRemoveItemFromContainer then sendRemoveItemFromContainer(container, item) end
        Infection.cure(player)
        deliver(player, "InfectionCured", {})
        deliverLocalized(player, "Announcement", "IGUI_ExtractionMode_Message_CureAdministered",
            "Experimental treatment administered. Knox infection cleared.")
        return
    end

    if command == "CompleteQuest" then
        local definition = Quests.definition(args.questId)
        local questProgress, questObjectives, questTrust, questOwner = questStateFor(data, player)
        if definition == nil then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_QuestMissing",
                "That quest does not exist.")
            return
        end
        if data.state ~= Config.STATE_HIDEOUT then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_QuestDeploymentActive",
                "Quest materials can only be turned in while no deployment is underway.")
            return
        end
        if player:isDead() or not Infection.playerInsideHideout(player) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_QuestHideoutOnly",
                "You must be alive and inside the hideout to complete a quest.")
            return
        end
        if Quests.isCompleted(questProgress, definition.id) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_QuestAlreadyCompleted",
                definition.name .. " is already completed.",
                { key = definition.nameKey, fallback = definition.name })
            sendState(player)
            return
        end
        if not Quests.isAcquired(questProgress, definition) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_QuestNotAcquired",
                "That quest has not been acquired yet.")
            sendState(player)
            return
        end
        if not Quests.skillRequirementsMet(player, definition) then
            local names = table.concat(Quests.missingSkillNames(player, definition), ", ")
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_QuestSkills",
                "Requires " .. names .. " to complete " .. definition.name .. ".", names,
                { key = definition.nameKey, fallback = definition.name })
            sendState(player)
            return
        end
        if not Quests.objectivesMet(questObjectives, definition) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_QuestObjectives",
                "Complete every objective before turning in " .. definition.name .. ".",
                { key = definition.nameKey, fallback = definition.name })
            sendState(player)
            return
        end
        if RaidQuests.definitionHasRaidVisitObjective(definition)
            and not RaidQuests.raidVisitProgressIsSecured(data, questOwner) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_QuestRaidSuccessRequired",
                "Successfully extract from the raid where those locations were checked before turning in "
                    .. definition.name .. ".",
                { key = definition.nameKey, fallback = definition.name })
            sendState(player)
            return
        end

        local inventory = player:getInventory()
        if not Quests.requirementsMet(inventory, definition) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_QuestItems",
                "You are not carrying all items required for " .. definition.name .. ".",
                { key = definition.nameKey, fallback = definition.name })
            sendState(player)
            return
        end
        if not Quests.consumeRequirements(inventory, definition) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_QuestItemsChanged",
                "The required items changed before the quest turn-in completed.")
            sendState(player)
            return
        end

        questProgress[definition.id] = true
        Quests.applyRewards(questTrust, definition)
        RaidQuests.grantQuestItemRewards(data, questOwner, definition)
        ExtractionMode.ProgressionBackup.write(RaidRuntime,
            "quest completed: " .. tostring(definition.id))
        broadcastState()
        local questCompletionMessage = username .. " completed the "
            .. (questOwner.kind == "faction" and "faction" or "personal")
            .. " quest: " .. definition.name .. "."
        RaidQuests.announceQuestGroup(questOwner, questCompletionMessage,
            questOwner.kind == "faction" and "IGUI_ExtractionMode_Message_FactionQuestCompleted"
                or "IGUI_ExtractionMode_Message_PersonalQuestCompleted",
            { username, { key = definition.nameKey, fallback = definition.name } })
        return
    end

    if command == "CompleteBarter" then
        local definition = Barters.definition(args.barterId)
        local _, _, questTrust = questStateFor(data, player)
        if definition == nil then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_BarterMissing",
                "That barter trade does not exist.")
            print("[ExtractionMode] Rejected unknown barter id=" .. tostring(args.barterId)
                .. " player=" .. tostring(username))
            return
        end
        if data.state ~= Config.STATE_HIDEOUT then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_BarterDeploymentActive",
                "Barter trades are unavailable during a deployment.")
            sendState(player)
            return
        end
        if player:isDead() or not Infection.playerInsideHideout(player) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_BarterHideoutOnly",
                "You must be alive and inside the hideout to barter.")
            sendState(player)
            return
        end
        if not Barters.isUnlocked(questTrust, definition) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_BarterTrust",
                "You have not earned enough trust for that trade.")
            sendState(player)
            return
        end

        local inventory = player:getInventory()
        if not Barters.requirementsMet(inventory, definition) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_BarterItems",
                "You are not carrying the items required for that trade.")
            Util.log("Barter requirements unavailable: trade=" .. tostring(definition.id)
                .. " player=" .. tostring(username))
            sendState(player)
            return
        end

        -- Validate every output before consuming the offer. This prevents a bad
        -- or removed item type from taking payment without a possible reward.
        local rewards, invalidType = RaidQuests.prepareBarterRewardItems(definition)
        if rewards == nil then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_BarterRewardUnavailable",
                "That trade's reward is currently unavailable.")
            print("[ExtractionMode] Could not create barter reward " .. tostring(invalidType)
                .. " for trade " .. tostring(definition.id))
            sendState(player)
            return
        end
        if not Barters.consumeRequirements(inventory, definition) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_BarterItemsChanged",
                "The offered items changed before the trade completed.")
            print("[ExtractionMode] Barter inventory changed during consumption: trade="
                .. tostring(definition.id) .. " player=" .. tostring(username))
            sendState(player)
            return
        end

        local granted = 0
        local dropped = 0
        for _, item in ipairs(rewards) do
            local ok, wasDropped = RaidQuests.givePreparedBarterItem(player, item)
            if ok then
                granted = granted + 1
                if wasDropped then dropped = dropped + 1 end
            end
        end
        if granted ~= #rewards then
            deliver(player, "Error", {
                message = "Some barter rewards could not be delivered. Check the server log.",
                messageKey = "IGUI_ExtractionMode_Error_BarterDeliveryFailed",
            })
            Util.log("Failed to deliver " .. tostring(#rewards - granted) .. " prepared reward item(s) for barter "
                .. tostring(definition.id) .. " to " .. tostring(username))
        else
            local message = "Trade completed with " .. Quests.contactName(definition.contactId) .. "."
            if dropped > 0 then message = message .. " Reward items were placed at your feet." end
            deliver(player, "Announcement", {
                message = message,
                messageKey = dropped > 0 and "IGUI_ExtractionMode_Message_BarterCompletedDropped"
                    or "IGUI_ExtractionMode_Message_BarterCompleted",
                messageArgs = { { key = Quests.contact(definition.contactId).nameKey,
                    fallback = Quests.contactName(definition.contactId) } },
                audioCue = "barter_completed",
            })
        end
        sendState(player)
        return
    end

    if command == "InstallUpgrade" then
        local definition = Upgrades.definition(args.upgradeId)
        if definition == nil then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_UpgradeMissing",
                "That hideout upgrade does not exist.")
            return
        end
        if data.state ~= Config.STATE_HIDEOUT then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_UpgradeDeploymentActive",
                "Hideout upgrades can only be installed while no deployment is underway.")
            return
        end
        if player:isDead() or not Infection.playerInsideHideout(player) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_UpgradeHideoutOnly",
                "You must be alive and inside the hideout to install an upgrade.")
            return
        end
        if Upgrades.isInstalled(data.upgrades, definition.id) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_UpgradeAlreadyInstalled",
                definition.name .. " is already installed.",
                { key = definition.nameKey, fallback = definition.name })
            sendState(player)
            return
        end
        if not Upgrades.prerequisitesMet(data.upgrades, definition) then
            local names = table.concat(Upgrades.missingPrerequisiteNames(data.upgrades, definition), ", ")
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_UpgradePrerequisites",
                "Install " .. names .. " before " .. definition.name .. ".", names,
                { key = definition.nameKey, fallback = definition.name })
            sendState(player)
            return
        end
        if not Upgrades.upgradeSkillRequirementsMet(player, definition) then
            local names = table.concat(Upgrades.missingUpgradeSkillNames(player, definition), ", ")
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_UpgradeSkills",
                "Requires " .. names .. " to install " .. definition.name .. ".", names,
                { key = definition.nameKey, fallback = definition.name })
            sendState(player)
            return
        end

        local inventory = player:getInventory()
        if not Upgrades.requirementsMet(inventory, definition) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_UpgradeMaterials",
                "You are not carrying all materials required for " .. definition.name .. ".",
                { key = definition.nameKey, fallback = definition.name })
            sendState(player)
            return
        end
        if not Upgrades.consumeRequirements(inventory, definition) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_UpgradeMaterialsChanged",
                "The required materials changed before installation completed.")
            sendState(player)
            return
        end

        local logisticsResult = completeUpgradeState(data, definition)
        broadcastState()
        announceLocalized("IGUI_ExtractionMode_Message_UpgradeInstalled",
            username .. " installed " .. definition.name .. " for the shared hideout.",
            { username, { key = definition.nameKey, fallback = definition.name } }, {
            audioCue = "upgrade_installed",
        })
        announceLogisticsResult(logisticsResult, true)
        return
    end

    if command == "AddGeneratorFuel" then
        if data.state ~= Config.STATE_HIDEOUT or player:isDead() or not Infection.playerInsideHideout(player) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_GeneratorHideoutOnly",
                "Generator controls are only available inside the idle hideout.")
            return
        end
        local freeCapacity = math.max(0, Generator.capacity() - (tonumber(data.generatorFuel) or 0))
        if freeCapacity <= 0.0001 then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_GeneratorFull",
                "The generator tank is already full.")
            return
        end
        local requested = math.min(freeCapacity, Generator.transferLimit())
        local added = Generator.consumeGasoline(player:getInventory(), requested)
        if added <= 0.0001 then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_NeedGasoline",
                "Carry a container holding pure gasoline to add fuel.")
            sendState(player)
            return
        end
        data.generatorFuel = math.min(Generator.capacity(), (tonumber(data.generatorFuel) or 0) + added)
        broadcastState()
        announceLocalized("IGUI_ExtractionMode_Message_GasolineAdded",
            username .. " added " .. string.format("%.1f", added) .. " L of gasoline to the hideout generator.",
            { username, string.format("%.1f", added) })
        return
    end

    if command == "SetGeneratorRunning" or command == "ToggleGenerator" then
        if data.state ~= Config.STATE_HIDEOUT or player:isDead() or not Infection.playerInsideHideout(player) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_GeneratorHideoutOnly",
                "Generator controls are only available inside the idle hideout.")
            return
        end
        -- New clients send the desired state, making simultaneous requests from
        -- multiple survivors idempotent. Retain ToggleGenerator only for a
        -- rolling-update client, where its legacy behavior is unavoidable.
        local desiredRunning
        if command == "SetGeneratorRunning" then
            desiredRunning = args.running == true
        else
            desiredRunning = data.generatorRunning ~= true
        end
        if desiredRunning == data.generatorRunning then
            sendState(player)
            return
        end
        if not desiredRunning then
            data.generatorRunning = false
            data.generatorLastWorldHour = Util.worldHours()
            HideoutAuthority.refreshUtilities()
            broadcastState()
            announceLocalized("IGUI_ExtractionMode_Message_GeneratorStopped",
                username .. " shut down the hideout generator. Electricity and water are offline.", { username })
            return
        end
        if (tonumber(data.generatorFuel) or 0) <= 0.0001 then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_StartGeneratorNoFuel",
                "Add gasoline before starting the generator.")
            return
        end
        data.generatorRunning = true
        data.generatorLastWorldHour = Util.worldHours()
        HideoutAuthority.refreshUtilities()
        if Upgrades.isInstalled(data.upgrades, "lighting") then
            HideoutAuthority.activateInstalledLighting()
        end
        broadcastState()
        announceLocalized("IGUI_ExtractionMode_Message_GeneratorStarted",
            username .. " started the hideout generator. Electricity and water are online.", { username })
        return
    end

    if command == "SelectTown" then
        if data.state ~= Config.STATE_HIDEOUT then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_DestinationLocked",
                "The destination cannot be changed after deployment begins.")
            return
        end
        if player:isDead() or not Util.playerNear(player, Config.hideout(), Config.hideout().radius) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_DestinationHideoutOnly",
                "You must be alive and inside the hideout to choose a destination.")
            return
        end
        Logistics.refreshTownChoices(data)
        local town = Config.town(args.townKey)
        local root = RaidRuntime.getRootStore()
        local owner = Groups.forPlayer(player, root.groupRegistry)
        local requestedRaidKey = args.activeRaidKey and tostring(args.activeRaidKey) or nil
        local factionRaid = requestedRaidKey and root.raids[requestedRaidKey] or nil
        local factionRaidOwner = factionRaid and (factionRaid.factionKey
            or (requestedRaidKey == tostring(owner.key) and owner.key)) or nil
        local selectingJoinableFactionRaid = owner.kind == "faction"
            and tostring(factionRaidOwner) == tostring(owner.key)
            and RaidRuntime.isJoinableRaid(factionRaid)
            and factionRaid.participants[username] ~= true
            and tostring(factionRaid.selectedTownKey) == tostring(args.townKey)
        local selectedVehicle = player:getVehicle()
        if selectedVehicle ~= nil then
            local driver = nil
            pcall(function() driver = selectedVehicle:getDriver() end)
            if driver == nil then
                deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_VehicleDriverRequired",
                    "A driver must be seated before choosing a vehicle raid destination.")
                return
            end
        end
        if town == nil or (not selectingJoinableFactionRaid
            and (not Logistics.isTownAvailable(data, town.key)
                or not questTownUnlockedFor(data, player, town))) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_TownUnavailable",
                "That town is not among today's available raid destinations.")
            sendState(player)
            return
        end
        data.selectedTownKey = town.key
        data.selectedTownBy = username
        data.selectedJoinRaidKey = selectingJoinableFactionRaid and requestedRaidKey or nil
        data.ready = {}
        data.optedOut = {}
        data.vehicleInsertion = nil
        if selectedVehicle ~= nil then
            data.vehicleInsertion = {
                vehicleId = tostring(selectedVehicle:getId()),
                selectedBy = username,
                occupants = {},
                hasDriver = true,
            }
            RaidRuntime.reconcileVehicleInsertion(data)
        end
        broadcastState()
        if type(data.vehicleInsertion) == "table" then
            announceLocalized("IGUI_ExtractionMode_Message_VehicleDestinationSelected",
                username .. " selected " .. town.name
                    .. " for vehicle insertion. Every occupant must ready up or leave the vehicle.",
                { username, { key = "IGUI_ExtractionMode_Town_" .. tostring(town.key), fallback = town.name } },
                { teamOnly = true })
        else
            announceLocalized("IGUI_ExtractionMode_Message_DestinationSelected",
                username .. " selected " .. town.name
                    .. ". Team members must ready up to deploy; multiplayer teammates may opt out.",
                { username, { key = "IGUI_ExtractionMode_Town_" .. tostring(town.key), fallback = town.name } },
                { teamOnly = true })
        end
        return
    end

    if command == "SetReady" then
        if data.state ~= Config.STATE_HIDEOUT and data.state ~= Config.STATE_COUNTDOWN then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_RaidAlreadyActive",
                "Your team already has an active raid. Wait until its participants return or die.")
            return
        end
        if player:isDead() or not Util.playerNear(player, Config.hideout(), Config.hideout().radius) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_ReadyHideoutOnly",
                "You must be alive and inside the hideout to ready up.")
            return
        end
        if Config.town(data.selectedTownKey) == nil then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_ChooseDestinationFirst",
                "Choose a raid destination before readying up.")
            return
        end
        RaidRuntime.reconcileVehicleInsertion(data)
        local ready = args.ready == true and true or nil
        if type(data.vehicleInsertion) == "table" then
            local vehicle = player:getVehicle()
            if vehicle == nil
                or tostring(vehicle:getId()) ~= tostring(data.vehicleInsertion.vehicleId) then
                deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_EnterInsertionVehicle",
                    "Enter the selected insertion vehicle before readying up.")
                broadcastState()
                return
            end
            local driver = nil
            pcall(function() driver = vehicle:getDriver() end)
            if driver == nil then
                deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_VehicleDriverRequired",
                    "A driver must be seated before vehicle occupants can ready up.")
                broadcastState()
                return
            end
            if ready == true then
                local available, required, tank = RaidRuntime.vehicleFuelDetails(vehicle)
                if required > 0 and (tank == nil or available + 0.0001 < required) then
                    deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_NotEnoughGas",
                        "Not enough gas!")
                    broadcastState()
                    return
                end
            end
        end
        local owner = Groups.forPlayer(player, data.groupRegistry)
        if owner.kind == "local-coop" then
            -- Split-screen has one shared raid HUD. A confirmation from either
            -- controller applies to the whole couch team so Deploy cannot wait
            -- on a second, inaccessible copy of the same button.
            local localReadyPlayers = type(data.vehicleInsertion) == "table"
                and RaidRuntime.eligibleLobbyPlayers(data) or RaidRuntime.playersForRaid(data)
            for _, localPlayer in ipairs(localReadyPlayers) do
                local belongsToCouch = true
                if isServer and isServer() then
                    belongsToCouch = false
                    pcall(function() belongsToCouch = localPlayer:isLocalPlayer() end)
                end
                if belongsToCouch then
                    local localUsername = Util.username(localPlayer)
                    data.ready[localUsername] = ready
                    if ready == true then data.optedOut[localUsername] = nil end
                end
            end
        else
            data.ready[username] = ready
            if ready == true then data.optedOut[username] = nil end
        end
        if RaidRuntime.readyEntryCount(data) == 0 then data.optedOut = {} end
        if data.state == Config.STATE_COUNTDOWN and not allPlayersReady() then
            cancelCountdown()
        elseif data.state == Config.STATE_HIDEOUT and allPlayersReady() then
            startCountdown()
        else
            broadcastState()
        end
        return
    end

    if command == "SetOptOut" then
        if not (isServer and isServer()) then return end
        if data.state ~= Config.STATE_HIDEOUT and data.state ~= Config.STATE_COUNTDOWN then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_OptOutUnavailable",
                "You can only opt out while your team is preparing a raid.")
            return
        end
        if player:isDead() or not Util.playerNear(player, Config.hideout(), Config.hideout().radius) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_ReadyHideoutOnly",
                "You must be alive and inside the hideout to opt out.")
            return
        end
        if type(data.vehicleInsertion) == "table" then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_LeaveVehicleToOptOut",
                "Leave the insertion vehicle to opt out of this raid.")
            return
        end
        local teamPlayers = RaidRuntime.playersForRaid(data)
        if #teamPlayers <= 1 or RaidRuntime.readyEntryCount(data) == 0 then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_OptOutUnavailable",
                "Opting out is available after another member of a multiplayer team readies up.")
            return
        end
        local optedOut = args.optedOut == true and true or nil
        data.optedOut[username] = optedOut
        if optedOut == true then data.ready[username] = nil end
        if RaidRuntime.readyEntryCount(data) == 0 then data.optedOut = {} end
        if data.state == Config.STATE_COUNTDOWN and not allPlayersReady() then
            cancelCountdown()
        elseif data.state == Config.STATE_HIDEOUT and allPlayersReady() then
            startCountdown()
        else
            broadcastState()
        end
        return
    end

    if command == "JoinFactionRaid" then
        if not (isServer and isServer()) or data.state ~= Config.STATE_HIDEOUT then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_JoinRaidUnavailable",
                "No active faction raid is available to join.")
            return
        end
        if player:isDead() or not Threats.playerInsideHideoutCell(player) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_JoinRaidHideoutOnly",
                "You must be alive and inside the hideout to join a raid.")
            return
        end
        local root = RaidRuntime.getRootStore()
        local owner = Groups.forPlayer(player, root.groupRegistry)
        local selectedRaidKey = data.selectedJoinRaidKey and tostring(data.selectedJoinRaidKey) or nil
        local factionRaid = selectedRaidKey and root.raids[selectedRaidKey] or nil
        local factionRaidOwner = factionRaid and (factionRaid.factionKey
            or (selectedRaidKey == tostring(owner.key) and owner.key)) or nil
        if owner.kind ~= "faction"
            or tostring(factionRaidOwner) ~= tostring(owner.key)
            or not RaidRuntime.isJoinableRaid(factionRaid)
            or factionRaid.participants[username] == true
            or tostring(factionRaid.selectedTownKey) ~= tostring(data.selectedTownKey) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_JoinRaidUnavailable",
                "Select your faction's active raid destination before joining.")
            sendState(player)
            return
        end
        factionRaid.lateJoinPending = factionRaid.lateJoinPending or {}
        factionRaid.lateInsertionCleanup = factionRaid.lateInsertionCleanup or {}
        if factionRaid.lateJoinPending[username] ~= nil then return end
        local deadline = Util.timerNowMs() + 10000
        factionRaid.lateJoinPending[username] = {
            raidId = factionRaid.raidId,
            deadlineMs = deadline,
            fadeAtMs = deadline - math.max(1,
                tonumber(Config.value("TransitFadeSeconds")) or 1) * 1000,
            phase = "WAITING",
        }
        factionRaid.optedOut[username] = nil
        root.playerRaidKeys[username] = selectedRaidKey
        deliverLocalized(player, "Announcement", "IGUI_ExtractionMode_Message_JoiningRaid",
            "Joining the active faction raid in 10 seconds. Remain inside the hideout.", "10")
        broadcastState()
        return
    end

    if command == "StartCampaignHandoff" then
        local definition = Quests.definition("one_last_flight")
        local completions, _, _, owner = questStateFor(data, player)
        if data.state ~= Config.STATE_RAID or data.selectedTownKey ~= "grand_ohio_mall"
            or data.participants[username] ~= true or player:isDead() then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_CampaignHandoffUnavailable",
                "The vaccine helicopter can only be signaled during an active Grand Ohio Mall raid.")
            return
        end
        if data.campaignHandoff ~= nil then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_CampaignHandoffActive",
                "The vaccine helicopter has already been signaled.")
            return
        end
        if definition == nil or not Quests.isAcquired(completions, definition)
            or Quests.isCompleted(completions, definition.id) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_CampaignQuestInactive",
                "One Last Flight is not active for your group.")
            return
        end
        if not atCampaignHandoff(player) then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_CampaignHelipad",
                "Reach the marked rooftop helipad before signaling the vaccine helicopter.")
            return
        end
        local sample = campaignSample(player)
        if sample == nil then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_VaccineSampleRequired",
                "Carry the Vaccine Sample onto the helipad before signaling.")
            return
        end
        data.campaignHandoff = {
            raidId = data.raidId,
            ownerKey = owner.key,
            startedBy = username,
            endMs = Util.timerNowMs() + math.max(10, tonumber(Config.CAMPAIGN_HANDOFF_SECONDS) or 180) * 1000,
            nextZombieAttractAtMs = 0,
        }
        data.extractionHelicopterStarted = false
        Util.log("Final quest handoff signaled: raid=" .. tostring(data.raidId)
            .. " owner=" .. tostring(owner.key)
            .. " player=" .. tostring(username)
            .. " sampleItemId=" .. tostring(questItemId(sample))
            .. " helipad=" .. tostring(Config.CAMPAIGN_HANDOFF_POINT.x) .. ","
            .. tostring(Config.CAMPAIGN_HANDOFF_POINT.y) .. ","
            .. tostring(Config.CAMPAIGN_HANDOFF_POINT.z)
            .. " holdSeconds=" .. tostring(Config.CAMPAIGN_HANDOFF_SECONDS))
        broadcastState()
        announceLocalized("IGUI_ExtractionMode_Message_CampaignHandoffStarted",
            username .. " signaled the vaccine helicopter. The Vaccine Sample carrier must remain on the helipad for three minutes.",
            { username }, { participantsOnly = true })
        return
    end

    if command == "FireFlare" then
        Util.log("Flare request from " .. username .. " via " .. tostring(args.source or "unknown"))
        if data.state ~= Config.STATE_RAID then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_ExtractionUnavailable",
                "Extraction is not available in the current raid state.")
            return
        end
        if data.participants[username] ~= true or player:isDead() then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_OnlyParticipantSignal",
                "Only a living raid participant can signal extraction.")
            return
        end
        if data.campaignHandoff ~= nil then
            RaidFlares.reloadFlare(player)
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_CampaignHandoffActive",
                "The vaccine helicopter is already inbound. Wait for the handoff before signaling extraction.")
            return
        end
        local flare = RaidFlares.equippedFlare(player)
        if flare == nil then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_EquipFlare",
                "Equip the extraction flare gun before firing it.")
            return
        end
        if tonumber(flare:getModData().ExtractionModeRaidId) ~= tonumber(data.raidId) then
            RaidFlares.reloadFlare(player)
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_FlareWrongRaid",
                "That flare belongs to a different raid instance.")
            return
        end
        local square = player:getCurrentSquare()
        if square == nil or not square:isOutside() then
            RaidFlares.reloadFlare(player)
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_FlareIndoors",
                "The helicopter cannot see a flare fired indoors.")
            return
        end
        for _, site in ipairs(activeExtractionSites(data)) do
            if Util.playerNear(player, site, site.radius) then beginExtraction(player, site); return end
        end
        RaidFlares.reloadFlare(player)
        local nearest, centerDistance = RaidFlares.nearestExtractionSite(player)
        local tilesAway = nearest and centerDistance
            and math.max(1, math.ceil(centerDistance - (tonumber(nearest.radius) or 12))) or nil
        if nearest and tilesAway and tilesAway <= Config.EXTRACTION_PROXIMITY_REVEAL_DISTANCE then
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_ExtractionDistance",
                "Too far from extraction. E" .. tostring(nearest.id)
                .. " is about " .. tostring(tilesAway) .. " tiles away.",
                tostring(nearest.id), tostring(tilesAway))
        else
            deliverLocalized(player, "Error", "IGUI_ExtractionMode_Error_TooFarExtraction",
                "Too far from a marked extraction site.")
        end
    end
end

local function onClientCommand(module, command, player, args)
    if module ~= Config.COMMAND_MODULE or player == nil then return end
    local raid, key = RaidRuntime.raidForPlayer(player)
    local previousRaid, previousKey = RaidRuntime.context, RaidRuntime.contextKey
    RaidRuntime.context, RaidRuntime.contextKey = raid, key
    local ok, err = pcall(onClientCommandInRaid, module, command, player, args)
    RaidRuntime.context, RaidRuntime.contextKey = previousRaid, previousKey
    if not ok then error(err) end
end

function Server.handleCommand(player, command, args)
    onClientCommand(Config.COMMAND_MODULE, command, player, args or {})
end

-- Garage entry points are also kept as callable helpers for local testing and
-- integrations; player-facing commands above apply the same authority rules.
function Server.spawnGarageVehicle(player, garageId)
    local ok, result = ExtractionMode.GarageAuthority.spawn(
        RaidRuntime.getRootStore(), player, garageId, Util.players(), deliver)
    if ok then broadcastState() end
    return ok, result
end

function Server.storeActiveHideoutVehicle(reason)
    local ok, result = ExtractionMode.GarageAuthority.beginStore(RaidRuntime.getRootStore(),
        reason or "stored", nil, Util.players(), deliver)
    if ok then broadcastState() end
    return ok, result
end

function Server.giveGarageVehicle(player, garageId, recipientUsername)
    local ok, result = ExtractionMode.GarageAuthority.transferStored(RaidRuntime.getRootStore(),
        Util.garageUsername(player), recipientUsername, garageId)
    if ok then broadcastState() end
    return ok, result
end

function Server.giveActiveHideoutVehicle(player, recipientUsername)
    local ok, result = ExtractionMode.GarageAuthority.giveActive(RaidRuntime.getRootStore(),
        Util.garageUsername(player), recipientUsername, Util.players(), deliver)
    if ok then broadcastState() end
    return ok, result
end

function Server.releaseHideoutVehicleToWorld(vehicle)
    local ok, result = ExtractionMode.GarageAuthority.releaseToWorld(
        RaidRuntime.getRootStore(), vehicle)
    if ok then broadcastState() end
    return ok, result
end

function RaidRuntime.captureSingleplayerRaidPositionsForSave()
    if not singleplayerAuthority() then return end
    local root = RaidRuntime.getRootStore()
    local activeRaidCount = 0
    local capturedCount = 0
    for key, raid in pairs(root.raids) do
        local active = raid.state == Config.STATE_RAID
            or raid.state == Config.STATE_EXTRACTING or raid.state == Config.STATE_BOARDING
        if active then
            activeRaidCount = activeRaidCount + 1
            local data = RaidRuntime.raidView(raid)
            local raidCapturedCount = 0
            for _, player in ipairs(Util.players()) do
                local username = Util.username(player)
                if RaidRuntime.rememberSingleplayerRaidPosition(
                    data, player, username, "save-event capture") then
                    capturedCount = capturedCount + 1
                    raidCapturedCount = raidCapturedCount + 1
                end
            end
            if raidCapturedCount == 0 then
                Util.log("[RaidResume] save-event found active raid but captured no field position"
                    .. " raid=" .. tostring(data.raidId) .. " team=" .. tostring(key)
                    .. " participants=" .. table.concat(sortedNames(data.participants), ","))
            end
        end
    end
    if activeRaidCount > 0 then
        Util.log("[RaidResume] save-event summary activeRaids=" .. tostring(activeRaidCount)
            .. " capturedPlayers=" .. tostring(capturedCount))
    end
end

local function initializeRaidOnLoad(data)
    -- Ground-extraction countdowns use real-time deadlines and are always safe
    -- to restart from the participant's live position after loading.
    data.groundExtractionPending = {}
    data.groundExtractionVehicles = {}
    -- A death rescue cannot safely resume after the survivor and its real-time
    -- fade deadline have been unloaded. Normal raid reconciliation takes over.
    data.deathRescuePending = {}
    for username in pairs(data.lateJoinPending or {}) do
        if data.participants[username] ~= true
            and tostring(RaidRuntime.getRootStore().playerRaidKeys[username]) == tostring(data.teamKey) then
            RaidRuntime.getRootStore().playerRaidKeys[username] = nil
        end
    end
    for _, pending in pairs(data.lateJoinVehicles or {}) do
        for username in pairs(pending.participantNames or {}) do
            if data.participants[username] ~= true
                and tostring(RaidRuntime.getRootStore().playerRaidKeys[username]) == tostring(data.teamKey) then
                RaidRuntime.getRootStore().playerRaidKeys[username] = nil
            end
        end
    end
    data.lateJoinPending = {}
    data.lateJoinVehicles = {}
    data.lateInsertionCleanup = {}
    if data.campaignHandoff ~= nil then
        -- Real-time UI deadlines do not survive a process restart. Abort safely;
        -- the carried sample remains available so the rooftop signal can be retried.
        Util.log("Final quest event canceled during authority initialization: real-time deadline cannot resume"
            .. " raid=" .. tostring(data.campaignHandoff.raidId)
            .. " owner=" .. tostring(data.campaignHandoff.ownerKey)
            .. " phase=" .. tostring(data.campaignHandoff.phase or "HOLDING"))
        data.campaignHandoff = nil
        data.extractionHelicopterStarted = nil
        Threats.stopOwnedHelicopter()
    end
    if data.state == Config.STATE_COUNTDOWN or data.state == Config.STATE_TRANSIT then
        data.state = Config.STATE_HIDEOUT
        data.ready = {}
        data.optedOut = {}
        data.participants = {}
        data.extractedPlayers = {}
        data.singleplayerRaidPositions = {}
        data.countdownEndMs = nil
        data.raidStartMs = nil
        data.transitPhase = nil
        data.transitDeadlineMs = nil
        data.raidSpawn = nil
        data.extractionSites = nil
        data.insertionCleanupUntilMs = nil
        data.selectedJoinRaidKey = nil
        data.vehicleInsertion = nil
    elseif data.state == Config.STATE_EXTRACTING or data.state == Config.STATE_BOARDING then
        data.state = Config.STATE_RAID
        data.activeExtraction = nil
        data.extractionEndMs = nil
        data.extractionHelicopterStarted = nil
        data.boardingEndMs = nil
        data.extractionRope = nil
        data.boardingPending = {}
        data.boardingExtractedCount = nil
        Threats.stopOwnedHelicopter()
    end
    if data.state == Config.STATE_HIDEOUT
        and not (tostring(data.teamKey) == "legacy" and data.selectedTownKey ~= nil) then
        local root = RaidRuntime.getRootStore()
        for username, key in pairs(root.playerRaidKeys) do
            if tostring(key) == tostring(data.teamKey)
                and data.disconnectedRaidPlayers[username] == nil then
                root.playerRaidKeys[username] = nil
            end
        end
    end
    if singleplayerAuthority() and data.state == Config.STATE_RAID then
        -- World-hour horde timing remains persisted. Only refresh the short
        -- physical-location grace period so an incompletely loaded character is
        -- not mistaken for someone who returned to the hideout.
        data.raidStartMs = Util.timerNowMs()
        singleplayerRaidResumePending = true
        singleplayerRaidResumeDeadlineMs = data.raidStartMs + 30000
        singleplayerRaidResumeLogged = false
        local saved = {}
        for username, point in pairs(data.singleplayerRaidPositions or {}) do
            saved[#saved + 1] = tostring(username) .. "@" .. RaidRuntime.resumePointLabel(point)
        end
        table.sort(saved)
        Util.log("[RaidResume] load armed raid=" .. tostring(data.raidId)
            .. " team=" .. tostring(data.teamKey)
            .. " participants=" .. table.concat(sortedNames(data.participants), ",")
            .. " savedPositions=" .. (#saved > 0 and table.concat(saved, ";") or "none")
            .. " windowMs=30000")
    end
    Threats.purgeHideoutZombies()
    Threats.suppressVanillaHelicopter()
    HideoutAuthority.refreshUtilities()
    Logistics.refreshTownChoices(data)
    Logistics.processDaily(data)
    HideoutBenefits.refreshHeating(data)
    Util.log("Authority initialized in state " .. tostring(data.state))
end

local function onInitGlobalModData()
    -- Some dedicated builds execute an early tick before loading persisted
    -- global mod data, then replace the table during OnInitGlobalModData. Rebind
    -- instead of continuing to mutate the now-detached empty table.
    local persisted = ModData.get and ModData.get(Config.DATA_KEY) or nil
    if persisted ~= nil and persisted ~= store then
        store = persisted
        RaidRuntime.initialized = false
        RaidRuntime.context = nil
        RaidRuntime.contextKey = nil
        RaidRuntime.views = {}
        RaidRuntime.progressionRecoveryPending = false
        Util.log("Rebound authority to persisted global mod data during initialization")
    end
    local root = RaidRuntime.getRootStore()
    ExtractionMode.ProgressionBackup.initialize(RaidRuntime)
    if not tableHasEntries(root.raids) then RaidRuntime.raidForKey("server") end
    ExtractionMode.GarageAuthority.initialize(root)
    GarageDoorAuthority.tick(root)
    singleplayerRaidResumePending = false
    singleplayerRaidResumeDeadlineMs = 0
    singleplayerRaidResumeLogged = false
    RaidRuntime.resumeStatus = nil
    for key, raid in pairs(root.raids) do
        local previousRaid, previousKey = RaidRuntime.context, RaidRuntime.contextKey
        RaidRuntime.context, RaidRuntime.contextKey = raid, key
        initializeRaidOnLoad(RaidRuntime.raidView(raid))
        RaidRuntime.context, RaidRuntime.contextKey = previousRaid, previousKey
    end
    for _, raid in pairs(root.raids) do
        if raid.state == Config.STATE_HIDEOUT and raid.selectedTownKey
            and not Logistics.isTownAvailable(RaidRuntime.raidView(raid), raid.selectedTownKey) then
            raid.selectedTownKey = nil
            raid.selectedTownBy = nil
            raid.selectedJoinRaidKey = nil
            raid.ready = {}
            raid.optedOut = {}
            raid.vehicleInsertion = nil
        end
    end
    RaidRuntime.pruneDormantRaidRecords(root)
end

function RaidRuntime.capturePersistentStateForSave()
    local root = RaidRuntime.getRootStore()
    ExtractionMode.GarageAuthority.captureForSave(root)
    ExtractionMode.Garage.refreshBackup(root, "world save")
    ExtractionMode.ProgressionBackup.write(RaidRuntime, "world save")
    RaidRuntime.captureSingleplayerRaidPositionsForSave()
end

Events.OnInitGlobalModData.Add(onInitGlobalModData)
Events.OnClientCommand.Add(onClientCommand)
Events.OnTick.Add(onTick)
-- Build 42 exposes OnPostSave in single-player. Prefer an earlier save hook if a
-- future build supplies one; normal tick captures already keep the persisted
-- global point current before either event fires.
if Events.OnSave then
    Events.OnSave.Add(RaidRuntime.capturePersistentStateForSave)
elseif Events.OnPostSave then
    Events.OnPostSave.Add(RaidRuntime.capturePersistentStateForSave)
end
if Events.OnTickEvenPaused then Events.OnTickEvenPaused.Add(sampleTimerClockWhilePaused) end
Events.OnZombieCreate.Add(Threats.applyAmbientZombieSpeed)
Events.OnZombieDead.Add(onQuestZombieDead)
if Events.LoadGridsquare then Events.LoadGridsquare.Add(purgeExpiredQuestItemsOnSquare) end
if Events.LoadGridsquare then Events.LoadGridsquare.Add(HideoutAuthority.onLoadGridSquare) end

ExtractionMode.Server = Server
return Server
