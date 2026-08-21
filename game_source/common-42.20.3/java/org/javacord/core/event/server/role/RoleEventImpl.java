/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.role;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.role.RoleEvent;
import org.javacord.core.event.server.ServerEventImpl;

public abstract class RoleEventImpl
extends ServerEventImpl
implements RoleEvent {
    private final Role role;

    public RoleEventImpl(Role role) {
        super(role.getServer());
        this.role = role;
    }

    @Override
    public Role getRole() {
        return this.role;
    }
}

