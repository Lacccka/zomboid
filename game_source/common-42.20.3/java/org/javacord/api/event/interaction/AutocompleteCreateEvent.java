/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.interaction;

import org.javacord.api.event.interaction.ApplicationCommandEvent;
import org.javacord.api.interaction.AutocompleteInteraction;

public interface AutocompleteCreateEvent
extends ApplicationCommandEvent {
    default public AutocompleteInteraction getAutocompleteInteraction() {
        return this.getInteraction().asAutocompleteInteraction().get();
    }
}

