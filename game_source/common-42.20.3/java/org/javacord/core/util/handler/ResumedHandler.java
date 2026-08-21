/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.util.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.core.util.gateway.PacketHandler;

public class ResumedHandler
extends PacketHandler {
    public ResumedHandler(DiscordApi api) {
        super(api, false, "RESUMED");
    }

    @Override
    public void handle(JsonNode packet) {
    }
}

