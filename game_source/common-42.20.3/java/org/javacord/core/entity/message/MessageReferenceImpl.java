/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageReference;
import org.javacord.core.DiscordApiImpl;

public class MessageReferenceImpl
implements MessageReference {
    private final DiscordApiImpl api;
    private final Long serverId;
    private final long channelId;
    private final Long messageId;
    private final Message message;

    public MessageReferenceImpl(DiscordApiImpl api, Message message, JsonNode data) {
        this.api = api;
        this.serverId = data.hasNonNull("guild_id") ? Long.valueOf(data.get("guild_id").asLong()) : null;
        this.channelId = data.get("channel_id").asLong();
        this.messageId = data.hasNonNull("message_id") ? Long.valueOf(data.get("message_id").asLong()) : null;
        this.message = message;
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public Optional<Long> getServerId() {
        return Optional.ofNullable(this.serverId);
    }

    @Override
    public long getChannelId() {
        return this.channelId;
    }

    @Override
    public Optional<Long> getMessageId() {
        return Optional.ofNullable(this.messageId);
    }

    @Override
    public Optional<Message> getMessage() {
        return Optional.ofNullable(this.message);
    }
}

