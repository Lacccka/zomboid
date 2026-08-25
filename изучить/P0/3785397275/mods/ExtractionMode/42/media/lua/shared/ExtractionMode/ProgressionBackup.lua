require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/Logistics"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Logistics = ExtractionMode.Logistics
local Backup = {}
local SCHEMA = 1
local FIELDS = {
    "upgrades", "questProgress", "questLegacyCompletions", "groupRegistry",
    "questObjectiveProgress", "questVisitRaidIds", "questVisitSuccessfulRaidIds",
    "questContactTrust", "generatorFuel", "generatorRunning", "generatorLastWorldHour",
    "availableTownKeys", "pendingDeliveries", "townRotationDay",
    "townChoicesChangedPending", "ammoDeliveryDay", "medicalDeliveryDay",
    "deliveryLockerPoint",
    "coopWelcomeAcknowledged",
}

local function copyValue(value, seen)
    if type(value) ~= "table" then return value end
    seen = seen or {}
    if seen[value] then return seen[value] end
    local result = {}
    seen[value] = result
    for key, nested in pairs(value) do
        result[copyValue(key, seen)] = copyValue(nested, seen)
    end
    return result
end

local function valid(record)
    if type(record) ~= "table" or tonumber(record.schema) ~= SCHEMA
        or type(record.fields) ~= "table" then return false end
    return type(record.fields.upgrades) == "table"
        and type(record.fields.questProgress) == "table"
        and type(record.fields.questContactTrust) == "table"
end

local function createRecord(runtime, reason)
    local root = runtime.getRootStore()
    local fields = {}
    for _, key in ipairs(FIELDS) do fields[key] = copyValue(root[key]) end
    return {
        schema = SCHEMA,
        configVersion = Config.VERSION,
        generation = math.max(0, math.floor(tonumber(root.progressionGeneration) or 0)),
        createdAtMs = Util.nowMs(),
        worldHours = Util.worldHours(),
        reason = tostring(reason or "progression change"),
        fields = fields,
    }
end

local function replaceContents(target, source)
    for key in pairs(target or {}) do target[key] = nil end
    for key, value in pairs(source or {}) do target[key] = copyValue(value) end
end

local function publish(runtime, record)
    -- Publishing makes this generation authoritative in the mirror and all
    -- currently loaded player copies, whether it came from a mutation or recovery.
    runtime.progressionRecoveryPending = false
    local mirror = ModData.getOrCreate(Config.BACKUP_DATA_KEY)
    replaceContents(mirror, record)
    local playerCopies = 0
    for _, player in ipairs(Util.players()) do
        local playerData = player and player:getModData()
        if playerData then
            playerData[Config.PLAYER_BACKUP_KEY] = copyValue(record)
            pcall(function() player:transmitModData() end)
            playerCopies = playerCopies + 1
        end
    end
    Util.log("Progression backup refreshed: reason=" .. tostring(record.reason)
        .. " generation=" .. tostring(record.generation or 0)
        .. " players=" .. tostring(playerCopies)
        .. " createdAtMs=" .. tostring(record.createdAtMs))
    return record
end

function Backup.write(runtime, reason)
    local root = runtime.getRootStore()
    root.progressionGeneration = math.max(0,
        math.floor(tonumber(root.progressionGeneration) or 0)) + 1
    local record = createRecord(runtime, reason)
    return publish(runtime, record)
end

function Backup.syncPlayers(runtime)
    local mirror = ModData.get and ModData.get(Config.BACKUP_DATA_KEY) or nil
    if not valid(mirror) then return 0 end
    local copied = 0
    for _, player in ipairs(Util.players()) do
        local playerData = player and player:getModData()
        local current = playerData and playerData[Config.PLAYER_BACKUP_KEY]
        local mirrorGeneration = tonumber(mirror.generation) or 0
        local currentGeneration = tonumber(current and current.generation) or 0
        if playerData and (not valid(current) or currentGeneration < mirrorGeneration
            or (currentGeneration == mirrorGeneration
                and (tonumber(current.createdAtMs) or 0) < (tonumber(mirror.createdAtMs) or 0))) then
            playerData[Config.PLAYER_BACKUP_KEY] = copyValue(mirror)
            pcall(function() player:transmitModData() end)
            copied = copied + 1
        end
    end
    return copied
end

local function newestAvailable()
    local candidates = {}
    local mirror = ModData.get and ModData.get(Config.BACKUP_DATA_KEY) or nil
    if valid(mirror) then candidates[#candidates + 1] = mirror end
    for _, player in ipairs(Util.players()) do
        local playerData = player and player:getModData()
        local record = playerData and playerData[Config.PLAYER_BACKUP_KEY]
        if valid(record) then candidates[#candidates + 1] = record end
    end
    local newest = nil
    for _, candidate in ipairs(candidates) do
        local candidateGeneration = tonumber(candidate.generation) or 0
        local newestGeneration = tonumber(newest and newest.generation) or -1
        if newest == nil or candidateGeneration > newestGeneration
            or (candidateGeneration == newestGeneration
                and (tonumber(candidate.createdAtMs) or 0)
                    > (tonumber(newest.createdAtMs) or 0)) then newest = candidate end
    end
    return newest
end

function Backup.recover(runtime)
    local record = newestAvailable()
    if record == nil then return false end
    local root = runtime.getRootStore()
    local recordGeneration = math.max(0, math.floor(tonumber(record.generation) or 0))
    local rootGeneration = math.max(0, math.floor(tonumber(root.progressionGeneration) or 0))
    if runtime.progressionRecoveryPending ~= true and recordGeneration <= rootGeneration then
        return false
    end
    for _, key in ipairs(FIELDS) do
        if record.fields[key] ~= nil then root[key] = copyValue(record.fields[key]) end
    end
    root.version = Config.VERSION
    root.progressionGeneration = recordGeneration
    Logistics.ensureState(root)
    runtime.progressionRecoveryPending = false
    local recoveredRecord = createRecord(runtime,
        "automatic recovery from " .. tostring(record.reason or "backup"))
    publish(runtime, recoveredRecord)
    print("[ExtractionMode] Recovered shared progression generation "
        .. tostring(recordGeneration) .. " from backup created at "
        .. tostring(record.createdAtMs or "unknown") .. ".")
    return true
end

function Backup.initialize(runtime)
    local recovered = Backup.recover(runtime)
    runtime.progressionRecoveryPending = false
    local root = runtime.getRootStore()
    local newest = newestAvailable()
    local rootGeneration = math.max(0, math.floor(tonumber(root.progressionGeneration) or 0))
    -- Pre-generation saves and their backups both read as generation zero. First
    -- recover a missing primary if possible, then establish a new authoritative
    -- baseline from the resulting live state instead of guessing by timestamp.
    if rootGeneration == 0 then
        Backup.write(runtime, recovered and "recovered progression generation baseline"
            or "progression generation baseline")
        return recovered
    end
    if recovered then return true end
    if valid(newest) and (tonumber(newest.generation) or 0) == rootGeneration then
        Backup.syncPlayers(runtime)
        return false
    end
    Backup.write(runtime, "authority initialization")
    return false
end

ExtractionMode.ProgressionBackup = Backup
return Backup
