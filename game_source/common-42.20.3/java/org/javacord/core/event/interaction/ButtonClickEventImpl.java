/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.interaction;

import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.interaction.Interaction;
import org.javacord.core.event.EventImpl;

public class ButtonClickEventImpl
extends EventImpl
implements ButtonClickEvent {
    private final Interaction interaction;

    public ButtonClickEventImpl(Interaction interaction) {
        super(interaction.getApi());
        this.interaction = interaction;
    }

    @Override
    public Interaction getInteraction() {
        return this.interaction;
    }
}

