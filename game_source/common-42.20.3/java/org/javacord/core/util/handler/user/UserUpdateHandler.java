/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.user;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.util.gateway.PacketHandler;

public class UserUpdateHandler
extends PacketHandler {
    public UserUpdateHandler(DiscordApi api) {
        super(api, true, "USER_UPDATE");
    }

    @Override
    public void handle(JsonNode packet) {
        this.api.setYourself(new UserImpl(this.api, packet, (MemberImpl)null, null));
    }
}

