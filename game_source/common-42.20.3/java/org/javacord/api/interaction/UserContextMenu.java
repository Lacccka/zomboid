/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.EnumSet;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.ContextMenu;
import org.javacord.api.interaction.UserContextMenuBuilder;
import org.javacord.api.interaction.UserContextMenuUpdater;

public interface UserContextMenu
extends ContextMenu {
    public static UserContextMenuBuilder with(String name) {
        return (UserContextMenuBuilder)new UserContextMenuBuilder().setName(name);
    }

    public static UserContextMenuBuilder withRequiredPermissions(String name, PermissionType ... requiredPermissions) {
        return (UserContextMenuBuilder)((UserContextMenuBuilder)new UserContextMenuBuilder().setName(name)).setDefaultEnabledForPermissions(requiredPermissions);
    }

    public static UserContextMenuBuilder withRequiredPermissions(String name, EnumSet<PermissionType> requiredPermissions) {
        return (UserContextMenuBuilder)((UserContextMenuBuilder)new UserContextMenuBuilder().setName(name)).setDefaultEnabledForPermissions(requiredPermissions);
    }

    default public UserContextMenuUpdater createUserContextMenuUpdater() {
        return new UserContextMenuUpdater(this.getId());
    }
}

