/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.emoji;

import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.event.server.emoji.KnownCustomEmojiCreateEvent;
import org.javacord.core.event.server.emoji.KnownCustomEmojiEventImpl;

public class KnownCustomEmojiCreateEventImpl
extends KnownCustomEmojiEventImpl
implements KnownCustomEmojiCreateEvent {
    public KnownCustomEmojiCreateEventImpl(KnownCustomEmoji emoji) {
        super(emoji);
    }
}

