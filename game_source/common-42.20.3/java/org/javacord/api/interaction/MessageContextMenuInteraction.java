/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import org.javacord.api.entity.message.Message;
import org.javacord.api.interaction.ApplicationCommandInteraction;

public interface MessageContextMenuInteraction
extends ApplicationCommandInteraction {
    public Message getTarget();
}

