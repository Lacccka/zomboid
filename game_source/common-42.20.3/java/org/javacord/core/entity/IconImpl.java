/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Icon;
import org.javacord.core.util.FileContainer;

public class IconImpl
implements Icon {
    private final DiscordApi api;
    private final URL url;

    public IconImpl(DiscordApi api, URL url) {
        this.api = api;
        this.url = url;
    }

    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public CompletableFuture<byte[]> asByteArray() {
        return new FileContainer(this.getUrl()).asByteArray(this.getApi());
    }

    @Override
    public InputStream asInputStream() throws IOException {
        return new FileContainer(this.getUrl()).asInputStream(this.getApi());
    }

    @Override
    public CompletableFuture<BufferedImage> asBufferedImage() {
        return new FileContainer(this.getUrl()).asBufferedImage(this.getApi());
    }
}

