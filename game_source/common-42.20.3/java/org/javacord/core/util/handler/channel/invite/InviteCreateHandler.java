/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.channel.invite;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.server.invite.ServerChannelInviteCreateEvent;
import org.javacord.core.entity.server.invite.InviteImpl;
import org.javacord.core.event.channel.server.invite.ServerChannelInviteCreateEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;

public class InviteCreateHandler
extends PacketHandler {
    public InviteCreateHandler(DiscordApi api) {
        super(api, true, "INVITE_CREATE");
    }

    @Override
    protected void handle(JsonNode packet) {
        InviteImpl invite = new InviteImpl(this.api, packet);
        invite.getServer().ifPresent(server -> {
            ServerChannel channel = invite.getChannel().orElseThrow(AssertionError::new);
            ServerChannelInviteCreateEventImpl event = new ServerChannelInviteCreateEventImpl(invite, channel);
            this.api.getEventDispatcher().dispatchServerChannelInviteCreateEvent((DispatchQueueSelector)((Object)server), (Server)server, (ServerChannelInviteCreateEvent)event);
        });
    }
}

