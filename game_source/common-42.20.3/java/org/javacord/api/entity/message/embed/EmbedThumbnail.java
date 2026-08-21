/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.embed;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;

public interface EmbedThumbnail {
    public URL getUrl();

    public URL getProxyUrl();

    public int getHeight();

    public int getWidth();

    public CompletableFuture<BufferedImage> asBufferedImage(DiscordApi var1);

    public CompletableFuture<byte[]> asByteArray(DiscordApi var1);

    public InputStream asInputStream(DiscordApi var1) throws IOException;
}

