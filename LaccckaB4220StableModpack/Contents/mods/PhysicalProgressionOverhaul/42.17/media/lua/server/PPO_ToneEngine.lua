require "PPO_Config"
require "PPO_ToneMath"
require "PPO_CourseCost"
require "PPO_BonusMath"
require "PPO_AdaptationMath"
require "PPO_ExerciseState"
require "PPO_RecoveryContext"

PPO = PPO or {}
PPO.ToneEngine = PPO.ToneEngine or {}

local ToneEngine = PPO.ToneEngine

local function settings()
    local ok, resolved = pcall(PPO.Config.getToneSettings)
    if not ok or type(resolved) ~= "table" then return PPO.Config.Tone end
    return resolved
end

local function supportedDirection(direction)
    return direction == "Strength" or direction == "Fitness"
end

-- Load is expressed once, here, as a stage gate. A session that ends outside the
-- Worked stage earns its credit as before and earns no tone at all.
local function inWorkedStage(component)
    return PPO.AdaptationMath.overtrainingStage(component.dailyStimulus)
        == "Worked"
end

function ToneEngine.onSessionFinalized(character, direction, execQuality)
    if character == nil or not supportedDirection(direction) then return nil end

    local component = PPO.ExerciseState.getComponent(character, direction)
    if component == nil then return nil end
    if not inWorkedStage(component) then return nil end

    local resolved = settings()
    local quality = PPO.ToneMath.quality(component.adaptation, execQuality)
    local minutes = PPO.ToneMath.durationMinutes(quality, resolved)

    local tone = PPO.ExerciseState.setTone(
        character, direction, quality, minutes)
    if tone == nil then return nil end
    return { quality = tone.quality, minutesRemaining = tone.minutesRemaining }
end

-- Called only from the single active-minute engine tick.
function ToneEngine.advance(character, direction, elapsedMinutes)
    if character == nil or not supportedDirection(direction) then return nil end
    return PPO.ExerciseState.advanceTone(character, direction, elapsedMinutes)
end

-- Crossing the line destroys the tone already in hand. That, not a slightly
-- smaller bonus, is what makes stopping a real decision: pushing on costs the
-- day-long buff plus most of the next day's session.
--
-- Clearing the tone releases the carry-weight bonus through the same path
-- expiry already uses, so a character can end up over-encumbered. That is the
-- consequence of the warning, not a failure, and it never damages anyone.
--
-- The fields are cleared directly: ExerciseState.setTone refreshes by the
-- maximum of the current and the new value, so setTone(0, 0) would be a no-op.
function ToneEngine.burnIfOvertrained(character, direction)
    if character == nil or not supportedDirection(direction) then return false end

    local component = PPO.ExerciseState.getComponent(character, direction)
    if component == nil then return false end
    if component.tone.quality <= 0 and component.tone.minutesRemaining <= 0 then
        return false
    end
    if PPO.AdaptationMath.overtrainingStage(component.dailyStimulus)
            ~= "Overtrained" then
        return false
    end

    component.tone.quality = 0
    component.tone.minutesRemaining = 0
    return true
end

-- The Strength tone is the carry bonus and the Fitness tone is the endurance
-- effect, so a direction's course can only ever widen its own. There is no
-- code path from the anabolic course to the Fitness tone.
local function courseScale(character, direction)
    local ok, component = pcall(
        PPO.ExerciseState.getComponent, character, direction)
    if not ok or type(component) ~= "table" then return 1 end
    local course = component.course
    if type(course) ~= "table" then return 1 end
    local scaled = 1
    local scaleOk, resolved = pcall(PPO.CourseCost.toneScale,
        course.active, course.withdrawalActive, direction)
    if scaleOk then scaled = resolved end
    return scaled
end

local function activeQuality(character, direction)
    local component = PPO.ExerciseState.getComponent(character, direction)
    if component == nil then return 0 end
    if component.tone.minutesRemaining <= 0 then return 0 end
    return component.tone.quality
end

function ToneEngine.effectStrength(character, direction)
    if character == nil or not supportedDirection(direction) then return 0 end
    return PPO.ToneMath.effectStrength(
        activeQuality(character, direction), settings(),
        courseScale(character, direction))
end

function ToneEngine.stage(character, direction)
    if character == nil or not supportedDirection(direction) then return 0 end
    return PPO.ToneMath.stage(activeQuality(character, direction))
end

function ToneEngine.carryBonus(character, direction)
    if direction ~= "Strength" then return 0 end
    if character == nil then return 0 end
    return PPO.ToneMath.carryBonus(
        activeQuality(character, direction), settings(),
        courseScale(character, direction))
end

function ToneEngine.fallbackBonus(character, direction)
    if character == nil or not supportedDirection(direction) then return 0 end
    return PPO.ToneMath.fallbackReadinessBonus(
        activeQuality(character, direction), settings())
end
