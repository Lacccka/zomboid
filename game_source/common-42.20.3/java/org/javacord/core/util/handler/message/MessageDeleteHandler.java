/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.message;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageDeleteEvent;
import org.javacord.core.event.message.MessageDeleteEventImpl;
import org.javacord.core.util.cache.MessageCacheImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class MessageDeleteHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(MessageDeleteHandler.class);

    public MessageDeleteHandler(DiscordApi api) {
        super(api, true, "MESSAGE_DELETE");
    }

    @Override
    public void handle(JsonNode packet) {
        long messageId = packet.get("id").asLong();
        long channelId = packet.get("channel_id").asLong();
        Optional<TextChannel> optionalChannel = this.api.getTextChannelById(channelId);
        if (optionalChannel.isPresent()) {
            TextChannel channel = optionalChannel.get();
            MessageDeleteEventImpl event = new MessageDeleteEventImpl(this.api, messageId, channel);
            this.api.getCachedMessageById(messageId).ifPresent(((MessageCacheImpl)channel.getMessageCache())::removeMessage);
            this.api.removeMessageFromCache(messageId);
            Optional<Server> optionalServer = channel.asServerChannel().map(ServerChannel::getServer);
            this.api.getEventDispatcher().dispatchMessageDeleteEvent(optionalServer.map(DispatchQueueSelector.class::cast).orElse(this.api), messageId, optionalServer.orElse(null), channel, (MessageDeleteEvent)event);
            this.api.removeObjectListeners(Message.class, messageId);
        } else {
            LoggerUtil.logMissingChannel(logger, channelId);
        }
    }
}

