/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.server.invite;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.javacord.api.entity.server.invite.WelcomeScreenChannel;

public class WelcomeScreenChannelImpl
implements WelcomeScreenChannel {
    private final long channelId;
    private final String description;
    private final Long emojiId;
    private final String emojiName;

    public WelcomeScreenChannelImpl(JsonNode data) {
        this.channelId = data.get("channel_id").asLong();
        this.description = data.get("description").asText();
        this.emojiId = data.hasNonNull("emoji_id") ? Long.valueOf(data.get("emoji_id").asLong()) : null;
        this.emojiName = data.hasNonNull("emoji_name") ? data.get("emoji_name").asText() : null;
    }

    @Override
    public long getChannelId() {
        return this.channelId;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Optional<Long> getEmojiId() {
        return Optional.ofNullable(this.emojiId);
    }

    @Override
    public Optional<String> getEmojiName() {
        return Optional.ofNullable(this.emojiName);
    }
}

