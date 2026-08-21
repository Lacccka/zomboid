/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.embed;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Nameable;

public interface EmbedAuthor
extends Nameable {
    public Optional<URL> getUrl();

    public Optional<URL> getIconUrl();

    public Optional<URL> getProxyIconUrl();

    public Optional<CompletableFuture<BufferedImage>> iconAsBufferedImage(DiscordApi var1);

    public Optional<CompletableFuture<byte[]>> iconAsByteArray(DiscordApi var1);

    public Optional<InputStream> iconAsInputStream(DiscordApi var1) throws IOException;
}

