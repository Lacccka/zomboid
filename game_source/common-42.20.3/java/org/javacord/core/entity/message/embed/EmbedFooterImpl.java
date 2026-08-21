/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message.embed;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedFooter;
import org.javacord.core.util.logging.LoggerUtil;

public class EmbedFooterImpl
implements EmbedFooter {
    private static final Logger logger = LoggerUtil.getLogger(EmbedFooterImpl.class);
    private final String text;
    private final String iconUrl;
    private final String proxyIconUrl;

    public EmbedFooterImpl(JsonNode data) {
        this.text = data.has("text") ? data.get("text").asText() : null;
        this.iconUrl = data.has("icon_url") && !data.get("icon_url").isNull() ? data.get("icon_url").asText() : null;
        this.proxyIconUrl = data.has("proxy_icon_url") && !data.get("proxy_icon_url").isNull() ? data.get("proxy_icon_url").asText() : null;
    }

    @Override
    public Optional<String> getText() {
        return Optional.ofNullable(this.text);
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
            logger.warn("Seems like the icon url of the embed footer is malformed! Please contact the developer!", (Throwable)e);
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
            logger.warn("Seems like the embed footer's proxy icon url is malformed! Please contact the developer!", (Throwable)e);
            return Optional.empty();
        }
    }
}

