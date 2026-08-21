/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import org.javacord.api.entity.user.User;
import org.javacord.api.event.user.UserEvent;
import org.javacord.core.event.EventImpl;

public abstract class UserEventImpl
extends EventImpl
implements UserEvent {
    private final User user;

    public UserEventImpl(User user) {
        super(user.getApi());
        this.user = user;
    }

    @Override
    public User getUser() {
        return this.user;
    }
}

