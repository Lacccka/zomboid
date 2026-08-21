/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.permission.internal;

import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;

public interface PermissionsBuilderDelegate {
    public void setState(PermissionType var1, PermissionState var2);

    public PermissionState getState(PermissionType var1);

    public Permissions build();
}

