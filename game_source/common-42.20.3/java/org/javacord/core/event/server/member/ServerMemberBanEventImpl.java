/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.member;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.server.Ban;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberBanEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerMemberBanEventImpl
extends ServerEventImpl
implements ServerMemberBanEvent {
    private final User user;

    public ServerMemberBanEventImpl(Server server, User user) {
        super(server);
        this.user = user;
    }

    @Override
    public CompletableFuture<Ban> requestBan() {
        return this.getServer().requestBan(this.getUser());
    }

    @Override
    public User getUser() {
        return this.user;
    }
}

