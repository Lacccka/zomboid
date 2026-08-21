/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageEvent;
import org.javacord.core.entity.emoji.UnicodeEmojiImpl;
import org.javacord.core.event.EventImpl;

public abstract class MessageEventImpl
extends EventImpl
implements MessageEvent {
    private final long messageId;
    private final TextChannel channel;

    public MessageEventImpl(Message message) {
        this(message.getApi(), message.getId(), message.getChannel());
    }

    public MessageEventImpl(DiscordApi api, long messageId, TextChannel channel) {
        super(api);
        this.messageId = messageId;
        this.channel = channel;
    }

    @Override
    public long getMessageId() {
        return this.messageId;
    }

    @Override
    public TextChannel getChannel() {
        return this.channel;
    }

    @Override
    public Optional<Server> getServer() {
        return this.getChannel().asServerChannel().map(ServerChannel::getServer);
    }

    @Override
    public CompletableFuture<Void> deleteMessage() {
        return this.deleteMessage(null);
    }

    @Override
    public CompletableFuture<Void> deleteMessage(String reason) {
        return Message.delete(this.getApi(), this.getChannel().getId(), this.getMessageId(), reason);
    }

    @Override
    public CompletableFuture<Message> editMessage(String content) {
        return Message.edit(this.getApi(), this.getChannel().getId(), this.getMessageId(), content);
    }

    @Override
    public CompletableFuture<Message> editMessage(List<EmbedBuilder> embeds) {
        return Message.edit(this.getApi(), this.getChannel().getId(), this.getMessageId(), null, embeds);
    }

    @Override
    public CompletableFuture<Message> editMessage(String content, List<EmbedBuilder> embeds) {
        return Message.edit(this.getApi(), this.getChannel().getId(), this.getMessageId(), content, embeds);
    }

    @Override
    public CompletableFuture<Void> addReactionToMessage(String unicodeEmoji) {
        return Message.addReaction(this.getApi(), this.getChannel().getId(), this.getMessageId(), unicodeEmoji);
    }

    @Override
    public CompletableFuture<Void> addReactionToMessage(Emoji emoji) {
        return Message.addReaction(this.getApi(), this.getChannel().getId(), this.getMessageId(), emoji);
    }

    @Override
    public CompletableFuture<Void> addReactionsToMessage(Emoji ... emojis) {
        return CompletableFuture.allOf((CompletableFuture[])Arrays.stream(emojis).map(this::addReactionToMessage).toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Void> addReactionsToMessage(String ... unicodeEmojis) {
        return this.addReactionsToMessage((Emoji[])Arrays.stream(unicodeEmojis).map(UnicodeEmojiImpl::fromString).toArray(Emoji[]::new));
    }

    @Override
    public CompletableFuture<Void> removeAllReactionsFromMessage() {
        return Message.removeAllReactions(this.getApi(), this.getChannel().getId(), this.getMessageId());
    }

    @Override
    public CompletableFuture<Void> removeReactionByEmojiFromMessage(User user, Emoji emoji) {
        return Reaction.removeUser(this.getApi(), this.getChannel().getId(), this.getMessageId(), emoji, user.getId());
    }

    @Override
    public CompletableFuture<Void> removeReactionByEmojiFromMessage(User user, String unicodeEmoji) {
        return this.removeReactionByEmojiFromMessage(user, UnicodeEmojiImpl.fromString(unicodeEmoji));
    }

    @Override
    public CompletableFuture<Void> removeReactionByEmojiFromMessage(Emoji emoji) {
        return Reaction.getUsers(this.getApi(), this.getChannel().getId(), this.getMessageId(), emoji).thenCompose(users -> CompletableFuture.allOf((CompletableFuture[])users.stream().map(user -> Reaction.removeUser(this.getApi(), this.getChannel().getId(), this.getMessageId(), emoji, user.getId())).toArray(CompletableFuture[]::new)));
    }

    @Override
    public CompletableFuture<Void> removeReactionByEmojiFromMessage(String unicodeEmoji) {
        return this.removeReactionByEmojiFromMessage(UnicodeEmojiImpl.fromString(unicodeEmoji));
    }

    @Override
    public CompletableFuture<Void> removeReactionsByEmojiFromMessage(User user, Emoji ... emojis) {
        return CompletableFuture.allOf((CompletableFuture[])Arrays.stream(emojis).map(emoji -> this.removeReactionByEmojiFromMessage(user, (Emoji)emoji)).toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Void> removeReactionsByEmojiFromMessage(User user, String ... unicodeEmojis) {
        return this.removeReactionsByEmojiFromMessage(user, (Emoji[])Arrays.stream(unicodeEmojis).map(UnicodeEmojiImpl::fromString).toArray(Emoji[]::new));
    }

    @Override
    public CompletableFuture<Void> removeReactionsByEmojiFromMessage(Emoji ... emojis) {
        return CompletableFuture.allOf((CompletableFuture[])Arrays.stream(emojis).map(this::removeReactionByEmojiFromMessage).toArray(CompletableFuture[]::new));
    }

    @Override
    public CompletableFuture<Void> removeReactionsByEmojiFromMessage(String ... unicodeEmojis) {
        return this.removeReactionsByEmojiFromMessage((Emoji[])Arrays.stream(unicodeEmojis).map(UnicodeEmojiImpl::fromString).toArray(Emoji[]::new));
    }

    @Override
    public CompletableFuture<Void> removeOwnReactionByEmojiFromMessage(Emoji emoji) {
        return this.removeReactionByEmojiFromMessage(this.getApi().getYourself(), emoji);
    }

    @Override
    public CompletableFuture<Void> removeOwnReactionByEmojiFromMessage(String unicodeEmoji) {
        return this.removeOwnReactionByEmojiFromMessage(UnicodeEmojiImpl.fromString(unicodeEmoji));
    }

    @Override
    public CompletableFuture<Void> removeOwnReactionsByEmojiFromMessage(Emoji ... emojis) {
        return this.removeReactionsByEmojiFromMessage(this.getApi().getYourself(), emojis);
    }

    @Override
    public CompletableFuture<Void> removeOwnReactionsByEmojiFromMessage(String ... unicodeEmojis) {
        return this.removeOwnReactionsByEmojiFromMessage((Emoji[])Arrays.stream(unicodeEmojis).map(UnicodeEmojiImpl::fromString).toArray(Emoji[]::new));
    }

    @Override
    public CompletableFuture<Void> pinMessage() {
        return Message.pin(this.getApi(), this.getChannel().getId(), this.getMessageId());
    }

    @Override
    public CompletableFuture<Void> unpinMessage() {
        return Message.unpin(this.getApi(), this.getChannel().getId(), this.getMessageId());
    }
}

