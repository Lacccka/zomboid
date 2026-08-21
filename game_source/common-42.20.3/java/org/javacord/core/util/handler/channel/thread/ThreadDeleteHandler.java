/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.channel.thread;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.thread.ThreadDeleteEvent;
import org.javacord.core.event.channel.thread.ThreadDeleteEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class ThreadDeleteHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(ThreadDeleteHandler.class);

    public ThreadDeleteHandler(DiscordApi api) {
        super(api, true, "THREAD_DELETE");
    }

    @Override
    public void handle(JsonNode packet) {
        ChannelType type = ChannelType.fromId(packet.get("type").asInt());
        switch (type) {
            case SERVER_PUBLIC_THREAD: 
            case SERVER_PRIVATE_THREAD: 
            case SERVER_NEWS_THREAD: {
                this.handleServerThread(packet);
                break;
            }
            default: {
                logger.warn("Unknown or unexpected channel type. Your Javacord version might be out of date!");
            }
        }
    }

    private void handleServerThread(JsonNode packet) {
        long serverId = packet.get("guild_id").asLong();
        long channelId = packet.get("id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).flatMap(server -> server.getThreadChannelById(channelId)).ifPresent(this::dispatchThreadDeleteEvent);
        this.api.removeObjectListeners(ServerThreadChannel.class, channelId);
        this.api.removeObjectListeners(ServerChannel.class, channelId);
        this.api.removeObjectListeners(TextChannel.class, channelId);
        this.api.removeObjectListeners(Channel.class, channelId);
        this.api.removeChannelFromCache(channelId);
    }

    private void dispatchThreadDeleteEvent(ServerThreadChannel serverThreadChannel) {
        ThreadDeleteEventImpl event = new ThreadDeleteEventImpl(serverThreadChannel);
        Server server = serverThreadChannel.getServer();
        this.api.getEventDispatcher().dispatchThreadDeleteEvent((DispatchQueueSelector)((Object)server), serverThreadChannel, (ThreadDeleteEvent)event);
    }
}

