/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.message;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageReplyEvent;
import org.javacord.core.entity.channel.PrivateChannelImpl;
import org.javacord.core.entity.channel.ServerThreadChannelImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.event.message.MessageCreateEventImpl;
import org.javacord.core.event.message.MessageReplyEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class MessageCreateHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(MessageCreateHandler.class);

    public MessageCreateHandler(DiscordApi api) {
        super(api, true, "MESSAGE_CREATE");
    }

    @Override
    public void handle(JsonNode packet) {
        long channelId = packet.get("channel_id").asLong();
        if (!packet.hasNonNull("guild_id")) {
            if (packet.hasNonNull("flags") && (packet.get("flags").asInt() & MessageFlag.EPHEMERAL.getId()) > 0) {
                Optional<ServerTextChannel> serverTextChannel = this.api.getServerTextChannelById(channelId);
                if (serverTextChannel.isPresent()) {
                    this.handle(serverTextChannel.get(), packet);
                    return;
                }
                Optional<ServerThreadChannel> serverThreadChannel = this.api.getServerThreadChannelById(channelId);
                if (serverThreadChannel.isPresent()) {
                    this.handle(serverThreadChannel.get(), packet);
                    return;
                }
            }
            UserImpl author = new UserImpl(this.api, packet.get("author"), (MemberImpl)null, null);
            PrivateChannelImpl privateChannel = PrivateChannelImpl.getOrCreatePrivateChannel(this.api, channelId, author.getId(), author);
            this.handle(privateChannel, packet);
            return;
        }
        Optional<TextChannel> optionalChannel = this.api.getTextChannelById(channelId);
        if (optionalChannel.isPresent()) {
            this.handle(optionalChannel.get(), packet);
        } else {
            LoggerUtil.logMissingChannel(logger, channelId);
        }
    }

    private void handle(TextChannel channel, JsonNode packet) {
        Message message = this.api.getOrCreateMessage(channel, packet);
        MessageCreateEventImpl event = new MessageCreateEventImpl(message);
        Optional<Server> optionalServer = channel.asServerChannel().map(ServerChannel::getServer);
        MessageAuthor author = message.getAuthor();
        message.getServerThreadChannel().ifPresent(stc -> ((ServerThreadChannelImpl)stc).setTotalNumberOfMessagesSent(stc.getTotalNumberOfMessagesSent() + 1));
        this.api.getEventDispatcher().dispatchMessageCreateEvent(optionalServer.map(DispatchQueueSelector.class::cast).orElse(this.api), (Server)optionalServer.orElse(null), channel, author.asUser().orElse(null), author.isWebhook() ? Long.valueOf(author.getId()) : null, (MessageCreateEvent)event);
        message.getReferencedMessage().ifPresent(referencedMessage -> {
            MessageReplyEventImpl replyEvent = new MessageReplyEventImpl(message, (Message)referencedMessage);
            HashSet<User> users = new HashSet<User>();
            referencedMessage.getUserAuthor().ifPresent(users::add);
            message.getUserAuthor().ifPresent(users::add);
            HashSet<Long> webhookIds = new HashSet<Long>();
            message.getAuthor().getWebhookId().ifPresent(webhookIds::add);
            referencedMessage.getAuthor().getWebhookId().ifPresent(webhookIds::add);
            this.api.getEventDispatcher().dispatchMessageReplyEvent(optionalServer.map(DispatchQueueSelector.class::cast).orElse(this.api), Collections.singleton(referencedMessage), optionalServer.map(Collections::singleton).orElse(null), Collections.singleton(message.getChannel()), users, webhookIds, (MessageReplyEvent)replyEvent);
        });
    }
}

