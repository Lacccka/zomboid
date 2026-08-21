/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import java.util.Optional;
import org.javacord.api.event.user.UserChangeNicknameEvent;
import org.javacord.core.entity.user.Member;
import org.javacord.core.event.user.ServerUserEventImpl;

public class UserChangeNicknameEventImpl
extends ServerUserEventImpl
implements UserChangeNicknameEvent {
    private final Member newMember;
    private final Member oldMember;

    public UserChangeNicknameEventImpl(Member newMember, Member oldMember) {
        super(newMember.getUser(), newMember.getServer());
        this.newMember = newMember;
        this.oldMember = oldMember;
    }

    @Override
    public Optional<String> getNewNickname() {
        return this.newMember.getNickname();
    }

    @Override
    public Optional<String> getOldNickname() {
        return this.oldMember.getNickname();
    }
}

