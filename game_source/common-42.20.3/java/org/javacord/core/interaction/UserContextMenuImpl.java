/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.interaction.ApplicationCommandType;
import org.javacord.api.interaction.UserContextMenu;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.interaction.ApplicationCommandImpl;

public class UserContextMenuImpl
extends ApplicationCommandImpl
implements UserContextMenu {
    public UserContextMenuImpl(DiscordApiImpl api, JsonNode data) {
        super(api, data);
    }

    @Override
    public ApplicationCommandType getType() {
        return ApplicationCommandType.USER;
    }
}

