/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.message.reaction;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionEvent;
import org.javacord.api.event.user.OptionalUserEvent;

public interface SingleReactionEvent
extends ReactionEvent,
OptionalUserEvent {
    public Emoji getEmoji();

    public Optional<Reaction> getReaction();

    public CompletableFuture<Optional<Reaction>> requestReaction();

    public Optional<Integer> getCount();

    public CompletableFuture<Integer> requestCount();

    public CompletableFuture<Set<User>> getUsers();
}

