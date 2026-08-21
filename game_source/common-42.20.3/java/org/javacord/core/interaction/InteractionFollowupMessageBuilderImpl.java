/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.message.Message;
import org.javacord.api.interaction.InteractionBase;
import org.javacord.api.interaction.callback.InteractionFollowupMessageBuilder;
import org.javacord.core.interaction.ExtendedInteractionMessageBuilderBaseImpl;
import org.javacord.core.interaction.InteractionImpl;

public class InteractionFollowupMessageBuilderImpl
extends ExtendedInteractionMessageBuilderBaseImpl<InteractionFollowupMessageBuilder>
implements InteractionFollowupMessageBuilder {
    private final InteractionImpl interaction;

    public InteractionFollowupMessageBuilderImpl(InteractionBase interaction) {
        super(InteractionFollowupMessageBuilder.class);
        this.interaction = (InteractionImpl)interaction;
    }

    @Override
    public CompletableFuture<Message> send() {
        return this.delegate.sendFollowupMessage(this.interaction);
    }

    @Override
    public CompletableFuture<Message> update(long messageId) {
        return this.update(String.valueOf(messageId));
    }

    @Override
    public CompletableFuture<Message> update(String messageId) {
        return this.delegate.editFollowupMessage(this.interaction, messageId);
    }
}

