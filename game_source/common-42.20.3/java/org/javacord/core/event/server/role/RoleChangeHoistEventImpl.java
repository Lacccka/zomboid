/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.role;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.role.RoleChangeHoistEvent;
import org.javacord.core.event.server.role.RoleEventImpl;

public class RoleChangeHoistEventImpl
extends RoleEventImpl
implements RoleChangeHoistEvent {
    private final boolean oldHoist;

    public RoleChangeHoistEventImpl(Role role, boolean oldHoist) {
        super(role);
        this.oldHoist = oldHoist;
    }

    @Override
    public boolean getOldHoist() {
        return this.oldHoist;
    }

    @Override
    public boolean getNewHoist() {
        return !this.oldHoist;
    }
}

