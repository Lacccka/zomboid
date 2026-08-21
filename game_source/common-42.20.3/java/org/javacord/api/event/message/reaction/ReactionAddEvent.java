/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.message.reaction;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.event.message.reaction.SingleReactionEvent;

public interface ReactionAddEvent
extends SingleReactionEvent {
    public CompletableFuture<Void> removeReaction();
}

