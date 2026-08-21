/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import org.javacord.api.entity.server.NsfwLevel;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeNsfwLevelEvent
extends ServerEvent {
    public NsfwLevel getOldNsfwLevel();

    public NsfwLevel getNewNsfwLevel();
}

