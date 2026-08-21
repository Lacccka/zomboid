/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.embed;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedProvider;
import org.javacord.core.util.logging.LoggerUtil;

public class EmbedProviderImpl
implements EmbedProvider {
    private static final Logger logger = LoggerUtil.getLogger(EmbedProviderImpl.class);
    private final String name;
    private final String url;

    public EmbedProviderImpl(JsonNode data) {
        this.name = data.has("name") ? data.get("name").asText() : null;
        this.url = data.has("url") && !data.get("url").isNull() ? data.get("url").asText() : null;
    }

    @Override
    public String getName() {
        return this.name;
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
            logger.warn("Seems like the url of the embed provider is malformed! Please contact the developer!", (Throwable)e);
            return null;
        }
    }
}

