/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.server;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.ApplicationCommandPermissionsUpdateEvent;
import org.javacord.api.interaction.ApplicationCommandPermissions;
import org.javacord.core.event.server.ServerEventImpl;

public class ApplicationCommandPermissionsUpdateEventImpl
extends ServerEventImpl
implements ApplicationCommandPermissionsUpdateEvent {
    private final long applicationId;
    private final long commandId;
    private final Set<ApplicationCommandPermissions> newPermissions;

    public ApplicationCommandPermissionsUpdateEventImpl(long applicationId, long commandId, Server server, Set<ApplicationCommandPermissions> newPermissions) {
        super(server);
        this.applicationId = applicationId;
        this.commandId = commandId;
        this.newPermissions = newPermissions;
    }

    @Override
    public long getApplicationId() {
        return this.applicationId;
    }

    @Override
    public Optional<Long> getCommandId() {
        return this.commandId != this.applicationId ? Optional.of(this.commandId) : Optional.empty();
    }

    @Override
    public Set<ApplicationCommandPermissions> getUpdatedPermissions() {
        return Collections.unmodifiableSet(this.newPermissions);
    }
}

