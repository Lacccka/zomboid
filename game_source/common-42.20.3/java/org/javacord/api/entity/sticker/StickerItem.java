/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.sticker;

import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.sticker.StickerFormatType;

public interface StickerItem
extends DiscordEntity,
Nameable {
    public StickerFormatType getFormatType();
}

