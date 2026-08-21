/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server.role;

import org.javacord.api.event.server.role.RoleEvent;

public interface RoleChangeNameEvent
extends RoleEvent {
    public String getOldName();

    public String getNewName();
}

