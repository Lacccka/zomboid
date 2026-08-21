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
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.core.entity.channel.PrivateChannelImpl;
import org.javacord.core.entity.emoji.UnicodeEmojiImpl;
import org.javacord.core.entity.message.MessageImpl;
import org.javacord.core.event.message.reaction.ReactionRemoveEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class MessageReactionRemoveHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(MessageReactionRemoveHandler.class);

    public MessageReactionRemoveHandler(DiscordApi api) {
        super(api, true, "MESSAGE_REACTION_REMOVE");
    }

    @Override
    public void handle(JsonNode packet) {
        long messageId = packet.get("message_id").asLong();
        long userId = packet.get("user_id").asLong();
        Optional<Message> message = this.api.getCachedMessageById(messageId);
        long channelId = packet.get("channel_id").asLong();
        TextChannel channel = packet.hasNonNull("guild_id") ? (TextChannel)this.api.getTextChannelById(channelId).orElse(null) : PrivateChannelImpl.getOrCreatePrivateChannel(this.api, channelId, userId, null);
        if (channel == null) {
            LoggerUtil.logMissingChannel(logger, channelId);
            return;
        }
        JsonNode emojiJson = packet.get("emoji");
        Emoji emoji = !emojiJson.has("id") || emojiJson.get("id").isNull() ? UnicodeEmojiImpl.fromString(emojiJson.get("name").asText()) : this.api.getKnownCustomEmojiOrCreateCustomEmoji(emojiJson);
        message.ifPresent(msg -> ((MessageImpl)msg).removeReaction(emoji, userId == this.api.getYourself().getId()));
        ReactionRemoveEventImpl event = new ReactionRemoveEventImpl(this.api, messageId, channel, emoji, userId);
        Optional<Server> optionalServer = channel.asServerChannel().map(ServerChannel::getServer);
        this.api.getEventDispatcher().dispatchReactionRemoveEvent(optionalServer.map(DispatchQueueSelector.class::cast).orElse(this.api), messageId, (Server)optionalServer.orElse(null), channel, userId, (ReactionRemoveEvent)event);
    }
}

