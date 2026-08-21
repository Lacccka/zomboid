/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.interaction;

import org.javacord.api.event.interaction.MessageContextMenuCommandEvent;
import org.javacord.api.interaction.Interaction;
import org.javacord.core.event.EventImpl;

public class MessageContextMenuCommandEventImpl
extends EventImpl
implements MessageContextMenuCommandEvent {
    private final Interaction interaction;

    public MessageContextMenuCommandEventImpl(Interaction interaction) {
        super(interaction.getApi());
        this.interaction = interaction;
    }

    @Override
    public Interaction getInteraction() {
        return this.interaction;
    }
}

