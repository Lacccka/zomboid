/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.thread;

import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.thread.ThreadDeleteEvent;
import org.javacord.core.event.channel.server.ServerChannelEventImpl;

public class ThreadDeleteEventImpl
extends ServerChannelEventImpl
implements ThreadDeleteEvent {
    private final ServerThreadChannel serverThreadChannel;

    public ThreadDeleteEventImpl(ServerThreadChannel channel) {
        super(channel);
        this.serverThreadChannel = channel;
    }

    @Override
    public ServerThreadChannel getChannel() {
        return this.serverThreadChannel;
    }

    @Override
    public Server getServer() {
        return this.serverThreadChannel.getServer();
    }
}

