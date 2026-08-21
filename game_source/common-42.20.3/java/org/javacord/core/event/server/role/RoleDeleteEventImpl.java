/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.role;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.role.RoleDeleteEvent;
import org.javacord.core.event.server.role.RoleEventImpl;

public class RoleDeleteEventImpl
extends RoleEventImpl
implements RoleDeleteEvent {
    public RoleDeleteEventImpl(Role role) {
        super(role);
    }
}

