/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server;

import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.ChannelEvent;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChannelEvent
extends ServerEvent,
ChannelEvent {
    @Override
    public ServerChannel getChannel();

    @Override
    default public Server getServer() {
        return this.getChannel().getServer();
    }
}

