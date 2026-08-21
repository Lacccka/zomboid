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
import org.javacord.api.event.server.ServerChangeIconEvent;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.event.server.ServerEventImpl;
import org.javacord.core.util.logging.LoggerUtil;

public class ServerChangeIconEventImpl
extends ServerEventImpl
implements ServerChangeIconEvent {
    private static final Logger logger = LoggerUtil.getLogger(ServerChangeIconEvent.class);
    private final String newIconHash;
    private final String oldIconHash;

    public ServerChangeIconEventImpl(Server server, String newIconHash, String oldIconHash) {
        super(server);
        this.newIconHash = newIconHash;
        this.oldIconHash = oldIconHash;
    }

    @Override
    public Optional<Icon> getOldIcon() {
        return this.getIcon(this.oldIconHash);
    }

    @Override
    public Optional<Icon> getNewIcon() {
        return this.getIcon(this.newIconHash);
    }

    private Optional<Icon> getIcon(String iconHash) {
        if (iconHash == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new IconImpl(this.getApi(), new URL("https://cdn.discordapp.com/icons/" + this.getServer().getIdAsString() + "/" + iconHash + ".png")));
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the url of the icon is malformed! Please contact the developer!", (Throwable)e);
            return Optional.empty();
        }
    }
}

