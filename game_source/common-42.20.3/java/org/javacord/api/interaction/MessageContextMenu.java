/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.interaction;

import java.util.EnumSet;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.ContextMenu;
import org.javacord.api.interaction.MessageContextMenuBuilder;
import org.javacord.api.interaction.MessageContextMenuUpdater;

public interface MessageContextMenu
extends ContextMenu {
    public static MessageContextMenuBuilder with(String name) {
        return (MessageContextMenuBuilder)new MessageContextMenuBuilder().setName(name);
    }

    public static MessageContextMenuBuilder withRequiredPermissions(String name, PermissionType ... requiredPermissions) {
        return (MessageContextMenuBuilder)((MessageContextMenuBuilder)new MessageContextMenuBuilder().setName(name)).setDefaultEnabledForPermissions(requiredPermissions);
    }

    public static MessageContextMenuBuilder withRequiredPermissions(String name, EnumSet<PermissionType> requiredPermissions) {
        return (MessageContextMenuBuilder)((MessageContextMenuBuilder)new MessageContextMenuBuilder().setName(name)).setDefaultEnabledForPermissions(requiredPermissions);
    }

    default public MessageContextMenuUpdater createMessageContextMenuUpdater() {
        return new MessageContextMenuUpdater(this.getId());
    }
}

