/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.interaction;

import java.util.Optional;
import org.javacord.api.event.interaction.ApplicationCommandEvent;
import org.javacord.api.interaction.SlashCommandInteraction;

public interface SlashCommandCreateEvent
extends ApplicationCommandEvent {
    default public SlashCommandInteraction getSlashCommandInteraction() {
        return this.getInteraction().asSlashCommandInteraction().get();
    }

    default public Optional<SlashCommandInteraction> getSlashCommandInteractionWithCommandId(long commandId) {
        return this.getInteraction().asSlashCommandInteractionWithCommandId(commandId);
    }
}

