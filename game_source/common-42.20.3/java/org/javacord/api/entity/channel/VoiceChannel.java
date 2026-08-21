/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.listener.channel.VoiceChannelAttachableListenerManager;

public interface VoiceChannel
extends Channel,
VoiceChannelAttachableListenerManager {
    default public Optional<? extends VoiceChannel> getCurrentCachedInstance() {
        return this.getApi().getVoiceChannelById(this.getId());
    }

    @Override
    default public CompletableFuture<? extends VoiceChannel> getLatestInstance() {
        Optional<? extends VoiceChannel> currentCachedInstance = this.getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        }
        CompletableFuture result = new CompletableFuture();
        result.completeExceptionally(new NoSuchElementException());
        return result;
    }
}

