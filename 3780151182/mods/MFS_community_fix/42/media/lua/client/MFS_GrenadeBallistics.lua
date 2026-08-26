--[[---------------------------------------------------------------------------
    MFS_GrenadeBallistics.lua

    Projectile flight, aiming and impact for the MFS grenade launcher.

    CREDIT: adapted, with permission, from GrenadeBallistics.lua in
    "US Military Explosives / US Military Grenades [B42]".
    The projectile integration, depth-correct cursor picking, mid-flight wall
    handling and IsoTrap impact model are his work. Retained here under an MFS
    namespace so the two mods can coexist without global collisions.

    See MFS_GRENADE_LAUNCHER_REBUILD_PLAN.txt for why this exists and what was
    deliberately stripped (throw coupling, flares, mines, traps, bounce,
    delayed fuse, Strength/weight range scaling).

    STATUS: Phase 6-complete foundation. Base.MFS_MTL30_cat and every pseudo-launcher in
    MFSUnderbarrelRegistry drive this system through an MFS-only OnWeaponSwing hook.
    Vanilla still owns magazine/chamber state, ammunition consumption, firing
    animation and reload actions.
-----------------------------------------------------------------------------]]

require "MFS_ExplosionFX"
require "MFSUnderbarrelRegistry"

MFS_GrenadeBallistics = MFS_GrenadeBallistics or {}
local M = MFS_GrenadeBallistics

M._activeProjectiles = {}
M._projectilesByShotID = {}
M._seenLaunches = {}
M._seenExplosions = {}
M._nextShotSequence = M._nextShotSequence or 0

-- Aim state, written each render frame, read by the launcher trigger (Phase 2).
M._validTargetSq   = nil
M._validImpactX    = nil
M._validImpactY    = nil
M._validImpactZ    = nil
M._validHitWall    = false
M._validFloorsDown = 0

-- Client command module. MUST NOT be "Explosives" - that is the other mod's
-- server handler and it would happily explode things on our behalf.
local MP = MFSUnderbarrelRegistry.MP
local MP_MODULE = MP.MODULE
local MP_VERSION = MP.VERSION
local MP_LAUNCH = MP.LAUNCH
local MP_EXPLOSION = MP.EXPLOSION
local MP_TRIGGER_EXPLOSION = MP.TRIGGER_EXPLOSION
local MP_HISTORY_MS = 30000

local DROP_MODE_FLOOR_THRESHOLD = 3
local DROP_GRAVITY = 5 -- tunable, floor-units per second^2 (not real gravity)

-- Custom ground-plane target marker (vanilla reticle reads wrong for arced shots).
-- Pre-colored textures, no runtime tint draw call available.
-- Copied into this overlay from Grenademod with permission. Explosives is
-- listed as a Steam required item but is deliberately not a hard mod.info
-- requirement; these local copies preserve compatibility for existing users.
local TARGET_MARKER_TEX_IN_RANGE     = "media/textures/FX/target_marker_green.png"
local TARGET_MARKER_TEX_OUT_OF_RANGE = "media/textures/FX/target_marker_red.png"
local TARGET_MARKER_WIDTH  = 128
local TARGET_MARKER_HEIGHT = 64
local TARGET_MARKER_ALPHA  = 0.8
local IMPACT_MARKER_SIZE   = 10 -- small dot, deliberately much smaller than the ground marker

local debug = getDebug()

-- The M.CONFGS here is for a Grenade gun, for underbarrel grenade launcher, the registry is in shared/MFSUnderbarrelRegistry.lua
M.CONFIGS = {
    ["Base.MFS_MTL30_cat"] = {
        speed = 18,
        maxRange = 30,
        arcHeightFactor = 0.03,
        projectileType = "Base.GrenadeAmmo",
        payloadType = "MFS_Explosives.40mmExplosives",

        -- Model-space attachment coordinates cannot be transformed into a
        -- live world point through any proven MFS API. For the Phase 2 test,
        -- use the MTL30 model's 0.84 forward muzzle offset and the reference
        -- ballistics code's half-floor firing height.
        muzzleForwardOffset = 0.84,
        muzzleHeight = 0.5,

        fxPrefix = "explosion_",
        fxFrames = 12,
        fxSize = 314,
        fxDuration = 40,
    },
}

-- All registered underbarrels receive their default or overridden ballistic
-- configuration from the single shared maintenance table.
for _, definition in pairs(MFSUnderbarrelRegistry.LAUNCHERS) do
    M.CONFIGS[definition.pseudoType] = definition.ballistics
end

-- Range for this shot. Deliberately NOT scaled by Perks.Strength or item
-- weight - that was throw logic and makes no sense for a launcher.
-- Returns: isDrop, maxRange
local function calculateMaxRange(cfg, floorsDown)
    local flatRange = cfg.maxRange or 30

    if floorsDown >= DROP_MODE_FLOOR_THRESHOLD then
        -- Steep drop (firing down off a roof): reach = speed * fall time.
        local flightTime = math.sqrt(2 * floorsDown / DROP_GRAVITY)
        return true, math.max(flatRange, (cfg.speed or 25) * flightTime)
    end

    return false, flatRange
end

--=============================================================================
-- Geometry helpers
--=============================================================================

local function isWallObject(obj)
    if not obj then return false end
    local ok, sprite = pcall(function() return obj:getSprite() end)
    if not ok or not sprite then return false end
    local props = sprite:getProperties()
    if not props then return false end
    return props:has("WallN") or props:has("WallW") or props:has("WallNW")
end

-- Mid-flight wall check. Windows smash instead of blocking -- a 40mm round
-- does not lose meaningful momentum through glass.
local function squareHasWall(sq)
    if not sq then return false end
    local objects = sq:getObjects()
    if not objects then return false end
    local blocked = false
    -- Collect windows first, then smash: smashWindow() mutates this list mid-iteration.
    local windows = {}
    for i = 0, objects:size() - 1 do
        local obj = objects:get(i)
        if instanceof(obj, "IsoWindow") then
            if not obj:isSmashed() then
                table.insert(windows, obj)
            end
        elseif isWallObject(obj) then
            blocked = true
        end
    end
    for _, win in ipairs(windows) do
        if not win:isSmashed() then
            win:smashWindow()
        end
    end
    return blocked
end

-- 2nd return: is the pick a wall sprite?
local function getLastPickedSquare()
    local ok, lastPicked = pcall(function() return UIManager.getLastPicked() end)
    if not ok or not lastPicked then return nil, false end
    local squareOk, sq = pcall(function() return lastPicked:getSquare() end)
    if squareOk and sq then return sq, isWallObject(lastPicked) end
    return nil, false
end

-- Estimates click height above baseZ: Z is linear with screen Y, so two
-- ToScreen samples give pixels-per-Z.
local function estimateClickHeightOffset(mouseX, mouseY, groundX, groundY, baseZ)
    local zoom = getCore():getZoom(0)
    local scaledMouseY = mouseY * zoom

    local _, screenYBase      = ISCoordConversion.ToScreen(groundX, groundY, baseZ)
    local _, screenYBasePlus1 = ISCoordConversion.ToScreen(groundX, groundY, baseZ + 1)
    local pixelsPerZ = screenYBase - screenYBasePlus1
    if pixelsPerZ == 0 then return 0 end

    return (screenYBase - scaledMouseY) / pixelsPerZ
end

--=============================================================================
-- Markers
--=============================================================================

local function drawTargetMarker(worldX, worldY, worldZ, inRange)
    local tex = getTexture(inRange and TARGET_MARKER_TEX_IN_RANGE or TARGET_MARKER_TEX_OUT_OF_RANGE)
    if not tex then return end
    local sx, sy = ISCoordConversion.ToScreen(worldX, worldY, worldZ)
    local hw, hh = TARGET_MARKER_WIDTH / 2, TARGET_MARKER_HEIGHT / 2
    getRenderer():renderPoly(tex,
        sx - hw, sy - hh,
        sx + hw, sy - hh,
        sx + hw, sy + hh,
        sx - hw, sy + hh,
        1, 1, 1, TARGET_MARKER_ALPHA)
end

local function drawImpactMarker(worldX, worldY, worldZ)
    local tex = getTexture(TARGET_MARKER_TEX_OUT_OF_RANGE)
    if not tex then return end
    local sx, sy = ISCoordConversion.ToScreen(worldX, worldY, worldZ)
    local h = IMPACT_MARKER_SIZE / 2
    getRenderer():renderPoly(tex,
        sx - h, sy - h,
        sx + h, sy - h,
        sx + h, sy + h,
        sx - h, sy + h,
        1, 1, 1, 1) -- texture is already red-colored, no extra tint needed
end

--=============================================================================
-- Impact
--=============================================================================

-- Cosmetic only, no damage. Same radius as the explosion, +/- one floor.
local function shatterNearbyWindows(square, radius)
    if not square or not radius or radius <= 0 then return end
    local cell = getCell()
    local cx, cy, cz = square:getX(), square:getY(), square:getZ()
    for dz = -1, 1 do
        for dx = -radius, radius do
            for dy = -radius, radius do
                local sq = cell:getGridSquare(cx + dx, cy + dy, cz + dz)
                local objects = sq and sq:getObjects()
                if objects then
                    local windows = {}
                    for i = 0, objects:size() - 1 do
                        local obj = objects:get(i)
                        if instanceof(obj, "IsoWindow") and not obj:isSmashed() then
                            table.insert(windows, obj)
                        end
                    end
                    for _, win in ipairs(windows) do
                        if not win:isSmashed() then
                            win:smashWindow()
                        end
                    end
                end
            end
        end
    end
end

-- `seen` dedupes multi-square vehicles. window:hit() is the actual break call.
local function shatterNearbyCarWindows(square, radius)
    if not square or not radius or radius <= 0 then return end
    local player = getPlayer()
    if not player then return end
    local cell = getCell()
    local cx, cy, cz = square:getX(), square:getY(), square:getZ()
    local seen = {}
    for dx = -radius, radius do
        for dy = -radius, radius do
            local sq = cell:getGridSquare(cx + dx, cy + dy, cz)
            local movingObjects = sq and sq:getMovingObjects()
            if movingObjects then
                for i = 0, movingObjects:size() - 1 do
                    local obj = movingObjects:get(i)
                    if instanceof(obj, "BaseVehicle") and not seen[obj] then
                        seen[obj] = true
                        for p = 0, obj:getPartCount() - 1 do
                            local part = obj:getPartByIndex(p)
                            local window = part and part:getWindow()
                            if window and window:isHittable() then
                                window:hit(player)
                            end
                        end
                    end
                end
            end
        end
    end
end

-- The whole point of the rebuild: damage comes from the vanilla explosion
-- pipeline via IsoTrap, so kills are attributed to the player by the engine.
-- payloadType MUST be an item carrying ExplosionPower/ExplosionRange.
--
-- MP MODEL (corrected after the first live MP test): trigger the owned IsoTrap
-- locally exactly as in SP, then repeat it authoritatively on the server. The
-- local trigger is required in B42.20 for the native explosion sound, shooter
-- kill attribution and the same composite visual seen in SP. This now mirrors
-- the proven Explosives-mod pattern rather than the failed FX-only experiment.
local function explodeGrenadeAt(payloadType, config, square, worldX, worldY, worldZ,
                                shotID, weaponType, remoteVisual)
    -- Remote flight is visual prediction only. The server's Explosion message
    -- supplies the exact authoritative impact point and owns its FX timing.
    if remoteVisual then return end

    if shotID then
        M._seenExplosions[shotID] = getTimestampMs()
    end
    if config.fxPrefix then
        MFS_ExplosionFX.spawn(worldX, worldY, worldZ, config)
    end

    if not square then return end

    local payload = instanceItem(payloadType)
    if not payload then return end

    -- Glass is cosmetic, so do it locally in both SP and MP for instant feedback.
    local ok, explosionRange = pcall(function() return payload:getExplosionRange() end)
    if ok and explosionRange and explosionRange > 0 then
        local radius = math.floor(explosionRange)
        shatterNearbyWindows(square, radius)
        shatterNearbyCarWindows(square, radius)
    end

    local trap = IsoTrap.new(getPlayer(), payload, getCell(), square)
    trap:triggerExplosion(false)

    if isClient() then
        -- The server repeats the trap for authoritative world state and remote
        -- clients receive its exact impact position through MP_EXPLOSION.
        sendClientCommand(getPlayer(), MP_MODULE, MP_TRIGGER_EXPLOSION, {
            protocolVersion = MP_VERSION,
            shotID = shotID,
            weaponType = weaponType,
            x = square:getX(),
            y = square:getY(),
            z = square:getZ(),
        })
        return
    end
end

--=============================================================================
-- Public API - the seam the launcher trigger calls (Phase 2)
--=============================================================================

-- params:
--   startX, startY, startZ    muzzle origin (use the weapon's `attachment muzzle` offset)
--   targetX, targetY, targetZ impact point
--   targetSq                  IsoGridSquare at the impact point
--   hitWall                   true if the aim pick was a wall sprite
--   floorsDown                how many floors below the shooter the target is
--   config                    an entry from M.CONFIGS
--
-- Returns true if a projectile was created.
function M.Launch(params)
    local cfg = params.config
    if not cfg then return false end

    local startX, startY = params.startX, params.startY
    local startZ = params.startZ or 0
    local targetX, targetY = params.targetX, params.targetY
    local targetSq = params.targetSq

    local dist = IsoUtils.DistanceTo(startX, startY, targetX, targetY)
    local isDrop, maxRange = calculateMaxRange(cfg, params.floorsDown or 0)

    -- Clamp to achievable range. Keep the clamp here so the aim preview and the
    -- actual shot can never disagree - they call the same calculateMaxRange.
    if dist > maxRange then
        local angle = math.atan2(targetY - startY, targetX - startX)
        targetX = startX + math.cos(angle) * maxRange
        targetY = startY + math.sin(angle) * maxRange
        dist = maxRange
        -- NOTE (plan Part 7): getOrCreateGridSquare can reach unloaded chunks at
        -- launcher ranges. Worth clamping to loaded bounds if this ever misbehaves.
        local newSq = getCell():getOrCreateGridSquare(targetX, targetY, math.floor(params.targetZ or 0))
        if newSq then targetSq = newSq end
    end

    local flightTime, arcHeight, dropDistance
    if isDrop then
        dropDistance = params.floorsDown or 0
        flightTime = math.sqrt(2 * dropDistance / DROP_GRAVITY)
        arcHeight = 0
    else
        dropDistance = 0
        flightTime = dist / (cfg.speed or 25)
        arcHeight = dist * (cfg.arcHeightFactor or 0.03)
    end

    if debug then
        print(string.format("[MFSGrenade][Launch] start(%.2f,%.2f,%.2f) target(%.2f,%.2f,%.2f) dist=%.2f floorsDown=%d isDrop=%s maxRange=%.2f",
            startX, startY, startZ, targetX, targetY, params.targetZ or 0, dist,
            params.floorsDown or 0, tostring(isDrop), maxRange))
    end

    -- Spawn the visible flying round.
    local startSq = getCell():getOrCreateGridSquare(startX, startY, startZ)
    if not startSq then return false end
    local item = startSq:AddWorldInventoryItem(instanceItem(cfg.projectileType), 0, 0, 0, false)
    if not item then return false end

    local dir = params.direction or 0
    item:setWorldZRotation(dir * 360 / (2 * math.pi))
    -- No setWorldScale() override: the item's model defines its own correct scale.

    local wi = item:getWorldItem()
    if not wi then return false end
    wi:setIgnoreRemoveSandbox(true)
    wi:setExtendedPlacement(false)
    -- This is a client-local visual. Phase 2 transmitted it once, which made
    -- remote clients receive a static networked item while only the shooter
    -- updated its offsets. Phase 4 broadcasts launch data instead, so every
    -- client integrates and removes its own non-persistent visual.

    if wi:getSquare() then
        wi:setOffX(startX - wi:getSquare():getX())
        wi:setOffY(startY - wi:getSquare():getY())
        wi:setOffZ(startZ - wi:getSquare():getZ())
    end

    local projectile = {
        item = wi,
        shotID = params.shotID,
        remoteVisual = params.remoteVisual == true,
        startX = startX,
        startY = startY,
        flightStartHeight = startZ,
        targetX = targetX,
        targetY = targetY,
        flightTargetHeight = params.targetZ or 0,
        targetSq = targetSq,
        hitWall = params.hitWall or false,
        arcHeight = arcHeight,
        flightTime = flightTime,
        isDrop = isDrop,
        dropDistance = dropDistance,
        elapsed = 0,
        config = cfg,
        weaponType = params.weaponType,
    }
    table.insert(M._activeProjectiles, projectile)
    if projectile.shotID then
        M._projectilesByShotID[projectile.shotID] = projectile
    end

    return true
end

--=============================================================================
-- Launcher front end
--=============================================================================

local function hasShootableRound(playerObj, weapon)
    -- Match the vanilla reload system's unlimited-ammunition exception.
    if playerObj:isUnlimitedAmmo() then
        return true
    end
    if weapon:getCurrentAmmoCount() and weapon:getCurrentAmmoCount() > 0 then
        return true
    end
    return weapon.isRoundChambered and weapon:isRoundChambered()
end

local function makeShotID(playerObj)
    M._nextShotSequence = M._nextShotSequence + 1
    local onlineID = -1
    pcall(function() onlineID = playerObj:getOnlineID() end)
    return tostring(onlineID) .. ":" .. tostring(getTimestampMs()) .. ":" .. tostring(M._nextShotSequence)
end

local function onWeaponSwing(playerObj, weapon)
    if not playerObj or not weapon then return end

    local cfg = M.CONFIGS[weapon:getFullType()]
    if not cfg then return end
    if playerObj:isDoShove() or not hasShootableRound(playerObj, weapon) then return end

    local targetSq = M._validTargetSq
    local targetX = M._validImpactX
    local targetY = M._validImpactY
    local targetZ = M._validImpactZ
    if not targetSq or not targetX or not targetY or not targetZ then
        if debug then
            print("[MFSGrenade][Fire] launcher shot had no valid aimed target; projectile not launched")
        end
        return
    end

    -- Aim direction is used for both the approximate muzzle origin and the
    -- visible projectile rotation. This keeps the launch aligned with the
    -- precise cursor impact point even near the edges of a direction sector.
    local direction = math.atan2(targetY - playerObj:getY(), targetX - playerObj:getX())
    local muzzleForward = cfg.muzzleForwardOffset or 0.1
    local startX = playerObj:getX() + math.cos(direction) * muzzleForward
    local startY = playerObj:getY() + math.sin(direction) * muzzleForward
    local startZ = playerObj:getZ() + (cfg.muzzleHeight or 0.5)

    local shotID = makeShotID(playerObj)
    local launchParams = {
        startX = startX,
        startY = startY,
        startZ = startZ,
        targetX = targetX,
        targetY = targetY,
        targetZ = targetZ,
        targetSq = targetSq,
        hitWall = M._validHitWall,
        floorsDown = M._validFloorsDown,
        direction = direction,
        config = cfg,
        shotID = shotID,
        weaponType = weapon:getFullType(),
        remoteVisual = false,
    }
    local launched = M.Launch(launchParams)

    -- Let the underbarrel visual layer distinguish a real launch from a dry
    -- attack without inferring from client ammo state, which may await server
    -- synchronization in multiplayer.
    if launched and MFSUnderbarrelRegistry.isPseudo(weapon) then
        weapon:getModData().MFSUnderbarrelLaunchedAt = getTimestampMs()
    end

    if launched and isClient() then
        M._seenLaunches[shotID] = getTimestampMs()
        sendClientCommand(playerObj, MP_MODULE, MP_LAUNCH, {
            protocolVersion = MP_VERSION,
            shotID = shotID,
            weaponType = weapon:getFullType(),
            startX = startX,
            startY = startY,
            startZ = startZ,
            targetX = targetX,
            targetY = targetY,
            targetZ = targetZ,
            hitWall = M._validHitWall == true,
            floorsDown = M._validFloorsDown,
            direction = direction,
        })
    end

    if debug then
        print("[MFSGrenade][Fire] weapon=" .. tostring(weapon:getFullType())
            .. " launched=" .. tostring(launched))
    end
end

M._onWeaponSwing = onWeaponSwing

local function removeProjectile(projectile)
    if not projectile then return end
    local wi = projectile.item
    if wi then
        if wi:getSquare() then
            wi:removeFromSquare()
        end
        wi:removeFromWorld()
    end
    if projectile.shotID then
        M._projectilesByShotID[projectile.shotID] = nil
    end
    for index, active in pairs(M._activeProjectiles) do
        if active == projectile then
            M._activeProjectiles[index] = nil
            break
        end
    end
end

local function onServerCommand(module, command, args)
    if module ~= MP_MODULE or type(args) ~= "table"
        or args.protocolVersion ~= MP_VERSION then
        return
    end

    local shotID = type(args.shotID) == "string" and args.shotID or nil
    if not shotID then return end

    if command == MP_LAUNCH then
        if M._seenLaunches[shotID] then return end
        M._seenLaunches[shotID] = getTimestampMs()

        local cfg = M.CONFIGS[args.weaponType]
        if not cfg then return end
        local targetX, targetY, targetZ = tonumber(args.targetX), tonumber(args.targetY), tonumber(args.targetZ)
        local startX, startY, startZ = tonumber(args.startX), tonumber(args.startY), tonumber(args.startZ)
        if not startX or not startY or not startZ or not targetX or not targetY or not targetZ then return end

        local targetSq = getCell():getGridSquare(math.floor(targetX), math.floor(targetY), math.floor(targetZ))
        if not targetSq then return end -- unloaded for this client; no persistent item to leak

        M.Launch({
            startX = startX,
            startY = startY,
            startZ = startZ,
            targetX = targetX,
            targetY = targetY,
            targetZ = targetZ,
            targetSq = targetSq,
            hitWall = args.hitWall == true,
            floorsDown = tonumber(args.floorsDown) or 0,
            direction = tonumber(args.direction) or 0,
            config = cfg,
            shotID = shotID,
            weaponType = args.weaponType,
            remoteVisual = true,
        })
        return
    end

    if command == MP_EXPLOSION then
        if M._seenExplosions[shotID] then return end
        M._seenExplosions[shotID] = getTimestampMs()

        removeProjectile(M._projectilesByShotID[shotID])
        local cfg = M.CONFIGS[args.weaponType]
        local x, y, z = tonumber(args.x), tonumber(args.y), tonumber(args.z)
        if cfg and cfg.fxPrefix and x and y and z then
            MFS_ExplosionFX.spawn(x, y, z, cfg)
        end
    end
end

M._onServerCommand = onServerCommand

--=============================================================================
-- Render tick: aim preview + projectile integration
--=============================================================================

local function onPostRender()
    local player = getPlayer()
    if not player then return end

    -- Reset each frame; stale values were causing shots to fly nowhere while
    -- the red marker was showing.
    M._validTargetSq   = nil
    M._validImpactX    = nil
    M._validImpactY    = nil
    M._validImpactZ    = nil
    M._validHitWall    = false
    M._validFloorsDown = 0

    -- Aim preview is active only for configured launcher weapons.
    local weapon = player:getPrimaryHandItem()
    local cfg = weapon and M.CONFIGS[weapon:getFullType()] or nil

    if cfg and player:isAiming() then
        local mouseX, mouseY = getMouseX(), getMouseY()
        local pickedSq, pickedIsWall = getLastPickedSquare()

        -- No line-of-sight gate: getCanSee rejected too many legitimate shots.
        -- The mid-flight wall check below is the real safety net.
        if pickedSq then
            -- Follow the mouse continuously; snapping to tile centre made the
            -- marker jump instead of glide.
            local zoom = getCore():getZoom(0)
            local worldX, worldY = ISCoordConversion.ToWorld(mouseX * zoom, mouseY * zoom, pickedSq:getZ())
            local markerX = worldX or (pickedSq:getX() + 0.5)
            local markerY = worldY or (pickedSq:getY() + 0.5)

            local dist = IsoUtils.DistanceTo(player:getX(), player:getY(), markerX, markerY)
            local floorsDown = math.max(0, math.floor(player:getZ() - pickedSq:getZ()))
            local isDrop, maxRange = calculateMaxRange(cfg, floorsDown)
            local inRange = dist <= maxRange

            if debug and getTimestampMs() - (M._lastAimLogMs or 0) > 500 then
                M._lastAimLogMs = getTimestampMs()
                print(string.format("[MFSGrenade][Aim] playerZ=%.1f pickedSq(%d,%d,%d) floorsDown=%d isDrop=%s wall=%s marker(%.2f,%.2f) dist=%.2f maxRange=%.2f inRange=%s",
                    player:getZ(), pickedSq:getX(), pickedSq:getY(), pickedSq:getZ(), floorsDown,
                    tostring(isDrop), tostring(pickedIsWall), markerX, markerY, dist, maxRange, tostring(inRange)))
            end

            if not inRange then
                -- Show the marker where the shot will actually clamp to.
                local angle = math.atan2(markerY - player:getY(), markerX - player:getX())
                markerX = player:getX() + math.cos(angle) * maxRange
                markerY = player:getY() + math.sin(angle) * maxRange
            end

            -- getCanSee is display-only (red marker as a heads-up); it does not
            -- block the shot.
            local canSeeTarget = pickedSq:getCanSee(player:getPlayerNum())
            drawTargetMarker(markerX, markerY, pickedSq:getZ(), inRange and canSeeTarget)

            -- Where on the picked object's vertical face the mouse is, not just
            -- the ground square. This is what puts a round through a window.
            local clickHeightOffset = estimateClickHeightOffset(mouseX, mouseY, markerX, markerY, pickedSq:getZ())
            -- pixelsPerZ can get tiny at some zoom/angles, blowing this up.
            clickHeightOffset = math.max(-2, math.min(10, clickHeightOffset))

            M._validTargetSq   = pickedSq
            M._validImpactX    = markerX
            M._validImpactY    = markerY
            M._validImpactZ    = pickedSq:getZ() + clickHeightOffset
            M._validHitWall    = pickedIsWall
            M._validFloorsDown = floorsDown

            if debug then
                drawImpactMarker(markerX, markerY, pickedSq:getZ() + clickHeightOffset)
            end
        else
            -- No valid pick: still show the red marker on a flat plane, display-only.
            local zoom = getCore():getZoom(0)
            local worldX, worldY = ISCoordConversion.ToWorld(mouseX * zoom, mouseY * zoom, player:getZ())
            if worldX and worldY then
                drawTargetMarker(worldX, worldY, player:getZ(), false)
            end
        end
    end

    local timeMultiplier = getGameTime():getMultiplier()

    for k, proj in pairs(M._activeProjectiles) do
        proj.elapsed = proj.elapsed + timeMultiplier
        local t = proj.elapsed / (proj.flightTime * 60)
        if t >= 1.0 then t = 1.0 end

        local currX = proj.startX + (proj.targetX - proj.startX) * t
        local currY = proj.startY + (proj.targetY - proj.startY) * t

        local grenadeHeight
        if proj.isDrop then
            grenadeHeight = proj.flightStartHeight - proj.dropDistance * (t * t)
        else
            local flightPathHeight = proj.flightStartHeight + (proj.flightTargetHeight - proj.flightStartHeight) * t
            local arcBump = proj.arcHeight * 4 * t * (1 - t)
            grenadeHeight = flightPathHeight + arcBump
        end

        -- Mid-flight wall hit. A 40mm is impact-fuzed, so it detonates right
        -- here - no fall-to-ground, no bounce.
        if t < 1.0 then
            local wallSq = getCell():getGridSquare(math.floor(currX), math.floor(currY), math.floor(grenadeHeight))
            if wallSq and squareHasWall(wallSq) then
                t = 1.0
                proj.targetSq = wallSq
                proj.targetX = currX
                proj.targetY = currY
                proj.flightTargetHeight = grenadeHeight
                proj.hitWall = true
                if debug then
                    print(string.format("[MFSGrenade][WallHit] (%d,%d,%d)", wallSq:getX(), wallSq:getY(), wallSq:getZ()))
                end
            end
        end

        if t >= 1.0 then
            explodeGrenadeAt(proj.config.payloadType, proj.config, proj.targetSq,
                proj.targetX, proj.targetY, proj.flightTargetHeight,
                proj.shotID, proj.weaponType, proj.remoteVisual)
            removeProjectile(proj)
        else
            -- Still flying: update the visible position.
            local wi = proj.item
            if wi and wi:getSquare() then
                wi:setOffX(currX - wi:getSquare():getX())
                wi:setOffY(currY - wi:getSquare():getY())
                wi:setOffZ(grenadeHeight - wi:getSquare():getZ())
            end
        end
    end


    local now = getTimestampMs()
    if now - (M._lastHistoryPruneMs or 0) >= MP_HISTORY_MS then
        M._lastHistoryPruneMs = now
        for shotID, seenAt in pairs(M._seenLaunches) do
            if now - seenAt > MP_HISTORY_MS then M._seenLaunches[shotID] = nil end
        end
        for shotID, seenAt in pairs(M._seenExplosions) do
            if now - seenAt > MP_HISTORY_MS then M._seenExplosions[shotID] = nil end
        end
    end

    MFS_ExplosionFX.render()
end

M._onPostRender = onPostRender

-- Idempotent registration keeps Lua reloads from adding duplicate projectiles
-- or rendering/updating the same projectile more than once per frame.
MFSPerformanceSafety = MFSPerformanceSafety or {}
if MFSPerformanceSafety.grenadeBallisticsSwing then
    Events.OnWeaponSwing.Remove(MFSPerformanceSafety.grenadeBallisticsSwing)
end
if MFSPerformanceSafety.grenadeBallisticsRender then
    Events.OnPostRender.Remove(MFSPerformanceSafety.grenadeBallisticsRender)
end
if MFSPerformanceSafety.grenadeBallisticsServerCommand then
    Events.OnServerCommand.Remove(MFSPerformanceSafety.grenadeBallisticsServerCommand)
end
MFSPerformanceSafety.grenadeBallisticsSwing = onWeaponSwing
MFSPerformanceSafety.grenadeBallisticsRender = onPostRender
MFSPerformanceSafety.grenadeBallisticsServerCommand = onServerCommand
Events.OnWeaponSwing.Add(onWeaponSwing)
Events.OnPostRender.Add(onPostRender)
Events.OnServerCommand.Add(onServerCommand)
