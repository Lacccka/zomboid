--
-- Bandits NPC - Companions Overhaul - Daily schedule (shifts)
--
-- A companion can follow a 3-shift daily schedule instead of one fixed order.
-- Each shift (Morning / Evening / Night) has an activity (follow / stay / guard)
-- and its own tile location(s). Apply() is called from the movement programs;
-- when the time block changes it switches the NPC's program/positions to match.
-- The needs routine (NPCRoutine) still takes priority over the schedule.
--

BanditsNPC = BanditsNPC or {}
BanditsNPC.Schedule = {}

-- THE activities a shift can be set to, in the order every picker offers them. One list,
-- because there are now three places that need it -- the schedule editor window, the Orders
-- tab's dropdown and its controller cycle -- and three copies would drift.
--
-- The keys are INDIRECT at the call sites (T("UI_BN_Type_" .. t)), which the key scanner
-- cannot see, so each UI_BN_Type_* key is written into the locale files by hand. Adding a
-- type here without adding its key means the raw key leaks into the UI. The English text
-- lives here only as the fallback; the lookup happens at call time so a language pack that
-- initialises after this file still reaches it.
BanditsNPC.Schedule.TYPES = {
    { "follow", "Follow" }, { "stay", "Stay" }, { "guard", "Guard" },
    { "relax", "Relax" },   { "sleep", "Sleep" },
}

-- The next activity in the ring, for the pickers that step rather than choose.
function BanditsNPC.Schedule.NextType(t)
    local L = BanditsNPC.Schedule.TYPES
    for i, v in ipairs(L) do
        if v[1] == t then return L[(i % #L) + 1][1] end
    end
    return L[1][1]
end

-- block index for an hour-of-day
function BanditsNPC.Schedule.BlockForHour(h)
    if h >= 6 and h < 14 then return 1
    elseif h >= 14 and h < 22 then return 2
    else return 3 end
end

-- Resolved at CALL time (not baked at load) so the translation is picked up even
-- if the language pack initializes after this file parses.
function BanditsNPC.Schedule.BlockLabel(i)
    local keys = {
        [1] = { "UI_BN_Block_Morning", "Morning (6am-2pm)" },
        [2] = { "UI_BN_Block_Evening", "Evening (2pm-10pm)" },
        [3] = { "UI_BN_Block_Night",   "Night (10pm-6am)" },
    }
    local k = keys[i]
    if not k then return nil end
    return BanditsNPC.T(k[1], k[2])
end

function BanditsNPC.Schedule.Ensure(brain)
    if not brain.schedule then
        brain.schedule = { enabled = false, blocks = { {type="follow"}, {type="follow"}, {type="follow"} } }
    end
    -- A schedule table that came back from a sync or an older save WITHOUT its blocks used
    -- to make every reader index nil -- SetType did `sch.blocks[idx] = ...` and the Orders
    -- tab now reads all three every frame. Ensure means ensure.
    if not brain.schedule.blocks then brain.schedule.blocks = {} end
    for i = 1, 3 do
        if not brain.schedule.blocks[i] then brain.schedule.blocks[i] = { type = "follow" } end
    end
    return brain.schedule
end

local function sync(zombie, brain)
    BanditBrain.Update(zombie, brain)
    if Bandit and Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(zombie, { id = brain.id, schedule = brain.schedule, scheduleBlock = brain.scheduleBlock })
    end
end

-- programs that are transient interruptions, not manual orders -- when snapshotting the
-- pre-schedule order we look through them to what she was REALLY told to do
local TRANSIENT = { NPCRoutine = true, NPCWork = true, NPCAnimPlay = true, NPCDowned = true }

function BanditsNPC.Schedule.SetEnabled(zombie, on)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie); if not brain then return end
    local sch = BanditsNPC.Schedule.Ensure(brain)
    on = on and true or false

    if on and not sch.enabled then
        -- SNAPSHOT the manual order: the schedule overwrites program/stayPos/guardA/B, so
        -- without this, turning the schedule OFF left her stuck in the last shift forever
        local p = brain.program
        if p and TRANSIENT[p.name] and brain.prevProgram then p = brain.prevProgram end
        brain.preSchedule = {
            program = { name = (p and p.name) or "NPCCompanion" },
            stayPos = brain.stayPos,
            guardA = brain.guardA, guardB = brain.guardB,
            relaxPos = brain.relaxPos,
        }
    elseif (not on) and sch.enabled then
        -- RESTORE the manual order the schedule replaced
        local pre = brain.preSchedule
        if pre then
            brain.stayPos = pre.stayPos
            brain.guardA, brain.guardB, brain.guardTarget = pre.guardA, pre.guardB, "a"
            brain.relaxPos = pre.relaxPos or brain.relaxPos
            brain.program = { name = (pre.program and pre.program.name) or "NPCCompanion", stage = "Prepare" }
        else
            brain.program = { name = "NPCCompanion", stage = "Prepare" }
        end
        brain.preSchedule = nil
    end

    sch.enabled = on
    brain.scheduleBlock = nil   -- force re-apply
    BanditBrain.Update(zombie, brain)
    if Bandit and Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(zombie, {
            id = brain.id, schedule = brain.schedule, scheduleBlock = brain.scheduleBlock,
            program = brain.program, preSchedule = brain.preSchedule,
            stayPos = brain.stayPos, guardA = brain.guardA, guardB = brain.guardB,
        })
    end
end

function BanditsNPC.Schedule.SetType(zombie, idx, t)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie); if not brain then return end
    local sch = BanditsNPC.Schedule.Ensure(brain)
    sch.blocks[idx] = sch.blocks[idx] or {}
    sch.blocks[idx].type = t
    brain.scheduleBlock = nil
    sync(zombie, brain)
end

function BanditsNPC.Schedule.SetLocation(zombie, idx, picks)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie); if not brain then return end
    local sch = BanditsNPC.Schedule.Ensure(brain)
    local b = sch.blocks[idx] or {}
    sch.blocks[idx] = b
    if b.type == "guard" then
        if picks[1] then b.a = { x=math.floor(picks[1].x), y=math.floor(picks[1].y), z=picks[1].z or 0 } end
        if picks[2] then b.b = { x=math.floor(picks[2].x), y=math.floor(picks[2].y), z=picks[2].z or 0 } end
    else
        if picks[1] then b.pos = { x=math.floor(picks[1].x), y=math.floor(picks[1].y), z=picks[1].z or 0 } end
    end
    brain.scheduleBlock = nil
    sync(zombie, brain)
end

-- Disable the schedule (used when a manual order is given). Does NOT restore the
-- pre-schedule snapshot -- the caller is about to set a NEW order, which supersedes it.
function BanditsNPC.Schedule.Disable(zombie)
    local brain = BanditBrain.Get(zombie); if not brain or not brain.schedule then return end
    brain.schedule.enabled = false
    brain.scheduleBlock = nil
    brain.preSchedule = nil
    sync(zombie, brain)
end

-- Force a specific block's activity right now, ignoring the clock (debug/preview,
-- so you can verify a shift's behaviour without waiting for that time of day).
-- Set the block's activity on the brain. "sleep" hands control to NPCRoutine's bed
-- step in SHIFT mode (sleeps the whole block; NPCRoutine itself ends it on block
-- change, because Apply skips while the program is NPCRoutine). Needs a Bed spot --
-- without one she just stays put for the block.
local function applyBlockType(bandit, brain, block, idx)
    local t = block and block.type or "follow"
    if t == "stay" and block.pos then
        brain.stayPos = block.pos
        brain.program = { name = "NPCStay", stage = "Prepare" }
    elseif t == "guard" and block.a and block.b then
        brain.guardA, brain.guardB, brain.guardTarget = block.a, block.b, "a"
        brain.program = { name = "NPCGuard", stage = "Prepare" }
    elseif t == "relax" then
        -- A relax block no longer NEEDS a picked tile. With a base area drawn,
        -- NPCRelax settles her somewhere inside it (see BanditsNPC.Base.Anchor), so
        -- "afternoon: relax" works straight out of the box. block.pos still wins.
        brain.relaxPos = block.pos
        brain.relaxGoal = nil
        brain.program = { name = "NPCRelax", stage = "Prepare" }
    elseif t == "sleep" then
        -- A BED IS EITHER ASSIGNED OR FOUND. This tested brain.spots.bed alone, so a
        -- companion whose beacon area had a bed in it -- which the auto-scan files and
        -- which NPCRoutine happily sleeps in, because the routine asks Base.Resolve -- was
        -- told "No bed assigned" here and parked on Stay for the whole night block.
        local hasBed = (brain.spots and brain.spots.bed)
                       or (BanditsNPC.Base and BanditsNPC.Base.Has(brain, "bed", bandit))
        if hasBed then
            brain.prevProgram = { name = "NPCStay" }   -- wake interim; Apply takes over next block
            brain.routineTask = { need = "fatigue", spotKey = "bed", mode = "shift", block = idx }
            brain.program = { name = "NPCRoutine", stage = "Prepare" }
        else
            pcall(function() bandit:addLineChatElement(BanditsNPC.T("UI_BN_Bark_NoBed", "No bed assigned..."), 0.9, 0.75, 0.5) end)
            brain.program = { name = "NPCStay", stage = "Prepare" }
        end
    else
        brain.program = { name = "NPCCompanion", stage = "Prepare" }
    end
end

function BanditsNPC.Schedule.ApplyBlock(zombie, idx)
    if not BanditsNPC.MayCommand(zombie) then return end
    local brain = BanditBrain.Get(zombie); if not brain then return end
    local sch = BanditsNPC.Schedule.Ensure(brain)
    brain.scheduleBlock = idx
    applyBlockType(zombie, brain, sch.blocks and sch.blocks[idx], idx)
    BanditBrain.Update(zombie, brain)
    if Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(zombie, {
            id = brain.id, program = brain.program, scheduleBlock = idx,
            stayPos = brain.stayPos, guardA = brain.guardA, guardB = brain.guardB,
            routineTask = brain.routineTask, prevProgram = brain.prevProgram,
            relaxPos = brain.relaxPos,
        })
    end
end

-- Apply the current shift's activity if the block changed. Returns true if it switched.
function BanditsNPC.Schedule.Apply(bandit)
    local brain = BanditBrain.Get(bandit)
    if not brain or not brain.master then return false end
    local sch = brain.schedule
    if not sch or not sch.enabled then return false end
    if brain.program and brain.program.name == "NPCRoutine" then return false end

    local idx = BanditsNPC.Schedule.BlockForHour(getGameTime():getHour())
    if brain.scheduleBlock == idx then return false end
    brain.scheduleBlock = idx

    applyBlockType(bandit, brain, sch.blocks and sch.blocks[idx], idx)

    BanditBrain.Update(bandit, brain)
    if Bandit.ForceSyncPart then
        Bandit.ForceSyncPart(bandit, {
            id = brain.id, program = brain.program, scheduleBlock = idx,
            stayPos = brain.stayPos, guardA = brain.guardA, guardB = brain.guardB,
            routineTask = brain.routineTask, prevProgram = brain.prevProgram,
            relaxPos = brain.relaxPos,
        })
    end
    return true
end
