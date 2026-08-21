/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.channel.invite;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.server.invite.ServerChannelInviteDeleteEvent;
import org.javacord.core.event.channel.server.invite.ServerChannelInviteDeleteEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

public class InviteDeleteHandler
extends PacketHandler {
    public InviteDeleteHandler(DiscordApi api) {
        super(api, true, "INVITE_DELETE");
    }

    @Override
    protected void handle(JsonNode packet) {
        String code = packet.get("code").asText();
        Channel channel = this.api.getChannelById(packet.get("channel_id").asLong()).orElseThrow(AssertionError::new);
        channel.asServerChannel().ifPresent(serverChannel -> {
            Server server = serverChannel.getServer();
            ServerChannelInviteDeleteEventImpl event = new ServerChannelInviteDeleteEventImpl(code, (ServerChannel)serverChannel);
            this.api.getEventDispatcher().dispatchServerChannelInviteDeleteEvent((DispatchQueueSelector)((Object)server), server, (ServerChannelInviteDeleteEvent)event);
        });
    }
}

