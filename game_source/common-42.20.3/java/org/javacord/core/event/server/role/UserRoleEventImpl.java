/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.role;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.role.UserRoleEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.server.role.RoleEventImpl;

public abstract class UserRoleEventImpl
extends RoleEventImpl
implements UserRoleEvent {
    private final Member member;

    public UserRoleEventImpl(Role role, Member member) {
        super(role);
        this.member = member;
    }

    @Override
    public User getUser() {
        return this.member.getUser();
    }
}

