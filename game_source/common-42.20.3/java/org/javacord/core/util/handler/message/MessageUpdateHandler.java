/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.message;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.CachedMessagePinEvent;
import org.javacord.api.event.message.CachedMessageUnpinEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.core.entity.message.MessageImpl;
import org.javacord.core.event.message.CachedMessagePinEventImpl;
import org.javacord.core.event.message.CachedMessageUnpinEventImpl;
import org.javacord.core.event.message.MessageEditEventImpl;
import org.javacord.core.util.event.DispatchQueueSelector;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class MessageUpdateHandler
extends PacketHandler {
    private static final int MAX_EDIT_TIMEOUT = 30000;
    private static final Logger logger = LoggerUtil.getLogger(MessageUpdateHandler.class);
    private final ConcurrentHashMap<Long, Long> lastKnownEditTimestamps = new ConcurrentHashMap();

    public MessageUpdateHandler(DiscordApi api) {
        super(api, true, "MESSAGE_UPDATE");
        long offset = this.api.getTimeOffset() == null ? 0L : this.api.getTimeOffset();
        api.getThreadPool().getScheduler().scheduleAtFixedRate(() -> {
            try {
                this.lastKnownEditTimestamps.entrySet().removeIf(entry -> System.currentTimeMillis() + offset - (Long)entry.getValue() > 30000L);
            }
            catch (Throwable t) {
                logger.error("Failed to clean last known edit timestamps cache!", t);
            }
        }, 1L, 1L, TimeUnit.MINUTES);
    }

    @Override
    public void handle(JsonNode packet) {
        boolean oldPinnedFlag;
        boolean newPinnedFlag;
        boolean isMostLikelyAnEdit;
        long messageId = packet.get("id").asLong();
        long channelId = packet.get("channel_id").asLong();
        Optional<TextChannel> optionalChannel = this.api.getTextChannelById(channelId);
        if (!optionalChannel.isPresent()) {
            LoggerUtil.logMissingChannel(logger, channelId);
            return;
        }
        TextChannel channel = optionalChannel.get();
        Optional<MessageImpl> cachedMessage = this.api.getCachedMessageById(messageId).map(msg -> (MessageImpl)msg);
        if (!cachedMessage.isPresent() && !MessageUpdateHandler.isCompleteMessagePacket(packet)) {
            logger.debug("Received a message update event for an uncached message that contains not enough data to construct a new message in channel {}. Packet: {}", (Object)channelId, (Object)packet);
            return;
        }
        MessageImpl oldMessage = cachedMessage.map(MessageImpl::copyMessage).orElse(null);
        MessageImpl newMessage = cachedMessage.orElseGet(() -> (MessageImpl)this.api.getOrCreateMessage(channel, packet));
        boolean bl = isMostLikelyAnEdit = packet.has("edited_timestamp") && !packet.get("edited_timestamp").isNull();
        if (isMostLikelyAnEdit) {
            long offset;
            long editTimestamp = OffsetDateTime.parse(packet.get("edited_timestamp").asText()).toInstant().toEpochMilli();
            long lastKnownEditTimestamp = this.lastKnownEditTimestamps.getOrDefault(messageId, 0L);
            this.lastKnownEditTimestamps.put(messageId, editTimestamp);
            long l = offset = this.api.getTimeOffset() == null ? 0L : this.api.getTimeOffset();
            if (editTimestamp == lastKnownEditTimestamp) {
                isMostLikelyAnEdit = false;
            } else if (System.currentTimeMillis() + offset - editTimestamp > 30000L) {
                isMostLikelyAnEdit = false;
            }
        }
        newMessage.setUpdatableFields(packet);
        if (cachedMessage.isPresent() && packet.hasNonNull("pinned") && (newPinnedFlag = newMessage.isPinned()) != (oldPinnedFlag = oldMessage.isPinned())) {
            if (newPinnedFlag) {
                CachedMessagePinEventImpl event = new CachedMessagePinEventImpl(newMessage);
                Optional<Server> optionalServer = newMessage.getChannel().asServerChannel().map(ServerChannel::getServer);
                this.api.getEventDispatcher().dispatchCachedMessagePinEvent(optionalServer.map(DispatchQueueSelector.class::cast).orElse(this.api), newMessage, optionalServer.orElse(null), newMessage.getChannel(), (CachedMessagePinEvent)event);
            } else {
                CachedMessageUnpinEventImpl event = new CachedMessageUnpinEventImpl(newMessage);
                Optional<Server> optionalServer = newMessage.getChannel().asServerChannel().map(ServerChannel::getServer);
                this.api.getEventDispatcher().dispatchCachedMessageUnpinEvent(optionalServer.map(DispatchQueueSelector.class::cast).orElse(this.api), newMessage, optionalServer.orElse(null), newMessage.getChannel(), (CachedMessageUnpinEvent)event);
            }
        }
        MessageEditEventImpl editEvent = new MessageEditEventImpl(this.api, messageId, channel, newMessage, oldMessage, isMostLikelyAnEdit);
        this.dispatchEditEvent(editEvent);
    }

    private void dispatchEditEvent(MessageEditEvent event) {
        Optional<Server> optionalServer = event.getChannel().asServerChannel().map(ServerChannel::getServer);
        this.api.getEventDispatcher().dispatchMessageEditEvent(optionalServer.map(DispatchQueueSelector.class::cast).orElse(this.api), event.getMessageId(), optionalServer.orElse(null), event.getChannel(), event);
    }

    private static boolean isCompleteMessagePacket(JsonNode packet) {
        return packet.hasNonNull("type") && packet.hasNonNull("author") && packet.hasNonNull("content") && packet.hasNonNull("timestamp") && packet.hasNonNull("embeds");
    }
}

