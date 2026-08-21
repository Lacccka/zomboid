/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import org.javacord.api.entity.Region;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeRegionEvent
extends ServerEvent {
    public Region getOldRegion();

    public Region getNewRegion();
}

