/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.channel;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.ChannelPinsUpdateEvent;
import org.javacord.core.event.message.ChannelPinsUpdateEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class ChannelPinsUpdateHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(ChannelPinsUpdateHandler.class);

    public ChannelPinsUpdateHandler(DiscordApi api) {
        super(api, true, "CHANNEL_PINS_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        long channelId = packet.get("channel_id").asLong();
        Optional<TextChannel> optionalChannel = this.api.getTextChannelById(channelId);
        if (optionalChannel.isPresent()) {
            TextChannel channel = optionalChannel.get();
            Instant lastPinTimestamp = packet.hasNonNull("last_pin_timestamp") ? OffsetDateTime.parse(packet.get("last_pin_timestamp").asText()).toInstant() : null;
            ChannelPinsUpdateEventImpl event = new ChannelPinsUpdateEventImpl(channel, lastPinTimestamp);
            Optional<Server> optionalServer = channel.asServerChannel().map(ServerChannel::getServer);
            this.api.getEventDispatcher().dispatchChannelPinsUpdateEvent(optionalServer.map(DispatchQueueSelector.class::cast).orElse(this.api), optionalServer.orElse(null), channel, (ChannelPinsUpdateEvent)event);
        } else {
            LoggerUtil.logMissingChannel(logger, channelId);
        }
    }
}

