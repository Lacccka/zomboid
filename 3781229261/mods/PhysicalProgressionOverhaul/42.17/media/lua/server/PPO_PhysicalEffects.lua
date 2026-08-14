require "PPO_Config"
require "PPO_ToneMath"

PPO = PPO or {}
PPO.PhysicalEffects = PPO.PhysicalEffects or {}

local PhysicalEffects = PPO.PhysicalEffects

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

local function settings()
    local ok, resolved = pcall(PPO.Config.getToneSettings)
    if not ok or type(resolved) ~= "table" then return PPO.Config.Tone end
    return resolved
end

local function readCarryBase(character)
    local ok, value = pcall(function()
        return character:getMaxWeightBase()
    end)
    if not ok then return nil end
    local resolved = finiteOr(value, nil)
    if resolved == nil then return nil end
    return math.floor(resolved)
end

local function writeCarryBase(character, value)
    return pcall(function()
        character:setMaxWeightBase(value)
    end)
end

-- Method wrappers mirror the AdaptationEngine.new pattern so the engine can
-- call an injected fake and the real instance through the same syntax.
function PhysicalEffects.new()
    local instance = {
        enduranceSamples = {},
        strainSamples = {},
        carryBases = {},
        disabled = {},
    }
    instance.applyStrengthTone = function(self, character, carryBonus)
        return PhysicalEffects.applyStrengthTone(self, character, carryBonus)
    end
    instance.applyFitnessTone = function(self, character, strength, elapsed,
            exercising, ceiling)
        return PhysicalEffects.applyFitnessTone(
            self, character, strength, elapsed, exercising, ceiling)
    end
    instance.floorStat = function(self, character, statName, value)
        return PhysicalEffects.floorStat(self, character, statName, value)
    end
    instance.rateStat = function(self, character, statName, amount)
        return PhysicalEffects.rateStat(self, character, statName, amount)
    end
    instance.capCalories = function(self, character, ceiling, descentPerMinute,
            elapsedMinutes)
        return PhysicalEffects.capCalories(self, character, ceiling,
            descentPerMinute, elapsedMinutes)
    end
    instance.applyMuscleStrain = function(self, character, target, restoreShare,
            maximumDrop)
        return PhysicalEffects.applyMuscleStrain(self, character, target,
            restoreShare, maximumDrop)
    end
    instance.cancelFutureStrain = function(self, character)
        return PhysicalEffects.cancelFutureStrain(self, character)
    end
    instance.forget = function(self, character)
        return PhysicalEffects.forget(self, character)
    end
    instance.available = function(self, character, seam)
        return PhysicalEffects.available(self, character, seam)
    end
    return instance
end

local function markDisabled(instance, character, seam)
    local record = instance.disabled[character]
    if record == nil then
        record = {}
        instance.disabled[character] = record
    end
    record[seam] = true
end

function PhysicalEffects.available(instance, character, seam)
    if instance == nil or character == nil then return false end
    local record = instance.disabled[character]
    if record == nil then return true end
    return record[seam] ~= true
end

function PhysicalEffects.forget(instance, character)
    if instance == nil or character == nil then return false end
    local had = instance.enduranceSamples[character] ~= nil

    local record = instance.carryBases[character]
    if record ~= nil then
        had = true
        if record.applied ~= record.original
                and readCarryBase(character) == record.applied then
            writeCarryBase(character, record.original)
        end
        instance.carryBases[character] = nil
    end

    -- Dropping the sample on release is what makes a returning player seed
    -- again rather than measure a drop against a baseline from a previous
    -- session.
    if instance.strainSamples[character] ~= nil then
        had = true
        instance.strainSamples[character] = nil
    end

    instance.enduranceSamples[character] = nil
    instance.disabled[character] = nil
    return had
end

-- Build 42.17 computes the mask as 1 << index in CharacterStat.ORDERED_STATS.
-- The fallbacks are the confirmed indices, used only when the packet helper is
-- unreadable; the 0x02 literal still present in vanilla Lua is a Build 41
-- leftover and addresses BOREDOM.
-- TEMPERATURE is index 18, which the Hyperthermia moodle reads straight back
-- out of the stat: Thermoregulator pulls the core halfway toward whatever is
-- written here, so this is an input to the thermal model and not a mirror of it.
local STAT_BIT_FALLBACK = {
    ENDURANCE = 0x08,
    FATIGUE = 0x10,
    STRESS = 0x20000,
    TEMPERATURE = 0x40000,
    THIRST = 0x80000,
    UNHAPPINESS = 0x100000,
}

local function statSyncFlags(statName)
    if SyncPlayerStatsPacket ~= nil
            and SyncPlayerStatsPacket.getBitMaskForStat ~= nil
            and CharacterStat ~= nil and CharacterStat[statName] ~= nil then
        local ok, flags = pcall(
            SyncPlayerStatsPacket.getBitMaskForStat, CharacterStat[statName])
        if ok then
            local resolved = finiteOr(flags, 0)
            if resolved > 0 then return resolved end
        end
    end
    return STAT_BIT_FALLBACK[statName] or 0
end

local function statValue(character, statName)
    if CharacterStat == nil or CharacterStat[statName] == nil then return nil end
    local ok, value = pcall(function()
        return character:getStats():get(CharacterStat[statName])
    end)
    if not ok then return nil end
    return finiteOr(value, nil)
end

local function writeStat(character, statName, value)
    if CharacterStat == nil or CharacterStat[statName] == nil then return false end
    local ok = pcall(function()
        character:getStats():set(CharacterStat[statName], value)
        syncPlayerStats(character, statSyncFlags(statName))
    end)
    return ok
end

local function enduranceValue(character)
    return statValue(character, "ENDURANCE")
end

local function writeEndurance(character, value)
    return writeStat(character, "ENDURANCE", value)
end

local function statSeam(statName) return "stat:" .. tostring(statName) end

-- A floor is asserted, never accumulated. Vanilla drives all of these stats
-- through Stats.add rather than a recompute, so re-asserting a floor once per
-- tick bounds the stat from below without fighting anything.
function PhysicalEffects.floorStat(instance, character, statName, value)
    if instance == nil or character == nil then return false end
    if type(statName) ~= "string" then return false end
    local seam = statSeam(statName)
    if not PhysicalEffects.available(instance, character, seam) then
        return false
    end

    local floor = finiteOr(value, 0)
    if floor <= 0 then return true end

    local current = statValue(character, statName)
    if current == nil then
        markDisabled(instance, character, seam)
        return false
    end
    if current >= floor then return true end

    if not writeStat(character, statName, floor) then
        markDisabled(instance, character, seam)
        return false
    end
    return true
end

-- A rate accumulates. Negative amounts are refused rather than clamped: this
-- path exists to charge a cost, and a cost that can refund is a bug in the
-- caller, not an input to tolerate silently.
function PhysicalEffects.rateStat(instance, character, statName, amount)
    if instance == nil or character == nil then return false end
    if type(statName) ~= "string" then return false end
    local seam = statSeam(statName)
    if not PhysicalEffects.available(instance, character, seam) then
        return false
    end

    local delta = finiteOr(amount, 0)
    if delta <= 0 then return true end

    local current = statValue(character, statName)
    if current == nil then
        markDisabled(instance, character, seam)
        return false
    end

    if not writeStat(character, statName, current + delta) then
        markDisabled(instance, character, seam)
        return false
    end
    return true
end

local function calorieValue(character)
    local ok, value = pcall(function()
        local nutrition = character:getNutrition()
        if nutrition == nil or nutrition.getCalories == nil then return nil end
        return nutrition:getCalories()
    end)
    if not ok then return nil end
    return finiteOr(value, nil)
end

-- Nutrition is the only vanilla field PPO writes that has no per-stat sync
-- mask. SyncPlayerStatsPacket carries the whole blob under syncParams = -1 and
-- nothing smaller, and the global only sends it from a server, so this is a
-- no-op in single player rather than a special case here.
local function writeCalories(character, value)
    return pcall(function()
        character:getNutrition():setCalories(value)
        syncPlayerStats(character, -1)
    end)
end

-- A descending ceiling: walk the stock down at a fixed rate and hold it there.
-- Downward only, and that is the whole mechanic rather than a safety rail.
-- Vanilla pays weight out of a negative calorie stock, so digging the stock is
-- what the thermogenic sells; filling it back would be handing the character
-- food they never ate. A character vanilla has already driven below the
-- ceiling is left exactly where vanilla put them.
--
-- Ordering matters and is the caller's job: this runs before the window is
-- spent, so the minutes charged are the minutes the window actually covered.
function PhysicalEffects.capCalories(instance, character, ceiling,
        descentPerMinute, elapsedMinutes)
    if instance == nil or character == nil then return false end
    if not PhysicalEffects.available(instance, character, "calories") then
        return false
    end

    local target = finiteOr(ceiling, 0)
    local rate = math.max(0, finiteOr(descentPerMinute, 0))
    local elapsed = math.max(0, finiteOr(elapsedMinutes, 0))
    if rate <= 0 or elapsed <= 0 then return true end

    local current = calorieValue(character)
    if current == nil then
        markDisabled(instance, character, "calories")
        return false
    end
    if current <= target then return true end

    local descended = current - rate * elapsed
    if descended < target then descended = target end
    if descended >= current then return true end

    if not writeCalories(character, descended) then
        markDisabled(instance, character, "calories")
        return false
    end
    return true
end

local function bodyPartList(character)
    local ok, parts = pcall(function()
        local damage = character:getBodyDamage()
        if damage == nil then return nil end
        return damage:getBodyParts()
    end)
    if not ok or parts == nil then return nil end
    return parts
end

local function painGated(character)
    local level = 0
    local ok, resolved = pcall(function()
        return character:getMoodles():getMoodleLevel(MoodleType.PAIN)
    end)
    if ok then level = finiteOr(resolved, 0) end
    return level >= 2
end

-- The only place in the mod that writes BodyPart.stiffness, and it only ever
-- raises it. Vanilla owns the clearing, in BodyPart.DamageUpdate at
-- 0.002 * GameTime.getMultiplier() per frame, gated on !onGoingStiffness().
--
-- The brake is written by sampling rather than by arithmetic because that
-- vanilla rate is a product of five multipliers, one of which scales with day
-- length, and is not reproducible from Lua. The drop it produces between two
-- PPO ticks is, so that is what gets braked.
--
-- Restore first, top up second: that order makes the target a stable fixed
-- point rather than an oscillation.
--
-- Signed from the start. The electrolyte slice clears strain faster than
-- vanilla by passing a negative share into this same call, never by opening a
-- second path to a persisted, network-synced field.
function PhysicalEffects.applyMuscleStrain(instance, character, target,
        restoreShare, maximumDrop)
    if instance == nil or character == nil then return false end
    if not PhysicalEffects.available(instance, character, "strain") then
        return false
    end

    local ceiling = math.max(0, finiteOr(target, 0))
    -- Signed: the debt brakes vanilla's clearing with a positive share, the
    -- pre-workout deepens it with a negative one, and the two compose into this
    -- single number before the call.
    local share = clamp(finiteOr(restoreShare, 0), -1, 1)
    local dropCeiling = math.max(0, finiteOr(maximumDrop, 0))

    -- A character with neither a debt nor a stimulant is never written to. This
    -- is the contract that keeps the mod out of a vanilla system it has no
    -- business in, so it is checked before the body is even read.
    if ceiling <= 0 and share == 0 then
        instance.strainSamples[character] = nil
        return true
    end

    local parts = bodyPartList(character)
    if parts == nil then
        markDisabled(instance, character, "strain")
        return false
    end

    local samples = instance.strainSamples[character]
    local seeding = samples == nil
    if seeding then
        samples = {}
        instance.strainSamples[character] = samples
    end

    -- Read once, outside the loop: a moodle level is a whole-body quantity, and
    -- re-reading it per part is the same answer at six times the cost. Only the
    -- top-up is gated; the brake is not, because a debt that stopped costing
    -- anything the moment the character hurt would be backwards.
    local blocked = ceiling > 0 and painGated(character)

    local ok = pcall(function()
        local count = parts:size()
        for index = 0, count - 1 do
            local part = parts:get(index)
            if part ~= nil then
                local current = finiteOr(part:getStiffness(), 0)
                local value = current

                -- Only the brake needs history. On first contact there is no
                -- baseline, and measuring a drop against one invented here is
                -- how a player who logs in mid-strain gets a phantom restore.
                if not seeding then
                    local previous = finiteOr(samples[index], current)
                    local drop = previous - current
                    if drop > dropCeiling then drop = dropCeiling end
                    if drop > 0 then value = value + share * drop end
                end

                -- A floor, never an addition: a part carrying more strain
                -- than the debt asks for keeps it and only clears slower.
                -- Unlike the brake this needs no history, so it lands on the
                -- first tick a debt is owed.
                if not blocked and value < ceiling then value = ceiling end
                if value ~= current then part:setStiffness(value) end
                -- Re-read rather than trust the write: setStiffness clamps
                -- to 0..100, and a sample above the clamp would read as a
                -- phantom drop on the next tick.
                samples[index] = finiteOr(part:getStiffness(), value)
            end
        end
    end)
    if not ok then
        markDisabled(instance, character, "strain")
        return false
    end
    return true
end

local STIFFNESS_GROUPS = { "arms", "legs", "chest", "abs" }

-- The only caller of vanilla removeStiffnessValue in the mod, and it is gated.
--
-- Fitness runs pending stiffness in three phases: a 72-tick timer written by
-- incFutureStiffness inside exerciseRepeat, then a move into
-- bodypartToIncStiffness, then a payout of one unit per 10 game minutes.
-- removeStiffnessValue clears the first two structures and not the third, so
-- calling it during the payout strands the group in bodypartToIncStiffness with
-- no amount behind it. Fitness.update then hits a bare `return`, the group never
-- leaves, onGoingStiffness() stays true forever and vanilla never clears
-- stiffness on this character again -- and that list is serialized by
-- Fitness.save, so a reload does not repair it.
--
-- A positive timer proves the group is still in the first phase:
-- incFutureStiffness refuses to write a timer for a group already in the payout
-- list, so the two states cannot overlap.
function PhysicalEffects.cancelFutureStrain(instance, character)
    if instance == nil or character == nil then return false end
    if not PhysicalEffects.available(instance, character, "futureStrain") then
        return false
    end

    local ok = pcall(function()
        local fitness = character:getFitness()
        if fitness == nil then return end
        for _, group in ipairs(STIFFNESS_GROUPS) do
            local timer = finiteOr(fitness:getCurrentExeStiffnessTimer(group), 0)
            if timer > 0 then fitness:removeStiffnessValue(group) end
        end
    end)
    if not ok then
        markDisabled(instance, character, "futureStrain")
        return false
    end
    return true
end

-- PPO owns only the bonus it added. The original base is recorded on first
-- contact and restored on expiry, release and death; vanilla never persists this
-- field, so a crash cannot leave an inflated character behind.
function PhysicalEffects.applyStrengthTone(instance, character, carryBonus)
    if instance == nil or character == nil then return false end
    if not PhysicalEffects.available(instance, character, "carry") then
        return false
    end

    local current = readCarryBase(character)
    if current == nil then
        markDisabled(instance, character, "carry")
        return false
    end

    local record = instance.carryBases[character]
    if record == nil then
        record = { original = current, applied = current }
        instance.carryBases[character] = record
    end

    if current ~= record.applied then
        markDisabled(instance, character, "carry")
        return false
    end

    local bonus = math.max(0, math.floor(finiteOr(carryBonus, 0) + 0.5))
    local target = record.original + bonus
    if target == current then return true end

    if not writeCarryBase(character, target) then
        markDisabled(instance, character, "carry")
        return false
    end
    record.applied = target
    return true
end

function PhysicalEffects.applyFitnessTone(instance, character, strength,
        elapsedMinutes, exercising, ceiling)
    if instance == nil or character == nil then return false end
    if not PhysicalEffects.available(instance, character, "endurance") then
        return false
    end

    local current = enduranceValue(character)
    if current == nil then
        markDisabled(instance, character, "endurance")
        return false
    end

    local previous = instance.enduranceSamples[character]
    instance.enduranceSamples[character] = current

    -- A refund during an accepted repetition would let tone extend the very
    -- session that created it. Nothing to do is not a broken seam, so these
    -- returns stay true and never engage the Readiness fallback.
    if exercising == true then return true end

    -- Checked by type: an unpassed Kahlua parameter carries the caller's stack
    -- slot, and a leaked number here would pin endurance at an arbitrary value.
    local cap = 1
    if type(ceiling) == "number" then cap = clamp(finiteOr(ceiling, 1), 0, 1) end

    local effect = clamp(finiteOr(strength, 0), 0, 1)
    local resolved = settings()
    local target = current

    if effect > 0 then
        if resolved.EnduranceRefundEnabled == true and previous ~= nil then
            local drop = previous - current
            local maximumDrop = math.max(0,
                finiteOr(resolved.MaxPlausibleEnduranceDrop, 0.25))
            if drop > 0 and drop <= maximumDrop then
                target = math.min(previous, target + drop * effect)
            end
        end

        local elapsed = math.max(0, finiteOr(elapsedMinutes, 0))
        if elapsed > 0 then
            target = target + math.max(0,
                finiteOr(resolved.EnduranceRecoveryPerMinute, 0.02))
                * effect * elapsed
        end
    end

    -- The debt's ceiling is applied last and to the whole result, so tone can
    -- raise endurance toward it but never through it. This is the only place
    -- PPO lowers endurance, which is why the write below is no longer gated on
    -- the target exceeding the current value.
    target = clamp(math.min(target, cap), 0, 1)
    if math.abs(target - current) < 0.0000001 then return true end
    if not writeEndurance(character, target) then
        markDisabled(instance, character, "endurance")
        return false
    end
    instance.enduranceSamples[character] = target
    return true
end
