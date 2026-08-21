/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server;

import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.event.channel.server.ServerChannelEvent;
import org.javacord.core.event.server.ServerEventImpl;

public abstract class ServerChannelEventImpl
extends ServerEventImpl
implements ServerChannelEvent {
    private final ServerChannel channel;

    public ServerChannelEventImpl(ServerChannel channel) {
        super(channel.getServer());
        this.channel = channel;
    }

    @Override
    public ServerChannel getChannel() {
        return this.channel;
    }
}

