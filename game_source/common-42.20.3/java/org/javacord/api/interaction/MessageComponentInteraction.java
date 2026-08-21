/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.Optional;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.InteractionBase;
import org.javacord.api.interaction.MessageComponentInteractionBase;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.util.Specializable;

public interface MessageComponentInteraction
extends MessageComponentInteractionBase,
Specializable<InteractionBase> {
    default public Optional<ButtonInteraction> asButtonInteraction() {
        return this.as(ButtonInteraction.class);
    }

    default public Optional<SelectMenuInteraction> asSelectMenuInteraction() {
        return this.as(SelectMenuInteraction.class);
    }
}

