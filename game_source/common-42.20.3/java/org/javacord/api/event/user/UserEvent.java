/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.user;

import org.javacord.api.entity.user.User;
import org.javacord.api.event.Event;

public interface UserEvent
extends Event {
    public User getUser();
}

