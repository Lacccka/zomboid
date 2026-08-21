/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server.member;

import java.util.Collections;
import java.util.Set;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMembersChunkEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ServerMembersChunkEventImpl
extends ServerEventImpl
implements ServerMembersChunkEvent {
    private final Set<User> membersChunk;

    public ServerMembersChunkEventImpl(Server server, Set<User> membersChunk) {
        super(server);
        this.membersChunk = membersChunk;
    }

    @Override
    public Set<User> getMembers() {
        return Collections.unmodifiableSet(this.membersChunk);
    }
}

