/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.sticker;

import java.util.Optional;
import java.util.Set;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Nameable;
import org.javacord.api.entity.sticker.Sticker;

public interface StickerPack
extends DiscordEntity,
Nameable {
    public Set<Sticker> getStickers();

    public long getSkuId();

    public Optional<Long> getCoverStickerId();

    public String getDescription();

    public long getBannerAssetId();
}

