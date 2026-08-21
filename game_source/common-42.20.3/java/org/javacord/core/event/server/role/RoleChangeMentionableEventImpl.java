/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.role;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.role.RoleChangeMentionableEvent;
import org.javacord.core.event.server.role.RoleEventImpl;

public class RoleChangeMentionableEventImpl
extends RoleEventImpl
implements RoleChangeMentionableEvent {
    private final boolean oldMentionable;

    public RoleChangeMentionableEventImpl(Role role, boolean oldMentionable) {
        super(role);
        this.oldMentionable = oldMentionable;
    }

    @Override
    public boolean getOldMentionableFlag() {
        return this.oldMentionable;
    }

    @Override
    public boolean getNewMentionableFlag() {
        return !this.oldMentionable;
    }
}

