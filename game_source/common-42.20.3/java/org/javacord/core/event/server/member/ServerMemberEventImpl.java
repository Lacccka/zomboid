/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.member;

import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberEvent;
import org.javacord.core.event.server.ServerEventImpl;

public abstract class ServerMemberEventImpl
extends ServerEventImpl
implements ServerMemberEvent {
    private final User user;

    public ServerMemberEventImpl(Server server, User user) {
        super(server);
        this.user = user;
    }

    @Override
    public User getUser() {
        return this.user;
    }
}

