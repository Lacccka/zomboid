/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.util.cache.MessageCache;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.audio.AudioConnectionImpl;
import org.javacord.core.entity.channel.InternalTextChannel;
import org.javacord.core.entity.channel.RegularServerChannelImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.listener.channel.server.voice.InternalServerVoiceChannelAttachableListenerManager;
import org.javacord.core.util.Cleanupable;
import org.javacord.core.util.cache.MessageCacheImpl;

public class ServerVoiceChannelImpl
extends RegularServerChannelImpl
implements ServerVoiceChannel,
Cleanupable,
InternalTextChannel,
InternalServerVoiceChannelAttachableListenerManager {
    private final MessageCacheImpl messageCache;
    private volatile int bitrate;
    private volatile int userLimit;
    private volatile long parentId;
    private volatile boolean nsfw;
    private final Set<Long> connectedUsers = new HashSet<Long>();

    public ServerVoiceChannelImpl(DiscordApiImpl api, ServerImpl server, JsonNode data) {
        super(api, server, data);
        this.nsfw = data.has("nsfw") && data.get("nsfw").asBoolean();
        this.bitrate = data.get("bitrate").asInt();
        this.userLimit = data.get("user_limit").asInt();
        this.parentId = Long.parseLong(data.has("parent_id") ? data.get("parent_id").asText("-1") : "-1");
        this.nsfw = data.has("nsfw") && data.get("nsfw").asBoolean();
        this.messageCache = new MessageCacheImpl(api, api.getDefaultMessageCacheCapacity(), api.getDefaultMessageCacheStorageTimeInSeconds(), api.isDefaultAutomaticMessageCacheCleanupEnabled());
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public void setUserLimit(int userLimit) {
        this.userLimit = userLimit;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public void setNsfwFlag(boolean nsfw) {
        this.nsfw = nsfw;
    }

    public void addConnectedUser(long userId) {
        this.connectedUsers.add(userId);
    }

    public void removeConnectedUser(long userId) {
        this.connectedUsers.remove(userId);
    }

    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }

    @Override
    public boolean isNsfw() {
        return this.nsfw;
    }

    @Override
    public Optional<ChannelCategory> getCategory() {
        return this.getServer().getChannelCategoryById(this.parentId);
    }

    @Override
    public CompletableFuture<AudioConnection> connect(boolean muted, boolean deafened) {
        return ((CompletableFuture)this.getServer().getAudioConnection().map(AudioConnection::close).orElseGet(() -> CompletableFuture.completedFuture(null)).thenCompose(closedAudioConnection -> {
            CompletableFuture<AudioConnection> future = new CompletableFuture<AudioConnection>();
            AudioConnectionImpl connection = new AudioConnectionImpl(this, future, muted, deafened);
            ((ServerImpl)this.getServer()).setPendingAudioConnection(connection);
            return future;
        })).thenApply(conn -> {
            ((ServerImpl)this.getServer()).setAudioConnection((AudioConnectionImpl)conn);
            return conn;
        });
    }

    @Override
    public int getBitrate() {
        return this.bitrate;
    }

    @Override
    public Optional<Integer> getUserLimit() {
        return this.userLimit == 0 ? Optional.empty() : Optional.of(this.userLimit);
    }

    @Override
    public Set<Long> getConnectedUserIds() {
        return Collections.unmodifiableSet(this.connectedUsers);
    }

    @Override
    public Set<User> getConnectedUsers() {
        return Collections.unmodifiableSet(this.connectedUsers.stream().map(this.getApi()::getCachedUserById).map(optionalUser -> (User)optionalUser.orElseThrow(AssertionError::new)).collect(Collectors.toSet()));
    }

    @Override
    public boolean isConnected(long userId) {
        return this.connectedUsers.contains(userId);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    @Override
    public String toString() {
        return String.format("ServerVoiceChannel (id: %s, name: %s)", this.getIdAsString(), this.getName());
    }

    @Override
    public MessageCache getMessageCache() {
        return this.messageCache;
    }

    @Override
    public void cleanup() {
        this.messageCache.cleanup();
    }
}

