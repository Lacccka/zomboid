/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import org.javacord.api.event.user.UserChangeSelfDeafenedEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.user.ServerUserEventImpl;

public class UserChangeSelfDeafenedEventImpl
extends ServerUserEventImpl
implements UserChangeSelfDeafenedEvent {
    private final Member newMember;
    private final Member oldMember;

    public UserChangeSelfDeafenedEventImpl(Member newMember, Member oldMember) {
        super(newMember.getUser(), newMember.getServer());
        this.newMember = newMember;
        this.oldMember = oldMember;
    }

    @Override
    public boolean isNewSelfDeafened() {
        return this.newMember.isSelfDeafened();
    }

    @Override
    public boolean isOldSelfDeafened() {
        return this.oldMember.isSelfDeafened();
    }
}

