/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.embed;

import java.awt.Color;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.javacord.api.entity.message.embed.EmbedAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.embed.EmbedField;
import org.javacord.api.entity.message.embed.EmbedFooter;
import org.javacord.api.entity.message.embed.EmbedImage;
import org.javacord.api.entity.message.embed.EmbedProvider;
import org.javacord.api.entity.message.embed.EmbedThumbnail;
import org.javacord.api.entity.message.embed.EmbedVideo;

public interface Embed {
    public Optional<String> getTitle();

    public String getType();

    public Optional<String> getDescription();

    public Optional<URL> getUrl();

    public Optional<Instant> getTimestamp();

    public Optional<Color> getColor();

    public Optional<EmbedFooter> getFooter();

    public Optional<EmbedImage> getImage();

    public Optional<EmbedThumbnail> getThumbnail();

    public Optional<EmbedVideo> getVideo();

    public Optional<EmbedProvider> getProvider();

    public Optional<EmbedAuthor> getAuthor();

    public List<EmbedField> getFields();

    default public EmbedBuilder toBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        this.getTitle().ifPresent(builder::setTitle);
        this.getDescription().ifPresent(builder::setDescription);
        this.getUrl().ifPresent(url -> builder.setUrl(url.toString()));
        this.getTimestamp().ifPresent(builder::setTimestamp);
        this.getColor().ifPresent(builder::setColor);
        this.getFooter().ifPresent(footer -> builder.setFooter((String)footer.getText().orElse(null), (String)footer.getIconUrl().map(URL::toString).orElse(null)));
        this.getImage().ifPresent(image -> builder.setImage(image.getUrl().toString()));
        this.getThumbnail().ifPresent(thumbnail -> builder.setThumbnail(thumbnail.getUrl().toString()));
        this.getAuthor().ifPresent(author -> builder.setAuthor(author.getName(), (String)author.getUrl().map(URL::toString).orElse(null), (String)author.getIconUrl().map(URL::toString).orElse(null)));
        this.getFields().forEach(field -> builder.addField(field.getName(), field.getValue(), field.isInline()));
        return builder;
    }
}

