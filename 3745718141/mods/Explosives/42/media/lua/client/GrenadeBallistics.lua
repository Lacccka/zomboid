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

-- Bounce state (delayed-fuse only): energy damps each hop until below threshold, then settles.
local BOUNCE_ENERGY_DAMPING = 0.45 -- tunable, energy fraction retained per bounce
local BOUNCE_ENERGY_THRESHOLD = 1.2 -- tunable, tiles; below this it settles instead of bouncing again
local BOUNCE_DISTANCE_FACTOR = 0.15 -- tunable, tiles traveled per unit of bounce energy
local BOUNCE_ARC_FACTOR = 0.015 -- tunable, arc height per unit of bounce energy
local BOUNCE_MIN_FLIGHT_TIME = 0.15 -- tunable, seconds, keeps bounce hops snappy
local BOUNCE_ANGLE_SPREAD_DEG = 15 -- tunable, +/- degrees of random drift applied each bounce
local BOUNCE_ENERGY_SCALE = 1.5 -- tunable, converts (impact height / weight) into bounce-energy tiles

-- Custom ground-plane target marker (vanilla reticle reads wrong for arced throws).
-- Pre-colored textures, no runtime tint draw call available.
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

-- FlareBurning shares physics with unlit Flare, only the item type differs.
local FLARE_CONFIG = {
    speed = 12,
    arcHeightFactor = 0.12,
    limitRange = 20,
    throwReleaseMs = 500,
    noExplosion = true, -- lands lit on the ground instead of exploding
}
GRENADE_CONFIGS["Explosives.Flare"] = FLARE_CONFIG
GRENADE_CONFIGS["Explosives.FlareBurning"] = FLARE_CONFIG

-- Mines/Claymore land as placed traps, not gated behind VanillaBallisticsEnabled (own items, not vanilla).
local MINE_THROW_CONFIG = {
    speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
    placeAsTrap = true,
}
GRENADE_CONFIGS["Explosives.M14Mine"] = MINE_THROW_CONFIG
GRENADE_CONFIGS["Explosives.M18a1Claymore"] = MINE_THROW_CONFIG
GRENADE_CONFIGS["Explosives.M18a1ClaymoreRemote"] = MINE_THROW_CONFIG

-- Vanilla throwable configs, gated behind VanillaBallisticsEnabled sandbox option.
-- Merged into GRENADE_CONFIGS at OnGameStart, once SandboxVars is populated.
local VANILLA_GRENADE_CONFIGS = {
    -- Base versions explode immediately on impact
    ["Base.PipeBomb"] = {
        speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
        fxPrefix = "explosion_", fxFrames = 12, fxSize = 314, fxDuration = 40,
    },
    ["Base.Aerosolbomb"] = {
        speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
        fxPrefix = "explosion_", fxFrames = 12, fxSize = 274, fxDuration = 40,
    },
    -- No ExplosionPower/Range; triggerExplosion() applies FireStartingEnergy/Chance natively (like M14TH3Grenade).
    ["Base.Molotov"] = {
        speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
        fxPrefix = "explosion_", fxFrames = 12, fxSize = 250, fxDuration = 40,
    },
    -- No ExplosionPower; native triggerExplosion() handles smoke/noise. No fxPrefix on purpose (no smoke FX yet).
    ["Base.SmokeBomb"] = {
        speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
    },
    -- No ExplosionPower, same fire-start as Molotov (see Base.Molotov check above).
    ["Base.FlameTrap"] = {
        speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
        fxPrefix = "explosion_", fxFrames = 12, fxSize = 250, fxDuration = 40,
    },
    -- Pure noise item, no ExplosionPower/fire, no fxPrefix (like SmokeBomb).
    ["Base.NoiseTrap"] = {
        speed = 10, arcHeightFactor = 0.1, limitRange = 20, throwReleaseMs = 500,
    },
    -- No placed/sensor/remote variants; always explodes on impact, no dedicated FX.
    ["Base.Firecracker"] = {
        speed = 12, arcHeightFactor = 0.12, limitRange = 20, throwReleaseMs = 500,
    },
    ["Base.Firecracker_Crafted"] = {
        speed = 12, arcHeightFactor = 0.12, limitRange = 20, throwReleaseMs = 500,
    },
}

-- Third-party grenades registered under the vanilla Base module. Kept separate from
-- VANILLA_GRENADE_CONFIGS for clarity. Harmless if the mod isn't installed (key never matches).
local OTHER_MOD_GRENADE_CONFIGS = {
    -- Rain's Firearms & Gun Parts (Workshop 3773858287): Base.Grenade, explodes like Base.PipeBomb.
    ["Base.Grenade"] = {
        speed = 11, arcHeightFactor = 0.11, limitRange = 18, throwReleaseMs = 500,
        fxPrefix = "explosion_", fxFrames = 12, fxSize = 392, fxDuration = 40,
    },
    -- Guns of Marz
    ["MarzGuns.M67"] = {
        speed = 11, arcHeightFactor = 0.11, limitRange = 18, throwReleaseMs = 500,
        fxPrefix = "explosion_", fxFrames = 12, fxSize = 392, fxDuration = 40,
    },
    ["MarzGuns.M18"] = {
        speed = 10, arcHeightFactor = 0.1, limitRange = 15, throwReleaseMs = 500,
    },
    ["MarzGuns.M14_Incendiary"] = {
        speed = 10, arcHeightFactor = 0.1, limitRange = 18, throwReleaseMs = 500,
        fxPrefix = "explosion_", fxFrames = 12, fxSize = 250, fxDuration = 40,
    },
    ["MarzGuns.40mm_HE_Explosion"] = {
        speed = 9, arcHeightFactor = 0.1, limitRange = 14, throwReleaseMs = 500,
        fxPrefix = "explosion_", fxFrames = 12, fxSize = 250, fxDuration = 40,
    },
    ["MarzGuns.40mm_Incendiary_Explosion"] = {
        speed = 9, arcHeightFactor = 0.1, limitRange = 14, throwReleaseMs = 500,
        fxPrefix = "explosion_", fxFrames = 12, fxSize = 250, fxDuration = 40,
    },
}
for fullType, cfg in pairs(OTHER_MOD_GRENADE_CONFIGS) do
    VANILLA_GRENADE_CONFIGS[fullType] = cfg
end

-- Sensor/Remote/Triggered variants land as placed traps; native logic + MineExplosionFX.lua handle detonation/FX.
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

-- Range scales with item weight (lighter flies further), on top of limitRange + Strength bonus. Kept modest.
local WEIGHT_RANGE_BASELINE = 0.6 -- weight at which limitRange applies unmodified
local WEIGHT_RANGE_SENSITIVITY = 8 -- tiles of range change per 1.0 weight difference
local MIN_LIMIT_RANGE = 10

local function getWeightAdjustedLimitRange(cfg, weight)
    if not weight then return cfg.limitRange end
    local delta = (WEIGHT_RANGE_BASELINE - weight) * WEIGHT_RANGE_SENSITIVITY
    return math.max(MIN_LIMIT_RANGE, cfg.limitRange + delta)
end

-- Max throw range; shared by the actual throw and the aim preview marker so they can't drift apart.
local function calculateMaxRange(player, cfg, floorsDown, weight)
    local limitRange = getWeightAdjustedLimitRange(cfg, weight)
    local strength = player:getPerkLevel(Perks.Strength)
    local rangeBonus = strength * 1.5
    local flatRange = limitRange + rangeBonus

    local isDrop = floorsDown >= DROP_MODE_FLOOR_THRESHOLD
    if isDrop then
        -- Steep drop: reach = speed * fall time. Never below flatRange.
        local flightTime = math.sqrt(2 * floorsDown / DROP_GRAVITY)
        return isDrop, math.max(flatRange, cfg.speed * flightTime)
    end

    return isDrop, flatRange
end

-- getLastPicked() is the engine's own depth-correct cursor pick (replaces the old flat pickSquare heuristic).
local function isWallObject(obj)
    if not obj then return false end
    local ok, sprite = pcall(function() return obj:getSprite() end)
    if not ok or not sprite then return false end
    local props = sprite:getProperties()
    if not props then return false end
    return props:has("WallN") or props:has("WallW") or props:has("WallNW")
end

-- Mid-flight wall check (getCanSee no longer blocks throws, so walls must stop the grenade itself).
-- Windows smash instead of block -- a thrown grenade doesn't lose meaningful momentum through glass.
local function squareHasWall(sq)
    if not sq then return false end
    local objects = sq:getObjects()
    if not objects then return false end
    local blocked = false
    -- Collect windows first, then smash: smashWindow() mutates this list mid-iteration (IndexOutOfBounds otherwise).
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

-- 2nd return: is the pick a wall sprite? Gates the post-impact ground-fall check to real wall hits only.
local function getLastPickedSquare()
    local ok, lastPicked = pcall(function() return UIManager.getLastPicked() end)
    if not ok or not lastPicked then return nil, false end
    local squareOk, sq = pcall(function() return lastPicked:getSquare() end)
    if squareOk and sq then return sq, isWallObject(lastPicked) end
    return nil, false
end

-- Prepare throw and suppress vanilla throwable
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

    -- Prefer the precise impact position computed each aim frame; falls back to square center/floor.
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
        -- Carried to the placed trap so an already-paired remote still triggers it
        -- (a fresh item on landing has no pairing otherwise).
        remoteControlID = weapon:getRemoteControlID(),
        remoteRange = weapon:getRemoteRange(),
        throwTime = getTimestampMs(),
    }

    -- Local removal is instant client feedback only; server removal via ExplosivesServer.lua is authoritative.
    local weaponID = weapon:getID()
    player:getInventory():RemoveOneOf(weapon:getFullType())
    player:setPrimaryHandItem(nil)
    player:setSecondaryHandItem(nil)

    if isClient() then
        sendClientCommand(player, "Explosives", "ConsumeThrownWeapon", { itemID = weaponID })
    end
end
Events.OnWeaponSwing.Add(OnWeaponSwing)

-- Launch projectile after throw animation delay
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

    -- Marks mid-flight so FlareHandler.lua's ground scanner doesn't mistake it for a landed flare.
    item:getModData().flying = true

    item:setWorldZRotation(dir * 360 / (2 * math.pi))
    -- No setWorldScale() override: each item's model already defines its own correct scale.
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
        -- Throw-release timestamp; docks the delayed-fuse countdown by flight/bounce time (see fuse-arming below).
        releaseTimeMs = getTimestampMs(),
        -- Bounce energy: fall height / weight (heavier bounces less). Direction = flight path heading.
        bounceEnergy = ((isDrop and dropDistance or arcHeight) / math.max(0.05, pending.weight or 0.5)) * BOUNCE_ENERGY_SCALE,
        throwDirX = dist > 0 and (pending.targetX - startX) / dist or 0,
        throwDirY = dist > 0 and (pending.targetY - startY) / dist or 0,
    })

    getSoundManager():PlayWorldSound("PipeBombThrow", player:getCurrentSquare(), 0, 50, 1.0, true)
end)

-- Delayed fuse: gated behind DelayedFuseEnabled, own grenades only (not vanilla/mines/Claymore/Flare).
local function getFuseDelaySeconds(cfg)
    if not cfg or not cfg.own then return nil end
    if not (SandboxVars.Explosives and SandboxVars.Explosives.DelayedFuseEnabled) then return nil end
    local seconds = SandboxVars.Explosives.DelayedFuseSeconds
    if not seconds or seconds <= 0 then return nil end
    return seconds
end

-- Shatters nearby windows (same radius as zombie damage, +/- one floor). Purely cosmetic.
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
                    -- Collect first, then smash: smashWindow() mutates this list mid-iteration.
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

-- Same as shatterNearbyWindows but for vehicles. `seen` dedupes multi-square vehicles.
-- window:hit() is the actual break call; smashCarWindow() is just an anim/sound cue.
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

-- Shared by immediate-explosion and delayed-fuse paths, avoids duplicating FX/trap/command logic.
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

    -- Local trigger is instant client FX/sound only; server trigger via ExplosivesServer.lua is authoritative.
    if isClient() then
        sendClientCommand(getPlayer(), "Explosives", "TriggerExplosion", {
            weaponType = weaponType,
            x = square:getX(),
            y = square:getY(),
            z = square:getZ(),
        })
    end
end

-- Lands the grenade as a visible, armed WorldInventoryItem and queues it in _armedGrenades instead of exploding immediately.
local function armGrenadeAt(weaponType, config, square, worldX, worldY, worldZ, fuseSeconds)
    local landedItem = square and square:AddWorldInventoryItem(instanceItem(weaponType), 0, 0, 0, false) or nil
    local landedWi = nil
    if landedItem then
        landedWi = landedItem:getWorldItem()
        if landedWi then
            landedWi:setIgnoreRemoveSandbox(true)
            landedWi:setExtendedPlacement(false)
            -- AddWorldInventoryItem places at square center by default; reposition to actual landing spot or it snaps visibly.
            if landedWi:getSquare() then
                landedWi:setOffX(worldX - landedWi:getSquare():getX())
                landedWi:setOffY(worldY - landedWi:getSquare():getY())
                landedWi:setOffZ(worldZ - landedWi:getSquare():getZ())
            end
            landedWi:transmitCompleteItemToClients()
        end
    end

    table.insert(GrenadeBallistic._armedGrenades, {
        -- WorldItem, not InventoryItem: removeFromSquare/removeFromWorld only exist there.
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

-- Estimates click height above baseZ: Z is linear with screen Y, so two ToScreen samples give pixels-per-Z.
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

-- Collision floor often has no floor tile there (e.g. outside face of a tall wall); scan down for a real floor, fallback 0.
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

-- Render tick: track aim target + update projectiles
local function onPostRender()
    local player = getPlayer()
    if not player then return end

    -- Reset to invalid state each frame; prevents stale values from a previous frame
    -- (was causing throws to fly into nowhere while the red marker was showing).
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

        -- No line-of-sight gate: getCanSee rejected too many legit throws.
        -- Mid-flight wall check below is the real safety net.
        if pickedSq then
            local cfg = GRENADE_CONFIGS[weapon:getFullType()]

            -- Follow mouse continuously (snapping to tile center made the marker jump instead of glide).
            local zoom = getCore():getZoom(0)
            local worldX, worldY = ISCoordConversion.ToWorld(mouseX * zoom, mouseY * zoom, pickedSq:getZ())
            local markerX = worldX or (pickedSq:getX() + 0.5)
            local markerY = worldY or (pickedSq:getY() + 0.5)

            local dist = IsoUtils.DistanceTo(player:getX(), player:getY(), markerX, markerY)
            -- Was hardcoded to 0 before, breaking drop-mode for downward throws.
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

            -- getCanSee is display-only now (red marker as a heads-up), it no longer blocks the throw itself.
            local canSeeTarget = pickedSq:getCanSee(player:getPlayerNum())
            drawTargetMarker(markerX, markerY, pickedSq:getZ(), inRange and canSeeTarget)

            -- Real throw target: where on the picked object's vertical extent the mouse clicked, not just ground square.
            local clickHeightOffset = estimateClickHeightOffset(mouseX, mouseY, markerX, markerY, pickedSq:getZ())
            -- Clamp: pixelsPerZ can get tiny at some zoom/angles, blowing this up to an absurd Z otherwise.
            clickHeightOffset = math.max(-2, math.min(10, clickHeightOffset))

            -- Always intercept even out-of-range (gating on inRange let vanilla take over);
            -- OnPlayerUpdate re-clamps targetX/Y/targetSq to the real throw origin anyway.
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
            -- No valid target: still show the red marker on a flat plane, display-only.
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
        -- Current visible height, includes the parabolic arc bump.
        local grenadeHeight

        if proj.isDrop then
            grenadeHeight = proj.flightStartHeight - proj.dropDistance * (t * t)
        else
            local flightPathHeight = proj.flightStartHeight + (proj.flightTargetHeight - proj.flightStartHeight) * t
            local arcBump = proj.arcHeight * 4 * t * (1 - t)
            grenadeHeight = flightPathHeight + arcBump
        end

        -- Skip during an active post-hit fall (proj.isDrop) -- that fall is at the wall's own column, would re-trigger.
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
                    -- Wall hits above ground level would float mid-air for the whole fuse;
                    -- fall to real ground first (wall hits only, not trees/roof edges).
                    if not proj.postHitFalling and proj.targetSq and proj.hitWall then
                        local targetFloor = proj.targetSq:getZ()
                        if targetFloor > 0 then
                            -- Use the wall's own square, not the mouse-projected X/Y (was drifting sideways instead of dropping straight).
                            local gx, gy = proj.targetSq:getX(), proj.targetSq:getY()
                            local realFloor = findGroundBelow(gx, gy, targetFloor)
                            if realFloor < targetFloor then
                                local fallDistance = math.max(0.3, grenadeHeight - realFloor)
                                local fallLandingSq = getCell():getGridSquare(gx, gy, realFloor) or proj.targetSq

                                proj.postHitFalling = true
                                -- Keep X/Y at arc's visual end (snapping to wall tile center caused a jump);
                                -- gx/gy above only picks the fall floor, not for repositioning.
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

                    -- Bounce once grounded (not mid-fall): damp energy each hop until below threshold, then settle+arm.
                    if not convertedToFall then
                        local bounceEnergy = (proj.bounceEnergy or 0) * BOUNCE_ENERGY_DAMPING
                        if bounceEnergy > BOUNCE_ENERGY_THRESHOLD and (proj.throwDirX ~= 0 or proj.throwDirY ~= 0) then
                            -- Cumulative angle drift per bounce, so it can wander further off the original heading.
                            local driftAngle = ZombRand(-BOUNCE_ANGLE_SPREAD_DEG, BOUNCE_ANGLE_SPREAD_DEG) * (math.pi / 180)
                            local cosA, sinA = math.cos(driftAngle), math.sin(driftAngle)
                            local dirX = proj.throwDirX * cosA - proj.throwDirY * sinA
                            local dirY = proj.throwDirX * sinA + proj.throwDirY * cosA
                            proj.throwDirX = dirX
                            proj.throwDirY = dirY

                            local bounceDist = bounceEnergy * BOUNCE_DISTANCE_FACTOR
                            local landFloor = proj.targetSq and proj.targetSq:getZ() or math.floor(grenadeHeight)
                            local newTargetX = proj.targetX + dirX * bounceDist
                            local newTargetY = proj.targetY + dirY * bounceDist
                            local newSq = getCell():getOrCreateGridSquare(newTargetX, newTargetY, landFloor)

                            proj.startX = proj.targetX
                            proj.startY = proj.targetY
                            proj.flightStartHeight = landFloor
                            proj.targetX = newTargetX
                            proj.targetY = newTargetY
                            proj.targetSq = newSq or proj.targetSq
                            proj.flightTargetHeight = landFloor
                            proj.arcHeight = bounceEnergy * BOUNCE_ARC_FACTOR
                            proj.flightTime = math.max(BOUNCE_MIN_FLIGHT_TIME, bounceDist / proj.config.speed)
                            proj.isDrop = false
                            proj.hitWall = false
                            proj.elapsed = 0
                            proj.bounceEnergy = bounceEnergy
                            convertedToFall = true

                            if debug then
                                print(string.format("[Grenade][Bounce] energy=%.2f dist=%.2f to(%.2f,%.2f,%d)",
                                    bounceEnergy, bounceDist, newTargetX, newTargetY, landFloor))
                            end
                        else
                            -- Fuse timer conceptually starts at release, not landing: flight/bounce time eats into it.
                            -- If already expired, detonates instantly on settling instead of the full DelayedFuseSeconds.
                            local elapsedSinceRelease = (getTimestampMs() - (proj.releaseTimeMs or getTimestampMs())) / 1000
                            local remainingFuse = math.max(0, fuseSeconds - elapsedSinceRelease)
                            armGrenadeAt(proj.weaponType, proj.config, proj.targetSq, proj.targetX, proj.targetY, proj.flightTargetHeight, remainingFuse)
                        end
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
