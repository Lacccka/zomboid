/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.role;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.role.RoleChangePositionEvent;
import org.javacord.core.event.server.role.RoleEventImpl;

public class RoleChangePositionEventImpl
extends RoleEventImpl
implements RoleChangePositionEvent {
    private final int newPosition;
    private final int oldPosition;
    private final int newRawPosition;
    private final int oldRawPosition;

    public RoleChangePositionEventImpl(Role role, int newPosition, int oldPosition, int newRawPosition, int oldRawPosition) {
        super(role);
        this.newPosition = newPosition;
        this.oldPosition = oldPosition;
        this.newRawPosition = newRawPosition;
        this.oldRawPosition = oldRawPosition;
    }

    @Override
    public int getNewPosition() {
        return this.newPosition;
    }

    @Override
    public int getOldPosition() {
        return this.oldPosition;
    }

    @Override
    public int getNewRawPosition() {
        return this.newRawPosition;
    }

    @Override
    public int getOldRawPosition() {
        return this.oldRawPosition;
    }
}

