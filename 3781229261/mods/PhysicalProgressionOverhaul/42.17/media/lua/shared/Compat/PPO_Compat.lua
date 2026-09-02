require "PPO_Num"

-- The registry every mod-support file writes into, and the only thing the mod
-- proper reads. Core PPO must never name a third-party mod: it asks this table
-- a question about an id or a situation, and the answer comes from whichever
-- file under `shared/Compat/<Mod>` claimed it.
--
-- One folder per supported mod, `Compat/Shared` for the parts more than one of
-- them needs. A support file may only:
--   * `Compat.declare` itself, once, with its Workshop id and what it does;
--   * register exercises through `Compat.Shared.Exercises`;
--   * document the exact mechanism it compensates for.
-- It may not reach into PPO state. Everything core needs from this tree is a
-- seam core itself calls.
--
-- Nothing here inspects whether the mod is actually installed. It cannot: a mod
-- may load after PPO, and `getActivatedMods` is a different answer on a client
-- than on the server. A declaration whose mod is absent costs an unused table
-- entry, because the ids it claims never reach an action.

PPO = PPO or {}
PPO.Compat = PPO.Compat or {}

local Compat = PPO.Compat

Compat.Shared = Compat.Shared or {}
Compat.mods = Compat.mods or {}
Compat.exercises = Compat.exercises or {}

-- `record.id` is the mod's `mod.info` id, `record.workshopId` the Workshop
-- item it ships in -- one item can carry several mods, so the pair is what
-- identifies a support target.
function Compat.declare(record)
    if type(record) ~= "table" or type(record.id) ~= "string" then
        return false
    end
    if Compat.mods[record.id] ~= nil then return false end
    Compat.mods[record.id] = {
        id = record.id,
        workshopId = record.workshopId,
        name = record.name or record.id,
        note = record.note,
    }
    return true
end

function Compat.declared(modId)
    return Compat.mods[modId] ~= nil
end

-- Claims an exercise id for a mod. Refuses a second claim on the same id: two
-- support files pricing one exercise is the same defect as two writers on one
-- field, and the first claim wins so the refusal is visible rather than silent
-- overwriting.
function Compat.registerExercise(exerciseId, source)
    if type(exerciseId) ~= "string" or type(source) ~= "table" then
        return false
    end
    if Compat.exercises[exerciseId] ~= nil then
        pcall(print, "[PPO] compat: exercise " .. exerciseId
            .. " is already claimed by " .. tostring(
                Compat.exercises[exerciseId].modId))
        return false
    end
    Compat.exercises[exerciseId] = source
    return true
end

-- The seam `PPO.ExerciseDefinitions.get` calls for an id it does not know.
function Compat.exercise(exerciseId)
    return Compat.exercises[exerciseId]
end
