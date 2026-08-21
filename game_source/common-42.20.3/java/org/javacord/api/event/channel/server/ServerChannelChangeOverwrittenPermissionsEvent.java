/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.event.channel.server;

import java.util.Optional;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.server.ServerChannelEvent;

public interface ServerChannelChangeOverwrittenPermissionsEvent
extends ServerChannelEvent {
    public Permissions getNewPermissions();

    public Permissions getOldPermissions();

    public long getEntityId();

    default public boolean isUserEntity() {
        return !this.getRole().isPresent();
    }

    default public boolean isRoleEntity() {
        return this.getRole().isPresent();
    }

    public Optional<DiscordEntity> getEntity();

    public Optional<User> getUser();

    public Optional<Role> getRole();
}

