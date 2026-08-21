/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.server.BoostLevel;
import org.javacord.api.event.server.ServerChangeBoostLevelEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeBoostLevelEventImpl
extends ServerEventImpl
implements ServerChangeBoostLevelEvent {
    private final BoostLevel oldBoostLevel;
    private final BoostLevel newBoostLevel;

    public ServerChangeBoostLevelEventImpl(ServerImpl server, BoostLevel newBoostLevel, BoostLevel oldBoostLevel) {
        super(server);
        this.oldBoostLevel = oldBoostLevel;
        this.newBoostLevel = newBoostLevel;
    }

    @Override
    public BoostLevel getOldBoostLevel() {
        return this.oldBoostLevel;
    }

    @Override
    public BoostLevel getNewBoostLevel() {
        return this.newBoostLevel;
    }
}

