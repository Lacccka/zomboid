/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;

public interface Updatable<T extends DiscordEntity> {
    public CompletableFuture<T> getLatestInstance();
}

