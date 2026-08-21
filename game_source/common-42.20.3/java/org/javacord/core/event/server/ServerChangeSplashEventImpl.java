/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerChangeSplashEvent;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.event.server.ServerEventImpl;
import org.javacord.core.util.logging.LoggerUtil;

public class ServerChangeSplashEventImpl
extends ServerEventImpl
implements ServerChangeSplashEvent {
    private static final Logger logger = LoggerUtil.getLogger(ServerChangeSplashEvent.class);
    private final String newSplashHash;
    private final String oldSplashHash;

    public ServerChangeSplashEventImpl(Server server, String newSplashHash, String oldSplashHash) {
        super(server);
        this.newSplashHash = newSplashHash;
        this.oldSplashHash = oldSplashHash;
    }

    @Override
    public Optional<Icon> getOldSplash() {
        return this.getSplash(this.oldSplashHash);
    }

    @Override
    public Optional<Icon> getNewSplash() {
        return this.getSplash(this.newSplashHash);
    }

    private Optional<Icon> getSplash(String splashHash) {
        if (splashHash == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new IconImpl(this.getApi(), new URL("https://cdn.discordapp.com/splashs/" + this.getServer().getIdAsString() + "/" + splashHash + ".png")));
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the url of the splash is malformed! Please contact the developer!", (Throwable)e);
            return Optional.empty();
        }
    }
}

