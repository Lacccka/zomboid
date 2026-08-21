/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import java.time.Instant;
import java.util.Optional;
import org.javacord.api.event.user.UserChangeTimeoutEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.user.ServerUserEventImpl;

public class UserChangeTimeoutEventImpl
extends ServerUserEventImpl
implements UserChangeTimeoutEvent {
    private final Member newMember;
    private final Member oldMember;

    public UserChangeTimeoutEventImpl(Member newMember, Member oldMember) {
        super(newMember.getUser(), newMember.getServer());
        this.newMember = newMember;
        this.oldMember = oldMember;
    }

    @Override
    public Optional<Instant> getNewTimeout() {
        return this.newMember.getTimeout();
    }

    @Override
    public Optional<Instant> getOldTimeout() {
        return this.oldMember.getTimeout();
    }
}

