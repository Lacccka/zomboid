/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeAfkTimeoutEvent
extends ServerEvent {
    public int getOldAfkTimeoutInSeconds();

    public int getNewAfkTimeoutInSeconds();
}

