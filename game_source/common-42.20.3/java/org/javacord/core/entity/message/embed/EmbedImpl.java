/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.embed;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedAuthor;
import org.javacord.api.entity.message.embed.EmbedField;
import org.javacord.api.entity.message.embed.EmbedFooter;
import org.javacord.api.entity.message.embed.EmbedImage;
import org.javacord.api.entity.message.embed.EmbedProvider;
import org.javacord.api.entity.message.embed.EmbedThumbnail;
import org.javacord.api.entity.message.embed.EmbedVideo;
import org.javacord.core.entity.message.embed.EmbedAuthorImpl;
import org.javacord.core.entity.message.embed.EmbedFieldImpl;
import org.javacord.core.entity.message.embed.EmbedFooterImpl;
import org.javacord.core.entity.message.embed.EmbedImageImpl;
import org.javacord.core.entity.message.embed.EmbedProviderImpl;
import org.javacord.core.entity.message.embed.EmbedThumbnailImpl;
import org.javacord.core.entity.message.embed.EmbedVideoImpl;
import org.javacord.core.util.logging.LoggerUtil;

public class EmbedImpl
implements Embed {
    private static final Logger logger = LoggerUtil.getLogger(EmbedImpl.class);
    private final String title;
    private final String type;
    private final String description;
    private final String url;
    private final Instant timestamp;
    private final Color color;
    private final EmbedFooter footer;
    private final EmbedImage image;
    private final EmbedThumbnail thumbnail;
    private final EmbedVideo video;
    private final EmbedProvider provider;
    private final EmbedAuthor author;
    private final List<EmbedField> fields = new ArrayList<EmbedField>();

    public EmbedImpl(JsonNode data) {
        this.title = data.has("title") ? data.get("title").asText() : null;
        this.type = data.has("type") ? data.get("type").asText() : null;
        this.description = data.has("description") ? data.get("description").asText() : null;
        this.url = data.has("url") ? data.get("url").asText() : null;
        this.timestamp = data.has("timestamp") ? OffsetDateTime.parse(data.get("timestamp").asText()).toInstant() : null;
        this.color = data.has("color") ? new Color(data.get("color").asInt()) : null;
        this.footer = data.has("footer") ? new EmbedFooterImpl(data.get("footer")) : null;
        this.image = data.has("image") ? new EmbedImageImpl(data.get("image")) : null;
        this.thumbnail = data.has("thumbnail") ? new EmbedThumbnailImpl(data.get("thumbnail")) : null;
        this.video = data.has("video") ? new EmbedVideoImpl(data.get("video")) : null;
        this.provider = data.has("provider") ? new EmbedProviderImpl(data.get("provider")) : null;
        EmbedAuthor embedAuthor = this.author = data.has("author") ? new EmbedAuthorImpl(data.get("author")) : null;
        if (data.has("fields")) {
            for (JsonNode jsonField : data.get("fields")) {
                this.fields.add(new EmbedFieldImpl(jsonField));
            }
        }
    }

    @Override
    public Optional<String> getTitle() {
        return Optional.ofNullable(this.title);
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(this.description);
    }

    @Override
    public Optional<URL> getUrl() {
        if (this.url == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new URL(this.url));
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the url of the embed is malformed! Please contact the developer!", (Throwable)e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Instant> getTimestamp() {
        return Optional.ofNullable(this.timestamp);
    }

    @Override
    public Optional<Color> getColor() {
        return Optional.ofNullable(this.color);
    }

    @Override
    public Optional<EmbedFooter> getFooter() {
        return Optional.ofNullable(this.footer);
    }

    @Override
    public Optional<EmbedImage> getImage() {
        return Optional.ofNullable(this.image);
    }

    @Override
    public Optional<EmbedThumbnail> getThumbnail() {
        return Optional.ofNullable(this.thumbnail);
    }

    @Override
    public Optional<EmbedVideo> getVideo() {
        return Optional.ofNullable(this.video);
    }

    @Override
    public Optional<EmbedProvider> getProvider() {
        return Optional.ofNullable(this.provider);
    }

    @Override
    public Optional<EmbedAuthor> getAuthor() {
        return Optional.ofNullable(this.author);
    }

    @Override
    public List<EmbedField> getFields() {
        return this.fields;
    }
}

