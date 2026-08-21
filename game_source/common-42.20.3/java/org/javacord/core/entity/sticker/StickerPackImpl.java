/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.sticker;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.sticker.Sticker;
import org.javacord.api.entity.sticker.StickerPack;
import org.javacord.core.DiscordApiImpl;

public class StickerPackImpl
implements StickerPack {
    private final DiscordApiImpl api;
    private final long id;
    private final Set<Sticker> stickers = new HashSet<Sticker>();
    private final String name;
    private final long skuId;
    private final Long coverStickerId;
    private final String description;
    private final long bannerAssetId;

    public StickerPackImpl(DiscordApiImpl api, JsonNode data) {
        this.api = api;
        this.id = data.get("id").asLong();
        for (JsonNode stickerJson : data.get("stickers")) {
            Sticker sticker = api.getOrCreateSticker(stickerJson);
            this.stickers.add(sticker);
        }
        this.name = data.get("name").asText();
        this.skuId = data.get("sku_id").asLong();
        this.coverStickerId = data.has("cover_sticker_id") ? Long.valueOf(data.get("cover_sticker_id").asLong()) : null;
        this.description = data.get("description").asText();
        this.bannerAssetId = data.get("banner_asset_id").asLong();
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<Sticker> getStickers() {
        return this.stickers;
    }

    @Override
    public long getSkuId() {
        return this.skuId;
    }

    @Override
    public Optional<Long> getCoverStickerId() {
        return Optional.ofNullable(this.coverStickerId);
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public long getBannerAssetId() {
        return this.bannerAssetId;
    }
}

