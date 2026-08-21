/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import org.javacord.api.entity.server.VerificationLevel;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeVerificationLevelEvent
extends ServerEvent {
    public VerificationLevel getOldVerificationLevel();

    public VerificationLevel getNewVerificationLevel();
}

