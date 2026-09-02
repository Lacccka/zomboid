require "Compat/PPO_Compat"
require "Compat/Shared/PPO_CompatExercises"
require "Compat/Shared/PPO_CompatForeignXp"

-- Support for `FWO Working Bench Press & Treadmill`.
--
--   mod id       FWOBenchPress&Treadmill
--   Workshop     2940354599   (the item also ships FWOFitnessWorkoutOverhaul,
--                              which owns the multiplier map and is not
--                              compatible with PPO -- this file supports the
--                              equipment add-on alone)
--   folder read  42.15, the highest version folder a 42.17 client loads
--
-- What the mod does, and why each part is or is not PPO's business:
--
-- 1. Registers `benchpress` and `treadmill` into the vanilla
--    `FitnessExercises.exercisesType`, and starts them through the vanilla
--    `ISFitnessAction` with those ids in `exeDataType`. So PPO's own wrappers
--    fire for them exactly as for the vanilla seven; all they lacked was a
--    price. That is the registration below, derived by the vanilla formula in
--    `Compat.Shared.Exercises` from the `stiffness` and `xpMod` the mod
--    registers -- no number here is copied from its code.
--
-- 2. Patches `ISFitnessAction` methods at file load; PPO installs its wrappers
--    from a bootstrap event, after every mod file has loaded, so PPO's wrapper
--    is always the outer one and the chain composes in either mod order.
--
-- 3. Pays awards of its own inside `exeLooped`: Sprinting on the treadmill
--    always, Strength on the treadmill while carrying more than half of
--    `getMaxWeight`, and Strength on the bench press every repetition. The
--    Sprinting award never concerned PPO -- it is not a physical direction and
--    the matcher ignores it. The two Strength awards are exactly what
--    `Compat.Shared.ForeignXp` is for: on the bench press they double the
--    proof of a direction PPO does train, and on the treadmill they add a
--    direction that session does not train at all.
--
-- 4. `SandboxVars.FWOWorkingTreadmill.StrengthXPMultiply = 0` removes both
--    Strength awards. Nothing here depends on that: the default is 1.0 and a
--    support that only works on a non-default setting is not support.

local MOD_ID = "FWOBenchPress&Treadmill"

PPO.Compat.declare({
    id = MOD_ID,
    workshopId = "2940354599",
    name = "FWO Working Bench Press & Treadmill",
    note = "adds benchpress and treadmill to the vanilla fitness table",
})

-- `stiffness`, `xpMod` and the metabolics class are what the mod writes into
-- the vanilla table; `periodMs` is the period its `serverStart` schedules.
-- Awards follow from those by `Fitness.incStats`: benchpress `arms,chest` is
-- Strength `(4 + 2) * 2.2`, treadmill `legs` is Fitness `4 * 1.5`.
PPO.Compat.Shared.Exercises.register(MOD_ID, {
    id = "benchpress",
    stiffness = "arms,chest",
    xpMod = 2.2,
    heavy = true,          -- Metabolics.FitnessHeavy
    periodMs = 2200,
})

PPO.Compat.Shared.Exercises.register(MOD_ID, {
    id = "treadmill",
    stiffness = "legs",
    xpMod = 1.5,
    heavy = false,         -- Metabolics.Fitness
    periodMs = 2000,
})
