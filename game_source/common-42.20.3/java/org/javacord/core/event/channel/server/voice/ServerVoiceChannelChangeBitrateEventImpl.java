/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.voice;

import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelChangeBitrateEvent;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelEventImpl;

public class ServerVoiceChannelChangeBitrateEventImpl
extends ServerVoiceChannelEventImpl
implements ServerVoiceChannelChangeBitrateEvent {
    private final int newBitrate;
    private final int oldBitrate;

    public ServerVoiceChannelChangeBitrateEventImpl(ServerVoiceChannel channel, int newBitrate, int oldBitrate) {
        super(channel);
        this.newBitrate = newBitrate;
        this.oldBitrate = oldBitrate;
    }

    @Override
    public int getNewBitrate() {
        return this.newBitrate;
    }

    @Override
    public int getOldBitrate() {
        return this.oldBitrate;
    }
}

