/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import org.javacord.api.interaction.ApplicationCommandType;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.internal.SlashCommandBuilderDelegate;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.interaction.ApplicationCommandBuilderDelegateImpl;
import org.javacord.core.interaction.SlashCommandImpl;
import org.javacord.core.interaction.SlashCommandOptionImpl;

public class SlashCommandBuilderDelegateImpl
extends ApplicationCommandBuilderDelegateImpl<SlashCommand>
implements SlashCommandBuilderDelegate {
    private List<SlashCommandOption> options = new ArrayList<SlashCommandOption>();

    @Override
    public void addOption(SlashCommandOption option) {
        this.options.add(option);
    }

    @Override
    public void setOptions(List<SlashCommandOption> options) {
        if (options == null) {
            this.options.clear();
        } else {
            this.options = new ArrayList<SlashCommandOption>(options);
        }
    }

    @Override
    public ObjectNode getJsonBodyForApplicationCommand() {
        ObjectNode jsonBody = super.getJsonBodyForApplicationCommand();
        if (!this.options.isEmpty()) {
            ArrayNode jsonOptions = jsonBody.putArray("options");
            this.options.stream().map(SlashCommandOptionImpl.class::cast).map(SlashCommandOptionImpl::toJsonNode).forEach(jsonOptions::add);
        }
        jsonBody.put("type", ApplicationCommandType.SLASH.getValue());
        return jsonBody;
    }

    @Override
    public SlashCommand createInstance(DiscordApiImpl api, JsonNode jsonNode) {
        return new SlashCommandImpl(api, jsonNode);
    }
}

