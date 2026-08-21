/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.channel.thread;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ThreadMember;
import org.javacord.api.event.channel.thread.ThreadMembersUpdateEvent;
import org.javacord.core.entity.channel.ServerThreadChannelImpl;
import org.javacord.core.entity.channel.ThreadMemberImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.channel.thread.ThreadMembersUpdateEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class ThreadMembersUpdateHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(ThreadMembersUpdateHandler.class);

    public ThreadMembersUpdateHandler(DiscordApi api) {
        super(api, true, "THREAD_MEMBERS_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        long channelId = packet.get("id").asLong();
        long serverId = packet.get("guild_id").asLong();
        ServerImpl server = this.api.getServerById(serverId).map(ServerImpl.class::cast).orElse(null);
        if (server == null) {
            logger.warn("Unable to find server with id {}", (Object)serverId);
            return;
        }
        ServerThreadChannelImpl thread2 = server.getThreadChannelById(channelId).map(ServerThreadChannelImpl.class::cast).orElse(null);
        if (thread2 == null) {
            logger.warn("Unable to find thread with id {}", (Object)channelId);
            return;
        }
        int memberCount = packet.get("member_count").asInt();
        HashSet<Long> removedMemberIds = new HashSet<Long>();
        if (packet.has("removed_member_ids")) {
            for (Object removedMemberId : packet.get("removed_member_ids")) {
                removedMemberIds.add(((JsonNode)removedMemberId).asLong());
            }
        }
        HashSet<ThreadMember> addedMembers = new HashSet<ThreadMember>();
        if (packet.has("added_members")) {
            for (JsonNode addedMember : packet.get("added_members")) {
                addedMembers.add(new ThreadMemberImpl(this.api, server, addedMember));
            }
        }
        thread2.setMembers(addedMembers);
        ThreadMembersUpdateEventImpl event = new ThreadMembersUpdateEventImpl(thread2, server, memberCount, addedMembers, removedMemberIds);
        this.api.getEventDispatcher().dispatchThreadMembersUpdateEvent((DispatchQueueSelector)server, server, thread2, (ThreadMembersUpdateEvent)event);
    }
}

