/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.permission;

import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.internal.PermissionsBuilderDelegate;
import org.javacord.core.entity.permission.PermissionsImpl;

public class PermissionsBuilderDelegateImpl
implements PermissionsBuilderDelegate {
    private long allowed = 0L;
    private long denied = 0L;

    public PermissionsBuilderDelegateImpl() {
    }

    public PermissionsBuilderDelegateImpl(Permissions permissions) {
        this.allowed = permissions.getAllowedBitmask();
        this.denied = permissions.getDeniedBitmask();
    }

    @Override
    public void setState(PermissionType type, PermissionState state) {
        this.allowed = type.set(this.allowed, state == PermissionState.ALLOWED);
        this.denied = type.set(this.denied, state == PermissionState.DENIED);
    }

    @Override
    public PermissionState getState(PermissionType type) {
        if (type.isSet(this.allowed)) {
            return PermissionState.ALLOWED;
        }
        if (type.isSet(this.denied)) {
            return PermissionState.DENIED;
        }
        return PermissionState.UNSET;
    }

    @Override
    public Permissions build() {
        return new PermissionsImpl(this.allowed, this.denied);
    }
}

