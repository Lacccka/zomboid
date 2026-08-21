/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.VerificationLevel;
import org.javacord.api.event.server.ServerChangeVerificationLevelEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeVerificationLevelEventImpl
extends ServerEventImpl
implements ServerChangeVerificationLevelEvent {
    private final VerificationLevel newVerificationLevel;
    private final VerificationLevel oldVerificationLevel;

    public ServerChangeVerificationLevelEventImpl(Server server, VerificationLevel newVerificationLevel, VerificationLevel oldVerificationLevel) {
        super(server);
        this.newVerificationLevel = newVerificationLevel;
        this.oldVerificationLevel = oldVerificationLevel;
    }

    @Override
    public VerificationLevel getOldVerificationLevel() {
        return this.oldVerificationLevel;
    }

    @Override
    public VerificationLevel getNewVerificationLevel() {
        return this.newVerificationLevel;
    }
}

