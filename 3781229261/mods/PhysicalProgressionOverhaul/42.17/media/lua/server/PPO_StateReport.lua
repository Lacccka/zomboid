require "PPO_Config"
require "PPO_ToneMath"
require "PPO_BonusMath"
require "PPO_WindowState"

PPO = PPO or {}
PPO.StateReport = PPO.StateReport or {}

local StateReport = PPO.StateReport
local DIRECTIONS = { "Strength", "Fitness" }
-- Kind to the settings resolver that owns its cap. A window is a
-- whole-character value, so it sits beside the two directions rather than
-- inside either of them.
local WINDOWS = {
    { key = "stimulant", settings = "getStimulantSettings" },
    { key = "thermogenic", settings = "getThermogenicSettings" },
}

local function finiteOr(value, fallback)
    if type(value) ~= "number" or value ~= value
            or value == math.huge or value == -math.huge then
        return fallback
    end
    return value
end

local function clamp(value, minimum, maximum)
    if value < minimum then return minimum end
    if value > maximum then return maximum end
    return value
end

local function ownerOnlineID(character)
    if character == nil or type(character.getOnlineID) ~= "function" then
        return nil
    end
    local ok, value = pcall(character.getOnlineID, character)
    if not ok then return nil end
    value = finiteOr(value, -1)
    if value < 0 then return nil end
    return value
end

local function ownerUsername(character)
    if character == nil or type(character.getUsername) ~= "function" then
        return nil
    end
    local ok, value = pcall(character.getUsername, character)
    if not ok or type(value) ~= "string" or value == "" then return nil end
    return value
end

local function settings()
    local ok, resolved = pcall(PPO.Config.getToneSettings)
    if not ok or type(resolved) ~= "table" then return PPO.Config.Tone end
    return resolved
end

function StateReport.new()
    local instance = { lastStages = {} }
    instance.build = function(self, tickResult, character)
        return StateReport.build(tickResult, character)
    end
    instance.publish = function(self, character, report)
        return StateReport.publish(self, character, report)
    end
    instance.forget = function(self, character)
        return StateReport.forget(self, character)
    end
    return instance
end

function StateReport.toneTimeDisplay(minutes)
    local remaining = math.max(0, finiteOr(minutes, 0))
    if remaining <= 0 then return "None", 0 end
    if remaining < 60 then return "UnderHour", 0 end
    return "Hours", math.max(1, math.floor(remaining / 60 + 0.5))
end

-- Built from the values the single engine tick already computed, so the report
-- can never disagree with the state it describes.
function StateReport.build(tickResult, character)
    local resolved = settings()
    local report = {}
    report.ownerOnlineID = ownerOnlineID(character)
    report.ownerUsername = ownerUsername(character)
    report.exerciseBonusDecay = PPO.BonusMath.isExerciseBonusDecayEnabled()

    local windowSource = nil
    if type(tickResult) == "table" then windowSource = tickResult.windows end
    if type(windowSource) ~= "table" then windowSource = {} end
    report.windows = {}
    for _, window in ipairs(WINDOWS) do
        local minutes = math.max(0, finiteOr(windowSource[window.key], 0))
        local resolved = PPO.Config[window.settings]()
        local timeKind, hours = StateReport.toneTimeDisplay(minutes)
        report.windows[window.key] = {
            minutesRemaining = minutes,
            capMinutes = PPO.WindowState.cap(
                resolved.WindowHours, resolved.ContainerServings),
            timeKind = timeKind,
            hoursRemaining = hours,
        }
    end

    for _, direction in ipairs(DIRECTIONS) do
        local entry = nil
        if type(tickResult) == "table" then entry = tickResult[direction] end
        if type(entry) ~= "table" then entry = {} end

        local toneStage = math.floor(math.max(0,
            math.min(3, finiteOr(entry.toneStage, 0))))
        local carryBonus = 0
        local enduranceStrength = 0
        if direction == "Strength" then
            carryBonus = math.floor(math.max(0, finiteOr(entry.carryBonus, 0)))
        else
            local stages = resolved.EnduranceStages
            local percent = 0
            if toneStage > 0 and type(stages) == "table"
                    and type(stages[toneStage]) == "number" then
                percent = math.max(0, stages[toneStage])
            end
            enduranceStrength = percent / 100
        end

        local toneMinutesRemaining = math.max(0,
            finiteOr(entry.toneMinutes, 0))
        local toneTimeKind, toneHoursRemaining =
            StateReport.toneTimeDisplay(toneMinutesRemaining)

        -- The breakdown is published so the panel can explain the multiplier
        -- rather than recompute it. Every field is bounded here, because the
        -- client renders what it receives and derives nothing.
        local source = entry.shares
        if type(source) ~= "table" then source = {} end
        local shares = {
            adaptation = clamp(finiteOr(source.adaptation, 0), 0, 1),
            protein = clamp(finiteOr(source.protein, 0), 0, 1),
            creatine = clamp(finiteOr(source.creatine, 0), 0, 1),
            sleep = clamp(finiteOr(source.sleep, 0), 0, 1),
            fuel = clamp(finiteOr(source.fuel, 0), 0, 1),
            tone = clamp(finiteOr(source.tone, 0), 0, 1),
        }
        report[direction] = {
            capEffective = math.max(0, finiteOr(entry.capEffective, 0)),
            fill = clamp(finiteOr(entry.fill, 0), 0, 1),
            shares = shares,
            course = clamp(finiteOr(entry.course, 0), 0, 1),
            courseLevel = clamp(finiteOr(entry.courseLevel, 0), 0, 1),
            withdrawal = clamp(finiteOr(entry.withdrawal, 0), 0, 1),
            sleepRequired = entry.sleepRequired ~= false,
            loadStage = entry.loadStage or "Fresh",
            toneStage = toneStage,
            toneMinutesRemaining = toneMinutesRemaining,
            toneTimeKind = toneTimeKind,
            toneHoursRemaining = toneHoursRemaining,
            carryBonus = carryBonus,
            enduranceStrength = enduranceStrength,
            -- The skill panel renders these and derives nothing.
            level = math.floor(clamp(finiteOr(entry.level, 0), 0, 10)),
            multiplier = math.max(1, finiteOr(entry.displayMultiplier, 1)),
            restedMultiplier = math.max(1,
                finiteOr(entry.restedMultiplier, 1)),
            levelCap = math.max(0, finiteOr(entry.levelCap, 0)),
            adaptation = clamp(finiteOr(entry.adaptation, 0), 0, 1),
        }
    end
    return report
end

-- Bucketing is what keeps a continuously moving multiplier from becoming a
-- packet per tick more often than the tick itself fires.
local function bucket(value, size)
    return math.floor(finiteOr(value, 0) / size + 0.5)
end

local function directionKey(entry)
    return tostring(entry.loadStage)
        .. "/" .. tostring(entry.toneStage)
        .. "/" .. tostring(entry.level)
        .. "/" .. tostring(bucket(entry.multiplier, 0.05))
        .. "/" .. tostring(bucket(entry.restedMultiplier, 0.05))
        .. "/" .. tostring(bucket(entry.adaptation, 0.01))
        .. "/" .. tostring(entry.toneTimeKind)
        .. "/" .. tostring(entry.toneHoursRemaining)
        -- The breakdown is rendered, so a share that moved without moving the
        -- multiplier's own bucket must still reach the panel.
        .. "/" .. tostring(bucket(entry.shares.protein, 0.05))
        .. "/" .. tostring(bucket(entry.shares.creatine, 0.05))
        .. "/" .. tostring(bucket(entry.shares.sleep, 0.05))
        .. "/" .. tostring(bucket(entry.shares.fuel, 0.05))
        .. "/" .. tostring(bucket(entry.shares.tone, 0.05))
        .. "/" .. tostring(bucket(entry.course, 0.05))
        .. "/" .. tostring(bucket(entry.courseLevel, 0.05))
        .. "/" .. tostring(bucket(entry.withdrawal, 0.05))
        .. "/" .. tostring(bucket(entry.capEffective, 0.05))
        .. "/" .. tostring(entry.sleepRequired)
end

-- Bucketed to whole hours: a window loses a minute every tick, and keying on
-- the raw value would put a packet on the wire once a game minute for nothing
-- the panel can draw.
function StateReport.stageKeyFor(report)
    local key = directionKey(report.Strength) .. "|" .. directionKey(report.Fitness)
    for _, window in ipairs(WINDOWS) do
        local entry = report.windows and report.windows[window.key] or nil
        local minutes = entry and entry.minutesRemaining or 0
        key = key .. "|" .. tostring(bucket(minutes, 60))
    end
    return key
end

local function stageKey(report)
    return StateReport.stageKeyFor(report)
end

local function isStandaloneContext()
    if type(isClient) ~= "function" or type(isServer) ~= "function" then
        return false
    end
    local clientOk, client = pcall(isClient)
    local serverOk, server = pcall(isServer)
    return clientOk and serverOk and client == false and server == false
end

local function deliver(character, report)
    if isStandaloneContext() then
        if type(triggerEvent) ~= "function" then return false end
        return pcall(triggerEvent, "OnServerCommand", "PPO", "state", report)
    end
    return pcall(sendServerCommand, character, "PPO", "state", report)
end

-- Pushed only when a stage changes, so a stable character costs no traffic.
function StateReport.publish(instance, character, report)
    if instance == nil or character == nil then return false end
    if type(report) ~= "table" or report.Strength == nil then return false end

    local key = stageKey(report)
    if instance.lastStages[character] == key then return false end

    local ok = deliver(character, report)
    if not ok then return false end

    instance.lastStages[character] = key
    return true
end

function StateReport.forget(instance, character)
    if instance == nil or character == nil then return false end
    local had = instance.lastStages[character] ~= nil
    instance.lastStages[character] = nil
    return had
end
