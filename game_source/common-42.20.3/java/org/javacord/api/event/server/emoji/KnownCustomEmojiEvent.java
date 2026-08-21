/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server.emoji;

import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.event.server.ServerEvent;

public interface KnownCustomEmojiEvent
extends ServerEvent {
    public KnownCustomEmoji getEmoji();
}

