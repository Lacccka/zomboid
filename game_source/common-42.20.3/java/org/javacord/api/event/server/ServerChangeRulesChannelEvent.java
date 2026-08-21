/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import java.util.Optional;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.event.server.ServerEvent;

public interface ServerChangeRulesChannelEvent
extends ServerEvent {
    public Optional<ServerTextChannel> getOldRulesChannel();

    public Optional<ServerTextChannel> getNewRulesChannel();
}

