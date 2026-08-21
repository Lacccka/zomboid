/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.UpdatableFromCache;
import org.javacord.api.entity.channel.Categorizable;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.api.entity.channel.ServerStageVoiceChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.channel.TextableRegularServerChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.channel.internal.ChannelSpecialization;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.channel.ChannelAttachableListenerManager;

public interface Channel
extends DiscordEntity,
UpdatableFromCache,
ChannelAttachableListenerManager,
ChannelSpecialization {
    public ChannelType getType();

    default public Optional<PrivateChannel> asPrivateChannel() {
        return this.as(PrivateChannel.class);
    }

    default public Optional<ServerChannel> asServerChannel() {
        return this.as(ServerChannel.class);
    }

    default public Optional<RegularServerChannel> asRegularServerChannel() {
        return this.as(RegularServerChannel.class);
    }

    default public Optional<ChannelCategory> asChannelCategory() {
        return this.as(ChannelCategory.class);
    }

    default public Optional<Categorizable> asCategorizable() {
        return this.as(Categorizable.class);
    }

    default public Optional<ServerTextChannel> asServerTextChannel() {
        return this.as(ServerTextChannel.class);
    }

    default public Optional<TextableRegularServerChannel> asTextableRegularServerChannel() {
        return this.as(TextableRegularServerChannel.class);
    }

    default public Optional<ServerForumChannel> asServerForumChannel() {
        return this.as(ServerForumChannel.class);
    }

    default public Optional<ServerVoiceChannel> asServerVoiceChannel() {
        return this.as(ServerVoiceChannel.class);
    }

    default public Optional<ServerStageVoiceChannel> asServerStageVoiceChannel() {
        return this.as(ServerStageVoiceChannel.class);
    }

    default public Optional<TextChannel> asTextChannel() {
        return this.as(TextChannel.class);
    }

    default public Optional<VoiceChannel> asVoiceChannel() {
        return this.as(VoiceChannel.class);
    }

    default public Optional<ServerThreadChannel> asServerThreadChannel() {
        return this.as(ServerThreadChannel.class);
    }

    default public boolean canSee(User user) {
        Optional<PrivateChannel> privateChannel = this.asPrivateChannel();
        if (privateChannel.isPresent()) {
            return user.isYourself() || privateChannel.get().getRecipient().map(recipient -> recipient.equals(user)).orElse(false) != false;
        }
        Optional<RegularServerChannel> severChannel = this.asRegularServerChannel();
        return !severChannel.isPresent() || severChannel.get().hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.VIEW_CHANNEL);
    }

    default public boolean canYouSee() {
        return this.canSee(this.getApi().getYourself());
    }

    default public Optional<? extends Channel> getCurrentCachedInstance() {
        return this.getApi().getChannelById(this.getId());
    }

    @Override
    default public CompletableFuture<? extends Channel> getLatestInstance() {
        Optional<? extends Channel> currentCachedInstance = this.getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        }
        CompletableFuture result = new CompletableFuture();
        result.completeExceptionally(new NoSuchElementException());
        return result;
    }
}

