/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.member.ServerMemberUnbanEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.event.server.member.ServerMemberUnbanEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

public class GuildBanRemoveHandler
extends PacketHandler {
    public GuildBanRemoveHandler(DiscordApi api) {
        super(api, true, "GUILD_BAN_REMOVE");
    }

    @Override
    public void handle(JsonNode packet) {
        this.api.getPossiblyUnreadyServerById(packet.get("guild_id").asLong()).map(server -> (ServerImpl)server).ifPresent(server -> {
            UserImpl user = new UserImpl(this.api, packet.get("user"), (MemberImpl)null, (ServerImpl)server);
            ServerMemberUnbanEventImpl event = new ServerMemberUnbanEventImpl((Server)server, user);
            this.api.getEventDispatcher().dispatchServerMemberUnbanEvent((DispatchQueueSelector)server, (Server)server, user, (ServerMemberUnbanEvent)event);
        });
    }
}

