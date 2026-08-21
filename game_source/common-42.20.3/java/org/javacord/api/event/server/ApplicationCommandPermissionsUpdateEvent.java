/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.server;

import java.util.Optional;
import java.util.Set;
import org.javacord.api.event.server.ServerEvent;
import org.javacord.api.interaction.ApplicationCommandPermissions;

public interface ApplicationCommandPermissionsUpdateEvent
extends ServerEvent {
    public long getApplicationId();

    public Optional<Long> getCommandId();

    public Set<ApplicationCommandPermissions> getUpdatedPermissions();
}

