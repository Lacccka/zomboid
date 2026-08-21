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
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.entity.webhook.internal.WebhookUpdaterDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class WebhookUpdater {
    private final WebhookUpdaterDelegate delegate;

    public WebhookUpdater(Webhook webhook) {
        this.delegate = DelegateFactory.createWebhookUpdaterDelegate(webhook);
    }

    public WebhookUpdater setAuditLogReason(String reason) {
        this.delegate.setAuditLogReason(reason);
        return this;
    }

    public WebhookUpdater setName(String name) {
        this.delegate.setName(name);
        return this;
    }

    public WebhookUpdater setChannel(ServerTextChannel channel) {
        this.delegate.setChannel(channel);
        return this;
    }

    public WebhookUpdater setAvatar(BufferedImage avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public WebhookUpdater setAvatar(BufferedImage avatar, String fileType) {
        this.delegate.setAvatar(avatar, fileType);
        return this;
    }

    public WebhookUpdater setAvatar(File avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public WebhookUpdater setAvatar(Icon avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public WebhookUpdater setAvatar(URL avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public WebhookUpdater setAvatar(byte[] avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public WebhookUpdater setAvatar(byte[] avatar, String fileType) {
        this.delegate.setAvatar(avatar, fileType);
        return this;
    }

    public WebhookUpdater setAvatar(InputStream avatar) {
        this.delegate.setAvatar(avatar);
        return this;
    }

    public WebhookUpdater setAvatar(InputStream avatar, String fileType) {
        this.delegate.setAvatar(avatar, fileType);
        return this;
    }

    public WebhookUpdater removeAvatar() {
        this.delegate.removeAvatar();
        return this;
    }

    public CompletableFuture<Webhook> update() {
        return this.delegate.update();
    }
}

