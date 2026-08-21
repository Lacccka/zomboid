/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import org.javacord.api.entity.Icon;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.user.UserChangeAvatarEvent;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.event.user.UserEventImpl;

public class UserChangeAvatarEventImpl
extends UserEventImpl
implements UserChangeAvatarEvent {
    private final String newAvatarHash;
    private final String oldAvatarHash;

    public UserChangeAvatarEventImpl(User user, String newAvatarHash, String oldAvatarHash) {
        super(user);
        this.newAvatarHash = newAvatarHash;
        this.oldAvatarHash = oldAvatarHash;
    }

    @Override
    public Icon getNewAvatar() {
        return UserImpl.getAvatar(this.api, this.newAvatarHash, this.getUser().getDiscriminator(), this.getUser().getId());
    }

    @Override
    public boolean newAvatarIsDefaultAvatar() {
        return this.newAvatarHash == null;
    }

    @Override
    public Icon getOldAvatar() {
        return UserImpl.getAvatar(this.api, this.oldAvatarHash, this.getUser().getDiscriminator(), this.getUser().getId());
    }

    @Override
    public boolean oldAvatarIsDefaultAvatar() {
        return this.oldAvatarHash == null;
    }
}

