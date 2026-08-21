/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import java.util.Optional;
import org.javacord.api.entity.VanityUrlCode;
import org.javacord.api.event.server.ServerChangeVanityUrlCodeEvent;
import org.javacord.core.entity.VanityUrlCodeImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeVanityUrlCodeEventImpl
extends ServerEventImpl
implements ServerChangeVanityUrlCodeEvent {
    private final String newVanityUrlCode;
    private final String oldVanityCode;

    public ServerChangeVanityUrlCodeEventImpl(ServerImpl server, String newVanityCode, String oldVanityCode) {
        super(server);
        this.oldVanityCode = oldVanityCode;
        this.newVanityUrlCode = newVanityCode;
    }

    @Override
    public Optional<VanityUrlCode> getOldVanityUrlCode() {
        return Optional.ofNullable(this.oldVanityCode == null ? null : new VanityUrlCodeImpl(this.oldVanityCode));
    }

    @Override
    public Optional<VanityUrlCode> getNewVanityUrlCode() {
        return Optional.ofNullable(this.newVanityUrlCode == null ? null : new VanityUrlCodeImpl(this.newVanityUrlCode));
    }
}

