/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.user;

import java.util.Optional;
import org.javacord.api.event.server.ServerEvent;
import org.javacord.api.event.user.UserEvent;

public interface UserChangeNicknameEvent
extends UserEvent,
ServerEvent {
    public Optional<String> getNewNickname();

    public Optional<String> getOldNickname();
}

