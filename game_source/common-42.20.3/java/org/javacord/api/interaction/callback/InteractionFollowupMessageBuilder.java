/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction.callback;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.message.Message;
import org.javacord.api.interaction.callback.ExtendedInteractionMessageBuilderBase;

public interface InteractionFollowupMessageBuilder
extends ExtendedInteractionMessageBuilderBase<InteractionFollowupMessageBuilder> {
    public CompletableFuture<Message> send();

    public CompletableFuture<Message> update(long var1);

    public CompletableFuture<Message> update(String var1);
}

