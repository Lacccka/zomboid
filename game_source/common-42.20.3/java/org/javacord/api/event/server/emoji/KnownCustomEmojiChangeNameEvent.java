/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server.emoji;

import org.javacord.api.event.server.emoji.KnownCustomEmojiEvent;

public interface KnownCustomEmojiChangeNameEvent
extends KnownCustomEmojiEvent {
    public String getOldName();

    public String getNewName();
}

