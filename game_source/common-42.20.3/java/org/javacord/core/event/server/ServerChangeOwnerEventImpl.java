/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerChangeOwnerEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeOwnerEventImpl
extends ServerEventImpl
implements ServerChangeOwnerEvent {
    private final long newOwnerId;
    private final long oldOwnerId;

    public ServerChangeOwnerEventImpl(Server server, long newOwnerId, long oldOwnerId) {
        super(server);
        this.newOwnerId = newOwnerId;
        this.oldOwnerId = oldOwnerId;
    }

    @Override
    public long getOldOwnerId() {
        return this.oldOwnerId;
    }

    @Override
    public long getNewOwnerId() {
        return this.newOwnerId;
    }
}

