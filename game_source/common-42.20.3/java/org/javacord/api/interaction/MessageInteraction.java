/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.InteractionType;

public interface MessageInteraction
extends DiscordEntity {
    @Override
    default public DiscordApi getApi() {
        return this.getMessage().getApi();
    }

    public Message getMessage();

    public InteractionType getType();

    public String getName();

    public User getUser();
}

