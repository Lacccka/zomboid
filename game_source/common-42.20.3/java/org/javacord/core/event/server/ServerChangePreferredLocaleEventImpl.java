/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import java.util.Locale;
import org.javacord.api.event.server.ServerChangePreferredLocaleEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangePreferredLocaleEventImpl
extends ServerEventImpl
implements ServerChangePreferredLocaleEvent {
    private final Locale newPreferredLocale;
    private final Locale oldPreferredLocale;

    public ServerChangePreferredLocaleEventImpl(ServerImpl server, Locale newPreferredLocale, Locale oldPreferredLocale) {
        super(server);
        this.oldPreferredLocale = oldPreferredLocale;
        this.newPreferredLocale = newPreferredLocale;
    }

    @Override
    public Locale getOldPreferredLocale() {
        return this.oldPreferredLocale;
    }

    @Override
    public Locale getNewPreferredLocale() {
        return this.newPreferredLocale;
    }
}

