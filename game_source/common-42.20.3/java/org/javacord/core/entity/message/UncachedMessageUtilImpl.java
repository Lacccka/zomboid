/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.UncachedMessageUtil;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.message.MessageImpl;
import org.javacord.core.entity.message.embed.EmbedBuilderDelegateImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.listener.message.InternalUncachedMessageAttachableListenerManager;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class UncachedMessageUtilImpl
implements UncachedMessageUtil,
InternalUncachedMessageAttachableListenerManager {
    private final DiscordApiImpl api;

    public UncachedMessageUtilImpl(DiscordApiImpl api) {
        this.api = api;
    }

    @Override
    public CompletableFuture<Message> crossPost(String channelId, String messageId) {
        return new RestRequest(this.api, RestMethod.POST, RestEndpoint.MESSAGE).setUrlParameters(channelId, messageId, "crosspost").execute(result -> this.api.getOrCreateMessage(this.api.getTextChannelById(channelId).orElseThrow(() -> new IllegalStateException("TextChannel is missing.")), result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Void> delete(long channelId, long messageId) {
        return this.delete(channelId, messageId, null);
    }

    @Override
    public CompletableFuture<Void> delete(String channelId, String messageId) {
        return this.delete(channelId, messageId, (String)null);
    }

    @Override
    public CompletableFuture<Void> delete(long channelId, long messageId, String reason) {
        return new RestRequest(this.api, RestMethod.DELETE, RestEndpoint.MESSAGE_DELETE).setUrlParameters(Long.toUnsignedString(channelId), Long.toUnsignedString(messageId)).setAuditLogReason(reason).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> delete(String channelId, String messageId, String reason) {
        try {
            return this.delete(Long.parseLong(channelId), Long.parseLong(messageId), reason);
        }
        catch (NumberFormatException e) {
            CompletableFuture<Void> future = new CompletableFuture<Void>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<Void> delete(long channelId, long ... messageIds) {
        Instant twoWeeksAgo = Instant.now().minus(14L, ChronoUnit.DAYS);
        Map<Boolean, List<Long>> messageIdsByAge = Arrays.stream(messageIds).distinct().boxed().collect(Collectors.groupingBy(messageId -> DiscordEntity.getCreationTimestamp(messageId).isAfter(twoWeeksAgo)));
        AtomicInteger batchCounter = new AtomicInteger();
        return CompletableFuture.allOf((CompletableFuture[])Stream.concat(messageIdsByAge.getOrDefault(true, Collections.emptyList()).stream().collect(Collectors.groupingBy(messageId -> batchCounter.getAndIncrement() / 100)).values().stream().map(messageIdBatch -> {
            if (messageIdBatch.size() == 1) {
                return Message.delete((DiscordApi)this.api, channelId, (long)((Long)messageIdBatch.get(0)));
            }
            ObjectNode body = JsonNodeFactory.instance.objectNode();
            ArrayNode messages = body.putArray("messages");
            messageIdBatch.stream().map(Long::toUnsignedString).forEach(messages::add);
            return new RestRequest(this.api, RestMethod.POST, RestEndpoint.MESSAGES_BULK_DELETE).setUrlParameters(Long.toUnsignedString(channelId)).setBody(body).execute(result -> null);
        }), messageIdsByAge.getOrDefault(false, Collections.emptyList()).stream().map(messageId -> Message.delete((DiscordApi)this.api, channelId, (long)messageId))).toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Void> delete(String channelId, String ... messageIds) {
        long[] messageLongIds = Arrays.stream(messageIds).filter(s -> {
            try {
                Long.parseLong(s);
                return true;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }).mapToLong(Long::parseLong).toArray();
        return this.delete(Long.parseLong(channelId), messageLongIds);
    }

    @Override
    public CompletableFuture<Void> delete(Message ... messages) {
        return CompletableFuture.allOf((CompletableFuture[])Arrays.stream(messages).collect(Collectors.groupingBy(message -> message.getChannel().getId(), Collectors.mapping(DiscordEntity::getId, Collectors.toList()))).entrySet().stream().map(entry -> this.delete((long)((Long)entry.getKey()), ((List)entry.getValue()).stream().mapToLong(Long::longValue).toArray())).toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Void> delete(Iterable<Message> messages) {
        return this.delete((Message[])StreamSupport.stream(messages.spliterator(), false).toArray(Message[]::new));
    }

    @Override
    public CompletableFuture<Message> edit(long channelId, long messageId, String content) {
        return this.edit(channelId, messageId, content, true, Collections.emptyList(), false);
    }

    @Override
    public CompletableFuture<Message> edit(String channelId, String messageId, String content) {
        return this.edit(channelId, messageId, content, true, Collections.emptyList(), false);
    }

    @Override
    public CompletableFuture<Message> edit(long channelId, long messageId, List<EmbedBuilder> embeds) {
        return this.edit(channelId, messageId, null, false, embeds, true);
    }

    @Override
    public CompletableFuture<Message> edit(String channelId, String messageId, List<EmbedBuilder> embeds) {
        return this.edit(channelId, messageId, null, false, embeds, true);
    }

    @Override
    public CompletableFuture<Message> edit(long channelId, long messageId, String content, List<EmbedBuilder> embeds) {
        return this.edit(channelId, messageId, content, true, embeds, true);
    }

    @Override
    public CompletableFuture<Message> edit(String channelId, String messageId, String content, List<EmbedBuilder> embeds) {
        return this.edit(channelId, messageId, content, true, embeds, true);
    }

    @Override
    public CompletableFuture<Message> edit(long channelId, long messageId, String content, boolean updateContent, List<EmbedBuilder> embeds, boolean updateEmbed) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        if (updateContent) {
            if (content == null || content.isEmpty()) {
                body.putNull("content");
            } else {
                body.put("content", content);
            }
        }
        if (updateEmbed) {
            ArrayNode embedArray = body.putArray("embeds");
            embeds.stream().map(embedBuilder -> ((EmbedBuilderDelegateImpl)embedBuilder.getDelegate()).toJsonNode()).forEach(embedArray::add);
        }
        return new RestRequest(this.api, RestMethod.PATCH, RestEndpoint.MESSAGE).setUrlParameters(Long.toUnsignedString(channelId), Long.toUnsignedString(messageId)).setBody(body).execute(result -> new MessageImpl(this.api, this.api.getTextChannelById(channelId).orElseThrow(() -> new IllegalStateException("TextChannel is missing.")), result.getJsonBody()));
    }

    @Override
    public CompletableFuture<Message> edit(String channelId, String messageId, String content, boolean updateContent, List<EmbedBuilder> embeds, boolean updateEmbed) {
        try {
            return this.edit(Long.parseLong(channelId), Long.parseLong(messageId), content, true, embeds, true);
        }
        catch (NumberFormatException e) {
            CompletableFuture<Message> future = new CompletableFuture<Message>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<Message> removeContent(long channelId, long messageId) {
        return this.edit(channelId, messageId, "");
    }

    @Override
    public CompletableFuture<Message> removeContent(String channelId, String messageId) {
        return this.edit(channelId, messageId, null, true, Collections.emptyList(), false);
    }

    @Override
    public CompletableFuture<Message> removeEmbed(long channelId, long messageId) {
        return this.edit(channelId, messageId, null, false, Collections.emptyList(), true);
    }

    @Override
    public CompletableFuture<Message> removeEmbed(String channelId, String messageId) {
        return this.edit(channelId, messageId, null, false, Collections.emptyList(), true);
    }

    @Override
    public CompletableFuture<Message> removeContentAndEmbed(long channelId, long messageId) {
        return this.edit(channelId, messageId, null, true, Collections.emptyList(), true);
    }

    @Override
    public CompletableFuture<Message> removeContentAndEmbed(String channelId, String messageId) {
        return this.edit(channelId, messageId, null, true, Collections.emptyList(), true);
    }

    @Override
    public CompletableFuture<Void> addReaction(long channelId, long messageId, String unicodeEmoji) {
        return new RestRequest(this.api, RestMethod.PUT, RestEndpoint.REACTION).setUrlParameters(Long.toUnsignedString(channelId), Long.toUnsignedString(messageId), unicodeEmoji, "@me").execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> addReaction(String channelId, String messageId, String unicodeEmoji) {
        try {
            return this.addReaction(Long.parseLong(channelId), Long.parseLong(messageId), unicodeEmoji);
        }
        catch (NumberFormatException e) {
            CompletableFuture<Void> future = new CompletableFuture<Void>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<Void> addReaction(long channelId, long messageId, Emoji emoji) {
        String value = emoji.asUnicodeEmoji().orElseGet(() -> emoji.asCustomEmoji().map(CustomEmoji::getReactionTag).orElse("UNKNOWN"));
        return new RestRequest(this.api, RestMethod.PUT, RestEndpoint.REACTION).setUrlParameters(Long.toUnsignedString(channelId), Long.toUnsignedString(messageId), value, "@me").execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> addReaction(String channelId, String messageId, Emoji emoji) {
        try {
            return this.addReaction(Long.parseLong(channelId), Long.parseLong(messageId), emoji);
        }
        catch (NumberFormatException e) {
            CompletableFuture<Void> future = new CompletableFuture<Void>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<Void> removeAllReactions(long channelId, long messageId) {
        return new RestRequest(this.api, RestMethod.DELETE, RestEndpoint.REACTION).setUrlParameters(Long.toUnsignedString(channelId), Long.toUnsignedString(messageId)).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> removeAllReactions(String channelId, String messageId) {
        try {
            return this.removeAllReactions(Long.parseLong(channelId), Long.parseLong(messageId));
        }
        catch (NumberFormatException e) {
            CompletableFuture<Void> future = new CompletableFuture<Void>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<Void> pin(long channelId, long messageId) {
        return new RestRequest(this.api, RestMethod.PUT, RestEndpoint.PINS).setUrlParameters(Long.toUnsignedString(channelId), Long.toUnsignedString(messageId)).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> pin(String channelId, String messageId) {
        try {
            return this.pin(Long.parseLong(channelId), Long.parseLong(messageId));
        }
        catch (NumberFormatException e) {
            CompletableFuture<Void> future = new CompletableFuture<Void>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<Void> unpin(long channelId, long messageId) {
        return new RestRequest(this.api, RestMethod.DELETE, RestEndpoint.PINS).setUrlParameters(Long.toUnsignedString(channelId), Long.toUnsignedString(messageId)).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> unpin(String channelId, String messageId) {
        try {
            return this.unpin(Long.parseLong(channelId), Long.parseLong(messageId));
        }
        catch (NumberFormatException e) {
            CompletableFuture<Void> future = new CompletableFuture<Void>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<Set<User>> getUsersWhoReactedWithEmoji(long channelId, long messageId, Emoji emoji) {
        CompletableFuture<Set<User>> future = new CompletableFuture<Set<User>>();
        this.api.getThreadPool().getExecutorService().submit(() -> {
            try {
                String value = emoji.asUnicodeEmoji().orElseGet(() -> emoji.asCustomEmoji().map(CustomEmoji::getReactionTag).orElse("UNKNOWN"));
                ArrayList users = new ArrayList();
                boolean requestMore = true;
                while (requestMore) {
                    RestRequest<List> request = new RestRequest(this.api, RestMethod.GET, RestEndpoint.REACTION).setUrlParameters(Long.toUnsignedString(channelId), Long.toUnsignedString(messageId), value).addQueryParameter("limit", "100");
                    if (!users.isEmpty()) {
                        request.addQueryParameter("after", ((User)users.get(users.size() - 1)).getIdAsString());
                    }
                    List incompleteUsers = request.execute(result -> {
                        ArrayList<UserImpl> paginatedUsers = new ArrayList<UserImpl>();
                        for (JsonNode userJson : result.getJsonBody()) {
                            paginatedUsers.add(new UserImpl(this.api, userJson, (MemberImpl)null, null));
                        }
                        return Collections.unmodifiableList(paginatedUsers);
                    }).join();
                    users.addAll(incompleteUsers);
                    requestMore = incompleteUsers.size() >= 100;
                }
                future.complete(Collections.unmodifiableSet(new HashSet(users)));
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<Set<User>> getUsersWhoReactedWithEmoji(String channelId, String messageId, Emoji emoji) {
        try {
            return this.getUsersWhoReactedWithEmoji(Long.parseLong(channelId), Long.parseLong(messageId), emoji);
        }
        catch (NumberFormatException e) {
            CompletableFuture<Set<User>> future = new CompletableFuture<Set<User>>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public CompletableFuture<Void> removeUserReactionByEmoji(long channelId, long messageId, Emoji emoji, long userId) {
        String value = emoji.asUnicodeEmoji().orElseGet(() -> emoji.asCustomEmoji().map(CustomEmoji::getReactionTag).orElse("UNKNOWN"));
        return new RestRequest(this.api, RestMethod.DELETE, RestEndpoint.REACTION).setUrlParameters(Long.toUnsignedString(channelId), Long.toUnsignedString(messageId), value, this.api.getYourself().getId() == userId ? "@me" : String.valueOf(userId)).execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> removeUserReactionByEmoji(String channelId, String messageId, Emoji emoji, String userId) {
        try {
            return this.removeUserReactionByEmoji(Long.parseLong(channelId), Long.parseLong(messageId), emoji, Long.parseLong(userId));
        }
        catch (NumberFormatException e) {
            CompletableFuture<Void> future = new CompletableFuture<Void>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public DiscordApi getApi() {
        return this.api;
    }
}

