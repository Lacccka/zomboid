/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.Set;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ApplicationCommandPermissionsUpdateEvent;
import org.javacord.api.interaction.ApplicationCommandPermissions;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.ApplicationCommandPermissionsUpdateEventImpl;
import org.javacord.core.interaction.ApplicationCommandPermissionsImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

public class ApplicationCommandPermissionsUpdateHandler
extends PacketHandler {
    public ApplicationCommandPermissionsUpdateHandler(DiscordApi api) {
        super(api, true, "APPLICATION_COMMAND_PERMISSIONS_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        this.api.getPossiblyUnreadyServerById(packet.get("guild_id").asLong()).map(server -> (ServerImpl)server).ifPresent(server -> {
            long commandId = packet.get("id").asLong();
            long applicationId = packet.get("application_id").asLong();
            HashSet<ApplicationCommandPermissions> newPermissions = new HashSet<ApplicationCommandPermissions>();
            packet.get("permissions").elements().forEachRemaining(permission -> newPermissions.add(new ApplicationCommandPermissionsImpl((Server)server, (JsonNode)permission)));
            ApplicationCommandPermissionsUpdateEventImpl event = new ApplicationCommandPermissionsUpdateEventImpl(applicationId, commandId, (Server)server, (Set<ApplicationCommandPermissions>)newPermissions);
            this.api.getEventDispatcher().dispatchApplicationCommandPermissionsUpdateEvent((DispatchQueueSelector)server, (Server)server, (ApplicationCommandPermissionsUpdateEvent)event);
        });
    }
}

