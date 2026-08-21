/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.server.NsfwLevel;
import org.javacord.api.event.server.ServerChangeNsfwLevelEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeNsfwLevelEventImpl
extends ServerEventImpl
implements ServerChangeNsfwLevelEvent {
    private final NsfwLevel oldNsfwLevel;
    private final NsfwLevel newNsfwLevel;

    public ServerChangeNsfwLevelEventImpl(ServerImpl server, NsfwLevel newNsfwLevel, NsfwLevel oldNsfwLevel) {
        super(server);
        this.oldNsfwLevel = oldNsfwLevel;
        this.newNsfwLevel = newNsfwLevel;
    }

    @Override
    public NsfwLevel getOldNsfwLevel() {
        return this.oldNsfwLevel;
    }

    @Override
    public NsfwLevel getNewNsfwLevel() {
        return this.newNsfwLevel;
    }
}

