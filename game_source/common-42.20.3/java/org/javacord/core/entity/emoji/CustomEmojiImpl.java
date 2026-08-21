/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.emoji;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.util.logging.LoggerUtil;

public class CustomEmojiImpl
implements CustomEmoji {
    private static final Logger logger = LoggerUtil.getLogger(CustomEmojiImpl.class);
    private final DiscordApiImpl api;
    private final long id;
    protected volatile String name;
    private final boolean animated;

    public CustomEmojiImpl(DiscordApiImpl api, JsonNode data) {
        this.api = api;
        this.id = data.get("id").asLong();
        JsonNode nameNode = data.get("name");
        this.name = nameNode == null ? "" : nameNode.asText();
        JsonNode animatedNode = data.get("animated");
        this.animated = animatedNode != null && animatedNode.asBoolean();
    }

    public CustomEmojiImpl(DiscordApiImpl api, long id, String name, boolean animated) {
        this.api = api;
        this.id = id;
        this.name = name;
        this.animated = animated;
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
    public Icon getImage() {
        String urlString = "https://cdn.discordapp.com/emojis/" + this.getIdAsString() + (this.isAnimated() ? ".gif" : ".png");
        try {
            return new IconImpl(this.getApi(), new URL(urlString));
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the url of the avatar is malformed! Please contact the developer!", (Throwable)e);
            return null;
        }
    }

    @Override
    public boolean isAnimated() {
        return this.animated;
    }

    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public String toString() {
        return String.format("CustomEmoji (id: %s, name: %s, animated: %b)", this.getIdAsString(), this.getName(), this.isAnimated());
    }
}

