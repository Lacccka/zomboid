/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler.user;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.core.util.gateway.PacketHandler;

public class PresencesReplaceHandler
extends PacketHandler {
    public PresencesReplaceHandler(DiscordApi api) {
        super(api, true, "PRESENCES_REPLACE");
    }

    @Override
    public void handle(JsonNode packet) {
    }
}

