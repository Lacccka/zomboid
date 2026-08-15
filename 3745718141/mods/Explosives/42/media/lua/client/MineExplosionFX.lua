require "client/ExplosionFX"

-- Explosion FX for placed traps -- this mod's own mines (M14Mine,
-- M18a1Claymore, M18a1ClaymoreRemote) plus vanilla PipeBomb/Aerosolbomb/
-- FlameTrap sensor and remote variants -- reusing the same frame-based
-- explosion animation the thrown grenades use. These are all placed
-- traps triggered natively by vanilla's own sensor/timer/remote logic
-- (b42.20 fixed the native sensor bug that used to require a manual
-- workaround for mines), so this only needs to catch the moment of
-- detonation via Events.OnThrowableExplode, not simulate the trigger itself.
local MINE_FX = {
    M14Mine = {
        fxPrefix = "explosion_",
        fxFrames = 12,
        fxSize = 196,
        fxDuration = 40,
    },
    M18a1Claymore = {
        fxPrefix = "explosion_",
        fxFrames = 12,
        fxSize = 588,
        fxDuration = 80,
    },
}
MINE_FX.M18a1ClaymoreRemote = MINE_FX.M18a1Claymore

-- Vanilla PipeBomb/Aerosolbomb sensor and remote variants explode the
-- same way whether they were placed directly (vanilla, always possible)
-- or landed via this mod's optional ballistic arc (VanillaBallisticsEnabled
-- sandbox option) -- either way this is just cosmetic FX for a detonation
-- that already happens, so it's not gated behind that option.
local PIPEBOMB_FX = {
    fxPrefix = "explosion_",
    fxFrames = 12,
    fxSize = 314,
    fxDuration = 40,
}
local AEROSOLBOMB_FX = {
    fxPrefix = "explosion_",
    fxFrames = 12,
    fxSize = 274,
    fxDuration = 40,
}
MINE_FX.PipeBombSensorV1 = PIPEBOMB_FX
MINE_FX.PipeBombSensorV2 = PIPEBOMB_FX
MINE_FX.PipeBombSensorV3 = PIPEBOMB_FX
MINE_FX.PipeBombRemote = PIPEBOMB_FX
MINE_FX.PipeBombTriggered = PIPEBOMB_FX
MINE_FX.AerosolbombSensorV1 = AEROSOLBOMB_FX
MINE_FX.AerosolbombSensorV2 = AEROSOLBOMB_FX
MINE_FX.AerosolbombSensorV3 = AEROSOLBOMB_FX
MINE_FX.AerosolbombRemote = AEROSOLBOMB_FX
MINE_FX.AerosolbombTriggered = AEROSOLBOMB_FX
local FLAMETRAP_FX = {
    fxPrefix = "explosion_",
    fxFrames = 12,
    fxSize = 250,
    fxDuration = 40,
}
MINE_FX.FlameTrapSensorV1 = FLAMETRAP_FX
MINE_FX.FlameTrapSensorV2 = FLAMETRAP_FX
MINE_FX.FlameTrapSensorV3 = FLAMETRAP_FX
MINE_FX.FlameTrapRemote = FLAMETRAP_FX
MINE_FX.FlameTrapTriggered = FLAMETRAP_FX
-- SmokeBomb and NoiseTrap (Sensor/Remote/Triggered) deliberately have no
-- entry here -- no dedicated smoke/noise FX exists yet, so their
-- detonation stays silent on the custom-FX side (native smoke/noise
-- effects still happen either way).

if Events.OnThrowableExplode then
    Events.OnThrowableExplode.Add(function(throwable, sq)
        if not throwable or not sq then return end
        local item = throwable:getItem()
        local fxCfg = item and MINE_FX[item:getType()]
        if fxCfg then
            ExplosionFX.spawn(sq:getX() + 0.5, sq:getY() + 0.5, sq:getZ(), fxCfg)
        end
    end)
end
