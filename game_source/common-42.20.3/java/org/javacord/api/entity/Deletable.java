/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.javacord.api.DiscordApi;

public interface Deletable {
    public DiscordApi getApi();

    default public CompletableFuture<Void> delete() {
        return this.delete(null);
    }

    public CompletableFuture<Void> delete(String var1);

    default public CompletableFuture<Void> deleteAfter(long duration, TimeUnit unit) {
        return this.deleteAfter(duration, unit, "Scheduled deletion");
    }

    default public CompletableFuture<Void> deleteAfter(long duration, TimeUnit unit, String auditLogReason) {
        return this.getApi().getThreadPool().runAfter(() -> this.delete(auditLogReason), duration, unit);
    }

    default public CompletableFuture<Void> deleteAfter(Duration duration) {
        return this.deleteAfter(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    default public CompletableFuture<Void> deleteAfter(Duration duration, String auditLogReason) {
        return this.deleteAfter(duration.toMillis(), TimeUnit.MILLISECONDS, auditLogReason);
    }
}

