/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.permission;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;

public interface Permissions {
    public long getAllowedBitmask();

    public long getDeniedBitmask();

    public PermissionState getState(PermissionType var1);

    default public PermissionsBuilder toBuilder() {
        return new PermissionsBuilder(this);
    }

    default public Set<PermissionType> getAllowedPermission() {
        return Collections.unmodifiableSet(Arrays.stream(PermissionType.values()).filter(type -> this.getState((PermissionType)((Object)type)) == PermissionState.ALLOWED).collect(Collectors.toSet()));
    }

    default public Set<PermissionType> getDeniedPermissions() {
        return Collections.unmodifiableSet(Arrays.stream(PermissionType.values()).filter(type -> this.getState((PermissionType)((Object)type)) == PermissionState.DENIED).collect(Collectors.toSet()));
    }

    default public Set<PermissionType> getUnsetPermissions() {
        return Collections.unmodifiableSet(Arrays.stream(PermissionType.values()).filter(type -> this.getState((PermissionType)((Object)type)) == PermissionState.UNSET).collect(Collectors.toSet()));
    }

    public boolean isEmpty();

    public static Permissions fromBitmask(long bitmask) {
        return Permissions.fromBitmask(bitmask, 0L);
    }

    public static Permissions fromBitmask(long allowedBitmask, long deniedBitmask) {
        PermissionsBuilder permissionsBuilder = new PermissionsBuilder();
        for (PermissionType permissionType : PermissionType.values()) {
            if (permissionType.isSet(allowedBitmask) && permissionType.isSet(deniedBitmask)) {
                permissionsBuilder.setState(permissionType, PermissionState.UNSET);
                continue;
            }
            if (permissionType.isSet(allowedBitmask)) {
                permissionsBuilder.setState(permissionType, PermissionState.ALLOWED);
                continue;
            }
            if (permissionType.isSet(deniedBitmask)) {
                permissionsBuilder.setState(permissionType, PermissionState.DENIED);
                continue;
            }
            permissionsBuilder.setState(permissionType, PermissionState.UNSET);
        }
        return permissionsBuilder.build();
    }
}

