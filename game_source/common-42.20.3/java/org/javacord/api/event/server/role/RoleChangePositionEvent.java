/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server.role;

import org.javacord.api.event.server.role.RoleEvent;

public interface RoleChangePositionEvent
extends RoleEvent {
    public int getNewPosition();

    public int getOldPosition();

    public int getNewRawPosition();

    public int getOldRawPosition();
}

