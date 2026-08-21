/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.thread;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelEvent;
import org.javacord.core.event.channel.server.ServerChannelEventImpl;

public abstract class ServerThreadChannelEventImpl
extends ServerChannelEventImpl
implements ServerThreadChannelEvent {
    public ServerThreadChannelEventImpl(ServerThreadChannel channel) {
        super(channel);
    }

    @Override
    public ServerThreadChannel getChannel() {
        return super.getChannel().asServerThreadChannel().orElseThrow(() -> new IllegalStateException("Channel is not a server thread channel."));
    }

    @Override
    public DiscordApi getApi() {
        return this.getChannel().getApi();
    }
}

