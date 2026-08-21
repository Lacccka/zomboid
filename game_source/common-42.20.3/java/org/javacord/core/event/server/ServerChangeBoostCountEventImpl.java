/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.event.server.ServerChangeBoostCountEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeBoostCountEventImpl
extends ServerEventImpl
implements ServerChangeBoostCountEvent {
    private final int oldBoostCount;
    private final int newBoostCount;

    public ServerChangeBoostCountEventImpl(ServerImpl server, int newBoostCount, int oldBoostCount) {
        super(server);
        this.oldBoostCount = oldBoostCount;
        this.newBoostCount = newBoostCount;
    }

    @Override
    public int getOldBoostCount() {
        return this.oldBoostCount;
    }

    @Override
    public int getNewBoostCount() {
        return this.newBoostCount;
    }
}

