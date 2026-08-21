/*
 * Decompiled with CFR 0.152.
 */
package org.javacord.api.entity.channel;

import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Permissionable;
import org.javacord.api.entity.channel.ServerChannelBuilder;
import org.javacord.api.entity.channel.internal.RegularServerChannelBuilderDelegate;
import org.javacord.api.entity.permission.Permissions;

public class RegularServerChannelBuilder<T>
extends ServerChannelBuilder<T> {
    protected final RegularServerChannelBuilderDelegate delegate;

    protected RegularServerChannelBuilder(Class<T> myClass, RegularServerChannelBuilderDelegate delegate) {
        super(myClass, delegate);
        this.delegate = delegate;
    }

    public T setRawPosition(int rawPosition) {
        this.delegate.setRawPosition(rawPosition);
        return this.myClass.cast(this);
    }

    public <U extends Permissionable & DiscordEntity> T addPermissionOverwrite(U permissionable, Permissions permissions) {
        this.delegate.addPermissionOverwrite(permissionable, permissions);
        return this.myClass.cast(this);
    }

    public <U extends Permissionable & DiscordEntity> T removePermissionOverwrite(U permissionable) {
        this.delegate.removePermissionOverwrite(permissionable);
        return this.myClass.cast(this);
    }
}

