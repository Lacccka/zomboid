/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server.role;

import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.event.server.role.RoleEvent;

public interface RoleChangePermissionsEvent
extends RoleEvent {
    public Permissions getNewPermissions();

    public Permissions getOldPermissions();
}

