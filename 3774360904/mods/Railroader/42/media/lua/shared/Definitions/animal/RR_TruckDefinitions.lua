--***********************************************************************
-- Railroader / RR_TruckDefinitions  (Task 3.C: independent truck articulation)
--
-- Defines the animal type "rr_truck": one TRUCK (bogie) of the locomotive, rendered as
-- its own entity so it can yaw independently of the carbody on a curve.
--
-- WHY AN ENTITY AND NOT A BONE. The loco is one skinned mesh, and B42 exposes no way for
-- Lua to read or write a single bone's transform on a live character/animal -- every bone
-- method lives on AnimationPlayer, which is not in Kahlua's whitelist and carries zero
-- @LuaMethod (verified 2026-07-21, docs/ENGINE_NOTES.md "Lua cannot drive individual
-- bones"). So a sub-part that has to move gets its own entity or it does not move at all.
--
-- ONE MODEL, TWO INSTANCES. RR_LocoTruck is the truck mesh cut out of the finished loco by
-- tools/split_trucks.py, with its origin ON THE CENTRE PIN -- so placing a truck is a
-- position and a heading, nothing else, and the same mesh serves both ends because the two
-- trucks of a GP7 are the same part (the splitter refuses to run unless it can prove it).
--
-- Cloned from cow like rr_loco and rr_collider, for the same reasons: the loader reads the
-- per-animal sub-tables, and a missing stage/breed/gene is an NPE inside AnimalData.initStage
-- or AnimalBreed init, not a graceful failure. See docs/MODEL_PIPELINE.md.
--
-- WHAT MAKES IT SCENERY RATHER THAN LIVESTOCK:
--   * collidable = false. The loco's wall is RR_Collider's ejectIntruders sweep over the
--     whole hull rectangle; a truck with its own push circle would be a second, rounder,
--     disagreeing wall inside the first one.
--   * no shadow. IsoAnimal.renderShadow has no per-entity opt-out, so a cloned cow drags a
--     shadow oval per entity -- two of them pooled under a body that already casts its own
--     (this is exactly what the collider segments used to do, and it is why they zero the
--     same three fields).
--   * silent. idleSoundVolume/Radius 0 and every breed's `sounds` cleared, so the vocals
--     system finds nothing to play. The loco's sound is RR_Sound's free emitter and the
--     trucks must not add to it.
--   * size pinned to 0.7, the loco's. The animal render scale is 1.5 * getAnimalSize()
--     (IsoAnimal.java:2642) and it is the ONLY size knob on this path, so a truck at any
--     other size would be a differently-scaled part of the same locomotive.
--
-- Spawned and pinned by client/Railroader/RR_Trucks.lua, which owns the lifecycle.
--***********************************************************************

AnimalDefinitions = AnimalDefinitions or {}

local MODEL = "RR_LocoTruck"        -- model script (42/media/scripts/railroader_models.txt)
local SKIN  = "RR_LocoTest"         -- texture: THE SAME ATLAS as the carbody, not a copy

local function buildTruckType()
    local A = AnimalDefinitions
    if not (A.animals and A.animals["cow"]) then return false end   -- cow not loaded yet
    if A.animals["rr_truck"] then return true end                   -- already built
    if not (A.genome and A.genome["cow"]) then return false end

    local truck = copyTable(A.animals["cow"])

    -- Our truck mesh on every body-model variant (the engine picks between them by state --
    -- skinned/headless/fleece -- and a truck has one appearance in all of them).
    truck.bodyModel           = MODEL
    truck.bodyModelSkel       = MODEL
    truck.bodyModelHeadless   = MODEL
    truck.bodyModelSkelNoHead = MODEL
    truck.modelscript         = MODEL
    truck.bodyModelFleece     = nil
    truck.textureSkinned      = nil     -- force the breed.texture path (our atlas)
    truck.animset             = "cow"   -- reuse the cow rig/animset (nothing animates)

    -- Single ADULT grow-stage keyed by the TYPE NAME -> initStage finds it and currentStage
    -- is not null. No calf, no growth, no reproduction.
    truck.stages   = { ["rr_truck"] = { ageToGrow = 12 * 30 } }
    truck.babyType = nil
    truck.minAge   = 0

    truck.genes = A.genome["cow"].genes

    -- Same render size as the loco: both models take 1.5 * size, so this is what keeps the
    -- truck the same scale as the body it belongs to. KEEP IN SYNC with rr_loco.
    truck.minSize    = 0.7
    truck.maxSize    = 0.7
    truck.animalSize = 0.5
    truck.female     = true             -- initTexture -> breed.texture (female) list

    -- Scenery, not an obstacle: the hull's keep-out is RR_Collider's rectangle sweep.
    truck.collidable    = false
    -- Not zero: width = collisionSize * size is recomputed every update whatever `collidable`
    -- says (IsoAnimal.java:431), and a zero-width moving object is a shape no engine code was
    -- written for. A tenth of a tile is a value, not a wall -- nothing pushes off it while
    -- collidable is false.
    truck.collisionSize = 0.1

    -- No shadow (see the header). Zero w/fm/bm -> a degenerate oval, i.e. none drawn.
    truck.shadoww  = 0
    truck.shadowfm = 0
    truck.shadowbm = 0

    -- Silence at the source.
    truck.idleSoundVolume = 0
    truck.idleSoundRadius = 0

    truck.breeds = {
        ["rr"] = {
            name        = "rr",
            texture     = SKIN,
            textureMale = SKIN,
        },
    }

    truck.group = "rr_truck"            -- debug-spawn grouping only

    A.animals["rr_truck"] = truck

    -- Parity holders (mirror vanilla / rr_loco / rr_collider).
    A.stages = A.stages or {}
    A.stages["rr_truck"] = { stages = truck.stages }
    A.genome = A.genome or {}
    A.genome["rr_truck"] = A.genome["cow"]
    A.breeds = A.breeds or {}
    A.breeds["rr_truck"] = { breeds = truck.breeds }

    print("[Railroader] rr_truck animal type defined (model=" .. MODEL .. ", skin=" .. SKIN .. ")")
    return true
end

if not buildTruckType() then
    print("[Railroader] RR_TruckDefinitions: cow not ready at load; deferring to OnGameBoot")
    if Events and Events.OnGameBoot then
        Events.OnGameBoot.Add(buildTruckType)
    end
end
