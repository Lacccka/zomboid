/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import org.javacord.api.entity.user.User;
import org.javacord.api.event.user.UserChangeNameEvent;
import org.javacord.core.event.user.UserEventImpl;

public class UserChangeNameEventImpl
extends UserEventImpl
implements UserChangeNameEvent {
    private final String newName;
    private final String oldName;

    public UserChangeNameEventImpl(User user, String newName, String oldName) {
        super(user);
        this.newName = newName;
        this.oldName = oldName;
    }

    @Override
    public String getNewName() {
        return this.newName;
    }

    @Override
    public String getOldName() {
        return this.oldName;
    }
}

