package lcc.internetradio.server;

import dev.aoqia.leaf.api.ModInitializer;

/** Leaf entrypoint for the isolated server-managed RadioBot probe. */
public final class InternetRadioServerMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerToneBridge.bootstrap();
    }
}
