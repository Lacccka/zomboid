/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import java.util.Optional;
import org.javacord.api.entity.VanityUrlCode;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeVanityUrlCodeEvent
extends ServerEvent {
    public Optional<VanityUrlCode> getOldVanityUrlCode();

    public Optional<VanityUrlCode> getNewVanityUrlCode();
}

