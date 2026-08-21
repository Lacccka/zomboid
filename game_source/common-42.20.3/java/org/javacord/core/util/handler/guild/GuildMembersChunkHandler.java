/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.guild;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.member.ServerMembersChunkEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.server.member.ServerMembersChunkEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

public class GuildMembersChunkHandler
extends PacketHandler {
    public GuildMembersChunkHandler(DiscordApi api) {
        super(api, true, "GUILD_MEMBERS_CHUNK");
    }

    @Override
    public void handle(JsonNode packet) {
        this.api.getPossiblyUnreadyServerById(packet.get("guild_id").asLong()).map(ServerImpl.class::cast).ifPresent(server -> {
            List<Member> members = server.addAndGetMembers(packet.get("members"));
            ServerMembersChunkEventImpl event = new ServerMembersChunkEventImpl((Server)server, members.stream().map(Member::getUser).collect(Collectors.toSet()));
            this.api.getEventDispatcher().dispatchServerMembersChunkEvent((DispatchQueueSelector)server, (Server)server, (ServerMembersChunkEvent)event);
        });
    }
}

