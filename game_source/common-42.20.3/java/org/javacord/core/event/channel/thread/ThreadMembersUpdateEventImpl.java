/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.thread;

import java.util.Set;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ThreadMember;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.thread.ThreadMembersUpdateEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ThreadMembersUpdateEventImpl
extends ServerEventImpl
implements ThreadMembersUpdateEvent {
    private final ServerThreadChannel serverThreadChannel;
    private final int memberCount;
    private final Set<ThreadMember> addedMembers;
    private final Set<Long> removedMemberIds;

    public ThreadMembersUpdateEventImpl(ServerThreadChannel serverThreadChannel, Server server, int memberCount, Set<ThreadMember> addedMembers, Set<Long> removedMemberIds) {
        super(server);
        this.serverThreadChannel = serverThreadChannel;
        this.memberCount = memberCount;
        this.addedMembers = addedMembers;
        this.removedMemberIds = removedMemberIds;
    }

    @Override
    public ServerThreadChannel getThread() {
        return this.serverThreadChannel;
    }

    @Override
    public int getMemberCount() {
        return this.memberCount;
    }

    @Override
    public Set<ThreadMember> getAddedMembers() {
        return this.addedMembers;
    }

    @Override
    public Set<Long> getRemovedMemberIds() {
        return this.removedMemberIds;
    }
}

