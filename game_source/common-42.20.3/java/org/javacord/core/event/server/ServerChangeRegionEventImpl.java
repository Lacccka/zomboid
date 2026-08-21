/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.Region;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerChangeRegionEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeRegionEventImpl
extends ServerEventImpl
implements ServerChangeRegionEvent {
    private final Region newRegion;
    private final Region oldRegion;

    public ServerChangeRegionEventImpl(Server server, Region newRegion, Region oldRegion) {
        super(server);
        this.newRegion = newRegion;
        this.oldRegion = oldRegion;
    }

    @Override
    public Region getOldRegion() {
        return this.oldRegion;
    }

    @Override
    public Region getNewRegion() {
        return this.newRegion;
    }
}

