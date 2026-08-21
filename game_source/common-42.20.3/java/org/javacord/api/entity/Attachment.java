/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;

public interface Attachment
extends DiscordEntity {
    public String getFileName();

    public Optional<String> getDescription();

    public int getSize();

    public URL getUrl();

    public URL getProxyUrl();

    default public boolean isImage() {
        return this.getHeight().isPresent();
    }

    public Optional<Integer> getHeight();

    public Optional<Integer> getWidth();

    public Optional<Boolean> isEphemeral();

    public InputStream asInputStream() throws IOException;

    public CompletableFuture<byte[]> asByteArray();

    public CompletableFuture<BufferedImage> asImage();

    default public boolean isSpoiler() {
        return this.getFileName().startsWith("SPOILER_");
    }
}

