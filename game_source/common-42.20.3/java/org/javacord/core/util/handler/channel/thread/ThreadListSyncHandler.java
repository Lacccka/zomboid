/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.channel.thread;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ThreadMember;
import org.javacord.api.event.channel.thread.ThreadListSyncEvent;
import org.javacord.core.entity.channel.ThreadMemberImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.channel.thread.ThreadListSyncEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class ThreadListSyncHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(ThreadListSyncHandler.class);

    public ThreadListSyncHandler(DiscordApi api) {
        super(api, true, "THREAD_LIST_SYNC");
    }

    @Override
    public void handle(JsonNode packet) {
        long serverId = packet.get("guild_id").asLong();
        ServerImpl server = this.api.getServerById(serverId).map(ServerImpl.class::cast).orElse(null);
        if (server == null) {
            logger.warn("Unable to find server with id {}", (Object)serverId);
            return;
        }
        ArrayList<ServerThreadChannel> threads = new ArrayList<ServerThreadChannel>();
        for (Object thread2 : packet.get("threads")) {
            threads.add(server.getOrCreateServerThreadChannel((JsonNode)thread2));
        }
        ArrayList<Long> threadIds = new ArrayList<Long>();
        for (ServerThreadChannel serverThreadChannel : threads) {
            threadIds.add(serverThreadChannel.getId());
        }
        HashSet<Long> channelIds = new HashSet<Long>();
        if (packet.has("channel_ids")) {
            for (Object channelId : packet.get("channel_ids")) {
                channelIds.add(((JsonNode)channelId).asLong());
            }
            server.getThreadChannels().stream().filter(stc -> channelIds.contains(stc.getParent().getId()) && !threadIds.contains(stc.getId())).map(DiscordEntity::getId).forEach(this.api::removeChannelFromCache);
        } else {
            server.getThreadChannels().stream().map(DiscordEntity::getId).filter(id -> !threadIds.contains(id)).forEach(this.api::removeChannelFromCache);
        }
        HashSet<ThreadMember> hashSet = new HashSet<ThreadMember>();
        for (JsonNode member : packet.get("members")) {
            hashSet.add(new ThreadMemberImpl(this.api, server, member));
        }
        ThreadListSyncEventImpl event = new ThreadListSyncEventImpl(server, channelIds, threads, hashSet);
        this.api.getEventDispatcher().dispatchThreadListSyncEvent((DispatchQueueSelector)server, server, (ThreadListSyncEvent)event);
    }
}

