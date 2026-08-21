/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import org.javacord.api.entity.server.MultiFactorAuthenticationLevel;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeMultiFactorAuthenticationLevelEvent
extends ServerEvent {
    public MultiFactorAuthenticationLevel getOldMultiFactorAuthenticationLevel();

    public MultiFactorAuthenticationLevel getNewMultiFactorAuthenticationLevel();
}

