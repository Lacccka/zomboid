/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild.role;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.role.RoleDeleteEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.role.RoleDeleteEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

public class GuildRoleDeleteHandler
extends PacketHandler {
    public GuildRoleDeleteHandler(DiscordApi api) {
        super(api, true, "GUILD_ROLE_DELETE");
    }

    @Override
    public void handle(JsonNode packet) {
        long serverId = packet.get("guild_id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).map(server -> (ServerImpl)server).ifPresent(server -> {
            long roleId = packet.get("role_id").asLong();
            server.getRoleById(roleId).ifPresent(role -> {
                server.removeRole(roleId);
                RoleDeleteEventImpl event = new RoleDeleteEventImpl((Role)role);
                this.api.getEventDispatcher().dispatchRoleDeleteEvent((DispatchQueueSelector)server, (Role)role, (Server)server, (RoleDeleteEvent)event);
            });
        });
    }
}

