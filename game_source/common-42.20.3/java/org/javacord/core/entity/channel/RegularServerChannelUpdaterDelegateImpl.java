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
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.internal.RegularServerChannelUpdaterDelegate;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.core.entity.channel.RegularServerChannelImpl;
import org.javacord.core.entity.channel.ServerChannelUpdaterDelegateImpl;

public class RegularServerChannelUpdaterDelegateImpl
extends ServerChannelUpdaterDelegateImpl
implements RegularServerChannelUpdaterDelegate {
    protected final RegularServerChannel channel;
    protected Integer position = null;
    protected Map<Long, Permissions> overwrittenUserPermissions = null;
    protected Map<Long, Permissions> overwrittenRolePermissions = null;

    public RegularServerChannelUpdaterDelegateImpl(RegularServerChannel channel) {
        super(channel);
        this.channel = channel;
    }

    @Override
    public void setRawPosition(int rawPosition) {
        this.position = rawPosition;
    }

    @Override
    public <T extends Permissionable & DiscordEntity> void addPermissionOverwrite(T permissionable, Permissions permissions) {
        this.populatePermissionOverwrites();
        if (permissionable instanceof Role) {
            this.overwrittenRolePermissions.put(((DiscordEntity)permissionable).getId(), permissions);
        } else if (permissionable instanceof User) {
            this.overwrittenUserPermissions.put(((DiscordEntity)permissionable).getId(), permissions);
        }
    }

    @Override
    public <T extends Permissionable & DiscordEntity> void removePermissionOverwrite(T permissionable) {
        this.populatePermissionOverwrites();
        if (permissionable instanceof Role) {
            this.overwrittenRolePermissions.remove(((DiscordEntity)permissionable).getId());
        } else if (permissionable instanceof User) {
            this.overwrittenUserPermissions.remove(((DiscordEntity)permissionable).getId());
        }
    }

    @Override
    protected boolean prepareUpdateBody(ObjectNode body) {
        boolean patchChannel = super.prepareUpdateBody(body);
        if (this.position != null) {
            body.put("position", (int)this.position);
            patchChannel = true;
        }
        ArrayNode permissionOverwrites = null;
        if (this.overwrittenUserPermissions != null || this.overwrittenRolePermissions != null) {
            permissionOverwrites = body.putArray("permission_overwrites");
            patchChannel = true;
        }
        if (this.overwrittenUserPermissions != null) {
            for (Map.Entry<Long, Permissions> entry : this.overwrittenUserPermissions.entrySet()) {
                permissionOverwrites.addObject().put("id", Long.toUnsignedString(entry.getKey())).put("type", 1).put("allow", entry.getValue().getAllowedBitmask()).put("deny", entry.getValue().getDeniedBitmask());
            }
        }
        if (this.overwrittenRolePermissions != null) {
            for (Map.Entry<Long, Permissions> entry : this.overwrittenRolePermissions.entrySet()) {
                permissionOverwrites.addObject().put("id", Long.toUnsignedString(entry.getKey())).put("type", 0).put("allow", entry.getValue().getAllowedBitmask()).put("deny", entry.getValue().getDeniedBitmask());
            }
        }
        return patchChannel;
    }

    private void populatePermissionOverwrites() {
        if (this.overwrittenUserPermissions == null) {
            this.overwrittenUserPermissions = new HashMap<Long, Permissions>();
            this.overwrittenUserPermissions.putAll(((RegularServerChannelImpl)this.channel).getInternalOverwrittenUserPermissions());
        }
        if (this.overwrittenRolePermissions == null) {
            this.overwrittenRolePermissions = new HashMap<Long, Permissions>();
            this.overwrittenRolePermissions.putAll(((RegularServerChannelImpl)this.channel).getInternalOverwrittenRolePermissions());
        }
    }
}

