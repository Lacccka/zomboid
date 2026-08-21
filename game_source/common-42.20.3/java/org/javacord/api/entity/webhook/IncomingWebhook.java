/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.webhook;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.message.WebhookMessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.webhook.Webhook;

public interface IncomingWebhook
extends Webhook,
Messageable {
    @Override
    default public boolean isIncomingWebhook() {
        return true;
    }

    @Override
    default public boolean isChannelFollowerWebhook() {
        return false;
    }

    public String getToken();

    default public URL getUrl() {
        try {
            return new URL(String.format("https://discord.com/api/webhooks/%s/%s", this.getIdAsString(), this.getToken()));
        }
        catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    default public CompletableFuture<Webhook> getLatestInstance() {
        return this.getLatestInstanceAsIncomingWebhook().thenApply(incomingWebhook -> incomingWebhook);
    }

    default public CompletableFuture<IncomingWebhook> getLatestInstanceAsIncomingWebhook() {
        return this.getApi().getIncomingWebhookByIdAndToken(this.getId(), this.getToken());
    }

    @Override
    default public CompletableFuture<Message> sendMessage(EmbedBuilder ... embeds) {
        return new WebhookMessageBuilder().addEmbeds(embeds).send(this);
    }

    @Override
    default public CompletableFuture<Message> sendMessage(String content, EmbedBuilder ... embeds) {
        return new WebhookMessageBuilder().append(content == null ? "" : content).addEmbeds(embeds).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, EmbedBuilder embed, String displayName, URL avatarUrl) {
        return new WebhookMessageBuilder().append(content == null ? "" : content).addEmbed(embed).setDisplayName(displayName).setDisplayAvatar(avatarUrl).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, EmbedBuilder embed, String displayName, Icon avatar) {
        return new WebhookMessageBuilder().append(content == null ? "" : content).addEmbed(embed).setDisplayName(displayName).setDisplayAvatar(avatar).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, String displayName, URL avatarUrl) {
        return new WebhookMessageBuilder().append(content == null ? "" : content).setDisplayName(displayName).setDisplayAvatar(avatarUrl).send(this);
    }

    default public CompletableFuture<Message> sendMessage(String content, String displayName, Icon avatar) {
        return new WebhookMessageBuilder().append(content == null ? "" : content).setDisplayName(displayName).setDisplayAvatar(avatar).send(this);
    }

    default public CompletableFuture<Message> sendMessage(EmbedBuilder embed, String displayName, URL avatarUrl) {
        return new WebhookMessageBuilder().addEmbed(embed).setDisplayName(displayName).setDisplayAvatar(avatarUrl).send(this);
    }

    default public CompletableFuture<Message> sendMessage(EmbedBuilder embed, String displayName, Icon avatar) {
        return new WebhookMessageBuilder().addEmbed(embed).setDisplayName(displayName).setDisplayAvatar(avatar).send(this);
    }
}

