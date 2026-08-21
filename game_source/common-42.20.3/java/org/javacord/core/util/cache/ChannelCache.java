/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.cache;

import io.vavr.Tuple;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import java.util.Optional;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.core.util.ImmutableToJavaMapper;
import org.javacord.core.util.cache.Cache;

public class ChannelCache {
    private static final String ID_INDEX_NAME = "id";
    private static final String TYPE_INDEX_NAME = "type";
    private static final String SERVER_ID_INDEX_NAME = "server-id";
    private static final String SERVER_ID_AND_TYPE_INDEX_NAME = "server-id | type";
    private static final String PRIVATE_CHANNEL_USER_ID_INDEX_NAME = "user-id";
    private static final ChannelCache EMPTY_CACHE = new ChannelCache(Cache.empty().addIndex("id", DiscordEntity::getId).addIndex("type", Channel::getType).addIndex("server-id", channel -> channel.asServerChannel().map(ServerChannel::getServer).map(DiscordEntity::getId).orElse(null)).addIndex("server-id | type", channel -> channel.asServerChannel().map(ServerChannel::getServer).map(DiscordEntity::getId).map(serverId -> Tuple.of(serverId, channel.getType())).orElse(null)).addIndex("user-id", channel -> channel.asPrivateChannel().flatMap(PrivateChannel::getRecipient).map(DiscordEntity::getId).orElse(null)));
    private final Cache<Channel> cache;

    private ChannelCache(Cache<Channel> cache) {
        this.cache = cache;
    }

    public static ChannelCache empty() {
        return EMPTY_CACHE;
    }

    public ChannelCache addChannel(Channel channel) {
        return new ChannelCache(this.cache.addElement(channel));
    }

    public ChannelCache removeChannel(Channel channel) {
        return new ChannelCache(this.cache.removeElement(channel));
    }

    public java.util.Set<Channel> getChannels() {
        return ImmutableToJavaMapper.mapToJava(this.cache.getAll());
    }

    public <T extends Channel> java.util.Set<T> getChannelsWithTypes(ChannelType ... types) {
        Set channels = HashSet.empty();
        for (ChannelType type : types) {
            channels = channels.addAll(this.cache.findByIndex(TYPE_INDEX_NAME, (Object)type));
        }
        return ImmutableToJavaMapper.mapToJava(channels);
    }

    public java.util.Set<ServerChannel> getChannelsOfServer(long serverId) {
        return ImmutableToJavaMapper.mapToJava(this.cache.findByIndex(SERVER_ID_INDEX_NAME, serverId));
    }

    public <T extends Channel> java.util.Set<T> getChannelsOfServerAndType(long serverId, ChannelType type) {
        return ImmutableToJavaMapper.mapToJava(this.cache.findByIndex(SERVER_ID_AND_TYPE_INDEX_NAME, Tuple.of(serverId, type)));
    }

    public Optional<Channel> getChannelById(long id) {
        return this.cache.findAnyByIndex(ID_INDEX_NAME, id);
    }

    public Optional<PrivateChannel> getPrivateChannelByUserId(long userId) {
        return this.cache.findAnyByIndex(PRIVATE_CHANNEL_USER_ID_INDEX_NAME, userId).flatMap(Channel::asPrivateChannel);
    }
}

