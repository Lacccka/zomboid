/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.emoji;

import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.event.server.emoji.KnownCustomEmojiDeleteEvent;
import org.javacord.core.event.server.emoji.KnownCustomEmojiEventImpl;

public class KnownCustomEmojiDeleteEventImpl
extends KnownCustomEmojiEventImpl
implements KnownCustomEmojiDeleteEvent {
    public KnownCustomEmojiDeleteEventImpl(KnownCustomEmoji emoji) {
        super(emoji);
    }
}

