/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import java.util.Optional;
import org.javacord.api.entity.Icon;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeSplashEvent
extends ServerEvent {
    public Optional<Icon> getOldSplash();

    public Optional<Icon> getNewSplash();
}

