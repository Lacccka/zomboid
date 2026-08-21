/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.interaction.InteractionBase;
import org.javacord.api.interaction.callback.ComponentInteractionOriginalMessageUpdater;

public interface MessageComponentInteractionBase
extends InteractionBase {
    public Message getMessage();

    public String getCustomId();

    public ComponentType getComponentType();

    public CompletableFuture<Void> acknowledge();

    public ComponentInteractionOriginalMessageUpdater createOriginalMessageUpdater();
}

