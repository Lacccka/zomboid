/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server.sticker;

import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.event.server.ServerEvent;

public interface StickerEvent
extends ServerEvent {
    public Sticker getSticker();
}

