/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.interaction.MessageComponentInteraction;
import org.javacord.api.interaction.callback.ComponentInteractionOriginalMessageUpdater;
import org.javacord.core.interaction.ExtendedInteractionMessageBuilderBaseImpl;
import org.javacord.core.interaction.MessageComponentInteractionImpl;

public class ComponentInteractionOriginalMessageUpdaterImpl
extends ExtendedInteractionMessageBuilderBaseImpl<ComponentInteractionOriginalMessageUpdater>
implements ComponentInteractionOriginalMessageUpdater {
    private final MessageComponentInteractionImpl interaction;

    public ComponentInteractionOriginalMessageUpdaterImpl(MessageComponentInteraction interaction) {
        super(ComponentInteractionOriginalMessageUpdater.class);
        this.interaction = (MessageComponentInteractionImpl)interaction;
    }

    @Override
    public CompletableFuture<Void> update() {
        return this.delegate.updateOriginalMessage(this.interaction);
    }
}

