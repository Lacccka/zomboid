/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event;

import org.javacord.api.DiscordApi;
import org.javacord.api.event.Event;

public abstract class EventImpl
implements Event {
    protected final DiscordApi api;

    public EventImpl(DiscordApi api) {
        this.api = api;
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }
}

