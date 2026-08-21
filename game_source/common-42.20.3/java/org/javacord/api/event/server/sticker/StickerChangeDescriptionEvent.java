/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server.sticker;

import java.util.Optional;
import org.javacord.api.event.server.sticker.StickerEvent;

public interface StickerChangeDescriptionEvent
extends StickerEvent {
    public Optional<String> getOldDescription();

    public Optional<String> getNewDescription();
}

