/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.javacord.api.interaction.ApplicationCommandType;
import org.javacord.api.interaction.UserContextMenu;
import org.javacord.api.interaction.internal.UserContextMenuBuilderDelegate;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.interaction.ApplicationCommandBuilderDelegateImpl;
import org.javacord.core.interaction.UserContextMenuImpl;

public class UserContextMenuBuilderDelegateImpl
extends ApplicationCommandBuilderDelegateImpl<UserContextMenu>
implements UserContextMenuBuilderDelegate {
    @Override
    public ObjectNode getJsonBodyForApplicationCommand() {
        ObjectNode jsonBody = super.getJsonBodyForApplicationCommand();
        jsonBody.put("type", ApplicationCommandType.USER.getValue());
        return jsonBody;
    }

    @Override
    public UserContextMenu createInstance(DiscordApiImpl api, JsonNode jsonNode) {
        return new UserContextMenuImpl(api, jsonNode);
    }
}

