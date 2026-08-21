/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.Optional;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ApplicationCommandPermissionType;

public interface ApplicationCommandPermissions {
    public long getId();

    public ApplicationCommandPermissionType getType();

    public boolean getPermission();

    public Optional<Role> getRole();

    public Optional<User> getUser();

    public Optional<ServerChannel> getChannel();

    public Server getServer();

    default public boolean affectsAllChannels() {
        return this.getId() == this.getServer().getId() - 1L;
    }

    default public boolean affectsEveryoneRole() {
        return this.getId() == this.getServer().getEveryoneRole().getId();
    }
}

