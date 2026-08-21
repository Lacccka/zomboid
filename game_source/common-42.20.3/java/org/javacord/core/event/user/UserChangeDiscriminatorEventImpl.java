/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import org.javacord.api.entity.user.User;
import org.javacord.api.event.user.UserChangeDiscriminatorEvent;
import org.javacord.core.event.user.UserEventImpl;

public class UserChangeDiscriminatorEventImpl
extends UserEventImpl
implements UserChangeDiscriminatorEvent {
    private final String newDiscriminator;
    private final String oldDiscriminator;

    public UserChangeDiscriminatorEventImpl(User user, String newDiscriminator, String oldDiscriminator) {
        super(user);
        this.newDiscriminator = newDiscriminator;
        this.oldDiscriminator = oldDiscriminator;
    }

    @Override
    public String getNewDiscriminator() {
        return this.newDiscriminator;
    }

    @Override
    public String getOldDiscriminator() {
        return this.oldDiscriminator;
    }
}

