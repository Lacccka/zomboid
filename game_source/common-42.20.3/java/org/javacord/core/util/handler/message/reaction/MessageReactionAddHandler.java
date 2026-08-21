/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.message.reaction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.core.entity.channel.PrivateChannelImpl;
import org.javacord.core.entity.emoji.UnicodeEmojiImpl;
import org.javacord.core.entity.message.MessageImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.event.message.reaction.ReactionAddEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class MessageReactionAddHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(MessageReactionAddHandler.class);

    public MessageReactionAddHandler(DiscordApi api) {
        super(api, true, "MESSAGE_REACTION_ADD");
    }

    @Override
    public void handle(JsonNode packet) {
        long channelId = packet.get("channel_id").asLong();
        long messageId = packet.get("message_id").asLong();
        long userId = packet.get("user_id").asLong();
        String serverId = packet.hasNonNull("guild_id") ? packet.get("guild_id").asText() : null;
        TextChannel channel = serverId == null ? PrivateChannelImpl.getOrCreatePrivateChannel(this.api, channelId, userId, null) : (TextChannel)this.api.getTextChannelById(channelId).orElse(null);
        if (channel == null) {
            LoggerUtil.logMissingChannel(logger, channelId);
            return;
        }
        Optional<Server> server = this.api.getServerById(serverId);
        MemberImpl member = null;
        if (packet.hasNonNull("member") && server.isPresent()) {
            member = new MemberImpl(this.api, (ServerImpl)server.get(), packet.get("member"), null);
        }
        Optional<Message> message = this.api.getCachedMessageById(messageId);
        JsonNode emojiJson = packet.get("emoji");
        Emoji emoji = !emojiJson.has("id") || emojiJson.get("id").isNull() ? UnicodeEmojiImpl.fromString(emojiJson.get("name").asText()) : this.api.getKnownCustomEmojiOrCreateCustomEmoji(emojiJson);
        message.ifPresent(msg -> ((MessageImpl)msg).addReaction(emoji, userId == this.api.getYourself().getId()));
        ReactionAddEventImpl event = new ReactionAddEventImpl(this.api, messageId, channel, emoji, userId, member);
        this.api.getEventDispatcher().dispatchReactionAddEvent(server.map(DispatchQueueSelector.class::cast).orElse(this.api), messageId, (Server)server.orElse(null), channel, userId, (ReactionAddEvent)event);
    }
}

