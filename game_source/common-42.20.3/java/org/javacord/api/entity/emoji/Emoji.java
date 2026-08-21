/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.emoji;

import java.util.Optional;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Mentionable;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.util.Specializable;

public interface Emoji
extends Mentionable,
Specializable<Emoji> {
    public Optional<String> asUnicodeEmoji();

    default public Optional<CustomEmoji> asCustomEmoji() {
        return this.as(CustomEmoji.class);
    }

    default public Optional<KnownCustomEmoji> asKnownCustomEmoji() {
        return this.as(KnownCustomEmoji.class);
    }

    default public boolean equalsEmoji(Emoji otherEmoji) {
        long otherId;
        if (otherEmoji.isUnicodeEmoji()) {
            return this.equalsEmoji(otherEmoji.asUnicodeEmoji().orElse(""));
        }
        if (this.isUnicodeEmoji()) {
            return false;
        }
        long thisId = this.asCustomEmoji().map(DiscordEntity::getId).orElseThrow(AssertionError::new);
        return thisId == (otherId = otherEmoji.asCustomEmoji().map(DiscordEntity::getId).orElseThrow(AssertionError::new).longValue());
    }

    default public boolean equalsEmoji(String otherEmoji) {
        return this.asUnicodeEmoji().map(emoji -> emoji.equals(otherEmoji)).orElse(false);
    }

    public boolean isAnimated();

    default public boolean isUnicodeEmoji() {
        return this.asUnicodeEmoji().isPresent();
    }

    default public boolean isCustomEmoji() {
        return this.asCustomEmoji().isPresent();
    }

    default public boolean isKnownCustomEmoji() {
        return this.asKnownCustomEmoji().isPresent();
    }
}

