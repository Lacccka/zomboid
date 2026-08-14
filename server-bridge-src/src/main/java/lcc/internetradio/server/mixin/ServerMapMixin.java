package lcc.internetradio.server.mixin;

import lcc.internetradio.server.ServerToneBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Deterministic Dedicated Server main-thread hook. */
@Mixin(targets = "zombie.network.ServerMap", remap = false)
public abstract class ServerMapMixin {
    @Inject(method = "preupdate", at = @At("HEAD"), require = 1)
    private void lccInternetRadioServerTick(CallbackInfo callback) {
        ServerToneBridge.serverTick();
    }
}
