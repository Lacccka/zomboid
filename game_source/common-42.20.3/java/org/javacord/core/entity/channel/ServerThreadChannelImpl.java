/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerThreadChannel;
import org.javacord.api.entity.channel.ThreadMember;
import org.javacord.api.entity.channel.thread.ThreadMetadata;
import org.javacord.api.util.cache.MessageCache;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.channel.InternalTextChannel;
import org.javacord.core.entity.channel.ServerChannelImpl;
import org.javacord.core.entity.channel.ThreadMemberImpl;
import org.javacord.core.entity.channel.thread.ThreadMetadataImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.listener.channel.server.text.InternalServerTextChannelAttachableListenerManager;
import org.javacord.core.util.Cleanupable;
import org.javacord.core.util.cache.MessageCacheImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class ServerThreadChannelImpl
extends ServerChannelImpl
implements ServerThreadChannel,
Cleanupable,
InternalTextChannel,
InternalServerTextChannelAttachableListenerManager {
    private final MessageCacheImpl messageCache;
    private final long parentId;
    private int messageCount;
    private int memberCount;
    private long lastMessageId;
    private int rateLimitPerUser;
    private final long ownerId;
    private final Set<ThreadMember> members;
    private final ThreadMetadata metadata;
    private int totalNumberOfMessagesSent;

    public ServerThreadChannelImpl(DiscordApiImpl api, ServerImpl server, JsonNode data) {
        super(api, server, data);
        this.parentId = data.get("parent_id").asLong();
        this.ownerId = data.get("owner_id").asLong();
        this.messageCount = data.get("message_count").asInt(0);
        this.memberCount = data.get("member_count").asInt(0);
        this.lastMessageId = data.hasNonNull("last_message_id") ? data.get("last_message_id").asLong() : 0L;
        this.rateLimitPerUser = data.get("rate_limit_per_user").asInt(0);
        this.members = new HashSet<ThreadMember>();
        if (data.hasNonNull("member")) {
            if (data.get("member").hasNonNull("user_id")) {
                this.members.add(new ThreadMemberImpl(api, server, data.get("member")));
            } else {
                this.members.add(new ThreadMemberImpl(api, server, data.get("member"), this.getId(), api.getYourself().getId()));
            }
        }
        this.metadata = new ThreadMetadataImpl(data.get("thread_metadata"));
        this.messageCache = new MessageCacheImpl(api, api.getDefaultMessageCacheCapacity(), api.getDefaultMessageCacheStorageTimeInSeconds(), api.isDefaultAutomaticMessageCacheCleanupEnabled());
        this.totalNumberOfMessagesSent = data.path("total_message_sent").asInt(0);
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public void setLastMessageId(long lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public void setRateLimitPerUser(int rateLimitPerUser) {
        this.rateLimitPerUser = rateLimitPerUser;
    }

    public void setTotalNumberOfMessagesSent(int totalNumberOfMessagesSent) {
        this.totalNumberOfMessagesSent = totalNumberOfMessagesSent;
    }

    @Override
    public RegularServerChannel getParent() {
        return this.getServer().getRegularChannelById(this.parentId).orElseThrow(() -> new AssertionError((Object)"Thread has no parent channel."));
    }

    @Override
    public int getMessageCount() {
        return this.messageCount;
    }

    @Override
    public int getMemberCount() {
        return this.memberCount;
    }

    @Override
    public long getLastMessageId() {
        return this.lastMessageId;
    }

    @Override
    public ThreadMetadata getMetadata() {
        return this.metadata;
    }

    @Override
    public long getOwnerId() {
        return this.ownerId;
    }

    @Override
    public Set<ThreadMember> getMembers() {
        return Collections.unmodifiableSet(this.members);
    }

    @Override
    public CompletableFuture<Void> addThreadMember(long userId) {
        return new RestRequest(this.getApi(), RestMethod.PUT, RestEndpoint.ADD_REMOVE_THREAD_MEMBER).setUrlParameters(this.getIdAsString(), String.valueOf(userId)).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> removeThreadMember(long userId) {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.ADD_REMOVE_THREAD_MEMBER).setUrlParameters(this.getIdAsString(), String.valueOf(userId)).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> leaveThread() {
        return new RestRequest(this.getApi(), RestMethod.DELETE, RestEndpoint.ADD_REMOVE_THREAD_MEMBER).setUrlParameters(this.getIdAsString()).execute(result -> null);
    }

    @Override
    public CompletableFuture<ThreadMember> requestThreadMemberById(long userId) {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.THREAD_MEMBER).setUrlParameters(this.getIdAsString(), String.valueOf(userId)).execute(result -> new ThreadMemberImpl(this.getApi(), this.getServer(), result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Set<ThreadMember>> requestThreadMembers() {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.LIST_THREAD_MEMBERS).setUrlParameters(this.getIdAsString()).execute(result -> {
            HashSet<ThreadMemberImpl> threadMembers = new HashSet<ThreadMemberImpl>();
            JsonNode jsonNode = result.getJsonBody();
            for (JsonNode node : jsonNode) {
                threadMembers.add(new ThreadMemberImpl(this.getApi(), this.getServer(), node));
            }
            return Collections.unmodifiableSet(threadMembers);
        });
    }

    @Override
    public int getTotalNumberOfMessagesSent() {
        return this.totalNumberOfMessagesSent;
    }

    @Override
    public int getRateLimitPerUser() {
        return this.rateLimitPerUser;
    }

    public void setMembers(Set<ThreadMember> members) {
        this.members.clear();
        this.members.addAll(members);
    }

    @Override
    public MessageCache getMessageCache() {
        return this.messageCache;
    }

    @Override
    public String getMentionTag() {
        return "<#" + this.getIdAsString() + ">";
    }

    @Override
    public void cleanup() {
        this.messageCache.cleanup();
        ((DiscordApiImpl)this.getApi()).forEachCachedMessageWhere(msg -> msg.getChannel().getId() == this.getId(), msg -> ((DiscordApiImpl)this.getApi()).removeMessageFromCache(msg.getId()));
    }
}

