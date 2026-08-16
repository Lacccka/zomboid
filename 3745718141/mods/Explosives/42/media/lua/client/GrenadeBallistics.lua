require "client/ExplosionFX"
require "client/FlareHandler"

local GrenadeBallistic = {}
GrenadeBallistic._activeProjectiles = {}
GrenadeBallistic._armedGrenades = {}
GrenadeBallistic._validTargetSq = nil
GrenadeBallistic._validFloorsDown = 0
GrenadeBallistic._pendingThrow = nil

local DROP_MODE_FLOOR_THRESHOLD = 3
local DROP_GRAVITY = 5 -- tunable, floor-units per second^2 (not real gravity)

-- ===============================================================
-- Ground target marker: the vanilla throw reticle (circle + a line
-- rising from it) is meant for direct-line weapons, not a real arced
-- trajectory, and players read the line's tip as the landing point
-- rather than the circle underneath it. Draw our own flat ground-plane
-- marker at the actual landing square instead. Uses two pre-colored
-- textures rather than runtime tinting since there's no color-tint
-- draw call available here.
-- ===============================================================
local TARGET_MARKER_TEX_IN_RANGE = "media/textures/FX/target_marker_green.png"
local TARGET_MARKER_TEX_OUT_OF_RANGE = "media/textures/FX/target_marker_red.png"
local TARGET_MARKER_WIDTH = 128
local TARGET_MARKER_HEIGHT = 64
local TARGET_MARKER_ALPHA = 0.8

local debug = getDebug()

local GRENADE_CONFIGS = {
    ["Explosives.Mk2Grenade"] = {
        speed = 9,
        arcHeightFactor = 0.09,
        limitRange = 20,
        throwReleaseMs = 500,
        fxPrefix = "explosion_",
        fxFrames = 12,
        fxSize = 274,
        fxDuration = 40,
        own = true,
    },
    ["Explosives.M26Grenade"] = {
        speed = 10,
        arcHeightFactor = 0.1,
        limitRange = 19,
        throwReleaseMs = 500,
        fxPrefix = "explosion_",
        fxFrames = 12,
        fxSize = 314,
        fxDuration = 40,
        own = true,
    },
    ["Explosives.M67Grenade"] = {
        speed = 11,
        arcHeightFactor = 0.11,
        limitRange = 18,
        throwReleaseMs = 500,
        fxPrefix = "explosion_",
        fxFrames = 12,
        fxSize = 392,
        fxDuration = 40,
        own = true,
    },
    ["Explosives.M84Flashbang"] = {
        speed = 12,
        arcHeightFactor = 0.12,
        limitRange = 22,
        throwReleaseMs = 500,
        fxPrefix = "flashbang_",
        fxFrames = 6,
        fxSize = 147,
        fxDuration = 80,
        own = true,
    },
    ["Explosives.M14TH3Grenade"] = {
        speed = 10,
        arcHeightFactor = 0.1,
        limitRange = 19,
        throwReleaseMs = 500,
        fxPrefix = "explosion_",
        fxFrames = 12,
        fxSize = 274,
        fxDuration = 20,
        own = true,
    },
}

-- FlareBurning throws with the same physics as an unlit Flare -- only
-- the item type differs, so both can be thrown/aimed the same way.
local FLARE_CONFIG = {
    speed = 12,
    arcHeightFactor = 0.12,
    limitRange = 20,
    throwReleaseMs = 500,
    noExplosion = true, -- lands lit on the ground instead of exploding
}
GRENADE_CONFIGS["Explosives.Flare"] = FLARE_CONFIG
GRENADE_CONFIGS["Explosives.FlareBurning"] = FLARE_CONFIG

-- This mod's own mines/Claymore land as an armed placed trap instead of
-- exploding on impact -- same placeAsTrap handling as the vanilla
-- sensor/remote bombs, always on (not gated behind VanillaBallisticsEnabled,
-- since these are this mod's own items, not vanilla ones).
local MINE_THROW_CONFIG = {
    speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
    placeAsTrap = true,
}
GRENADE_CONFIGS["Explosives.M14Mine"] = MINE_THROW_CONFIG
GRENADE_CONFIGS["Explosives.M18a1Claymore"] = MINE_THROW_CONFIG
GRENADE_CONFIGS["Explosives.M18a1ClaymoreRemote"] = MINE_THROW_CONFIG

-- ===============================================================
-- Optional: apply this mod's ballistic arc + target marker to vanilla
-- throwables too, gated behind the VanillaBallisticsEnabled sandbox
-- option (off by default) so vanilla behavior is unaffected unless a
-- server/player explicitly turns it on. Merged into GRENADE_CONFIGS at
-- OnGameStart, once SandboxVars is guaranteed to be populated.
-- ===============================================================
local VANILLA_GRENADE_CONFIGS = {
    -- Base versions explode immediately on impact, same as this mod's
    -- own grenades.
    ["Base.PipeBomb"] = {
        speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
        fxPrefix = "explosion_", fxFrames = 12, fxSize = 314, fxDuration = 40,
    },
    ["Base.Aerosolbomb"] = {
        speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
        fxPrefix = "explosion_", fxFrames = 12, fxSize = 274, fxDuration = 40,
    },
    -- No ExplosionPower/Range on this item -- triggerExplosion() still
    -- fires natively and applies FireStartingEnergy/Chance/Range itself,
    -- the same mechanism this mod's own M14TH3Grenade already relies on.
    ["Base.Molotov"] = {
        speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
        fxPrefix = "explosion_", fxFrames = 12, fxSize = 250, fxDuration = 40,
    },
    -- SmokeBomb has no ExplosionPower either -- native triggerExplosion()
    -- handles its smoke/noise release itself. No fxPrefix on purpose: a
    -- fire-blast animation would look wrong for smoke, and there's no
    -- dedicated smoke FX yet, so this skips the custom FX overlay (see
    -- the fxPrefix guard around the ExplosionFX.spawn call below).
    ["Base.SmokeBomb"] = {
        speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
    },
    -- FlameTrap ("Fire Bomb") has no ExplosionPower either, same as
    -- Molotov -- needs the same IsoFireManager.StartFire fire-start (see
    -- the Base.FlameTrap check next to the Base.Molotov one below).
    ["Base.FlameTrap"] = {
        speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
        fxPrefix = "explosion_", fxFrames = 12, fxSize = 250, fxDuration = 40,
    },
    -- NoiseTrap is a pure noise/distraction item, no ExplosionPower and
    -- no fire -- same no-fxPrefix treatment as SmokeBomb.
    ["Base.NoiseTrap"] = {
        speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
    },
    -- Firecracker/Firecracker_Crafted have no placed/sensor/remote variants
    -- at all -- always explode (make noise) on impact, no dedicated FX.
    ["Base.Firecracker"] = {
        speed = 12, arcHeightFactor = 0.12, limitRange = 20, throwReleaseMs = 500,
    },
    ["Base.Firecracker_Crafted"] = {
        speed = 12, arcHeightFactor = 0.12, limitRange = 20, throwReleaseMs = 500,
    },
}
-- Sensor/Remote/Triggered versions just land as an armed placed trap
-- (like a manually placed mine) instead of exploding on impact -- native
-- sensor/timer/remote logic takes over from there. MineExplosionFX.lua
-- catches the eventual detonation for FX (SmokeBomb/NoiseTrap variants
-- excluded there too, for the same no-dedicated-FX reason as above).
local PLACE_AS_TRAP_CONFIG = {
    speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
    placeAsTrap = true,
}
for _, fullType in ipairs({
    "Base.PipeBombSensorV1", "Base.PipeBombSensorV2", "Base.PipeBombSensorV3", "Base.PipeBombRemote", "Base.PipeBombTriggered",
    "Base.AerosolbombSensorV1", "Base.AerosolbombSensorV2", "Base.AerosolbombSensorV3", "Base.AerosolbombRemote", "Base.AerosolbombTriggered",
    "Base.SmokeBombSensorV1", "Base.SmokeBombSensorV2", "Base.SmokeBombSensorV3", "Base.SmokeBombRemote", "Base.SmokeBombTriggered",
    "Base.FlameTrapSensorV1", "Base.FlameTrapSensorV2", "Base.FlameTrapSensorV3", "Base.FlameTrapRemote", "Base.FlameTrapTriggered",
    "Base.NoiseTrapSensorV1", "Base.NoiseTrapSensorV2", "Base.NoiseTrapSensorV3", "Base.NoiseTrapRemote", "Base.NoiseTrapTriggered",
}) do
    VANILLA_GRENADE_CONFIGS[fullType] = PLACE_AS_TRAP_CONFIG
end

Events.OnGameStart.Add(function()
    if SandboxVars.Explosives and SandboxVars.Explosives.VanillaBallisticsEnabled then
        for fullType, cfg in pairs(VANILLA_GRENADE_CONFIGS) do
            GRENADE_CONFIGS[fullType] = cfg
        end
    end
end)

-- ===============================================================
-- Range scales a bit with the thrown item's actual Weight -- lighter
-- items fly further, heavier ones land closer, on top of this mod's
-- per-item base limitRange and the player's Strength bonus. Kept
-- modest (a few tiles across this mod's full weight range of 0.05 to
-- 1.5) rather than dominating the throw feel.
-- ===============================================================
local WEIGHT_RANGE_BASELINE = 0.6 -- weight at which limitRange applies unmodified
local WEIGHT_RANGE_SENSITIVITY = 8 -- tiles of range change per 1.0 weight difference
local MIN_LIMIT_RANGE = 10

local function getWeightAdjustedLimitRange(cfg, weight)
    if not weight then return cfg.limitRange end
    local delta = (WEIGHT_RANGE_BASELINE - weight) * WEIGHT_RANGE_SENSITIVITY
    return math.max(MIN_LIMIT_RANGE, cfg.limitRange + delta)
end

-- ===============================================================
-- Max horizontal throw range for this weapon/floor-drop combo.
-- Shared between the actual throw (which clamps to it) and the aim
-- preview marker (which needs to know in-range vs out-of-range before
-- the throw happens) -- kept as one function so they can't drift apart.
-- ===============================================================
local function calculateMaxRange(player, cfg, floorsDown, weight)
    local limitRange = getWeightAdjustedLimitRange(cfg, weight)
    local strength = player:getPerkLevel(Perks.Strength)
    local rangeBonus = strength * 1.5
    local flatRange = limitRange + rangeBonus

    local isDrop = floorsDown >= DROP_MODE_FLOOR_THRESHOLD
    if isDrop then
        -- Steep drop: reach = speed * fall time (oblong projectiles don't
        -- lose much horizontal speed to drag). Never below flatRange --
        -- extra airtime from falling can only add distance, not remove it.
        local flightTime = math.sqrt(2 * floorsDown / DROP_GRAVITY)
        return isDrop, math.max(flatRange, cfg.speed * flightTime)
    end

    return isDrop, flatRange
end

-- ===============================================================
-- UIManager.getLastPicked() is the same depth-correct "what's under the
-- cursor" the engine uses for hover/highlight -- unlike the old flat
-- ground-plane pickSquare heuristic, it correctly tells apart wall sprites
-- on different floors and switches to the roof once the cursor passes a
-- wall's top edge. No pick this frame = no valid target, period.
-- ===============================================================
local function isWallObject(obj)
    if not obj then return false end
    local ok, sprite = pcall(function() return obj:getSprite() end)
    if not ok or not sprite then return false end
    local props = sprite:getProperties()
    if not props then return false end
    return props:has("WallN") or props:has("WallW") or props:has("WallNW")
end

-- Mid-flight safety net: since the throw is no longer rejected just because
-- getCanSee failed, a wall between the player and the target needs to stop
-- the grenade itself (checked per-frame in the projectile update loop).
-- Windows smash instead of blocking -- a thrown grenade doesn't lose
-- meaningful momentum punching through glass.
local function squareHasWall(sq)
    if not sq then return false end
    local objects = sq:getObjects()
    if not objects then return false end
    local blocked = false
    for i = 0, objects:size() - 1 do
        local obj = objects:get(i)
        if instanceof(obj, "IsoWindow") then
            if not obj:isSmashed() then
                obj:smashWindow()
            end
        elseif isWallObject(obj) then
            blocked = true
        end
    end
    return blocked
end

-- Second return value: whether the picked object itself is a wall sprite
-- (WallN/WallW/WallNW) -- used to gate the post-impact ground-fall check
-- to actual wall hits only, not every elevated target (trees, roof edges).
local function getLastPickedSquare()
    local ok, lastPicked = pcall(function() return UIManager.getLastPicked() end)
    if not ok or not lastPicked then return nil, false end
    local squareOk, sq = pcall(function() return lastPicked:getSquare() end)
    if squareOk and sq then return sq, isWallObject(lastPicked) end
    return nil, false
end

-- ===============================================================
-- Prepare throw and suppress vanilla throwable
-- ===============================================================
local function OnWeaponSwing()
    local player = getPlayer()
    if not player then return end
    local weapon = player:getPrimaryHandItem()
    if not weapon then return end
    local cfg = GRENADE_CONFIGS[weapon:getFullType()]
    if not cfg then return end
    if player:isDoShove() then return end

    local targetSq = GrenadeBallistic._validTargetSq
    if not targetSq then return end

    -- Prefer the precise impact position (ground X/Y plus estimated click
    -- height on the picked object, e.g. partway up a pole) computed every
    -- aim frame -- falls back to the plain square center/floor if that
    -- somehow wasn't set (shouldn't happen while targetSq is valid).
    local targetX = GrenadeBallistic._validImpactX or (targetSq:getX() + 0.5)
    local targetY = GrenadeBallistic._validImpactY or (targetSq:getY() + 0.5)
    local targetZ = GrenadeBallistic._validImpactZ or targetSq:getZ()

    GrenadeBallistic._pendingThrow = {
        targetX = targetX,
        targetY = targetY,
        targetZ = targetZ,
        targetSq = targetSq,
        hitWall = GrenadeBallistic._validHitWall or false,
        floorsDown = GrenadeBallistic._validFloorsDown or 0,
        weaponType = weapon:getFullType(),
        weight = weapon:getWeight(),
        config = cfg,
        ignited = weapon:getFullType() == "Explosives.FlareBurning",
        ignitedAtGameHours = weapon:getModData().ignitedAtGameHours,
        -- Carried over to the placed trap on landing (placeAsTrap path)
        -- so a remote controller already paired to this specific item
        -- still triggers it -- instanceItem() at landing creates a fresh
        -- item that otherwise wouldn't have this pairing.
        remoteControlID = weapon:getRemoteControlID(),
        remoteRange = weapon:getRemoteRange(),
        throwTime = getTimestampMs(),
    }

    -- Local removal is instant client-side feedback only -- it doesn't
    -- reliably persist to the server's saved state in MP, so the actual
    -- authoritative removal is requested from the server via ExplosivesServer.lua.
    local weaponID = weapon:getID()
    player:getInventory():RemoveOneOf(weapon:getFullType())
    player:setPrimaryHandItem(nil)
    player:setSecondaryHandItem(nil)

    if isClient() then
        sendClientCommand(player, "Explosives", "ConsumeThrownWeapon", { itemID = weaponID })
    end
end
Events.OnWeaponSwing.Add(OnWeaponSwing)

-- ============================================================
-- Launch projectile after throw animation delay
-- ============================================================
Events.OnPlayerUpdate.Add(function(player)
    local pending = GrenadeBallistic._pendingThrow
    if not pending then return end

    local cfg = pending.config
    if getTimestampMs() - pending.throwTime < cfg.throwReleaseMs then return end

    GrenadeBallistic._pendingThrow = nil

    local dir = player:getForwardDirection():getDirection()
    local startX = player:getX() + math.cos(dir) * 0.1
    local startY = player:getY() + math.sin(dir) * 0.1
    -- Player's own throw-hand height (their floor + half a unit for arm height)
    local throwOriginHeight = player:getZ() + 0.5

    local dist = IsoUtils.DistanceTo(startX, startY, pending.targetX, pending.targetY)
    local isDrop, maxRange = calculateMaxRange(player, cfg, pending.floorsDown, pending.weight)

    -- determine flight time based on throw mode
    local flightTime, arcHeight, dropDistance

    if isDrop then
        -- steep drop trajectory: fall time is fixed by height, not distance
        dropDistance = pending.floorsDown
        flightTime = math.sqrt(2 * dropDistance / DROP_GRAVITY)
        arcHeight = 0
    else
        arcHeight = nil -- computed after distance clamp below
        dropDistance = 0
    end

    -- clamp target to max achievable range for this throw mode
    if dist > maxRange then
        local angle = math.atan2(pending.targetY - startY, pending.targetX - startX)
        pending.targetX = startX + math.cos(angle) * maxRange
        pending.targetY = startY + math.sin(angle) * maxRange
        dist = maxRange
        local newSq = getCell():getOrCreateGridSquare(pending.targetX, pending.targetY, math.floor(pending.targetZ))
        if newSq then pending.targetSq = newSq end
    end

    if not isDrop then
        flightTime = dist / cfg.speed
        arcHeight = dist * cfg.arcHeightFactor
    end

    if debug then
        print(string.format("[Grenade][Throw] start(%.2f,%.2f,%.2f) target(%.2f,%.2f,%.2f) dist=%.2f floorsDown=%d isDrop=%s maxRange=%.2f",
            startX, startY, throwOriginHeight, pending.targetX, pending.targetY, pending.targetZ, dist, pending.floorsDown, tostring(isDrop), maxRange))
    end

    local startSq = getCell():getOrCreateGridSquare(startX, startY, throwOriginHeight)
    if not startSq then return end
    local item = startSq:AddWorldInventoryItem(instanceItem(pending.weaponType), 0, 0, 0, false)
    if not item then return end

    -- Marks this as still mid-flight so FlareHandler.lua's ground scanner
    -- doesn't mistake the flying WorldInventoryItem (it's still technically
    -- sitting on a square every frame, just manually repositioned) for a
    -- landed flare.
    item:getModData().flying = true

    item:setWorldZRotation(dir * 360 / (2 * math.pi))
    -- No setWorldScale() override -- each item's own model defines its
    -- correct world-object scale (this mod's own items have none, i.e.
    -- the native 1.0 default; vanilla items like PipeBomb define their
    -- own, e.g. 0.6). An override here would fight that per-item value.
    local wi = item:getWorldItem()
    if wi then
        wi:setIgnoreRemoveSandbox(true)
        wi:setExtendedPlacement(false)
        wi:transmitCompleteItemToClients()
    end

    if wi and wi:getSquare() then
        wi:setOffX(startX - wi:getSquare():getX())
        wi:setOffY(startY - wi:getSquare():getY())
        wi:setOffZ(throwOriginHeight - wi:getSquare():getZ())
    end

    table.insert(GrenadeBallistic._activeProjectiles, {
        item = wi,
        startX = startX,
        startY = startY,
        flightStartHeight = throwOriginHeight,
        targetX = pending.targetX,
        targetY = pending.targetY,
        flightTargetHeight = pending.targetZ,
        targetSq = pending.targetSq,
        hitWall = pending.hitWall,
        arcHeight = arcHeight,
        flightTime = flightTime,
        isDrop = isDrop,
        dropDistance = dropDistance,
        elapsed = 0,
        weaponType = pending.weaponType,
        config = pending.config,
        ignited = pending.ignited,
        ignitedAtGameHours = pending.ignitedAtGameHours,
        remoteControlID = pending.remoteControlID,
        remoteRange = pending.remoteRange,
        flightLight = nil,
    })

    getSoundManager():PlayWorldSound("PipeBombThrow", player:getCurrentSquare(), 0, 50, 1.0, true)
end)

-- ===============================================================
-- Delayed fuse: gated behind DelayedFuseEnabled and scoped to this
-- mod's own grenades only (cfg.own), not vanilla throwables or this
-- mod's mines/Claymores (already land as placed traps) or Flare
-- (already lands lit, no explosion at all).
-- ===============================================================
local function getFuseDelaySeconds(cfg)
    if not cfg or not cfg.own then return nil end
    if not (SandboxVars.Explosives and SandboxVars.Explosives.DelayedFuseEnabled) then return nil end
    local seconds = SandboxVars.Explosives.DelayedFuseSeconds
    if not seconds or seconds <= 0 then return nil end
    return seconds
end

-- Blast radius shatters nearby windows (same radius zombies take damage
-- in, plus the floor above/below) on top of the native ExplosionPower/Range
-- damage -- purely cosmetic glass breaking, not a damage/knockback effect
-- of its own.
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
                    for i = 0, objects:size() - 1 do
                        local obj = objects:get(i)
                        if instanceof(obj, "IsoWindow") and not obj:isSmashed() then
                            obj:smashWindow()
                        end
                    end
                end
            end
        end
    end
end

-- Same idea as shatterNearbyWindows but for vehicles -- found via
-- sq:getMovingObjects() + instanceof BaseVehicle (vanilla's own
-- ISVehicleTrailerUtils.getTowableVehicleNear uses the same pattern).
-- A vehicle spans multiple squares, hence the `seen` dedupe. window:hit()
-- is the actual break call -- smashCarWindow() is just an anim/sound cue.
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

-- Shared by the immediate-explosion path and the delayed-fuse countdown
-- below, so both stay in sync instead of duplicating the FX/trap/command logic.
local function explodeGrenadeAt(weaponType, config, square, worldX, worldY, worldZ)
    if config.fxPrefix then
        ExplosionFX.spawn(worldX, worldY, worldZ, config)
    end

    if not square then return end

    local trapItem = instanceItem(weaponType)
    if trapItem then
        local trap = IsoTrap.new(getPlayer(), trapItem, getCell(), square)
        trap:triggerExplosion(false)

        local ok, explosionRange = pcall(function() return trapItem:getExplosionRange() end)
        if ok and explosionRange and explosionRange > 0 then
            local radius = math.floor(explosionRange)
            shatterNearbyWindows(square, radius)
            shatterNearbyCarWindows(square, radius)
        end
    end

    -- Local trigger above is instant client-side FX/sound only -- side
    -- effects (fire-starting, damage) don't reliably persist server-side
    -- from outside a TimedAction. The server performs its own authoritative
    -- trigger via ExplosivesServer.lua.
    if isClient() then
        sendClientCommand(getPlayer(), "Explosives", "TriggerExplosion", {
            weaponType = weaponType,
            x = square:getX(),
            y = square:getY(),
            z = square:getZ(),
        })
    end
end

-- Lands the grenade as a visible, armed WorldInventoryItem and queues it in
-- _armedGrenades instead of exploding immediately.
local function armGrenadeAt(weaponType, config, square, worldX, worldY, worldZ, fuseSeconds)
    local landedItem = square and square:AddWorldInventoryItem(instanceItem(weaponType), 0, 0, 0, false) or nil
    local landedWi = nil
    if landedItem then
        landedWi = landedItem:getWorldItem()
        if landedWi then
            landedWi:setIgnoreRemoveSandbox(true)
            landedWi:setExtendedPlacement(false)
            landedWi:transmitCompleteItemToClients()
        end
    end

    table.insert(GrenadeBallistic._armedGrenades, {
        -- WorldItem, not the InventoryItem -- removeFromSquare()/removeFromWorld()
        -- only exist on the world-side object, same as _activeProjectiles' proj.item.
        item = landedWi,
        weaponType = weaponType,
        config = config,
        square = square,
        worldX = worldX,
        worldY = worldY,
        worldZ = worldZ,
        elapsedFrames = 0,
        fuseFrames = fuseSeconds * 60,
    })
end

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

local IMPACT_MARKER_SIZE = 10 -- small dot, deliberately much smaller than the ground marker

-- Estimates how high up (world Z, ABOVE baseZ -- baseZ is already the
-- picked square's own Z, which itself already reflects roughly which floor
-- was clicked, not always 0) the mouse's actual screen position
-- corresponds to. Z is linear with screen Y in the iso projection, so
-- sampling ToScreen at two known Z values on the same X/Y gives an exact
-- pixels-per-Z scale to compare the real mouse position against.
local function estimateClickHeightOffset(mouseX, mouseY, groundX, groundY, baseZ)
    local zoom = getCore():getZoom(0)
    local scaledMouseY = mouseY * zoom

    local _, screenYBase = ISCoordConversion.ToScreen(groundX, groundY, baseZ)
    local _, screenYBasePlus1 = ISCoordConversion.ToScreen(groundX, groundY, baseZ + 1)
    local pixelsPerZ = screenYBase - screenYBasePlus1
    if pixelsPerZ == 0 then return 0 end

    return (screenYBase - scaledMouseY) / pixelsPerZ
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

-- The floor a collision is detected on often has no actual floor surface to
-- land on there (e.g. the collision square is just the outside face of a
-- tall wall that runs straight down to true ground, with nothing beside it
-- at that height) -- scan downward from startFloor for the first floor that
-- does have a real floor tile, falling back to 0 (true ground) if none found.
local function findGroundBelow(x, y, startFloor)
    local cell = getCell()
    for floor = startFloor, 0, -1 do
        local sq = cell:getGridSquare(x, y, floor)
        if sq and sq:getFloor() then
            return floor
        end
    end
    return 0
end

-- ============================================================
-- Render tick: track aim target + update projectiles
-- ============================================================
local function onPostRender()
    local player = getPlayer()
    if not player then return end

    -- Fully invalid state -- no throw possible this frame. Set at the top
    -- and only overridden below once we have a real, visible target, so
    -- every early-out path (not aiming, nothing picked, no line of sight)
    -- leaves a consistent "can't throw" state instead of stale values from
    -- a previous frame (that stale-state bug is what caused throws to fly
    -- into nowhere while the red marker was showing).
    GrenadeBallistic._validTargetSq = false
    GrenadeBallistic._validFloorsDown = 0
    GrenadeBallistic._validImpactX = nil
    GrenadeBallistic._validImpactY = nil
    GrenadeBallistic._validImpactZ = nil
    GrenadeBallistic._validHitWall = false

    -- track mouse target while aiming
    local weapon = player:getPrimaryHandItem()
    if weapon and GRENADE_CONFIGS[weapon:getFullType()] and player:isAiming() then
        local mouseX = getMouseX()
        local mouseY = getMouseY()
        local pickedSq, pickedIsWall = getLastPickedSquare()

        -- No line-of-sight gate here -- getCanSee rejected legitimate
        -- throws too often. Whatever getLastPicked resolves to (incl. Z)
        -- is used as the target; mid-flight wall collision below is the
        -- real safety net for anything actually in the way.
        if pickedSq then
            local cfg = GRENADE_CONFIGS[weapon:getFullType()]

            -- follow the mouse continuously instead of snapping to the
            -- target square's center -- that only changes tile-to-tile,
            -- which made the marker visibly jump instead of gliding.
            local zoom = getCore():getZoom(0)
            local worldX, worldY = ISCoordConversion.ToWorld(mouseX * zoom, mouseY * zoom, pickedSq:getZ())
            local markerX = worldX or (pickedSq:getX() + 0.5)
            local markerY = worldY or (pickedSq:getY() + 0.5)

            local dist = IsoUtils.DistanceTo(player:getX(), player:getY(), markerX, markerY)
            -- Floors below the player -- was hardcoded to 0 before, so
            -- downward throws always used the flat range formula instead
            -- of drop-mode.
            local floorsDown = math.max(0, math.floor(player:getZ() - pickedSq:getZ()))
            local isDrop, maxRange = calculateMaxRange(player, cfg, floorsDown, weapon:getWeight())
            local inRange = dist <= maxRange

            if debug and getTimestampMs() - (GrenadeBallistic._lastAimLogMs or 0) > 500 then
                GrenadeBallistic._lastAimLogMs = getTimestampMs()
                print(string.format("[Grenade][Aim] mouse(%d,%d) playerZ=%.1f pickedSq(%d,%d,%d) floorsDown=%d isDrop=%s wall=%s worldXY=%s marker(%.2f,%.2f) dist=%.2f maxRange=%.2f inRange=%s",
                    mouseX, mouseY, player:getZ(), pickedSq:getX(), pickedSq:getY(), pickedSq:getZ(), floorsDown, tostring(isDrop), tostring(pickedIsWall),
                    tostring(worldX ~= nil), markerX, markerY, dist, maxRange, tostring(inRange)))
            end

            if not inRange then
                -- show the marker where the throw will actually clamp to, not the raw mouse position
                local angle = math.atan2(markerY - player:getY(), markerX - player:getX())
                markerX = player:getX() + math.cos(angle) * maxRange
                markerY = player:getY() + math.sin(angle) * maxRange
            end

            -- getCanSee is display-only now (red marker as a heads-up),
            -- it no longer blocks the throw itself.
            local canSeeTarget = pickedSq:getCanSee(player:getPlayerNum())
            drawTargetMarker(markerX, markerY, pickedSq:getZ(), inRange and canSeeTarget)

            -- Where on the picked object's vertical extent the mouse
            -- actually clicked (e.g. partway up a pole), not just its
            -- ground square -- this is the real throw target, not just a
            -- debug visualization.
            local clickHeightOffset = estimateClickHeightOffset(mouseX, mouseY, markerX, markerY, pickedSq:getZ())
            -- Sanity clamp -- pixelsPerZ can get tiny (near-zero) at some
            -- zoom/angle combos, which would otherwise blow this up into an
            -- absurd Z and break getOrCreateGridSquare() downstream.
            clickHeightOffset = math.max(-2, math.min(10, clickHeightOffset))

            -- Always intercept, even out of range -- markerX/Y is already
            -- clamped to maxRange above. Gating this on inRange let vanilla
            -- take over (no fuse) whenever a throw exceeded range;
            -- OnPlayerUpdate re-clamps targetX/Y/targetSq to the real throw
            -- origin anyway, so the unclamped pickedSq here is harmless.
            GrenadeBallistic._validTargetSq = pickedSq
            GrenadeBallistic._validImpactX = markerX
            GrenadeBallistic._validImpactY = markerY
            GrenadeBallistic._validImpactZ = pickedSq:getZ() + clickHeightOffset
            GrenadeBallistic._validHitWall = pickedIsWall
            GrenadeBallistic._validFloorsDown = floorsDown

            if debug then
                drawImpactMarker(markerX, markerY, pickedSq:getZ() + clickHeightOffset)
            end
        else
            -- Nothing valid to throw at (no pick, or no line of sight) --
            -- still show the big red "can't throw here" marker, following
            -- the mouse on a flat plane at the player's own floor purely
            -- for placement (this is display-only, never used as a target).
            local zoom = getCore():getZoom(0)
            local worldX, worldY = ISCoordConversion.ToWorld(getMouseX() * zoom, getMouseY() * zoom, player:getZ())
            if worldX and worldY then
                drawTargetMarker(worldX, worldY, player:getZ(), false)
            end
        end
    end

    local timeMultiplier = getGameTime():getMultiplier()

    -- update active projectiles
    for k, proj in pairs(GrenadeBallistic._activeProjectiles) do
        proj.elapsed = proj.elapsed + timeMultiplier
        local t = proj.elapsed / (proj.flightTime * 60)

        if t >= 1.0 then
            t = 1.0
        end

        local currX = proj.startX + (proj.targetX - proj.startX) * t
        local currY = proj.startY + (proj.targetY - proj.startY) * t
        -- grenadeHeight: the grenade's actual visible height right now
        -- (includes the parabolic arc bump).
        local grenadeHeight

        if proj.isDrop then
            grenadeHeight = proj.flightStartHeight - proj.dropDistance * (t * t)
        else
            local flightPathHeight = proj.flightStartHeight + (proj.flightTargetHeight - proj.flightStartHeight) * t
            local arcBump = proj.arcHeight * 4 * t * (1 - t)
            grenadeHeight = flightPathHeight + arcBump
        end

        -- Mid-flight wall collision -- not checked during an already-active
        -- post-hit fall (proj.isDrop), since that fall happens right at a
        -- wall's own column and would otherwise re-trigger immediately.
        if t < 1.0 and not proj.isDrop then
            local wallSq = getCell():getGridSquare(math.floor(currX), math.floor(currY), math.floor(grenadeHeight))
            if wallSq and squareHasWall(wallSq) then
                t = 1.0
                proj.targetSq = wallSq
                proj.targetX = currX
                proj.targetY = currY
                proj.flightTargetHeight = grenadeHeight
                proj.hitWall = true
                if debug then
                    print(string.format("[Grenade][WallHit] mid-flight wall at (%d,%d,%d)", wallSq:getX(), wallSq:getY(), wallSq:getZ()))
                end
            end
        end

        if t >= 1.0 then
            local convertedToFall = false

            if proj.config.placeAsTrap then
                if proj.targetSq then
                    local trapItem = instanceItem(proj.weaponType)
                    if trapItem then
                        trapItem:setRemoteControlID(proj.remoteControlID)
                        trapItem:setRemoteRange(proj.remoteRange)
                        local trap = IsoTrap.new(getPlayer(), trapItem, getCell(), proj.targetSq)
                        trap:place()
                    end
                    if isClient() then
                        sendClientCommand(getPlayer(), "Explosives", "PlaceTrap", {
                            weaponType = proj.weaponType,
                            x = proj.targetSq:getX(),
                            y = proj.targetSq:getY(),
                            z = proj.targetSq:getZ(),
                            remoteControlID = proj.remoteControlID,
                            remoteRange = proj.remoteRange,
                        })
                    end
                end
            elseif proj.config.noExplosion then
                ExplosivesFlare.clearFlightLight(proj)
                if proj.targetSq then
                    local landedItem = proj.targetSq:AddWorldInventoryItem(instanceItem(proj.weaponType), 0, 0, 0, false)
                    if landedItem then
                        landedItem:getModData().ignitedAtGameHours = proj.ignitedAtGameHours
                        if proj.ignited then ExplosivesFlare.spawnGroundLight(proj.targetSq, landedItem, proj.ignitedAtGameHours) end
                        local landedWi = landedItem:getWorldItem()
                        if landedWi then
                            landedWi:setIgnoreRemoveSandbox(true)
                            landedWi:setExtendedPlacement(false)
                            landedWi:transmitCompleteItemToClients()
                        end
                    end
                end
            else
                local fuseSeconds = getFuseDelaySeconds(proj.config)
                if fuseSeconds then
                    -- Delayed-fuse grenades hitting a wall above ground
                    -- level would sit floating mid-air for the whole fuse;
                    -- fall to real ground first. Gated to wall hits only
                    -- (not trees/roof edges). Non-fused throws skip this
                    -- and detonate right at the impact point.
                    if not proj.postHitFalling and proj.targetSq and proj.hitWall then
                        local targetFloor = proj.targetSq:getZ()
                        if targetFloor > 0 then
                            -- Use the wall's own square, not the smooth mouse-
                            -- projected impact X/Y -- those can land a hair
                            -- off the wall's actual tile and made the fall
                            -- drift sideways instead of dropping straight down.
                            local gx, gy = proj.targetSq:getX(), proj.targetSq:getY()
                            local realFloor = findGroundBelow(gx, gy, targetFloor)
                            if realFloor < targetFloor then
                                local fallDistance = math.max(0.3, grenadeHeight - realFloor)
                                local fallLandingSq = getCell():getGridSquare(gx, gy, realFloor) or proj.targetSq

                                proj.postHitFalling = true
                                -- Keep X/Y at the arc's visual end
                                -- (proj.targetX/Y) -- snapping to the wall
                                -- tile's center caused a visible sideways
                                -- jump. gx/gy above is only for picking the
                                -- floor to fall to, not for repositioning.
                                proj.startX = proj.targetX
                                proj.startY = proj.targetY
                                proj.flightStartHeight = grenadeHeight
                                proj.flightTargetHeight = realFloor
                                proj.targetSq = fallLandingSq
                                proj.isDrop = true
                                proj.dropDistance = fallDistance
                                proj.flightTime = math.max(0.2, math.sqrt(2 * fallDistance / DROP_GRAVITY))
                                proj.elapsed = 0
                                convertedToFall = true

                                if debug then
                                    print(string.format("[Grenade][GroundFall] wall at (%d,%d,%d) grenadeHeight=%.2f to realFloor=%d (fallDistance=%.2f)",
                                        gx, gy, targetFloor, grenadeHeight, realFloor, fallDistance))
                                end
                            end
                        end
                    end

                    if not convertedToFall then
                        armGrenadeAt(proj.weaponType, proj.config, proj.targetSq, proj.targetX, proj.targetY, proj.flightTargetHeight, fuseSeconds)
                    end
                else
                    explodeGrenadeAt(proj.weaponType, proj.config, proj.targetSq, proj.targetX, proj.targetY, proj.flightTargetHeight)
                end
            end

            if not convertedToFall then
                if proj.item:getSquare() then
                    proj.item:getSquare():transmitRemoveItemFromSquare(proj.item)
                end
                proj.item:removeFromSquare()
                proj.item:removeFromWorld()
                GrenadeBallistic._activeProjectiles[k] = nil
            end

        else
            -- still flying, update visible position
            local wi = proj.item
            if wi and wi:getSquare() then
                local sqX = wi:getSquare():getX()
                local sqY = wi:getSquare():getY()
                local sqZ = wi:getSquare():getZ()
                wi:setOffX(currX - sqX)
                wi:setOffY(currY - sqY)
                wi:setOffZ(grenadeHeight - sqZ)
            end

            if proj.config.noExplosion and proj.ignited then
                ExplosivesFlare.updateFlightLight(proj, currX, currY, grenadeHeight)
            end
        end
    end

    -- count down grenades sitting armed on the ground (delayed fuse)
    for k, armed in pairs(GrenadeBallistic._armedGrenades) do
        armed.elapsedFrames = armed.elapsedFrames + timeMultiplier
        if armed.elapsedFrames >= armed.fuseFrames then
            explodeGrenadeAt(armed.weaponType, armed.config, armed.square, armed.worldX, armed.worldY, armed.worldZ)

            if armed.item then
                if armed.item:getSquare() then
                    armed.item:getSquare():transmitRemoveItemFromSquare(armed.item)
                end
                armed.item:removeFromSquare()
                armed.item:removeFromWorld()
            end
            GrenadeBallistic._armedGrenades[k] = nil
        end
    end

    -- render all active explosions
    ExplosionFX.render()
end

Events.OnPostRender.Add(onPostRender)