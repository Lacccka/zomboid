--
-- Bandits NPC - Companions Overhaul - Spawning (client glue)
--
-- Adds "Spawn NPC..." to the ground right-click menu (opens the spawn
-- window), builds the SurvivorDesc used for the 3D preview, and sends the
-- spawn request to our server handler (see server/BanditsNPCSpawnServer.lua).
--

require "ISUI/BanditsNPCSpawnWindow"
require "ISUI/BanditsNPCEditorWindow"

BanditsNPC = BanditsNPC or {}
BanditsNPC.Spawn = BanditsNPC.Spawn or {}

-- Picks a random clan that is both friendly and companion-capable. Falls back
-- to any clan if none match.
function BanditsNPC.Spawn.PickRandomCompanionClan()
    BanditCustom.Load()
    local clanData = BanditCustom.ClanGetAll()

    local candidates = {}
    for cid, clan in pairs(clanData) do
        if clan.spawn and clan.spawn.friendly and clan.spawn.companion then
            table.insert(candidates, cid)
        end
    end

    if #candidates == 0 then
        for cid, _ in pairs(clanData) do
            table.insert(candidates, cid)
        end
    end

    if #candidates == 0 then return nil end
    return candidates[ZombRand(#candidates) + 1]
end

-- Builds a clothed SurvivorDesc from an NPC profile for the 3D preview.
-- Mirrors the visual fields Bandit.ApplyVisuals applies to a live bandit,
-- but targets a desc instead of an entity. Caller wraps this in pcall.
function BanditsNPC.Spawn.BuildPreviewDesc(bandit)
    local g = bandit.general or {}
    local female = g.female or false

    local desc = SurvivorFactory.CreateSurvivor()
    desc:setFemale(female)

    local hv = desc:getHumanVisual()
    hv:clear()

    if g.skin then
        hv:setSkinTextureName(Bandit.GetSkinTexture(female, g.skin))
    end
    if g.hairType then
        hv:setHairModel(Bandit.GetHairStyle(female, g.hairType))
    end
    if not female and g.beardType then
        local beard = Bandit.GetBeardStyle(female, g.beardType)
        if beard then hv:setBeardModel(beard) end
    end
    if g.hairColor then
        local hc = Bandit.GetHairColor(g.hairColor)
        if hc then
            local ic = ImmutableColor.new(hc.r, hc.g, hc.b, 1)
            hv:setHairColor(ic)
            hv:setBeardColor(ic)
        end
    end

    desc:getWornItems():clear()
    if bandit.clothing then
        for bodyLocation, itemType in pairs(bandit.clothing) do
            local realType = BanditCompatibility.GetLegacyItem(itemType)
            local item = BanditCompatibility.InstanceItem(realType)
            if item then
                -- guard each item so one bad location can't kill the preview
                pcall(function()
                    -- apply the profile's saved tint so preview colors match
                    -- the actual spawn (Bandit.ApplyVisuals does the same).
                    if bandit.tint and bandit.tint[bodyLocation] and item.getVisual then
                        local c = BanditUtils.dec2rgb(bandit.tint[bodyLocation])
                        local iv = item:getVisual()
                        if c and iv then
                            iv:setTint(ImmutableColor.new(c.r, c.g, c.b, 1))
                        end
                    end
                    desc:setWornItem(ItemBodyLocation.get(ResourceLocation.of(bodyLocation)), item)
                end)
            end
        end
    end

    return desc
end

-- Sends a spawn request to the server. bid may be nil (= random member of cid).
function BanditsNPC.Spawn.SendSpawn(cid, bid, program, x, y, z)
    local player = getSpecificPlayer(0)
    if not player then return end

    local args = {
        cid = cid,
        bid = bid,
        program = program or "Companion",
        x = x or player:getX(),
        y = y or player:getY(),
        z = z or player:getZ(),
    }
    sendClientCommand(player, 'BanditsNPC', 'Spawn', args)
    print("[BanditsNPC] Spawn request sent (cid=" .. tostring(cid) ..
          ", bid=" .. tostring(bid) .. ", program=" .. tostring(program) .. ").")
end

function BanditsNPC.Spawn.OpenWindow(player, square)
    BanditsNPCSpawnWindow.OpenAt(square)
end

local function onFillWorldObjectContextMenu(playerID, context, worldobjects, test)
    if test then return end

    local player = getSpecificPlayer(playerID)
    if not player then return end
    if not BanditCustom then return end

    local square
    if BanditCompatibility and BanditCompatibility.GetClickedSquare then
        square = BanditCompatibility.GetClickedSquare()
    end
    if not square and worldobjects then
        for _, o in ipairs(worldobjects) do
            if o.getSquare then
                square = o:getSquare()
                if square then break end
            end
        end
    end
    if not square then return end

    -- Spawn NPC and the editor are debug/admin tools. In normal play, companions
    -- come from the wild-spawn system; crafting stations are assigned from the
    -- NPC's Work tab ("Select Workstation"), so no "Place Workstation" option.
    local debugTools = isDebugEnabled() or isAdmin()

    if debugTools then
        context:addOption(BanditsNPC.T("UI_BN_Ctx_SpawnNPC", "Spawn NPC..."), player, BanditsNPC.Spawn.OpenWindow, square)
    end

    if debugTools and BanditsNPCEditorWindow then
        context:addOption(BanditsNPC.T("UI_BN_Ctx_Editor", "NPC Core Editor"), player, function() BanditsNPCEditorWindow.OpenIt() end)
    end
end

Events.OnFillWorldObjectContextMenu.Add(onFillWorldObjectContextMenu)

-- ===== LIVE SURVIVOR MARKER (29 Jul, author ask) =====
-- The arrival icon used to be a ONE-SHOT marker pinned to the spawn tile: it was created
-- with BanditEventMarkerHandler.set(getRandomUUID(), ...), and because the id was a fresh
-- random UUID every time it could never be found again, so it could never be moved. A
-- neutral survivor wanders (NPCNeutral), so within a minute the arrow pointed at an empty
-- patch of ground the survivor had already left.
--
-- Bandits' own marker API already supports this properly -- set() ends with
--     if marker then marker:setDuration(duration); marker:update(posX, posY) end
-- so calling set() again with the SAME id repositions the existing marker. We just never
-- used a stable id. Here we re-point it at the survivor's real position a few times a
-- second, and hide it (duration 0) the moment there is nobody left to point at.
--
-- Cost: BanditZombie.CacheLightB holds brains only (a handful of entries), and we read the
-- live position off the cached zombie object rather than the cache's stale x/y -- the cache
-- itself is only rebuilt once a minute, which would have made the arrow lag badly.
local MARKER_ID = "bnwsArrival"
local MARKER_ICON = "media/ui/friend.png"
local MARKER_COLOR = { r = 0.5, g = 1, b = 0.5 }
local lastMarkerMs = 0
local markerShown = false

local function arrivalIconEnabled()
    return not (SandboxVars and SandboxVars.Bandits
                and SandboxVars.Bandits.General_ArrivalIcon == false)
end

-- nearest UN-RECRUITED, non-hostile bandit NPC, with its LIVE position. Returns x, y or nil.
local function nearestRecruitableSurvivor(player)
    local cacheB = BanditZombie and BanditZombie.CacheLightB
    local cache = BanditZombie and BanditZombie.Cache
    if not (cacheB and cache) then return nil end
    local px, py, pz = player:getX(), player:getY(), player:getZ()
    local bestX, bestY, bestD
    for id, light in pairs(cacheB) do
        local brain = light.brain
        if brain and not brain.recruited and not (brain.hostile or brain.hostileP) then
            local z = cache[id]
            local ok, alive = pcall(function() return z and z:isAlive() end)
            if ok and alive then
                local zx, zy, zz = z:getX(), z:getY(), z:getZ()
                if math.abs(zz - pz) < 2 then
                    local d = BanditUtils.DistTo(px, py, zx, zy)
                    if not bestD or d < bestD then bestD, bestX, bestY = d, zx, zy end
                end
            end
        end
    end
    return bestX, bestY
end

local function updateSurvivorMarker()
    if not arrivalIconEnabled() then return end
    local now = getTimestampMs and getTimestampMs() or 0
    if now - lastMarkerMs < 400 then return end   -- ~2x/second is plenty for an arrow
    lastMarkerMs = now
    pcall(function()
        if not (BanditEventMarkerHandler and BanditEventMarkerHandler.set) then return end
        local player = getSpecificPlayer(0)
        if not player or player:isDead() then return end
        local x, y = nearestRecruitableSurvivor(player)
        if x then
            -- same id every call -> set() finds the marker and repositions it
            BanditEventMarkerHandler.set(MARKER_ID, MARKER_ICON, 1800, x, y, MARKER_COLOR,
                BanditsNPC.T("UI_BN_Marker_Survivor", "Survivor nearby"))
            markerShown = true
        elseif markerShown then
            -- nobody left to point at (recruited, dead, or streamed out): hide it. A
            -- duration of 0 makes the marker invisible and lets Bandits' own
            -- RemoveOldMarkers sweep it.
            markerShown = false
            BanditEventMarkerHandler.set(MARKER_ID, MARKER_ICON, 0, x or 0, y or 0, MARKER_COLOR, "")
        end
    end)
end

Events.OnPlayerUpdate.Add(updateSurvivorMarker)
