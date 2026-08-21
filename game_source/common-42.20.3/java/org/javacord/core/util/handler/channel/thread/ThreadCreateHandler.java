/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.channel.thread;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.event.channel.thread.ThreadCreateEvent;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.channel.thread.ThreadCreateEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class ThreadCreateHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(ThreadCreateHandler.class);

    public ThreadCreateHandler(DiscordApi api) {
        super(api, true, "THREAD_CREATE");
    }

    @Override
    public void handle(JsonNode packet) {
        ChannelType type = ChannelType.fromId(packet.get("type").asInt());
        switch (type) {
            case SERVER_PUBLIC_THREAD: 
            case SERVER_PRIVATE_THREAD: 
            case SERVER_NEWS_THREAD: {
                this.handleThread(packet);
                break;
            }
            default: {
                logger.warn("Unknown or unexpected channel type. Your Javacord version might be out of date!");
            }
        }
    }

    private void handleThread(JsonNode channel) {
        long serverId = channel.get("guild_id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).ifPresent(server -> {
            ServerThreadChannel serverThreadChannel = ((ServerImpl)server).getOrCreateServerThreadChannel(channel);
            ThreadCreateEventImpl event = new ThreadCreateEventImpl(serverThreadChannel);
            this.api.getEventDispatcher().dispatchThreadCreateEvent((DispatchQueueSelector)((Object)server), serverThreadChannel, (ThreadCreateEvent)event);
        });
    }
}

