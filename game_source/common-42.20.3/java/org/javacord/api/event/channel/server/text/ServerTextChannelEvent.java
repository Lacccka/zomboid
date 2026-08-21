/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.text;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.event.channel.TextChannelEvent;
import org.javacord.api.event.channel.server.ServerChannelEvent;

public interface ServerTextChannelEvent
extends ServerChannelEvent,
TextChannelEvent {
    @Override
    public ServerTextChannel getChannel();
}

