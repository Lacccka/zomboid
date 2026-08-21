/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.VoiceServerUpdateEvent;
import org.javacord.core.audio.AudioConnectionImpl;
import org.javacord.core.event.server.VoiceServerUpdateEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

public class VoiceServerUpdateHandler
extends PacketHandler {
    public VoiceServerUpdateHandler(DiscordApi api) {
        super(api, true, "VOICE_SERVER_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        String token = packet.get("token").asText();
        String endpoint = packet.get("endpoint").asText();
        long serverId = packet.get("guild_id").asLong();
        AudioConnectionImpl pendingAudioConnection = this.api.getPendingAudioConnectionByServerId(serverId);
        if (pendingAudioConnection != null) {
            pendingAudioConnection.setToken(token);
            pendingAudioConnection.setEndpoint(endpoint);
            pendingAudioConnection.tryConnect();
        }
        this.api.getServerById(serverId).ifPresent(server -> {
            VoiceServerUpdateEventImpl event = new VoiceServerUpdateEventImpl((Server)server, token, endpoint);
            this.api.getEventDispatcher().dispatchVoiceServerUpdateEvent((DispatchQueueSelector)((Object)server), (Server)server, (VoiceServerUpdateEvent)event);
        });
    }
}

