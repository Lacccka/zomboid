/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.atomic.AtomicInteger;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Reaction;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.emoji.UnicodeEmojiImpl;

public class ReactionImpl
implements Reaction {
    private final Message message;
    private final Emoji emoji;
    private final AtomicInteger count = new AtomicInteger();
    private volatile boolean containsYou;

    public ReactionImpl(Message message, JsonNode data) {
        this.message = message;
        this.count.set(data.get("count").asInt());
        this.containsYou = data.get("me").asBoolean();
        JsonNode emojiJson = data.get("emoji");
        this.emoji = !emojiJson.has("id") || emojiJson.get("id").isNull() ? UnicodeEmojiImpl.fromString(emojiJson.get("name").asText()) : ((DiscordApiImpl)message.getApi()).getKnownCustomEmojiOrCreateCustomEmoji(emojiJson);
    }

    public ReactionImpl(Message message, Emoji emoji, int count, boolean you) {
        this.message = message;
        this.emoji = emoji;
        this.count.set(count);
        this.containsYou = you;
    }

    public void incrementCount(boolean you) {
        this.count.getAndIncrement();
        if (you) {
            this.containsYou = true;
        }
    }

    public void decrementCount(boolean you) {
        this.count.decrementAndGet();
        if (you) {
            this.containsYou = false;
        }
    }

    @Override
    public Message getMessage() {
        return this.message;
    }

    @Override
    public Emoji getEmoji() {
        return this.emoji.asCustomEmoji().map(DiscordEntity::getId).flatMap(id -> this.message.getApi().getCustomEmojiById((long)id)).map(Emoji.class::cast).orElse(this.emoji);
    }

    @Override
    public int getCount() {
        return this.count.get();
    }

    @Override
    public boolean containsYou() {
        return this.containsYou;
    }

    public String toString() {
        return String.format("Reaction (message id: %s, emoji: %s, count: %s)", this.getMessage().getIdAsString(), this.getEmoji(), this.getCount());
    }
}

