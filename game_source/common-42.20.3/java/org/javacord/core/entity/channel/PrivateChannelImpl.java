/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import java.util.Optional;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.user.PrivateChannelCreateEvent;
import org.javacord.api.util.cache.MessageCache;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.channel.InternalTextChannel;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.event.channel.user.PrivateChannelCreateEventImpl;
import org.javacord.core.listener.channel.user.InternalPrivateChannelAttachableListenerManager;
import org.javacord.core.util.Cleanupable;
import org.javacord.core.util.cache.MessageCacheImpl;
import org.javacord.core.util.event.DispatchQueueSelector;

public class PrivateChannelImpl
implements PrivateChannel,
Cleanupable,
InternalTextChannel,
InternalPrivateChannelAttachableListenerManager {
    private final DiscordApiImpl api;
    private final long id;
    private UserImpl recipient;
    private Long recipientId;
    private final MessageCacheImpl messageCache;

    public PrivateChannelImpl(DiscordApiImpl api, JsonNode data) {
        this(api, data.get("id").asLong(), new UserImpl(api, data.get("recipients").get(0), (MemberImpl)null, null), (Long)data.get("recipients").get(0).get("id").asLong());
    }

    public PrivateChannelImpl(DiscordApiImpl api, String channelId, UserImpl recipient, Long recipientId) {
        this(api, Long.parseLong(channelId), recipient, recipientId);
    }

    public PrivateChannelImpl(DiscordApiImpl api, long channelId, UserImpl recipient, Long recipientId) {
        this.api = api;
        this.recipient = recipient;
        this.recipientId = recipientId;
        this.messageCache = new MessageCacheImpl(api, api.getDefaultMessageCacheCapacity(), api.getDefaultMessageCacheStorageTimeInSeconds(), api.isDefaultAutomaticMessageCacheCleanupEnabled());
        this.id = channelId;
        api.addChannelToCache(this);
    }

    private void updateRecipientId(long recipientId) {
        this.recipientId = recipientId;
    }

    private void updateRecipient(UserImpl recipient) {
        if (this.recipientId.longValue() == recipient.getId()) {
            this.recipient = recipient;
        }
    }

    public static PrivateChannelImpl getOrCreatePrivateChannel(DiscordApiImpl api, long channelId, long userId, UserImpl user) {
        Optional<PrivateChannel> optionalChannel = api.getPrivateChannelById(channelId);
        if (optionalChannel.isPresent()) {
            UserImpl recipient;
            PrivateChannelImpl channel = (PrivateChannelImpl)optionalChannel.get();
            if (!channel.getRecipientId().isPresent() && userId != api.getYourself().getId()) {
                channel.updateRecipientId(userId);
            }
            if (!channel.getRecipient().isPresent() && (recipient = user == null ? (UserImpl)api.getCachedUserById(userId).orElse(null) : user) != null && !recipient.isYourself()) {
                channel.updateRecipient(recipient);
            }
            return channel;
        }
        if (userId == api.getYourself().getId()) {
            return PrivateChannelImpl.dispatchPrivateChannelCreateEvent(api, new PrivateChannelImpl(api, channelId, null, null));
        }
        UserImpl recipient = user == null ? (UserImpl)api.getCachedUserById(userId).orElse(null) : user;
        return PrivateChannelImpl.dispatchPrivateChannelCreateEvent(api, new PrivateChannelImpl(api, channelId, recipient, (Long)userId));
    }

    public static PrivateChannelImpl dispatchPrivateChannelCreateEvent(DiscordApiImpl api, PrivateChannelImpl privateChannel) {
        PrivateChannelCreateEventImpl event = new PrivateChannelCreateEventImpl(privateChannel);
        api.getEventDispatcher().dispatchPrivateChannelCreateEvent((DispatchQueueSelector)api, (User)privateChannel.getRecipient().orElse(null), (PrivateChannelCreateEvent)event);
        return privateChannel;
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Optional<User> getRecipient() {
        return Optional.ofNullable(this.recipient);
    }

    @Override
    public Optional<Long> getRecipientId() {
        return Optional.ofNullable(this.recipientId);
    }

    @Override
    public MessageCache getMessageCache() {
        return this.messageCache;
    }

    @Override
    public void cleanup() {
        this.messageCache.cleanup();
    }

    public boolean equals(Object o) {
        return this == o || o != null && this.getClass() == o.getClass() && this.getId() == ((DiscordEntity)o).getId();
    }

    public int hashCode() {
        return Objects.hash(this.getId());
    }

    public String toString() {
        return String.format("PrivateChannel (id: %s, recipient: %s)", this.getIdAsString(), this.getRecipient());
    }
}

