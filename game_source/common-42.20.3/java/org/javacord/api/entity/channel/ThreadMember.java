/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public interface ThreadMember
extends DiscordEntity {
    @Override
    public DiscordApi getApi();

    public Server getServer();

    public long getUserId();

    default public Optional<User> getCachedUser() {
        return this.getApi().getCachedUserById(this.getUserId());
    }

    default public CompletableFuture<User> requestUser() {
        return this.getApi().getUserById(this.getUserId());
    }

    public Instant getJoinTimestamp();

    public int getFlags();
}

