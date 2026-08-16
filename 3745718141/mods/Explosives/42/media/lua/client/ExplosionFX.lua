ExplosionFX = {}
ExplosionFX._activeExplosions = {}

function ExplosionFX.spawn(x, y, z, config)
    table.insert(ExplosionFX._activeExplosions, {
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

function ExplosionFX.render()
    for k, expl in pairs(ExplosionFX._activeExplosions) do
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
            ExplosionFX._activeExplosions[k] = nil
        end
    end
end