/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.thread;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.event.channel.thread.ThreadUpdateEvent;
import org.javacord.core.event.channel.server.ServerChannelEventImpl;

public class ThreadUpdateEventImpl
extends ServerChannelEventImpl
implements ThreadUpdateEvent {
    private final ServerThreadChannel serverThreadChannel;

    public ThreadUpdateEventImpl(ServerThreadChannel serverThreadChannel) {
        super(serverThreadChannel);
        this.serverThreadChannel = serverThreadChannel;
    }

    @Override
    public ServerThreadChannel getChannel() {
        return this.serverThreadChannel;
    }

    @Override
    public DiscordApi getApi() {
        return this.getChannel().getApi();
    }
}

