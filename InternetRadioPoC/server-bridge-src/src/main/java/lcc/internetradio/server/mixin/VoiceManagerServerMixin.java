package lcc.internetradio.server.mixin;

import lcc.internetradio.server.ServerToneBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zombie.core.raknet.UdpConnection;

/** Observes fully-connected clients after vanilla refreshes VOIP routing. */
@Mixin(targets = "zombie.core.raknet.VoiceManager", remap = false)
public abstract class VoiceManagerServerMixin {
    @Inject(method = "UpdateChannelsRoaming", at = @At("TAIL"), require = 0)
    private void lccObserveVoiceConnection(UdpConnection connection, CallbackInfo callback) {
        ServerToneBridge.observe(connection);
    }
}
