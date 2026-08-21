/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerChangeAfkTimeoutEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeAfkTimeoutEventImpl
extends ServerEventImpl
implements ServerChangeAfkTimeoutEvent {
    private final int newAfkTimeout;
    private final int oldAfkTimeout;

    public ServerChangeAfkTimeoutEventImpl(Server server, int newAfkTimeout, int oldAfkTimeout) {
        super(server);
        this.newAfkTimeout = newAfkTimeout;
        this.oldAfkTimeout = oldAfkTimeout;
    }

    @Override
    public int getOldAfkTimeoutInSeconds() {
        return this.oldAfkTimeout;
    }

    @Override
    public int getNewAfkTimeoutInSeconds() {
        return this.newAfkTimeout;
    }
}

