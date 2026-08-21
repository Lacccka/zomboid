/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction.callback;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.interaction.callback.InteractionMessageBuilderBase;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

public interface InteractionImmediateResponseBuilder
extends InteractionMessageBuilderBase<InteractionImmediateResponseBuilder> {
    public CompletableFuture<InteractionOriginalResponseUpdater> respond();
}

