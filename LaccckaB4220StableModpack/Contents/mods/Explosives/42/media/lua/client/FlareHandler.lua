-- Custom ignition + red glow for Explosives.Flare.
-- No vanilla ActivatedItem/light: uses IsoLightSource directly so the light
-- is a pure, fixed color (r,g,b) instead of the engine's white torch light.
require "ISIgniteFlareAction"

ExplosivesFlare = {}
ExplosivesFlare.COLOR = { 1.0, 0, 0 } -- pure red, max channel strength
ExplosivesFlare.HAND_RADIUS = 15
ExplosivesFlare.GROUND_RADIUS = 20
ExplosivesFlare.BURN_DURATION_GAME_MINUTES = 15 -- in-game minutes, tune later; scales with time multiplier
ExplosivesFlare.SIZZLE_RETRIGGER_MS = 4000 -- how often the crackle loop re-triggers

local FLARE_TYPE = "Explosives.Flare"
local FLARE_BURNING_TYPE = "Explosives.FlareBurning"
local FLARE_SPENT_TYPE = "Explosives.FlareSpent"

local debug = getDebug()

local handLights = {} -- [playerNum] = {light=IsoLightSource, x=, y=, z=}
local handSizzle = {} -- [playerNum] = soundId
local groundLights = {} -- list of {light=, square=, item=, expiresAt=, nextSizzleAt=}

-- Burn state lives in the item's fulltype (Flare/FlareBurning/FlareSpent)
-- so the model/icon reflect it natively. ignitedAtGameHours stays in
-- modData since it's a per-instance timestamp, not something a type can encode.
local function isIgnitedFlare(item)
    return item ~= nil and item:getFullType() == FLARE_BURNING_TYPE
end

-- PZ has no "change this item's type in place" API, so igniting/burning
-- out a held flare means removing the old instance and adding a new one
-- of the target type, preserving modData and the hand slot it occupied.
function ExplosivesFlare.swapHeldItem(player, oldItem, newFullType)
    local oldItemID = oldItem:getID()
    local primaryItem = player:getPrimaryHandItem()
    local secondaryItem = player:getSecondaryHandItem()
    local isPrimary = primaryItem ~= nil and primaryItem:getID() == oldItemID
    local isSecondary = secondaryItem ~= nil and secondaryItem:getID() == oldItemID
    local ignitedAtGameHours = oldItem:getModData().ignitedAtGameHours

    if isPrimary then player:setPrimaryHandItem(nil) end
    if isSecondary then player:setSecondaryHandItem(nil) end

    local inventory = player:getInventory()
    inventory:Remove(oldItem)
    local newItem = inventory:AddItem(newFullType)
    if newItem then
        newItem:getModData().ignitedAtGameHours = ignitedAtGameHours
        if isPrimary then player:setPrimaryHandItem(newItem) end
        if isSecondary then player:setSecondaryHandItem(newItem) end
    end

    -- Local swap above is instant client-side feedback only and doesn't
    -- reliably persist server-side in MP -- the server performs its own
    -- authoritative swap via ExplosivesServer.lua, driven by this command.
    if isClient() then
        sendClientCommand(player, "Explosives", "SwapHeldFlare", {
            oldItemID = oldItemID,
            newFullType = newFullType,
            ignitedAtGameHours = ignitedAtGameHours,
        })
    end

    return newItem
end

-- Same idea for a WorldInventoryItem lying on the ground -- preserves
-- its sub-tile offset position and ignitedAtGameHours.
function ExplosivesFlare.swapGroundItem(square, oldItem, newFullType)
    local oldWi = oldItem and oldItem:getWorldItem()
    local offX, offY, offZ = 0, 0, 0
    if oldWi then
        offX, offY, offZ = oldWi:getOffX(), oldWi:getOffY(), oldWi:getOffZ()
    end
    local ignitedAtGameHours = oldItem and oldItem:getModData().ignitedAtGameHours

    if oldWi and oldWi:getSquare() then oldWi:getSquare():transmitRemoveItemFromSquare(oldWi) end
    if oldWi then oldWi:removeFromSquare() end
    if oldItem then oldItem:removeFromWorld() end

    local newItem = square:AddWorldInventoryItem(instanceItem(newFullType), 0, 0, 0, false)
    if newItem then
        newItem:getModData().ignitedAtGameHours = ignitedAtGameHours
        local newWi = newItem:getWorldItem()
        if newWi then
            newWi:setOffX(offX)
            newWi:setOffY(offY)
            newWi:setOffZ(offZ)
            newWi:setIgnoreRemoveSandbox(true)
            newWi:setExtendedPlacement(false)
            newWi:transmitCompleteItemToClients()
        end
    end
    return newItem
end

-- ===============================================================
-- Sparkle fountain for a burning flare (hand and ground).
-- ===============================================================
local SPARKLE_TEX = "media/textures/FX/flare/sparkle.png"
local SPARKLE_SIZE = 2
local SPARKLE_DURATION_MS = 850 -- how long a spark stays visible; also caps how far it can travel regardless of speed
local SPARKLE_INTERVAL_MS = 10
local SPARKLE_COUNT_PER_SPAWN = 18 -- spawn this many at once each time the interval allows it
local SPARKLE_HEIGHT = 0.05 -- how far above the ground the spark anchor sits
local SPARKLE_GRAVITY = 3 -- tiles/sec^2, pulls the spark back down
local SPARKLE_SPEED_MIN = 0.5 -- tiles/sec, outward burst speed
local SPARKLE_SPEED_MAX = 2.5
local SPARKLE_POP_MIN = 0.8 -- tiles/sec, initial upward kick
local SPARKLE_POP_MAX = 3.0

-- The flare lies on the ground at a fixed compass angle (its model always
-- lands the same way up), so the ground fountain leans in that direction
-- instead of spraying a full 360 circle like the mid-flight fountain does.
local SPARKLE_GROUND_LEAN_DEG = 180
local SPARKLE_GROUND_TILT = 0.5 -- 0 = straight up, 1 = fully sideways along the lean direction
local SPARKLE_GROUND_SPREAD_DEG = 3 -- jitter cone around the lean direction
local SPARKLE_GROUND_POP_BIAS = 0.3 -- extra kick along the lean direction for the tip's slight upward bend

-- Nudges the spawn anchor toward the model's glowing tip, which sits
-- off-center from the item's registered position/pivot.
local SPARKLE_GROUND_ORIGIN_OFFSET_TILES = 0.15
local SPARKLE_GROUND_LEAN_RAD = SPARKLE_GROUND_LEAN_DEG * (math.pi / 180)
local SPARKLE_GROUND_ORIGIN_OFFSET_X = math.cos(SPARKLE_GROUND_LEAN_RAD) * SPARKLE_GROUND_ORIGIN_OFFSET_TILES
local SPARKLE_GROUND_ORIGIN_OFFSET_Y = math.sin(SPARKLE_GROUND_LEAN_RAD) * SPARKLE_GROUND_ORIGIN_OFFSET_TILES

local sparkles = {}

-- state is the per-source table (a groundLights entry, or a projectile)
-- that owns lastSparkleAt, so multiple burning flares each get their own
-- spawn cooldown instead of starving each other via a shared one.
local function trySpawnSparkle(state, x, y, z, leanDeg, tilt, spreadDeg, popBias)
    local now = getTimestampMs()
    if now - (state.lastSparkleAt or 0) < SPARKLE_INTERVAL_MS then return end
    state.lastSparkleAt = now

    for _ = 1, SPARKLE_COUNT_PER_SPAWN do
        local ox = (ZombRand(21) - 10) / 250 -- +/- 0.1 tile jitter
        local oy = (ZombRand(21) - 10) / 250
        local oz = ZombRand(10) / 100

        local dirDeg = leanDeg or ZombRand(360)
        if leanDeg and spreadDeg then
            dirDeg = dirDeg + (ZombRand(spreadDeg) - spreadDeg / 2)
        end
        local dirRad = dirDeg * (math.pi / 180)
        local leanAmount = tilt or 0 -- fraction of the pop redirected from straight-up into dirDeg

        local outSpeed = SPARKLE_SPEED_MIN + ZombRand(100) / 100 * (SPARKLE_SPEED_MAX - SPARKLE_SPEED_MIN)
        local popSpeed = SPARKLE_POP_MIN + (popBias or 0) + ZombRand(100) / 100 * (SPARKLE_POP_MAX - SPARKLE_POP_MIN)

        table.insert(sparkles, {
            x0 = x + ox,
            y0 = y + oy,
            z0 = z + oz,
            vx = math.cos(dirRad) * (outSpeed + popSpeed * leanAmount),
            vy = math.sin(dirRad) * (outSpeed + popSpeed * leanAmount),
            vz = popSpeed * (1 - leanAmount),
            spawnedAt = now,
        })
    end
end

local function renderSparkles()
    local now = getTimestampMs()
    for i = #sparkles, 1, -1 do
        local s = sparkles[i]
        local age = now - s.spawnedAt
        if age > SPARKLE_DURATION_MS then
            table.remove(sparkles, i)
        else
            local tex = getTexture(SPARKLE_TEX)
            if tex then
                local alpha = 1.0 - (age / SPARKLE_DURATION_MS)
                local dt = age / 1000
                local x = s.x0 + s.vx * dt
                local y = s.y0 + s.vy * dt
                local z = s.z0 + s.vz * dt - 0.5 * SPARKLE_GRAVITY * dt * dt
                if z < s.z0 - 0.1 then z = s.z0 - 0.1 end -- don't visually sink into the floor
                local sx, sy = ISCoordConversion.ToScreen(x, y, z)
                UIManager.DrawTexture(tex, sx - SPARKLE_SIZE / 2, sy - SPARKLE_SIZE / 2, SPARKLE_SIZE, SPARKLE_SIZE, alpha)
            end
        end
    end
end
Events.OnPostRender.Add(renderSparkles)

-- ===============================================================
-- Smoke puffs drifting up from a burning flare on the ground.
-- Ground-only; a held flare doesn't get smoke.
-- ===============================================================
local SMOKE_TEX = "media/textures/FX/explosion/explosion_11.png"
local SMOKE_SIZE = 70
local SMOKE_DURATION_MS = 2500
local SMOKE_INTERVAL_MS = 200
local SMOKE_RISE = 0.6 -- tiles drifted upward over its lifetime
local SMOKE_HEIGHT = 0.1

local smokePuffs = {}

-- same per-source cooldown as trySpawnSparkle above.
local function trySpawnSmoke(state, x, y, z)
    local now = getTimestampMs()
    if now - (state.lastSmokeAt or 0) < SMOKE_INTERVAL_MS then return end
    state.lastSmokeAt = now

    local ox = (ZombRand(21) - 10) / 100 -- +/- 0.1 tile jitter
    local oy = (ZombRand(21) - 10) / 100

    table.insert(smokePuffs, {
        x = x + ox,
        y = y + oy,
        z = z,
        spawnedAt = now,
    })
end

local function renderSmoke()
    local now = getTimestampMs()
    for i = #smokePuffs, 1, -1 do
        local s = smokePuffs[i]
        local age = now - s.spawnedAt
        if age > SMOKE_DURATION_MS then
            table.remove(smokePuffs, i)
        else
            local tex = getTexture(SMOKE_TEX)
            if tex then
                local t = age / SMOKE_DURATION_MS
                local alpha = 1.0
                if t > 0.4 then
                    alpha = 1.0 - ((t - 0.4) / 0.6) -- stay near-solid, then fade out over the back half
                end
                local sx, sy = ISCoordConversion.ToScreen(s.x, s.y, s.z + SMOKE_RISE * t)
                UIManager.DrawTexture(tex, sx - SMOKE_SIZE / 2, sy - SMOKE_SIZE / 2, SMOKE_SIZE, SMOKE_SIZE, alpha)
            end
        end
    end
end
Events.OnPostRender.Add(renderSmoke)

-- ===============================================================
-- Ignite (one-way, no deactivation) -- queues ISIgniteFlareAction,
-- which plays the ignite sound and animation, then swaps the item to
-- FlareBurning once that finishes. Burns out on its own after
-- BURN_DURATION_GAME_MINUTES of in-game time and can't be re-ignited.
-- ===============================================================
local function onIgniteFlare(item)
    local player = getPlayer()
    ISTimedActionQueue.add(ISIgniteFlareAction:new(player, item))
end

local function FlareContextMenu(playerNum, context, items)
    local player = getSpecificPlayer(playerNum)
    if not player then return end

    for _, v in ipairs(items) do
        local item = instanceof(v, "InventoryItem") and v or v.items[1]
        if item and item:getFullType() == FLARE_TYPE then
            if player:isHandItem(item) then
                context:addOption("Ignite Flare", item, onIgniteFlare)
            end
        end
    end
end
Events.OnFillInventoryObjectContextMenu.Add(FlareContextMenu)

-- ===============================================================
-- Catches FlareBurning items that end up on the ground without going
-- through the throw/land pipeline (dropped from inventory, placed via
-- precise-placement, spilled from a corpse, etc.), so they still get
-- their light/sparkle/sound instead of just sitting there inert.
-- ===============================================================
local UNTRACKED_SCAN_RADIUS = 3
local UNTRACKED_SCAN_INTERVAL_MS = 1000
local lastUntrackedScanAt = 0

-- Compares by unique item ID rather than Lua object identity, since an
-- InventoryItem reference obtained via worldObject:getItem() isn't
-- guaranteed to be == the original reference for the same underlying item.
local function isGroundLightTracked(item)
    local id = item:getID()
    for _, entry in ipairs(groundLights) do
        if entry.item and entry.item:getID() == id then return true end
    end
    return false
end

local function scanForUntrackedBurningFlares(player)
    local now = getTimestampMs()
    if now - lastUntrackedScanAt < UNTRACKED_SCAN_INTERVAL_MS then return end
    lastUntrackedScanAt = now

    local px, py, pz = math.floor(player:getX()), math.floor(player:getY()), math.floor(player:getZ())
    for dx = -UNTRACKED_SCAN_RADIUS, UNTRACKED_SCAN_RADIUS do
        for dy = -UNTRACKED_SCAN_RADIUS, UNTRACKED_SCAN_RADIUS do
            local sq = getCell():getGridSquare(px + dx, py + dy, pz)
            if sq then
                local worldObjects = sq:getWorldObjects()
                for i = 0, worldObjects:size() - 1 do
                    local worldObject = worldObjects:get(i)
                    local item = worldObject:getItem()
                    if item and item:getFullType() == FLARE_BURNING_TYPE and not item:getModData().flying and not isGroundLightTracked(item) then
                        ExplosivesFlare.spawnGroundLight(sq, item, item:getModData().ignitedAtGameHours)
                    end
                end
            end
        end
    end
end

-- ===============================================================
-- Red glow (+ sizzle loop) that follows the player while an
-- ignited flare is held. Extinguishes itself once burnt out.
-- ===============================================================
local function updateHandLight(player)
    local playerNum = player:getPlayerNum()

    scanForUntrackedBurningFlares(player)

    local heldItem = nil
    if isIgnitedFlare(player:getPrimaryHandItem()) then
        heldItem = player:getPrimaryHandItem()
    elseif isIgnitedFlare(player:getSecondaryHandItem()) then
        heldItem = player:getSecondaryHandItem()
    end

    if heldItem then
        local elapsedGameMinutes = (getGameTime():getWorldAgeHours() - (heldItem:getModData().ignitedAtGameHours or 0)) * 60
        if elapsedGameMinutes > ExplosivesFlare.BURN_DURATION_GAME_MINUTES then
            ExplosivesFlare.swapHeldItem(player, heldItem, FLARE_SPENT_TYPE)
            heldItem = nil
        end
    end

    local state = handLights[playerNum]
    local emitter = player:getEmitter()

    if heldItem then
        local x = math.floor(player:getX())
        local y = math.floor(player:getY())
        local z = math.floor(player:getZ())

        if not state or state.x ~= x or state.y ~= y or state.z ~= z then
            if state then
                getCell():removeLamppost(state.light)
            end
            local light = IsoLightSource.new(x, y, z, ExplosivesFlare.COLOR[1], ExplosivesFlare.COLOR[2], ExplosivesFlare.COLOR[3], ExplosivesFlare.HAND_RADIUS)
            getCell():addLamppost(light)
            handLights[playerNum] = { light = light, x = x, y = y, z = z }
        end

        if not handSizzle[playerNum] or not emitter:isPlaying(handSizzle[playerNum]) then
            handSizzle[playerNum] = emitter:playSound("flare_burn")
        end
    else
        if state then
            getCell():removeLamppost(state.light)
            handLights[playerNum] = nil
        end
        if handSizzle[playerNum] then
            emitter:stopSound(handSizzle[playerNum])
            handSizzle[playerNum] = nil
        end
    end
end
Events.OnPlayerUpdate.Add(updateHandLight)

-- ===============================================================
-- Light that follows a thrown, ignited flare mid-flight.
-- Called from GrenadeBallistics.lua's projectile update loop.
-- ===============================================================
function ExplosivesFlare.updateFlightLight(proj, x, y, z)
    local fx, fy, fz = math.floor(x), math.floor(y), math.floor(z)
    local fl = proj.flightLight
    if not fl or fl.x ~= fx or fl.y ~= fy or fl.z ~= fz then
        if fl then getCell():removeLamppost(fl.light) end
        local light = IsoLightSource.new(fx, fy, fz, ExplosivesFlare.COLOR[1], ExplosivesFlare.COLOR[2], ExplosivesFlare.COLOR[3], ExplosivesFlare.HAND_RADIUS)
        getCell():addLamppost(light)
        proj.flightLight = { light = light, x = fx, y = fy, z = fz }
    end
    trySpawnSparkle(proj, x, y, z)
end

function ExplosivesFlare.clearFlightLight(proj)
    if proj.flightLight then
        getCell():removeLamppost(proj.flightLight.light)
        proj.flightLight = nil
    end
end

-- ===============================================================
-- Stationary red glow at the spot a thrown, ignited flare lands.
-- Keeps burning for the remainder of BURN_DURATION_GAME_MINUTES
-- (measured in in-game time from the moment it was originally
-- ignited, not from landing).
-- ===============================================================
function ExplosivesFlare.spawnGroundLight(square, item, ignitedAtGameHours)
    if not square then return end

    -- the dropped item can sit at a random sub-tile offset within its
    -- square (engine scatters ground items so they don't all stack dead
    -- center) -- read the real spot instead of assuming tile center.
    -- Rotation is left to the engine's own scatter, same as any other
    -- dropped item -- deliberately not overridden.
    local x, y, z = square:getX() + 0.5, square:getY() + 0.5, square:getZ()
    local wi = item and item:getWorldItem()
    if wi then
        x = square:getX() + wi:getOffX()
        y = square:getY() + wi:getOffY()
        z = square:getZ() + wi:getOffZ()
    end

    if debug then
        print(string.format("[Flare][GroundLight] square(%d,%d,%d) light(%.3f,%.3f,%.3f)",
            square:getX(), square:getY(), square:getZ(), x, y, z))
    end

    local light = IsoLightSource.new(x, y, z, ExplosivesFlare.COLOR[1], ExplosivesFlare.COLOR[2], ExplosivesFlare.COLOR[3], ExplosivesFlare.GROUND_RADIUS)
    getCell():addLamppost(light)

    table.insert(groundLights, {
        light = light,
        square = square,
        x = x,
        y = y,
        z = z,
        item = item,
        ignitedAtGameHours = ignitedAtGameHours or getGameTime():getWorldAgeHours(),
        nextSizzleAt = getTimestampMs(),
    })
end

local function updateGroundLights()
    local now = getTimestampMs()
    local nowGameHours = getGameTime():getWorldAgeHours()

    for i = #groundLights, 1, -1 do
        local entry = groundLights[i]
        -- picked back up (or otherwise removed from the world) -- the
        -- item's WorldItem wrapper goes away once it's no longer sitting
        -- on the ground, so this is how we notice and clean up instead
        -- of leaving the light/FX behind forever.
        local pickedUp = entry.item and not entry.item:getWorldItem()
        local elapsedGameMinutes = (nowGameHours - entry.ignitedAtGameHours) * 60
        if pickedUp or elapsedGameMinutes >= ExplosivesFlare.BURN_DURATION_GAME_MINUTES then
            getCell():removeLamppost(entry.light)
            if entry.item and not pickedUp then
                ExplosivesFlare.swapGroundItem(entry.square, entry.item, FLARE_SPENT_TYPE)
            end
            table.remove(groundLights, i)
        else
            trySpawnSparkle(entry, entry.x + SPARKLE_GROUND_ORIGIN_OFFSET_X, entry.y + SPARKLE_GROUND_ORIGIN_OFFSET_Y, entry.z + SPARKLE_HEIGHT, SPARKLE_GROUND_LEAN_DEG, SPARKLE_GROUND_TILT, SPARKLE_GROUND_SPREAD_DEG, SPARKLE_GROUND_POP_BIAS)
            trySpawnSmoke(entry, entry.x, entry.y, entry.z + SMOKE_HEIGHT)
            if now >= entry.nextSizzleAt then
                entry.nextSizzleAt = now + ExplosivesFlare.SIZZLE_RETRIGGER_MS
                getSoundManager():PlayWorldSound("flare_burn", entry.square, 0, 15, 0.6, false)
            end
        end
    end
end
-- driven by render tick (not OnPlayerUpdate) so the per-source spawn
-- intervals above are the only throttle.
Events.OnPostRender.Add(updateGroundLights)
