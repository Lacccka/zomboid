/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.server.ExplicitContentFilterLevel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerChangeExplicitContentFilterLevelEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeExplicitContentFilterLevelEventImpl
extends ServerEventImpl
implements ServerChangeExplicitContentFilterLevelEvent {
    private final ExplicitContentFilterLevel newExplicitContentFilterLevel;
    private final ExplicitContentFilterLevel oldExplicitContentFilterLevel;

    public ServerChangeExplicitContentFilterLevelEventImpl(Server server, ExplicitContentFilterLevel newExplicitContentFilterLevel, ExplicitContentFilterLevel oldExplicitContentFilterLevel) {
        super(server);
        this.newExplicitContentFilterLevel = newExplicitContentFilterLevel;
        this.oldExplicitContentFilterLevel = oldExplicitContentFilterLevel;
    }

    @Override
    public ExplicitContentFilterLevel getOldExplicitContentFilterLevel() {
        return this.oldExplicitContentFilterLevel;
    }

    @Override
    public ExplicitContentFilterLevel getNewExplicitContentFilterLevel() {
        return this.newExplicitContentFilterLevel;
    }
}

