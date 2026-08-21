/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.Icon;
import org.javacord.api.event.server.ServerChangeDiscoverySplashEvent;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.ServerEventImpl;
import org.javacord.core.util.logging.LoggerUtil;

public class ServerChangeDiscoverySplashEventImpl
extends ServerEventImpl
implements ServerChangeDiscoverySplashEvent {
    private static final Logger logger = LoggerUtil.getLogger(ServerChangeDiscoverySplashEvent.class);
    private final String oldDiscoverySplashHash;
    private final String newDiscoverySplashHash;

    public ServerChangeDiscoverySplashEventImpl(ServerImpl server, String newDiscoverySplashHash, String oldDiscoverySplashHash) {
        super(server);
        this.oldDiscoverySplashHash = oldDiscoverySplashHash;
        this.newDiscoverySplashHash = newDiscoverySplashHash;
    }

    @Override
    public Optional<Icon> getOldDiscoverySplash() {
        return this.getDiscoverySplash(this.oldDiscoverySplashHash);
    }

    @Override
    public Optional<Icon> getNewDiscoverySplash() {
        return this.getDiscoverySplash(this.newDiscoverySplashHash);
    }

    private Optional<Icon> getDiscoverySplash(String discoverySplashHash) {
        if (discoverySplashHash == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new IconImpl(this.getApi(), new URL("https://cdn.discordapp.com/discovery-splashes/" + this.getServer().getIdAsString() + "/" + discoverySplashHash + ".png")));
        }
        catch (MalformedURLException e) {
            throw new AssertionError("Failed to create discovery splash url", e);
        }
    }
}

