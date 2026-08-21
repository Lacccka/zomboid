/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Region;
import org.javacord.api.entity.server.DefaultMessageNotificationLevel;
import org.javacord.api.entity.server.ExplicitContentFilterLevel;
import org.javacord.api.entity.server.VerificationLevel;
import org.javacord.api.entity.server.internal.ServerBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class ServerBuilder {
    private final ServerBuilderDelegate delegate;

    public ServerBuilder(DiscordApi api) {
        this.delegate = DelegateFactory.createServerBuilderDelegate(api);
    }

    public ServerBuilder setName(String name) {
        this.delegate.setName(name);
        return this;
    }

    public ServerBuilder setRegion(Region region) {
        this.delegate.setRegion(region);
        return this;
    }

    public ServerBuilder setExplicitContentFilterLevel(ExplicitContentFilterLevel explicitContentFilterLevel) {
        this.delegate.setExplicitContentFilterLevel(explicitContentFilterLevel);
        return this;
    }

    public ServerBuilder setVerificationLevel(VerificationLevel verificationLevel) {
        this.delegate.setVerificationLevel(verificationLevel);
        return this;
    }

    public ServerBuilder setDefaultMessageNotificationLevel(DefaultMessageNotificationLevel defaultMessageNotificationLevel) {
        this.delegate.setDefaultMessageNotificationLevel(defaultMessageNotificationLevel);
        return this;
    }

    public ServerBuilder setAfkTimeoutInSeconds(int afkTimeout) {
        this.delegate.setAfkTimeoutInSeconds(afkTimeout);
        return this;
    }

    public ServerBuilder setIcon(BufferedImage icon) {
        this.delegate.setIcon(icon);
        return this;
    }

    public ServerBuilder setIcon(BufferedImage icon, String fileType) {
        this.delegate.setIcon(icon, fileType);
        return this;
    }

    public ServerBuilder setIcon(File icon) {
        this.delegate.setIcon(icon);
        return this;
    }

    public ServerBuilder setIcon(Icon icon) {
        this.delegate.setIcon(icon);
        return this;
    }

    public ServerBuilder setIcon(URL icon) {
        this.delegate.setIcon(icon);
        return this;
    }

    public ServerBuilder setIcon(byte[] icon) {
        this.delegate.setIcon(icon);
        return this;
    }

    public ServerBuilder setIcon(byte[] icon, String fileType) {
        this.delegate.setIcon(icon, fileType);
        return this;
    }

    public ServerBuilder setIcon(InputStream icon) {
        this.delegate.setIcon(icon);
        return this;
    }

    public ServerBuilder setIcon(InputStream icon, String fileType) {
        this.delegate.setIcon(icon, fileType);
        return this;
    }

    public CompletableFuture<Long> create() {
        return this.delegate.create();
    }
}

