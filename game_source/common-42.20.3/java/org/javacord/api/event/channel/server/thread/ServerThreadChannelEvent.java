/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.thread;

import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.event.channel.server.ServerChannelEvent;

public interface ServerThreadChannelEvent
extends ServerChannelEvent {
    @Override
    public ServerThreadChannel getChannel();
}

