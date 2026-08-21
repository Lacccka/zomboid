/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server.role;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.ServerEvent;

public interface RoleEvent
extends ServerEvent {
    public Role getRole();
}

