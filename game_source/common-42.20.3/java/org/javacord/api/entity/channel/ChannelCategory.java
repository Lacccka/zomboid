/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.Categorizable;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.channel.server.ChannelCategoryAttachableListenerManager;

public interface ChannelCategory
extends RegularServerChannel,
ChannelCategoryAttachableListenerManager {
    @Override
    default public ChannelType getType() {
        return ChannelType.CHANNEL_CATEGORY;
    }

    public List<RegularServerChannel> getChannels();

    default public List<ServerChannel> getVisibleChannels(User user) {
        ArrayList<RegularServerChannel> channels = new ArrayList<RegularServerChannel>(this.getChannels());
        channels.removeIf(channel -> !channel.canSee(user));
        return Collections.unmodifiableList(channels);
    }

    default public boolean canSeeAll(User user) {
        for (ServerChannel serverChannel : this.getChannels()) {
            if (serverChannel.canSee(user)) continue;
            return false;
        }
        return true;
    }

    default public boolean canYouSeeAll() {
        return this.canSeeAll(this.getApi().getYourself());
    }

    public boolean isNsfw();

    default public CompletableFuture<Void> addCategorizable(Categorizable categorizable) {
        return categorizable.updateCategory(this);
    }

    default public CompletableFuture<Void> removeCategorizable(Categorizable categorizable) {
        if (categorizable.getCategory().map(this::equals).orElse(false).booleanValue()) {
            return categorizable.removeCategory();
        }
        return CompletableFuture.completedFuture(null);
    }

    default public Optional<ChannelCategory> getCurrentCachedInstance() {
        return this.getApi().getServerById(this.getServer().getId()).flatMap(server -> server.getChannelCategoryById(this.getId()));
    }

    @Override
    default public CompletableFuture<ChannelCategory> getLatestInstance() {
        Optional<ChannelCategory> currentCachedInstance = this.getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        }
        CompletableFuture<ChannelCategory> result = new CompletableFuture<ChannelCategory>();
        result.completeExceptionally(new NoSuchElementException());
        return result;
    }
}

