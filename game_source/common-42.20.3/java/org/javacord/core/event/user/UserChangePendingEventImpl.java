/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import org.javacord.api.event.user.UserChangePendingEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.user.ServerUserEventImpl;

public class UserChangePendingEventImpl
extends ServerUserEventImpl
implements UserChangePendingEvent {
    private final boolean oldPending;
    private final boolean newPending;

    public UserChangePendingEventImpl(Member oldMember, Member newMember) {
        super(newMember.getUser(), newMember.getServer());
        this.oldPending = oldMember.isPending();
        this.newPending = newMember.isPending();
    }

    @Override
    public boolean getOldPending() {
        return this.oldPending;
    }

    @Override
    public boolean getNewPending() {
        return this.newPending;
    }
}

