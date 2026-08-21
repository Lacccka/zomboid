/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.webhook;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.channel.TextableRegularServerChannel;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.entity.webhook.internal.WebhookBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class WebhookBuilder {
    private final WebhookBuilderDelegate delegate;

    public WebhookBuilder(TextableRegularServerChannel channel) {
        this.delegate = DelegateFactory.createWebhookBuilderDelegate(channel);
    }

    public WebhookBuilder setAuditLogReason(String reason) {
        this.delegate.setAuditLogReason(reason);
        return this;
    }

    public WebhookBuilder setName(String name) {
        this.delegate.setName(name);
        return this;
    }

    public WebhookBuilder setAvatar(BufferedImage avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public WebhookBuilder setAvatar(BufferedImage avatar, String fileType) {
        this.delegate.setAvatar(avatar, fileType);
        return this;
    }

    public WebhookBuilder setAvatar(File avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public WebhookBuilder setAvatar(Icon avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public WebhookBuilder setAvatar(URL avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public WebhookBuilder setAvatar(byte[] avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public WebhookBuilder setAvatar(byte[] avatar, String fileType) {
        this.delegate.setAvatar(avatar, fileType);
        return this;
    }

    public WebhookBuilder setAvatar(InputStream avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public WebhookBuilder setAvatar(InputStream avatar, String fileType) {
        this.delegate.setAvatar(avatar, fileType);
        return this;
    }

    public CompletableFuture<IncomingWebhook> create() {
        return this.delegate.create();
    }
}

