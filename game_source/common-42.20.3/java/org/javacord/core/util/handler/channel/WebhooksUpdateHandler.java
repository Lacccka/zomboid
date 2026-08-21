/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.channel;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.event.channel.server.text.WebhooksUpdateEvent;
import org.javacord.core.event.channel.server.text.WebhooksUpdateEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class WebhooksUpdateHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(WebhooksUpdateHandler.class);

    public WebhooksUpdateHandler(DiscordApi api) {
        super(api, true, "WEBHOOKS_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        long channelId = packet.get("channel_id").asLong();
        Optional<ServerTextChannel> optionalChannel = this.api.getServerTextChannelById(channelId);
        if (optionalChannel.isPresent()) {
            ServerTextChannel channel = optionalChannel.get();
            WebhooksUpdateEventImpl event = new WebhooksUpdateEventImpl(channel);
            this.api.getEventDispatcher().dispatchWebhooksUpdateEvent((DispatchQueueSelector)((Object)channel.getServer()), channel.getServer(), channel, (WebhooksUpdateEvent)event);
        } else {
            LoggerUtil.logMissingChannel(logger, channelId);
        }
    }
}

