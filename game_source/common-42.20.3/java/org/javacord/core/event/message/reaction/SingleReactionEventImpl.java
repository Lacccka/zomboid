/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.message.reaction;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.SingleReactionEvent;
import org.javacord.core.event.message.RequestableMessageEventImpl;

public abstract class SingleReactionEventImpl
extends RequestableMessageEventImpl
implements SingleReactionEvent {
    private final Emoji emoji;
    private final long userId;

    public SingleReactionEventImpl(DiscordApi api, long messageId, TextChannel channel, Emoji emoji, long userId) {
        super(api, messageId, channel);
        this.emoji = emoji;
        this.userId = userId;
    }

    @Override
    public Emoji getEmoji() {
        return this.emoji;
    }

    @Override
    public long getUserId() {
        return this.userId;
    }

    @Override
    public Optional<Reaction> getReaction() {
        return this.getMessage().flatMap(msg -> msg.getReactionByEmoji(this.getEmoji()));
    }

    @Override
    public CompletableFuture<Optional<Reaction>> requestReaction() {
        return this.requestMessage().thenApply(msg -> msg.getReactionByEmoji(this.getEmoji()));
    }

    @Override
    public Optional<Integer> getCount() {
        return this.getMessage().map(msg -> msg.getReactionByEmoji(this.getEmoji()).map(Reaction::getCount).orElse(0));
    }

    @Override
    public CompletableFuture<Integer> requestCount() {
        return this.requestMessage().thenApply(msg -> msg.getReactionByEmoji(this.getEmoji()).map(Reaction::getCount).orElse(0));
    }

    @Override
    public CompletableFuture<Set<User>> getUsers() {
        return Reaction.getUsers(this.getApi(), this.getChannel().getId(), this.getMessageId(), this.getEmoji());
    }
}

