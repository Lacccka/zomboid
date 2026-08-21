/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import org.javacord.api.DiscordApi;
import org.javacord.api.event.user.OptionalUserEvent;
import org.javacord.core.event.EventImpl;

public abstract class OptionalUserEventImpl
extends EventImpl
implements OptionalUserEvent {
    private final long userId;

    public OptionalUserEventImpl(DiscordApi api, long userId) {
        super(api);
        this.userId = userId;
    }

    @Override
    public long getUserId() {
        return this.userId;
    }
}

