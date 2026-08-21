/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import org.javacord.api.entity.server.MultiFactorAuthenticationLevel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerChangeMultiFactorAuthenticationLevelEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeMultiFactorAuthenticationLevelEventImpl
extends ServerEventImpl
implements ServerChangeMultiFactorAuthenticationLevelEvent {
    private final MultiFactorAuthenticationLevel newMultiFactorAuthenticationLevel;
    private final MultiFactorAuthenticationLevel oldMultiFactorAuthenticationLevel;

    public ServerChangeMultiFactorAuthenticationLevelEventImpl(Server server, MultiFactorAuthenticationLevel newMultiFactorAuthenticationLevel, MultiFactorAuthenticationLevel oldMultiFactorAuthenticationLevel) {
        super(server);
        this.newMultiFactorAuthenticationLevel = newMultiFactorAuthenticationLevel;
        this.oldMultiFactorAuthenticationLevel = oldMultiFactorAuthenticationLevel;
    }

    @Override
    public MultiFactorAuthenticationLevel getOldMultiFactorAuthenticationLevel() {
        return this.oldMultiFactorAuthenticationLevel;
    }

    @Override
    public MultiFactorAuthenticationLevel getNewMultiFactorAuthenticationLevel() {
        return this.newMultiFactorAuthenticationLevel;
    }
}

