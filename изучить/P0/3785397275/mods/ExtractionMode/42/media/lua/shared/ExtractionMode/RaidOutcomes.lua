require "ExtractionMode/Config"
require "ExtractionMode/Infection"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Infection = ExtractionMode.Infection
local Outcomes = {}

local function boundedMode(key)
    return math.max(1, math.min(3, math.floor(tonumber(Config.value(key)) or 1)))
end

function Outcomes.healingMode()
    return boundedMode("ExtractionHealing")
end

function Outcomes.deathHandlingMode()
    return boundedMode("RaidDeathHandling")
end

local function safeBoolean(callback)
    local ok, value = pcall(callback)
    return ok and value == true
end

local function safeNumber(callback)
    local ok, value = pcall(callback)
    return ok and tonumber(value) or 0
end

local function healMajorWounds(player)
    local bodyDamage = player and player:getBodyDamage()
    local parts = bodyDamage and bodyDamage:getBodyParts()
    if bodyDamage == nil or parts == nil then return false end

    local treated = false
    for index = 0, parts:size() - 1 do
        local part = parts:get(index)
        if part then
            local bleeding = safeBoolean(function() return part:bleeding() end)
            local deepWound = safeBoolean(function() return part:deepWounded() end)
            local bullet = safeBoolean(function() return part:haveBullet() end)
            local glass = safeBoolean(function() return part:haveGlass() end)
            local burnt = safeBoolean(function() return part:isBurnt() end)
            local fracture = safeNumber(function() return part:getFractureTime() end) > 0
            if bleeding or deepWound or bullet or glass or burnt or fracture then
                treated = true
                pcall(function() part:setBleeding(false) end)
                pcall(function() part:setBleedingTime(0) end)
                pcall(function() part:SetBleedingStemmed(true) end)
                pcall(function() part:setDeepWounded(false) end)
                pcall(function() part:setDeepWoundTime(0) end)
                pcall(function() part:setStitched(false) end)
                pcall(function() part:setStitchTime(0) end)
                pcall(function() part:setHaveBullet(false, 0) end)
                pcall(function() part:setHaveGlass(false) end)
                pcall(function() part:setFractureTime(0) end)
                pcall(function() part:setSplint(false, 0) end)
                pcall(function() part:setSplintFactor(0) end)
                pcall(function() part:setSplintItem(nil) end)
                pcall(function() part:setBurnTime(0) end)
                pcall(function() part:setNeedBurnWash(false) end)
                pcall(function() part:setAdditionalPain(0) end)
                local health = safeNumber(function() return part:getHealth() end)
                if health < 80 then pcall(function() part:SetHealth(80) end) end
            end
        end
    end
    pcall(function()
        bodyDamage:setBurntToDeath(false)
        bodyDamage:calculateOverallHealth()
        local health = tonumber(bodyDamage:getOverallBodyHealth()) or 0
        if health < 75 then bodyDamage:AddGeneralHealth(75 - health) end
    end)
    return treated
end

function Outcomes.restoreFullHealth(player)
    if player == nil then return false end
    local bodyDamage = player:getBodyDamage()
    if bodyDamage == nil then return false end
    pcall(function() bodyDamage:RestoreToFullHealth() end)
    pcall(function() player:setHealth(1.0) end)
    -- A lethal zombie hit enters PlayerHitReactionState before Lua can restore
    -- health. Its later Death animation event calls Kill() without checking
    -- health again, so remove the queued attacker/reaction as part of rescue.
    pcall(function() player:setAttackedBy(nil) end)
    pcall(function() player:setHitReaction("") end)
    pcall(function() player:setDeathDragDown(false) end)
    pcall(function() player:setKilledByFall(false) end)
    pcall(function() player:setOnDeathDone(false) end)
    pcall(function() player:setOnKillDone(false) end)
    pcall(function() player:setPlayingDeathSound(false) end)
    Infection.cure(player)
    return true
end

function Outcomes.releaseDeathRescueState(player)
    if player == nil then return false end
    Outcomes.restoreFullHealth(player)

    -- Clearing the death flags does not remove PlayerHitReactionState itself.
    -- Exit it explicitly so its delayed Death animation event can no longer call
    -- Kill() after the survivor has already been restored or teleported.
    pcall(function()
        local defaultState = player:getDefaultState()
        if defaultState then player:changeState(defaultState) end
    end)
    pcall(function()
        player:setIgnoreMovement(false)
        player:setBlockMovement(false)
        player:setKnockedDown(false)
        player:setReanimateTimer(0)
        player:setFallOnFront(false)
        player:setSitOnGround(false)
        -- EndDeath has no direct transition back to idle. Route through the
        -- vanilla on-ground/get-up path now that bDead is false.
        player:setOnFloor(true)
        player:setVariable("forceGetUp", true)
    end)
    return true
end

function Outcomes.applyExtractionHealing(player, mode)
    mode = tonumber(mode) or Outcomes.healingMode()
    if mode == 2 then return healMajorWounds(player) end
    if mode >= 3 then return Outcomes.restoreFullHealth(player) end
    return false
end

ExtractionMode.RaidOutcomes = Outcomes
return Outcomes
