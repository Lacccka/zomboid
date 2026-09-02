require "Compat/PPO_Compat"
require "Compat/Shared/PPO_CompatForeignXp"

-- Support for `More Traits`.
--
--   mod id       ToadTraits   (Workshop-prefixed as `1299328280/ToadTraits`)
--   Workshop     1299328280
--   folder read  42.17 on a 42.17 client, 42.20 on a 42.20 server
--
-- The collision is the Gym Goer trait. `More Traits` registers a handler on
-- `Events.AddXP`; with the trait, and with the character in `FitnessState`, it
-- answers every Strength or Fitness award by adding
-- `(GymGoerPercent * 0.01 - 1) * 0.1` of it back through
-- `getXp():AddXP(...)`, which is `0.1` at the default `GymGoerPercent = 200`.
--
-- That inner call reaches the full `AddXP` overload, and its last act --
-- `zombie.characters.IsoGameCharacter$XP.AddXP`, the
-- `LuaEventManager.triggerEventGarbage("AddXP", ...)` at the tail -- fires
-- unconditionally off a client. So one repetition produces two notifications,
-- and the second is the echo `Compat.Shared.ForeignXp` drops. Traced on the
-- queue: repetitions one to four were credited, the fifth closed the session,
-- and the rest of the set paid vanilla rates with nothing in the log.
--
-- PPO's own bonus is echoed too, and that one is already invisible to the
-- matcher: the award runs inside `BonusAwarder`'s internal window, so
-- `isInternal` drops both it and anything a foreign handler adds on top of it.
-- What the echo does do is pay the character an extra tenth of PPO's bonus.
-- That is the trait working as written, on top of an award PPO chose to make,
-- and it is not this file's business to take it back.
--
-- Two more parts of the same mod, deliberately left alone:
--
--   * `GymGoerNoExerciseFatigue` zeroes vanilla stiffness and strain for a
--     muscle group once its exercise stiffness decays. PPO prices recovery
--     from its own reservoirs, not from vanilla stiffness, so the two do not
--     read each other. `PPO.PhysicalEffects` writes stiffness for its own
--     reasons and both writers converge on zero.
--   * from 42.17 the mod requires `UnifiedCarryWeightFramework`, which scales
--     carry weight through `setMaxWeightDelta` off a snapshot of
--     `getMaxWeight()`. PPO's carry bonus writes `maxWeightBase`, a different
--     field, so there is no second writer -- but the framework's snapshot
--     includes PPO's bonus and its normalisation does not expect that base to
--     move. UCWF declares `versionMin=42.19` and does not load on 42.17 at
--     all, so this is a 42.20 question and is not answered here.

PPO.Compat.declare({
    id = "ToadTraits",
    workshopId = "1299328280",
    name = "More Traits",
    note = "Gym Goer echoes every physical XP award during a set",
})
