/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.Optional;
import org.javacord.api.interaction.AutocompleteInteraction;
import org.javacord.api.interaction.InteractionBase;
import org.javacord.api.interaction.MessageComponentInteraction;
import org.javacord.api.interaction.MessageContextMenuInteraction;
import org.javacord.api.interaction.ModalInteraction;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.UserContextMenuInteraction;
import org.javacord.api.util.Specializable;

public interface Interaction
extends InteractionBase,
Specializable<InteractionBase> {
    default public Optional<SlashCommandInteraction> asSlashCommandInteraction() {
        return this.as(SlashCommandInteraction.class);
    }

    default public Optional<SlashCommandInteraction> asSlashCommandInteractionWithCommandId(long commandId) {
        return this.asSlashCommandInteraction().filter(interaction -> interaction.getCommandId() == commandId);
    }

    default public Optional<AutocompleteInteraction> asAutocompleteInteraction() {
        return this.as(AutocompleteInteraction.class);
    }

    default public Optional<AutocompleteInteraction> asAutocompleteInteraction(long commandId) {
        return this.asAutocompleteInteraction().filter(autocompleteInteraction -> autocompleteInteraction.getCommandId() == commandId);
    }

    default public Optional<UserContextMenuInteraction> asUserContextMenuInteraction() {
        return this.as(UserContextMenuInteraction.class);
    }

    default public Optional<UserContextMenuInteraction> asUserContextMenuInteractionWithCommandId(long commandId) {
        return this.asUserContextMenuInteraction().filter(interaction -> interaction.getCommandId() == commandId);
    }

    default public Optional<MessageContextMenuInteraction> asMessageContextMenuInteraction() {
        return this.as(MessageContextMenuInteraction.class);
    }

    default public Optional<MessageContextMenuInteraction> asMessageContextMenuInteractionWithCommandId(long commandId) {
        return this.asMessageContextMenuInteraction().filter(interaction -> interaction.getCommandId() == commandId);
    }

    default public Optional<MessageComponentInteraction> asMessageComponentInteraction() {
        return this.as(MessageComponentInteraction.class);
    }

    default public Optional<MessageComponentInteraction> asMessageComponentInteractionWithCustomId(String customId) {
        return this.asMessageComponentInteraction().filter(interaction -> interaction.getCustomId().equals(customId));
    }

    default public Optional<ModalInteraction> asModalInteraction() {
        return this.as(ModalInteraction.class);
    }

    default public Optional<ModalInteraction> asModalInteractionWithCustomId(String customId) {
        return this.asModalInteraction().filter(interaction -> interaction.getCustomId().equals(customId));
    }
}

