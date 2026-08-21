/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.thread;

import java.util.List;
import java.util.Set;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ThreadMember;
import org.javacord.api.event.server.ServerEvent;

public interface ThreadListSyncEvent
extends ServerEvent {
    public Set<Long> getChannelIds();

    public List<ServerThreadChannel> getServerThreadChannels();

    public Set<ThreadMember> getMembers();
}

