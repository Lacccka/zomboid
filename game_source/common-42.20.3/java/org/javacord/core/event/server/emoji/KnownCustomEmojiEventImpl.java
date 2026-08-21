/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.emoji;

import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.event.server.emoji.KnownCustomEmojiEvent;
import org.javacord.core.event.server.ServerEventImpl;

public abstract class KnownCustomEmojiEventImpl
extends ServerEventImpl
implements KnownCustomEmojiEvent {
    private final KnownCustomEmoji emoji;

    public KnownCustomEmojiEventImpl(KnownCustomEmoji emoji) {
        super(emoji.getServer());
        this.emoji = emoji;
    }

    @Override
    public KnownCustomEmoji getEmoji() {
        return this.emoji;
    }
}

