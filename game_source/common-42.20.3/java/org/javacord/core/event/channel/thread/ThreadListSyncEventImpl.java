/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.thread;

import java.util.List;
import java.util.Set;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ThreadMember;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.thread.ThreadListSyncEvent;
import org.javacord.core.event.server.ServerEventImpl;

public class ThreadListSyncEventImpl
extends ServerEventImpl
implements ThreadListSyncEvent {
    private final Set<Long> channelIds;
    private final List<ServerThreadChannel> serverThreadChannels;
    private final Set<ThreadMember> members;

    public ThreadListSyncEventImpl(Server server, Set<Long> channelIds, List<ServerThreadChannel> serverThreadChannels, Set<ThreadMember> members) {
        super(server);
        this.channelIds = channelIds;
        this.serverThreadChannels = serverThreadChannels;
        this.members = members;
    }

    @Override
    public Set<Long> getChannelIds() {
        return this.channelIds;
    }

    @Override
    public List<ServerThreadChannel> getServerThreadChannels() {
        return this.serverThreadChannels;
    }

    @Override
    public Set<ThreadMember> getMembers() {
        return this.members;
    }
}

