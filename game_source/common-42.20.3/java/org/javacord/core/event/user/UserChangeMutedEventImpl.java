/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import org.javacord.api.event.user.UserChangeMutedEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.user.ServerUserEventImpl;

public class UserChangeMutedEventImpl
extends ServerUserEventImpl
implements UserChangeMutedEvent {
    private final Member newMember;
    private final Member oldMember;

    public UserChangeMutedEventImpl(Member newMember, Member oldMember) {
        super(newMember.getUser(), newMember.getServer());
        this.newMember = newMember;
        this.oldMember = oldMember;
    }

    @Override
    public boolean isNewMuted() {
        return this.newMember.getServer().isSelfMuted(this.newMember.getUser());
    }

    @Override
    public boolean isOldMuted() {
        return !this.isNewMuted();
    }
}

