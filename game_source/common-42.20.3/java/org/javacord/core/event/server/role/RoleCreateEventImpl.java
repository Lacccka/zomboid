/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.role;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.role.RoleCreateEvent;
import org.javacord.core.event.server.role.RoleEventImpl;

public class RoleCreateEventImpl
extends RoleEventImpl
implements RoleCreateEvent {
    public RoleCreateEventImpl(Role role) {
        super(role);
    }
}

