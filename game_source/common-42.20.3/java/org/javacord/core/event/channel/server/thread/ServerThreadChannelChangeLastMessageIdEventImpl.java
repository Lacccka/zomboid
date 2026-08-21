/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.thread;

import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeLastMessageIdEvent;
import org.javacord.core.entity.channel.ServerThreadChannelImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelEventImpl;

public class ServerThreadChannelChangeLastMessageIdEventImpl
extends ServerThreadChannelEventImpl
implements ServerThreadChannelChangeLastMessageIdEvent {
    private final long oldLastMessageId;
    private final long newLastMessageId;

    public ServerThreadChannelChangeLastMessageIdEventImpl(ServerThreadChannelImpl channel, long oldLastMessageId, long newLastMessageId) {
        super(channel);
        this.oldLastMessageId = oldLastMessageId;
        this.newLastMessageId = newLastMessageId;
    }

    @Override
    public long getOldLastMessageId() {
        return this.oldLastMessageId;
    }

    @Override
    public long getNewLastMessageId() {
        return this.newLastMessageId;
    }
}

