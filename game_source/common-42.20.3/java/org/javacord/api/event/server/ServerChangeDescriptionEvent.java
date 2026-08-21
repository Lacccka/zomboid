/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import java.util.Optional;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeDescriptionEvent
extends ServerEvent {
    public Optional<String> getOldDescription();

    public Optional<String> getNewDescription();
}

