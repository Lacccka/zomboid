/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server.voice;

import org.javacord.api.entity.channel.ServerStageVoiceChannel;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelEvent;

public interface ServerStageVoiceChannelChangeTopicEvent
extends ServerVoiceChannelEvent {
    @Override
    public ServerStageVoiceChannel getChannel();

    public String getNewTopic();

    public String getOldTopic();
}

