/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.sticker;

import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.event.server.sticker.StickerCreateEvent;
import org.javacord.core.event.server.sticker.StickerEventImpl;

public class StickerCreateEventImpl
extends StickerEventImpl
implements StickerCreateEvent {
    public StickerCreateEventImpl(Sticker sticker) {
        super(sticker);
    }
}

