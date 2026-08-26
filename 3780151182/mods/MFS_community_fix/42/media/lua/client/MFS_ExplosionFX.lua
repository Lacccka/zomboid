--[[---------------------------------------------------------------------------
    MFS_ExplosionFX.lua

    Sprite-frame explosion FX, driven from MFS_GrenadeBallistics.lua.

    CREDIT: adapted, with permission, from ExplosionFX.lua in
    "US Military Explosives / US Military Grenades [B42]".
    Renamed from the global `ExplosionFX` to `MFS_ExplosionFX` so both mods can
    be installed at once without one overwriting the other's table.

    The frames this reads (media/textures/FX/explosion/explosion_1..12.png) are
    copied into this overlay from Grenademod with permission. Explosives is
    listed on Steam as a required item, but is deliberately not a hard mod.info
    requirement; the local copies protect existing subscribers and preserve the
    exact tested artwork.
-----------------------------------------------------------------------------]]

MFS_ExplosionFX = MFS_ExplosionFX or {}
MFS_ExplosionFX._activeExplosions = MFS_ExplosionFX._activeExplosions or {}

function MFS_ExplosionFX.spawn(x, y, z, config)
    table.insert(MFS_ExplosionFX._activeExplosions, {
        x = x,
        y = y,
        z = z,
        frame = 1,
        maxFrames = config.fxFrames or 12,
        lastFrameTime = getTimestampMs(),
        fxPrefix = config.fxPrefix or "explosion_",
        fxSize = config.fxSize or 196,
        fxDuration = config.fxDuration or 40,
    })
end

function MFS_ExplosionFX.render()
    for k, expl in pairs(MFS_ExplosionFX._activeExplosions) do
        if expl.frame <= expl.maxFrames then
            local tex = getTexture("media/textures/FX/explosion/" .. expl.fxPrefix .. expl.frame .. ".png")
            if tex then
                local sx, sy = ISCoordConversion.ToScreen(expl.x, expl.y, expl.z)
                local size = expl.fxSize
                UIManager.DrawTexture(tex, sx - size/2, sy - size/2, size, size, 1.0)
            end
            local now = getTimestampMs()
            if now - expl.lastFrameTime > expl.fxDuration then
                expl.frame = expl.frame + 1
                expl.lastFrameTime = now
            end
        else
            MFS_ExplosionFX._activeExplosions[k] = nil
        end
    end
end
