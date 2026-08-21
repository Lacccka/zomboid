/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.user;

import org.javacord.api.event.server.ServerEvent;
import org.javacord.api.event.user.UserEvent;

public interface UserChangeDeafenedEvent
extends UserEvent,
ServerEvent {
    public boolean isNewDeafened();

    public boolean isOldDeafened();
}

