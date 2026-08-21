/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeNameEvent
extends ServerEvent {
    public String getOldName();

    public String getNewName();
}

