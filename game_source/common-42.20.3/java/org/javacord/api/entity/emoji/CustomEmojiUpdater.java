/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.emoji;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.emoji.internal.CustomEmojiUpdaterDelegate;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.util.internal.DelegateFactory;

public class CustomEmojiUpdater {
    private final CustomEmojiUpdaterDelegate delegate;

    public CustomEmojiUpdater(KnownCustomEmoji emoji) {
        this.delegate = DelegateFactory.createCustomEmojiUpdaterDelegate(emoji);
    }

    public CustomEmojiUpdater setAuditLogReason(String reason) {
        this.delegate.setAuditLogReason(reason);
        return this;
    }

    public CustomEmojiUpdater setName(String name) {
        this.delegate.setName(name);
        return this;
    }

    public CustomEmojiUpdater addRoleToWhitelist(Role role) {
        this.delegate.addRoleToWhitelist(role);
        return this;
    }

    public CustomEmojiUpdater removeRoleFromWhitelist(Role role) {
        this.delegate.removeRoleFromWhitelist(role);
        return this;
    }

    public CustomEmojiUpdater removeWhitelist() {
        this.delegate.removeWhitelist();
        return this;
    }

    public CustomEmojiUpdater setWhitelist(Collection<Role> roles) {
        this.delegate.setWhitelist(roles);
        return this;
    }

    public CustomEmojiUpdater setWhitelist(Role ... roles) {
        this.delegate.setWhitelist(roles);
        return this;
    }

    public CompletableFuture<Void> update() {
        return this.delegate.update();
    }
}

