/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import java.util.Optional;
import org.javacord.api.entity.Icon;
import org.javacord.api.event.user.UserChangeServerAvatarEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.user.ServerUserEventImpl;

public class UserChangeServerAvatarEventImpl
extends ServerUserEventImpl
implements UserChangeServerAvatarEvent {
    private final Member newMember;
    private final Member oldMember;

    public UserChangeServerAvatarEventImpl(Member newMember, Member oldMember) {
        super(oldMember.getUser(), oldMember.getServer());
        this.newMember = newMember;
        this.oldMember = oldMember;
    }

    @Override
    public Optional<Icon> getOldServerAvatar() {
        return this.oldMember.getServerAvatar();
    }

    @Override
    public Optional<Icon> getOldServerAvatar(int size) {
        return this.oldMember.getServerAvatar(size);
    }

    @Override
    public Optional<Icon> getNewServerAvatar() {
        return this.newMember.getServerAvatar();
    }

    @Override
    public Optional<Icon> getNewServerAvatar(int size) {
        return this.newMember.getServerAvatar(size);
    }
}

