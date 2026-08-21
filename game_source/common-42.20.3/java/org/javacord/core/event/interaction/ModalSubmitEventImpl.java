/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.interaction;

import org.javacord.api.event.interaction.ModalSubmitEvent;
import org.javacord.api.interaction.Interaction;
import org.javacord.core.event.EventImpl;

public class ModalSubmitEventImpl
extends EventImpl
implements ModalSubmitEvent {
    private final Interaction interaction;

    public ModalSubmitEventImpl(Interaction interaction) {
        super(interaction.getApi());
        this.interaction = interaction;
    }

    @Override
    public Interaction getInteraction() {
        return this.interaction;
    }
}

