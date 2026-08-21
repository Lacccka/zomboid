/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.core.entity.channel;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Map;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Permissionable;
import org.javacord.api.entity.channel.internal.RegularServerChannelBuilderDelegate;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.core.entity.channel.ServerChannelBuilderDelegateImpl;
import org.javacord.core.entity.server.ServerImpl;

public class RegularServerChannelBuilderDelegateImpl
extends ServerChannelBuilderDelegateImpl
implements RegularServerChannelBuilderDelegate {
    private final Map<Long, Permissions> overwrittenUserPermissions = new HashMap<Long, Permissions>();
    private final Map<Long, Permissions> overwrittenRolePermissions = new HashMap<Long, Permissions>();
    protected Integer position = null;

    protected RegularServerChannelBuilderDelegateImpl(ServerImpl server) {
        super(server);
    }

    @Override
    public void setRawPosition(int rawPosition) {
        this.position = rawPosition;
    }

    @Override
    public <T extends Permissionable & DiscordEntity> void addPermissionOverwrite(T permissionable, Permissions permissions) {
        if (permissionable instanceof Role) {
            this.overwrittenRolePermissions.put(((DiscordEntity)permissionable).getId(), permissions);
        } else if (permissionable instanceof User) {
            this.overwrittenUserPermissions.put(((DiscordEntity)permissionable).getId(), permissions);
        }
    }

    @Override
    public <T extends Permissionable & DiscordEntity> void removePermissionOverwrite(T permissionable) {
        if (permissionable instanceof Role) {
            this.overwrittenRolePermissions.remove(((DiscordEntity)permissionable).getId());
        } else if (permissionable instanceof User) {
            this.overwrittenUserPermissions.remove(((DiscordEntity)permissionable).getId());
        }
    }

    @Override
    protected void prepareBody(ObjectNode body) {
        super.prepareBody(body);
        if (this.overwrittenUserPermissions.size() + this.overwrittenRolePermissions.size() > 0) {
            ArrayNode permissionOverwrites = body.putArray("permission_overwrites");
            for (Map.Entry<Long, Permissions> entry : this.overwrittenUserPermissions.entrySet()) {
                permissionOverwrites.addObject().put("id", Long.toUnsignedString(entry.getKey())).put("type", 1).put("allow", entry.getValue().getAllowedBitmask()).put("deny", entry.getValue().getDeniedBitmask());
            }
            for (Map.Entry<Long, Permissions> entry : this.overwrittenRolePermissions.entrySet()) {
                permissionOverwrites.addObject().put("id", Long.toUnsignedString(entry.getKey())).put("type", 0).put("allow", entry.getValue().getAllowedBitmask()).put("deny", entry.getValue().getDeniedBitmask());
            }
        }
    }
}

