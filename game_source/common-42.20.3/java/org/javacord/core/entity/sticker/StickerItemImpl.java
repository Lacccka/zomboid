/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.sticker;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.sticker.StickerFormatType;
import org.javacord.api.entity.sticker.StickerItem;
import org.javacord.core.DiscordApiImpl;

public class StickerItemImpl
implements StickerItem {
    private final DiscordApiImpl api;
    private final long id;
    private final String name;
    private final StickerFormatType formatType;

    public StickerItemImpl(DiscordApiImpl api, JsonNode data) {
        this.api = api;
        this.id = data.get("id").asLong();
        this.name = data.get("name").asText();
        this.formatType = StickerFormatType.fromId(data.get("format_type").asInt());
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
    public StickerFormatType getFormatType() {
        return this.formatType;
    }
}

