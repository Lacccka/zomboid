/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.sticker;

import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.event.server.sticker.StickerEvent;
import org.javacord.core.event.server.ServerEventImpl;

public abstract class StickerEventImpl
extends ServerEventImpl
implements StickerEvent {
    private final Sticker sticker;

    public StickerEventImpl(Sticker sticker) {
        super(sticker.getServer().get());
        this.sticker = sticker;
    }

    @Override
    public Sticker getSticker() {
        return this.sticker;
    }
}

