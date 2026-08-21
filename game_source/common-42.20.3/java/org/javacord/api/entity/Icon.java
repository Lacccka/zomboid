/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public interface Icon {
    public URL getUrl();

    default public boolean isAnimated() {
        return this.getUrl().getFile().endsWith(".gif");
    }

    public CompletableFuture<byte[]> asByteArray();

    public InputStream asInputStream() throws IOException;

    public CompletableFuture<BufferedImage> asBufferedImage();
}

