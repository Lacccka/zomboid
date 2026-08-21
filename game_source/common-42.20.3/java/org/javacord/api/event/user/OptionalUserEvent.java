/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.user;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.Event;

public interface OptionalUserEvent
extends Event {
    public long getUserId();

    default public String getUserIdAsString() {
        return String.valueOf(this.getUserId());
    }

    default public Optional<User> getUser() {
        return this.getApi().getCachedUserById(this.getUserId());
    }

    default public CompletableFuture<User> requestUser() {
        return this.getApi().getUserById(this.getUserId());
    }
}

