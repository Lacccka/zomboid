require "PPO_Config"
require "PPO_MultiplierMath"
require "PPO_BonusMath"
require "PPO_AdaptationMath"
require "PPO_ExerciseState"
require "PPO_RecoveryContext"
require "PPO_TrainingSession"
require "PPO_AdaptationEngine"
require "PPO_ExerciseAuthority"
require "PPO_ConsumeAuthority"
require "PPO_BonusAwarder"

PPO = PPO or {}
PPO.ServerRuntime = PPO.ServerRuntime or {}

local ServerRuntime = PPO.ServerRuntime
ServerRuntime.PendingCharacters = ServerRuntime.PendingCharacters or {}
ServerRuntime.OnlineCharacters = ServerRuntime.OnlineCharacters or {}
ServerRuntime.ActiveCharacters = ServerRuntime.ActiveCharacters or {}
-- A corpse can stay in getOnlinePlayers() for many discovery passes, so a
-- handled death is remembered until that character leaves the list.
ServerRuntime.DeadCharacters = ServerRuntime.DeadCharacters or {}
-- The world minute of each online character's previous tick. Ephemeral: a
-- character that leaves loses its baseline, so offline time can never be
-- counted as active time.
ServerRuntime.LastTickMinute = ServerRuntime.LastTickMinute or {}
ServerRuntime.AdaptationEngine = ServerRuntime.AdaptationEngine
    or PPO.AdaptationEngine.new(nil)

local function directionFor(perk)
    if perk == Perks.Strength then return "Strength" end
    if perk == Perks.Fitness then return "Fitness" end
    return nil
end

local function debugLog(message)
    if PPO.Config.Runtime.Debug then
        print("[PPO] " .. message)
    end
end

function ServerRuntime.ensureExerciseAuthority()
    return PPO.ExerciseAuthority.ensureInstalled()
end

-- The consume seams follow the exercise authority exactly: one process-owned
-- instance, installed once at bootstrap and released only on an explicit
-- teardown. A non-authoritative process installs nothing.
function ServerRuntime.ensureConsumeAuthority()
    if not PPO.Config.Runtime.Enabled then return false end
    return PPO.ConsumeAuthority.ensureInstalled()
end

function ServerRuntime.releaseConsumeAuthority()
    return PPO.ConsumeAuthority.release(PPO.ConsumeAuthority.Default)
end

local function isInWorld(character)
    return character ~= nil and character:isExistInTheWorld()
end

local function queueCharacter(character)
    if PPO.Config.Runtime.Enabled and character ~= nil then
        ServerRuntime.PendingCharacters[character] = true
    end
end

-- Outside exercise the multiplier reads persisted Adaptation and the bounded
-- Readiness provider, never a config constant.
local function applyPerkValue(character, perk, level)
    local direction = directionFor(perk)
    if direction == nil then return end

    local engine = ServerRuntime.AdaptationEngine
    local inputs = engine:multiplierInputs(character, direction)
    local dailyStimulus = 0
    local component = PPO.ExerciseState.getComponent(character, direction)
    if component ~= nil then dailyStimulus = component.dailyStimulus end

    -- The additive multiplier already spends load through `loadFactor`; the
    -- daily return on top is the XP-facing surface that answers to the Sandbox
    -- decay switch, exactly as the in-exercise award does.
    local full = PPO.AdaptationEngine.multiplierFor(
        level, inputs, inputs.loadFactor)
    local multiplier = nil
    if full ~= nil then
        multiplier = PPO.BonusMath.effectiveMultiplier(
            full, PPO.MultiplierMath.dailyReturn(dailyStimulus))
    end

    local targetLevel = level + 1
    if multiplier == nil then
        multiplier = 1
        targetLevel = 10
    end

    -- `AddXP` applies its own nutrition factor before it reads this map, so the
    -- entry carries the drawn multiplier divided by that factor and the two
    -- together land on the number the panel shows. The applied value, not the
    -- drawn one, is what gets recorded: the engine compares against the same
    -- absorbed number, so crossing a protein threshold registers as a change
    -- and rewrites the entry within the minute instead of leaving it stale.
    local applied = PPO.BonusMath.absorbed(
        multiplier, PPO.BonusAwarder.vanillaFactor(character, perk))

    addXpMultiplier(character, perk, applied, targetLevel, targetLevel)
    engine:recordApplied(character, direction, applied, level)
    debugLog(tostring(perk) .. " level=" .. tostring(level)
        .. " multiplier=" .. tostring(applied))
end

function ServerRuntime.applyPerk(character, perk, levelOverride)
    if not PPO.Config.Runtime.Enabled or character == nil then return end
    if not isInWorld(character) then
        queueCharacter(character)
        return
    end

    -- The override is checked by type, not against `nil`: an unpassed
    -- parameter carries whatever the caller left on the stack, and anything
    -- that is not a number here would be read as a perk level.
    local level = levelOverride
    if type(level) ~= "number" then level = character:getPerkLevel(perk) end
    if PPO.ExerciseAuthority.refreshActiveLevel(
            character, perk, level, function()
                applyPerkValue(character, perk, level)
            end) then
        return
    end
    applyPerkValue(character, perk, level)
end

function ServerRuntime.refreshCharacter(character)
    if not PPO.Config.Runtime.Enabled or character == nil then return end
    if not isInWorld(character) then
        queueCharacter(character)
        return
    end

    ServerRuntime.PendingCharacters[character] = nil
    ServerRuntime.ActiveCharacters[character] = true
    ServerRuntime.applyPerk(character, Perks.Strength, nil)
    ServerRuntime.applyPerk(character, Perks.Fitness, nil)
end

function ServerRuntime.onCreatePlayer(playerIndex, character)
    ServerRuntime.refreshCharacter(character)
end

function ServerRuntime.onLevelPerk(character, perk, level, addBuffer)
    if perk ~= Perks.Strength and perk ~= Perks.Fitness then return end
    if not PPO.Config.Runtime.Enabled or character == nil then return end
    if not isInWorld(character) then
        queueCharacter(character)
        return
    end
    if ServerRuntime.PendingCharacters[character] then
        ServerRuntime.refreshCharacter(character)
        return
    end
    ServerRuntime.applyPerk(character, perk, level)
end

local function isDeadCharacter(character)
    local ok, dead = pcall(function()
        return character:isDead()
    end)
    return ok and dead == true
end

-- Death discards the incomplete session and runtime state for that character.
-- A new character is initialized independently; PPO state never transfers by
-- username, account or connection identity.
function ServerRuntime.onPlayerDeath(character)
    if character == nil then return end

    pcall(PPO.ExerciseAuthority.closeActiveCharacter, character)
    pcall(PPO.AdaptationEngine.discardCharacter,
        ServerRuntime.AdaptationEngine, character)
    PPO.RecoveryContext.SeamAvailability[character] = nil
    ServerRuntime.ActiveCharacters[character] = nil
    ServerRuntime.OnlineCharacters[character] = nil
    ServerRuntime.PendingCharacters[character] = nil
    ServerRuntime.DeadCharacters[character] = true
end

-- A disconnect only freezes; it never finalizes a partial session.
local function releaseOfflineCharacter(character)
    pcall(PPO.ExerciseAuthority.closeActiveCharacter, character)
    pcall(PPO.AdaptationEngine.freezeCharacter,
        ServerRuntime.AdaptationEngine, character)
    PPO.RecoveryContext.SeamAvailability[character] = nil
    ServerRuntime.OnlineCharacters[character] = nil
    ServerRuntime.ActiveCharacters[character] = nil
    ServerRuntime.PendingCharacters[character] = nil
    ServerRuntime.LastTickMinute[character] = nil
end

local function discoverDedicatedCharacters()
    if not isServer() then return end

    local onlinePlayers = getOnlinePlayers()
    if onlinePlayers == nil then return end

    local seenCharacters = {}
    for index = 0, onlinePlayers:size() - 1 do
        local character = onlinePlayers:get(index)
        if character ~= nil then
            if isDeadCharacter(character) then
                -- Dedicated cleanup cannot rely on OnPlayerDeath delivery, and
                -- a corpse stays discoverable, so it is handled exactly once.
                seenCharacters[character] = true
                if not ServerRuntime.DeadCharacters[character] then
                    ServerRuntime.onPlayerDeath(character)
                end
            else
                seenCharacters[character] = true
                if not ServerRuntime.OnlineCharacters[character] then
                    ServerRuntime.OnlineCharacters[character] = true
                    ServerRuntime.refreshCharacter(character)
                end
            end
        end
    end

    for character in pairs(ServerRuntime.OnlineCharacters) do
        if not seenCharacters[character] then
            releaseOfflineCharacter(character)
        end
    end

    for character in pairs(ServerRuntime.DeadCharacters) do
        if not seenCharacters[character] then
            ServerRuntime.DeadCharacters[character] = nil
        end
    end
end

-- Active time is the game time that passed while the character was online, not
-- the number of times vanilla called the minute event. Vanilla fires that event
-- about 0.4 times per game minute while a character sleeps, so counting calls
-- made a night's rest recover less than half of what its hours promise. One
-- minute is the fallback for the first tick after a login, an unreadable clock
-- and a clock that ran backwards; the ceiling keeps a server stall or a debug
-- clock jump from dumping hours into a single tick, because under-counting is
-- the safe direction.
local MAX_TICK_MINUTES = 10

local function elapsedSinceLastTick(character)
    local ok, minute = pcall(function()
        return getGameTime():getWorldAgeHours() * 60
    end)
    if not ok or type(minute) ~= "number" or minute ~= minute
            or minute == math.huge or minute == -math.huge then
        ServerRuntime.LastTickMinute[character] = nil
        return 1
    end

    local previous = ServerRuntime.LastTickMinute[character]
    ServerRuntime.LastTickMinute[character] = minute
    if previous == nil or minute < previous then return 1 end
    return math.min(minute - previous, MAX_TICK_MINUTES)
end

-- One engine tick per loaded online character owns load recovery, credit
-- conversion, grace, decay and session expiry, so nothing advances twice.
function ServerRuntime.advanceActiveRecovery()
    if not PPO.Config.Runtime.Enabled then return end

    for character in pairs(ServerRuntime.ActiveCharacters) do
        if isInWorld(character) then
            local ok, result = pcall(
                PPO.AdaptationEngine.tickCharacter,
                ServerRuntime.AdaptationEngine, character,
                elapsedSinceLastTick(character))
            if ok and result ~= nil then
                if result.Strength.changed then
                    ServerRuntime.applyPerk(character, Perks.Strength, nil)
                end
                if result.Fitness.changed then
                    ServerRuntime.applyPerk(character, Perks.Fitness, nil)
                end
            end
        else
            ServerRuntime.ActiveCharacters[character] = nil
        end
    end
end

function ServerRuntime.retryPendingCharacters()
    if not PPO.Config.Runtime.Enabled then return end

    discoverDedicatedCharacters()
    ServerRuntime.advanceActiveRecovery()

    for character in pairs(ServerRuntime.PendingCharacters) do
        if isInWorld(character) then
            ServerRuntime.refreshCharacter(character)
        end
    end
end

if not ServerRuntime.EventsRegistered then
    Events.OnCreatePlayer.Add(ServerRuntime.onCreatePlayer)
    Events.LevelPerk.Add(ServerRuntime.onLevelPerk)
    Events.EveryOneMinute.Add(ServerRuntime.retryPendingCharacters)
    if Events.OnPlayerDeath ~= nil and Events.OnPlayerDeath.Add ~= nil then
        Events.OnPlayerDeath.Add(ServerRuntime.onPlayerDeath)
    end
    ServerRuntime.EventsRegistered = true
    debugLog("server event handlers registered")
end

ServerRuntime.ensureExerciseAuthority()
ServerRuntime.ensureConsumeAuthority()
