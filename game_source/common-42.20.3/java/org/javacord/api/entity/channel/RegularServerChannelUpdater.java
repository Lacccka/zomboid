/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Permissionable;
import org.javacord.api.entity.channel.RegularServerChannel;
import org.javacord.api.entity.channel.ServerChannelUpdater;
import org.javacord.api.entity.channel.internal.RegularServerChannelUpdaterDelegate;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.util.internal.DelegateFactory;

public class RegularServerChannelUpdater<T extends RegularServerChannelUpdater<T>>
extends ServerChannelUpdater<T> {
    protected final RegularServerChannelUpdaterDelegate regularServerChannelUpdaterDelegate;

    protected RegularServerChannelUpdater(RegularServerChannelUpdaterDelegate regularServerChannelUpdaterDelegate) {
        super(regularServerChannelUpdaterDelegate);
        this.regularServerChannelUpdaterDelegate = regularServerChannelUpdaterDelegate;
    }

    public RegularServerChannelUpdater(RegularServerChannel channel) {
        this(DelegateFactory.createRegularServerChannelUpdaterDelegate(channel));
    }

    public T setRawPosition(int rawPosition) {
        this.regularServerChannelUpdaterDelegate.setRawPosition(rawPosition);
        return (T)this;
    }

    public <U extends Permissionable & DiscordEntity> T addPermissionOverwrite(U permissionable, Permissions permissions) {
        this.regularServerChannelUpdaterDelegate.addPermissionOverwrite(permissionable, permissions);
        return (T)this;
    }

    public <U extends Permissionable & DiscordEntity> T removePermissionOverwrite(U permissionable) {
        this.regularServerChannelUpdaterDelegate.removePermissionOverwrite(permissionable);
        return (T)this;
    }
}

