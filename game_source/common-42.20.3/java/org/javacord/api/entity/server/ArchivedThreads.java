/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.server;

import java.util.List;
import java.util.Set;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ThreadMember;

public interface ArchivedThreads {
    public List<ServerThreadChannel> getServerThreadChannels();

    public Set<ThreadMember> getThreadMembers();

    public boolean hasMoreThreads();
}

