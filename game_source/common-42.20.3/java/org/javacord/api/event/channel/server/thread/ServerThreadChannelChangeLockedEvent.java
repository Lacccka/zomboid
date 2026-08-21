/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.thread;

import org.javacord.api.event.channel.server.thread.ServerThreadChannelEvent;

public interface ServerThreadChannelChangeLockedEvent
extends ServerThreadChannelEvent {
    public boolean isLocked();

    public boolean wasLocked();
}

