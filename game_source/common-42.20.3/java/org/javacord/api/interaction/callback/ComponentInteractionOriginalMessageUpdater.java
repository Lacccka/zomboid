/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction.callback;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.interaction.callback.ExtendedInteractionMessageBuilderBase;

public interface ComponentInteractionOriginalMessageUpdater
extends ExtendedInteractionMessageBuilderBase<ComponentInteractionOriginalMessageUpdater> {
    public CompletableFuture<Void> update();
}

