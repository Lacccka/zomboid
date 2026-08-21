/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.interaction;

import java.util.Optional;
import org.javacord.api.event.Event;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.MessageComponentInteraction;

public interface MessageComponentCreateEvent
extends Event {
    public Interaction getInteraction();

    default public MessageComponentInteraction getMessageComponentInteraction() {
        return this.getInteraction().asMessageComponentInteraction().get();
    }

    default public Optional<MessageComponentInteraction> getMessageComponentInteractionWithCustomId(String customId) {
        return this.getInteraction().asMessageComponentInteractionWithCustomId(customId);
    }
}

