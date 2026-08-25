require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/ModCompatibility"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Compatibility = ExtractionMode.ModCompatibility
local Infection = {}

function Infection.level(player)
    if player == nil or player:getStats() == nil then return 0 end
    local ok, value = pcall(function()
        return player:getStats():get(CharacterStat.ZOMBIE_INFECTION)
    end)
    return ok and (tonumber(value) or 0) or 0
end

function Infection.playerInsideHideout(player)
    if player == nil then return false end
    local hideout = Config.hideout()
    local radius = tonumber(hideout.radius) or 14
    local effective = Compatibility.effectivePlayerPosition(player)
    if effective ~= nil then
        local bounds = Config.hideoutCellBounds()
        return effective.x >= bounds.minX and effective.x < bounds.maxXExclusive
            and effective.y >= bounds.minY and effective.y < bounds.maxYExclusive
    end
    local insideFallbackRadius = Util.distanceSquaredXY(
        { x = player:getX(), y = player:getY() }, hideout) <= radius * radius

    -- Prefer the mapped building identity so every floor, basement, and room of
    -- a large hideout agrees with the UI and server permission checks. During
    -- initial chunk loading one of these definitions may not exist yet, so the
    -- configured radius remains a conservative fallback around the spawn.
    local cell = getCell and getCell()
    if cell == nil then return insideFallbackRadius end
    local anchor = cell:getGridSquare(math.floor(hideout.x), math.floor(hideout.y),
        math.floor(tonumber(hideout.z) or 0))
    local hideoutBuilding = anchor and anchor:getBuilding()
    local playerBuilding = nil
    pcall(function() playerBuilding = player:getBuilding() end)
    if hideoutBuilding ~= nil and playerBuilding ~= nil then
        return playerBuilding == hideoutBuilding
    end
    return insideFallbackRadius
end

function Infection.playerInsideHideoutCell(player)
    if player == nil then return false end
    local bounds = Config.hideoutCellBounds()
    local point = Compatibility.playerPosition(player)
    local x = point and tonumber(point.x) or nil
    local y = point and tonumber(point.y) or nil
    return x ~= nil and y ~= nil
        and x >= bounds.minX and x < bounds.maxXExclusive
        and y >= bounds.minY and y < bounds.maxYExclusive
end

function Infection.setLevel(player, level)
    if player == nil or player:getStats() == nil then return false end
    local bounded = math.max(0, math.min(100, tonumber(level) or 0))
    local ok = pcall(function()
        player:getStats():set(CharacterStat.ZOMBIE_INFECTION, bounded)
    end)
    return ok
end

local function holdMortalityClockAtLevel(player, level)
    local bodyDamage = player and player:getBodyDamage()
    if bodyDamage == nil or bodyDamage:isInfected() ~= true then return false end
    local duration = nil
    local currentHour = nil
    pcall(function() duration = bodyDamage:getInfectionMortalityDuration() end)
    pcall(function() currentHour = player:getHoursSurvived() end)
    if currentHour == nil and GameTime and GameTime.getInstance then
        pcall(function() currentHour = GameTime.getInstance():getWorldAgeHours() end)
    end
    duration = tonumber(duration)
    currentHour = tonumber(currentHour)
    if duration == nil or duration <= 0 or currentHour == nil then return false end

    local bounded = math.max(0, math.min(99.9, tonumber(level) or 0))
    local infectionStartHour = currentHour - duration * bounded / 100
    return pcall(function() bodyDamage:setInfectionTime(infectionStartHour) end)
end

function Infection.clampInHideout(player)
    if player == nil or player:isDead() or not Infection.playerInsideHideoutCell(player) then return false end
    local cap = math.max(0, math.min(100,
        tonumber(Config.value("HideoutInfectionCapPercent")) or 50))
    local level = Infection.level(player)
    if level < cap then return false end

    -- Build 42 derives Knox lethality from BodyDamage's infection start time and
    -- mortality duration, then rewrites the visible stat every update. Merely
    -- lowering that stat therefore cannot prevent the hidden clock reaching its
    -- fatal endpoint. Re-anchor the clock at the configured cap while the player
    -- remains anywhere in the isolated hideout cell; leaving resumes progression.
    holdMortalityClockAtLevel(player, cap)
    if level <= cap then return false end
    return Infection.setLevel(player, cap)
end

function Infection.cure(player)
    if player == nil or player:isDead() then return false end
    Infection.setLevel(player, 0)
    pcall(function() player:getStats():set(CharacterStat.ZOMBIE_FEVER, 0) end)
    pcall(function()
        local bodyDamage = player:getBodyDamage()
        local parts = bodyDamage:getBodyParts()
        if parts then
            for index = 0, parts:size() - 1 do
                local part = parts:get(index)
                if part then part:SetInfected(false) end
            end
        end
        bodyDamage:setInfected(false)
        bodyDamage:setInfectionTime(-1)
        bodyDamage:setInfectionMortalityDuration(-1)
    end)
    return true
end

ExtractionMode.Infection = Infection
return Infection
