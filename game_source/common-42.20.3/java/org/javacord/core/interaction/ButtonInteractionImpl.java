/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.component.ComponentType;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.interaction.MessageComponentInteractionImpl;

public class ButtonInteractionImpl
extends MessageComponentInteractionImpl
implements ButtonInteraction {
    public ButtonInteractionImpl(DiscordApiImpl api, TextChannel channel, JsonNode jsonData) {
        super(api, channel, jsonData);
    }

    @Override
    public ComponentType getComponentType() {
        return ComponentType.BUTTON;
    }
}

