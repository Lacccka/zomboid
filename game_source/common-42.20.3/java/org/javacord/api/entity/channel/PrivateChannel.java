/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.channel.user.PrivateChannelAttachableListenerManager;

public interface PrivateChannel
extends TextChannel,
VoiceChannel,
PrivateChannelAttachableListenerManager {
    @Override
    default public boolean canWrite(User user) {
        return user.isYourself() || this.getRecipient().map(recipient -> recipient.equals(user)).orElse(false) != false;
    }

    @Override
    default public boolean canUseExternalEmojis(User user) {
        return this.canWrite(user);
    }

    @Override
    default public boolean canEmbedLinks(User user) {
        return this.canWrite(user);
    }

    @Override
    default public boolean canReadMessageHistory(User user) {
        return this.canSee(user);
    }

    @Override
    default public boolean canUseTts(User user) {
        return this.canWrite(user);
    }

    @Override
    default public boolean canAttachFiles(User user) {
        return user.isYourself() || this.getRecipient().map(recipient -> recipient.equals(user)).orElse(false) != false;
    }

    @Override
    default public boolean canAddNewReactions(User user) {
        return user.isYourself() || this.getRecipient().map(recipient -> recipient.equals(user)).orElse(false) != false;
    }

    @Override
    default public boolean canManageMessages(User user) {
        return this.canSee(user);
    }

    @Override
    default public boolean canMentionEveryone(User user) {
        return this.canSee(user);
    }

    @Override
    default public ChannelType getType() {
        return ChannelType.PRIVATE_CHANNEL;
    }

    public Optional<User> getRecipient();

    public Optional<Long> getRecipientId();

    default public Optional<PrivateChannel> getCurrentCachedInstance() {
        return this.getApi().getPrivateChannelById(this.getId());
    }

    @Override
    default public CompletableFuture<PrivateChannel> getLatestInstance() {
        Optional<PrivateChannel> currentCachedInstance = this.getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        }
        CompletableFuture<PrivateChannel> result = new CompletableFuture<PrivateChannel>();
        result.completeExceptionally(new NoSuchElementException());
        return result;
    }
}

