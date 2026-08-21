/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.event.channel.server;

import java.util.Optional;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.server.ServerChannelChangeOverwrittenPermissionsEvent;
import org.javacord.core.event.channel.server.ServerChannelEventImpl;

public class ServerChannelChangeOverwrittenPermissionsEventImpl
extends ServerChannelEventImpl
implements ServerChannelChangeOverwrittenPermissionsEvent {
    private final Permissions newPermissions;
    private final Permissions oldPermissions;
    private final long entityId;
    private final DiscordEntity entity;

    public ServerChannelChangeOverwrittenPermissionsEventImpl(ServerChannel channel, Permissions newPermissions, Permissions oldPermissions, long entityId, DiscordEntity entity) {
        super(channel);
        this.newPermissions = newPermissions;
        this.oldPermissions = oldPermissions;
        this.entityId = entityId;
        this.entity = entity;
    }

    @Override
    public Permissions getNewPermissions() {
        return this.newPermissions;
    }

    @Override
    public Permissions getOldPermissions() {
        return this.oldPermissions;
    }

    @Override
    public long getEntityId() {
        return this.entityId;
    }

    @Override
    public Optional<DiscordEntity> getEntity() {
        return Optional.ofNullable(this.entity);
    }

    @Override
    public Optional<User> getUser() {
        if (this.entity instanceof User) {
            return Optional.of((User)this.entity);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Role> getRole() {
        if (this.entity instanceof Role) {
            return Optional.of((Role)this.entity);
        }
        return Optional.empty();
    }
}

