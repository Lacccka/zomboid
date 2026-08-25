--
-- True Companions - Workstation build flow (client)
--
-- Placement cursor + a walk-over/hammer timed action that builds a station PART
-- (1- or 2-tile). Used by:
--   * Build (tier 1)  -- place the first piece
--   * Add  (upgrade)  -- place a new piece adjacent to the existing group
--   * Replace (upgrade) -- rebuild a slot's piece in place (swap sprite, bump tier)
-- We run our OWN timed action instead of ISBuildAction so we can place two tiles at once
-- and tag them into a group; each tile is still placed via the engine's
-- ISSimpleFurniture:create (correct IsoThumpable creation) carrying our modData.
--
-- NOTE: ISBuildingObject / ISBaseTimedAction live in engine lua that may load AFTER this
-- file, so we build the subclasses LAZILY (ensureClasses) on first use rather than at
-- file load -- otherwise ":derive" hits a nil base class.
--

local S = BanditsNPC.Stations

local classesReady = false
local function ensureClasses()
    if classesReady then return true end
    if not (rawget(_G, "ISBuildingObject") and rawget(_G, "ISBaseTimedAction")) then return false end

    -- ===== placement cursor =====
    BanditsNPCStationCursor = ISBuildingObject:derive("BanditsNPCStationCursor")

    function BanditsNPCStationCursor:new(playerNum, job)
        local o = {}
        setmetatable(o, self)
        self.__index = self
        o:init()
        o.player = playerNum
        o.job = job
        o.name = job.piece.label
        o.canBeAlwaysPlaced = false
        o.blockAllTheSquare = true
        o.dragNilAfterPlace = true
        o.noNeedHammer = true
        o.nSprite = 1
        o.secOffX, o.secOffY = 0, 0
        return o
    end

    function BanditsNPCStationCursor:getSprite()
        local p = self.job.piece
        self.north, self.south, self.east, self.west = false, false, false, false
        if p.tiles == 2 then
            local ori = (self.nSprite % 2 == 1) and "h" or "v"
            local pair = p.ori[ori] or p.ori.h
            self.secondarySprite = pair[2]
            self.secOffX = (ori == "h") and 1 or 0
            self.secOffY = (ori == "h") and 0 or 1
            self.north = (ori == "v")
            self.chosenSprite = pair[1]
            return pair[1]
        else
            local faces = p.faces
            local idx = ((self.nSprite - 1) % #faces) + 1
            self.secondarySprite = nil
            self.secOffX, self.secOffY = 0, 0
            self.north = (self.nSprite == 2)
            self.chosenSprite = faces[idx]
            return faces[idx]
        end
    end

    function BanditsNPCStationCursor:tileFree(sq)
        if not sq then return false end
        if sq:isVehicleIntersecting() then return false end
        if not sq:isFreeOrMidair(false) then return false end
        local objs = sq:getObjects()
        for i = 0, objs:size() - 1 do
            local o = objs:get(i)
            if o and o.getModData and o:getModData().npcWorkstation then return false end
        end
        return true
    end

    -- does this square hold a same-group tile (optionally of a given slot)?
    function BanditsNPCStationCursor:squareHasGroupSlot(sq, slot)
        if not sq then return false end
        local function scan(list)
            if not list then return false end
            for i = 0, list:size() - 1 do
                local o = list:get(i)
                if o and o.getModData then
                    local md = o:getModData()
                    if md.npcWorkstationGroup == self.job.groupId and (not slot or md.npcWorkstationSlot == slot) then return true end
                end
            end
            return false
        end
        return scan(sq:getObjects()) or scan(sq:getSpecialObjects())
    end

    function BanditsNPCStationCursor:isValid(square)
        if not square then return false end
        self:getSprite()
        local z = square:getZ()
        -- tabletop (onBench) add: valid ON the station's bench surface, OR on a free
        -- adjacent tile (so you can place it on the bench or beside it).
        if self.job.onBench and self.job.mode == "add" then
            if self:squareHasGroupSlot(square, "main") and not self:squareHasGroupSlot(square, self.job.slot) then
                return true
            end
            if self:tileFree(square) and S.IsAdjacentToGroup(square:getX(), square:getY(), z, 0, 0, self.job.groupId) then
                return true
            end
            return false
        end
        -- floor placement: tiles must be free; for an ADD they must touch the group.
        if not self:tileFree(square) then return false end
        if self.secondarySprite then
            local sq2 = getCell():getGridSquare(square:getX() + self.secOffX, square:getY() + self.secOffY, z)
            if not self:tileFree(sq2) then return false end
        end
        if self.job.requireGroup then
            if not S.IsAdjacentToGroup(square:getX(), square:getY(), z, self.secOffX or 0, self.secOffY or 0, self.job.groupId) then
                return false
            end
        end
        return true
    end

    function BanditsNPCStationCursor:render(x, y, z, square)
        local spriteName = self:getSprite()
        if not self.RENDER_SPRITE then self.RENDER_SPRITE = IsoSprite.new() end
        if self.RENDER_SPRITE_NAME ~= spriteName then
            self.RENDER_SPRITE:LoadSingleTexture(spriteName); self.RENDER_SPRITE_NAME = spriteName
        end
        local ok = self:isValid(square)
        if ok then self.RENDER_SPRITE:RenderGhostTile(x, y, z) else self.RENDER_SPRITE:RenderGhostTileRed(x, y, z) end
        if self.secondarySprite then
            if not self.RENDER_SPRITE2 then self.RENDER_SPRITE2 = IsoSprite.new() end
            if self.RENDER_SPRITE2_NAME ~= self.secondarySprite then
                self.RENDER_SPRITE2:LoadSingleTexture(self.secondarySprite); self.RENDER_SPRITE2_NAME = self.secondarySprite
            end
            local x2, y2 = x + self.secOffX, y + self.secOffY
            if ok then self.RENDER_SPRITE2:RenderGhostTile(x2, y2, z) else self.RENDER_SPRITE2:RenderGhostTileRed(x2, y2, z) end
        end
    end

    function BanditsNPCStationCursor:tryBuild(x, y, z)
        self:getSprite()
        local job = self.job
        local placeJob = {
            x = x, y = y, z = z,
            primarySprite = self.chosenSprite, secondarySprite = self.secondarySprite,
            secOffX = self.secOffX or 0, secOffY = self.secOffY or 0, north = self.north,
            piece = job.piece, pieceId = job.pieceId, stationType = job.stationType,
            slot = job.slot, mode = job.mode, groupId = job.groupId, tier = job.tier,
            label = job.piece.label,
        }
        getCell():setDrag(nil, self.player)
        S.QueueConstruct(self.player, placeJob)
    end

    -- ===== timed action (walk-over + hammer) =====
    ISBanditsBuildAction = ISBaseTimedAction:derive("ISBanditsBuildAction")
    function ISBanditsBuildAction:isValid() return true end
    function ISBanditsBuildAction:update() end
    function ISBanditsBuildAction:start()
        local anim = (self.job.piece.tiles == 2) and CharacterActionAnims.Build or CharacterActionAnims.BuildLow
        self:setActionAnim(anim)
        local cheat = false; pcall(function() cheat = self.character:isBuildCheat() end)
        if not cheat then
            pcall(function()
                local hammer = self.character:getInventory():getFirstTagEvalRecurse(ItemTag.HAMMER, function(it) return not it:isBroken() end)
                if hammer then ISInventoryPaneContextMenu.equipWeapon(hammer, true, false, self.character:getPlayerNum()) end
            end)
        end
        pcall(function() self.character:playSound("BuildWoodenStructureSmall") end)
    end
    function ISBanditsBuildAction:stop() ISBaseTimedAction.stop(self) end
    function ISBanditsBuildAction:perform()
        S.DoConstruct(self.character, self.job)
        ISBaseTimedAction.perform(self)
    end
    function ISBanditsBuildAction:new(character, job)
        local o = ISBaseTimedAction.new(self, character)
        o.job = job
        o.maxTime = character:isTimedActionInstant() and 1 or 220
        o.stopOnWalk = true
        o.stopOnRun = true
        o.caloriesModifier = 4
        return o
    end

    classesReady = true
    return true
end

-- ===== orchestration =====
function S.StartCursor(player, job)
    if not (player and getCell) then return end
    if not ensureClasses() then return end
    local cur = BanditsNPCStationCursor:new(player:getPlayerNum(), job)
    getCell():setDrag(cur, player:getPlayerNum())
end

function S.QueueConstruct(playerNum, job)
    if not ensureClasses() then return end
    local player = getSpecificPlayer(playerNum)
    if not player then return end
    local sq = getCell():getGridSquare(job.x, job.y, job.z)
    if not sq then return end
    -- always walk ADJACENT to the build tile (it may be occupied -- e.g. the bench we're
    -- replacing -- so we can't path onto it). luautils.walkAdj queues the walk. We walk
    -- even under build-cheat (cheat only skips the material cost, not the walk/animation).
    local cheat = false; pcall(function() cheat = player:isBuildCheat() end)
    local reached = luautils.walkAdj(player, sq)
    if not reached and not cheat then
        pcall(function() player:Say(BanditsNPC.T("UI_BN_Ws_CantReach", "Can't reach the spot to build.")) end)
        return
    end
    ISTimedActionQueue.add(ISBanditsBuildAction:new(player, job))
end

-- actually place/replace the tiles (runs at the end of the build action).
function S.DoConstruct(player, job)
    local piece = job.piece
    local cheat = false; pcall(function() cheat = player:isBuildCheat() end)
    -- PAY BEFORE PLACING, AND ABORT IF THE PAYMENT FAILS (v0.75.6, audit Cluster E).
    -- Affordability is decided when the MENU is built, then the player walks to the site
    -- and hammers for several seconds -- a real window in which the materials can be
    -- dropped, stashed or used. TakeMaterials now re-checks and takes nothing unless it
    -- can take everything, so a shortfall here means the player no longer has the parts
    -- and the piece must not appear. The beacon's own upgrade had the mirror-image bug
    -- and the same helper fixes both.
    if not cheat then
        local paid, missing = S.TakeMaterials(player, piece.materials)
        if not paid then
            pcall(function()
                player:Say(BanditsNPC.TF("UI_BN_Ws_NeedParts", "I still need: %1", missing or "?"))
            end)
            return
        end
    end

    local groupId = job.groupId
    if not groupId then
        groupId = "bnws_" .. job.stationType .. "_" .. tostring((getTimestampMs and getTimestampMs()) or 0) .. "_" .. ZombRand(1000000)
    end
    local tier = job.tier or 1

    if job.mode == "replace" and job.groupId then
        for _, o in ipairs(S.FindGroupTiles(job.groupId, job.x, job.y, job.z, 8)) do
            if o:getModData().npcWorkstationSlot == job.slot then
                local sq = o:getSquare()
                if sq then sq:transmitRemoveItemFromSquare(o) end
            end
        end
    end

    local function placeTile(x, y, z, spriteName, isPrimary)
        if not spriteName then return end
        local fc = ISSimpleFurniture:new(piece.label, spriteName, spriteName)
        fc.player = player:getPlayerNum()
        fc.dismantable = false
        fc.modData = {
            npcWorkstation = job.stationType,
            npcWorkstationTier = tier,
            npcWorkstationGroup = groupId,
            npcWorkstationSlot = job.slot,
            npcWorkstationPiece = job.pieceId,
            npcWorkstationPrimary = isPrimary and true or false,
        }
        fc:create(x, y, z, job.north and true or false, spriteName)
        -- make it solid + recompute the square so you can't walk through it
        local obj = fc.javaObject
        if obj then
            pcall(function()
                obj:setBlockAllTheSquare(true)
                obj:setCanPassThrough(false)
                local sq = obj:getSquare()
                if sq then sq:RecalcAllWithNeighbours(true) end
            end)
        end
    end
    placeTile(job.x, job.y, job.z, job.primarySprite, true)
    if job.secondarySprite then
        placeTile(job.x + (job.secOffX or 0), job.y + (job.secOffY or 0), job.z, job.secondarySprite, false)
    end

    S.SetGroupTier(groupId, tier, job.x, job.y, job.z)
    -- Two LITERAL TF calls rather than one string.format over a chosen T(): the key
    -- scanner only sees a key written out at the call site, so picking the key with a
    -- conditional would leave both of these untranslatable and invisible to it.
    if player.Say then pcall(function()
        player:Say((job.mode == "replace")
            and BanditsNPC.TF("UI_BN_Ws_Rebuilt", "%1 rebuilt.", piece.label)
            or  BanditsNPC.TF("UI_BN_Ws_Built", "%1 built.", piece.label))
    end) end
end

function S.SetGroupTier(groupId, tier, x, y, z)
    for _, o in ipairs(S.FindGroupTiles(groupId, x, y, z, 8)) do
        o:getModData().npcWorkstationTier = tier
        pcall(function() o:transmitCompleteItemToClients() end)
    end
end

function S.RemoveGroup(groupId, x, y, z)
    for _, o in ipairs(S.FindGroupTiles(groupId, x, y, z, 8)) do
        local sq = o:getSquare()
        if sq then pcall(function() sq:transmitRemoveItemFromSquare(o) end) end
    end
end

-- Where/how to place a REPLACEMENT piece for a slot: reuse the existing slot tiles'
-- squares + orientation so a 2-tile bench keeps its footprint (and the 2nd tile never
-- lands on an occupied square). Returns a placement table or nil.
function S.SlotPlacement(groupId, slot, origin, piece)
    local tiles = {}
    for _, o in ipairs(S.FindGroupTiles(groupId, origin.x, origin.y, origin.z, 8)) do
        if o:getModData().npcWorkstationSlot == slot then table.insert(tiles, o) end
    end
    if #tiles == 0 then return nil end
    local prim, other
    for _, o in ipairs(tiles) do
        if o:getModData().npcWorkstationPrimary then prim = o else other = o end
    end
    prim = prim or tiles[1]
    local psq = prim:getSquare()
    local z = psq:getZ()
    if piece.tiles == 2 and other then
        local osq = other:getSquare()
        local ax, ay, bx, by = psq:getX(), psq:getY(), osq:getX(), osq:getY()
        if ax == bx then
            return { x = ax, y = math.min(ay, by), z = z, primarySprite = piece.ori.v[1], secondarySprite = piece.ori.v[2], secOffX = 0, secOffY = 1, north = true }
        end
        return { x = math.min(ax, bx), y = ay, z = z, primarySprite = piece.ori.h[1], secondarySprite = piece.ori.h[2], secOffX = 1, secOffY = 0, north = false }
    elseif piece.tiles == 2 then
        return { x = psq:getX(), y = psq:getY(), z = z, primarySprite = piece.ori.h[1], secondarySprite = piece.ori.h[2], secOffX = 1, secOffY = 0, north = false }
    end
    return { x = psq:getX(), y = psq:getY(), z = z, primarySprite = piece.faces[1], secOffX = 0, secOffY = 0, north = false }
end

-- ===== high-level entry points (called by the UI / world menu) =====
function S.HasHammer(player)
    local ok, h = pcall(function()
        return player:getInventory():getFirstTagEvalRecurse(ItemTag.HAMMER, function(it) return not it:isBroken() end)
    end)
    return ok and (h ~= nil)
end

function S.GetNearbyStation(stationType, x, y, z, radius)
    local tile = S.FindStationTile(stationType, x, y, z, radius or 20)
    if not tile then return nil end
    local md = tile:getModData()
    local sq = tile:getSquare()
    return { tile = tile, groupId = md.npcWorkstationGroup, tier = md.npcWorkstationTier or 1,
             origin = { x = sq:getX(), y = sq:getY(), z = sq:getZ() } }
end

function S.NextUpgrade(stationType, curTier)
    if curTier >= S.MAX_TIER then return nil end
    local diff = S.UpgradeDiff(stationType, curTier, curTier + 1)
    local op = (diff.replace[1] and { kind = "replace", part = diff.replace[1] })
            or (diff.add[1] and { kind = "add", part = diff.add[1] })
    if not op then return nil end
    local piece = S.Pieces[op.part.piece]
    return { toTier = curTier + 1, kind = op.kind, part = op.part, piece = piece,
             materials = piece.materials, skill = piece.skill, power = piece.power, tiles = piece.tiles }
end

function S.BeginBuildTier1(player, stationType)
    local parts = S.TierParts(stationType, 1)
    if not (parts and parts[1]) then return end
    local p = parts[1]
    S.StartCursor(player, { piece = S.Pieces[p.piece], pieceId = p.piece, stationType = stationType,
                            slot = p.slot, mode = "build", groupId = nil, tier = 1 })
end

function S.BeginUpgrade(player, stationType, station)
    local up = S.NextUpgrade(stationType, station.tier)
    if not up then return false, BanditsNPC.T("UI_BN_Ws_AlreadyTop", "Already at the top tier.") end
    local okSkill, sm = S.MeetsSkill(player, up.skill)
    if not okSkill then return false, BanditsNPC.TF("UI_BN_Ws_NeedSkillMsg", "Need %1.", sm) end
    local okMat, mm = S.HasMaterials(player, up.materials)
    if not okMat then return false, BanditsNPC.TF("UI_BN_Ws_MissingMsg", "Missing: %1.", mm) end
    if not S.HasHammer(player) then return false, BanditsNPC.T("UI_BN_Ws_NeedHammer", "Need a hammer.") end

    if up.kind == "add" then
        S.StartCursor(player, { piece = up.piece, pieceId = up.part.piece, stationType = stationType,
                                slot = up.part.slot, mode = "add", groupId = station.groupId, tier = up.toTier,
                                requireGroup = true, onBench = up.piece.onBench, groupOrigin = station.origin })
        return true, (up.piece.onBench and BanditsNPC.T("UI_BN_Ws_PlaceOnBench", "Place it on the workstation surface (or beside it).")
                                     or BanditsNPC.T("UI_BN_Ws_PlaceBeside", "Place the new piece next to the station."))
    else
        local pl = S.SlotPlacement(station.groupId, up.part.slot, station.origin, up.piece)
        if not pl then return false, BanditsNPC.T("UI_BN_Ws_NoPartToRebuild", "Couldn't find the part to rebuild.") end
        S.QueueConstruct(player:getPlayerNum(), {
            x = pl.x, y = pl.y, z = pl.z,
            primarySprite = pl.primarySprite, secondarySprite = pl.secondarySprite,
            secOffX = pl.secOffX, secOffY = pl.secOffY, north = pl.north,
            piece = up.piece, pieceId = up.part.piece, stationType = stationType, slot = up.part.slot,
            mode = "replace", groupId = station.groupId, tier = up.toTier, label = up.piece.label,
        })
        return true, BanditsNPC.T("UI_BN_Ws_Rebuilding", "Rebuilding the station...")
    end
end
