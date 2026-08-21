/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction.callback;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.message.Message;
import org.javacord.api.interaction.callback.ExtendedInteractionMessageBuilderBase;

public interface InteractionOriginalResponseUpdater
extends ExtendedInteractionMessageBuilderBase<InteractionOriginalResponseUpdater> {
    public CompletableFuture<Message> update();

    public CompletableFuture<Void> delete();
}

