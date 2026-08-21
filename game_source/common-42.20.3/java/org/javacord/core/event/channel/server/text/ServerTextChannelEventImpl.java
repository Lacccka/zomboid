/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.text;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.event.channel.server.text.ServerTextChannelEvent;
import org.javacord.core.event.channel.server.ServerChannelEventImpl;

public abstract class ServerTextChannelEventImpl
extends ServerChannelEventImpl
implements ServerTextChannelEvent {
    private final ServerTextChannel channel;

    public ServerTextChannelEventImpl(ServerTextChannel channel) {
        super(channel);
        this.channel = channel;
    }

    @Override
    public ServerTextChannel getChannel() {
        return this.channel;
    }
}

