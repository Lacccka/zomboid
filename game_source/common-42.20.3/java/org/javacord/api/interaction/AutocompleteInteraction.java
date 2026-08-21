/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;

public interface AutocompleteInteraction
extends SlashCommandInteraction {
    public CompletableFuture<Void> respondWithChoices(List<SlashCommandOptionChoice> var1);

    public SlashCommandInteractionOption getFocusedOption();
}

