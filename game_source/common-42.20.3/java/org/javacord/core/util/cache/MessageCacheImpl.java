/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;
import org.javacord.api.util.cache.MessageCache;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.Cleanupable;
import org.javacord.core.util.logging.LoggerUtil;

public class MessageCacheImpl
implements MessageCache,
Cleanupable {
    private static final Logger logger = LoggerUtil.getLogger(MessageCacheImpl.class);
    private final List<Reference<? extends Message>> messages = new ArrayList<Reference<? extends Message>>();
    private final ReferenceQueue<Message> messagesCleanupQueue = new ReferenceQueue();
    private final Future<?> messagesCleanupFuture;
    private final List<Message> cacheForeverMessages = Collections.synchronizedList(new ArrayList());
    private final AtomicReference<Future<?>> cleanFuture = new AtomicReference();
    private final DiscordApiImpl api;
    private volatile int capacity;
    private volatile int storageTimeInSeconds;

    public MessageCacheImpl(DiscordApiImpl api, int capacity, int storageTimeInSeconds, boolean automaticCleanupEnabled) {
        this.api = api;
        this.capacity = capacity;
        this.storageTimeInSeconds = storageTimeInSeconds;
        this.setAutomaticCleanupEnabled(automaticCleanupEnabled);
        this.messagesCleanupFuture = api.getThreadPool().getScheduler().scheduleWithFixedDelay(() -> {
            api.getMessageCacheLock().lock();
            try {
                int removedMessages = 0;
                Reference<Message> messageRef = this.messagesCleanupQueue.poll();
                while (messageRef != null) {
                    this.messages.remove(messageRef);
                    ++removedMessages;
                    messageRef = this.messagesCleanupQueue.poll();
                }
                if (removedMessages > 0) {
                    logger.warn("Heap memory was too low to hold all configured messages in the cache. Removed {} messages from the cache due to memory shortage. Either increase your heap settings or decrease your message cache settings!", (Object)removedMessages);
                }
            }
            catch (Throwable t) {
                logger.error("Failed to clean softly referenced messages!", t);
            }
            finally {
                api.getMessageCacheLock().unlock();
            }
        }, 30L, 30L, TimeUnit.SECONDS);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addMessage(Message message) {
        this.api.getMessageCacheLock().lock();
        try {
            this.api.addMessageToCache(message);
            if (this.messages.stream().map(Reference::get).anyMatch(message::equals)) {
                return;
            }
            this.messages.removeIf(messageRef -> messageRef.get() == null);
            SoftReference<Message> messageRef2 = new SoftReference<Message>(message, this.messagesCleanupQueue);
            int pos = Collections.binarySearch(this.messages, messageRef2, Comparator.comparing(Reference::get));
            if (pos < 0) {
                pos = -pos - 1;
            }
            this.messages.add(pos, messageRef2);
        }
        finally {
            this.api.getMessageCacheLock().unlock();
        }
    }

    public void addCacheForeverMessage(Message message) {
        this.cacheForeverMessages.add(message);
    }

    public void removeCacheForeverMessage(Message message) {
        this.cacheForeverMessages.remove(message);
    }

    public void removeMessage(Message message) {
        this.api.getMessageCacheLock().lock();
        try {
            this.messages.removeIf(messageRef -> Objects.equals(messageRef.get(), message));
        }
        finally {
            this.api.getMessageCacheLock().unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void clean() {
        Instant minAge = Instant.now().minus(this.storageTimeInSeconds, ChronoUnit.SECONDS);
        this.api.getMessageCacheLock().lock();
        try {
            this.messages.removeIf(messageRef -> Optional.ofNullable((Message)messageRef.get()).map(message -> !message.isCachedForever() && message.getCreationTimestamp().isBefore(minAge)).orElse(true));
            long foreverCachedAmount = this.messages.stream().map(Reference::get).filter(Objects::nonNull).filter(Message::isCachedForever).count();
            this.messages.removeAll(this.messages.stream().filter(messageRef -> Optional.ofNullable((Message)messageRef.get()).map(message -> !message.isCachedForever()).orElse(true)).limit(Math.max(0L, (long)(this.messages.size() - this.capacity) - foreverCachedAmount)).collect(Collectors.toList()));
        }
        finally {
            this.api.getMessageCacheLock().unlock();
        }
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = Math.max(capacity, 0);
    }

    @Override
    public int getStorageTimeInSeconds() {
        return this.storageTimeInSeconds;
    }

    @Override
    public void setStorageTimeInSeconds(int storageTimeInSeconds) {
        this.storageTimeInSeconds = Math.max(storageTimeInSeconds, 0);
    }

    @Override
    public void setAutomaticCleanupEnabled(boolean automaticCleanupEnabled) {
        if (automaticCleanupEnabled) {
            this.cleanFuture.updateAndGet(future -> {
                if (future != null) {
                    return future;
                }
                return this.api.getThreadPool().getScheduler().scheduleWithFixedDelay(() -> {
                    try {
                        this.clean();
                    }
                    catch (Throwable t) {
                        logger.error("Failed to clean message cache!", t);
                    }
                }, 1L, 1L, TimeUnit.MINUTES);
            });
        } else {
            this.cleanFuture.updateAndGet(future -> {
                if (future != null) {
                    future.cancel(false);
                }
                return null;
            });
        }
    }

    @Override
    public void cleanup() {
        this.setAutomaticCleanupEnabled(false);
        this.messagesCleanupFuture.cancel(false);
    }
}

