/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerBecomesUnavailableEvent;
import org.javacord.api.event.server.ServerLeaveEvent;
import org.javacord.core.event.server.ServerBecomesUnavailableEventImpl;
import org.javacord.core.event.server.ServerLeaveEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

public class GuildDeleteHandler
extends PacketHandler {
    public GuildDeleteHandler(DiscordApi api) {
        super(api, true, "GUILD_DELETE");
    }

    @Override
    public void handle(JsonNode packet) {
        long serverId = packet.get("id").asLong();
        if (packet.has("unavailable") && packet.get("unavailable").asBoolean()) {
            this.api.addUnavailableServerToCache(serverId);
            this.api.getPossiblyUnreadyServerById(serverId).ifPresent(server -> {
                ServerBecomesUnavailableEventImpl event = new ServerBecomesUnavailableEventImpl((Server)server);
                this.api.getEventDispatcher().dispatchServerBecomesUnavailableEvent((DispatchQueueSelector)((Object)server), (Server)server, (ServerBecomesUnavailableEvent)event);
                this.api.forEachCachedMessageWhere(msg -> msg.getServer().map(s -> s.getId() == serverId).orElse(false), msg -> this.api.removeMessageFromCache(msg.getId()));
            });
            this.api.removeServerFromCache(serverId);
            return;
        }
        this.api.getPossiblyUnreadyServerById(serverId).ifPresent(server -> {
            ServerLeaveEventImpl event = new ServerLeaveEventImpl((Server)server);
            this.api.getEventDispatcher().dispatchServerLeaveEvent((DispatchQueueSelector)((Object)server), (Server)server, (ServerLeaveEvent)event);
        });
        this.api.removeObjectListeners(Server.class, serverId);
        this.api.forEachCachedMessageWhere(msg -> msg.getServer().map(s -> s.getId() == serverId).orElse(false), msg -> this.api.removeMessageFromCache(msg.getId()));
        this.api.removeServerFromCache(serverId);
    }
}

