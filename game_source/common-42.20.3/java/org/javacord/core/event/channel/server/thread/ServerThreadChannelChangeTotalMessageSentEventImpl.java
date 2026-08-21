/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.thread;

import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeTotalMessageSentEvent;
import org.javacord.core.entity.channel.ServerThreadChannelImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelEventImpl;

public class ServerThreadChannelChangeTotalMessageSentEventImpl
extends ServerThreadChannelEventImpl
implements ServerThreadChannelChangeTotalMessageSentEvent {
    private final int newTotalMessageSent;
    private final int oldTotalMessageSent;

    public ServerThreadChannelChangeTotalMessageSentEventImpl(ServerThreadChannelImpl channel, int newTotalMessageSent, int oldTotalMessageSent) {
        super(channel);
        this.newTotalMessageSent = newTotalMessageSent;
        this.oldTotalMessageSent = oldTotalMessageSent;
    }

    @Override
    public int getNewTotalMessageSent() {
        return this.newTotalMessageSent;
    }

    @Override
    public int getOldTotalMessageSent() {
        return this.oldTotalMessageSent;
    }
}

