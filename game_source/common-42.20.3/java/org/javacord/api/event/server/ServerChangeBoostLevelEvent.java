/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import org.javacord.api.entity.server.BoostLevel;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeBoostLevelEvent
extends ServerEvent {
    public BoostLevel getOldBoostLevel();

    public BoostLevel getNewBoostLevel();
}

