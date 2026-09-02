require "PPO_Directions"
require "PPO_Num"
require "PPO_MultiplierMath"

PPO = PPO or {}
PPO.TrainingPanelMath = PPO.TrainingPanelMath or {}

local TrainingPanelMath = PPO.TrainingPanelMath
local DIRECTIONS = PPO.Directions.order()

-- The shared reservoirs, in draw order. Form and tone are deliberately absent:
-- both differ between directions, so printing them here would be a lie.
local SHARED_ROWS = {
    { color = "protein", key = "IGUI_PPO_ShareProtein", input = "protein" },
    { color = "creatine", key = "IGUI_PPO_ShareCreatine", input = "creatine" },
    { color = "sleep", key = "IGUI_PPO_ShareSleep", input = "sleep" },
    { color = "fuel", key = "IGUI_PPO_ShareFuel", input = "fuel" },
}

-- Both Class B windows, in draw order, always present. An absent row would be
-- invisible information: an empty bar says "the window is closed", and a
-- missing one says nothing at all. The course bar is drawn on the same
-- principle.
local WINDOW_ROWS = {
    { color = "stimulant", key = "IGUI_PPO_WindowStimulant",
        input = "stimulant" },
    { color = "thermogenic", key = "IGUI_PPO_WindowThermogenic",
        input = "thermogenic" },
}

-- Segment order matches the shares table the multiplier is built from.
local SEGMENTS = {
    { color = "form", weight = "adaptation", input = "adaptation" },
    { color = "protein", weight = "protein", input = "protein" },
    { color = "creatine", weight = "creatine", input = "creatine" },
    { color = "sleep", weight = "sleep", input = "sleep" },
    { color = "fuel", weight = "fuel", input = "fuel" },
}

local Num = PPO.Num

-- Widths come from the same call the server weighs with, so a rebalance moves
-- the picture and the multiplier together. `shares` already renormalizes when
-- sleep is not required, which is why nothing here divides by anything.
local function segmentsFor(inputs, sleepRequired)
    local weights = PPO.MultiplierMath.shares(sleepRequired)
    local segments = {}
    for _, definition in ipairs(SEGMENTS) do
        local width = Num.unit(weights[definition.weight])
        if width > 0 then
            table.insert(segments, {
                color = definition.color,
                width = width,
                filled = Num.unit(inputs[definition.input]),
            })
        end
    end
    return segments
end

-- Form is the only per-direction number worth printing. The tone fallback is
-- deliberately absent: tone is surfaced as a moodle, and a mechanic with two
-- homes is a mechanic whose two homes have to agree.
local function rowsFor(inputs)
    return {
        { color = "form", key = "IGUI_PPO_ShareForm",
            value = Num.unit(inputs.adaptation) },
    }
end

local function windowsFor(source)
    if type(source) ~= "table" then source = {} end
    local rows = {}
    for _, definition in ipairs(WINDOW_ROWS) do
        local entry = source[definition.input]
        if type(entry) ~= "table" then entry = {} end
        local minutes = math.max(0, Num.finite(entry.minutesRemaining, 0))
        local cap = math.max(0, Num.finite(entry.capMinutes, 0))
        local fraction = 0
        -- A disabled window has a zero cap, and a bar is a proportion of
        -- something: with nothing to be a proportion of, it is empty.
        if cap > 0 then fraction = Num.unit(minutes / cap) end
        table.insert(rows, {
            color = definition.color,
            key = definition.key,
            value = fraction,
            timeKind = entry.timeKind or "None",
            hoursRemaining = math.max(0, Num.finite(entry.hoursRemaining, 0)),
        })
    end
    return rows
end

local function directionFor(name, entry)
    local inputs = entry.shares
    if type(inputs) ~= "table" then inputs = {} end
    local ceiling = math.max(0, Num.finite(entry.capEffective, 0))
    return {
        name = name,
        hasMultiplier = ceiling > 1,
        multiplier = math.max(1, Num.finite(entry.multiplier, 1)),
        ceiling = ceiling,
        fill = Num.unit(entry.fill),
        segments = segmentsFor(inputs, entry.sleepRequired),
        rows = rowsFor(inputs),
        course = {
            level = Num.unit(entry.courseLevel),
            active = Num.unit(entry.course),
            withdrawal = Num.unit(entry.withdrawal),
        },
    }
end

-- Returns nil rather than an empty shell, so the panel can tell "nothing has
-- arrived yet" from "everything reads zero".
function TrainingPanelMath.model(state)
    if type(state) ~= "table" then return nil end
    for _, name in ipairs(DIRECTIONS) do
        if type(state[name]) ~= "table" then return nil end
    end

    local reference = state.Strength
    local inputs = reference.shares
    if type(inputs) ~= "table" then inputs = {} end

    local shared = {}
    for _, definition in ipairs(SHARED_ROWS) do
        if definition.input ~= "sleep" or reference.sleepRequired ~= false then
            table.insert(shared, {
                color = definition.color,
                key = definition.key,
                value = Num.unit(inputs[definition.input]),
            })
        end
    end

    local directions = {}
    for _, name in ipairs(DIRECTIONS) do
        table.insert(directions, directionFor(name, state[name]))
    end
    return {
        shared = shared,
        windows = windowsFor(state.windows),
        directions = directions,
    }
end

return TrainingPanelMath
