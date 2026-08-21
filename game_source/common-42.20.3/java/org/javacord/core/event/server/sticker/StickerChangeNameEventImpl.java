/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.sticker;

import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.event.server.sticker.StickerChangeNameEvent;
import org.javacord.core.event.server.sticker.StickerEventImpl;

public class StickerChangeNameEventImpl
extends StickerEventImpl
implements StickerChangeNameEvent {
    private final String oldName;
    private final String newName;

    public StickerChangeNameEventImpl(Sticker sticker, String oldName, String newName) {
        super(sticker);
        this.oldName = oldName;
        this.newName = newName;
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

