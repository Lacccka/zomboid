/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeBoostCountEvent
extends ServerEvent {
    public int getOldBoostCount();

    public int getNewBoostCount();
}

