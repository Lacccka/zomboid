/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.interaction;

import java.util.Optional;
import org.javacord.api.event.interaction.InteractionCreateEvent;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.interaction.MessageComponentInteraction;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.core.event.EventImpl;

public class InteractionCreateEventImpl
extends EventImpl
implements InteractionCreateEvent {
    private final Interaction interaction;

    public InteractionCreateEventImpl(Interaction interaction) {
        super(interaction.getApi());
        this.interaction = interaction;
    }

    @Override
    public Interaction getInteraction() {
        return this.interaction;
    }

    @Override
    public Optional<SlashCommandInteraction> getSlashCommandInteraction() {
        return this.interaction.asSlashCommandInteraction();
    }

    @Override
    public Optional<MessageComponentInteraction> getMessageComponentInteraction() {
        return this.interaction.asMessageComponentInteraction();
    }
}

