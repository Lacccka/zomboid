/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server.voice;

import org.javacord.api.entity.channel.ServerStageVoiceChannel;
import org.javacord.api.event.channel.server.voice.ServerStageVoiceChannelChangeTopicEvent;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelEventImpl;

public class ServerStageVoiceChannelChangeTopicEventImpl
extends ServerVoiceChannelEventImpl
implements ServerStageVoiceChannelChangeTopicEvent {
    private final String newTopic;
    private final String oldTopic;

    public ServerStageVoiceChannelChangeTopicEventImpl(ServerStageVoiceChannel channel, String newTopic, String oldTopic) {
        super(channel);
        this.newTopic = newTopic;
        this.oldTopic = oldTopic;
    }

    @Override
    public String getNewTopic() {
        return this.newTopic;
    }

    @Override
    public String getOldTopic() {
        return this.oldTopic;
    }

    @Override
    public ServerStageVoiceChannel getChannel() {
        return (ServerStageVoiceChannel)this.channel;
    }
}

