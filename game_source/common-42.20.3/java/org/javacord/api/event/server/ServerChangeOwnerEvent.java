/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import java.util.Optional;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeOwnerEvent
extends ServerEvent {
    default public Optional<User> getOldOwner() {
        return this.getApi().getCachedUserById(this.getOldOwnerId());
    }

    public long getOldOwnerId();

    default public Optional<User> getNewOwner() {
        return this.getApi().getCachedUserById(this.getNewOwnerId());
    }

    public long getNewOwnerId();
}

