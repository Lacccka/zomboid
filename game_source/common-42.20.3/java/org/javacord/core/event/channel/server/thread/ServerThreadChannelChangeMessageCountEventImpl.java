/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.thread;

import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeMessageCountEvent;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelEventImpl;

public class ServerThreadChannelChangeMessageCountEventImpl
extends ServerThreadChannelEventImpl
implements ServerThreadChannelChangeMessageCountEvent {
    private final int newMessageCount;
    private final int oldMessageCount;

    public ServerThreadChannelChangeMessageCountEventImpl(ServerThreadChannel channel, int newMessageCount, int oldMessageCount) {
        super(channel);
        this.newMessageCount = newMessageCount;
        this.oldMessageCount = oldMessageCount;
    }

    @Override
    public int getNewMessageCount() {
        return this.newMessageCount;
    }

    @Override
    public int getOldMessageCount() {
        return this.oldMessageCount;
    }
}

