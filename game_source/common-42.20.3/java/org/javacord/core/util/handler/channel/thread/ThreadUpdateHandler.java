/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.channel.thread;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ThreadMember;
import org.javacord.api.event.channel.server.ServerChannelChangeNameEvent;
import org.javacord.api.event.channel.server.thread.ServerPrivateThreadJoinEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeArchiveTimestampEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeArchivedEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeAutoArchiveDurationEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeInvitableEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeLastMessageIdEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeLockedEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeMemberCountEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeMessageCountEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeRateLimitPerUserEvent;
import org.javacord.api.event.channel.server.thread.ServerThreadChannelChangeTotalMessageSentEvent;
import org.javacord.core.entity.channel.ServerThreadChannelImpl;
import org.javacord.core.entity.channel.ThreadMemberImpl;
import org.javacord.core.entity.channel.thread.ThreadMetadataImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.event.channel.server.ServerChannelChangeNameEventImpl;
import org.javacord.core.event.channel.server.thread.ServerPrivateThreadJoinEventImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelChangeArchiveTimestampEventImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelChangeArchivedEventImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelChangeAutoArchiveDurationEventImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelChangeInvitableEventImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelChangeLastMessageIdEventImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelChangeLockedEventImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelChangeMemberCountEventImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelChangeMessageCountEventImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelChangeRateLimitPerUserEventImpl;
import org.javacord.core.event.channel.server.thread.ServerThreadChannelChangeTotalMessageSentEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class ThreadUpdateHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(ThreadUpdateHandler.class);

    public ThreadUpdateHandler(DiscordApi api) {
        super(api, true, "THREAD_UPDATE");
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

    private void handleThread(JsonNode jsonChannel) {
        Boolean isInvitable;
        Instant newArchiveTimestamp;
        Instant oldArchiveTimestamp;
        boolean isLocked;
        boolean wasLocked;
        boolean isArchived;
        boolean wasArchived;
        int newAutoArchiveDuration;
        int newRateLimitPerUser;
        int oldRateLimitPerUser;
        long newLastMessageId;
        long oldLastMessageId;
        int newNumberOfMessages;
        int oldNumberOfMessages;
        int newMemberCount;
        int oldMemberCount;
        int newMessageCount;
        int oldMessageCount;
        String newName;
        long channelId = jsonChannel.get("id").asLong();
        long serverId = jsonChannel.get("guild_id").asLong();
        ServerImpl server = this.api.getServerById(serverId).map(ServerImpl.class::cast).orElse(null);
        if (server == null) {
            logger.warn("Unable to find server with id {}", (Object)serverId);
            return;
        }
        ServerThreadChannelImpl thread2 = (ServerThreadChannelImpl)server.getOrCreateServerThreadChannel(jsonChannel);
        String oldName = thread2.getName();
        if (!Objects.deepEquals(oldName, newName = jsonChannel.get("name").asText())) {
            thread2.setName(newName);
            ServerChannelChangeNameEventImpl event = new ServerChannelChangeNameEventImpl(thread2, newName, oldName);
            this.api.getEventDispatcher().dispatchServerChannelChangeNameEvent((DispatchQueueSelector)((Object)thread2.getServer()), thread2.getServer(), thread2, (ServerChannelChangeNameEvent)event);
        }
        if ((oldMessageCount = thread2.getMessageCount()) != (newMessageCount = jsonChannel.get("message_count").asInt())) {
            thread2.setMessageCount(newMessageCount);
            ServerThreadChannelChangeMessageCountEventImpl event = new ServerThreadChannelChangeMessageCountEventImpl(thread2, newMessageCount, oldMessageCount);
            this.api.getEventDispatcher().dispatchServerThreadChannelChangeMessageCountEvent((DispatchQueueSelector)((Object)thread2.getServer()), thread2.getServer(), thread2, (ServerThreadChannelChangeMessageCountEvent)event);
        }
        if ((oldMemberCount = thread2.getMemberCount()) != (newMemberCount = jsonChannel.get("member_count").asInt())) {
            thread2.setMemberCount(newMemberCount);
            ServerThreadChannelChangeMemberCountEventImpl event = new ServerThreadChannelChangeMemberCountEventImpl(thread2, newMemberCount, oldMemberCount);
            this.api.getEventDispatcher().dispatchServerThreadChannelChangeMemberCountEvent((DispatchQueueSelector)((Object)thread2.getServer()), thread2.getServer(), thread2, (ServerThreadChannelChangeMemberCountEvent)event);
        }
        if ((oldNumberOfMessages = thread2.getTotalNumberOfMessagesSent()) != (newNumberOfMessages = jsonChannel.get("total_message_sent").asInt(0))) {
            thread2.setTotalNumberOfMessagesSent(newNumberOfMessages);
            ServerThreadChannelChangeTotalMessageSentEventImpl event = new ServerThreadChannelChangeTotalMessageSentEventImpl(thread2, newNumberOfMessages, oldNumberOfMessages);
            this.api.getEventDispatcher().dispatchServerThreadChannelChangeTotalMessageSentEvent((DispatchQueueSelector)((Object)thread2.getServer()), thread2.getServer(), thread2, (ServerThreadChannelChangeTotalMessageSentEvent)event);
        }
        if ((oldLastMessageId = thread2.getLastMessageId()) != (newLastMessageId = jsonChannel.get("last_message_id").asLong())) {
            thread2.setLastMessageId(newLastMessageId);
            ServerThreadChannelChangeLastMessageIdEventImpl event = new ServerThreadChannelChangeLastMessageIdEventImpl(thread2, newLastMessageId, oldLastMessageId);
            this.api.getEventDispatcher().dispatchServerThreadChannelChangeLastMessageIdEvent((DispatchQueueSelector)((Object)thread2.getServer()), thread2.getServer(), thread2, (ServerThreadChannelChangeLastMessageIdEvent)event);
        }
        if ((oldRateLimitPerUser = thread2.getRateLimitPerUser()) != (newRateLimitPerUser = jsonChannel.get("rate_limit_per_user").asInt())) {
            thread2.setRateLimitPerUser(newRateLimitPerUser);
            ServerThreadChannelChangeRateLimitPerUserEventImpl event = new ServerThreadChannelChangeRateLimitPerUserEventImpl(thread2, newRateLimitPerUser, oldRateLimitPerUser);
            this.api.getEventDispatcher().dispatchServerThreadChannelChangeRateLimitPerUserEvent((DispatchQueueSelector)((Object)thread2.getServer()), thread2.getServer(), thread2, (ServerThreadChannelChangeRateLimitPerUserEvent)event);
        }
        ThreadMetadataImpl metadata = (ThreadMetadataImpl)thread2.getMetadata();
        JsonNode metadataJson = jsonChannel.get("thread_metadata");
        int oldAutoArchiveDuration = metadata.getAutoArchiveDuration();
        if (oldAutoArchiveDuration != (newAutoArchiveDuration = metadataJson.get("auto_archive_duration").asInt())) {
            metadata.setAutoArchiveDuration(newAutoArchiveDuration);
            ServerThreadChannelChangeAutoArchiveDurationEventImpl event = new ServerThreadChannelChangeAutoArchiveDurationEventImpl(thread2, newAutoArchiveDuration, oldAutoArchiveDuration);
            this.api.getEventDispatcher().dispatchServerThreadChannelChangeAutoArchiveDurationEvent((DispatchQueueSelector)((Object)thread2.getServer()), thread2.getServer(), thread2, (ServerThreadChannelChangeAutoArchiveDurationEvent)event);
        }
        if ((wasArchived = metadata.isArchived()) != (isArchived = metadataJson.get("archived").asBoolean())) {
            metadata.setArchived(isArchived);
            ServerThreadChannelChangeArchivedEventImpl event = new ServerThreadChannelChangeArchivedEventImpl(thread2, isArchived, wasArchived);
            this.api.getEventDispatcher().dispatchServerThreadChannelChangeArchivedEvent((DispatchQueueSelector)((Object)thread2.getServer()), thread2.getServer(), thread2, (ServerThreadChannelChangeArchivedEvent)event);
        }
        if ((wasLocked = metadata.isLocked()) != (isLocked = metadataJson.get("locked").asBoolean())) {
            metadata.setLocked(isLocked);
            ServerThreadChannelChangeLockedEventImpl event = new ServerThreadChannelChangeLockedEventImpl(thread2, isLocked, wasLocked);
            this.api.getEventDispatcher().dispatchServerThreadChannelChangeLockedEvent((DispatchQueueSelector)((Object)thread2.getServer()), thread2.getServer(), thread2, (ServerThreadChannelChangeLockedEvent)event);
        }
        if (!Objects.deepEquals(oldArchiveTimestamp = metadata.getArchiveTimestamp(), newArchiveTimestamp = OffsetDateTime.parse(metadataJson.get("archive_timestamp").asText()).toInstant())) {
            metadata.setArchiveTimestamp(newArchiveTimestamp);
            ServerThreadChannelChangeArchiveTimestampEventImpl event = new ServerThreadChannelChangeArchiveTimestampEventImpl(thread2, newArchiveTimestamp, oldArchiveTimestamp);
            this.api.getEventDispatcher().dispatchServerThreadChannelChangeArchiveTimestampEvent((DispatchQueueSelector)((Object)thread2.getServer()), thread2.getServer(), thread2, (ServerThreadChannelChangeArchiveTimestampEvent)event);
        }
        Boolean wasInvitable = metadata.isInvitable().orElse(null);
        Boolean bl = isInvitable = metadataJson.hasNonNull("invitable") ? Boolean.valueOf(metadataJson.get("invitable").asBoolean()) : null;
        if (thread2.isPrivate() && wasInvitable != isInvitable) {
            metadata.setInvitable(isInvitable);
            ServerThreadChannelChangeInvitableEventImpl event = new ServerThreadChannelChangeInvitableEventImpl(thread2, isInvitable, wasInvitable);
            this.api.getEventDispatcher().dispatchServerThreadChannelChangeInvitableEvent((DispatchQueueSelector)((Object)thread2.getServer()), thread2.getServer(), thread2, (ServerThreadChannelChangeInvitableEvent)event);
        }
        if (jsonChannel.hasNonNull("member")) {
            HashSet<ThreadMember> members = new HashSet<ThreadMember>(thread2.getMembers());
            ThreadMemberImpl member = new ThreadMemberImpl(this.api, server, jsonChannel.get("member"));
            members.add(member);
            thread2.setMembers(members);
            ServerPrivateThreadJoinEventImpl event = new ServerPrivateThreadJoinEventImpl(thread2, member);
            this.api.getEventDispatcher().dispatchServerPrivateThreadJoinEvent((DispatchQueueSelector)((Object)thread2.getServer()), thread2.getServer(), thread2, (ServerPrivateThreadJoinEvent)event);
        }
    }
}

