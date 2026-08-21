/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.embed;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedAuthor;
import org.javacord.core.util.FileContainer;
import org.javacord.core.util.logging.LoggerUtil;

public class EmbedAuthorImpl
implements EmbedAuthor {
    private static final Logger logger = LoggerUtil.getLogger(EmbedAuthorImpl.class);
    private final String name;
    private final String url;
    private final String iconUrl;
    private final String proxyIconUrl;

    public EmbedAuthorImpl(JsonNode data) {
        this.name = data.has("name") ? data.get("name").asText() : null;
        this.url = data.has("url") && !data.get("url").isNull() ? data.get("url").asText() : null;
        this.iconUrl = data.has("icon_url") && !data.get("icon_url").isNull() ? data.get("icon_url").asText() : null;
        this.proxyIconUrl = data.has("proxy_icon_url") && !data.get("proxy_icon_url").isNull() ? data.get("proxy_icon_url").asText() : null;
    }

    @Override
    public String getName() {
        return this.name;
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
            logger.warn("Seems like the url of the embed author is malformed! Please contact the developer!", (Throwable)e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<URL> getIconUrl() {
        if (this.iconUrl == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new URL(this.iconUrl));
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the icon url of the embed author is malformed! Please contact the developer!", (Throwable)e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<URL> getProxyIconUrl() {
        if (this.proxyIconUrl == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new URL(this.proxyIconUrl));
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the embed author's proxy icon url is malformed! Please contact the developer!", (Throwable)e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<CompletableFuture<BufferedImage>> iconAsBufferedImage(DiscordApi api) {
        return this.getIconUrl().map(url -> new FileContainer((URL)url).asBufferedImage(api));
    }

    @Override
    public Optional<CompletableFuture<byte[]>> iconAsByteArray(DiscordApi api) {
        return this.getIconUrl().map(url -> new FileContainer((URL)url).asByteArray(api));
    }

    @Override
    public Optional<InputStream> iconAsInputStream(DiscordApi api) throws IOException {
        URL iconUrl = this.getIconUrl().orElse(null);
        if (iconUrl == null) {
            return Optional.empty();
        }
        return Optional.of(new FileContainer(iconUrl).asInputStream(api));
    }
}

