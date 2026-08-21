/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.message.embed;

import java.net.URL;
import java.util.Optional;

public interface EmbedFooter {
    public Optional<String> getText();

    public Optional<URL> getIconUrl();

    public Optional<URL> getProxyIconUrl();
}

