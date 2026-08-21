/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import java.util.Locale;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangePreferredLocaleEvent
extends ServerEvent {
    public Locale getOldPreferredLocale();

    public Locale getNewPreferredLocale();
}

