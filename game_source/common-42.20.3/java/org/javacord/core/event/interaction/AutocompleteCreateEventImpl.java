/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.interaction;

import org.javacord.api.event.interaction.AutocompleteCreateEvent;
import org.javacord.api.interaction.Interaction;
import org.javacord.core.event.EventImpl;

public class AutocompleteCreateEventImpl
extends EventImpl
implements AutocompleteCreateEvent {
    private final Interaction interaction;

    public AutocompleteCreateEventImpl(Interaction interaction) {
        super(interaction.getApi());
        this.interaction = interaction;
    }

    @Override
    public Interaction getInteraction() {
        return this.interaction;
    }
}

