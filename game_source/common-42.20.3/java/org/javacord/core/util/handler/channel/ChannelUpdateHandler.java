/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.channel;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.Categorizable;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerForumChannel;
import org.javacord.api.entity.channel.ServerStageVoiceChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.server.ServerChannelChangeNameEvent;
import org.javacord.api.event.channel.server.ServerChannelChangeNsfwFlagEvent;
import org.javacord.api.event.channel.server.ServerChannelChangeOverwrittenPermissionsEvent;
import org.javacord.api.event.channel.server.ServerChannelChangePositionEvent;
import org.javacord.api.event.channel.server.text.ServerTextChannelChangeDefaultAutoArchiveDurationEvent;
import org.javacord.api.event.channel.server.text.ServerTextChannelChangeSlowmodeEvent;
import org.javacord.api.event.channel.server.text.ServerTextChannelChangeTopicEvent;
import org.javacord.api.event.channel.server.voice.ServerStageVoiceChannelChangeTopicEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelChangeBitrateEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelChangeNsfwEvent;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelChangeUserLimitEvent;
import org.javacord.core.entity.channel.ChannelCategoryImpl;
import org.javacord.core.entity.channel.RegularServerChannelImpl;
import org.javacord.core.entity.channel.ServerChannelImpl;
import org.javacord.core.entity.channel.ServerForumChannelImpl;
import org.javacord.core.entity.channel.ServerStageVoiceChannelImpl;
import org.javacord.core.entity.channel.ServerTextChannelImpl;
import org.javacord.core.entity.channel.ServerVoiceChannelImpl;
import org.javacord.core.entity.permission.PermissionsImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.channel.server.ServerChannelChangeNameEventImpl;
import org.javacord.core.event.channel.server.ServerChannelChangeNsfwFlagEventImpl;
import org.javacord.core.event.channel.server.ServerChannelChangeOverwrittenPermissionsEventImpl;
import org.javacord.core.event.channel.server.ServerChannelChangePositionEventImpl;
import org.javacord.core.event.channel.server.text.ServerTextChannelChangeDefaultAutoArchiveDurationEventImpl;
import org.javacord.core.event.channel.server.text.ServerTextChannelChangeSlowmodeEventImpl;
import org.javacord.core.event.channel.server.text.ServerTextChannelChangeTopicEventImpl;
import org.javacord.core.event.channel.server.voice.ServerStageVoiceChannelChangeTopicEventImpl;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelChangeBitrateEventImpl;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelChangeNsfwEventImpl;
import org.javacord.core.event.channel.server.voice.ServerVoiceChannelChangeUserLimitEventImpl;
import org.javacord.core.util.cache.MessageCacheImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class ChannelUpdateHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(ChannelUpdateHandler.class);

    public ChannelUpdateHandler(DiscordApi api) {
        super(api, true, "CHANNEL_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        ChannelType type = ChannelType.fromId(packet.get("type").asInt());
        switch (type) {
            case SERVER_TEXT_CHANNEL: {
                this.handleServerChannel(packet);
                this.handleRegularServerChannel(packet);
                this.handleServerTextChannel(packet);
                break;
            }
            case SERVER_VOICE_CHANNEL: {
                this.handleServerChannel(packet);
                this.handleRegularServerChannel(packet);
                this.handleServerVoiceChannel(packet);
                break;
            }
            case SERVER_STAGE_VOICE_CHANNEL: {
                this.handleServerChannel(packet);
                this.handleRegularServerChannel(packet);
                this.handleServerVoiceChannel(packet);
                this.handleServerStageVoiceChannel(packet);
                break;
            }
            case SERVER_FORUM_CHANNEL: {
                this.handleServerChannel(packet);
                this.handleRegularServerChannel(packet);
                break;
            }
            case SERVER_NEWS_CHANNEL: {
                logger.debug("Received CHANNEL_UPDATE packet for a news channel. In this Javacord version it is treated as a normal text channel!");
                this.handleServerChannel(packet);
                this.handleRegularServerChannel(packet);
                this.handleServerTextChannel(packet);
                break;
            }
            case SERVER_STORE_CHANNEL: {
                logger.debug("Received CHANNEL_UPDATE packet for a store channel. These are not supported in this Javacord version and get ignored!");
                break;
            }
            case PRIVATE_CHANNEL: {
                this.handlePrivateChannel(packet);
                break;
            }
            case GROUP_CHANNEL: {
                logger.info("Received CHANNEL_UPDATE packet for a group channel. This should be impossible.");
                break;
            }
            case CHANNEL_CATEGORY: {
                this.handleServerChannel(packet);
                this.handleRegularServerChannel(packet);
                this.handleChannelCategory(packet);
                break;
            }
            default: {
                try {
                    this.handleServerChannel(packet);
                    if (!packet.has("position")) break;
                    this.handleRegularServerChannel(packet);
                    break;
                }
                catch (Exception exception) {
                    logger.warn("An error occurred when trying to update a fallback channel implementation", (Throwable)exception);
                }
            }
        }
    }

    private void handleServerChannel(JsonNode jsonChannel) {
        String newName;
        long channelId = jsonChannel.get("id").asLong();
        long guildId = jsonChannel.get("guild_id").asLong();
        ServerImpl server = this.api.getPossiblyUnreadyServerById(guildId).map(ServerImpl.class::cast).orElse(null);
        if (server == null) {
            return;
        }
        ServerChannelImpl channel = server.getChannelById(channelId).map(ServerChannelImpl.class::cast).orElse(null);
        if (channel == null) {
            return;
        }
        String oldName = channel.getName();
        if (!Objects.deepEquals(oldName, newName = jsonChannel.get("name").asText())) {
            channel.setName(newName);
            ServerChannelChangeNameEventImpl event = new ServerChannelChangeNameEventImpl(channel, newName, oldName);
            if (server.isReady()) {
                this.api.getEventDispatcher().dispatchServerChannelChangeNameEvent((DispatchQueueSelector)((Object)channel.getServer()), channel.getServer(), channel, (ServerChannelChangeNameEvent)event);
            }
        }
    }

    private void handleRegularServerChannel(JsonNode jsonChannel) {
        int newRawPosition;
        long channelId = jsonChannel.get("id").asLong();
        Optional<RegularServerChannel> optionalChannel = this.api.getRegularServerChannelById(channelId);
        if (!optionalChannel.isPresent()) {
            LoggerUtil.logMissingChannel(logger, channelId);
            return;
        }
        RegularServerChannelImpl channel = (RegularServerChannelImpl)optionalChannel.get();
        ServerImpl server = (ServerImpl)channel.getServer();
        AtomicBoolean areYouAffected = new AtomicBoolean(false);
        ChannelCategory oldCategory = channel.asCategorizable().flatMap(Categorizable::getCategory).orElse(null);
        ChannelCategory newCategory = jsonChannel.hasNonNull("parent_id") ? (ChannelCategory)channel.getServer().getChannelCategoryById(jsonChannel.get("parent_id").asLong(-1L)).orElse(null) : null;
        RegularServerChannelImpl regularServerChannel = channel;
        int oldRawPosition = regularServerChannel.getRawPosition();
        if (oldRawPosition != (newRawPosition = jsonChannel.get("position").asInt()) || !Objects.deepEquals(oldCategory, newCategory)) {
            int oldPosition = regularServerChannel.getPosition();
            if (regularServerChannel instanceof ServerTextChannelImpl) {
                ((ServerTextChannelImpl)regularServerChannel).setParentId(newCategory == null ? -1L : newCategory.getId());
            } else if (regularServerChannel instanceof ServerVoiceChannelImpl) {
                ((ServerVoiceChannelImpl)regularServerChannel).setParentId(newCategory == null ? -1L : newCategory.getId());
            }
            regularServerChannel.setRawPosition(newRawPosition);
            int newPosition = regularServerChannel.getPosition();
            ServerChannelChangePositionEventImpl event = new ServerChannelChangePositionEventImpl(regularServerChannel, newPosition, oldPosition, newRawPosition, oldRawPosition, newCategory, oldCategory);
            if (server.isReady()) {
                this.api.getEventDispatcher().dispatchServerChannelChangePositionEvent((DispatchQueueSelector)((Object)regularServerChannel.getServer()), regularServerChannel.getServer(), regularServerChannel, (ServerChannelChangePositionEvent)event);
            }
        }
        HashSet<Long> rolesWithOverwrittenPermissions = new HashSet<Long>();
        HashSet<Long> usersWithOverwrittenPermissions = new HashSet<Long>();
        if (jsonChannel.has("permission_overwrites") && !jsonChannel.get("permission_overwrites").isNull()) {
            block4: for (JsonNode permissionOverwriteJson : jsonChannel.get("permission_overwrites")) {
                long deny;
                long allow;
                PermissionsImpl newOverwrittenPermissions;
                ConcurrentHashMap<Long, Permissions> overwrittenPermissions;
                Permissions oldOverwrittenPermissions;
                Optional<DiscordEntity> entity;
                long entityId = permissionOverwriteJson.get("id").asLong();
                switch (permissionOverwriteJson.get("type").asInt()) {
                    case 0: {
                        Optional<Role> optionalRole = server.getRoleById(entityId);
                        if (!optionalRole.isPresent()) continue block4;
                        Role role2 = optionalRole.get();
                        entity = Optional.of(role2);
                        oldOverwrittenPermissions = regularServerChannel.getOverwrittenPermissions(role2);
                        overwrittenPermissions = regularServerChannel.getInternalOverwrittenRolePermissions();
                        rolesWithOverwrittenPermissions.add(entityId);
                        break;
                    }
                    case 1: {
                        oldOverwrittenPermissions = regularServerChannel.getOverwrittenUserPermissions().getOrDefault(entityId, PermissionsImpl.EMPTY_PERMISSIONS);
                        entity = this.api.getCachedUserById(entityId).map(DiscordEntity.class::cast);
                        overwrittenPermissions = regularServerChannel.getInternalOverwrittenUserPermissions();
                        usersWithOverwrittenPermissions.add(entityId);
                        break;
                    }
                    default: {
                        throw new IllegalStateException("Permission overwrite object with unknown type: " + permissionOverwriteJson);
                    }
                }
                if (((Object)(newOverwrittenPermissions = new PermissionsImpl(allow = permissionOverwriteJson.get("allow").asLong(0L), deny = permissionOverwriteJson.get("deny").asLong(0L)))).equals(oldOverwrittenPermissions)) continue;
                overwrittenPermissions.put(entityId, newOverwrittenPermissions);
                if (!server.isReady()) continue;
                this.dispatchServerChannelChangeOverwrittenPermissionsEvent(channel, newOverwrittenPermissions, oldOverwrittenPermissions, entityId, entity.orElse(null));
                areYouAffected.compareAndSet(false, entityId == this.api.getYourself().getId());
                entity.filter(e -> e instanceof Role).map(Role.class::cast).ifPresent(role -> areYouAffected.compareAndSet(false, role.getUsers().stream().anyMatch(User::isYourself)));
            }
        }
        ConcurrentHashMap<Long, Permissions> overwrittenRolePermissions = regularServerChannel.getInternalOverwrittenRolePermissions();
        ConcurrentHashMap<Long, Permissions> overwrittenUserPermissions = regularServerChannel.getInternalOverwrittenUserPermissions();
        Iterator<Map.Entry<Long, Permissions>> userIt = overwrittenUserPermissions.entrySet().iterator();
        while (userIt.hasNext()) {
            Map.Entry<Long, Permissions> entry = userIt.next();
            if (usersWithOverwrittenPermissions.contains(entry.getKey())) continue;
            Permissions oldPermissions = entry.getValue();
            userIt.remove();
            if (!server.isReady()) continue;
            this.dispatchServerChannelChangeOverwrittenPermissionsEvent(channel, PermissionsImpl.EMPTY_PERMISSIONS, oldPermissions, entry.getKey(), this.api.getCachedUserById(entry.getKey()).orElse(null));
            areYouAffected.compareAndSet(false, entry.getKey().longValue() == this.api.getYourself().getId());
        }
        Iterator<Map.Entry<Long, Permissions>> roleIt = overwrittenRolePermissions.entrySet().iterator();
        while (roleIt.hasNext()) {
            Map.Entry<Long, Permissions> entry = roleIt.next();
            if (rolesWithOverwrittenPermissions.contains(entry.getKey())) continue;
            this.api.getRoleById(entry.getKey()).ifPresent(role -> {
                Permissions oldPermissions = (Permissions)entry.getValue();
                roleIt.remove();
                if (server.isReady()) {
                    this.dispatchServerChannelChangeOverwrittenPermissionsEvent(channel, PermissionsImpl.EMPTY_PERMISSIONS, oldPermissions, role.getId(), (DiscordEntity)role);
                    areYouAffected.compareAndSet(false, role.getUsers().stream().anyMatch(User::isYourself));
                }
            });
        }
        if (areYouAffected.get() && !channel.canYouSee()) {
            this.api.forEachCachedMessageWhere(msg -> msg.getChannel().getId() == channelId, msg -> {
                this.api.removeMessageFromCache(msg.getId());
                ((MessageCacheImpl)((TextChannel)((Object)channel)).getMessageCache()).removeMessage((Message)msg);
            });
        }
    }

    private void handleChannelCategory(JsonNode jsonChannel) {
        long channelCategoryId = jsonChannel.get("id").asLong();
        this.api.getChannelCategoryById(channelCategoryId).map(ChannelCategoryImpl.class::cast).ifPresent(channel -> {
            boolean newNsfwFlag;
            boolean oldNsfwFlag = channel.isNsfw();
            if (oldNsfwFlag != (newNsfwFlag = jsonChannel.path("nsfw").asBoolean(false))) {
                channel.setNsfwFlag(newNsfwFlag);
                ServerChannelChangeNsfwFlagEventImpl event = new ServerChannelChangeNsfwFlagEventImpl((ServerChannel)channel, newNsfwFlag, oldNsfwFlag);
                this.api.getEventDispatcher().dispatchServerChannelChangeNsfwFlagEvent((DispatchQueueSelector)((Object)channel.getServer()), (ChannelCategory)channel, channel.getServer(), null, (ServerChannelChangeNsfwFlagEvent)event);
            }
        });
    }

    private void handleServerTextChannel(JsonNode jsonChannel) {
        int newDefaultAutoArchiveDuration;
        int newSlowmodeDelay;
        boolean newNsfwFlag;
        boolean oldNsfwFlag;
        String newTopic;
        long channelId = jsonChannel.get("id").asLong();
        Optional<ServerTextChannel> optionalChannel = this.api.getServerTextChannelById(channelId);
        if (!optionalChannel.isPresent()) {
            LoggerUtil.logMissingChannel(logger, channelId);
            return;
        }
        ServerTextChannelImpl channel = (ServerTextChannelImpl)optionalChannel.get();
        String oldTopic = channel.getTopic();
        String string = newTopic = jsonChannel.has("topic") && !jsonChannel.get("topic").isNull() ? jsonChannel.get("topic").asText() : "";
        if (!oldTopic.equals(newTopic)) {
            channel.setTopic(newTopic);
            ServerTextChannelChangeTopicEventImpl event = new ServerTextChannelChangeTopicEventImpl(channel, newTopic, oldTopic);
            this.api.getEventDispatcher().dispatchServerTextChannelChangeTopicEvent((DispatchQueueSelector)((Object)channel.getServer()), channel.getServer(), channel, (ServerTextChannelChangeTopicEvent)event);
        }
        if ((oldNsfwFlag = channel.isNsfw()) != (newNsfwFlag = jsonChannel.get("nsfw").asBoolean())) {
            channel.setNsfwFlag(newNsfwFlag);
            ServerChannelChangeNsfwFlagEventImpl event = new ServerChannelChangeNsfwFlagEventImpl(channel, newNsfwFlag, oldNsfwFlag);
            this.api.getEventDispatcher().dispatchServerChannelChangeNsfwFlagEvent((DispatchQueueSelector)((Object)channel.getServer()), null, channel.getServer(), channel, (ServerChannelChangeNsfwFlagEvent)event);
        }
        int oldSlowmodeDelay = channel.getSlowmodeDelayInSeconds();
        int n = newSlowmodeDelay = jsonChannel.has("rate_limit_per_user") ? jsonChannel.get("rate_limit_per_user").asInt(0) : 0;
        if (oldSlowmodeDelay != newSlowmodeDelay) {
            channel.setSlowmodeDelayInSeconds(newSlowmodeDelay);
            ServerTextChannelChangeSlowmodeEventImpl event = new ServerTextChannelChangeSlowmodeEventImpl(channel, oldSlowmodeDelay, newSlowmodeDelay);
            this.api.getEventDispatcher().dispatchServerTextChannelChangeSlowmodeEvent((DispatchQueueSelector)((Object)channel.getServer()), channel.getServer(), channel, (ServerTextChannelChangeSlowmodeEvent)event);
        }
        int oldDefaultAutoArchiveDuration = channel.getDefaultAutoArchiveDuration();
        int n2 = newDefaultAutoArchiveDuration = jsonChannel.has("default_auto_archive_duration") ? jsonChannel.get("default_auto_archive_duration").asInt() : 1440;
        if (oldDefaultAutoArchiveDuration != newDefaultAutoArchiveDuration) {
            channel.setDefaultAutoArchiveDuration(newDefaultAutoArchiveDuration);
            ServerTextChannelChangeDefaultAutoArchiveDurationEventImpl event = new ServerTextChannelChangeDefaultAutoArchiveDurationEventImpl(channel, oldDefaultAutoArchiveDuration, newDefaultAutoArchiveDuration);
            this.api.getEventDispatcher().dispatchServerTextChannelChangeDefaultAutoArchiveDurationEvent((DispatchQueueSelector)((Object)channel.getServer()), channel.getServer(), channel, (ServerTextChannelChangeDefaultAutoArchiveDurationEvent)event);
        }
    }

    private void handleServerForumChannel(JsonNode jsonChannel) {
        long channelId = jsonChannel.get("id").asLong();
        Optional<ServerForumChannel> optionalChannel = this.api.getServerForumChannelById(channelId);
        if (!optionalChannel.isPresent()) {
            LoggerUtil.logMissingChannel(logger, channelId);
            return;
        }
        ServerForumChannelImpl channel = (ServerForumChannelImpl)optionalChannel.get();
    }

    private void handleServerVoiceChannel(JsonNode jsonChannel) {
        boolean newNsfwFlag;
        int newUserLimit;
        int oldUserLimit;
        int newBitrate;
        long channelId = jsonChannel.get("id").asLong();
        Optional<ServerVoiceChannel> optionalChannel = this.api.getServerVoiceChannelById(channelId);
        if (!optionalChannel.isPresent()) {
            LoggerUtil.logMissingChannel(logger, channelId);
            return;
        }
        ServerVoiceChannelImpl channel = (ServerVoiceChannelImpl)optionalChannel.get();
        int oldBitrate = channel.getBitrate();
        if (oldBitrate != (newBitrate = jsonChannel.get("bitrate").asInt())) {
            channel.setBitrate(newBitrate);
            ServerVoiceChannelChangeBitrateEventImpl event = new ServerVoiceChannelChangeBitrateEventImpl(channel, newBitrate, oldBitrate);
            this.api.getEventDispatcher().dispatchServerVoiceChannelChangeBitrateEvent((DispatchQueueSelector)((Object)channel.getServer()), channel.getServer(), channel, (ServerVoiceChannelChangeBitrateEvent)event);
        }
        if ((oldUserLimit = channel.getUserLimit().orElse(0).intValue()) != (newUserLimit = jsonChannel.get("user_limit").asInt())) {
            channel.setUserLimit(newUserLimit);
            ServerVoiceChannelChangeUserLimitEventImpl event = new ServerVoiceChannelChangeUserLimitEventImpl(channel, newUserLimit, oldUserLimit);
            this.api.getEventDispatcher().dispatchServerVoiceChannelChangeUserLimitEvent((DispatchQueueSelector)((Object)channel.getServer()), channel.getServer(), channel, (ServerVoiceChannelChangeUserLimitEvent)event);
        }
        boolean oldNsfwFlag = channel.isNsfw();
        boolean bl = newNsfwFlag = jsonChannel.has("nsfw") && jsonChannel.get("nsfw").asBoolean();
        if (oldNsfwFlag != newNsfwFlag) {
            channel.setNsfw(newNsfwFlag);
            ServerVoiceChannelChangeNsfwEventImpl event = new ServerVoiceChannelChangeNsfwEventImpl(channel, newNsfwFlag, oldNsfwFlag);
            this.api.getEventDispatcher().dispatchServerVoiceChannelChangeNsfwEvent((DispatchQueueSelector)((Object)channel.getServer()), channel.getServer(), channel, (ServerVoiceChannelChangeNsfwEvent)event);
        }
    }

    private void handleServerStageVoiceChannel(JsonNode jsonChannel) {
        long channelId = jsonChannel.get("id").asLong();
        this.api.getServerStageVoiceChannelById(channelId).map(ServerStageVoiceChannelImpl.class::cast).ifPresent(channel -> {
            String newTopic;
            String oldTopic = channel.getTopic().orElse(null);
            String string = newTopic = jsonChannel.hasNonNull("topic") ? jsonChannel.get("topic").asText() : null;
            if (!Objects.equals(oldTopic, newTopic)) {
                channel.setTopic(newTopic);
                ServerStageVoiceChannelChangeTopicEventImpl event = new ServerStageVoiceChannelChangeTopicEventImpl((ServerStageVoiceChannel)channel, newTopic, oldTopic);
                this.api.getEventDispatcher().dispatchServerStageVoiceChannelChangeTopicEvent((DispatchQueueSelector)((Object)channel.getServer()), channel.getServer(), (ServerStageVoiceChannel)channel, (ServerStageVoiceChannelChangeTopicEvent)event);
            }
        });
    }

    private void handlePrivateChannel(JsonNode channel) {
    }

    private void dispatchServerChannelChangeOverwrittenPermissionsEvent(ServerChannel channel, Permissions newPermissions, Permissions oldPermissions, long entityId, DiscordEntity entity) {
        if (newPermissions.equals(oldPermissions)) {
            return;
        }
        ServerChannelChangeOverwrittenPermissionsEventImpl event = new ServerChannelChangeOverwrittenPermissionsEventImpl(channel, newPermissions, oldPermissions, entityId, entity);
        this.api.getEventDispatcher().dispatchServerChannelChangeOverwrittenPermissionsEvent((DispatchQueueSelector)((Object)channel.getServer()), entity instanceof Role ? (Role)entity : null, channel.getServer(), channel, entity instanceof User ? (User)entity : null, (ServerChannelChangeOverwrittenPermissionsEvent)event);
    }
}

