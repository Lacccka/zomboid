/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.thread;

import java.time.Instant;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelEvent;

public interface ServerThreadChannelChangeArchiveTimestampEvent
extends ServerThreadChannelEvent {
    public Instant getNewArchiveTimestamp();

    public Instant getOldArchiveTimestamp();
}

