require "PPO_Config"
require "PPO_ReadinessEngine"

PPO = PPO or {}
PPO.RecoveryContext = PPO.RecoveryContext or {}

local RecoveryContext = PPO.RecoveryContext

local NEUTRAL = 1

RecoveryContext.SeamAvailability = RecoveryContext.SeamAvailability or {}

-- PPO_PhysicalEffects reports a dead seam here so the tone still produces a
-- bounded, honest benefit instead of silently doing nothing.
function RecoveryContext.setSeamAvailability(character, direction, available)
    if character == nil then return false end
    local record = RecoveryContext.SeamAvailability[character]
    if record == nil then
        record = {}
        RecoveryContext.SeamAvailability[character] = record
    end
    record[direction] = available ~= false
    return true
end

-- PPO_ToneEngine already requires this module, so the tone engine is reached
-- lazily through the global table instead of a second require that would close
-- the dependency cycle.
local function fallbackBonus(character, direction)
    local record = RecoveryContext.SeamAvailability[character]
    if record == nil or record[direction] ~= false then return 0 end
    if PPO.ToneEngine == nil then return 0 end
    local ok, bonus = pcall(PPO.ToneEngine.fallbackBonus, character, direction)
    if not ok then return 0 end
    return RecoveryContext.bounded(bonus)
end

-- Every provider value stays bounded to 0..1 so a future nutrition or
-- pharmacology input can never escape the designed range.
function RecoveryContext.bounded(value)
    if type(value) ~= "number" or value ~= value
            or value == math.huge or value == -math.huge then
        return NEUTRAL
    end
    if value < 0 then return 0 end
    if value > 1 then return 1 end
    return value
end

-- Stage 3 owns real short-term recovery. This provider keeps the Stage 2
-- interface so no consumer of readiness or recovery support changed shape.
function RecoveryContext.get(character, direction)
    local ok, evaluated = pcall(
        PPO.ReadinessEngine.evaluate, character, direction)
    if not ok or type(evaluated) ~= "table" then
        return {
            readiness = NEUTRAL,
            recoverySupport = NEUTRAL,
            restedReadiness = NEUTRAL,
            -- An unreadable seam must not read as a starving, sleepless
            -- character: every component falls back neutral, exactly like the
            -- composite above it.
            sleepFactor = NEUTRAL,
            sleepShare = NEUTRAL,
            fuelFactor = NEUTRAL,
            fuelShare = NEUTRAL,
            loadFactor = NEUTRAL,
            sleepRequired = true,
            toneFallback = 0,
        }
    end
    local readiness = RecoveryContext.bounded(evaluated.readiness)
    local bonus = fallbackBonus(character, direction)
    -- The same readiness with the load term at its neutral value. Sleep and
    -- food still cost, because "once recovered" promises the end of the load
    -- penalty and nothing else.
    local rested = PPO.ReadinessMath.readiness(
        1, evaluated.sleepFactor, evaluated.fuelFactor, nil)
    -- The composite `readiness` still carries the fallback so Adaptation
    -- conversion is unchanged; `toneFallback` is published separately so the
    -- multiplier can add it to `fill` instead of hiding it inside a product.
    return {
        readiness = RecoveryContext.bounded(readiness + bonus),
        recoverySupport = RecoveryContext.bounded(evaluated.recoverySupport),
        restedReadiness = RecoveryContext.bounded(rested + bonus),
        sleepFactor = RecoveryContext.bounded(evaluated.sleepFactor),
        -- Published beside `sleepFactor`, never instead of it: Readiness and
        -- RecoverySupport read the shallow curve, the multiplier share reads
        -- this one.
        sleepShare = RecoveryContext.bounded(evaluated.sleepShare),
        fuelFactor = RecoveryContext.bounded(evaluated.fuelFactor),
        -- Beside `fuelFactor` for the same reason `sleepShare` sits beside
        -- `sleepFactor`: two curves, two consumers, one stat.
        fuelShare = RecoveryContext.bounded(evaluated.fuelShare),
        loadFactor = RecoveryContext.bounded(
            PPO.ReadinessMath.loadFactor(evaluated.loadReturn)),
        sleepRequired = evaluated.sleepRequired ~= false,
        toneFallback = bonus,
    }
end
