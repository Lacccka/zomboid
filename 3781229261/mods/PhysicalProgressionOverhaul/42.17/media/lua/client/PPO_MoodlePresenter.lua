require "PPO_MoodleDefinitions"

PPO = PPO or {}
PPO.MoodlePresenter = PPO.MoodlePresenter or {}

local Presenter = PPO.MoodlePresenter
local Definitions = PPO.MoodleDefinitions

local LOAD_STAGES = {
    Warmed = { severity = 1, name = "Warmed" },
    Worked = { severity = 2, name = "Worked" },
    Overtrained = { severity = 3, name = "Overtrained" },
}

local TONE_STAGES = {
    [1] = { severity = 1, name = "Light" },
    [2] = { severity = 2, name = "Pronounced" },
    [3] = { severity = 3, name = "Peak" },
}

local function finite(value)
    return type(value) == "number" and value == value
        and value ~= math.huge and value ~= -math.huge
end

function Presenter.hourForm(languageCode, hours)
    local value = finite(hours) and math.max(0, math.floor(hours)) or 0
    if languageCode == "RU" or languageCode == "UA" then
        local lastTwo = value % 100
        local last = value % 10
        if last == 1 and lastTwo ~= 11 then return "One" end
        if last >= 2 and last <= 4
                and not (lastTwo >= 12 and lastTwo <= 14) then
            return "Few"
        end
        return "Many"
    end
    if value == 1 then return "One" end
    return "Many"
end

local function timeFields(direction, languageCode)
    if direction.toneTimeKind == "UnderHour" then
        return "Moodles_PPO_Time_UnderHour", nil
    end
    if direction.toneTimeKind ~= "Hours"
            or not finite(direction.toneHoursRemaining)
            or direction.toneHoursRemaining < 1 then
        return nil, nil
    end
    local hours = math.floor(direction.toneHoursRemaining)
    local form = Presenter.hourForm(languageCode, hours)
    return "Moodles_PPO_Time_Hours_" .. form, hours
end

local function loadRecord(definition, direction, decayEnabled)
    local stage = LOAD_STAGES[direction[definition.sourceField]]
    if stage == nil then return nil end
    local prefix = "Moodles_PPO_" .. definition.id
    local descriptionKey = prefix .. "_" .. stage.name .. "_Description"
    if decayEnabled == false then
        descriptionKey = prefix .. "_DecayDisabled_Description"
    end
    return {
        id = definition.id,
        direction = definition.direction,
        kind = definition.kind,
        severity = stage.severity,
        alignment = definition.alignment,
        icon = definition.icon,
        titleKey = prefix .. "_" .. stage.name .. "_Title",
        descriptionKey = descriptionKey,
    }
end

local function toneRecord(definition, direction, languageCode)
    local value = direction[definition.sourceField]
    if not finite(value) then return nil end
    local stage = TONE_STAGES[value]
    if stage == nil then return nil end
    local prefix = "Moodles_PPO_" .. definition.id
    local timeKey, timeValue = timeFields(direction, languageCode)
    return {
        id = definition.id,
        direction = definition.direction,
        kind = definition.kind,
        severity = stage.severity,
        alignment = definition.alignment,
        icon = definition.icon,
        titleKey = prefix .. "_" .. stage.name .. "_Title",
        descriptionKey = prefix .. "_" .. stage.name .. "_Description",
        timeKey = timeKey,
        timeValue = timeValue,
    }
end

function Presenter.records(report, languageCode)
    local records = {}
    if type(report) ~= "table" then return records end

    for _, id in ipairs(Definitions.Order) do
        local definition = Definitions.ByID[id]
        local direction = report[definition.direction]
        if type(direction) == "table" then
            local record = nil
            if definition.kind == "Load" then
                record = loadRecord(definition, direction,
                    report.exerciseBonusDecay)
            else
                record = toneRecord(definition, direction, languageCode)
            end
            if record ~= nil then table.insert(records, record) end
        end
    end
    return records
end

