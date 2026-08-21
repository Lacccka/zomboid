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
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedThumbnail;
import org.javacord.core.util.FileContainer;
import org.javacord.core.util.logging.LoggerUtil;

public class EmbedThumbnailImpl
implements EmbedThumbnail {
    private static final Logger logger = LoggerUtil.getLogger(EmbedThumbnailImpl.class);
    private final String url;
    private final String proxyUrl;
    private final int height;
    private final int width;

    public EmbedThumbnailImpl(JsonNode data) {
        this.url = data.has("url") ? data.get("url").asText() : null;
        this.proxyUrl = data.has("proxy_url") ? data.get("proxy_url").asText() : null;
        this.height = data.has("height") ? data.get("height").asInt() : -1;
        this.width = data.has("width") ? data.get("width").asInt() : -1;
    }

    @Override
    public URL getUrl() {
        if (this.url == null) {
            return null;
        }
        try {
            return new URL(this.url);
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the url of the embed thumbnail is malformed! Please contact the developer!", (Throwable)e);
            return null;
        }
    }

    @Override
    public URL getProxyUrl() {
        if (this.proxyUrl == null) {
            return null;
        }
        try {
            return new URL(this.proxyUrl);
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the embed thumbnail's proxy url is malformed! Please contact the developer!", (Throwable)e);
            return null;
        }
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public CompletableFuture<BufferedImage> asBufferedImage(DiscordApi api) {
        return new FileContainer(this.getUrl()).asBufferedImage(api);
    }

    @Override
    public CompletableFuture<byte[]> asByteArray(DiscordApi api) {
        return new FileContainer(this.getUrl()).asByteArray(api);
    }

    @Override
    public InputStream asInputStream(DiscordApi api) throws IOException {
        return new FileContainer(this.getUrl()).asInputStream(api);
    }
}

