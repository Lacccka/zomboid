--***********************************************************************
-- Railroader / RR_Distributions  -- where the two training books are found in the
-- world (see shared/Railroader/RR_Training.lua and docs/TRAINING_DESIGN.md).
--
-- THE CAB IS THE PRIMARY SOURCE and it is not in this file: RR_Licence.seedCab puts
-- a locomotive's own operating journal in her cab locker when she is first found,
-- deterministically, because that is the whole first-contact loop -- walk to her,
-- climb in, open the locker, read it where you are sitting, drive away. What is
-- below is the SECOND copy, for the player who burned the first one, and the source
-- of the base manual, which was never kept in a cab.
--
-- HOW THIS HOOKS IN (verified against vanilla):
--   * ProceduralDistributions.list is a plain Lua table of {items = {name, weight,
--     name, weight, ...}} read at merge time, so appending to an existing list adds
--     our item to every container that already draws from it. The rail-yard rooms
--     draw exactly two lists -- RailYardTools and RailYardSpikes (Distributions.lua
--     :13478-13507, rooms `railroadrepair` and `railroadstorage`) -- so RailYardTools
--     is the one that means "a railroad building".
--   * The merge is driven by SuburbsDistributions.lua, which registers
--     OnPreDistributionMerge / OnDistributionMerge / OnPostDistributionMerge
--     (:208-210; the events exist in LuaEventManager.java:783-785). We inject on
--     OnPreDistributionMerge -- before anything is compiled, so our rows are simply
--     part of the table by the time it is read.
--
-- THE TWO BOOKS ARE NOT THE SAME KIND OF LOOT and their weights are set from
-- different arguments -- see the table below. The journal is insurance against
-- losing the copy in the cab; the base manual has no other source at all.
--
-- WHY NOT A ROOM DEFINITION OF OUR OWN: appending to lists vanilla already uses
-- cannot fight another mod for a room, and it survives a map rework -- the rooms are
-- the map's business, the lists are the loot's.
--***********************************************************************

print("[Railroader] RR_Distributions.lua: loading...")
require("Items/ProceduralDistributions")
require("Railroader/RR_Training")

local Dist = {}

-- item full-type (bare -- the loot tables are Base by default), weight, list name
Dist.ROWS = {
    -- THE MODEL JOURNAL IS A BACKSTOP, NOT A SOURCE, and the weights say so. The
    -- deterministic copy in the cab is the whole first-contact loop and it carries
    -- the design; these two rows exist only for the player who burned, dropped or
    -- lost that one, and for the day a second model needs a lead before it is found.
    -- Two lists, both meaning "railroad company property": the yard itself, and the
    -- office that served it. Deliberately NOT garages or generic crates -- a
    -- builder's manual for a locomotive did not circulate.
    --
    -- Calibration against the neighbours in each list, because a raw number means
    -- nothing on its own: RailYardTools runs 0.1 (a sledgehammer) to 10 (a spike
    -- puller) with the ordinary tools at 2-4, so 1.5 sits just under the common band.
    -- OfficeDesk's literature runs 1 (a business book) to 20 (paperwork), so 0.5 is
    -- below its rarest book. Both are "you may turn one up while searching a rail
    -- building", not "you will".
    { "RR_ModelManual_GP7",   1.5, "RailYardTools"           },
    { "RR_ModelManual_GP7",   0.5, "OfficeDesk"              },

    -- THE BASE MANUAL IS WITHHELD FOR NOW and these rows are inert -- see
    -- RR.Training.C.CONSIST_BOOK_IN_WORLD. Its permit has no consumer until rolling
    -- stock lands (task 3.B), and a six-and-a-half-hour read that ends with nothing
    -- in the world to couple is worse than the book not existing. They are kept
    -- written down, not deleted, because the weights are the considered ones and the
    -- day the first boxcar exists this is a flag, not an archaeology exercise.
    --
    -- Its weights are NOT a backstop like the journal's: loot is its ONLY source, so
    -- thinning these later would make consist work unreachable. Railroad property
    -- first, offices second, ordinary bookshops last and rarely -- it is a company
    -- book, not a retail one.
    { "RR_TrainHandlingBook", 4,   "RailYardTools"           },
    { "RR_TrainHandlingBook", 2,   "OfficeDesk"              },
    { "RR_TrainHandlingBook", 2,   "OfficeDrawers"           },
    { "RR_TrainHandlingBook", 2,   "FactoryLockers"          },
    { "RR_TrainHandlingBook", 1,   "BookstoreGeneralReference" },
    { "RR_TrainHandlingBook", 1,   "BookstoreBlueCollar"     },
}

--------------------------------------------------------------------------
-- inject(): append every row, once. Guarded per list so a missing vanilla list
-- (a map/loot rework renaming one) costs that row and nothing else -- the rest of
-- the books still spawn, and the log says which one went missing.
--------------------------------------------------------------------------
function Dist.inject()
    if Dist._done then return end
    Dist._done = true

    local lists = ProceduralDistributions and ProceduralDistributions.list
    if not lists then
        print("[Railroader] distributions: ProceduralDistributions.list is missing -- "
              .. "the training books have NO world source this session.")
        return
    end

    local T = RR and RR.Training
    local added, missing, withheld = 0, 0, 0
    for _, row in ipairs(Dist.ROWS) do
        local item, weight, listName = row[1], row[2], row[3]
        -- A book whose permit has nothing to unlock yet is not placed at all.
        if T and T.spawnsInWorld and not T.spawnsInWorld(item) then
            withheld = withheld + 1
        else
            local list = lists[listName]
            if list and list.items then
                table.insert(list.items, item)
                table.insert(list.items, weight)
                added = added + 1
            else
                missing = missing + 1
                print("[Railroader] distributions: no such loot list '" .. tostring(listName)
                      .. "' -- '" .. tostring(item) .. "' will not spawn there.")
            end
        end
    end
    -- Say what was held back. A silent zero reads as "covered everything" when it is
    -- exactly the opposite, and this is the line that explains an empty rail yard.
    print(string.format("[Railroader] distributions: %d loot rows added, %d skipped, %d withheld.",
                        added, missing, withheld))
    if withheld > 0 then
        print("[Railroader] distributions: the train-handling manual is NOT in the world yet -- "
              .. "its permit has nothing to unlock until rolling stock lands. "
              .. "RR.Training.C.CONSIST_BOOK_IN_WORLD = true to place it anyway.")
    end
end

Events.OnPreDistributionMerge.Add(Dist.inject)

RR = RR or {}
RR.Distributions = Dist
print("[Railroader] RR_Distributions.lua: loaded OK")

return Dist
