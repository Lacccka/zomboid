require "ExtractionMode/ModCompatibility"

ExtractionMode = ExtractionMode or {}

local Compatibility = ExtractionMode.ModCompatibility
local Integration = {}
local pendingByPlayer = {}
local RELOCATION_TIMEOUT_MS = 20000
local SOURCE_CAPTURE_RADIUS = 512

local TRANSIENT_PROGRAMS = {
    NPCRoutine = true,
    NPCWork = true,
    NPCAnimPlay = true,
    NPCDowned = true,
}

local function safeCall(callback, fallback)
    local ok, value = pcall(callback)
    if ok then return value end
    return fallback
end

local function playerNumber(player)
    return safeCall(function() return math.max(0, tonumber(player:getPlayerNum()) or 0) end, 0)
end

local function nowMs()
    return (ExtractionMode.Util and ExtractionMode.Util.nowMs and ExtractionMode.Util.nowMs())
        or ((getTimestampMs and getTimestampMs()) or 0)
end

function Integration.isAvailable()
    return Compatibility ~= nil and Compatibility.isTrueCompanionsActive()
        and type(BanditsNPC) == "table"
        and type(BanditsNPC.IsOwnedBy) == "function"
        and type(BanditsNPC.Nav) == "table"
        and type(BanditsNPC.Nav.IsSafeSquare) == "function"
        and type(BanditsNPC.Nav.Teleport) == "function"
        and type(BanditBrain) == "table"
        and type(BanditBrain.Get) == "function"
end

local function livingCompanion(character)
    if character == nil then return false end
    return safeCall(function()
        return not character:isDead() and character:getVariableBoolean("Bandit") == true
    end, false)
end

-- A routine, animation, or knockdown is an interruption rather than an order.
-- Look through it so a follower still travels even if the transition happens
-- while they are eating, emoting, or downed. An enabled work schedule remains
-- a base assignment and therefore does not count as Follow.
local function effectiveOrder(brain)
    if type(brain) ~= "table" then return nil, false end
    local scheduled = type(brain.schedule) == "table" and brain.schedule.enabled == true
    local program = brain.program
    local name = type(program) == "table" and program.name or nil
    if TRANSIENT_PROGRAMS[name] and type(brain.prevProgram) == "table" then
        name = brain.prevProgram.name
    end
    return name, scheduled
end

local function addCaptured(result, seen, character, player, raidOnly)
    if character == nil or seen[character] or not livingCompanion(character) then return end
    local brain = safeCall(function() return BanditBrain.Get(character) end, nil)
    if type(brain) ~= "table" or brain.recruited ~= true then return end
    if not safeCall(function() return BanditsNPC.IsOwnedBy(brain, player) == true end, false) then return end
    local orderName, scheduled = effectiveOrder(brain)
    local following = not scheduled and orderName == "NPCCompanion"
    if raidOnly and not following then return end
    -- In split-screen, the global zombie list can simultaneously contain the
    -- raid and the hideout. On a return trip, capture companions from the
    -- transitioning player's area without pulling assigned hideout residents
    -- away from the other local player. The radius still covers a large base
    -- and companions spread across the nearby part of a raid map.
    if not raidOnly then
        local nearby = safeCall(function()
            local dx = character:getX() - player:getX()
            local dy = character:getY() - player:getY()
            return dx * dx + dy * dy <= SOURCE_CAPTURE_RADIUS * SOURCE_CAPTURE_RADIUS
        end, false)
        if not nearby then return end
    end
    seen[character] = true
    result[#result + 1] = {
        character = character,
        uid = brain.npcUid,
        following = following,
        orderName = orderName,
        scheduled = scheduled,
    }
end

-- Capture live objects before the player changes cells. Deliberately do not
-- rewrite True Companions' roster entries for streamed-out companions: that
-- mod's persistence comments document that doing so can restore a duplicate of
-- an NPC whose original body still exists in an unloaded chunk.
function Integration.captureForTransition(player, raidOnly)
    local result, seen = {}, {}
    if player == nil or not Integration.isAvailable() then return result end

    local oldPending = pendingByPlayer[playerNumber(player)]
    pendingByPlayer[playerNumber(player)] = nil
    if oldPending ~= nil then
        for _, entry in ipairs(oldPending.entries or {}) do
            if entry.moved ~= true then
                addCaptured(result, seen, entry.character, player, raidOnly == true)
            end
        end
    end

    local cell = getCell and getCell() or nil
    local zombies = cell and cell.getZombieList and cell:getZombieList() or nil
    if zombies == nil then return result end
    for index = 0, zombies:size() - 1 do
        addCaptured(result, seen, zombies:get(index), player, raidOnly == true)
    end
    return result
end

-- True Companions deliberately represents a companion in a moving vehicle as
-- a roster entry rather than a real passenger (real zombie vehicle occupants
-- can crash PZ's damage code). Use that mod's own boarding path once vehicle
-- insertion is committed. Any companion it refuses remains live and is handed
-- back for a normal safe relocation beside the vehicle at the raid boundary.
function Integration.prepareVehicleTransition(player)
    local fallback = {}
    local captured = Integration.captureForTransition(player, true)
    for _, entry in ipairs(captured) do
        local brain = safeCall(function() return BanditBrain.Get(entry.character) end, nil)
        local boarded = false
        if type(brain) == "table" and BanditsNPC.Persistence
            and type(BanditsNPC.Persistence.BeginRide) == "function" then
            boarded = safeCall(function()
                return BanditsNPC.Persistence.BeginRide(entry.character, brain) == true
            end, false)
        end
        if not boarded then fallback[#fallback + 1] = entry end
    end
    return fallback
end

local function usableSquare(square, requireOutdoor, building, claimed)
    if square == nil then return false end
    local key = tostring(square:getX()) .. ":" .. tostring(square:getY())
        .. ":" .. tostring(square:getZ())
    if claimed[key] then return false end
    local usable = safeCall(function()
        if not BanditsNPC.Nav.IsSafeSquare(square) then return false end
        if requireOutdoor and (not square:isOutside() or square:has(IsoFlagType.water)) then
            return false
        end
        if not requireOutdoor then
            if building ~= nil then
                if square:getBuilding() ~= building then return false end
            elseif square:getBuilding() == nil or square:getRoom() == nil then
                return false
            end
        end
        return true
    end, false)
    if usable then claimed[key] = true end
    return usable
end

local function destinationSquare(x, y, z, requireOutdoor, building, claimed, ordinal)
    local cell = getCell and getCell() or nil
    if cell == nil then return nil end
    local baseX = math.floor(tonumber(x) or 0)
    local baseY = math.floor(tonumber(y) or 0)
    local baseZ = math.floor(tonumber(z) or 0)
    local start = math.max(0, (tonumber(ordinal) or 1) - 1)
    for radius = 1, 8 do
        local candidates = {}
        for offsetX = -radius, radius do
            for offsetY = -radius, radius do
                if math.abs(offsetX) == radius or math.abs(offsetY) == radius then
                    candidates[#candidates + 1] = { offsetX, offsetY }
                end
            end
        end
        for shift = 0, #candidates - 1 do
            local offset = candidates[((start + shift) % #candidates) + 1]
            local square = cell:getGridSquare(baseX + offset[1], baseY + offset[2], baseZ)
            if usableSquare(square, requireOutdoor, building, claimed) then return square end
        end
    end
    return nil
end

local function prepareBrainForDestination(character, brain, square, entry)
    if Bandit and Bandit.ClearTasks then pcall(function() Bandit.ClearTasks(character) end) end
    brain.tasks = {}
    brain.routineTask = nil
    brain.scheduleBlock = nil

    local following = entry.following == true
    local assignedProgram = "NPCStay"
    if entry.scheduled ~= true and entry.orderName == "NPCGuard" then
        assignedProgram = "NPCGuard"
    elseif entry.scheduled ~= true and entry.orderName == "NPCRelax" then
        assignedProgram = "NPCRelax"
    end
    local position = { x = square:getX(), y = square:getY(), z = square:getZ() }
    if not following then
        brain.stayPos = nil
        brain.guardA = nil
        brain.guardB = nil
        brain.guardTarget = nil
        brain.relaxPos = nil
        brain.workstation = nil
        if assignedProgram == "NPCGuard" then
            -- Preserve the intent to guard. A zero-length patrol is deliberate:
            -- it keeps the companion at the safe arrival tile until the player
            -- chooses a second patrol point in the newly adopted hideout.
            brain.guardA = position
            brain.guardB = { x = position.x, y = position.y, z = position.z }
            brain.guardTarget = "a"
        elseif assignedProgram == "NPCRelax" then
            brain.relaxPos = position
        else
            brain.stayPos = position
        end
        if type(brain.schedule) == "table" then brain.schedule.enabled = false end
        brain.preSchedule = nil
    end

    local currentName = type(brain.program) == "table" and brain.program.name or nil
    local downed = brain.downed == true or currentName == "NPCDowned"
    if downed then
        brain.program = { name = "NPCDowned", stage = "Prepare" }
        brain.prevProgram = {
            name = following and "NPCCompanion" or assignedProgram,
            stage = "Prepare",
        }
    elseif following then
        brain.program = { name = "NPCCompanion", stage = "Prepare" }
        brain.prevProgram = nil
    else
        -- A stationary assignment cannot keep pointing at the old world cell.
        -- Re-home its order beside the owner; assignments already inside the
        -- hideout are never captured for a raid and remain completely untouched.
        brain.program = { name = assignedProgram, stage = "Prepare" }
        brain.prevProgram = nil
    end

    pcall(function() BanditBrain.Update(character, brain) end)
    if Bandit and Bandit.ForceSyncPart then
        pcall(function()
            Bandit.ForceSyncPart(character, {
                id = brain.id,
                program = brain.program,
                prevProgram = brain.prevProgram,
                tasks = brain.tasks,
                stayPos = brain.stayPos,
                guardA = brain.guardA,
                guardB = brain.guardB,
                relaxPos = brain.relaxPos,
                workstation = brain.workstation,
                schedule = brain.schedule,
                preSchedule = brain.preSchedule,
                scheduleBlock = brain.scheduleBlock,
            })
        end)
    end
end

local function moveEntry(entry, player, pending, claimed, ordinal)
    local character = entry.character
    if not livingCompanion(character) then return true end
    local brain = safeCall(function() return BanditBrain.Get(character) end, nil)
    if type(brain) ~= "table" or not safeCall(function()
        return BanditsNPC.IsOwnedBy(brain, player) == true
    end, false) then return true end

    local baseSquare = safeCall(function() return player:getSquare() end, nil)
    local building = nil
    if not pending.requireOutdoor and baseSquare ~= nil then
        building = safeCall(function() return baseSquare:getBuilding() end, nil)
    end
    local square = destinationSquare(pending.x, pending.y, pending.z,
        pending.requireOutdoor, building, claimed, ordinal)
    if square == nil then return false end
    local moved = safeCall(function()
        return BanditsNPC.Nav.Teleport(character, square, "Extraction Mode transition") == true
    end, false)
    if not moved then return false end

    prepareBrainForDestination(character, brain, square, entry)
    if BanditsNPC.Persistence and BanditsNPC.Persistence.Record then
        pcall(function() BanditsNPC.Persistence.Record(character, brain) end)
    end
    return true
end

function Integration.update(player)
    if player == nil then return 0 end
    local number = playerNumber(player)
    local pending = pendingByPlayer[number]
    if pending == nil then return 0 end
    if not Integration.isAvailable() or safeCall(function() return player:isDead() end, true) then
        pendingByPlayer[number] = nil
        return 0
    end
    if nowMs() > pending.expiresAt then
        local remaining = 0
        for _, entry in ipairs(pending.entries or {}) do
            if entry.moved ~= true then remaining = remaining + 1 end
        end
        if remaining > 0 then
            print("[ExtractionMode] True Companions relocation timed out for "
                .. tostring(remaining) .. " companion(s).")
        end
        pendingByPlayer[number] = nil
        return 0
    end

    local claimed, moved, remaining = {}, 0, 0
    for index, entry in ipairs(pending.entries or {}) do
        if entry.moved ~= true then
            if moveEntry(entry, player, pending, claimed, index) then
                entry.moved = true
                moved = moved + 1
            else
                remaining = remaining + 1
            end
        end
    end
    if remaining == 0 then pendingByPlayer[number] = nil end
    return moved
end

function Integration.relocateCaptured(captured, player, x, y, z, requireOutdoor)
    if player == nil or not Integration.isAvailable() or #(captured or {}) == 0 then return 0 end
    pendingByPlayer[playerNumber(player)] = {
        entries = captured,
        x = tonumber(x) or player:getX(),
        y = tonumber(y) or player:getY(),
        z = tonumber(z) or player:getZ(),
        requireOutdoor = requireOutdoor == true,
        expiresAt = nowMs() + RELOCATION_TIMEOUT_MS,
    }
    return Integration.update(player)
end

ExtractionMode.TrueCompanionsIntegration = Integration
return Integration
