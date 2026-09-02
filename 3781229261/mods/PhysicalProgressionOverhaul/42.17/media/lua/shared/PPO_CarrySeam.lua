require "PPO_Num"
require "PPO_ToneMath"

-- The one place `IsoGameCharacter.maxWeightBase` is read and written. Two
-- appliers drive it -- `PPO.PhysicalEffects` wherever the server can reach the
-- character, `PPO.ClientRuntime` on a dedicated client -- and they used to
-- carry a copy each of the same protocol. A second copy of a seam whose whole
-- job is "own exactly the points you added" is how a bonus gets paid twice, so
-- the protocol lives here and the appliers only decide how to react to a
-- refusal.
--
-- `records` is the caller's own store, keyed by character. It stays with the
-- caller because its lifetime is the caller's: the server drops a record when a
-- character is released or dies, the client when its report source goes away.

PPO = PPO or {}
PPO.CarrySeam = PPO.CarrySeam or {}

local CarrySeam = PPO.CarrySeam
local Num = PPO.Num

-- Returns nil when the seam cannot be read, which every caller treats as a
-- refusal rather than as a zero.
function CarrySeam.read(character)
    local ok, value = pcall(function()
        return character:getMaxWeightBase()
    end)
    if not ok then return nil end
    local resolved = Num.finite(value, nil)
    if resolved == nil then return nil end
    return math.floor(resolved)
end

function CarrySeam.write(character, value)
    return pcall(function()
        character:setMaxWeightBase(value)
    end)
end

-- The Strength ladder vanilla multiplies the carry base by. An unreadable one
-- means the bonus is written as plain base points, which is what the seam did
-- before the ladder was accounted for.
function CarrySeam.weightMod(character)
    local ok, value = pcall(function()
        return character:getWeightMod()
    end)
    if not ok then return 1 end
    return Num.finite(value, 1)
end

-- PPO owns only the bonus it added. The original base is recorded on first
-- contact and restored on release; vanilla never persists this field, so a crash
-- cannot leave an inflated character behind.
--
-- A base that no longer matches what this seam last wrote belongs to somebody
-- else now, and the answer is to stop rather than to fight for it. Both callers
-- collapse every refusal onto one reaction, so the answer is a boolean: true
-- means the base is where it should be, including the tick where there was
-- nothing to do.
function CarrySeam.apply(records, character, carryBonus)
    local current = CarrySeam.read(character)
    if current == nil then return false end

    local record = records[character]
    if record == nil then
        record = { original = current, applied = current }
        records[character] = record
    end
    if current ~= record.applied then return false end

    -- The bonus arrives in carried kilograms and the base is not carried
    -- kilograms: vanilla multiplies it by the Strength ladder on every update.
    -- The conversion is redone every tick because levelling Strength moves the
    -- ladder under a tone that is already standing.
    local bonus = math.max(0, math.floor(Num.finite(carryBonus, 0) + 0.5))
    local target = record.original + PPO.ToneMath.carryBaseDelta(
        bonus, CarrySeam.weightMod(character))
    if target == current then return true end

    if not CarrySeam.write(character, target) then return false end
    record.applied = target
    return true
end

-- Hands the base back and forgets the character. The restore is conditional on
-- the base still being the one this seam wrote: a base another mod has since
-- moved is not ours to put back. Returns whether a record was held at all,
-- which is what both callers report as "this character was tracked".
function CarrySeam.release(records, character)
    local record = records[character]
    if record == nil then return false end
    if record.applied ~= record.original
            and CarrySeam.read(character) == record.applied then
        CarrySeam.write(character, record.original)
    end
    records[character] = nil
    return true
end

return CarrySeam
