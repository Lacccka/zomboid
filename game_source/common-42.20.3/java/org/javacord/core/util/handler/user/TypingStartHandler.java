/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.user;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.user.UserStartTypingEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.event.user.UserStartTypingEventImpl;
import org.javacord.core.util.gateway.PacketHandler;

public class TypingStartHandler
extends PacketHandler {
    public TypingStartHandler(DiscordApi api) {
        super(api, true, "TYPING_START");
    }

    @Override
    public void handle(JsonNode packet) {
        long userId = packet.get("user_id").asLong();
        long channelId = packet.get("channel_id").asLong();
        TextChannel channel = this.api.getTextChannelById(channelId).orElse(null);
        ServerImpl server = null;
        if (packet.hasNonNull("guild_id")) {
            long serverId = packet.get("guild_id").asLong();
            server = (ServerImpl)this.api.getPossiblyUnreadyServerById(serverId).orElseThrow(AssertionError::new);
        }
        MemberImpl member = null;
        if (packet.hasNonNull("member") && server != null) {
            member = new MemberImpl(this.api, server, packet.get("member"), null);
        }
        if (channel != null) {
            UserStartTypingEventImpl event = new UserStartTypingEventImpl(channel, userId, member);
            this.api.getEventDispatcher().dispatchUserStartTypingEvent(server != null ? server : this.api, (Server)server, channel, userId, (UserStartTypingEvent)event);
        }
    }
}

