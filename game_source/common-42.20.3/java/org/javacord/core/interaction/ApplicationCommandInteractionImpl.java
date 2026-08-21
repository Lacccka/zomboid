/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.interaction.ApplicationCommandInteraction;
import org.javacord.api.interaction.InteractionType;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.interaction.InteractionImpl;

public class ApplicationCommandInteractionImpl
extends InteractionImpl
implements ApplicationCommandInteraction {
    protected final long commandId;
    protected final String commandName;
    protected final Long registeredCommandServerId;

    public ApplicationCommandInteractionImpl(DiscordApiImpl api, TextChannel channel, JsonNode jsonData) {
        super(api, channel, jsonData);
        JsonNode data = jsonData.get("data");
        this.commandId = data.get("id").asLong();
        this.commandName = data.get("name").asText();
        this.registeredCommandServerId = data.hasNonNull("guild_id") ? Long.valueOf(data.get("guild_id").asLong()) : null;
    }

    @Override
    public InteractionType getType() {
        return InteractionType.APPLICATION_COMMAND;
    }

    @Override
    public long getCommandId() {
        return this.commandId;
    }

    @Override
    public String getCommandIdAsString() {
        return String.valueOf(this.commandId);
    }

    @Override
    public String getCommandName() {
        return this.commandName;
    }

    @Override
    public Optional<Long> getRegisteredCommandServerId() {
        return Optional.ofNullable(this.registeredCommandServerId);
    }
}

