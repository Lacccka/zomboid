/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.webhook;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Deletable;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.Updatable;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.entity.webhook.WebhookType;
import org.javacord.api.entity.webhook.WebhookUpdater;
import org.javacord.api.listener.webhook.WebhookAttachableListenerManager;

public interface Webhook
extends DiscordEntity,
Deletable,
Updatable<Webhook>,
WebhookAttachableListenerManager {
    public Optional<Long> getServerId();

    public Optional<Server> getServer();

    public long getChannelId();

    public Optional<TextChannel> getChannel();

    public Optional<User> getCreator();

    public Optional<String> getName();

    public Optional<Icon> getAvatar();

    public WebhookType getType();

    default public boolean isIncomingWebhook() {
        return this.getType() == WebhookType.INCOMING;
    }

    default public boolean isChannelFollowerWebhook() {
        return this.getType() == WebhookType.CHANNEL_FOLLOWER;
    }

    public Optional<IncomingWebhook> asIncomingWebhook();

    default public WebhookUpdater createUpdater() {
        return new WebhookUpdater(this);
    }

    default public CompletableFuture<Webhook> updateName(String name) {
        return this.createUpdater().setName(name).update();
    }

    default public CompletableFuture<Webhook> updateChannel(ServerTextChannel channel) {
        return this.createUpdater().setChannel(channel).update();
    }

    default public CompletableFuture<Webhook> updateAvatar(BufferedImage avatar) {
        return this.createUpdater().setAvatar(avatar).update();
    }

    default public CompletableFuture<Webhook> updateAvatar(BufferedImage avatar, String fileType) {
        return this.createUpdater().setAvatar(avatar, fileType).update();
    }

    default public CompletableFuture<Webhook> updateAvatar(File avatar) {
        return this.createUpdater().setAvatar(avatar).update();
    }

    default public CompletableFuture<Webhook> updateAvatar(Icon avatar) {
        return this.createUpdater().setAvatar(avatar).update();
    }

    default public CompletableFuture<Webhook> updateAvatar(URL avatar) {
        return this.createUpdater().setAvatar(avatar).update();
    }

    default public CompletableFuture<Webhook> updateAvatar(byte[] avatar) {
        return this.createUpdater().setAvatar(avatar).update();
    }

    default public CompletableFuture<Webhook> updateAvatar(byte[] avatar, String fileType) {
        return this.createUpdater().setAvatar(avatar, fileType).update();
    }

    default public CompletableFuture<Webhook> updateAvatar(InputStream avatar) {
        return this.createUpdater().setAvatar(avatar).update();
    }

    default public CompletableFuture<Webhook> updateAvatar(InputStream avatar, String fileType) {
        return this.createUpdater().setAvatar(avatar, fileType).update();
    }

    default public CompletableFuture<Webhook> removeAvatar() {
        return this.createUpdater().removeAvatar().update();
    }

    @Override
    default public CompletableFuture<Webhook> getLatestInstance() {
        return this.getApi().getWebhookById(this.getId());
    }
}

