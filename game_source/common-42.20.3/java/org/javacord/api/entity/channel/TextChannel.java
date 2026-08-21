/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.api.listener.channel.TextChannelAttachableListenerManager;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.NonThrowingAutoCloseable;
import org.javacord.api.util.cache.MessageCache;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.api.util.logging.ExceptionLogger;

public interface TextChannel
extends Channel,
Messageable,
TextChannelAttachableListenerManager {
    public CompletableFuture<Void> type();

    default public NonThrowingAutoCloseable typeContinuously() {
        return this.typeContinuouslyAfter(0L, TimeUnit.NANOSECONDS, null);
    }

    default public NonThrowingAutoCloseable typeContinuously(Consumer<Throwable> exceptionHandler) {
        return this.typeContinuouslyAfter(0L, TimeUnit.NANOSECONDS, exceptionHandler);
    }

    default public NonThrowingAutoCloseable typeContinuouslyAfter(long delay, TimeUnit timeUnit) {
        return this.typeContinuouslyAfter(delay, timeUnit, null);
    }

    default public NonThrowingAutoCloseable typeContinuouslyAfter(long delay, TimeUnit timeUnit, Consumer<Throwable> exceptionHandler) {
        Runnable typeRunnable = () -> {
            try {
                CompletableFuture<Void> typeFuture = this.type();
                if (exceptionHandler != null) {
                    typeFuture.exceptionally(throwable -> {
                        exceptionHandler.accept((Throwable)throwable);
                        return null;
                    });
                }
            }
            catch (Throwable t) {
                ExceptionLogger.getConsumer(new Class[0]).accept(t);
            }
        };
        DiscordApi api = this.getApi();
        ScheduledFuture<?> typingIndicator = api.getThreadPool().getScheduler().scheduleWithFixedDelay(typeRunnable, TimeUnit.NANOSECONDS.convert(delay, timeUnit), 8000000000L, TimeUnit.NANOSECONDS);
        ListenerManager<MessageCreateListener> typingInterruptedListenerManager = api.addMessageCreateListener(event -> {
            if (event.getMessage().getAuthor().isYourself()) {
                typeRunnable.run();
            }
        });
        return () -> {
            typingInterruptedListenerManager.remove();
            typingIndicator.cancel(true);
        };
    }

    default public CompletableFuture<Void> bulkDelete(Iterable<Message> messages) {
        return this.bulkDelete(StreamSupport.stream(messages.spliterator(), false).mapToLong(DiscordEntity::getId).toArray());
    }

    public CompletableFuture<Void> bulkDelete(long ... var1);

    default public CompletableFuture<Void> bulkDelete(String ... messageIds) {
        long[] messageLongIds = Arrays.stream(messageIds).filter(s -> {
            try {
                Long.parseLong(s);
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }).mapToLong(Long::parseLong).toArray();
        return this.bulkDelete(messageLongIds);
    }

    default public CompletableFuture<Void> bulkDelete(Message ... messages) {
        return this.bulkDelete(Arrays.stream(messages).mapToLong(DiscordEntity::getId).toArray());
    }

    default public CompletableFuture<Void> deleteMessages(Iterable<Message> messages) {
        return this.deleteMessages(StreamSupport.stream(messages.spliterator(), false).mapToLong(DiscordEntity::getId).toArray());
    }

    default public CompletableFuture<Void> deleteMessages(long ... messageIds) {
        return Message.delete(this.getApi(), this.getId(), messageIds);
    }

    default public CompletableFuture<Void> deleteMessages(String ... messageIds) {
        long[] messageLongIds = Arrays.stream(messageIds).filter(s -> {
            try {
                Long.parseLong(s);
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }).mapToLong(Long::parseLong).toArray();
        return this.deleteMessages(messageLongIds);
    }

    default public CompletableFuture<Void> deleteMessages(Message ... messages) {
        return this.deleteMessages(Arrays.stream(messages).mapToLong(DiscordEntity::getId).toArray());
    }

    public CompletableFuture<Message> getMessageById(long var1);

    default public CompletableFuture<Message> getMessageById(String id) {
        try {
            return this.getMessageById(Long.parseLong(id));
        }
        catch (NumberFormatException exception) {
            CompletableFuture<Message> future = new CompletableFuture<Message>();
            future.completeExceptionally(exception);
            return future;
        }
    }

    public CompletableFuture<MessageSet> getPins();

    public CompletableFuture<MessageSet> getMessages(int var1);

    public CompletableFuture<MessageSet> getMessagesUntil(Predicate<Message> var1);

    public CompletableFuture<MessageSet> getMessagesWhile(Predicate<Message> var1);

    public Stream<Message> getMessagesAsStream();

    public CompletableFuture<MessageSet> getMessagesBefore(int var1, long var2);

    default public CompletableFuture<MessageSet> getMessagesBefore(int limit, Message before) {
        return this.getMessagesBefore(limit, before.getId());
    }

    public CompletableFuture<MessageSet> getMessagesBeforeUntil(Predicate<Message> var1, long var2);

    default public CompletableFuture<MessageSet> getMessagesBeforeUntil(Predicate<Message> condition, Message before) {
        return this.getMessagesBeforeUntil(condition, before.getId());
    }

    public CompletableFuture<MessageSet> getMessagesBeforeWhile(Predicate<Message> var1, long var2);

    default public CompletableFuture<MessageSet> getMessagesBeforeWhile(Predicate<Message> condition, Message before) {
        return this.getMessagesBeforeWhile(condition, before.getId());
    }

    public Stream<Message> getMessagesBeforeAsStream(long var1);

    default public Stream<Message> getMessagesBeforeAsStream(Message before) {
        return this.getMessagesBeforeAsStream(before.getId());
    }

    public CompletableFuture<MessageSet> getMessagesAfter(int var1, long var2);

    default public CompletableFuture<MessageSet> getMessagesAfter(int limit, Message after) {
        return this.getMessagesAfter(limit, after.getId());
    }

    public CompletableFuture<MessageSet> getMessagesAfterUntil(Predicate<Message> var1, long var2);

    default public CompletableFuture<MessageSet> getMessagesAfterUntil(Predicate<Message> condition, Message after) {
        return this.getMessagesAfterUntil(condition, after.getId());
    }

    public CompletableFuture<MessageSet> getMessagesAfterWhile(Predicate<Message> var1, long var2);

    default public CompletableFuture<MessageSet> getMessagesAfterWhile(Predicate<Message> condition, Message after) {
        return this.getMessagesAfterWhile(condition, after.getId());
    }

    public Stream<Message> getMessagesAfterAsStream(long var1);

    default public Stream<Message> getMessagesAfterAsStream(Message after) {
        return this.getMessagesAfterAsStream(after.getId());
    }

    public CompletableFuture<MessageSet> getMessagesAround(int var1, long var2);

    default public CompletableFuture<MessageSet> getMessagesAround(int limit, Message around) {
        return this.getMessagesAround(limit, around.getId());
    }

    public CompletableFuture<MessageSet> getMessagesAroundUntil(Predicate<Message> var1, long var2);

    default public CompletableFuture<MessageSet> getMessagesAroundUntil(Predicate<Message> condition, Message around) {
        return this.getMessagesAroundUntil(condition, around.getId());
    }

    public CompletableFuture<MessageSet> getMessagesAroundWhile(Predicate<Message> var1, long var2);

    default public CompletableFuture<MessageSet> getMessagesAroundWhile(Predicate<Message> condition, Message around) {
        return this.getMessagesAroundWhile(condition, around.getId());
    }

    public Stream<Message> getMessagesAroundAsStream(long var1);

    default public Stream<Message> getMessagesAroundAsStream(Message around) {
        return this.getMessagesAroundAsStream(around.getId());
    }

    public CompletableFuture<MessageSet> getMessagesBetween(long var1, long var3);

    default public CompletableFuture<MessageSet> getMessagesBetween(Message from, Message to) {
        return this.getMessagesBetween(from.getId(), to.getId());
    }

    public CompletableFuture<MessageSet> getMessagesBetweenUntil(Predicate<Message> var1, long var2, long var4);

    default public CompletableFuture<MessageSet> getMessagesBetweenUntil(Predicate<Message> condition, Message from, Message to) {
        return this.getMessagesBetweenUntil(condition, from.getId(), to.getId());
    }

    public CompletableFuture<MessageSet> getMessagesBetweenWhile(Predicate<Message> var1, long var2, long var4);

    default public CompletableFuture<MessageSet> getMessagesBetweenWhile(Predicate<Message> condition, Message from, Message to) {
        return this.getMessagesBetweenWhile(condition, from.getId(), to.getId());
    }

    public Stream<Message> getMessagesBetweenAsStream(long var1, long var3);

    default public Stream<Message> getMessagesBetweenAsStream(Message from, Message to) {
        return this.getMessagesBetweenAsStream(from.getId(), to.getId());
    }

    public MessageCache getMessageCache();

    public CompletableFuture<List<Webhook>> getWebhooks();

    public CompletableFuture<List<Webhook>> getAllIncomingWebhooks();

    public CompletableFuture<List<IncomingWebhook>> getIncomingWebhooks();

    public boolean canWrite(User var1);

    default public boolean canYouWrite() {
        return this.canWrite(this.getApi().getYourself());
    }

    public boolean canUseExternalEmojis(User var1);

    default public boolean canYouUseExternalEmojis() {
        return this.canUseExternalEmojis(this.getApi().getYourself());
    }

    public boolean canEmbedLinks(User var1);

    default public boolean canYouEmbedLinks() {
        return this.canEmbedLinks(this.getApi().getYourself());
    }

    public boolean canReadMessageHistory(User var1);

    default public boolean canYouReadMessageHistory() {
        return this.canReadMessageHistory(this.getApi().getYourself());
    }

    public boolean canUseTts(User var1);

    default public boolean canYouUseTts() {
        return this.canUseTts(this.getApi().getYourself());
    }

    public boolean canAttachFiles(User var1);

    default public boolean canYouAttachFiles() {
        return this.canAttachFiles(this.getApi().getYourself());
    }

    public boolean canAddNewReactions(User var1);

    default public boolean canYouAddNewReactions() {
        return this.canAddNewReactions(this.getApi().getYourself());
    }

    public boolean canManageMessages(User var1);

    default public boolean canYouManageMessages() {
        return this.canManageMessages(this.getApi().getYourself());
    }

    default public boolean canRemoveReactionsOfOthers(User user) {
        return this.canManageMessages(user);
    }

    default public boolean canYouRemoveReactionsOfOthers() {
        return this.canRemoveReactionsOfOthers(this.getApi().getYourself());
    }

    public boolean canMentionEveryone(User var1);

    default public boolean canYouMentionEveryone() {
        return this.canMentionEveryone(this.getApi().getYourself());
    }

    default public Optional<? extends TextChannel> getCurrentCachedInstance() {
        return this.getApi().getTextChannelById(this.getId());
    }

    @Override
    default public CompletableFuture<? extends TextChannel> getLatestInstance() {
        Optional<? extends TextChannel> currentCachedInstance = this.getCurrentCachedInstance();
        if (currentCachedInstance.isPresent()) {
            return CompletableFuture.completedFuture(currentCachedInstance.get());
        }
        CompletableFuture result = new CompletableFuture();
        result.completeExceptionally(new NoSuchElementException());
        return result;
    }
}

