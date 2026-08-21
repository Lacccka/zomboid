/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.thread;

import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeRateLimitPerUserEvent;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelEventImpl;

public class ServerThreadChannelChangeRateLimitPerUserEventImpl
extends ServerThreadChannelEventImpl
implements ServerThreadChannelChangeRateLimitPerUserEvent {
    private final int newRateLimitPerUser;
    private final int oldRateLimitPerUser;

    public ServerThreadChannelChangeRateLimitPerUserEventImpl(ServerThreadChannel channel, int newRateLimitPerUser, int oldRateLimitPerUser) {
        super(channel);
        this.newRateLimitPerUser = newRateLimitPerUser;
        this.oldRateLimitPerUser = oldRateLimitPerUser;
    }

    @Override
    public int getNewRateLimitPerUser() {
        return this.newRateLimitPerUser;
    }

    @Override
    public int getOldRateLimitPerUser() {
        return this.oldRateLimitPerUser;
    }
}

