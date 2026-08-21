/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity;

import java.time.Instant;
import org.javacord.api.DiscordApi;

public interface DiscordEntity {
    public static Instant getCreationTimestamp(long entityId) {
        return Instant.ofEpochMilli((entityId >>> 22) + 1420070400000L);
    }

    default public Instant getCreationTimestamp() {
        return DiscordEntity.getCreationTimestamp(this.getId());
    }

    public DiscordApi getApi();

    public long getId();

    default public String getIdAsString() {
        return Long.toUnsignedString(this.getId());
    }
}

