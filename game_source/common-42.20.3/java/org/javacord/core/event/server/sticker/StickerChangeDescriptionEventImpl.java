/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.sticker;

import java.util.Optional;
import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.event.server.sticker.StickerChangeDescriptionEvent;
import org.javacord.core.event.server.sticker.StickerEventImpl;

public class StickerChangeDescriptionEventImpl
extends StickerEventImpl
implements StickerChangeDescriptionEvent {
    private final String oldDescription;
    private final String newDescription;

    public StickerChangeDescriptionEventImpl(Sticker sticker, String oldDescription, String newDescription) {
        super(sticker);
        this.oldDescription = oldDescription;
        this.newDescription = newDescription;
    }

    @Override
    public Optional<String> getOldDescription() {
        return Optional.ofNullable(this.oldDescription);
    }

    @Override
    public Optional<String> getNewDescription() {
        return Optional.ofNullable(this.newDescription);
    }
}

