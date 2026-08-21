/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.message.reaction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.reaction.ReactionRemoveAllEvent;
import org.javacord.core.entity.channel.PrivateChannelImpl;
import org.javacord.core.entity.message.MessageImpl;
import org.javacord.core.event.message.reaction.ReactionRemoveAllEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class MessageReactionRemoveAllHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(MessageReactionRemoveAllHandler.class);

    public MessageReactionRemoveAllHandler(DiscordApi api) {
        super(api, true, "MESSAGE_REACTION_REMOVE_ALL");
    }

    @Override
    public void handle(JsonNode packet) {
        long messageId = packet.get("message_id").asLong();
        Optional<Message> message = this.api.getCachedMessageById(messageId);
        message.ifPresent(msg -> ((MessageImpl)msg).removeAllReactionsFromCache());
        long channelId = packet.get("channel_id").asLong();
        TextChannel channel = this.api.getTextChannelById(channelId).orElse(null);
        if (channel == null) {
            if (packet.hasNonNull("guild_id")) {
                LoggerUtil.logMissingChannel(logger, channelId);
                return;
            }
            channel = PrivateChannelImpl.dispatchPrivateChannelCreateEvent(this.api, new PrivateChannelImpl(this.api, channelId, null, null));
        }
        ReactionRemoveAllEventImpl event = new ReactionRemoveAllEventImpl(this.api, messageId, channel);
        Optional<Server> optionalServer = channel.asServerChannel().map(ServerChannel::getServer);
        this.api.getEventDispatcher().dispatchReactionRemoveAllEvent(optionalServer.map(DispatchQueueSelector.class::cast).orElse(this.api), messageId, optionalServer.orElse(null), channel, (ReactionRemoveAllEvent)event);
    }
}

