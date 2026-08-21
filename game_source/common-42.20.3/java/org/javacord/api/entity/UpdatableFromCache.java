/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Updatable;

public interface UpdatableFromCache<T extends DiscordEntity>
extends Updatable<T> {
    public Optional<T> getCurrentCachedInstance();

    @Override
    default public CompletableFuture<T> getLatestInstance() {
        Optional<T> currentCachedInstance = this.getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture((DiscordEntity)currentCachedInstance.get());
        }
        CompletableFuture result = new CompletableFuture();
        result.completeExceptionally(new NoSuchElementException());
        return result;
    }
}

