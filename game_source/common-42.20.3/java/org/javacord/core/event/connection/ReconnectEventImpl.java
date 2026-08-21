/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.connection;

import org.javacord.api.DiscordApi;
import org.javacord.api.event.connection.ReconnectEvent;
import org.javacord.core.event.EventImpl;

public class ReconnectEventImpl
extends EventImpl
implements ReconnectEvent {
    public ReconnectEventImpl(DiscordApi api) {
        super(api);
    }
}

