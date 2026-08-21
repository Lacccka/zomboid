/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.permission;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.permission.internal.RoleBuilderDelegate;
import org.javacord.api.entity.server.Server;
import org.javacord.api.util.internal.DelegateFactory;

public class RoleBuilder {
    private final RoleBuilderDelegate delegate;

    public RoleBuilder(Server server) {
        this.delegate = DelegateFactory.createRoleBuilderDelegate(server);
    }

    public RoleBuilder setAuditLogReason(String reason) {
        this.delegate.setAuditLogReason(reason);
        return this;
    }

    public RoleBuilder setName(String name) {
        this.delegate.setName(name);
        return this;
    }

    public RoleBuilder setPermissions(Permissions permissions) {
        this.delegate.setPermissions(permissions);
        return this;
    }

    public RoleBuilder setColor(Color color) {
        this.delegate.setColor(color);
        return this;
    }

    public RoleBuilder setMentionable(boolean mentionable) {
        this.delegate.setMentionable(mentionable);
        return this;
    }

    public RoleBuilder setDisplaySeparately(boolean displaySeparately) {
        this.delegate.setDisplaySeparately(displaySeparately);
        return this;
    }

    public CompletableFuture<Role> create() {
        return this.delegate.create();
    }
}

