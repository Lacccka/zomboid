/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.interaction;

import org.javacord.api.event.interaction.SelectMenuChooseEvent;
import org.javacord.api.interaction.Interaction;
import org.javacord.core.event.EventImpl;

public class SelectMenuChooseEventImpl
extends EventImpl
implements SelectMenuChooseEvent {
    private final Interaction interaction;

    public SelectMenuChooseEventImpl(Interaction interaction) {
        super(interaction.getApi());
        this.interaction = interaction;
    }

    @Override
    public Interaction getInteraction() {
        return this.interaction;
    }
}

