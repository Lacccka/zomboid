/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.channel;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.api.entity.channel.ServerStageVoiceChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.channel.UnknownRegularServerChannel;
import org.javacord.api.entity.channel.UnknownServerChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.event.channel.server.ServerChannelDeleteEvent;
import org.javacord.core.event.channel.server.ServerChannelDeleteEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class ChannelDeleteHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(ChannelDeleteHandler.class);

    public ChannelDeleteHandler(DiscordApi api) {
        super(api, true, "CHANNEL_DELETE");
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
                this.handleServerTextChannel(packet);
                break;
            }
            case SERVER_STORE_CHANNEL: {
                logger.debug("Received CHANNEL_DELETE packet for a store channel. These are not supported in this Javacord version and get ignored!");
                break;
            }
            case GROUP_CHANNEL: {
                logger.info("Received CHANNEL_DELETE packet for a group channel. This should be impossible.");
                break;
            }
            case CHANNEL_CATEGORY: {
                this.handleCategory(packet);
                break;
            }
            case PRIVATE_CHANNEL: {
                this.handlePrivateChannel(packet);
                break;
            }
            default: {
                try {
                    this.handleUnknownRegularServerChannel(packet);
                    this.handleUnknownServerChannel(packet);
                    break;
                }
                catch (Exception exception) {
                    logger.warn("An error occurred when trying to delete a fallback channel", (Throwable)exception);
                }
            }
        }
        this.api.removeChannelFromCache(packet.get("id").asLong());
    }

    private void handleUnknownServerChannel(JsonNode channelJson) {
        long serverId = channelJson.get("guild_id").asLong();
        long channelId = channelJson.get("id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).flatMap(server -> server.getUnknownChannelById(channelId)).ifPresent(this::dispatchServerChannelDeleteEvent);
        this.api.removeObjectListeners(UnknownServerChannel.class, channelId);
        this.api.removeObjectListeners(ServerChannel.class, channelId);
        this.api.removeObjectListeners(Channel.class, channelId);
    }

    private void handleUnknownRegularServerChannel(JsonNode channelJson) {
        long serverId = channelJson.get("guild_id").asLong();
        long channelId = channelJson.get("id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).flatMap(server -> server.getUnknownRegularChannelById(channelId)).ifPresent(this::dispatchServerChannelDeleteEvent);
        this.api.removeObjectListeners(UnknownRegularServerChannel.class, channelId);
        this.api.removeObjectListeners(RegularServerChannel.class, channelId);
        this.api.removeObjectListeners(ServerChannel.class, channelId);
        this.api.removeObjectListeners(Channel.class, channelId);
    }

    private void handleCategory(JsonNode channelJson) {
        long serverId = channelJson.get("guild_id").asLong();
        long channelId = channelJson.get("id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).flatMap(server -> server.getChannelCategoryById(channelId)).ifPresent(this::dispatchServerChannelDeleteEvent);
        this.api.removeObjectListeners(ChannelCategory.class, channelId);
        this.api.removeObjectListeners(ServerChannel.class, channelId);
        this.api.removeObjectListeners(Channel.class, channelId);
    }

    private void handleServerTextChannel(JsonNode channelJson) {
        long serverId = channelJson.get("guild_id").asLong();
        long channelId = channelJson.get("id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).flatMap(server -> server.getTextChannelById(channelId)).ifPresent(channel -> {
            this.dispatchServerChannelDeleteEvent((ServerChannel)channel);
            this.api.forEachCachedMessageWhere(msg -> msg.getChannel().getId() == channelId, msg -> this.api.removeMessageFromCache(msg.getId()));
        });
        this.api.removeObjectListeners(ServerTextChannel.class, channelId);
        this.api.removeObjectListeners(RegularServerChannel.class, channelId);
        this.api.removeObjectListeners(ServerChannel.class, channelId);
        this.api.removeObjectListeners(TextChannel.class, channelId);
        this.api.removeObjectListeners(Channel.class, channelId);
    }

    private void handleServerForumChannel(JsonNode channelJson) {
        long serverId = channelJson.get("guild_id").asLong();
        long channelId = channelJson.get("id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).flatMap(server -> server.getForumChannelById(channelId)).ifPresent(this::dispatchServerChannelDeleteEvent);
        this.api.removeObjectListeners(ServerForumChannel.class, channelId);
        this.api.removeObjectListeners(RegularServerChannel.class, channelId);
        this.api.removeObjectListeners(ServerChannel.class, channelId);
        this.api.removeObjectListeners(Channel.class, channelId);
    }

    private void handleServerVoiceChannel(JsonNode channelJson) {
        long serverId = channelJson.get("guild_id").asLong();
        long channelId = channelJson.get("id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).flatMap(server -> server.getVoiceChannelById(channelId)).ifPresent(this::dispatchServerChannelDeleteEvent);
        this.api.removeObjectListeners(ServerVoiceChannel.class, channelId);
        this.api.removeObjectListeners(RegularServerChannel.class, channelId);
        this.api.removeObjectListeners(ServerChannel.class, channelId);
        this.api.removeObjectListeners(VoiceChannel.class, channelId);
        this.api.removeObjectListeners(Channel.class, channelId);
    }

    private void handleServerStageVoiceChannel(JsonNode channelJson) {
        long serverId = channelJson.get("guild_id").asLong();
        long channelId = channelJson.get("id").asLong();
        this.api.getPossiblyUnreadyServerById(serverId).flatMap(server -> server.getStageVoiceChannelById(channelId)).ifPresent(this::dispatchServerChannelDeleteEvent);
        this.api.removeObjectListeners(ServerStageVoiceChannel.class, channelId);
        this.api.removeObjectListeners(ServerVoiceChannel.class, channelId);
        this.api.removeObjectListeners(RegularServerChannel.class, channelId);
        this.api.removeObjectListeners(ServerChannel.class, channelId);
        this.api.removeObjectListeners(VoiceChannel.class, channelId);
        this.api.removeObjectListeners(Channel.class, channelId);
    }

    private void handlePrivateChannel(JsonNode channel) {
    }

    private void dispatchServerChannelDeleteEvent(ServerChannel channel) {
        ServerChannelDeleteEventImpl event = new ServerChannelDeleteEventImpl(channel);
        this.api.getEventDispatcher().dispatchServerChannelDeleteEvent((DispatchQueueSelector)((Object)channel.getServer()), channel.getServer(), channel, (ServerChannelDeleteEvent)event);
    }
}

