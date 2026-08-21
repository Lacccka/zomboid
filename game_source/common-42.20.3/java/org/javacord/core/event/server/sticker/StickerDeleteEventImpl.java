/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.sticker;

import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.event.server.sticker.StickerDeleteEvent;
import org.javacord.core.event.server.sticker.StickerEventImpl;

public class StickerDeleteEventImpl
extends StickerEventImpl
implements StickerDeleteEvent {
    public StickerDeleteEventImpl(Sticker sticker) {
        super(sticker);
    }
}

