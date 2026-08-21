/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.javacord.api.interaction.InteractionBase;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.core.interaction.ExtendedInteractionMessageBuilderBaseImpl;
import org.javacord.core.interaction.InteractionImpl;
import org.javacord.core.interaction.InteractionOriginalResponseUpdaterImpl;

public class InteractionImmediateResponseBuilderImpl
extends ExtendedInteractionMessageBuilderBaseImpl<InteractionImmediateResponseBuilder>
implements InteractionImmediateResponseBuilder {
    private final InteractionImpl interaction;

    public InteractionImmediateResponseBuilderImpl(InteractionBase interaction) {
        super(InteractionImmediateResponseBuilder.class);
        this.interaction = (InteractionImpl)interaction;
    }

    @Override
    public CompletableFuture<InteractionOriginalResponseUpdater> respond() {
        CompletableFuture<InteractionOriginalResponseUpdater> future = new CompletableFuture<InteractionOriginalResponseUpdater>();
        CompletionStage job = ((CompletableFuture)this.delegate.sendInitialResponse(this.interaction).thenRun(() -> future.complete(new InteractionOriginalResponseUpdaterImpl(this.interaction, this.delegate)))).exceptionally(e -> {
            future.completeExceptionally((Throwable)e);
            return null;
        });
        return future;
    }
}

