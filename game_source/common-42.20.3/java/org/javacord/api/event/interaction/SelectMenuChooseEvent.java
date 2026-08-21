/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.interaction;

import java.util.Optional;
import org.javacord.api.event.Event;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.MessageComponentInteraction;
import org.javacord.api.interaction.SelectMenuInteraction;

public interface SelectMenuChooseEvent
extends Event {
    public Interaction getInteraction();

    default public SelectMenuInteraction getSelectMenuInteraction() {
        return this.getInteraction().asMessageComponentInteraction().get().asSelectMenuInteraction().get();
    }

    default public Optional<SelectMenuInteraction> getSelectMenuInteractionWithCustomId(String customId) {
        return this.getInteraction().asMessageComponentInteractionWithCustomId(customId).flatMap(MessageComponentInteraction::asSelectMenuInteraction);
    }
}

