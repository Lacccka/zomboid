/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.voice;

import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.channel.VoiceChannelEvent;
import org.javacord.api.event.channel.server.ServerChannelEvent;

public interface ServerVoiceChannelEvent
extends ServerChannelEvent,
VoiceChannelEvent {
    @Override
    public ServerVoiceChannel getChannel();
}

