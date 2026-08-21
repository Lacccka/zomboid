/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import java.util.Optional;
import org.javacord.api.event.server.ServerChangeDescriptionEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerChangeDescriptionEventImpl
extends ServerEventImpl
implements ServerChangeDescriptionEvent {
    private final String oldDescription;
    private final String newDescription;

    public ServerChangeDescriptionEventImpl(ServerImpl server, String newDescription, String oldDescription) {
        super(server);
        this.newDescription = newDescription;
        this.oldDescription = oldDescription;
    }

    @Override
    public Optional<String> getOldDescription() {
        return Optional.ofNullable(this.oldDescription);
    }

    @Override
    public Optional<String> getNewDescription() {
        return Optional.ofNullable(this.newDescription);
    }
}

