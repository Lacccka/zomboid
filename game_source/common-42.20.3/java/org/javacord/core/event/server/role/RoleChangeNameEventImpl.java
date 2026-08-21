/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.role;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.role.RoleChangeNameEvent;
import org.javacord.core.event.server.role.RoleEventImpl;

public class RoleChangeNameEventImpl
extends RoleEventImpl
implements RoleChangeNameEvent {
    private final String newName;
    private final String oldName;

    public RoleChangeNameEventImpl(Role role, String newName, String oldName) {
        super(role);
        this.newName = newName;
        this.oldName = oldName;
    }

    @Override
    public String getOldName() {
        return this.oldName;
    }

    @Override
    public String getNewName() {
        return this.newName;
    }
}

