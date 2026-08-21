/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.emoji;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.Deletable;
import org.javacord.api.entity.UpdatableFromCache;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.CustomEmojiUpdater;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.server.emoji.KnownCustomEmojiAttachableListenerManager;

public interface KnownCustomEmoji
extends CustomEmoji,
Deletable,
UpdatableFromCache<KnownCustomEmoji>,
KnownCustomEmojiAttachableListenerManager {
    public Server getServer();

    default public CustomEmojiUpdater createUpdater() {
        return new CustomEmojiUpdater(this);
    }

    public Optional<Set<Role>> getWhitelistedRoles();

    public boolean requiresColons();

    public boolean isManaged();

    public CompletableFuture<Optional<User>> getCreator();

    default public CompletableFuture<Void> updateName(String name) {
        return this.createUpdater().setName(name).update();
    }

    default public CompletableFuture<Void> updateWhitelist(Collection<Role> roles) {
        return this.createUpdater().setWhitelist(roles).update();
    }

    default public CompletableFuture<Void> updateWhitelist(Role ... roles) {
        return this.createUpdater().setWhitelist(roles).update();
    }

    default public CompletableFuture<Void> removeWhitelist() {
        return this.createUpdater().removeWhitelist().update();
    }

    @Override
    default public Optional<KnownCustomEmoji> getCurrentCachedInstance() {
        return this.getApi().getCustomEmojiById(this.getId());
    }
}

