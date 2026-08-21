/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel;

import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.event.channel.ChannelEvent;

public interface VoiceChannelEvent
extends ChannelEvent {
    @Override
    public VoiceChannel getChannel();
}

