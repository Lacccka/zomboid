/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.interaction;

import java.util.Optional;
import org.javacord.api.event.Event;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.InteractionBase;
import org.javacord.api.interaction.MessageComponentInteraction;
import org.javacord.api.interaction.SlashCommandInteraction;

public interface InteractionCreateEvent
extends Event {
    public Interaction getInteraction();

    default public Optional<SlashCommandInteraction> getSlashCommandInteraction() {
        return this.getInteraction().asSlashCommandInteraction();
    }

    default public Optional<SlashCommandInteraction> getSlashCommandInteractionWithCommandId(long commandId) {
        return this.getInteraction().asSlashCommandInteractionWithCommandId(commandId);
    }

    default public Optional<MessageComponentInteraction> getMessageComponentInteraction() {
        return this.getInteraction().asMessageComponentInteraction();
    }

    default public Optional<MessageComponentInteraction> getMessageComponentInteractionWithCustomId(String customId) {
        return this.getInteraction().asMessageComponentInteractionWithCustomId(customId);
    }

    default public <T extends InteractionBase> Optional<T> getInteractionAs(Class<T> type) {
        return this.getInteraction().as(type);
    }
}

