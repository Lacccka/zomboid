/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.core.entity.server.ServerImpl;
import org.javacord.core.entity.user.MemberImpl;
import org.javacord.core.entity.user.UserImpl;
import org.javacord.core.util.gateway.PacketHandler;
import org.javacord.core.util.logging.LoggerUtil;

public class ReadyHandler
extends PacketHandler {
    private static final Logger logger = LoggerUtil.getLogger(ReadyHandler.class);

    public ReadyHandler(DiscordApi api) {
        super(api, false, "READY");
    }

    @Override
    public void handle(JsonNode packet) {
        this.api.purgeCache();
        JsonNode guilds = packet.get("guilds");
        for (JsonNode guildJson : guilds) {
            if (guildJson.has("unavailable") && guildJson.get("unavailable").asBoolean()) {
                this.api.addUnavailableServerToCache(guildJson.get("id").asLong());
                continue;
            }
            new ServerImpl(this.api, guildJson);
        }
        this.api.setYourself(new UserImpl(this.api, packet.get("user"), (MemberImpl)null, null));
    }
}

