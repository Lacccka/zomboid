require "PPO_ClientRuntime"

PPO = PPO or {}
PPO.SkillTooltip = PPO.SkillTooltip or { Installed = false }

local SkillTooltip = PPO.SkillTooltip

local function finiteOr(value, fallback)
    if type(value) ~= "number" or value ~= value
            or value == math.huge or value == -math.huge then
        return fallback
    end
    return value
end

local function directionFor(perkType)
    if Perks == nil then return nil end
    if perkType == Perks.Strength then return "Strength" end
    if perkType == Perks.Fitness then return "Fitness" end
    return nil
end

local function multiplierText(value)
    return string.format("%.2f", math.max(1, finiteOr(value, 1)))
end

-- The exact string vanilla appended, rebuilt from the same inputs, so the line
-- is removed by identity rather than by guessing at localized text.
function SkillTooltip.vanillaMultiplierLine(character, perkType)
    local ok, multiplier = pcall(function()
        return character:getXp():getMultiplier(perkType)
    end)
    if not ok or finiteOr(multiplier, 0) <= 0 then return nil end

    local textOk, text = pcall(getText, "IGUI_skills_Multiplier",
        round(multiplier, 2))
    if not textOk or type(text) ~= "string" then return nil end
    return " <LINE> " .. text
end

function SkillTooltip.strip(message, line)
    if type(message) ~= "string" or type(line) ~= "string" then
        return message
    end
    local from, to = string.find(message, line, 1, true)
    if from == nil then return message end
    return string.sub(message, 1, from - 1) .. string.sub(message, to + 1)
end

-- Level 10 owns no multiplier, so it gets nothing: the breakdown that used to
-- justify a lone conditioning line here now lives in the training tab.
function SkillTooltip.block(entry)
    local cap = finiteOr(entry.levelCap, 0)
    if cap <= 1 then return "" end

    -- The two numbers are equal exactly when load is zero, and the comparison
    -- is on the rendered text, so the line disappears only when the player
    -- could not have told the two apart anyway.
    local now = multiplierText(entry.multiplier)
    local rested = multiplierText(entry.restedMultiplier)
    local block = " <LINE> <LINE> " .. getText("IGUI_PPO_MultiplierTitle")
        .. " <LINE> <INDENT:16> " .. getText("IGUI_PPO_MultiplierNow", now)
    if rested ~= now then
        block = block .. " <LINE> "
            .. getText("IGUI_PPO_MultiplierRested", rested)
    end

    -- The ceiling line shows the course-adjusted value, because that is the
    -- number the multiplier is actually measured against; `levelCap` stays the
    -- untouched band and only decides whether the block is rendered at all.
    local ceiling = finiteOr(entry.capEffective, 0)
    if ceiling <= 0 then ceiling = cap end

    return block
        .. " <LINE> " .. getText("IGUI_PPO_MultiplierCeiling",
            tostring(math.floor(finiteOr(entry.level, 0))),
            multiplierText(ceiling))
        .. " <LINE> <INDENT:0> "
end

function SkillTooltip.decorate(bar, lvlSelected)
    if bar == nil or type(bar.message) ~= "string" then return false end
    if bar.perk == nil or bar.char == nil then return false end

    local perkType = bar.perk:getType()
    local direction = directionFor(perkType)
    if direction == nil then return false end

    -- Vanilla annotates only the square in progress. At level 10 there is no
    -- such square, and conditioning must still be reachable.
    local level = finiteOr(bar.level, 0)
    if level < 10 and lvlSelected ~= bar.level then return false end

    local state = PPO.ClientRuntime.state(bar.char)
    if type(state) ~= "table" then return false end
    local entry = state[direction]
    if type(entry) ~= "table" then return false end

    bar.message = SkillTooltip.strip(bar.message,
        SkillTooltip.vanillaMultiplierLine(bar.char, perkType))
        .. SkillTooltip.block(entry)
    return true
end

-- The wrapper never replaces the vanilla tooltip; it edits the result. A
-- failure inside the decoration leaves exactly what vanilla built.
function SkillTooltip.install()
    if SkillTooltip.Installed then return false end
    if ISSkillProgressBar == nil
            or ISSkillProgressBar.updateTooltip == nil then
        return false
    end

    local original = ISSkillProgressBar.updateTooltip
    SkillTooltip.original = original
    ISSkillProgressBar.updateTooltip = function(self, lvlSelected)
        original(self, lvlSelected)
        pcall(SkillTooltip.decorate, self, lvlSelected)
    end
    SkillTooltip.Installed = true
    return true
end

SkillTooltip.install()
