/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import org.javacord.api.entity.server.DefaultMessageNotificationLevel;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeDefaultMessageNotificationLevelEvent
extends ServerEvent {
    public DefaultMessageNotificationLevel getOldDefaultMessageNotificationLevel();

    public DefaultMessageNotificationLevel getNewDefaultMessageNotificationLevel();
}

