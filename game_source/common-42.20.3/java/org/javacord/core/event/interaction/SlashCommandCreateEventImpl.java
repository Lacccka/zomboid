/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.interaction;

import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.Interaction;
import org.javacord.core.event.EventImpl;

public class SlashCommandCreateEventImpl
extends EventImpl
implements SlashCommandCreateEvent {
    private final Interaction interaction;

    public SlashCommandCreateEventImpl(Interaction interaction) {
        super(interaction.getApi());
        this.interaction = interaction;
    }

    @Override
    public Interaction getInteraction() {
        return this.interaction;
    }
}

