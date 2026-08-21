/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.user;

import org.javacord.api.event.user.UserEvent;

public interface UserChangeNameEvent
extends UserEvent {
    public String getNewName();

    public String getOldName();
}

