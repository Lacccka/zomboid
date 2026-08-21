/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerEvent;
import org.javacord.core.event.EventImpl;

public abstract class ServerEventImpl
extends EventImpl
implements ServerEvent {
    private final Server server;

    public ServerEventImpl(Server server) {
        super(server.getApi());
        this.server = server;
    }

    @Override
    public Server getServer() {
        return this.server;
    }
}

