/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.sticker;

import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.event.server.sticker.StickerChangeTagsEvent;
import org.javacord.core.event.server.sticker.StickerEventImpl;

public class StickerChangeTagsEventImpl
extends StickerEventImpl
implements StickerChangeTagsEvent {
    private final String oldTags;
    private final String newTags;

    public StickerChangeTagsEventImpl(Sticker sticker, String oldTags, String newTags) {
        super(sticker);
        this.oldTags = oldTags;
        this.newTags = newTags;
    }

    @Override
    public String getOldTags() {
        return this.oldTags;
    }

    @Override
    public String getNewTags() {
        return this.newTags;
    }
}

