/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerJoinEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerJoinEventImpl
extends ServerEventImpl
implements ServerJoinEvent {
    public ServerJoinEventImpl(Server server) {
        super(server);
    }
}

