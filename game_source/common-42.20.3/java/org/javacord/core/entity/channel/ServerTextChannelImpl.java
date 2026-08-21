/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.ArchivedThreads;
import org.javacord.api.util.cache.MessageCache;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.channel.InternalTextChannel;
import org.javacord.core.entity.channel.RegularServerChannelImpl;
import org.javacord.core.entity.server.ArchivedThreadsImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.listener.channel.server.text.InternalServerTextChannelAttachableListenerManager;
import org.javacord.core.util.Cleanupable;
import org.javacord.core.util.cache.MessageCacheImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class ServerTextChannelImpl
extends RegularServerChannelImpl
implements ServerTextChannel,
Cleanupable,
InternalTextChannel,
InternalServerTextChannelAttachableListenerManager {
    private final MessageCacheImpl messageCache;
    private volatile boolean nsfw;
    private volatile long parentId;
    private volatile String topic;
    private volatile int delay;
    private volatile int defaultAutoArchiveDuration;

    public ServerTextChannelImpl(DiscordApiImpl api, ServerImpl server, JsonNode data) {
        super(api, server, data);
        this.nsfw = data.has("nsfw") && data.get("nsfw").asBoolean();
        this.parentId = Long.parseLong(data.has("parent_id") ? data.get("parent_id").asText("-1") : "-1");
        this.topic = data.has("topic") && !data.get("topic").isNull() ? data.get("topic").asText() : "";
        this.delay = data.has("rate_limit_per_user") ? data.get("rate_limit_per_user").asInt(0) : 0;
        this.defaultAutoArchiveDuration = data.has("default_auto_archive_duration") ? data.get("default_auto_archive_duration").asInt() : 1440;
        this.messageCache = new MessageCacheImpl(api, api.getDefaultMessageCacheCapacity(), api.getDefaultMessageCacheStorageTimeInSeconds(), api.isDefaultAutomaticMessageCacheCleanupEnabled());
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setNsfwFlag(boolean nsfw) {
        this.nsfw = nsfw;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public void setSlowmodeDelayInSeconds(int delay) {
        this.delay = delay;
    }

    @Override
    public int getSlowmodeDelayInSeconds() {
        return this.delay;
    }

    @Override
    public CompletableFuture<ArchivedThreads> getPublicArchivedThreads(Long before, Integer limit) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        if (before != null) {
            body.put("before", before);
        }
        if (limit != null) {
            body.put("limit", limit);
        }
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.LIST_PUBLIC_ARCHIVED_THREADS).setUrlParameters(this.getIdAsString()).setBody(body).execute(result -> new ArchivedThreadsImpl((DiscordApiImpl)this.getApi(), (ServerImpl)this.getServer(), result.getJsonBody()));
    }

    @Override
    public CompletableFuture<ArchivedThreads> getPrivateArchivedThreads(Long before, Integer limit) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        if (before != null) {
            body.put("before", before);
        }
        if (limit != null) {
            body.put("limit", limit);
        }
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.LIST_PRIVATE_ARCHIVED_THREADS).setUrlParameters(this.getIdAsString()).setBody(body).execute(result -> new ArchivedThreadsImpl((DiscordApiImpl)this.getApi(), (ServerImpl)this.getServer(), result.getJsonBody()));
    }

    @Override
    public CompletableFuture<ArchivedThreads> getJoinedPrivateArchivedThreads(Long before, Integer limit) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        if (before != null) {
            body.put("before", before);
        }
        if (limit != null) {
            body.put("limit", limit);
        }
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.LIST_JOINED_PRIVATE_ARCHIVED_THREADS).setUrlParameters(this.getIdAsString()).setBody(body).execute(result -> new ArchivedThreadsImpl((DiscordApiImpl)this.getApi(), (ServerImpl)this.getServer(), result.getJsonBody()));
    }

    public void setDefaultAutoArchiveDuration(int defaultAutoArchiveDuration) {
        this.defaultAutoArchiveDuration = defaultAutoArchiveDuration;
    }

    @Override
    public int getDefaultAutoArchiveDuration() {
        return this.defaultAutoArchiveDuration;
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
    public String getTopic() {
        return this.topic;
    }

    @Override
    public MessageCache getMessageCache() {
        return this.messageCache;
    }

    @Override
    public void cleanup() {
        this.messageCache.cleanup();
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
        return String.format("ServerTextChannel (id: %s, name: %s)", this.getIdAsString(), this.getName());
    }
}

