/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild.role;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.role.RoleCreateEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.role.RoleCreateEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

public class GuildRoleCreateHandler
extends PacketHandler {
    public GuildRoleCreateHandler(DiscordApi api) {
        super(api, true, "GUILD_ROLE_CREATE");
    }

    @Override
    public void handle(JsonNode packet) {
        long serverId = Long.parseLong(packet.get("guild_id").asText());
        this.api.getPossiblyUnreadyServerById(serverId).ifPresent(server -> {
            Role role = ((ServerImpl)server).getOrCreateRole(packet.get("role"));
            RoleCreateEventImpl event = new RoleCreateEventImpl(role);
            this.api.getEventDispatcher().dispatchRoleCreateEvent((DispatchQueueSelector)((Object)server), (Server)server, (RoleCreateEvent)event);
        });
    }
}

