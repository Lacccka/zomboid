/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.user;

import java.util.Optional;
import org.javacord.api.entity.Icon;
import org.javacord.api.event.server.ServerEvent;
import org.javacord.api.event.user.UserEvent;

public interface UserChangeServerAvatarEvent
extends ServerEvent,
UserEvent {
    public Optional<Icon> getOldServerAvatar();

    public Optional<Icon> getOldServerAvatar(int var1);

    public Optional<Icon> getNewServerAvatar();

    public Optional<Icon> getNewServerAvatar(int var1);
}

