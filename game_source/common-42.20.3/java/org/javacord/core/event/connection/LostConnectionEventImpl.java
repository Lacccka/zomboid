/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.connection;

import org.javacord.api.DiscordApi;
import org.javacord.api.event.connection.LostConnectionEvent;
import org.javacord.core.event.EventImpl;

public class LostConnectionEventImpl
extends EventImpl
implements LostConnectionEvent {
    public LostConnectionEventImpl(DiscordApi api) {
        super(api);
    }
}

