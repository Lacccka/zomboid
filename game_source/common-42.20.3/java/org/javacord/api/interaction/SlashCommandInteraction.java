/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import org.javacord.api.interaction.ApplicationCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOptionsProvider;

public interface SlashCommandInteraction
extends ApplicationCommandInteraction,
SlashCommandInteractionOptionsProvider {
    public String getFullCommandName();
}

