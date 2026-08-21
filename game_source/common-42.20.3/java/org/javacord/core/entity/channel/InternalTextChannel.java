/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.webhook.IncomingWebhook;
import org.javacord.api.entity.webhook.Webhook;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.message.MessageSetImpl;
import org.javacord.core.entity.webhook.IncomingWebhookImpl;
import org.javacord.core.entity.webhook.WebhookImpl;
import org.javacord.core.listener.channel.InternalTextChannelAttachableListenerManager;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public interface InternalTextChannel
extends TextChannel,
InternalTextChannelAttachableListenerManager {
    @Override
    default public CompletableFuture<Void> type() {
        return new RestRequest(this.getApi(), RestMethod.POST, RestEndpoint.CHANNEL_TYPING).setUrlParameters(this.getIdAsString()).execute(result -> null);
    }

    @Override
    default public CompletableFuture<Void> bulkDelete(long ... messageIds) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        ArrayNode messages = body.putArray("messages");
        LongStream.of(messageIds).boxed().map(Long::toUnsignedString).forEach(messages::add);
        return new RestRequest(this.getApi(), RestMethod.POST, RestEndpoint.MESSAGES_BULK_DELETE).setUrlParameters(this.getIdAsString()).setBody(body).execute(result -> null);
    }

    @Override
    default public CompletableFuture<Message> getMessageById(long id) {
        return this.getApi().getCachedMessageById(id).filter(message -> message.getChannel().equals(this)).map(CompletableFuture::completedFuture).orElseGet(() -> new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.MESSAGE).setUrlParameters(this.getIdAsString(), Long.toUnsignedString(id)).execute(result -> ((DiscordApiImpl)this.getApi()).getOrCreateMessage(this, result.getJsonBody())));
    }

    @Override
    default public CompletableFuture<MessageSet> getPins() {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.PINS).setUrlParameters(this.getIdAsString()).execute(result -> {
            Collection pins = StreamSupport.stream(result.getJsonBody().spliterator(), false).map(pinJson -> ((DiscordApiImpl)this.getApi()).getOrCreateMessage(this, (JsonNode)pinJson)).collect(Collectors.toList());
            return new MessageSetImpl(pins);
        });
    }

    @Override
    default public CompletableFuture<MessageSet> getMessages(int limit) {
        return MessageSetImpl.getMessages(this, limit);
    }

    @Override
    default public CompletableFuture<MessageSet> getMessagesUntil(Predicate<Message> condition) {
        return MessageSetImpl.getMessagesUntil(this, condition);
    }

    @Override
    default public CompletableFuture<MessageSet> getMessagesWhile(Predicate<Message> condition) {
        return MessageSetImpl.getMessagesWhile(this, condition);
    }

    @Override
    default public Stream<Message> getMessagesAsStream() {
        return MessageSetImpl.getMessagesAsStream(this);
    }

    @Override
    default public CompletableFuture<MessageSet> getMessagesBefore(int limit, long before) {
        return MessageSetImpl.getMessagesBefore(this, limit, before);
    }

    @Override
    default public CompletableFuture<MessageSet> getMessagesBeforeUntil(Predicate<Message> condition, long before) {
        return MessageSetImpl.getMessagesBeforeUntil(this, condition, before);
    }

    @Override
    default public CompletableFuture<MessageSet> getMessagesBeforeWhile(Predicate<Message> condition, long before) {
        return MessageSetImpl.getMessagesBeforeWhile(this, condition, before);
    }

    @Override
    default public Stream<Message> getMessagesBeforeAsStream(long before) {
        return MessageSetImpl.getMessagesBeforeAsStream(this, before);
    }

    @Override
    default public CompletableFuture<MessageSet> getMessagesAfter(int limit, long after) {
        return MessageSetImpl.getMessagesAfter(this, limit, after);
    }

    @Override
    default public CompletableFuture<MessageSet> getMessagesAfterUntil(Predicate<Message> condition, long after) {
        return MessageSetImpl.getMessagesAfterUntil(this, condition, after);
    }

    @Override
    default public CompletableFuture<MessageSet> getMessagesAfterWhile(Predicate<Message> condition, long after) {
        return MessageSetImpl.getMessagesAfterWhile(this, condition, after);
    }

    @Override
    default public Stream<Message> getMessagesAfterAsStream(long after) {
        return MessageSetImpl.getMessagesAfterAsStream(this, after);
    }

    @Override
    default public CompletableFuture<MessageSet> getMessagesAround(int limit, long around) {
        return MessageSetImpl.getMessagesAround(this, limit, around);
    }

    @Override
    default public CompletableFuture<MessageSet> getMessagesAroundUntil(Predicate<Message> condition, long around) {
        return MessageSetImpl.getMessagesAroundUntil(this, condition, around);
    }

    @Override
    default public CompletableFuture<MessageSet> getMessagesAroundWhile(Predicate<Message> condition, long around) {
        return MessageSetImpl.getMessagesAroundWhile(this, condition, around);
    }

    @Override
    default public Stream<Message> getMessagesAroundAsStream(long around) {
        return MessageSetImpl.getMessagesAroundAsStream(this, around);
    }

    @Override
    default public CompletableFuture<MessageSet> getMessagesBetween(long from, long to) {
        return MessageSetImpl.getMessagesBetween(this, from, to);
    }

    @Override
    default public CompletableFuture<MessageSet> getMessagesBetweenUntil(Predicate<Message> condition, long from, long to) {
        return MessageSetImpl.getMessagesBetweenUntil(this, condition, from, to);
    }

    @Override
    default public CompletableFuture<MessageSet> getMessagesBetweenWhile(Predicate<Message> condition, long from, long to) {
        return MessageSetImpl.getMessagesBetweenWhile(this, condition, from, to);
    }

    @Override
    default public Stream<Message> getMessagesBetweenAsStream(long from, long to) {
        return MessageSetImpl.getMessagesBetweenAsStream(this, from, to);
    }

    @Override
    default public CompletableFuture<List<Webhook>> getWebhooks() {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.CHANNEL_WEBHOOK).setUrlParameters(this.getIdAsString()).execute(result -> {
            ArrayList<WebhookImpl> webhooks = new ArrayList<WebhookImpl>();
            for (JsonNode webhookJson : result.getJsonBody()) {
                webhooks.add(WebhookImpl.createWebhook(this.getApi(), webhookJson));
            }
            return Collections.unmodifiableList(webhooks);
        });
    }

    @Override
    default public CompletableFuture<List<Webhook>> getAllIncomingWebhooks() {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.CHANNEL_WEBHOOK).setUrlParameters(this.getIdAsString()).execute(result -> WebhookImpl.createAllIncomingWebhooksFromJsonArray(this.getApi(), result.getJsonBody()));
    }

    @Override
    default public CompletableFuture<List<IncomingWebhook>> getIncomingWebhooks() {
        return new RestRequest(this.getApi(), RestMethod.GET, RestEndpoint.CHANNEL_WEBHOOK).setUrlParameters(this.getIdAsString()).execute(result -> IncomingWebhookImpl.createIncomingWebhooksFromJsonArray(this.getApi(), result.getJsonBody()));
    }
}

