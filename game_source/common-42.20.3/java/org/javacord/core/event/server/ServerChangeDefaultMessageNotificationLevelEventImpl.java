/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.server.DefaultMessageNotificationLevel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerChangeDefaultMessageNotificationLevelEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeDefaultMessageNotificationLevelEventImpl
extends ServerEventImpl
implements ServerChangeDefaultMessageNotificationLevelEvent {
    private final DefaultMessageNotificationLevel newDefaultMessageNotificationLevel;
    private final DefaultMessageNotificationLevel oldDefaultMessageNotificationLevel;

    public ServerChangeDefaultMessageNotificationLevelEventImpl(Server server, DefaultMessageNotificationLevel newDefaultMessageNotificationLevel, DefaultMessageNotificationLevel oldDefaultMessageNotificationLevel) {
        super(server);
        this.newDefaultMessageNotificationLevel = newDefaultMessageNotificationLevel;
        this.oldDefaultMessageNotificationLevel = oldDefaultMessageNotificationLevel;
    }

    @Override
    public DefaultMessageNotificationLevel getOldDefaultMessageNotificationLevel() {
        return this.oldDefaultMessageNotificationLevel;
    }

    @Override
    public DefaultMessageNotificationLevel getNewDefaultMessageNotificationLevel() {
        return this.newDefaultMessageNotificationLevel;
    }
}

