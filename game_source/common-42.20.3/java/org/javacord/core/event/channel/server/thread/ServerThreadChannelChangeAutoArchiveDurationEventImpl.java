/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.thread;

import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeAutoArchiveDurationEvent;
import org.javacord.core.entity.channel.ServerThreadChannelImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelEventImpl;

public class ServerThreadChannelChangeAutoArchiveDurationEventImpl
extends ServerThreadChannelEventImpl
implements ServerThreadChannelChangeAutoArchiveDurationEvent {
    private final int newAutoArchiveDuration;
    private final int oldAutoArchiveDuration;

    public ServerThreadChannelChangeAutoArchiveDurationEventImpl(ServerThreadChannelImpl channel, int newAutoArchiveDuration, int oldAutoArchiveDuration) {
        super(channel);
        this.newAutoArchiveDuration = newAutoArchiveDuration;
        this.oldAutoArchiveDuration = oldAutoArchiveDuration;
    }

    @Override
    public int getNewAutoArchiveDuration() {
        return this.newAutoArchiveDuration;
    }

    @Override
    public int getOldAutoArchiveDuration() {
        return this.oldAutoArchiveDuration;
    }
}

