/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerBecomesUnavailableEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerBecomesUnavailableEventImpl
extends ServerEventImpl
implements ServerBecomesUnavailableEvent {
    public ServerBecomesUnavailableEventImpl(Server server) {
        super(server);
    }
}

