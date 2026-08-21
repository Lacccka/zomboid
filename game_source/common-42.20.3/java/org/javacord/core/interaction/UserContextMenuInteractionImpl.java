/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.UserContextMenuInteraction;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.interaction.ApplicationCommandInteractionImpl;

public class UserContextMenuInteractionImpl
extends ApplicationCommandInteractionImpl
implements UserContextMenuInteraction {
    private final User target;

    public UserContextMenuInteractionImpl(DiscordApiImpl api, TextChannel channel, JsonNode jsonData) {
        super(api, channel, jsonData);
        JsonNode data = jsonData.get("data");
        String targetId = data.get("target_id").asText();
        JsonNode userData = data.get("resolved").get("users").get(targetId);
        JsonNode memberData = data.get("resolved").get("members").get(targetId);
        ServerImpl server = (ServerImpl)this.getServer().orElseThrow(AssertionError::new);
        this.target = server.getMemberById(targetId).orElse(new UserImpl(api, userData, memberData, server));
    }

    @Override
    public User getTarget() {
        return this.target;
    }
}

