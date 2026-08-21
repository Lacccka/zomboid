/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import org.javacord.api.entity.server.Server;
import org.javacord.api.event.Event;

public interface ServerEvent
extends Event {
    public Server getServer();
}

