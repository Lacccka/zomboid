/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import org.javacord.api.entity.server.ExplicitContentFilterLevel;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeExplicitContentFilterLevelEvent
extends ServerEvent {
    public ExplicitContentFilterLevel getOldExplicitContentFilterLevel();

    public ExplicitContentFilterLevel getNewExplicitContentFilterLevel();
}

