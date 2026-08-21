/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.UserContextMenu;
import org.javacord.api.interaction.internal.UserContextMenuUpdaterDelegate;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.interaction.ApplicationCommandUpdaterDelegateImpl;
import org.javacord.core.interaction.UserContextMenuImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;

public class UserContextMenuUpdaterDelegateImpl
extends ApplicationCommandUpdaterDelegateImpl<UserContextMenu>
implements UserContextMenuUpdaterDelegate {
    public UserContextMenuUpdaterDelegateImpl(long commandId) {
        this.commandId = commandId;
    }

    @Override
    public CompletableFuture<UserContextMenu> updateGlobal(DiscordApi api) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        this.prepareBody(body);
        return new RestRequest(api, RestMethod.PATCH, RestEndpoint.APPLICATION_COMMANDS).setUrlParameters(String.valueOf(api.getClientId()), String.valueOf(this.commandId)).setBody(body).execute(result -> new UserContextMenuImpl((DiscordApiImpl)api, result.getJsonBody()));
    }

    @Override
    public CompletableFuture<UserContextMenu> updateForServer(DiscordApi api, long server) {
        ObjectNode body = JsonNodeFactory.instance.objectNode();
        this.prepareBody(body);
        return new RestRequest(api, RestMethod.PATCH, RestEndpoint.SERVER_APPLICATION_COMMANDS).setUrlParameters(String.valueOf(api.getClientId()), String.valueOf(server), String.valueOf(this.commandId)).setBody(body).execute(result -> new UserContextMenuImpl((DiscordApiImpl)api, result.getJsonBody()));
    }
}

