/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.event.server.member.ServerMemberJoinEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

public class GuildMemberAddHandler
extends PacketHandler {
    public GuildMemberAddHandler(DiscordApi api) {
        super(api, true, "GUILD_MEMBER_ADD");
    }

    @Override
    public void handle(JsonNode packet) {
        this.api.getPossiblyUnreadyServerById(packet.get("guild_id").asLong()).map(server -> (ServerImpl)server).ifPresent(server -> {
            MemberImpl member = server.addMember(packet);
            server.incrementMemberCount();
            UserImpl user = new UserImpl(this.api, packet.get("user"), member, (ServerImpl)server);
            ServerMemberJoinEventImpl event = new ServerMemberJoinEventImpl((Server)server, user);
            this.api.getEventDispatcher().dispatchServerMemberJoinEvent((DispatchQueueSelector)server, (Server)server, user, (ServerMemberJoinEvent)event);
        });
    }
}

