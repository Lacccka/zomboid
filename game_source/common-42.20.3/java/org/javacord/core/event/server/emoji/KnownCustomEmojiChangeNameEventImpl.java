/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.emoji;

import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.event.server.emoji.KnownCustomEmojiChangeNameEvent;
import org.javacord.core.event.server.emoji.KnownCustomEmojiEventImpl;

public class KnownCustomEmojiChangeNameEventImpl
extends KnownCustomEmojiEventImpl
implements KnownCustomEmojiChangeNameEvent {
    private final String newName;
    private final String oldName;

    public KnownCustomEmojiChangeNameEventImpl(KnownCustomEmoji emoji, String newName, String oldName) {
        super(emoji);
        this.newName = newName;
        this.oldName = oldName;
    }

    @Override
    public String getOldName() {
        return this.oldName;
    }

    @Override
    public String getNewName() {
        return this.newName;
    }
}

