/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.emoji;

import java.util.Optional;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.UpdatableFromCache;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.emoji.KnownCustomEmoji;

public interface CustomEmoji
extends DiscordEntity,
Nameable,
Emoji,
UpdatableFromCache<KnownCustomEmoji> {
    public Icon getImage();

    default public String getReactionTag() {
        return this.getName() + ":" + this.getIdAsString();
    }

    @Override
    default public String getMentionTag() {
        return "<" + (this.isAnimated() ? "a" : "") + ":" + this.getReactionTag() + ">";
    }

    @Override
    default public Optional<String> asUnicodeEmoji() {
        return Optional.empty();
    }

    @Override
    default public Optional<CustomEmoji> asCustomEmoji() {
        return Optional.of(this);
    }

    @Override
    default public Optional<KnownCustomEmoji> asKnownCustomEmoji() {
        return this instanceof KnownCustomEmoji ? Optional.of((KnownCustomEmoji)this) : Optional.empty();
    }

    @Override
    default public Optional<KnownCustomEmoji> getCurrentCachedInstance() {
        return this.getApi().getCustomEmojiById(this.getId());
    }
}

