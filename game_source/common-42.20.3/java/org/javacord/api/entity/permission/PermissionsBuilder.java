/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.permission;

import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.internal.PermissionsBuilderDelegate;
import org.javacord.api.util.internal.DelegateFactory;

public class PermissionsBuilder {
    private final PermissionsBuilderDelegate delegate;

    public PermissionsBuilder() {
        this.delegate = DelegateFactory.createPermissionsBuilderDelegate();
    }

    public PermissionsBuilder(Permissions permissions) {
        this.delegate = DelegateFactory.createPermissionsBuilderDelegate(permissions);
    }

    public PermissionsBuilder setState(PermissionType type, PermissionState state) {
        this.delegate.setState(type, state);
        return this;
    }

    public PermissionsBuilder setAllowed(PermissionType ... types) {
        for (PermissionType type : types) {
            this.setState(type, PermissionState.ALLOWED);
        }
        return this;
    }

    public PermissionsBuilder setAllAllowed() {
        return this.setAllowed(PermissionType.values());
    }

    public PermissionsBuilder setDenied(PermissionType ... types) {
        for (PermissionType type : types) {
            this.setState(type, PermissionState.DENIED);
        }
        return this;
    }

    public PermissionsBuilder setAllDenied() {
        return this.setDenied(PermissionType.values());
    }

    public PermissionsBuilder setUnset(PermissionType ... types) {
        for (PermissionType type : types) {
            this.setState(type, PermissionState.UNSET);
        }
        return this;
    }

    public PermissionsBuilder setAllUnset() {
        return this.setUnset(PermissionType.values());
    }

    public PermissionState getState(PermissionType type) {
        return this.delegate.getState(type);
    }

    public Permissions build() {
        return this.delegate.build();
    }
}

