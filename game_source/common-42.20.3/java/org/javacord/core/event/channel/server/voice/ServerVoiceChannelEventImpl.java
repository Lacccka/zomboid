/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.voice;

import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelEvent;
import org.javacord.core.event.server.ServerEventImpl;

public abstract class ServerVoiceChannelEventImpl
extends ServerEventImpl
implements ServerVoiceChannelEvent {
    protected final ServerVoiceChannel channel;

    public ServerVoiceChannelEventImpl(ServerVoiceChannel channel) {
        super(channel.getServer());
        this.channel = channel;
    }

    @Override
    public ServerVoiceChannel getChannel() {
        return this.channel;
    }
}

