/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.emoji;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.emoji.KnownCustomEmojiChangeWhitelistedRolesEvent;
import org.javacord.core.event.server.emoji.KnownCustomEmojiEventImpl;

public class KnownCustomEmojiChangeWhitelistedRolesEventImpl
extends KnownCustomEmojiEventImpl
implements KnownCustomEmojiChangeWhitelistedRolesEvent {
    private final Set<Role> newWhitelist;
    private final Set<Role> oldWhitelist;

    public KnownCustomEmojiChangeWhitelistedRolesEventImpl(KnownCustomEmoji emoji, Set<Role> newWhitelist, Set<Role> oldWhitelist) {
        super(emoji);
        this.newWhitelist = newWhitelist;
        this.oldWhitelist = oldWhitelist;
    }

    @Override
    public Optional<Set<Role>> getOldWhitelistedRoles() {
        return this.oldWhitelist == null || this.oldWhitelist.isEmpty() ? Optional.empty() : Optional.of(Collections.unmodifiableSet(this.oldWhitelist));
    }

    @Override
    public Optional<Set<Role>> getNewWhitelistedRoles() {
        return this.newWhitelist == null || this.newWhitelist.isEmpty() ? Optional.empty() : Optional.of(Collections.unmodifiableSet(this.newWhitelist));
    }
}

