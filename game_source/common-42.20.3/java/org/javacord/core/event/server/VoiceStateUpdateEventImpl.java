/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.server.VoiceStateUpdateEvent;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelEventImpl;

public class VoiceStateUpdateEventImpl
extends ServerVoiceChannelEventImpl
implements VoiceStateUpdateEvent {
    private final String sessionId;

    public VoiceStateUpdateEventImpl(ServerVoiceChannel channel, String sessionId) {
        super(channel);
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId() {
        return this.sessionId;
    }
}

