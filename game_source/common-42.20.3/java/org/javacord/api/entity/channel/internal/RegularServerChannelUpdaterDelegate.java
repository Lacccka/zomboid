/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel.internal;

import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Permissionable;
import org.javacord.api.entity.channel.internal.ServerChannelUpdaterDelegate;
import org.javacord.api.entity.permission.Permissions;

public interface RegularServerChannelUpdaterDelegate
extends ServerChannelUpdaterDelegate {
    public void setRawPosition(int var1);

    public <T extends Permissionable & DiscordEntity> void addPermissionOverwrite(T var1, Permissions var2);

    public <T extends Permissionable & DiscordEntity> void removePermissionOverwrite(T var1);
}

