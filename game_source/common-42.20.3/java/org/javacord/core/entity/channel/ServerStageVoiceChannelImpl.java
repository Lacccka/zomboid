/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.javacord.api.entity.channel.ServerStageVoiceChannel;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.channel.ServerVoiceChannelImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.listener.channel.server.voice.InternalServerStageVoiceChannelAttachableListenerManager;

public class ServerStageVoiceChannelImpl
extends ServerVoiceChannelImpl
implements ServerStageVoiceChannel,
InternalServerStageVoiceChannelAttachableListenerManager {
    private String topic;

    public ServerStageVoiceChannelImpl(DiscordApiImpl api, ServerImpl server, JsonNode data) {
        super(api, server, data);
        this.topic = data.hasNonNull("topic") ? data.get("topic").asText() : null;
    }

    @Override
    public Optional<String> getTopic() {
        return Optional.ofNullable(this.topic);
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return String.format("ServerStageVoiceChannel (id: %s, name: %s)", this.getIdAsString(), this.getName());
    }
}

