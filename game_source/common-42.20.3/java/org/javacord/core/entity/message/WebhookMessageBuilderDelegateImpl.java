/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message;

import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.internal.WebhookMessageBuilderDelegate;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.core.entity.message.MessageBuilderBaseDelegateImpl;
import org.javacord.core.util.logging.LoggerUtil;

public class WebhookMessageBuilderDelegateImpl
extends MessageBuilderBaseDelegateImpl
implements WebhookMessageBuilderDelegate {
    private static final Logger logger = LoggerUtil.getLogger(WebhookMessageBuilderDelegateImpl.class);
    private URL avatarUrl = null;
    private String displayName = null;

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public void setDisplayAvatar(URL avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Override
    public void setDisplayAvatar(Icon avatar) {
        this.avatarUrl = avatar.getUrl();
    }

    @Override
    public CompletableFuture<Message> send(IncomingWebhook webhook) {
        return this.send(webhook.getIdAsString(), webhook.getToken(), this.displayName, this.avatarUrl, true, webhook.getApi());
    }

    @Override
    public CompletableFuture<Message> send(DiscordApi api, String webhookId, String webhookToken) {
        return this.send(webhookId, webhookToken, this.displayName, this.avatarUrl, true, api);
    }

    @Override
    public CompletableFuture<Void> sendSilently(IncomingWebhook webhook) {
        return this.sendSilently(webhook.getApi(), webhook.getIdAsString(), webhook.getToken());
    }

    @Override
    public CompletableFuture<Void> sendSilently(DiscordApi api, String webhookId, String webhookToken) {
        return this.send(webhookId, webhookToken, this.displayName, this.avatarUrl, false, api).thenApply(m -> null);
    }
}

