/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerLeaveEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerLeaveEventImpl
extends ServerEventImpl
implements ServerLeaveEvent {
    public ServerLeaveEventImpl(Server server) {
        super(server);
    }
}

