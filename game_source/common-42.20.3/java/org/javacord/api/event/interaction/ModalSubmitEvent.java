/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.interaction;

import org.javacord.api.event.interaction.ApplicationCommandEvent;
import org.javacord.api.interaction.ModalInteraction;

public interface ModalSubmitEvent
extends ApplicationCommandEvent {
    default public ModalInteraction getModalInteraction() {
        return this.getInteraction().asModalInteraction().get();
    }
}

