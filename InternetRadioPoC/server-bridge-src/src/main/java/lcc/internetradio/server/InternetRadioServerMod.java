package lcc.internetradio.server;

import dev.aoqia.leaf.api.ModInitializer;

/** Leaf entrypoint for the server-only transport probe. */
public final class InternetRadioServerMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerToneBridge.bootstrap();
    }
}
