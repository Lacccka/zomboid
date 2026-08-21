/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server;

import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.event.channel.server.ServerChannelCreateEvent;
import org.javacord.core.event.channel.server.ServerChannelEventImpl;

public class ServerChannelCreateEventImpl
extends ServerChannelEventImpl
implements ServerChannelCreateEvent {
    public ServerChannelCreateEventImpl(ServerChannel channel) {
        super(channel);
    }
}

