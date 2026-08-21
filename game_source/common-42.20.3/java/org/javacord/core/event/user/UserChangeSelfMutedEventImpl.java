/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import org.javacord.api.event.user.UserChangeSelfMutedEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.user.ServerUserEventImpl;

public class UserChangeSelfMutedEventImpl
extends ServerUserEventImpl
implements UserChangeSelfMutedEvent {
    private final Member newMember;
    private final Member oldMember;

    public UserChangeSelfMutedEventImpl(Member newMember, Member oldMember) {
        super(newMember.getUser(), newMember.getServer());
        this.newMember = newMember;
        this.oldMember = oldMember;
    }

    @Override
    public boolean isNewSelfMuted() {
        return this.newMember.isSelfMuted();
    }

    @Override
    public boolean isOldSelfMuted() {
        return this.oldMember.isSelfMuted();
    }
}

