/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import org.javacord.api.event.user.UserChangeDeafenedEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.user.ServerUserEventImpl;

public class UserChangeDeafenedEventImpl
extends ServerUserEventImpl
implements UserChangeDeafenedEvent {
    private final Member newMember;
    private final Member oldMember;

    public UserChangeDeafenedEventImpl(Member newMember, Member oldMember) {
        super(newMember.getUser(), newMember.getServer());
        this.newMember = newMember;
        this.oldMember = oldMember;
    }

    @Override
    public boolean isNewDeafened() {
        return this.newMember.isSelfDeafened();
    }

    @Override
    public boolean isOldDeafened() {
        return this.oldMember.isSelfDeafened();
    }
}

