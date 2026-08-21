/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ApplicationCommandPermissionType;
import org.javacord.api.interaction.ApplicationCommandPermissions;

public class ApplicationCommandPermissionsImpl
implements ApplicationCommandPermissions {
    private final long id;
    private final Server server;
    private final ApplicationCommandPermissionType type;
    private final boolean permission;

    public ApplicationCommandPermissionsImpl(Server server, JsonNode data) {
        this.server = server;
        this.id = data.get("id").asLong();
        this.type = ApplicationCommandPermissionType.fromValue(data.get("type").asInt());
        this.permission = data.get("permission").asBoolean();
    }

    public ApplicationCommandPermissionsImpl(Server server, long id, ApplicationCommandPermissionType type, boolean permission) {
        this.server = server;
        this.id = id;
        this.type = type;
        this.permission = permission;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public ApplicationCommandPermissionType getType() {
        return this.type;
    }

    @Override
    public boolean getPermission() {
        return this.permission;
    }

    @Override
    public Optional<Role> getRole() {
        return this.type == ApplicationCommandPermissionType.ROLE ? this.server.getRoleById(this.id) : Optional.empty();
    }

    @Override
    public Optional<User> getUser() {
        return this.type == ApplicationCommandPermissionType.USER ? this.server.getMemberById(this.id) : Optional.empty();
    }

    @Override
    public Optional<ServerChannel> getChannel() {
        return this.type == ApplicationCommandPermissionType.CHANNEL ? (this.id != this.server.getId() - 1L ? this.server.getChannelById(this.id) : Optional.empty()) : Optional.empty();
    }

    @Override
    public Server getServer() {
        return this.server;
    }
}

