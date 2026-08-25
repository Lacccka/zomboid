--
-- True Companions - recruited companions don't block sleep
--
-- Vanilla refuses sleep whenever the engine counts any visible / chasing / very-close ZOMBIE
-- near the player (ISWorldObjectContextMenu.onSleepWalkToComplete). Our companions are
-- Bandit-flagged IsoZombies, so they trip that check and you "can't sleep" just because a
-- companion is standing next to you.
--
-- Those counts come from Java player-stats with no Lua setters, so we can't subtract our
-- companions from them. Instead we replace that one function with a faithful copy whose ONLY
-- change is the zombie test: a companion-aware scan that ignores recruited companions but
-- still blocks on real zombies AND hostile bandits.
--
-- NOTE: the body below is a verbatim copy of the B42 vanilla function with a single changed
-- line (the isZombies computation). Re-sync if PZ changes onSleepWalkToComplete.
--

BanditsNPC = BanditsNPC or {}

-- True if a REAL threat is a sleep-blocking presence near the player. FRIENDLY NPCs
-- are ignored (13 Jul widening): our Bandit-flagged companions AND neutrals
-- (recruited or non-hostile brain -- the same test as the panic damper), plus
-- HDX_Strangers' "Stranger"-flagged NPCs (recruited/master or non-hostile) -- users
-- run both mods together and neither mod's friendly NPC may block sleep. Wild
-- zombies and hostile NPCs of either mod still count. Missing brain = treated as a
-- real zombie (hostile-safe default). Runs once per sleep attempt (not per tick),
-- so a full zombie-list scan is fine.
function BanditsNPC.RealZombiesNear(playerObj)
    if not playerObj then return false end
    local cell = getCell()
    local zlist = cell and cell.getZombieList and cell:getZombieList()
    if not zlist then return false end
    local px, py, pz = playerObj:getX(), playerObj:getY(), playerObj:getZ()
    for i = 0, zlist:size() - 1 do
        local blocks = false
        pcall(function()
            local z = zlist:get(i)
            if not z or z:isDead() then return end
            if z:getVariableBoolean("Bandit") then
                local b = BanditBrain and BanditBrain.Get(z)
                if b and (b.recruited or not (b.hostile or b.hostileP)) then return end
            end
            if z:getVariableBoolean("Stranger") then
                local b = StrangerBrain and StrangerBrain.Get and StrangerBrain.Get(z)
                if b and (b.recruited or b.master or not (b.hostile or b.hostileP)) then return end
            end
            if math.abs(z:getZ() - pz) >= 0.5 then return end
            local dx, dy = z:getX() - px, z:getY() - py
            local dist = math.sqrt(dx * dx + dy * dy)
            if dist <= 6 or (dist <= 40 and playerObj:CanSee(z)) then blocks = true end
        end)
        if blocks then return true end
    end
    return false
end

-- The engine-side "fall asleep now" tail of vanilla onSleepWalkToComplete
-- (ISWorldObjectContextMenu.onSleepWalkToComplete, verbatim -- re-sync if PZ changes
-- it). CITED BY FUNCTION NAME, NOT LINE NUMBERS (v0.75.41): this said :1068-1104 and
-- the real range is :1076-1131 -- the old numbers began inside the PANIC check and
-- stopped 27 lines early, so anyone following them to re-sync would have diffed the
-- wrong region. Line numbers drift every time vanilla edits anything above the
-- function; the name does not. (The copied body itself is still current -- diffed
-- line by line, single intended change at the isZombies line below.)
-- Extracted so BOTH our patched gate below and the guaranteed "companions keep
-- watch" option further down share one copy. `player` is the player NUMBER.
function BanditsNPC.BeginSleep(player, playerObj, bed)
    ISTimedActionQueue.clear(playerObj)
    local sleepFor = ZombRand(playerObj:getStats():get(CharacterStat.FATIGUE) * 10, playerObj:getStats():get(CharacterStat.FATIGUE) * 13) + 1;
    local bedType = ISWorldObjectContextMenu.getBedQuality(playerObj, bed)
    if bedType == "goodBed" or bedType:contains("goodBedPillow") then
        sleepFor = sleepFor - 1;
    end
    if bedType == "badBed" or bedType:contains("badBedPillow") then
        sleepFor = sleepFor + 1;
    end
    if bedType == "floor" or bedType:contains("floorPillow") then
        sleepFor = sleepFor * 0.7;
    end
    if playerObj:hasTrait(CharacterTrait.INSOMNIAC) then
        sleepFor = sleepFor * 0.5;
    end
    if playerObj:hasTrait(CharacterTrait.NEEDS_LESS_SLEEP) then
        sleepFor = sleepFor * 0.75;
    end
    if playerObj:hasTrait(CharacterTrait.NEEDS_MORE_SLEEP) then
        sleepFor = sleepFor * 1.18;
    end
    if sleepFor > 16 then sleepFor = 16; end
    if sleepFor < 3 then sleepFor = 3; end
    local sleepHours = sleepFor + GameTime.getInstance():getTimeOfDay()
    if sleepHours >= 24 then
        sleepHours = sleepHours - 24
    end

    playerObj:setBed(bed);
    playerObj:setBedType(bedType);
    playerObj:setForceWakeUpTime(tonumber(sleepHours))
    playerObj:setAsleepTime(0.0)
    playerObj:setAsleep(true)

    if playerObj:getVehicle() then
        playerObj:playSound("VehicleGoToSleep")
    end

    if isClient() and getServerOptions():getBoolean("SleepAllowed") then
        UIManager.setFadeBeforeUI(player, true)
        UIManager.FadeOut(player, 1)
        if playerObj:getVehicle() then
            sendClientCommand(playerObj, "player", "onVehicleSleep", { id = playerObj:getOnlineID(), isAsleep = true })
        end
        return
    end

    getSleepingEvent():setPlayerFallAsleep(playerObj, sleepFor);
    UIManager.setFadeBeforeUI(playerObj:getPlayerNum(), true)
    UIManager.FadeOut(playerObj:getPlayerNum(), 1)

    if IsoPlayer.allPlayersAsleep() then
        UIManager.getSpeedControls():SetCurrentGameSpeed(3)
        save(true)
    end
end

-- ===== vehicle sleep (the radial menu gate) =====
-- Sleeping INSIDE a car is offered via the in-vehicle radial menu. Its Sleep slice is
-- hidden behind the engine zombie-stat counts, and a companion in/near the car trips
-- them ("It's not safe to sleep"). Earlier versions shipped a full REPLACEMENT copy of
-- the vanilla ISVehicleMenu.showRadialMenu, which erased every other mod's radial
-- slices depending on load order (RV Interiors' "enter interior" button, seatbelt mods,
-- More Car Features -- tester reports 5-7 Jul). Now we CHAIN: call whatever
-- showRadialMenu is currently live (vanilla or another mod's wrapper), and afterwards,
-- when Sleep was blocked ONLY by friendly companions, append a working Sleep slice.
-- The dead "not safe" slice stays visible next to it -- cosmetic, but no other mod's
-- menu entries are ever touched again.
if ISVehicleMenu and ISVehicleMenu.showRadialMenu and not BanditsNPC._vehSleepPatched then
    BanditsNPC._vehSleepPatched = true
    local origShowRadialMenu = ISVehicleMenu.showRadialMenu

    -- vanilla's own sleep preconditions (ISVehicleMenu.showRadialMenu, the SleepAllowed
    -- block) minus the zombie
    -- check: only add our slice when the zombie count was the ONE thing blocking.
    -- Every API call here appears verbatim in that vanilla body.
    local function sleepWouldBeOffered(playerObj, vehicle)
        if isClient() and not getServerOptions():getBoolean("SleepAllowed") then return false end
        local sleepNeeded = not isClient() or getServerOptions():getBoolean("SleepNeeded")
        if not sleepNeeded then return false end   -- gate never blocks without SleepNeeded
        if playerObj:getStats():get(CharacterStat.FATIGUE) <= 0.3 then return false end
        if not vehicle:isStopped() then return false end
        if (playerObj:getHoursSurvived() - playerObj:getLastHourSleeped()) <= 1 then return false end
        if playerObj:getSleepingTabletEffect() < 2000 then
            if playerObj:getMoodles():getMoodleLevel(MoodleType.PAIN) >= 2
               and playerObj:getStats():get(CharacterStat.FATIGUE) <= 0.85 then return false end
            if playerObj:getMoodles():getMoodleLevel(MoodleType.PANIC) >= 1 then return false end
        end
        return true
    end

    function ISVehicleMenu.showRadialMenu(playerObj)
        origShowRadialMenu(playerObj)
        pcall(function()
            local vehicle = playerObj and playerObj:getVehicle()
            if not vehicle then return end
            local menu = getPlayerRadialMenu(playerObj:getPlayerNum())
            if not (menu and menu:isReallyVisible()) then return end   -- orig toggled it off
            local stats = playerObj:getStats()
            local engineSaysZombies = stats:getNumVisibleZombies() > 0
                or stats:getNumChasingZombies() > 0
                or stats:getNumVeryCloseZombies() > 0
            if not engineSaysZombies then return end                 -- vanilla already offered Sleep
            if BanditsNPC.RealZombiesNear(playerObj) then return end -- genuinely unsafe
            if not sleepWouldBeOffered(playerObj, vehicle) then return end
            menu:addSlice(getText("ContextMenu_Sleep"), getTexture("media/ui/vehicles/vehicle_sleep.png"),
                          ISVehicleMenu.onSleep, playerObj, vehicle)
        end)
    end
end

if ISWorldObjectContextMenu and ISWorldObjectContextMenu.onSleepWalkToComplete and not BanditsNPC._sleepPatched then
    BanditsNPC._sleepPatched = true

    function ISWorldObjectContextMenu.onSleepWalkToComplete(player, bed)
        local playerObj = getSpecificPlayer(player)
        if not playerObj then return end
        local isZombies = BanditsNPC.RealZombiesNear(playerObj)   -- CHANGED: was engine zombie-stat counts (companions tripped it)
        if isZombies then
            HaloTextHelper.addBadText(playerObj, getText("IGUI_Sleep_NotSafe"));
            return
        end
        if playerObj:getSleepingTabletEffect() < 2000 then
            if playerObj:getMoodles():getMoodleLevel(MoodleType.PAIN) >= 2 and playerObj:getStats():get(CharacterStat.FATIGUE) <= 0.85 then
                HaloTextHelper.addBadText(playerObj, getText("ContextMenu_PainNoSleep"));
                return
            end
            if playerObj:getMoodles():getMoodleLevel(MoodleType.PANIC) >= 1 then
                HaloTextHelper.addBadText(playerObj, getText("ContextMenu_PanicNoSleep"));
                return
            end
        end

        if (playerObj:getVariableBoolean("ExerciseEnded") == false) then
            return
        end

        BanditsNPC.BeginSleep(player, playerObj, bed)
    end

    -- HDX_Strangers ships its OWN full replacement of this same function, and its
    -- threat scan only excuses Stranger-flagged NPCs -- with both mods installed and
    -- HDX loading after us, OUR companions blocked sleep again ("Not safe to sleep
    -- here", report 13 Jul). Same defense as the BWO CheckFriendlyFire clash:
    -- remember our function and re-assert it by identity at OnGameStart. Ours
    -- excuses BOTH mods' friendly NPCs (see RealZombiesNear), so whichever mod the
    -- user loads last, sleep works next to everyone's companions.
    BanditsNPC._sleepGateFn = ISWorldObjectContextMenu.onSleepWalkToComplete
    Events.OnGameStart.Add(function()
        if ISWorldObjectContextMenu.onSleepWalkToComplete ~= BanditsNPC._sleepGateFn then
            ISWorldObjectContextMenu.onSleepWalkToComplete = BanditsNPC._sleepGateFn
        end
    end)
end

-- ===== "Sleep (companions keep watch)" -- the guaranteed path =====
-- The gate patches above can still lose in the field: other NPC mods replace the
-- same function (HDX gate war), and next to an NPC the engine's panic ramp can
-- hold the PANIC moodle at >= 1 so even a correct zombie scan refuses ("your fix
-- did not work", author 13 Jul). Technique adopted from the "Just Sleep" mod
-- (workshop 3633069724; code studied, implementation our own): don't argue with
-- the gate -- add our own context option that calls the engine sleep primitives
-- directly via BanditsNPC.BeginSleep. Differences from Just Sleep: the option
-- only appears when it's a COMPANION situation (friendly NPC within 12 tiles,
-- bed within a tile, player actually tired) and NEVER with a real threat near
-- (RealZombiesNear -- a genuine zombie still blocks it); the vanilla PAIN block
-- stays (unrelated to companions); the artificial PANIC moodle check is skipped
-- (that panic IS the companion artifact); normal wake-time math, no forced 8h.

-- any friendly NPC (ours: recruited or neutral non-hostile) within 12 tiles
local function friendlyNpcNear(playerObj)
    local cache = BanditZombie and BanditZombie.CacheLightB
    if not cache then return false end
    local px, py = playerObj:getX(), playerObj:getY()
    for _, z in pairs(cache) do
        local b = z.brain
        if b and (b.recruited or not (b.hostile or b.hostileP)) then
            local dx, dy = (z.x or 0) - px, (z.y or 0) - py
            if dx * dx + dy * dy <= 144 then return true end
        end
    end
    return false
end

-- real bed furniture on the player's tile or any neighbor (the engine's own
-- sleepable sprite flag -- same probe as our bed-spot validator, modded beds set it)
local function findNearbyBed(playerObj)
    local psq = playerObj:getSquare()
    if not psq then return nil end
    for dx = -1, 1 do
        for dy = -1, 1 do
            local sq = getCell():getGridSquare(psq:getX() + dx, psq:getY() + dy, psq:getZ())
            if sq then
                local objs = sq:getObjects()
                for i = 0, objs:size() - 1 do
                    local o = objs:get(i)
                    local ok, isBed = pcall(function()
                        return o and o:getProperties() and o:getProperties():has(IsoFlagType.bed)
                    end)
                    if ok and isBed then return o end
                end
            end
        end
    end
    return nil
end

function BanditsNPC.SleepHere(player, playerObj, bed)
    -- vanilla pain rule kept verbatim; panic moodle deliberately not checked
    if playerObj:getSleepingTabletEffect() < 2000 then
        if playerObj:getMoodles():getMoodleLevel(MoodleType.PAIN) >= 2
                and playerObj:getStats():get(CharacterStat.FATIGUE) <= 0.85 then
            HaloTextHelper.addBadText(playerObj, getText("ContextMenu_PainNoSleep"))
            return
        end
    end
    -- vanilla's ExerciseEnded latch, restored (v0.75.21). The patched gate above keeps
    -- it; this path dropped it, and unlike the PANIC omission -- which is deliberate and
    -- documented twice, because that panic IS the companion artifact -- this one had no
    -- comment because it was an oversight. ISFitnessAction clears the variable while an
    -- exercise is running, and vanilla refuses to sleep through one.
    if playerObj:getVariableBoolean("ExerciseEnded") == false then return end
    BanditsNPC.BeginSleep(player, playerObj, bed)
end

local function addGuardedSleepOption(player, context, worldobjects, test)
    if test then return end
    pcall(function()
        local playerObj = getSpecificPlayer(player)
        if not playerObj or playerObj:isAsleep() or playerObj:getVehicle() then return end
        if playerObj:getStats():get(CharacterStat.FATIGUE) <= 0.3 then return end   -- vanilla offer rule
        if not friendlyNpcNear(playerObj) then return end
        local bed = findNearbyBed(playerObj)
        if not bed then return end
        if BanditsNPC.RealZombiesNear(playerObj) then return end
        context:addOption(BanditsNPC.T("UI_BN_Ctx_SleepGuarded", "Sleep (companions keep watch)"),
            playerObj, function()
                BanditsNPC.SleepHere(player, playerObj, bed)
            end)
    end)
end
Events.OnFillWorldObjectContextMenu.Add(addGuardedSleepOption)
