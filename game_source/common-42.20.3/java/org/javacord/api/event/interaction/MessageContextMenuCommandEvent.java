/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.interaction;

import java.util.Optional;
import org.javacord.api.event.interaction.ApplicationCommandEvent;
import org.javacord.api.interaction.MessageContextMenuInteraction;

public interface MessageContextMenuCommandEvent
extends ApplicationCommandEvent {
    default public MessageContextMenuInteraction getMessageContextMenuInteraction() {
        return this.getInteraction().asMessageContextMenuInteraction().get();
    }

    default public Optional<MessageContextMenuInteraction> getMessageContextMenuInteractionWithCommandId(long commandId) {
        return this.getInteraction().asMessageContextMenuInteractionWithCommandId(commandId);
    }
}

