/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.permission;

import java.util.Objects;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;

public class PermissionsImpl
implements Permissions {
    public static final Permissions EMPTY_PERMISSIONS = new PermissionsImpl(0L, 0L);
    private final long allowed;
    private final long denied;

    public PermissionsImpl(long allow, long deny) {
        this.allowed = allow;
        this.denied = deny;
    }

    public PermissionsImpl(long allow) {
        this.allowed = allow;
        long tempDenied = 0L;
        for (PermissionType type : PermissionType.values()) {
            if (type.isSet(allow)) continue;
            tempDenied = type.set(tempDenied, true);
        }
        this.denied = tempDenied;
    }

    @Override
    public long getAllowedBitmask() {
        return this.allowed;
    }

    @Override
    public long getDeniedBitmask() {
        return this.denied;
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
    public boolean isEmpty() {
        return this.allowed == 0L && this.denied == 0L;
    }

    public int hashCode() {
        return Objects.hash(this.allowed, this.denied);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof PermissionsImpl)) {
            return false;
        }
        PermissionsImpl other = (PermissionsImpl)obj;
        return other.allowed == this.allowed && other.denied == this.denied;
    }

    public String toString() {
        return "Permissions (allowed: " + this.getAllowedBitmask() + ", denied: " + this.getDeniedBitmask() + ")";
    }
}

