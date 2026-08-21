/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.permission;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.permission.internal.RoleUpdaterDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class RoleUpdater {
    private final RoleUpdaterDelegate delegate;

    public RoleUpdater(Role role) {
        this.delegate = DelegateFactory.createRoleUpdaterDelegate(role);
    }

    public RoleUpdater setAuditLogReason(String reason) {
        this.delegate.setAuditLogReason(reason);
        return this;
    }

    public RoleUpdater setName(String name) {
        this.delegate.setName(name);
        return this;
    }

    public RoleUpdater setPermissions(Permissions permissions) {
        this.delegate.setPermissions(permissions);
        return this;
    }

    public RoleUpdater setColor(Color color) {
        this.delegate.setColor(color);
        return this;
    }

    public RoleUpdater setDisplaySeparatelyFlag(boolean displaySeparately) {
        this.delegate.setDisplaySeparatelyFlag(displaySeparately);
        return this;
    }

    public RoleUpdater setMentionableFlag(boolean mentionable) {
        this.delegate.setMentionableFlag(mentionable);
        return this;
    }

    public CompletableFuture<Void> update() {
        return this.delegate.update();
    }
}

