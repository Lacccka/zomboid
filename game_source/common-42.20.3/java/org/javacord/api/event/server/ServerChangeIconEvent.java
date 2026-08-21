/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import java.util.Optional;
import org.javacord.api.entity.Icon;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeIconEvent
extends ServerEvent {
    public Optional<Icon> getOldIcon();

    public Optional<Icon> getNewIcon();
}

