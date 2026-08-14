package lcc.internetradio.mixin;

import lcc.internetradio.InternetStreamBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/** Adds a narrow client-only URL-stream API without replacing FMODSoundEmitter. */
@Mixin(targets = "fmod.fmod.FMODSoundEmitter", remap = false)
public abstract class FMODSoundEmitterMixin {
    @Shadow(remap = false) public float x;
    @Shadow(remap = false) public float y;
    @Shadow(remap = false) public float z;

    @Unique public String lccInternetRadioBridgeVersion() {
        return InternetStreamBridge.VERSION;
    }

    @Unique public String lccInternetRadioBridgeLastError() {
        return InternetStreamBridge.lastError();
    }

    @Unique public long lccPlayInternetStream(String url, float minDistance,
                                              float maxDistance, float volume) {
        return InternetStreamBridge.start(url, x, y, z, minDistance, maxDistance, volume);
    }

    @Unique public boolean lccUpdateInternetStream(long channel, float volume) {
        return InternetStreamBridge.update(channel, x, y, z, volume);
    }

    @Unique public boolean lccIsInternetStreamPlaying(long channel) {
        return InternetStreamBridge.isPlaying(channel);
    }

    @Unique public void lccStopInternetStream(long channel) {
        InternetStreamBridge.stop(channel);
    }
}
