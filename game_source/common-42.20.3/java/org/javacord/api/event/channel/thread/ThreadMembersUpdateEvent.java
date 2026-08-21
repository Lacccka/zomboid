/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.thread;

import java.util.Set;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ThreadMember;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ServerEvent;

public interface ThreadMembersUpdateEvent
extends ServerEvent {
    public ServerThreadChannel getThread();

    @Override
    public Server getServer();

    public int getMemberCount();

    public Set<ThreadMember> getAddedMembers();

    public Set<Long> getRemovedMemberIds();
}

