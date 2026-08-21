/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.server;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ThreadMember;
import org.javacord.api.entity.server.ActiveThreads;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.channel.ThreadMemberImpl;
import org.javacord.core.entity.server.ServerImpl;

public class ActiveThreadsImpl
implements ActiveThreads {
    final List<ServerThreadChannel> serverThreadChannels = new ArrayList<ServerThreadChannel>();
    final Set<ThreadMember> threadMembers;

    public ActiveThreadsImpl(DiscordApiImpl api, ServerImpl server, JsonNode data) {
        for (JsonNode jsonNode : data.get("threads")) {
            this.serverThreadChannels.add(server.getOrCreateServerThreadChannel(jsonNode));
        }
        this.threadMembers = new HashSet<ThreadMember>();
        for (JsonNode jsonNode : data.get("members")) {
            this.threadMembers.add(new ThreadMemberImpl(api, server, jsonNode));
        }
    }

    @Override
    public List<ServerThreadChannel> getServerThreadChannels() {
        return Collections.unmodifiableList(this.serverThreadChannels);
    }

    @Override
    public Set<ThreadMember> getThreadMembers() {
        return Collections.unmodifiableSet(this.threadMembers);
    }
}

