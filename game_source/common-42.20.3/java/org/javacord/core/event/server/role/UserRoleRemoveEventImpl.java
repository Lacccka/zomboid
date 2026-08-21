/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.role;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.server.role.UserRoleEventImpl;

public class UserRoleRemoveEventImpl
extends UserRoleEventImpl
implements UserRoleRemoveEvent {
    public UserRoleRemoveEventImpl(Role role, Member member) {
        super(role, member);
    }
}

