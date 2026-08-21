/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.emoji;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.emoji.KnownCustomEmoji;

public class UnicodeEmojiImpl
implements Emoji {
    private static final ConcurrentHashMap<String, UnicodeEmojiImpl> unicodeEmojis = new ConcurrentHashMap();
    private final String emoji;

    private UnicodeEmojiImpl(String emoji) {
        this.emoji = emoji;
    }

    public static UnicodeEmojiImpl fromString(String emoji) {
        return unicodeEmojis.computeIfAbsent(emoji, key -> new UnicodeEmojiImpl(emoji));
    }

    @Override
    public String getMentionTag() {
        return this.emoji;
    }

    @Override
    public Optional<String> asUnicodeEmoji() {
        return Optional.of(this.emoji);
    }

    @Override
    public Optional<CustomEmoji> asCustomEmoji() {
        return Optional.empty();
    }

    @Override
    public Optional<KnownCustomEmoji> asKnownCustomEmoji() {
        return Optional.empty();
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    public String toString() {
        return String.format("UnicodeEmoji (emoji: %s)", this.emoji);
    }
}

