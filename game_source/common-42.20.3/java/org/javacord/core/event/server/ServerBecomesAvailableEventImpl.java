/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerBecomesAvailableEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerBecomesAvailableEventImpl
extends ServerEventImpl
implements ServerBecomesAvailableEvent {
    public ServerBecomesAvailableEventImpl(Server server) {
        super(server);
    }
}

