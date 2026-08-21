/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.role;

import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.role.RoleChangePermissionsEvent;
import org.javacord.core.event.server.role.RoleEventImpl;

public class RoleChangePermissionsEventImpl
extends RoleEventImpl
implements RoleChangePermissionsEvent {
    private final Permissions newPermissions;
    private final Permissions oldPermissions;

    public RoleChangePermissionsEventImpl(Role role, Permissions newPermissions, Permissions oldPermissions) {
        super(role);
        this.newPermissions = newPermissions;
        this.oldPermissions = oldPermissions;
    }

    @Override
    public Permissions getNewPermissions() {
        return this.newPermissions;
    }

    @Override
    public Permissions getOldPermissions() {
        return this.oldPermissions;
    }
}

