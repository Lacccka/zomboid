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

-- Identify a deployed tower object. The tower is a dropped world inventory item
-- (an IsoWorldInventoryObject holding the PagerMod.TOWER item) rendering its
-- WorldStaticModel, so match that too — plus the modData tag / legacy name.
function PagerMod.isTowerObject(o)
    if not o then return false end
    local ok, name = pcall(function() return o:getName() end)
    if ok and name == PagerMod.TOWER_OBJECT_NAME then return true end
    if o.getModData then
        local md = o:getModData()
        if md ~= nil and md.pagerTower == true then return true end
    end
    if o.getItem then
        local oki, it = pcall(function() return o:getItem() end)
        if oki and it and it.getFullType and it:getFullType() == PagerMod.TOWER then
            return true
        end
    end
    return false
end

-- No-op: the deployed tower is now rendered as a real 3D model set on the object
-- itself (obj:setSpriteModelName("PagerTower"), done SERVER-SIDE in
-- spawnTowerObject; model defined in 42/media/scripts/PagerMod_models.txt). The
-- old approach — an invisible base sprite plus a client-side attached-anim of
-- the tower art — never rendered on network-synced objects (that was the tower-
-- invisible-in-MP bug) and would now double up with the model, so it's gone.
-- Kept as a stub so any lingering callers don't error.
function PagerMod.applyTowerVisual(obj)
end

-- Still register the invisible base sprite: it is the object's MAIN sprite (the
-- model renders on top), so it must resolve on both server and client.
Events.OnGameBoot.Add(PagerMod.registerTowerSprites)
if Events.OnServerStarted then Events.OnServerStarted.Add(PagerMod.registerTowerSprites) end
