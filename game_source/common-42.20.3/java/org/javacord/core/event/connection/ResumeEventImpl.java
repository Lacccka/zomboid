/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.connection;

import org.javacord.api.DiscordApi;
import org.javacord.api.event.connection.ResumeEvent;
import org.javacord.core.event.EventImpl;

public class ResumeEventImpl
extends EventImpl
implements ResumeEvent {
    public ResumeEventImpl(DiscordApi api) {
        super(api);
    }
}

