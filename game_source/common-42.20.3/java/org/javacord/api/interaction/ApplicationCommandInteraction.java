/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.Optional;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.InteractionBase;

public interface ApplicationCommandInteraction
extends InteractionBase {
    public long getCommandId();

    public String getCommandIdAsString();

    public String getCommandName();

    public Optional<Long> getRegisteredCommandServerId();

    default public Optional<Server> getRegisteredCommandServer() {
        return this.getRegisteredCommandServerId().flatMap(this.getApi()::getServerById);
    }
}

