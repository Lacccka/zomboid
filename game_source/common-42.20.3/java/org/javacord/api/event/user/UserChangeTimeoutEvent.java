/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.user;

import java.time.Instant;
import java.util.Optional;
import org.javacord.api.event.server.ServerEvent;
import org.javacord.api.event.user.UserEvent;

public interface UserChangeTimeoutEvent
extends UserEvent,
ServerEvent {
    public Optional<Instant> getNewTimeout();

    public Optional<Instant> getOldTimeout();
}

