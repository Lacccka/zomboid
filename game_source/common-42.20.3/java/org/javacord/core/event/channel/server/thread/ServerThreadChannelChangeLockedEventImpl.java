/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.thread;

import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeLockedEvent;
import org.javacord.core.entity.channel.ServerThreadChannelImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelEventImpl;

public class ServerThreadChannelChangeLockedEventImpl
extends ServerThreadChannelEventImpl
implements ServerThreadChannelChangeLockedEvent {
    private final boolean isLocked;
    private final boolean wasLocked;

    public ServerThreadChannelChangeLockedEventImpl(ServerThreadChannelImpl channel, boolean isLocked, boolean wasLocked) {
        super(channel);
        this.isLocked = isLocked;
        this.wasLocked = wasLocked;
    }

    @Override
    public boolean isLocked() {
        return this.isLocked;
    }

    @Override
    public boolean wasLocked() {
        return this.wasLocked;
    }
}

