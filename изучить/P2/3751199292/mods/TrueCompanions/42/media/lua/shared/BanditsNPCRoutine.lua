--
-- Bandits NPC - Companions Overhaul - Routine controller
--
-- Decides when a companion should break off to satisfy a need, using the spots
-- assigned in the Orders tab. MaybeStart() is called at the top of the movement
-- programs; if a need is critical and a relevant spot exists it saves the
-- current order and switches the NPC to the NPCRoutine program (which walks
-- there and performs the action). Mirrors the production NPCWork flow.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Routine = {}

-- need -> which assigned spot(s) can satisfy it (priority order)
BanditsNPC.Routine.NeedSpots = {
    { need = "fatigue", spots = {"bed"} },
    { need = "hunger",  spots = {"food"} },
    { need = "boredom", spots = {"tv", "chair", "reading"} },
    { need = "hygiene", spots = {"bathroom"} },
}

-- sleep window (hours of day, wraps midnight). Matches the schedule's Night block.
BanditsNPC.Routine.SleepStart = 22
BanditsNPC.Routine.SleepEnd = 7

function BanditsNPC.Routine.InSleepWindow(hour)
    return hour >= BanditsNPC.Routine.SleepStart or hour < BanditsNPC.Routine.SleepEnd
end

-- Should fatigue send her to bed NOW? Returns trigger, mode:
--   "collapse" - exhausted (>= 90): any hour, any order -- she's dropping.
--   "night"    - idling at base (Stay/Relax) inside the sleep window and at least
--                moderately tired: a base companion keeps a human day-night rhythm
--                instead of waiting to hit critical.
-- A guard/follow order is DUTY and suppresses the window trigger (only collapse
-- breaks it) -- which is also exactly what makes an evening guard SHIFT take
-- priority over "go to bed on time".
function BanditsNPC.Routine.SleepCheck(brain, level)
    level = level or 0
    if level >= 90 then return true, "collapse" end
    local pname = brain.program and brain.program.name
    if pname ~= "NPCStay" and pname ~= "NPCRelax" then return false end
    if BanditsNPC.Routine.InSleepWindow(getGameTime():getHour()) and level >= 30 then
        return true, "night"
    end
    return false
end

-- If a need is critical and has an assigned spot, switch to NPCRoutine.
-- Returns true if it took over.
function BanditsNPC.Routine.MaybeStart(bandit)
    local brain = BanditBrain.Get(bandit)
    if not brain or not brain.master then return false end
    if brain.program and brain.program.name == "NPCRoutine" then return false end
    if brain.routineTask then return false end

    local n = brain.needs
    if not n then return false end
    local crit = (BanditsNPC.Needs and BanditsNPC.Needs.GetCritical and BanditsNPC.Needs.GetCritical())
        or (BanditsNPC.Needs and BanditsNPC.Needs.Critical) or 70
    -- relaxing at base = free time: tend to needs PROACTIVELY instead of waiting
    -- until they're critical (base life looks alive; fatigue has its own rules)
    -- A WORKER WHO HAS KNOCKED OFF FOR THE DAY IS RELAXING. Without this she stands at
    -- base doing nothing from the moment she stops working until bedtime, because the
    -- work program keeps running and the proactive threshold below only looked at
    -- NPCRelax.
    if brain.program and brain.program.name == "NPCRelax" then
        crit = math.min(crit, 40)
    end

    for _, e in ipairs(BanditsNPC.Routine.NeedSpots) do
        -- fatigue has its own time-of-day rules (SleepCheck); the rest fire at critical
        local go, mode
        if e.need == "fatigue" then
            go, mode = BanditsNPC.Routine.SleepCheck(brain, n[e.need])
        else
            go = (n[e.need] or 0) >= crit
        end
        if go then
            -- A SPOT IS NOW EITHER ASSIGNED OR FOUND. BanditsNPC.Base.Resolve checks
            -- brain.spots first -- a spot the player placed by hand always wins -- and
            -- otherwise draws from the furniture scanned inside the base area. This is
            -- what makes base life work with no setup at all; before it, a companion
            -- with no hand-placed bed simply never slept, and the routine system was
            -- effectively invisible to anyone who had not read the Orders tab.
            local spotKey, spot
            for _, sk in ipairs(e.spots) do
                local s = BanditsNPC.Base and BanditsNPC.Base.Resolve(brain, sk, bandit)
                if s then spotKey, spot = sk, s; break end
            end

            -- SHE CAN ALWAYS EAT WHAT SHE IS CARRYING. If there is no food container
            -- she can reach -- no base, out on the road, the store not set -- but there
            -- is food in her bag, that is enough. The pseudo-spot "self" means "do it
            -- where you stand"; NPCRoutine handles it without travelling anywhere.
            if not spotKey and e.need == "hunger"
               and BanditsNPC.Routine.FoodInInventory(bandit) then
                spotKey = "self"
                spot = { x = math.floor(bandit:getX()), y = math.floor(bandit:getY()),
                         z = math.floor(bandit:getZ()) }
            end
            if spotKey then
                brain.prevProgram = brain.program
                -- The RESOLVED coordinates are carried on the task, not looked up
                -- again each tick: the auto lists get rescanned, and a target that
                -- can change mid-walk is a companion who turns round halfway.
                brain.routineTask = { need = e.need, spotKey = spotKey, spot = spot, mode = mode }
                brain.program = { name = "NPCRoutine", stage = "Prepare" }
                BanditBrain.Update(bandit, brain)
                if Bandit and Bandit.ForceSyncPart then
                    Bandit.ForceSyncPart(bandit, {
                        id = brain.id, program = brain.program,
                        prevProgram = brain.prevProgram, routineTask = brain.routineTask,
                    })
                end
                return true
            end
        end
    end
    return false
end

-- Debug/manual: send the companion to satisfy a need at a specific assigned spot
-- right now (used by the Orders debug buttons). Bumps the need so there's
-- something to do when she gets there. Returns true if a spot was assigned.
function BanditsNPC.Routine.Force(bandit, need, spotKey)
    local brain = BanditBrain.Get(bandit)
    if not brain then return false end
    local spot = BanditsNPC.Base and BanditsNPC.Base.Resolve(brain, spotKey, bandit)
    if not spot then return false end

    local n = BanditsNPC.Needs and BanditsNPC.Needs.Get(brain)
    if n and (n[need] or 0) < 55 then n[need] = 55 end   -- so she doesn't bail instantly

    if brain.program and brain.program.name ~= "NPCRoutine" then
        brain.prevProgram = brain.program
    end
    brain.routineTask = { need = need, spotKey = spotKey, spot = spot }
    brain.program = { name = "NPCRoutine", stage = "Prepare" }
    BanditBrain.Update(bandit, brain)
    if Bandit and Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(bandit, {
            id = brain.id, program = brain.program,
            prevProgram = brain.prevProgram, routineTask = brain.routineTask, needs = n,
        })
    end
    return true
end

-- The best thing in her own bag to eat, or nil.
--
-- Prefers the LEAST nutritious edible thing she has, so she works through the
-- scavenged apples before the tinned goods -- a companion who eats your best food
-- first is a companion you stop feeding.
function BanditsNPC.Routine.FoodInInventory(zombie)
    local best, bestVal
    pcall(function()
        local items = zombie:getInventory():getItems()
        for i = 0, items:size() - 1 do
            local it = items:get(i)
            if it and it.IsFood and it:IsFood() then
                local hunger = 0
                pcall(function() hunger = it:getHungerChange() or 0 end)
                -- getHungerChange is NEGATIVE for food; least filling = closest to 0
                local val = -hunger
                if bestVal == nil or val < bestVal then best, bestVal = it, val end
            end
        end
    end)
    return best
end

-- Eat what she is carrying, wherever she is. Returns the item NAME if she ate.
--
-- This is what makes a companion self-sufficient away from base: the whole routine
-- system used to require an assigned food CONTAINER on a known square, so a companion
-- out on the road simply starved next to a bag full of food.
function BanditsNPC.Routine.EatFromInventory(zombie)
    local item = BanditsNPC.Routine.FoodInInventory(zombie)
    if not item then return nil end
    local name
    pcall(function() name = item:getName() end)
    pcall(function()
        local c = item:getContainer()
        if c then c:Remove(item) end
    end)
    return name or "?"
end

-- Consume one food item from a container on the given square. Returns true if eaten.
function BanditsNPC.Routine.EatFromContainer(square)
    if not square then return false end
    local objs = square:getObjects()
    for i = 0, objs:size() - 1 do
        local o = objs:get(i)
        local cont = o and o:getContainer()
        if cont then
            local list = ArrayList.new()
            cont:getAllEvalRecurse(function(it) return it.IsFood and it:IsFood() end, list)
            if list:size() > 0 then
                local item = list:get(0)
                local c = item:getContainer()
                if c then c:Remove(item) end
                return true
            end
        end
    end
    return false
end
