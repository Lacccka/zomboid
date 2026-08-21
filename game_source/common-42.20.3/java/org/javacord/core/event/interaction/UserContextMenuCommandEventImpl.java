/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.interaction;

import org.javacord.api.event.interaction.UserContextMenuCommandEvent;
import org.javacord.api.interaction.Interaction;
import org.javacord.core.event.EventImpl;

public class UserContextMenuCommandEventImpl
extends EventImpl
implements UserContextMenuCommandEvent {
    private final Interaction interaction;

    public UserContextMenuCommandEventImpl(Interaction interaction) {
        super(interaction.getApi());
        this.interaction = interaction;
    }

    @Override
    public Interaction getInteraction() {
        return this.interaction;
    }
}

