/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.thread;

import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.thread.ThreadCreateEvent;
import org.javacord.core.event.channel.server.ServerChannelEventImpl;

public class ThreadCreateEventImpl
extends ServerChannelEventImpl
implements ThreadCreateEvent {
    private final ServerThreadChannel serverThreadChannel;

    public ThreadCreateEventImpl(ServerThreadChannel serverThreadChannel) {
        super(serverThreadChannel);
        this.serverThreadChannel = serverThreadChannel;
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

