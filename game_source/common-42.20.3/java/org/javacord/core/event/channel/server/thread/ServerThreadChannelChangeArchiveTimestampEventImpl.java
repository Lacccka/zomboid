/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.thread;

import java.time.Instant;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeArchiveTimestampEvent;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelEventImpl;

public class ServerThreadChannelChangeArchiveTimestampEventImpl
extends ServerThreadChannelEventImpl
implements ServerThreadChannelChangeArchiveTimestampEvent {
    private final Instant newArchiveTimestamp;
    private final Instant oldArchiveTimestamp;

    public ServerThreadChannelChangeArchiveTimestampEventImpl(ServerThreadChannel channel, Instant newArchiveTimestamp, Instant oldArchiveTimestamp) {
        super(channel);
        this.newArchiveTimestamp = newArchiveTimestamp;
        this.oldArchiveTimestamp = oldArchiveTimestamp;
    }

    @Override
    public Instant getNewArchiveTimestamp() {
        return this.newArchiveTimestamp;
    }

    @Override
    public Instant getOldArchiveTimestamp() {
        return this.oldArchiveTimestamp;
    }
}

