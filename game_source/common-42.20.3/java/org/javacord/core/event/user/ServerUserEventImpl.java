/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.user;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.user.UserEvent;
import org.javacord.core.event.server.ServerEventImpl;

public abstract class ServerUserEventImpl
extends ServerEventImpl
implements UserEvent {
    private final User user;

    public ServerUserEventImpl(User user, Server server) {
        super(server);
        this.user = user;
    }

    @Override
    public User getUser() {
        return this.user;
    }
}

