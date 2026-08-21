/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.text;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.event.channel.server.text.ServerTextChannelChangeDefaultAutoArchiveDurationEvent;
import org.javacord.core.event.channel.server.text.ServerTextChannelEventImpl;

public class ServerTextChannelChangeDefaultAutoArchiveDurationEventImpl
extends ServerTextChannelEventImpl
implements ServerTextChannelChangeDefaultAutoArchiveDurationEvent {
    private final int oldDefaultAutoArchiveDuration;
    private final int newDefaultAutoArchiveDuration;

    public ServerTextChannelChangeDefaultAutoArchiveDurationEventImpl(ServerTextChannel channel, int oldDefaultAutoArchiveDuration, int newDefaultAutoArchiveDuration) {
        super(channel);
        this.oldDefaultAutoArchiveDuration = oldDefaultAutoArchiveDuration;
        this.newDefaultAutoArchiveDuration = newDefaultAutoArchiveDuration;
    }

    @Override
    public int getOldDefaultAutoArchiveDuration() {
        return this.oldDefaultAutoArchiveDuration;
    }

    @Override
    public int getAutoArchiveDuration() {
        return this.newDefaultAutoArchiveDuration;
    }
}

