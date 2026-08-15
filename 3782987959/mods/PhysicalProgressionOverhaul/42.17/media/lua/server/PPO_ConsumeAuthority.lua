require "PPO_SupplementDefinitions"
require "PPO_UtilityDefinitions"
require "PPO_SupplementState"
require "PPO_ExerciseState"

PPO = PPO or {}
PPO.ConsumeAuthority = PPO.ConsumeAuthority or {}

local Authority = PPO.ConsumeAuthority
local COURSE_DIRECTION = { anabolic = "Strength", cardio = "Fitness" }

local function finiteOr(value, fallback)
    if type(value) ~= "number" or value ~= value
            or value == math.huge or value == -math.huge then
        return fallback
    end
    return value
end

function Authority.new()
    return { installed = false, depth = 0, originals = {} }
end

-- Single-player and the dedicated server are authorities; the dedicated client
-- is not. An unreadable process check is treated as non-authoritative, so a
-- failure can only withhold credit. In Kahlua both stubs report false, which is
-- the single-player shape.
function Authority.authoritative()
    local clientOk, client = pcall(isClient)
    local serverOk, server = pcall(isServer)
    if not clientOk or not serverOk then return false end
    if server == true then return true end
    return client ~= true
end

-- The single routing point. A kind names exactly one reservoir, which is how
-- direction isolation is enforced: there is no code path from anabolic credit
-- to the Fitness course.
function Authority.commit(manager, character, kind, credit)
    if manager == nil or character == nil then return false end
    if not Authority.authoritative() then return false end
    local amount = finiteOr(credit, 0)
    if amount <= 0 then return false end

    local direction = COURSE_DIRECTION[kind]
    local committed = false
    pcall(function()
        if direction ~= nil then
            local course = PPO.ExerciseState.getCourse(character, direction)
            if course == nil then return end
            course.level = PPO.SupplementState.add(course.level, amount)
            if course.level > course.peak then course.peak = course.level end
            local state = PPO.ExerciseState.get(character)
            if state ~= nil then
                course.lastDoseMinute = finiteOr(state.activeMinutes, 0)
            end
            committed = true
        elseif kind == "protein" or kind == "creatine" then
            local supplements = PPO.ExerciseState.getSupplements(character)
            if supplements == nil then return end
            supplements[kind] = PPO.SupplementState.add(supplements[kind], amount)
            committed = true
        elseif kind == "stimulant" or kind == "thermogenic" then
            -- Class B: servings, not credit. A window is the only thing these
            -- kinds can reach -- no reservoir, no course, no multiplier.
            if PPO.ExerciseState.extendWindow(character, kind, amount) ~= nil then
                committed = true
            end
        end
    end)
    return committed
end

function Authority.foodDelta(pre, post)
    if type(pre) ~= "table" or type(post) ~= "table" then return 0 end
    local before = finiteOr(pre.fraction, nil)
    local after = finiteOr(post.fraction, nil)
    if before == nil or after == nil then return 0 end
    if before <= 0 then return 0 end
    if after < 0 then after = 0 end
    local delta = before - after
    if delta <= 0 then return 0 end
    if delta > before then delta = before end
    return delta
end

local function foodSnapshot(item)
    local ok, fraction = pcall(function()
        local base = item:getBaseHunger()
        local current = item:getHungChange()
        if type(base) ~= "number" or base == 0
                or type(current) ~= "number" then
            return nil
        end
        return current / base
    end)
    if not ok or fraction == nil then return nil end
    return { fraction = fraction }
end

local function identity(item, resolver)
    local ok, fullType = pcall(function() return item:getFullType() end)
    if not ok or type(fullType) ~= "string" then return nil end
    return resolver(fullType)
end

-- The transaction body every adapter shares. The adapter supplies how to read
-- the amount and which kind the identity resolves to; everything else — the
-- guard, the single delegation, the error path and the commit — lives here.
local function observe(manager, action, original, adapter)
    if manager == nil or type(original) ~= "function" then
        if type(original) == "function" then return original() end
        return nil
    end

    local character = action and action.character or nil
    local item = action and action.item or nil
    local nested = manager.depth > 0
    local kind = nil
    local pre = nil
    if not nested and character ~= nil and item ~= nil then
        kind = adapter.kind(item)
        if kind ~= nil then pre = adapter.snapshot(item) end
    end

    manager.depth = manager.depth + 1
    local ok, result = pcall(original)
    manager.depth = manager.depth - 1
    if not ok then error(result, 0) end

    if kind ~= nil and pre ~= nil then
        local post = adapter.snapshot(item)
        local credit = adapter.credit(kind, pre, post)
        if credit > 0 then Authority.commit(manager, character, kind, credit) end
    end
    return result
end

-- One identity lookup answers for both classes, so the seam stays a single
-- observation: Class A resolves to a reservoir kind, Class B to a utility kind,
-- and a food is never both.
local function foodKind(fullType)
    local kind = PPO.SupplementDefinitions.foodKind(fullType)
    if kind ~= nil then return kind end
    return PPO.UtilityDefinitions.foodKind(fullType)
end

local function foodAmount(kind, delta)
    local credit = PPO.SupplementDefinitions.credit(kind, "food", delta)
    if credit > 0 then return credit end
    return PPO.UtilityDefinitions.servings(kind, "food", delta)
end

-- The same one-lookup-for-both-classes shape food already uses: Class A
-- resolves to a reservoir or a course, Class B to a window, and a drainable is
-- never both.
local function drainableKind(fullType)
    local kind = PPO.SupplementDefinitions.drainableKind(fullType)
    if kind ~= nil then return kind end
    return PPO.UtilityDefinitions.drainableKind(fullType)
end

local function drainableAmount(kind, delta)
    local credit = PPO.SupplementDefinitions.credit(kind, "drainable", delta)
    if credit > 0 then return credit end
    return PPO.UtilityDefinitions.servings(kind, "drainable", delta)
end

function Authority.observeFood(manager, action, original)
    return observe(manager, action, original, {
        kind = function(item) return identity(item, foodKind) end,
        snapshot = foodSnapshot,
        credit = function(kind, pre, post)
            return foodAmount(kind, Authority.foodDelta(pre, post))
        end,
    })
end

local SUPPORTED_FLUIDS = {
    "ProteinShake", "HomemadeProteinShake", "CreatineDrink",
    "HomemadeCreatineDrink", "PreWorkoutDrink", "HomemadePreWorkoutDrink",
}

local function fluidSnapshot(item)
    local ok, amounts = pcall(function()
        local container = item:getFluidContainer()
        if container == nil then return nil end
        local measured = {}
        for _, name in ipairs(SUPPORTED_FLUIDS) do
            local resolved, fluid = pcall(Fluid.Get, name)
            if resolved and fluid ~= nil then
                measured[name] = container:getSpecificFluidAmount(fluid)
            end
        end
        return measured
    end)
    if not ok or amounts == nil then return nil end
    return amounts
end

function Authority.fluidDelta(pre, post, fluidName)
    if type(pre) ~= "table" or type(post) ~= "table" then return 0 end
    local before = finiteOr(pre[fluidName], nil)
    local after = finiteOr(post[fluidName], nil)
    if before == nil or after == nil or before <= 0 then return 0 end
    local delta = before - after
    if delta <= 0 then return 0 end
    if delta > before then delta = before end
    return delta
end

-- A container may hold more than one supported fluid, and they need not be the
-- same kind, so every supported component is measured and accumulated under its
-- own kind. Summing across kinds is what the previous shape did, and with two
-- classes on this seam it would credit a protein blend as a stimulant.
local function fluidAmounts(pre, post)
    local amounts = {}
    for _, name in ipairs(SUPPORTED_FLUIDS) do
        local delta = Authority.fluidDelta(pre, post, name)
        if delta > 0 then
            local kind = PPO.SupplementDefinitions.fluidKind(name)
            local amount = 0
            if kind ~= nil then
                amount = PPO.SupplementDefinitions.fluidCredit(name, delta)
            else
                kind = PPO.UtilityDefinitions.fluidKind(name)
                if kind ~= nil then
                    amount = PPO.UtilityDefinitions.fluidServings(name, delta)
                end
            end
            if kind ~= nil and amount > 0 then
                amounts[kind] = (amounts[kind] or 0) + amount
            end
        end
    end
    return amounts
end

-- Because a fluid container carries no single identity, this path resolves its
-- kind from the component that actually moved rather than from the item, which
-- is why it does not reuse the identity-first `observe` body.
function Authority.observeFluid(manager, action, original)
    local item = action and action.item or nil
    local character = action and action.character or nil
    local nested = manager ~= nil and manager.depth > 0
    local pre = nil
    if not nested and item ~= nil and character ~= nil then
        pre = fluidSnapshot(item)
    end

    if manager ~= nil then manager.depth = manager.depth + 1 end
    local ok, result = pcall(original)
    if manager ~= nil then manager.depth = manager.depth - 1 end
    if not ok then error(result, 0) end

    if pre ~= nil then
        local post = fluidSnapshot(item)
        if post ~= nil then
            for kind, amount in pairs(fluidAmounts(pre, post)) do
                Authority.commit(manager, character, kind, amount)
            end
        end
    end
    return result
end

local function drainableSnapshot(item, character)
    local ok, snapshot = pcall(function()
        local useDelta = item:getUseDelta()
        local current = item:getCurrentUsesFloat()
        if type(useDelta) ~= "number" or useDelta <= 0
                or type(current) ~= "number" then
            return nil
        end
        local present = true
        local inventory = character ~= nil and character:getInventory() or nil
        if inventory ~= nil then present = inventory:contains(item) == true end
        return {
            uses = current / useDelta,
            useDelta = useDelta,
            present = present,
        }
    end)
    if not ok or snapshot == nil then return nil end
    return snapshot
end

-- Use() decrements an integer use count and the last dose removes the item from
-- its container, so a vanished pack is confirmed consumption only when exactly
-- one dose was left. Every other unreadable post state is a refusal.
function Authority.drainableDelta(pre, post)
    if type(pre) ~= "table" or type(post) ~= "table" then return 0 end
    local useDelta = finiteOr(pre.useDelta, 0)
    local before = finiteOr(pre.uses, nil)
    if useDelta <= 0 or before == nil or before <= 0 then return 0 end

    if post.present == false then
        if before > 1.0000001 then return 0 end
        return before * useDelta
    end

    local after = finiteOr(post.uses, nil)
    if after == nil then return 0 end
    local delta = (before - after) * useDelta
    if delta <= 0 then return 0 end
    if delta > before * useDelta then delta = before * useDelta end
    return delta
end

function Authority.observeDrainable(manager, action, original)
    local item = action and action.item or nil
    local character = action and action.character or nil
    local nested = manager ~= nil and manager.depth > 0
    local kind = nil
    local pre = nil
    if not nested and item ~= nil and character ~= nil then
        kind = identity(item, drainableKind)
        if kind ~= nil then pre = drainableSnapshot(item, character) end
    end

    if manager ~= nil then manager.depth = manager.depth + 1 end
    local ok, result = pcall(original)
    if manager ~= nil then manager.depth = manager.depth - 1 end
    if not ok then error(result, 0) end

    if kind ~= nil and pre ~= nil then
        local post = drainableSnapshot(item, character)
        if post == nil then post = { present = false } end
        local credit = drainableAmount(
            kind, Authority.drainableDelta(pre, post))
        if credit > 0 then Authority.commit(manager, character, kind, credit) end
    end
    return result
end

local SEAMS = {
    { global = "ISEatFoodAction", method = "complete", observer = "observeFood" },
    { global = "ISEatFoodAction", method = "eat", observer = "observeFood" },
    { global = "ISDrinkFluidAction", method = "updateEat", observer = "observeFluid" },
    { global = "ISTakePillAction", method = "complete", observer = "observeDrainable" },
}

local function seamKey(seam) return seam.global .. "." .. seam.method end

function Authority.install(manager)
    if manager == nil then return false end
    if not Authority.authoritative() then return false end
    if manager.installed then return true end

    for _, seam in ipairs(SEAMS) do
        local target = _G[seam.global]
        if type(target) == "table" and type(target[seam.method]) == "function" then
            local original = target[seam.method]
            local observer = Authority[seam.observer]
            local wrapper = function(action, ...)
                local arguments = { ... }
                return observer(manager, action, function()
                    return original(action, unpack(arguments))
                end)
            end
            manager.originals[seamKey(seam)] = {
                original = original,
                wrapper = wrapper,
                target = target,
                method = seam.method,
            }
            target[seam.method] = wrapper
        end
    end
    manager.installed = true
    return true
end

-- A wrapper is restored only while the installed function is still the exact
-- PPO-owned one, so a later mod's replacement is never overwritten.
function Authority.release(manager)
    if manager == nil or not manager.installed then return false end

    for key, record in pairs(manager.originals) do
        if record.target[record.method] == record.wrapper then
            record.target[record.method] = record.original
        end
        manager.originals[key] = nil
    end
    manager.installed = false
    manager.depth = 0
    return true
end

-- One instance per process, so the seams can never be wrapped twice by two
-- separate managers holding different originals.
function Authority.ensureInstalled()
    if Authority.Default == nil then Authority.Default = Authority.new() end
    if Authority.Default.installed then return true end
    return Authority.install(Authority.Default)
end
