/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.embed;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedVideo;
import org.javacord.core.util.logging.LoggerUtil;

public class EmbedVideoImpl
implements EmbedVideo {
    private static final Logger logger = LoggerUtil.getLogger(EmbedVideoImpl.class);
    private final String url;
    private final int height;
    private final int width;

    public EmbedVideoImpl(JsonNode data) {
        this.url = data.has("url") ? data.get("url").asText() : null;
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
            logger.warn("Seems like the url of the embed video is malformed! Please contact the developer!", (Throwable)e);
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
}

