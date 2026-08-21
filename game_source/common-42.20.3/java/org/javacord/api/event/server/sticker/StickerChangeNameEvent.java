/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server.sticker;

import org.javacord.api.event.server.sticker.StickerEvent;

public interface StickerChangeNameEvent
extends StickerEvent {
    public String getOldName();

    public String getNewName();
}

