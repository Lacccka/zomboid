/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.interaction;

import java.util.Optional;
import org.javacord.api.event.Event;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.MessageComponentInteraction;

public interface ButtonClickEvent
extends Event {
    public Interaction getInteraction();

    default public ButtonInteraction getButtonInteraction() {
        return this.getInteraction().asMessageComponentInteraction().get().asButtonInteraction().get();
    }

    default public Optional<ButtonInteraction> getButtonInteractionWithCustomId(String customId) {
        return this.getInteraction().asMessageComponentInteractionWithCustomId(customId).flatMap(MessageComponentInteraction::asButtonInteraction);
    }
}

