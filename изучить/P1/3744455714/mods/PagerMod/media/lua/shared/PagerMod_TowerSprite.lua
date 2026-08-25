-- ============================================================
-- PagerMod_TowerSprite.lua
-- Custom pager-tower sprite (rendered from the 3D model by
-- tools/render_tower_sprite.py). TrampleSteam pattern:
--   * register named sprites at boot: an invisible 1px base (given a solidtrans
--     flag so the object is SOLID) + one art sprite per facing;
--   * the object's MAIN sprite is the base; the visible art is an ATTACHED anim
--     (a runtime LoadSingleTexture sprite won't render via the main-sprite path);
--   * attached anims don't persist, so re-apply on OnObjectAdded/LoadGridsquare.
-- ============================================================

require "PagerMod_Shared"

local function spriteHasTexture(spr)
    if not spr then return false end
    if spr.hasNoTextures then
        local ok, none = pcall(function() return spr:hasNoTextures() end)
        if ok then return not none end
    end
    return true
end

local function ensureSprite(name, tex)
    local mgr = IsoSpriteManager and IsoSpriteManager.instance
    if not mgr then return nil end
    local spr = mgr:getSprite(name)
    if not spr then
        local ok, s = pcall(function() return mgr:AddSprite(name) end)
        spr = (ok and s) or nil
    end
    if spr and not spriteHasTexture(spr) then
        local base = tex:gsub("^media/textures/", ""):gsub("%.png$", "")
        for _, t in ipairs({ tex, base, "media/textures/" .. base }) do
            pcall(function() spr:LoadSingleTexture(t) end)
            if spriteHasTexture(spr) then break end
        end
    end
    return spr
end

function PagerMod.registerTowerSprites()
    -- Register the invisible base sprite. We deliberately do NOT touch its
    -- solidtrans flag: IsoSpriteProperties:set(IsoFlagType) throws in B41, and
    -- collision is already guaranteed on the deployed object itself
    -- (deployTower: setBlockAllTheSquare(true) + setCanPassThrough(false)), so
    -- the sprite flag is redundant.
    ensureSprite(PagerMod.TOWER_BASE_SPRITE, PagerMod.TOWER_BASE_TEX)
    for _, d in pairs(PagerMod.TOWER_SPRITES) do
        ensureSprite(d.name, d.tex)
    end
end

function PagerMod.towerSpriteReady()
    local mgr = IsoSpriteManager and IsoSpriteManager.instance
    return mgr ~= nil and spriteHasTexture(mgr:getSprite(PagerMod.TOWER_SPRITES[0].name))
end

-- Identify a deployed tower object. B41: the tower is a dropped world inventory
-- item (an IsoWorldInventoryObject holding the PagerMod.TOWER item) rendering its
-- WorldStaticModel, so match that too — plus the legacy object name / modData tag.
function PagerMod.isTowerObject(o)
    if not o then return false end
    local ok, name = pcall(function() return o:getName() end)
    if ok and name == PagerMod.TOWER_OBJECT_NAME then return true end
    if o.getModData then
        local md = o:getModData()
        if md ~= nil and md.pagerTower == true then return true end
    end
    -- B41 world-item tower: a dropped PagerTower item.
    if o.getItem then
        local oki, it = pcall(function() return o:getItem() end)
        if oki and it and it.getFullType and it:getFullType() == PagerMod.TOWER then
            return true
        end
    end
    return false
end

-- B41 build: the tower uses a real vanilla sprite assigned at deploy time (see
-- PagerMod.deployTower) because runtime-loaded custom sprites don't render as
-- world tiles here (invisible both as a main sprite and as an attached anim).
-- That vanilla sprite persists on the IsoThumpable across save/load, so there is
-- no per-object visual to re-apply — this is a no-op kept for call-site
-- compatibility (the reapply hooks below still call it).
function PagerMod.applyTowerVisual(obj)
    return
end

local function reapply(o)
    if PagerMod.isTowerObject(o) then PagerMod.applyTowerVisual(o) end
end

local function onLoadGridsquare(sq)
    if not sq then return end
    local objs = sq:getObjects()
    if not objs then return end
    for i = 0, objs:size() - 1 do reapply(objs:get(i)) end
end

Events.OnGameBoot.Add(PagerMod.registerTowerSprites)
if Events.OnServerStarted then Events.OnServerStarted.Add(PagerMod.registerTowerSprites) end
Events.OnObjectAdded.Add(reapply)
Events.LoadGridsquare.Add(onLoadGridsquare)
