/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.internal.SlashCommandUpdaterDelegate;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.interaction.ApplicationCommandUpdaterDelegateImpl;
import org.javacord.core.interaction.SlashCommandImpl;
import org.javacord.core.interaction.SlashCommandOptionImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class SlashCommandUpdaterDelegateImpl
extends ApplicationCommandUpdaterDelegateImpl<SlashCommand>
implements SlashCommandUpdaterDelegate {
    private List<SlashCommandOption> slashCommandOptions = new ArrayList<SlashCommandOption>();

    public SlashCommandUpdaterDelegateImpl(long commandId) {
        this.commandId = commandId;
    }

    @Override
    public void setOptions(List<SlashCommandOption> slashCommandOptions) {
        this.slashCommandOptions = slashCommandOptions;
    }

    @Override
    protected void prepareBody(ObjectNode body) {
        super.prepareBody(body);
        if (!this.slashCommandOptions.isEmpty()) {
            ArrayNode array = body.putArray("options");
            for (SlashCommandOption slashCommandOption : this.slashCommandOptions) {
                array.add(((SlashCommandOptionImpl)slashCommandOption).toJsonNode());
            }
        }
    }

    @Override
    public CompletableFuture<SlashCommand> updateGlobal(DiscordApi api) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        this.prepareBody(body);
        return new RestRequest(api, RestMethod.PATCH, RestEndpoint.APPLICATION_COMMANDS).setUrlParameters(String.valueOf(api.getClientId()), String.valueOf(this.commandId)).setBody(body).execute(result -> new SlashCommandImpl((DiscordApiImpl)api, result.getJsonBody()));
    }

    @Override
    public CompletableFuture<SlashCommand> updateForServer(DiscordApi api, long server) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        this.prepareBody(body);
        return new RestRequest(api, RestMethod.PATCH, RestEndpoint.SERVER_APPLICATION_COMMANDS).setUrlParameters(String.valueOf(api.getClientId()), String.valueOf(server), String.valueOf(this.commandId)).setBody(body).execute(result -> new SlashCommandImpl((DiscordApiImpl)api, result.getJsonBody()));
    }
}

