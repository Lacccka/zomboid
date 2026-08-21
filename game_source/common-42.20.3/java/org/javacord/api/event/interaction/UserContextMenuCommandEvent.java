/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.interaction;

import java.util.Optional;
import org.javacord.api.event.interaction.ApplicationCommandEvent;
import org.javacord.api.interaction.UserContextMenuInteraction;

public interface UserContextMenuCommandEvent
extends ApplicationCommandEvent {
    default public UserContextMenuInteraction getUserContextMenuInteraction() {
        return this.getInteraction().asUserContextMenuInteraction().get();
    }

    default public Optional<UserContextMenuInteraction> getUserContextMenuInteractionWithCommandId(long commandId) {
        return this.getInteraction().asUserContextMenuInteractionWithCommandId(commandId);
    }
}

