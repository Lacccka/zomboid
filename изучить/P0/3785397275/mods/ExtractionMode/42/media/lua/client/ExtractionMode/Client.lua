require "ExtractionMode/Config"
require "ExtractionMode/Util"
require "ExtractionMode/Infection"
require "ExtractionMode/RaidOutcomes"
require "ExtractionMode/TownPicker"
require "ExtractionMode/BoardExtractionAction"
require "ExtractionMode/UseInfectionCureAction"
require "ExtractionMode/Upgrades"
require "ExtractionMode/Quests"
require "ExtractionMode/Barters"
require "ExtractionMode/Localization"
require "ExtractionMode/HideoutUtilities"
require "ExtractionMode/HideoutBenefits"
require "ExtractionMode/HideoutReading"
require "ExtractionMode/ProjectRemnantsIntegration"
require "ExtractionMode/CompanionDogsIntegration"
require "ExtractionMode/TrueCompanionsIntegration"
require "ExtractionMode/UpgradePanel"
require "ExtractionMode/QuestPanel"
require "ExtractionMode/CoopWelcome"
require "ExtractionMode/ControllerUI"
require "ExtractionMode/HideoutLighting"
require "ExtractionMode/HideoutLoadFade"
require "ExtractionMode/DeliveryLockerProtection"
require "ExtractionMode/GaragePanel"
require "ExtractionMode/GarageControls"
require "ExtractionMode/DebugPanel"
require "ExtractionMode/SinkDiagnostics"
require "ExtractionMode/RemnantsUICompatibility"
require "ExtractionMode/CampaignEpilogue"
require "ExtractionMode/HUD"
require "ExtractionMode/MapMarkers"
require "ExtractionMode/ExtractionRopeMarker"
require "ExtractionMode/ModCompatibility"
require "ExtractionMode/CommonSenseCompatibility"
require "ExtractionMode/ProjectRVClientCompatibility"
require "ISUI/ISContextMenu"
require "ISUI/ISToolTip"
require "ISUI/ISPostDeathUI"
require "ISUI/PlayerData/ISPlayerData"
require "TimedActions/ISEquipWeaponAction"
require "TimedActions/ISTimedActionQueue"

ExtractionMode = ExtractionMode or {}
local Config = ExtractionMode.Config
local Util = ExtractionMode.Util
local Infection = ExtractionMode.Infection
local RaidOutcomes = ExtractionMode.RaidOutcomes
local Upgrades = ExtractionMode.Upgrades
local HideoutUtilities = ExtractionMode.HideoutUtilities
local ModCompatibility = ExtractionMode.ModCompatibility
local HideoutBenefits = ExtractionMode.HideoutBenefits
local ProjectRemnantsIntegration = ExtractionMode.ProjectRemnantsIntegration
local CompanionDogsIntegration = ExtractionMode.CompanionDogsIntegration
local TrueCompanionsIntegration = ExtractionMode.TrueCompanionsIntegration
local HideoutLighting = ExtractionMode.HideoutLighting
local Localization = ExtractionMode.Localization
local Quests = ExtractionMode.Quests
local Client = {}
local lastPowerRefresh = 0
local flareTracking = {}
local lightingActivationPending = false
local boardingProtectionPrevious = {}
local extractionMusicOverride = nil
local lastGarageActivityAt = {}
local garageVehiclePulse = {
    vehicle = nil,
    vehicleId = nil,
    headlightsOn = false,
    stoplightsOn = false,
    alpha = {},
    targetAlpha = {},
    phase = nil,
    requestedVehicleId = nil,
    requestedUntil = 0,
}
local pendingRaidVehicleRelocation = nil
local pendingRebuiltRaidVehicleSeats = {}
local raidVehicleLaunch = nil
local RAID_VEHICLE_RELOCATION_TIMEOUT_MS = 10000
local RAID_VEHICLE_RELOCATION_STABLE_MS = 750
local RAID_VEHICLE_LAUNCH_SPEED_KMH = 20
local RAID_VEHICLE_LAUNCH_PREROLL_MS = 500
local RAID_VEHICLE_LAUNCH_VISIBLE_MS = 4000
local RAID_VEHICLE_LAUNCH_PHYSICS_PUSH_MS = 100
local RAID_VEHICLE_LAUNCH_READY_TIMEOUT_MS = 15000
-- Bob_EndDeath is 16960 ticks at 4800 ticks/second and plays at 1.04 speed.
-- Hand off at its midpoint without indexing engine animation objects from Lua.
local DRAG_DOWN_HALF_ANIMATION_MS = 1700

ExtractionMode.ClientStates = ExtractionMode.ClientStates or {}
local clientRuntimeByPlayer = {}

local function playerNumber(playerOrNumber)
    if type(playerOrNumber) == "number" then return math.max(0, math.floor(playerOrNumber)) end
    local number = 0
    if playerOrNumber ~= nil then
        pcall(function() number = math.max(0, tonumber(playerOrNumber:getPlayerNum()) or 0) end)
    end
    return number
end

function Client.stateFor(playerOrNumber)
    local number = playerNumber(playerOrNumber)
    local state = ExtractionMode.ClientStates[number]
    if state == nil then
        state = { state = nil, extractionSites = {}, readyNames = {}, participantNames = {} }
        ExtractionMode.ClientStates[number] = state
    end
    if number == 0 then ExtractionMode.ClientState = state end
    return state
end

local function runtimeFor(playerOrNumber)
    local number = playerNumber(playerOrNumber)
    local runtime = clientRuntimeByPlayer[number]
    if runtime == nil then
        runtime = {
            lastStateReceivedAt = 0,
            lastStateRequestAt = 0,
            lastRemnantsPlacementCheck = 0,
            pendingSafeLanding = nil,
            lastSafeLandingCheck = 0,
            groundExtractionVehicle = nil,
            deathRescueRequested = false,
            deathRescueGraceUntil = 0,
            deathRescueCinematic = false,
            deathRescueCinematicDeadline = 0,
            deathRescueFinalizeSent = false,
            deathRescueFinalAnimationStartedAt = 0,
            hideoutVehicleLock = nil,
        }
        clientRuntimeByPlayer[number] = runtime
    end
    return runtime
end

Client.stateFor(0)

function Client.sendCommand(player, command, args)
    if player == nil then return end
    if isClient and isClient() then
        sendClientCommand(player, Config.COMMAND_MODULE, command, args or {})
    elseif ExtractionMode.Server and ExtractionMode.Server.handleCommand then
        ExtractionMode.Server.handleCommand(player, command, args or {})
    end
end

function Client.findFlare(player)
    local inventory = player and player:getInventory()
    return inventory and inventory:getItemFromType(Config.FLARE_FULL_TYPE, true, true) or nil
end

function Client.findVaccineSample(player)
    local inventory = player and player:getInventory()
    return inventory and inventory:getItemFromType("ExtractionMode.VaccineSample", true, true) or nil
end

function Client.campaignQuestActive(data)
    data = data or Client.stateFor(0)
    local definition = Quests.definition("one_last_flight")
    return definition ~= nil and Quests.isAcquired(data.quests or {}, definition)
        and not Quests.isCompleted(data.quests or {}, definition.id)
end

function Client.atCampaignHandoff(player, data)
    data = data or Client.stateFor(player)
    local point = data.campaignHandoffPoint or Config.CAMPAIGN_HANDOFF_POINT
    if player == nil or point == nil then return false end
    return math.abs((tonumber(player:getZ()) or 0) - (tonumber(point.z) or 0)) <= 0.5
        and Util.playerNear(player, point, tonumber(point.radius) or 4)
end

function Client.flareEquipped(player)
    local item = player and player:getPrimaryHandItem()
    return item and item:getFullType() == Config.FLARE_FULL_TYPE
end

function Client.equipFlare(player)
    local item = Client.findFlare(player)
    if item == nil or Client.flareEquipped(player) then return end
    Config.applyExtractionFlareNoise(item)
    ISTimedActionQueue.add(ISEquipWeaponAction:new(player, item, 30, true, false))
end

function Client.boardExtraction(player)
    local data = Client.stateFor(player)
    if player == nil or player:getVehicle() ~= nil
        or data.extractionRope == nil or data.boardingPendingSelf == true then return end
    ISTimedActionQueue.add(ExtractionMode.BoardExtractionAction:new(player, data.extractionRope))
end

function Client.useInfectionCure(player, item)
    if player == nil or item == nil or item:getFullType() ~= Config.INFECTION_CURE_TYPE then return end
    ISTimedActionQueue.add(ExtractionMode.UseInfectionCureAction:new(player, item))
end

local function announce(message, isError, suppressDefaultSound, extendedHalo, targetPlayer)
    local player = targetPlayer or (getPlayer and getPlayer())
    if player and message and tostring(message) ~= "" then
        -- Build 42 removed the old addText(player, text, color) overload. Keep
        -- presentation failures isolated so they can never stop a raid transition.
        pcall(function()
            if extendedHalo == true then
                -- Standard halo notes last 128 ticks, which is too short for
                -- insertion briefings and death-loss summaries. Scale their
                -- timer to localized message length while keeping a sane cap.
                local text = tostring(message)
                local duration = math.max(256, math.min(640, 128 + #text * 2))
                if isError then
                    player:setHaloNote(text, 255, 105, 97, duration)
                else
                    player:setHaloNote(text, 137, 232, 148, duration)
                end
            elseif isError then
                HaloTextHelper.addBadText(player, tostring(message), "[br/]")
            else
                HaloTextHelper.addGoodText(player, tostring(message), "[br/]")
            end
        end)
        if suppressDefaultSound ~= true then
            pcall(function()
                if getSoundManager then getSoundManager():playUISound(isError and "UIError" or "UIActivateButton") end
            end)
        end
    end
end

local function showLocationChecked(message, targetPlayer)
    local player = targetPlayer or (getPlayer and getPlayer())
    message = tostring(message or "")
    if player == nil or message == "" then return end

    -- Build 42's configurable addGoodText color can resolve to black because
    -- its RGB components are truncated before conversion to 0-255 values.
    -- Set this short halo directly with the engine's known green RGB instead.
    local shown = pcall(function()
        player:setHaloNote(message, 137, 232, 148, 128)
    end)
    if not shown then
        pcall(function()
            HaloTextHelper.forceNextAddText()
            HaloTextHelper.addText(player, message, "[br/]", 137, 232, 148)
        end)
    end
end

local function playerByUsername(username)
    username = tostring(username or "")
    if username == "" then return nil end

    for _, player in ipairs(Util.players()) do
        if Util.username(player) == username then return player end
    end
    return nil
end

local function addressedLocalPlayer(args, explicitPlayer)
    if explicitPlayer ~= nil then return explicitPlayer end
    local identity = args and tostring(args._targetIdentity or "") or ""
    if identity ~= "" then
        for _, player in ipairs(Util.players()) do
            if Util.username(player) == identity then return player end
        end
    end
    local playerNum = args and tonumber(args._targetPlayerNum) or nil
    if playerNum ~= nil and getSpecificPlayer then
        local player = getSpecificPlayer(math.max(0, math.floor(playerNum)))
        if player ~= nil then return player end
    end
    return getPlayer and getPlayer() or nil
end

local function playDeathRescueVoice(username)
    local player = playerByUsername(username)
    if player == nil then return false end

    -- Play the vanilla death voice on the rescued character's world emitter.
    -- Every client receives this command, so nearby players hear the scream
    -- with normal positional attenuation while distant players do not.
    return pcall(function() player:playerVoiceSound("DeathAlone") end)
end

local function playAudioCue(cue, targetPlayer)
    cue = tostring(cue or "")
    if cue == "" then return false end

    if cue == "barter_completed" then
        local player = targetPlayer or (getPlayer and getPlayer())
        local emitter = player and player:getEmitter()
        if emitter ~= nil then
            local played = pcall(function() emitter:playSound("PutItemInBag") end)
            if played then return true end
        end
    end

    if not getSoundManager then return false end
    local manager = getSoundManager()
    if manager == nil then return false end

    local ok = false
    if cue == "extraction_music" then
        -- Play through Build 42's dedicated listener-global music channel for
        -- each participating client. Launching a Music-category sound through
        -- playUISound can still inherit a nearby world emitter and attenuation.
        ok = pcall(function()
            -- Keep the normal gameplay audio mix so weapons, zombies, ambience,
            -- and UI sounds remain audible. StopMusic targets only the music
            -- bus, allowing this one-clip track to replace the current score
            -- without the broad SFX ducking caused by the PauseMenu state.
            manager:setMusicState("InGame")
            manager:StopMusic()
            manager:playMusic("ExtractionMode_WWLTense")
            extractionMusicOverride = true
        end)
        if not ok then
            extractionMusicOverride = nil
            pcall(function() manager:setMusicState("InGame") end)
        end
    elseif cue == "raid_start" then
        ok = pcall(function() manager:playUISound("UIClickToStart") end)
    elseif cue == "upgrade_installed" or cue == "quest_completed" then
        ok = pcall(function() manager:playUISound("UIAchievement") end)
    elseif cue == "barter_completed" then
        ok = pcall(function() manager:playUISound("UIActivateButton") end)
    end
    return ok
end

local function stopExtractionMusicOverride()
    if extractionMusicOverride == nil or not getSoundManager then return end
    local manager = getSoundManager()
    if manager then
        pcall(function()
            manager:StopMusic()
            -- Return control to the normal adaptive in-game score after the
            -- extraction sequence ends.
            manager:setMusicState("InGame")
        end)
    end
    extractionMusicOverride = nil
end

local function boardingProtectionKey(player)
    if player == nil then return nil end
    local playerNum = 0
    pcall(function() playerNum = tonumber(player:getPlayerNum()) or 0 end)
    return tostring(playerNum)
end

local function localBoardingProtection(player)
    local key = boardingProtectionKey(player)
    return key and boardingProtectionPrevious[key] or nil
end

local function reinforceLocalTransitionProtection(player)
    local previous = localBoardingProtection(player)
    if player == nil or previous == nil then return false end
    pcall(function() player:setAvoidDamage(true) end)
    pcall(function() player:setInvincible(true) end)
    if previous.keepZombieTargeting == true then
        pcall(function() player:setZombiesDontAttack(previous.zombiesDontAttack == true) end)
        pcall(function() player:setInvisible(previous.invisible == true, true) end)
    else
        pcall(function() player:setZombiesDontAttack(true) end)
        pcall(function() player:setInvisible(true, true) end)
    end
    return true
end

local function setBoardingProtection(player, args)
    if player == nil then return end
    local key = boardingProtectionKey(player)
    if args.enabled == true then
        local previous = boardingProtectionPrevious[key]
        if previous == nil then
            local previousInvincible = player:isInvincible()
            local previousZombiesDontAttack = player:isZombiesDontAttack()
            local previousInvisible = player:isInvisible()
            local previousAvoidDamage = player:avoidDamage()
            if args.invincible ~= nil then previousInvincible = args.invincible == true end
            if args.zombiesDontAttack ~= nil then
                previousZombiesDontAttack = args.zombiesDontAttack == true
            end
            if args.invisible ~= nil then previousInvisible = args.invisible == true end
            previous = {
                invincible = previousInvincible,
                zombiesDontAttack = previousZombiesDontAttack,
                invisible = previousInvisible,
                avoidDamage = previousAvoidDamage,
            }
            boardingProtectionPrevious[key] = previous
        end
        previous.keepZombieTargeting = args.keepZombieTargeting == true
        reinforceLocalTransitionProtection(player)
        if args.keepZombieTargeting == true then
            -- A drag-down rescue must not break the grapple animation by making
            -- the survivor untargetable. Preserve the pre-transition visibility
            -- and zombie-ignore flags while still preventing ordinary damage.
            player:setZombiesDontAttack(previous.zombiesDontAttack == true)
            player:setInvisible(previous.invisible == true, true)
        else
            player:setZombiesDontAttack(true)
            player:setInvisible(true, true)
        end
    else
        local previous = boardingProtectionPrevious[key] or {
            invincible = args.invincible == true,
            zombiesDontAttack = args.zombiesDontAttack == true,
            invisible = args.invisible == true,
            avoidDamage = false,
        }
        player:setInvincible(previous.invincible == true)
        player:setZombiesDontAttack(previous.zombiesDontAttack == true)
        player:setInvisible(previous.invisible == true, true)
        player:setAvoidDamage(previous.avoidDamage == true)
        boardingProtectionPrevious[key] = nil
    end
end


function Client.showMessage(message, isError)
    announce(message, isError == true)
end

local function receiveState(args, targetPlayer)
    local player = targetPlayer or (getPlayer and getPlayer())
    local state = Client.stateFor(player)
    local runtime = runtimeFor(player)
    local lightingWasInstalled = Upgrades.isInstalled(state.upgrades, "lighting")
    local generatorWasRunning = state.generator and state.generator.running == true
    -- Server-command tables omit nil values. Clear the nullable destination
    -- fields before merging so a post-raid snapshot cannot leave the previous
    -- town displayed even though the authority has reset its selection.
    state.selectedTownKey = nil
    state.selectedTownName = nil
    state.selectedTownBy = nil
    state.raidBounds = nil
    state.groundExtractionPhaseSelf = nil
    state.deathRescuePhaseSelf = nil
    state.lateJoinPhaseSelf = nil
    state.joinableFactionTownKey = nil
    state.selectedJoinRaidKey = nil
    state.joinableFactionRaidId = nil
    state.activeHideoutVehicle = nil
    -- A completed garage transition is represented by nil on the authority and
    -- is therefore omitted from the server-command table. Clear the previous
    -- value before merging or the garage panel remains permanently disabled
    -- with PLEASE WAIT after a successful despawn.
    state.garageTransition = nil
    for key, value in pairs(args or {}) do
        if tostring(key):sub(1, 1) ~= "_" then state[key] = value end
    end
    ExtractionMode.CoopWelcome.maybeShow(player, args)
    if state.isParticipant ~= true then
        runtime.deathRescueRequested = false
        runtime.deathRescueCinematic = false
        runtime.deathRescueCinematicDeadline = 0
        runtime.deathRescueFinalizeSent = false
        runtime.deathRescueFinalAnimationStartedAt = 0
        if localBoardingProtection(player) == nil then
            pcall(function() if player then player:setAvoidDamage(false) end end)
        end
    elseif args.deathRescuePendingSelf == true then
        runtime.deathRescueRequested = true
        -- An older state snapshot may arrive after this client has already sent
        -- finalization. Do not let it restart the terminal animation locally.
        runtime.deathRescueCinematic = args.deathRescuePhaseSelf == "CINEMATIC"
            and not runtime.deathRescueFinalizeSent
        if runtime.deathRescueCinematic then
            runtime.deathRescueCinematicDeadline = Util.nowMs()
                + math.max(1, tonumber(args.deathRescueSeconds) or 6) * 1000
        end
    end
    if args.transitionProtectionSelf == true then
        setBoardingProtection(player, {
            enabled = true,
            invincible = args.transitionProtectionInvincible,
            zombiesDontAttack = args.transitionProtectionZombiesDontAttack,
            invisible = args.transitionProtectionInvisible,
            forceInvisible = args.transitionProtectionForceInvisible,
            keepZombieTargeting = args.transitionProtectionKeepZombieTargeting,
        })
    elseif args.transitionProtectionSelf == false and localBoardingProtection(player) ~= nil then
        setBoardingProtection(player, { enabled = false })
    end
    local extractionMusicShouldContinue = state.isParticipant == true
        and (state.state == Config.STATE_EXTRACTING or state.state == Config.STATE_BOARDING)
    if not extractionMusicShouldContinue then stopExtractionMusicOverride() end
    local lightingInstalled = Upgrades.isInstalled(state.upgrades, "lighting")
    local generatorRunning = state.generator and state.generator.running == true
    if (not lightingWasInstalled and lightingInstalled)
        or (not generatorWasRunning and generatorRunning and lightingInstalled) then
        lightingActivationPending = true
        lastPowerRefresh = 0
    elseif generatorWasRunning and not generatorRunning then
        lastPowerRefresh = 0
    end
    runtime.lastStateReceivedAt = Util.nowMs()
    HideoutBenefits.refreshHeating(state)
    local hud = ExtractionMode.createHUD()
    -- A panel that was hidden before the first multiplayer snapshot no longer
    -- receives prerender calls, so it cannot make itself visible again. Wake it
    -- explicitly as soon as authoritative state arrives.
    if hud then hud:setVisible(true) end
end

local function resolvePendingSafeLanding(player)
    local runtime = runtimeFor(player)
    local pendingSafeLanding = runtime.pendingSafeLanding
    if pendingSafeLanding == nil then return end
    if player == nil or player:isDead() then
        runtime.pendingSafeLanding = nil
        return
    end
    -- Vehicle insertion first preloads a safe outdoor square and then seats the
    -- player in the reconstructed vehicle. A slow chunk can leave this terrain
    -- fallback pending until after seating, at which point teleporting only the
    -- character ejects them beside an otherwise healthy vehicle. Never relocate
    -- a seated player, and pause while the rebuilt-seat retry still owns them.
    if player:getVehicle() ~= nil then
        runtime.pendingSafeLanding = nil
        return
    end
    local pendingSeat = pendingRebuiltRaidVehicleSeats[playerNumber(player)]
    if pendingSeat ~= nil and pendingSeat.player == player then return end
    local now = Util.nowMs()
    if now > pendingSafeLanding.expiresAt then runtime.pendingSafeLanding = nil; return end
    if now - runtime.lastSafeLandingCheck < 500 then return end
    runtime.lastSafeLandingCheck = now

    local square = Util.safeOutdoorLandSquareNear(pendingSafeLanding, 48)
    if square == nil then return end
    local x = square:getX() + 0.5
    local y = square:getY() + 0.5
    local z = square:getZ()
    if math.abs(player:getX() - x) > 0.25 or math.abs(player:getY() - y) > 0.25
        or math.floor(player:getZ()) ~= z then
        local squad = ProjectRemnantsIntegration.captureActiveSquad()
        local trueCompanions = TrueCompanionsIntegration.captureForTransition(player, true)
        player:teleportTo(x, y, z)
        ProjectRemnantsIntegration.relocateCapturedSquad(squad, player, x, y, z, true)
        TrueCompanionsIntegration.relocateCaptured(trueCompanions, player, x, y, z, true)
    end
    runtime.pendingSafeLanding = nil
end

local function leaveVehicleForGroundExtraction(player)
    local vehicle = player and player:getVehicle()
    runtimeFor(player).groundExtractionVehicle = nil
    if vehicle == nil then return end

    local seat = nil
    pcall(function() vehicle:setForceBrake() end)
    pcall(function() seat = vehicle:getSeat(player) end)
    -- Mirror the state-changing part of vanilla ISExitVehicle. Running it under
    -- full-black avoids an exit animation at the field boundary while ensuring
    -- the subsequent character teleport cannot relocate the vehicle entity.
    pcall(function() vehicle:exit(player) end)
    if seat ~= nil and tonumber(seat) and tonumber(seat) >= 0 then
        pcall(function() vehicle:setCharacterPosition(player, seat, "outside") end)
    end
    pcall(function()
        player:ClearVariable("ExitAnimationFinished")
        player:ClearVariable("bExitingVehicle")
        player:PlayAnim("Idle")
    end)
    pcall(function() triggerEvent("OnExitVehicle", player) end)
    pcall(function() vehicle:updateHasExtendOffsetForExitEnd(player) end)
end

local function prepareGarageVehicleRemoval(args, targetPlayer)
    local player = targetPlayer or (getPlayer and getPlayer())
    if player == nil then return end
    local vehicleId = tostring(args.vehicleId or "")
    if vehicleId ~= "" then
        -- Start the visual immediately. Waiting for the next replicated garage
        -- summary can consume most of the short storage-settle window.
        garageVehiclePulse.requestedVehicleId = vehicleId
        garageVehiclePulse.requestedUntil = Util.nowMs() + 3000
    end
    local nearby = false
    if tonumber(args.x) and tonumber(args.y) then
        local dx = player:getX() - tonumber(args.x)
        local dy = player:getY() - tonumber(args.y)
        local radius = math.max(1, tonumber(args.radius) or 8)
        nearby = dx * dx + dy * dy <= radius * radius
    end

    local mechanics = nil
    if getPlayerMechanicsUI then
        pcall(function() mechanics = getPlayerMechanicsUI(player:getPlayerNum()) end)
    end
    local workingOnVehicle = false
    if mechanics and mechanics.vehicle then
        pcall(function()
            workingOnVehicle = tostring(mechanics.vehicle:getId()) == vehicleId
        end)
    end
    if nearby or workingOnVehicle then
        pcall(function()
            if ISTimedActionQueue and ISTimedActionQueue.clear then
                ISTimedActionQueue.clear(player)
            end
        end)
    end
    if workingOnVehicle then pcall(function() mechanics:close() end) end
    if getPlayerLoot ~= nil then
        local loot = nil
        pcall(function() loot = getPlayerLoot(player:getPlayerNum()) end)
        if loot ~= nil and loot.inventoryPane ~= nil then
            local container = loot.inventoryPane.inventory
            local lootVehicle = nil
            local checked = {}
            while container ~= nil and checked[container] ~= true and lootVehicle == nil do
                checked[container] = true
                pcall(function() lootVehicle = container:getVehicle() end)
                if lootVehicle == nil then
                    local containingItem = nil
                    pcall(function() containingItem = container:getContainingItem() end)
                    container = nil
                    if containingItem ~= nil then
                        pcall(function() container = containingItem:getContainer() end)
                    end
                end
            end
            if lootVehicle ~= nil and tostring(lootVehicle:getId()) == vehicleId then
                pcall(function() loot:close() end)
            end
        end
    end

    local current = player:getVehicle()
    if current ~= nil and tostring(current:getId()) == vehicleId then
        leaveVehicleForGroundExtraction(player)
    end
end

local function prepareRaidVehicleRelocation(args, targetPlayer)
    local player = targetPlayer or (getPlayer and getPlayer())
    if player == nil then return end
    local vehicleId = tostring(args and args.vehicleId or "")
    if vehicleId == "" then return end

    -- Vehicle container pages retain VehiclePart references. If the destination
    -- chunk replaces or reattaches those parts while the page is rendering,
    -- third-party inventory UIs can dereference a part whose vehicle is briefly
    -- nil. Close these interfaces under the fade before moving the vehicle.
    local mechanics = nil
    if getPlayerMechanicsUI then
        pcall(function() mechanics = getPlayerMechanicsUI(player:getPlayerNum()) end)
    end
    if mechanics ~= nil and mechanics.vehicle ~= nil then
        local matches = false
        pcall(function() matches = tostring(mechanics.vehicle:getId()) == vehicleId end)
        if matches then pcall(function() mechanics:close() end) end
    end
    if getPlayerLoot ~= nil then
        local loot = nil
        pcall(function() loot = getPlayerLoot(player:getPlayerNum()) end)
        if loot ~= nil then pcall(function() loot:close() end) end
    end
end

local function teleport(args, targetPlayer)
    local player = targetPlayer or (getPlayer and getPlayer())
    -- A teleport packet can already be in flight when multiplayer confirms a
    -- death. Never relocate the corpse; allow the vanilla death screen and
    -- new-character flow to finish normally.
    if player == nil or player:isDead() then return end
    if ModCompatibility.isCommonSenseActive() then
        pcall(function()
            if ISTimedActionQueue and ISTimedActionQueue.clear then
                ISTimedActionQueue.clear(player)
            end
        end)
    end
    local runtime = runtimeFor(player)
    if args.leaveVehicle == true then leaveVehicleForGroundExtraction(player) end
    local goingToRaid = args.safeOutdoor == true
    local squad = ProjectRemnantsIntegration.captureActiveSquad()
    local trueCompanions = TrueCompanionsIntegration.captureForTransition(player, goingToRaid)
    if args.safeOutdoor == true then
        runtime.pendingSafeLanding = {
            x = tonumber(args.x), y = tonumber(args.y), z = tonumber(args.z) or 0,
            expiresAt = Util.nowMs() + 20000,
        }
        runtime.lastSafeLandingCheck = 0
    else
        runtime.pendingSafeLanding = nil
    end
    player:teleportTo(tonumber(args.x), tonumber(args.y), tonumber(args.z) or 0)
    resolvePendingSafeLanding(player)
    ProjectRemnantsIntegration.relocateCapturedSquad(squad, player,
        player:getX(), player:getY(), player:getZ(), args.safeOutdoor == true)
    TrueCompanionsIntegration.relocateCaptured(trueCompanions, player,
        player:getX(), player:getY(), player:getZ(), goingToRaid)
    CompanionDogsIntegration.onPlayerTeleported(player)
    if Infection.clampInHideout(player) and isClient and isClient() then
        pcall(function() sendPlayerStat(player, CharacterStat.ZOMBIE_INFECTION) end)
    end
end

local function applyRaidVehicleTransform(vehicle, args)
    if vehicle == nil then return false, "vehicle is unavailable" end
    local moved, moveError = pcall(function()
        local targetX = tonumber(args and args.x) or 0
        local targetY = tonumber(args and args.y) or 0
        local targetZ = tonumber(args and args.z) or 0
        -- setForceBrake dereferences BaseVehicle.controller internally. During
        -- cross-cell streaming that controller may briefly be nil, which aborts
        -- the entire transform with a Java NPE. Braking is not required for the
        -- teleport itself; only use it while a live physics controller exists.
        local controller = nil
        pcall(function() controller = vehicle:getController() end)
        if controller ~= nil then vehicle:setForceBrake() end
        local transform = Transform.new()
        vehicle:getWorldTransform(transform)
        local origin = transform:getOrigin()
        if origin ~= nil then
            origin:set(origin:x() + targetX - vehicle:getX(), origin:y(),
                origin:z() + targetY - vehicle:getY())
            vehicle:setWorldTransform(transform)
        end
        vehicle:setPosition(targetX, targetY, targetZ)
        -- Do not detach the vehicle from its current square until the destination
        -- has streamed in. The relocation tick attaches it as soon as that square
        -- is available.
        local targetSquare = getCell():getGridSquare(math.floor(targetX),
            math.floor(targetY), math.floor(targetZ))
        if targetSquare ~= nil then vehicle:setCurrentSquareFromPosition() end
        local heading = tonumber(args and args.angleY) or tonumber(args and args.angleZ) or 0
        vehicle:setAngles(0, heading, 0)
        if controller ~= nil then vehicle:setSpeedKmHour(0) end
        vehicle:updatePhysicsNetwork()
    end)
    return moved, moveError
end

local function raidVehiclePhysicsController(vehicle)
    local controller = nil
    if vehicle ~= nil then pcall(function() controller = vehicle:getController() end) end
    return controller
end

local function raidVehiclePhysicsReady(vehicle)
    if raidVehiclePhysicsController(vehicle) == nil then return false end
    local active = false
    pcall(function() active = vehicle:isPhysicsActive() == true end)
    return active
end

local function ensureRaidVehiclePhysics(vehicle)
    if vehicle == nil then return false, "vehicle is unavailable" end
    local ready, physicsError = pcall(function()
        vehicle:setCurrentSquareFromPosition()
        if vehicle:getController() == nil then return end
        vehicle:checkSurroundingChunks()
        vehicle:setPhysicsActive(true, true)
        vehicle:updatePhysicsNetwork()
    end)
    local active = false
    if ready and raidVehiclePhysicsController(vehicle) ~= nil then
        pcall(function() active = vehicle:isPhysicsActive() == true end)
    end
    return ready and active, physicsError
end

local function raidVehicleAtRelocationTarget(vehicle, args)
    if vehicle == nil then return false end
    local atTarget = false
    pcall(function()
        local dx = (tonumber(vehicle:getX()) or 0) - (tonumber(args and args.x) or 0)
        local dy = (tonumber(vehicle:getY()) or 0) - (tonumber(args and args.y) or 0)
        atTarget = dx * dx + dy * dy <= 4
    end)
    return atTarget
end

function Client.relocateRaidVehicle(args, targetPlayer)
    local player = targetPlayer or (getPlayer and getPlayer())
    local vehicle = player and player:getVehicle() or nil
    local vehicleId = tostring(args and args.vehicleId or "")
    if vehicle == nil or tostring(vehicle:getId()) ~= vehicleId then
        local vehicles = nil
        pcall(function() vehicles = getCell():getVehicles():toArray() end)
        if vehicles ~= nil then
            for index = 0, #vehicles - 1 do
                local candidate = vehicles[index]
                if candidate ~= nil and tostring(candidate:getId()) == vehicleId then
                    vehicle = candidate
                    break
                end
            end
        end
    end
    if vehicle == nil then return false end
    local localOccupants = {}
    for _, occupant in ipairs(Util.players()) do
        local localPlayer = false
        if occupant ~= nil then pcall(function() localPlayer = occupant:isLocalPlayer() == true end) end
        if localPlayer and occupant:getVehicle() == vehicle then
            local seat = -1
            pcall(function() seat = vehicle:getSeat(occupant) end)
            local fallbackCompanions = TrueCompanionsIntegration.prepareVehicleTransition(occupant)
            localOccupants[#localOccupants + 1] = {
                player = occupant,
                seat = seat,
                fallbackCompanions = fallbackCompanions,
            }
            prepareRaidVehicleRelocation(args, occupant)
        end
    end
    if #localOccupants == 0 then
        local moved, moveError = applyRaidVehicleTransform(vehicle, args)
        if not moved then
            Util.log("Client raid vehicle relocation failed vehicle=" .. vehicleId
                .. " error=" .. tostring(moveError))
        end
        return moved
    end

    -- The driver client owns the live Bullet controller. Move the complete,
    -- occupied physics body while that controller and every VehiclePart are
    -- still attached. Exiting/teleporting occupants first unloads the hideout
    -- chunk and leaves behind a detached vehicle whose coordinates can change
    -- but which can no longer render, simulate, or accept passengers.
    local moved, moveError = applyRaidVehicleTransform(vehicle, args)
    if not moved then
        Util.log("Client occupied raid vehicle relocation failed vehicle=" .. vehicleId
            .. " error=" .. tostring(moveError))
        return false
    end
    for _, record in ipairs(localOccupants) do
        TrueCompanionsIntegration.relocateCaptured(record.fallbackCompanions,
            record.player, tonumber(args.x), tonumber(args.y), tonumber(args.z), true)
    end
    local now = Util.nowMs()
    pendingRaidVehicleRelocation = {
        vehicle = vehicle,
        vehicleId = vehicleId,
        args = args,
        occupants = localOccupants,
        coordinatorPlayerNum = playerNumber(player),
        startedAt = now,
        nextAttemptAt = now,
        stableSince = nil,
        queuedFadeIns = {},
    }
    Util.log("Staging client raid vehicle relocation vehicle=" .. vehicleId
        .. " target=" .. tostring(args.x) .. "," .. tostring(args.y) .. ","
        .. tostring(args.z) .. " occupants=" .. tostring(#localOccupants)
        .. " seatsPreserved=true")
    return true
end

local function prepareRebuiltRaidVehicleEngine(vehicle, pending)
    if vehicle == nil or pending == nil then return false end
    if pending.enginePrepared == true then return true end
    local prepared = false
    local prepareError = nil
    local ok, result = pcall(function()
        -- Build 42 can deliver an incremental VehicleEngine packet before a
        -- newly reconstructed multiplayer vehicle has received VehicleSounds.
        -- Wait for VehicleManager to finish that setup before changing or
        -- transmitting engine state.
        if vehicle:getVehicleSounds() == nil then return false end
        local power = math.max(1, math.floor(tonumber(pending.enginePower) or 0))
        vehicle:setEngineFeature(math.floor(tonumber(pending.engineQuality) or 0),
            math.floor(tonumber(pending.engineLoudness) or 0), power)
        return true
    end)
    if ok then
        prepared = result == true
    else
        prepareError = result
    end
    if not prepared and prepareError ~= nil then pending.lastError = prepareError end
    pending.enginePrepared = prepared
    return prepared
end

function Client.applyExtractionShove(args)
    local records = args and args.zombies or nil
    local zombies = getCell and getCell() and getCell():getZombieList()
    if records == nil or zombies == nil then return end

    local byId = {}
    for _, record in pairs(records) do
        local onlineId = tonumber(record and record.id)
        if onlineId ~= nil and onlineId >= 0 then byId[onlineId] = record end
    end

    for index = 0, zombies:size() - 1 do
        local zombie = zombies:get(index)
        local record = nil
        if zombie ~= nil then
            pcall(function() record = byId[tonumber(zombie:getOnlineID())] end)
        end
        if record ~= nil then
            pcall(function()
                local victim = zombie:getTarget()
                zombie:setTarget(nil)
                zombie:setEatBodyTarget(nil, false)
                if victim ~= nil then
                    pcall(function()
                        if victim:getAttackedBy() == zombie then victim:setAttackedBy(nil) end
                        victim:setHitReaction("")
                        victim:setDeathDragDown(false)
                    end)
                end
                local dx = tonumber(record.dx) or 0
                local dy = tonumber(record.dy) or 0
                zombie:getHitDir():set(dx, dy)
                zombie:setHitForce(1.5)
                zombie:setStaggerTimeMod(1.0)
                zombie:setHitFromBehind(false)
                zombie:setHitReaction("")
                zombie:setStaggerBack(true)
                if not zombie:isOnFloor() then
                    -- The action event activates the animation graph and its
                    -- deferred backward movement. StaggerBackState by itself
                    -- only manages timing and therefore looked stationary.
                    zombie:reportEvent("wasHit")
                end
            end)
        end
    end
end

local function fade(out, args, targetPlayer)
    local player = targetPlayer or (getPlayer and getPlayer())
    local playerNum = player and player:getPlayerNum() or 0
    local runtime = runtimeFor(playerNum)
    local seconds = math.max(0.1, tonumber(args and args.seconds) or 1)
    if out and player ~= nil and ModCompatibility.isCommonSenseActive() then
        pcall(function()
            if ISTimedActionQueue and ISTimedActionQueue.clear then
                ISTimedActionQueue.clear(player)
            end
        end)
    end
    if out and args and args.leaveVehicle == true then
        runtime.groundExtractionVehicle = player and player:getVehicle() or nil
        if runtime.groundExtractionVehicle then
            pcall(function() runtime.groundExtractionVehicle:setForceBrake() end)
        end
    elseif not out then
        runtime.groundExtractionVehicle = nil
    end
    pcall(function()
        UIManager.setFadeBeforeUI(playerNum, true)
        if out then UIManager.FadeOut(playerNum, seconds) else UIManager.FadeIn(playerNum, seconds) end
    end)
end

local function pendingRelocationContainsPlayer(player)
    local pending = pendingRaidVehicleRelocation
    if pending == nil or player == nil then return false end
    for _, record in ipairs(pending.occupants or {}) do
        if record.player == player then return true end
    end
    return false
end

local function launchContainsPlayer(player)
    if raidVehicleLaunch == nil or player == nil then return false end
    if player:getVehicle() == raidVehicleLaunch.vehicle then return true end
    for _, record in ipairs(raidVehicleLaunch.occupants or {}) do
        if record.player == player then return true end
    end
    return false
end

local function armRaidVehicleLaunch(pending, now)
    raidVehicleLaunch = {
        vehicle = pending.vehicle,
        vehicleId = pending.vehicleId,
        occupants = pending.occupants,
        coordinatorPlayerNum = pending.coordinatorPlayerNum,
        phase = "ARMED",
        armedAt = tonumber(now) or Util.nowMs(),
        queuedFadeIns = {},
    }
    -- Start on the driver-owned simulation only after seating and sound setup;
    -- the server deliberately avoids racing an incremental engine packet ahead
    -- of the freshly reconstructed client vehicle.
    pcall(function()
        if pending.vehicle:isEngineRunning() ~= true then pending.vehicle:engineDoRunning() end
        pending.vehicle:transmitEngine()
        pending.vehicle:updatePhysicsNetwork()
    end)
end

local function queueRaidVehicleLaunchFadeIn(player, args)
    local launch = raidVehicleLaunch
    if launch == nil or player == nil then return false end
    launch.queuedFadeIns = launch.queuedFadeIns or {}
    launch.queuedFadeIns[playerNumber(player)] = { player = player, args = args }
    return true
end

local function releaseRaidVehicleLaunchFadeIns(launch)
    if launch == nil or launch.fadeReleased == true then return end
    launch.fadeReleased = true
    for _, record in pairs(launch.queuedFadeIns or {}) do
        fade(false, record.args, record.player)
    end
    launch.queuedFadeIns = {}
end

local function applyRaidVehicleLaunchThrottle(vehicle, enabled, pushPhysics)
    if vehicle == nil then return false, "vehicle is unavailable" end
    local applied, applyError = pcall(function()
        local controller = vehicle:getController()
        if controller == nil then error("physics controller unavailable") end
        if enabled then
            if vehicle:isEngineRunning() ~= true then vehicle:engineDoRunning() end
            vehicle:setBraking(false)
            vehicle:setCurrentSteering(0)
            -- CarController is returned to Lua but its methods are not exported by
            -- Build 42. Cruise control is entirely available through BaseVehicle
            -- and CarController.update() treats it as forward throttle while below
            -- the target speed. BaseVehicle:updatePhysics() safely reaches that
            -- native update without indexing the hidden controller API.
            vehicle:setRegulatorSpeed(RAID_VEHICLE_LAUNCH_SPEED_KMH)
            vehicle:setRegulator(true)
            if pushPhysics == true then vehicle:updatePhysics() end
        else
            vehicle:setRegulator(false)
            vehicle:setRegulatorSpeed(0)
            if pushPhysics == true then vehicle:updatePhysics() end
        end
    end)
    return applied, applyError
end

local function beginRaidVehicleLaunch(now)
    local launch = raidVehicleLaunch
    if launch == nil or launch.phase ~= "ARMED" or launch.vehicle == nil then return false end
    now = tonumber(now) or Util.nowMs()
    if now < (tonumber(launch.nextReadyAttemptAt) or 0) then return false end
    launch.nextReadyAttemptAt = now + RAID_VEHICLE_LAUNCH_PHYSICS_PUSH_MS
    local physicsReady, physicsError = ensureRaidVehiclePhysics(launch.vehicle)
    if not physicsReady then
        launch.lastPhysicsError = physicsError
        if launch.physicsWaitLogged ~= true then
            launch.physicsWaitLogged = true
            Util.log("Raid vehicle rolling arrival waiting for active physics vehicle="
                .. tostring(launch.vehicleId) .. " controller="
                .. tostring(raidVehiclePhysicsController(launch.vehicle) ~= nil)
                .. " error=" .. tostring(physicsError))
        end
        return false
    end
    local started, startError = applyRaidVehicleLaunchThrottle(launch.vehicle, true, true)
    if started then
        started, startError = pcall(function()
            launch.vehicle:transmitEngine()
            launch.vehicle:updatePhysicsNetwork()
        end)
    end
    if not started then
        Util.log("Raid vehicle rolling arrival failed vehicle=" .. tostring(launch.vehicleId)
            .. " error=" .. tostring(startError))
        releaseRaidVehicleLaunchFadeIns(launch)
        raidVehicleLaunch = nil
        return false
    end
    launch.phase = "DRIVING"
    launch.startedAt = now
    launch.revealAt = now + RAID_VEHICLE_LAUNCH_PREROLL_MS
    launch.releaseAt = launch.revealAt + RAID_VEHICLE_LAUNCH_VISIBLE_MS
    launch.nextPhysicsPushAt = now + RAID_VEHICLE_LAUNCH_PHYSICS_PUSH_MS
    Util.log("Raid vehicle rolling arrival started vehicle=" .. tostring(launch.vehicleId)
        .. " targetSpeed=" .. tostring(RAID_VEHICLE_LAUNCH_SPEED_KMH))
    return true
end

local function raidVehicleLaunchHasQueuedFadeIn(launch)
    for _, record in pairs((launch and launch.queuedFadeIns) or {}) do
        if record ~= nil then return true end
    end
    return false
end

local function tryBeginRaidVehicleLaunch(now)
    local launch = raidVehicleLaunch
    local callOk, started = pcall(function() return beginRaidVehicleLaunch(now) end)
    if callOk then return started == true end
    Util.log("Raid vehicle rolling arrival retry failed vehicle="
        .. tostring(launch and launch.vehicleId) .. " error=" .. tostring(started))
    releaseRaidVehicleLaunchFadeIns(launch)
    raidVehicleLaunch = nil
    return false
end

local function processRaidVehicleLaunch(player, now)
    local launch = raidVehicleLaunch
    if launch == nil or playerNumber(player) ~= launch.coordinatorPlayerNum then return end
    now = tonumber(now) or Util.nowMs()
    if launch.phase == "ARMED" then
        if now - (tonumber(launch.armedAt) or now) > RAID_VEHICLE_LAUNCH_READY_TIMEOUT_MS then
            Util.log("Raid vehicle rolling arrival timed out waiting for physics vehicle="
                .. tostring(launch.vehicleId) .. " controller="
                .. tostring(raidVehiclePhysicsController(launch.vehicle) ~= nil)
                .. " active=" .. tostring(raidVehiclePhysicsReady(launch.vehicle))
                .. " error=" .. tostring(launch.lastPhysicsError))
            releaseRaidVehicleLaunchFadeIns(launch)
            raidVehicleLaunch = nil
        elseif raidVehicleLaunchHasQueuedFadeIn(launch) then
            tryBeginRaidVehicleLaunch(now)
        end
        return
    end
    if launch.phase ~= "DRIVING" then raidVehicleLaunch = nil; return end
    -- Input polling rewrites ClientControls every frame. Reassert the synthetic
    -- accelerator throughout the short arrival so this behaves like held gas,
    -- not merely a cruise-control target.
    local pushPhysics = now >= (tonumber(launch.nextPhysicsPushAt) or 0)
    applyRaidVehicleLaunchThrottle(launch.vehicle, true, pushPhysics)
    if pushPhysics then
        launch.nextPhysicsPushAt = now + RAID_VEHICLE_LAUNCH_PHYSICS_PUSH_MS
    end
    if launch.fadeReleased ~= true and now >= (tonumber(launch.revealAt) or now) then
        releaseRaidVehicleLaunchFadeIns(launch)
        local engineRunning, throttle, speed = false, 0, 0
        pcall(function()
            engineRunning = launch.vehicle:isEngineRunning() == true
            throttle = tonumber(launch.vehicle:getThrottle()) or 0
            speed = tonumber(launch.vehicle:getCurrentSpeedKmHour()) or 0
        end)
        Util.log("Raid vehicle fade-in released after powered preroll vehicle="
            .. tostring(launch.vehicleId) .. " engineRunning=" .. tostring(engineRunning)
            .. " throttle=" .. tostring(throttle) .. " speedKmh=" .. tostring(speed))
    end
    local driver = nil
    pcall(function() driver = launch.vehicle:getDriver() end)
    if driver == nil or driver:getVehicle() ~= launch.vehicle
        or now >= (tonumber(launch.releaseAt) or now) then
        local releaseReason = "timer"
        local releaseThrottle, releaseSpeed = 0, 0
        if driver == nil or driver:getVehicle() ~= launch.vehicle then releaseReason = "driver-left" end
        pcall(function()
            releaseThrottle = tonumber(launch.vehicle:getThrottle()) or 0
            releaseSpeed = tonumber(launch.vehicle:getCurrentSpeedKmHour()) or 0
        end)
        applyRaidVehicleLaunchThrottle(launch.vehicle, false, true)
        pcall(function()
            -- Leave the engine and the vehicle's existing momentum alone; only
            -- release the short automatic throttle so the driver takes over.
            launch.vehicle:setRegulator(false)
            launch.vehicle:setRegulatorSpeed(0)
            launch.vehicle:updatePhysicsNetwork()
        end)
        releaseRaidVehicleLaunchFadeIns(launch)
        Util.log("Raid vehicle rolling arrival released vehicle="
            .. tostring(launch.vehicleId) .. " reason=" .. releaseReason
            .. " throttle=" .. tostring(releaseThrottle)
            .. " speedKmh=" .. tostring(releaseSpeed))
        raidVehicleLaunch = nil
    end
end

local function loadedRaidVehicleById(vehicleId)
    local found = nil
    pcall(function()
        found = getVehicleById(tonumber(vehicleId))
        if found ~= nil then return end
        local vehicles = getCell():getVehicles():toArray()
        for index = 0, #vehicles - 1 do
            local candidate = vehicles[index]
            if candidate ~= nil and tostring(candidate:getId()) == tostring(vehicleId) then
                found = candidate
                return
            end
        end
    end)
    return found
end

local function queueRebuiltRaidVehicleSeat(args, player)
    if player == nil then return end
    local playerNum = playerNumber(player)
    pendingRebuiltRaidVehicleSeats[playerNum] = {
        player = player,
        vehicleId = tostring(args and args.vehicleId or ""),
        seat = tonumber(args and args.seat) or -1,
        isDriver = args and args.isDriver == true,
        engineQuality = tonumber(args and args.engineQuality) or 0,
        engineLoudness = tonumber(args and args.engineLoudness) or 0,
        enginePower = tonumber(args and args.enginePower) or 0,
        startedAt = Util.nowMs(),
        nextAttemptAt = 0,
        queuedFadeIn = nil,
        lastError = nil,
    }
end

local function processRebuiltRaidVehicleSeat(player, now)
    local playerNum = playerNumber(player)
    local pending = pendingRebuiltRaidVehicleSeats[playerNum]
    if pending == nil or pending.player ~= player then return end
    now = tonumber(now) or Util.nowMs()
    local vehicle = loadedRaidVehicleById(pending.vehicleId)
    if vehicle ~= nil and not prepareRebuiltRaidVehicleEngine(vehicle, pending) then
        vehicle = nil
    end
    local seated = false
    if vehicle ~= nil and player:getVehicle() == vehicle then
        local actualSeat = -1
        pcall(function() actualSeat = vehicle:getSeat(player) end)
        seated = actualSeat == pending.seat
    elseif vehicle ~= nil and pending.seat >= 0
        and now >= (tonumber(pending.nextAttemptAt) or 0) then
        pending.nextAttemptAt = now + 250
        local entered, enterError = pcall(function()
            local current = vehicle:getCharacter(pending.seat)
            if current ~= nil and current ~= player then return end
            vehicle:enter(pending.seat, player)
            vehicle:setCharacterPosition(player, pending.seat, "inside")
            vehicle:transmitCharacterPosition(pending.seat, "inside")
            vehicle:playPassengerAnim(pending.seat, "idle")
            player:ClearVariable("bExitingVehicle")
        end)
        if not entered then pending.lastError = enterError end
        if player:getVehicle() == vehicle then
            local actualSeat = -1
            pcall(function() actualSeat = vehicle:getSeat(player) end)
            seated = actualSeat == pending.seat
            if seated then pcall(function() triggerEvent("OnEnterVehicle", player) end) end
        end
    end

    if seated then
        local runtime = runtimeFor(player)
        if runtime.pendingSafeLanding ~= nil then
            runtime.pendingSafeLanding = nil
            Util.log("Cancelled pending safe landing after reconstructed vehicle seating player="
                .. tostring(Util.username(player)) .. " vehicle=" .. tostring(pending.vehicleId))
        end
        if pending.isDriver then
            armRaidVehicleLaunch({
                vehicle = vehicle,
                vehicleId = pending.vehicleId,
                occupants = { { player = player, seat = pending.seat } },
                coordinatorPlayerNum = playerNum,
            }, now)
        end
        local queuedFadeIn = pending.queuedFadeIn
        pendingRebuiltRaidVehicleSeats[playerNum] = nil
        Util.log("Client seated in reconstructed raid vehicle=" .. tostring(pending.vehicleId)
            .. " seat=" .. tostring(pending.seat))
        if queuedFadeIn ~= nil then
            if launchContainsPlayer(player) then
                queueRaidVehicleLaunchFadeIn(player, queuedFadeIn)
                if raidVehicleLaunch.phase ~= "DRIVING" then tryBeginRaidVehicleLaunch(now) end
                if raidVehicleLaunch ~= nil then return end
            end
            fade(false, queuedFadeIn, player)
        end
        return
    end

    if now - (tonumber(pending.startedAt) or now) >= 15000 then
        local queuedFadeIn = pending.queuedFadeIn
        pendingRebuiltRaidVehicleSeats[playerNum] = nil
        Util.log("Client timed out seating in reconstructed raid vehicle="
            .. tostring(pending.vehicleId) .. " loaded=" .. tostring(vehicle ~= nil)
            .. " lastError=" .. tostring(pending.lastError))
        if queuedFadeIn ~= nil then fade(false, queuedFadeIn, player) end
    end
end

local function releasePendingRaidVehicleFadeIns(pending)
    local hasQueuedFadeIn = false
    for _ in pairs(pending and pending.queuedFadeIns or {}) do
        hasQueuedFadeIn = true
        break
    end
    if hasQueuedFadeIn then
        tryBeginRaidVehicleLaunch(Util.nowMs())
    end
    for _, record in pairs(pending and pending.queuedFadeIns or {}) do
        fade(false, record.args, record.player)
    end
end

local function pendingRaidVehicleSeatsMatch(pending)
    local vehicle = pending and pending.vehicle
    if vehicle == nil then return false end
    for _, record in ipairs(pending.occupants or {}) do
        local occupant = record.player
        local expectedSeat = tonumber(record.seat) or -1
        local actualSeat = -1
        if occupant == nil or occupant:getVehicle() ~= vehicle then return false end
        pcall(function() actualSeat = vehicle:getSeat(occupant) end)
        if actualSeat ~= expectedSeat then return false end
    end
    return true
end

local function restorePendingRaidVehicleSeats(pending)
    local vehicle = pending and pending.vehicle
    if vehicle == nil or not raidVehiclePhysicsReady(vehicle) then return false end
    local allSeated = true
    for _, record in ipairs(pending.occupants or {}) do
        local occupant = record.player
        local seat = tonumber(record.seat) or -1
        local enteredNow = false
        if occupant == nil or seat < 0 then
            allSeated = false
        elseif occupant:getVehicle() ~= vehicle then
            local seatAvailable = true
            pcall(function()
                local current = vehicle:getCharacter(seat)
                seatAvailable = current == nil or current == occupant
            end)
            if seatAvailable then
                pcall(function() enteredNow = vehicle:enter(seat, occupant) == true end)
            end
            if occupant:getVehicle() ~= vehicle then allSeated = false end
        end
        if occupant ~= nil and occupant:getVehicle() == vehicle and seat >= 0 then
            pcall(function()
                vehicle:setCharacterPosition(occupant, seat, "inside")
                vehicle:transmitCharacterPosition(seat, "inside")
                vehicle:playPassengerAnim(seat, "idle")
                occupant:ClearVariable("bExitingVehicle")
            end)
            if enteredNow then pcall(function() triggerEvent("OnEnterVehicle", occupant) end) end
        end
    end
    return allSeated
end

local function processPendingRaidVehicleRelocation(player, now)
    local pending = pendingRaidVehicleRelocation
    if pending == nil or playerNumber(player) ~= pending.coordinatorPlayerNum then return end
    now = tonumber(now) or Util.nowMs()
    local targetSquare = nil
    pcall(function()
        targetSquare = getCell():getGridSquare(math.floor(tonumber(pending.args.x) or 0),
            math.floor(tonumber(pending.args.y) or 0),
            math.floor(tonumber(pending.args.z) or 0))
    end)
    local atTarget = targetSquare ~= nil
        and raidVehicleAtRelocationTarget(pending.vehicle, pending.args)
    local physicsReady = atTarget and raidVehiclePhysicsReady(pending.vehicle)
    if atTarget and not physicsReady
        and now >= (tonumber(pending.nextAttemptAt) or 0) then
        pending.nextAttemptAt = now + 200
        local ready, physicsError = ensureRaidVehiclePhysics(pending.vehicle)
        physicsReady = ready
        if not ready then pending.lastError = physicsError or "physics controller unavailable" end
    end
    if atTarget and physicsReady then
        pending.stableSince = pending.stableSince or now
        if now - pending.stableSince >= RAID_VEHICLE_RELOCATION_STABLE_MS then
            if pendingRaidVehicleSeatsMatch(pending) then
                pending.seatedSince = pending.seatedSince or now
                if now - pending.seatedSince >= RAID_VEHICLE_RELOCATION_STABLE_MS then
                    Util.log("Client confirmed raid vehicle relocation vehicle="
                        .. tostring(pending.vehicleId) .. " physicsStableMs="
                        .. tostring(now - pending.stableSince) .. " seatStableMs="
                        .. tostring(now - pending.seatedSince))
                    armRaidVehicleLaunch(pending, now)
                    pendingRaidVehicleRelocation = nil
                    releasePendingRaidVehicleFadeIns(pending)
                    return
                end
            else
                pending.seatedSince = nil
                if now >= (tonumber(pending.nextSeatAttemptAt) or 0) then
                    pending.nextSeatAttemptAt = now + 250
                    restorePendingRaidVehicleSeats(pending)
                end
            end
        end
    else
        pending.stableSince = nil
        pending.seatedSince = nil
        if targetSquare ~= nil and not atTarget
            and now >= (tonumber(pending.nextAttemptAt) or 0) then
            pending.nextAttemptAt = now + 200
            local moved, moveError = applyRaidVehicleTransform(pending.vehicle, pending.args)
            if not moved then pending.lastError = moveError end
        end
    end
    if now - (tonumber(pending.startedAt) or now) >= RAID_VEHICLE_RELOCATION_TIMEOUT_MS then
        local actualX, actualY = "?", "?"
        pcall(function()
            actualX = tostring(pending.vehicle:getX())
            actualY = tostring(pending.vehicle:getY())
        end)
        Util.log("Client raid vehicle relocation timed out vehicle="
            .. tostring(pending.vehicleId) .. " targetSquareLoaded="
            .. tostring(targetSquare ~= nil) .. " actual=" .. actualX .. "," .. actualY
            .. " physicsReady="
            .. tostring(raidVehiclePhysicsReady(pending.vehicle))
            .. " lastError=" .. tostring(pending.lastError))
        pendingRaidVehicleRelocation = nil
        releasePendingRaidVehicleFadeIns(pending)
    end
end

local function syncOutcomeHealth(player)
    if player == nil or not (isClient and isClient()) then return end
    pcall(function() sendPlayerStat(player, CharacterStat.ZOMBIE_INFECTION) end)
    pcall(function() sendPlayerStat(player, CharacterStat.ZOMBIE_FEVER) end)
end

local function clearDeathRescuePresentation(player)
    if player == nil then return end
    local playerNum = player:getPlayerNum()

    -- Drag-down can run IsoPlayer.OnDeath after health has already been
    -- restored. Undo the client-only pieces installed by that event so the
    -- rescued character keeps a normal living-player UI and music state.
    local panel = ISPostDeathUI and ISPostDeathUI.instance
        and ISPostDeathUI.instance[playerNum] or nil
    if panel ~= nil then
        pcall(function() panel:removeFromUIManager() end)
        local joypadData = JoypadState and JoypadState.players
            and JoypadState.players[playerNum + 1] or nil
        if joypadData and joypadData.focus == panel then joypadData.focus = nil end
        ISPostDeathUI.instance[playerNum] = nil
    end

    local playerData = nil
    if getPlayerData then pcall(function() playerData = getPlayerData(playerNum) end) end
    if playerData == nil and createPlayerData then
        pcall(function() createPlayerData(playerNum) end)
    end

    if getSoundManager then
        local manager = getSoundManager()
        if manager ~= nil then
            pcall(function()
                manager:StopMusic()
                manager:setMusicState("InGame")
            end)
        end
    end
end

local function refreshDeathRescueEquipment(player)
    if player == nil then return end
    local playerNum = player:getPlayerNum()

    -- The server has just re-sent its authoritative clothing and item state.
    -- Discard inventory-page caches left behind by the death UI and rebuild the
    -- owning player's model so retained clothing is immediately usable again.
    pcall(function() player:getInventory():setDrawDirty(true) end)
    pcall(function() player:resetModelNextFrame() end)
    if ISInventoryPage and ISInventoryPage.dirtyUI then
        pcall(function() ISInventoryPage.dirtyUI() end)
    end

    local playerData = nil
    if getPlayerData then pcall(function() playerData = getPlayerData(playerNum) end) end
    if playerData then
        if playerData.playerInventory then
            pcall(function() playerData.playerInventory:refreshBackpacks() end)
        end
        if playerData.lootInventory then
            pcall(function() playerData.lootInventory:refreshBackpacks() end)
        end
    end
end

local function onServerCommand(module, command, args, targetPlayer)
    if module ~= Config.COMMAND_MODULE then return end
    args = args or {}
    targetPlayer = addressedLocalPlayer(args, targetPlayer)
    if command == "State" then receiveState(args, targetPlayer); return end
    if command == "FadeOut" then
        if args.vehicleId ~= nil then prepareRaidVehicleRelocation(args, targetPlayer) end
        fade(true, args, targetPlayer)
        return
    end
    if command == "FadeIn" then
        local rebuiltSeat = pendingRebuiltRaidVehicleSeats[playerNumber(targetPlayer)]
        if rebuiltSeat ~= nil and rebuiltSeat.player == targetPlayer then
            rebuiltSeat.queuedFadeIn = args
            return
        end
        if pendingRelocationContainsPlayer(targetPlayer) then
            pendingRaidVehicleRelocation.queuedFadeIns[playerNumber(targetPlayer)] = {
                args = args,
                player = targetPlayer,
            }
            return
        end
        if launchContainsPlayer(targetPlayer) then
            queueRaidVehicleLaunchFadeIn(targetPlayer, args)
            if raidVehicleLaunch.phase ~= "DRIVING" then
                tryBeginRaidVehicleLaunch(Util.nowMs())
            end
            if raidVehicleLaunch ~= nil then return end
        end
        fade(false, args, targetPlayer)
        return
    end
    if command == "Teleport" then teleport(args, targetPlayer); return end
    if command == "RelocateRaidVehicle" then
        Client.relocateRaidVehicle(args, targetPlayer)
        return
    end
    if command == "SeatRaidVehicle" then
        queueRebuiltRaidVehicleSeat(args, targetPlayer)
        return
    end
    if command == "ExtractionShove" then
        Client.applyExtractionShove(args)
        return
    end
    if command == "GaragePrepareVehicleRemoval" then
        prepareGarageVehicleRemoval(args, targetPlayer)
        return
    end
    if command == "CampaignEpilogue" then
        stopExtractionMusicOverride()
        if ExtractionMode.playCampaignEpilogue then ExtractionMode.playCampaignEpilogue() end
        return
    end
    if command == "BoardingProtection" then
        setBoardingProtection(targetPlayer or (getPlayer and getPlayer()), args)
        return
    end
    if command == "ApplyExtractionHealing" then
        local player = targetPlayer or (getPlayer and getPlayer())
        if player then
            RaidOutcomes.applyExtractionHealing(player, args.mode)
            syncOutcomeHealth(player)
        end
        return
    end
    if command == "ApplyDeathRescueHealth" then
        local player = targetPlayer or (getPlayer and getPlayer())
        if player then
            local runtime = runtimeFor(player)
            runtime.deathRescueCinematic = false
            runtime.deathRescueCinematicDeadline = 0
            runtime.deathRescueFinalizeSent = true
            runtime.deathRescueFinalAnimationStartedAt = 0
            runtime.deathRescueGraceUntil = Util.nowMs() + 5000
            RaidOutcomes.releaseDeathRescueState(player)
            syncOutcomeHealth(player)
            clearDeathRescuePresentation(player)
        end
        return
    end
    if command == "RefreshDeathRescueEquipment" then
        refreshDeathRescueEquipment(targetPlayer or (getPlayer and getPlayer()))
        return
    end
    if command == "SyncDeathRescueObserver" then
        local observed = playerByUsername(args.username)
        local localPlayer = false
        if observed then pcall(function() localPlayer = observed:isLocalPlayer() end) end
        if observed and not localPlayer then
            -- Remote IsoPlayers do not receive the rescued owner's local health
            -- and animation reset. Clear the terminal state on this observing
            -- client, then apply the authority's exact arrival point when sent.
            RaidOutcomes.releaseDeathRescueState(observed)
            if tonumber(args.x) ~= nil and tonumber(args.y) ~= nil then
                pcall(function()
                    observed:teleportTo(tonumber(args.x), tonumber(args.y), tonumber(args.z) or 0)
                end)
            end
        end
        return
    end
    if command == "DeathRescueVoice" then
        playDeathRescueVoice(args.username)
        return
    end
    if command == "DeathRescueRejected" then
        local player = targetPlayer or (getPlayer and getPlayer())
        local runtime = runtimeFor(player)
        runtime.deathRescueRequested = false
        runtime.deathRescueGraceUntil = 0
        runtime.deathRescueCinematic = false
        runtime.deathRescueCinematicDeadline = 0
        runtime.deathRescueFinalizeSent = false
        runtime.deathRescueFinalAnimationStartedAt = 0
        pcall(function() if player then player:setAvoidDamage(false) end end)
        if localBoardingProtection(player) ~= nil then
            setBoardingProtection(player, { enabled = false })
        end
        fade(false, args, targetPlayer)
        return
    end
    if command == "InfectionCured" or command == "InfectionPrevented" then
        local player = targetPlayer or (getPlayer and getPlayer())
        if player then
            Infection.cure(player)
            pcall(function() sendPlayerStat(player, CharacterStat.ZOMBIE_INFECTION) end)
            pcall(function() sendPlayerStat(player, CharacterStat.ZOMBIE_FEVER) end)
        end
        return
    end
    if command == "Announcement" then
        local hasAudioCue = args.audioCue ~= nil and tostring(args.audioCue) ~= ""
        if hasAudioCue then playAudioCue(args.audioCue, targetPlayer) end
        announce(Localization.resolveMessage(args), false, hasAudioCue,
            args.extendedHalo == true, targetPlayer)
        return
    end
    if command == "LocationChecked" then
        showLocationChecked(Localization.resolveMessage(args), targetPlayer)
        return
    end
    if command == "Error" then
        announce(Localization.resolveMessage(args), true, nil, nil, targetPlayer)
        return
    end
end

Client.receiveServerCommand = onServerCommand

local function requestState(playerNum, player)
    player = player or getSpecificPlayer(playerNum or 0)
    runtimeFor(player or playerNum or 0).lastStateRequestAt = Util.nowMs()
    ExtractionMode.createHUD()
    Client.sendCommand(player, "RequestState", {})
end

local function atExtractionSite(player)
    for _, site in ipairs(Client.stateFor(player).extractionSites or {}) do
        if Util.playerNear(player, site, tonumber(site.radius) or 12) then return site end
    end
    return nil
end

Client.atExtractionSite = atExtractionSite

local function onWorldContext(playerNum, context, worldObjects, test)
    if test then return end
    local player = getSpecificPlayer(playerNum)
    if player == nil then return end
    local data = Client.stateFor(player)
    if data.state == Config.STATE_HIDEOUT then
        context:addOption(Localization.get("ContextMenu_ExtractionMode_HideoutUpgrades",
            "Hideout Upgrades..."), nil, function()
            ExtractionMode.openUpgradePanel(playerNum)
        end)
        context:addOption(Localization.get("ContextMenu_ExtractionMode_Contacts", "Contacts..."), nil, function()
            ExtractionMode.openQuestPanel(playerNum)
        end)
        context:addOption(Localization.get("ContextMenu_ExtractionMode_ChooseDestination",
            "Choose Raid Destination..."), nil, function()
            ExtractionMode.openTownPicker(playerNum)
        end)
        if data.selectedTownKey then
            local readyOption = context:addOption(data.canJoinFactionRaid == true
                and Localization.get("IGUI_ExtractionMode_JoinRaid", "Join Raid")
                or (data.vehicleInsertionActive == true and data.vehicleInsertionHasDriver ~= true
                    and Localization.get("IGUI_ExtractionMode_DriverRequired", "Driver Required")
                or (data.canReady == false
                    and Localization.get("IGUI_ExtractionMode_EnterInsertionVehicle", "Enter Insertion Vehicle")
                or (data.selfReady
                    and Localization.get("ContextMenu_ExtractionMode_CancelReady", "Cancel Raid Ready")
                    or Localization.get("ContextMenu_ExtractionMode_Ready", "Ready for Raid")))), nil, function()
                if data.canJoinFactionRaid == true then
                    Client.sendCommand(player, "JoinFactionRaid", {})
                elseif data.canReady ~= false then
                    Client.sendCommand(player, "SetReady", { ready = data.selfReady ~= true })
                end
            end)
            if data.canJoinFactionRaid ~= true and data.canReady == false then
                readyOption.notAvailable = true
            end
            if data.canOptOut == true and data.canJoinFactionRaid ~= true then
                context:addOption(data.selfOptedOut == true
                    and Localization.get("IGUI_ExtractionMode_CancelOptOut", "Cancel Opt Out")
                    or Localization.get("IGUI_ExtractionMode_OptOut", "Opt Out"), nil, function()
                    Client.sendCommand(player, "SetOptOut", { optedOut = data.selfOptedOut ~= true })
                end)
            end
        end
    elseif data.state == Config.STATE_COUNTDOWN then
        local readyOption = context:addOption(data.vehicleInsertionActive == true
            and data.vehicleInsertionHasDriver ~= true
            and Localization.get("IGUI_ExtractionMode_DriverRequired", "Driver Required")
            or (data.canReady == false
            and Localization.get("IGUI_ExtractionMode_EnterInsertionVehicle", "Enter Insertion Vehicle")
            or (data.selfReady
            and Localization.get("ContextMenu_ExtractionMode_CancelReady", "Cancel Raid Ready")
            or Localization.get("ContextMenu_ExtractionMode_Ready", "Ready for Raid"))), nil, function()
            if data.canReady ~= false then
                Client.sendCommand(player, "SetReady", { ready = data.selfReady ~= true })
            end
        end)
        if data.canReady == false then readyOption.notAvailable = true end
        if data.canOptOut == true then
            context:addOption(data.selfOptedOut == true
                and Localization.get("IGUI_ExtractionMode_CancelOptOut", "Cancel Opt Out")
                or Localization.get("IGUI_ExtractionMode_OptOut", "Opt Out"), nil, function()
                Client.sendCommand(player, "SetOptOut", { optedOut = data.selfOptedOut ~= true })
            end)
        end
    elseif data.state == Config.STATE_RAID and data.isParticipant == true then
        if data.selectedTownKey == "grand_ohio_mall" and Client.campaignQuestActive(data)
            and Client.atCampaignHandoff(player, data) then
            local option = context:addOption(data.campaignHandoffActive
                and Localization.get("ContextMenu_ExtractionMode_VaccineHelicopterInbound",
                    "Vaccine Helicopter Inbound (%1s)", tostring(data.campaignHandoffSeconds or 0))
                or Localization.get("ContextMenu_ExtractionMode_SignalVaccineHelicopter",
                    "Signal Vaccine Helicopter"), nil, function()
                Client.sendCommand(player, "StartCampaignHandoff", {})
            end)
            if data.campaignHandoffActive or Client.findVaccineSample(player) == nil then
                option.notAvailable = true
            end
            if Client.findVaccineSample(player) == nil then
                option.toolTip = ISToolTip:new()
                option.toolTip.description = Localization.get(
                    "IGUI_ExtractionMode_Tooltip_VaccineSampleRequired",
                    "Carry the Vaccine Sample onto the rooftop helipad to signal the pilot.")
            end
        end
        local site = atExtractionSite(player)
        if site then
            local square = player:getCurrentSquare()
            local outside = square and square:isOutside()
            local option
            if Client.flareEquipped(player) and outside then
                option = context:addOption(Localization.get("ContextMenu_ExtractionMode_FlareReady",
                    "Flare Ready: Aim and Fire (E%1)", tostring(site.id)), nil, function()
                    announce(Localization.get("IGUI_ExtractionMode_FireFlareHint",
                        "Hold Aim, then press Attack to fire the extraction flare."), false)
                end)
            else
                option = context:addOption(Localization.get("ContextMenu_ExtractionMode_EquipFlare",
                    "Equip Extraction Flare Gun (E%1)", tostring(site.id)), nil, function()
                    Client.equipFlare(player)
                end)
            end
            if Client.findFlare(player) == nil then
                option.notAvailable = true
                option.toolTip = ISToolTip:new()
                option.toolTip.description = Localization.get("IGUI_ExtractionMode_Tooltip_FlareRequired",
                    "An extraction flare gun is required.")
            elseif not outside then
                option.notAvailable = true
                option.toolTip = ISToolTip:new()
                option.toolTip.description = Localization.get("IGUI_ExtractionMode_Tooltip_FlareOutdoors",
                    "Move outdoors so the helicopter can see the flare.")
            end
        end
    elseif data.state == Config.STATE_BOARDING and data.isParticipant == true then
        local rope = data.extractionRope
        if rope and Util.playerNear(player, rope, tonumber(rope.radius) or 3) then
            local inVehicle = player:getVehicle() ~= nil
            local option = context:addOption(inVehicle
                and Localization.get("IGUI_ExtractionMode_ExitVehicleToBoard", "Exit Vehicle to Board")
                or (data.boardingPendingSelf
                    and Localization.get("ContextMenu_ExtractionMode_Boarding", "Boarding Extraction Helicopter...")
                    or Localization.get("ContextMenu_ExtractionMode_Board", "Board Extraction Helicopter")), nil, function()
                Client.boardExtraction(player)
            end)
            if data.boardingPendingSelf or inVehicle then option.notAvailable = true end
            if inVehicle then
                option.toolTip = ISToolTip:new()
                option.toolTip.description = Localization.get("IGUI_ExtractionMode_Tooltip_BoardVehicle",
                    "Exit the vehicle before boarding the extraction helicopter.")
            end
        end
    end
end

local function cureFromContextItems(items)
    for _, value in ipairs(items or {}) do
        if instanceof(value, "InventoryItem") then
            if value:getFullType() == Config.INFECTION_CURE_TYPE then return value end
        elseif value.items then
            for _, item in ipairs(value.items) do
                if item:getFullType() == Config.INFECTION_CURE_TYPE then return item end
            end
        end
    end
    return nil
end

local function onInventoryContext(playerNum, context, items)
    local item = cureFromContextItems(items)
    if item == nil then return end
    local player = getSpecificPlayer(playerNum)
    if player == nil then return end
    local option = context:addOption(Localization.get("ContextMenu_ExtractionMode_AdministerCure",
        "Administer Experimental Infection Cure"), nil, function()
        Client.useInfectionCure(player, item)
    end)
    option.toolTip = ISToolTip:new()
    option.toolTip.description = Localization.get("IGUI_ExtractionMode_Tooltip_AdministerCure",
        "Completely clears the Knox infection. Ordinary wound infections are unaffected.")
end

local function signalFlareShot(player, weapon, source)
    if player == nil or weapon == nil or weapon:getFullType() ~= Config.FLARE_FULL_TYPE then return end
    local playerNum = -1
    pcall(function() playerNum = tonumber(player:getPlayerNum()) or -1 end)
    local localPlayer = playerNum >= 0
    if not localPlayer then pcall(function() localPlayer = player:isLocalPlayer() == true end) end
    if not localPlayer then return end

    local now = Util.nowMs()
    local key = playerNum >= 0 and tostring(playerNum) or player
    local tracking = flareTracking[key]
    if tracking == nil then
        tracking = { lastSignalAt = -10000 }
        flareTracking[key] = tracking
    end
    if tracking.lastSignaled == weapon and now - tracking.lastSignalAt < 1500 then return end
    tracking.lastSignaled = weapon
    tracking.lastSignalAt = now
    Client.sendCommand(player, "FireFlare", { source = source or "weapon-event" })
end

local function onWeaponFired(player, weapon)
    signalFlareShot(player, weapon, "swing-hit-point")
end

local function onPlayerAttackFinished(player, weapon)
    signalFlareShot(player, weapon, "attack-finished")
end

local function detectConsumedFlareRound(player)
    local playerNum = -1
    pcall(function() playerNum = tonumber(player:getPlayerNum()) or -1 end)
    local key = playerNum >= 0 and tostring(playerNum) or player
    local tracking = flareTracking[key]
    if tracking == nil then
        tracking = { lastSignalAt = -10000 }
        flareTracking[key] = tracking
    end
    local weapon = player and player:getPrimaryHandItem()
    if weapon == nil or weapon:getFullType() ~= Config.FLARE_FULL_TYPE then
        tracking.observed = nil
        tracking.observedAmmo = nil
        return
    end

    -- Refresh while equipped so a live sandbox change and manually equipped
    -- flare guns both have the correct zombie-attention radius before firing.
    Config.applyExtractionFlareNoise(weapon)

    local ammo = tonumber(weapon:getCurrentAmmoCount()) or 0
    if tracking.observed == weapon and tracking.observedAmmo ~= nil
        and ammo < tracking.observedAmmo then
        signalFlareShot(player, weapon, "ammo-consumed")
    end
    tracking.observed = weapon
    tracking.observedAmmo = ammo
end

local function refreshLocalHideoutPower(player)
    local now = Util.nowMs()
    local state = Client.stateFor(player)
    local hideout = state.hideout
    if hideout == nil then return end
    local cellBounds = Config.hideoutCellBounds()
    if player:getX() < cellBounds.minX or player:getX() >= cellBounds.maxXExclusive
        or player:getY() < cellBounds.minY or player:getY() >= cellBounds.maxYExclusive then return end
    -- An out-of-cell split-screen player must not consume the shared refresh
    -- interval before another local player inside the hideout gets this update.
    if now - lastPowerRefresh < 2000 then return end
    lastPowerRefresh = now
    local generator = state.generator or {}
    local powered = generator.running == true and (tonumber(generator.fuel) or 0) > 0.0001
    HideoutUtilities.ensureGridIsolation(powered)

    local hideoutRadius = tonumber(hideout.radius) or 14
    local radius = math.min(50, math.max(1, math.floor(hideoutRadius)))
    local minimumX = math.floor(hideout.x) - radius
    local maximumX = math.floor(hideout.x) + radius
    local minimumY = math.floor(hideout.y) - radius
    local maximumY = math.floor(hideout.y) + radius
    local anchorZ = math.floor(tonumber(hideout.z) or 0)
    local minimumZ = anchorZ
    local maximumZ = anchorZ
    local cell = getCell()
    local anchor = cell:getGridSquare(math.floor(hideout.x), math.floor(hideout.y), anchorZ)
    local hideoutBuilding = anchor and anchor:getBuilding()
    pcall(function()
        minimumZ = math.max(-32, math.min(anchorZ, tonumber(cell:getMinZ()) or anchorZ))
        maximumZ = math.min(31, math.max(anchorZ, tonumber(cell:getMaxZ()) or anchorZ))
        local definition = hideoutBuilding and hideoutBuilding:getDef()
        if definition then
            minimumX = math.max(cellBounds.minX, definition:getX())
            maximumX = math.min(cellBounds.maxXExclusive - 1, definition:getX2())
            minimumY = math.max(cellBounds.minY, definition:getY())
            maximumY = math.min(cellBounds.maxYExclusive - 1, definition:getY2())
        end
    end)
    local visitedChunks = {}
    HideoutUtilities.setVirtualPowerArea(cell, hideout, powered, visitedChunks,
        minimumZ, maximumZ)
    for x = minimumX, maximumX do
        for y = minimumY, maximumY do
            for z = minimumZ, maximumZ do
                local square = cell:getGridSquare(x, y, z)
                if square and (hideoutBuilding == nil or square:getBuilding() == hideoutBuilding) then
                    HideoutUtilities.setVirtualPower(square, powered, visitedChunks)
                    square:setHaveElectricity(powered)
                    -- Sink options are derived from the client's local fluid
                    -- container, so refresh plumbing here as well as on the
                    -- authority. The authority remains responsible for syncing.
                    HideoutUtilities.setPipedWaterAvailable(square, powered, false)
                    local lightingInstalled = Upgrades.isInstalled(state.upgrades, "lighting")
                    -- Power loss still disables the authored fixtures. While the
                    -- grid is powered, only turn them all on for the one-time
                    -- lighting activation/power-restoration pass; normal refreshes
                    -- must preserve switches that players deliberately turned off.
                    local objects = square:getObjects()
                    if objects then
                        for index = 0, objects:size() - 1 do
                            local object = objects:get(index)
                            if object and instanceof(object, "IsoLightSwitch")
                                and HideoutLighting.upgradeControlsSwitch(object) then
                                if not powered or not lightingInstalled then
                                    HideoutLighting.forceSwitchState(object, false)
                                elseif lightingActivationPending then
                                    HideoutLighting.forceSwitchState(object, true)
                                end
                            end
                        end
                    end
                end
            end
        end
    end
    lightingActivationPending = false
end

local function refreshLocalHideoutInfection(player)
    if Infection.clampInHideout(player) and isClient and isClient() then
        pcall(function() sendPlayerStat(player, CharacterStat.ZOMBIE_INFECTION) end)
    end
end

local function raidDeathRescueAvailable(player)
    local data = Client.stateFor(player)
    local active = data.state == Config.STATE_RAID or data.state == Config.STATE_EXTRACTING
        or data.state == Config.STATE_BOARDING
    return player ~= nil and active and data.isParticipant == true
        and data.deathRescuePendingSelf ~= true and RaidOutcomes.deathHandlingMode() > 1
end

local function damageWouldBeLethal(player, damageType, damage)
    local dragDown = false
    pcall(function() dragDown = player:isDeathDragDown() end)
    if dragDown then return true, false, true end

    local bodyDamage = player and player:getBodyDamage()
    local overallHealth = nil
    if bodyDamage then
        pcall(function() overallHealth = tonumber(bodyDamage:getOverallBodyHealth()) end)
    end
    local characterHealth = nil
    pcall(function() characterHealth = tonumber(player:getHealth()) end)
    if overallHealth ~= nil and overallHealth <= 1 then return true, false, false end
    if characterHealth ~= nil and characterHealth <= 0.01 then return true, false, false end
    local incoming = tonumber(damage)
    local preDamageWeaponHit = tostring(damageType or "") == "WEAPONHIT"
        and overallHealth ~= nil and incoming ~= nil and incoming > 0
        and overallHealth - incoming <= 0
    return preDamageWeaponHit, preDamageWeaponHit, false
end

local function requestDeathRescue(player, damageType, damage)
    local runtime = runtimeFor(player)
    if runtime.deathRescueRequested or not raidDeathRescueAvailable(player) then return false end
    local lethal, preventIncoming, cinematic = damageWouldBeLethal(player, damageType, damage)
    if not lethal then return false end
    runtime.deathRescueRequested = true
    runtime.deathRescueCinematic = cinematic == true
    runtime.deathRescueFinalizeSent = false
    runtime.deathRescueFinalAnimationStartedAt = 0
    if runtime.deathRescueCinematic then
        -- Leave the engine's grapple state completely untouched. Finalization
        -- waits until half of the EndDeath animation has played, then interrupts
        -- it before its terminal Death event.
        runtime.deathRescueGraceUntil = 0
        runtime.deathRescueCinematicDeadline = Util.nowMs() + 6000
    else
        runtime.deathRescueGraceUntil = Util.nowMs() + 5000
        -- Non-drag-down deaths still need to be canceled immediately.
        RaidOutcomes.releaseDeathRescueState(player)
        syncOutcomeHealth(player)
    end
    if preventIncoming then pcall(function() player:setAvoidDamage(true) end) end
    Client.sendCommand(player, "RescueFromDeath", {
        damageType = tostring(damageType or "unknown"),
        damage = tonumber(damage) or 0,
        cinematic = runtime.deathRescueCinematic,
        deathEvent = tostring(damageType or "") == "death-event",
    })
    if runtime.deathRescueRequested and not runtime.deathRescueCinematic then
        setBoardingProtection(player, { enabled = true, forceInvisible = true })
        fade(true, {
            seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
            leaveVehicle = true,
        })
    end
    return true
end

local function dragDownFinalAnimationReady(player, now)
    local runtime = runtimeFor(player)
    local actionState = ""
    local hitReaction = ""
    pcall(function() actionState = tostring(player:getActionStateName() or "") end)
    pcall(function() hitReaction = tostring(player:getHitReaction() or "") end)
    actionState = string.lower(actionState)
    hitReaction = string.lower(hitReaction)
    local finalState = string.find(actionState, "enddeath", 1, true) ~= nil
        or hitReaction == "enddeath"
    if not finalState then
        runtime.deathRescueFinalAnimationStartedAt = 0
        return false
    end
    if runtime.deathRescueFinalAnimationStartedAt <= 0 then
        runtime.deathRescueFinalAnimationStartedAt = now
    end

    return now - runtime.deathRescueFinalAnimationStartedAt >= DRAG_DOWN_HALF_ANIMATION_MS
end

local function requestDeathRescueFinalization(player)
    local runtime = runtimeFor(player)
    if not runtime.deathRescueCinematic or runtime.deathRescueFinalizeSent then return end
    runtime.deathRescueFinalizeSent = true
    -- Begin obscuring the local state change before the authority clears the
    -- terminal animation, especially across a multiplayer round trip.
    fade(true, {
        seconds = tonumber(Config.value("TransitFadeSeconds")) or 1,
        leaveVehicle = true,
    })
    -- Cancel the terminal state locally at the handoff instead of waiting for a
    -- multiplayer round trip. The server remains authoritative for equipment,
    -- extraction state, protection, and the eventual hideout teleport.
    runtime.deathRescueCinematic = false
    runtime.deathRescueCinematicDeadline = 0
    runtime.deathRescueFinalAnimationStartedAt = 0
    runtime.deathRescueGraceUntil = Util.nowMs() + 5000
    RaidOutcomes.releaseDeathRescueState(player)
    syncOutcomeHealth(player)
    clearDeathRescuePresentation(player)
    setBoardingProtection(player, { enabled = true, forceInvisible = true })
    Client.sendCommand(player, "FinalizeDeathRescue", {})
end

local function onPlayerGetDamage(character, damageType, damage)
    if character == nil or not instanceof(character, "IsoPlayer")
        or not character:isLocalPlayer() then return end
    if localBoardingProtection(character) ~= nil then
        reinforceLocalTransitionProtection(character)
        return
    end
    requestDeathRescue(character, damageType, damage)
end

local function onPlayerDeath(player)
    if player == nil or not player:isLocalPlayer() then return end
    local runtime = runtimeFor(player)
    if not runtime.deathRescueRequested and not raidDeathRescueAvailable(player) then return end

    -- This is a last-resort recovery for instant engine death paths. Vanilla
    -- listeners run before this mod and may already have removed player UI.
    if not runtime.deathRescueRequested then requestDeathRescue(player, "death-event", 0) end
    if not runtime.deathRescueRequested then return end
    if runtime.deathRescueCinematic then requestDeathRescueFinalization(player) end
    runtime.deathRescueCinematic = false
    runtime.deathRescueCinematicDeadline = 0
    runtime.deathRescueFinalAnimationStartedAt = 0
    runtime.deathRescueGraceUntil = Util.nowMs() + 5000
    RaidOutcomes.releaseDeathRescueState(player)
    syncOutcomeHealth(player)
    clearDeathRescuePresentation(player)
end

local function updateGarageActivity(player, now)
    local active = Client.stateFor(0).activeHideoutVehicle
    if type(active) ~= "table" or active.vehicleId == nil then return end
    local vehicleId = tostring(active.vehicleId)
    local interacting = false
    local currentVehicle = player:getVehicle()
    if currentVehicle ~= nil and tostring(currentVehicle:getId()) == vehicleId then
        interacting = true
    end
    if not interacting and getPlayerMechanicsUI ~= nil then
        local mechanics = nil
        pcall(function() mechanics = getPlayerMechanicsUI(player:getPlayerNum()) end)
        if mechanics ~= nil and mechanics.vehicle ~= nil then
            pcall(function()
                interacting = mechanics:isVisible()
                    and tostring(mechanics.vehicle:getId()) == vehicleId
            end)
        end
    end
    if not interacting and getPlayerLoot ~= nil then
        local loot = nil
        pcall(function() loot = getPlayerLoot(player:getPlayerNum()) end)
        if loot ~= nil and loot.inventoryPane ~= nil then
            local container = loot.inventoryPane.inventory
            local lootVehicle = nil
            local checked = {}
            while container ~= nil and checked[container] ~= true and lootVehicle == nil do
                checked[container] = true
                pcall(function() lootVehicle = container:getVehicle() end)
                if lootVehicle == nil then
                    local containingItem = nil
                    pcall(function() containingItem = container:getContainingItem() end)
                    container = nil
                    if containingItem ~= nil then
                        pcall(function() container = containingItem:getContainer() end)
                    end
                end
            end
            pcall(function()
                interacting = lootVehicle ~= nil and not loot.isCollapsed
                    and tostring(lootVehicle:getId()) == vehicleId
            end)
        end
    end
    local playerNumber = player:getPlayerNum()
    if interacting and now - (tonumber(lastGarageActivityAt[playerNumber]) or 0) >= 2000 then
        lastGarageActivityAt[playerNumber] = now
        Client.sendCommand(player, "GarageActivity", { vehicleId = vehicleId })
    end
end

local function loadedVehicleById(vehicleId)
    return loadedRaidVehicleById(vehicleId)
end

local function stopGarageVehiclePulse()
    local pulse = garageVehiclePulse
    if pulse.vehicle ~= nil then
        pcall(function() pulse.vehicle:setHeadlightsOn(pulse.headlightsOn == true) end)
        pcall(function() pulse.vehicle:setStoplightsOn(pulse.stoplightsOn == true) end)
        for playerIndex = 0, 3 do
            local alpha = pulse.alpha[playerIndex]
            local targetAlpha = pulse.targetAlpha[playerIndex]
            if alpha ~= nil then
                pcall(function() pulse.vehicle:setAlpha(playerIndex, alpha) end)
            end
            if targetAlpha ~= nil then
                pcall(function() pulse.vehicle:setTargetAlpha(playerIndex, targetAlpha) end)
            end
        end
    end
    pulse.vehicle = nil
    pulse.vehicleId = nil
    pulse.headlightsOn = false
    pulse.stoplightsOn = false
    pulse.alpha = {}
    pulse.targetAlpha = {}
    pulse.phase = nil
end

local function updateGarageVehiclePulse(now)
    now = tonumber(now) or Util.nowMs()
    local pulse = garageVehiclePulse
    local active = Client.stateFor(0).activeHideoutVehicle
    local vehicleId = type(active) == "table" and active.storing == true
        and active.vehicleId ~= nil and tostring(active.vehicleId) or nil
    if vehicleId == nil and pulse.requestedVehicleId ~= nil and now < pulse.requestedUntil then
        vehicleId = tostring(pulse.requestedVehicleId)
    end
    if vehicleId == nil then
        pulse.requestedVehicleId = nil
        pulse.requestedUntil = 0
        stopGarageVehiclePulse()
        return
    end
    if pulse.vehicleId ~= vehicleId or pulse.vehicle == nil then
        stopGarageVehiclePulse()
        local vehicle = loadedVehicleById(vehicleId)
        if vehicle == nil then return end
        pulse.vehicle = vehicle
        pulse.vehicleId = vehicleId
        pcall(function() pulse.headlightsOn = vehicle:getHeadlightsOn() == true end)
        pcall(function() pulse.stoplightsOn = vehicle:getStoplightsOn() == true end)
        for playerIndex = 0, 3 do
            pcall(function() pulse.alpha[playerIndex] = vehicle:getAlpha(playerIndex) end)
            pcall(function() pulse.targetAlpha[playerIndex] = vehicle:getTargetAlpha(playerIndex) end)
        end
    end
    local phase = math.floor(now / 220) % 2 == 0
    if pulse.phase ~= phase then
        pulse.phase = phase
        pcall(function() pulse.vehicle:setHeadlightsOn(phase) end)
        pcall(function() pulse.vehicle:setStoplightsOn(phase) end)
    end
    local alpha = phase and 1.0 or 0.22
    -- Vehicle rendering rapidly approaches its normal target alpha. Reapply the
    -- phase every frame so the transparent half of the pulse remains visible.
    for playerIndex = 0, 3 do
        pcall(function() pulse.vehicle:setAlphaAndTarget(playerIndex, alpha) end)
    end
end

local function onPlayerUpdate(player)
    if player == nil or not player:isLocalPlayer() then return end
    -- OnCreatePlayer can fire before a multiplayer connection is ready to
    -- forward client commands. Keep requesting the initial authoritative
    -- snapshot until one arrives; otherwise the HUD remains correctly hidden
    -- behind a nil state forever.
    local now = Util.nowMs()
    local state = Client.stateFor(player)
    local runtime = runtimeFor(player)
    processPendingRaidVehicleRelocation(player, now)
    processRebuiltRaidVehicleSeat(player, now)
    processRaidVehicleLaunch(player, now)
    reinforceLocalTransitionProtection(player)
    updateGarageVehiclePulse(now)
    updateGarageActivity(player, now)
    if runtime.groundExtractionVehicle ~= nil then
        if player:getVehicle() == runtime.groundExtractionVehicle then
            pcall(function() runtime.groundExtractionVehicle:setForceBrake() end)
        else
            runtime.groundExtractionVehicle = nil
        end
    end
    local activeHideoutVehicle = Client.stateFor(0).activeHideoutVehicle
    local currentVehicle = player:getVehicle()
    local insideHideoutCell = false
    if currentVehicle ~= nil then
        local bounds = Config.hideoutCellBounds()
        local vehicleX, vehicleY = currentVehicle:getX(), currentVehicle:getY()
        insideHideoutCell = vehicleX >= bounds.minX and vehicleX < bounds.maxXExclusive
            and vehicleY >= bounds.minY and vehicleY < bounds.maxYExclusive
    end
    local hideoutLifecycle = state.state == Config.STATE_HIDEOUT
        or state.state == Config.STATE_COUNTDOWN
    if activeHideoutVehicle ~= nil and currentVehicle ~= nil
        and tostring(currentVehicle:getId()) == tostring(activeHideoutVehicle.vehicleId)
        and hideoutLifecycle and insideHideoutCell then
        local lock = runtime.hideoutVehicleLock
        if lock == nil or lock.vehicle ~= currentVehicle then
            lock = { vehicle = currentVehicle, lastBrakeAt = 0 }
            runtime.hideoutVehicleLock = lock
        end
        lock.engineQuality = tonumber(activeHideoutVehicle.engineQuality) or 0
        lock.engineLoudness = tonumber(activeHideoutVehicle.engineLoudness) or 0
        lock.enginePower = tonumber(activeHideoutVehicle.enginePower) or 0
        -- Keep normal suspension physics alive, but give the locally simulated
        -- drivetrain no force. This prevents throttle from waking a static body
        -- and pulling the chassis down through the garage floor.
        pcall(function()
            if currentVehicle:isPhysicsActive() ~= true then
                currentVehicle:setPhysicsActive(true, true)
            end
            -- setEngineFeature can restart engine audio/transmission evaluation;
            -- only invoke it when power is not already locked.
            if tonumber(currentVehicle:getEnginePower()) ~= 0 then
                currentVehicle:setEngineFeature(lock.engineQuality, lock.engineLoudness, 0)
            end
            -- Refresh the time-limited parking brake without rewriting vehicle
            -- control state sixty times per second.
            if now - (tonumber(lock.lastBrakeAt) or 0) >= 250 then
                lock.lastBrakeAt = now
                currentVehicle:setCurrentSteering(0)
                currentVehicle:setSpeedKmHour(0)
                if raidVehiclePhysicsController(currentVehicle) ~= nil then
                    currentVehicle:setClientForce(0)
                end
                currentVehicle:setBraking(true)
                currentVehicle:setForceBrake()
            end
        end)
    elseif runtime.hideoutVehicleLock ~= nil then
        local released = runtime.hideoutVehicleLock
        pcall(function()
            released.vehicle:setEngineFeature(released.engineQuality,
                released.engineLoudness, released.enginePower)
            if raidVehiclePhysicsController(released.vehicle) ~= nil then
                released.vehicle:setClientForce(0)
            end
            released.vehicle:setBraking(false)
            released.vehicle:setPhysicsActive(true, true)
        end)
        runtime.hideoutVehicleLock = nil
    end
    if now - runtime.lastRemnantsPlacementCheck >= 1000 then
        runtime.lastRemnantsPlacementCheck = now
        ProjectRemnantsIntegration.ensureActiveSquadSafeInHideout(Config.hideout(), player)
    end
    if state.state == nil and now - runtime.lastStateRequestAt >= 1000 then
        requestState(player:getPlayerNum(), player)
    end
    resolvePendingSafeLanding(player)
    TrueCompanionsIntegration.update(player)
    detectConsumedFlareRound(player)
    refreshLocalHideoutPower(player)
    refreshLocalHideoutInfection(player)
    HideoutBenefits.processLocalPlayer(player, state)
    if not runtime.deathRescueRequested then
        local dragDown = false
        pcall(function() dragDown = player:isDeathDragDown() end)
        if dragDown then requestDeathRescue(player, "drag-down", 0) end
    end
    -- Covers health drains which do not report a useful incoming damage amount,
    -- including the final tick of illness or infection damage.
    if runtime.deathRescueCinematic then
        if dragDownFinalAnimationReady(player, now) or now >= runtime.deathRescueCinematicDeadline then
            requestDeathRescueFinalization(player)
        end
    elseif runtime.deathRescueRequested or now < runtime.deathRescueGraceUntil then
        -- Some damage callbacks continue updating status values after Lua
        -- returns, and zombie death reactions can finish after the teleport.
        -- Keep clearing both until the short post-arrival grace expires.
        RaidOutcomes.restoreFullHealth(player)
    else
        runtime.deathRescueGraceUntil = 0
        requestDeathRescue(player, "health-check", 0)
    end

    -- A countdown should receive a server snapshot every second. If it does not,
    -- ask for the authoritative state so a dropped/failed update self-recovers.
    if (state.state == Config.STATE_COUNTDOWN or state.state == Config.STATE_TRANSIT)
        and now - runtime.lastStateReceivedAt >= 2500
        and now - runtime.lastStateRequestAt >= 2000 then
        requestState(nil, player)
    end
end

local function requestStateOnGameStart()
    local player = getPlayer and getPlayer()
    ExtractionMode.createHUD()
    if player then requestState(player:getPlayerNum(), player) end
end

Events.OnServerCommand.Add(onServerCommand)
Events.OnCreatePlayer.Add(requestState)
Events.OnGameStart.Add(requestStateOnGameStart)
Events.OnFillWorldObjectContextMenu.Add(onWorldContext)
Events.OnFillInventoryObjectContextMenu.Add(onInventoryContext)
Events.OnPlayerUpdate.Add(onPlayerUpdate)
Events.OnPlayerGetDamage.Add(onPlayerGetDamage)
Events.OnPlayerDeath.Add(onPlayerDeath)
Events.OnWeaponSwingHitPoint.Add(onWeaponFired)
Events.OnPlayerAttackFinished.Add(onPlayerAttackFinished)

ExtractionMode.Client = Client
return Client
