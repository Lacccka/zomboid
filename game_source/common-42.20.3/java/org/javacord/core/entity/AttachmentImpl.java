/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Attachment;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.core.util.FileContainer;
import org.javacord.core.util.logging.LoggerUtil;

public class AttachmentImpl
implements Attachment {
    private static final Logger logger = LoggerUtil.getLogger(AttachmentImpl.class);
    private final DiscordApi api;
    private final long id;
    private final String fileName;
    private final String description;
    private final int size;
    private final String url;
    private final String proxyUrl;
    private final Integer height;
    private final Integer width;
    private final Boolean ephemeral;

    public AttachmentImpl(DiscordApi api, JsonNode data) {
        this.api = api;
        this.id = data.get("id").asLong();
        this.fileName = data.get("filename").asText();
        this.description = data.hasNonNull("description") ? data.get("description").asText() : null;
        this.size = data.get("size").asInt();
        this.url = data.get("url").asText();
        this.proxyUrl = data.get("proxy_url").asText();
        this.height = data.hasNonNull("height") ? Integer.valueOf(data.get("height").asInt()) : null;
        this.width = data.hasNonNull("width") ? Integer.valueOf(data.get("width").asInt()) : null;
        this.ephemeral = data.hasNonNull("ephemeral") ? Boolean.valueOf(data.get("ephemeral").asBoolean()) : null;
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
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(this.description);
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public URL getUrl() {
        try {
            return new URL(this.url);
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the url of the attachment is malformed! Please contact the developer!", (Throwable)e);
            return null;
        }
    }

    @Override
    public URL getProxyUrl() {
        try {
            return new URL(this.proxyUrl);
        }
        catch (MalformedURLException e) {
            logger.warn("Seems like the proxy url of the attachment is malformed! Please contact the developer!", (Throwable)e);
            return null;
        }
    }

    @Override
    public Optional<Integer> getHeight() {
        return Optional.ofNullable(this.height);
    }

    @Override
    public Optional<Integer> getWidth() {
        return Optional.ofNullable(this.width);
    }

    @Override
    public Optional<Boolean> isEphemeral() {
        return Optional.ofNullable(this.ephemeral);
    }

    @Override
    public InputStream asInputStream() throws IOException {
        return new FileContainer(this.getUrl()).asInputStream(this.getApi());
    }

    @Override
    public CompletableFuture<byte[]> asByteArray() {
        return new FileContainer(this.getUrl()).asByteArray(this.getApi());
    }

    @Override
    public CompletableFuture<BufferedImage> asImage() {
        return new FileContainer(this.getUrl()).asBufferedImage(this.getApi());
    }

    public ObjectNode toJsonNode() {
        ObjectNode attachments = JsonNodeFactory.instance.objectNode();
        attachments.put("id", this.id);
        attachments.put("filename", this.fileName);
        if (this.description != null) {
            attachments.put("description", this.description);
        }
        attachments.put("size", this.size);
        attachments.put("url", this.url);
        attachments.put("proxy_url", this.proxyUrl);
        if (this.height != null) {
            attachments.put("height", this.height);
        }
        if (this.width != null) {
            attachments.put("width", this.width);
        }
        if (this.ephemeral != null) {
            attachments.put("ephemeral", this.ephemeral);
        }
        return attachments;
    }

    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public String toString() {
        return String.format("Attachment (file name: %s, url: %s)", this.getFileName(), this.getUrl().toString());
    }
}

