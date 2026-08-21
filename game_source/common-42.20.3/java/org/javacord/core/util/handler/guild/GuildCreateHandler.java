/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.ServerBecomesAvailableEventImpl;
import org.javacord.core.event.server.ServerJoinEventImpl;
import org.javacord.core.util.gateway.PacketHandler;

public class GuildCreateHandler
extends PacketHandler {
    public GuildCreateHandler(DiscordApi api) {
        super(api, true, "GUILD_CREATE");
    }

    @Override
    public void handle(JsonNode packet) {
        if (packet.has("unavailable") && packet.get("unavailable").asBoolean()) {
            return;
        }
        long id = packet.get("id").asLong();
        if (this.api.getUnavailableServers().contains(id)) {
            ServerImpl server = new ServerImpl(this.api, packet);
            ServerBecomesAvailableEventImpl event = new ServerBecomesAvailableEventImpl(server);
            this.api.getEventDispatcher().dispatchServerBecomesAvailableEvent(server, event);
            return;
        }
        ServerImpl server = new ServerImpl(this.api, packet);
        ServerJoinEventImpl event = new ServerJoinEventImpl(server);
        this.api.getEventDispatcher().dispatchServerJoinEvent(server, event);
    }
}

