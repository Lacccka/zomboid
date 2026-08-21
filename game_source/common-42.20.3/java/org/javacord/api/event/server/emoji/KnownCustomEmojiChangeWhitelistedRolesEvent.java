/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server.emoji;

import java.util.Optional;
import java.util.Set;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.emoji.KnownCustomEmojiEvent;

public interface KnownCustomEmojiChangeWhitelistedRolesEvent
extends KnownCustomEmojiEvent {
    public Optional<Set<Role>> getOldWhitelistedRoles();

    public Optional<Set<Role>> getNewWhitelistedRoles();
}

