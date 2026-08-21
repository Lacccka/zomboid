/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server;

import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.event.channel.server.ServerChannelDeleteEvent;
import org.javacord.core.event.channel.server.ServerChannelEventImpl;

public class ServerChannelDeleteEventImpl
extends ServerChannelEventImpl
implements ServerChannelDeleteEvent {
    public ServerChannelDeleteEventImpl(ServerChannel channel) {
        super(channel);
    }
}

