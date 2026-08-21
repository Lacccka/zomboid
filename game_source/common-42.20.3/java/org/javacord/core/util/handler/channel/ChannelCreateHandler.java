/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.channel;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.api.entity.channel.ServerStageVoiceChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.UnknownRegularServerChannel;
import org.javacord.api.entity.channel.UnknownServerChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.channel.server.ServerChannelCreateEvent;
import org.javacord.api.event.channel.user.PrivateChannelCreateEvent;
import org.javacord.core.entity.channel.PrivateChannelImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.event.channel.server.ServerChannelCreateEventImpl;
import org.javacord.core.event.channel.user.PrivateChannelCreateEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class ChannelCreateHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(ChannelCreateHandler.class);

    public ChannelCreateHandler(DiscordApi api) {
        super(api, true, "CHANNEL_CREATE");
    }

    @Override
    public void handle(JsonNode packet) {
        ChannelType type = ChannelType.fromId(packet.get("type").asInt());
        switch (type) {
            case SERVER_TEXT_CHANNEL: {
                this.handleServerTextChannel(packet);
                break;
            }
            case SERVER_VOICE_CHANNEL: {
                this.handleServerVoiceChannel(packet);
                break;
            }
            case SERVER_STAGE_VOICE_CHANNEL: {
                this.handleServerStageVoiceChannel(packet);
                break;
            }
            case SERVER_FORUM_CHANNEL: {
                this.handleServerForumChannel(packet);
                break;
            }
            case SERVER_NEWS_CHANNEL: {
                logger.debug("Received CHANNEL_CREATE packet for a news channel. In this Javacord version it is treated as a normal text channel!");
                this.handleServerTextChannel(packet);
                break;
            }
            case SERVER_STORE_CHANNEL: {
                logger.debug("Received CHANNEL_CREATE packet for a store channel. These are not supported in this Javacord version and get ignored!");
                break;
            }
            case PRIVATE_CHANNEL: {
                this.handlePrivateChannel(packet);
                break;
            }
            case GROUP_CHANNEL: {
                logger.info("Received CHANNEL_CREATE packet for a group channel. This should be impossible.");
                break;
            }
            case CHANNEL_CATEGORY: {
                this.handleChannelCategory(packet);
                break;
            }
            default: {
                try {
                    if (packet.has("position")) {
                        this.showFallbackWarningMessage(packet.get("type").asInt(), "UnknownRegularServerChannel");
                        this.handleUnknownRegularServerChannel(packet);
                        break;
                    }
                    this.showFallbackWarningMessage(packet.get("type").asInt(), "UnknownServerChannel");
                    this.handleUnknownServerChannel(packet);
                    break;
                }
                catch (Exception exception) {
                    logger.warn("An error occurred when trying to use a fallback channel implementation", (Throwable)exception);
                }
            }
        }
    }

    private void showFallbackWarningMessage(int channelType, String fallbackName) {
        logger.warn("Encountered not handled channel type: {}. Trying to use the {} fallback implementation", (Object)channelType, (Object)fallbackName);
    }

    private void handleUnknownServerChannel(JsonNode channel) {
        long serverId = channel.get("guild_id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).ifPresent(server -> {
            UnknownServerChannel unknownServerChannel = ((ServerImpl)server).getOrCreateUnknownServerChannel(channel);
            ServerChannelCreateEventImpl event = new ServerChannelCreateEventImpl(unknownServerChannel);
            this.api.getEventDispatcher().dispatchServerChannelCreateEvent((DispatchQueueSelector)((Object)server), (Server)server, (ServerChannelCreateEvent)event);
        });
    }

    private void handleUnknownRegularServerChannel(JsonNode channel) {
        long serverId = channel.get("guild_id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).ifPresent(server -> {
            UnknownRegularServerChannel unknownRegularServerChannel = ((ServerImpl)server).getOrCreateUnknownRegularServerChannel(channel);
            ServerChannelCreateEventImpl event = new ServerChannelCreateEventImpl(unknownRegularServerChannel);
            this.api.getEventDispatcher().dispatchServerChannelCreateEvent((DispatchQueueSelector)((Object)server), (Server)server, (ServerChannelCreateEvent)event);
        });
    }

    private void handleServerForumChannel(JsonNode channel) {
        long serverId = channel.get("guild_id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).ifPresent(server -> {
            ServerForumChannel serverForumChannel = ((ServerImpl)server).getOrCreateServerForumChannel(channel);
            ServerChannelCreateEventImpl event = new ServerChannelCreateEventImpl(serverForumChannel);
            this.api.getEventDispatcher().dispatchServerChannelCreateEvent((DispatchQueueSelector)((Object)server), (Server)server, (ServerChannelCreateEvent)event);
        });
    }

    private void handleChannelCategory(JsonNode channel) {
        long serverId = channel.get("guild_id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).ifPresent(server -> {
            ChannelCategory channelCategory = ((ServerImpl)server).getOrCreateChannelCategory(channel);
            ServerChannelCreateEventImpl event = new ServerChannelCreateEventImpl(channelCategory);
            this.api.getEventDispatcher().dispatchServerChannelCreateEvent((DispatchQueueSelector)((Object)server), (Server)server, (ServerChannelCreateEvent)event);
        });
    }

    private void handleServerTextChannel(JsonNode channel) {
        long serverId = channel.get("guild_id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).ifPresent(server -> {
            ServerTextChannel textChannel = ((ServerImpl)server).getOrCreateServerTextChannel(channel);
            ServerChannelCreateEventImpl event = new ServerChannelCreateEventImpl(textChannel);
            this.api.getEventDispatcher().dispatchServerChannelCreateEvent((DispatchQueueSelector)((Object)server), (Server)server, (ServerChannelCreateEvent)event);
        });
    }

    private void handleServerVoiceChannel(JsonNode channel) {
        long serverId = channel.get("guild_id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).ifPresent(server -> {
            ServerVoiceChannel voiceChannel = ((ServerImpl)server).getOrCreateServerVoiceChannel(channel);
            ServerChannelCreateEventImpl event = new ServerChannelCreateEventImpl(voiceChannel);
            this.api.getEventDispatcher().dispatchServerChannelCreateEvent((DispatchQueueSelector)((Object)server), (Server)server, (ServerChannelCreateEvent)event);
        });
    }

    private void handleServerStageVoiceChannel(JsonNode channel) {
        long serverId = channel.get("guild_id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).ifPresent(server -> {
            ServerStageVoiceChannel voiceChannel = ((ServerImpl)server).getOrCreateServerStageVoiceChannel(channel);
            ServerChannelCreateEventImpl event = new ServerChannelCreateEventImpl(voiceChannel);
            this.api.getEventDispatcher().dispatchServerChannelCreateEvent((DispatchQueueSelector)((Object)server), (Server)server, (ServerChannelCreateEvent)event);
        });
    }

    private void handlePrivateChannel(JsonNode channel) {
        UserImpl recipient = new UserImpl(this.api, channel.get("recipients").get(0), (MemberImpl)null, null);
        if (!recipient.getPrivateChannel().isPresent()) {
            PrivateChannelImpl privateChannel = new PrivateChannelImpl(this.api, channel.get("id").asText(), recipient, (Long)recipient.getId());
            PrivateChannelCreateEventImpl event = new PrivateChannelCreateEventImpl(privateChannel);
            this.api.getEventDispatcher().dispatchPrivateChannelCreateEvent((DispatchQueueSelector)this.api, recipient, (PrivateChannelCreateEvent)event);
        }
    }
}

