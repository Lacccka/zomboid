require "ExplosionFX"

-- Explosion FX for placed traps (own mines + vanilla PipeBomb/Aerosolbomb/FlameTrap
-- sensor/remote variants), reusing the thrown-grenade animation. Native sensor/timer/
-- remote logic handles triggering (b42.20 fixed the sensor bug); this just catches
-- detonation via Events.OnThrowableExplode.
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

-- Same FX whether placed directly (vanilla) or landed via VanillaBallisticsEnabled;
-- purely cosmetic, not gated behind that option.
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
-- SmokeBomb/NoiseTrap variants have no entry: no dedicated FX yet (native smoke/noise still happens).

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
