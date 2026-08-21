/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Spliterators;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public class MessageSetImpl
implements MessageSet {
    private final NavigableSet<Message> messages;

    public MessageSetImpl(NavigableSet<Message> messages) {
        this.messages = Collections.unmodifiableNavigableSet(messages);
    }

    public MessageSetImpl(Collection<Message> messages) {
        this((NavigableSet<Message>)new TreeSet<Message>(messages));
    }

    public MessageSetImpl(Message ... messages) {
        this(Arrays.asList(messages));
    }

    public static CompletableFuture<MessageSet> getMessages(TextChannel channel, int limit) {
        return MessageSetImpl.getMessages(channel, limit, -1L, -1L);
    }

    private static CompletableFuture<MessageSet> getMessages(TextChannel channel, int limit, long before, long after) {
        CompletableFuture<MessageSet> future = new CompletableFuture<MessageSet>();
        channel.getApi().getThreadPool().getExecutorService().submit(() -> {
            try {
                int initialBatchSize = limit % 100 == 0 ? 100 : limit % 100;
                MessageSet initialMessages = MessageSetImpl.requestAsMessages(channel, initialBatchSize, before, after);
                if (limit <= 100 || initialMessages.isEmpty()) {
                    future.complete(initialMessages);
                    return;
                }
                int remainingMessages = limit - initialBatchSize;
                int steps = remainingMessages / 100;
                boolean older = before != -1L || after == -1L;
                boolean newer = after != -1L;
                ArrayList<MessageSet> messageSets = new ArrayList<MessageSet>();
                MessageSet lastMessages = initialMessages;
                messageSets.add(lastMessages);
                for (int step = 0; step < steps && !(lastMessages = MessageSetImpl.requestAsMessages(channel, 100, lastMessages.getOldestMessage().filter(message -> older).map(DiscordEntity::getId).orElse(-1L), lastMessages.getNewestMessage().filter(message -> newer).map(DiscordEntity::getId).orElse(-1L))).isEmpty(); ++step) {
                    messageSets.add(lastMessages);
                }
                future.complete(new MessageSetImpl(messageSets.stream().flatMap(Collection::stream).collect(Collectors.toList())));
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public static CompletableFuture<MessageSet> getMessagesUntil(TextChannel channel, Predicate<Message> condition) {
        return MessageSetImpl.getMessagesUntil(channel, condition, -1L, -1L);
    }

    private static CompletableFuture<MessageSet> getMessagesUntil(TextChannel channel, Predicate<Message> condition, long before, long after) {
        CompletableFuture<MessageSet> future = new CompletableFuture<MessageSet>();
        channel.getApi().getThreadPool().getExecutorService().submit(() -> {
            try {
                boolean[] found = new boolean[]{false};
                List<Message> messages = MessageSetImpl.getMessagesAsStream(channel, before, after).takeWhile(__ -> !found[0]).peek(message -> {
                    found[0] = condition.test((Message)message);
                }).collect(Collectors.toList());
                future.complete(new MessageSetImpl(found[0] ? messages : Collections.emptyList()));
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public static CompletableFuture<MessageSet> getMessagesWhile(TextChannel channel, Predicate<Message> condition) {
        return MessageSetImpl.getMessagesWhile(channel, condition, -1L, -1L);
    }

    private static CompletableFuture<MessageSet> getMessagesWhile(TextChannel channel, Predicate<Message> condition, long before, long after) {
        CompletableFuture<MessageSet> future = new CompletableFuture<MessageSet>();
        channel.getApi().getThreadPool().getExecutorService().submit(() -> {
            try {
                future.complete(new MessageSetImpl(MessageSetImpl.getMessagesAsStream(channel, before, after).takeWhile(condition).collect(Collectors.toList())));
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public static Stream<Message> getMessagesAsStream(TextChannel channel) {
        return MessageSetImpl.getMessagesAsStream(channel, -1L, -1L);
    }

    private static Stream<Message> getMessagesAsStream(final TextChannel channel, final long before, final long after) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<Message>(){
            private final DiscordApiImpl api;
            private final boolean older;
            private final boolean newer;
            private long referenceMessageId;
            private final List<JsonNode> messageJsons;
            {
                this.api = (DiscordApiImpl)channel.getApi();
                this.older = before != -1L || after == -1L;
                this.newer = after != -1L;
                this.referenceMessageId = this.older ? before : after;
                this.messageJsons = Collections.synchronizedList(new ArrayList());
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            private void ensureMessagesAvailable() {
                if (this.messageJsons.isEmpty()) {
                    List<JsonNode> list = this.messageJsons;
                    synchronized (list) {
                        if (this.messageJsons.isEmpty()) {
                            this.messageJsons.addAll(MessageSetImpl.requestAsSortedJsonNodes(channel, 100, this.older ? this.referenceMessageId : -1L, this.newer ? this.referenceMessageId : -1L, this.older));
                            if (!this.messageJsons.isEmpty()) {
                                this.referenceMessageId = this.messageJsons.get(this.messageJsons.size() - 1).get("id").asLong();
                            }
                        }
                    }
                }
            }

            @Override
            public boolean hasNext() {
                this.ensureMessagesAvailable();
                return !this.messageJsons.isEmpty();
            }

            @Override
            public Message next() {
                this.ensureMessagesAvailable();
                return this.api.getOrCreateMessage(channel, this.messageJsons.remove(0));
            }
        }, 4369), false);
    }

    public static CompletableFuture<MessageSet> getMessagesBefore(TextChannel channel, int limit, long before) {
        return MessageSetImpl.getMessages(channel, limit, before, -1L);
    }

    public static CompletableFuture<MessageSet> getMessagesBeforeUntil(TextChannel channel, Predicate<Message> condition, long before) {
        return MessageSetImpl.getMessagesUntil(channel, condition, before, -1L);
    }

    public static CompletableFuture<MessageSet> getMessagesBeforeWhile(TextChannel channel, Predicate<Message> condition, long before) {
        return MessageSetImpl.getMessagesWhile(channel, condition, before, -1L);
    }

    public static Stream<Message> getMessagesBeforeAsStream(TextChannel channel, long before) {
        return MessageSetImpl.getMessagesAsStream(channel, before, -1L);
    }

    public static CompletableFuture<MessageSet> getMessagesAfter(TextChannel channel, int limit, long after) {
        return MessageSetImpl.getMessages(channel, limit, -1L, after);
    }

    public static CompletableFuture<MessageSet> getMessagesAfterUntil(TextChannel channel, Predicate<Message> condition, long after) {
        return MessageSetImpl.getMessagesUntil(channel, condition, -1L, after);
    }

    public static CompletableFuture<MessageSet> getMessagesAfterWhile(TextChannel channel, Predicate<Message> condition, long after) {
        return MessageSetImpl.getMessagesWhile(channel, condition, -1L, after);
    }

    public static Stream<Message> getMessagesAfterAsStream(TextChannel channel, long after) {
        return MessageSetImpl.getMessagesAsStream(channel, -1L, after);
    }

    public static CompletableFuture<MessageSet> getMessagesAround(TextChannel channel, int limit, long around) {
        CompletableFuture<MessageSet> future = new CompletableFuture<MessageSet>();
        channel.getApi().getThreadPool().getExecutorService().submit(() -> {
            try {
                int halfLimit = limit / 2;
                MessageSet newerMessages = MessageSetImpl.getMessagesAfter(channel, halfLimit, around).join();
                MessageSet olderMessages = MessageSetImpl.getMessagesBefore(channel, halfLimit + 1, around + 1L).join();
                if (olderMessages.getNewestMessage().map(DiscordEntity::getId).map(id -> id != around).orElse(false).booleanValue()) {
                    olderMessages = olderMessages.tailSet(olderMessages.getOldestMessage().orElseThrow(AssertionError::new), false);
                }
                Collection messages = Stream.of(olderMessages, newerMessages).flatMap(Collection::stream).collect(Collectors.toList());
                future.complete(new MessageSetImpl(messages));
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public static CompletableFuture<MessageSet> getMessagesAroundUntil(TextChannel channel, Predicate<Message> condition, long around) {
        CompletableFuture<MessageSet> future = new CompletableFuture<MessageSet>();
        channel.getApi().getThreadPool().getExecutorService().submit(() -> {
            try {
                boolean[] found = new boolean[]{false};
                List<Message> messages = MessageSetImpl.getMessagesAroundAsStream(channel, around).takeWhile(__ -> !found[0]).peek(message -> {
                    found[0] = condition.test((Message)message);
                }).collect(Collectors.toList());
                future.complete(new MessageSetImpl(found[0] ? messages : Collections.emptyList()));
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public static CompletableFuture<MessageSet> getMessagesAroundWhile(TextChannel channel, Predicate<Message> condition, long around) {
        CompletableFuture<MessageSet> future = new CompletableFuture<MessageSet>();
        channel.getApi().getThreadPool().getExecutorService().submit(() -> {
            try {
                future.complete(new MessageSetImpl(MessageSetImpl.getMessagesAroundAsStream(channel, around).takeWhile(condition).collect(Collectors.toList())));
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public static Stream<Message> getMessagesAroundAsStream(final TextChannel channel, final long around) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<Message>(){
            private final DiscordApiImpl api;
            private final AtomicBoolean firstBatch;
            private final AtomicBoolean nextIsOlder;
            private long olderReferenceMessageId;
            private long newerReferenceMessageId;
            private final List<JsonNode> olderMessageJsons;
            private final List<JsonNode> newerMessageJsons;
            private final AtomicBoolean hasMoreOlderMessages;
            private final AtomicBoolean hasMoreNewerMessages;
            {
                this.api = (DiscordApiImpl)channel.getApi();
                this.firstBatch = new AtomicBoolean(true);
                this.nextIsOlder = new AtomicBoolean();
                this.olderReferenceMessageId = around;
                this.newerReferenceMessageId = around - 1L;
                this.olderMessageJsons = Collections.synchronizedList(new ArrayList());
                this.newerMessageJsons = Collections.synchronizedList(new ArrayList());
                this.hasMoreOlderMessages = new AtomicBoolean(true);
                this.hasMoreNewerMessages = new AtomicBoolean(true);
            }

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            private void ensureMessagesAvailable() {
                List<JsonNode> list;
                if (this.olderMessageJsons.isEmpty() && this.hasMoreOlderMessages.get()) {
                    list = this.olderMessageJsons;
                    synchronized (list) {
                        if (this.olderMessageJsons.isEmpty() && this.hasMoreOlderMessages.get()) {
                            this.olderMessageJsons.addAll(MessageSetImpl.requestAsSortedJsonNodes(channel, 100, this.olderReferenceMessageId, -1L, true));
                            if (this.olderMessageJsons.isEmpty()) {
                                this.hasMoreOlderMessages.set(false);
                            } else {
                                this.olderReferenceMessageId = this.olderMessageJsons.get(this.olderMessageJsons.size() - 1).get("id").asLong();
                            }
                        }
                    }
                }
                if (this.newerMessageJsons.isEmpty() && this.hasMoreNewerMessages.get()) {
                    list = this.newerMessageJsons;
                    synchronized (list) {
                        if (this.newerMessageJsons.isEmpty() && this.hasMoreNewerMessages.get()) {
                            this.newerMessageJsons.addAll(MessageSetImpl.requestAsSortedJsonNodes(channel, 100, -1L, this.newerReferenceMessageId, false));
                            if (this.newerMessageJsons.isEmpty()) {
                                this.hasMoreNewerMessages.set(false);
                            } else {
                                this.newerReferenceMessageId = this.newerMessageJsons.get(this.newerMessageJsons.size() - 1).get("id").asLong();
                                if (this.firstBatch.getAndSet(false)) {
                                    this.nextIsOlder.set(this.newerMessageJsons.get(0).get("id").asLong() != around);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public boolean hasNext() {
                this.ensureMessagesAvailable();
                return !this.olderMessageJsons.isEmpty() || !this.newerMessageJsons.isEmpty();
            }

            @Override
            public Message next() {
                this.ensureMessagesAvailable();
                boolean nextIsOlder = this.nextIsOlder.get();
                this.nextIsOlder.set(!nextIsOlder);
                JsonNode messageJson = nextIsOlder && !this.olderMessageJsons.isEmpty() || this.newerMessageJsons.isEmpty() ? this.olderMessageJsons.remove(0) : this.newerMessageJsons.remove(0);
                return this.api.getOrCreateMessage(channel, messageJson);
            }
        }, 4369), false);
    }

    public static CompletableFuture<MessageSet> getMessagesBetween(TextChannel channel, long from, long to) {
        CompletableFuture<MessageSet> future = new CompletableFuture<MessageSet>();
        channel.getApi().getThreadPool().getExecutorService().submit(() -> {
            try {
                future.complete(new MessageSetImpl(MessageSetImpl.getMessagesBetweenAsStream(channel, from, to).collect(Collectors.toList())));
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public static CompletableFuture<MessageSet> getMessagesBetweenUntil(TextChannel channel, Predicate<Message> condition, long from, long to) {
        CompletableFuture<MessageSet> future = new CompletableFuture<MessageSet>();
        channel.getApi().getThreadPool().getExecutorService().submit(() -> {
            try {
                boolean[] found = new boolean[]{false};
                List<Message> messages = MessageSetImpl.getMessagesBetweenAsStream(channel, from, to).takeWhile(__ -> !found[0]).peek(message -> {
                    found[0] = condition.test((Message)message);
                }).collect(Collectors.toList());
                future.complete(new MessageSetImpl(found[0] ? messages : Collections.emptyList()));
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public static CompletableFuture<MessageSet> getMessagesBetweenWhile(TextChannel channel, Predicate<Message> condition, long from, long to) {
        CompletableFuture<MessageSet> future = new CompletableFuture<MessageSet>();
        channel.getApi().getThreadPool().getExecutorService().submit(() -> {
            try {
                future.complete(new MessageSetImpl(MessageSetImpl.getMessagesBetweenAsStream(channel, from, to).takeWhile(condition).collect(Collectors.toList())));
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public static Stream<Message> getMessagesBetweenAsStream(TextChannel channel, long from, long to) {
        long before = Math.max(from, to);
        long after = Math.min(from, to);
        Stream<Message> messages = MessageSetImpl.getMessagesAsStream(channel, -1L, after).takeWhile(message -> message.getId() < before);
        return from == after ? messages : messages.sorted(Comparator.reverseOrder());
    }

    private static MessageSet requestAsMessages(TextChannel channel, int limit, long before, long after) {
        DiscordApiImpl api = (DiscordApiImpl)channel.getApi();
        return new MessageSetImpl(MessageSetImpl.requestAsJsonNodes(channel, limit, before, after).stream().map(jsonNode -> api.getOrCreateMessage(channel, (JsonNode)jsonNode)).collect(Collectors.toList()));
    }

    private static List<JsonNode> requestAsSortedJsonNodes(TextChannel channel, int limit, long before, long after, boolean reversed) {
        List<JsonNode> messageJsonNodes = MessageSetImpl.requestAsJsonNodes(channel, limit, before, after);
        Comparator<JsonNode> idComparator = Comparator.comparingLong(jsonNode -> jsonNode.get("id").asLong());
        messageJsonNodes.sort(reversed ? idComparator.reversed() : idComparator);
        return messageJsonNodes;
    }

    private static List<JsonNode> requestAsJsonNodes(TextChannel channel, int limit, long before, long after) {
        RestRequest<List> restRequest = new RestRequest(channel.getApi(), RestMethod.GET, RestEndpoint.MESSAGE).setUrlParameters(channel.getIdAsString());
        if (limit != -1) {
            restRequest.addQueryParameter("limit", String.valueOf(limit));
        }
        if (before != -1L) {
            restRequest.addQueryParameter("before", Long.toUnsignedString(before));
        }
        if (after != -1L) {
            restRequest.addQueryParameter("after", Long.toUnsignedString(after));
        }
        return restRequest.execute(result -> {
            ArrayList messageJsonNodes = new ArrayList();
            result.getJsonBody().iterator().forEachRemaining(messageJsonNodes::add);
            return messageJsonNodes;
        }).join();
    }

    @Override
    public Message lower(Message message) {
        return this.messages.lower(message);
    }

    @Override
    public Message floor(Message message) {
        return this.messages.floor(message);
    }

    @Override
    public Message ceiling(Message message) {
        return this.messages.ceiling(message);
    }

    @Override
    public Message higher(Message message) {
        return this.messages.higher(message);
    }

    @Override
    public Message pollFirst() {
        return this.messages.pollFirst();
    }

    @Override
    public Message pollLast() {
        return this.messages.pollLast();
    }

    @Override
    public int size() {
        return this.messages.size();
    }

    @Override
    public boolean isEmpty() {
        return this.messages.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.messages.contains(o);
    }

    @Override
    public Iterator<Message> iterator() {
        return this.messages.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.messages.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.messages.toArray(a);
    }

    @Override
    public boolean add(Message message) {
        return this.messages.add(message);
    }

    @Override
    public boolean remove(Object o) {
        return this.messages.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.messages.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Message> c) {
        return this.messages.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.messages.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.messages.removeAll(c);
    }

    @Override
    public void clear() {
        this.messages.clear();
    }

    @Override
    public NavigableSet<Message> descendingSet() {
        return this.messages.descendingSet();
    }

    @Override
    public Iterator<Message> descendingIterator() {
        return this.messages.descendingIterator();
    }

    @Override
    public MessageSet subSet(Message fromElement, boolean fromInclusive, Message toElement, boolean toInclusive) {
        return new MessageSetImpl(this.messages.subSet(fromElement, fromInclusive, toElement, toInclusive));
    }

    @Override
    public MessageSet subSet(Message fromElement, Message toElement) {
        return new MessageSetImpl(this.messages.subSet(fromElement, toElement));
    }

    @Override
    public MessageSet headSet(Message toElement, boolean inclusive) {
        return new MessageSetImpl(this.messages.headSet(toElement, inclusive));
    }

    @Override
    public MessageSet headSet(Message toElement) {
        return new MessageSetImpl(this.messages.headSet(toElement));
    }

    @Override
    public MessageSet tailSet(Message fromElement, boolean inclusive) {
        return new MessageSetImpl(this.messages.tailSet(fromElement, inclusive));
    }

    @Override
    public MessageSet tailSet(Message fromElement) {
        return new MessageSetImpl(this.messages.tailSet(fromElement));
    }

    @Override
    public Comparator<? super Message> comparator() {
        return this.messages.comparator();
    }

    @Override
    public Message first() {
        return (Message)this.messages.first();
    }

    @Override
    public Message last() {
        return (Message)this.messages.last();
    }
}

