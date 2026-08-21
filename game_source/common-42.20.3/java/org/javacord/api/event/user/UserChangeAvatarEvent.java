/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.user;

import org.javacord.api.entity.Icon;
import org.javacord.api.event.user.UserEvent;

public interface UserChangeAvatarEvent
extends UserEvent {
    public Icon getNewAvatar();

    public boolean newAvatarIsDefaultAvatar();

    public Icon getOldAvatar();

    public boolean oldAvatarIsDefaultAvatar();
}

